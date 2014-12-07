/*
 * LogConv.java
 *
 * Created on July 13, 2005, 5:27 PM
 *
 */

import java.io.*;
import eunomia.core.data.*;
import eunomia.core.data.flow.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class LogConv {
/*    private static void compare(String bin, String str) throws Exception{
        DataInputStream din = new DataInputStream(new FileInputStream(bin));
        BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(str)));
        Flow flowBin = new ModifiableFlow();
        Flow flowStr = new ModifiableFlow();
        int count = 0;
        
        while(true){
            flowBin.readFromDataStream(din);
            Flow.parseFlowString(fin.readLine(), flowStr);
            if(!flowBin.areEqual(flowStr)){
                System.out.println("(" + flowBin + ") != (" + flowStr + ")");
                System.out.println("Count: " + count);
                return;
            }
            ++count;
        }
    }*/
    public static void main(String[] argv) throws Exception {
/*        //compare(argv[0], argv[1]);
        BufferedReader fin = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(argv[0]))));
        DataOutputStream fout = new DataOutputStream(new FileOutputStream(argv[1]));
        Flow flow = new ModifiableFlow();
        String line = null;
        int count = 0;
        
        while( (line = fin.readLine()) != null){
            count++;
            flow.parseFlowString(line,  flow);
            flow.writeToDataStream(fout);
            if(count % 1000000 == 0){
                System.out.println("count: " + count);
            }
        }*/
    }
}