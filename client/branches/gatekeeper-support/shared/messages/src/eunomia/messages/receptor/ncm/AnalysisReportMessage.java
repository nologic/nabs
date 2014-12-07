/*
 * AnalysisReportMessage.java
 *
 * Created on December 5, 2006, 10:51 PM
 *
 */

package eunomia.messages.receptor.ncm;

import eunomia.messages.ByteArrayMessage;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.messages.receptor.NoCauseMessage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AnalysisReportMessage implements NoCauseMessage {
    private ModuleHandle handle;
    private ByteArrayMessage report;
    
    public AnalysisReportMessage() {
        report = new ByteArrayMessage();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(handle);
        out.writeObject(report);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        handle = (ModuleHandle)in.readObject();
        report = (ByteArrayMessage)in.readObject();
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public ModuleHandle getHandle() {
        return handle;
    }

    public void setHandle(ModuleHandle handle) {
        this.handle = handle;
    }

    public ByteArrayMessage getReport() {
        return report;
    }

    public void setReport(ByteArrayMessage report) {
        this.report = report;
    }
    
    public DataOutputStream getReportOutputStream() {
        return new DataOutputStream(report.getOutputStream());
    }
    
    public DataInputStream getReportInputStream() {
        return new DataInputStream(report.getInputStream());
    }
}