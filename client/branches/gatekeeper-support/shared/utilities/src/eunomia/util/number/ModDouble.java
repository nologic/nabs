/*
 * ModDouble.java
 *
 * Created on September 20, 2006, 9:43 PM
 *
 */

package eunomia.util.number;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModDouble extends Number implements Comparable {
    private double value;
    
    public void setDouble(double v){
        value = v;
    }
    
    public void setNumber(Number num){
        value = num.doubleValue();
    }
    
    public int hashCode(){
        long bits = Double.doubleToLongBits(value);
        
	return (int)(bits ^ (bits >>> 32));
    }
    
    public boolean equals(Object o){
        Number num = (Number)o;
        
        return num.doubleValue() == value;
    }
    
    public double doubleValue(){
        return value;
    }
    
    public float floatValue(){
        return (float)value;
    }
    
    public int intValue(){
        return (int)value;
    }
    
    public long longValue(){
        return (long)value;
    }
    
    public int compareTo(Object o) {
        Number num = (Number)o;
        double thisVal = this.value;
        double anotherVal = num.longValue();
        
	return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
    }
    
    public String toString(){
        return Double.toString(value);
    }
}