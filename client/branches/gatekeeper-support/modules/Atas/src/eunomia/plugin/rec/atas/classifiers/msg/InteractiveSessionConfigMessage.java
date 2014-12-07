/*
 * InteractiveSessionConfigMessage.java
 *
 * Created on April 11, 2007, 9:27 PM
 *
 */

package eunomia.plugin.rec.atas.classifiers.msg;

import eunomia.plugin.com.atas.ClassifierConfigurationMessage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class InteractiveSessionConfigMessage implements ClassifierConfigurationMessage {
    private int roleNumber;
    private String roleName;
    private double alpha;
    private double beta;
    private double epsilon;
    
    public InteractiveSessionConfigMessage() {
    }

    public String getClassName() {
        return "InteractiveSessionParticipants";
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(roleNumber);
        out.writeObject(roleName);
        out.writeDouble(alpha);
        out.writeDouble(beta);
        out.writeDouble(epsilon);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        roleNumber = in.readInt();
        roleName = (String)in.readObject();
        alpha = in.readDouble();
        beta = in.readDouble();
        epsilon = in.readDouble();
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public int getRoleNumber() {
        return roleNumber;
    }

    public void setRoleNumber(int roleNumber) {
        this.roleNumber = roleNumber;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }
    
}
