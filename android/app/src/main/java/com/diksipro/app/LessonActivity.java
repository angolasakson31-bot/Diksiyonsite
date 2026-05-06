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

    static final int BG     = 0xFF07090F;
    static final int CARD   = 0xFF0D1220;
    static final int CARD2  = 0xFF131B2C;
    static final int BORDER = 0xFF1C2A3F;
    static final int GOLD   = 0xFFCFAB52;
    static final int TEXT   = 0xFFE2EAF4;
    static final int SUB    = 0xFF8FA3BC;
    static final int MUTED  = 0xFF4D617A;
    static final int GREEN  = 0xFF2EA868;
    static final int BLUE   = 0xFF4E8CF5;

    // Section accent colors
    static final int ACCENT_BREATH  = 0xFF4E8CF5;
    static final int ACCENT_REZON   = 0xFFCFAB52;
    static final int ACCENT_ISINMA  = 0xFF2EA868;
    static final int ACCENT_TEK     = 0xFFCFAB52;
    static final int ACCENT_OKUMA   = 0xFF8FA3BC;

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

        content.addView(sectionLabel("DİYAFRAM NEFESİ", ACCENT_BREATH));
        content.addView(buildBreathCard());

        content.addView(sectionLabel("REZONANS", ACCENT_REZON));
        content.addView(buildRezonansCard());

        content.addView(sectionLabel("ARTİKÜLASYON ISINMASI", ACCENT_ISINMA));
        content.addView(buildIsinmaCard());

        buildTekerlemeler(content);

        content.addView(sectionLabel("SESLİ OKUMA METNİ", ACCENT_OKUMA));
        content.addView(buildOkumaCard());

        sv.addView(content, new ScrollView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(sv, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(buildDoneBar(), doneBarLp());

        setContentView(root);
    }

    // ── TOP BAR ───────────────────────────────────────────────────────────────

    View buildTopBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(CARD);
        bar.setPadding(dp(16), dp(16), dp(20), dp(16));

        TextView back = new TextView(this);
        back.setText("←");
        back.setTextSize(24);
        back.setTextColor(SUB);
        back.setPadding(0, 0, dp(16), 0);
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

        // Done indicator
        if (isDone()) {
            TextView doneTv = new TextView(this);
            doneTv.setText("✓");
            doneTv.setTextSize(16);
            doneTv.setTypeface(Typeface.DEFAULT_BOLD);
            doneTv.setTextColor(GREEN);
            doneTv.setPadding(dp(10), dp(5), dp(10), dp(5));
            GradientDrawable dGd = new GradientDrawable();
            dGd.setShape(GradientDrawable.OVAL);
            dGd.setColor(0x182EA868);
            doneTv.setBackgroundDrawable(dGd);
            bar.addView(doneTv);
        }

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setBackgroundColor(CARD);
        wrapper.addView(bar, fullW());
        View div = new View(this);
        div.setBackgroundColor(BORDER);
        wrapper.addView(div, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        return wrapper;
    }

    // ── BREATH CARD ───────────────────────────────────────────────────────────

    View buildBreathCard() {
        LinearLayout card = card(ACCENT_BREATH);

        TextView title = new TextView(this);
        title.setText("4 · 2 · 6 Nefes Tekniği");
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(TEXT);
        title.setPadding(0, 0, 0, dp(6));
        card.addView(title, fullW());

        TextView sub = new TextView(this);
        sub.setText("Sesin gücü diyaframdan gelir. Uzun nefes verme diyaframı aktive eder. 5 set yapılır.");
        sub.setTextSize(13);
        sub.setTextColor(SUB);
        sub.setLineSpacing(dp(3), 1.4f);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subLp.setMargins(0, 0, 0, dp(16));
        card.addView(sub, subLp);

        card.addView(stepRow("4", "Burundan nefes al — yalnızca karın şişsin, göğüs hareketsiz kalsın."));
        card.addView(stepRow("2", "Nefesi tut — karın şişkin sabit, omuzlar düşük."));
        card.addView(stepRow("6", "Ağızdan yavaşça ver — karın içeri çekilsin."));

        breathBtn = new TextView(this);
        breathBtn.setText("▶  Zamanlayıcıyı Başlat");
        breathBtn.setTextSize(14);
        breathBtn.setTypeface(Typeface.DEFAULT_BOLD);
        breathBtn.setTextColor(0xFF060910);
        breathBtn.setGravity(Gravity.CENTER);
        breathBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_gold));
        breathBtn.setOnClickListener(v -> toggleBreath());
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(50));
        btnLp.setMargins(0, dp(16), 0, dp(10));
        card.addView(breathBtn, btnLp);

        // Breath display
        LinearLayout disp = new LinearLayout(this);
        disp.setOrientation(LinearLayout.VERTICAL);
        disp.setGravity(Gravity.CENTER);
        disp.setVisibility(View.GONE);
        disp.setPadding(0, dp(6), 0, dp(10));
        breathDisplay = disp;

        // Circular display container
        FrameLayout circleContainer = new FrameLayout(this);
        GradientDrawable cirGd = new GradientDrawable();
        cirGd.setShape(GradientDrawable.OVAL);
        cirGd.setColor(0x18000000);
        cirGd.setStroke(dp(2), 0x40CFAB52);
        int csz = dp(130);
        circleContainer.setBackgroundDrawable(cirGd);

        LinearLayout cirInner = new LinearLayout(this);
        cirInner.setOrientation(LinearLayout.VERTICAL);
        cirInner.setGravity(Gravity.CENTER);

        breathPhaseTv = new TextView(this);
        breathPhaseTv.setText("NEFES AL");
        breathPhaseTv.setTextSize(9);
        breathPhaseTv.setLetterSpacing(0.2f);
        breathPhaseTv.setTypeface(Typeface.DEFAULT_BOLD);
        breathPhaseTv.setTextColor(MUTED);
        breathPhaseTv.setGravity(Gravity.CENTER);

        breathCountTv = new TextView(this);
        breathCountTv.setText("4");
        breathCountTv.setTextSize(64);
        breathCountTv.setTypeface(Typeface.DEFAULT_BOLD);
        breathCountTv.setTextColor(BLUE);
        breathCountTv.setGravity(Gravity.CENTER);

        breathSetTv = new TextView(this);
        breathSetTv.setText("Set 1 / 5");
        breathSetTv.setTextSize(11);
        breathSetTv.setTextColor(MUTED);
        breathSetTv.setGravity(Gravity.CENTER);

        cirInner.addView(breathPhaseTv, fullW());
        cirInner.addView(breathCountTv, fullW());
        cirInner.addView(breathSetTv, fullW());

        circleContainer.addView(cirInner, new FrameLayout.LayoutParams(csz, csz));
        LinearLayout.LayoutParams cirLp = new LinearLayout.LayoutParams(csz, csz);
        cirLp.gravity = Gravity.CENTER_HORIZONTAL;
        disp.addView(circleContainer, cirLp);
        card.addView(disp, fullW());

        TextView note = new TextView(this);
        note.setText("5 seti bitirince: cümle ortasında nefes tükenmez, ses titreşmez.");
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
        breathBtn.setTextColor(0xFF060910);
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
        LinearLayout card = card(ACCENT_REZON);

        TextView hum = new TextView(this);
        hum.setText("M · N · NG");
        hum.setTextSize(30);
        hum.setTypeface(Typeface.DEFAULT_BOLD);
        hum.setTextColor(GOLD);
        hum.setLetterSpacing(0.08f);
        LinearLayout.LayoutParams humLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        humLp.setMargins(0, 0, 0, dp(6));
        card.addView(hum, humLp);

        TextView sub = new TextView(this);
        sub.setText("Sesi göğse çeker, kalınlaştırır. Sıra: M → N → NG → Mmm-maa/mee/mii/moo/muu → Ha×5. Toplam 3 set.");
        sub.setTextSize(13);
        sub.setTextColor(SUB);
        sub.setLineSpacing(dp(3), 1.4f);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subLp.setMargins(0, 0, 0, dp(14));
        card.addView(sub, subLp);

        card.addView(turBox(new String[][]{
            {"M",  "Mmm — 5 saniye → geçiş ↓"},
            {"N",  "Nnnnn — 5 saniye → geçiş ↓"},
            {"NG", "Ngngng — 5 saniye → geçiş ↓"},
            {"+",  "Mmm-maa · mee · mii · moo · muu → geçiş ↓"},
            {"✓",  "Ha-ha-ha × 5 → 1 set bitti, 3-5 sn nefes, tekrar"}
        }));

        card.addView(posTitle("M — Ağız Pozisyonu"));
        card.addView(posNote("Dudaklar kapalı, dişler ayrık. İşaret parmağını dudağına daya: titreşim hissedilmeli."));
        card.addView(posTitle("N — Ağız Pozisyonu"));
        card.addView(posNote("Dil ucunu üst dişlerin hemen arkasına daya. Parmağını burnunun üstüne koy: titreşim hissedilmeli."));
        card.addView(posTitle("NG — Ağız Pozisyonu"));
        card.addView(posNote("Dil kökü yumuşak damağa değiyor. Elini kafanın arkasına koy: titreşim oradan gelmeli."));

        return wrapCard(card);
    }

    // ── ISINMA CARD ───────────────────────────────────────────────────────────

    View buildIsinmaCard() {
        LinearLayout card = card(ACCENT_ISINMA);

        TextView vowels = new TextView(this);
        vowels.setText("A · E · I · İ · O · Ö · U · Ü");
        vowels.setTextSize(20);
        vowels.setTypeface(Typeface.DEFAULT_BOLD);
        vowels.setTextColor(GREEN);
        vowels.setLetterSpacing(0.04f);
        LinearLayout.LayoutParams vLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        vLp.setMargins(0, 0, 0, dp(14));
        card.addView(vowels, vLp);

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
        noteLp.setMargins(0, dp(12), 0, 0);
        card.addView(note, noteLp);

        return wrapCard(card);
    }

    // ── TEKERLEMELER ──────────────────────────────────────────────────────────

    void buildTekerlemeler(LinearLayout parent) {
        try {
            String sesGrubu = safeStr(dayData, "sesGrubu");

            // Ses grubu section label (special)
            LinearLayout sgRow = new LinearLayout(this);
            sgRow.setOrientation(LinearLayout.HORIZONTAL);
            sgRow.setGravity(Gravity.CENTER_VERTICAL);
            sgRow.setPadding(dp(16), dp(26), dp(16), dp(10));

            // Colored accent bar
            View accent = new View(this);
            GradientDrawable accGd = new GradientDrawable();
            accGd.setShape(GradientDrawable.RECTANGLE);
            accGd.setColor(GOLD);
            accGd.setCornerRadius(dp(2));
            accent.setBackgroundDrawable(accGd);
            LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(dp(4), dp(16));
            aLp.setMargins(0, 0, dp(10), 0);
            sgRow.addView(accent, aLp);

            TextView sgLabel = new TextView(this);
            sgLabel.setText("TEKERLEME");
            sgLabel.setTextSize(9);
            sgLabel.setLetterSpacing(0.25f);
            sgLabel.setTypeface(Typeface.DEFAULT_BOLD);
            sgLabel.setTextColor(SUB);
            sgRow.addView(sgLabel);

            View line = new View(this);
            line.setBackgroundColor(BORDER);
            LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(0, 1, 1f);
            lLp.setMargins(dp(12), 0, 0, 0);
            sgRow.addView(line, lLp);

            // Ses grubu chip
            TextView sgChip = new TextView(this);
            sgChip.setText("  " + sesGrubu + "  ");
            sgChip.setTextSize(10);
            sgChip.setTypeface(Typeface.DEFAULT_BOLD);
            sgChip.setTextColor(GOLD);
            GradientDrawable scGd = new GradientDrawable();
            scGd.setShape(GradientDrawable.RECTANGLE);
            scGd.setColor(0x1ACFAB52);
            scGd.setStroke(1, 0x40CFAB52);
            scGd.setCornerRadius(dp(20));
            sgChip.setBackgroundDrawable(scGd);
            sgChip.setPadding(dp(10), dp(4), dp(10), dp(4));
            LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            chipLp.setMargins(dp(10), 0, 0, 0);
            sgRow.addView(sgChip, chipLp);

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
        col.setPadding(dp(18), dp(18), dp(16), dp(16));

        TextView num = new TextView(this);
        num.setText("TEKERLEME " + (idx + 1));
        num.setTextSize(9);
        num.setLetterSpacing(0.22f);
        num.setTypeface(Typeface.DEFAULT_BOLD);
        num.setTextColor(MUTED);
        LinearLayout.LayoutParams nLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nLp.setMargins(0, 0, 0, dp(12));
        col.addView(num, nLp);

        TextView txt = new TextView(this);
        txt.setText(metin);
        txt.setTextSize(19);
        txt.setTextColor(TEXT);
        txt.setLineSpacing(dp(5), 1.3f);
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

        col.addView(turBoxTek(idx));
        outer.addView(col);
        return outer;
    }

    View turBoxTek(int tekIdx) {
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
        tlLp.setMargins(0, 0, 0, dp(12));
        box.addView(title, tlLp);

        String[] turNames = {"5× Yavaş", "5× Normal", "5× Hızlı"};
        String[] turDescs = {"Her heceyi abartarak", "Doğal konuşma hızı", "Maks hız — ses yutulmasın"};
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
        rowLp.setMargins(0, 0, 0, turIdx < 2 ? dp(12) : 0);
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

        // Text
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

        // 5 tap dots (larger = more tappable)
        LinearLayout dotsRow = new LinearLayout(this);
        dotsRow.setOrientation(LinearLayout.HORIZONTAL);
        dotsRow.setGravity(Gravity.CENTER_VERTICAL);
        View[] dots = new View[5];
        for (int d = 0; d < 5; d++) {
            View dot = new View(this);
            int dsz = dp(28);
            LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(dsz, dsz);
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
        LinearLayout card = card(ACCENT_OKUMA);
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
            bLp.setMargins(0, 0, 0, dp(20));
            card.addView(bas, bLp);

            for (String para : body.split("\n+")) {
                if (para.trim().isEmpty()) continue;
                TextView pt = new TextView(this);
                pt.setText(para.trim());
                pt.setTextSize(15);
                pt.setTextColor(0xFFAFC4D8);
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
                LinearLayout.LayoutParams vBoxLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                vBoxLp.setMargins(0, 0, 0, dp(14));
                vBox.setLayoutParams(vBoxLp);

                TextView vLbl = new TextView(this);
                vLbl.setText("VURGU PRATİĞİ");
                vLbl.setTextSize(9);
                vLbl.setLetterSpacing(0.2f);
                vLbl.setTypeface(Typeface.DEFAULT_BOLD);
                vLbl.setTextColor(MUTED);
                LinearLayout.LayoutParams vLblLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                vLblLp.setMargins(0, 0, 0, dp(10));
                vBox.addView(vLbl, vLblLp);

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

                    TextView tv = new TextView(this);
                    tv.setText(v.getString("t"));
                    tv.setTextSize(13);
                    tv.setTextColor(SUB);
                    tv.setLineSpacing(dp(2), 1.4f);
                    vRow.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
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
        return wrapCard(card);
    }

    // ── DONE BAR ──────────────────────────────────────────────────────────────

    View buildDoneBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.VERTICAL);
        bar.setPadding(dp(16), dp(10), dp(16), dp(26));
        bar.setBackgroundDrawable(getDrawable(R.drawable.bottom_fade));

        boolean done = isDone();
        TextView btn = new TextView(this);
        btn.setText(done ? "✓  Tamamlandı — Kapat" : "✓  Bugünü Tamamladım");
        btn.setTextSize(16);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setGravity(Gravity.CENTER);
        btn.setTextColor(done ? GREEN : 0xFF060910);
        btn.setBackgroundDrawable(getDrawable(done ? R.drawable.btn_green_outline : R.drawable.btn_gold));
        btn.setOnClickListener(v -> {
            if (!isDone()) markDone();
            finish();
        });
        bar.addView(btn, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(58)));
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

    View sectionLabel(String text, int accentColor) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(26), dp(16), dp(10));

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

        View line = new View(this);
        line.setBackgroundColor(BORDER);
        LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(0, 1, 1f);
        lLp.setMargins(dp(10), 0, 0, 0);
        row.addView(line, lLp);
        return row;
    }

    // card() with thin colored top accent
    LinearLayout card(int accentColor) {
        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);

        View topAccent = new View(this);
        topAccent.setBackgroundColor(accentColor & 0x40FFFFFF | (accentColor & 0x00FFFFFF));
        // Just use a subtle version via alpha
        topAccent.setBackgroundColor((accentColor & 0x00FFFFFF) | 0x30000000);
        // Actually set it properly:
        topAccent.setBackgroundColor(accentColor);
        topAccent.setAlpha(0.5f);
        outer.addView(topAccent, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(2)));

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(dp(18), dp(18), dp(18), dp(18));
        outer.addView(inner, fullW());
        return inner; // caller adds children to inner; outer is the real card
    }

    View wrapCard(LinearLayout inner) {
        // inner's parent is the outer LinearLayout created in card()
        LinearLayout outer = (LinearLayout) inner.getParent();
        if (outer == null) {
            // Fallback: plain card
            FrameLayout frame = new FrameLayout(this);
            frame.setBackgroundDrawable(getDrawable(R.drawable.card_bg));
            frame.addView(inner, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(dp(14), 0, dp(14), dp(12));
            frame.setLayoutParams(lp);
            return frame;
        }
        outer.setBackgroundDrawable(getDrawable(R.drawable.card_bg));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(14), 0, dp(14), dp(12));
        outer.setLayoutParams(lp);
        return outer;
    }

    View stepRow(String num, String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.setMargins(0, 0, 0, dp(10));
        row.setLayoutParams(rowLp);

        // Number badge
        TextView nc = new TextView(this);
        nc.setText(num);
        nc.setTextSize(11);
        nc.setTypeface(Typeface.DEFAULT_BOLD);
        nc.setTextColor(0xFF060910);
        nc.setGravity(Gravity.CENTER);
        GradientDrawable ngd = new GradientDrawable();
        ngd.setShape(GradientDrawable.OVAL);
        ngd.setColor(BLUE);
        nc.setBackgroundDrawable(ngd);
        LinearLayout.LayoutParams ncLp = new LinearLayout.LayoutParams(dp(24), dp(24));
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
            LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(dp(34), dp(24));
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

    View posTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(10);
        tv.setLetterSpacing(0.1f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(MUTED);
        tv.setPadding(0, dp(14), 0, dp(6));
        return tv;
    }

    View posNote(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(13);
        tv.setTextColor(SUB);
        tv.setLineSpacing(dp(2), 1.4f);
        return tv;
    }

    // ── LAYOUT HELPERS ────────────────────────────────────────────────────────

    LinearLayout.LayoutParams fullW() {
        return new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
