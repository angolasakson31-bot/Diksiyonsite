import React, { useState, useEffect, useRef, useCallback } from 'react';
import {
  View, Text, StyleSheet, ScrollView, TouchableOpacity,
  SafeAreaView, StatusBar, Animated, Pressable,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import * as Haptics from 'expo-haptics';
import { colors, fonts, spacing, radius } from '../theme';
import { SETS, SES_GRUPLARI, BREATH_PHASES, BREATH_DURATIONS, BREATH_COLORS } from '../data/constants';
import { SIIRLER } from '../data/poems';
import { TEKERLEMELER } from '../data/twisters';
import { METINLER } from '../data/texts';
import { maxUnlocked, calcStreak, saveState } from '../storage';

// ─── Header ───────────────────────────────────────────────────────────────────
function Header({ state }) {
  const weekIdx = Math.floor(state.currentDay / 7);
  const setKey  = SETS[weekIdx];
  const label   = `Set ${setKey[0]} · ${setKey[1]}. Hafta`;
  const streak  = state.streak;
  const mu      = maxUnlocked(state.completedDays);
  const weekStart = Math.floor(mu / 7) * 7;

  return (
    <View style={styles.header}>
      <View style={styles.headerRow1}>
        <Text style={styles.logo}>Diksi<Text style={styles.logoGold}>Pro</Text></Text>
        <View style={[styles.streakBadge, streak > 0 && styles.streakHot]}>
          <Text style={[styles.streakText, streak > 0 && styles.streakTextHot]}>
            🔥 {streak} gün
          </Text>
        </View>
      </View>
      <View style={styles.headerRow2}>
        <Text style={styles.setLabel}>{label}</Text>
        <WeekDots completedDays={state.completedDays} weekStart={weekStart} mu={mu} />
      </View>
    </View>
  );
}

function WeekDots({ completedDays, weekStart, mu }) {
  const dots = [];
  for (let d = weekStart; d < weekStart + 7 && d <= 62; d++) {
    const done   = completedDays.includes(d);
    const active = d === mu && !done;
    dots.push(
      <View
        key={d}
        style={[
          styles.dot,
          done   && styles.dotDone,
          active && styles.dotActive,
        ]}
      />
    );
  }
  return <View style={styles.dotsRow}>{dots}</View>;
}

// ─── Day Navigator ─────────────────────────────────────────────────────────────
function DayNav({ state, onNavigate }) {
  const mu     = maxUnlocked(state.completedDays);
  const day    = state.currentDay;
  const isDone = state.completedDays.includes(day);
  const sub    = day > mu ? 'Kilitli' : isDone ? 'Tamamlandı' : day === mu ? 'Bugünün egzersizi' : 'Geçmiş gün';

  return (
    <View style={styles.nav}>
      <TouchableOpacity
        style={[styles.navBtn, day === 0 && styles.navBtnDisabled]}
        onPress={() => onNavigate(-1)}
        disabled={day === 0}
      >
        <Text style={[styles.navBtnText, day === 0 && styles.navBtnTextDisabled]}>← Önceki</Text>
      </TouchableOpacity>
      <View style={styles.navMid}>
        <Text style={styles.navDay}>Gün {day + 1}</Text>
        <Text style={styles.navSub}>{sub}</Text>
      </View>
      <TouchableOpacity
        style={[styles.navBtn, day >= mu && styles.navBtnDisabled]}
        onPress={() => onNavigate(1)}
        disabled={day >= mu}
      >
        <Text style={[styles.navBtnText, day >= mu && styles.navBtnTextDisabled]}>Sonraki →</Text>
      </TouchableOpacity>
    </View>
  );
}

// ─── Section Label ─────────────────────────────────────────────────────────────
function SectionLabel({ children }) {
  return (
    <View style={styles.sectionLabel}>
      <Text style={styles.sectionLabelText}>{children}</Text>
      <View style={styles.sectionLabelLine} />
    </View>
  );
}

// ─── Breath Timer ──────────────────────────────────────────────────────────────
function BreathTimer() {
  const [running, setRunning]   = useState(false);
  const [phase, setPhase]       = useState(0);
  const [count, setCount]       = useState(BREATH_DURATIONS[0]);
  const [set, setSet]           = useState(1);
  const [done, setDone]         = useState(false);
  const intervalRef             = useRef(null);
  const phaseRef                = useRef(0);
  const countRef                = useRef(BREATH_DURATIONS[0]);
  const setRef                  = useRef(1);

  const stop = useCallback(() => {
    if (intervalRef.current) clearInterval(intervalRef.current);
    intervalRef.current = null;
    setRunning(false);
  }, []);

  const tick = useCallback(() => {
    countRef.current -= 1;
    if (countRef.current <= 0) {
      const nextPhase = (phaseRef.current + 1) % 3;
      if (nextPhase === 0) {
        const nextSet = setRef.current + 1;
        if (nextSet > 5) {
          stop();
          setDone(true);
          return;
        }
        setRef.current = nextSet;
        setSet(nextSet);
      }
      phaseRef.current = nextPhase;
      countRef.current = BREATH_DURATIONS[nextPhase];
      setPhase(nextPhase);
    }
    setCount(countRef.current);
  }, [stop]);

  const toggle = useCallback(() => {
    if (done) return;
    if (running) {
      stop();
    } else {
      phaseRef.current = 0;
      countRef.current = BREATH_DURATIONS[0];
      setRef.current   = 1;
      setPhase(0);
      setCount(BREATH_DURATIONS[0]);
      setSet(1);
      setRunning(true);
      intervalRef.current = setInterval(tick, 1000);
    }
  }, [running, done, stop, tick]);

  useEffect(() => () => { if (intervalRef.current) clearInterval(intervalRef.current); }, []);

  return (
    <View style={styles.breathTimer}>
      {done ? (
        <View style={styles.breathDone}>
          <Text style={styles.breathDoneText}>✓ 5 Set Tamamlandı</Text>
        </View>
      ) : (
        <>
          <TouchableOpacity style={styles.breathBtn} onPress={toggle} activeOpacity={0.8}>
            <Text style={styles.breathBtnText}>
              {running ? '⏸ Duraklat' : '▶ Zamanlayıcıyı Başlat'}
            </Text>
          </TouchableOpacity>
          {running && (
            <View style={styles.breathDisplay}>
              <Text style={styles.breathPhase}>{BREATH_PHASES[phase]}</Text>
              <Text style={[styles.breathCount, { color: BREATH_COLORS[phase] }]}>{count}</Text>
              <Text style={styles.breathSet}>Set {set} / 5</Text>
            </View>
          )}
        </>
      )}
    </View>
  );
}

// ─── Nefes Card ────────────────────────────────────────────────────────────────
function NefesCard() {
  return (
    <View style={styles.card}>
      <SectionLabel>Diyafram Nefesi</SectionLabel>
      <Text style={styles.cardTitle}>4 · 2 · 6 Nefes Tekniği</Text>
      <Text style={styles.cardDesc}>
        Sesin gücü akciğerden değil, diyaframdan gelir. Uzun nefes verme (6 saniye) diyaframı aktive eder, sesi derinleştirir. 5 set yapılır.
      </Text>
      {[
        ['1','4 sayarak burundan nefes al — yalnızca karın şişsin, göğüs hareketsiz kalsın.'],
        ['2','2 sayarak nefesini tut. Karın şişkin halde sabit dursun.'],
        ['3','6 sayarak ağızdan yavaşça ver. Karın içeri çekilsin. Ses çıkarmadan, kontrollü.'],
      ].map(([n, t]) => (
        <View key={n} style={styles.stepRow}>
          <View style={styles.stepNum}><Text style={styles.stepNumText}>{n}</Text></View>
          <Text style={styles.stepText}>{t}</Text>
        </View>
      ))}
      <Text style={styles.note}>5 seti bitirince: cümle ortasında nefes tükenmez, ses titreşmez.</Text>
      <BreathTimer />
    </View>
  );
}

// ─── Rezonans Card ─────────────────────────────────────────────────────────────
function RezonansCard() {
  return (
    <View style={styles.card}>
      <SectionLabel>Rezonans Egzersizi</SectionLabel>
      <Text style={[styles.resonanceTitle]}>M · N · NG</Text>
      <Text style={styles.cardDesc}>
        Sesi göğse çeker, kalınlaştırır, otoriteli tını yaratır. Her sesi sırayla bitirince 1 set — toplamda 3 set.
      </Text>
      <View style={styles.infoBox}>
        <Text style={styles.infoBoxTitle}>Her Set — Bu Sırayla</Text>
        {[
          ['M','Mmmm — 5 saniye → direkt geçiş'],
          ['N','Nnnnn — 5 saniye → direkt geçiş'],
          ['NG','Ngngng — 5 saniye → direkt geçiş'],
          ['+','Mmm-maa · mee · mii · moo · muu'],
          ['✓','Ha-ha-ha × 5 → 1 set bitti'],
        ].map(([lbl, desc]) => (
          <View key={lbl} style={styles.infoRow}>
            <View style={styles.infoNum}><Text style={styles.infoNumText}>{lbl}</Text></View>
            <Text style={styles.infoDesc}>{desc}</Text>
          </View>
        ))}
      </View>
      <Text style={styles.note}>
        Titreşimi hiç hissedemiyorsan: sesi öne yönlendir, boğazdan değil önden geldiğini hayal et.
      </Text>
    </View>
  );
}

// ─── Isınma Card ───────────────────────────────────────────────────────────────
function IsinmaCard() {
  return (
    <View style={styles.card}>
      <SectionLabel>Artikülasyon Isınması</SectionLabel>
      <Text style={styles.vowelsText}>A · E · I · İ · O · Ö · U · Ü</Text>
      <View style={styles.infoBox}>
        <Text style={styles.infoBoxTitle}>3 Tur — A E I O U Kesintisiz</Text>
        {[
          ['1','Yavaş ve abartarak: Her ünlüde çene maksimum açılsın.'],
          ['2','Normal hızda: Akıcı ve temiz, her ünlü net çıksın.'],
          ['3','Hızlı: Maksimum hız ama netlik düşmesin.'],
        ].map(([n, t]) => (
          <View key={n} style={styles.infoRow}>
            <View style={styles.infoNum}><Text style={styles.infoNumText}>{n}</Text></View>
            <Text style={styles.infoDesc}>{t}</Text>
          </View>
        ))}
      </View>
      <Text style={styles.note}>Burun sesi fark edersen ağzı daha da aç, sesi önden çıkar.</Text>
    </View>
  );
}

// ─── Tekerleme Card ────────────────────────────────────────────────────────────
function TekerlemeCard({ twister, index, dayKey }) {
  const turLabels = [
    ['5× Yavaş',  'Her heceyi abartarak söyle — netlik önemli'],
    ['5× Normal', 'Doğal konuşma hızı — ses akıcı çıksın'],
    ['5× Hızlı',  'Maksimum hız — hiçbir ses yutulmasın'],
  ];
  const [dots, setDots] = useState([[0,0,0,0,0],[0,0,0,0,0],[0,0,0,0,0]]);

  const toggleDot = (turIdx, dotIdx) => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
    setDots(prev => {
      const next = prev.map(r => [...r]);
      const cur  = next[turIdx].filter(Boolean).length;
      const newCount = dotIdx < cur ? dotIdx : dotIdx + 1;
      next[turIdx] = next[turIdx].map((_, i) => i < newCount ? 1 : 0);
      return next;
    });
  };

  return (
    <View style={styles.twisterCard}>
      <View style={styles.twisterAccent} />
      <Text style={styles.twisterNo}>Tekerleme {index + 1}</Text>
      <Text style={styles.twisterText}>{twister.m}</Text>
      <Text style={styles.twisterNote}>{twister.n}</Text>
      <View style={styles.infoBox}>
        <Text style={styles.infoBoxTitle}>15 Tekrar · 3 Tur · Dokunarak işaretle</Text>
        {turLabels.map(([lbl, desc], turIdx) => {
          const count = dots[turIdx].filter(Boolean).length;
          const done  = count >= 5;
          return (
            <View key={turIdx} style={[styles.infoRow, done && styles.infoRowDone]}>
              <View style={[styles.infoNum, done && styles.infoNumDone]}>
                <Text style={[styles.infoNumText, done && styles.infoNumTextDone]}>
                  {turIdx + 1}
                </Text>
              </View>
              <View style={{ flex: 1 }}>
                <Text style={styles.infoDesc}>
                  <Text style={styles.infoBold}>{lbl}:</Text> {desc}
                </Text>
                <View style={styles.dotsCounter}>
                  {dots[turIdx].map((active, di) => (
                    <Pressable
                      key={di}
                      style={[styles.counterDot, active && styles.counterDotActive]}
                      onPress={() => toggleDot(turIdx, di)}
                    />
                  ))}
                </View>
              </View>
            </View>
          );
        })}
      </View>
    </View>
  );
}

