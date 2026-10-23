package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;
import com.redisclone.storage.Database;
import com.redisclone.storage.value.SetValue;

import java.io.IOException;
import java.util.List;

public class SmembersCommand implements Command {
    private final Database db;

    public SmembersCommand(Database db) { this.db = db; }

    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        if (args.size() != 2) {
            encoder.writeError("ERR wrong number of arguments for 'smembers'");
            return;
        }
        SetValue set = db.getSet(args.get(1));
        if (set == null) {
            encoder.writeBulkStringArrayBytes(List.of());
            return;
        }
        encoder.writeBulkStringArrayBytes(set.members());
    }
}