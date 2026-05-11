package com.hasan.miniredis;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MiniRedisServer {
    private final int port;
    private final MiniRedisStore store;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public MiniRedisServer(int port, MiniRedisStore store) {
        this.port = port;
        this.store = store;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("MiniRedis Sunucusu " + port + " portunda calismaya basladi...");
            System.out.println("Baglanti bekliyor...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Yeni bir istemci baglandi: " + clientSocket.getInetAddress());

                threadPool.execute(new ClientHandler(clientSocket, store));
            }
        } catch (IOException e) {
            System.err.println("Sunucu baslatilamadi veya hata olustu: " + e.getMessage());
        }
    }
}