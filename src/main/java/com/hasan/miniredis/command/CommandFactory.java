package com.hasan.miniredis.command;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    private final Map<String, Command> commands = new HashMap<>();

    public CommandFactory() {
        commands.put("GET", new GetCommand());
        commands.put("SET", new SetCommand());
        commands.put("DEL", new DelCommand());
        commands.put("SETEX", new SetexCommand());
        commands.put("TTL", new TtlCommand());

        // YENİ EKLENEN FİNAL SİLAHLARI
        commands.put("INCR", new IncrCommand());
        commands.put("DECR", new DecrCommand());
        commands.put("INFO", new InfoCommand());
        commands.put("FLUSHDB", new FlushdbCommand());
    }

    public Command getCommand(String commandName) {
        return commands.get(commandName.toUpperCase());
    }
}