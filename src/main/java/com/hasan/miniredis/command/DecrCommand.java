package com.hasan.miniredis.command;
import com.hasan.miniredis.MiniRedisStore;

public class DecrCommand implements Command {
    @Override
    public String execute(MiniRedisStore store, String[] args) {
        if (args.length != 2) return "HATA: DECR komutu 1 parametre alir. (Ornek: DECR stok)";
        return store.decr(args[1]);
    }
}