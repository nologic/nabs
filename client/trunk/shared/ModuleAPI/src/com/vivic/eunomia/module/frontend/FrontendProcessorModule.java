/*
 * Module.java
 *
 * Created on June 24, 2005, 4:09 PM
 *
 */

package com.vivic.eunomia.module.frontend;

import com.vivic.eunomia.module.EunomiaModule;
import com.vivic.eunomia.sys.frontend.ConsoleReceptor;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JComponent;

/**
 * Sieve supports many different module types. The modules will need to implement
 * specific interfaces in order to play their roles. One type is the
 * <B>Flow Processor Module</B>. This module requires seperate components for Sieve
 * side and Console side. {@link FrontendProcessorModule} defines the methods required
 * for the Console side. <br><br>
 *
 * FrontendProcessorModule is expected to be implemented by class:<br>
 * <CODE>eunomia.module.frontend.proc.[Module Name].Main</CODE><br><br>
 *
 * The flow processor module can be instantiated multiple times on the Sieve side.
 * Consequently there maybe several instance of the Console side as well refer to
 * {@link com.vivic.eunomia.module.receptor.ReceptorProcessorModule} for more details.
 * Each module on the Console will be loaded by a seperate class loader, however
 * multiple instances will stem from the same class loader. This is true on per
 * Sieve connection basis, meaning that different class loaders will be used for the
 * same module for different Sieve connections.
 * @author Mikhail Sosonkin
 */
public interface FrontendProcessorModule extends EunomiaModule {
    /**
     * Returns the UI component for this module.
     *
     * Since this module represents the user interface side, it is required to display
     * it some how. This function should construct an interface with Swing compatible
     * classes. There are no restrictions on the format or the contents, but be mindful
     * that the user will not see this as the only module and it will not be in full
     * screen mode.
     *
     * <p>Currently there are only 2 consoles available: Front-end and Gatekeeper. There is
     * also RemoteGate, however it does not use this method to present the user with a
     * UI.
     *
     * @return The swing component that will interact with the user.
     */
    public JComponent getJComponent();

    /**
     * Returns the UI for the control panel of this module.
     *
     * Each module will be displayed in a seperate window. To provide a consistent
     * UI among the modules, there will be a tool bar that will allow the user to open
     * a window and configure the module. This will window will return the component
     * created by this method. Again, there are no restrictions but be mindful of what
     * the user will see.
     *
     * @return Swing compatible component to be displayed to the user.
     */
    public JComponent getControlComponent();

    /**
     * Returns the title of the module instance.
     *
     * The title will be displayed at the top of the module window. Current Console
     * implementations will update the title everytime the module recieves a new state.
     * This, however, is not guaranteed.
     *
     * @return The title of the module.
     */
    public String getTitle();

    /**
     * Processes intercom response messages
     *
     * As mentioned in {@link com.vivic.eunomia.module.receptor.ReceptorProcessorModule}
     * the 2 sides of the same module can communicate through an asynchronous channel.
     * This method will be receiving the return value from the module's corresponding
     * Sieve instance. The communication should be initiated through the ConsoleModuleManager
     * {@link com.vivic.eunomia.sys.frontend.ConsoleModuleManager}. Once the message is
     * processed by the Sieve side instance, the return value will be passed to this
     * method.
     *
     * <pre>
     *  --------             ----------------------  SSL Socket  -------            --------
     * |        |---MESG--->|                      |====MESG===>|       |---MSEG-->|        |
     * | Module |           | ConsoleModuleManager |            | Sieve |          | Module |
     * |        |<--RESP----|                      |<===RESP====|       |<--RESP---|        |
     *  --------             ----------------------              -------            --------
     * </pre>
     *
     * @return Response value from Sieve side module.
     */
    public void processMessage(DataInputStream din) throws IOException;

    /**
     * Set a specific property for the module.
     *
     * This method is a generic of passing arbitrary data to the module. The action taken
     * on this data dependant entirely on the implementation of the module.
     *
     * @param name Name of the property.
     * @param value New value for the property.
     */
    public void setProperty(String name, Object value);

    /**
     * Returns the value of a specific module.
     *
     * This method is an inverse of <code>setProperty()</code> method. The value returned
     * the action(s) taken are entirely dependent on the module implementation. Some console
     * may also want to access this method. For example, REMOTEGATE will call this method with
     * "web" as parameter. This indicates to the module that it should generate either text
     * or an image to be displayed on a web page. (This sort of action may change in the future).
     *
     * @param name Name of the property.
     * @return Value for the property
     */
    public Object getProperty(String name);

    /**
     * Notified the module about a new state.
     *
     * Since the processing and reporting is seperated into different processes (or hosts) the
     * system takes care of updating data on the Console side. The Console will periodically
     * request updates for all modules and pass them on to the module instances. The module
     * implementation is reponsible to representing and decoding the data streams.
     *
     * @param in Input buffer with the state data.
     */
    public void updateStatus(InputStream in) throws IOException;

    /**
     * Obtains configuration data from the module instance.
     *
     * Each module may have specific configuration parameters that the user may want to tweak.
     * Since the Console cannot anticipate all of them, the module can specify arbitrary bytes
     * to store configuration in its own format. This method is used to obtain such data after
     * the user is done editing it through the control panel (returned by
     * <code>getControlComponent()</code>). It will then be passed on the Sieve side of the
     * instance.
     *
     * @param out Output stream that expects the configuration data.
     */
    public void getControlData(OutputStream out) throws IOException;

    /**
     * Assignes current configuration data.
     *
     * This method is very similar to <code>getControlData()</code>, in reverse. The data
     * passed in is the configuration data from the Sieve side instance.
     *
     * @param in Incoming configuration data.
     */
    public void setControlData(InputStream in) throws IOException;
}