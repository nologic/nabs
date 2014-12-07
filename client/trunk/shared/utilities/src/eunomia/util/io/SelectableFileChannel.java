package eunomia.util.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;

/**
 *
 * @author Mikhail Sosonkin
 */
public class SelectableFileChannel extends SelectableChannel implements ByteChannel, GatheringByteChannel, ScatteringByteChannel {
    private FileChannel channel;
    private HashMap selectorKeyMap;
    
    public SelectableFileChannel(FileChannel ch) {
        channel = ch;
    }

    public SelectorProvider provider() {
        return null;
    }

    public int validOps() {
        return SelectionKey.OP_READ | SelectionKey.OP_WRITE;
    }

    public boolean isRegistered() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SelectionKey keyFor(Selector sel) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SelectionKey register(Selector sel, int ops, Object att) throws ClosedChannelException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SelectableChannel configureBlocking(boolean block) throws IOException {
        return this;
    }

    public boolean isBlocking() {
        return false;
    }

    public Object blockingLock() {
        return this;
    }

    protected void implCloseChannel() throws IOException {
        channel.close();
    }

    public int read(ByteBuffer dst) throws IOException {
        return channel.read(dst);
    }

    public int write(ByteBuffer src) throws IOException {
        return channel.write(src);
    }

    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        return channel.write(srcs, offset, length);
    }

    public long write(ByteBuffer[] srcs) throws IOException {
        return channel.read(srcs);
    }

    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        return channel.read(dsts, offset, length);
    }

    public long read(ByteBuffer[] dsts) throws IOException {
        return channel.read(dsts);
    }

}