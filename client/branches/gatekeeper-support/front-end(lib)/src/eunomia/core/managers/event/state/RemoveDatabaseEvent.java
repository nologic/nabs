/*
 * RemoveDatabaseEvent.java
 *
 * Created on May 15, 2007, 10:56 PM
 *
 */

package eunomia.core.managers.event.state;

import eunomia.core.receptor.Receptor;
import eunomia.messages.DatabaseDescriptor;

/**
 *
 * @author Mikhail Sosonkin
 */
public class RemoveDatabaseEvent extends ReceptorStateEvent {
    private DatabaseDescriptor database;
    
    public RemoveDatabaseEvent(Receptor r) {
        super(r);
    }

    public DatabaseDescriptor getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseDescriptor database) {
        this.database = database;
    }
}