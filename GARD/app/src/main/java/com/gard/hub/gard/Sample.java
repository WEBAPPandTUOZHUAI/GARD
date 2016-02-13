package com.gard.hub.gard;

import android.graphics.Point;

/**
 * Created by 重力以太 on 16-1-10.
 *
 * 本类的函数用来创建一些例子。
 */
public class Sample {

    /**
     * 本函数用来新建一些电荷，把它们放到一个矩形里面。
     *
     * @param space 指定二维空间；
     * @param left  矩形的左顶点的x坐标；
     * @param top  矩形的左顶点的y坐标；
     * @param step 每个电荷之间的间距；
     * @param row  指定行数；
     * @param col  指定列数；
     * @param positive 如果是正电荷，设置为true；否则设定为false。
     */
    private static void createElectrons(D2Space space,double left,double top,double step,int row,int col,boolean positive){
        for(int j=0;j<row;j++) {
            double x,y;
            x=left;
            y=top;
            for(int i=0;i<col;i++) {
                Point pos=new Point((int)x,(int)y);
                if(positive) space.addPE(pos);
                else space.addNE(pos);
                x+=step;
            }
            top+=step;
        }
    }

    /**
     * 本函数用来新建一些电荷，把它们放到一条直线上。
     *
     * @param space 指定二维空间；
     * @param left  直线开始点的x坐标；
     * @param top 直线开始点的y坐标；
     * @param stepX 直线的x轴增量，可以是负数；
     * @param stepY 直线的y轴增量，可以是负数；
     * @param count 指定新建多少个点；
     * @param positive 如果是正电荷，设置为true；否则设定为false。
     */
    private static void createElectronsLine(D2Space space,double left,double top,double stepX,double stepY,int count,boolean positive){
        double x,y;
        x = left;
        y = top;
        for(int i=0;i<count;i++) {
            Point pos=new Point((int)x,(int)y);
            if(positive) space.addPE(pos);
            else space.addNE(pos);
            x+=stepX;
            y+=stepY;
        }
    }

    /**
     * 本函数用来新建一些电荷，把它们放到一条圆上。
     *
     * @param space 指定二维空间；
     * @param x 原点X坐标；
     * @param y 原点Y坐标；
     * @param radius 半径；
     * @param count 指定新建多少个点；
     * @param positive 如果是正电荷，设置为true；否则设定为false。
     */
    private static void createElectronsCircle(D2Space space,double x,double y,double radius,int count,boolean positive){
        double da=2*Math.PI/count;
        double a;
        for(a=0;a<2*Math.PI;a+=da) {
            double x1,y1;
            x1=radius*Math.cos(a)+x;
            y1=y-radius*Math.sin(a);
            Point pos=new Point((int)x1,(int)y1);
            if(positive) space.addPE(pos);
            else space.addNE(pos);
        }
    }

    /**
     * 演示爱恩斯坦空间扭曲的本质。
     *
     * @param bmpView 指定电荷创建到哪个位图查看对象。
     */
    public static void showEinsteinSpace(BitmapView bmpView){
        D2Space space = bmpView.getSpace();
        space.clear();
        int nColCount=8;
        int nRowCount=8;
        double nStepSize=8*D2Space.nElectronRadius;
        double nLeft=(bmpView.getWidth()-nStepSize*(nColCount-1))/2;
        double nTop=(bmpView.getHeight()-nStepSize*(nRowCount-1))/2;
        if(nLeft<=0) {
            nLeft=4*D2Space.nElectronRadius;;
            nStepSize= D2Space.nElectronRadius;
        }
        if(nTop<=0) {
            nTop=4*D2Space.nElectronRadius;;
            nStepSize= D2Space.nElectronRadius;
        }
        nLeft/=space.getScale();
        nTop/=space.getScale();
        nStepSize/=space.getScale();
        for(int j=0;j<nRowCount;j++) {
            double x,y;
            x=nLeft;
            y=nTop;
            for(int i=0;i<nColCount;i++) {
                Point pos=new Point((int)x,(int)y);
                space.addPE(pos);
                space.addNE(pos);
                x+=nStepSize;
            }
            nTop+=nStepSize;
        }
        bmpView.reflesh();
    }

