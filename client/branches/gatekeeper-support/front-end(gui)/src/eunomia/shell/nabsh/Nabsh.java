/*
 * Nabsh.java
 *
 * Created on September 16, 2006, 1:06 AM
 *
 */

package eunomia.shell.nabsh;

import bsh.BshMethod;
import bsh.ConsoleInterface;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.Variable;
import eunomia.util.Util;
import eunomia.core.managers.ReceptorManager;
import eunomia.core.receptor.Receptor;
import eunomia.gui.MainGui;
import eunomia.gui.interfaces.Exiter;
import eunomia.shell.Shell;
import eunomia.shell.ShellHistory;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Nabsh extends JPanel implements ActionListener, Runnable, ConsoleInterface, KeyListener, Exiter, Shell {
    private static String PROMPT_REMOTE = " R> ", PROMPT_LOCAL = " >> ", PROMPT_LINE_REQUEST = " |> ";
    
    private Interpreter bsh;
    private JTextField entry;
    private JPasswordField passE;
    private JLabel prompt;
    private Thread thread;
    private Object lock;
    private ShellHistory history;
    private Receptor modeReceptor;
    private JPanel entryPanel;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(Nabsh.class);
    }
    
    public Nabsh() {
        bsh = new Interpreter(this);
        history = new ShellHistory();
        try {
            String initScript = new String(Util.catFile(ClassLoader.getSystemResource("scripts/commands.sh").toURI()));
            bsh.eval(initScript);
            bsh.set("logger", logger);
            bsh.set("sh", this);
            bsh.unset("bsh");
            history.load("Nabsh");
        } catch (Exception e){
            e.printStackTrace();
        }
        lock = new Object();
        thread = new Thread(this);
        thread.start();
        
        MainGui.v().addExiter(this);
        
        addControls();
    }

    public void execFile(String txt) throws EvalError {
        bsh.eval(txt);
    }

    public Component getComponent(){
        return this;
    }

    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == entry || o == passE){
            synchronized(lock){
                lock.notify();
            }
        }
    }
    
    public void execute(String cmd) throws EvalError {
        history.addItem(cmd);
        logger.info(cmd);
        if(cmd.equals("m?")) {
            listCommands();
        } else if(cmd.equals("v?")) {
            listVariables();
        } else if(cmd.startsWith(".")){
            executeRemote(cmd.substring(1));
        } else if(modeReceptor != null){
            execRemoteCommand(modeReceptor, cmd);
        } else {
            bsh.eval(cmd);
        }
    }
    
    private void executeRemote(String cmd){
        int index = cmd.indexOf(' ');
        if(index != -1){
            String rec = cmd.substring(0, index);
            Receptor receptor = ReceptorManager.ins.getByName(rec);
            if(receptor != null){
                String command = cmd.substring(index + 1);
                execRemoteCommand(receptor, command);
            } else {
                logger.error("Receptor " + rec + " not found");
            }
        } else {
            Receptor newMode = ReceptorManager.ins.getByName(cmd);
            if(newMode == null){
                logger.error("Receptor " + cmd + " not found");
            } else if(modeReceptor == newMode){
                modeReceptor = null;
                setPrompt(Nabsh.PROMPT_LOCAL);
                logger.info("Returning to local execution");
            } else {
                modeReceptor = newMode;
                setPrompt(newMode + Nabsh.PROMPT_REMOTE);
                logger.info("Remote mode to receptor: " + modeReceptor);
            }
        }
    }
    
    private void execRemoteCommand(Receptor rec, String cmd){
        if(rec.isConnected()){
            rec.getOutComm().executeCommand(cmd);
        } else {
            logger.error("Not connected to: " + rec);
        }
    }
    
    private void listCommands() {
        StringBuilder builder = new StringBuilder("Methods:\n");
        BshMethod[] methods = bsh.getNameSpace().getMethods();
        for (int i = 0; i < methods.length; i++) {
            builder.append("\t" + methods[i] + "\n");
        }
        
        print(builder);
    }

    private void listVariables() {
        StringBuilder builder = new StringBuilder("Variables:\n");
        Variable[] variables = bsh.getNameSpace().getDeclaredVariables();
        for (int i = 0; i < variables.length; i++) {
            builder.append("\t" + variables[i] + "\n");
        }
        
        print(builder);
    }

    public void run() {
        while(true) {
            waitForInput();
            
            String cmd = entry.getText();
            if(cmd.equals("")){
                continue;
            }
            
            entry.setEditable(false);
            try {
                execute(cmd);
            } catch (EvalError ex) {
                logger.error(ex.getMessage());
            }
            entry.setEditable(true);
            entry.setText("");
        }
    }
    
    private void waitForInput() {
        synchronized(lock){
            try {
                lock.wait();
            } catch (InterruptedException ex) {
            }
        }
    }

    private void addControls() {
        this.setLayout(new BorderLayout());
        
        entryPanel = new JPanel(new BorderLayout());
        
        entryPanel.add(entry = new JTextField());
        passE = new JPasswordField();
        
        add(entryPanel);
        add(prompt = new JLabel(), BorderLayout.WEST);
        
        entry.addActionListener(this);
        entry.addKeyListener(this);
        passE.addActionListener(this);
        
        entry.setBorder(BorderFactory.createEmptyBorder());
        entry.setFont(MainGui.log_font);
        entry.setForeground(MainGui.sh_text_color);
        entry.setBackground(MainGui.sh_color);
        entry.setCaretColor(MainGui.sh_text_color);
        
        passE.setBorder(BorderFactory.createEmptyBorder());
        passE.setFont(MainGui.log_font);
        passE.setBackground(MainGui.sh_pass_color);
        passE.setForeground(MainGui.sh_text_color);
        passE.setCaretColor(MainGui.sh_text_color);

        prompt.setBorder(BorderFactory.createEmptyBorder());
        prompt.setFont(MainGui.log_font);
        prompt.setForeground(MainGui.sh_text_color);
        prompt.setBackground(MainGui.sh_color);
        
        prompt.setOpaque(true);
        setPrompt(Nabsh.PROMPT_LOCAL);
    }

    public Reader getIn() {
        return new InputStreamReader(System.in);
    }

    public PrintStream getOut() {
        return System.out;
    }

    public PrintStream getErr() {
        return System.err;
    }

    public void println(Object object) {
        System.out.println(object);
    }

    public void print(Object object) {
        System.out.print(object);
    }

    public void error(Object object) {
        System.err.println(object);
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_UP){
            entry.setText(history.getOlder());
        } else if(e.getKeyCode() == KeyEvent.VK_DOWN){
            entry.setText(history.getNewer());
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void startExitSequence() {
        try {
            history.save("Nabsh");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getLine() {
        return getLine(PROMPT_LINE_REQUEST, null, true);
    }
    
    public String getLine(String varName, String desc){
        return getLine(varName + PROMPT_LINE_REQUEST, desc, true);
    }
    
    public String getPasswordLine(String varName, String desc) {
        return getLine(varName + PROMPT_LINE_REQUEST, desc, false);
    }
    
    public void setPrompt(String str){
        prompt.setText(str);
    }
    
    private void setEntryComponent(JComponent comp){
        entryPanel.removeAll();
        entryPanel.add(comp);
        entryPanel.revalidate();
        entryPanel.repaint();
        comp.grabFocus();
    }

    public String getLine(String prpt, String description, boolean showtext) {
        String pmt = prompt.getText();
        String lineInput;
        setPrompt(prpt);
        
        if(showtext) {
            entry.setEditable(true);
            entry.setText("");
            entry.setToolTipText(description);
        } else {
            setEntryComponent(passE);
            passE.setText("");
            passE.setToolTipText(description);
        }
        
        waitForInput();
        
        if(showtext){ 
            entry.setToolTipText(null);
            lineInput = entry.getText();
            entry.setEditable(false);
        } else {
            lineInput = new String(passE.getPassword());
            passE.setText("");
            passE.setToolTipText(null);
            setEntryComponent(entry);
        }
        
        setPrompt(pmt);
        
        return lineInput;
    }
}