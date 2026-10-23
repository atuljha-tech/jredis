package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;
import com.redisclone.storage.Database;
import com.redisclone.storage.value.SetValue;

import java.io.IOException;
import java.util.List;

public class ScardCommand implements Command {
    private final Database db;

    public ScardCommand(Database db) { this.db = db; }

    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        if (args.size() != 2) {
            encoder.writeError("ERR wrong number of arguments for 'scard'");
            return;
        }
        SetValue set = db.getSet(args.get(1));
        encoder.writeInteger(set == null ? 0 : set.size());
    }
}