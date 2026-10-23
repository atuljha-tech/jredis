package com.redisclone.commands;

import com.redisclone.commands.impl.*;
import com.redisclone.storage.Database;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {

    private final Map<String, Command> commands = new HashMap<>();

    public CommandRegistry(Database db, Path rdbPath) {
        // Basic
        register("PING",     new PingCommand());
        register("ECHO",     new EchoCommand());
        register("DBSIZE",   new DbsizeCommand(db));
        register("FLUSHALL", new FlushAllCommand(db));

        // String storage
        register("SET",      new SetCommand(db));
        register("GET",      new GetCommand(db));
        register("DEL",      new DelCommand(db));
        register("EXISTS",   new ExistsCommand(db));
        register("TYPE",     new TypeCommand(db));

        // Arithmetic & string ops
        register("INCR",     new IncrCommand(db));
        register("DECR",     new DecrCommand(db));
        register("APPEND",   new AppendCommand(db));
        register("STRLEN",   new StrlenCommand(db));

        // TTL
        register("EXPIRE",   new ExpireCommand(db));
        register("TTL",      new TtlCommand(db));
        register("PTTL",     new PttlCommand(db));
        register("PERSIST",  new PersistCommand(db));

        // Lists
        register("LPUSH",    new LpushCommand(db));
        register("RPUSH",    new RpushCommand(db));
        register("LPOP",     new LpopCommand(db));
        register("RPOP",     new RpopCommand(db));
        register("LLEN",     new LlenCommand(db));
        register("LRANGE",   new LrangeCommand(db));

        // Sets
        register("SADD",      new SaddCommand(db));
        register("SMEMBERS",  new SmembersCommand(db));
        register("SISMEMBER", new SismemberCommand(db));
        register("SCARD",     new ScardCommand(db));
        register("SREM",      new SremCommand(db));

        // Persistence
        register("SAVE",      new SaveCommand(db, rdbPath));
        register("BGSAVE",    new BgsaveCommand(db, rdbPath));
    }

    private void register(String name, Command command) {
        commands.put(name.toUpperCase(), command);
    }

    public Command get(String name) {
        return commands.get(name.toUpperCase());
    }
}