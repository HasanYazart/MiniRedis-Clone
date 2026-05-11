package com.hasan.miniredis.command;

import com.hasan.miniredis.MiniRedisStore;

public interface Command {
    // Tüm komutlar bu metodu çalıştırmak zorunda
    String execute(MiniRedisStore store, String[] args);
}