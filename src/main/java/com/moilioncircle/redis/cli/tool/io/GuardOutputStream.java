package com.moilioncircle.redis.cli.tool.io;

import com.moilioncircle.redis.cli.tool.glossary.Guard;
import com.moilioncircle.redis.cli.tool.util.OutputStreams;
import com.moilioncircle.redis.replicator.util.ByteBuilder;

import java.io.IOException;
import java.io.OutputStream;

import static com.moilioncircle.redis.cli.tool.glossary.Guard.DRAIN;
import static com.moilioncircle.redis.cli.tool.glossary.Guard.SAVE;


/**
 * @author Baoyi Chen
 */
public class GuardOutputStream extends OutputStream {
    
    private final int cap;
    private OutputStream out;
    private Guard guard = SAVE;
    private ByteBuilder builder;
    
    
    public GuardOutputStream(int cap, OutputStream out) {
        this.cap = cap;
        this.out = out;
        this.builder = ByteBuilder.allocate(cap);
    }
    
    public void setGuard(Guard guard) {
        this.guard = guard;
    }
    
    public void reset(OutputStream out) {
        OutputStreams.closeQuietly(this.out);
        this.out = out;
        this.builder.clear();
    }
    
    public byte[] array() {
        return builder.array();
    }
    
    @Override
    public void write(int b) throws IOException {
        if (guard == DRAIN) {
            if (out == null) return;
            if (builder.length() > 0) {
                byte[] ary = builder.array();
                out.write(ary);
                builder.clear();
            }
            out.write(b);
        } else if (guard == SAVE) {
            builder.put((byte) b);
        } else {
            if (builder.length() > 0) {
                builder.clear();
            }
        }
    }
    
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
    
    public void write(byte[] b, int off, int len) throws IOException {
        for (int i = off; i < len; i++) write(b[i]);
    }
    
    public void flush() throws IOException {
        if (out != null) out.flush();
    }
    
    public void close() throws IOException {
        if (out != null) out.close();
    }
}