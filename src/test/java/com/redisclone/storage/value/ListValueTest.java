package com.redisclone.storage.value;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListValueTest {

    private static byte[] b(String s) { return s.getBytes(StandardCharsets.UTF_8); }
    private static String s(byte[] b) { return new String(b, StandardCharsets.UTF_8); }

    @Test
    void pushLeftProducesReverseOrder() {
        ListValue lv = new ListValue();
        lv.pushLeft(b("a"));
        lv.pushLeft(b("b"));
        lv.pushLeft(b("c"));
        List<byte[]> items = lv.range(0, -1);
        assertEquals(List.of("c", "b", "a"), items.stream().map(ListValueTest::s).toList());
    }

    @Test
    void pushRightPreservesOrder() {
        ListValue lv = new ListValue();
        lv.pushRight(b("a"));
        lv.pushRight(b("b"));
        lv.pushRight(b("c"));
        List<byte[]> items = lv.range(0, -1);
        assertEquals(List.of("a", "b", "c"), items.stream().map(ListValueTest::s).toList());
    }

    @Test
    void rangeHandlesNegativeIndexes() {
        ListValue lv = new ListValue();
        for (String v : List.of("a", "b", "c", "d", "e")) lv.pushRight(b(v));
        assertEquals(List.of("d", "e"), lv.range(-2, -1).stream().map(ListValueTest::s).toList());
        assertEquals(List.of("a", "b", "c", "d", "e"), lv.range(0, -1).stream().map(ListValueTest::s).toList());
    }

    @Test
    void rangeClampsOutOfBounds() {
        ListValue lv = new ListValue();
        lv.pushRight(b("a"));
        assertTrue(lv.range(100, 200).isEmpty());
        assertTrue(lv.range(5, 10).isEmpty());
    }

    @Test
    void popOnEmptyReturnsNull() {
        ListValue lv = new ListValue();
        assertNull(lv.popLeft());
        assertNull(lv.popRight());
    }
}