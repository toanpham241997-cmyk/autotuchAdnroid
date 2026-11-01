package vn.havanhuan.assist;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Random;
public class TrackerTest {
 @Test public void tracksTranslatedPatch(){int w=120,h=100;int[] first=new int[w*h],next=new int[w*h];Random random=new Random(7);for(int y=0;y<24;y++)for(int x=0;x<18;x++){int color=0xff000000|random.nextInt(0xffffff);first[(30+y)*w+40+x]=color;next[(36+y)*w+48+x]=color;}Tracker t=new Tracker(first,w,40,30,18,24);assertTrue(t.update(next,w,h));assertEquals(48,t.x);assertEquals(36,t.y);}
 @Test public void rejectsDisappearedPatch(){int[] first=new int[80*80],next=new int[80*80];Random random=new Random(19);for(int i=0;i<first.length;i++)first[i]=0xff000000|random.nextInt(0xffffff);Tracker t=new Tracker(first,80,20,20,12,12);assertFalse(t.update(next,80,80));}
 @Test public void rejectsBlackCapture(){int[] frame=new int[40*40];Tracker t=new Tracker(frame,40,0,0,12,12);assertFalse(t.usable);assertFalse(t.update(frame,40,40));}
 @Test public void handlesFrameEdge(){int[] frame=new int[40*40];Random random=new Random(11);for(int i=0;i<frame.length;i++)frame[i]=0xff000000|random.nextInt(0xffffff);Tracker t=new Tracker(frame,40,0,0,12,12);assertTrue(t.update(frame,40,40));assertTrue(t.x>=0&&t.y>=0);}
}
