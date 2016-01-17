package com.gard.hub.gard;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by 重力以太 on 15-12-23.
 *
 * 本类的作用是把二维空间中的点画到一个Bitmap中。
 *
 * 定义：
 * 点：二维空间上的逻辑点，单色范围是：[0,aPointBColors]
 * 小点：Bitmap的真实点，单色范围是：[0,255]
 *
 * 由于一个小点的单色最大为255，为了表示一个更大范围的颜色，使用多个小点来表示一个点。这里使用nScale*nScale个小点来表示一个点。
 * 所以nMaxColor=nScale*nScale*255。
 */
public class BitmapCanvas {
    private Bitmap bmp=null;           /**< 需要画到View去的位图 */
    private int nWidth;                /**< 画布宽度 */
    private int nHeight;               /**< 画布高度 */
    private int nScale=1;              /**< 放大倍数 */
    private int nPointsPerPixel=1;        /**< 每个点的子点数，nScale×nScale */
    private int [] aPointXOffsets=null;    /**< 每个对外的pixcel的组成的点的X偏移量*/
    private int [] aPointYOffsets=null;    /**< 每个对外的pixcel的组成的点的Y偏移量*/
    private int [] aPointRColors=null;     /**< 红色分量 */
    private int [] aPointGColors=null;     /**< 绿色分量 */
    private int [] aPointBColors=null;     /**< 蓝色分量 */
    private int nMaxColor=255;         /**< 取最大的单色颜色值，255×nPointsPerPixel */

    protected  void finalize(){
        bmp = null;
        aPointXOffsets = null;
        aPointYOffsets = null;
        aPointRColors = null;
        aPointGColors = null;
        aPointBColors = null;
    }

    public int getWidth(){
        return nWidth/nScale;
    }

    public int getHeight(){
        return nHeight/nScale;
    }

    public int getMaxColor(){
        return nMaxColor;
    }

    public Bitmap getBitmap() {
        return bmp;
    }

    /**
     * 将组成点的小点相对左上角的坐标转化为一维数组，主要是为了可以按一定的顺序来访问这些小点。
     * 这里是从组成小点的正方形的左上角，顺时针组个遍历这些小点，是一个螺旋型的路径。
     */
    private void calcPointOffsets(){
        int i,j;
        int wb,hb;
        int we,he;
        int n=0;
        int [][] aXOffsets = new int[nScale][nScale];
        int [][] aYOffsets = new int[nScale][nScale];
        for(j=0;j<nScale;j++) {
            for(i=0;i<nScale;i++) {
                aXOffsets[j][i] = i;
                aYOffsets[j][i] = j;
            }
        }

        wb = 0;
        hb = 0;
        we = nScale;
        he = nScale;
        for(;n<nPointsPerPixel;) {
            i = wb;
            j = hb;
            for (; i < we; i++) {
                aPointXOffsets[n] = aXOffsets[j][i];
                aPointYOffsets[n] = aYOffsets[j][i];
                //Log.i("BMP","offset:"+n+","+aPointXOffsets[n]);
                n++;
            }
            i--;
            for (j++; j < he; j++) {
                aPointXOffsets[n] = aXOffsets[j][i];
                aPointYOffsets[n] = aYOffsets[j][i];
                //Log.i("BMP","offset:"+n+","+aPointXOffsets[n]);
                n++;
            }
            j--;
            for (i--; i > wb; i--) {
                aPointXOffsets[n] = aXOffsets[j][i];
                aPointYOffsets[n] = aYOffsets[j][i];
                //Log.i("BMP","offset:"+n+","+aPointXOffsets[n]);
                n++;
            }
            for (; j > hb; j--) {
                aPointXOffsets[n] = aXOffsets[j][i];
                aPointYOffsets[n] = aYOffsets[j][i];
                //Log.i("BMP","offset:"+n+","+aPointXOffsets[n]);
                n++;
            }
            wb++;
            hb++;
            we--;
            he--;
        }
        aXOffsets = null;
        aYOffsets = null;
    }

    /**
     * 重新设置bmp的大小。
     *
     * @param width 指定bmp的宽度。
     * @param height 指定bmp的高度。
     * @param scale 指定一个逻辑点如何使用几个小点来表示，这里是小点组成的正方形的边长。
     */
    public void setSize(int width,int height,int scale) {
        bmp = null;
        nWidth = width;
        nHeight = height;
        bmp=Bitmap.createBitmap(nWidth, nHeight, Bitmap.Config.ARGB_8888);
        nScale = scale;
        nPointsPerPixel = nScale*nScale;
        aPointXOffsets = null;
        aPointXOffsets = new int[nPointsPerPixel];
        aPointYOffsets = null;
        aPointYOffsets = new int[nPointsPerPixel];
        aPointRColors = null;
        aPointRColors = new int[nPointsPerPixel];
        aPointGColors = null;
        aPointGColors = new int[nPointsPerPixel];
        aPointBColors = null;
        aPointBColors = new int[nPointsPerPixel];
        nMaxColor = 255*nPointsPerPixel;
        calcPointOffsets();
    }

