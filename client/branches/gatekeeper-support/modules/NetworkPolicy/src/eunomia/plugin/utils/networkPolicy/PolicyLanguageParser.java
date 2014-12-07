/*
 * PolicyLanguageParser.java
 *
 * Created on October 13, 2007, 7:59 PM
 *
 */

package eunomia.plugin.utils.networkPolicy;

import eunomia.flow.Filter;
import eunomia.receptor.module.NABFlow.NABFilterEntry;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Mikhail Sosonkin
 */
public class PolicyLanguageParser {

    public static Filter parseBasicFilter(Filter filter, String from, String to, boolean[] allowList) throws ParseException {
        if(filter == null) {
            filter = new Filter();
        } else {
            filter.clearFilter();
        }
        
        String[] sep = separateIPPort(from.trim().toLowerCase(), to.trim().toLowerCase());
        
        int[][] fromIPRange = null;
        int[][] fromPortRange = null;
        int[][] toIPRange = null;
        int[][] toPortRange = null;
        
        try {
            fromIPRange = parseIPRange(sep[0]);
        } catch(ParseException ex) {
            throw new ParseException("Error parsing FROM IP range: " + ex.getMessage(), 0);
        }

        try {
            fromPortRange = parsePortRanges(sep[1]);
        } catch(ParseException ex) {
            throw new ParseException("Error parsing FROM Port range: " + ex.getMessage(), 0);
        }
        
        try {
            toIPRange = parseIPRange(sep[2]);
        } catch(ParseException ex) {
            throw new ParseException("Error parsing TO IP range: " + ex.getMessage(), 0);
        }
        
        try {
            toPortRange = parsePortRanges(sep[3]);
        } catch(ParseException ex) {
            throw new ParseException("Error parsing TO Port range: " + ex.getMessage(), 0);
        }
        
        constructFilter(filter, allowList, fromIPRange, fromPortRange, toIPRange, toPortRange);
        
        return filter;
    }
    
    private static void constructFilter(Filter f, boolean[] aList, int[][] fromIPRange, int[][] fromPortRange, int[][] toIPRange, int[][] toPortRange) {
        boolean srcIPSet = !( (fromIPRange[0][0] << 24 | fromIPRange[0][1] << 16 | fromIPRange[0][2] << 8 | fromIPRange[0][3]) == 0 &&
                              (fromIPRange[1][0] << 24 | fromIPRange[1][1] << 16 | fromIPRange[1][2] << 8 | fromIPRange[1][3]) == 0xFFFFFFFF);
        boolean srcPortSet = !(fromPortRange[0][0] == 0 && fromPortRange[1][0] == 0xFFFF);
        boolean dstIPSet = !( (toIPRange[0][0] << 24 | toIPRange[0][1] << 16 | toIPRange[0][2] << 8 | toIPRange[0][3]) == 0 &&
                              (toIPRange[1][0] << 24 | toIPRange[1][1] << 16 | toIPRange[1][2] << 8 | toIPRange[1][3]) == 0xFFFFFFFF);;
        boolean dstPortSet = !(toPortRange[0][0] == 0 && toPortRange[1][0] == 0xFFFF);;
        
        for (int i = 0; i < fromPortRange[0].length; i++) {
            for (int j = 0; j < toPortRange[0].length; j++) {
                NABFilterEntry entry = new NABFilterEntry();
                
                for (int k = 0; k < aList.length; k++) {
                    entry.setAllowed(k, aList[k]);
                }
                
                entry.setSourceIpRange(fromIPRange[0], fromIPRange[1]);
                entry.setSourcePortRange(fromPortRange[0][i], fromPortRange[1][i]);
                entry.setDestinationIpRange(toIPRange[0], toIPRange[1]);
                entry.setDestinationPortRange(toPortRange[0][j], toPortRange[1][j]);

                entry.setIsSrcIPSet(srcIPSet);
                entry.setIsSrcPortSet(srcPortSet);
                entry.setIsDstIPSet(dstIPSet);
                entry.setIsDstPortSet(dstPortSet);
                
                f.addFilterWhite(entry);
            }
        }
    }
    