// ─── Okuma Card ────────────────────────────────────────────────────────────────
function OkumaCard({ metin }) {
  if (!metin) return null;
  return (
    <View style={styles.readCard}>
      <SectionLabel>Sesli Okuma Metni</SectionLabel>
      <Text style={styles.readTitle}>{metin.baslik}</Text>
      <Text style={styles.readKategori}>{metin.kategori}</Text>
      <Text style={styles.readBody}>{metin.body}</Text>
      {metin.vurgu && metin.vurgu.length > 0 && (
        <View style={styles.infoBox}>
          <Text style={styles.infoBoxTitle}>Vurgu Pratiği</Text>
          {metin.vurgu.map((v, i) => (
            <View key={i} style={styles.infoRow}>
              <Text style={styles.vurguNum}>{v.s}.</Text>
              <Text style={styles.infoDesc}>{v.t}</Text>
            </View>
          ))}
        </View>
      )}
      {metin.tip && <Text style={styles.readTip}>{metin.tip}</Text>}
    </View>
  );
}

// ─── Siir ──────────────────────────────────────────────────────────────────────
function SiirCard({ poem }) {
  return (
    <View style={styles.siirWrap}>
      <Text style={styles.siirLabel}>♡ Tuğ için</Text>
      <Text style={styles.siirText}>{poem}</Text>
    </View>
  );
}

