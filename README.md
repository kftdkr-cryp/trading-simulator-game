# MARGIN CALL (Trading Simülatörü) / PROTRADER FİNANSAL TRADING SİMÜLASYONU

Bu proje, oyuncuların kaldıraçlı işlem (leverage/futures) ve spot piyasaları deneyimlediği, yapay zeka rakipleriyle yarışıp liderlik tablosunda üst sıralara tırmandığı ve mini oyunlarla sermaye kazandığı üst düzey, modern bir mobil finansal simülasyon oyunudur.

---

## 🚀 Son Yapılan Geliştirmeler ve Hata Düzeltmeleri (Mükemmel Sürüm)

Kullanıcı geri bildirimlerine ve derinlemesine oyun mekaniği testlerine dayanarak projede şu kritik hata düzeltmeleri ve üst düzey görsel/mantıksal parlatmalar yapılmıştır:

1. **Kaldıraçlı Pozisyonların Kâr/Zarar Mantığı Düzeltildi (`PortfolioCard.kt`):**
   - **Önceki Sorun:** Oyuncunun kaldıraçlı (Futures) pozisyonları, sanki normal Spot pozisyonlarmış gibi `miktar * fiyat` şeklinde hesaplanıyordu. Bu durum, $10 teminat (margin) ile açılan 100x kaldıraçlı bir işlemde oyuncunun toplam cüzdan değerinin (Equity) bir anda gerçek dışı şekilde binlerce dolar artmış gibi görünmesine sebep oluyordu.
   - **Çözüm:** Kaldıraçlı pozisyonların cüzdan ve portföy değerine etkisi tamamen düzeltildi. Artık kaldıraçlı pozisyonlarda oyuncunun net varlığı doğru şekilde hesaplanmaktadır: `Teminat + Gerçekleşmemiş Kâr/Zarar (Margin + Unrealized PnL)`.

2. **Sıfıra Bölünme (NaN) ve Liderlik Tablosu Hataları Giderildi (`LeaderboardScreen.kt`):**
   - **Önceki Sorun:** Oyuncu mini oyun oynamadan veya ilk kazancını elde etmeden önce başlangıç sermayesi (initialCapital) $0.0 olduğunda, liderlik tablosundaki kâr/zarar oranı (ROI) formülü sıfıra bölünerek ekranda `NaN` veya `Infinity` şeklinde hatalı gösteriliyordu.
   - **Çözüm:** Başlangıç sermayesinin sıfır olması durumu kontrol altına alındı; sıfıra bölünme hatası engellendi ve ROI kâr oranı sıfırdan güvenli şekilde başlatıldı.

3. **Otomatik Dil ve Dinamik Oyun Adı Algılama (`GameRepository.kt` & `Localizer.kt`):**
   - Oyun, kullanıcının cihazındaki varsayılan sistem dilini (`Locale.getDefault().language`) ilk açılışta otomatik olarak algılar.
   - Eğer sistem dili Türkçe ise, oyun otomatik olarak Türkçe dilinde başlar ve başlık dinamik olarak **"PROTRADER FİNANSAL TRADING SİMÜLASYONU"** / **"MARGIN CALL (Trading simülatörü)"** olur.
   - Diğer dillerde ise sistem otomatik olarak İngilizce, Azerbaycan Türkçesi, Rusça, Fransızca, Hintçe veya Çince yerelleştirmelerini devreye sokar.

4. **Gradle Wrapper Dosyaları Oluşturuldu:**
   - Projenin GitHub Actions ve harici sunucularda sorunsuz derlenebilmesi için `gradlew` ve `gradlew.bat` dosyaları projeye kazandırıldı.

---

## 🎮 Itch.io Üzerinde Yayınlama ve Bağışları Aktif Etme Adımları

Oyunu **Itch.io** platformunda yayınlarken bağışları aktif etmek ve kullanıcıların size destek olmasını sağlamak için şu adımları izleyin:

### 1. Itch.io'da Yeni Oyun Sayfası Oluşturun
- [Itch.io](https://itch.io) adresine gidin, giriş yapın ve sağ üstten **"Create new project"** butonuna tıklayın.
- **Title (Başlık):** `PROTRADER FİNANSAL TRADING SİMÜLASYONU` veya `MARGIN CALL (Trading simulator)` olarak belirleyin.
- **Classification:** `Games` seçin.
- **Kind of project:** Mobil APK olarak dağıtacağınız için `Downloadable` seçeneğini işaretleyin.

### 2. Bağışları (Donations) Aktif Hale Getirin (ÖNEMLİ)
- Proje düzenleme sayfasında **"Pricing"** (Fiyatlandırma) bölümünü bulun.
- **"No payments"** yerine **"Donation"** veya **"Name your own price"** (Kendi fiyatını belirle) seçeneğini seçin.
- **Suggested donation (Önerilen bağış):** Örneğin `$2.00` veya `$5.00` yazabilirsiniz. Kullanıcılar oyunu indirirken dilerlerse bu tutarda veya daha yüksek bir tutarda size doğrudan bağış yapabilirler.
- Ödeme yönteminizi (PayPal, Stripe vb.) Itch.io hesap ayarlarınızdan bağladığınızda bağışlar doğrudan hesabınıza aktarılır.

### 3. Otomatik GitHub Actions ile APK Yükleme Kurulumu (Butler)
Projenizde oluşturduğumuz `.github/workflows/itch_deploy.yml` dosyası, GitHub'a kod pushladığınızda APK'yı otomatik olarak derler ve itch.io'ya yükler. Bu sistemi bağlamak için:
1. **Butler API Anahtarı Alın:** Itch.io hesap ayarlarınızdan `Developer -> API Keys` kısmına gidin ve yeni bir API key oluşturup kopyalayın.
2. **GitHub Sırlarına Ekleyin:** GitHub deponuzda (repository) `Settings -> Secrets and variables -> Actions` yolunu izleyin.
3. **Yeni Secret Oluşturun:** Adını `BUTLER_API_KEY` koyun ve kopyaladığınız Itch.io API anahtarını buraya yapıştırın.
4. Kodunuz her güncellendiğinde itch.io sayfanızdaki APK dosyası otomatik olarak en güncel sürüme yükseltilecektir!

---

## 🛠️ Projeyi Yerelde Çalıştırma ve Test Etme

Proje Jetpack Compose ve Kotlin tabanlıdır. Yerel geliştirme için Android Studio Ladybug veya daha yeni bir sürüm kullanılması önerilir.

* Derlemek ve APK çıktısı almak için terminalden:
  ```bash
  ./gradlew assembleDebug
  ```
