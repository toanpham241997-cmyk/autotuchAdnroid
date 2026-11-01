package vn.havanhuan.assist;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import java.util.ArrayList;

final class Ui {
    static final int WHITE = 0xfff5f7fc;
    static final int MUTED = 0xffb3becd;
    static final int ACCENT = 0xff8debdb;
    static final int BG = 0xff12171f;
    static final int GREEN = 0xff80efa7;

    static int dp(Context c, float value) {
        return Math.round(value * c.getResources().getDisplayMetrics().density);
    }

    static GradientDrawable surface(int color, float radius) {
        GradientDrawable background = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR, new int[]{color, 0xff151a23});
        background.setCornerRadius(radius);
        background.setStroke(1, 0xff535d6c);
        return background;
    }

    static LinearLayout column(Context c) {
        LinearLayout layout = new LinearLayout(c);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(c, 16), dp(c, 12), dp(c, 16), dp(c, 12));
        return layout;
    }

    static TextView text(Context c, String content, int sp, int color) {
        TextView text = new TextView(c);
        text.setText(content);
        text.setTextSize(sp);
        text.setTextColor(color);
        text.setPadding(0, dp(c, 6), 0, dp(c, 6));
        return text;
    }

    static Button button(Context c, String title, View.OnClickListener click) {
        Button button = new Button(c);
        button.setText(title);
        button.setTextSize(14);
        button.setTextColor(WHITE);
        button.setAllCaps(false);
        button.setMinHeight(dp(c, 48));
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(0x448debdb),
            surface(0xff2e3743, dp(c, 14)), null));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(c, 5), 0, dp(c, 5));
        button.setLayoutParams(lp);
        button.setOnClickListener(click);
        return button;
    }

    static void enter(View view, boolean side) {
        if (!ValueAnimator.areAnimatorsEnabled()) return;
        view.setAlpha(0);
        if (side) view.setTranslationX(-40);
        else {
            view.setScaleX(.88f);
            view.setScaleY(.88f);
        }
        view.animate().alpha(1).scaleX(1).scaleY(1).translationX(0)
            .setDuration(280).start();
    }

    static class Glyph extends View {
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        final String kind;

        Glyph(Context c, String kind) {
            super(c);
            this.kind = kind;
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            c.save();
            c.scale(getWidth() / 64f, getHeight() / 64f);
            p.setShader(null);
            p.setColor(WHITE);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            switch (kind) {
                case "orb":
                    p.setStyle(Paint.Style.FILL);
                    p.setShader(new RadialGradient(27,22,35,
                        new int[]{0xfff9fcff,0xffb9c2cd,0xff303741},
                        new float[]{0,.45f,1},Shader.TileMode.CLAMP));
                    c.drawCircle(32,32,30,p);
                    p.setShader(null);
                    p.setColor(0x997c8798);
                    c.drawCircle(32,32,23,p);
                    p.setShader(new LinearGradient(20,15,44,49,
                        0xffffffff,0xffc3cedc,Shader.TileMode.CLAMP));
                    c.drawCircle(32,32,17,p);
                    break;
                case "image":
                    c.drawRoundRect(8,10,56,54,9,9,p);
                    c.drawCircle(43,23,4,p);
                    Path image = new Path();
                    image.moveTo(10,45); image.lineTo(25,29);
                    image.lineTo(38,43); image.lineTo(45,36); image.lineTo(55,46);
                    c.drawPath(image,p);
                    break;
                case "fps":
                    c.drawRoundRect(6,10,58,49,7,7,p);
                    c.drawLine(23,56,41,56,p);
                    Path wave = new Path();
                    wave.moveTo(12,33); wave.lineTo(22,33); wave.lineTo(28,21);
                    wave.lineTo(36,41); wave.lineTo(43,30); wave.lineTo(51,30);
                    c.drawPath(wave,p);
                    break;
                case "device":
                    c.drawRoundRect(17,5,47,59,8,8,p);
                    c.drawLine(27,11,37,11,p); c.drawLine(28,53,36,53,p);
                    break;
                case "controls":
                    c.drawLine(7,20,57,20,p); c.drawLine(7,44,57,44,p);
                    p.setStyle(Paint.Style.FILL);
                    c.drawCircle(42,20,7,p); c.drawCircle(22,44,7,p);
                    break;
                case "touch":
                    c.drawArc(12,6,48,42,180,180,false,p);
                    c.drawArc(20,14,40,34,180,180,false,p);
                    Path hand = new Path();
                    hand.moveTo(27,43); hand.lineTo(27,27);
                    hand.cubicTo(27,22,34,22,34,27); hand.lineTo(34,39);
                    hand.cubicTo(48,30,54,48,43,56);
                    hand.cubicTo(33,64,19,46,19,43);
                    hand.cubicTo(18,36,23,36,27,43);
                    c.drawPath(hand,p);
                    break;
                default:
                    c.drawCircle(32,32,20,p);
                    c.drawLine(32,5,32,18,p); c.drawLine(32,46,32,59,p);
                    c.drawLine(5,32,18,32,p); c.drawLine(46,32,59,32,p);
                    p.setStyle(Paint.Style.FILL); c.drawCircle(32,32,3,p);
            }
            c.restore();
        }
    }

    static class Chart extends View {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        final ArrayList<Float> values = new ArrayList<>();
        final int color;

        Chart(Context c, int color) {
            super(c);
            this.color = color;
            setMinimumHeight(dp(c,40));
        }

        void push(float value) {
            values.add(value);
            if (values.size() > 40) values.remove(0);
            invalidate();
            setContentDescription("Mẫu mới nhất: " + Math.round(value) + " phần trăm");
        }

        @Override protected void onDraw(Canvas c) {
            float w = getWidth(), h = getHeight();
            paint.setStrokeWidth(1);
            paint.setColor(0xff3a4554);
            for (int i=1; i<4; i++) c.drawLine(0,h*i/4,w,h*i/4,paint);
            if (values.isEmpty()) return;
            Path line = new Path();
            for (int i=0; i<values.size(); i++) {
                float x=w*i/39f, y=h-(h-4)*values.get(i)/100f-2;
                if (i==0) line.moveTo(x,y); else line.lineTo(x,y);
            }
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(color);
            paint.setStrokeWidth(dp(getContext(),2));
            c.drawPath(line,paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }
}