    /**
     * 演示电光效应的本质。
     *
     * @param bmpView 指定电荷创建到哪个位图查看对象。
     */
    public static void showElectroOpticalEffect(BitmapView bmpView) {
        D2Space space = bmpView.getSpace();
        space.clear();
        int nColCount=12;
        int nRowCount=8;
        double nXStepSize=8*D2Space.nElectronRadius;
        double nYStepSize=16*D2Space.nElectronRadius;
        double nLeft=(bmpView.getWidth()-nXStepSize*(nColCount-1))/2;
        double nTop=(bmpView.getHeight()-nYStepSize*(nRowCount-1))/2;
        Boolean bPositive=true;
        if(nLeft<=0) {
            nLeft=4*D2Space.nElectronRadius;
            nXStepSize= 2*D2Space.nElectronRadius;
        }
        if(nTop<=0) {
            nTop=4*D2Space.nElectronRadius;
            nYStepSize= 4*D2Space.nElectronRadius;
        }
        nLeft/=space.getScale();
        nTop/=space.getScale();
        nXStepSize/=space.getScale();
        nYStepSize/=space.getScale();
        for(int j=0;j<nRowCount;j++) {
            double x,y;
            x=nLeft;
            y=nTop;
            for(int i=0;i<nColCount;i++) {
                Point pos=new Point((int)x,(int)y);
                if(bPositive) space.addPE(pos);
                else space.addNE(pos);
                x+=nXStepSize;
            }
            nTop+=nYStepSize;
            bPositive = !bPositive;
        }
        bmpView.reflesh();
    }

    /**
     * 演示同种电荷的库仑力的本质。
     *
     * @param bmpView 指定电荷创建到哪个位图查看对象。
     */
    public static void showCoulombForce1(BitmapView bmpView) {
        D2Space space = bmpView.getSpace();
        space.clear();
        int nColCount=2;
        int nRowCount=2;
        double nStepSize=4*D2Space.nElectronRadius;
        double nDistance=20*D2Space.nElectronRadius;
        double nLeft=(bmpView.getWidth()-nStepSize*(nColCount-1))/2-nDistance;
        double nTop=(bmpView.getHeight()-nStepSize*(nRowCount-1))/2;
        nLeft/=space.getScale();
        nTop/=space.getScale();
        createElectrons(space,nLeft,nTop,nStepSize/space.getScale(),nColCount,nColCount,true);
        nLeft=(bmpView.getWidth()-nStepSize*(nColCount-1))/2+nDistance;
        nTop=(bmpView.getHeight()-nStepSize*(nRowCount-1))/2;
        nLeft/=space.getScale();
        nTop/=space.getScale();
        createElectrons(space,nLeft,nTop,nStepSize/space.getScale(),nRowCount,nColCount,true);
        bmpView.reflesh();
    }

    /**
     * 演示异种电荷的库仑力的本质。
     *
     * @param bmpView 指定电荷创建到哪个位图查看对象。
     */
    public static void showCoulombForce2(BitmapView bmpView) {
        D2Space space = bmpView.getSpace();
        space.clear();
        int nColCount=2;
        int nRowCount=2;
        double nStepSize=4*D2Space.nElectronRadius;
        double nDistance=20*D2Space.nElectronRadius;
        double nLeft=(bmpView.getWidth()-nStepSize*(nColCount-1))/2-nDistance;
        double nTop=(bmpView.getHeight()-nStepSize*(nRowCount-1))/2;
        nLeft/=space.getScale();
        nTop/=space.getScale();
        createElectrons(space,nLeft,nTop,nStepSize/space.getScale(),nColCount,nColCount,true);
        nLeft=(bmpView.getWidth()-nStepSize*(nColCount-1))/2+nDistance;
        nTop=(bmpView.getHeight()-nStepSize*(nRowCount-1))/2;
        nLeft/=space.getScale();
        nTop/=space.getScale();
        createElectrons(space, nLeft, nTop, nStepSize / space.getScale(), nRowCount, nColCount, false);
        bmpView.reflesh();
    }

