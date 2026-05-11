package com.hasan.miniredis.command;

import com.hasan.miniredis.MiniRedisStore;

public class TtlCommand implements Command {
    @Override
    public String execute(MiniRedisStore store, String[] args) {
        if (args.length != 2) return "HATA: TTL komutu 1 parametre alir. (Ornek: TTL mesaj)";

        long timeLeft = store.ttl(args[1]);
        return "(integer) " + timeLeft;
    }
}