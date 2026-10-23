package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;
import com.redisclone.storage.Database;

import java.io.IOException;
import java.util.List;

public class ExistsCommand implements Command {
    private final Database db;

    public ExistsCommand(Database db) {
        this.db = db;
    }

    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        if (args.size() < 2) {
            encoder.writeError("ERR wrong number of arguments for 'exists'");
            return;
        }
        long count = 0;
        for (int i = 1; i < args.size(); i++) {
            if (db.exists(args.get(i))) count++;
        }
        encoder.writeInteger(count);
    }
}