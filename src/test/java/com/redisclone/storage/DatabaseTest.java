package com.redisclone.storage;

import com.redisclone.persistence.RdbSerializer;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseTest {

    @Test
    void setAndGetString() {
        Database db = new Database();
        db.setString("k", "v");
        assertEquals("v", db.getString("k"));
    }

    @Test
    void getMissingKeyReturnsNull() {
        Database db = new Database();
        assertNull(db.getString("ghost"));
    }

    @Test
    void delRemovesKey() {
        Database db = new Database();
        db.setString("k", "v");
        assertTrue(db.del("k"));
        assertFalse(db.exists("k"));
    }

    @Test
    void expireAndTtl() {
        Database db = new Database();
        db.setString("k", "v");
        assertTrue(db.expire("k", 10));
        long ttl = db.ttl("k");
        assertTrue(ttl >= 9 && ttl <= 10);
    }

    @Test
    void ttlOnMissingKeyReturnsMinusTwo() {
        Database db = new Database();
        assertEquals(-2, db.ttl("ghost"));
    }

    @Test
    void ttlOnKeyWithoutExpiryReturnsMinusOne() {
        Database db = new Database();
        db.setString("k", "v");
        assertEquals(-1, db.ttl("k"));
    }

    @Test
    void setClearsTtl() {
        Database db = new Database();
        db.setString("k", "v");
        db.expire("k", 100);
        db.setString("k", "v2"); // must clear TTL
        assertEquals(-1, db.ttl("k"));
    }

    @Test
    void wrongTypeThrows() {
        Database db = new Database();
        db.setString("k", "v");
        assertThrows(Database.WrongTypeException.class, () -> db.getList("k"));
    }

    @Test
    void saveAndLoadRoundTrip() throws Exception {
        Path temp = Files.createTempFile("test-rdb", ".bin");
        try {
            Database db1 = new Database();
            db1.setString("s", "hello");
            db1.getOrCreateList("l").pushRight("a".getBytes());
            db1.getOrCreateList("l").pushRight("b".getBytes());
            db1.getOrCreateSet("st").add("x".getBytes());
            db1.expire("s", 60);
            db1.saveToFile(temp);

            Database db2 = new Database();
            db2.loadFromFile(temp);

            assertEquals("hello", db2.getString("s"));
            assertEquals(2, db2.getList("l").size());
            assertTrue(db2.getSet("st").contains("x".getBytes()));
            assertTrue(db2.ttl("s") > 0); // TTL persisted
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}