package vn.havanhuan.assist;

import android.app.*;
import android.app.usage.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.graphics.*;
import android.os.*;
import android.provider.Settings;
import android.text.InputType;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.util.*;

public class OverlayService extends Service {
    static OverlayService active;
    boolean unlocked;
    final Handler main=new Handler(Looper.getMainLooper());
    WindowManager wm; android.content.SharedPreferences prefs;
    View orb,panel,stats,cursor,notice,selector;
    Hud hud; Capture capture; TextView ramText,cpuText,batteryText,deviceText; Ui.Chart ramChart,cpuChart,batteryChart;
    ImageView deviceImage;
    int screenW,screenH; int crossStyle,crossSize=24,crossX,crossY,crossColor=Ui.ACCENT; boolean crossOn,glow,rotate;
    int pointerX,pointerY; long lastWall,lastCpu; boolean tracking;
    final Set<View> windows=new HashSet<>();
    final Runnable sample=new Runnable(){public void run(){if(!Settings.canDrawOverlays(OverlayService.this)){stopSelf();return;}updateMetrics();main.postDelayed(this,1000);}};
    @Override public void onCreate(){super.onCreate();active=this;wm=getSystemService(WindowManager.class);prefs=getSharedPreferences("assist",MODE_PRIVATE);dimensions();load();notification(false);main.post(sample);}
    private void dimensions(){android.util.DisplayMetrics d=new android.util.DisplayMetrics();wm.getDefaultDisplay().getRealMetrics(d);screenW=d.widthPixels;screenH=d.heightPixels;}
    private void load(){crossStyle=prefs.getInt("style",0);crossSize=prefs.getInt("size",24);crossX=prefs.getInt("x",0);crossY=prefs.getInt("y",0);crossColor=prefs.getInt("color",Ui.ACCENT);glow=prefs.getBoolean("glow",false);rotate=prefs.getBoolean("rotate",false);pointerX=prefs.getInt("pointerX",screenW/2);pointerY=prefs.getInt("pointerY",screenH/2);}
    private void save(){prefs.edit().putInt("style",crossStyle).putInt("size",crossSize).putInt("x",crossX).putInt("y",crossY).putInt("color",crossColor).putBoolean("glow",glow).putBoolean("rotate",rotate).apply();}
    void notification(boolean projecting){
        NotificationManager nm=getSystemService(NotificationManager.class);nm.createNotificationChannel(new NotificationChannel("assist","Điều khiển nổi",NotificationManager.IMPORTANCE_LOW));
        PendingIntent stop=PendingIntent.getService(this,1,new Intent(this,OverlayService.class).setAction("stop"),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent open=PendingIntent.getActivity(this,2,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        Notification n=new Notification.Builder(this,"assist").setSmallIcon(vn.havanhuan.assist.R.drawable.ic_app).setContentTitle("Huan Assist đang hoạt động").setContentText(projecting?"Đang chia sẻ màn hình — có thể dừng bất kỳ lúc nào":"Chạm nút nổi để mở công cụ").setContentIntent(open).setOngoing(true).addAction(new Notification.Action.Builder(null,"Dừng tất cả",stop).build()).build();
        if(Build.VERSION.SDK_INT>=34)startForeground(7,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE|(projecting?ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION:0));else if(Build.VERSION.SDK_INT>=29&&projecting)startForeground(7,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);else startForeground(7,n);
    }
    @Override public int onStartCommand(Intent intent,int flags,int id){
        if(intent!=null&&"stop".equals(intent.getAction())){stopSelf();return START_NOT_STICKY;}
        if(!Settings.canDrawOverlays(this)){stopSelf();return START_NOT_STICKY;}
        if(intent!=null&&"projection".equals(intent.getAction())&&unlocked){
            try {if(capture!=null)capture.close();notification(true);capture=new Capture(this,intent.getIntExtra("result",-1),intent.getParcelableExtra("data"));capture.start();main.postDelayed(()->{if(capture!=null)selectRegion();},700);}
            catch(RuntimeException e){if(capture!=null)capture.close();capture=null;notification(false);toast("Không thể chia sẻ màn hình. Hãy thử lại.",false);}
        }else if(orb==null&&panel==null){if(unlocked)showOrb();else login();}
        return START_NOT_STICKY;
    }
    @Override public IBinder onBind(Intent i){return null;}
    int dp(float n){return Ui.dp(this,n);}
    WindowManager.LayoutParams params(int w,int h,int gravity,boolean focus,boolean touch){
        int flags=WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        if(!focus)flags|=WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        if(!touch)flags|=WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        WindowManager.LayoutParams lp=new WindowManager.LayoutParams(w,h,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,flags,PixelFormat.TRANSLUCENT);lp.gravity=gravity;lp.softInputMode=WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;return lp;
    }
    void add(View v,WindowManager.LayoutParams lp){try{wm.addView(v,lp);windows.add(v);}catch(RuntimeException e){stopSelf();}}
    void remove(View v){if(v!=null&&windows.remove(v)){v.animate().cancel();try{wm.removeViewImmediate(v);}catch(IllegalArgumentException ignored){}}}
    void dismiss(){remove(panel);panel=null;deviceText=null;deviceImage=null;remove(cursor);cursor=null;}
    void collapse() {
        View old=panel;panel=null;deviceText=null;deviceImage=null;remove(cursor);cursor=null;
        if(old==null)return;
        if(!android.animation.ValueAnimator.areAnimatorsEnabled()){remove(old);return;}
        old.animate().alpha(0).translationY(dp(8)).setDuration(160).withEndAction(()->remove(old)).start();
    }
    LinearLayout sheet(String title,String subtitle,int gravity,boolean focus) {
        dismiss();
        LinearLayout frame=Ui.column(this);frame.setPadding(0,0,0,dp(4));
        frame.setBackground(Ui.surface(Ui.BG,dp(22)));
        LinearLayout header=Ui.column(this);header.setPadding(dp(18),dp(14),dp(18),dp(8));
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand=Ui.badge(this,"HUAN / NEON",Ui.ACCENT);
        top.addView(brand);
        View space=new View(this);top.addView(space,new LinearLayout.LayoutParams(0,1,1));
        Button close=Ui.button(this,"Đóng",v->{if(unlocked)collapse();else stopSelf();});
        top.addView(close,new LinearLayout.LayoutParams(dp(68),dp(48)));
        header.addView(top);header.addView(Ui.text(this,title,22,Ui.WHITE));
        if(subtitle!=null)header.addView(Ui.text(this,subtitle,13,Ui.MUTED));
        frame.addView(header);
        View separator=new View(this);separator.setBackgroundColor(Ui.BORDER);
        frame.addView(separator,new LinearLayout.LayoutParams(-1,dp(1)));
        Ui.BoundedScroll scroll=new Ui.BoundedScroll(this,Math.max(dp(96),screenH-dp(214)));
        LinearLayout box=Ui.column(this);box.setPadding(dp(16),dp(12),dp(16),dp(16));
        scroll.addView(box);frame.addView(scroll);panel=frame;
        int width=Math.min(dp(356),screenW-dp(24));
        WindowManager.LayoutParams lp=params(width,WindowManager.LayoutParams.WRAP_CONTENT,gravity,focus,true);
        lp.y=gravity==Gravity.CENTER?0:dp(24);add(frame,lp);
        Ui.enterFrom(frame,gravity==Gravity.CENTER?0:((gravity&Gravity.RIGHT)==Gravity.RIGHT?1:-1));
        return box;
    }
    void closeButton(LinearLayout b) { /* Every sheet has a pinned close action. */ }
    void login() {
        LinearLayout b=sheet("Mở khóa Assist","Công cụ của bạn, ngay trên màn hình.",Gravity.CENTER,true);
        b.addView(Ui.text(this,"KEY TRUY CẬP",12,Ui.MUTED));
        EditText key=Ui.input(this,"Nhập key của bạn",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        key.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_DONE);b.addView(key);
        CheckBox show=new CheckBox(this);show.setText("Hiển thị key");show.setTextColor(Ui.MUTED);show.setTextSize(13);show.setMinHeight(dp(48));
        show.setButtonTintList(android.content.res.ColorStateList.valueOf(Ui.ACCENT));
        show.setOnCheckedChangeListener((v,on)->{int selection=key.getSelectionEnd();key.setTransformationMethod(on?null:android.text.method.PasswordTransformationMethod.getInstance());key.setSelection(Math.max(0,selection));});b.addView(show);
        TextView error=Ui.text(this,"",13,Ui.DANGER);error.setVisibility(View.GONE);error.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);b.addView(error);
        Button login=Ui.primary(this,"Đăng nhập",v->{
            if(!"havanhuan".equals(key.getText().toString())){
                error.setText("Key chưa đúng. Kiểm tra rồi thử lại.");error.setVisibility(View.VISIBLE);
                key.requestFocus();if(android.animation.ValueAnimator.areAnimatorsEnabled()){
                    android.animation.ObjectAnimator.ofFloat(key,"translationX",0,-dp(5),dp(5),-dp(3),0).setDuration(240).start();
                }return;
            }
            getSystemService(InputMethodManager.class).hideSoftInputFromWindow(key.getWindowToken(),0);
            unlocked=true;dismiss();showOrb();toast("Đăng nhập thành công",true);
        });b.addView(login);
        key.setOnEditorActionListener((v,action,event)->{if(action==android.view.inputmethod.EditorInfo.IME_ACTION_DONE){login.performClick();return true;}return false;});
        TextView note=Ui.text(this,"Nhấn Đóng để dừng cửa sổ nổi.",12,Ui.MUTED);note.setGravity(Gravity.CENTER);b.addView(note);
    }

    void showOrb(){if(orb!=null)return;Ui.Glyph g=new Ui.Glyph(this,"orb");g.setContentDescription("Mở menu AssistiveTouch; kéo để di chuyển");orb=g;
        WindowManager.LayoutParams lp=params(dp(56),dp(56),Gravity.TOP|Gravity.LEFT,false,true);lp.x=Math.max(0,Math.min(screenW-dp(56),prefs.getInt("orbX",screenW-dp(68))));lp.y=Math.max(dp(28),Math.min(screenH-dp(80),prefs.getInt("orbY",screenH/3)));add(g,lp);g.setOnClickListener(v->{if(panel!=null)collapse();else menu();});drag(g,lp,()->prefs.edit().putInt("orbX",lp.x).putInt("orbY",lp.y).apply());Ui.enter(g,false);}
    void drag(View view,WindowManager.LayoutParams lp,Runnable end){view.setOnTouchListener(new View.OnTouchListener(){float x,y;int ox,oy;boolean moved;public boolean onTouch(View v,android.view.MotionEvent e){switch(e.getActionMasked()){case MotionEvent.ACTION_DOWN:x=e.getRawX();y=e.getRawY();ox=lp.x;oy=lp.y;moved=false;return true;case MotionEvent.ACTION_MOVE:float dx=e.getRawX()-x,dy=e.getRawY()-y;if(Math.abs(dx)+Math.abs(dy)>dp(6))moved=true;if(moved){lp.x=Math.max(0,Math.min(screenW-lp.width,ox+(int)dx));lp.y=Math.max(dp(24),Math.min(screenH-lp.height-dp(24),oy+(int)dy));if(windows.contains(v))wm.updateViewLayout(v,lp);}return true;case MotionEvent.ACTION_UP:if(!moved)v.performClick();else end.run();return true;case MotionEvent.ACTION_CANCEL:return true;}return false;}});}
    void menu(){
        LinearLayout b=sheet("Trung tâm điều khiển","Chọn công cụ · kéo nút nổi để di chuyển",Gravity.CENTER,false);
        row(b,new String[]{"Hình ảnh","FPS / Thông số"},new String[]{"image","fps"},new Runnable[]{this::imageMenu,this::toggleStats});
        row(b,new String[]{"Thiết bị","Điều khiển"},new String[]{"device","controls"},new Runnable[]{this::device,this::controls});
        row(b,new String[]{"Sensitivity Touch","Tâm ảo"},new String[]{"touch","cross"},new Runnable[]{this::sensitivity,this::crossMenu});
        Button stop=Ui.button(this,"Dừng Assist",v->stopSelf());stop.setTextColor(Ui.DANGER);b.addView(stop);
    }
    void row(LinearLayout parent,String[] labels,String[] icons,Runnable[] actions){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);
        for(int i=0;i<labels.length;i++){
            LinearLayout tile=Ui.column(this);tile.setPadding(dp(8),dp(12),dp(8),dp(10));tile.setGravity(Gravity.CENTER);
            Ui.Glyph icon=new Ui.Glyph(this,icons[i]);icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            tile.addView(icon,new LinearLayout.LayoutParams(dp(36),dp(36)));
            TextView label=Ui.text(this,labels[i],13,Ui.WHITE);label.setGravity(Gravity.CENTER);tile.addView(label);
            android.graphics.drawable.GradientDrawable bg=Ui.surface(i%2==0?0xff142738:0xff201e3b,dp(16));
            bg.setStroke(dp(1),i%2==0?0xff2d6470:0xff51466e);
            tile.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x3363f5e4),bg,null));
            final Runnable action=actions[i];tile.setOnClickListener(v->{v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);action.run();});
            tile.setFocusable(true);tile.setContentDescription(labels[i]);
            LinearLayout.LayoutParams cell=new LinearLayout.LayoutParams(0,-1,1);cell.setMargins(dp(4),dp(4),dp(4),dp(4));row.addView(tile,cell);
        }
        parent.addView(row,new LinearLayout.LayoutParams(-1,-2));
    }

    void toast(String text,boolean success){remove(notice);LinearLayout box=Ui.column(this);box.setBackground(Ui.surface(0xff102a2c,dp(16)));TextView t=Ui.text(this,(success?"✓  ":"")+text,14,success?Ui.GREEN:Ui.WHITE);t.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);box.addView(t);notice=box;WindowManager.LayoutParams lp=params(Math.min(dp(288),screenW-dp(32)),-2,Gravity.TOP|Gravity.LEFT,false,false);lp.x=dp(16);lp.y=dp(48);lp.alpha=.4f;add(box,lp);Ui.enter(box,true);main.postDelayed(()->{if(!windows.contains(box))return;box.animate().alpha(0).setDuration(300).withEndAction(()->remove(box)).start();},2700);}
    void ensureHud(){if(hud!=null)return;hud=new Hud(this);WindowManager.LayoutParams lp=params(-1,-1,Gravity.TOP|Gravity.LEFT,false,false);lp.alpha=.6f;add(hud,lp);}
    void toggleStats(){dismiss();if(stats!=null){remove(stats);stats=null;ramText=cpuText=batteryText=null;return;}LinearLayout row=new LinearLayout(this);row.setPadding(dp(8),dp(6),dp(8),dp(6));row.setBackground(Ui.surface(Ui.BG,dp(18)));String[] titles={"RAM thiết bị","CPU Assist","Pin"};int[] colors={Ui.ACCENT,0xffbda8ff,Ui.GREEN};for(int i=0;i<3;i++){LinearLayout c=Ui.column(this);c.setPadding(dp(10),dp(6),dp(10),dp(6));c.setBackground(Ui.surface(0xff122238,dp(12)));c.addView(Ui.text(this,titles[i],12,Ui.MUTED));TextView value=Ui.text(this,"Đang đo…",18,colors[i]);c.addView(value);Ui.Chart chart=new Ui.Chart(this,colors[i]);c.addView(chart,new LinearLayout.LayoutParams(-1,dp(36)));LinearLayout.LayoutParams cardParams=new LinearLayout.LayoutParams(0,-2,1);cardParams.setMargins(dp(3),0,dp(3),0);row.addView(c,cardParams);if(i==0){ramText=value;ramChart=chart;}if(i==1){cpuText=value;cpuChart=chart;}if(i==2){batteryText=value;batteryChart=chart;}}stats=row;WindowManager.LayoutParams lp=params(Math.min(screenW-dp(32),dp(540)),-2,Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,false,true);lp.y=dp(20);add(row,lp);row.setContentDescription("Biểu đồ RAM, CPU tiến trình Assist và pin. Chạm để đóng.");row.setOnClickListener(v->toggleStats());Ui.enter(row,false);updateMetrics();}
    float ramPercent,cpuPercent;int battery=-1;String foreground="Chưa cấp quyền xem ứng dụng";
    void updateMetrics(){ActivityManager.MemoryInfo mi=new ActivityManager.MemoryInfo();getSystemService(ActivityManager.class).getMemoryInfo(mi);ramPercent=100f*(mi.totalMem-mi.availMem)/mi.totalMem;long now=SystemClock.elapsedRealtime(),cpu=android.os.Process.getElapsedCpuTime();if(lastWall>0&&now>lastWall)cpuPercent=Math.min(100,100f*(cpu-lastCpu)/(now-lastWall)/Runtime.getRuntime().availableProcessors());lastWall=now;lastCpu=cpu;battery=getSystemService(BatteryManager.class).getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        if(ramText!=null){ramText.setText(String.format(Locale.getDefault(),"%.0f%%",ramPercent));cpuText.setText(String.format(Locale.getDefault(),"%.1f%%",cpuPercent));batteryText.setText(battery>=0&&battery<=100?battery+"%":"N/A");ramChart.push(ramPercent);cpuChart.push(cpuPercent);if(battery>=0&&battery<=100)batteryChart.push(battery);}
        if(deviceText!=null){foreground=currentApp();deviceText.setText(Build.MANUFACTURER+" "+Build.MODEL+"\nAndroid "+Build.VERSION.RELEASE+" · "+screenW+" × "+screenH+"\nRAM "+Math.round(ramPercent)+"% · CPU Assist "+String.format(Locale.getDefault(),"%.1f",cpuPercent)+"%\nPin "+(battery>=0&&battery<=100?battery+"%":"N/A")+"\nỨng dụng: "+foreground);}
    }
    String currentApp(){AppOpsManager ops=getSystemService(AppOpsManager.class);if(ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(),getPackageName())!=AppOpsManager.MODE_ALLOWED)return "Chưa cấp quyền xem ứng dụng";UsageEvents events=getSystemService(UsageStatsManager.class).queryEvents(System.currentTimeMillis()-24*60*60*1000L,System.currentTimeMillis());UsageEvents.Event event=new UsageEvents.Event();String found="Không xác định";while(events.hasNextEvent()){events.getNextEvent(event);if(event.getEventType()==UsageEvents.Event.MOVE_TO_FOREGROUND)found=event.getPackageName();}return found;}
    void device(){LinearLayout b=sheet("Thiết bị","Thông tin trực tiếp · cập nhật mỗi giây",Gravity.TOP|Gravity.LEFT,false);LinearLayout phone=Ui.column(this);phone.setBackground(Ui.surface(0xff0b1325,dp(24)));TextView camera=Ui.text(this,"●",14,Ui.MUTED);camera.setGravity(Gravity.CENTER);phone.addView(camera);deviceImage=new ImageView(this);deviceImage.setScaleType(ImageView.ScaleType.FIT_CENTER);deviceImage.setContentDescription("Ảnh màn hình chia sẻ gần nhất");if(capture!=null&&capture.latest!=null)deviceImage.setImageBitmap(capture.latest);phone.addView(deviceImage,new LinearLayout.LayoutParams(-1,dp(140)));deviceText=Ui.text(this,"",14,Ui.WHITE);phone.addView(deviceText);b.addView(phone);if(capture==null)b.addView(Ui.text(this,"Chưa có ảnh preview · chọn Chia sẻ màn hình bên dưới.",13,Ui.PURPLE));b.addView(Ui.text(this,"Ảnh preview lấy từ phiên chia sẻ màn hình. CPU là tiến trình Assist; Android hạn chế đọc CPU toàn hệ thống và FPS của game.",12,Ui.MUTED));b.addView(Ui.button(this,"Cấp quyền tên ứng dụng",v->{dismiss();startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}));b.addView(Ui.primary(this,"Chia sẻ màn hình / chọn ảnh",v->startCapture()));closeButton(b);updateMetrics();}
    interface Change{void set(int v);}
    void slider(LinearLayout b,String title,int min,int max,int initial,Change change){TextView label=Ui.text(this,title+": "+initial,13,Ui.MUTED);b.addView(label);SeekBar s=new SeekBar(this);s.setMax(max-min);s.setProgress(Math.max(0,Math.min(max-min,initial-min)));s.setContentDescription(title);s.setMinimumHeight(dp(48));s.setProgressTintList(android.content.res.ColorStateList.valueOf(Ui.ACCENT));s.setThumbTintList(android.content.res.ColorStateList.valueOf(Ui.ACCENT));s.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Ui.BORDER));s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int v,boolean user){if(user){label.setText(title+": "+(v+min));change.set(v+min);}}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});b.addView(s);}
    void crossMenu(){
        ensureHud();LinearLayout b=sheet("Tâm ảo","Thiết kế tâm theo cách của bạn",Gravity.TOP|Gravity.RIGHT,false);
        Ui.CrossPreview preview=new Ui.CrossPreview(this);
        Runnable refresh=()->{preview.style=crossStyle;preview.size=crossSize;preview.color=crossColor;preview.glow=glow;preview.rotate=rotate;preview.invalidate();hud.invalidate();};
        b.addView(preview,new LinearLayout.LayoutParams(-1,dp(112)));refresh.run();
        Switch enabled=new Switch(this);enabled.setText("Hiển thị trên màn hình");Ui.styleSwitch(enabled);enabled.setChecked(crossOn);
        enabled.setOnCheckedChangeListener((v,on)->{crossOn=on;refresh.run();});b.addView(enabled);
        b.addView(Ui.text(this,"KIỂU TÂM",12,Ui.MUTED));
        String[] styles={"Dấu cộng","Chấm","Vòng tròn","Vòng + chấm","Bốn góc","Kim cương"};
        ArrayList<Button> styleButtons=new ArrayList<>();
        for(int rowIndex=0;rowIndex<3;rowIndex++){
            LinearLayout row=new LinearLayout(this);
            for(int col=0;col<2;col++){
                final int type=rowIndex*2+col;
                Button choice=Ui.button(this,styles[type],v->{crossStyle=type;crossOn=true;enabled.setChecked(true);for(int k=0;k<styleButtons.size();k++)styleButtons.get(k).setSelected(k==type);refresh.run();});
                choice.setSelected(crossStyle==type);styleButtons.add(choice);
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(choice,lp);
            }b.addView(row);
        }
        slider(b,"Kích thước",8,80,crossSize,v->{crossSize=v;refresh.run();});
        slider(b,"Ngang X (px)",-screenW/2+dp(48),screenW/2-dp(48),crossX,v->{crossX=v;refresh.run();});
        slider(b,"Dọc Y (px)",-screenH/2+dp(48),screenH/2-dp(48),crossY,v->{crossY=v;refresh.run();});
        b.addView(Ui.button(this,"Đưa về giữa",v->{crossX=crossY=0;crossMenu();}));
        b.addView(Ui.text(this,"MÀU SẮC",12,Ui.MUTED));
        int[] colors={Ui.ACCENT,Ui.PURPLE,Ui.WHITE,0xffffdf85};String[] names={"Cyan","Tím neon","Trắng","Vàng"};
        ArrayList<Button> colorButtons=new ArrayList<>();
        for(int rowIndex=0;rowIndex<2;rowIndex++){
            LinearLayout row=new LinearLayout(this);
            for(int col=0;col<2;col++){
                final int index=rowIndex*2+col;
                Button choice=Ui.button(this,names[index],v->{crossColor=colors[index];for(int k=0;k<colorButtons.size();k++)colorButtons.get(k).setSelected(k==index);refresh.run();});
                choice.setTextColor(colors[index]);choice.setSelected(crossColor==colors[index]);colorButtons.add(choice);
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(choice,lp);
            }b.addView(row);
        }
        Switch g=new Switch(this);g.setText("Phát sáng");Ui.styleSwitch(g);g.setChecked(glow);g.setOnCheckedChangeListener((v,on)->{glow=on;refresh.run();});b.addView(g);
        Switch r=new Switch(this);r.setText("Xoay nhẹ");Ui.styleSwitch(r);r.setChecked(rotate);r.setOnCheckedChangeListener((v,on)->{rotate=on;refresh.run();});b.addView(r);
        b.addView(Ui.primary(this,"Lưu tâm ảo",v->{save();collapse();toast("Đã lưu tâm ảo",true);}));
    }

    void sensitivity(){LinearLayout b=sheet("Sensitivity Touch","Đặt mốc tọa độ. Không thay đổi độ nhạy của game.",Gravity.TOP|Gravity.LEFT,false);Ui.Glyph g=new Ui.Glyph(this,"cross");cursor=g;WindowManager.LayoutParams lp=params(dp(48),dp(48),Gravity.TOP|Gravity.LEFT,false,true);lp.x=Math.max(0,Math.min(screenW-dp(48),pointerX));lp.y=Math.max(dp(24),Math.min(screenH-dp(72),pointerY));add(g,lp);g.setContentDescription("Mốc tọa độ; kéo để đặt vị trí");TextView position=Ui.text(this,"X: "+lp.x+" · Y: "+lp.y,14,Ui.WHITE);b.addView(position);drag(g,lp,()->position.setText("X: "+lp.x+" · Y: "+lp.y));
        slider(b,"X",0,screenW-dp(48),lp.x,v->{lp.x=v;wm.updateViewLayout(g,lp);position.setText("X: "+lp.x+" · Y: "+lp.y);});slider(b,"Y",dp(24),screenH-dp(72),lp.y,v->{lp.y=v;wm.updateViewLayout(g,lp);position.setText("X: "+lp.x+" · Y: "+lp.y);});b.addView(Ui.primary(this,"Lưu & ẩn con trỏ",v->{pointerX=lp.x;pointerY=lp.y;prefs.edit().putInt("pointerX",pointerX).putInt("pointerY",pointerY).apply();dismiss();toast("Đã lưu tọa độ",true);}));closeButton(b);}
    EditText number(LinearLayout b,String label,int value){
        b.addView(Ui.text(this,label,13,Ui.MUTED));EditText e=Ui.input(this,label,InputType.TYPE_CLASS_NUMBER);
        e.setText(String.valueOf(value));b.addView(e);return e;
    }

    void controls(){LinearLayout b=sheet("Điều khiển","Hồ sơ mục tiêu & preview",Gravity.TOP|Gravity.LEFT,true);b.addView(Ui.text(this,Build.MODEL+" · "+screenW+" × "+screenH+"\nMật độ hiện tại: "+getResources().getDisplayMetrics().densityDpi+" dpi\nTần số màn hình: "+Math.round(wm.getDefaultDisplay().getRefreshRate())+" Hz",14,Ui.WHITE));EditText fps=number(b,"FPS mục tiêu (30–240)",prefs.getInt("fps",60));EditText dpi=number(b,"DPI mục tiêu (120–960)",prefs.getInt("dpi",getResources().getDisplayMetrics().densityDpi));TextView preview=Ui.text(this,"",14,Ui.ACCENT);b.addView(preview);b.addView(Ui.text(this,"Đây là hồ sơ tham khảo. App thường không thể ép FPS, DPI hoặc độ nhạy của Free Fire. Hãy áp dụng thiết lập được game/thiết bị hỗ trợ trong cài đặt của chúng.",13,Ui.MUTED));b.addView(Ui.primary(this,"Xem preview & lưu hồ sơ",v->{try{int f=Integer.parseInt(fps.getText().toString()),d=Integer.parseInt(dpi.getText().toString());if(f<30||f>240){fps.setError("Nhập 30–240");return;}if(d<120||d>960){dpi.setError("Nhập 120–960");return;}prefs.edit().putInt("fps",f).putInt("dpi",d).apply();preview.setText("Mục tiêu: "+f+" FPS · "+d+" DPI\nKhông gian UI tham khảo: "+Math.round(screenW*160f/d)+" × "+Math.round(screenH*160f/d)+" dp");getSystemService(InputMethodManager.class).hideSoftInputFromWindow(fps.getWindowToken(),0);toast("Đã lưu hồ sơ mục tiêu",true);}catch(NumberFormatException e){fps.setError("Vui lòng nhập số hợp lệ");}}));b.addView(Ui.button(this,"Mở cài đặt màn hình",v->{dismiss();startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}));closeButton(b);}
    void imageMenu(){LinearLayout b=sheet("Hình ảnh","Theo dõi vùng ảnh trong 3 giây",Gravity.CENTER,false);b.addView(Ui.text(this,"Cho phép chia sẻ toàn màn hình, rồi kéo khung quanh nhân vật. Ảnh chỉ được xử lý trên máy. Nhãn “bot” là nhãn do giao diện đặt, không xác định loại nhân vật. Không đọc tài nguyên game; có thể mất dấu khi hình thay đổi nhanh.",14,Ui.MUTED));b.addView(Ui.primary(this,"Chụp & chọn vùng",v->startCapture()));if(capture!=null)b.addView(Ui.button(this,"Dừng chia sẻ màn hình",v->{capture.close();capture=null;notification(false);dismiss();}));closeButton(b);}
    void startCapture(){dismiss();if(capture!=null){selectRegion();return;}startActivity(new Intent(this,MainActivity.class).setAction("capture").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}
    void selectRegion(){dismiss();remove(selector);if(capture==null)return;if(orb!=null)orb.setVisibility(View.INVISIBLE);if(stats!=null)stats.setVisibility(View.INVISIBLE);if(hud!=null)hud.setVisibility(View.INVISIBLE);Selection s=new Selection(this);selector=s;add(s,params(-1,-1,Gravity.TOP|Gravity.LEFT,false,true));main.postDelayed(()->{if(selector==s){remove(selector);selector=null;restoreVisuals();toast("Đã hết thời gian chọn vùng",false);}},20000);}
    void restoreVisuals(){if(orb!=null)orb.setVisibility(View.VISIBLE);if(stats!=null)stats.setVisibility(View.VISIBLE);if(hud!=null)hud.setVisibility(View.VISIBLE);}
    void selected(RectF region){remove(selector);selector=null;restoreVisuals();if(capture==null)return;tracking=true;ensureHud();hud.target=null;hud.status="Đang chuẩn bị vùng ảnh…";hud.invalidate();capture.track(region);}
    void trackUpdate(RectF region,float progress,boolean found){ensureHud();hud.target=found?region:null;hud.progress=progress;hud.status=found?"bot · theo dõi ảnh":"Mất dấu vùng ảnh";hud.invalidate();}
    void trackDone(boolean ok){tracking=false;if(hud!=null){hud.target=null;hud.status=null;hud.invalidate();}toast(ok?"Đã hoàn tất theo dõi 3 giây":"Kết thúc: không giữ được dấu ảnh",ok);}
    @Override public void onConfigurationChanged(Configuration c){super.onConfigurationChanged(c);dimensions();dismiss();remove(selector);selector=null;restoreVisuals();if(capture!=null){capture.close();capture=null;notification(false);tracking=false;toast("Màn hình đã xoay. Chọn Hình ảnh để chia sẻ lại.",false);}if(hud!=null){hud.target=null;hud.status=null;hud.invalidate();}remove(orb);orb=null;if(unlocked)showOrb();else login();if(stats!=null){remove(stats);stats=null;ramText=cpuText=batteryText=null;}crossX=Math.max(-screenW/2+dp(48),Math.min(screenW/2-dp(48),crossX));crossY=Math.max(-screenH/2+dp(48),Math.min(screenH/2-dp(48),crossY));}
    @Override public void onDestroy(){main.removeCallbacksAndMessages(null);if(capture!=null)capture.close();for(View v:new ArrayList<>(windows))remove(v);active=null;stopForeground(STOP_FOREGROUND_REMOVE);super.onDestroy();}
    class Selection extends View {final Paint p=new Paint(3);float x,y,ex,ey;boolean down;Selection(Context c){super(c);setContentDescription("Kéo để chọn vùng nhân vật. Chạm góc trên bên phải để hủy.");}protected void onDraw(Canvas c){p.setColor(0x44000000);c.drawRect(0,0,getWidth(),getHeight(),p);p.setColor(Ui.WHITE);p.setTextSize(dp(16));c.drawText("Kéo khung quanh nhân vật",dp(20),dp(54),p);c.drawText("Đóng",getWidth()-dp(65),dp(54),p);if(down){p.setColor(Ui.ACCENT);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2));c.drawRect(Math.min(x,ex),Math.min(y,ey),Math.max(x,ex),Math.max(y,ey),p);p.setStyle(Paint.Style.FILL);}}public boolean onTouchEvent(android.view.MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN){if(e.getY()<dp(80)&&e.getX()>getWidth()-dp(90)){remove(this);selector=null;restoreVisuals();return true;}x=ex=e.getX();y=ey=e.getY();down=true;}else if(e.getAction()==MotionEvent.ACTION_MOVE){ex=e.getX();ey=e.getY();}else if(e.getAction()==MotionEvent.ACTION_UP&&down){ex=e.getX();ey=e.getY();if(Math.abs(ex-x)<dp(16)||Math.abs(ey-y)<dp(16)){down=false;toast("Chọn vùng lớn hơn 16 dp",false);}else selected(new RectF(Math.min(x,ex),Math.min(y,ey),Math.max(x,ex),Math.max(y,ey)));}else if(e.getAction()==MotionEvent.ACTION_CANCEL){remove(this);selector=null;restoreVisuals();}invalidate();return true;}}
    class Hud extends View {final Paint p=new Paint(3);RectF target;float progress;String status;Hud(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}protected void onDraw(Canvas c){p.setShader(null);p.clearShadowLayer();p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2));if(crossOn){c.save();c.translate(getWidth()/2f+crossX,getHeight()/2f+crossY);boolean anim=rotate&&android.animation.ValueAnimator.areAnimatorsEnabled();if(anim)c.rotate((SystemClock.uptimeMillis()%6000)*.06f);p.setColor(crossColor);if(glow)p.setShadowLayer(dp(8),0,0,crossColor);float r=dp(crossSize)/2f,g=dp(4);switch(crossStyle){case 1:p.setStyle(Paint.Style.FILL);c.drawCircle(0,0,Math.max(dp(2),r/5),p);break;case 2:c.drawCircle(0,0,r,p);break;case 3:c.drawCircle(0,0,r,p);p.setStyle(Paint.Style.FILL);c.drawCircle(0,0,dp(2),p);break;case 4:for(int i=0;i<4;i++){c.drawLine(-r,-r,-r/2,-r,p);c.drawLine(-r,-r,-r,-r/2,p);c.rotate(90);}break;case 5:Path path=new Path();path.moveTo(0,-r);path.lineTo(r,0);path.lineTo(0,r);path.lineTo(-r,0);path.close();c.drawPath(path,p);break;default:c.drawLine(-r,0,-g,0,p);c.drawLine(g,0,r,0,p);c.drawLine(0,-r,0,-g,p);c.drawLine(0,g,0,r,p);}c.restore();if(anim)postInvalidateDelayed(33);}p.clearShadowLayer();if(status!=null){p.setColor(Ui.ACCENT);p.setStyle(Paint.Style.STROKE);if(target!=null){c.drawRoundRect(target,dp(4),dp(4),p);c.drawLine(getWidth()/2f,getHeight()-dp(24),target.centerX(),target.bottom,p);}p.setStyle(Paint.Style.FILL);p.setTextSize(dp(14));float tx=target==null?dp(20):Math.max(dp(16),Math.min(getWidth()-dp(200),target.left));float ty=target==null?dp(110):Math.max(dp(90),target.top-dp(12));c.drawText(status,tx,ty,p);p.setColor(0xff353e4b);c.drawRoundRect(tx,ty+dp(8),tx+dp(160),ty+dp(12),dp(2),dp(2),p);p.setColor(Ui.ACCENT);c.drawRoundRect(tx,ty+dp(8),tx+dp(160)*progress,ty+dp(12),dp(2),dp(2),p);}}}
}
