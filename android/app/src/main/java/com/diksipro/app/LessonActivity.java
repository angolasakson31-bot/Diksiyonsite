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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LessonActivity extends Activity {

    int dayIndex;
    JSONObject dayData;

    // Breath timer
    static final String[] BREATH_PHASES  = {"NEFES AL", "TUT", "NEFES VER"};
    static final int[]    BREATH_DUR     = {4, 2, 6};
    static final int[]    BREATH_COLORS  = {0xFF4E90FF, 0xFFFFB94A, 0xFF2ECC71};
    int bPhase = 0, bCount = 4, bSet = 1;
    boolean bRunning = false;
    CountDownTimer bTimer;
    TextView breathPhaseTv, breathCountTv, breathSetTv, breathBtn;
    View breathDisplay;

    // Tur dots per tekerleme: [tekIdx][turIdx] = completed count 0..5
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

    // ── Colors (site palette) ─────────────────────────────────────────────────
    static final int BG      = 0xFF080b10;
    static final int CARD    = 0xFF0f1520;
    static final int CARD2   = 0xFF16202e;
    static final int BORDER  = 0xFF1e2d40;
    static final int GOLD    = 0xFFc8a84b;
    static final int TEXT    = 0xFFdce6f0;
    static final int SUB     = 0xFF8fa3b8;
    static final int MUTED   = 0xFF4a5d72;
    static final int GREEN   = 0xFF2e9e5e;
    static final int CORAL   = 0xFFFF6B35;

    void loadData() {
        try {
            InputStream is = getAssets().open("program.json");
            byte[] buf = new byte[is.available()];
            is.read(buf);
            is.close();
            JSONArray arr = new JSONArray(new String(buf, StandardCharsets.UTF_8));
            dayData = arr.getJSONObject(dayIndex);
        } catch (Exception e) {
            dayData = new JSONObject();
        }
    }

    void buildUI() {
        // Root container: ScrollView fills screen, bottom bar floats over it
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG);

        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(BG);
        sv.setFillViewport(true);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, 0, 0, dp(110));

        // Top bar
        content.addView(buildTopBar());

        // Sections
        content.addView(blabel("DİYAFRAM NEFESİ"));
        content.addView(buildBreathCard());

        content.addView(blabel("REZONANS"));
        content.addView(buildRezonansCard());

        content.addView(blabel("ARTİKÜLASYON ISINMASI"));
        content.addView(buildIsinmaCard());

        // Ses grubu divider + tekerlemeler
        buildTekerlemeler(content);

        content.addView(blabel("SESLİ OKUMA METNİ"));
        content.addView(buildOkumaCard());

        sv.addView(content, new ScrollView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        root.addView(sv, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Bottom done bar (floats over scroll)
        root.addView(buildDoneBar(), doneBarLp());

        setContentView(root);
    }

    // ── TOP BAR ───────────────────────────────────────────────────────────────

    View buildTopBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(CARD);
        bar.setPadding(dp(16), dp(14), dp(16), dp(14));

        // Border bottom
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setBackgroundColor(CARD);

        TextView back = new TextView(this);
        back.setText("←");
        back.setTextSize(22);
        back.setTextColor(SUB);
        back.setPadding(0, 0, dp(14), 0);
        back.setOnClickListener(v -> finish());
        bar.addView(back);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        String set = safeStr(dayData, "set");
        int setWeek = (dayIndex / 7) % 3 + 1;
        TextView setLbl = new TextView(this);
        setLbl.setText("Set " + (set.isEmpty() ? "" : set.charAt(0)) + " · " + setWeek + ". Hafta");
        setLbl.setTextSize(10);
        setLbl.setLetterSpacing(0.15f);
        setLbl.setTextColor(MUTED);
        col.addView(setLbl, wrapW());

        TextView dayTv = new TextView(this);
        dayTv.setText("Gün " + (dayIndex + 1));
        dayTv.setTextSize(18);
        dayTv.setTypeface(Typeface.DEFAULT_BOLD);
        dayTv.setTextColor(TEXT);
        col.addView(dayTv, wrapW());

        bar.addView(col);

        wrapper.addView(bar, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        View div = new View(this);
        div.setBackgroundColor(BORDER);
        wrapper.addView(div, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        return wrapper;
    }

    // ── BREATH CARD ───────────────────────────────────────────────────────────

    View buildBreathCard() {
        LinearLayout card = card();

        TextView title = new TextView(this);
        title.setText("4 · 2 · 6 Nefes Tekniği");
        title.setTextSize(15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(TEXT);
        title.setPadding(0, 0, 0, dp(6));
        card.addView(title, fullW());

        TextView sub = new TextView(this);
        sub.setText("Sesin gücü akciğerden değil, diyaframdan gelir. Uzun nefes verme (6 sn) diyaframı aktive eder. 5 set yapılır.");
        sub.setTextSize(12);
        sub.setTextColor(SUB);
        sub.setLineSpacing(dp(3), 1f);
        sub.setPadding(0, 0, 0, dp(14));
        card.addView(sub, fullW());

        card.addView(stepRow("1", "Bir elini göbeğinin üstüne koy. 4 sayarak burundan nefes al — yalnızca karın şişsin, göğüs hareketsiz."));
        card.addView(stepRow("2", "2 sayarak nefesini tut. Karın şişkin sabit, omuzlar düşük."));
        card.addView(stepRow("3", "6 sayarak ağızdan yavaşça ver. Karın içeri çekilsin — ses gücünü besleyen kas bu."));

        // Timer button
        breathBtn = new TextView(this);
        breathBtn.setText("▶  Zamanlayıcıyı Başlat");
        breathBtn.setTextSize(13);
        breathBtn.setTypeface(Typeface.DEFAULT_BOLD);
        breathBtn.setTextColor(BG);
        breathBtn.setGravity(Gravity.CENTER);
        breathBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_gold));
        breathBtn.setOnClickListener(v -> toggleBreath());
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(46));
        btnLp.setMargins(0, dp(12), 0, dp(8));
        card.addView(breathBtn, btnLp);

        // Display (hidden)
        LinearLayout disp = new LinearLayout(this);
        disp.setOrientation(LinearLayout.VERTICAL);
        disp.setGravity(Gravity.CENTER);
        disp.setVisibility(View.GONE);
        disp.setPadding(0, dp(10), 0, dp(6));
        breathDisplay = disp;

        breathPhaseTv = new TextView(this);
        breathPhaseTv.setText("NEFES AL");
        breathPhaseTv.setTextSize(10);
        breathPhaseTv.setLetterSpacing(0.2f);
        breathPhaseTv.setTextColor(MUTED);
        breathPhaseTv.setGravity(Gravity.CENTER);
        disp.addView(breathPhaseTv, fullW());

        breathCountTv = new TextView(this);
        breathCountTv.setText("4");
        breathCountTv.setTextSize(68);
        breathCountTv.setTypeface(Typeface.DEFAULT_BOLD);
        breathCountTv.setTextColor(0xFF4E90FF);
        breathCountTv.setGravity(Gravity.CENTER);
        disp.addView(breathCountTv, fullW());

        breathSetTv = new TextView(this);
        breathSetTv.setText("Set 1 / 5");
        breathSetTv.setTextSize(11);
        breathSetTv.setTextColor(MUTED);
        breathSetTv.setGravity(Gravity.CENTER);
        disp.addView(breathSetTv, fullW());

        card.addView(disp, fullW());

        TextView note = new TextView(this);
        note.setText("5 seti bitirince: cümle ortasında nefes tükenmez, ses titreşmez.");
        note.setTextSize(12);
        note.setTextColor(MUTED);
        note.setLineSpacing(dp(2), 1.4f);
        LinearLayout.LayoutParams noteLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noteLp.setMargins(0, dp(10), 0, 0);
        note.setBackgroundDrawable(getDrawable(R.drawable.note_border_top));
        note.setPadding(0, dp(10), 0, 0);
        card.addView(note, noteLp);

        return wrapCard(card);
    }

    void toggleBreath() {
        if (bRunning) stopBreath();
        else startBreath();
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
        breathBtn.setTextColor(BG);
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
                            breathBtn.setTextColor(GREEN);
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
        LinearLayout card = card();

        TextView hum = new TextView(this);
        hum.setText("M · N · NG");
        hum.setTextSize(28);
        hum.setTypeface(Typeface.DEFAULT_BOLD);
        hum.setTextColor(GOLD);
        hum.setLetterSpacing(0.12f);
        hum.setPadding(0, 0, 0, dp(6));
        card.addView(hum, fullW());

        TextView sub = new TextView(this);
        sub.setText("Sesi göğse çeker, kalınlaştırır. Sırası: M → N → NG → Mmm-maa/mee/mii/moo/muu → Ha×5. Toplamda 3 set.");
        sub.setTextSize(12);
        sub.setTextColor(SUB);
        sub.setLineSpacing(dp(3), 1f);
        sub.setPadding(0, 0, 0, dp(14));
        card.addView(sub, fullW());

        card.addView(turBox(new String[][]{
            {"M",  "Mmm — 5 saniye → geçiş ↓"},
            {"N",  "Nnnnn — 5 saniye → geçiş ↓"},
            {"NG", "Ngngng — 5 saniye → geçiş ↓"},
            {"+",  "Mmm-maa · mee · mii · moo · muu → geçiş ↓"},
            {"✓",  "Ha-ha-ha × 5 → 1 set bitti, 3-5 sn nefes, tekrar"}
        }));

        card.addView(secTitle("M Sesi — Ağız Pozisyonu"));
        card.addView(noteRow("Dudaklar kapalı, dişler ayrık. İşaret parmağını dudağına daya: titreşim hissedilmeli."));
        card.addView(secTitle("N Sesi — Ağız Pozisyonu"));
        card.addView(noteRow("Dil ucunu üst dişlerin hemen arkasına daya. Parmağını burnunun üstüne koy: titreşim hissedilmeli."));
        card.addView(secTitle("NG Sesi — Ağız Pozisyonu"));
        card.addView(noteRow("Dil kökü yumuşak damağa değiyor. Elini kafanın arkasına koy: titreşim oradan gelmeli."));

        return wrapCard(card);
    }

    // ── ISINMA CARD ───────────────────────────────────────────────────────────

    View buildIsinmaCard() {
        LinearLayout card = card();

        TextView vowels = new TextView(this);
        vowels.setText("A · E · I · İ · O · Ö · U · Ü");
        vowels.setTextSize(22);
        vowels.setTypeface(Typeface.DEFAULT_BOLD);
        vowels.setTextColor(GOLD);
        vowels.setLetterSpacing(0.06f);
        vowels.setPadding(0, 0, 0, dp(10));
        card.addView(vowels, fullW());

        card.addView(turBox(new String[][]{
            {"1", "Yavaş ve abartarak — her ünlüde çene maksimum açılsın → 1 nefes ↓"},
            {"2", "Normal hızda — akıcı ve temiz, biri diğerine karışmasın → 1 nefes ↓"},
            {"3", "Hızlı — maksimum hız ama netlik düşmesin, buruna kaçma. Bitti."}
        }));

        TextView note = new TextView(this);
        note.setText("Sesler arası direkt geçiş — bekleme yok. Burun sesi fark edersen ağzı daha da aç.");
        note.setTextSize(12);
        note.setTextColor(MUTED);
        note.setLineSpacing(dp(2), 1.4f);
        note.setBackgroundDrawable(getDrawable(R.drawable.note_border_top));
        note.setPadding(0, dp(10), 0, 0);
        LinearLayout.LayoutParams noteLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noteLp.setMargins(0, dp(10), 0, 0);
        card.addView(note, noteLp);

        return wrapCard(card);
    }

    // ── TEKERLEMELER ──────────────────────────────────────────────────────────

    void buildTekerlemeler(LinearLayout parent) {
        try {
            String sesGrubu = safeStr(dayData, "sesGrubu");
            // Ses grubu divider
            LinearLayout sgRow = new LinearLayout(this);
            sgRow.setOrientation(LinearLayout.HORIZONTAL);
            sgRow.setGravity(Gravity.CENTER_VERTICAL);
            sgRow.setPadding(dp(16), dp(20), dp(16), dp(8));

            TextView sgTv = new TextView(this);
            sgTv.setText(sesGrubu);
            sgTv.setTextSize(10);
            sgTv.setTypeface(Typeface.DEFAULT_BOLD);
            sgTv.setTextColor(GOLD);
            sgTv.setLetterSpacing(0.05f);
            sgRow.addView(sgTv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            View line = new View(this);
            line.setBackgroundColor(BORDER);
            LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(0, 1, 1f);
            lineLp.setMargins(dp(8), 0, 0, 0);
            sgRow.addView(line, lineLp);
            parent.addView(sgRow, fullW());

            JSONArray teks = dayData.getJSONArray("teks");
            turCounts = new int[teks.length()][3];

            for (int i = 0; i < teks.length(); i++) {
                JSONObject tek = teks.getJSONObject(i);
                parent.addView(buildTekCard(i, tek.getString("metin"), tek.getString("not")));
            }
        } catch (Exception ignored) {}
    }

    View buildTekCard(int tekIdx, String metin, String not) {
        // Card with gold left border
        FrameLayout outer = new FrameLayout(this);
        outer.setBackgroundDrawable(getDrawable(R.drawable.card_tek));
        LinearLayout.LayoutParams outerLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        outerLp.setMargins(dp(14), 0, dp(14), dp(10));
        outer.setLayoutParams(outerLp);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(16), dp(16), dp(16), dp(16));

        // Number
        TextView num = new TextView(this);
        num.setText("TEKERLEME " + (tekIdx + 1));
        num.setTextSize(9);
        num.setLetterSpacing(0.22f);
        num.setTextColor(MUTED);
        num.setPadding(0, 0, 0, dp(10));
        col.addView(num, fullW());

        // Main text
        TextView txt = new TextView(this);
        txt.setText(metin);
        txt.setTextSize(18);
        txt.setTextColor(TEXT);
        txt.setLineSpacing(dp(4), 1.3f);
        txt.setPadding(0, 0, 0, dp(12));
        col.addView(txt, fullW());

        // Note
        TextView noteTv = new TextView(this);
        noteTv.setText(not);
        noteTv.setTextSize(12);
        noteTv.setTextColor(SUB);
        noteTv.setLineSpacing(dp(3), 1.45f);
        noteTv.setBackgroundDrawable(getDrawable(R.drawable.note_border_top));
        noteTv.setPadding(0, dp(10), 0, dp(14));
        col.addView(noteTv, fullW());

        // Tur box
        col.addView(turBoxTek(tekIdx));

        outer.addView(col);
        return outer;
    }

    View turBoxTek(int tekIdx) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundDrawable(getDrawable(R.drawable.card_inner));
        box.setPadding(dp(12), dp(12), dp(12), dp(12));

        TextView title = new TextView(this);
        title.setText("15 TEKRAR — 3 TUR  ·  Dokunarak işaretle");
        title.setTextSize(9);
        title.setLetterSpacing(0.15f);
        title.setTextColor(MUTED);
        title.setPadding(0, 0, 0, dp(10));
        box.addView(title, fullW());

        String[] turNames = {"5× Yavaş", "5× Normal", "5× Hızlı"};
        String[] turDescs = {"Her heceyi abartarak", "Doğal konuşma hızı", "Maksimum hız — ses yutulmasın"};
        for (int j = 0; j < 3; j++) {
            box.addView(buildTurRow(tekIdx, j, turNames[j], turDescs[j]));
        }
        return box;
    }

    View buildTurRow(int tekIdx, int turIdx, String name, String desc) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.setMargins(0, 0, 0, turIdx < 2 ? dp(10) : 0);
        row.setLayoutParams(rowLp);

        // Number circle
        TextView nc = new TextView(this);
        nc.setText(String.valueOf(turIdx + 1));
        nc.setTextSize(9);
        nc.setTypeface(Typeface.DEFAULT_BOLD);
        nc.setTextColor(GOLD);
        nc.setGravity(Gravity.CENTER);
        nc.setBackgroundDrawable(getDrawable(R.drawable.circle_gold_outline));
        LinearLayout.LayoutParams ncLp = new LinearLayout.LayoutParams(dp(20), dp(20));
        ncLp.setMargins(0, 0, dp(10), 0);
        row.addView(nc, ncLp);

        // Text
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        cLp.setMargins(0, 0, dp(10), 0);
        col.setLayoutParams(cLp);

        TextView nameTv = new TextView(this);
        nameTv.setText(name);
        nameTv.setTextSize(12);
        nameTv.setTypeface(Typeface.DEFAULT_BOLD);
        nameTv.setTextColor(TEXT);
        col.addView(nameTv, wrapW());
        TextView descTv = new TextView(this);
        descTv.setText(desc);
        descTv.setTextSize(10);
        descTv.setTextColor(MUTED);
        col.addView(descTv, wrapW());
        row.addView(col);

        // 5 dots
        LinearLayout dotsRow = new LinearLayout(this);
        dotsRow.setOrientation(LinearLayout.HORIZONTAL);
        dotsRow.setGravity(Gravity.CENTER_VERTICAL);
        View[] dots = new View[5];
        for (int d = 0; d < 5; d++) {
            View dot = new View(this);
            LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(dp(26), dp(26));
            dLp.setMargins(0, 0, d < 4 ? dp(5) : 0, 0);
            dot.setLayoutParams(dLp);
            dot.setBackgroundDrawable(getDrawable(R.drawable.dot_idle));
            dots[d] = dot;
            final int di = d;
            dot.setOnClickListener(v -> {
                turCounts[tekIdx][turIdx] = (di < turCounts[tekIdx][turIdx]) ? di : di + 1;
                for (int dd = 0; dd < 5; dd++) {
                    dots[dd].setBackgroundDrawable(getDrawable(
                        dd < turCounts[tekIdx][turIdx] ? R.drawable.dot_active : R.drawable.dot_idle));
                }
            });
            dotsRow.addView(dot);
        }
        row.addView(dotsRow);
        return row;
    }

    // ── OKUMA CARD ────────────────────────────────────────────────────────────

    View buildOkumaCard() {
        LinearLayout card = card();
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
            kat.setLetterSpacing(0.2f);
            kat.setTextColor(GOLD);
            kat.setPadding(0, 0, 0, dp(6));
            card.addView(kat, fullW());

            TextView bas = new TextView(this);
            bas.setText(baslik);
            bas.setTextSize(22);
            bas.setTypeface(Typeface.DEFAULT_BOLD);
            bas.setTextColor(TEXT);
            bas.setPadding(0, 0, 0, dp(16));
            card.addView(bas, fullW());

            // Body paragraphs
            for (String para : body.split("\n+")) {
                if (para.trim().isEmpty()) continue;
                TextView pt = new TextView(this);
                pt.setText(para.trim());
                pt.setTextSize(15);
                pt.setTextColor(0xFFb0c2d4);
                pt.setLineSpacing(dp(4), 1.55f);
                LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                pLp.setMargins(0, 0, 0, dp(14));
                card.addView(pt, pLp);
            }

            // Vurgu
            if (vurgu != null && vurgu.length() > 0) {
                LinearLayout vBox = new LinearLayout(this);
                vBox.setOrientation(LinearLayout.VERTICAL);
                vBox.setBackgroundDrawable(getDrawable(R.drawable.card_inner));
                vBox.setPadding(dp(12), dp(12), dp(12), dp(12));
                LinearLayout.LayoutParams vBoxLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                vBoxLp.setMargins(0, 0, 0, dp(12));
                vBox.setLayoutParams(vBoxLp);

                TextView vLbl = new TextView(this);
                vLbl.setText("VURGU PRATİĞİ");
                vLbl.setTextSize(9);
                vLbl.setLetterSpacing(0.2f);
                vLbl.setTextColor(MUTED);
                vLbl.setPadding(0, 0, 0, dp(8));
                vBox.addView(vLbl, fullW());

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
                    LinearLayout.LayoutParams nsLp = new LinearLayout.LayoutParams(dp(18), ViewGroup.LayoutParams.WRAP_CONTENT);
                    nsLp.setMargins(0, 0, dp(8), 0);
                    vRow.addView(ns, nsLp);

                    TextView tv = new TextView(this);
                    tv.setText(v.getString("t"));
                    tv.setTextSize(12);
                    tv.setTextColor(SUB);
                    tv.setLineSpacing(dp(2), 1.4f);
                    vRow.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    vBox.addView(vRow);
                }
                card.addView(vBox);
            }

            // Tip
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
        return wrapCard(card);
    }

    // ── DONE BAR ──────────────────────────────────────────────────────────────

    View buildDoneBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.VERTICAL);
        bar.setPadding(dp(16), dp(10), dp(16), dp(24));
        bar.setBackgroundDrawable(getDrawable(R.drawable.bottom_fade));

        boolean done = isDone();
        TextView btn = new TextView(this);
        btn.setText(done ? "✓  Tamamlandı" : "✓  Bugünü Tamamladım");
        btn.setTextSize(15);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setGravity(Gravity.CENTER);
        btn.setTextColor(done ? GREEN : BG);
        btn.setBackgroundDrawable(getDrawable(done ? R.drawable.btn_green_outline : R.drawable.btn_gold));
        btn.setOnClickListener(v -> {
            if (!isDone()) markDone();
            finish();
        });
        bar.addView(btn, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)));
        return bar;
    }

    FrameLayout.LayoutParams doneBarLp() {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.BOTTOM;
        return lp;
    }

    // ── MARK DONE ─────────────────────────────────────────────────────────────

    void markDone() {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
        String completedStr = prefs.getString(MainActivity.KEY_COMPLETED, "");
        Set<Integer> set = new HashSet<>();
        if (!completedStr.isEmpty()) {
            for (String s : completedStr.split(",")) {
                try { set.add(Integer.parseInt(s)); } catch (Exception ignored) {}
            }
        }
        set.add(dayIndex);
        StringBuilder sb = new StringBuilder();
        for (int d : set) { if (sb.length() > 0) sb.append(","); sb.append(d); }
        List<Integer> sorted = new ArrayList<>(set);
        Collections.sort(sorted);
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
            try { if (Integer.parseInt(tok) == dayIndex) return true; } catch (Exception ignored) {}
        }
        return false;
    }

    // ── SHARED VIEW BUILDERS ──────────────────────────────────────────────────

    View blabel(String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(20), dp(16), dp(8));
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(9);
        tv.setLetterSpacing(0.25f);
        tv.setTextColor(MUTED);
        row.addView(tv, wrapW());
        View line = new View(this);
        line.setBackgroundColor(BORDER);
        LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(0, 1, 1f);
        lLp.setMargins(dp(8), 0, 0, 0);
        row.addView(line, lLp);
        return row;
    }

    LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        return card;
    }

    View wrapCard(LinearLayout inner) {
        FrameLayout frame = new FrameLayout(this);
        frame.setBackgroundDrawable(getDrawable(R.drawable.card_bg));
        frame.addView(inner, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(14), 0, dp(14), dp(10));
        frame.setLayoutParams(lp);
        return frame;
    }

    View stepRow(String num, String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.setMargins(0, 0, 0, dp(8));
        row.setLayoutParams(rowLp);
        TextView nc = new TextView(this);
        nc.setText(num);
        nc.setTextSize(9);
        nc.setTypeface(Typeface.DEFAULT_BOLD);
        nc.setTextColor(GOLD);
        nc.setGravity(Gravity.CENTER);
        nc.setBackgroundDrawable(getDrawable(R.drawable.circle_gold_outline));
        LinearLayout.LayoutParams ncLp = new LinearLayout.LayoutParams(dp(22), dp(22));
        ncLp.setMargins(0, dp(2), dp(10), 0);
        row.addView(nc, ncLp);
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(12);
        tv.setTextColor(SUB);
        tv.setLineSpacing(dp(2), 1.4f);
        row.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return row;
    }

    View turBox(String[][] rows) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundDrawable(getDrawable(R.drawable.card_inner));
        box.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        boxLp.setMargins(0, 0, 0, dp(4));
        box.setLayoutParams(boxLp);
        for (int i = 0; i < rows.length; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            if (i < rows.length - 1) {
                LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rLp.setMargins(0, 0, 0, dp(8));
                row.setLayoutParams(rLp);
            }
            TextView sym = new TextView(this);
            sym.setText(rows[i][0]);
            sym.setTextSize(10);
            sym.setTypeface(Typeface.DEFAULT_BOLD);
            sym.setTextColor(GOLD);
            sym.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(dp(32), dp(22));
            sLp.setMargins(0, 0, dp(8), 0);
            row.addView(sym, sLp);
            TextView desc = new TextView(this);
            desc.setText(rows[i][1]);
            desc.setTextSize(12);
            desc.setTextColor(SUB);
            desc.setLineSpacing(dp(2), 1.3f);
            row.addView(desc, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            box.addView(row);
        }
        return box;
    }

    View secTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(9);
        tv.setLetterSpacing(0.15f);
        tv.setTextColor(MUTED);
        tv.setPadding(0, dp(14), 0, dp(8));
        return tv;
    }

    View noteRow(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(12);
        tv.setTextColor(SUB);
        tv.setLineSpacing(dp(2), 1.4f);
        return tv;
    }

    // ── LAYOUT HELPERS ────────────────────────────────────────────────────────

    LinearLayout.LayoutParams fullW() {
        return new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
    LinearLayout.LayoutParams wrapW() {
        return new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
