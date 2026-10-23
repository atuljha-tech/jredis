package com.redisclone.storage;

import com.redisclone.persistence.RdbSerializer;
import com.redisclone.storage.value.ListValue;
import com.redisclone.storage.value.RedisValue;
import com.redisclone.storage.value.SetValue;
import com.redisclone.storage.value.StringValue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Database {

    private final ConcurrentHashMap<String, RedisValue> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> expirations = new ConcurrentHashMap<>();
    private final ScheduledExecutorService janitor;

    public Database() {
        this.janitor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "redis-janitor");
            t.setDaemon(true);
            return t;
        });
    }

    // ============ Raw access ============

    public RedisValue getValue(String key) {
        if (isExpired(key)) {
            deleteExpired(key);
            return null;
        }
        return store.get(key);
    }

    public void setValue(String key, RedisValue value) {
        store.put(key, value);
        expirations.remove(key);
    }

    public boolean del(String key) {
        expirations.remove(key);
        return store.remove(key) != null;
    }

    public boolean exists(String key) {
        if (isExpired(key)) {
            deleteExpired(key);
            return false;
        }
        return store.containsKey(key);
    }

    public int size() {
        return store.size();
    }

    public void flushAll() {
        store.clear();
        expirations.clear();
    }

    // ============ String helpers ============

    public boolean setString(String key, String value) {
        setValue(key, new StringValue(value.getBytes(StandardCharsets.UTF_8)));
        return true;
    }

    public String getString(String key) {
        RedisValue v = getValue(key);
        if (v == null) return null;
        if (!(v instanceof StringValue sv)) {
            throw new WrongTypeException();
        }
        return new String(sv.data(), StandardCharsets.UTF_8);
    }

    public byte[] getStringBytes(String key) {
        RedisValue v = getValue(key);
        if (v == null) return null;
        if (!(v instanceof StringValue sv)) {
            throw new WrongTypeException();
        }
        return sv.data();
    }

    // ============ List helpers ============

    public ListValue getOrCreateList(String key) {
        RedisValue v = getValue(key);
        if (v == null) {
            ListValue list = new ListValue();
            store.put(key, list);
            return list;
        }
        if (!(v instanceof ListValue lv)) {
            throw new WrongTypeException();
        }
        return lv;
    }

    public ListValue getList(String key) {
        RedisValue v = getValue(key);
        if (v == null) return null;
        if (!(v instanceof ListValue lv)) {
            throw new WrongTypeException();
        }
        return lv;
    }

    // ============ Set helpers ============

    public SetValue getOrCreateSet(String key) {
        RedisValue v = getValue(key);
        if (v == null) {
            SetValue set = new SetValue();
            store.put(key, set);
            return set;
        }
        if (!(v instanceof SetValue sv)) {
            throw new WrongTypeException();
        }
        return sv;
    }

    public SetValue getSet(String key) {
        RedisValue v = getValue(key);
        if (v == null) return null;
        if (!(v instanceof SetValue sv)) {
            throw new WrongTypeException();
        }
        return sv;
    }

    // ============ Type ============

    public String type(String key) {
        RedisValue v = getValue(key);
        return v == null ? "none" : v.typeName();
    }

    // ============ TTL operations ============

    public boolean expire(String key, long seconds) {
        if (!store.containsKey(key)) return false;
        if (seconds <= 0) {
            del(key);
            return true;
        }
        expirations.put(key, System.currentTimeMillis() + (seconds * 1000L));
        return true;
    }

    public long ttl(String key) {
        if (isExpired(key)) { deleteExpired(key); return -2; }
        if (!store.containsKey(key)) return -2;
        Long expiresAt = expirations.get(key);
        if (expiresAt == null) return -1;
        long remaining = expiresAt - System.currentTimeMillis();
        if (remaining <= 0) { deleteExpired(key); return -2; }
        return remaining / 1000;
    }

    public long pttl(String key) {
        if (isExpired(key)) { deleteExpired(key); return -2; }
        if (!store.containsKey(key)) return -2;
        Long expiresAt = expirations.get(key);
        if (expiresAt == null) return -1;
        long remaining = expiresAt - System.currentTimeMillis();
        if (remaining <= 0) { deleteExpired(key); return -2; }
        return remaining;
    }

    public boolean persist(String key) {
        if (isExpired(key)) { deleteExpired(key); return false; }
        return expirations.remove(key) != null;
    }

    // ============ Lazy expiry ============

    private boolean isExpired(String key) {
        Long expiresAt = expirations.get(key);
        return expiresAt != null && System.currentTimeMillis() >= expiresAt;
    }

    private void deleteExpired(String key) {
        store.remove(key);
        expirations.remove(key);
    }

    // ============ Active expiry ============

    public void startActiveExpiry() {
        janitor.scheduleAtFixedRate(this::sweepExpired, 100, 100, TimeUnit.MILLISECONDS);
    }

    private void sweepExpired() {
        try {
            long now = System.currentTimeMillis();
            for (Map.Entry<String, Long> entry : expirations.entrySet()) {
                if (now >= entry.getValue()) {
                    store.remove(entry.getKey());
                    expirations.remove(entry.getKey());
                }
            }
        } catch (Exception e) {
            System.err.println("Janitor error: " + e.getMessage());
        }
    }

    public void close() {
        janitor.shutdown();
    }

    // ============ Persistence ============

    public Map<String, RdbSerializer.SnapshotEntry> snapshotData() {
        long now = System.currentTimeMillis();
        Map<String, RdbSerializer.SnapshotEntry> snapshot = new HashMap<>();

        for (Map.Entry<String, RedisValue> entry : store.entrySet()) {
            String key = entry.getKey();
            Long expiresAt = expirations.get(key);

            long ttl = -1;
            if (expiresAt != null) {
                long remaining = expiresAt - now;
                if (remaining <= 0) continue;
                ttl = remaining;
            }

            snapshot.put(key, new RdbSerializer.SnapshotEntry(entry.getValue(), ttl));
        }
        return snapshot;
    }

    public void restoreData(Map<String, RdbSerializer.SnapshotEntry> snapshot) {
        store.clear();
        expirations.clear();

        long now = System.currentTimeMillis();
        for (Map.Entry<String, RdbSerializer.SnapshotEntry> entry : snapshot.entrySet()) {
            String key = entry.getKey();
            RdbSerializer.SnapshotEntry se = entry.getValue();

            if (se.ttlMillis > 0) {
                store.put(key, se.value);
                expirations.put(key, now + se.ttlMillis);
            } else if (se.ttlMillis == -1) {
                store.put(key, se.value);
            }
        }
    }

    public void saveToFile(Path path) throws IOException {
        RdbSerializer.save(path, snapshotData());
    }

    public void bgSaveToFile(Path path) {
        Thread t = new Thread(() -> {
            try {
                saveToFile(path);
                System.out.println("[BGSAVE] Snapshot written to " + path);
            } catch (IOException e) {
                System.err.println("[BGSAVE] Failed: " + e.getMessage());
            }
        }, "redis-bgsave");
        t.setDaemon(true);
        t.start();
    }

    public void loadFromFile(Path path) throws IOException {
        Map<String, RdbSerializer.SnapshotEntry> loaded = RdbSerializer.load(path);
        restoreData(loaded);
        System.out.println("[LOAD] Restored " + loaded.size() + " keys from " + path);
    }

    // ============ Wrong-type exception ============

    public static class WrongTypeException extends RuntimeException {
        public WrongTypeException() {
            super("WRONGTYPE Operation against a key holding the wrong kind of value");
        }
    }
}