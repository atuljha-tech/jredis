package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;
import com.redisclone.storage.Database;

import java.io.IOException;
import java.util.List;

public class ExpireCommand implements Command {
    private final Database db;

    public ExpireCommand(Database db) {
        this.db = db;
    }

    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        if (args.size() != 3) {
            encoder.writeError("ERR wrong number of arguments for 'expire'");
            return;
        }
        String key = args.get(1);
        long seconds;
        try {
            seconds = Long.parseLong(args.get(2));
        } catch (NumberFormatException e) {
            encoder.writeError("ERR value is not an integer or out of range");
            return;
        }
        encoder.writeInteger(db.expire(key, seconds) ? 1 : 0);
    }
}