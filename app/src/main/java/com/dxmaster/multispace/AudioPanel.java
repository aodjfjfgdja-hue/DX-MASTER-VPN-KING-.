package com.dxmaster.multispace;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class AudioPanel {
    public interface Listener { void onChange(String key, int value); }

    public static final String[] KEYS = {"mic", "vol", "bass", "mid", "treble"};
    private static final String[] NAMES = {"🎤 মাইক বুস্ট", "🔊 ভলিউম", "🥁 বাস", "🎚 মিড", "✨ ট্রেবল"};
    private static final int[] COLORS = {0xFFFF5252, 0xFF64B5F6, 0xFFFFB74D, 0xFF81C784, 0xFFBA68C8};

    public final LinearLayout root;
    private final SeekBar[] bars = new SeekBar[5];
    private final TextView[] vals = new TextView[5];

    public static int dp(Context c, int v) {
        return (int) (v * c.getResources().getDisplayMetrics().density);
    }

    public static TextView button(Context c, String text, int color) {
        TextView t = new TextView(c);
        t.setText(text);
        t.setTextColor(0xFFFFFFFF);
        t.setTextSize(14);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(c, 12), dp(c, 10), dp(c, 12), dp(c, 10));
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(c, 6));
        t.setBackground(g);
        return t;
    }

    public AudioPanel(Context c, int[] init, final Listener l) {
        root = new LinearLayout(c);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(c, 12), dp(c, 10), dp(c, 12), dp(c, 10));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF0D1B2A);
        bg.setCornerRadius(dp(c, 12));
        root.setBackground(bg);

        LinearLayout head = new LinearLayout(c);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = new TextView(c);
        title.setText("🔊 JBL স্পিকার ডিজে স্টুডিও");
        title.setTextColor(0xFF64B5F6);
        title.setTextSize(15);
        head.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        TextView max = button(c, "🔥 অল ম্যাক্স", 0xFFE0314B);
        max.setOnClickListener(v -> setAllMax());
        head.addView(max);
        root.addView(head);

        for (int i = 0; i < 5; i++) {
            final int idx = i;
            LinearLayout row = new LinearLayout(c);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(c, 4), 0, dp(c, 4));

            TextView n = new TextView(c);
            n.setText(NAMES[i]);
            n.setTextColor(COLORS[i]);
            n.setTextSize(13);
            row.addView(n, new LinearLayout.LayoutParams(dp(c, 118), -2));

            SeekBar sb = new SeekBar(c);
            sb.setMax(100);
            sb.setProgress(init[i]);
            sb.setProgressTintList(ColorStateList.valueOf(COLORS[i]));
            sb.setThumbTintList(ColorStateList.valueOf(COLORS[i]));
            bars[i] = sb;
            row.addView(sb, new LinearLayout.LayoutParams(0, -2, 1));

            TextView v = new TextView(c);
            v.setText(init[i] + "%");
            v.setTextColor(0xFFFFFFFF);
            v.setTextSize(13);
            v.setGravity(Gravity.END);
            vals[i] = v;
            row.addView(v, new LinearLayout.LayoutParams(dp(c, 50), -2));

            sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar s, int p, boolean user) {
                    vals[idx].setText(p + "%");
                    if (l != null) l.onChange(KEYS[idx], p);
                }
                public void onStartTrackingTouch(SeekBar s) {}
                public void onStopTrackingTouch(SeekBar s) {}
            });
            root.addView(row);
        }
    }

    public void setAllMax() {
        for (SeekBar b : bars) b.setProgress(100);
    }

    public int get(int i) {
        return bars[i].getProgress();
    }
}
