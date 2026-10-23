package com.redisclone.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RespEncoderTest {

    private String encodeToString(java.util.function.Consumer<RespEncoder> action) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RespEncoder encoder = new RespEncoder(out);
        action.accept(encoder);
        encoder.flush();
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void encodesSimpleString() throws Exception {
        String out = encodeToString(e -> {
            try { e.writeSimpleString("OK"); } catch (IOException ex) { throw new RuntimeException(ex); }
        });
        assertEquals("+OK\r\n", out);
    }

    @Test
    void encodesError() throws Exception {
        String out = encodeToString(e -> {
            try { e.writeError("ERR bad"); } catch (IOException ex) { throw new RuntimeException(ex); }
        });
        assertEquals("-ERR bad\r\n", out);
    }

    @Test
    void encodesInteger() throws Exception {
        String out = encodeToString(e -> {
            try { e.writeInteger(42); } catch (IOException ex) { throw new RuntimeException(ex); }
        });
        assertEquals(":42\r\n", out);
    }

    @Test
    void encodesBulkString() throws Exception {
        String out = encodeToString(e -> {
            try { e.writeBulkString("hello"); } catch (IOException ex) { throw new RuntimeException(ex); }
        });
        assertEquals("$5\r\nhello\r\n", out);
    }

    @Test
    void encodesNullBulkString() throws Exception {
        String out = encodeToString(e -> {
            try { e.writeBulkString(null); } catch (IOException ex) { throw new RuntimeException(ex); }
        });
        assertEquals("$-1\r\n", out);
    }

    @Test
    void encodesArrayOfStrings() throws Exception {
        String out = encodeToString(e -> {
            try { e.writeBulkStringArray(List.of("a", "b", "c")); } catch (IOException ex) { throw new RuntimeException(ex); }
        });
        assertEquals("*3\r\n$1\r\na\r\n$1\r\nb\r\n$1\r\nc\r\n", out);
    }
}