package com.hasan.miniredis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final MiniRedisStore store;

    public ClientHandler(Socket socket, MiniRedisStore store) {
        this.socket = socket;
        this.store = store;
    }

    // YENİ: Telnet'ten gelen Backspace (Silme) karakterlerini arka planda işleyen metod
    private String cleanInput(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c == '\b' || c == 127) { // '\b' (Backspace) veya 127 (Delete)
                if (sb.length() > 0) {
                    sb.deleteCharAt(sb.length() - 1); // Bir önceki karakteri sil
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String inputLine;
            out.println("MiniRedis'e Hosgeldiniz! (Ornek komutlar: SET ad Hasan, GET ad, SETEX mesaj 15 selam, TTL mesaj, QUIT)");

            while ((inputLine = in.readLine()) != null) {

                // Klavyeden gelen silme işlemlerini koda uygula ve temizle
                inputLine = cleanInput(inputLine);

                String[] parts = inputLine.trim().split("\\s+");

                if (parts.length == 0 || parts[0].isEmpty()) continue;

                String command = parts[0].toUpperCase();

                if (command.equals("SET") && parts.length == 3) {
                    String result = store.set(parts[1], parts[2]);
                    out.println(result);
                } else if (command.equals("GET") && parts.length == 2) {
                    String result = store.get(parts[1]);
                    out.println(result != null ? result : "(nil)");
                } else if (command.equals("DEL") && parts.length == 2) {
                    int result = store.del(parts[1]);
                    out.println("(integer) " + result);
                } else if (command.equals("SETEX") && parts.length == 4) {
                    try {
                        int seconds = Integer.parseInt(parts[2]);
                        String result = store.setex(parts[1], seconds, parts[3]);
                        out.println(result);
                    } catch (NumberFormatException e) {
                        out.println("HATA: Saniye degeri bir sayi olmalidir!");
                    }
                } else if (command.equals("TTL") && parts.length == 2) {
                    long timeLeft = store.ttl(parts[1]);
                    out.println("(integer) " + timeLeft);
                } else if (command.equals("QUIT")) {
                    out.println("Baglanti kapatiliyor. Gorusmek uzere!");
                    break;
                } else {
                    out.println("HATA: Bilinmeyen komut veya eksik/fazla parametre!");
                }
            }
        } catch (IOException e) {
            System.err.println("Istemci baglantisi koptu: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}