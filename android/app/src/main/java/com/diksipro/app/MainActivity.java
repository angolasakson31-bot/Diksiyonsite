package com.diksipro.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {

    static final String PREFS         = "diksipro";
    static final String KEY_STARTED   = "started";
    static final String KEY_CURRENT   = "currentDay";
    static final String KEY_COMPLETED = "completed";
    static final String KEY_STREAK    = "streak";

    static final int BG     = 0xFF07090F;
    static final int S1     = 0xFF0D1220;
    static final int S2     = 0xFF131B2C;
    static final int GOLD   = 0xFFCFAB52;
    static final int GOLD2  = 0xFFE8CC80;
    static final int TEXT   = 0xFFE2EAF4;
    static final int SUB    = 0xFF8FA3BC;
    static final int MUTED  = 0xFF4D617A;
    static final int GREEN  = 0xFF2EA868;
    static final int BORDER = 0xFF1C2A3F;

    JSONArray programData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(S1);
        loadData();
        if (!prefs().getBoolean(KEY_STARTED, false)) showSetup();
        else showMain();
    }

    void loadData() {
        try {
            InputStream is = getAssets().open("program.json");
            byte[] buf = new byte[is.available()];
            is.read(buf); is.close();
            programData = new JSONArray(new String(buf, StandardCharsets.UTF_8));
        } catch (Exception e) { programData = new JSONArray(); }
    }

    SharedPreferences prefs() { return getSharedPreferences(PREFS, MODE_PRIVATE); }

    // ── SETUP ──────────────────────────────────────────────────────────────────

    void showSetup() {
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(BG);
        sv.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(28), dp(72), dp(28), dp(52));

        // D-wave logo approximation
        LinearLayout logoArea = new LinearLayout(this);
        logoArea.setOrientation(LinearLayout.HORIZONTAL);
        logoArea.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams laLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        laLp.setMargins(0, 0, 0, dp(8));
        logoArea.setLayoutParams(laLp);

        // Bar
        View bar = new View(this);
        android.graphics.drawable.GradientDrawable barGd = new android.graphics.drawable.GradientDrawable();
        barGd.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        barGd.setColor(GOLD);
        barGd.setCornerRadius(dp(6));
        bar.setBackgroundDrawable(barGd);
        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(dp(8), dp(54));
        barLp.setMargins(0, 0, dp(3), 0);
        logoArea.addView(bar, barLp);

        // Arcs as text
        String[] arcs = {")", ")", ")"};
        int[] alphas = {0xFF, 0xAA, 0x55};
        for (int i = 0; i < arcs.length; i++) {
            TextView a = new TextView(this);
            a.setText(arcs[i]);
            a.setTextSize(36);
            a.setTypeface(Typeface.DEFAULT_BOLD);
            a.setTextColor((alphas[i] << 24) | (GOLD & 0xFFFFFF));
            LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            aLp.setMargins(i == 0 ? 0 : -dp(8), 0, 0, 0);
            logoArea.addView(a, aLp);
        }
        root.addView(logoArea);

        // Brand
        TextView brand = new TextView(this);
        brand.setText("DiksiPro");
        brand.setTextSize(46);
        brand.setTypeface(Typeface.DEFAULT_BOLD);
        brand.setTextColor(TEXT);
        brand.setGravity(Gravity.CENTER);
        brand.setLetterSpacing(-0.03f);
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bLp.setMargins(0, 0, 0, dp(8));
        root.addView(brand, bLp);

        TextView tagline = new TextView(this);
        tagline.setText("PROFESYONEL SES & DİKSİYON EĞİTİMİ");
        tagline.setTextSize(9);
        tagline.setLetterSpacing(0.28f);
        tagline.setTextColor(MUTED);
        tagline.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tLp.setMargins(0, 0, 0, dp(56));
        root.addView(tagline, tLp);

        // Feature pills
        LinearLayout feats = new LinearLayout(this);
        feats.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams fLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fLp.setMargins(0, 0, 0, dp(44));
        feats.setLayoutParams(fLp);

        String[][] items = {{"9", "HAFTA"}, {"63", "GÜN"}, {"~10dk", "GÜNLÜK"}};
        for (int i = 0; i < 3; i++) {
            LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            if (i < 2) fp.setMargins(0, 0, dp(10), 0);
            feats.addView(featPill(items[i][0], items[i][1]), fp);
        }
        root.addView(feats);

        // Desc card
        LinearLayout descCard = new LinearLayout(this);
        descCard.setOrientation(LinearLayout.VERTICAL);
        descCard.setBackgroundDrawable(getDrawable(R.drawable.card_bg));
        descCard.setPadding(dp(20), dp(20), dp(20), dp(20));
        LinearLayout.LayoutParams dcLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dcLp.setMargins(0, 0, 0, dp(24));

        String[] points = {
            "Nefes ve diyafram teknikleri",
            "Ses grubu tekerlemeleri (günlük değişen)",
            "Rezonans ve artikülasyon ısınmaları",
            "Sesli okuma metinleri"
        };
        for (String pt : points) {
            LinearLayout ptRow = new LinearLayout(this);
            ptRow.setOrientation(LinearLayout.HORIZONTAL);
            ptRow.setGravity(Gravity.TOP);
            LinearLayout.LayoutParams ptLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ptLp.setMargins(0, 0, 0, dp(10));
            ptRow.setLayoutParams(ptLp);

            TextView dot = new TextView(this);
            dot.setText("▸");
            dot.setTextSize(13);
            dot.setTextColor(GOLD);
            LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(dp(22), ViewGroup.LayoutParams.WRAP_CONTENT);
            ptRow.addView(dot, dLp);

            TextView ptTv = new TextView(this);
            ptTv.setText(pt);
            ptTv.setTextSize(13);
            ptTv.setTextColor(SUB);
            ptTv.setLineSpacing(dp(2), 1.3f);
            ptRow.addView(ptTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            descCard.addView(ptRow);
        }
        root.addView(descCard, dcLp);

        // CTA
        TextView cta = new TextView(this);
        cta.setText("Programa Başla  →");
        cta.setTextSize(17);
        cta.setTypeface(Typeface.DEFAULT_BOLD);
        cta.setTextColor(0xFF060910);
        cta.setGravity(Gravity.CENTER);
        cta.setBackgroundDrawable(getDrawable(R.drawable.btn_gold));
        cta.setOnClickListener(v -> {
            prefs().edit()
                .putBoolean(KEY_STARTED, true)
                .putInt(KEY_CURRENT, 0)
                .putString(KEY_COMPLETED, "")
                .putInt(KEY_STREAK, 0)
                .apply();
            showMain();
        });
        root.addView(cta, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(58)));

        sv.addView(root);
        setContentView(sv);
    }

    // ── MAIN ───────────────────────────────────────────────────────────────────

    void showMain() {
        SharedPreferences p = prefs();
        int cur    = p.getInt(KEY_CURRENT, 0);
        int streak = p.getInt(KEY_STREAK, 0);
        boolean[] done = parseDone(p.getString(KEY_COMPLETED, ""));
        int maxUnlocked = calcMaxUnlocked(done);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG);

        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(BG);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, 0, 0, dp(84));

        content.addView(buildAppBar(streak));
        content.addView(buildHero(cur, done, maxUnlocked));
        content.addView(buildStats(done, streak));
        content.addView(buildWeekSection(cur, done, maxUnlocked));

        sv.addView(content, new ScrollView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(sv, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        FrameLayout.LayoutParams navLp = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        navLp.gravity = Gravity.BOTTOM;
        root.addView(buildBottomNav(cur, maxUnlocked), navLp);

        setContentView(root);
    }

    View buildAppBar(int streak) {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(S1);
        bar.setPadding(dp(20), dp(16), dp(20), dp(16));

        TextView logo = new TextView(this);
        logo.setText("DiksiPro");
        logo.setTextSize(20);
        logo.setTypeface(Typeface.DEFAULT_BOLD);
        logo.setTextColor(TEXT);
        logo.setLetterSpacing(-0.02f);
        bar.addView(logo, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout chip = new LinearLayout(this);
        chip.setOrientation(LinearLayout.HORIZONTAL);
        chip.setGravity(Gravity.CENTER_VERTICAL);
        chip.setPadding(dp(11), dp(6), dp(11), dp(6));
        chip.setBackgroundDrawable(getDrawable(streak > 2 ? R.drawable.badge_hot : R.drawable.badge_normal));

        TextView chipTv = new TextView(this);
        chipTv.setText((streak > 2 ? "🔥 " : "◈ ") + streak + " gün");
        chipTv.setTextSize(12);
        chipTv.setTypeface(Typeface.DEFAULT_BOLD);
        chipTv.setTextColor(streak > 2 ? GOLD : SUB);
        chip.addView(chipTv);
        bar.addView(chip);

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.addView(bar, fullW());
        View div = new View(this);
        div.setBackgroundColor(BORDER);
        wrapper.addView(div, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        return wrapper;
    }

    View buildHero(int cur, boolean[] done, int maxUnlocked) {
        boolean isDone   = done[cur];
        boolean isLocked = cur > maxUnlocked;

        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(16), dp(20), dp(16), dp(4));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundDrawable(getDrawable(R.drawable.card_bg));
        card.setPadding(dp(22), dp(24), dp(22), dp(22));

        // Set + week chip
        String set = getSetName(cur);
        int setWeek = (cur / 7) % 3 + 1;
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams trLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        trLp.setMargins(0, 0, 0, dp(12));

        TextView setChip = new TextView(this);
        setChip.setText("Set " + set + "  ·  " + setWeek + ". Hafta");
        setChip.setTextSize(10);
        setChip.setLetterSpacing(0.18f);
        setChip.setTypeface(Typeface.DEFAULT_BOLD);
        setChip.setTextColor(GOLD);
        setChip.setPadding(dp(10), dp(4), dp(10), dp(4));
        android.graphics.drawable.GradientDrawable chipGd = new android.graphics.drawable.GradientDrawable();
        chipGd.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        chipGd.setColor(0x18CFAB52);
        chipGd.setStroke(1, 0x40CFAB52);
        chipGd.setCornerRadius(dp(20));
        setChip.setBackgroundDrawable(chipGd);
        topRow.addView(setChip);
        topRow.addView(spacer(1f));

        if (isDone) {
            TextView donePill = new TextView(this);
            donePill.setText("✓ Tamamlandı");
            donePill.setTextSize(10);
            donePill.setTypeface(Typeface.DEFAULT_BOLD);
            donePill.setTextColor(GREEN);
            donePill.setPadding(dp(10), dp(4), dp(10), dp(4));
            android.graphics.drawable.GradientDrawable dpGd = new android.graphics.drawable.GradientDrawable();
            dpGd.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            dpGd.setColor(0x182EA868);
            dpGd.setStroke(1, 0x502EA868);
            dpGd.setCornerRadius(dp(20));
            donePill.setBackgroundDrawable(dpGd);
            topRow.addView(donePill);
        }
        card.addView(topRow, trLp);

        // Big day number
        TextView dayBig = new TextView(this);
        dayBig.setText("GÜN " + (cur + 1));
        dayBig.setTextSize(54);
        dayBig.setTypeface(Typeface.DEFAULT_BOLD);
        dayBig.setTextColor(isDone ? MUTED : TEXT);
        dayBig.setLetterSpacing(-0.04f);
        LinearLayout.LayoutParams dlLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dlLp.setMargins(0, 0, 0, dp(6));
        card.addView(dayBig, dlLp);

        // Ses grubu
        try {
            String sg = programData.getJSONObject(cur).getString("sesGrubu");
            TextView sgTv = new TextView(this);
            sgTv.setText(sg);
            sgTv.setTextSize(16);
            sgTv.setTextColor(isDone ? MUTED : GOLD);
            LinearLayout.LayoutParams sgLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            sgLp.setMargins(0, 0, 0, dp(22));
            card.addView(sgTv, sgLp);
        } catch (Exception ignored) {}

        // Progress bar
        int totalDone = 0;
        for (boolean b : done) if (b) totalDone++;
        int pct = (int)(totalDone / 63.0 * 100);

        LinearLayout progRow = new LinearLayout(this);
        progRow.setOrientation(LinearLayout.HORIZONTAL);
        progRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams prLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        prLp.setMargins(0, 0, 0, dp(22));
        progRow.setLayoutParams(prLp);

        FrameLayout track = new FrameLayout(this);
        track.setBackgroundDrawable(getDrawable(R.drawable.progress_track));
        LinearLayout.LayoutParams trkLp = new LinearLayout.LayoutParams(0, dp(5), 1f);
        track.setLayoutParams(trkLp);
        View fill = new View(this);
        fill.setBackgroundDrawable(getDrawable(R.drawable.progress_fill));
        fill.setLayoutParams(new FrameLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT));
        track.addView(fill);
        track.post(() -> fill.setLayoutParams(new FrameLayout.LayoutParams(
            (int)(track.getWidth() * pct / 100.0), ViewGroup.LayoutParams.MATCH_PARENT)));
        progRow.addView(track, trkLp);

        TextView pctTv = new TextView(this);
        pctTv.setText(pct + "%");
        pctTv.setTextSize(12);
        pctTv.setTypeface(Typeface.DEFAULT_BOLD);
        pctTv.setTextColor(GOLD);
        LinearLayout.LayoutParams pctLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pctLp.setMargins(dp(12), 0, 0, 0);
        progRow.addView(pctTv, pctLp);
        card.addView(progRow);

        // Action
        final int fCur = cur;
        if (isLocked) {
            TextView lockTv = new TextView(this);
            lockTv.setText("🔒  Önceki günü tamamla");
            lockTv.setTextSize(14);
            lockTv.setTypeface(Typeface.DEFAULT_BOLD);
            lockTv.setTextColor(MUTED);
            lockTv.setGravity(Gravity.CENTER);
            lockTv.setBackgroundDrawable(getDrawable(R.drawable.btn_locked_card));
            lockTv.setAlpha(0.7f);
            card.addView(lockTv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)));
        } else if (isDone) {
            TextView replayBtn = new TextView(this);
            replayBtn.setText("↺  Dersi Tekrar Aç");
            replayBtn.setTextSize(14);
            replayBtn.setTypeface(Typeface.DEFAULT_BOLD);
            replayBtn.setTextColor(GREEN);
            replayBtn.setGravity(Gravity.CENTER);
            replayBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_green_outline));
            replayBtn.setOnClickListener(v -> openLesson(fCur));
            card.addView(replayBtn, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)));
        } else {
            TextView startBtn = new TextView(this);
            startBtn.setText("Derse Başla  →");
            startBtn.setTextSize(17);
            startBtn.setTypeface(Typeface.DEFAULT_BOLD);
            startBtn.setTextColor(0xFF060910);
            startBtn.setGravity(Gravity.CENTER);
            startBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_gold));
            startBtn.setOnClickListener(v -> openLesson(fCur));
            card.addView(startBtn, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(58)));
        }

        wrap.addView(card, fullW());
        return wrap;
    }

    View buildStats(boolean[] done, int streak) {
        int totalDone = 0;
        for (boolean b : done) if (b) totalDone++;

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rLp.setMargins(dp(16), 0, dp(16), dp(20));
        row.setLayoutParams(rLp);

        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        LinearLayout.LayoutParams sm = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        sm.setMargins(dp(8), 0, dp(8), 0);

        row.addView(statCard("" + totalDone, "Tamamlanan", GREEN), sp);
        row.addView(statCard("" + (63 - totalDone), "Kalan gün", MUTED), sm);
        row.addView(statCard(streak + " gün", "Seri", streak > 2 ? GOLD : MUTED), sp);
        return row;
    }

    View buildWeekSection(int cur, boolean[] done, int maxUnlocked) {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(dp(16), 0, dp(16), dp(8));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hLp.setMargins(0, 0, 0, dp(12));
        header.setLayoutParams(hLp);

        TextView lbl = new TextView(this);
        lbl.setText("BU HAFTA");
        lbl.setTextSize(10);
        lbl.setLetterSpacing(0.22f);
        lbl.setTypeface(Typeface.DEFAULT_BOLD);
        lbl.setTextColor(MUTED);
        header.addView(lbl);

        View hLine = new View(this);
        hLine.setBackgroundColor(BORDER);
        LinearLayout.LayoutParams hlLp = new LinearLayout.LayoutParams(0, 1, 1f);
        hlLp.setMargins(dp(12), 0, 0, 0);
        header.addView(hLine, hlLp);
        section.addView(header);

        LinearLayout weekCard = new LinearLayout(this);
        weekCard.setOrientation(LinearLayout.VERTICAL);
        weekCard.setBackgroundDrawable(getDrawable(R.drawable.card_bg));

        int weekStart = (cur / 7) * 7;
        for (int d = weekStart; d < weekStart + 7 && d <= 62; d++) {
            weekCard.addView(buildWeekRow(d, cur, done, maxUnlocked));
            if (d < weekStart + 6 && d < 62) {
                View div = new View(this);
                div.setBackgroundColor(BORDER);
                weekCard.addView(div, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            }
        }
        section.addView(weekCard, fullW());
        return section;
    }

    View buildWeekRow(int day, int cur, boolean[] done, int maxUnlocked) {
        boolean isDone  = done[day];
        boolean isToday = day == cur;
        boolean isLocked = day > maxUnlocked;

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(14), dp(16), dp(14));
        if (isToday) row.setBackgroundColor(0x0FCFAB52);

        // Circle
        FrameLayout circ = new FrameLayout(this);
        int csz = dp(38);
        circ.setBackgroundDrawable(getDrawable(
            isDone ? R.drawable.circle_green :
            isToday ? R.drawable.circle_gold :
            R.drawable.circle_muted));
        TextView numTv = new TextView(this);
        numTv.setText(String.valueOf(day + 1));
        numTv.setTextSize(12);
        numTv.setTypeface(Typeface.DEFAULT_BOLD);
        numTv.setTextColor(isDone || isToday ? 0xFF050810 : MUTED);
        numTv.setGravity(Gravity.CENTER);
        circ.addView(numTv, new FrameLayout.LayoutParams(csz, csz));
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(csz, csz);
        cLp.setMargins(0, 0, dp(14), 0);
        row.addView(circ, cLp);

        // Text
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        String sg = "";
        try { sg = programData.getJSONObject(day).getString("sesGrubu"); } catch (Exception ignored) {}
        TextView sg1 = new TextView(this);
        sg1.setText(sg);
        sg1.setTextSize(13);
        sg1.setTypeface(isToday ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        sg1.setTextColor(isDone ? SUB : isToday ? TEXT : SUB);
        col.addView(sg1);

        String status = isDone ? "Tamamlandı" :
            isToday ? "Devam et" :
            isLocked ? "Kilitli" : "Başlanmadı";
        TextView stTv = new TextView(this);
        stTv.setText(status);
        stTv.setTextSize(11);
        stTv.setTextColor(isDone ? GREEN : isToday ? GOLD : MUTED);
        col.addView(stTv);
        row.addView(col);

        // Right icon
        TextView arrow = new TextView(this);
        arrow.setText(isDone ? "✓" : isLocked ? "🔒" : "›");
        arrow.setTextSize(isLocked ? 11 : 20);
        arrow.setTextColor(isDone ? GREEN : MUTED);
        row.addView(arrow);

        final int d = day;
        row.setOnClickListener(v -> {
            if (!isLocked) {
                prefs().edit().putInt(KEY_CURRENT, d).apply();
                openLesson(d);
            }
        });
        return row;
    }

    View buildBottomNav(int cur, int maxUnlocked) {
        boolean canPrev = cur > 0;
        boolean canNext = cur < maxUnlocked;

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        nav.setBackgroundColor(S1);
        nav.setPadding(dp(16), dp(12), dp(16), dp(26));

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        View topBorder = new View(this);
        topBorder.setBackgroundColor(BORDER);
        wrapper.addView(topBorder, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        TextView prev = navBtn("← Önceki", canPrev);
        prev.setOnClickListener(v -> {
            if (canPrev) {
                prefs().edit().putInt(KEY_CURRENT, cur - 1).apply();
                showMain();
            }
        });

        TextView midTv = new TextView(this);
        midTv.setText("Gün " + (cur + 1) + " / 63");
        midTv.setTextSize(12);
        midTv.setTextColor(MUTED);
        midTv.setGravity(Gravity.CENTER);

        TextView next = navBtn("Sonraki →", canNext);
        next.setOnClickListener(v -> {
            if (canNext) {
                prefs().edit().putInt(KEY_CURRENT, cur + 1).apply();
                showMain();
            }
        });

        nav.addView(prev);
        nav.addView(midTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        nav.addView(next);

        wrapper.setBackgroundColor(S1);
        wrapper.addView(nav, fullW());
        return wrapper;
    }

    // ── HELPERS ────────────────────────────────────────────────────────────────

    void openLesson(int day) {
        prefs().edit().putInt(KEY_CURRENT, day).apply();
        Intent i = new Intent(this, LessonActivity.class);
        i.putExtra("day", day);
        startActivityForResult(i, 1);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == 1) showMain();
    }

    boolean[] parseDone(String s) {
        boolean[] done = new boolean[63];
        if (!s.isEmpty()) {
            for (String tok : s.split(",")) {
                try { done[Integer.parseInt(tok.trim())] = true; } catch (Exception ignored) {}
            }
        }
        return done;
    }

    int calcMaxUnlocked(boolean[] done) {
        for (int i = 62; i >= 0; i--) {
            if (done[i]) return Math.min(62, i + 1);
        }
        return 0;
    }

    String getSetName(int day) {
        String[] sets = {"A1","A2","A3","B1","B2","B3","C1","C2","C3"};
        int wi = day / 7;
        return wi < sets.length ? sets[wi] : "C3";
    }

    // ── VIEW FACTORIES ─────────────────────────────────────────────────────────

    View featPill(String num, String label) {
        LinearLayout p = new LinearLayout(this);
        p.setOrientation(LinearLayout.VERTICAL);
        p.setGravity(Gravity.CENTER);
        p.setBackgroundDrawable(getDrawable(R.drawable.card_s2));
        p.setPadding(dp(10), dp(18), dp(10), dp(18));

        TextView nv = new TextView(this);
        nv.setText(num);
        nv.setTextSize(28);
        nv.setTypeface(Typeface.DEFAULT_BOLD);
        nv.setTextColor(GOLD);
        nv.setGravity(Gravity.CENTER);
        p.addView(nv, fullW());

        TextView lv = new TextView(this);
        lv.setText(label);
        lv.setTextSize(9);
        lv.setLetterSpacing(0.2f);
        lv.setTextColor(MUTED);
        lv.setGravity(Gravity.CENTER);
        p.addView(lv, fullW());
        return p;
    }

    View statCard(String val, String label, int color) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setGravity(Gravity.CENTER);
        c.setBackgroundDrawable(getDrawable(R.drawable.card_bg));
        c.setPadding(dp(10), dp(18), dp(10), dp(18));

        TextView vt = new TextView(this);
        vt.setText(val);
        vt.setTextSize(22);
        vt.setTypeface(Typeface.DEFAULT_BOLD);
        vt.setTextColor(color);
        vt.setGravity(Gravity.CENTER);
        c.addView(vt, fullW());

        TextView lt = new TextView(this);
        lt.setText(label);
        lt.setTextSize(10);
        lt.setTextColor(MUTED);
        lt.setGravity(Gravity.CENTER);
        c.addView(lt, fullW());
        return c;
    }

    TextView navBtn(String text, boolean enabled) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextSize(13);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setTextColor(enabled ? SUB : MUTED);
        btn.setPadding(dp(14), dp(8), dp(14), dp(8));
        btn.setBackgroundDrawable(getDrawable(enabled ? R.drawable.btn_nav : R.drawable.btn_nav_disabled));
        btn.setAlpha(enabled ? 1f : 0.35f);
        return btn;
    }

    View spacer(float weight) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(0, 0, weight));
        return v;
    }

    LinearLayout.LayoutParams fullW() {
        return new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
