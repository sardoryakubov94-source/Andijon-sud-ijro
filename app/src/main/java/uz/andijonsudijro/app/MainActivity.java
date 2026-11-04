package uz.andijonsudijro.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.SafeBrowsingResponse;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String HOME = "https://sardoryakubov94-source.github.io/Andijon-sud-ijro/";
    private WebView web;
    private ProgressBar progress;
    private TextView title;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(18, 25, 38));
        getWindow().setNavigationBarColor(Color.BLACK);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(8, 5, 8, 5);
        bar.setBackgroundColor(Color.rgb(18, 25, 38));
        Button back = button("‹");
        Button home = button("⌂");
        Button reload = button("↻");
        title = new TextView(this);
        title.setText("Andijon sud ijro");
        title.setTextColor(Color.WHITE);
        title.setTextSize(15);
        title.setGravity(Gravity.CENTER);
        bar.addView(back, new LinearLayout.LayoutParams(dp(42), dp(42)));
        bar.addView(home, new LinearLayout.LayoutParams(dp(42), dp(42)));
        bar.addView(title, new LinearLayout.LayoutParams(0, dp(42), 1));
        bar.addView(reload, new LinearLayout.LayoutParams(dp(42), dp(42)));
        root.addView(bar);
        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        root.addView(progress, new LinearLayout.LayoutParams(-1, dp(3)));
        web = new WebView(this);
        root.addView(web, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setSupportMultipleWindows(true);
        s.setLoadsImagesAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true);

        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // HTTPS sahifalar ilovaning ichki brauzerida ochiladi.
                return false;
            }
            @Override public void onPageStarted(WebView view, String url, android.graphics.Bitmap icon) {
                title.setText("Yuklanmoqda…");
                progress.setVisibility(View.VISIBLE);
            }
            @Override public void onPageFinished(WebView view, String url) {
                title.setText(host(url));
                progress.setVisibility(View.GONE);
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int value) {
                progress.setProgress(value);
                progress.setVisibility(value >= 100 ? View.GONE : View.VISIBLE);
            }
            @Override public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, android.os.Message resultMsg) {
                // window.open() so'rovlari ham ilova ichidagi alohida WebView oynasida ochiladi.
                final WebView popup = new WebView(MainActivity.this);
                WebSettings ps = popup.getSettings();
                ps.setJavaScriptEnabled(true);
                ps.setDomStorageEnabled(true);
                ps.setJavaScriptCanOpenWindowsAutomatically(true);
                ps.setSupportMultipleWindows(true);
                ps.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
                ps.setAllowFileAccess(false);
                ps.setAllowContentAccess(false);
                CookieManager.getInstance().setAcceptThirdPartyCookies(popup, true);
                popup.setWebViewClient(new WebViewClient());
                AlertDialog dlg = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("MIB / ichki sahifa")
                    .setView(popup)
                    .setNegativeButton("Yopish", (d, w) -> popup.destroy())
                    .create();
                dlg.setOnKeyListener((d, key, event) -> {
                    if (key == android.view.KeyEvent.KEYCODE_BACK && event.getAction() == android.view.KeyEvent.ACTION_UP && popup.canGoBack()) {
                        popup.goBack(); return true;
                    }
                    return false;
                });
                dlg.show();
                popup.setWebChromeClient(new WebChromeClient());
                ((WebView.WebViewTransport) resultMsg.obj).setWebView(popup);
                resultMsg.sendToTarget();
                return true;
            }
        });
        back.setOnClickListener(v -> { if (web.canGoBack()) web.goBack(); else Toast.makeText(this, "Oldingi sahifa yo‘q", Toast.LENGTH_SHORT).show(); });
        home.setOnClickListener(v -> web.loadUrl(HOME));
        reload.setOnClickListener(v -> web.reload());
        if (state == null) web.loadUrl(HOME); else web.restoreState(state);
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text); b.setTextColor(Color.WHITE); b.setTextSize(20);
        b.setAllCaps(false); b.setPadding(0,0,0,0);
        b.setBackgroundColor(Color.TRANSPARENT);
        return b;
    }
    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }
    private String host(String url) {
        try { return android.net.Uri.parse(url).getHost(); } catch (Exception e) { return "Andijon sud ijro"; }
    }
    @Override public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        if (web != null) web.saveState(out);
        super.onSaveInstanceState(out);
    }
    @Override protected void onDestroy() {
        if (web != null) { web.stopLoading(); web.destroy(); }
        super.onDestroy();
    }
}
