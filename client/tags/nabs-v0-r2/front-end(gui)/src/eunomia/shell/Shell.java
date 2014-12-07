/*
 * Shell.java
 *
 * Created on September 28, 2006, 10:31 PM
 */

package eunomia.shell;

import bsh.EvalError;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface Shell {
    public void execute(String cmd) throws EvalError;
    public String getLine();
    public String getLine(String varName, String desc);
    public String getPasswordLine(String varName, String desc);
}