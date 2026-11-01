package com.dxmaster.multispace;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ChatActivity extends Activity {

    public static class C1 extends ChatActivity { protected int slot() { return 1; } }
    public static class C2 extends ChatActivity { protected int slot() { return 2; } }
    public static class C3 extends ChatActivity { protected int slot() { return 3; } }
    public static class C4 extends ChatActivity { protected int slot() { return 4; } }
    public static class C5 extends ChatActivity { protected int slot() { return 5; } }

    protected int slot() { return 1; }

    private static final String UA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";
    private static final String HOME = "https://www.messenger.com/";

    private WebView web;
    private SharedPreferences slotSp, defs;
    private String js = "";
    private boolean boostOn;
    private AudioManager am;
    private BroadcastReceiver rx;
    private AudioPanel panel;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        try {
            if (Build.VERSION.SDK_INT >= 28) WebView.setDataDirectorySuffix("c" + slot());
        } catch (Exception ignored) {}

        defs = getSharedPreferences("settings", 0);
        slotSp = getSharedPreferences("s" + slot(), 0);
        boostOn = defs.getBoolean("on", true);
        js = readAsset("dx.js");
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF000000);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(AudioPanel.dp(this, 8), AudioPanel.dp(this, 6), AudioPanel.dp(this, 8), AudioPanel.dp(this, 6));
        TextView title = new TextView(this);
        title.setText("Messenger(" + slot() + ")");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(16);
        top.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        TextView bSound = AudioPanel.button(this, "🔊", 0xFF1565C0);
        TextView bReload = AudioPanel.button(this, "🔄", 0xFF37474F);
        TextView bClose = AudioPanel.button(this, "✖", 0xFFE0314B);
        LinearLayout.LayoutParams bl = new LinearLayout.LayoutParams(-2, -2);
        bl.leftMargin = AudioPanel.dp(this, 6);
        top.addView(bSound, bl);
        top.addView(bReload, new LinearLayout.LayoutParams(bl));
        top.addView(bClose, new LinearLayout.LayoutParams(bl));
        root.addView(top);

        web = new WebView(this);
        root.addView(web, new LinearLayout.LayoutParams(-1, 0, 1));

        int[] init = new int[5];
        for (int i = 0; i < 5; i++) init[i] = val(AudioPanel.KEYS[i]);
        panel = new AudioPanel(this, init, (k, v) -> {
            slotSp.edit().putInt(k, v).apply();
            web.evaluateJavascript("window.__dx&&__dx.set('" + k + "'," + v + ")", null);
        });
        panel.root.setVisibility(View.GONE);
        root.addView(panel.root, new LinearLayout.LayoutParams(-1, -2));

        bSound.setOnClickListener(v -> panel.root.setVisibility(
                panel.root.getVisibility() == View.GONE ? View.VISIBLE : View.GONE));
        bReload.setOnClickListener(v -> web.reload());
        bClose.setOnClickListener(v -> stopSelf());

        setContentView(root);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setUserAgentString(UA);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(web, true);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView v, String url, Bitmap f) { inject(); }
            @Override
            public void onPageFinished(WebView v, String url) { inject(); }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest r) {
                runOnUiThread(() -> r.grant(r.getResources()));
            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, 1);
        }

        rx = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                int sl = i.getIntExtra("slot", 0);
                if (sl == 0 || sl == slot()) stopSelf();
            }
        };
        IntentFilter f = new IntentFilter(MainActivity.ACTION_STOP);
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(rx, f, Context.RECEIVER_NOT_EXPORTED);
        else registerReceiver(rx, f);

        web.loadUrl(HOME);
    }

    private int val(String k) {
        return slotSp.getInt(k, defs.getInt(k, 50));
    }

    private void inject() {
        if (!boostOn) return;
        web.evaluateJavascript(js, null);
        StringBuilder sb = new StringBuilder("window.__dx&&__dx.setAll({");
        for (String k : AudioPanel.KEYS) sb.append(k).append(':').append(val(k)).append(',');
        sb.append("});");
        web.evaluateJavascript(sb.toString(), null);
    }

    private String readAsset(String name) {
        try (InputStream in = getAssets().open(name); ByteArrayOutputStream o = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int r;
            while ((r = in.read(buf)) > 0) o.write(buf, 0, r);
            return o.toString("UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    private void stopSelf() {
        CookieManager.getInstance().flush();
        finishAndRemoveTask();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] p, int[] r) {
        super.onRequestPermissionsResult(code, p, r);
        web.reload();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (boostOn) {
            try {
                am.setSpeakerphoneOn(true);
                am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else finish();
    }

    @Override
    protected void onDestroy() {
        try { unregisterReceiver(rx); } catch (Exception ignored) {}
        try { am.setSpeakerphoneOn(false); } catch (Exception ignored) {}
        super.onDestroy();
    }
}