    /**
     * 画一个点。这个点是由nScale*nScale的小点组成。
     * 采取颜色先分配满一个小点在分配给其他小点的算法。
     *
     * @param x 点的x坐标；
     * @param y 点的y坐标；
     * @param r 红色分量，值的范围取[0,nMaxColor]；
     * @param g 绿色分量，值的范围取[0,nMaxColor]；
     * @param b 蓝色分量，值的范围取[0,nMaxColor]；
     */
    public void setPixel(int x,int y,int r,int g,int b) {
        int i;
        x*=nScale;
        y*=nScale;
        for(i=nPointsPerPixel-1;i>-1;i--){
            if(r>255){
                aPointRColors[i]=255;
                r-=255;
            } else if(r>0){
                aPointRColors[i]=r;
                r=0;
            } else aPointRColors[i]=0;

            if(g>255){
                aPointGColors[i]=255;
                g-=255;
            } else if(g>0){
                aPointGColors[i]=g;
                g=0;
            } else aPointGColors[i]=0;

            if(b>255){
                aPointBColors[i]=255;
                b-=255;
            } else if(b>0){
                aPointBColors[i]=b;
                b=0;
            } else aPointBColors[i]=0;
        }
        for(i=nPointsPerPixel-1;i>-1;i--){
            bmp.setPixel(x + aPointXOffsets[i], y + aPointYOffsets[i],
                    Color.argb(0xFF, aPointRColors[i], aPointGColors[i], aPointBColors[i]));
        }
    }

    /**
     * 画一个点。这个点是由nScale*nScale的小点组成。
     * 采取颜色尽量平均分配给每个小点的算法。
     *
     * @param x 点的x坐标；
     * @param y 点的y坐标；
     * @param r 红色分量，值的范围取[0,nMaxColor]；
     * @param g 绿色分量，值的范围取[0,nMaxColor]；
     * @param b 蓝色分量，值的范围取[0,nMaxColor]；
     */
    public void setPixel2(int x,int y,int r,int g,int b) {
        int i;
        int ar=r/nPointsPerPixel; // 每个小点的红色平均值
        int ag=g/nPointsPerPixel; // 每个小点的绿色平均值
        int ab=b/nPointsPerPixel; // 每个小点的蓝色平均值
        int rr=r%nPointsPerPixel; // 红色分量的剩余值。本函数为每个小点中从该值中取出1，直到取完成为止。
        int rg=g%nPointsPerPixel; // 绿色分量的剩余值。本函数为每个小点中从该值中取出1，直到取完成为止。
        int rb=b%nPointsPerPixel; // 蓝色分量的剩余值。本函数为每个小点中从该值中取出1，直到取完成为止。
        x*=nScale;
        y*=nScale;
        for(i=nPointsPerPixel-1;i>-1;i--){
            // 设置小点的平均值
            aPointRColors[i]=ar;
            aPointGColors[i]=ag;
            aPointBColors[i]=ab;
            // 从剩余值中分配颜色到小点
            if(rr>0){
                aPointRColors[i]+=1;
                rr--;
            }
            if(rg>0){
                aPointGColors[i]+=1;
                rg--;
            }
            if(rb>0){
                aPointBColors[i]+=1;
                rb--;
            }
        }
        for(i=nPointsPerPixel-1;i>-1;i--){
            bmp.setPixel(x + aPointXOffsets[i], y + aPointYOffsets[i],
                    Color.argb(0xFF, aPointRColors[i], aPointGColors[i], aPointBColors[i]));
        }
    }

    /**
     * 使用指定的颜色画一根长度等于nScale的水平的线，并与上次画的线连接起来。
     * @param x 开始点的x坐标；
     * @param y 开始点的y坐标；
     * @param yl 最近一次画的线的y坐标；
     * @param r 红色分量，值的范围取[0,nMaxColor]；
     * @param g 绿色分量，值的范围取[0,nMaxColor]；
     * @param b 蓝色分量，值的范围取[0,nMaxColor]；
     */
    public void setHLine(int x,int y,int yl,int r,int g,int b) {
        int i;
        y*=nScale;
        yl*=nScale;
        y=nHeight-y;
        yl=nHeight-yl;
        if(y>0&&y<nHeight) {
            int x2;
            x *= nScale;
            if(y>yl) {
                for (i = yl; i < y; i++) {
                    if(i<nHeight) bmp.setPixel(x, i, Color.argb(0xFF, r, g, b));
                }
            } else if(y<yl){
                for (i = y; i < yl; i++) {
                    if(i<nHeight) bmp.setPixel(x, i, Color.argb(0xFF, r, g, b));
                }
            }
            x2 = x + nScale;
            for (i = x; i < x2; i++) {
                if(i<nWidth) bmp.setPixel(i, y, Color.argb(0xFF, r, g, b));
            }
        }
    }

    /**
     * 使用指定的颜色画一根长度等于nScale的垂直的线，并与上次画的线连接起来。
     * @param x 开始点的x坐标；
     * @param y 开始点的y坐标；
     * @param xl 最近一次画的线的x坐标；
     * @param r 红色分量，值的范围取[0,nMaxColor]；
     * @param g 绿色分量，值的范围取[0,nMaxColor]；
     * @param b 蓝色分量，值的范围取[0,nMaxColor]；
     */
    public void setVLine(int x,int y,int xl,int r,int g,int b) {
        int i;
        int y2;
        x*=nScale;
        xl*=nScale;
        if (x<nWidth){
            y *= nScale;
            if(x>xl) {
                for (i = xl; i < x; i++) {
                    if(i<nWidth) bmp.setPixel(i, y, Color.argb(0xFF, r, g, b));
                }
            } else if(x<xl){
                for (i = x; i < xl; i++) {
                    if(i<nWidth) bmp.setPixel(i, y, Color.argb(0xFF, r, g, b));
                }
            }
            y2 = y + nScale;
            for (i = y; i < y2; i++) {
                if (i < nHeight) bmp.setPixel(x, i, Color.argb(0xFF, r, g, b));
            }
        }
    }
}
