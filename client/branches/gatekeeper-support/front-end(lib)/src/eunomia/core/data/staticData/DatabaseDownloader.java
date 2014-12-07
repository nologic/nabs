/*
 * DatabaseImporter.java
 *
 * Created on April 15, 2006, 6:21 PM
 *
 */

package eunomia.core.data.staticData;

import eunomia.util.database.sql.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DatabaseDownloader {
    private String file1;
    private String file2;
    private DiskResultSet drs;
    
    public DatabaseDownloader(String f1, String f2) {
        file1 = f1;
        file2 = f2;
        
        drs = new DiskResultSet();
    }
    
    public DiskResultSet downloadResultSet(String ip, int port1, int port2, int int1, int int2) throws IOException {
        drs.closeRandomReads();
        
        byte[] intArr1 = new byte[4];
        byte[] intArr2 = new byte[4];
        
        intArr1[0] = (byte)((int1 >> 24) & 0xFF);
        intArr1[1] = (byte)((int1 >> 16) & 0xFF);
        intArr1[2] = (byte)((int1 >>  8) & 0xFF);
        intArr1[3] = (byte)((int1 >>  0) & 0xFF);
        
        intArr2[0] = (byte)((int2 >> 24) & 0xFF);
        intArr2[1] = (byte)((int2 >> 16) & 0xFF);
        intArr2[2] = (byte)((int2 >>  8) & 0xFF);
        intArr2[3] = (byte)((int2 >>  0) & 0xFF);
        
        
        ByteBuffer buffer = ByteBuffer.allocateDirect(4096);

        File f1 = new File(file1);
        File f2 = new File(file2);
        if(f1.exists()){
            f1.delete();
        }
        if(f2.exists()){
            f2.delete();
        }
        
        FileOutputStream fout1 = new FileOutputStream(f1);
        FileOutputStream fout2 = new FileOutputStream(f2);
        FileChannel fch1 = fout1.getChannel();
        FileChannel fch2 = fout2.getChannel();

        SocketAddress addr1 = new InetSocketAddress(ip, port1);
        SocketAddress addr2 = new InetSocketAddress(ip, port2);
        SocketChannel sock1 = SocketChannel.open(addr1);
        SocketChannel sock2 = SocketChannel.open(addr2);

        sock1.write(ByteBuffer.wrap(intArr1));
        sock2.write(ByteBuffer.wrap(intArr2));
        sock1.configureBlocking(false);
        sock2.configureBlocking(false);

        Selector selector = Selector.open();
        sock1.register(selector, SelectionKey.OP_READ);
        sock2.register(selector, SelectionKey.OP_READ);

        int dataRead = 0;
        while(sock1.isConnected() || sock2.isConnected()){
            int sel = selector.select(100);
            if(sel == 0){
                continue;
            }

            Iterator it = selector.selectedKeys().iterator();

            while (it.hasNext()) {
                SelectionKey selKey = (SelectionKey)it.next();
                it.remove();

                if(selKey.isReadable()){
                    SocketChannel sock = (SocketChannel)selKey.channel();
                    int read;
                    
                    if( (read = sock.read(buffer)) > 0) {
                        buffer.flip();
                        if(sock == sock1){
                            dataRead += read;
                            fch1.write(buffer);
                        } else {
                            fch2.write(buffer);
                        }
                        buffer.clear();
                    } else {
                        sock.close();
                    }
                }
            }
        }

        fch1.close();
        fch2.close();
        fout1.close();
        fout2.close();

        if(dataRead == 0){
            return null;
        }
        drs.setInputFiles(file1, file2);
        drs.prepareRandomReads();

        return drs;
    }
}