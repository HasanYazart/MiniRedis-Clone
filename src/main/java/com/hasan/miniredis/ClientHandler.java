package com.hasan.miniredis;

import com.hasan.miniredis.command.Command;
import com.hasan.miniredis.command.CommandFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final MiniRedisStore store;

    // YENİ: Yönlendirici fabrikamızı buraya ekledik
    private final CommandFactory commandFactory;

    public ClientHandler(Socket socket, MiniRedisStore store) {
        this.socket = socket;
        this.store = store;
        this.commandFactory = new CommandFactory();
    }

    // Telnet çöp karakter temizleyicimiz aynen duruyor
    private String cleanInput(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c == '\b' || c == 127) {
                if (sb.length() > 0) {
                    sb.deleteCharAt(sb.length() - 1);
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
            out.println("MiniRedis'e Hosgeldiniz! (Ornek komutlar: SET ad Hasan, GET ad, QUIT)");

            while ((inputLine = in.readLine()) != null) {

                inputLine = cleanInput(inputLine);
                String[] parts = inputLine.trim().split("\\s+");

                if (parts.length == 0 || parts[0].isEmpty()) continue;

                String commandName = parts[0].toUpperCase();

                // QUIT komutunu özel olarak yakalıyoruz çünkü döngüyü kırması (break) gerekiyor
                if (commandName.equals("QUIT")) {
                    out.println("Baglanti kapatiliyor. Gorusmek uzere!");
                    break;
                }

                // YENİ: O koca if-else yığını yerine gelen sihirli yapı!
                Command command = commandFactory.getCommand(commandName);

                if (command != null) {
                    // Eğer komut fabrikada (CommandFactory) tanımlıysa, çalıştır ve sonucu yolla
                    String result = command.execute(store, parts);
                    out.println(result);
                } else {
                    out.println("HATA: Bilinmeyen komut!");
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