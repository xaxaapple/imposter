package com.example.imposter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.Choreographer;
import android.view.View;

import java.util.Random;

/** Невесомый "полёт" с вращением, как выброс в Among Us. Без библиотек, один Bitmap 160x160. */
final class FlyView extends View implements Choreographer.FrameCallback {
    private final Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.sprite);
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    private final float dp = getResources().getDisplayMetrics().density;
    private float x, y, vx, vy, ang, av, t, base;
    private long last;
    private boolean started;

    FlyView(Context c) { super(c); }

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
        if (started) return;
        started = true;
        Random r = new Random();
        int dir = r.nextBoolean() ? 1 : -1;
        base = 56 * dp / bmp.getWidth();                 // иконка ~56dp
        x = w * (0.25f + 0.5f * r.nextFloat());
        y = h * (0.35f + 0.4f * r.nextFloat());
        vx = dir * (160 + r.nextInt(100)) * dp;          // px/s
        vy = -(40 + r.nextInt(120)) * dp;
        av = dir * (150 + r.nextInt(200));               // град/s
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Choreographer.getInstance().postFrameCallback(this);
    }

    @Override protected void onDetachedFromWindow() {
        Choreographer.getInstance().removeFrameCallback(this);
        super.onDetachedFromWindow();
    }

    @Override public void doFrame(long ns) {
        if (last == 0) last = ns;
        float dt = Math.min((ns - last) / 1e9f, 0.05f);
        last = ns;
        t += dt;
        x += vx * dt; y += vy * dt; ang += av * dt;
        float m = bmp.getWidth() * base;
        if (x < -m || x > getWidth() + m || y < -m || t > 8f) {
            ((Activity) getContext()).finish();
            ((Activity) getContext()).overridePendingTransition(0, 0);
            return;
        }
        invalidate();
        Choreographer.getInstance().postFrameCallback(this);
    }

    @Override protected void onDraw(Canvas c) {
        float s = base * Math.max(0.5f, 1f - 0.12f * t);   // чуть уменьшается — эффект глубины
        c.save();
        c.translate(x, y);
        c.rotate(ang);
        c.scale(s, s);
        c.drawBitmap(bmp, -bmp.getWidth() / 2f, -bmp.getHeight() / 2f, paint);
        c.restore();
    }
}
