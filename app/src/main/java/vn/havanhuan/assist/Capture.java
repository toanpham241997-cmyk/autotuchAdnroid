package vn.havanhuan.assist;

import android.content.Intent;
import android.graphics.*;
import android.hardware.display.*;
import android.media.*;
import android.media.projection.*;
import android.os.*;
import java.nio.ByteBuffer;

/** Projection is authorized per session. Frames stay in memory; no storage/network permission. */
final class Capture {
    final OverlayService service; final int code; final Intent consent;
    HandlerThread thread; Handler worker; MediaProjection projection; VirtualDisplay display; ImageReader reader;
    volatile Bitmap latest; volatile boolean closed; int width,height; long lastFrame,started;
    RectF pending; Tracker tracker; int tw,th; boolean allFound; int generation;
    Capture(OverlayService service,int code,Intent consent){this.service=service;this.code=code;this.consent=consent;}
    void start(){
        width=Math.min(480,service.screenW);height=Math.max(1,Math.round((float)service.screenH*width/service.screenW));
        thread=new HandlerThread("local-image-tracker");thread.start();worker=new Handler(thread.getLooper());
        projection=service.getSystemService(MediaProjectionManager.class).getMediaProjection(code,consent);
        if(projection==null)throw new IllegalStateException("No projection");
        projection.registerCallback(new MediaProjection.Callback(){@Override public void onStop(){if(closed)return;service.main.post(()->{if(closed)return;close();service.capture=null;service.notification(false);service.remove(service.selector);service.selector=null;service.restoreVisuals();if(service.tracking)service.trackDone(false);service.toast("Đã dừng chia sẻ màn hình",false);});}},worker);
        reader=ImageReader.newInstance(width,height,PixelFormat.RGBA_8888,3);
        reader.setOnImageAvailableListener(r->{
            if(closed)return;
            try(Image image=r.acquireLatestImage()){
                if(image==null)return;long now=SystemClock.elapsedRealtime();if(now-lastFrame<120)return;lastFrame=now;
                Image.Plane plane=image.getPlanes()[0];ByteBuffer buf=plane.getBuffer();int pixel=plane.getPixelStride(),stride=plane.getRowStride();
                int[] pixels=new int[width*height];
                for(int y=0;y<height;y++)for(int x=0;x<width;x++){int o=y*stride+x*pixel;int red=buf.get(o)&255,green=buf.get(o+1)&255,blue=buf.get(o+2)&255;pixels[y*width+x]=0xff000000|(red<<16)|(green<<8)|blue;}
                Bitmap bitmap=Bitmap.createBitmap(pixels,width,height,Bitmap.Config.ARGB_8888);latest=bitmap;
                if(pending!=null){
                    float scale=(float)width/service.screenW;int x=Math.max(0,Math.min(width-6,Math.round(pending.left*scale))),y=Math.max(0,Math.min(height-6,Math.round(pending.top*scale)));
                    tw=Math.max(6,Math.min(width-x,Math.round(pending.width()*scale)));th=Math.max(6,Math.min(height-y,Math.round(pending.height()*scale)));
                    // Bound work on very large selections and avoid misleading broad-scene tracking.
                    if(tw>width/2||th>height/2){pending=null;service.main.post(()->{if(!closed)service.trackDone(false);});return;}
                    tracker=new Tracker(pixels,width,x,y,tw,th);pending=null;if(!tracker.usable){tracker=null;service.main.post(()->{if(!closed){service.trackDone(false);service.toast("Vùng ảnh ít chi tiết hoặc bị chặn chụp",false);}});return;}started=now;allFound=true;int token=++generation;
                    worker.postDelayed(()->{if(!closed&&token==generation&&tracker!=null){tracker=null;boolean success=allFound;service.main.post(()->{if(!closed)service.trackDone(success);});}},3000);
                }
                if(tracker!=null){boolean found=tracker.update(pixels,width,height);allFound&=found;float ratio=(float)service.screenW/width;RectF rect=new RectF(tracker.x*ratio,tracker.y*ratio,(tracker.x+tw)*ratio,(tracker.y+th)*ratio);float progress=Math.min(1,(now-started)/3000f);service.main.post(()->{if(!closed&&service.tracking)service.trackUpdate(rect,progress,found);});}
            }catch(IllegalStateException|IndexOutOfBoundsException e){if(!closed)service.main.post(()->{if(service.tracking)service.trackDone(false);});}
        },worker);
        display=projection.createVirtualDisplay("HuanAssist-consented-capture",width,height,service.getResources().getDisplayMetrics().densityDpi,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,reader.getSurface(),null,worker);
    }
    void track(RectF region){if(worker==null||closed)return;worker.postDelayed(()->{if(!closed){tracker=null;generation++;pending=new RectF(region);int token=generation;worker.postDelayed(()->{if(!closed&&pending!=null&&token==generation){pending=null;service.main.post(()->service.trackDone(false));}},2500);}},250);}
    void close(){if(closed)return;closed=true;if(worker!=null)worker.removeCallbacksAndMessages(null);if(reader!=null)reader.setOnImageAvailableListener(null,null);if(display!=null)display.release();if(reader!=null)reader.close();if(projection!=null)projection.stop();if(thread!=null)thread.quitSafely();latest=null;}
}
