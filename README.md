# MiniRedis 🚀 | Java In-Memory Key-Value Store

MiniRedis, tamamen **Saf Java (Core Java)** kullanılarak sıfırdan geliştirilmiş, dış kütüphane bağımlılığı olmayan, TCP tabanlı ve çok iş parçacıklı (multi-threaded) bir bellek içi (in-memory) veritabanı motorudur.

## 🎯 Projenin Amacı
Bu projenin temel amacı; modern veritabanlarının (özellikle Redis gibi in-memory cache sistemlerinin) kapalı kapılar ardında nasıl çalıştığını anlamaktır. Veriyi sadece bellekte tutmanın ötesinde; **soket programlama (networking)**, **eşzamanlılık (concurrency)**, **bellek yönetimi (TTL)** ve **kalıcılık (persistence)** gibi kritik arka uç (backend) mühendisliği konseptlerini teorikten pratiğe dökmek için geliştirilmiştir.

## ⚙️ Mimari ve Kullanılan Teknolojiler

Bu projede Spring veya Hibernate gibi soyutlayıcı framework'ler **bilinçli olarak kullanılmamış**, sistemin tüm çarkları JDK'nın temel yetenekleriyle inşa edilmiştir:

* **Core Java & TCP Sockets:** İstemcilerle iletişim kurmak için `java.net.ServerSocket` ve I/O Stream'leri kullanılarak özel bir ağ iletişim protokolü yazıldı.
* **Concurrency (Eşzamanlılık):**
    * Aynı anda bağlanan birden fazla istemcinin sunucuyu kilitlemesini önlemek için `ExecutorService` ile bir **Thread Pool** (İş Parçacığı Havuzu) kuruldu.
    * Veri okuma/yazma sırasındaki çakışmaları (Race Condition) önlemek için thread-safe bir yapı olan `ConcurrentHashMap` kullanıldı.
* **Command Pattern (Tasarım Deseni):** Spagetti `if-else` bloklarından kaçınmak ve SOLID prensiplerinin *Open/Closed (Gelişime açık, değişime kapalı)* kuralına uymak için her veritabanı komutu kendi sınıfına (Class) ayrıldı ve bir `CommandFactory` (Yönlendirici Fabrika) üzerinden yönetildi.
* **AOF (Append-Only File) Persistence:** İn-memory sistemlerin en büyük zayıflığı olan elektrik kesintisinde veri kaybını önlemek için, veriyi değiştiren her komut anında bir `.aof` dosyasına loglandı (`BufferedWriter`). Sistem yeniden başladığında bu dosya okunarak RAM eski haline getirilir.
* **Aktif & Tembel TTL (Time-To-Live):** `ScheduledExecutorService` kullanılarak arka planda çalışan bir çöp toplayıcı (Garbage Collector) thread yazıldı. Süresi dolan veriler hem anlık erişimde (Lazy) hem de arka plan taramasında (Active) bellekten silinir.

## 🛠️ Kurulum ve Çalıştırma

Projeyi bilgisayarınıza indirdikten sonra çalıştırmak için:
1. `Main` sınıfını çalıştırın. Sunucu varsayılan olarak `6379` portunda dinlemeye başlayacaktır.
2. Sisteme bağlanmak için herhangi bir terminalden `telnet` kullanabilirsiniz:
   ```bash
   telnet localhost 6379
   ```
   Sisteme bağlandıktan sonra aşağıdaki komutları kullanarak veritabanını yönetebilirsiniz:

Temel CRUD İşlemleri
SET [anahtar] [değer] : Yeni bir anahtar-değer çifti kaydeder. (Örn: SET ad Hasan)

GET [anahtar] : Anahtara ait değeri getirir. Bulamazsa (nil) döner. (Örn: GET ad)

DEL [anahtar] : Anahtarı siler. (Örn: DEL ad)

Süreli Veri (TTL) Yönetimi
SETEX [anahtar] [saniye] [değer] : Veriyi sadece belirtilen saniye kadar bellekte tutar, süre dolunca otomatik imha eder. (Örn: SETEX mesaj 15 selam)

TTL [anahtar] : Anahtarın silinmesine kaç saniye kaldığını gösterir.

Atomik Sayaçlar (E-Ticaret & Stok Mantığı)
INCR [anahtar] : Değeri tam sayı olan bir anahtarı atomik olarak 1 artırır. (Örn: INCR ziyaretci_sayisi)

DECR [anahtar] : Değeri tam sayı olan bir anahtarı atomik olarak 1 azaltır. Stok düşmek için idealdir. (Örn: DECR stok_miktari)

Sistem Yönetimi
INFO : Sunucunun anlık durumunu, aktif anahtar sayısını ve RAM tüketimini gösterir.

FLUSHDB : Tüm veritabanını (RAM) ve AOF log dosyasını sıfırlayarak temiz bir başlangıç sağlar.

QUIT : Bağlantıyı güvenli bir şekilde sonlandırır.



Bu proje, yüksek performanslı dağıtık sistemlerin temel çalışma prensiplerini kavramak amacıyla bir mühendislik pratiği olarak geliştirilmiştir.
   
