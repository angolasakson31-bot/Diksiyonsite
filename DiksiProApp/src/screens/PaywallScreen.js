import React from 'react';
import {
  View, Text, StyleSheet, TouchableOpacity,
  SafeAreaView, StatusBar,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { colors, fonts, spacing, radius } from '../theme';

export default function PaywallScreen({ onRestore, onPurchase }) {
  return (
    <SafeAreaView style={styles.safe}>
      <StatusBar barStyle="light-content" backgroundColor={colors.bg} />
      <View style={styles.container}>

        <View style={styles.top}>
          <Text style={styles.logo}>Diksi<Text style={styles.logoGold}>Pro</Text></Text>
          <Text style={styles.tagline}>PROFESYONELSESEĞİTİMİ</Text>
        </View>

        <View style={styles.card}>
          <Text style={styles.lockIcon}>🔒</Text>
          <Text style={styles.title}>Deneme Süresi Sona Erdi</Text>
          <Text style={styles.desc}>
            Programın geri kalanına erişmek için DiksiPro'yu satın alın. Tek seferlik ödeme, sonsuza kadar kullanım.
          </Text>

          <View style={styles.features}>
            {[
              '63 günlük tam program',
              '9 haftalık ses eğitimi',
              'Diyafram nefes zamanlayıcısı',
              'Tekerleme sayacı',
              'Sesli okuma metinleri',
              'Streak ve ilerleme takibi',
            ].map((f) => (
              <View key={f} style={styles.featureRow}>
                <Text style={styles.featureCheck}>✓</Text>
                <Text style={styles.featureText}>{f}</Text>
              </View>
            ))}
          </View>

          <TouchableOpacity
            style={styles.buyBtn}
            onPress={onPurchase}
            activeOpacity={0.85}
          >
            <LinearGradient
              colors={[colors.gold, colors.goldDim]}
              start={{ x: 0, y: 0 }} end={{ x: 1, y: 0 }}
              style={styles.buyBtnGradient}
            >
              <Text style={styles.buyBtnPrice}>Satın Al</Text>
              <Text style={styles.buyBtnSub}>Google Play üzerinden</Text>
            </LinearGradient>
          </TouchableOpacity>

          <TouchableOpacity style={styles.restoreBtn} onPress={onRestore} activeOpacity={0.7}>
            <Text style={styles.restoreBtnText}>Satın almayı geri yükle</Text>
          </TouchableOpacity>
        </View>

        <Text style={styles.legal}>
          Ödeme Google Play üzerinden güvenli şekilde işlenir.
        </Text>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.bg },
  container: { flex: 1, alignItems: 'center', justifyContent: 'center', paddingHorizontal: spacing.lg },

  top: { alignItems: 'center', marginBottom: 32 },
  logo: { fontFamily: fonts.display, fontSize: 44, color: colors.text, letterSpacing: -2 },
  logoGold: { color: colors.gold },
  tagline: { fontFamily: fonts.sansMedium, fontSize: 9, letterSpacing: 4, color: colors.muted, marginTop: 4 },

  card: { backgroundColor: colors.s1, borderWidth: 1, borderColor: colors.border, borderRadius: radius.xl, padding: 24, width: '100%', maxWidth: 400, alignItems: 'center' },
  lockIcon: { fontSize: 36, marginBottom: 12 },
  title: { fontFamily: fonts.display, fontSize: 24, color: colors.text, textAlign: 'center', marginBottom: 10 },
  desc: { fontFamily: fonts.sans, fontSize: 14, color: colors.sub, textAlign: 'center', lineHeight: 22, marginBottom: 20 },

  features: { width: '100%', marginBottom: 24 },
  featureRow: { flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 8 },
  featureCheck: { fontFamily: fonts.sansBold, fontSize: 13, color: colors.green },
  featureText: { fontFamily: fonts.sans, fontSize: 13, color: colors.sub },

  buyBtn: { width: '100%', borderRadius: radius.md, overflow: 'hidden', marginBottom: 12 },
  buyBtnGradient: { paddingVertical: 16, alignItems: 'center' },
  buyBtnPrice: { fontFamily: fonts.sansBold, fontSize: 17, color: colors.black },
  buyBtnSub: { fontFamily: fonts.sans, fontSize: 11, color: 'rgba(0,0,0,0.6)', marginTop: 2 },

  restoreBtn: { paddingVertical: 10 },
  restoreBtnText: { fontFamily: fonts.sans, fontSize: 13, color: colors.muted },

  legal: { fontFamily: fonts.sans, fontSize: 11, color: colors.muted, textAlign: 'center', marginTop: 20 },
});
