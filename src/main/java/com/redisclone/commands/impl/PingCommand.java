package com.redisclone.commands.impl;

import com.redisclone.commands.Command;
import com.redisclone.protocol.RespEncoder;

import java.io.IOException;
import java.util.List;

public class PingCommand implements Command {
    @Override
    public void execute(List<String> args, RespEncoder encoder) throws IOException {
        if (args.size() == 1) {
            encoder.writeSimpleString("PONG");
        } else {
            encoder.writeBulkString(args.get(1));
        }
    }
}