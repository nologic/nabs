/*
 * NabsClient.java
 *
 * Created on June 9, 2005, 12:41 PM
 */

package eunomia.core.data.streamData.client;

import java.io.*;
import eunomia.core.data.streamData.client.listeners.*;

/**
 *
 * @author  Mikhail Sosonkin
 */
public interface NabsClient extends FlowProcessor {
    public void activate() throws IOException;
    public void deactivate() throws IOException;
    public void connect() throws IOException;
    public void disconnect() throws IOException;
    public boolean isActive();
    public boolean registerProcessor(FlowProcessor fp);
    public boolean deregisterProcessor(FlowProcessor fp);
    public void setServer(String ip, int port);
    public int getPort();
    public String getIP();
}