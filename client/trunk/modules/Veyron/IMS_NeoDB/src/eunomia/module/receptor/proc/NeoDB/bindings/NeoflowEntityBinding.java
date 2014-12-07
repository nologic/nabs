/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eunomia.module.receptor.proc.NeoDB.bindings;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.bind.tuple.TupleTupleBinding;
import eunomia.receptor.module.NEOFlow.NEOFlow;
import eunomia.receptor.module.NEOFlow.TimeStamp;

/**
 *
 * @author justin
 */
public class NeoflowEntityBinding extends TupleTupleBinding {
    @Override
    public Object entryToObject(TupleInput keyInput, TupleInput dataInput) {
        NEOFlow f = new NEOFlow();
        TimeStamp t = new TimeStamp();
        
        // read the key
        f.setProtocol(keyInput.readByte());
        f.setSourcePort(keyInput.readUnsignedShort());
        f.setDestinationPort(keyInput.readUnsignedShort());
        f.setSourcrIP(keyInput.readUnsignedInt());
        f.setDestinationIp(keyInput.readUnsignedInt());
        t.setSeconds(keyInput.readUnsignedInt());
        t.setMicroSeconds(keyInput.readUnsignedInt());
        f.setStartTime(t.clone());
        t.setSeconds(keyInput.readUnsignedInt());
        t.setMicroSeconds(keyInput.readUnsignedInt());
        f.setEndTime(t.clone());
        
        // read the data
        // FIXME BUG in NEOFlow....ALL int should be unsigned int
        f.setPackets(dataInput.readInt());
        f.setSize(dataInput.readInt());
        f.setMin_packet_size(dataInput.readInt());
        f.setMax_packet_size(dataInput.readInt());
        for (int i = 0; i < NEOFlow.MAX_HISTOGRAM_INDEX; ++i) {
            f.setHistogram(i, dataInput.readInt());
        }
        t.setSeconds(dataInput.readUnsignedInt());
        t.setMicroSeconds(dataInput.readUnsignedInt());
        f.setMinInterArrivalTime(t.clone());
        t.setSeconds(dataInput.readUnsignedInt());
        t.setMicroSeconds(dataInput.readUnsignedInt());
        f.setMaxInterArrivalTime(t.clone());
        f.setTos(dataInput.readByte());
        f.setFragCount(dataInput.readUnsignedInt());
        f.setMin_ttl((short) dataInput.readUnsignedByte());
        f.setMax_ttl((short) dataInput.readUnsignedByte());
        for (int i = 0; i < NEOFlow.NUM_TCP_FLAGS; ++i) {
            f.setTcpFlag(i, dataInput.readInt());
        }
        t.setSeconds(dataInput.readUnsignedInt());
        t.setMicroSeconds(dataInput.readUnsignedInt());
        f.setFirstSYNpackTime(t.clone());
        t.setSeconds(dataInput.readUnsignedInt());
        t.setMicroSeconds(dataInput.readUnsignedInt());
        f.setFirstSYNACKpackTime(t.clone());
        t.setSeconds(dataInput.readUnsignedInt());
        t.setMicroSeconds(dataInput.readUnsignedInt());
        f.setFirstACKpackTime(t.clone());
        for (int i = 0; i < NEOFlow.NUM_TYPES; ++i) {
            f.setTypeCount(i, dataInput.readInt());
        }
        
        return f;
    }

    @Override
    public void objectToKey(Object obj, TupleOutput key) {
        // write the key
        NEOFlow f = (NEOFlow) obj;
        
        key.writeByte(f.getProtocol());
        key.writeUnsignedShort(f.getSourcePort());
        key.writeUnsignedShort(f.getDestinationPort());
        key.writeUnsignedInt(f.getSourceIP());
        key.writeUnsignedInt(f.getDestinationIP());
        key.writeUnsignedInt(f.getStartTime().getSeconds());
        key.writeUnsignedInt(f.getStartTime().getMicroSeconds());
        key.writeUnsignedInt(f.getEndTime().getSeconds());
        key.writeUnsignedInt(f.getEndTime().getMicroSeconds());
    }

    @Override
    public void objectToData(Object obj, TupleOutput data) {
        // write the data
        // FIXME BUG in NEOFlow....ALL int should be unsigned int
        NEOFlow f = (NEOFlow) obj;
        
        data.writeInt(f.getPackets());
        data.writeInt(f.getSize());
        data.writeInt(f.getMin_packet_size());
        data.writeInt(f.getMax_packet_size());
        for (int i = 0; i < NEOFlow.MAX_HISTOGRAM_INDEX; ++i) {
            data.writeInt(f.getHistogram(i));
        }
        data.writeUnsignedInt(f.getMinInterArrivalTime().getSeconds());
        data.writeUnsignedInt(f.getMinInterArrivalTime().getMicroSeconds());
        data.writeUnsignedInt(f.getMaxInterArrivalTime().getSeconds());
        data.writeUnsignedInt(f.getMaxInterArrivalTime().getMicroSeconds());
        data.writeByte(f.getTos());
        data.writeUnsignedInt(f.getFragCount());
        data.writeUnsignedByte(f.getMin_ttl());
        data.writeUnsignedByte(f.getMax_ttl());
        for (int i = 0; i < NEOFlow.NUM_TCP_FLAGS; ++i) {
            data.writeInt(f.getTcpFlag(i));
        }
        data.writeUnsignedInt(f.getFirstSYNpackTime().getSeconds());
        data.writeUnsignedInt(f.getFirstSYNpackTime().getMicroSeconds());
        data.writeUnsignedInt(f.getFirstSYNACKpackTime().getSeconds());
        data.writeUnsignedInt(f.getFirstSYNACKpackTime().getMicroSeconds());
        data.writeUnsignedInt(f.getFirstACKpackTime().getSeconds());
        data.writeUnsignedInt(f.getFirstACKpackTime().getMicroSeconds());
        for (int i = 0; i < NEOFlow.NUM_TYPES; ++i) {
            data.writeInt(f.getTypeCount(i));
        }
    }
}
