package com.hasan.miniredis;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MiniRedisStore {
    private final Map<String, String> store = new ConcurrentHashMap<>();
    private final Map<String, Long> expiries = new ConcurrentHashMap<>();
    private final String AOF_FILE = "miniredis.aof";
    private BufferedWriter aofWriter;

    public MiniRedisStore() {
        try {
            aofWriter = new BufferedWriter(new FileWriter(AOF_FILE, true));
        } catch (IOException e) {
            System.err.println("AOF Dosyasi acilamadi: " + e.getMessage());
        }

        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();
        cleaner.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            for (Map.Entry<String, Long> entry : expiries.entrySet()) {
                if (entry.getValue() < now) {
                    store.remove(entry.getKey());
                    expiries.remove(entry.getKey());
                    appendToAof("DEL " + entry.getKey());
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private synchronized void appendToAof(String command) {
        if (aofWriter != null) {
            try {
                aofWriter.write(command);
                aofWriter.newLine();
                aofWriter.flush();
            } catch (IOException ignored) {}
        }
    }

    public void loadFromAof() {
        File file = new File(AOF_FILE);
        if (!file.exists()) return;

        System.out.println("Diskten eski veriler okunuyor, lutfen bekleyin...");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts[0].equals("SET") && parts.length == 3) {
                    store.put(parts[1], parts[2]);
                    expiries.remove(parts[1]);
                } else if (parts[0].equals("DEL") && parts.length == 2) {
                    store.remove(parts[1]);
                    expiries.remove(parts[1]);
                }
            }
            System.out.println("Geri yukleme tamamlandi! Toplam " + store.size() + " anahtar RAM'e alindi.");
        } catch (IOException ignored) {}
    }

    public String set(String key, String value) {
        store.put(key, value);
        expiries.remove(key);
        appendToAof("SET " + key + " " + value);
        return "OK";
    }

    public String setex(String key, int seconds, String value) {
        store.put(key, value);
        long expiryTime = System.currentTimeMillis() + (seconds * 1000L);
        expiries.put(key, expiryTime);
        appendToAof("SET " + key + " " + value);
        return "OK";
    }

    public String get(String key) {
        if (expiries.containsKey(key) && expiries.get(key) < System.currentTimeMillis()) {
            store.remove(key);
            expiries.remove(key);
            appendToAof("DEL " + key);
            return null;
        }
        return store.get(key);
    }

    public int del(String key) {
        expiries.remove(key);
        if (store.containsKey(key)) {
            store.remove(key);
            appendToAof("DEL " + key);
            return 1;
        }
        return 0;
    }

    public long ttl(String key) {
        if (!store.containsKey(key)) return -2;
        if (!expiries.containsKey(key)) return -1;
        long timeLeft = expiries.get(key) - System.currentTimeMillis();
        return timeLeft > 0 ? timeLeft / 1000 : -2;
    }

    // YENİ: Atomik Artırma
    public synchronized String incr(String key) {
        String val = store.getOrDefault(key, "0");
        try {
            long num = Long.parseLong(val);
            num++;
            store.put(key, String.valueOf(num));
            appendToAof("SET " + key + " " + num);
            return "(integer) " + num;
        } catch (NumberFormatException e) {
            return "HATA: Deger bir tam sayi degil!";
        }
    }

    // YENİ: Atomik Azaltma
    public synchronized String decr(String key) {
        String val = store.getOrDefault(key, "0");
        try {
            long num = Long.parseLong(val);
            num--;
            store.put(key, String.valueOf(num));
            appendToAof("SET " + key + " " + num);
            return "(integer) " + num;
        } catch (NumberFormatException e) {
            return "HATA: Deger bir tam sayi degil!";
        }
    }

    // YENİ: Sistem Metrikleri
    public String info() {
        Runtime runtime = Runtime.getRuntime();
        long memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        return String.format("MiniRedis v1.0 | Anahtar: %d | Zamanli Anahtar: %d | RAM: %d MB",
                store.size(), expiries.size(), memoryUsed);
    }

    // YENİ: Komple Temizlik
    public synchronized String flushdb() {
        store.clear();
        expiries.clear();
        try {
            if (aofWriter != null) aofWriter.close();
            new PrintWriter(AOF_FILE).close(); // Dosyanın içini boşalt
            aofWriter = new BufferedWriter(new FileWriter(AOF_FILE, true)); // Yeniden aç
        } catch (IOException ignored) {}
        return "OK";
    }
}