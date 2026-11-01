package vn.havanhuan.assist;

import android.app.*;
import android.os.*;
import android.content.*;
import android.net.Uri;
import android.provider.Settings;
import android.media.projection.*;
import android.widget.*;

public class MainActivity extends Activity {
    private boolean awaitingOverlay, launched, notificationAsked;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if ("capture".equals(getIntent().getAction())) {
            if (OverlayService.active == null || !OverlayService.active.unlocked) { finish(); return; }
            MediaProjectionManager m = getSystemService(MediaProjectionManager.class);
            Intent consent = Build.VERSION.SDK_INT >= 34 ? m.createScreenCaptureIntent(MediaProjectionConfig.createConfigForDefaultDisplay()) : m.createScreenCaptureIntent();
            startActivityForResult(consent, 42);
            return;
        }
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        scroll.setBackground(Ui.surface(0xff090e1c,0));
        scroll.setOnApplyWindowInsetsListener((v,insets)->{
            v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;
        });
        LinearLayout page=Ui.column(this);page.setGravity(android.view.Gravity.CENTER);
        page.setPadding(Ui.dp(this,20),Ui.dp(this,28),Ui.dp(this,20),Ui.dp(this,28));
        LinearLayout box=Ui.column(this);box.setPadding(Ui.dp(this,24),Ui.dp(this,24),Ui.dp(this,24),Ui.dp(this,24));
        box.setBackground(Ui.surface(Ui.BG,Ui.dp(this,24)));
        box.addView(Ui.badge(this,"HUAN / NEON",Ui.ACCENT));
        Ui.Glyph hero=new Ui.Glyph(this,"orb");hero.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams heroSize=new LinearLayout.LayoutParams(Ui.dp(this,80),Ui.dp(this,80));heroSize.topMargin=Ui.dp(this,20);box.addView(hero,heroSize);
        box.addView(Ui.text(this,"Không gian\ncủa bạn.",32,Ui.WHITE));
        box.addView(Ui.text(this,"Một chạm để mở bộ công cụ trên màn hình.",16,Ui.MUTED));
        LinearLayout info=Ui.column(this);info.setBackground(Ui.surface(0xff142437,Ui.dp(this,16)));
        info.addView(Ui.text(this,"01  Cho phép hiển thị nổi",14,Ui.ACCENT));
        info.addView(Ui.text(this,"02  Nhập key trên màn hình chính",14,Ui.WHITE));
        info.addView(Ui.text(this,"03  Mở menu và chọn công cụ",14,Ui.WHITE));
        LinearLayout.LayoutParams infoSize=new LinearLayout.LayoutParams(-1,-2);infoSize.setMargins(0,Ui.dp(this,16),0,Ui.dp(this,16));box.addView(info,infoSize);
        box.addView(Ui.primary(this,"Cấp quyền & bắt đầu",v->enable()));
        box.addView(Ui.text(this,"Bạn có thể dừng Assist bất cứ lúc nào từ menu hoặc thông báo.",12,Ui.MUTED));
        int width=Math.min(Ui.dp(this,420),getResources().getDisplayMetrics().widthPixels-Ui.dp(this,40));
        page.addView(box,new LinearLayout.LayoutParams(width,-2));scroll.addView(page);setContentView(scroll);Ui.enter(box,false);
        if (Settings.canDrawOverlays(this)) enable();
    }
    private void enable() {
        if (!Settings.canDrawOverlays(this)) {
            awaitingOverlay=true;
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));
        } else if (!launched) {
            if(Build.VERSION.SDK_INT>=33 && !notificationAsked && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED){notificationAsked=true;requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS},43);return;}
            launched=true;
            startForegroundService(new Intent(this,OverlayService.class));
            startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        }
    }
    @Override protected void onResume() { super.onResume(); if (awaitingOverlay && Settings.canDrawOverlays(this)) enable(); }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){super.onRequestPermissionsResult(request,permissions,results);if(request==43)enable();}
    @Override protected void onActivityResult(int r,int result,Intent data) {
        super.onActivityResult(r,result,data);
        if (r==42) {
            if(result==RESULT_OK && data!=null) startForegroundService(new Intent(this,OverlayService.class).setAction("projection").putExtra("result",result).putExtra("data",data));
            else if(OverlayService.active!=null) OverlayService.active.toast("Đã hủy chia sẻ màn hình",false);
            finish();
        }
    }
}
