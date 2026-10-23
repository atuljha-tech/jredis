package com.redisclone.commands;

import com.redisclone.protocol.RespEncoder;
import com.redisclone.storage.Database;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class CommandHandler {

    private final CommandRegistry registry;

    public CommandHandler(Database db, Path rdbPath) {
        this.registry = new CommandRegistry(db, rdbPath);
    }

    public void handle(List<String> command, RespEncoder encoder) throws IOException {
        if (command.isEmpty()) {
            encoder.writeError("ERR empty command");
            return;
        }

        Command cmd = registry.get(command.get(0));
        if (cmd == null) {
            encoder.writeError("ERR unknown command '" + command.get(0) + "'");
            return;
        }

        try {
            cmd.execute(command, encoder);
        } catch (Database.WrongTypeException e) {
            encoder.writeError(e.getMessage());
        }
    }
}