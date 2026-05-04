import React from 'react';
import {
  View, Text, StyleSheet, TouchableOpacity,
  SafeAreaView, StatusBar, Dimensions,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { colors, fonts, spacing, radius } from '../theme';

const { width } = Dimensions.get('window');

export default function OnboardingScreen({ onStart }) {
  return (
    <SafeAreaView style={styles.safe}>
      <StatusBar barStyle="light-content" backgroundColor={colors.bg} />
      <LinearGradient
        colors={[colors.bg, '#0a1018', colors.bg]}
        style={styles.container}
      >
        {/* Brand */}
        <View style={styles.brandWrap}>
          <Text style={styles.brandName}>
            Diksi<Text style={styles.brandGold}>Pro</Text>
          </Text>
          <Text style={styles.brandSub}>PROFESYONELSESEĞİTİMİ</Text>
        </View>

        {/* Card */}
        <View style={styles.card}>
          <Text style={styles.cardTitle}>Hoş Geldin</Text>
          <Text style={styles.cardDesc}>
            9 haftalık profesyonel diksiyon programı. Her gün yaklaşık 10 dakika. Kendi hızında ilerle.
          </Text>

          {/* Stats */}
          <View style={styles.pills}>
            <Pill value="9" label="Hafta" />
            <Pill value="63" label="Gün" />
            <Pill value="~10dk" label="Günlük" />
          </View>

          {/* CTA */}
          <TouchableOpacity style={styles.btn} onPress={onStart} activeOpacity={0.85}>
            <LinearGradient
              colors={[colors.gold, colors.goldDim]}
              start={{ x: 0, y: 0 }} end={{ x: 1, y: 0 }}
              style={styles.btnGradient}
            >
              <Text style={styles.btnText}>Programa Başla →</Text>
            </LinearGradient>
          </TouchableOpacity>
        </View>

        {/* Footer poem */}
        <Text style={styles.footer}>
          ♡ <Text style={styles.footerGold}>Tuğ</Text> için
        </Text>
      </LinearGradient>
    </SafeAreaView>
  );
}

function Pill({ value, label }) {
  return (
    <View style={styles.pill}>
      <Text style={styles.pillValue}>{value}</Text>
      <Text style={styles.pillLabel}>{label}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  safe: {
    flex: 1,
    backgroundColor: colors.bg,
  },
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: spacing.lg,
  },
  brandWrap: {
    alignItems: 'center',
    marginBottom: 48,
  },
  brandName: {
    fontFamily: fonts.display,
    fontSize: 60,
    color: colors.text,
    letterSpacing: -2,
    lineHeight: 64,
  },
  brandGold: {
    color: colors.gold,
  },
  brandSub: {
    fontFamily: fonts.sansMedium,
    fontSize: 10,
    letterSpacing: 4,
    color: colors.muted,
    marginTop: 6,
  },
  card: {
    backgroundColor: colors.s1,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: radius.xl,
    padding: spacing.lg + 6,
    width: '100%',
    maxWidth: 400,
  },
  cardTitle: {
    fontFamily: fonts.display,
    fontSize: 26,
    color: colors.text,
    marginBottom: 8,
  },
  cardDesc: {
    fontFamily: fonts.sans,
    fontSize: 14,
    color: colors.sub,
    lineHeight: 22,
    marginBottom: 24,
  },
  pills: {
    flexDirection: 'row',
    gap: 8,
    marginBottom: 28,
  },
  pill: {
    flex: 1,
    backgroundColor: colors.s2,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: radius.md,
    paddingVertical: 12,
    paddingHorizontal: 6,
    alignItems: 'center',
  },
  pillValue: {
    fontFamily: fonts.sansBold,
    fontSize: 22,
    color: colors.gold,
  },
  pillLabel: {
    fontFamily: fonts.sans,
    fontSize: 10,
    color: colors.muted,
    marginTop: 2,
  },
  btn: {
    borderRadius: radius.md,
    overflow: 'hidden',
  },
  btnGradient: {
    paddingVertical: 16,
    alignItems: 'center',
  },
  btnText: {
    fontFamily: fonts.sansBold,
    fontSize: 16,
    color: colors.black,
  },
  footer: {
    fontFamily: fonts.sans,
    fontSize: 12,
    color: colors.muted,
    letterSpacing: 1,
    marginTop: 28,
  },
  footerGold: {
    fontFamily: fonts.display,
    fontSize: 15,
    color: colors.gold,
    fontStyle: 'italic',
  },
});
