package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;
import com.redisclone.storage.Database;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class StrlenCommand implements Command {
    private final Database db;

    public StrlenCommand(Database db) {
        this.db = db;
    }

    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        if (args.size() != 2) {
            encoder.writeError("ERR wrong number of arguments for 'strlen'");
            return;
        }
        String value = db.getString(args.get(1));
        if (value == null) {
            encoder.writeInteger(0);
        } else {
            encoder.writeInteger(value.getBytes(StandardCharsets.UTF_8).length);
        }
    }
}