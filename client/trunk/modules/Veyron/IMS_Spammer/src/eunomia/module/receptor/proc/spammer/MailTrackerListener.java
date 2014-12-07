/*
 * MailTrackerListener.java
 *
 * Created on November 21, 2007, 5:16 PM
 *
 */

package eunomia.module.receptor.proc.spammer;

import eunomia.module.common.proc.spammer.MailServer;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface MailTrackerListener {
    public void spammerAppeared(MailServer serv);
    public void spammerDisappeared(MailServer serv);
    public void mailServerAppeared(MailServer serv);
    public void mailServerDisappeared(MailServer serv);
}