package com.diksipro.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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

    // Website palette — dark navy + gold
    static final int BG     = 0xFF080B10;
    static final int S1     = 0xFF0F1520;
    static final int S2     = 0xFF16202E;
    static final int BORDER = 0xFF1E2D40;
    static final int GOLD   = 0xFFC8A84B;
    static final int TEXT   = 0xFFDCE6F0;
    static final int SUB    = 0xFF8FA3B8;
    static final int MUTED  = 0xFF4A5D72;
    static final int GREEN  = 0xFF2E9E5E;

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
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(32), dp(80), dp(32), dp(56));

        // Logo mark — D glyph via text approximation
        LinearLayout logoRow = new LinearLayout(this);
        logoRow.setOrientation(LinearLayout.HORIZONTAL);
        logoRow.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lrLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lrLp.setMargins(0, 0, 0, dp(4));
        logoRow.setLayoutParams(lrLp);

        // Bar
        View bar = new View(this);
        GradientDrawable barGd = rd(dp(6)); barGd.setColor(GOLD);
        bar.setBackgroundDrawable(barGd);
        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(dp(9), dp(52));
        barLp.setMargins(0, 0, dp(2), 0);
        logoRow.addView(bar, barLp);

        // Three arcs at descending opacity
        int[] alphas = {0xFF, 0x80, 0x30};
        for (int i = 0; i < 3; i++) {
            TextView a = new TextView(this);
            a.setText(")");
            a.setTextSize(34);
            a.setTypeface(Typeface.DEFAULT_BOLD);
            a.setTextColor((alphas[i] << 24) | (GOLD & 0x00FFFFFF));
            LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            aLp.setMargins(i == 0 ? 0 : -dp(7), 0, 0, 0);
            logoRow.addView(a, aLp);
        }
        root.addView(logoRow);

        // Brand
        TextView brand = new TextView(this);
        brand.setText("DiksiPro");
        brand.setTextSize(48);
        brand.setTypeface(Typeface.DEFAULT_BOLD);
        brand.setTextColor(TEXT);
        brand.setGravity(Gravity.CENTER);
        brand.setLetterSpacing(-0.03f);
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bLp.setMargins(0, 0, 0, dp(10));
        root.addView(brand, bLp);

        TextView tagline = new TextView(this);
        tagline.setText("PROFESYONEL SES EĞİTİMİ");
        tagline.setTextSize(9);
        tagline.setLetterSpacing(0.30f);
        tagline.setTextColor(MUTED);
        tagline.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tLp.setMargins(0, 0, 0, dp(64));
        root.addView(tagline, tLp);

        // Stats row — three numbers, minimal
        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams stLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stLp.setMargins(0, 0, 0, dp(52));
        stats.setLayoutParams(stLp);

        String[][] sv = {{"9", "hafta"}, {"63", "gün"}, {"~10dk", "günlük"}};
        for (int i = 0; i < 3; i++) {
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            stats.addView(setupStat(sv[i][0], sv[i][1]), sp);
        }
        root.addView(stats);

        // CTA
        TextView cta = new TextView(this);
        cta.setText("Programa Başla");
        cta.setTextSize(16);
        cta.setTypeface(Typeface.DEFAULT_BOLD);
        cta.setTextColor(0xFF080B10);
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
        root.addView(cta, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(58)));

        setContentView(root);
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
        content.setPadding(0, 0, 0, dp(80));

        content.addView(buildAppBar(streak));
        content.addView(buildHero(cur, done, maxUnlocked));
        content.addView(buildStatsRow(done, streak));
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

    // ── APP BAR ────────────────────────────────────────────────────────────────

    View buildAppBar(int streak) {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(S1);
        bar.setPadding(dp(22), dp(16), dp(22), dp(16));

        TextView logo = new TextView(this);
        logo.setText("DiksiPro");
        logo.setTextSize(19);
        logo.setTypeface(Typeface.DEFAULT_BOLD);
        logo.setTextColor(TEXT);
        logo.setLetterSpacing(-0.02f);
        bar.addView(logo, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        // Streak: minimal text, no heavy badge
        TextView streakTv = new TextView(this);
        streakTv.setText((streak > 2 ? "🔥 " : "") + streak + " gün");
        streakTv.setTextSize(13);
        streakTv.setTypeface(streak > 2 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        streakTv.setTextColor(streak > 2 ? GOLD : MUTED);
        bar.addView(streakTv);

        LinearLayout w = new LinearLayout(this);
        w.setOrientation(LinearLayout.VERTICAL);
        w.setBackgroundColor(S1);
        w.addView(bar, fullW());
        View div = new View(this); div.setBackgroundColor(BORDER);
        w.addView(div, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        return w;
    }

    // ── HERO ───────────────────────────────────────────────────────────────────

    View buildHero(int cur, boolean[] done, int maxUnlocked) {
        boolean isDone   = done[cur];
        boolean isLocked = cur > maxUnlocked;

        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(16), dp(22), dp(16), dp(6));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundDrawable(getDrawable(R.drawable.card_bg));
        card.setPadding(dp(24), dp(26), dp(24), dp(24));

        // Set chip — tiny, gold outline
        String set = getSetName(cur);
        int setWeek = (cur / 7) % 3 + 1;
        TextView setChip = new TextView(this);
        setChip.setText("Set " + set + "  ·  " + setWeek + ". Hafta");
        setChip.setTextSize(10);
        setChip.setLetterSpacing(0.18f);
        setChip.setTypeface(Typeface.DEFAULT_BOLD);
        setChip.setTextColor(GOLD);
        setChip.setPadding(dp(10), dp(5), dp(10), dp(5));
        GradientDrawable chipGd = rd(dp(20));
        chipGd.setColor(0x14C8A84B);
        chipGd.setStroke(1, 0x38C8A84B);
        setChip.setBackgroundDrawable(chipGd);
        LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        chipLp.setMargins(0, 0, 0, dp(20));
        card.addView(setChip, chipLp);

        // Giant day number
        TextView dayTv = new TextView(this);
        dayTv.setText("GÜN " + (cur + 1));
        dayTv.setTextSize(58);
        dayTv.setTypeface(Typeface.DEFAULT_BOLD);
        dayTv.setTextColor(isDone ? MUTED : TEXT);
        dayTv.setLetterSpacing(-0.04f);
        LinearLayout.LayoutParams dayLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dayLp.setMargins(0, 0, 0, dp(4));
        card.addView(dayTv, dayLp);

        // Ses grubu
        String sg = "";
        try { sg = programData.getJSONObject(cur).getString("sesGrubu"); } catch (Exception ignored) {}
        if (!sg.isEmpty()) {
            TextView sgTv = new TextView(this);
            sgTv.setText(sg);
            sgTv.setTextSize(17);
            sgTv.setTextColor(isDone ? MUTED : GOLD);
            LinearLayout.LayoutParams sgLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            sgLp.setMargins(0, 0, 0, dp(28));
            card.addView(sgTv, sgLp);
        }

        // Progress bar + percent
        int totalDone = 0;
        for (boolean b : done) if (b) totalDone++;
        int pct = (int)(totalDone / 63.0 * 100);

        LinearLayout progRow = new LinearLayout(this);
        progRow.setOrientation(LinearLayout.HORIZONTAL);
        progRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams prLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        prLp.setMargins(0, 0, 0, dp(24));

        FrameLayout track = new FrameLayout(this);
        track.setBackgroundDrawable(getDrawable(R.drawable.progress_track));
        LinearLayout.LayoutParams trkLp = new LinearLayout.LayoutParams(0, dp(4), 1f);
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
        pctTv.setTextSize(11);
        pctTv.setTypeface(Typeface.DEFAULT_BOLD);
        pctTv.setTextColor(GOLD);
        LinearLayout.LayoutParams pctLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pctLp.setMargins(dp(12), 0, 0, 0);
        progRow.addView(pctTv, pctLp);
        card.addView(progRow, prLp);

        // Action button
        final int fCur = cur;
        if (isLocked) {
            TextView lock = new TextView(this);
            lock.setText("🔒  Önceki günü tamamla");
            lock.setTextSize(14);
            lock.setTypeface(Typeface.DEFAULT_BOLD);
            lock.setTextColor(MUTED);
            lock.setGravity(Gravity.CENTER);
            lock.setBackgroundDrawable(getDrawable(R.drawable.btn_locked_card));
            lock.setAlpha(0.65f);
            card.addView(lock, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)));
        } else if (isDone) {
            LinearLayout doneRow = new LinearLayout(this);
            doneRow.setOrientation(LinearLayout.HORIZONTAL);
            doneRow.setGravity(Gravity.CENTER_VERTICAL);

            TextView checkTv = new TextView(this);
            checkTv.setText("✓  Tamamlandı");
            checkTv.setTextSize(14);
            checkTv.setTypeface(Typeface.DEFAULT_BOLD);
            checkTv.setTextColor(GREEN);
            doneRow.addView(checkTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView replayBtn = new TextView(this);
            replayBtn.setText("Tekrar aç →");
            replayBtn.setTextSize(13);
            replayBtn.setTypeface(Typeface.DEFAULT_BOLD);
            replayBtn.setTextColor(GOLD);
            replayBtn.setPadding(dp(16), dp(10), dp(16), dp(10));
            GradientDrawable rpGd = rd(dp(12)); rpGd.setColor(0x10C8A84B); rpGd.setStroke(1, 0x30C8A84B);
            replayBtn.setBackgroundDrawable(rpGd);
            replayBtn.setOnClickListener(v -> openLesson(fCur));
            doneRow.addView(replayBtn);
            card.addView(doneRow, fullW());
        } else {
            TextView startBtn = new TextView(this);
            startBtn.setText("Derse Başla  →");
            startBtn.setTextSize(17);
            startBtn.setTypeface(Typeface.DEFAULT_BOLD);
            startBtn.setTextColor(0xFF080B10);
            startBtn.setGravity(Gravity.CENTER);
            startBtn.setBackgroundDrawable(getDrawable(R.drawable.btn_gold));
            startBtn.setOnClickListener(v -> openLesson(fCur));
            card.addView(startBtn, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(58)));
        }

        wrap.addView(card, fullW());
        return wrap;
    }

    // ── STATS ROW ──────────────────────────────────────────────────────────────

    View buildStatsRow(boolean[] done, int streak) {
        int totalDone = 0;
        for (boolean b : done) if (b) totalDone++;

        // Minimal — three numbers directly on bg, no card wrappers
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rLp.setMargins(dp(16), dp(4), dp(16), dp(24));
        row.setLayoutParams(rLp);

        row.addView(statNum("" + totalDone, "tamamlanan", GREEN), weight1());
        row.addView(statDivider());
        row.addView(statNum("" + (63 - totalDone), "kalan gün", MUTED), weight1());
        row.addView(statDivider());
        row.addView(statNum(streak + " gün", "seri", streak > 2 ? GOLD : MUTED), weight1());
        return row;
    }

    View statDivider() {
        View v = new View(this);
        v.setBackgroundColor(BORDER);
        return v;
    }

    // ── WEEK SECTION ───────────────────────────────────────────────────────────

    View buildWeekSection(int cur, boolean[] done, int maxUnlocked) {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(dp(16), 0, dp(16), dp(8));

        // Section label
        LinearLayout hdr = new LinearLayout(this);
        hdr.setOrientation(LinearLayout.HORIZONTAL);
        hdr.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hLp.setMargins(0, 0, 0, dp(14));
        hdr.setLayoutParams(hLp);

        TextView lbl = new TextView(this);
        lbl.setText("BU HAFTA");
        lbl.setTextSize(10);
        lbl.setLetterSpacing(0.22f);
        lbl.setTypeface(Typeface.DEFAULT_BOLD);
        lbl.setTextColor(MUTED);
        hdr.addView(lbl);

        View line = new View(this); line.setBackgroundColor(BORDER);
        LinearLayout.LayoutParams llLp = new LinearLayout.LayoutParams(0, 1, 1f);
        llLp.setMargins(dp(12), 0, 0, 0);
        hdr.addView(line, llLp);
        section.addView(hdr);

        // Week rows in a card
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundDrawable(getDrawable(R.drawable.card_bg));

        int weekStart = (cur / 7) * 7;
        for (int d = weekStart; d < weekStart + 7 && d <= 62; d++) {
            card.addView(buildWeekRow(d, cur, done, maxUnlocked));
            if (d < Math.min(weekStart + 6, 62)) {
                View div = new View(this); div.setBackgroundColor(BORDER);
                card.addView(div, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 1));
            }
        }
        section.addView(card, fullW());
        return section;
    }

    View buildWeekRow(int day, int cur, boolean[] done, int maxUnlocked) {
        boolean isDone   = done[day];
        boolean isToday  = day == cur;
        boolean isLocked = day > maxUnlocked;

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(18), dp(15), dp(18), dp(15));
        if (isToday) row.setBackgroundColor(0x0DC8A84B);

        // Circle with day number
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
        numTv.setTextColor(isDone || isToday ? 0xFF080B10 : MUTED);
        numTv.setGravity(Gravity.CENTER);
        circ.addView(numTv, new FrameLayout.LayoutParams(csz, csz));
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(csz, csz);
        cLp.setMargins(0, 0, dp(14), 0);
        row.addView(circ, cLp);

        // Text column
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        String sesGrubu = "";
        try { sesGrubu = programData.getJSONObject(day).getString("sesGrubu"); } catch (Exception ignored) {}
        TextView t1 = new TextView(this);
        t1.setText(sesGrubu);
        t1.setTextSize(13);
        t1.setTypeface(isToday ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        t1.setTextColor(isDone ? SUB : isToday ? TEXT : SUB);
        col.addView(t1);

        String status = isDone ? "Tamamlandı" :
            isToday ? "Devam et" :
            isLocked ? "Kilitli" : "Başlanmadı";
        TextView t2 = new TextView(this);
        t2.setText(status);
        t2.setTextSize(11);
        t2.setTextColor(isDone ? GREEN : isToday ? GOLD : MUTED);
        col.addView(t2);
        row.addView(col);

        // Right indicator
        TextView ind = new TextView(this);
        ind.setText(isDone ? "✓" : isLocked ? "🔒" : "›");
        ind.setTextSize(isLocked ? 11 : 18);
        ind.setTextColor(isDone ? GREEN : MUTED);
        row.addView(ind);

        final int d = day;
        row.setOnClickListener(v -> {
            if (!isLocked) { prefs().edit().putInt(KEY_CURRENT, d).apply(); openLesson(d); }
        });
        return row;
    }

    // ── BOTTOM NAV ─────────────────────────────────────────────────────────────

    View buildBottomNav(int cur, int maxUnlocked) {
        boolean canPrev = cur > 0;
        boolean canNext = cur < maxUnlocked;

        LinearLayout w = new LinearLayout(this);
        w.setOrientation(LinearLayout.VERTICAL);
        w.setBackgroundColor(S1);

        View topDiv = new View(this); topDiv.setBackgroundColor(BORDER);
        w.addView(topDiv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        nav.setPadding(dp(16), dp(13), dp(16), dp(28));

        TextView prev = navBtn("← Önceki", canPrev);
        prev.setOnClickListener(v -> {
            if (canPrev) { prefs().edit().putInt(KEY_CURRENT, cur - 1).apply(); showMain(); }
        });

        TextView mid = new TextView(this);
        mid.setText("Gün " + (cur + 1) + " / 63");
        mid.setTextSize(12);
        mid.setTextColor(MUTED);
        mid.setGravity(Gravity.CENTER);

        TextView next = navBtn("Sonraki →", canNext);
        next.setOnClickListener(v -> {
            if (canNext) { prefs().edit().putInt(KEY_CURRENT, cur + 1).apply(); showMain(); }
        });

        nav.addView(prev);
        nav.addView(mid, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        nav.addView(next);
        w.addView(nav, fullW());
        return w;
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

    // ── VIEW HELPERS ───────────────────────────────────────────────────────────

    View setupStat(String num, String label) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setGravity(Gravity.CENTER);
        c.setBackgroundDrawable(getDrawable(R.drawable.card_s2));
        c.setPadding(dp(8), dp(18), dp(8), dp(18));

        TextView nv = new TextView(this);
        nv.setText(num);
        nv.setTextSize(28);
        nv.setTypeface(Typeface.DEFAULT_BOLD);
        nv.setTextColor(GOLD);
        nv.setGravity(Gravity.CENTER);
        c.addView(nv, fullW());

        TextView lv = new TextView(this);
        lv.setText(label.toUpperCase());
        lv.setTextSize(9);
        lv.setLetterSpacing(0.18f);
        lv.setTextColor(MUTED);
        lv.setGravity(Gravity.CENTER);
        c.addView(lv, fullW());
        return c;
    }

    View statNum(String val, String label, int color) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setGravity(Gravity.CENTER);
        c.setPadding(dp(8), dp(20), dp(8), dp(20));
        c.setBackgroundDrawable(getDrawable(R.drawable.card_bg));

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

    TextView navBtn(String text, boolean on) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextSize(13);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setTextColor(on ? SUB : MUTED);
        btn.setPadding(dp(14), dp(8), dp(14), dp(8));
        btn.setBackgroundDrawable(getDrawable(on ? R.drawable.btn_nav : R.drawable.btn_nav_disabled));
        btn.setAlpha(on ? 1f : 0.35f);
        return btn;
    }

    GradientDrawable rd(int radius) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setCornerRadius(radius);
        return gd;
    }

    LinearLayout.LayoutParams fullW() {
        return new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    LinearLayout.LayoutParams weight1() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
    }

    int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