    /**
     * 
     * @param from 
     * @param to 
     * @return String array of {fromIp, fromPort, toIp, toPort}, "" for elements that are not
     * specified.
     */
    private static String[] separateIPPort(String from, String to) {
        String[] separated = new String[4];
        
        String fromIP = null;
        String fromPort = null;
        String[] fromIPport = from.split(":");
        
        if(fromIPport.length == 1) {
            if(fromIPport[0].indexOf(".") != -1) {
                fromIP = fromIPport[0];
                fromPort = "";
            } else {
                fromPort = fromIPport[0];
                fromIP = "";
            }
        } else {
            fromIP = fromIPport[0];
            fromPort = fromIPport[1];
        }
        
        String toIP = null;
        String toPort = null;
        String[] toIPport = to.split(":");
        if(toIPport.length == 1) {
            if(toIPport[0].indexOf(".") != -1) {
                toIP = toIPport[0];
                toPort = "";
            } else {
                toPort = toIPport[0];
                toIP = "";
            }
        } else {
            toIP = toIPport[0];
            toPort = toIPport[1];
        }
        
        separated[0] = fromIP;
        separated[1] = fromPort;
        separated[2] = toIP;
        separated[3] = toPort;
        
        return separated;
    }
    
    /**
     * 
     * @param ip 
     * @throws java.text.ParseException 
     * @return array in the form of {start = {1, 2, 3, 4}, end = {1, 2, 3, 4}}
     */
    private static int[][] parseIPRange(String ip) throws ParseException {
        if(ip.equals("")) {
            int[][] range = new int[2][4];
            for (int i = 0; i < 4; i++) {
                range[0][i] = 0;
                range[1][i] = 0xFF;
            }
            
            return range;
        }
        
        String[] octets = ip.split("\\.");
        if(octets.length != 4) {
            throw new ParseException("Found " + octets.length + " in '" + ip + "' octets, IPv4 requires 4", 0);
        }
        
        int[][] range = new int[2][4];
        
        try {
            for (int i = 0; i < octets.length; i++) {
                if(octets[i].indexOf("*") != -1) {
                    range[0][i] = 0;
                    range[1][i] = 0xFF;
                } else {
                    String[] oct = octets[i].split("-");
                    if(oct.length == 1) {
                        range[0][i] = range[1][i] = Integer.parseInt(oct[0].trim());
                    } else {
                        range[0][i] = Integer.parseInt(oct[0].trim());
                        range[1][i] = Integer.parseInt(oct[1].trim());
                    }
                }
            }
        } catch (NumberFormatException ex) {
            throw new ParseException("Unable to parse IP octets - " + ex.getMessage(), 0);
        }
        
        validateIPRange(range);
        
        return range;
    }
    
    /**
     * 
     * @param ports 
     * @return int array of the port ranges in form of { {start1, start2, ..., startN}, {end1, end2, ..., endN} }
     * @throws java.text.ParseException 
     */
    private static int[][] parsePortRanges(String ports)  throws ParseException {
        StringBuffer cleaned = new StringBuffer();
        char[] chars = ports.toCharArray();
        
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            
            if(c == '-' || c == ',' || Character.isDigit(c)) {
                cleaned.append(c);
            }
        }
        
        Set uniqs = new HashSet();
        String[] parts = cleaned.toString().split(",");
        for (int i = 0; i < parts.length; i++) {
            if(!parts[i].equals("") && !parts[i].equals("-")) {
                uniqs.add(parts[i]);
            }
        }
        
        if(uniqs.size() == 0) {
            int[][] ranges = new int[2][1];
            ranges[0][0] = 0;
            ranges[1][0] = 0xFFFF;
            
            return ranges;
        }
        
        int[][] ranges = new int[2][uniqs.size()];
        parts = (String[])uniqs.toArray(parts);
        try {
            for (int i = 0; i < parts.length; ++i) {
                String[] pR = parts[i].split("-");
                if(pR.length == 1) {
                    ranges[0][i] = ranges[1][i] = Integer.parseInt(pR[0]);
                } else {
                    ranges[0][i] = Integer.parseInt(pR[0]);
                    ranges[1][i] = Integer.parseInt(pR[1]);
                }
            }
        } catch (NumberFormatException ex) {
            throw new ParseException("Unable to parse ports - " + ex.getMessage(), 0);
        }
        
        validatePortRange(ranges);
        
        return ranges;
    }

    private static void validateIPRange(int[][] ipRange) throws ParseException {
        for (int i = 0; i < ipRange[0].length; i++) {
            int i1 = ipRange[0][i];
            int i2 = ipRange[1][i];
            
            if(i1 > 0xFF || i2 > 0xFF || i1 > i2) {
                throw new ParseException("Invalid IP range or number too large", 0);
            }
        }
    }
    
    private static void validatePortRange(int[][] fromPortRange) throws ParseException {
        for (int i = 0; i < fromPortRange[0].length; i++) {
            int i1 = fromPortRange[0][i];
            int i2 = fromPortRange[1][i];
            
            if(i1 > 0xFFFF || i2 > 0xFFFF || i1 > i2) {
                throw new ParseException("Invalid Port range or number too large", 0);
            }
        }
    }
}