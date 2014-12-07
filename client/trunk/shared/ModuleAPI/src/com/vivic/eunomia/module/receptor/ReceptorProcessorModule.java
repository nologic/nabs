/*
 * ReceptorModule.java
 *
 * Created on October 23, 2005, 5:44 PM
 *
 */

package com.vivic.eunomia.module.receptor;

import com.vivic.eunomia.module.EunomiaModule;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Sieve supports many different module types. The modules
 * will need to implement specific interfaces in order to play their roles. One type
 * is the <B>Flow Processor Module</B>. <CODE>{@link ReceptorProcessorModule}</CODE> 
 * interface defines the functions required for the Sieve side of the module.<br><br>
 * 
 * ReceptorProcessorModule is expected to be implemented by class:<br>
 * <CODE>eunomia.module.receptor.proc.[Module Name].Main</CODE><br><br>
 * 
 * The flow processor modules can be instantiated many times on the Sieve. As a 
 * rule each module is loaded in a separate class loader, however the same 
 * classloader is used for the different instances. This means that the module 
 * can be instantiated several times but the static space is shared. There can 
 * be multiple Console connections at the same time so the module must be aware that
 * requests can come from the different sources. Module author should be careful
 * when making assumptions about what state the Console side of the module is.<br><br>
 * 
 * The following is an example of the Flow Processor, it simply tracks of the rate
 * and total size of flows:
 * 
 * <PRE>
 * /*
 * * StreamStatus.java
 * *
 * * Created on June 14, 2005, 7:55 PM
 * * /
 * 
 * package eunomia.plugin.rec.streamStatus;
 * 
 * import eunomia.messages.Message;
 * import eunomia.messages.module.ModuleMessage;
 * import com.vivic.eunomia.filter.Filter;
 * import com.vivic.eunomia.module.flow.FlowModule;
 * import com.vivic.eunomia.module.flow.Flow;
 * import com.vivic.eunomia.module.receptor.FlowProcessor;
 * import com.vivic.eunomia.module.receptor.ReceptorProcessorModule;
 * import eunomia.util.io.EunomiaObjectInputStream;
 * import java.io.ByteArrayInputStream;
 * import java.io.DataInputStream;
 * import java.io.DataOutputStream;
 * import java.io.IOException;
 * import java.io.InputStream;
 * import java.io.OutputStream;
 * import java.lang.reflect.Method;
 * 
 * /**
 * *
 * * @author  Mikhail Sosonkin
 * * /
 * 
 * public class Main implements ReceptorProcessorModule, FlowProcessor {
 *    private long events;
 *    private long streamSize;
 *    private long lastTime;
 *    private long lastEvents;
 *    private long lastStreamSize;
 *    private double streamRate;
 *    private double eventRate;
 *    private Method method;
 * 
 *    public Main() {
 *    }
 *    
 *    public long getEvents(){
 *        return events;
 *    }
 *    
 *    public long getBytes(){
 *        return streamSize;
 *    }
 *    
 *    public void setProperty(String name, Object value) {
 *    }
 *    
 *    public Object getProperty(String name) {
 *        return null;
 *    }
 *    
 *    public void computeRates(){
 *        long tmpEvents = events;
 *        long stream = streamSize;
 *        
 *        double eventDiff = (double)(tmpEvents - lastEvents);
 *        double streamDiff = (double)(stream - lastStreamSize);
 *        
 *        long time = System.currentTimeMillis();
 *        double timeDiff = ((double)(time - lastTime))/1000.0;
 *        lastTime = time;
 *        
 *        lastEvents = tmpEvents;
 *        lastStreamSize = stream;
 *        
 *        streamRate = (streamDiff)/(timeDiff);
 *        eventRate = eventDiff/(timeDiff);
 *    }
 *    
 *    public double getEventRate(){
 *        return eventRate;
 *    }
 *    
 *    public double getByteRate(){
 *        return streamRate;
 *    }
 * 
 *    public void newFlow(Flow flow) {
 *        ++events;
 *        streamSize += flow.getSize();
 *    }
 *    
 *    public Filter getFilter() {
 *        return null;
 *    }
 *    
 *    public void setFilter(Filter f){
 *    }
 * 
 *    public FlowProcessor getFlowPocessor(){
 *        return this;
 *    }
 *    
 *    public void start() {
 *    }
 * 
 *    public void stop() {
 *    }
 * 
 *    public void reset() {
 *    }
 * 
 *    public FlowProcessor getFlowProcessor() {
 *        return this;
 *    }
 * 
 *    public void updateStatus(OutputStream out) throws IOException {
 *        computeRates();
 *        
 *        DataOutputStream dout = new DataOutputStream(out);
 *        dout.writeLong(events);
 *        dout.writeLong(streamSize);
 *        dout.writeDouble(streamRate);
 *        dout.writeDouble(eventRate);
 *    }
 *    
 *    public void setControlData(InputStream in) throws IOException {
 *    }
 *    
 *    public void getControlData(OutputStream out) throws IOException {
 *    }
 * 
 *    public Message processMessage(ModuleMessage msg) throws IOException {
 *        return null;
 *    }
 * 
 *    public void initialize() {
 *    }
 * 
 *    public void destroy() {
 *    }
 * 
 *    public Object[] getCommands() {
 *        return null;
 *    }
 * 
 *    public Object executeCommand(Object command, Object[] parameters) {
 *        return null;
 *    }
 * 
 *    public boolean accept(FlowModule module) {
 *        return true;
 *    }
 * 
 *    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException {
 *    }
 * }</PRE>
 * @author Mikhail Sosonkin
 */
