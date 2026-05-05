package com.diksipro.app;

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
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private WebView webView;
    private View splashLayout;
    private LinearLayout offlineLayout;
    private View[] bars;
    private ValueAnimator[] barAnimators;

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
        Button retryBtn = findViewById(R.id.retry_button);

        bars = new View[]{
            findViewById(R.id.bar1), findViewById(R.id.bar2), findViewById(R.id.bar3),
            findViewById(R.id.bar4), findViewById(R.id.bar5)
        };

        setupWebView();

        // Start equalizer animation after first layout pass
        splashLayout.post(this::startEqualizerAnimation);

        retryBtn.setOnClickListener(v -> {
            offlineLayout.setVisibility(View.GONE);
            splashLayout.setVisibility(View.VISIBLE);
            webView.loadUrl(APP_URL);
        });

        webView.loadUrl(APP_URL);

        // Check for updates in background after 4s
        new Handler(Looper.getMainLooper()).postDelayed(this::checkForUpdate, 4000);
    }

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
            public void onPageStarted(WebView v, String url, Bitmap favicon) {
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

    private void startEqualizerAnimation() {
        float   density  = getResources().getDisplayMetrics().density;
        float[] maxDp    = {34f, 54f, 74f, 54f, 34f};
        long[]  duration = {920L, 760L, 600L, 760L, 920L};
        long[]  delay    = {0L, 160L, 320L, 160L, 0L};

        barAnimators = new ValueAnimator[bars.length];
        for (int i = 0; i < bars.length; i++) {
            bars[i].setPivotY(bars[i].getHeight()); // anchor to bottom
            final int idx = i;
            ValueAnimator a = ValueAnimator.ofFloat(1f, 0.14f, 1f);
            a.setDuration(duration[i]);
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

    private void stopEqualizerAnimation() {
        if (barAnimators == null) return;
        for (ValueAnimator a : barAnimators) if (a != null) a.cancel();
    }

    // ── Auto-update ───────────────────────────────────────────────────────────

    private void checkForUpdate() {
        new Thread(() -> {
            try {
                HttpURLConnection conn =
                    (HttpURLConnection) new URL(RELEASES_API).openConnection();
                conn.setRequestProperty("Accept", "application/vnd.github+json");
                conn.setConnectTimeout(6000);
                conn.setReadTimeout(6000);

                if (conn.getResponseCode() != 200) return;

                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                String json    = sb.toString();
                String tagName = extractJson(json, "tag_name");
                String apkUrl  = extractJson(json, "browser_download_url");

                if (tagName.isEmpty() || apkUrl.isEmpty()) return;

                int latestCode  = parseVersionCode(tagName);
                int currentCode = BuildConfig.VERSION_CODE;

                if (latestCode > currentCode && !isDestroyed()) {
                    runOnUiThread(() -> showUpdateDialog(tagName, apkUrl));
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void showUpdateDialog(String version, String url) {
        new AlertDialog.Builder(this)
            .setTitle("Yeni Sürüm Mevcut")
            .setMessage("DiksiPro " + version + " yayınlandı. Güncellemek ister misiniz?")
            .setPositiveButton("Güncelle", (d, w) ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))))
            .setNegativeButton("Daha Sonra", null)
            .show();
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private static String extractJson(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) return "";
        start += search.length();
        int end = json.indexOf('"', start);
        return end > start ? json.substring(start, end) : "";
    }

    private static int parseVersionCode(String tag) {
        try {
            String[] p = tag.replace("v", "").split("\\.");
            return p.length >= 3 ? Integer.parseInt(p[2]) : 0;
        } catch (Exception e) { return 0; }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
            (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network net = cm.getActiveNetwork();
            if (net == null) return false;
            NetworkCapabilities caps = cm.getNetworkCapabilities(net);
            return caps != null &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }
        @SuppressWarnings("deprecation")
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        stopEqualizerAnimation();
        super.onDestroy();
    }
}
