package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;
import com.redisclone.storage.Database;
import com.redisclone.storage.value.ListValue;

import java.io.IOException;
import java.util.List;

public class LlenCommand implements Command {
    private final Database db;

    public LlenCommand(Database db) { this.db = db; }

    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        if (args.size() != 2) {
            encoder.writeError("ERR wrong number of arguments for 'llen'");
            return;
        }
        ListValue list = db.getList(args.get(1));
        encoder.writeInteger(list == null ? 0 : list.size());
    }
}