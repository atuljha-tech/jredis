package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;
import com.redisclone.storage.Database;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AppendCommand implements Command {
    private final Database db;

    public AppendCommand(Database db) {
        this.db = db;
    }

    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        if (args.size() != 3) {
            encoder.writeError("ERR wrong number of arguments for 'append'");
            return;
        }
        String key = args.get(1);
        String toAppend = args.get(2);
        String current = db.getString(key);
        String newValue = (current == null ? "" : current) + toAppend;
        db.setString(key, newValue);
        encoder.writeInteger(newValue.getBytes(StandardCharsets.UTF_8).length);
    }
}