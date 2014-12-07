/*
 * ProtocolEditor.java
 *
 * Created on August 27, 2006, 1:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia.gui.realtime.receptorAdmin.editors;

import eunomia.messages.receptor.protocol.ProtocolDescriptor;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ProtocolEditor {
    public ProtocolDescriptor getDescriptor();
    public void setDescriptor(ProtocolDescriptor d);
}
