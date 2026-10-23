package com.redisclone.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RespParserTest {

    private RespParser parserFor(String input) {
        return new RespParser(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void parsesSingleBulkStringCommand() throws Exception {
        // PING → *1\r\n$4\r\nPING\r\n
        RespParser p = parserFor("*1\r\n$4\r\nPING\r\n");
        List<String> cmd = p.parseCommand();
        assertEquals(List.of("PING"), cmd);
    }

    @Test
    void parsesMultiArgumentCommand() throws Exception {
        // SET name John
        RespParser p = parserFor("*3\r\n$3\r\nSET\r\n$4\r\nname\r\n$4\r\nJohn\r\n");
        List<String> cmd = p.parseCommand();
        assertEquals(List.of("SET", "name", "John"), cmd);
    }

    @Test
    void returnsNullOnCleanEof() throws Exception {
        RespParser p = parserFor("");
        assertNull(p.parseCommand());
    }

    @Test
    void rejectsPlainTextInput() {
        RespParser p = parserFor("PING\r\n");
        assertThrows(RespException.class, p::parseCommand);
    }

    @Test
    void parsesMultipleConsecutiveCommands() throws Exception {
        RespParser p = parserFor(
                "*1\r\n$4\r\nPING\r\n" +
                "*2\r\n$4\r\nECHO\r\n$5\r\nhello\r\n"
        );
        assertEquals(List.of("PING"), p.parseCommand());
        assertEquals(List.of("ECHO", "hello"), p.parseCommand());
        assertNull(p.parseCommand());
    }
}