package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;
import com.redisclone.storage.Database;
import com.redisclone.storage.value.ListValue;

import java.io.IOException;
import java.util.List;

public class LrangeCommand implements Command {
    private final Database db;

    public LrangeCommand(Database db) { this.db = db; }

    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        if (args.size() != 4) {
            encoder.writeError("ERR wrong number of arguments for 'lrange'");
            return;
        }
        int start, stop;
        try {
            start = Integer.parseInt(args.get(2));
            stop = Integer.parseInt(args.get(3));
        } catch (NumberFormatException e) {
            encoder.writeError("ERR value is not an integer or out of range");
            return;
        }
        ListValue list = db.getList(args.get(1));
        if (list == null) {
            encoder.writeBulkStringArrayBytes(List.of());
            return;
        }
        encoder.writeBulkStringArrayBytes(list.range(start, stop));
    }
}