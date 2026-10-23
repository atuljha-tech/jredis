package com.redisclone;

import com.redisclone.server.RedisServer;

public class Main {
    public static void main(String[] args) {
        int port = 6379; // The default Redis port
        RedisServer server = new RedisServer(port);
        server.start();
    }
}