package com.redisclone.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RespParser {

    private final InputStream in;

    public RespParser(InputStream in) {
        this.in = in;
    }

    /**
     * Parses a complete RESP command from the client.
     * Returns a List<String> representing the command (e.g., ["SET", "name", "John"]).
     * Returns null if the client disconnected cleanly (EOF).
     */
    public List<String> parseCommand() throws IOException, RespException {
        int firstByte = in.read();
        if (firstByte == -1) {
            return null; // client closed connection
        }

        char type = (char) firstByte;

        if (type == '*') {
            return parseArray();
        } else {
            throw new RespException("Expected '*' at start of command, got '" + type + "'");
        }
    }

    /**
     * Parses an array: *<count>\r\n followed by <count> bulk strings.
     */
    private List<String> parseArray() throws IOException, RespException {
        int count = readInteger();
        List<String> items = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            int marker = in.read();
            if (marker == -1) {
                throw new RespException("Unexpected end of stream in array");
            }
            char type = (char) marker;
            if (type == '$') {
                items.add(readBulkString());
            } else {
                throw new RespException("Expected '$' in array, got '" + type + "'");
            }
        }
        return items;
    }

    /**
     * Reads an integer terminated by \r\n (used for array counts and bulk string lengths).
     */
    private int readInteger() throws IOException, RespException {
        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\r') {
                int next = in.read();
                if (next != '\n') {
                    throw new RespException("Expected \\n after \\r");
                }
                break;
            }
            sb.append((char) b);
        }
        if (sb.length() == 0) {
            throw new RespException("Empty integer");
        }
        try {
            return Integer.parseInt(sb.toString());
        } catch (NumberFormatException e) {
            throw new RespException("Invalid integer: " + sb, e);
        }
    }

    /**
     * Reads a bulk string: $<len>\r\n<len bytes of data>\r\n
     */
    private String readBulkString() throws IOException, RespException {
        int length = readInteger();
        if (length == -1) {
            return null; // null bulk string (Redis uses this for missing keys)
        }

        byte[] data = new byte[length];
        int read = 0;
        while (read < length) {
            int n = in.read(data, read, length - read);
            if (n == -1) {
                throw new RespException("Unexpected end of stream in bulk string");
            }
            read += n;
        }

        // Consume the trailing \r\n
        int cr = in.read();
        int lf = in.read();
        if (cr != '\r' || lf != '\n') {
            throw new RespException("Expected \\r\\n after bulk string data");
        }

        return new String(data, java.nio.charset.StandardCharsets.UTF_8);
    }
}