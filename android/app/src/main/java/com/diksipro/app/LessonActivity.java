package com.diksipro.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class LessonActivity extends Activity {

    int dayIndex;
    JSONObject dayData;
    JSONArray programData;

    // Breath timer state
    static final String[] PHASES      = {"Nefes Al", "Tut", "Nefes Ver"};
    static final int[]    DURATIONS   = {4, 2, 6};
    static final int[]    COLORS      = {0xFF4E90FF, 0xFFFFB94A, 0xFF2ECC71};
    int breathPhase = 0, breathCount = 4, breathSet = 1;
    boolean breathRunning = false;
    CountDownTimer breathTimer;
    TextView breathPhaseTv, breathCountTv, breathSetTv, breathBtn;
    View breathDisplay;

    // Tongue twister counters: [tek_index][tur_index] → dot count
    int[][] turCounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(0xFF0A0D16);
        getWindow().setNavigationBarColor(0xFF0A0D16);

        dayIndex = getIntent().getIntExtra("day", 0);
        loadData();

        buildUI();
    }

    void loadData() {
        try {
            InputStream is = getAssets().open("program.json");
            byte[] buf = new byte[is.available()];
            is.read(buf);
            is.close();
            programData = new JSONArray(new String(buf, StandardCharsets.UTF_8));
            dayData = programData.getJSONObject(dayIndex);
        } catch (Exception e) {
            dayData = new JSONObject();
        }
    }

    void buildUI() {
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(0xFF0A0D16);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, dp(8), 0, dp(120));

        // ── Top bar
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setBackgroundColor(0xFF0F1520);
        topBar.setPadding(dp(16), dp(14), dp(16), dp(14));

        TextView backBtn = new TextView(this);
        backBtn.setText("←");
        backBtn.setTextSize(22);
        backBtn.setTextColor(0xFF8896AA);
        backBtn.setOnClickListener(v -> finish());
        topBar.addView(backBtn, new LinearLayout.LayoutParams(dp(40), dp(40)));

        LinearLayout titleCol = new LinearLayout(this);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleLp.setMargins(dp(8), 0, dp(8), 0);

        TextView dayTv = new TextView(this);
        dayTv.setText("Gün " + (dayIndex + 1));
        dayTv.setTextSize(16);
        dayTv.setTypeface(Typeface.DEFAULT_BOLD);
        dayTv.setTextColor(0xFFEEF2F8);
        titleCol.addView(dayTv, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        String sesGrubu = safe(dayData, "sesGrubu");
        TextView sesGrubuTv = new TextView(this);
        sesGrubuTv.setText(sesGrubu);
        sesGrubuTv.setTextSize(11);
        sesGrubuTv.setTextColor(0xFFFF6B35);
        titleCol.addView(sesGrubuTv, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        topBar.addView(titleCol, titleLp);

        root.addView(topBar, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ── Section 1: NEFES
        root.addView(makeSectionLabel("DİYAFRAM NEFESİ"), lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(makeBreathCard(), lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ── Section 2: REZONANS
        root.addView(makeSectionLabel("REZONANS"), lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(makeRezonansCard(), lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ── Section 3: ISINMA
        root.addView(makeSectionLabel("ARTİKÜLASYON ISINMASI"), lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(makeIsinmaCard(), lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ── Section 4: TEKERLEMELER
        root.addView(makeSesGrubuDivider(sesGrubu), lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addTekerlemeler(root);

        // ── Section 5: OKUMA
        root.addView(makeSectionLabel("SESLİ OKUMA"), lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(makeOkumaCard(), lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        sv.addView(root);
        setContentView(sv);

        // ── Fixed bottom bar with "Tamamla" button
        FrameLayout container = new FrameLayout(this);
        container.addView(sv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout bottomBar = new LinearLayout(this);
        bottomBar.setOrientation(LinearLayout.VERTICAL);
        bottomBar.setPadding(dp(16), dp(10), dp(16), dp(24));
        bottomBar.setBackgroundDrawable(getDrawable(R.drawable.bottom_bar_bg));

        boolean isDone = isDayCompleted(dayIndex);
        TextView doneBtn = new TextView(this);
        if (isDone) {
            doneBtn.setText("✓  Tamamlandı");
            doneBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_done));
            doneBtn.setTextColor(0xFF2ECC71);
        } else {
            doneBtn.setText("✓  Bugünü Tamamladım");
            doneBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_primary));
            doneBtn.setTextColor(0xFF000000);
        }
        doneBtn.setTextSize(15);
        doneBtn.setTypeface(Typeface.DEFAULT_BOLD);
        doneBtn.setGravity(Gravity.CENTER);
        doneBtn.setOnClickListener(v -> {
            if (!isDayCompleted(dayIndex)) markDone();
            finish();
        });
        bottomBar.addView(doneBtn, lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)));

        FrameLayout.LayoutParams barLp = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        barLp.gravity = Gravity.BOTTOM;
        container.addView(bottomBar, barLp);

        setContentView(container);
    }

    // ── BREATH CARD ──────────────────────────────────────────────────────────────

    View makeBreathCard() {
        FrameLayout card = makeCard();
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(18), dp(18), dp(18), dp(18));

        TextView title = makeTitleTv("4 · 2 · 6 Nefes Tekniği");
        col.addView(title, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView sub = makeSubTv("Sesin gücü diyaframdan gelir. 5 set yap — nefes ortasında bitmesin, ses titreşmesin.");
        col.addView(sub, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Steps
        col.addView(makeStepRow("1", "4 sayarak burundan nefes al — yalnızca karın şişsin."));
        col.addView(makeStepRow("2", "2 sayarak nefes tut. Omuzlar düşük, çene serbest."));
        col.addView(makeStepRow("3", "6 sayarak ağızdan yavaşça ver — karın içeri çekilsin."));

        // Timer button
        breathBtn = new TextView(this);
        breathBtn.setText("▶  Zamanlayıcıyı Başlat");
        breathBtn.setTextSize(14);
        breathBtn.setTypeface(Typeface.DEFAULT_BOLD);
        breathBtn.setTextColor(0xFF0A0D16);
        breathBtn.setGravity(Gravity.CENTER);
        breathBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_primary));
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        bLp.setMargins(0, dp(14), 0, dp(12));
        breathBtn.setOnClickListener(v -> toggleBreath());
        col.addView(breathBtn, bLp);

        // Timer display (hidden initially)
        LinearLayout display = new LinearLayout(this);
        display.setOrientation(LinearLayout.VERTICAL);
        display.setGravity(Gravity.CENTER);
        display.setVisibility(View.GONE);
        display.setPadding(0, dp(8), 0, dp(4));
        breathDisplay = display;

        breathPhaseTv = new TextView(this);
        breathPhaseTv.setText("Nefes Al");
        breathPhaseTv.setTextSize(11);
        breathPhaseTv.setLetterSpacing(0.18f);
        breathPhaseTv.setTextColor(0xFF8896AA);
        breathPhaseTv.setGravity(Gravity.CENTER);
        display.addView(breathPhaseTv, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        breathCountTv = new TextView(this);
        breathCountTv.setText("4");
        breathCountTv.setTextSize(64);
        breathCountTv.setTypeface(Typeface.DEFAULT_BOLD);
        breathCountTv.setTextColor(0xFF4E90FF);
        breathCountTv.setGravity(Gravity.CENTER);
        display.addView(breathCountTv, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        breathSetTv = new TextView(this);
        breathSetTv.setText("Set 1 / 5");
        breathSetTv.setTextSize(11);
        breathSetTv.setTextColor(0xFF556677);
        breathSetTv.setGravity(Gravity.CENTER);
        display.addView(breathSetTv, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        col.addView(display, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        card.addView(col);
        return card;
    }

    void toggleBreath() {
        if (breathRunning) {
            stopBreath();
        } else {
            startBreath();
        }
    }

    void startBreath() {
        breathRunning = true;
        breathPhase = 0; breathCount = DURATIONS[0]; breathSet = 1;
        breathDisplay.setVisibility(View.VISIBLE);
        breathBtn.setText("■  Durdur");
        breathBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_outline));
        breathBtn.setTextColor(0xFF8896AA);
        updateBreathDisplay();
        scheduleBreathTick();
    }

    void scheduleBreathTick() {
        if (!breathRunning) return;
        breathTimer = new CountDownTimer(1000, 1000) {
            @Override public void onTick(long ms) {}
            @Override public void onFinish() {
                if (!breathRunning) return;
                breathCount--;
                if (breathCount <= 0) {
                    breathPhase++;
                    if (breathPhase >= PHASES.length) {
                        breathPhase = 0;
                        breathSet++;
                        if (breathSet > 5) {
                            stopBreath();
                            breathBtn.setText("✓  5 Set Tamamlandı");
                            breathBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_done));
                            breathBtn.setTextColor(0xFF2ECC71);
                            return;
                        }
                    }
                    breathCount = DURATIONS[breathPhase];
                }
                updateBreathDisplay();
                scheduleBreathTick();
            }
        }.start();
    }

    void stopBreath() {
        breathRunning = false;
        if (breathTimer != null) { breathTimer.cancel(); breathTimer = null; }
        breathBtn.setText("▶  Zamanlayıcıyı Başlat");
        breathBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_primary));
        breathBtn.setTextColor(0xFF0A0D16);
    }

    void updateBreathDisplay() {
        breathPhaseTv.setText(PHASES[breathPhase].toUpperCase());
        breathCountTv.setText(String.valueOf(breathCount));
        breathCountTv.setTextColor(COLORS[breathPhase]);
        breathSetTv.setText("Set " + breathSet + " / 5");
    }

    // ── REZONANS CARD ────────────────────────────────────────────────────────────

    View makeRezonansCard() {
        FrameLayout card = makeCard();
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(18), dp(18), dp(18), dp(18));

        TextView humTv = new TextView(this);
        humTv.setText("M · N · NG");
        humTv.setTextSize(26);
        humTv.setTypeface(Typeface.DEFAULT_BOLD);
        humTv.setTextColor(0xFFFFB94A);
        humTv.setLetterSpacing(0.1f);
        humTv.setPadding(0, 0, 0, dp(6));
        col.addView(humTv, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        col.addView(makeSubTv("Sesi göğse çeker, kalınlaştırır. 3 set — her sette M→N→NG→Mmmaa→Ha×5 sırası."));

        // Steps
        col.addView(makeRezoStep("M", "Dudaklar kapalı, 5 sn — titreşim hissedilmeli."));
        col.addView(makeRezoStep("N", "Dil ucu üst dişler arkasına, 5 sn."));
        col.addView(makeRezoStep("NG", "Dil kökü yumuşak damağa, 5 sn — kafanın arkasında titreşim."));
        col.addView(makeRezoStep("→", "Mmm-maa · mee · mii · moo · muu (ses kesilmeden)"));
        col.addView(makeRezoStep("✓", "Ha-ha-ha × 5 — karın içeri çekilmeli. 1 set bitti."));

        card.addView(col);
        return card;
    }

    // ── ISINMA CARD ──────────────────────────────────────────────────────────────

    View makeIsinmaCard() {
        FrameLayout card = makeCard();
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(18), dp(18), dp(18), dp(18));

        TextView vowelsTv = new TextView(this);
        vowelsTv.setText("A · E · I · İ · O · Ö · U · Ü");
        vowelsTv.setTextSize(20);
        vowelsTv.setTypeface(Typeface.DEFAULT_BOLD);
        vowelsTv.setTextColor(0xFF4E90FF);
        vowelsTv.setLetterSpacing(0.05f);
        vowelsTv.setPadding(0, 0, 0, dp(6));
        col.addView(vowelsTv, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        col.addView(makeSubTv("3 tur — sesler arası direkt geçiş, bekleme yok."));
        col.addView(makeStepRow("1", "Yavaş ve abartarak — her ünlüde çene maksimum açılsın."));
        col.addView(makeStepRow("2", "Normal hızda — akıcı ve temiz, birbirine karışmasın."));
        col.addView(makeStepRow("3", "Hızlı — netlik düşmesin, burun sesine kaçma."));

        card.addView(col);
        return card;
    }

    // ── TEKERLEMELER ─────────────────────────────────────────────────────────────

    void addTekerlemeler(LinearLayout root) {
        try {
            JSONArray teks = dayData.getJSONArray("teks");
            turCounts = new int[teks.length()][3];

            for (int i = 0; i < teks.length(); i++) {
                JSONObject tek = teks.getJSONObject(i);
                String metin = tek.getString("metin");
                String not   = tek.getString("not");

                FrameLayout card = makeCard();
                LinearLayout col = new LinearLayout(this);
                col.setOrientation(LinearLayout.VERTICAL);
                col.setPadding(dp(18), dp(18), dp(18), dp(18));

                // Tekerleme number
                TextView numTv = new TextView(this);
                numTv.setText("TEKERLEME " + (i + 1));
                numTv.setTextSize(9);
                numTv.setLetterSpacing(0.22f);
                numTv.setTextColor(0xFF4A6070);
                numTv.setPadding(0, 0, 0, dp(10));
                col.addView(numTv, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                // Main text
                TextView metinTv = new TextView(this);
                metinTv.setText(metin);
                metinTv.setTextSize(17);
                metinTv.setTextColor(0xFFEEF2F8);
                metinTv.setLineSpacing(dp(4), 1.2f);
                metinTv.setPadding(0, 0, 0, dp(12));
                col.addView(metinTv, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                // Note
                TextView notTv = new TextView(this);
                notTv.setText(not);
                notTv.setTextSize(12);
                notTv.setTextColor(0xFF8896AA);
                notTv.setLineSpacing(dp(2), 1.4f);
                notTv.setPadding(0, 0, 0, dp(16));
                col.addView(notTv, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                // Divider
                View div = new View(this);
                div.setBackgroundColor(0xFF1D2A3A);
                LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
                divLp.setMargins(0, 0, 0, dp(14));
                col.addView(div, divLp);

                // Tur counter label
                TextView turLabel = new TextView(this);
                turLabel.setText("15 TEKRAR  ·  3 TUR  ·  Dokunarak işaretle");
                turLabel.setTextSize(9);
                turLabel.setLetterSpacing(0.15f);
                turLabel.setTextColor(0xFF4A6070);
                turLabel.setPadding(0, 0, 0, dp(10));
                col.addView(turLabel, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                // 3 tur rows
                String[] turNames = {"5× Yavaş", "5× Normal", "5× Hızlı"};
                String[] turDescs = {"Abartarak söyle — her hece ayrı", "Doğal hız — ses akıcı ve temiz", "Maksimum hız — hiçbir ses yutulmasın"};
                for (int j = 0; j < 3; j++) {
                    col.addView(makeTurRow(i, j, turNames[j], turDescs[j]));
                }

                card.addView(col);
                LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                cardLp.setMargins(dp(14), 0, dp(14), dp(12));
                root.addView(card, cardLp);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    View makeTurRow(int tekIdx, int turIdx, String name, String desc) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.setMargins(0, 0, 0, dp(10));
        row.setLayoutParams(rowLp);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        // Number circle
        TextView numCircle = new TextView(this);
        numCircle.setText(String.valueOf(turIdx + 1));
        numCircle.setTextSize(10);
        numCircle.setTypeface(Typeface.DEFAULT_BOLD);
        numCircle.setTextColor(0xFFFF6B35);
        numCircle.setGravity(Gravity.CENTER);
        numCircle.setBackgroundDrawable(getDrawable(R.drawable.circle_coral_outline));
        LinearLayout.LayoutParams nLp = new LinearLayout.LayoutParams(dp(22), dp(22));
        nLp.setMargins(0, 0, dp(10), 0);
        top.addView(numCircle, nLp);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        TextView nameTv = new TextView(this);
        nameTv.setText(name);
        nameTv.setTextSize(12);
        nameTv.setTypeface(Typeface.DEFAULT_BOLD);
        nameTv.setTextColor(0xFFEEF2F8);
        col.addView(nameTv, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView descTv = new TextView(this);
        descTv.setText(desc);
        descTv.setTextSize(11);
        descTv.setTextColor(0xFF6A7A8A);
        col.addView(descTv, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        top.addView(col, cLp);
        row.addView(top, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // 5 dots
        LinearLayout dotsRow = new LinearLayout(this);
        dotsRow.setOrientation(LinearLayout.HORIZONTAL);
        dotsRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams drLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        drLp.setMargins(dp(32), dp(8), 0, 0);
        dotsRow.setLayoutParams(drLp);

        View[] dots = new View[5];
        for (int d = 0; d < 5; d++) {
            View dot = new View(this);
            LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(dp(28), dp(28));
            dLp.setMargins(0, 0, dp(6), 0);
            dot.setLayoutParams(dLp);
            dot.setBackgroundDrawable(getDrawable(R.drawable.dot_tap_idle));
            dots[d] = dot;
            final int di = d;
            final int ti = tekIdx, tj = turIdx;
            dot.setOnClickListener(v -> {
                // Toggle: tap below current → set to that, tap at/above current → set to 0
                int cur = turCounts[ti][tj];
                turCounts[ti][tj] = (di < cur) ? di : di + 1;
                for (int dd = 0; dd < 5; dd++) {
                    dots[dd].setBackgroundDrawable(getDrawable(
                        dd < turCounts[ti][tj] ? R.drawable.dot_tap_active : R.drawable.dot_tap_idle));
                }
            });
            dotsRow.addView(dot);
        }
        row.addView(dotsRow);
        return row;
    }

    // ── OKUMA CARD ───────────────────────────────────────────────────────────────

    View makeOkumaCard() {
        FrameLayout card = makeCard();
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(18), dp(20), dp(18), dp(18));

        try {
            JSONObject okuma = dayData.getJSONObject("okuma");
            String baslik   = okuma.getString("baslik");
            String kategori = okuma.getString("kategori");
            String body     = okuma.getString("body");
            String tip      = okuma.optString("tip", "");
            JSONArray vurgu = okuma.optJSONArray("vurgu");

            TextView katTv = new TextView(this);
            katTv.setText(kategori.toUpperCase());
            katTv.setTextSize(9);
            katTv.setLetterSpacing(0.2f);
            katTv.setTextColor(0xFFFF6B35);
            katTv.setPadding(0, 0, 0, dp(6));
            col.addView(katTv, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            TextView baslikTv = new TextView(this);
            baslikTv.setText(baslik);
            baslikTv.setTextSize(22);
            baslikTv.setTypeface(Typeface.DEFAULT_BOLD);
            baslikTv.setTextColor(0xFFEEF2F8);
            baslikTv.setPadding(0, 0, 0, dp(16));
            col.addView(baslikTv, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            // Body text — split into paragraphs
            String[] paragraphs = body.split("\n\n?");
            for (String para : paragraphs) {
                if (para.trim().isEmpty()) continue;
                TextView pTv = new TextView(this);
                pTv.setText(para.trim());
                pTv.setTextSize(15);
                pTv.setTextColor(0xFFB0C2D4);
                pTv.setLineSpacing(dp(4), 1.55f);
                LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                pLp.setMargins(0, 0, 0, dp(14));
                col.addView(pTv, pLp);
            }

            // Vurgu section
            if (vurgu != null && vurgu.length() > 0) {
                View vDiv = new View(this);
                vDiv.setBackgroundColor(0xFF1D2A3A);
                LinearLayout.LayoutParams vDivLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
                vDivLp.setMargins(0, dp(4), 0, dp(14));
                col.addView(vDiv, vDivLp);

                TextView vLabel = new TextView(this);
                vLabel.setText("VURGU PRATİĞİ");
                vLabel.setTextSize(9);
                vLabel.setLetterSpacing(0.22f);
                vLabel.setTextColor(0xFF4A6070);
                vLabel.setPadding(0, 0, 0, dp(10));
                col.addView(vLabel, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                for (int i = 0; i < vurgu.length(); i++) {
                    JSONObject v = vurgu.getJSONObject(i);
                    LinearLayout vRow = new LinearLayout(this);
                    vRow.setOrientation(LinearLayout.HORIZONTAL);
                    vRow.setGravity(Gravity.TOP);
                    LinearLayout.LayoutParams vrLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    vrLp.setMargins(0, 0, 0, dp(8));
                    vRow.setLayoutParams(vrLp);

                    TextView numTv = new TextView(this);
                    numTv.setText(v.getString("s") + ".");
                    numTv.setTextSize(12);
                    numTv.setTypeface(Typeface.DEFAULT_BOLD);
                    numTv.setTextColor(0xFFFF6B35);
                    LinearLayout.LayoutParams numLp = new LinearLayout.LayoutParams(dp(20), ViewGroup.LayoutParams.WRAP_CONTENT);
                    numLp.setMargins(0, 0, dp(8), 0);
                    vRow.addView(numTv, numLp);

                    TextView tTv = new TextView(this);
                    tTv.setText(v.getString("t"));
                    tTv.setTextSize(12);
                    tTv.setTextColor(0xFF8896AA);
                    tTv.setLineSpacing(dp(2), 1.4f);
                    vRow.addView(tTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    col.addView(vRow);
                }
            }

            // Tip
            if (!tip.isEmpty()) {
                View tDiv = new View(this);
                tDiv.setBackgroundColor(0xFF1D2A3A);
                LinearLayout.LayoutParams tDivLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
                tDivLp.setMargins(0, dp(8), 0, dp(10));
                col.addView(tDiv, tDivLp);

                TextView tipTv = new TextView(this);
                tipTv.setText(tip);
                tipTv.setTextSize(12);
                tipTv.setTextColor(0xFF6A7A8A);
                tipTv.setLineSpacing(dp(2), 1.4f);
                col.addView(tipTv, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        } catch (Exception e) {
            col.addView(makeSubTv("Yüklenemedi."));
        }

        card.addView(col);
        return card;
    }

    // ── MARK DONE ─────────────────────────────────────────────────────────────────

    void markDone() {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
        String completedStr = prefs.getString(MainActivity.KEY_COMPLETED, "");

        // Parse completed set
        java.util.Set<Integer> completedSet = new java.util.HashSet<>();
        if (!completedStr.isEmpty()) {
            for (String s : completedStr.split(",")) {
                try { completedSet.add(Integer.parseInt(s)); } catch (Exception ignored) {}
            }
        }
        completedSet.add(dayIndex);

        // Rebuild string
        StringBuilder sb = new StringBuilder();
        for (int d : completedSet) { if (sb.length() > 0) sb.append(","); sb.append(d); }

        // Calculate streak (no streams — compatible with API 21+)
        int streak = 0;
        java.util.List<Integer> sortedList = new java.util.ArrayList<>(completedSet);
        java.util.Collections.sort(sortedList);
        for (int i = sortedList.size() - 1; i >= 0; i--) {
            if (i == sortedList.size() - 1 || sortedList.get(i) == sortedList.get(i + 1) - 1) streak++;
            else break;
        }

        prefs.edit()
            .putString(MainActivity.KEY_COMPLETED, sb.toString())
            .putInt(MainActivity.KEY_STREAK, streak)
            .apply();
    }

    boolean isDayCompleted(int day) {
        String completedStr = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
            .getString(MainActivity.KEY_COMPLETED, "");
        if (completedStr.isEmpty()) return false;
        for (String s : completedStr.split(",")) {
            try { if (Integer.parseInt(s) == day) return true; } catch (Exception ignored) {}
        }
        return false;
    }

    // ── VIEW HELPERS ─────────────────────────────────────────────────────────────

    View makeSectionLabel(String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(18), dp(22), dp(18), dp(8));

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(9);
        tv.setLetterSpacing(0.25f);
        tv.setTextColor(0xFF4A6070);
        row.addView(tv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        View line = new View(this);
        line.setBackgroundColor(0xFF1D2A3A);
        LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(0, dp(1), 1f);
        lineLp.setMargins(dp(10), 0, 0, 0);
        row.addView(line, lineLp);
        return row;
    }

    View makeSesGrubuDivider(String sesGrubu) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(18), dp(22), dp(18), dp(8));

        TextView tv = new TextView(this);
        tv.setText(sesGrubu);
        tv.setTextSize(11);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(0xFFFF6B35);
        row.addView(tv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        View line = new View(this);
        line.setBackgroundColor(0xFF1D2A3A);
        LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(0, dp(1), 1f);
        lineLp.setMargins(dp(10), 0, 0, 0);
        row.addView(line, lineLp);
        return row;
    }

    FrameLayout makeCard() {
        FrameLayout card = new FrameLayout(this);
        card.setBackgroundDrawable(getDrawable(R.drawable.card_bg));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(14), 0, dp(14), dp(12));
        card.setLayoutParams(lp);
        return card;
    }

    TextView makeTitleTv(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(16);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(0xFFEEF2F8);
        tv.setPadding(0, 0, 0, dp(6));
        return tv;
    }

    TextView makeSubTv(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(13);
        tv.setTextColor(0xFF8896AA);
        tv.setLineSpacing(dp(3), 1f);
        tv.setPadding(0, 0, 0, dp(14));
        return tv;
    }

    View makeStepRow(String num, String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.setMargins(0, 0, 0, dp(8));
        row.setLayoutParams(rowLp);

        TextView numTv = new TextView(this);
        numTv.setText(num);
        numTv.setTextSize(9);
        numTv.setTypeface(Typeface.DEFAULT_BOLD);
        numTv.setTextColor(0xFFFF6B35);
        numTv.setGravity(Gravity.CENTER);
        numTv.setBackgroundDrawable(getDrawable(R.drawable.circle_coral_outline));
        LinearLayout.LayoutParams nLp = new LinearLayout.LayoutParams(dp(22), dp(22));
        nLp.setMargins(0, dp(2), dp(10), 0);
        row.addView(numTv, nLp);

        TextView textTv = new TextView(this);
        textTv.setText(text);
        textTv.setTextSize(12);
        textTv.setTextColor(0xFF8896AA);
        textTv.setLineSpacing(dp(2), 1.4f);
        row.addView(textTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return row;
    }

    View makeRezoStep(String sym, String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.setMargins(0, 0, 0, dp(8));
        row.setLayoutParams(rowLp);

        TextView symTv = new TextView(this);
        symTv.setText(sym);
        symTv.setTextSize(14);
        symTv.setTypeface(Typeface.DEFAULT_BOLD);
        symTv.setTextColor(0xFFFFB94A);
        symTv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(dp(32), ViewGroup.LayoutParams.WRAP_CONTENT);
        sLp.setMargins(0, 0, dp(8), 0);
        row.addView(symTv, sLp);

        TextView textTv = new TextView(this);
        textTv.setText(text);
        textTv.setTextSize(12);
        textTv.setTextColor(0xFF8896AA);
        textTv.setLineSpacing(dp(2), 1.35f);
        row.addView(textTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return row;
    }

    String safe(JSONObject obj, String key) {
        try { return obj.getString(key); } catch (Exception e) { return ""; }
    }

    LinearLayout.LayoutParams lp(int w, int h) {
        return new LinearLayout.LayoutParams(w, h);
    }

    int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        stopBreath();
        super.onDestroy();
    }
}
