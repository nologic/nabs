/*
 * FlowModule.java
 *
 * Created on June 19, 2006, 10:05 PM
 *
 */

package com.vivic.eunomia.module.flow;

import com.vivic.eunomia.module.EunomiaModule;
import com.vivic.eunomia.filter.FilterEntry;
import com.vivic.eunomia.module.frontend.FilterEntryEditor;
import com.vivic.eunomia.module.receptor.FlowCreator;
import eunomia.messages.FilterEntryMessage;

/**
 *
 * <p> The FlowModule interface defines the methods for the Main class of
 *     the <b>Flow Producer</b> module. The main class should be located
 *     in package: <br><br>
 *
 *     <code>eunomia.module.receptor.flow.[module name]</code>
 *
 * <p> The main class should is used to obtain information about the module
 *     and to get instances of its components. Depending on where the module
 *     is used (Sieve or Console) different components will be retrieved.
 *
 * @author Mikhail Sosonkin
 */
public interface FlowModule extends EunomiaModule {
    /**
     * Returns a new instance of a Flow.
     *
     * This method should create a new Object that implements the Flow interface
     * for this module and return it. Note, that many times the Flow object will
     * be reused within Eunomia, so this method should return only new instances
     * and not reuse older ones.
     *
     * @return Flow instance for this module.
     */
    public Flow getNewFlowInstance();

    /**
     * Returns an instance of FlowCreator.
     *
     * The FlowCreator is an Object that will actually parse the data coming down
     * the pipe from sensors. It is possible to return the same instance every time
     *
     * @return FlowCreator instance
     */
    public FlowCreator getNewFlowCreatorInstance();

    /**
     * Returns a FilterEntry of the type for this module.
     *
     * The Object returned must be new, not reused. This object will represent a
     * single filter entry based on the FilterEntryMessage. In particular it should
     * focus on the data specific to the module.
     *
     * @param fem a message that represents an entry in serialized form.
     *
     * @return FilterEntry object
     */
    public FilterEntry getNewFilterEntry(FilterEntryMessage fem);

    /**
     * Returns a FilterEntryEditor object for this module (new or reused).
     *
     * The filter editor is used by the Console. It will be displayed to the user
     * for access to module specific filter fields. This component needs to be
     * Swing compatible IU component.
     *
     * @return instance of a filter editor.
     */
    public FilterEntryEditor getFilterEditor();

    /**
     * Returns an array of classes used for serializing specific components of the
     * filter entry.
     *
     * Each FilterEntry reserves space for module specific data. That data will be
     * stored as a object, the class for that object needs to be specified to the
     * system. All classes that may be used for such purpose must be included in
     * this array.
     *
     * @return Array of classes.
     */
    public Class[] getFilterMessageClassList();
}