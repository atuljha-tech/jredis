package com.redisclone.storage.value;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class SetValueTest {

    private static byte[] b(String s) { return s.getBytes(StandardCharsets.UTF_8); }

    @Test
    void addIgnoresDuplicates() {
        SetValue sv = new SetValue();
        assertTrue(sv.add(b("a")));
        assertFalse(sv.add(b("a"))); // duplicate
        assertFalse(sv.add(b("a")));
        assertEquals(1, sv.size());
    }

    @Test
    void byteArrayEqualityIsContentBased() {
        SetValue sv = new SetValue();
        sv.add("hello".getBytes(StandardCharsets.UTF_8));
        // Different byte[] instance, same contents
        assertTrue(sv.contains("hello".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void removeReturnsTrueOnlyIfPresent() {
        SetValue sv = new SetValue();
        sv.add(b("a"));
        assertTrue(sv.remove(b("a")));
        assertFalse(sv.remove(b("a")));
    }

    @Test
    void isEmptyAfterRemovingAll() {
        SetValue sv = new SetValue();
        sv.add(b("x"));
        sv.add(b("y"));
        sv.remove(b("x"));
        sv.remove(b("y"));
        assertTrue(sv.isEmpty());
    }
}