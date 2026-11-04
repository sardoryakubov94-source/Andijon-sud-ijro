package uz.andijonsudijro.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
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
    private static final String APP_HOST = "sardoryakubov94-source.github.io";
    private static final String MIB_BASE = "https://mib.uz";
    private static final int BG = Color.rgb(10, 14, 26);

    private WebView web;
    private Dialog mibDialog;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        applyCutout(getWindow());

        // To'liq ekran: tepada manzil/sarlavha paneli yo'q, faqat dastur o'zi.
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        installInsets(root);
        web = new WebView(this);
        web.setBackgroundColor(BG);
        root.addView(web, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        immersive(getWindow());

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
                if (!request.isForMainFrame()) return false;
                Uri u = request.getUrl();
                String sch = u.getScheme();
                if ("https".equals(sch) && APP_HOST.equals(u.getHost())) return false;
                if ("blob".equals(sch) || "data".equals(sch) || "javascript".equals(sch) || "about".equals(sch)) return false;
                // Boshqa saytlar va tel:/mailto: kabi havolalar dastur tashqarisida ochiladi
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) { }
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onConsoleMessage(android.webkit.ConsoleMessage message) {
                android.util.Log.e("SudIjroWeb", message.message() + " -- " + message.sourceId() + ":" + message.lineNumber());
                return true;
            }
        });
        if (state == null) web.loadUrl(HOME); else web.restoreState(state);
        web.setInitialScale(0);
    }

    /* ===================== TO'LIQ EKRAN ===================== */

    private void applyCutout(Window w) {
        if (Build.VERSION.SDK_INT >= 28) {
            WindowManager.LayoutParams lp = w.getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            w.setAttributes(lp);
        }
    }

    @SuppressWarnings("deprecation")
    private void immersive(Window w) {
        w.getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /** Kamera o'yig'i (cutout) ostiga tushmaslik va klaviatura chiqqanda pastdan bo'sh joy qoldirish. */
    @SuppressWarnings("deprecation")
    private void installInsets(View v) {
        v.setOnApplyWindowInsetsListener((view, insets) -> {
            int top = 0;
            if (Build.VERSION.SDK_INT >= 28 && insets.getDisplayCutout() != null) {
                top = insets.getDisplayCutout().getSafeInsetTop();
            }
            int bottom = insets.getSystemWindowInsetBottom();
            if (bottom < dp(120)) bottom = 0; // navigatsiya paneli emas, faqat klaviatura hisobga olinadi
            view.setPadding(0, top, 0, bottom);
            return insets;
        });
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) immersive(getWindow());
    }

    /* ===================== MIB ICHKI OYNASI ===================== */

    /** Faqat ilovaning o'z sahifasi (github.io) chaqira oladi. */
    private boolean trustedPage() {
        try {
            String u = web.getUrl();
            if (u == null) return false;
            String h = Uri.parse(u).getHost();
            return APP_HOST.equals(h);
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

    private GradientDrawable pill(int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    private Button pillButton(String text, int color, int sp) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(Color.WHITE);
        b.setTextSize(sp);
        b.setAllCaps(false);
        b.setPadding(dp(6), 0, dp(6), 0);
        b.setBackground(pill(color, 12));
        b.setStateListAnimator(null);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        return b;
    }

    private LinearLayout.LayoutParams lp(int w, int h, float weight, int l, int t, int r, int b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h, weight);
        p.setMargins(l, t, r, b);
        return p;
    }

    private static final String DETECT_JS =
        "(function(){var t=((document.body&&document.body.innerText)||'');"
        + "return /жшшир|jshshir/i.test(t)&&document.querySelectorAll('input').length>1;})()";

    /**
     * MIB sahifasida: 1) qarzdorlikni aniqlash bo'limini topadi, 2) JSHSHIR yorlig'ini tanlaydi,
     * 3) JSHSHIRni yozadi, 4) tekshiruv javobi (kod) maydoniga o'tkazadi. Kodni foydalanuvchi o'zi kiritadi.
     */
    private String autoScript(String digits) {
        return "(function(v){"
        + "if(window.__mibAuto){return 'busy';}window.__mibAuto=true;"
        + "var n=0,clicked=false,home=false;"
        + "function vis(e){return !!(e&&(e.offsetParent!==null||e.getClientRects().length));}"
        + "function ins(){return [].slice.call(document.querySelectorAll('input')).filter(function(e){"
        + "var t=(e.type||'text').toLowerCase();"
        + "return ['text','search','tel','number',''].indexOf(t)>=0&&!e.readOnly&&!e.disabled&&vis(e);});}"
        + "function sig(e){return ((e.name||'')+' '+(e.id||'')+' '+(e.placeholder||'')).toLowerCase();}"
        + "var cap=/javob|жавоб|captcha|kapcha|answer|natija|amal|амал/;"
        + "var jr=/jshshir|жшшир|pinfl|pnfl|jshshr/;"
        + "function txt(e){return (e.innerText||e.textContent||'').trim();}"
        + "function setVal(el,val){el.focus();"
        + "var d=Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value');d.set.call(el,val);"
        + "el.dispatchEvent(new Event('input',{bubbles:true}));el.dispatchEvent(new Event('change',{bubbles:true}));}"
        + "function clickable(e){return e.closest('a,button,[role=tab],li')||e;}"
        + "function findTab(){var best=null,bl=1e9;"
        + "[].slice.call(document.querySelectorAll('a,button,li,div,span,label,h2,h3,h4,p,[role=tab]')).forEach(function(e){"
        + "var t=txt(e);if(!t||t.length>160)return;"
        + "var c=t.replace(/^[^A-Za-zА-Яа-я]+/,'');"
        + "if(/^(жшшир|jshshir|jshshr|pinfl|пинфл)/i.test(c)&&c.indexOf(':')<0&&!/киритинг|kiriting/i.test(c)&&vis(e)&&t.length<bl){best=e;bl=t.length;}});"
        + "return best;}"
        + "function goDebt(){var best=null,bl=1e9;"
        + "[].slice.call(document.querySelectorAll('a,button')).forEach(function(e){"
        + "var t=txt(e).toLowerCase();if(!t||t.length>120)return;"
        + "if(/(қарздорлик|qarzdorlik)/.test(t)&&/(аниқла|текшир|aniqla|tekshir)/.test(t)&&t.length<bl&&vis(e)){best=e;bl=t.length;}});"
        + "if(best){best.click();return true;}return false;}"
        + "var iv=setInterval(function(){"
        + "n++;"
        + "var tab=findTab();"
        + "if(tab&&!clicked){clickable(tab).click();clicked=true;return;}"
        + "if(!tab&&!clicked&&!home&&n>3){home=goDebt();}"
        + "var list=ins();"
        + "var capEl=list.filter(function(e){return cap.test(sig(e));})[0];"
        + "if(capEl){var scope=capEl.form||capEl.closest('form')||document;"
        + "list=list.filter(function(e){return scope.contains(e);});}"
        + "var ready=clicked||list.some(function(e){return jr.test(sig(e));});"
        + "if(ready){"
        + "var main=list.filter(function(e){return !cap.test(sig(e));})[0];"
        + "if(!capEl&&list.length>1){capEl=list[list.length-1];}"
        + "if(main&&main!==capEl){setVal(main,v);"
        + "if(capEl){try{capEl.scrollIntoView({block:'center'});capEl.focus();}catch(x){}}"
        + "clearInterval(iv);window.__mibAuto=false;return;}}"
        + "if(n>30){clearInterval(iv);window.__mibAuto=false;}"
        + "},400);"
        + "return 'start';"
        + "})('" + digits + "')";
    }

    private void showMib(String url, String jshr, String fio) {
        if (mibDialog != null) { try { mibDialog.dismiss(); } catch (Exception ignored) { } }
        final String digits = jshr == null ? "" : jshr.replaceAll("[^0-9]", "");
        if (!digits.isEmpty()) copyText(digits);

        final SharedPreferences prefs = getSharedPreferences("mib", MODE_PRIVATE);
        String saved = prefs.getString("form_url", null);
        String base = (url != null && url.startsWith("https://")) ? url : MIB_BASE;
        String target = base;
        try {
            if (saved != null && saved.startsWith("https://")) {
                String h = Uri.parse(saved).getHost();
                if (h != null && h.endsWith("mib.uz")) target = saved;
            }
        } catch (Exception ignored) { }
        final String startUrl = target;
        final String baseUrl = base;

        final Dialog dlg = new Dialog(this, android.R.style.Theme_Material_NoActionBar);
        mibDialog = dlg;
        applyCutout(dlg.getWindow());

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(Color.WHITE);
        installInsets(box);

        final ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pb.setMax(100);
        box.addView(pb, new LinearLayout.LayoutParams(-1, dp(3)));

        // MIB sayti: butun ekran (yuqorida hech qanday panel/manzil yo'q)
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
                return !"https".equals(request.getUrl().getScheme());
            }
            @Override public void onPageFinished(WebView view, String u) {
                pb.setVisibility(View.GONE);
                view.evaluateJavascript(DETECT_JS, val -> {
                    if (val != null && val.contains("true") && u != null) {
                        try {
                            String h = Uri.parse(u).getHost();
                            if (h != null && h.endsWith("mib.uz")) prefs.edit().putString("form_url", u).apply();
                        } catch (Exception ignored) { }
                    }
                });
                if (!digits.isEmpty()) view.evaluateJavascript(autoScript(digits), null);
            }
        });
        mw.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int value) {
                pb.setProgress(value);
                pb.setVisibility(value >= 100 ? View.GONE : View.VISIBLE);
            }
        });
        box.addView(mw, new LinearLayout.LayoutParams(-1, 0, 1));

        // Pastki panel (faqat pastda): JSHSHIR + amallar + natija tugmalari
        LinearLayout dock = new LinearLayout(this);
        dock.setOrientation(LinearLayout.VERTICAL);
        dock.setPadding(dp(10), dp(10), dp(10), dp(10));
        GradientDrawable dbg = new GradientDrawable();
        dbg.setColor(Color.rgb(15, 23, 42));
        float r = dp(18);
        dbg.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        dock.setBackground(dbg);

        LinearLayout rowA = new LinearLayout(this);
        rowA.setGravity(Gravity.CENTER_VERTICAL);
        Button closeB = pillButton("✕", Color.rgb(51, 65, 85), 18);
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        TextView ttl = new TextView(this);
        ttl.setText(fio == null || fio.isEmpty() ? "MIB" : fio);
        ttl.setTextColor(Color.rgb(148, 163, 184));
        ttl.setTextSize(11);
        ttl.setSingleLine(true);
        ttl.setEllipsize(android.text.TextUtils.TruncateAt.END);
        TextView jt = new TextView(this);
        jt.setText(digits.isEmpty() ? "JSHSHIR yo'q" : digits);
        jt.setTextColor(Color.WHITE);
        jt.setTextSize(17);
        jt.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        jt.setSingleLine(true);
        info.addView(ttl);
        info.addView(jt);
        Button copyB = pillButton("📋", Color.rgb(59, 130, 246), 18);
        Button fillB = pillButton("✍️", Color.rgb(34, 197, 94), 18);
        Button reloadB = pillButton("↻", Color.rgb(51, 65, 85), 18);
        rowA.addView(closeB, lp(dp(44), dp(44), 0, 0, 0, 0, 0));
        rowA.addView(info, lp(0, -2, 1, dp(10), 0, dp(6), 0));
        rowA.addView(copyB, lp(dp(44), dp(44), 0, 0, 0, dp(6), 0));
        rowA.addView(fillB, lp(dp(44), dp(44), 0, 0, 0, dp(6), 0));
        rowA.addView(reloadB, lp(dp(44), dp(44), 0, 0, 0, 0, 0));
        dock.addView(rowA, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout rowB = new LinearLayout(this);
        rowB.setGravity(Gravity.CENTER_VERTICAL);
        Button rOk = pillButton("✅ Qo'zg'atilgan", Color.rgb(22, 163, 74), 12);
        Button rNo = pillButton("❌ Yo'q", Color.rgb(220, 38, 38), 12);
        Button rCl = pillButton("🗄️ Yopilgan", Color.rgb(71, 85, 105), 12);
        rowB.addView(rOk, lp(0, dp(46), 1.3f, 0, 0, dp(6), 0));
        rowB.addView(rNo, lp(0, dp(46), 0.8f, 0, 0, dp(6), 0));
        rowB.addView(rCl, lp(0, dp(46), 1f, 0, 0, 0, 0));
        dock.addView(rowB, lp(-1, -2, 0, 0, dp(8), 0, 0));
        box.addView(dock, new LinearLayout.LayoutParams(-1, -2));

        dlg.setContentView(box);
        dlg.setOnDismissListener(d -> {
            try { mw.stopLoading(); mw.destroy(); } catch (Exception ignored) { }
            if (mibDialog == dlg) mibDialog = null;
        });
        dlg.setOnKeyListener((d, key, event) -> {
            if (key == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                if (mw.canGoBack()) mw.goBack(); else finishMib(dlg, "");
                return true;
            }
            return false;
        });

        closeB.setOnClickListener(v -> finishMib(dlg, ""));
        reloadB.setOnClickListener(v -> mw.reload());
        reloadB.setOnLongClickListener(v -> {
            prefs.edit().remove("form_url").apply();
            mw.loadUrl(baseUrl);
            Toast.makeText(this, "MIB bosh sahifasiga qaytildi", Toast.LENGTH_SHORT).show();
            return true;
        });
        copyB.setOnClickListener(v -> { copyText(digits); Toast.makeText(this, "JSHSHIR nusxalandi", Toast.LENGTH_SHORT).show(); });
        fillB.setOnClickListener(v -> fillMib(mw, digits));
        rOk.setOnClickListener(v -> finishMib(dlg, "qozgatilgan"));
        rNo.setOnClickListener(v -> finishMib(dlg, "yoq"));
        rCl.setOnClickListener(v -> finishMib(dlg, "yopilgan"));

        dlg.show();
        if (dlg.getWindow() != null) {
            dlg.getWindow().setLayout(-1, -1);
            immersive(dlg.getWindow());
        }
        mw.loadUrl(startUrl);
    }

    /** JSHSHIRni MIB formasiga qayta qo'yishga urinadi. */
    private void fillMib(WebView mw, String digits) {
        if (digits.isEmpty()) { Toast.makeText(this, "JSHSHIR yo'q", Toast.LENGTH_SHORT).show(); return; }
        mw.evaluateJavascript("window.__mibAuto=false;", null);
        mw.evaluateJavascript(autoScript(digits), null);
        Toast.makeText(this, "JSHSHIR joylanmoqda… topilmasa nusxadan qo'ying", Toast.LENGTH_SHORT).show();
    }

    /** MIB oynasini yopib, ilovaga (qarzdor formasiga) qaytadi. */
    private void finishMib(Dialog dlg, String holat) {
        try { dlg.dismiss(); } catch (Exception ignored) { }
        if (web != null) web.evaluateJavascript("window.mibAfterClose&&mibAfterClose('" + holat + "')", null);
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }

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