    /**
     * 演示混合电荷的库仑力的本质。
     *
     * @param bmpView 指定电荷创建到哪个位图查看对象。
     */
    public static void showCoulombForce3(BitmapView bmpView) {
        D2Space space = bmpView.getSpace();
        space.clear();
        int nColCount=2;
        int nRowCount=2;
        double nStepSize=4*D2Space.nElectronRadius;
        double nDistance=20*D2Space.nElectronRadius;
        double nLeft=(bmpView.getWidth()-nStepSize*(nColCount-1))/2-nDistance;
        double nTop=(bmpView.getHeight()-nStepSize*(nRowCount-1))/2;
        nLeft/=space.getScale();
        nTop/=space.getScale();
        createElectrons(space,nLeft,nTop,nStepSize/space.getScale(),nColCount,nColCount,true);
        nLeft=(bmpView.getWidth()-nStepSize*(nColCount-1))/2+nDistance;
        nTop=(bmpView.getHeight()-nStepSize*(nRowCount-1))/2;
        nLeft/=space.getScale();
        nTop/=space.getScale();
        createElectrons(space,nLeft,nTop,nStepSize/space.getScale(),nRowCount,nColCount,false);
        Point pos=new Point(bmpView.getWidth()/2/space.getScale(),bmpView.getHeight()/2/space.getScale());
        space.addPE(pos);
        bmpView.reflesh();
    }

    /**
     * 演示平行板电容的重力以太的分布情况。
     *
     * @param bmpView 指定电荷创建到哪个位图查看对象。
     */
    public static void showParallelCapacitor(BitmapView bmpView) {
        D2Space space = bmpView.getSpace();
        space.clear();
        int nCount=24;
        double nStepSize=4*D2Space.nElectronRadius;
        double nDistance=40*D2Space.nElectronRadius;
        double nLeft=(bmpView.getWidth()-nStepSize*(nCount-1))/2;
        double nTop=(bmpView.getHeight()-(4*nStepSize+nDistance))/2;
        nLeft/=space.getScale();
        createElectronsLine(space, nLeft, nTop / space.getScale(), nStepSize / space.getScale(), 0.0, nCount, true);
        nTop+=nStepSize;
        createElectronsLine(space, nLeft, nTop/space.getScale(), nStepSize / space.getScale(), 0.0, nCount,true);
        nTop+=nDistance;
        createElectronsLine(space, nLeft, nTop/space.getScale(), nStepSize / space.getScale(), 0.0, nCount,false);
        nTop+=nStepSize;
        createElectronsLine(space, nLeft, nTop / space.getScale(), nStepSize / space.getScale(), 0.0, nCount, false);
        bmpView.reflesh();
    }

    /**
     * 演示飘升机1的重力以太的分布情况。
     *
     * @param bmpView 指定电荷创建到哪个位图查看对象。
     */
    public static void showFlyingLifter(BitmapView bmpView) {
        D2Space space = bmpView.getSpace();
        space.clear();
        int nCount=16;
        double nStepSize=4*D2Space.nElectronRadius;
        double nDistance=16*D2Space.nElectronRadius;
        double nLeft=bmpView.getWidth()/2;
        double nTop=(bmpView.getHeight()-nStepSize*(nCount-1)-nDistance)/2;
        nLeft/=space.getScale();
        createElectronsLine(space, nLeft, nTop/space.getScale(), 0.0, 0.0, nCount,true);
        nTop+=nDistance;
        createElectronsLine(space, nLeft, nTop/space.getScale(), 0.0, nStepSize / space.getScale(), nCount,false);
        bmpView.reflesh();
    }

