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

    // YENİ: AOF Dosya Yolu ve Yazıcısı
    private final String AOF_FILE = "miniredis.aof";
    private BufferedWriter aofWriter;

    public MiniRedisStore() {
        // AOF dosyasını "ekleme (append)" modunda açıyoruz
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
                    appendToAof("DEL " + entry.getKey()); // Süresi dolup silinince diske de "silindi" yaz
                    System.out.println("[SİSTEM] Süresi dolan anahtar otomatik silindi: " + entry.getKey());
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    // YENİ: Komutları anında diske yazan metod
    private synchronized void appendToAof(String command) {
        if (aofWriter != null) {
            try {
                aofWriter.write(command);
                aofWriter.newLine();
                aofWriter.flush(); // RAM'de bekletme, anında sabit diske (SSD/HDD) yaz!
            } catch (IOException e) {
                System.err.println("AOF yazma hatasi: " + e.getMessage());
            }
        }
    }

    // YENİ: Sunucu açıldığında eski verileri diskten RAM'e geri yükleyen metod
    public void loadFromAof() {
        File file = new File(AOF_FILE);
        if (!file.exists()) return; // Dosya yoksa, demek ki ilk defa açılıyor

        System.out.println("Diskten eski veriler okunuyor, lutfen bekleyin...");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts[0].equals("SET") && parts.length == 3) {
                    store.put(parts[1], parts[2]); // Belleğe yaz ama loglama
                    expiries.remove(parts[1]);
                    count++;
                } else if (parts[0].equals("DEL") && parts.length == 2) {
                    store.remove(parts[1]);
                    expiries.remove(parts[1]);
                    count--;
                }
            }
            System.out.println("Geri yukleme tamamlandi! Toplam " + count + " anahtar RAM'e alindi.");
        } catch (IOException e) {
            System.err.println("AOF okuma hatasi: " + e.getMessage());
        }
    }

    public String set(String key, String value) {
        store.put(key, value);
        expiries.remove(key);
        appendToAof("SET " + key + " " + value); // Yapılan işlemi diske logla
        return "OK";
    }

    public String setex(String key, int seconds, String value) {
        store.put(key, value);
        long expiryTime = System.currentTimeMillis() + (seconds * 1000L);
        expiries.put(key, expiryTime);
        appendToAof("SET " + key + " " + value); // Şimdilik basitleştirmek adına süreli verileri diske süresiz olarak kaydediyoruz
        return "OK";
    }

    public String get(String key) {
        if (expiries.containsKey(key)) {
            if (expiries.get(key) < System.currentTimeMillis()) {
                store.remove(key);
                expiries.remove(key);
                appendToAof("DEL " + key);
                return null;
            }
        }
        return store.get(key);
    }

    public int del(String key) {
        expiries.remove(key);
        if (store.containsKey(key)) {
            store.remove(key);
            appendToAof("DEL " + key); // Silme işlemini diske logla
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
}