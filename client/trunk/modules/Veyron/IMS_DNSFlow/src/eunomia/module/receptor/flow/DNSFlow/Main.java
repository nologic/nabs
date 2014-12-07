package eunomia.module.receptor.flow.DNSFlow;

import com.vivic.eunomia.module.flow.FlowModule;
import com.vivic.eunomia.module.flow.Flow;
import com.vivic.eunomia.module.receptor.FlowCreator;
import com.vivic.eunomia.filter.FilterEntry;
import eunomia.messages.FilterEntryMessage;
import com.vivic.eunomia.module.frontend.FilterEntryEditor;
import eunomia.module.receptor.flow.DNSFlow.messages.DNSFlowSpecificMessage;

/**
 *
 * @author justin
 */

public class Main implements FlowModule {
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
    public Flow getNewFlowInstance() {
        return new DNSFlow();
    }

    /**
     * Returns an instance of FlowCreator.
     *
     * The FlowCreator is an Object that will actually parse the data coming down
     * the pipe from sensors. It is possible to return the same instance every time
     *
     * @return FlowCreator instance
     */
    public FlowCreator getNewFlowCreatorInstance() {
        return new DNSFlowCreator();
    }

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
    public FilterEntry getNewFilterEntry(FilterEntryMessage fem) {
        return new DNSFilterEntry(fem);
    }

    /**
     * Returns a FilterEntryEditor object for this module (new or reused).
     *
     * The filter editor is used by the Console. It will be displayed to the user
     * for access to module specific filter fields. This component needs to be
     * Swing compatible IU component.
     *
     * @return instance of a filter editor.
     */
    public FilterEntryEditor getFilterEditor() {
        // TODO
        return null;
    }

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
    public Class[] getFilterMessageClassList() {
        return new Class[] {
            DNSFlowSpecificMessage.class
        };
    }
}