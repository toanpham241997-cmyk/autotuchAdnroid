package vn.havanhuan.assist;

/** Small local appearance tracker. No game data, object classification or network. */
final class Tracker {
    private final int[] template; private final int width,height; int x,y; float confidence; final boolean usable;
    Tracker(int[] pixels,int frameWidth,int x,int y,int width,int height){
        this.width=width;this.height=height;this.x=x;this.y=y;template=new int[width*height];
        for(int row=0;row<height;row++)System.arraycopy(pixels,(y+row)*frameWidth+x,template,row*width,width);
        double sum=0,squared=0;for(int color:template){double value=(((color>>16)&255)+((color>>8)&255)+(color&255))/3.0;sum+=value;squared+=value*value;}
        double mean=sum/template.length;usable=squared/template.length-mean*mean>100;
    }
    boolean update(int[] pixels,int fw,int fh){
        if(!usable){confidence=0;return false;}
        double best=Double.MAX_VALUE;int bx=x,by=y;
        for(int cy=Math.max(0,y-32);cy<=Math.min(fh-height,y+32);cy+=2)
            for(int cx=Math.max(0,x-32);cx<=Math.min(fw-width,x+32);cx+=2){
                long sum=0;int count=0;
                for(int ty=0;ty<height;ty+=3)for(int tx=0;tx<width;tx+=3){int a=template[ty*width+tx],b=pixels[(cy+ty)*fw+cx+tx];sum+=Math.abs(((a>>16)&255)-((b>>16)&255))+Math.abs(((a>>8)&255)-((b>>8)&255))+Math.abs((a&255)-(b&255));count++;}
                double score=(double)sum/(count*3);if(score<best){best=score;bx=cx;by=cy;}
            }
        confidence=(float)Math.max(0,1-best/100);if(best>40)return false;x=bx;y=by;return true;
    }
}
