package com.redisclone.commands;

import com.redisclone.protocol.RespEncoder;

import java.io.IOException;
import java.util.List;

/**
 * Contract for all Redis commands.
 * args[0] is the command name (e.g., "SET"), subsequent items are arguments.
 */
public interface Command {
    void execute(List<String> args, RespEncoder encoder) throws IOException;
}