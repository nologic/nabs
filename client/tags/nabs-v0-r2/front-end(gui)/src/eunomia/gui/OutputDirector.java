/*
 * OutputDirector.java
 *
 * Created on June 7, 2005, 7:12 PM
 */

package eunomia.gui;

import java.io.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class OutputDirector extends OutputStream implements Runnable {
    private StringBuilder build;
    private OutputStream stdOut;
    private JTextArea text;
    private Document doc;
    private boolean doStdOut;
    private int txtLimit;
    private int limitDiff;
    
    public OutputDirector(boolean doOut) {
        stdOut = System.out;
        build = new StringBuilder();
        doStdOut = doOut;
        new Thread(this, "Output Director").start();
    }
    
    public OutputDirector(){
        this(true);
    }
    
    public void setTxtOptions(int limit, int lDiff){
        txtLimit = limit;
        limitDiff = lDiff;
    }
    
    public void setTextArea(JTextArea area){
        text = area;
        doc = area.getDocument();
    }
    
    public void write(int b) throws IOException {
        synchronized(build){
            build.append((char)b);
        }
        if(doStdOut){
            stdOut.write(b);
        }
    }

    public void run() {
        while(true){
            try {
                if(text != null && build.length() != 0){
                    String str = "";
                    synchronized(build){
                        str = build.toString();
                        build.delete(0, str.length());
                    }
                    int len = doc.getLength();
                    doc.insertString(len, str, null);
                    text.setCaretPosition(len + str.length());
                    
                    len = doc.getLength();
                    if(len >= txtLimit){
                        int toRem = limitDiff + (len - txtLimit);
                        doc.remove(0, toRem);
                    }
                }
                
                Thread.sleep(50);
            } catch(Exception e){
            }
        }
    }
}