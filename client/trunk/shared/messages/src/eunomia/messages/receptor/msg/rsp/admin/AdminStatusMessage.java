/*
 * AdminStatusMessahe.java
 *
 * Created on April 23, 2007, 8:52 PM
 *
 */

package eunomia.messages.receptor.msg.rsp.admin;

import eunomia.messages.receptor.msg.cmd.admin.AdminMessage;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AdminStatusMessage extends AdminMessage {
    private String[] users;
    private List modules;
    
    public AdminStatusMessage() {
        modules = new ArrayList();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(users.length);
        for (int i = 0; i < users.length; i++) {
            out.writeObject(users[i]);
        }
        
        out.writeInt(modules.size());
        for (int i = 0; i < modules.size(); i++) {
            Object o = modules.get(i);
            out.writeObject(o);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int size = in.readInt();
        users = new String[size];
        for (int i = 0; i < users.length; i++) {
            users[i] = (String)in.readObject();
        }
        
        size = in.readInt();
        for (int i = 0; i < size; i++) {
            Object o = in.readObject();
            modules.add(o);
        }
    }
    
    public void addModule(int type, String name, String desc) {
        ModDesc mod = new ModDesc();
        
        mod.setType(type);
        mod.setDescription(desc);
        mod.setName(name);
        
        modules.add(mod);
    }
    
    public List getModules() {
        return modules;
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public String[] getUsers() {
        return users;
    }

    public void setUsers(String[] users) {
        this.users = users;
    }
    
    public static class ModDesc implements Externalizable {
        private int type;
        private String name;
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(type);
            out.writeObject(name);
            out.writeObject(description);
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            type = in.readInt();
            name = (String)in.readObject();
            description = (String)in.readObject();
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }
}