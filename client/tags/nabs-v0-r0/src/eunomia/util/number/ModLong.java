/*
 * ModLong.java
 *
 * Created on August 12, 2005, 12:02 PM
 *
 */

package eunomia.util.number;

/**
 *
 * @author Mikhail Sosonkin
 */

public class ModLong extends Number implements Comparable {
    private long value;
    
    public void setLong(long v){
        value = v;
    }
    
    public void setNumber(Number num){
        value = num.longValue();
    }
    
    public int hashCode(){
        return (int)(value ^ (value >>> 32));
    }
    
    public boolean equals(Object o){
        Number num = (Number)o;
        
        return num.longValue() == value;
    }
    
    public double doubleValue(){
        return (double)value;
    }
    
    public float floatValue(){
        return (float)value;
    }
    
    public int intValue(){
        return (int)value;
    }
    
    public long longValue(){
        return value;
    }
    
    public int compareTo(Object o) {
        Number num = (Number)o;
        long thisVal = this.value;
        long anotherVal = num.longValue();
        
	return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
    }
    
    public String toString(){
        return Long.toString(value);
    }
}
