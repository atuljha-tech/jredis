package com.redisclone.storage.value;

public final class StringValue implements RedisValue {

    private final byte[] data;

    public StringValue(byte[] data) {
        this.data = data;
    }

    public byte[] data() {
        return data;
    }

    @Override
    public String typeName() {
        return "string";
    }
}