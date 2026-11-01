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
    static final int MUTED = 0xffa8b9cf;
    static final int ACCENT = 0xff63f5e4;
    static final int BG = 0xff0c1120;
    static final int GREEN = 0xff80efa7;
    static final int PURPLE = 0xffb7a0ff;
    static final int BORDER = 0xff30435e;
    static final int DANGER = 0xffffa1b8;

    static int dp(Context c, float value) {
        return Math.round(value * c.getResources().getDisplayMetrics().density);
    }

    static GradientDrawable surface(int color, float radius) {
        GradientDrawable background = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR, new int[]{color, 0xff101527});
        background.setCornerRadius(radius);
        background.setStroke(2, BORDER);
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
        text.setFontFeatureSettings("tnum");
        if (sp >= 20) text.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        text.setLineSpacing(dp(c, 2), 1.05f);
        text.setTextColor(color);
        text.setPadding(0, dp(c, 6), 0, dp(c, 6));
        return text;
    }

    static Button button(Context c, String title, View.OnClickListener click) {
        Button b = new Button(c);
        b.setText(title); b.setTextSize(14); b.setTextColor(WHITE); b.setAllCaps(false);
        b.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        b.setMinHeight(dp(c,48)); b.setMinimumHeight(dp(c,48));
        b.setPadding(dp(c,12),dp(c,8),dp(c,12),dp(c,8));
        b.setStateListAnimator(null);
        StateListDrawable states = new StateListDrawable();
        GradientDrawable selected = surface(0xff203448,dp(c,14)); selected.setStroke(dp(c,1),ACCENT);
        states.addState(new int[]{android.R.attr.state_selected},selected);
        states.addState(new int[]{android.R.attr.state_focused},selected);
        states.addState(new int[]{},surface(0xff172136,dp(c,14)));
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x3363f5e4),states,null));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0,dp(c,4),0,dp(c,4)); b.setLayoutParams(lp);
        b.setOnClickListener(v->{v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);click.onClick(v);});
        return b;
    }

    static Button primary(Context c,String title,View.OnClickListener click) {
        Button b=button(c,title,click);
        GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{ACCENT,0xff8fb8ff});
        bg.setCornerRadius(dp(c,14));
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x44ffffff),bg,null));
        b.setTextColor(0xff071321); return b;
    }

    static EditText input(Context c,String hint,int inputType) {
        EditText e=new EditText(c); e.setSingleLine(true); e.setTextSize(16);
        e.setTextColor(WHITE);e.setHintTextColor(MUTED);e.setHint(hint);e.setContentDescription(hint);
        e.setInputType(inputType);e.setMinHeight(dp(c,52));
        e.setPadding(dp(c,16),dp(c,10),dp(c,16),dp(c,10));
        GradientDrawable focused=surface(0xff132635,dp(c,14));focused.setStroke(dp(c,1),ACCENT);
        StateListDrawable state=new StateListDrawable();state.addState(new int[]{android.R.attr.state_focused},focused);
        state.addState(new int[]{},surface(0xff080d18,dp(c,14)));e.setBackground(state);
        return e;
    }

    static TextView badge(Context c,String title,int color) {
        TextView t=text(c,title,11,color);t.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        t.setLetterSpacing(.10f);t.setPadding(dp(c,10),dp(c,5),dp(c,10),dp(c,5));
        GradientDrawable bg=surface(0xff152337,dp(c,8));bg.setStroke(1,color);t.setBackground(bg);
        t.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));return t;
    }

    static void styleSwitch(Switch control) {
        control.setTextColor(WHITE);control.setTextSize(14);control.setMinHeight(dp(control.getContext(),48));
        ColorStateList colors=new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{ACCENT,MUTED});
        control.setThumbTintList(colors);control.setTrackTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{0xff276c6b,0xff344056}));
    }

    static void enter(View view,boolean side) { enterFrom(view,side?-1:0); }
    static void enterFrom(View view,int direction) {
        if(!ValueAnimator.areAnimatorsEnabled())return;
        view.setAlpha(0);view.setTranslationX(dp(view.getContext(),24)*direction);
        if(direction==0){view.setScaleX(.94f);view.setScaleY(.94f);view.setTranslationY(dp(view.getContext(),10));}
        view.animate().alpha(1).translationX(0).translationY(0).scaleX(1).scaleY(1)
            .setInterpolator(new android.view.animation.DecelerateInterpolator()).setDuration(240).start();
    }

    static class BoundedScroll extends ScrollView {
        final int maximum;
        BoundedScroll(Context c,int maximum){super(c);this.maximum=maximum;setFillViewport(false);setClipToPadding(false);}
        @Override protected void onMeasure(int widthSpec,int heightSpec){
            int available=MeasureSpec.getMode(heightSpec)==MeasureSpec.UNSPECIFIED?maximum:Math.min(maximum,MeasureSpec.getSize(heightSpec));
            super.onMeasure(widthSpec,MeasureSpec.makeMeasureSpec(available,MeasureSpec.AT_MOST));
        }
    }

    static class CrossPreview extends View {
        int style=0,size=24,color=ACCENT; boolean glow,rotate;
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);final Path path=new Path();
        CrossPreview(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);setContentDescription("Xem trước tâm ảo");setBackground(surface(0xff090e1b,dp(c,16)));}
        @Override protected void onDraw(Canvas c){
            float x=getWidth()/2f,y=getHeight()/2f,r=dp(getContext(),size)/2f,g=dp(getContext(),4);
            p.clearShadowLayer();p.setColor(BORDER);p.setStrokeWidth(1);p.setStyle(Paint.Style.STROKE);
            for(int i=dp(getContext(),16);i<getWidth();i+=dp(getContext(),24))c.drawLine(i,0,i,getHeight(),p);
            for(int i=dp(getContext(),16);i<getHeight();i+=dp(getContext(),24))c.drawLine(0,i,getWidth(),i,p);
            c.save();c.translate(x,y);
            boolean animate=rotate&&ValueAnimator.areAnimatorsEnabled();
            if(animate)c.rotate((android.os.SystemClock.uptimeMillis()%6000)*.06f);
            p.setColor(color);p.setStrokeWidth(dp(getContext(),2));
            if(glow)p.setShadowLayer(dp(getContext(),7),0,0,color);
            switch(style){
                case 1:p.setStyle(Paint.Style.FILL);c.drawCircle(0,0,Math.max(dp(getContext(),2),r/5),p);break;
                case 2:c.drawCircle(0,0,r,p);break;
                case 3:c.drawCircle(0,0,r,p);p.setStyle(Paint.Style.FILL);c.drawCircle(0,0,dp(getContext(),2),p);break;
                case 4:for(int i=0;i<4;i++){c.drawLine(-r,-r,-r/2,-r,p);c.drawLine(-r,-r,-r,-r/2,p);c.rotate(90);}break;
                case 5:path.reset();path.moveTo(0,-r);path.lineTo(r,0);path.lineTo(0,r);path.lineTo(-r,0);path.close();c.drawPath(path,p);break;
                default:c.drawLine(-r,0,-g,0,p);c.drawLine(g,0,r,0,p);c.drawLine(0,-r,0,-g,p);c.drawLine(0,g,0,r,p);
            }
            c.restore();p.clearShadowLayer();if(animate&&isShown())postInvalidateDelayed(33);
        }
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
            p.setColor(ACCENT);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            switch (kind) {
                case "orb":
                    p.setStyle(Paint.Style.FILL);p.setColor(0xff0c1427);c.drawCircle(32,32,29,p);
                    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);
                    p.setShader(new LinearGradient(7,7,57,57,ACCENT,PURPLE,Shader.TileMode.CLAMP));
                    p.setShadowLayer(5,0,0,0x9963f5e4);c.drawCircle(32,32,25,p);p.clearShadowLayer();
                    p.setShader(null);p.setColor(0xff334c63);p.setStrokeWidth(1);c.drawCircle(32,32,19,p);
                    p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(20,18,44,46,ACCENT,0xffa5b6ff,Shader.TileMode.CLAMP));
                    c.drawRoundRect(22,22,42,42,7,7,p);p.setShader(null);p.setColor(0xff081521);c.drawCircle(32,32,4,p);
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
        final Path line = new Path();
        final Path fill = new Path();

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
            line.reset();
            for (int i=0; i<values.size(); i++) {
                float x=w*i/39f, y=h-(h-4)*values.get(i)/100f-2;
                if (i==0) line.moveTo(x,y); else line.lineTo(x,y);
            }
            fill.set(line);fill.lineTo(w*(values.size()-1)/39f,h);fill.lineTo(0,h);fill.close();
            paint.setStyle(Paint.Style.FILL);paint.setColor((color & 0x00ffffff) | 0x22000000);c.drawPath(fill,paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(color);
            paint.setStrokeWidth(dp(getContext(),2));
            c.drawPath(line,paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }
}
