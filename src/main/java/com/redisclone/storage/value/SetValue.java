package com.redisclone.storage.value;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A Redis set: unordered, unique strings.
 * Uses a wrapper to make byte[] work with hashCode/equals correctly.
 */
public final class SetValue implements RedisValue {

    // We need a wrapper because byte[] doesn't override equals/hashCode
    private final Set<ByteArrayWrapper> members = new LinkedHashSet<>();

    public boolean add(byte[] member) {
        return members.add(new ByteArrayWrapper(member));
    }

    public boolean remove(byte[] member) {
        return members.remove(new ByteArrayWrapper(member));
    }

    public boolean contains(byte[] member) {
        return members.contains(new ByteArrayWrapper(member));
    }

    public int size() {
        return members.size();
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public List<byte[]> members() {
        return members.stream().map(ByteArrayWrapper::data).toList();
    }

    @Override
    public String typeName() {
        return "set";
    }

    /** Wrapper so byte[] respects content-based equality in a HashSet. */
    private static final class ByteArrayWrapper {
        private final byte[] data;

        ByteArrayWrapper(byte[] data) {
            this.data = data;
        }

        byte[] data() {
            return data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ByteArrayWrapper other)) return false;
            return Arrays.equals(data, other.data);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(data);
        }
    }
}