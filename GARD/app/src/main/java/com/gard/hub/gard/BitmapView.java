package com.gard.hub.gard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by 重力以太 on 15-12-19.
 *
 * 位图查看对象。本类是一个用来编辑二维空间的类，包括增加和减少电荷，维护二维空间栈，对二维空间画图，以及采集用户的输入信息。
 */
public class BitmapView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder holder=null; /**< View的掌控者 */
    private int nScale=4;              /**< 放大倍数 */
    private int nWidth;           /**< 画布宽度 */
    private int nHeight;          /**< 画布高度 */
    private int nDownPosX;        /**< 鼠标按下的位置X坐标 */
    private int nDownPosY;        /**< 鼠标按下的位置Y坐标 */
    private int nTouchX;               /**< 鼠标当前的位置X坐标 */
    private int nTouchY;               /**< 鼠标当前的位置Y坐标 */
    private Rect lineRect=null;        /**< 线区域 */
    private Rect selectRect=null;      /**< 选择区域 */
    private boolean bMoveH=false;   /**< 是否是水平移动区域 */
    private boolean bMoveV=false;   /**< 是否是垂直移动区域 */
    private boolean bLineMode=false;   /**< 是否是线模式 */
    private boolean bMoved=false;      /**< 是否移动 */
    private boolean bDrawGrid=false;      /**< 是否画方格 */
    private final int nSpaceStackSize = 128; /**< 空间栈大小 */
    private int nSpaceStackIndex = 0; /**< 空间栈当前指针 */
    private int nSpaceStackPushIndex = 0; /**< 空间栈当前添加指针 */
    private D2Space[] spaceStack=null;

    public D2Space getSpace() {
        return space;
    }

    /**< 空间栈，用来实现撤销和重做 */
    private D2Space space=null;          /**< 二维空间 */
    private D2Space.DrawMode drawMode;  /**< 画BMP的方法 */
    BitmapCanvas bmpCanvas=null;
    private String sLastFileName="";     /**< 记录上次打开或者保存的文件名称 */
    private D2Space.SelectedElectrons copyItems=null; /**< 用来复制的电话 */
    private D2Space.SelectedElectrons selectedItems=null; /**< 选中的电荷 */

    /**
     * 把当前的二维空间压入栈。
     */
    private void pushSpace(){
        nSpaceStackPushIndex++;
        nSpaceStackPushIndex%=nSpaceStackSize;
        spaceStack[nSpaceStackPushIndex] = null;
        try {
            //Log.i("BV","try to store space on index:"+nSpaceStackPushIndex);
            spaceStack[nSpaceStackPushIndex] = space.clone();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        nSpaceStackIndex = nSpaceStackPushIndex;
    }

    /**
     * 取上一个入栈的二维空间。
     */
    private void prevSpace(){
        nSpaceStackIndex--;
        if(nSpaceStackIndex<0) nSpaceStackIndex = nSpaceStackSize-1;
        if(spaceStack[nSpaceStackIndex]!=null) {
            //Log.i("BV","recover prev space on index:"+nSpaceStackIndex);
            space = null;
            space = spaceStack[nSpaceStackIndex];
        } else {
            nSpaceStackIndex++;
            nSpaceStackIndex%=nSpaceStackSize;
        }
    }

    /**
     * 取下一个入栈的二维空间。
     */
    private void nextSpace(){
        nSpaceStackIndex++;
        nSpaceStackIndex%=nSpaceStackSize;
        if(spaceStack[nSpaceStackIndex]!=null) {
            //Log.i("BV","recover next space on index:"+nSpaceStackIndex);
            space = null;
            space = spaceStack[nSpaceStackIndex];
        } else {
            nSpaceStackIndex--;
            if(nSpaceStackIndex<0) nSpaceStackIndex = nSpaceStackSize-1;
        }
    }

    /**
     * 重新画图。
     */
    public void reflesh(){
        updateBmp();
        doDraw();
    }

    /**
     * 重做已经撤销的二维空间。
     */
    public void redo(){
        nextSpace();
        nScale = space.getScale();
        bmpCanvas.setSize(nWidth, nHeight, nScale);
        reflesh();
    }

    /**
     * 撤销最近一次对二维空间的编辑。
     */
    public void undo(){
        prevSpace();
        nScale = space.getScale();
        bmpCanvas.setSize(nWidth, nHeight, nScale);
        reflesh();
    }

    /**
     * 设定或者取消二维空间的水平移动模式。
     *
     * @param b 如果为true，那么是水平移动模式；否则为不能水平移动。
     */
    public void setMoveH(boolean b){
        bMoveH = b;
    }

    /**
     * 设定或者取消二维空间的垂直移动模式。
     *
     * @param b 如果为true，那么是垂直移动模式；否则为不能垂直移动。
     */
    public void setMoveV(boolean b){
        bMoveV = b;
    }

    /**
     * 设定是否是选择线模式还是选择矩形模式。
     * 选择线模式用来画出直线上点的场的变化情况。
     * 选择矩形模式用来选择要操作的电荷。
     *
     * @param b 如果为true，那么是选择线模式；否则为选择矩形模式。
     */
    public void setLineMode(boolean b){
        bLineMode = b;
    }

    /**
     * 用来指定是否画网格。
     * 通过网格可以知道电荷之间的对齐情况。
     *
     * @param b 如果为true，那么需要画网格；否则不需要画网格。
     */
    public void setDrawGrid(boolean b){
        bDrawGrid = b;
        doDraw();
    }

    public void init() {
        if(space!=null) return;
        try {
            holder = getHolder();
            holder.addCallback(this);
            lineRect = new Rect(0, 0, 0, 0);
            selectRect = new Rect(0, 0, 0, 0);
            spaceStack=new D2Space[nSpaceStackSize];
            for(int i=0;i<nSpaceStackSize;i++){
                spaceStack[i]=null;
            }
            space = new D2Space();
            space.setScale(nScale);
            drawMode = D2Space.DrawMode.emORG;
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void finalize(){
        bmpCanvas = null;
        lineRect = null;
        selectRect = null;
        for(int i=0;i<nSpaceStackSize;i++){
            spaceStack[i]=null;
        }
        spaceStack = null;
        space = null;
    }

    public BitmapView(Context context) {
        super(context);
        init();
    }

    public BitmapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        nWidth = getWidth();
        nHeight = getHeight();
        bmpCanvas = new BitmapCanvas();
        bmpCanvas.setSize(nWidth, nHeight, nScale);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
        try{
            nWidth = getWidth();
            nHeight = getHeight();
            if(bmpCanvas!=null) bmpCanvas = null;
            bmpCanvas = new BitmapCanvas();
            bmpCanvas.setSize(nWidth, nHeight, nScale);
            reflesh();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        bmpCanvas = null;
    }

    /**
     * 把二维空间画到bmp上。
     */
    private void updateBmp(){
        try{
            if(bLineMode) {
                space.draw(bmpCanvas,lineRect,drawMode);
            } else {
                space.draw(bmpCanvas, null, drawMode);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示bmp和选择情况以及电荷的位置。
     */
    public void doDraw(){
        Canvas canvas=null;
        try{
            canvas=holder.lockCanvas();
            if(canvas!=null){
                canvas.drawColor(Color.BLACK);// 清除画布
                canvas.drawBitmap(bmpCanvas.getBitmap(), 0, 0, null);
                Rect rect=new Rect();
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1);
                PathEffect effects = new DashPathEffect(new float[]{3,3,3,3},1);
                paint.setPathEffect(effects);
                if(bDrawGrid){
                    int i;
                    int nStep= (int)D2Space.nElectronRadius*nScale;
                    if(drawMode==D2Space.DrawMode.emGA) {
                        paint.setColor(Color.YELLOW);
                    } else {
                        paint.setColor(Color.GRAY);
                    }
                    for(i=nScale/2;i<nWidth;i+=nStep){
                        canvas.drawLine(i, 0, i, nHeight, paint);
                    }
                    for(i=nScale/2;i<nHeight;i+=nStep){
                        canvas.drawLine(0, i, nWidth, i, paint);
                    }
                }
                if(drawMode==D2Space.DrawMode.emGA) {
                    paint.setColor(Color.BLACK);
                } else {
                    paint.setColor(Color.WHITE);
                }
                if(bLineMode) {
                    canvas.drawLine(lineRect.left * nScale, lineRect.top * nScale, lineRect.right * nScale, lineRect.bottom * nScale, paint); //画出选择区域
                }
                else {
                    rect.left = selectRect.left*nScale;
                    rect.right = selectRect.right*nScale;
                    rect.top = selectRect.top*nScale;
                    rect.bottom = selectRect.bottom*nScale;
                    canvas.drawRect(rect, paint); //画出选择区域
                }
                space.drawElectron(canvas,paint);
                rect = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            if(canvas!=null){
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    /**
     * 响应用户的触摸事件。
     * 根据当前的移动模式或者选择模式来响应用户的触摸事件。
     * 如果是移动模式，那么对选择区域中的电荷进行移动操作。
     * 如果是线选择模式，那么进行画线操作，用来选择一根直线。
     * 如果是矩形选择模式，那么进行画矩形操作，用来选择电荷。
     *
     * @param event 触摸事件。
     * @return 必须返回true。
     */
    public boolean onTouchEvent(MotionEvent event){
        nTouchX=(int)event.getX()/nScale;
        nTouchY=(int)event.getY()/ nScale;
        if(event.getActionMasked()==MotionEvent.ACTION_DOWN) {
            nDownPosX = nTouchX;
            nDownPosY = nTouchY;
        } else {
            if(bMoveH||bMoveV) {
                int dX=0;
                int dY=0;
                Rect rect = new Rect(nDownPosX, nDownPosY, nTouchX, nTouchY);
                if(bMoveH) dX = rect.width();
                if(bMoveV) dY = rect.height();
                if(bLineMode) {
                    if (dX != 0 || dY != 0) {
                        lineRect.offset(dX, dY);
                        nDownPosX = nTouchX;
                        nDownPosY = nTouchY;
                        doDraw();
                    }
                } else  if(selectedItems!=null) {
                    if (dX != 0 || dY != 0) {
                        bMoved = true;
                        space.move(selectedItems, dX, dY);
                        selectRect.offset(dX, dY);
                        nDownPosX = nTouchX;
                        nDownPosY = nTouchY;
                        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                            pushSpace(); // 保存数据
                            reflesh();
                            bMoved = false;
                        }
                        doDraw();
                    } else {
                        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                            if (bMoved) {
                                pushSpace(); // 保存数据
                                reflesh();
                                bMoved = false;
                            }
                            doDraw();
                        }
                    }
                }
            } else {
                if(bLineMode) {
                    lineRect.set(nDownPosX, nDownPosY, nTouchX, nTouchY);
                    doDraw();
                } else {
                    if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                        int x = (nDownPosX + nTouchX) / 2;
                        int y = (nDownPosY + nTouchY) / 2;
                        int nValue = space.getValue(x, y, drawMode);
                        String s = "Point: (" + x + "," + y + "), Value: " + nValue;
                        Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
                        selectRect.set(nDownPosX, nDownPosY, nTouchX, nTouchY);
                        selectRect.sort();
                        selectedItems = space.getSelectedElectrons(selectRect);
                        if (selectedItems == null) selectedItems = space.getSelectedElectrons(x, y);
                        doDraw();
                    } else {
                        selectRect.set(nDownPosX, nDownPosY, nTouchX, nTouchY);
                        selectRect.sort();
                        doDraw();
                    }
                }
            }
        }
        return true;
    }

    /**
     * 增加正电荷。
     * 如果存在选择区域，那么在选择区域中心增加；否则在画布中心增加。
     */
    public void addPE(){
        if (selectRect.isEmpty()){
            //space.addPE(new Point(150, 150));
            space.addPE(new Point(nWidth / 2 / nScale, nHeight / 2 / nScale));
        } else {
            space.addPE(new Point((selectRect.left + selectRect.right) / 2, (selectRect.top + selectRect.bottom) / 2));
        }
        pushSpace(); // 保存数据
        reflesh();
    }

    /**
     * 增加负电荷。
     * 如果存在选择区域，那么在选择区域中心增加；否则在画布中心增加。
     */
    public void addNE(){
        if (selectRect.isEmpty()){
            space.addNE(new Point(nWidth / 2 / nScale, nHeight / 2 / nScale));
        } else {
            space.addNE(new Point((selectRect.left + selectRect.right) / 2, (selectRect.top + selectRect.bottom) / 2));
        }
        pushSpace(); // 保存数据
        reflesh();
    }

    /**
     * 删除选择的电荷。
     */
    public void delElectrons(){
        if (selectedItems!=null){
            space.delElectron(selectedItems);
            copyItems = null;
            selectedItems = null;
            pushSpace(); // 保存数据
            reflesh();
        }
    }

    /**
     * 把选择的电荷放到剪贴板。
     */
    public void copy(){
        Log.i("GA", "select item:" + selectedItems);
        if (selectedItems!=null){
            try {
                copyItems = selectedItems.clone();
                Log.i("GA","copy item:"+copyItems);
            }catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 把剪贴板中的电荷粘贴到用户选定的地方。
     */
    public void paste(){
        if (copyItems!=null){
            selectedItems = space.addElectron(copyItems,nDownPosX,nDownPosY);
            pushSpace(); // 保存数据
            reflesh();
        }
    }

    /**
     * 使选择的电荷水平方向间隔相等。
     */
    public void averageElectronsHorizontaly(){
        if (selectedItems!=null){
            space.averageHorizontal(selectedItems);
            pushSpace(); // 保存数据
            reflesh();
        }
    }

    /**
     * 使选择的电荷垂直方向间隔相等。
     */
    public void averageElectronsVerticaly(){
        if (selectedItems!=null){
            space.averageVertical(selectedItems);
            pushSpace(); // 保存数据
            reflesh();
        }
    }

    /**
     * 使选择的电荷自动间隔相等。
     *
     * 通过第一行电荷计算每行有多少个电荷，然后就可以计算出有多少列，然后分别使这些电荷的水平间隔相等和垂直方向的间隔相等。
     */
    public void averageElectronsAuto(){
        if (selectedItems!=null){
            space.averagerAuto(selectedItems);
            pushSpace(); // 保存数据
            reflesh();
        }
    }

    /**
     * 对电荷进行水平方向的反转。
     */
    public void revertElectronsHorizontaly(){
        if (selectedItems!=null){
            space.revertHorizontal(selectedItems);
            pushSpace(); // 保存数据
            reflesh();
        }
    }

    /**
     * 对电荷进行垂直方向的反转。
     */
    public void revertElectronsVerticaly(){
        if (selectedItems!=null){
            space.revertVertical(selectedItems);
            pushSpace(); // 保存数据
            reflesh();
        }
    }

    /**
     * 对电荷进行正负性的反转。
     */
    public void revertElectronsPN(){
        if (selectedItems!=null){
            space.revertPN(selectedItems);
            pushSpace(); // 保存数据
            reflesh();
        }
    }

    /**
     * 对电荷进行向左对齐。
     */
    public void alignLeftElectrons(){
        if (selectedItems!=null){
            space.alignLeft(selectedItems);
            pushSpace(); // 保存数据
            reflesh();
        }
    }

    /**
     * 对电荷进行向右对齐。
     */
    public void alignRightElectrons(){
        if (selectedItems!=null){
            space.alignRight(selectedItems);
            pushSpace(); // 保存数据
            reflesh();
        }
    }

    /**
     * 对电荷按顶部对齐。
     */
    public void alignTopElectrons(){
        if (selectedItems!=null){
            space.alignTop(selectedItems);
            pushSpace(); // 保存数据
            reflesh();
        }
    }

    /**
     * 对电荷按底部对齐。
     */
    public void alignBottomElectrons(){
        if (selectedItems!=null){
            space.alignBottom(selectedItems);
            pushSpace(); // 保存数据
            reflesh();
        }
    }

    /**
     * 取当前画图模式的名称。
     *
     * @return 返回当前画图模式的名称。
     */
    public String getDrawModeName(){
        String s;
        switch (drawMode) {
            case emORG:
                s = "原始图";
                break;
            case emEA:
                s = "电以太图";
                break;
            case emLA:
                s = "光以太图";
                break;
            case emGA:
                s = "重力以太图";
                break;
            default:
                s = "未知";
                break;
        }
        return s;
    }

    /**
     * 设置当前画图模式。
     *
     * @param m 指定当前的画图模式。
     */
    public void setDrawMode(D2Space.DrawMode m){
        drawMode = m;
        reflesh();
    }

    /**
     * 根据当前画图模式把当前画图模式设置为上一种画图模式。
     */
    public void setPrevDrawMode(){
        switch (drawMode) {
            case emORG:
                setDrawMode(D2Space.DrawMode.emGA);
                break;
            case emEA:
                setDrawMode(D2Space.DrawMode.emORG);
                break;
            case emLA:
                setDrawMode(D2Space.DrawMode.emEA);
                break;
            case emGA:
                setDrawMode(D2Space.DrawMode.emLA);
                break;
            default:
                break;
        }
    }


    /**
     * 根据当前画图模式把当前画图模式设置为下一种画图模式。
     */
    public void setNextDrawMode(){
        switch (drawMode) {
            case emORG:
                setDrawMode(D2Space.DrawMode.emEA);
                break;
            case emEA:
                setDrawMode(D2Space.DrawMode.emLA);
                break;
            case emLA:
                setDrawMode(D2Space.DrawMode.emGA);
                break;
            case emGA:
                setDrawMode(D2Space.DrawMode.emORG);
                break;
            default:
                break;
        }
    }

    /**
     * 把当前二维空间压入栈，并且把它放大。
     */
    public void setScaleUp(){
        pushSpace(); // 保存数据
        nScale++;
        bmpCanvas.setSize(nWidth,nHeight,nScale);
        space.setScale(nScale);
        reflesh();
    }

    /**
     * 把当前二维空间压入栈，并且把它缩小。
     */
    public void setScaleDown(){
        pushSpace(); // 保存数据
        if(nScale>1) {
            nScale--;
            bmpCanvas.setSize(nWidth,nHeight,nScale);
            space.setScale(nScale);
            reflesh();
        }
    }

    /**
     * 把当前二维空间写到文件。
     *
     * @param file 指定文件名称。
     */
    public void saveFile(String file){
        try {
            FileOutputStream out = null;
            File f = new File(file);
            out = new FileOutputStream(f);
            space.saveToFile(out);
            out.close();
            sLastFileName = file;
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 把当前的bmp保存成JPG文件。
     *
     * @param filename 指定文件名称。
     */
    public void saveJPGFile(String filename){
        try {
            File file = new File(filename);
            FileOutputStream out = new FileOutputStream(file);
            bmpCanvas.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 把当前的bmp保存成PNG文件。
     *
     * @param filename 指定文件名称。
     */
    public void savePNGFile(String filename){
        try {
            File file = new File(filename);
            FileOutputStream out = new FileOutputStream(file);
            bmpCanvas.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 取上次保存二维空间的文件的名称。
     *
     * @return 返回上次保存二维空间的文件的名称。
     */
    public String getLastFileName(){
        return sLastFileName;
    }

    /**
     * 从JSON文件加载二维空间的信息。
     *
     * @param file 指定JSON文件名称。
     */
    public void loadFile(String file){
        pushSpace(); // 保存数据
        try {
            FileInputStream in = null;
            File f = new File(file);
            in = new FileInputStream(f);
            space.loadFromFile(in);
            in.close();
            sLastFileName = file;
            nScale = space.getScale();
            bmpCanvas.setSize(nWidth, nHeight, nScale);
            //Log.i("BV", "scale:" + nScale);
            reflesh();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 把当前二维空间的数据转化为JSON字符串。
     *
     * @return 返回JSON字符串。
     */
    public String toString(){
        return space.toString(4);
    }

    /**
     * 从JSON字符串加载二维空间信息。
     *
     * @param strJson 指定JSON字符串。
     * @return 加载成功返回true，否则返回false。
     */
    public boolean loadFromString(String strJson){
        pushSpace(); // 保存数据
        boolean ret = space.loadFromString(strJson);
        reflesh();
        return ret;
    }
}
