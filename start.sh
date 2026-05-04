#!/bin/bash
echo "Paketler yukleniyor..."
cd DiksiProApp && npm install --legacy-peer-deps

echo ""
echo "========================================="
echo "  Expo Web baslatiliyor (port 8081)"
echo "  Codespaces PORTS sekmesinden:"
echo "  8081 portunu PUBLIC yap"
echo "  URL'yi telefonunun tarayicisinda ac"
echo "========================================="
echo ""

npx expo start --web --clear
