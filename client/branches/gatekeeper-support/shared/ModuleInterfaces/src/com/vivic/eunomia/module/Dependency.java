package com.vivic.eunomia.module;

/**
 * This class is used to specify more dependencies. For example, the PieChart
 * flow processing module depends on NABFlow flow module. So it will specify that
 * to the system:
 * <CODE>
 * <PRE>
 *    public Dependency[] getDependencies() {
 *        return new Dependency[]{new Dependency("NABFlow", Descriptor.TYPE_FLOW), 
 *                                new Dependency("NABFlowV2", Descriptor.TYPE_FLOW),
 *                                new Dependency("jcommon", Descriptor.TYPE_LIBB),
 *                                new Dependency("jfreechart", Descriptor.TYPE_LIBB)};
 *    }
 * </PRE>
 * </CODE>
 * @author Mikhail Sosonkin
 */
public class Dependency {
    private String name;
    private int type;
    private String key;
    
    /**
     * 
     * @param name Name of the dependency module.
     * @param type Type of the dependency module.
     */
    public Dependency(String name, int type) {
        this.name = name;
        this.type = type;
        
        key = name + type;
    }
    
    public int getType() {
        return type;
    }
    
    public String getName() {
        return name;
    }
    
    public int hashCode() {
        return key.hashCode();
    }
    
    public boolean equals(Object o) {
        return key.equals(o);
    }
}