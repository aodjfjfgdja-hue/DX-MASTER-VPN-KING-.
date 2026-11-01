package com.dxmaster.multispace;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    static final String ACTION_STOP = "com.dxmaster.multispace.STOP";
    private static final Class<?>[] CHATS = {
            ChatActivity.C1.class, ChatActivity.C2.class, ChatActivity.C3.class,
            ChatActivity.C4.class, ChatActivity.C5.class};

    private SharedPreferences sp;
    private int count;
    private LinearLayout grid;
    private AudioPanel panel;

    private int dp(int v) { return AudioPanel.dp(this, v); }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        sp = getSharedPreferences("settings", 0);
        count = sp.getInt("count", 2);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF000000);

        TextView title = new TextView(this);
        title.setText("☰  MultiSpace");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(22);
        title.setTypeface(Typeface.SERIF, Typeface.BOLD);
        title.setPadding(dp(16), dp(16), dp(16), dp(10));
        root.addView(title);

        ScrollView sv = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(10), dp(6), dp(10), dp(10));

        grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        content.addView(grid);
        buildGrid();

        int[] init = new int[5];
        for (int i = 0; i < 5; i++) init[i] = sp.getInt(AudioPanel.KEYS[i], 50);
        panel = new AudioPanel(this, init, (k, v) -> sp.edit().putInt(k, v).apply());
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(-1, -2);
        plp.topMargin = dp(16);
        content.addView(panel.root, plp);

        final Switch sw = new Switch(this);
        sw.setTextColor(0xFFFFFFFF);
        sw.setPadding(dp(10), dp(12), dp(10), dp(12));
        sw.setChecked(sp.getBoolean("on", true));
        sw.setText(sw.isChecked() ? "📞 কল সাউন্ড বুস্ট: চালু" : "📞 কল সাউন্ড বুস্ট: বন্ধ");
        sw.setOnCheckedChangeListener((v, on) -> {
            sp.edit().putBoolean("on", on).apply();
            sw.setText(on ? "📞 কল সাউন্ড বুস্ট: চালু" : "📞 কল সাউন্ড বুস্ট: বন্ধ");
        });
        content.addView(sw);

        TextView save = AudioPanel.button(this, "💾 সেভ করুন", 0xFF1565C0);
        save.setOnClickListener(v -> {
            SharedPreferences.Editor e = sp.edit();
            for (int i = 0; i < 5; i++) e.putInt(AudioPanel.KEYS[i], panel.get(i));
            e.apply();
            Toast.makeText(this, "সেভ হয়েছে ✅ (নতুন করে ক্লোন খুললে কাজ করবে)", Toast.LENGTH_LONG).show();
        });
        content.addView(save);

        sv.addView(content);
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout bar = new LinearLayout(this);
        bar.setPadding(dp(8), dp(8), dp(8), dp(8));
        TextView clone = AudioPanel.button(this, "➕ ক্লোন করুন", 0xFF1E9E5A);
        TextView stop = AudioPanel.button(this, "✖ বন্ধ করুন", 0xFF1565C0);
        LinearLayout.LayoutParams l1 = new LinearLayout.LayoutParams(0, -2, 1);
        l1.setMargins(dp(4), 0, dp(4), 0);
        bar.addView(clone, l1);
        bar.addView(stop, new LinearLayout.LayoutParams(l1));
        root.addView(bar);

        clone.setOnClickListener(v -> {
            if (count >= 5) {
                Toast.makeText(this, "সর্বোচ্চ ৫টা ক্লোন", Toast.LENGTH_SHORT).show();
                return;
            }
            count++;
            sp.edit().putInt("count", count).apply();
            buildGrid();
        });
        stop.setOnClickListener(v -> {
            Intent i = new Intent(ACTION_STOP).setPackage(getPackageName());
            i.putExtra("slot", 0);
            sendBroadcast(i);
            Toast.makeText(this, "সব ক্লোন বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show();
        });

        setContentView(root);
    }

    private void buildGrid() {
        grid.removeAllViews();
        LinearLayout row = null;
        for (int i = 0; i < count; i++) {
            if (i % 3 == 0) {
                row = new LinearLayout(this);
                row.setPadding(0, dp(10), 0, dp(10));
                grid.addView(row);
            }
            final int idx = i;
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER_HORIZONTAL);

            TextView icon = new TextView(this);
            icon.setText("💬");
            icon.setTextSize(32);
            icon.setGravity(Gravity.CENTER);
            GradientDrawable g = new GradientDrawable();
            g.setColor(0xFFFFFFFF);
            g.setCornerRadius(dp(10));
            icon.setBackground(g);
            cell.addView(icon, new LinearLayout.LayoutParams(dp(84), dp(84)));

            TextView name = new TextView(this);
            name.setText("Messenger(" + (i + 1) + ")");
            name.setTextColor(0xFFFFFFFF);
            name.setTextSize(13);
            name.setTypeface(Typeface.SERIF, Typeface.BOLD);
            name.setPadding(0, dp(4), 0, 0);
            cell.addView(name);

            cell.setOnClickListener((View v) -> startActivity(new Intent(this, CHATS[idx])));
            row.addView(cell, new LinearLayout.LayoutParams(0, -2, 1));
        }
        if (row != null) {
            for (int k = count % 3; k != 0 && k < 3; k++) {
                row.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1));
            }
        }
    }
}
