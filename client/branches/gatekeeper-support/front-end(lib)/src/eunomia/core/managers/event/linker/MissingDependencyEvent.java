/*
 * MissingDependencyEvent.java
 *
 * Created on July 9, 2007, 9:35 PM
 *
 */

package eunomia.core.managers.event.linker;

import eunomia.core.receptor.Receptor;
import com.vivic.eunomia.module.Dependency;

/**
 *
 * @author Mikhail Sosonkin
 */
public class MissingDependencyEvent extends ModuleLinkerEvent {
    private Dependency dependency;
    
    public MissingDependencyEvent(Receptor rec) {
        super(rec);
    }

    public Dependency getDependency() {
        return dependency;
    }

    public void setDependency(Dependency dependency) {
        this.dependency = dependency;
    }
    
}
