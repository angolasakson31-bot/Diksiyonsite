package com.diksipro.app;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private WebView webView;
    private View splashLayout;
    private View offlineLayout;
    private View[] bars;
    private View[] dots;
    private ValueAnimator[] barAnimators;
    private ObjectAnimator[] dotAnimators;
    private ConnectivityManager.NetworkCallback networkCallback;

    private static final String APP_URL =
            "https://angolasakson31-bot.github.io/Diksiyonsite/";
    private static final String RELEASES_API =
            "https://api.github.com/repos/angolasakson31-bot/Diksiyonsite/releases/latest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView       = findViewById(R.id.webview);
        splashLayout  = findViewById(R.id.splash_layout);
        offlineLayout = findViewById(R.id.offline_layout);

        bars = new View[]{
            findViewById(R.id.bar1), findViewById(R.id.bar2), findViewById(R.id.bar3),
            findViewById(R.id.bar4), findViewById(R.id.bar5), findViewById(R.id.bar6),
            findViewById(R.id.bar7)
        };
        dots = new View[]{
            findViewById(R.id.dot1), findViewById(R.id.dot2), findViewById(R.id.dot3)
        };

        setupWebView();

        // Tap anywhere on offline screen to retry
        offlineLayout.setOnClickListener(v -> retryLoad());

        // Start animations after first layout pass
        splashLayout.post(() -> {
            startEqualizerAnimation();
            startDotsAnimation();
        });

        webView.loadUrl(APP_URL);
        registerNetworkCallback();  // auto-retry when connection returns

        new Handler(Looper.getMainLooper()).postDelayed(this::checkForUpdate, 4000);
    }

    private void retryLoad() {
        offlineLayout.setVisibility(View.GONE);
        splashLayout.setVisibility(View.VISIBLE);
        webView.loadUrl(APP_URL);
    }

    // ── WebView ──────────────────────────────────────────────────────────────

    private void setupWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView v, String url, Bitmap fav) {
                splashLayout.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                offlineLayout.setVisibility(View.GONE);
            }
            @Override
            public void onPageFinished(WebView v, String url) {
                splashLayout.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }
            @Override
            public void onReceivedError(WebView v, WebResourceRequest req,
                                        WebResourceError err) {
                if (req.isForMainFrame()) {
                    splashLayout.setVisibility(View.GONE);
                    webView.setVisibility(View.GONE);
                    offlineLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
    }

    // ── Animations ────────────────────────────────────────────────────────────

    private void startEqualizerAnimation() {
        // Organic min scales matching the waveform shape (tallest bar = smallest min)
        float[] minS  = {0.18f, 0.12f, 0.22f, 0.08f, 0.20f, 0.13f, 0.26f};
        long[]  dur   = {820L,  640L,  910L,  530L,  720L,  590L,  870L};
        long[]  delay = {0L,   140L,  270L,   60L,  210L,  150L,  330L};

        barAnimators = new ValueAnimator[bars.length];
        for (int i = 0; i < bars.length; i++) {
            final int   idx  = i;
            final float minScale = minS[i];
            bars[i].setPivotY(bars[i].getHeight());

            ValueAnimator a = ValueAnimator.ofFloat(1f, minScale, 1f);
            a.setDuration(dur[i]);
            a.setStartDelay(delay[i]);
            a.setRepeatCount(ValueAnimator.INFINITE);
            a.setRepeatMode(ValueAnimator.RESTART);
            a.setInterpolator(new AccelerateDecelerateInterpolator());
            a.addUpdateListener(anim -> {
                bars[idx].setPivotY(bars[idx].getHeight());
                bars[idx].setScaleY((float) anim.getAnimatedValue());
            });
            barAnimators[i] = a;
            a.start();
        }
    }

    private void startDotsAnimation() {
        dotAnimators = new ObjectAnimator[dots.length];
        for (int i = 0; i < dots.length; i++) {
            ObjectAnimator a = ObjectAnimator.ofFloat(dots[i], "alpha", 0.12f, 1f, 0.12f);
            a.setDuration(1200);
            a.setStartDelay(i * 230L);
            a.setRepeatCount(ValueAnimator.INFINITE);
            a.setRepeatMode(ValueAnimator.RESTART);
            dotAnimators[i] = a;
            a.start();
        }
    }

    private void stopAllAnimations() {
        if (barAnimators != null)
            for (ValueAnimator a : barAnimators) if (a != null) a.cancel();
        if (dotAnimators != null)
            for (ObjectAnimator a : dotAnimators) if (a != null) a.cancel();
    }

    // ── Network auto-retry ───────────────────────────────────────────────

    private void registerNetworkCallback() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;
        ConnectivityManager cm =
            (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm == null) return;
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                runOnUiThread(() -> {
                    if (offlineLayout.getVisibility() == View.VISIBLE) retryLoad();
                });
            }
        };
        cm.registerDefaultNetworkCallback(networkCallback);
    }

    private void unregisterNetworkCallback() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || networkCallback == null) return;
        ConnectivityManager cm =
            (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm != null) cm.unregisterNetworkCallback(networkCallback);
        networkCallback = null;
    }

    // ── Auto-update ─────────────────────────────────────────────────────────

    private void checkForUpdate() {
        new Thread(() -> {
            try {
                HttpURLConnection conn =
                    (HttpURLConnection) new URL(RELEASES_API).openConnection();
                conn.setRequestProperty("Accept", "application/vnd.github+json");
                conn.setConnectTimeout(6000);
                conn.setReadTimeout(6000);
                if (conn.getResponseCode() != 200) return;

                StringBuilder sb = new StringBuilder();
                try (BufferedReader r = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = r.readLine()) != null) sb.append(line);
                }
                String json   = sb.toString();
                String tag    = extractJson(json, "tag_name");
                String apkUrl = extractJson(json, "browser_download_url");
                if (tag.isEmpty() || apkUrl.isEmpty()) return;

                if (parseVersionCode(tag) > BuildConfig.VERSION_CODE && !isDestroyed())
                    runOnUiThread(() -> showUpdateDialog(tag, apkUrl));

            } catch (Exception ignored) {}
        }).start();
    }

    private void showUpdateDialog(String version, String url) {
        new AlertDialog.Builder(this)
            .setTitle("Yeni Sürüm")
            .setMessage("DiksiPro " + version + " hazır. Güncellemek ister misiniz?")
            .setPositiveButton("Güncelle", (d, w) ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))))
            .setNegativeButton("Daha Sonra", null)
            .show();
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private static String extractJson(String json, String key) {
        String s = "\"" + key + "\":\"";
        int i = json.indexOf(s);
        if (i < 0) return "";
        i += s.length();
        int j = json.indexOf('"', i);
        return j > i ? json.substring(i, j) : "";
    }

    private static int parseVersionCode(String tag) {
        try {
            String[] p = tag.replace("v", "").split("\\.");
            return p.length >= 3 ? Integer.parseInt(p[2]) : 0;
        } catch (Exception e) { return 0; }
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        stopAllAnimations();
        unregisterNetworkCallback();
        super.onDestroy();
    }
}
