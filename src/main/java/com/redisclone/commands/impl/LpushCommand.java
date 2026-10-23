package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;
import com.redisclone.storage.Database;
import com.redisclone.storage.value.ListValue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LpushCommand implements Command {
    private final Database db;

    public LpushCommand(Database db) { this.db = db; }

    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        if (args.size() < 3) {
            encoder.writeError("ERR wrong number of arguments for 'lpush'");
            return;
        }
        ListValue list = db.getOrCreateList(args.get(1));
        for (int i = 2; i < args.size(); i++) {
            list.pushLeft(args.get(i).getBytes(StandardCharsets.UTF_8));
        }
        encoder.writeInteger(list.size());
    }
}