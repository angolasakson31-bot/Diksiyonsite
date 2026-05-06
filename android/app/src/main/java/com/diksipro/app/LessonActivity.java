package com.diksipro.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LessonActivity extends Activity {

    int dayIndex;
    JSONObject dayData;

    // Minimalist depth palette — warm orange
    static final int BG     = 0xFF0C0906;
    static final int CARD   = 0xFF141009;
    static final int CARD2  = 0xFF1D1510;
    static final int BORDER = 0xFF2D2016;
    static final int GOLD   = 0xFFD97D2E;
    static final int TEXT   = 0xFFEEE0CC;
    static final int SUB    = 0xFF9C8670;
    static final int MUTED  = 0xFF5C4838;
    static final int GREEN  = 0xFF30A858;
    static final int BLUE   = 0xFF4E8CF5;

    static final String[] BREATH_PHASES = {"NEFES AL", "TUT", "NEFES VER"};
    static final int[]    BREATH_DUR    = {4, 2, 6};
    static final int[]    BREATH_COLORS = {BLUE, 0xFFFFB94A, GREEN};

    int bPhase = 0, bCount = 4, bSet = 1;
    boolean bRunning = false;
    CountDownTimer bTimer;
    TextView breathPhaseTv, breathCountTv, breathSetTv, breathBtn;
    View breathDisplay;

    int[][] turCounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        dayIndex = getIntent().getIntExtra("day", 0);
        loadData();
        buildUI();
    }

    void loadData() {
        try {
            InputStream is = getAssets().open("program.json");
            byte[] buf = new byte[is.available()];
            is.read(buf); is.close();
            JSONArray arr = new JSONArray(new String(buf, StandardCharsets.UTF_8));
            dayData = arr.getJSONObject(dayIndex);
        } catch (Exception e) { dayData = new JSONObject(); }
    }

    void buildUI() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG);

        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(BG);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, 0, 0, dp(110));

        content.addView(buildTopBar());

        content.addView(secLabel("DİYAFRAM NEFESİ", BLUE));
        content.addView(buildBreathCard());

        content.addView(secLabel("REZONANS", GOLD));
        content.addView(buildRezonansCard());

        content.addView(secLabel("ARTİKÜLASYON ISINMASI", GREEN));
        content.addView(buildIsinmaCard());

        buildTekerlemeler(content);

        content.addView(secLabel("SESLİ OKUMA METNİ", SUB));
        content.addView(buildOkumaCard());

        sv.addView(content, new ScrollView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(sv, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        FrameLayout.LayoutParams barLp = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        barLp.gravity = Gravity.BOTTOM;
        root.addView(buildDoneBar(), barLp);

        setContentView(root);
    }

    // ── TOP BAR ───────────────────────────────────────────────────────────────

    View buildTopBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(CARD);
        bar.setPadding(dp(18), dp(16), dp(22), dp(16));

        TextView back = new TextView(this);
        back.setText("←");
        back.setTextSize(22);
        back.setTextColor(SUB);
        back.setPadding(0, 0, dp(18), 0);
        back.setOnClickListener(v -> finish());
        bar.addView(back);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        String set = safeStr(dayData, "set");
        int setWeek = (dayIndex / 7) % 3 + 1;
        TextView setLbl = new TextView(this);
        setLbl.setText("Set " + (set.isEmpty() ? "" : set.charAt(0)) + "  ·  " + setWeek + ". Hafta");
        setLbl.setTextSize(10);
        setLbl.setLetterSpacing(0.15f);
        setLbl.setTextColor(MUTED);
        col.addView(setLbl);

        TextView dayTv = new TextView(this);
        dayTv.setText("Gün " + (dayIndex + 1));
        dayTv.setTextSize(20);
        dayTv.setTypeface(Typeface.DEFAULT_BOLD);
        dayTv.setTextColor(TEXT);
        col.addView(dayTv);
        bar.addView(col);

        if (isDone()) {
            TextView doneMark = new TextView(this);
            doneMark.setText("✓");
            doneMark.setTextSize(15);
            doneMark.setTypeface(Typeface.DEFAULT_BOLD);
            doneMark.setTextColor(GREEN);
            GradientDrawable dGd = oval(); dGd.setColor(0x1430A858);
            doneMark.setBackgroundDrawable(dGd);
            doneMark.setPadding(dp(10), dp(5), dp(10), dp(5));
            bar.addView(doneMark);
        }

        LinearLayout w = new LinearLayout(this);
        w.setOrientation(LinearLayout.VERTICAL);
        w.setBackgroundColor(CARD);
        w.addView(bar, fullW());
        View div = new View(this); div.setBackgroundColor(BORDER);
        w.addView(div, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        return w;
    }

    // ── BREATH CARD ───────────────────────────────────────────────────────────

    View buildBreathCard() {
        LinearLayout card = makeCard();

        title(card, "4 · 2 · 6 Nefes Tekniği");
        sub(card, "Sesin gücü diyaframdan gelir. Uzun nefes verme diyaframı aktive eder. 5 set yapılır.", dp(16));

        card.addView(stepRow("4s", "Burundan nefes al — yalnızca karın şişsin, göğüs hareketsiz."));
        card.addView(stepRow("2s", "Nefesi tut — karın şişkin sabit, omuzlar düşük."));
        card.addView(stepRow("6s", "Ağızdan yavaşça ver — karın içeri çekilsin, ses buradan beslenir."));

        // Timer button
        breathBtn = new TextView(this);
        breathBtn.setText("▶  Zamanlayıcıyı Başlat");
        breathBtn.setTextSize(14);
        breathBtn.setTypeface(Typeface.DEFAULT_BOLD);
        breathBtn.setTextColor(0xFF0C0906);
        breathBtn.setGravity(Gravity.CENTER);
        breathBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_gold));
        breathBtn.setOnClickListener(v -> toggleBreath());
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(50));
        btnLp.setMargins(0, dp(18), 0, dp(10));
        card.addView(breathBtn, btnLp);

        // Breath display (hidden until started)
        LinearLayout disp = new LinearLayout(this);
        disp.setOrientation(LinearLayout.VERTICAL);
        disp.setGravity(Gravity.CENTER);
        disp.setVisibility(View.GONE);
        disp.setPadding(0, dp(8), 0, dp(8));
        breathDisplay = disp;

        // Ring-style countdown container
        FrameLayout ring = new FrameLayout(this);
        GradientDrawable ringGd = oval();
        ringGd.setColor(0x10000000);
        ringGd.setStroke(dp(2), 0x30D97D2E);
        int rsz = dp(136);
        ring.setBackgroundDrawable(ringGd);

        LinearLayout ringInner = new LinearLayout(this);
        ringInner.setOrientation(LinearLayout.VERTICAL);
        ringInner.setGravity(Gravity.CENTER);

        breathPhaseTv = new TextView(this);
        breathPhaseTv.setText("NEFES AL");
        breathPhaseTv.setTextSize(9);
        breathPhaseTv.setLetterSpacing(0.22f);
        breathPhaseTv.setTypeface(Typeface.DEFAULT_BOLD);
        breathPhaseTv.setTextColor(MUTED);
        breathPhaseTv.setGravity(Gravity.CENTER);

        breathCountTv = new TextView(this);
        breathCountTv.setText("4");
        breathCountTv.setTextSize(66);
        breathCountTv.setTypeface(Typeface.DEFAULT_BOLD);
        breathCountTv.setTextColor(BLUE);
        breathCountTv.setGravity(Gravity.CENTER);
        breathCountTv.setIncludeFontPadding(false);

        breathSetTv = new TextView(this);
        breathSetTv.setText("Set 1 / 5");
        breathSetTv.setTextSize(11);
        breathSetTv.setTextColor(MUTED);
        breathSetTv.setGravity(Gravity.CENTER);

        ringInner.addView(breathPhaseTv, fullW());
        ringInner.addView(breathCountTv, fullW());
        ringInner.addView(breathSetTv, fullW());
        ring.addView(ringInner, new FrameLayout.LayoutParams(rsz, rsz, Gravity.CENTER));

        LinearLayout.LayoutParams ringLp = new LinearLayout.LayoutParams(rsz, rsz);
        ringLp.gravity = Gravity.CENTER_HORIZONTAL;
        disp.addView(ring, ringLp);
        card.addView(disp, fullW());

        noteRow(card, "5 seti bitirince: cümle ortasında nefes tükenmez, ses titreşmez.");
        return card;
    }

    void toggleBreath() {
        if (bRunning) stopBreath(); else startBreath();
    }

    void startBreath() {
        bRunning = true; bPhase = 0; bCount = BREATH_DUR[0]; bSet = 1;
        breathDisplay.setVisibility(View.VISIBLE);
        breathBtn.setText("■  Durdur");
        breathBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_outline_gold));
        breathBtn.setTextColor(GOLD);
        updateBreathDisplay();
        scheduleTick();
    }

    void stopBreath() {
        bRunning = false;
        if (bTimer != null) { bTimer.cancel(); bTimer = null; }
        breathBtn.setText("▶  Zamanlayıcıyı Başlat");
        breathBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_gold));
        breathBtn.setTextColor(0xFF0C0906);
    }

    void scheduleTick() {
        if (!bRunning) return;
        bTimer = new CountDownTimer(1000, 1000) {
            @Override public void onTick(long ms) {}
            @Override public void onFinish() {
                if (!bRunning) return;
                bCount--;
                if (bCount <= 0) {
                    bPhase++;
                    if (bPhase >= BREATH_PHASES.length) {
                        bPhase = 0; bSet++;
                        if (bSet > 5) {
                            bRunning = false;
                            breathBtn.setText("✓  5 Set Tamamlandı");
                            breathBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_green));
                            breathBtn.setTextColor(TEXT);
                        return;
                        }
                    }
                    bCount = BREATH_DUR[bPhase];
                }
                updateBreathDisplay();
                scheduleTick();
            }
        }.start();
    }

    void updateBreathDisplay() {
        breathPhaseTv.setText(BREATH_PHASES[bPhase]);
        breathCountTv.setText(String.valueOf(bCount));
        breathCountTv.setTextColor(BREATH_COLORS[bPhase]);
        breathSetTv.setText("Set " + bSet + " / 5");
    }

    // ── REZONANS CARD ─────────────────────────────────────────────────────────

    View buildRezonansCard() {
        LinearLayout card = makeCard();

        TextView hum = new TextView(this);
        hum.setText("M · N · NG");
        hum.setTextSize(30);
        hum.setTypeface(Typeface.DEFAULT_BOLD);
        hum.setTextColor(GOLD);
        hum.setLetterSpacing(0.06f);
        LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hLp.setMargins(0, 0, 0, dp(8));
        card.addView(hum, hLp);

        sub(card, "Sesi göğse çeker, kalınlaştırır. Sıra: M → N → NG → Mmm-maa/mee/mii/moo/muu → Ha×5. Toplam 3 set.", dp(16));

        card.addView(turBox(new String[][]{
            {"M",  "Mmm — 5 saniye → geçiş ↓"},
            {"N",  "Nnnnn — 5 saniye → geçiş ↓"},
            {"NG", "Ngngng — 5 saniye → geçiş ↓"},
            {"+",  "Mmm-maa · mee · mii · moo · muu → geçiş ↓"},
            {"✓",  "Ha-ha-ha × 5 → 1 set bitti · 3-5 sn nefes · tekrar"}
        }));

        posSection(card, "M", "Dudaklar kapalı, dişler ayrık. İşaret parmağını dudağına daya: titreşim hissedilmeli.");
        posSection(card, "N", "Dil ucunu üst dişlerin hemen arkasına daya. Burnun üstüne koy: titreşim hissedilmeli.");
        posSection(card, "NG", "Dil kökü yumuşak damağa değiyor. Kafanın arkasına koy: titreşim oradan gelmeli.");
        return card;
    }

    // ── ISINMA CARD ───────────────────────────────────────────────────────────

    View buildIsinmaCard() {
        LinearLayout card = makeCard();

        TextView vowels = new TextView(this);
        vowels.setText("A · E · I · İ · O · Ö · U · Ü");
        vowels.setTextSize(19);
        vowels.setTypeface(Typeface.DEFAULT_BOLD);
        vowels.setTextColor(GREEN);
        vowels.setLetterSpacing(0.03f);
        LinearLayout.LayoutParams vLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        vLp.setMargins(0, 0, 0, dp(16));
        card.addView(vowels, vLp);

        card.addView(turBox(new String[][]{
            {"1", "Yavaş ve abartarak — her ünlüde çene maksimum açılsın → 1 nefes ↓"},
            {"2", "Normal hızda — akıcı ve temiz, biri diğerine karışmasın → 1 nefes ↓"},
            {"3", "Hızlı — maksimum hız ama netlik düşmesin, buruna kaçma."}
        }));

        noteRow(card, "Sesler arası direkt geçiş — bekleme yok. Burun sesi fark edersen ağzı daha da aç.");
        return card;
    }

    // ── TEKERLEMELER ──────────────────────────────────────────────────────────

    void buildTekerlemeler(LinearLayout parent) {
        try {
            String sesGrubu = safeStr(dayData, "sesGrubu");

            // Custom section label with ses grubu chip
            LinearLayout sgRow = new LinearLayout(this);
            sgRow.setOrientation(LinearLayout.HORIZONTAL);
            sgRow.setGravity(Gravity.CENTER_VERTICAL);
            sgRow.setPadding(dp(16), dp(28), dp(16), dp(10));

            View dot = new View(this);
            GradientDrawable dotGd = new GradientDrawable();
            dotGd.setShape(GradientDrawable.RECTANGLE);
            dotGd.setColor(GOLD);
            dotGd.setCornerRadius(dp(2));
            dot.setBackgroundDrawable(dotGd);
            LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(dp(4), dp(14));
            dLp.setMargins(0, 0, dp(10), 0);
            sgRow.addView(dot, dLp);

            TextView sgLabel = new TextView(this);
            sgLabel.setText("TEKERLEME");
            sgLabel.setTextSize(9);
            sgLabel.setLetterSpacing(0.25f);
            sgLabel.setTypeface(Typeface.DEFAULT_BOLD);
            sgLabel.setTextColor(SUB);
            sgRow.addView(sgLabel);

            View line = new View(this); line.setBackgroundColor(BORDER);
            LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(0, 1, 1f);
            lLp.setMargins(dp(12), 0, dp(10), 0);
            sgRow.addView(line, lLp);

            TextView sgChip = new TextView(this);
            sgChip.setText(sesGrubu);
            sgChip.setTextSize(10);
            sgChip.setTypeface(Typeface.DEFAULT_BOLD);
            sgChip.setTextColor(GOLD);
            sgChip.setPadding(dp(10), dp(4), dp(10), dp(4));
            GradientDrawable scGd = new GradientDrawable();
            scGd.setShape(GradientDrawable.RECTANGLE);
            scGd.setColor(0x14D97D2E);
            scGd.setStroke(1, 0x36D97D2E);
            scGd.setCornerRadius(dp(20));
            sgChip.setBackgroundDrawable(scGd);
            sgRow.addView(sgChip);

            parent.addView(sgRow, fullW());

            JSONArray teks = dayData.getJSONArray("teks");
            turCounts = new int[teks.length()][3];
            for (int i = 0; i < teks.length(); i++) {
                JSONObject tek = teks.getJSONObject(i);
                parent.addView(buildTekCard(i, tek.getString("metin"), tek.getString("not")));
            }
        } catch (Exception ignored) {}
    }

    View buildTekCard(int idx, String metin, String not) {
        FrameLayout outer = new FrameLayout(this);
        outer.setBackgroundDrawable(getDrawable(R.drawable.card_tek));
        LinearLayout.LayoutParams outerLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        outerLp.setMargins(dp(14), 0, dp(14), dp(12));
        outer.setLayoutParams(outerLp);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(20), dp(20), dp(18), dp(18));

        TextView num = new TextView(this);
        num.setText("TEKERLEME " + (idx + 1));
        num.setTextSize(9);
        num.setLetterSpacing(0.22f);
        num.setTypeface(Typeface.DEFAULT_BOLD);
        num.setTextColor(MUTED);
        LinearLayout.LayoutParams nLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nLp.setMargins(0, 0, 0, dp(14));
        col.addView(num, nLp);

        TextView txt = new TextView(this);
        txt.setText(metin);
        txt.setTextSize(20);
        txt.setTextColor(TEXT);
        txt.setLineSpacing(dp(6), 1.25f);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tLp.setMargins(0, 0, 0, dp(14));
        col.addView(txt, tLp);

        TextView noteTv = new TextView(this);
        noteTv.setText(not);
        noteTv.setTextSize(12);
        noteTv.setTextColor(SUB);
        noteTv.setLineSpacing(dp(3), 1.4f);
        noteTv.setBackgroundDrawable(getDrawable(R.drawable.note_border_top));
        noteTv.setPadding(0, dp(10), 0, dp(14));
        col.addView(noteTv, fullW());

        col.addView(buildTurBox(idx));
        outer.addView(col);
        return outer;
    }

    View buildTurBox(int tekIdx) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundDrawable(getDrawable(R.drawable.card_inner));
        box.setPadding(dp(14), dp(14), dp(14), dp(14));

        TextView title = new TextView(this);
        title.setText("15 TEKRAR — 3 TUR  ·  Dokunarak işaretle");
        title.setTextSize(9);
        title.setLetterSpacing(0.15f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(MUTED);
        LinearLayout.LayoutParams tlLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlLp.setMargins(0, 0, 0, dp(14));
        box.addView(title, tlLp);

        String[] names = {"5× Yavaş", "5× Normal", "5× Hızlı"};
        String[] descs = {"Her heceyi abartarak", "Doğal konuşma hızı", "Maks hız — ses yutulmasın"};
        for (int j = 0; j < 3; j++) {
            box.addView(buildTurRow(tekIdx, j, names[j], descs[j]));
        }
        return box;
    }

    View buildTurRow(int tekIdx, int turIdx, String name, String desc) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.setMargins(0, 0, 0, turIdx < 2 ? dp(14) : 0);
        row.setLayoutParams(rowLp);

        // Number circle
        TextView nc = new TextView(this);
        nc.setText(String.valueOf(turIdx + 1));
        nc.setTextSize(10);
        nc.setTypeface(Typeface.DEFAULT_BOLD);
        nc.setTextColor(GOLD);
        nc.setGravity(Gravity.CENTER);
        nc.setBackgroundDrawable(getDrawable(R.drawable.circle_gold_outline));
        LinearLayout.LayoutParams ncLp = new LinearLayout.LayoutParams(dp(22), dp(22));
        ncLp.setMargins(0, 0, dp(12), 0);
        row.addView(nc, ncLp);

        // Labels
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        cLp.setMargins(0, 0, dp(12), 0);
        col.setLayoutParams(cLp);

        TextView nameTv = new TextView(this);
        nameTv.setText(name);
        nameTv.setTextSize(13);
        nameTv.setTypeface(Typeface.DEFAULT_BOLD);
        nameTv.setTextColor(TEXT);
        col.addView(nameTv);

        TextView descTv = new TextView(this);
        descTv.setText(desc);
        descTv.setTextSize(11);
        descTv.setTextColor(MUTED);
        col.addView(descTv);
        row.addView(col);

        // 5 tap dots
        LinearLayout dots = new LinearLayout(this);
        dots.setOrientation(LinearLayout.HORIZONTAL);
        dots.setGravity(Gravity.CENTER_VERTICAL);
        View[] dotViews = new View[5];
        for (int d = 0; d < 5; d++) {
            View dot = new View(this);
            int dsz = dp(28);
            LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(dsz, dsz);
            dLp.setMargins(0, 0, d < 4 ? dp(6) : 0, 0);
            dot.setLayoutParams(dLp);
            dot.setBackgroundDrawable(getDrawable(R.drawable.dot_idle));
            dotViews[d] = dot;
            final int di = d;
            dot.setOnClickListener(v -> {
                turCounts[tekIdx][turIdx] = (di < turCounts[tekIdx][turIdx]) ? di : di + 1;
                for (int dd = 0; dd < 5; dd++) {
                    dotViews[dd].setBackgroundDrawable(getDrawable(
                        dd < turCounts[tekIdx][turIdx] ? R.drawable.dot_active : R.drawable.dot_idle));
                }
            });
            dots.addView(dot);
        }
        row.addView(dots);
        return row;
    }

    // ── OKUMA CARD ────────────────────────────────────────────────────────────

    View buildOkumaCard() {
        LinearLayout card = makeCard();
        try {
            JSONObject okuma = dayData.getJSONObject("okuma");
            String baslik   = okuma.getString("baslik");
            String kategori = okuma.getString("kategori");
            String body     = okuma.getString("body");
            String tip      = okuma.optString("tip", "");
            JSONArray vurgu = okuma.optJSONArray("vurgu");

            TextView kat = new TextView(this);
            kat.setText(kategori.toUpperCase());
            kat.setTextSize(9);
            kat.setLetterSpacing(0.22f);
            kat.setTypeface(Typeface.DEFAULT_BOLD);
            kat.setTextColor(GOLD);
            LinearLayout.LayoutParams kLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            kLp.setMargins(0, 0, 0, dp(8));
            card.addView(kat, kLp);

            TextView bas = new TextView(this);
            bas.setText(baslik);
            bas.setTextSize(24);
            bas.setTypeface(Typeface.DEFAULT_BOLD);
            bas.setTextColor(TEXT);
            bas.setLineSpacing(dp(4), 1.2f);
            LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            bLp.setMargins(0, 0, 0, dp(22));
            card.addView(bas, bLp);

            for (String para : body.split("\n+")) {
                if (para.trim().isEmpty()) continue;
                TextView pt = new TextView(this);
                pt.setText(para.trim());
                pt.setTextSize(15);
                pt.setTextColor(0xFFBBA898);
                pt.setLineSpacing(dp(5), 1.55f);
                LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                pLp.setMargins(0, 0, 0, dp(14));
                card.addView(pt, pLp);
            }

            if (vurgu != null && vurgu.length() > 0) {
                LinearLayout vBox = new LinearLayout(this);
                vBox.setOrientation(LinearLayout.VERTICAL);
                vBox.setBackgroundDrawable(getDrawable(R.drawable.card_inner));
                vBox.setPadding(dp(14), dp(14), dp(14), dp(14));
                LinearLayout.LayoutParams vLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                vLp.setMargins(0, 0, 0, dp(14));
                vBox.setLayoutParams(vLp);

                TextView vLbl = new TextView(this);
                vLbl.setText("VURGU PRATİĞİ");
                vLbl.setTextSize(9);
                vLbl.setLetterSpacing(0.2f);
                vLbl.setTypeface(Typeface.DEFAULT_BOLD);
                vLbl.setTextColor(MUTED);
                LinearLayout.LayoutParams vlLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                vlLp.setMargins(0, 0, 0, dp(10));
                vBox.addView(vLbl, vlLp);

                for (int i = 0; i < vurgu.length(); i++) {
                    JSONObject v = vurgu.getJSONObject(i);
                    LinearLayout vRow = new LinearLayout(this);
                    vRow.setOrientation(LinearLayout.HORIZONTAL);
                    vRow.setGravity(Gravity.TOP);
                    LinearLayout.LayoutParams vrLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    vrLp.setMargins(0, 0, 0, i < vurgu.length() - 1 ? dp(8) : 0);
                    vRow.setLayoutParams(vrLp);

                    TextView ns = new TextView(this);
                    ns.setText(v.getString("s") + ".");
                    ns.setTextSize(12);
                    ns.setTypeface(Typeface.DEFAULT_BOLD);
                    ns.setTextColor(GOLD);
                    LinearLayout.LayoutParams nsLp = new LinearLayout.LayoutParams(dp(20), ViewGroup.LayoutParams.WRAP_CONTENT);
                    nsLp.setMargins(0, 0, dp(8), 0);
                    vRow.addView(ns, nsLp);

                    TextView vt = new TextView(this);
                    vt.setText(v.getString("t"));
                    vt.setTextSize(13);
                    vt.setTextColor(SUB);
                    vt.setLineSpacing(dp(2), 1.4f);
                    vRow.addView(vt, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    vBox.addView(vRow);
                }
                card.addView(vBox);
            }

            if (!tip.isEmpty()) {
                TextView tipTv = new TextView(this);
                tipTv.setText(tip);
                tipTv.setTextSize(12);
                tipTv.setTextColor(MUTED);
                tipTv.setLineSpacing(dp(2), 1.4f);
                tipTv.setBackgroundDrawable(getDrawable(R.drawable.note_border_top));
                tipTv.setPadding(0, dp(10), 0, 0);
                card.addView(tipTv, fullW());
            }
        } catch (Exception ignored) {}
        return card;
    }

    // ── DONE BAR ──────────────────────────────────────────────────────────────

    View buildDoneBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.VERTICAL);
        bar.setPadding(dp(18), dp(12), dp(18), dp(28));
        bar.setBackgroundDrawable(getDrawable(R.drawable.bottom_fade));

        boolean done = isDone();
        TextView btn = new TextView(this);
        btn.setText(done ? "✓  Tamamlandı — Kapat" : "✓  Bugünü Tamamladım");
        btn.setTextSize(16);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setGravity(Gravity.CENTER);
        btn.setTextColor(done ? GREEN : 0xFF0C0906);
        btn.setBackgroundDrawable(getDrawable(done ? R.drawable.btn_green_outline : R.drawable.btn_gold));
        btn.setOnClickListener(v -> { if (!isDone()) markDone(); finish(); });
        bar.addView(btn, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(58)));
        return bar;
    }

    // ── MARK DONE ─────────────────────────────────────────────────────────────

    void markDone() {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
        Set<Integer> set = new HashSet<>();
        String cs = prefs.getString(MainActivity.KEY_COMPLETED, "");
        if (!cs.isEmpty()) {
            for (String s : cs.split(",")) {
                try { set.add(Integer.parseInt(s.trim())); } catch (Exception ignored) {}
            }
        }
        set.add(dayIndex);
        List<Integer> sorted = new ArrayList<>(set);
        Collections.sort(sorted);
        StringBuilder sb = new StringBuilder();
        for (int d : sorted) { if (sb.length() > 0) sb.append(","); sb.append(d); }
        int streak = 0;
        for (int i = sorted.size() - 1; i >= 0; i--) {
            if (i == sorted.size() - 1 || sorted.get(i) == sorted.get(i + 1) - 1) streak++;
            else break;
        }
        prefs.edit()
            .putString(MainActivity.KEY_COMPLETED, sb.toString())
            .putInt(MainActivity.KEY_STREAK, streak)
            .apply();
    }

    boolean isDone() {
        String s = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
            .getString(MainActivity.KEY_COMPLETED, "");
        if (s.isEmpty()) return false;
        for (String tok : s.split(",")) {
            try { if (Integer.parseInt(tok.trim()) == dayIndex) return true; } catch (Exception ignored) {}
        }
        return false;
    }

    // ── SHARED BUILDERS ───────────────────────────────────────────────────────

    // Standard card: card_bg, consistent margins and padding
    LinearLayout makeCard() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackgroundDrawable(getDrawable(R.drawable.card_bg));
        c.setPadding(dp(20), dp(22), dp(20), dp(22));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(14), 0, dp(14), dp(12));
        c.setLayoutParams(lp);
        return c;
    }

    View secLabel(String text, int accentColor) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(28), dp(16), dp(10));

        // Colored accent bar
        View accent = new View(this);
        GradientDrawable agd = new GradientDrawable();
        agd.setShape(GradientDrawable.RECTANGLE);
        agd.setColor(accentColor);
        agd.setCornerRadius(dp(2));
        accent.setBackgroundDrawable(agd);
        LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(dp(4), dp(14));
        aLp.setMargins(0, 0, dp(10), 0);
        row.addView(accent, aLp);

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(9);
        tv.setLetterSpacing(0.25f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(SUB);
        row.addView(tv);

        View line = new View(this); line.setBackgroundColor(BORDER);
        LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(0, 1, 1f);
        lLp.setMargins(dp(10), 0, 0, 0);
        row.addView(line, lLp);
        return row;
    }

    void title(LinearLayout card, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(17);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(TEXT);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(6));
        card.addView(tv, lp);
    }

    void sub(LinearLayout card, String text, int bottomMargin) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(13);
        tv.setTextColor(SUB);
        tv.setLineSpacing(dp(3), 1.4f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, bottomMargin);
        card.addView(tv, lp);
    }

    void noteRow(LinearLayout card, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(12);
        tv.setTextColor(MUTED);
        tv.setLineSpacing(dp(2), 1.4f);
        tv.setBackgroundDrawable(getDrawable(R.drawable.note_border_top));
        tv.setPadding(0, dp(10), 0, 0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(12), 0, 0);
        card.addView(tv, lp);
    }

    void posSection(LinearLayout card, String name, String body) {
        TextView lbl = new TextView(this);
        lbl.setText(name + " — Ağız Pozisyonu");
        lbl.setTextSize(10);
        lbl.setLetterSpacing(0.1f);
        lbl.setTypeface(Typeface.DEFAULT_BOLD);
        lbl.setTextColor(MUTED);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(14), 0, dp(6));
        card.addView(lbl, lp);

        TextView tv = new TextView(this);
        tv.setText(body);
        tv.setTextSize(13);
        tv.setTextColor(SUB);
        tv.setLineSpacing(dp(2), 1.4f);
        card.addView(tv, fullW());
    }

    View stepRow(String num, String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.setMargins(0, 0, 0, dp(10));
        row.setLayoutParams(rowLp);

        // Badge (solid filled circle)
        TextView nc = new TextView(this);
        nc.setText(num);
        nc.setTextSize(9);
        nc.setTypeface(Typeface.DEFAULT_BOLD);
        nc.setTextColor(0xFF0C0906);
        nc.setGravity(Gravity.CENTER);
        nc.setIncludeFontPadding(false);
        GradientDrawable ngd = oval(); ngd.setColor(BLUE);
        nc.setBackgroundDrawable(ngd);
        LinearLayout.LayoutParams ncLp = new LinearLayout.LayoutParams(dp(26), dp(26));
        ncLp.setMargins(0, dp(1), dp(12), 0);
        row.addView(nc, ncLp);

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(13);
        tv.setTextColor(SUB);
        tv.setLineSpacing(dp(2), 1.4f);
        row.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return row;
    }

    View turBox(String[][] rows) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundDrawable(getDrawable(R.drawable.card_inner));
        box.setPadding(dp(14), dp(14), dp(14), dp(14));
        LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        boxLp.setMargins(0, 0, 0, dp(4));
        box.setLayoutParams(boxLp);

        for (int i = 0; i < rows.length; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rLp.setMargins(0, 0, 0, i < rows.length - 1 ? dp(10) : 0);
            row.setLayoutParams(rLp);

            TextView sym = new TextView(this);
            sym.setText(rows[i][0]);
            sym.setTextSize(10);
            sym.setTypeface(Typeface.DEFAULT_BOLD);
            sym.setTextColor(GOLD);
            sym.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(dp(34), dp(22));
            sLp.setMargins(0, 0, dp(10), 0);
            row.addView(sym, sLp);

            TextView desc = new TextView(this);
            desc.setText(rows[i][1]);
            desc.setTextSize(13);
            desc.setTextColor(SUB);
            desc.setLineSpacing(dp(2), 1.3f);
            row.addView(desc, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            box.addView(row);
        }
        return box;
    }

    // ── LAYOUT HELPERS ────────────────────────────────────────────────────────

    LinearLayout.LayoutParams fullW() {
        return new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    GradientDrawable oval() {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        return gd;
    }

    String safeStr(JSONObject obj, String key) {
        try { return obj.getString(key); } catch (Exception e) { return ""; }
    }

    int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        if (bTimer != null) bTimer.cancel();
        super.onDestroy();
    }
}
