package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;
import com.redisclone.storage.Database;
import com.redisclone.storage.value.SetValue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SismemberCommand implements Command {
    private final Database db;

    public SismemberCommand(Database db) { this.db = db; }

    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        if (args.size() != 3) {
            encoder.writeError("ERR wrong number of arguments for 'sismember'");
            return;
        }
        SetValue set = db.getSet(args.get(1));
        if (set == null) {
            encoder.writeInteger(0);
            return;
        }
        boolean present = set.contains(args.get(2).getBytes(StandardCharsets.UTF_8));
        encoder.writeInteger(present ? 1 : 0);
    }
}