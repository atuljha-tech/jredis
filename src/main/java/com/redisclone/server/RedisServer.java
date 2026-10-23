package com.redisclone.server;

import com.redisclone.storage.Database;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RedisServer {

    private final int port;
    private final Database db;
    private final Path rdbPath;
    private boolean running = true;

    public RedisServer(int port) {
        this.port = port;
        this.db = new Database();
        this.rdbPath = Paths.get("dump.rdb");
    }

    public void start() {
        // Load previous snapshot before starting the janitor or accepting clients
        try {
            db.loadFromFile(rdbPath);
        } catch (IOException e) {
            System.err.println("[LOAD] Failed to load snapshot: " + e.getMessage());
            // Continue anyway — fresh start
        }

        db.startActiveExpiry();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Redis Server started on port " + port);
            System.out.println("Waiting for clients...");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());
                Thread t = new Thread(new ClientHandler(clientSocket, db, rdbPath));
                t.start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            db.close();
        }
    }
}