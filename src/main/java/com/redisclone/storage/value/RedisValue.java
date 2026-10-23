package com.redisclone.storage.value;

/**
 * A sealed interface representing any value Redis can store.
 * Only StringValue, ListValue, or SetValue can implement it.
 */
public sealed interface RedisValue permits StringValue, ListValue, SetValue {
    /** The Redis type name ("string", "list", "set"). */
    String typeName();
}