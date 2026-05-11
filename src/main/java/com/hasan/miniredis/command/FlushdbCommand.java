package com.hasan.miniredis.command;
import com.hasan.miniredis.MiniRedisStore;

public class FlushdbCommand implements Command {
    @Override
    public String execute(MiniRedisStore store, String[] args) {
        return store.flushdb();
    }
}