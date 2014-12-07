/*
 * Descriptor.java
 *
 * Created on October 19, 2006, 10:52 PM
 *
 */

package com.vivic.eunomia.module;

/**
 * <p>Every module must implement this interface by a Descriptor class. The module must
 * define the class in the <CODE>eunomia</CODE> package. The class's full name
 * is: eunomia.Descriptor. The module will be loaded only if this class is defined.
 *
 * <p>An example of a Descriptor class, this one is defined in the Stream Status
 * module:
 *
 * <PRE>
 * package eunomia;
 *
 * import com.vivic.eunomia.module.Dependency;
 *
 * public class Descriptor implements com.vivic.eunomia.module.Descriptor {
 *    public String moduleName() {
 *        return "streamStatus";
 *    }
 *
 *    public int moduleType() {
 *        return com.vivic.eunomia.module.Descriptor.TYPE_PROC;
 *    }
 *
 *    public String shortDescription() {
 *        return "Stream Status";
 *    }
 *
 *    public String longDescription() {
 *        return "Tracks simple flow statistics.";
 *    }
 *
 *    public int version() {
 *        return 0;
 *    }
 *
 *    public Dependency[] getDependencies() {
 *        return null;
 *    }
 * }
 * </PRE>
 * @author Mikhail Sosonkin
 */
public interface Descriptor {

    /**
     * Flow processor.
     */
    public static final int TYPE_PROC = 0;

    /**
     * Flow producer.
     */
    public static final int TYPE_FLOW = 1;

    /**
     * Static analysis
     */
    public static final int TYPE_ANLZ = 2;

    /**
     * Flow collection.
     */
    public static final int TYPE_COLL = 3;
    /**
     * Library package or 3rd party library.
     */
    public static final int TYPE_LIBB = 4;

    /**
     * Returns the internal name of the module.
     *
     * This method identifies the module's name. The name used by Eunomia to
     * refer to the module. Depending on how the console uses the name it may be
     * displayed to the user. However, this is the name as it appears in the java
     * package for the module's name space. So, it must follow the restrictions
     * imposed on the java package names. It is advised that a 2 word concatenation
     * is used (i.e. streamStatus). The reasoning is that this name will be used in
     * error messages and it would be useful to have something meaningful. Also,
     * with a longer names it becomes less likely to have name conflicts with other
     * modules.
     * @return module name.
     */
    public String moduleName();

    /**
     * Returns the module type.
     *
     * The existence of the Descriptor class identifies a jar file as a Eunomia module.
     * This method defines the type of the module. This could be any number between
     * 0 and 4 (inclusive). The numbers represent TYPE_PROC, TYPE_FLOW, TYPE_ANLZ,
     * TYPE_COLL and TYPE_LIBB constants.
     * @return Module Type
     */
    public int moduleType();

    /**
     * Returns an extended module description.
     *
     * This method needs to be defined but it's value is free form and optional. It is
     * there to tell the users something about the module. The description can be as
     * long as neccessary and has no formatting requirements. When displayed to the
     * user, it may be reformated or truncated depending on the console.
     * @return Module description String
     */
    public String longDescription();

    /**
     * Returns a brief module description.
     *
     * This method is the same as the longDescription method, the only difference is
     * that it is only used for a short (one-liner) description of the module. Again,
     * there are no restrictions on the content, however it is advised that the content
     * is limited to one sentense. This description will be displayed on the command
     * line to show what kind of a module was loaded on both Console and Sieve.
     * @return Module description string.
     */
    public String shortDescription();

    /**
     * Returns module version.
     *
     * Currently, Eunomia does not fully support different versions of the same module
     * and only one module (the 1st) of the same name will loaded. This method defines
     * the module's version. It is mostly a place holder for future functions, however
     * some modules may choose to use it for their purposes.
     * @return Module version number
     */
    public int version();

    /**
     * Returns the module dependency list.
     *
     * Modules may be compiled to reference other modules directly, through method
     * calls, etc. So, the module's class loader will need to be able to locate those
     * classes. This method defines the dependency list. For description on how to
     * define dependencies please read: DependencyClass. It is not neccessary to
     * define sub-dependencies. The framework will recursively read the entire
     * dependency graph and link all required modules. It is also important to note
     * that circular dependecies are allowed. If there are no dependencies then this
     * function can return either a null or an array of zero elements.
     * @return Dependency array
     */
    public Dependency[] getDependencies();
}
