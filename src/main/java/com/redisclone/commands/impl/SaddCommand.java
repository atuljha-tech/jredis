package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;
import com.redisclone.storage.Database;
import com.redisclone.storage.value.SetValue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SaddCommand implements Command {
    private final Database db;

    public SaddCommand(Database db) { this.db = db; }

    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        if (args.size() < 3) {
            encoder.writeError("ERR wrong number of arguments for 'sadd'");
            return;
        }
        SetValue set = db.getOrCreateSet(args.get(1));
        int added = 0;
        for (int i = 2; i < args.size(); i++) {
            if (set.add(args.get(i).getBytes(StandardCharsets.UTF_8))) added++;
        }
        encoder.writeInteger(added);
    }
}