package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;
import com.redisclone.storage.Database;

import java.io.IOException;
import java.util.List;

public class DelCommand implements Command {
    private final Database db;

    public DelCommand(Database db) {
        this.db = db;
    }

    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        if (args.size() < 2) {
            encoder.writeError("ERR wrong number of arguments for 'del'");
            return;
        }
        long deleted = 0;
        for (int i = 1; i < args.size(); i++) {
            if (db.del(args.get(i))) deleted++;
        }
        encoder.writeInteger(deleted);
    }
}
