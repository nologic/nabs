import org.apache.log4j.Logger;
import eunomia.shell.nabsh.Nabsh;
import eunomia.shell.Shell;

Logger logger;
Shell sh;

lsRec() {
    import eunomia.core.managers.ReceptorManager;
    import java.util.Iterator;

    Iterator it = ReceptorManager.ins.getReceptors().iterator();
    while(it.hasNext()){
        System.out.println(it.next());
    }
}

connect(String rec, String user){
    connect(rec, user, null);
}

connect(String rec, String user, String pass){
    import eunomia.core.managers.ReceptorManager;
    import eunomia.core.receptor.Receptor;

    Receptor r = ReceptorManager.ins.getByName(rec);
    if(r == null){
        System.out.print("Receptor not found: " + rec);
    } else {
        if(pass == null) {
            pass = sh.getPasswordLine("Password", "Enter password for user '" + user + "' on receptor '" + r);
        }

        r.setCredentials(user, pass);
        logger.info(r + " -> Connecting...");
        try {
            r.connect();
        } catch (Exception e){
            logger.info(e.getMessage());
        }
    }
}

disconnect(String rec){
    import eunomia.core.managers.ReceptorManager;
    import eunomia.core.receptor.Receptor;

    Receptor r = ReceptorManager.ins.getByName(rec);
    if(r == null){
        print("Receptor not found: " + rec);
    } else {
        r.disconnect();
    }
}

su(String rec){
    su(rec, null);
}

su(String rec, String pass){
    import eunomia.core.receptor.Receptor;
    import eunomia.core.managers.ReceptorManager;

    Receptor r = ReceptorManager.ins.getByName(rec);
    if(r == null){
        print("Receptor not found: " + rec);
    } else {
        if(pass == null){
            pass = sh.getPasswordLine("Password", "Enter password for user '" + user + "' on receptor '" + r);
        }
        r.autheticateRoot(pass);
    }
}

addUser(String rec, String user){
    import eunomia.core.receptor.Receptor;
    import eunomia.core.managers.ReceptorManager;

    Receptor r = ReceptorManager.ins.getByName(rec);
    if(r == null){
        print("Receptor not found: " + rec);
    } else {
        String pass = sh.getPasswordLine("Password", "Enter password for user '" + user + "' on receptor '" + r);
        r.getOutComm().addUser(user, pass);
    }
}

removeUser(String rec, String user){
    import eunomia.core.receptor.Receptor;
    import eunomia.core.managers.ReceptorManager;

    Receptor r = ReceptorManager.ins.getByName(rec);
    if(r == null){
        print("Receptor not found: " + rec);
    } else {
        r.getOutComm().removeUser(user);
    }
}

exit(){
    System.exit(0);
}