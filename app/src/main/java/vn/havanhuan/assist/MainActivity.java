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
        LinearLayout box = Ui.column(this); box.setPadding(Ui.dp(this,28),Ui.dp(this,60),Ui.dp(this,28),24);
        box.addView(Ui.text(this,"HUAN / ASSIST",14,Ui.ACCENT));
        box.addView(Ui.text(this,"Điều khiển ngay trên màn hình",30,Ui.WHITE));
        box.addView(Ui.text(this,"Cấp quyền hiển thị trên ứng dụng khác. Sau đó cửa sổ nhập key sẽ xuất hiện trên màn hình chính.",16,Ui.MUTED));
        box.addView(Ui.button(this,"Cấp quyền & mở Assist",v -> enable()));
        setContentView(box);
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
