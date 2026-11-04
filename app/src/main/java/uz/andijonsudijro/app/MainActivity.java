package uz.andijonsudijro.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.webkit.JavascriptInterface;
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
        s.setSupportMultipleWindows(false);
        s.setLoadsImagesAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true);

        web.addJavascriptInterface(new Bridge(), "AndroidMib");

        web.setFocusable(true);
        web.setFocusableInTouchMode(true);
        web.setClickable(true);
        web.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
            }
            return false;
        });

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
            @Override public boolean onConsoleMessage(android.webkit.ConsoleMessage message) {
                android.util.Log.e("SudIjroWeb", message.message() + " -- " + message.sourceId() + ":" + message.lineNumber());
                return true;
            }
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
                if (dlg.getWindow() != null) dlg.getWindow().setLayout(-1, -1);
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
        web.setInitialScale(0);
    }


    /* ===================== MIB ICHKI OYNASI ===================== */
    private Dialog mibDialog;

    /** Faqat ilovaning o'z sahifasi (github.io) chaqira oladi. */
    private boolean trustedPage() {
        try {
            String u = web.getUrl();
            if (u == null) return false;
            String h = android.net.Uri.parse(u).getHost();
            return "sardoryakubov94-source.github.io".equals(h);
        } catch (Exception e) { return false; }
    }

    private class Bridge {
        @JavascriptInterface public void openMib(final String url, final String jshr, final String fio) {
            runOnUiThread(() -> { if (trustedPage()) showMib(url, jshr, fio); });
        }
        @JavascriptInterface public void copy(final String text) {
            runOnUiThread(() -> { if (trustedPage()) copyText(text); });
        }
    }

    private void copyText(String text) {
        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("JSHSHIR", text == null ? "" : text));
        } catch (Exception ignored) { }
    }

    private Button smallButton(String text, int bg) {
        Button b = new Button(this);
        b.setText(text); b.setTextColor(Color.WHITE); b.setTextSize(13);
        b.setAllCaps(false); b.setPadding(dp(8), 0, dp(8), 0);
        b.setBackgroundColor(bg);
        return b;
    }

    private void showMib(String url, String jshr, String fio) {
        if (mibDialog != null) { try { mibDialog.dismiss(); } catch (Exception ignored) { } }
        final String digits = jshr == null ? "" : jshr.replaceAll("[^0-9]", "");
        final String target = (url != null && url.startsWith("https://")) ? url : "https://mib.uz";
        if (!digits.isEmpty()) copyText(digits);

        final Dialog dlg = new Dialog(this, android.R.style.Theme_Material_NoActionBar);
        mibDialog = dlg;
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(Color.WHITE);

        // 1) Yuqori panel: yopish + qarzdor nomi + yangilash
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(4), dp(4), dp(4), dp(4));
        top.setBackgroundColor(Color.rgb(18, 25, 38));
        Button closeB = button("✕");
        TextView ttl = new TextView(this);
        ttl.setText(fio == null || fio.isEmpty() ? "MIB" : fio);
        ttl.setTextColor(Color.WHITE); ttl.setTextSize(14); ttl.setSingleLine(true);
        ttl.setEllipsize(android.text.TextUtils.TruncateAt.END);
        Button reloadB = button("↻");
        top.addView(closeB, new LinearLayout.LayoutParams(dp(42), dp(42)));
        top.addView(ttl, new LinearLayout.LayoutParams(0, dp(42), 1));
        top.addView(reloadB, new LinearLayout.LayoutParams(dp(42), dp(42)));
        box.addView(top);

        // 2) JSHSHIR paneli: raqam + nusxalash + maydonga qo'yish
        LinearLayout jb = new LinearLayout(this);
        jb.setGravity(Gravity.CENTER_VERTICAL);
        jb.setPadding(dp(8), dp(4), dp(8), dp(4));
        jb.setBackgroundColor(Color.rgb(30, 41, 59));
        TextView jt = new TextView(this);
        jt.setText(digits.isEmpty() ? "JSHSHIR yo'q" : digits);
        jt.setTextColor(Color.WHITE); jt.setTextSize(16); jt.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        jt.setTextIsSelectable(true);
        Button copyB = smallButton("📋 Nusxa", Color.rgb(59, 130, 246));
        Button fillB = smallButton("✍ Qo'yish", Color.rgb(34, 197, 94));
        jb.addView(jt, new LinearLayout.LayoutParams(0, dp(40), 1));
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(-2, dp(38));
        lp1.setMargins(dp(4), 0, 0, 0);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(-2, dp(38));
        lp2.setMargins(dp(4), 0, 0, 0);
        jb.addView(copyB, lp1);
        jb.addView(fillB, lp2);
        box.addView(jb);

        final ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pb.setMax(100);
        box.addView(pb, new LinearLayout.LayoutParams(-1, dp(3)));

        // 3) MIB sayti
        final WebView mw = new WebView(this);
        WebSettings ms = mw.getSettings();
        ms.setJavaScriptEnabled(true);
        ms.setDomStorageEnabled(true);
        ms.setLoadsImagesAutomatically(true);
        ms.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        ms.setAllowFileAccess(false);
        ms.setAllowContentAccess(false);
        ms.setUseWideViewPort(true);
        ms.setLoadWithOverviewMode(true);
        ms.setBuiltInZoomControls(true);
        ms.setDisplayZoomControls(false);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(mw, true);
        mw.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // faqat https sahifalar ichkarida ochiladi
                return !"https".equals(request.getUrl().getScheme());
            }
        });
        mw.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int value) {
                pb.setProgress(value);
                pb.setVisibility(value >= 100 ? View.GONE : View.VISIBLE);
            }
        });
        box.addView(mw, new LinearLayout.LayoutParams(-1, 0, 1));

        // 4) Pastki panel: natijani tez belgilash
        LinearLayout bot = new LinearLayout(this);
        bot.setGravity(Gravity.CENTER_VERTICAL);
        bot.setPadding(dp(4), dp(4), dp(4), dp(4));
        bot.setBackgroundColor(Color.rgb(18, 25, 38));
        Button rOk = smallButton("✅ Qo'zg'atilgan", Color.rgb(22, 163, 74));
        Button rNo = smallButton("❌ Yo'q", Color.rgb(220, 38, 38));
        Button rCl = smallButton("🗄️ Yopilgan", Color.rgb(71, 85, 105));
        LinearLayout.LayoutParams bl1 = new LinearLayout.LayoutParams(0, dp(44), 1);
        LinearLayout.LayoutParams bl2 = new LinearLayout.LayoutParams(0, dp(44), 1);
        LinearLayout.LayoutParams bl3 = new LinearLayout.LayoutParams(0, dp(44), 1);
        bl1.setMargins(dp(3), 0, dp(3), 0);
        bl2.setMargins(dp(3), 0, dp(3), 0);
        bl3.setMargins(dp(3), 0, dp(3), 0);
        bot.addView(rOk, bl1); bot.addView(rNo, bl2); bot.addView(rCl, bl3);
        box.addView(bot);

        dlg.setContentView(box);
        dlg.setOnDismissListener(d -> {
            try { mw.stopLoading(); mw.destroy(); } catch (Exception ignored) { }
            if (mibDialog == dlg) mibDialog = null;
        });
        dlg.setOnKeyListener((d, key, event) -> {
            if (key == android.view.KeyEvent.KEYCODE_BACK && event.getAction() == android.view.KeyEvent.ACTION_UP) {
                if (mw.canGoBack()) mw.goBack(); else finishMib(dlg, "");
                return true;
            }
            return false;
        });

        closeB.setOnClickListener(v -> finishMib(dlg, ""));
        reloadB.setOnClickListener(v -> mw.reload());
        copyB.setOnClickListener(v -> { copyText(digits); Toast.makeText(this, "JSHSHIR nusxalandi", Toast.LENGTH_SHORT).show(); });
        fillB.setOnClickListener(v -> fillMib(mw, digits));
        rOk.setOnClickListener(v -> finishMib(dlg, "qozgatilgan"));
        rNo.setOnClickListener(v -> finishMib(dlg, "yoq"));
        rCl.setOnClickListener(v -> finishMib(dlg, "yopilgan"));

        dlg.show();
        if (dlg.getWindow() != null) {
            dlg.getWindow().setLayout(-1, -1);
            dlg.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        mw.loadUrl(target);
        if (!digits.isEmpty()) Toast.makeText(this, "JSHSHIR nusxalandi: MIB maydoniga qo'ying (✍ Qo'yish)", Toast.LENGTH_LONG).show();
    }

    /** MIB sahifasidagi mos kiritish maydoniga JSHSHIRni qo'yishga urinadi. */
    private void fillMib(WebView mw, String digits) {
        if (digits.isEmpty()) { Toast.makeText(this, "JSHSHIR yo'q", Toast.LENGTH_SHORT).show(); return; }
        String js = "(function(v){"
            + "var els=[].slice.call(document.querySelectorAll('input'));"
            + "var ok=els.filter(function(e){var t=(e.type||'text').toLowerCase();"
            + "return ['text','search','tel','number',''].indexOf(t)>=0&&!e.readOnly&&!e.disabled&&e.offsetParent!==null;});"
            + "if(!ok.length)return 'no';"
            + "var pref=ok.filter(function(e){return /pinfl|jshshir|jshir|pnfl|pin|passport|pasport|stir|inn|search|qidir|izla/i.test((e.name||'')+(e.id||'')+(e.placeholder||''));});"
            + "var el=pref[0]||ok[0];el.focus();"
            + "var set=Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value').set;"
            + "set.call(el,v);"
            + "el.dispatchEvent(new Event('input',{bubbles:true}));"
            + "el.dispatchEvent(new Event('change',{bubbles:true}));"
            + "return 'ok';})('" + digits + "')";
        mw.evaluateJavascript(js, value -> {
            if (value != null && value.contains("ok")) Toast.makeText(this, "JSHSHIR maydonga qo'yildi", Toast.LENGTH_SHORT).show();
            else Toast.makeText(this, "Maydon topilmadi: JSHSHIR nusxalangan, qo'lda qo'ying", Toast.LENGTH_LONG).show();
        });
    }

    /** MIB oynasini yopib, ilovaga (qarzdor formasiga) qaytadi. */
    private void finishMib(Dialog dlg, String holat) {
        try { dlg.dismiss(); } catch (Exception ignored) { }
        if (web != null) web.evaluateJavascript("window.mibAfterClose&&mibAfterClose('" + holat + "')", null);
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