// ─── Done Button ───────────────────────────────────────────────────────────────
function DoneButton({ state, onMark }) {
  const mu     = maxUnlocked(state.completedDays);
  const day    = state.currentDay;
  const isDone = state.completedDays.includes(day);
  const isActive = day === mu && !isDone;
  const isLocked = day > mu;

  let label = '';
  let variant = 'locked';
  if (isLocked)       { label = '🔒 Önceki günü tamamla'; variant = 'locked'; }
  else if (isDone)    { label = '✓ Tamamlandı';           variant = 'done'; }
  else if (isActive)  { label = '✓ Bugünü Tamamladım';   variant = 'active'; }
  else                { label = 'Geçmiş gün';             variant = 'past'; }

  return (
    <View style={styles.doneBarWrap}>
      <LinearGradient
        colors={['transparent', colors.bg]}
        style={styles.doneBarGradient}
        pointerEvents="none"
      />
      <TouchableOpacity
        style={styles.doneBarInner}
        onPress={variant === 'active' ? onMark : undefined}
        activeOpacity={variant === 'active' ? 0.85 : 1}
      >
        {variant === 'active' ? (
          <LinearGradient
            colors={[colors.gold, colors.goldDim]}
            start={{ x: 0, y: 0 }} end={{ x: 1, y: 0 }}
            style={styles.doneBtn}
          >
            <Text style={[styles.doneBtnText, { color: colors.black }]}>{label}</Text>
          </LinearGradient>
        ) : (
          <View style={[
            styles.doneBtn,
            variant === 'done'   && styles.doneBtnDone,
            variant === 'locked' && styles.doneBtnLocked,
            variant === 'past'   && styles.doneBtnLocked,
          ]}>
            <Text style={[
              styles.doneBtnText,
              variant === 'done' && { color: colors.white },
            ]}>{label}</Text>
          </View>
        )}
      </TouchableOpacity>
    </View>
  );
}

