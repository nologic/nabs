package eunomia.plugin.rec.pieChart;

import eunomia.messages.Message;
import eunomia.messages.module.ModuleMessage;
import eunomia.plugin.interfaces.ReceptorModule;
import eunomia.flow.Filter;
import eunomia.flow.Flow;
import eunomia.flow.FlowProcessor;
import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.receptor.module.interfaces.FlowModule;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.log4j.Logger;

/*
 * StreamPieData.java
 *
 * Created on June 14, 2005, 5:40 PM
 */

/**
 * This is the main class used by the GUI to instantiate the module. The class name
 * will be generated by concatenating the prefix "eunomia.plugin.rec.", the name 
 * of the module (pieChart in this case) and postfix ".Main"
 * @author Mikhail Sosonkin
 */
public class Main implements FlowProcessor, ReceptorModule {
    /**
     * Maintains the count of each type of flow.
     */
    private long[] counts;
    
    /**
     * Maintains the aged value of each type.
     */
    private double[] percents;
    
    /**
     * The total amount of flows that have passed though this module instance since
     * the last resets
     */
    private long total;
    
    /**
     * Filter that is used to select only the desired flows.
     */
    private Filter filter;
    
    /**
     * Flag for processing the flows. If set then the flows will be processed, 
     * otherwise the flows are ignored but still received by the module.
     */
    private boolean doProc;
    
    /**
     * The last time this module was reset, if never then the time this module was
     * instantiated.
     */
    private long lastReset;
    
    /**
     * The aging coefficient.
     */
    private double ageCoef;

    private static Logger logger;
    
    static {
        logger = Logger.getLogger(Main.class);
    }
    
    public Main() {
        // set some initial values.
        filter = new Filter();
        doProc = true;
        ageCoef = 1.0;
        counts = new long[NABFlow.NUM_TYPES];
        percents = new double[NABFlow.NUM_TYPES];
        // the last reset happened now.
        lastReset = System.currentTimeMillis();
    }
    
    /**
     * Changes module properties. Primarily used for intermodule communication.
     * @param name Property
     * @param value 
     */
    public void setProperty(String name, Object value) {
    }
    
    /**
     * Obtains module's property.
     * @param name 
     * @return Does not have to be the same as the one set by <I>setProperty</I>
     */
    public Object getProperty(String name) {
        // this module has no properties.
        return null;
    }
    
    /**
     * Method is part of the FlowProcessor interface. It will be called by the
     * middleware automatically when the new flow is generated.
     * @param flow Generic flow object.
     */
    public void newFlow(Flow flow) {
        // check if we want to process the flow.
        // Flows are always received until the module is terminated.
        if(doProc){
            if(filter != null && !filter.allow(flow)){
                // The filter rejected the flow. This module does not process it
                // if that happens. Overall it is up to the developer to decide on
                // how the filter should be applied and what its result means.
                return;
            }

            // This module only works on NABFlow type of the module.
            // Everything else is assumed to not have a compatible 
            // the data type field.
            if(flow instanceof NABFlow){
                // In order for this to work the NABFlow module must be linked in
                // At the moment there is no enforcement. The assumtion is made that
                // the user knows to link in the dependancy modules.
                NABFlow nFlow = (NABFlow)flow;
                
                // calculate the statistics on the Flow specific data.
                ++total;
                if(total % 8 == 0){
                    for(int i = percents.length - 1; i != -1; --i){
                        // Age the values.
                        percents[i] *= ageCoef;
                    }
                }
                
                // 'type' and 'Size' are NABFlow specific values.
                ++percents[nFlow.getType()];
                counts[nFlow.getType()] += nFlow.getSize();
            }
        }
    }
    
    /**
     * Sets a new filter
     * @param f The new filter selected by the user on the Front-end. Filter transfer will be
     * done by Eunomia automatically.
     */
    public void setFilter(Filter f) {
        filter = f;
    }

    /**
     * 
     * @return The filter user by the module. Can be null.
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * The OutputStream should contains the module configuration data after 
     * them method has returned.
     * @param out Stream for sending the data from the module.
     * @throws java.io.IOException 
     */
    public void getControlData(OutputStream out) throws IOException {
        DataOutputStream dout = new DataOutputStream(out);
        
        // write out the coefficient value to be sent to the front-end.
        dout.writeDouble(ageCoef);
    }

    /**
     * All flows are sent to the modules serially. This method does not need to be 
     * thread-safe.
     * @return the object that can process flows for this modules. 
     */
    public FlowProcessor getFlowProcessor() {
        // This class is also the flow processor.
        return this;
    }

    /**
     * Reset the counters
     */
    public void reset() {
        // clear the counters.
        // NOTE: this is not synchronized, so some counters might not agree. this is
        //  negligeble for large amounts of flows.
        lastReset = System.currentTimeMillis();
        for(int i = counts.length - 1; i != -1; --i){
            counts[i] = 0;
            percents[i] = 0.0;
        }
    }

    /**
     * This data comes from the front-end's part of the module. The format is dictated 
     * by the module developer.
     * @param in The InputSteam will contain data for configuring the module instance
     * @throws java.io.IOException 
     */
    public void setControlData(InputStream in) throws IOException {
        DataInputStream din = new DataInputStream(in);
        
        ageCoef = din.readDouble();
    }

    public void start() {
        doProc = true;
    }

    public void stop() {
        doProc = false;
    }

    /**
     * Obtain the state of the module instance.
     * @param out All the data should be written to the output stream in the format that it is
     * expected by the front-end side.
     * @throws java.io.IOException 
     */
    public void updateStatus(OutputStream out) throws IOException {
        DataOutputStream dout = new DataOutputStream(out);
        
        // All the data is sent to the front-end.
        dout.writeLong(lastReset);
        for(int i = counts.length - 1; i != -1; --i){
            dout.writeDouble(percents[i]);
            dout.writeLong(counts[i]);
        }
    }
    
    /**
     * Receives messages sent to the module.
     * @param msg Any type of Message.
     * @throws java.io.IOException 
     * @return returns the result that will be sent back to the <I>msg</I> originator.
     */
    public Message processMessage(ModuleMessage msg) throws IOException {
        return null;
    }

    public void initialize() {
    }

    public void destroy() {
    }

    public Object[] getCommands() {
        return null;
    }

    public Object executeCommand(Object command, Object[] parameters) {
        return null;
    }

    /**
     * Will be called by the middleware to findout whether or not the flows from 
     * <I>module</I> can be processed by this module.
     * @param module The flow module in question.
     * @return True if the flow is allowed by the module.
     */
    public boolean accept(FlowModule module) {
        // at this point the module agrees to accept all the flow types.
        return true;
    }
}