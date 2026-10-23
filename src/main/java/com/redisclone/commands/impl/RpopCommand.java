package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;
import com.redisclone.storage.Database;
import com.redisclone.storage.value.ListValue;

import java.io.IOException;
import java.util.List;

public class RpopCommand implements Command {
    private final Database db;

    public RpopCommand(Database db) { this.db = db; }

    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        if (args.size() != 2) {
            encoder.writeError("ERR wrong number of arguments for 'rpop'");
            return;
        }
        String key = args.get(1);
        ListValue list = db.getList(key);
        if (list == null) {
            encoder.writeBulkString(null);
            return;
        }
        byte[] item = list.popRight();
        if (list.isEmpty()) {
            db.del(key);
        }
        encoder.writeBulkString(item == null ? null : new String(item, java.nio.charset.StandardCharsets.UTF_8));
    }
}