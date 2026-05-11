package com.hasan.miniredis.command;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    private final Map<String, Command> commands = new HashMap<>();

    public CommandFactory() {
        // Eski komutlar
        commands.put("GET", new GetCommand());
        commands.put("SET", new SetCommand());

        // YENİ: Sisteme eklediğimiz diğer komutlar
        commands.put("DEL", new DelCommand());
        commands.put("SETEX", new SetexCommand());
        commands.put("TTL", new TtlCommand());
    }

    public Command getCommand(String commandName) {
        return commands.get(commandName.toUpperCase());
    }
}