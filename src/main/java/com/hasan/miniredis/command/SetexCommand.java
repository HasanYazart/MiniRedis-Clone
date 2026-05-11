package com.hasan.miniredis.command;

import com.hasan.miniredis.MiniRedisStore;

public class SetexCommand implements Command {
    @Override
    public String execute(MiniRedisStore store, String[] args) {
        if (args.length != 4) return "HATA: SETEX komutu 3 parametre alir. (Ornek: SETEX mesaj 15 selam)";

        try {
            int seconds = Integer.parseInt(args[2]);
            return store.setex(args[1], seconds, args[3]);
        } catch (NumberFormatException e) {
            return "HATA: Saniye degeri bir sayi olmalidir!";
        }
    }
}