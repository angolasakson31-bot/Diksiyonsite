import AsyncStorage from '@react-native-async-storage/async-storage';

const KEY = 'diksipro_v4';

export const defaultState = {
  started: false,
  currentDay: 0,
  completedDays: [],
  completedAt: {},
  streak: 0,
  trialStartedAt: null,
  isPro: false,
};

export async function loadState() {
  try {
    const raw = await AsyncStorage.getItem(KEY);
    if (raw) return { ...defaultState, ...JSON.parse(raw) };
  } catch (_) {}
  return { ...defaultState };
}

export async function saveState(state) {
  try {
    await AsyncStorage.setItem(KEY, JSON.stringify(state));
  } catch (_) {}
}

export async function clearState() {
  try {
    await AsyncStorage.removeItem(KEY);
  } catch (_) {}
}

export function calcStreak(completedDays) {
  if (!completedDays.length) return 0;
  const sorted = [...completedDays].sort((a, b) => a - b);
  let streak = 0;
  for (let i = sorted.length - 1; i >= 0; i--) {
    if (i === sorted.length - 1 || sorted[i] === sorted[i + 1] - 1) streak++;
    else break;
  }
  return streak;
}

export function maxUnlocked(completedDays) {
  if (!completedDays.length) return 0;
  const last = Math.max(...completedDays);
  return Math.min(62, last + 1);
}

export function isTrialExpired(trialStartedAt, trialDays = 3) {
  if (!trialStartedAt) return false;
  const elapsed = Date.now() - new Date(trialStartedAt).getTime();
  return elapsed > trialDays * 24 * 60 * 60 * 1000;
}
