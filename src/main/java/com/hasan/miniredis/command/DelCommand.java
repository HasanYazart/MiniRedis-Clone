package com.hasan.miniredis.command;

import com.hasan.miniredis.MiniRedisStore;

public class DelCommand implements Command {
    @Override
    public String execute(MiniRedisStore store, String[] args) {
        if (args.length != 2) return "HATA: DEL komutu 1 parametre alir. (Ornek: DEL ad)";

        int result = store.del(args[1]);
        return "(integer) " + result;
    }
}