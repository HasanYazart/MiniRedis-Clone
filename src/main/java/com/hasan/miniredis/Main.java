package com.hasan.miniredis;

public class Main {
    public static void main(String[] args) {
        MiniRedisStore db = new MiniRedisStore();

        // YENİ: Sunucuyu dış dünyaya açmadan önce eski verileri diskten belleğe yükle
        db.loadFromAof();

        MiniRedisServer server = new MiniRedisServer(6379, db);
        server.start();
    }
}