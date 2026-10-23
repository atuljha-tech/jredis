package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;
import com.redisclone.storage.Database;
import com.redisclone.storage.value.ListValue;

import java.io.IOException;
import java.util.List;

public class LpopCommand implements Command {
    private final Database db;

    public LpopCommand(Database db) { this.db = db; }

    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        if (args.size() != 2) {
            encoder.writeError("ERR wrong number of arguments for 'lpop'");
            return;
        }
        String key = args.get(1);
        ListValue list = db.getList(key);
        if (list == null) {
            encoder.writeBulkString(null);
            return;
        }
        byte[] item = list.popLeft();
        if (list.isEmpty()) {
            db.del(key); // Redis auto-deletes empty lists
        }
        encoder.writeBulkString(item == null ? null : new String(item, java.nio.charset.StandardCharsets.UTF_8));
    }
}