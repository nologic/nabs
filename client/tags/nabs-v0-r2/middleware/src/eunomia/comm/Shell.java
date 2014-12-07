/*
 * shell.java
 *
 * Created on November 10, 2006, 6:18 PM
 *
 */

package eunomia.comm;

import bsh.ConsoleInterface;
import bsh.EvalError;
import bsh.Interpreter;
import eunomia.messages.receptor.ncm.ShellLineMessage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Shell extends OutputStream implements ConsoleInterface {
    private Interpreter bsh;
    private ClientComm clientComm;
    private StringBuilder output;
    private PrintStream pStream;
    private ShellLineMessage slm;
    
    public Shell(ClientComm cc) {
        clientComm = cc;
        slm = new ShellLineMessage();
        pStream = new PrintStream(this);
        output = new StringBuilder();
        bsh = new Interpreter(this);
    }
    
    public void execute(String cmd){
        try {
            bsh.eval(cmd);
        } catch (EvalError ex) {
            sendLine(ex.getMessage());
        }
    }
    
    public void sendLine(String line){
        slm.setLine(line);
        try {
            clientComm.sendMessage(slm);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public Reader getIn() {
        return null;
    }

    public PrintStream getOut() {
        return pStream;
    }

    public PrintStream getErr() {
        return pStream;
    }

    public void println(Object object) {
        sendLine(object.toString());
    }

    public void print(Object object) {
        sendLine(object.toString());
    }

    public void error(Object object) {
        sendLine(object.toString());
    }

    public void write(int b) throws IOException {
        if(b == '\n'){
            sendLine(output.toString());
            output.delete(0, output.length());
        } else {
            output.append((char)b);
        }
        
        if(output.length() > 127){
            sendLine(output.toString());
            output.delete(0, output.length());
        }
    }
}
