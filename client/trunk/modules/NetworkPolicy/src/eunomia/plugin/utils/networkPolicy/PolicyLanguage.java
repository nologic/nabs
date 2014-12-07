/*
 * FilterLanguage.java
 *
 * Created on October 13, 2007, 4:37 PM
 *
 */

package eunomia.plugin.utils.networkPolicy;

import com.vivic.eunomia.filter.Filter;
import com.vivic.eunomia.filter.FilterEntry;
import eunomia.plugin.com.networkPolicy.PolicyItem;
import eunomia.receptor.module.NABFlow.NABFilterEntry;
import eunomia.receptor.module.NABFlow.NABFlow;
import com.vivic.eunomia.sys.util.Util;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Mikhail Sosonkin
 */
public class PolicyLanguage {
    public static final String[] TYPE_NAME = new String[] {
        "Real Time", "Hourly", "Daily", "Weekly", "Monthly"
    };
    
    public static String[] makeFilterBasicDescription(Filter f) {
        if(!checkBasic(f)) {
            return null;
        }
        
        Set fromSet = new HashSet();
        Set toSet = new HashSet();
        
        StringBuilder b = null;
        NABFilterEntry sample = null;
        FilterEntry[] wList = f.getWhiteList().getArray();
        for (int i = 0; i < wList.length; i++) {
            NABFilterEntry entry = (NABFilterEntry)wList[i];
            if(entry != null) {
                sample = entry;
                
                int fromPort1 = entry.getSrc_lport();
                int fromPort2 = entry.getSrc_uport();

                int toPort1 = entry.getDst_lport();
                int toPort2 = entry.getDst_uport();
                
                if( (b = genPortRange(fromPort1, fromPort2, true)) != null) {
                    fromSet.add(b);
                }
                
                if( (b = genPortRange(toPort1, toPort2, true)) != null) {
                    toSet.add(b);
                }
            }
        }
        
        String[] fromTo = new String[]{"", ""};
        if(sample != null) {
            fromTo[0] = genBasicString(fromSet, genIPRange(sample.getSrc_lip(), sample.getSrc_uip()));
            fromTo[1] = genBasicString(toSet, genIPRange(sample.getDst_lip(), sample.getDst_uip()));
        }

        return fromTo;
    }
    
    private static String genBasicString(Set ports, StringBuilder ip) {
        boolean hasIp = ip != null;
        if(!hasIp) {
            ip = new StringBuilder();
        }
        
        Iterator it = ports.iterator();
        while (it.hasNext()) {
            StringBuilder port = (StringBuilder) it.next();
            if(hasIp) {
                ip.append(":");
                hasIp = false;
            }
            
            ip.append(port);
            if(it.hasNext()) {
                ip.append(", ");
            }
        }
        
        return ip.toString();
    }
    