// ─── Main Screen ───────────────────────────────────────────────────────────────
export default function HomeScreen({ state, onStateChange }) {
  const day       = state.currentDay;
  const weekIdx   = Math.floor(day / 7);
  const dayInWeek = day % 7;
  const setKey    = SETS[weekIdx];
  const teks      = TEKERLEMELER[setKey]?.[dayInWeek] ?? [];
  const metin     = METINLER[day];
  const poem      = SIIRLER[day] ?? SIIRLER[SIIRLER.length - 1];
  const isDone    = state.completedDays.includes(day);
  const scrollRef = useRef(null);

  const navigate = (dir) => {
    const mu   = maxUnlocked(state.completedDays);
    const next = day + dir;
    if (next < 0 || next > mu) return;
    const newState = { ...state, currentDay: next };
    onStateChange(newState);
    scrollRef.current?.scrollTo({ y: 0, animated: true });
  };

  const markDone = async () => {
    const mu = maxUnlocked(state.completedDays);
    if (day !== mu) return;
    if (state.completedDays.includes(day)) return;
    Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
    const newCompleted = [...state.completedDays, day];
    const newAt = { ...state.completedAt, [day]: new Date().toISOString() };
    const newStreak = calcStreak(newCompleted);
    const newState = { ...state, completedDays: newCompleted, completedAt: newAt, streak: newStreak };
    await saveState(newState);
    onStateChange(newState);
  };

  return (
    <SafeAreaView style={styles.safe}>
      <StatusBar barStyle="light-content" backgroundColor={colors.bg} />
      <Header state={state} />
      <DayNav state={state} onNavigate={navigate} />
      <ScrollView
        ref={scrollRef}
        style={styles.scroll}
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
      >
        {isDone && (
          <View style={styles.doneBanner}>
            <Text style={styles.doneBannerText}>✓ Bu günü tamamladın 🎉</Text>
          </View>
        )}

        <NefesCard />
        <RezonansCard />
        <IsinmaCard />

        <View style={styles.sesGrp}>
          <Text style={styles.sesGrpText}>{SES_GRUPLARI[dayInWeek]}</Text>
          <View style={styles.sesGrpLine} />
        </View>

        {teks.map((t, i) => (
          <TekerlemeCard key={`${day}-${i}`} twister={t} index={i} dayKey={`${day}-${i}`} />
        ))}

        <View style={styles.divider} />
        <OkumaCard metin={metin} />
        <SiirCard poem={poem} />
        <View style={{ height: 100 }} />
      </ScrollView>

      <DoneButton state={state} onMark={markDone} />
    </SafeAreaView>
  );
}

