package com.hasan.miniredis.command;
import com.hasan.miniredis.MiniRedisStore;

public class IncrCommand implements Command {
    @Override
    public String execute(MiniRedisStore store, String[] args) {
        if (args.length != 2) return "HATA: INCR komutu 1 parametre alir. (Ornek: INCR bilet)";
        return store.incr(args[1]);
    }
}