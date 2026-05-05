package com.diksipro.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {

    static final String PREFS = "diksipro";
    static final String KEY_STARTED     = "started";
    static final String KEY_CURRENT     = "currentDay";
    static final String KEY_COMPLETED   = "completed";
    static final String KEY_STREAK      = "streak";
    static final String KEY_COMPLETED_AT = "completedAt";

    JSONArray programData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(0xFF0A0D16);
        getWindow().setNavigationBarColor(0xFF0A0D16);

        loadProgramData();

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean started = prefs.getBoolean(KEY_STARTED, false);

        if (!started) {
            showOnboarding();
        } else {
            showDashboard();
        }
    }

    void loadProgramData() {
        try {
            InputStream is = getAssets().open("program.json");
            byte[] buf = new byte[is.available()];
            is.read(buf);
            is.close();
            programData = new JSONArray(new String(buf, StandardCharsets.UTF_8));
        } catch (Exception e) {
            programData = new JSONArray();
        }
    }

    // ── ONBOARDING ─────────────────────────────────────────────────────────────

    void showOnboarding() {
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(0xFF0A0D16);
        sv.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(28), dp(60), dp(28), dp(40));

        // Logo area
        LinearLayout logoRow = new LinearLayout(this);
        logoRow.setOrientation(LinearLayout.HORIZONTAL);
        logoRow.setGravity(Gravity.CENTER);
        logoRow.setPadding(0, 0, 0, dp(8));

        // D icon (mini version)
        View dIcon = makeDIcon(dp(56), dp(56));
        logoRow.addView(dIcon);

        root.addView(logoRow, lp(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Brand name
        TextView brand = new TextView(this);
        brand.setText("DiksiPro");
        brand.setTextSize(42);
        brand.setTypeface(Typeface.DEFAULT_BOLD);
        brand.setTextColor(0xFFEEF2F8);
        brand.setGravity(Gravity.CENTER);
        brand.setPadding(0, dp(16), 0, dp(4));
        root.addView(brand, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tagline = new TextView(this);
        tagline.setText("PROFESYONEL SES EĞİTİMİ");
        tagline.setTextSize(10);
        tagline.setTextColor(0xFF556677);
        tagline.setLetterSpacing(0.22f);
        tagline.setGravity(Gravity.CENTER);
        tagline.setPadding(0, 0, 0, dp(48));
        root.addView(tagline, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Stats pills
        LinearLayout pills = new LinearLayout(this);
        pills.setOrientation(LinearLayout.HORIZONTAL);
        pills.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams pillLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        pillLp.setMargins(dp(6), 0, dp(6), 0);
        pills.addView(makePill("9", "Hafta"), pillLp);
        pills.addView(makePill("63", "Gün"), pillLp);
        pills.addView(makePill("~10dk", "Günlük"), pillLp);
        pills.setPadding(0, 0, 0, dp(32));
        root.addView(pills, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Description card
        View descCard = makeCard();
        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(dp(20), dp(20), dp(20), dp(20));

        TextView h2 = new TextView(this);
        h2.setText("Hoş Geldin");
        h2.setTextSize(20);
        h2.setTypeface(Typeface.DEFAULT_BOLD);
        h2.setTextColor(0xFFEEF2F8);
        h2.setPadding(0, 0, 0, dp(8));
        cardContent.addView(h2, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView desc = new TextView(this);
        desc.setText("9 haftalık profesyonel diksiyon programı. Her gün yaklaşık 10 dakika. Sesin güçlenir, konuşman netleşir.");
        desc.setTextSize(14);
        desc.setTextColor(0xFF8896AA);
        desc.setLineSpacing(dp(4), 1f);
        desc.setPadding(0, 0, 0, dp(20));
        cardContent.addView(desc, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Feature rows
        cardContent.addView(makeFeatureRow("Tekerlemeler", "Artikülasyon kaslarını aktive eder"));
        cardContent.addView(makeFeatureRow("Nefes Tekniği", "Diyafram kontrolü, sesin temeli"));
        cardContent.addView(makeFeatureRow("Sesli Okuma", "Tonlama ve ritim pratiği"));

        ((FrameLayout) descCard).addView(cardContent);
        root.addView(descCard, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Start button
        TextView startBtn = new TextView(this);
        startBtn.setText("Programa Başla");
        startBtn.setTextSize(16);
        startBtn.setTypeface(Typeface.DEFAULT_BOLD);
        startBtn.setTextColor(0xFF000000);
        startBtn.setGravity(Gravity.CENTER);
        startBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_primary));
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(56));
        btnLp.setMargins(0, dp(24), 0, 0);
        startBtn.setOnClickListener(v -> {
            SharedPreferences.Editor ed = getSharedPreferences(PREFS, MODE_PRIVATE).edit();
            ed.putBoolean(KEY_STARTED, true);
            ed.putInt(KEY_CURRENT, 0);
            ed.putString(KEY_COMPLETED, "");
            ed.putInt(KEY_STREAK, 0);
            ed.apply();
            showDashboard();
        });
        root.addView(startBtn, btnLp);

        sv.addView(root, new ScrollView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(sv);
    }

    // ── DASHBOARD ──────────────────────────────────────────────────────────────

    void showDashboard() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        int currentDay  = prefs.getInt(KEY_CURRENT, 0);
        int streak      = prefs.getInt(KEY_STREAK, 0);
        String completedStr = prefs.getString(KEY_COMPLETED, "");

        boolean[] completed = new boolean[63];
        if (!completedStr.isEmpty()) {
            for (String s : completedStr.split(",")) {
                try { completed[Integer.parseInt(s)] = true; } catch (Exception ignored) {}
            }
        }

        int maxUnlocked = 0;
        for (int i = 62; i >= 0; i--) {
            if (completed[i]) { maxUnlocked = Math.min(62, i + 1); break; }
        }
        boolean todayDone = completed[currentDay];

        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(0xFF0A0D16);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, dp(8), 0, dp(32));

        // ── Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(0xFF0F1520);
        header.setPadding(dp(18), dp(14), dp(18), dp(14));

        View dIcon = makeDIcon(dp(32), dp(32));
        header.addView(dIcon, new LinearLayout.LayoutParams(dp(32), dp(32)));

        TextView appName = new TextView(this);
        appName.setText("DiksiPro");
        appName.setTextSize(20);
        appName.setTypeface(Typeface.DEFAULT_BOLD);
        appName.setTextColor(0xFFEEF2F8);
        LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        nameLp.setMargins(dp(10), 0, 0, 0);
        header.addView(appName, nameLp);

        // Streak badge
        LinearLayout streakBadge = new LinearLayout(this);
        streakBadge.setOrientation(LinearLayout.HORIZONTAL);
        streakBadge.setGravity(Gravity.CENTER);
        streakBadge.setPadding(dp(10), dp(5), dp(10), dp(5));
        streakBadge.setBackgroundDrawable(getDrawable(streak > 0 ? R.drawable.badge_streak_hot : R.drawable.badge_streak));
        TextView streakTxt = new TextView(this);
        streakTxt.setText((streak > 0 ? "🔥 " : "") + streak + " gün");
        streakTxt.setTextSize(13);
        streakTxt.setTypeface(Typeface.DEFAULT_BOLD);
        streakTxt.setTextColor(streak > 0 ? 0xFFFFB94A : 0xFF8896AA);
        streakBadge.addView(streakTxt);
        header.addView(streakBadge);

        root.addView(header, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ── Progress section label
        LinearLayout secRow = new LinearLayout(this);
        secRow.setOrientation(LinearLayout.HORIZONTAL);
        secRow.setGravity(Gravity.CENTER_VERTICAL);
        secRow.setPadding(dp(18), dp(24), dp(18), dp(10));

        TextView secLabel = new TextView(this);
        int weekIdx = currentDay / 7;
        String setName = getSetName(currentDay);
        secLabel.setText("SET " + setName.charAt(0) + "  ·  " + (weekIdx % 3 + 1) + ". HAFTA");
        secLabel.setTextSize(10);
        secLabel.setTextColor(0xFF4A6070);
        secLabel.setLetterSpacing(0.22f);
        LinearLayout.LayoutParams secLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        secLabel.setLayoutParams(secLp);
        secRow.addView(secLabel, secLp);

        // Week dots
        LinearLayout weekDots = new LinearLayout(this);
        weekDots.setOrientation(LinearLayout.HORIZONTAL);
        weekDots.setGravity(Gravity.CENTER_VERTICAL);
        int weekStart = (currentDay / 7) * 7;
        for (int d = weekStart; d < weekStart + 7 && d <= 62; d++) {
            View dot = new View(this);
            LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(d == currentDay ? 10 : 7), dp(d == currentDay ? 10 : 7));
            dotLp.setMargins(dp(3), 0, dp(3), 0);
            dot.setLayoutParams(dotLp);
            if (completed[d]) {
                dot.setBackgroundDrawable(getDrawable(R.drawable.dot_green));
            } else if (d == currentDay) {
                dot.setBackgroundDrawable(getDrawable(R.drawable.dot_coral));
            } else {
                dot.setBackgroundDrawable(getDrawable(R.drawable.dot_muted));
            }
            weekDots.addView(dot);
        }
        secRow.addView(weekDots);
        root.addView(secRow, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ── Today's lesson card (hero)
        FrameLayout heroCard = (FrameLayout) makeCard();
        heroCard.setPadding(dp(20), dp(22), dp(20), dp(22));
        LinearLayout heroContent = new LinearLayout(this);
        heroContent.setOrientation(LinearLayout.VERTICAL);

        TextView dayLabel = new TextView(this);
        dayLabel.setText("GÜN " + (currentDay + 1) + " / 63");
        dayLabel.setTextSize(10);
        dayLabel.setTextColor(0xFFFF6B35);
        dayLabel.setLetterSpacing(0.18f);
        dayLabel.setPadding(0, 0, 0, dp(8));
        heroContent.addView(dayLabel, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Ses grubu
        String sesGrubu = getDaySesGrubu(currentDay);
        TextView sesGrubuTv = new TextView(this);
        sesGrubuTv.setText(sesGrubu);
        sesGrubuTv.setTextSize(18);
        sesGrubuTv.setTypeface(Typeface.DEFAULT_BOLD);
        sesGrubuTv.setTextColor(0xFFEEF2F8);
        sesGrubuTv.setLineSpacing(dp(2), 1f);
        sesGrubuTv.setPadding(0, 0, 0, dp(6));
        heroContent.addView(sesGrubuTv, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Reading title
        String okumaBaslik = getDayOkumaBaslik(currentDay);
        TextView readTitle = new TextView(this);
        readTitle.setText("Okuma: " + okumaBaslik);
        readTitle.setTextSize(13);
        readTitle.setTextColor(0xFF8896AA);
        readTitle.setPadding(0, 0, 0, dp(20));
        heroContent.addView(readTitle, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Tek progress bar
        int totalDone = 0;
        for (boolean b : completed) if (b) totalDone++;

        LinearLayout progressRow = new LinearLayout(this);
        progressRow.setOrientation(LinearLayout.HORIZONTAL);
        progressRow.setGravity(Gravity.CENTER_VERTICAL);
        progressRow.setPadding(0, 0, 0, dp(20));

        FrameLayout progressTrack = new FrameLayout(this);
        progressTrack.setBackgroundDrawable(getDrawable(R.drawable.progress_track));
        View progressFill = new View(this);
        int pct = (int)((totalDone / 63.0) * 100);
        progressFill.setBackgroundDrawable(getDrawable(R.drawable.progress_fill));
        FrameLayout.LayoutParams fillLp = new FrameLayout.LayoutParams(
            0, ViewGroup.LayoutParams.MATCH_PARENT);
        progressTrack.addView(progressFill, fillLp);
        LinearLayout.LayoutParams trackLp = new LinearLayout.LayoutParams(0, dp(6), 1f);
        progressTrack.setLayoutParams(trackLp);
        progressRow.addView(progressTrack, trackLp);

        // Set fill width after layout
        final View fFill = progressFill;
        final int fPct = pct;
        progressTrack.post(() -> {
            int w = progressTrack.getWidth();
            FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams((int)(w * fPct / 100.0), ViewGroup.LayoutParams.MATCH_PARENT);
            fFill.setLayoutParams(lp2);
        });

        TextView pctTv = new TextView(this);
        pctTv.setText(pct + "%");
        pctTv.setTextSize(11);
        pctTv.setTypeface(Typeface.DEFAULT_BOLD);
        pctTv.setTextColor(0xFFFF6B35);
        LinearLayout.LayoutParams pctLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pctLp.setMargins(dp(10), 0, 0, 0);
        progressRow.addView(pctTv, pctLp);
        heroContent.addView(progressRow, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Action button
        boolean isLocked = currentDay > maxUnlocked;
        TextView actionBtn = new TextView(this);
        if (todayDone) {
            actionBtn.setText("✓  Tamamlandı");
            actionBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_done));
            actionBtn.setTextColor(0xFF2ECC71);
        } else if (isLocked) {
            actionBtn.setText("🔒  Önceki günü tamamla");
            actionBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_locked));
            actionBtn.setTextColor(0xFF4A6070);
        } else {
            actionBtn.setText("Derse Başla  →");
            actionBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_primary));
            actionBtn.setTextColor(0xFF000000);
        }
        actionBtn.setTextSize(15);
        actionBtn.setTypeface(Typeface.DEFAULT_BOLD);
        actionBtn.setGravity(Gravity.CENTER);
        final int fDay = currentDay;
        actionBtn.setOnClickListener(v -> {
            if (!isLocked) openLesson(fDay);
        });
        heroContent.addView(actionBtn, lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));

        heroCard.addView(heroContent);
        LinearLayout.LayoutParams heroLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        heroLp.setMargins(dp(14), 0, dp(14), dp(14));
        root.addView(heroCard, heroLp);

        // ── Navigation: Prev / Next
        LinearLayout navRow = new LinearLayout(this);
        navRow.setOrientation(LinearLayout.HORIZONTAL);
        navRow.setGravity(Gravity.CENTER);
        navRow.setPadding(dp(14), 0, dp(14), dp(14));

        TextView prevBtn = makeNavBtn("← Önceki");
        prevBtn.setAlpha(currentDay == 0 ? 0.3f : 1f);
        final boolean canPrev = currentDay > 0;
        prevBtn.setOnClickListener(v -> {
            if (canPrev) {
                getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putInt(KEY_CURRENT, currentDay - 1).apply();
                showDashboard();
            }
        });
        LinearLayout.LayoutParams navBtnLp = new LinearLayout.LayoutParams(0, dp(44), 1f);
        navBtnLp.setMargins(0, 0, dp(8), 0);
        navRow.addView(prevBtn, navBtnLp);

        TextView nextBtn = makeNavBtn("Sonraki →");
        boolean canNext = currentDay < maxUnlocked;
        nextBtn.setAlpha(canNext ? 1f : 0.3f);
        nextBtn.setOnClickListener(v -> {
            if (canNext) {
                getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putInt(KEY_CURRENT, currentDay + 1).apply();
                showDashboard();
            }
        });
        LinearLayout.LayoutParams navBtnLp2 = new LinearLayout.LayoutParams(0, dp(44), 1f);
        navRow.addView(nextBtn, navBtnLp2);
        root.addView(navRow, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ── Stats row
        LinearLayout statsRow = new LinearLayout(this);
        statsRow.setOrientation(LinearLayout.HORIZONTAL);
        statsRow.setGravity(Gravity.CENTER);
        statsRow.setPadding(dp(14), 0, dp(14), dp(14));

        LinearLayout.LayoutParams statLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        statLp.setMargins(dp(5), 0, dp(5), 0);
        statsRow.addView(makeStatCard(String.valueOf(totalDone), "Tamamlanan"), statLp);
        statsRow.addView(makeStatCard(String.valueOf(63 - totalDone), "Kalan"), statLp);
        statsRow.addView(makeStatCard(streak + " gün", "Seri"), statLp);
        root.addView(statsRow, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ── Week overview
        TextView weekTitle = new TextView(this);
        weekTitle.setText("BU HAFTA");
        weekTitle.setTextSize(10);
        weekTitle.setTextColor(0xFF4A6070);
        weekTitle.setLetterSpacing(0.22f);
        weekTitle.setPadding(dp(18), dp(8), dp(18), dp(10));
        root.addView(weekTitle, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        for (int d = weekStart; d < weekStart + 7 && d <= 62; d++) {
            root.addView(makeWeekDayRow(d, currentDay, completed, maxUnlocked),
                lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        sv.addView(root);
        setContentView(sv);
    }

    void openLesson(int day) {
        Intent intent = new Intent(this, LessonActivity.class);
        intent.putExtra("day", day);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == 1) showDashboard();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // ── VIEW HELPERS ────────────────────────────────────────────────────────────

    View makeDIcon(int w, int h) {
        FrameLayout frame = new FrameLayout(this);
        frame.setBackgroundDrawable(getDrawable(R.drawable.ic_app_d));
        TextView dTv = new TextView(this);
        dTv.setText("D");
        dTv.setTextSize(w / getResources().getDisplayMetrics().density * 0.45f);
        dTv.setTypeface(Typeface.DEFAULT_BOLD);
        dTv.setTextColor(0xFFFFFFFF);
        dTv.setGravity(Gravity.CENTER);
        frame.addView(dTv, new FrameLayout.LayoutParams(w, h));
        return frame;
    }

    View makeCard() {
        FrameLayout card = new FrameLayout(this);
        card.setBackgroundDrawable(getDrawable(R.drawable.card_bg));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(14), 0, dp(14), dp(12));
        card.setLayoutParams(lp);
        return card;
    }

    View makePill(String big, String small) {
        LinearLayout pill = new LinearLayout(this);
        pill.setOrientation(LinearLayout.VERTICAL);
        pill.setGravity(Gravity.CENTER);
        pill.setBackgroundDrawable(getDrawable(R.drawable.card_bg));
        pill.setPadding(dp(10), dp(14), dp(10), dp(14));

        TextView bigTv = new TextView(this);
        bigTv.setText(big);
        bigTv.setTextSize(22);
        bigTv.setTypeface(Typeface.DEFAULT_BOLD);
        bigTv.setTextColor(0xFFFF6B35);
        bigTv.setGravity(Gravity.CENTER);
        pill.addView(bigTv, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView smallTv = new TextView(this);
        smallTv.setText(small);
        smallTv.setTextSize(10);
        smallTv.setTextColor(0xFF556677);
        smallTv.setGravity(Gravity.CENTER);
        pill.addView(smallTv, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return pill;
    }

    View makeFeatureRow(String title, String sub) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.setMargins(0, 0, 0, dp(12));
        row.setLayoutParams(rowLp);

        View dot = new View(this);
        dot.setBackgroundDrawable(getDrawable(R.drawable.dot_coral));
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(6), dp(6));
        dotLp.setMargins(0, 0, dp(12), 0);
        row.addView(dot, dotLp);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        TextView t1 = new TextView(this);
        t1.setText(title);
        t1.setTextSize(14);
        t1.setTypeface(Typeface.DEFAULT_BOLD);
        t1.setTextColor(0xFFEEF2F8);
        col.addView(t1, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView t2 = new TextView(this);
        t2.setText(sub);
        t2.setTextSize(12);
        t2.setTextColor(0xFF8896AA);
        col.addView(t2, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        row.addView(col);
        return row;
    }

    View makeStatCard(String val, String label) {
        FrameLayout card = new FrameLayout(this);
        card.setBackgroundDrawable(getDrawable(R.drawable.card_bg));
        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setGravity(Gravity.CENTER);
        inner.setPadding(dp(10), dp(14), dp(10), dp(14));

        TextView vt = new TextView(this);
        vt.setText(val);
        vt.setTextSize(18);
        vt.setTypeface(Typeface.DEFAULT_BOLD);
        vt.setTextColor(0xFFEEF2F8);
        vt.setGravity(Gravity.CENTER);
        inner.addView(vt, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView lt = new TextView(this);
        lt.setText(label);
        lt.setTextSize(10);
        lt.setTextColor(0xFF556677);
        lt.setGravity(Gravity.CENTER);
        inner.addView(lt, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        card.addView(inner);
        return card;
    }

    TextView makeNavBtn(String text) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextSize(13);
        btn.setTextColor(0xFF8896AA);
        btn.setGravity(Gravity.CENTER);
        btn.setBackgroundDrawable(getDrawable(R.drawable.btn_outline));
        return btn;
    }

    View makeWeekDayRow(int day, int currentDay, boolean[] completed, int maxUnlocked) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(18), dp(11), dp(18), dp(11));
        boolean isCurrent = (day == currentDay);
        if (isCurrent) row.setBackgroundColor(0x0AFF6B35);

        // Day number circle
        FrameLayout circle = new FrameLayout(this);
        circle.setBackgroundDrawable(getDrawable(
            completed[day] ? R.drawable.circle_green :
            isCurrent     ? R.drawable.circle_coral :
            day > maxUnlocked ? R.drawable.circle_muted : R.drawable.circle_muted));
        int circleSize = dp(34);
        TextView dayNum = new TextView(this);
        dayNum.setText(String.valueOf(day + 1));
        dayNum.setTextSize(12);
        dayNum.setTypeface(Typeface.DEFAULT_BOLD);
        dayNum.setTextColor(completed[day] ? 0xFF000000 : isCurrent ? 0xFF000000 : 0xFF4A6070);
        dayNum.setGravity(Gravity.CENTER);
        circle.addView(dayNum, new FrameLayout.LayoutParams(circleSize, circleSize));
        LinearLayout.LayoutParams circleLp = new LinearLayout.LayoutParams(circleSize, circleSize);
        circleLp.setMargins(0, 0, dp(14), 0);
        row.addView(circle, circleLp);

        // Title
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        TextView t1 = new TextView(this);
        t1.setText(getDaySesGrubu(day));
        t1.setTextSize(13);
        t1.setTypeface(Typeface.DEFAULT_BOLD);
        t1.setTextColor(completed[day] ? 0xFF4A6070 : isCurrent ? 0xFFEEF2F8 : 0xFF6A7A8A);
        col.addView(t1, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        String status = completed[day] ? "Tamamlandı" : isCurrent ? "Bugün" : day > maxUnlocked ? "Kilitli" : "Devam et";
        TextView t2 = new TextView(this);
        t2.setText(status);
        t2.setTextSize(11);
        t2.setTextColor(completed[day] ? 0xFF2ECC71 : isCurrent ? 0xFFFF6B35 : 0xFF4A6070);
        col.addView(t2, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        row.addView(col, colLp);

        // Tap to open
        final int d = day;
        final boolean isLocked = day > maxUnlocked;
        row.setOnClickListener(v -> {
            if (!isLocked) {
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt(KEY_CURRENT, d).apply();
                openLesson(d);
            }
        });

        // Divider at bottom
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.addView(row, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        View divider = new View(this);
        divider.setBackgroundColor(0xFF1D2A3A);
        wrapper.addView(divider, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        return wrapper;
    }

    // ── DATA HELPERS ────────────────────────────────────────────────────────────

    String getSetName(int day) {
        String[] sets = {"A1","A2","A3","B1","B2","B3","C1","C2","C3"};
        int wi = day / 7;
        return wi < sets.length ? sets[wi] : "C3";
    }

    String getDaySesGrubu(int day) {
        try {
            return programData.getJSONObject(day).getString("sesGrubu");
        } catch (Exception e) { return ""; }
    }

    String getDayOkumaBaslik(int day) {
        try {
            return programData.getJSONObject(day).getJSONObject("okuma").getString("baslik");
        } catch (Exception e) { return ""; }
    }

    // ── LAYOUT PARAM HELPERS ────────────────────────────────────────────────────

    LinearLayout.LayoutParams lp(int w, int h) {
        return new LinearLayout.LayoutParams(w, h);
    }

    int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