    /**
     * 演示飘升机2的重力以太的分布情况。
     *
     * @param bmpView 指定电荷创建到哪个位图查看对象。
     */
    public static void showFlyingLifter2(BitmapView bmpView) {
        D2Space space = bmpView.getSpace();
        space.clear();
        int nCount=16;
        double nStepSize=4*D2Space.nElectronRadius;
        double nDistance=32*D2Space.nElectronRadius;
        double nLeft=bmpView.getWidth()/2;
        double nTop=(bmpView.getHeight()-nDistance)/2;
        nLeft/=space.getScale();
        createElectronsLine(space, nLeft, nTop/space.getScale(), 0.0, 0.0, nCount,true);
        nTop+=nDistance;
        nLeft=(bmpView.getWidth()-nStepSize*(nCount-1))/2;
        createElectronsLine(space, nLeft/space.getScale(), nTop/space.getScale(), nStepSize / space.getScale(),0.0,  nCount,false);
        bmpView.reflesh();
    }

    /**
     * 演示飞碟的重力以太的分布情况。
     *
     * @param bmpView 指定电荷创建到哪个位图查看对象。
     */
    public static void action_sample_flying_saucer(BitmapView bmpView) {
        D2Space space = bmpView.getSpace();
        space.clear();
        int nCount=8;
        double nStepSize=4*D2Space.nElectronRadius;
        double nLeft=(bmpView.getWidth()-nStepSize)/2;
        double nTop=(bmpView.getHeight()-nStepSize*(nCount-1)*2)/2;
        createElectronsLine(space, nLeft/space.getScale(), nTop/space.getScale(), -nStepSize / space.getScale(), nStepSize / space.getScale(), nCount,true);
        createElectronsLine(space, (nLeft+nStepSize)/space.getScale(), nTop/space.getScale(), nStepSize / space.getScale(), nStepSize / space.getScale(), nCount,true);
        createElectronsLine(space, nLeft/space.getScale(), (nTop+nStepSize)/space.getScale(), -nStepSize / space.getScale(), nStepSize / space.getScale(), nCount,false);
        createElectronsLine(space, (nLeft+nStepSize)/space.getScale(), (nTop+nStepSize)/space.getScale(), nStepSize / space.getScale(), nStepSize / space.getScale(), nCount,false);
        nLeft-=nStepSize*nCount*1.5;
        nLeft+=nStepSize;
        nTop+=nStepSize*nCount;
        createElectronsLine(space, nLeft/space.getScale(), nTop/space.getScale(), nStepSize / space.getScale(), 0.5* nStepSize / space.getScale(), nCount,true);
        createElectronsLine(space, nLeft/space.getScale(), (nTop+nStepSize)/space.getScale(), nStepSize / space.getScale(), 0.5* nStepSize / space.getScale(), nCount,false);
        nLeft += nStepSize*nCount*1.5*2;
        nLeft-=nStepSize;
        createElectronsLine(space, nLeft/space.getScale(), nTop/space.getScale(), -nStepSize / space.getScale(), 0.5* nStepSize / space.getScale(), nCount,true);
        createElectronsLine(space, nLeft/space.getScale(), (nTop+nStepSize)/space.getScale(), -nStepSize / space.getScale(), 0.5* nStepSize / space.getScale(), nCount,false);

        bmpView.reflesh();
    }

    /**
     * 演示环形的分布情况。
     *
     * @param bmpView 指定电荷创建到哪个位图查看对象。
     */
    public static void action_sample_ring(BitmapView bmpView) {
        D2Space space = bmpView.getSpace();
        space.clear();
        int nCount=32;
        double nStepSize=4*D2Space.nElectronRadius;
        double x=(bmpView.getWidth()-D2Space.nElectronRadius)/2;
        double y=(bmpView.getHeight()-D2Space.nElectronRadius)/2;
        createElectronsCircle(space,x/space.getScale(),y/space.getScale(),64,nCount,false);
        createElectronsCircle(space,x/space.getScale(),y/space.getScale(),32,nCount,true);
        bmpView.reflesh();
    }
}
