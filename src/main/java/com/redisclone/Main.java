package com.redisclone;

import com.redisclone.server.RedisServer;

public class Main {
    public static void main(String[] args) {
        int port = 6379; // Default Redis port
        
        // Parse --port argument
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--port") && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid port number. Using default: 6379");
                }
            }
        }

        RedisServer server = new RedisServer(port);
        server.start();
    }
}