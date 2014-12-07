/*
 * ConsoleInterface.java
 *
 * Created on July 21, 2007, 1:22 AM
 *
 */

package com.vivic.eunomia.sys.frontend;

import eunomia.messages.Message;

/**
 * This interface is used to access module's context on the Console. An object
 * implementing this interface will be passed to the module after start up. The
 * module can then use it to access the various Console services.
 *
 * @author Mikhail Sosonkin
 */
public interface ConsoleReceptor {
    /**
     * Returns the state maintence object.
     *
     * The state object is used to maintain information about the Sieve, such as
     * list of sensors, users, modules, etc. The console will be responsible for
     * keeping the state up to data.
     *
     * @return Receptor state.
     */
    public ConsoleReceptorState getState();

    /**
     * Returns the module manager object for the specific Sieve connection.
     *
     * The module managet object provides access other modules on the sieve and
     * allowes for triggering events. Module manager will also provide mechanisms
     * for inter-module communication (Console side to Sieve side) and for retrieval
     * of module instances.
     *
     * @return module manager
     */
    public ConsoleModuleManager getManager();

    /**
     * Returns the console's global settings.
     *
     * The global settings are those shared among all modules and Sieve connections.
     * For example, Flow type color to provide a consistent view to the user.
     *
     * @return global settings instance.
     */
    public GlobalSettings getGlobalSettings();

    /**
     * Indicates if the Console is connected to the Sieve.
     *
     * @return true if there is a socket connection.
     */
    public boolean isConnected();

    /**
     * Indicates if the connection has been authenticated for a particular user.
     *
     * @return true if authentication was accepted.
     */
    public boolean isAuthenticated();

    /**
     * Indicates if the root user was authentiated. This is possible through 2 way.
     * One is to authenticate as a regular user then elevate to root. Another is to
     * authenticate as root directly.
     *
     * @return true if root credentials were accepted.
     */
    public boolean isRootAuthenticated();

    /**
     * Returns the IP of the Sieve connection.
     *
     * @return Sieve IP.
     */
    public String getIP();

    /**
     * Returns the port of the connection.
     *
     * @return Sieve Port.
     */
    public int getPort();

    /**
     * Returns the names used to describe the Sieve connection. This name is provided
     * by the user on creation of the object.
     *
     * @return Sieve name
     */
    public String getName();

    /**
     * Returns the module refresh rate.
     *
     * The modules are automatically updated by the framework with new status information.
     * This number indicates how long to wait between the updates. If the module takes too
     * long to update then another refresh will not be triggered until the previous is
     * completed.
     *
     * @return refresh rate in seconds.
     */
    public int getRefreshRate();
}
