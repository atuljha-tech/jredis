package com.redisclone.server;

import com.redisclone.commands.CommandHandler;
import com.redisclone.protocol.RespEncoder;
import com.redisclone.protocol.RespException;
import com.redisclone.protocol.RespParser;
import com.redisclone.storage.Database;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final Database db;
    private final Path rdbPath;

    public ClientHandler(Socket socket, Database db, Path rdbPath) {
        this.clientSocket = socket;
        this.db = db;
        this.rdbPath = rdbPath;
    }

    @Override
    public void run() {
        try {
            RespParser parser = new RespParser(clientSocket.getInputStream());
            RespEncoder encoder = new RespEncoder(clientSocket.getOutputStream());
            CommandHandler commandHandler = new CommandHandler(db, rdbPath);

            List<String> command;
            while ((command = parser.parseCommand()) != null) {
                commandHandler.handle(command, encoder);
                encoder.flush();
            }
            System.out.println("Client disconnected: " + clientSocket.getRemoteSocketAddress());

        } catch (RespException e) {
            System.err.println("Protocol error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }
}