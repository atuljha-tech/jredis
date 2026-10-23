package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;
import com.redisclone.storage.Database;

import java.io.IOException;
import java.util.List;

public class DbsizeCommand implements Command {
    private final Database db;

    public DbsizeCommand(Database db) {
        this.db = db;
    }

    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        encoder.writeInteger(db.size());
    }
}