public interface ReceptorProcessorModule extends EunomiaModule {
    /**
     * This method is invoked just before an instance of the module is terminated. The 
     * module should perform cleanup to release resources, static context should be 
     * cleaned up with caution as it may be used by other instances.
     */
    public void destroy();
    
    /**
     * This method should return an object that will perform the actual processing of 
     * Flows. Generally this function will be called only once after instantiation, 
     * so the module is not guaranteed to have the ability to change it later on.
     * @return an instance for processing flows. For simple modules it can be the Main class
     * instance.
     */
    public FlowProcessor getFlowProcessor();
    /**
     * The Sieve will use this method to syncronize with the Console. The console will 
     * request an update, generatlly in regular intervals (but could be never depending 
     * on the implementation). This method will be called when such a request is 
     * received. The module will be provided an output stream which is it excepted to 
     * fill with data. The data can be in any format the module chooses. It will be 
     * stored as written and transfered over to the Console. While there is no limit 
     * on the size of the data, it is advised to be less than 256K long. The reason is 
     * that longer data will require a different form of data transfer and demand more 
     * resources and time to transfer. Finally, this data is intended for displaying 
     * real-time analysis results of the module, so it should be properly structured 
     * to contain such information.
     * @param out destination buffer.
     * @throws java.io.IOException Error reporting
     */
    public void updateStatus(OutputStream out) throws IOException;
    /**
     * As with many applications, complex modules will require user input for 
     * configuration purposes. This method will be called if some configuration data 
     * is changed by the user on the console side. This method can, but will most 
     * likely not, be called in regular intervals. It will only happen upon the request 
     * by the user. When a request comes in to the Sieve, this method will be called 
     * with an input stream as the parameter. This stream will contain the data 
     * generated by this module instance on the Console side. As with the 
     * <CODE>updateStatus()</CODE> method, there are no requirements for any specific 
     * data format. The module is responsible for parsing, Eunomia will merely ensure 
     * that the bytes are transfered unmodified. The module should be prepared to handle
     * this call at any point after instantiation.
     * @param in Incoming configuration data
     * @throws java.io.IOException Error reporting
     */
    public void setControlData(InputStream in) throws IOException;
    /**
     * This method is the reverse of the <CODE>setControlData()</CODE> method. Is it 
     * called by the Sieve to obtain configuration data from the module. It is called 
     * by the Sieve when a request comes in from a Console to obtain module's 
     * configuration data. An output stream will be passed to the method. The module 
     * is expected to write its output data there. Again, similar to other 
     * communication methods there are no format requirements.
     * @param out Destination buffer
     * @throws java.io.IOException Error reporting
     */
    public void getControlData(OutputStream out) throws IOException;
    
    /**
     * There are scenarios where the Console side will need to send messages to the 
     * Sieve side. For example, to send user input. For those cases the framework 
     * provides mechanisms for asynchronous communication. The Console side can create 
     * such a message by using <CODE>InterModuleCommunication</CODE> provided by the 
     * module manager. The data will be transfered to the Sieve side and invoke this 
     * method. The input stream argument will provide the data generated by the module 
     * (without any format requirements). The output stream argument will accept any 
     * data and return to the Console side. The caveat with this system is that 
     * communication must be instantiated by the Console side. This is because there 
     * maybe multiple instances of the Console side for one instance of the Sieve side.
     * For scenarios where data needs to be retrieved from the Sieve side, the Console 
     * side will need to poll for it.
     * @param in Input buffer
     * @param out Destination buffer with the return values.
     * @throws java.io.IOException Error Reporting
     */
    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException;
    
    /**
     * This method is intended to indicate to the module that it should pause processing
     * of the flows. This is the intended function, compliance is voluntary. The module 
     * is expected to initialize is started mode. Generally this method will be 
     * called only at the request of the user.
     */
    public void start();
    /**
     * This method is intended to indicate to the module that it should pause processing
     * of the flows. This is the intended function, compliance is voluntary. The module 
     * is expected to initialize is started mode. Generally this method will be 
     * called only at the request of the user.
     */
    public void stop();
    /**
     * This method is intended to indicate to the module that it should reset current 
     * processing state. This is the intended function, compliance is voluntary. 
     * Generally this method will be called only at the request of the user.
     */
    public void reset();
    
    /**
     * Generally this is used for intermodule communication, for those cases where
     * performance is not needed and/or the modules should not be interdependent.
     * @param name Property name
     * @param value Parameter for the property
     */
    public void setProperty(String name, Object value);
    /**
     * Used to retrieve values from the module without knowing specific module 
     * implementation
     * @param name Property name
     * @return The value mapped to the property
     */
    public Object getProperty(String name);
    
    /**
     * This is currently not used
     * @return Commands list
     */
    public Object[] getCommands();
    /**
     * This is currently not used
     * @param command Command name
     * @param parameters Parameters list to the commands
     * @return Any Object return value as the result of the command.
     */
    public Object executeCommand(Object command, Object[] parameters);
}