    private static boolean checkBasic(Filter f) {
        if(f.getBlackList().getCount() != 0) {
            return false;
        }
        
        NABFilterEntry sample = null;
        FilterEntry[] wList = f.getWhiteList().getArray();
        for (int i = 0; i < wList.length; i++) {
            NABFilterEntry entry = (NABFilterEntry)wList[i];
            if(entry != null) {
                if(sample == null) {
                    sample = entry;
                } else if(!compareBasic_Entries(sample, entry)){
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private static boolean compareBasic_Entries(NABFilterEntry e1, NABFilterEntry e2) {
        return Arrays.equals(e1.getAllowType(), e2.getAllowType()) &&
               
               Arrays.equals(e1.getSrc_lip(), e2.getSrc_lip()) &&
               Arrays.equals(e1.getSrc_uip(), e2.getSrc_uip()) &&
               
               Arrays.equals(e1.getDst_lip(), e2.getDst_lip()) &&
               Arrays.equals(e1.getDst_uip(), e2.getDst_uip());
    }
    
    public static String makeFilterDescription(Filter filter) {
        StringBuilder b = new StringBuilder();
        
        FilterEntry[] wList = filter.getWhiteList().getArray();
        FilterEntry[] bList = filter.getBlackList().getArray();
        
        for (int i = 0; i < wList.length; i++) {
            if(wList[i] != null) {
                makeEntryFullDescription((NABFilterEntry)wList[i], b).append("\n");
            }
        }
        
        for (int i = 0; i < bList.length; i++) {
            if(bList[i] != null) {
                makeEntryFullDescription((NABFilterEntry)bList[i], b.append("not ")).append("\n");
            }
        }

        return b.toString();
    }
    
    private static StringBuilder makeEntryFullDescription(NABFilterEntry entry, StringBuilder b){
        StringBuilder ip, port;
        
        if(b == null)
            b = new StringBuilder();

        int nextLocation = b.length();
        
        PolicyLanguage.getAllowList(entry.getAllowType(), b).append(" ");
        
        ip = PolicyLanguage.genIPRange(entry.getSrc_lip(), entry.getSrc_uip());
        port = PolicyLanguage.genPortRange(entry.getSrc_lport(), entry.getSrc_uport(), ip != null);
        
        if(ip != null || port != null){
            b.append("from ");
        
            if(ip != null) b.append(ip);
            if(port != null) (ip == null?b:b.append(":")).append(port).append(" ");
        }
        
        ip = PolicyLanguage.genIPRange(entry.getDst_lip(), entry.getDst_uip());
        port = PolicyLanguage.genPortRange(entry.getDst_lport(), entry.getDst_uport(), ip != null);
        if(ip != null || port != null){
            b.append("to ");
        
            if(ip != null) b.append(ip);
            if(port != null) (ip == null?b:b.append(":")).append(port);
        }

        b.setCharAt(nextLocation, Character.toUpperCase(b.charAt(nextLocation)));
        
        return b;
    }
    
    public static String makePolicySummaryHTML(PolicyItem policy) {
        Filter filter = policy.getFilter();
        StringBuilder b = new StringBuilder();
        
        b.append("<html><body>");
        b.append(genPolicyDescription_Summary(policy));
        b.append("<br>");
        if(checkBasic(filter)) {
            b.append(genFilterString_BasicSummary(policy.getFilter()));
        } else {
            b.append("Advanced Filter");
        }
        b.append("</body></html>");
        
        return b.toString();
    }
    
    private static String genPolicyDescription_Summary(PolicyItem p) {
        StringBuilder b = new StringBuilder();
        
        int type = p.getPolicyType();
        
        b.append(p.getPolicyID());
        b.append(") ");
        b.append(TYPE_NAME[type]);
        b.append(" over ");
        
        if(type == PolicyItem.REAL_TIME) {
            b.append(Util.convertBytesRate(p.getRate(), true));
        } else {
            b.append(Util.convertBytes(p.getRate()));
        }
        
        b.append(" of ");
    
        boolean[] allowed = new boolean[NABFlow.NUM_TYPES];
        Arrays.fill(allowed, false);
        
        FilterEntry[] entries = p.getFilter().getWhiteList().getArray();
        for (int i = 0; i < entries.length; i++) {
            NABFilterEntry entry = (NABFilterEntry)entries[i];
            if(entry != null){
                boolean[] aList = entry.getAllowType();
                for (int j = 0; j < aList.length; j++) {
                    allowed[j] |= aList[j];
                }
            }
        }
        
        PolicyLanguage.getAllowList(allowed, b);
        
        return b.toString();
    }
    
    private static String genFilterString_BasicSummary(Filter filter) {
        int wListCount = filter.getWhiteList().getCount();
        if(wListCount == 0) {
            return "for all traffic";
        }
        
        NABFilterEntry entry = null;
        FilterEntry[] entries = filter.getWhiteList().getArray();
        for (int i = 0; i < entries.length; i++) {
            if( (entry = (NABFilterEntry)entries[i]) != null) {
                break;
            }
        }
        
        StringBuilder b = new StringBuilder();

        StringBuilder ip = null;
        StringBuilder port = null;
        if(entry.isSrcIPSet()) {
            ip = PolicyLanguage.genIPRange(entry.getSrc_lip(), entry.getSrc_uip());
        }

        if(entry.isSrcPortSet()) {
            port = PolicyLanguage.genPortRange(entry.getSrc_lport(), entry.getSrc_uport(), false);
        }
        
        if(ip != null || port != null) {
            b.append(" from ");
            if(ip != null) {
                b.append(ip);
            }
            
            if(port != null) {
                if(ip != null) {
                    b.append(" ");
                }
                b.append(port);
                if(wListCount > 1) {
                    b.append("...");
                }
            }
        }

        ip = null;
        port = null;
        
        if(entry.isDstIPSet()) {
            ip = PolicyLanguage.genIPRange(entry.getDst_lip(), entry.getDst_uip());
        }

        if(entry.isDstPortSet()) {
            port = PolicyLanguage.genPortRange(entry.getDst_lport(), entry.getDst_uport(), false);
        }
        
        if(ip != null || port != null) {
            b.append(" to ");
            if(ip != null) {
                b.append(ip);
            }
            
            if(port != null) {
                if(ip != null) {
                    b.append(" ");
                }
                b.append(port);
                if(wListCount > 1) {
                    b.append("...");
                }
            }
        }
        
        if(b.length() == 0) {
            b.append("for all traffic");
        }
        
        return b.toString();
    }
    
    private static StringBuilder getAllowList(boolean[] allowList, StringBuilder b) {
        if(b == null)
            b = new StringBuilder();
        
        boolean isAllSelected = true;
        boolean isNoneSelected = true;
        
        for (int i = 0; i < allowList.length; i++) {
            isAllSelected = isAllSelected && allowList[i];
            isNoneSelected = isNoneSelected && !allowList[i];
        }
        
        if(isAllSelected) {
            b.append("data");
        } else if(isNoneSelected) {
            b.append("no data");
        } else {
            boolean video = allowList[NABFlow.DT_Audio_MP3] &&
                    allowList[NABFlow.DT_Audio_WAV] &&
                    allowList[NABFlow.DT_Video_MPG];
            
            boolean images = allowList[NABFlow.DT_Image_BMP] &&
                    allowList[NABFlow.DT_Image_JPG];
            
            boolean audio = allowList[NABFlow.DT_Audio_MP3] &&
                    allowList[NABFlow.DT_Audio_WAV];
            
            boolean multimedia = video && images && audio;
            
            boolean encoded = allowList[NABFlow.DT_Compressed] &&
                    allowList[NABFlow.DT_Encrypted];
            
            boolean text = allowList[NABFlow.DT_Plain_Text];
            
            if(multimedia) {
                audio = images = video = false;
            }
            
            if(video) {
                audio = false;
            }
            
            if(video || images || audio || encoded || text){
                boolean comma = false;
                if(multimedia) {
                    if(comma) b.append(", ");
                    b.append("multimedia");
                    comma = true;
                }
                
                if(video) {
                    if(comma) b.append(", ");
                    b.append("video");
                    comma = true;
                }
                
                if(images) {
                    if(comma) b.append(", ");
                    b.append("images");
                    comma = true;
                }
                
                if(audio) {
                    if(comma) b.append(", ");
                    b.append("audio");
                    comma = true;
                }
                
                if(encoded){
                    if(comma) b.append(", ");
                    b.append("encoded");
                    comma = true;
                }
                
                if(text) {
                    if(comma) b.append(", ");
                    b.append("text");
                    comma = true;
                }
            } else {
                boolean added = false;
                
                for (int i = 0; i < allowList.length; i++) {
                    if(allowList[i]){
                        if(added) {
                            b.append(", ");
                        }
                        b.append(NABFlow.typeNames[i]);
                        added = true;
                    } else {
                        added = false;
                    }
                }
            }
        }
        
        return b;
    }

    private static StringBuilder genPortRange(int start, int end, boolean compressed) {
        if(start == 0 && end == 0xFFFF) {
            return null;
        }
        
        StringBuilder builder = new StringBuilder();
        if(start != end){
            if(!compressed) builder.append("ports ");
            
            builder.append(start);
            builder.append( (compressed?"-":" to ") );
            builder.append(end);
        } else {
            if(!compressed) builder.append("port ");
            builder.append(start);
        }

        return builder;
    }
    
    private static StringBuilder genIPRange(int[] start, int[] end) {
        boolean isAll = true;
        for (int i = 0; i < start.length; i++){
            if(start[i] > 0 && end[i] < 255){
                isAll = false;
                break;
            }
        }
        
        if(isAll) {
            return null;
        }
        
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < start.length; i++){
            int s = start[i];
            int e = end[i];
            
            if(s != e){
                if(s == 0 && e == 255){
                    builder.append("*");
                } else {
                    builder.append(s);
                    builder.append("-");
                    builder.append(e);
                }
            } else {
                builder.append(s);
            }
            
            if(i != start.length - 1){
                builder.append(".");
            }
        }
        
        return builder;
    }
}