// ─── Styles ────────────────────────────────────────────────────────────────────
const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.bg },
  scroll: { flex: 1 },
  scrollContent: { paddingHorizontal: 14, paddingBottom: 20 },

  // Header
  header: { backgroundColor: colors.s1, borderBottomWidth: 1, borderBottomColor: colors.border, paddingHorizontal: 18, paddingTop: 14, paddingBottom: 12 },
  headerRow1: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 },
  logo: { fontFamily: fonts.display, fontSize: 22, color: colors.text },
  logoGold: { color: colors.gold },
  streakBadge: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.s2, borderWidth: 1, borderColor: colors.border, borderRadius: radius.full, paddingVertical: 5, paddingHorizontal: 12 },
  streakHot: { borderColor: colors.gold },
  streakText: { fontFamily: fonts.sansBold, fontSize: 13, color: colors.text },
  streakTextHot: { color: colors.gold },
  headerRow2: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  setLabel: { fontFamily: fonts.sansMedium, fontSize: 10, color: colors.muted, letterSpacing: 2, textTransform: 'uppercase' },
  dotsRow: { flexDirection: 'row', gap: 5, alignItems: 'center' },
  dot: { width: 7, height: 7, borderRadius: 4, backgroundColor: colors.border },
  dotDone: { backgroundColor: colors.green },
  dotActive: { width: 10, height: 10, borderRadius: 5, backgroundColor: colors.gold },

  // Nav
  nav: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingVertical: 11, paddingHorizontal: 16, backgroundColor: colors.s2, borderBottomWidth: 1, borderBottomColor: colors.border },
  navBtn: { backgroundColor: 'transparent', borderWidth: 1, borderColor: colors.border, borderRadius: 9, paddingVertical: 7, paddingHorizontal: 14 },
  navBtnDisabled: { opacity: 0.2 },
  navBtnText: { fontFamily: fonts.sansMedium, fontSize: 13, color: colors.sub },
  navBtnTextDisabled: { color: colors.muted },
  navMid: { alignItems: 'center' },
  navDay: { fontFamily: fonts.sansBold, fontSize: 15, color: colors.text },
  navSub: { fontFamily: fonts.sans, fontSize: 11, color: colors.muted, marginTop: 1 },

  // Section label
  sectionLabel: { flexDirection: 'row', alignItems: 'center', marginBottom: 10 },
  sectionLabelText: { fontFamily: fonts.sansBold, fontSize: 9, letterSpacing: 3, textTransform: 'uppercase', color: colors.muted },
  sectionLabelLine: { flex: 1, height: 1, backgroundColor: colors.border, marginLeft: 8 },

  // Cards
  card: { backgroundColor: colors.s1, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: 16, marginBottom: 10 },
  cardTitle: { fontFamily: fonts.sansBold, fontSize: 15, color: colors.text, marginBottom: 6 },
  cardDesc: { fontFamily: fonts.sans, fontSize: 12, color: colors.sub, lineHeight: 20, marginBottom: 12 },

  // Steps
  stepRow: { flexDirection: 'row', alignItems: 'flex-start', gap: 9, marginBottom: 6 },
  stepNum: { width: 22, height: 22, borderRadius: 11, backgroundColor: colors.s3, borderWidth: 1, borderColor: colors.border, alignItems: 'center', justifyContent: 'center', flexShrink: 0, marginTop: 1 },
  stepNumText: { fontFamily: fonts.sansBold, fontSize: 9, color: colors.gold },
  stepText: { flex: 1, fontFamily: fonts.sans, fontSize: 12, color: colors.sub, lineHeight: 19 },
  note: { fontFamily: fonts.sans, fontSize: 12, color: colors.muted, borderTopWidth: 1, borderTopColor: colors.border, paddingTop: 9, marginTop: 4, lineHeight: 18 },

  // Info box
  infoBox: { backgroundColor: colors.s2, borderWidth: 1, borderColor: colors.border, borderRadius: radius.md, padding: 12, marginTop: 8 },
  infoBoxTitle: { fontFamily: fonts.sansBold, fontSize: 9, letterSpacing: 2, textTransform: 'uppercase', color: colors.muted, marginBottom: 8 },
  infoRow: { flexDirection: 'row', alignItems: 'flex-start', gap: 9, marginBottom: 6 },
  infoRowDone: {},
  infoNum: { width: 20, height: 20, borderRadius: 10, backgroundColor: colors.s3, borderWidth: 1, borderColor: colors.border, alignItems: 'center', justifyContent: 'center', flexShrink: 0, marginTop: 1 },
  infoNumDone: { backgroundColor: colors.green, borderColor: colors.green },
  infoNumText: { fontFamily: fonts.sansBold, fontSize: 9, color: colors.gold },
  infoNumTextDone: { color: colors.white },
  infoDesc: { flex: 1, fontFamily: fonts.sans, fontSize: 12, color: colors.sub, lineHeight: 18 },
  infoBold: { fontFamily: fonts.sansBold, color: colors.text },
  vurguNum: { fontFamily: fonts.sansBold, fontSize: 12, color: colors.gold, flexShrink: 0 },

  // Resonance
  resonanceTitle: { fontFamily: fonts.display, fontSize: 28, color: colors.gold, letterSpacing: 8, marginBottom: 6 },

  // Vowels warm-up
  vowelsText: { fontFamily: fonts.display, fontSize: 28, fontWeight: '700', letterSpacing: 8, color: colors.gold, marginBottom: 10 },

  // Ses grubu label
  sesGrp: { flexDirection: 'row', alignItems: 'center', marginVertical: 16 },
  sesGrpText: { fontFamily: fonts.sansBold, fontSize: 10, letterSpacing: 2.5, textTransform: 'uppercase', color: colors.gold },
  sesGrpLine: { flex: 1, height: 1, backgroundColor: colors.border, marginLeft: 8 },

  // Twister card
  twisterCard: { backgroundColor: colors.s1, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: 16, marginBottom: 8, overflow: 'hidden', position: 'relative' },
  twisterAccent: { position: 'absolute', left: 0, top: 0, bottom: 0, width: 3, backgroundColor: colors.gold, opacity: 0.4 },
  twisterNo: { fontFamily: fonts.sansBold, fontSize: 9, letterSpacing: 2, textTransform: 'uppercase', color: colors.muted, marginBottom: 9 },
  twisterText: { fontFamily: fonts.sansMedium, fontSize: 17, lineHeight: 28, color: colors.text, marginBottom: 12 },
  twisterNote: { fontFamily: fonts.sans, fontSize: 12, color: colors.sub, borderTopWidth: 1, borderTopColor: colors.border, paddingTop: 10, lineHeight: 19, marginBottom: 10 },
  dotsCounter: { flexDirection: 'row', gap: 6, marginTop: 6, flexWrap: 'wrap' },
  counterDot: { width: 28, height: 28, borderRadius: 14, borderWidth: 1.5, borderColor: colors.border, backgroundColor: colors.s3 },
  counterDotActive: { backgroundColor: colors.gold, borderColor: colors.gold },

  // Divider
  divider: { height: 1, backgroundColor: colors.border, marginVertical: 22 },

  // Reading card
  readCard: { backgroundColor: colors.s1, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: 20, marginBottom: 10 },
  readTitle: { fontFamily: fonts.display, fontSize: 22, color: colors.text, lineHeight: 30, marginBottom: 4 },
  readKategori: { fontFamily: fonts.sansMedium, fontSize: 10, color: colors.gold, letterSpacing: 2, textTransform: 'uppercase', marginBottom: 16 },
  readBody: { fontFamily: fonts.sans, fontSize: 15, lineHeight: 28, color: '#b0c2d4', marginBottom: 4 },
  readTip: { fontFamily: fonts.sans, fontSize: 12, color: colors.sub, borderTopWidth: 1, borderTopColor: colors.border, paddingTop: 12, marginTop: 12, lineHeight: 19 },

  // Poem
  siirWrap: { alignItems: 'center', paddingVertical: 24, paddingHorizontal: 14 },
  siirLabel: { fontFamily: fonts.display, fontSize: 13, color: colors.gold, letterSpacing: 1, marginBottom: 8, fontStyle: 'italic' },
  siirText: { fontFamily: fonts.displayItalic, fontSize: 16, color: colors.sub, lineHeight: 28, textAlign: 'center', fontStyle: 'italic' },

  // Done banner
  doneBanner: { backgroundColor: 'rgba(46,158,94,0.1)', borderWidth: 1, borderColor: 'rgba(46,158,94,0.25)', borderRadius: radius.md, padding: 12, marginBottom: 14, marginTop: 10 },
  doneBannerText: { fontFamily: fonts.sansSemiBold, fontSize: 13, color: colors.green, textAlign: 'center' },

  // Done button
  doneBarWrap: { position: 'absolute', bottom: 0, left: 0, right: 0 },
  doneBarGradient: { height: 32, position: 'absolute', top: -32, left: 0, right: 0 },
  doneBarInner: { paddingHorizontal: 14, paddingBottom: 28, paddingTop: 10, backgroundColor: colors.bg },
  doneBtn: { borderRadius: radius.md, paddingVertical: 16, alignItems: 'center' },
  doneBtnDone: { backgroundColor: colors.green },
  doneBtnLocked: { backgroundColor: colors.s2, borderWidth: 1, borderColor: colors.border },
  doneBtnText: { fontFamily: fonts.sansBold, fontSize: 15, color: colors.muted },

  // Breath timer
  breathTimer: { marginTop: 14, borderTopWidth: 1, borderTopColor: colors.border, paddingTop: 12 },
  breathBtn: { backgroundColor: colors.s3, borderWidth: 1, borderColor: colors.border, borderRadius: radius.md, padding: 10, alignItems: 'center' },
  breathBtnText: { fontFamily: fonts.sansSemiBold, fontSize: 13, color: colors.text },
  breathDisplay: { alignItems: 'center', paddingTop: 16, paddingBottom: 4, gap: 4 },
  breathPhase: { fontFamily: fonts.sansBold, fontSize: 11, letterSpacing: 3, textTransform: 'uppercase', color: colors.muted },
  breathCount: { fontFamily: fonts.display, fontSize: 68, lineHeight: 76 },
  breathSet: { fontFamily: fonts.sans, fontSize: 11, color: colors.muted, marginTop: 2 },
  breathDone: { alignItems: 'center', paddingVertical: 12 },
  breathDoneText: { fontFamily: fonts.sansSemiBold, fontSize: 13, color: colors.green },
});
