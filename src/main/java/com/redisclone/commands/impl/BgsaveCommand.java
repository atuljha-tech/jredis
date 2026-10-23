package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;
import com.redisclone.storage.Database;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class BgsaveCommand implements Command {
    private final Database db;
    private final Path rdbPath;

    public BgsaveCommand(Database db, Path rdbPath) {
        this.db = db;
        this.rdbPath = rdbPath;
    }

    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        if (args.size() != 1) {
            encoder.writeError("ERR wrong number of arguments for 'bgsave'");
            return;
        }
        db.bgSaveToFile(rdbPath);
        encoder.writeSimpleString("Background saving started");
    }
}