package com.redisclone.protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class RespEncoder {

    private final OutputStream out;

    public RespEncoder(OutputStream out) {
        this.out = out;
    }

    /** +<message>\r\n — e.g., +OK\r\n */
    public void writeSimpleString(String message) throws IOException {
        write("+" + message + "\r\n");
    }

    /** -<message>\r\n — e.g., -ERR something went wrong\r\n */
    public void writeError(String message) throws IOException {
        write("-" + message + "\r\n");
    }

    /** :<number>\r\n — e.g., :42\r\n */
    public void writeInteger(long value) throws IOException {
        write(":" + value + "\r\n");
    }

    /** $<len>\r\n<data>\r\n — e.g., $5\r\nhello\r\n */
    public void writeBulkString(String value) throws IOException {
        if (value == null) {
            write("$-1\r\n"); // null bulk string
            return;
        }
        byte[] data = value.getBytes(StandardCharsets.UTF_8);
        write("$" + data.length + "\r\n");
        out.write(data);
        write("\r\n");
    }

    private void write(String s) throws IOException {
        out.write(s.getBytes(StandardCharsets.UTF_8));
    }

    public void flush() throws IOException {
        out.flush();
    }
    /** *<count>\r\n followed by <count> bulk strings. Used for LRANGE, SMEMBERS. */
public void writeBulkStringArray(java.util.List<String> items) throws IOException {
    write("*" + items.size() + "\r\n");
    for (String item : items) {
        writeBulkString(item);
    }
}

public void writeBulkStringArrayBytes(java.util.List<byte[]> items) throws IOException {
    write("*" + items.size() + "\r\n");
    for (byte[] item : items) {
        if (item == null) {
            write("$-1\r\n");
        } else {
            write("$" + item.length + "\r\n");
            out.write(item);
            write("\r\n");
        }
    }
}
}