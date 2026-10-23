package com.redisclone.storage.value;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A Redis list: ordered, non-unique strings.
 * Uses ArrayDeque for O(1) push/pop at both ends.
 */
public final class ListValue implements RedisValue {

    private final Deque<byte[]> items = new ArrayDeque<>();

    public void pushLeft(byte[] item) {
        items.addFirst(item);
    }

    public void pushRight(byte[] item) {
        items.addLast(item);
    }

    public byte[] popLeft() {
        return items.pollFirst();
    }

    public byte[] popRight() {
        return items.pollLast();
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Redis LRANGE semantics:
     * - Negative indexes count from the end (-1 = last)
     * - Out-of-bounds indexes are clamped
     * - If start > stop after normalization, returns empty
     */
    public List<byte[]> range(int start, int stop) {
        int n = items.size();
        // Normalize negative indexes
        if (start < 0) start = Math.max(0, n + start);
        if (stop < 0) stop = Math.max(0, n + stop);
        // Clamp
        if (start >= n || start > stop) return List.of();
        if (stop >= n) stop = n - 1;

        List<byte[]> result = new ArrayList<>(stop - start + 1);
        int i = 0;
        for (byte[] item : items) {
            if (i > stop) break;
            if (i >= start) result.add(item);
            i++;
        }
        return result;
    }

    @Override
    public String typeName() {
        return "list";
    }
}