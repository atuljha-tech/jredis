package com.redisclone.persistence;

import com.redisclone.storage.value.ListValue;
import com.redisclone.storage.value.RedisValue;
import com.redisclone.storage.value.SetValue;
import com.redisclone.storage.value.StringValue;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Binary serializer for our Redis snapshot (RDB-like format).
 *
 * File format:
 *   MAGIC        (8 bytes: "REDIS001")
 *   KEY_COUNT    (4 bytes int)
 *   For each key:
 *     KEY_LEN    (4 bytes int)
 *     KEY_BYTES
 *     TYPE       (1 byte: 0=string, 1=list, 2=set)
 *     VALUE_PAYLOAD (depends on type)
 *     TTL_MILLIS (8 bytes long; -1 = no TTL)
 *
 * Value payloads:
 *   string: LEN(4) DATA
 *   list:   COUNT(4) then COUNT × [LEN(4) DATA]
 *   set:    COUNT(4) then COUNT × [LEN(4) DATA]
 */
public class RdbSerializer {

    private static final byte[] MAGIC = "REDIS001".getBytes(StandardCharsets.US_ASCII);

    private static final byte TYPE_STRING = 0;
    private static final byte TYPE_LIST   = 1;
    private static final byte TYPE_SET    = 2;

    public static class SnapshotEntry {
        public final RedisValue value;
        public final long ttlMillis; // -1 = no TTL, >0 = remaining ms

        public SnapshotEntry(RedisValue value, long ttlMillis) {
            this.value = value;
            this.ttlMillis = ttlMillis;
        }
    }

    // ============ SAVE (write) ============

    /**
     * Writes the snapshot to a temp file, fsyncs, then atomically renames to the target.
     * This is the crash-safe pattern.
     */
    public static void save(Path target, Map<String, SnapshotEntry> data) throws IOException {
        Path temp = target.resolveSibling(target.getFileName() + ".tmp");

        try (FileOutputStream fos = new FileOutputStream(temp.toFile());
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             DataOutputStream out = new DataOutputStream(bos)) {

            out.write(MAGIC);
            out.writeInt(data.size());

            for (Map.Entry<String, SnapshotEntry> entry : data.entrySet()) {
                String key = entry.getKey();
                SnapshotEntry se = entry.getValue();

                writeBytes(out, key.getBytes(StandardCharsets.UTF_8));
                writeValue(out, se.value);
                out.writeLong(se.ttlMillis);
            }

            out.flush();
            // Force the OS to actually write to disk (not just the page cache)
            fos.getFD().sync();
        }

        // Atomic rename — on POSIX filesystems this is guaranteed all-or-nothing
        Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private static void writeValue(DataOutputStream out, RedisValue value) throws IOException {
        if (value instanceof StringValue sv) {
            out.writeByte(TYPE_STRING);
            writeBytes(out, sv.data());
        } else if (value instanceof ListValue lv) {
            out.writeByte(TYPE_LIST);
            List<byte[]> items = lv.range(0, -1);
            out.writeInt(items.size());
            for (byte[] item : items) writeBytes(out, item);
        } else if (value instanceof SetValue sv) {
            out.writeByte(TYPE_SET);
            List<byte[]> members = sv.members();
            out.writeInt(members.size());
            for (byte[] m : members) writeBytes(out, m);
        } else {
            throw new IOException("Unknown value type: " + value.getClass());
        }
    }

    private static void writeBytes(DataOutputStream out, byte[] data) throws IOException {
        out.writeInt(data.length);
        out.write(data);
    }

    // ============ LOAD (read) ============

    /**
     * Reads a snapshot file. If the file doesn't exist, returns an empty map.
     */
    public static Map<String, SnapshotEntry> load(Path target) throws IOException {
        Map<String, SnapshotEntry> result = new HashMap<>();
        if (!Files.exists(target)) {
            return result;
        }

        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(target)))) {

            byte[] magic = new byte[8];
            in.readFully(magic);
            if (!java.util.Arrays.equals(magic, MAGIC)) {
                throw new IOException("Invalid RDB magic number — file is corrupt or from a different version");
            }

            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                String key = new String(readBytes(in), StandardCharsets.UTF_8);
                RedisValue value = readValue(in);
                long ttl = in.readLong();
                result.put(key, new SnapshotEntry(value, ttl));
            }
        }
        return result;
    }

    private static RedisValue readValue(DataInputStream in) throws IOException {
        byte type = in.readByte();
        switch (type) {
            case TYPE_STRING:
                return new StringValue(readBytes(in));
            case TYPE_LIST: {
                int n = in.readInt();
                ListValue lv = new ListValue();
                for (int i = 0; i < n; i++) lv.pushRight(readBytes(in));
                return lv;
            }
            case TYPE_SET: {
                int n = in.readInt();
                SetValue sv = new SetValue();
                for (int i = 0; i < n; i++) sv.add(readBytes(in));
                return sv;
            }
            default:
                throw new IOException("Unknown value type byte: " + type);
        }
    }

    private static byte[] readBytes(DataInputStream in) throws IOException {
        int len = in.readInt();
        if (len < 0 || len > 100_000_000) {
            throw new IOException("Invalid length: " + len);
        }
        byte[] data = new byte[len];
        in.readFully(data);
        return data;
    }
}