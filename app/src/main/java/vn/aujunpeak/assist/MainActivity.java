package vn.aujunpeak.assist;
import android.app.*; import android.os.*; import android.content.*; import android.net.Uri; import android.provider.Settings; import android.widget.*;
public class MainActivity extends Activity {
 boolean waiting;
 @Override public void onCreate(Bundle b){super.onCreate(b); if(!Settings.canDrawOverlays(this)){
 new AlertDialog.Builder(this).setTitle("Aujunpeak Assist").setMessage("Cấp quyền hiển thị trên ứng dụng khác để mở bảng nhập key ngoài màn hình chính.").setPositiveButton("Cấp quyền",(d,w)->{waiting=true;try{startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));}catch(Exception e){waiting=false;Toast.makeText(this,"Thiết bị không có trang cấp quyền overlay",Toast.LENGTH_LONG).show();finish();}}).setNegativeButton("Đóng",(d,w)->finish()).setOnCancelListener(d->finish()).show();
 }else launch();}
 @Override protected void onResume(){super.onResume();if(waiting){waiting=false;if(Settings.canDrawOverlays(this))launch();else{Toast.makeText(this,"Chưa cấp quyền hiển thị nổi",Toast.LENGTH_LONG).show();finish();}}}
 void launch(){try{startForegroundService(new Intent(this,OverlayService.class));startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}catch(Exception e){Toast.makeText(this,"Không thể mở overlay: "+e.getMessage(),Toast.LENGTH_LONG).show();}finish();}
}
