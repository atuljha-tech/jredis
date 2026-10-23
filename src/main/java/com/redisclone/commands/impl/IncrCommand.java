package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;
import com.redisclone.storage.Database;

import java.io.IOException;
import java.util.List;

public class IncrCommand implements Command {
    private final Database db;

    public IncrCommand(Database db) {
        this.db = db;
    }

    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        if (args.size() != 2) {
            encoder.writeError("ERR wrong number of arguments for 'incr'");
            return;
        }
        String key = args.get(1);
        String current = db.getString(key);
        long value = 1;

        if (current != null) {
            try {
                value = Long.parseLong(current);
            } catch (NumberFormatException e) {
                encoder.writeError("ERR value is not an integer or out of range");
                return;
            }
            if (value == Long.MAX_VALUE) {
                encoder.writeError("ERR increment would overflow");
                return;
            }
            value++;
        }

        db.setString(key, Long.toString(value));
        encoder.writeInteger(value);
    }
}