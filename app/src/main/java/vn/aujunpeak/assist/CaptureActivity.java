package vn.aujunpeak.assist;
import android.app.*;import android.os.*;import android.content.*;import android.media.projection.*;
public class CaptureActivity extends Activity {
 @Override public void onCreate(Bundle b){super.onCreate(b);if(b==null)new AlertDialog.Builder(this).setTitle("Chụp màn hình").setMessage("Cho phép chụp một ảnh sau 3 giây. Ảnh chỉ lưu trong ứng dụng; không nhận diện hay theo dõi nhân vật. Chọn toàn bộ màn hình nếu muốn chụp game.").setPositiveButton("Tiếp tục",(d,w)->startActivityForResult(((MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE)).createScreenCaptureIntent(),9)).setNegativeButton("Hủy",(d,w)->finish()).setOnCancelListener(d->finish()).show();}
 @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(req==9&&result==RESULT_OK&&data!=null){Intent i=new Intent(this,CaptureService.class).putExtra("code",result).putExtra("data",data);startForegroundService(i);}finish();}
}
