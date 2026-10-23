package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;
import com.redisclone.storage.Database;
import com.redisclone.storage.value.SetValue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SremCommand implements Command {
    private final Database db;

    public SremCommand(Database db) { this.db = db; }

    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        if (args.size() < 3) {
            encoder.writeError("ERR wrong number of arguments for 'srem'");
            return;
        }
        String key = args.get(1);
        SetValue set = db.getSet(key);
        if (set == null) {
            encoder.writeInteger(0);
            return;
        }
        int removed = 0;
        for (int i = 2; i < args.size(); i++) {
            if (set.remove(args.get(i).getBytes(StandardCharsets.UTF_8))) removed++;
        }
        if (set.isEmpty()) {
            db.del(key);
        }
        encoder.writeInteger(removed);
    }
}