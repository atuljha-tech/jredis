package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;

import java.io.IOException;
import java.util.List;

public class EchoCommand implements Command {
    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        if (args.size() != 2) {
            encoder.writeError("ERR wrong number of arguments for 'echo'");
            return;
        }
        encoder.writeBulkString(args.get(1));
    }
}