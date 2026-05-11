package com.hasan.miniredis.command;

import com.hasan.miniredis.MiniRedisStore;

public class SetCommand implements Command {
    @Override
    public String execute(MiniRedisStore store, String[] args) {
        if (args.length != 3) return "HATA: SET komutu 2 parametre alir. (Ornek: SET ad Hasan)";

        return store.set(args[1], args[2]);
    }
}