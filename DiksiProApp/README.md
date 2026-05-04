# DiksiPro — React Native App

## Kurulum

```bash
npm install
npx expo start
```

## Telefonda Test

1. Telefona **Expo Go** kur (Play Store'dan)
2. `npx expo start` çalıştır
3. Terminal'de QR kodu çıkar → Expo Go ile tara

## Proje Yapısı

```
src/
  theme/index.js          # renkler, fontlar, spacing
  data/
    constants.js          # SETS, SES_GRUPLARI, sabitler
    poems.js              # 63 günlük şiirler
    twisters.js           # TEKERLEMELER (tüm setler)
    texts.js              # METINLER (63 günlük okuma)
  storage/index.js        # AsyncStorage yardımcıları
  screens/
    OnboardingScreen.js   # Karşılama ekranı
    HomeScreen.js         # Ana egzersiz ekranı
    PaywallScreen.js      # Satın alma ekranı
```

## Deneme Süresi

`src/data/constants.js` içinde `TRIAL_DAYS` değerini değiştirerek ayarla.
Şu an: **3 gün**

## Ödeme Sistemi (TODO)

`App.js` içinde `handlePurchase` fonksiyonu şu an test modunda (isPro = true yapıyor).
Gerçek ödeme için:
1. `expo-in-app-purchases` paketi ekle
2. Google Play Console'da ürün tanımla
3. `handlePurchase` fonksiyonunu Play Billing API'sine bağla

## Play Store için Build

```bash
npm install -g eas-cli
eas login
eas build --platform android --profile production
```
