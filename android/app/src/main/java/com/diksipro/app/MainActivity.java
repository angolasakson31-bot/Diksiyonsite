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

    static final String PREFS           = "diksipro";
    static final String KEY_STARTED     = "started";
    static final String KEY_CURRENT     = "currentDay";
    static final String KEY_COMPLETED   = "completed";
    static final String KEY_STREAK      = "streak";

    // Site palette
    static final int BG     = 0xFF080b10;
    static final int S1     = 0xFF0f1520;
    static final int S2     = 0xFF16202e;
    static final int S3     = 0xFF1e2d40;
    static final int GOLD   = 0xFFc8a84b;
    static final int TEXT   = 0xFFdce6f0;
    static final int SUB    = 0xFF8fa3b8;
    static final int MUTED  = 0xFF4a5d72;
    static final int GREEN  = 0xFF2e9e5e;
    static final int BORDER = 0xFF1e2d40;
    static final int CORAL  = 0xFFFF6B35;

    JSONArray programData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        loadData();
        SharedPreferences p = prefs();
        if (!p.getBoolean(KEY_STARTED, false)) showSetup();
        else showMain();
    }

    void loadData() {
        try {
            InputStream is = getAssets().open("program.json");
            byte[] buf = new byte[is.available()];
            is.read(buf);
            is.close();
            programData = new JSONArray(new String(buf, StandardCharsets.UTF_8));
        } catch (Exception e) { programData = new JSONArray(); }
    }

    SharedPreferences prefs() { return getSharedPreferences(PREFS, MODE_PRIVATE); }

    // ── SETUP SCREEN ──────────────────────────────────────────────────────────

    void showSetup() {
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(BG);
        sv.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(20), dp(60), dp(20), dp(40));

        // Brand
        TextView brand = new TextView(this);
        brand.setText("DiksiPro");
        brand.setTextSize(56);
        brand.setTypeface(Typeface.DEFAULT_BOLD);
        brand.setTextColor(TEXT);
        brand.setGravity(Gravity.CENTER);
        brand.setLetterSpacing(-0.04f);
        root.addView(brand, fullW());

        TextView brandSub = new TextView(this);
        brandSub.setText("PROFESYONEL SES EĞİTİMİ");
        brandSub.setTextSize(10);
        brandSub.setLetterSpacing(0.3f);
        brandSub.setTextColor(MUTED);
        brandSub.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subLp.setMargins(0, dp(8), 0, dp(48));
        root.addView(brandSub, subLp);

        // Setup card
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundDrawable(getDrawable(R.drawable.card_bg));
        card.setPadding(dp(22), dp(26), dp(22), dp(26));

        TextView h2 = new TextView(this);
        h2.setText("Hoş Geldin");
        h2.setTextSize(24);
        h2.setTypeface(Typeface.DEFAULT_BOLD);
        h2.setTextColor(TEXT);
        h2.setPadding(0, 0, 0, dp(8));
        card.addView(h2, fullW());

        TextView desc = new TextView(this);
        desc.setText("9 haftalık profesyonel diksiyon programı. Her gün yaklaşık 10 dakika. Kendi hızında ilerle.");
        desc.setTextSize(13);
        desc.setTextColor(SUB);
        desc.setLineSpacing(dp(4), 1f);
        LinearLayout.LayoutParams descLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descLp.setMargins(0, 0, 0, dp(24));
        card.addView(desc, descLp);

        // Pills
        LinearLayout pills = new LinearLayout(this);
        pills.setOrientation(LinearLayout.HORIZONTAL);
        pills.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams pillAreaLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pillAreaLp.setMargins(0, 0, 0, dp(24));
        pills.setLayoutParams(pillAreaLp);

        LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        pLp.setMargins(0, 0, dp(8), 0);
        pills.addView(pill("9", "Hafta"), pLp);
        pills.addView(pill("63", "Gün"), pLp);
        LinearLayout.LayoutParams pLp3 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        pills.addView(pill("~10dk", "Günlük"), pLp3);
        card.addView(pills);

        // Start button
        TextView startBtn = new TextView(this);
        startBtn.setText("Programa Başla →");
        startBtn.setTextSize(15);
        startBtn.setTypeface(Typeface.DEFAULT_BOLD);
        startBtn.setTextColor(0xFF000000);
        startBtn.setGravity(Gravity.CENTER);
        startBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_gold));
        startBtn.setOnClickListener(v -> {
            prefs().edit()
                .putBoolean(KEY_STARTED, true)
                .putInt(KEY_CURRENT, 0)
                .putString(KEY_COMPLETED, "")
                .putInt(KEY_STREAK, 0)
                .apply();
            showMain();
        });
        card.addView(startBtn, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));

        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        card.setLayoutParams(cardLp);
        root.addView(card);

        sv.addView(root);
        setContentView(sv);
    }

    // ── MAIN SCREEN ───────────────────────────────────────────────────────────

    void showMain() {
        SharedPreferences p = prefs();
        int cur     = p.getInt(KEY_CURRENT, 0);
        int streak  = p.getInt(KEY_STREAK, 0);
        boolean[] done = parseDone(p.getString(KEY_COMPLETED, ""));
        int maxUnlocked = calcMaxUnlocked(done);

        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, 0, 0, dp(20));

        // ── Header
        root.addView(buildHeader(cur, streak, done));

        // ── Nav bar
        root.addView(buildNav(cur, maxUnlocked, done));

        // ── Content
        root.addView(buildDayContent(cur, maxUnlocked, done));

        // ── Done bar (sticky)
        root.addView(buildDoneButton(cur, maxUnlocked, done));

        sv.addView(root);
        setContentView(sv);
    }

    View buildHeader(int cur, int streak, boolean[] done) {
        LinearLayout hdr = new LinearLayout(this);
        hdr.setOrientation(LinearLayout.VERTICAL);
        hdr.setBackgroundColor(S1);

        // Row 1: logo + streak
        LinearLayout r1 = new LinearLayout(this);
        r1.setOrientation(LinearLayout.HORIZONTAL);
        r1.setGravity(Gravity.CENTER_VERTICAL);
        r1.setPadding(dp(18), dp(14), dp(18), dp(12));

        TextView logo = new TextView(this);
        logo.setText("DiksiPro");
        logo.setTextSize(22);
        logo.setTypeface(Typeface.DEFAULT_BOLD);
        logo.setTextColor(TEXT);
        r1.addView(logo, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        // Streak badge
        LinearLayout badge = new LinearLayout(this);
        badge.setOrientation(LinearLayout.HORIZONTAL);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(10), dp(5), dp(10), dp(5));
        badge.setBackgroundDrawable(getDrawable(streak > 0 ? R.drawable.badge_hot : R.drawable.badge_normal));
        TextView badgeTv = new TextView(this);
        badgeTv.setText((streak > 0 ? "🔥 " : "") + streak + " gün");
        badgeTv.setTextSize(13);
        badgeTv.setTypeface(Typeface.DEFAULT_BOLD);
        badgeTv.setTextColor(streak > 0 ? GOLD : SUB);
        badge.addView(badgeTv);
        r1.addView(badge);
        hdr.addView(r1, fullW());

        // Row 2: set label + week dots
        LinearLayout r2 = new LinearLayout(this);
        r2.setOrientation(LinearLayout.HORIZONTAL);
        r2.setGravity(Gravity.CENTER_VERTICAL);
        r2.setPadding(dp(18), 0, dp(18), dp(12));

        TextView setLbl = new TextView(this);
        String setName = getSetName(cur);
        int setWeek = (cur / 7) % 3 + 1;
        setLbl.setText("Set " + setName.charAt(0) + " · " + setWeek + ". Hafta");
        setLbl.setTextSize(10);
        setLbl.setLetterSpacing(0.18f);
        setLbl.setTextColor(MUTED);
        r2.addView(setLbl, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        // Week dots
        LinearLayout dotsRow = new LinearLayout(this);
        dotsRow.setOrientation(LinearLayout.HORIZONTAL);
        dotsRow.setGravity(Gravity.CENTER_VERTICAL);
        int weekStart = (cur / 7) * 7;
        for (int d = weekStart; d < weekStart + 7 && d <= 62; d++) {
            View dot = new View(this);
            int sz = (d == cur) ? dp(10) : dp(7);
            LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(sz, sz);
            dLp.setMargins(dp(3), 0, dp(3), 0);
            dot.setLayoutParams(dLp);
            if (done[d]) dot.setBackgroundDrawable(getDrawable(R.drawable.dot_green));
            else if (d == cur) dot.setBackgroundDrawable(getDrawable(R.drawable.dot_gold));
            else dot.setBackgroundDrawable(getDrawable(R.drawable.dot_muted));
            dotsRow.addView(dot);
        }
        r2.addView(dotsRow);
        hdr.addView(r2, fullW());

        // Border
        View border = new View(this);
        border.setBackgroundColor(BORDER);
        hdr.addView(border, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        return hdr;
    }

    View buildNav(int cur, int maxUnlocked, boolean[] done) {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        nav.setBackgroundColor(S2);
        nav.setPadding(dp(16), dp(10), dp(16), dp(10));

        // Prev button
        TextView prev = new TextView(this);
        prev.setText("← Önceki");
        prev.setTextSize(13);
        prev.setTextColor(cur == 0 ? MUTED : SUB);
        prev.setPadding(dp(12), dp(7), dp(12), dp(7));
        prev.setBackgroundDrawable(getDrawable(cur == 0 ? R.drawable.btn_nav_disabled : R.drawable.btn_nav));
        prev.setAlpha(cur == 0 ? 0.4f : 1f);
        prev.setOnClickListener(v -> {
            if (cur > 0) {
                prefs().edit().putInt(KEY_CURRENT, cur - 1).apply();
                showMain();
            }
        });
        nav.addView(prev, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout mid = new LinearLayout(this);
        mid.setOrientation(LinearLayout.VERTICAL);
        mid.setGravity(Gravity.CENTER);
        mid.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView dayN = new TextView(this);
        dayN.setText("Gün " + (cur + 1));
        dayN.setTextSize(15);
        dayN.setTypeface(Typeface.DEFAULT_BOLD);
        dayN.setTextColor(TEXT);
        dayN.setGravity(Gravity.CENTER);
        mid.addView(dayN, fullW());

        boolean isDone = done[cur];
        boolean isLocked = cur > maxUnlocked;
        String subText = isDone ? "Tamamlandı" : (cur == maxUnlocked && !isDone) ? "Bugünün egzersizi" : isLocked ? "Kilitli" : "Geçmiş gün";
        TextView subN = new TextView(this);
        subN.setText(subText);
        subN.setTextSize(11);
        subN.setTextColor(MUTED);
        subN.setGravity(Gravity.CENTER);
        mid.addView(subN, fullW());
        nav.addView(mid);

        // Next button
        boolean canNext = cur < maxUnlocked;
        TextView next = new TextView(this);
        next.setText("Sonraki →");
        next.setTextSize(13);
        next.setTextColor(canNext ? SUB : MUTED);
        next.setPadding(dp(12), dp(7), dp(12), dp(7));
        next.setBackgroundDrawable(getDrawable(canNext ? R.drawable.btn_nav : R.drawable.btn_nav_disabled));
        next.setAlpha(canNext ? 1f : 0.4f);
        next.setOnClickListener(v -> {
            if (canNext) {
                prefs().edit().putInt(KEY_CURRENT, cur + 1).apply();
                showMain();
            }
        });
        nav.addView(next, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setBackgroundColor(S2);
        wrapper.addView(nav, fullW());
        View border = new View(this);
        border.setBackgroundColor(BORDER);
        wrapper.addView(border, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        return wrapper;
    }

    View buildDayContent(int cur, int maxUnlocked, boolean[] done) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(14), dp(16), dp(14), dp(8));

        // Done banner
        if (done[cur]) {
            TextView banner = new TextView(this);
            banner.setText("✓ Bu günü tamamladın");
            banner.setTextSize(13);
            banner.setTypeface(Typeface.DEFAULT_BOLD);
            banner.setTextColor(GREEN);
            banner.setGravity(Gravity.CENTER);
            banner.setBackgroundDrawable(getDrawable(R.drawable.banner_done));
            banner.setPadding(dp(14), dp(10), dp(14), dp(10));
            LinearLayout.LayoutParams bannerLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            bannerLp.setMargins(0, 0, 0, dp(14));
            content.addView(banner, bannerLp);
        }

        // Day summary card
        LinearLayout sumCard = new LinearLayout(this);
        sumCard.setOrientation(LinearLayout.VERTICAL);
        sumCard.setBackgroundDrawable(getDrawable(R.drawable.card_bg));
        sumCard.setPadding(dp(16), dp(18), dp(16), dp(18));
        LinearLayout.LayoutParams scLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        scLp.setMargins(0, 0, 0, dp(14));
        sumCard.setLayoutParams(scLp);

        // Ses grubu
        try {
            String sesGrubu = programData.getJSONObject(cur).getString("sesGrubu");
            TextView sgTv = new TextView(this);
            sgTv.setText(sesGrubu);
            sgTv.setTextSize(12);
            sgTv.setTypeface(Typeface.DEFAULT_BOLD);
            sgTv.setTextColor(GOLD);
            sgTv.setPadding(0, 0, 0, dp(6));
            sumCard.addView(sgTv, fullW());

            String okumaBaslik = programData.getJSONObject(cur)
                .getJSONObject("okuma").getString("baslik");
            TextView okTv = new TextView(this);
            okTv.setText("Okuma: " + okumaBaslik);
            okTv.setTextSize(13);
            okTv.setTextColor(SUB);
            okTv.setPadding(0, 0, 0, dp(16));
            sumCard.addView(okTv, fullW());
        } catch (Exception ignored) {}

        // Progress bar
        int totalDone = 0;
        for (boolean b : done) if (b) totalDone++;
        int pct = (int)(totalDone / 63.0 * 100);

        LinearLayout progRow = new LinearLayout(this);
        progRow.setOrientation(LinearLayout.HORIZONTAL);
        progRow.setGravity(Gravity.CENTER_VERTICAL);
        progRow.setPadding(0, 0, 0, dp(16));

        FrameLayout track = new FrameLayout(this);
        track.setBackgroundDrawable(getDrawable(R.drawable.progress_track));
        LinearLayout.LayoutParams trackLp = new LinearLayout.LayoutParams(0, dp(5), 1f);
        track.setLayoutParams(trackLp);
        View fill = new View(this);
        fill.setBackgroundDrawable(getDrawable(R.drawable.progress_fill));
        track.addView(fill, new FrameLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT));
        track.post(() -> {
            int w = track.getWidth();
            fill.setLayoutParams(new FrameLayout.LayoutParams((int)(w * pct / 100.0), ViewGroup.LayoutParams.MATCH_PARENT));
        });
        progRow.addView(track, trackLp);

        TextView pctTv = new TextView(this);
        pctTv.setText(pct + "%");
        pctTv.setTextSize(11);
        pctTv.setTypeface(Typeface.DEFAULT_BOLD);
        pctTv.setTextColor(GOLD);
        LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pLp.setMargins(dp(10), 0, 0, 0);
        progRow.addView(pctTv, pLp);
        sumCard.addView(progRow, fullW());

        // Action button
        boolean isLocked = cur > maxUnlocked;
        boolean isDone   = done[cur];
        TextView actionBtn = new TextView(this);
        if (isDone) {
            actionBtn.setText("✓  Tamamlandı");
            actionBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_done_card));
            actionBtn.setTextColor(GREEN);
        } else if (isLocked) {
            actionBtn.setText("🔒  Önceki günü tamamla");
            actionBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_locked_card));
            actionBtn.setTextColor(MUTED);
        } else {
            actionBtn.setText("Derse Başla →");
            actionBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_gold));
            actionBtn.setTextColor(0xFF000000);
        }
        actionBtn.setTextSize(15);
        actionBtn.setTypeface(Typeface.DEFAULT_BOLD);
        actionBtn.setGravity(Gravity.CENTER);
        final int fCur = cur;
        actionBtn.setOnClickListener(v -> {
            if (!isLocked) openLesson(fCur);
        });
        sumCard.addView(actionBtn, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50)));
        content.addView(sumCard);

        // Stats row
        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams stLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stLp.setMargins(0, 0, 0, dp(16));
        stats.setLayoutParams(stLp);

        int streak = prefs().getInt(KEY_STREAK, 0);
        LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        sLp.setMargins(0, 0, dp(8), 0);
        LinearLayout.LayoutParams sLp2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        sLp2.setMargins(0, 0, dp(8), 0);
        LinearLayout.LayoutParams sLp3 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        stats.addView(statCard(String.valueOf(totalDone), "Tamamlanan"), sLp);
        stats.addView(statCard(String.valueOf(63 - totalDone), "Kalan"), sLp2);
        stats.addView(statCard(streak + " gün", "Seri"), sLp3);
        content.addView(stats);

        // Week list
        int weekStart = (cur / 7) * 7;
        LinearLayout weekCard = new LinearLayout(this);
        weekCard.setOrientation(LinearLayout.VERTICAL);
        weekCard.setBackgroundDrawable(getDrawable(R.drawable.card_bg));
        LinearLayout.LayoutParams wcLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        weekCard.setLayoutParams(wcLp);

        // Week header
        LinearLayout weekHdr = new LinearLayout(this);
        weekHdr.setOrientation(LinearLayout.HORIZONTAL);
        weekHdr.setGravity(Gravity.CENTER_VERTICAL);
        weekHdr.setPadding(dp(16), dp(12), dp(16), dp(12));
        TextView weekLbl = new TextView(this);
        weekLbl.setText("BU HAFTA");
        weekLbl.setTextSize(9);
        weekLbl.setLetterSpacing(0.22f);
        weekLbl.setTextColor(MUTED);
        weekHdr.addView(weekLbl, fullW());
        weekCard.addView(weekHdr, fullW());
        View hdrBorder = new View(this);
        hdrBorder.setBackgroundColor(BORDER);
        weekCard.addView(hdrBorder, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        for (int d = weekStart; d < weekStart + 7 && d <= 62; d++) {
            weekCard.addView(buildWeekRow(d, cur, done, maxUnlocked));
        }
        content.addView(weekCard);

        return content;
    }

    View buildWeekRow(int day, int cur, boolean[] done, int maxUnlocked) {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        if (day == cur) wrapper.setBackgroundColor(0x0AC8A84B);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(12), dp(16), dp(12));

        // Day circle
        FrameLayout circle = new FrameLayout(this);
        int sz = dp(34);
        circle.setBackgroundDrawable(getDrawable(
            done[day] ? R.drawable.circle_green :
            day == cur ? R.drawable.circle_gold :
            R.drawable.circle_muted));
        TextView dayNum = new TextView(this);
        dayNum.setText(String.valueOf(day + 1));
        dayNum.setTextSize(12);
        dayNum.setTypeface(Typeface.DEFAULT_BOLD);
        dayNum.setGravity(Gravity.CENTER);
        dayNum.setTextColor(done[day] ? 0xFF000000 : day == cur ? 0xFF000000 : MUTED);
        circle.addView(dayNum, new FrameLayout.LayoutParams(sz, sz));
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(sz, sz);
        cLp.setMargins(0, 0, dp(14), 0);
        row.addView(circle, cLp);

        // Text column
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        String sesGrubu = "";
        try { sesGrubu = programData.getJSONObject(day).getString("sesGrubu"); } catch (Exception ignored) {}
        TextView t1 = new TextView(this);
        t1.setText(sesGrubu);
        t1.setTextSize(13);
        t1.setTypeface(Typeface.DEFAULT_BOLD);
        t1.setTextColor(done[day] ? MUTED : day == cur ? TEXT : SUB);
        col.addView(t1, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        String status = done[day] ? "Tamamlandı" : day == cur ? "Bugün" : day > maxUnlocked ? "Kilitli" : "Devam et";
        TextView t2 = new TextView(this);
        t2.setText(status);
        t2.setTextSize(11);
        t2.setTextColor(done[day] ? GREEN : day == cur ? GOLD : MUTED);
        col.addView(t2, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        row.addView(col);

        final int d = day;
        final boolean locked = day > maxUnlocked;
        row.setOnClickListener(v -> {
            if (!locked) {
                prefs().edit().putInt(KEY_CURRENT, d).apply();
                openLesson(d);
            }
        });
        wrapper.addView(row, fullW());
        View border = new View(this);
        border.setBackgroundColor(BORDER);
        wrapper.addView(border, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        return wrapper;
    }

    View buildDoneButton(int cur, int maxUnlocked, boolean[] done) {
        boolean isDone   = done[cur];
        boolean isLocked = cur > maxUnlocked;
        TextView btn = new TextView(this);
        if (isDone) {
            btn.setText("✓ Tamamlandı");
            btn.setBackgroundDrawable(getDrawable(R.drawable.btn_done_card));
            btn.setTextColor(GREEN);
        } else if (isLocked) {
            btn.setText("🔒 Önceki günü tamamla");
            btn.setBackgroundDrawable(getDrawable(R.drawable.btn_locked_card));
            btn.setTextColor(MUTED);
        } else {
            btn.setText("✓ Bugünü Tamamladım");
            btn.setBackgroundDrawable(getDrawable(R.drawable.btn_gold));
            btn.setTextColor(0xFF000000);
        }
        btn.setTextSize(15);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setGravity(Gravity.CENTER);
        final int fCur = cur;
        btn.setOnClickListener(v -> {
            if (!isDone && !isLocked) {
                markDone(fCur, done);
                showMain();
            }
        });
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54));
        btnLp.setMargins(dp(14), dp(8), dp(14), dp(14));
        btn.setLayoutParams(btnLp);
        return btn;
    }

    // ── MARK DONE ─────────────────────────────────────────────────────────────

    void markDone(int day, boolean[] done) {
        done[day] = true;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 63; i++) {
            if (done[i]) { if (sb.length() > 0) sb.append(","); sb.append(i); }
        }
        int streak = 0;
        for (int i = 62; i >= 0; i--) {
            if (done[i]) streak++;
            else break;
        }
        // Find streak from end of completed set
        int streakCount = 0;
        java.util.List<Integer> list = new java.util.ArrayList<>();
        for (int i = 0; i < 63; i++) if (done[i]) list.add(i);
        for (int i = list.size() - 1; i >= 0; i--) {
            if (i == list.size() - 1 || list.get(i) == list.get(i + 1) - 1) streakCount++;
            else break;
        }
        prefs().edit()
            .putString(KEY_COMPLETED, sb.toString())
            .putInt(KEY_STREAK, streakCount)
            .apply();
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

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
                try { done[Integer.parseInt(tok)] = true; } catch (Exception ignored) {}
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

    // ── VIEW FACTORIES ────────────────────────────────────────────────────────

    View pill(String big, String small) {
        LinearLayout p = new LinearLayout(this);
        p.setOrientation(LinearLayout.VERTICAL);
        p.setGravity(Gravity.CENTER);
        p.setBackgroundDrawable(getDrawable(R.drawable.card_s2));
        p.setPadding(dp(10), dp(12), dp(10), dp(12));

        TextView bt = new TextView(this);
        bt.setText(big);
        bt.setTextSize(22);
        bt.setTypeface(Typeface.DEFAULT_BOLD);
        bt.setTextColor(GOLD);
        bt.setGravity(Gravity.CENTER);
        p.addView(bt, fullW());

        TextView st = new TextView(this);
        st.setText(small);
        st.setTextSize(10);
        st.setTextColor(MUTED);
        st.setGravity(Gravity.CENTER);
        p.addView(st, fullW());
        return p;
    }

    View statCard(String val, String label) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setGravity(Gravity.CENTER);
        c.setBackgroundDrawable(getDrawable(R.drawable.card_bg));
        c.setPadding(dp(10), dp(14), dp(10), dp(14));

        TextView vt = new TextView(this);
        vt.setText(val);
        vt.setTextSize(18);
        vt.setTypeface(Typeface.DEFAULT_BOLD);
        vt.setTextColor(TEXT);
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

    LinearLayout.LayoutParams fullW() {
        return new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
