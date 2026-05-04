#!/bin/bash
echo "📦 Paketler yükleniyor..."
cd DiksiProApp && npm install
echo "🚀 Expo başlatılıyor..."
npx expo start --tunnel
