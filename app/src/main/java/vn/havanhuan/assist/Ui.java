package vn.havanhuan.assist;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.content.res.ColorStateList;
import android.view.*;
import android.widget.*;
import java.util.*;

final class Ui {
    static final int WHITE=0xfff5f7fc, MUTED=0xffb3becd, ACCENT=0xff8debdb, BG=0xff12171f, GREEN=0xff80efa7;
    static int dp(Context c,float v){return Math.round(v*c.getResources().getDisplayMetrics().density);}
    static GradientDrawable surface(int color,float radius){GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{color,0xff151a23});g.setCornerRadius(radius);g.setStroke(1,0xff535d6c);return g;}
    static LinearLayout column(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(1);l.setPadding(dp(c,16),dp(c,12),dp(c,16),dp(c,12));return l;}
    static TextView text(Context c,String s,int sp,int color){TextView t=new TextView(c);t.setText(s);t.setTextSize(sp);t.setTextColor(color);t.setPadding(0,dp(c,6),0,dp(c,6));return t;}
    static Button button(Context c,String s,View.OnClickListener click){Button b=new Button(c);b.setText(s);b.setTextSize(14);b.setTextColor(WHITE);b.setAllCaps(false);b.setMinHeight(dp(c,48));b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x448debdb),surface(0xff2e3743,dp(c,14)),null));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(c,5),0,dp(c,5));b.setLayoutParams(lp);b.setOnClickListener(click);return b;}
    static void enter(View v,boolean side){if(!ValueAnimator.areAnimatorsEnabled())return;v.setAlpha(0);if(side)v.setTranslationX(-40);else {v.setScaleX(.88f);v.setScaleY(.88f);}v.animate().alpha(1).scaleX(1).scaleY(1).translationX(0).setDuration(280).start();}
    static class Glyph extends View {
        final Paint p=new Paint(3); final String kind;
        Glyph(Context c,String k){super(c);kind=k;setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onDraw(Canvas c){super.onDraw(c);float sx=getWidth()/64f,sy=getHeight()/64f;c.save();c.scale(sx,sy);p.setShader(null);p.setColor(WHITE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
            switch(kind){
                case "orb": p.setStyle(Paint.Style.FILL);p.setShader(new RadialGradient(27,22,35,new int[]{0xfff9fcff,0xffb9c2cd,0xff303741},new float[]{0,.45f,1},Shader.TileMode.CLAMP));c.drawCircle(32,32,30,p);p.setShader(null);p.setColor(0x997c8798);c.drawCircle(32,32,23,p);p.setShader(new LinearGradient(20,15,44,49,0xffffffff,0xffc3cedc,Shader.TileMode.CLAMP));c.drawCircle(32,32,17,p);break;
                case "image": c.drawRoundRect(8,10,56,54,9,9,p);c.drawCircle(43,23,4,p);Path a=new Path();a.moveTo(10,45);a.lineTo(25,29);a.lineTo(38,43);a.lineTo(45,36);a.lineTo(55,46);c.drawPath(a,p);break;
                case "fps": c.drawRoundRect(6,10,58,49,7,7,p);c.drawLine(23,56,41,56,p);Path b=new Path();b.moveTo(12,33);b.lineTo(22,33);b.lineTo(28,21);b.lineTo(36,41);b.lineTo(43,30);b.lineTo(51,30);c.drawPath(b,p);break;
                case "device": c.drawRoundRect(17,5,47,59,8,8,p);c.drawLine(27,11,37,11,p);c.drawLine(28,53,36,53,p);break;
                case "controls": c.drawLine(7,20,57,20,p);c.drawLine(7,44,57,44,p);p.setStyle(Paint.Style.FILL);c.drawCircle(42,20,7,p);c.drawCircle(22,44,7,p);break;
                case "touch": c.drawArc(12,6,48,42,180,180,false,p);c.drawArc(20,14,40,34,180,180,false,p);Path h=new Path();h.moveTo(27,43);h.lineTo(27,27);h.cubicTo(27,22,34,22,34,27);h.lineTo(34,39);h.cubicTo(48,30,54,48,43,56);h.cubicTo(33,64,19,46,19,43);h.cubicTo(18,36,23,36,27,43);c.drawPath(h,p);break;
                default: c.drawCircle(32,32,20,p);c.drawLine(32,5,32,18,p);c.drawLine(32,46,32,59,p);c.drawLine(5,32,18,32,p);c.drawLine(46,32,59,32,p);p.setStyle(Paint.Style.FILL);c.drawCircle(32,32,3,p);
            }c.restore();
        }
    }
    static class Chart extends View {
        final Paint paint=new Paint(3);final ArrayList<Float> values=new ArrayList<>();final int color;
        Chart(Context c,int color){super(c);this.color=color;setMinimumHeight(dp(c,40));}
        void push(float v){values.add(v);if(values.size()>40)values.remove(0);invalidate();setContentDescription("Mẫu mới nhất: "+Math.round(v)+" phần trăm");}
        @Override protected void onDraw(Canvas c){float w=getWidth(),h=getHeight();paint.setStrokeWidth(1);paint.setColor(0xff3a4554);for(int i=1;i<4;i++)c.drawLine(0,h*i/4,w,h*i/4,paint);if(values.isEmpty())return;Path path=new Path();for(int i=0;i<values.size();i++){float x=w*i/39f,y=h-(h-4)*values.get(i)/100f-2;if(i==0)path.moveTo(x,y);else path.lineTo(x,y);}paint.setStyle(Paint.Style.STROKE);paint.setColor(color);paint.setStrokeWidth(dp(getContext(),2));c.drawPath(path,paint);paint.setStyle(Paint.Style.FILL);}
    }
}
