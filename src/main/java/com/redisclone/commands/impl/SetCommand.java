package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;
import com.redisclone.storage.Database;

import java.io.IOException;
import java.util.List;

public class SetCommand implements Command {
    private final Database db;

    public SetCommand(Database db) {
        this.db = db;
    }

    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        if (args.size() != 3) {
            encoder.writeError("ERR wrong number of arguments for 'set'");
            return;
        }
        db.setString(args.get(1), args.get(2));
        encoder.writeSimpleString("OK");
    }
}