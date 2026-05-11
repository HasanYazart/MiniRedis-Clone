package com.hasan.miniredis.command;

import com.hasan.miniredis.MiniRedisStore;

public class GetCommand implements Command {
    @Override
    public String execute(MiniRedisStore store, String[] args) {
        if (args.length != 2) return "HATA: GET komutu 1 parametre alir. (Ornek: GET ad)";

        String result = store.get(args[1]);
        return result != null ? result : "(nil)";
    }
}