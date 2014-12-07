/*
 * NumberFormater.java
 *
 * Created on August 15, 2005, 4:51 PM
 *
 */

package eunomia.util.number;

import java.text.NumberFormat;
import eunomia.util.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class NumberFormater extends NumberFormat {
    
    public NumberFormater() {
    }

    public StringBuffer format(double number, StringBuffer toAppendTo, java.text.FieldPosition pos) {
        return toAppendTo.append(Util.convertBytes(number, true));
    }

    public StringBuffer format(long number, StringBuffer toAppendTo, java.text.FieldPosition pos) {
        return toAppendTo.append(Util.convertBytes(number, true));
    }

    public Number parse(String source, java.text.ParsePosition parsePosition) {
        System.out.println("Should actually implement");
        return null;
    }
}