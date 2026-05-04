import React, { useState, useEffect, useCallback } from 'react';
import { View, ActivityIndicator, StyleSheet } from 'react-native';
import * as SplashScreen from 'expo-splash-screen';
import * as Font from 'expo-font';
import {
  PlayfairDisplay_700Bold,
  PlayfairDisplay_400Italic,
} from '@expo-google-fonts/playfair-display';
import {
  DMSans_400Regular,
  DMSans_500Medium,
  DMSans_600SemiBold,
  DMSans_700Bold,
} from '@expo-google-fonts/dm-sans';

import { loadState, saveState, defaultState, isTrialExpired } from './src/storage';
import { TRIAL_DAYS } from './src/data/constants';
import { colors } from './src/theme';
import OnboardingScreen from './src/screens/OnboardingScreen';
import HomeScreen       from './src/screens/HomeScreen';
import PaywallScreen    from './src/screens/PaywallScreen';

SplashScreen.preventAutoHideAsync();

export default function App() {
  const [fontsLoaded, setFontsLoaded] = useState(false);
  const [appState, setAppState]       = useState(null);

  // Load fonts + state
  useEffect(() => {
    (async () => {
      await Font.loadAsync({
        PlayfairDisplay_700Bold,
        PlayfairDisplay_400Italic,
        DMSans_400Regular,
        DMSans_500Medium,
        DMSans_600SemiBold,
        DMSans_700Bold,
      });
      const saved = await loadState();
      setAppState(saved);
      setFontsLoaded(true);
    })();
  }, []);

  const onLayoutRootView = useCallback(async () => {
    if (fontsLoaded) await SplashScreen.hideAsync();
  }, [fontsLoaded]);

  if (!fontsLoaded || !appState) {
    return (
      <View style={styles.loading}>
        <ActivityIndicator color={colors.gold} />
      </View>
    );
  }

  const handleStart = async () => {
    const newState = {
      ...defaultState,
      started: true,
      trialStartedAt: new Date().toISOString(),
    };
    await saveState(newState);
    setAppState(newState);
  };

  const handleStateChange = (newState) => {
    setAppState(newState);
  };

  const handlePurchase = () => {
    // Google Play Billing bağlandığında burası doldurulacak
    // Şimdilik test modu: isPro = true
    const newState = { ...appState, isPro: true };
    saveState(newState);
    setAppState(newState);
  };

  const handleRestore = () => {
    // Google Play satın alma geri yükleme buraya
    handlePurchase();
  };

  // Ekran belirleme
  let screen;
  if (!appState.started) {
    screen = <OnboardingScreen onStart={handleStart} />;
  } else if (!appState.isPro && isTrialExpired(appState.trialStartedAt, TRIAL_DAYS)) {
    screen = <PaywallScreen onPurchase={handlePurchase} onRestore={handleRestore} />;
  } else {
    screen = <HomeScreen state={appState} onStateChange={handleStateChange} />;
  }

  return (
    <View style={{ flex: 1 }} onLayout={onLayoutRootView}>
      {screen}
    </View>
  );
}

const styles = StyleSheet.create({
  loading: {
    flex: 1,
    backgroundColor: colors.bg,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
