package eunomia.module.receptor.libb.imsCore.dns;

import eunomia.module.receptor.libb.imsCore.bind.ByteUtils;
import eunomia.module.receptor.libb.imsCore.iterators.IteratorFilter;

/**
 *
 * @author Justin Stallard
 */
public class DNSResponseFilter implements IteratorFilter {
    
    private long responseTimeIntervalStartSeconds;
    private long responseTimeIntervalStartMicroSeconds;
    private long responseTimeIntervalEndSeconds;
    private long responseTimeIntervalEndMicroSeconds;
    
    private String name;
    private char responseType;
    private byte[] resourceData;
    private int resourceDataLength;
    
    public DNSResponseFilter() {
        resourceData = new byte[0xFFFF];
    }
    
    // returns true if any of the responses in a record match this filter
    public boolean allow(Object o) {
        if (!(o instanceof DNSFlowRecord)) {
            return false;
        }
        
        DNSFlowRecord r = (DNSFlowRecord) o;
        
        if (!(responseTimeIntervalStartSeconds == 0 && responseTimeIntervalStartMicroSeconds == 0) &&
             (responseTimeIntervalStartSeconds > r.getEndTimeSeconds() ||
                (responseTimeIntervalStartSeconds == r.getEndTimeSeconds() && responseTimeIntervalStartMicroSeconds > r.getEndTimeMicroSeconds())))
        {
            return false;
        }
        
        if (!(responseTimeIntervalEndSeconds == 0 && responseTimeIntervalStartMicroSeconds == 0) &&
             (responseTimeIntervalEndSeconds < r.getEndTimeSeconds() ||
                (responseTimeIntervalEndSeconds == r.getEndTimeSeconds() && responseTimeIntervalEndMicroSeconds <= r.getEndTimeMicroSeconds())))
        {
            return false;
        }
        
        if (name == null && responseType == 0 && resourceDataLength == 0) {
            return true;
        }
        
        DNSFlowRecord.DNSResponse response;
        byte[] rData;
        
evalAnswers:        
        for (int i = 0; i < (int) r.getAnswerCount(); ++i) {
            response = r.getAnswers()[i];
            
            if (name != null && !response.getName().equals(name)) {
                continue;
            }
            
            if (responseType != 0 && responseType != response.getResponseType()) {
                continue;
            }
            
            if (resourceDataLength != response.getResourceDataLength()) {
                continue;
            }
            
            rData = response.getResourceData();
            for (int j = 0; i < resourceDataLength; ++i) {
                if (resourceData[j] != rData[j]) {
                    continue evalAnswers;
                }
            }
            
            return true;
        }

evalAuthorities:        
        for (int i = 0; i < (int) r.getAuthorityCount(); ++i) {
            response = r.getAuthorities()[i];
            
            if (name != null && !response.getName().equals(name)) {
                continue;
            }
            
            if (responseType != 0 && responseType != response.getResponseType()) {
                continue;
            }
            
            if (resourceDataLength != response.getResourceDataLength()) {
                continue;
            }
            
            rData = response.getResourceData();
            for (int j = 0; i < resourceDataLength; ++i) {
                if (resourceData[j] != rData[j]) {
                    continue evalAuthorities;
                }
            }
            
            return true;
        }
        
evalAdditionals:        
        for (int i = 0; i < (int) r.getAdditionalCount(); ++i) {
            response = r.getAdditionals()[i];
            
            if (name != null && !response.getName().equals(name)) {
                continue;
            }
            
            if (responseType != 0 && responseType != response.getResponseType()) {
                continue;
            }
            
            if (resourceDataLength != response.getResourceDataLength()) {
                continue;
            }
            
            rData = response.getResourceData();
            for (int j = 0; i < resourceDataLength; ++i) {
                if (resourceData[j] != rData[j]) {
                    continue evalAdditionals;
                }
            }
            
            return true;
        }

        return false;
    }
    
    public void setResponseTimeIntervalStart(long seconds, long microSeconds) throws Exception {
        if ((responseTimeIntervalEndSeconds == 0 && responseTimeIntervalEndMicroSeconds == 0) ||
            (responseTimeIntervalEndSeconds > seconds) ||
            (responseTimeIntervalEndSeconds == seconds && responseTimeIntervalEndMicroSeconds > microSeconds))
        {
            responseTimeIntervalStartSeconds = seconds;
            responseTimeIntervalStartMicroSeconds = microSeconds;
            return;
        }
        
        throw new Exception("Cannot set start of interval later than end of interval.");
    }
    
    public void setResponseTimeIntervalEnd(long seconds, long microSeconds) throws Exception {
        if ((responseTimeIntervalStartSeconds == 0 && responseTimeIntervalStartMicroSeconds == 0) ||
            (responseTimeIntervalStartSeconds < seconds) ||
            (responseTimeIntervalStartSeconds == seconds && responseTimeIntervalStartMicroSeconds < microSeconds))
        {
            responseTimeIntervalStartSeconds = seconds;
            responseTimeIntervalStartMicroSeconds = microSeconds;
            return;
        }
        
        throw new Exception("Cannot set end of interval earlier than start of interval.");
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setResponseType(char responseType) {
        this.responseType = responseType;
    }
    
    public void setResourceDataIP(long resourceDataIP) {
        resourceDataLength = ByteUtils.intToBytes(resourceData, 0, (int) resourceDataIP);
    }
    
    public void setResourceDataName(String name) throws Exception {
        if (name.length() > 0xFFFF) {
            throw new Exception("Name is too long to be valid resource data.");
        }
        
        resourceDataLength = name.length();
        System.arraycopy(name.getBytes("US-ASCII"), 0, resourceData, 0, resourceDataLength);
    }
    
    public void setResourceData(byte[] arr) throws Exception {
        setResourceData(arr, 0, arr.length);
    }
    
    public void setResourceData(byte[] arr, int offset, int count) throws Exception {
        if (count > 0xFFFF) {
            throw new Exception("Invalid resource data length.");
        }
        
        System.arraycopy(arr, offset, resourceData, 0, count);
    }
}