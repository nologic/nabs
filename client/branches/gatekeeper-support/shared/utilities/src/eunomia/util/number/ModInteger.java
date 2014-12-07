/*
 * ModInteger.java
 *
 * Created on August 12, 2005, 11:50 AM
 *
 */

package eunomia.util.number;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModInteger extends Number implements Comparable {
    private int value;
    
    public void setInt(int v){
        value = v;
    }
    
    public void setNumber(Number num){
        value = num.intValue();
    }
    
    public int hashCode(){
        return value;
    }
    
    public boolean equals(Object o){
        if(o instanceof Number){
            Number num = (Number)o;

            return num.intValue() == value;
        } else {
            return hashCode() == o.hashCode();
        }
    }
    
    public double doubleValue(){
        return (double)value;
    }
    
    public float floatValue(){
        return (float)value;
    }
    
    public int intValue(){
        return value;
    }
    
    public long longValue(){
        return (long)value;
    }
    
    public int compareTo(Object o) {
        Number num = (Number)o;
        int thisVal = this.value;
        int anotherVal = num.intValue();
        
	return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
    }
}