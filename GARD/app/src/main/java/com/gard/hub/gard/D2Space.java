package com.gard.hub.gard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by 重力以太 on 15-12-20.
 *
 * 本类记录一个二维空间中电荷的信息，如电荷的正负型和坐标。
 *
 */
public class D2Space implements Cloneable {
    public final static float nElectronRadius=5.0f; /**< 电荷半径，不是真的电荷半径，只是用来画一个圆，用来定位电荷的位置。*/
    private final static int nElectronArraySize = 256; /**< 电荷数组大小 */
    private final static double nElectricCenterFieldValue = 65536.0; /**< 电荷中心点电场强度 */
    private int nPETail=0;      /**< 指向最后一个正电荷的坐标 */
    private int nNETail=0;     /**< 指向最后一个负电荷的坐标 */
    private int nScale=1;       /**< 放大倍数 */
    private Point[] aPE = null;  /**< 正电荷坐标 */
    private Point[] aNE = null; /**< 负电荷坐标 */


    /**
     * 本类记录一个被选择的电荷的信息，即是属于正电荷数组还是负电荷数组，以及在数组中的索引号。
     */
    public class SelectedElectron {
        public Point[] aPos = null; /**< 指向电荷坐标数组,可以是aPE,可以是aNE */
        public int nIndex = 0; /**< 指向电荷坐标数组 */
    }

    /**
     * 本类用来对被选中的电荷按X坐标来排序。
     */
    static class ComparatorX implements Comparator {
        public int compare(Object object1, Object object2) {// 实现接口中的方法
            int ret=0;
            SelectedElectron p1 = (SelectedElectron) object1; // 强制转换
            SelectedElectron p2 = (SelectedElectron) object2;
            if(p1.aPos[p1.nIndex].x==p2.aPos[p2.nIndex].x) ret=0;
            else if(p1.aPos[p1.nIndex].x<p2.aPos[p2.nIndex].x) ret=-1;
            else ret = 1;
            return ret;
        }
    }

    /**
     * 本类用来对被选中的电荷按Y坐标来排序。
     */
    static class ComparatorY implements Comparator {
        public int compare(Object object1, Object object2) {// 实现接口中的方法
            int ret=0;
            SelectedElectron p1 = (SelectedElectron) object1; // 强制转换
            SelectedElectron p2 = (SelectedElectron) object2;
            if(p1.aPos[p1.nIndex].y==p2.aPos[p2.nIndex].y) ret=0;
            else if(p1.aPos[p1.nIndex].y<p2.aPos[p2.nIndex].y) ret=-1;
            else ret = 1;
            return ret;
        }
    }

    /**
     * 本类用来对被选中的电荷先按Y坐标来排序，如果想等，再按X坐标排序。
     */
    static class ComparatorYX implements Comparator {
        public int compare(Object object1, Object object2) {// 实现接口中的方法
            int ret=0;
            SelectedElectron p1 = (SelectedElectron) object1; // 强制转换
            SelectedElectron p2 = (SelectedElectron) object2;
            if(p1.aPos[p1.nIndex].y==p2.aPos[p2.nIndex].y) {
                if(p1.aPos[p1.nIndex].x==p2.aPos[p2.nIndex].x) ret=0;
                else if(p1.aPos[p1.nIndex].x<p2.aPos[p2.nIndex].x) ret=-1;
                else ret = 1;
            }
            else if(p1.aPos[p1.nIndex].y<p2.aPos[p2.nIndex].y) ret=-1;
            else ret = 1;
            return ret;
        }
    }

    /**
     * 本类是被选中电荷的列表。
     */
    public class SelectedElectrons implements Cloneable {
        public List<SelectedElectron> list = new ArrayList<SelectedElectron>(); // 数组序列

        @Override
        public SelectedElectrons clone() throws CloneNotSupportedException {
            SelectedElectrons electrons=null;
            electrons = (SelectedElectrons) super.clone();
/*
            electrons.list.clear();

            Log.i("GA", "copy:" + this);
            for(SelectedElectron item : this.list){
                SelectedElectron newItem = new SelectedElectron();
                newItem.aPos = item.aPos;
                newItem.nIndex = item.nIndex;
                Log.i("GA","copy index:"+item.nIndex);
                electrons.put(newItem);
            }
*/
            return electrons;
        }

        /**
         * 把一个被选中的电荷添加到列表。
         *
         * @param item
         */
        public void put(SelectedElectron item){
            list.add(item);
        }

        /**
         * 对列表中的电荷的X坐标进行排序。
         */
        public void sortX(){
            Collections.sort(list, new ComparatorX());
        }

        /**
         * 对列表中的电荷的Y坐标进行排序。
         */
        public void sortY(){
            Collections.sort(list, new ComparatorY());
        }

        /**
         * 对列表中的电荷，先按Y坐标进行排序，如果相等，那么再按X坐标排序。
         */
        public void sortYX(){
            Collections.sort(list, new ComparatorYX());
        }
    }

    /**
     * 指定输出的图片是什么图片，不同的图片，输出的算法不同。
     */
    public enum DrawMode {
        emORG, /**< 原始图 */
        emEA,  /**< 电以太图 */
        emLA,  /**< 光以太图 */
        emGA   /**< 重力以太图 */
    }

    /**
     * 清除本二维空间中的电荷。
     */
    public void clear() {
        nPETail=0;
        nNETail=0;
        //nScale=4;
    }

    public D2Space() {
        aPE = new Point[nElectronArraySize];
        aNE = new Point[nElectronArraySize];
    }

    @Override
    public D2Space clone() throws CloneNotSupportedException {
        D2Space space=null;
        space =  (D2Space) super.clone();
        space.aPE = null;
        space.aNE = null;
        space.aPE = new Point[nElectronArraySize];
        space.aNE = new Point[nElectronArraySize];
        for (int i=0;i<nPETail;i++) {
            space.aPE[i]=new Point(aPE[i]);
        }
        for (int i=0;i<nNETail;i++) {
            space.aNE[i]=new Point(aNE[i]);
        }
        return space;
    }

    /**
     * 把本二维空间中的电荷转化为JSON数据。
     *
     * @param indentSpaces 指定JSON格式数据的缩进，如果设置位0，那么不使用缩进。
     * @return 返回转化得到的字符串。
     */
    public String toString(int indentSpaces){
        String s="";
        try {
            JSONObject file = new JSONObject();
            JSONObject header = new JSONObject();
            JSONObject space = new JSONObject();
            JSONObject PEs=new JSONObject();
            JSONObject NEs=new JSONObject();
            header.put("version", "1.0.0");

            JSONArray PEPoints = new JSONArray();
            for(int i=0;i<nPETail;i++){
                Point p = aPE[i];
                JSONObject point = new JSONObject();
                point.put("x",p.x);
                point.put("y",p.y);
                PEPoints.put(point);
            }
            PEs.put("points",PEPoints);

            JSONArray NEPoints = new JSONArray();
            for(int i=0;i<nNETail;i++){
                Point p = aNE[i];
                JSONObject point = new JSONObject();
                point.put("x",p.x);
                point.put("y",p.y);
                NEPoints.put(point);
            }
            NEs.put("points",NEPoints);

            space.put("scale", nScale);
            space.put("pe",PEs);
            space.put("ne",NEs);
            file.put("header", header);
            file.put("space", space);

            s = file.toString(indentSpaces);
        } catch (Exception e){
            e.printStackTrace();
        }
        return s;
    }

    /**
     * 把本二维空间的数据写到文件。
     *
     * @param out 指定输出的目的文件流。
     */
    public void saveToFile(FileOutputStream out){
        try {
            out.write(toString(0).getBytes());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 从JSON字符串数据中加载电荷数据到本二维空间。加载前会先清空本二维空间的电荷。
     *
     * @param strJson 指定JSON字符串数据。
     * @return 加载成功返回true，否则返回false。
     */
    public boolean loadFromString(String strJson){
        boolean ret=false;
        try {
            //Log.i("GARD","data:"+strJson);
            JSONObject file = new JSONObject(strJson);
            clear();
/*
TODO: 校验版本号,目前只有一个版本，故不需要处理
            JSONObject header = file.get("header");
            if(header.get("version").toString().compareTo("1.0.0")<0)
            {

            }
*/
            JSONObject space = file.getJSONObject("space");
            JSONObject PEs=space.getJSONObject("pe");
            JSONObject NEs=space.getJSONObject("ne");
            nScale = space.getInt("scale");
            //Log.i("D2S","scale:"+nScale);
            if(PEs!=null){
                JSONArray PEPoints = PEs.getJSONArray("points");
                for (int i = 0; i < PEPoints.length(); i++){
                    JSONObject point = PEPoints.getJSONObject(i);
                    Point pos = new Point(point.getInt("x"),point.getInt("y"));
                    addPE(pos);
                }
            }
            if(NEs!=null){
                JSONArray NEPoints = NEs.getJSONArray("points");
                for (int i = 0; i < NEPoints.length(); i++){
                    JSONObject point = NEPoints.getJSONObject(i);
                    Point pos = new Point(point.getInt("x"),point.getInt("y"));
                    addNE(pos);
                }
            }
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 从指定的文件流中读取电荷数据到本二维空间。
     *
     * @param in 指定读取数据的文件流。
     */
    public void loadFromFile(FileInputStream in){
        try {
            int nSize = in.available();
            //Log.i("GARD","filesize:"+nSize);
            if(nSize>0) {
                byte [] data = new byte[nSize];
                nSize = in.read(data);
                if(nSize>0){
                    String strJson=new String(data);
                    //Log.i("GARD","read num:"+nSize);
                    loadFromString(strJson);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加正电荷。
     *
     * @param pos 指定正电荷坐标。
     */
    public void addPE(Point pos) {
        if(nPETail==aPE.length) return; //数组已满
        aPE[nPETail] = new Point(pos);
        nPETail++;
    }

    /**
     * 增加负电荷。
     *
     * @param pos 指定负电荷坐标。
     */
    public void addNE(Point pos) {
        if(nNETail==aNE.length) return; //数组已满
        aNE[nNETail] = new Point(pos);
        nNETail++;
    }

    /**
     * 删除正电荷。
     *
     * @param nIndex 指定正电荷在数组中的索引号，从0开始。
     */
    private void delPE(int nIndex) {
        if(nIndex!=nPETail-1) {
            aPE[nIndex] = aPE[nPETail-1];
        }
        nPETail--;
    }

    /**
     * 删除负电荷。
     *
     * @param nIndex 指定负电荷在数组中的索引号，从0开始。
     */
    private void delNE(int nIndex) {
        if(nIndex!=nNETail-1) {
            aNE[nIndex] = aNE[nNETail-1];
        }
        nNETail--;

    }

    /**
     * 取矩形区域内的电荷。
     *
     * @param rect 指定矩形区域。
     * @return 返回矩形区域内的电荷。
     */
    public SelectedElectrons getSelectedElectrons(Rect rect) {
        SelectedElectrons items=new SelectedElectrons();
        int nIndex;
        rect.sort();
        for (nIndex=nPETail-1;nIndex>-1;nIndex--){
            if(rect.contains(aPE[nIndex].x,aPE[nIndex].y)){
                SelectedElectron item = new SelectedElectron();
                item.aPos = aPE;
                item.nIndex = nIndex;
                items.put(item);
            }
        }
        for (nIndex=nNETail-1;nIndex>-1;nIndex--){
            if(rect.contains(aNE[nIndex].x,aNE[nIndex].y)){
                SelectedElectron item = new SelectedElectron();
                item.aPos = aNE;
                item.nIndex = nIndex;
                items.put(item);
            }
        }
        if(items.list.isEmpty()) {
            items = null;
        }
        return items;
    }

    /**
     * 取点附近的电荷。
     *
     * @param x 指定点的X坐标；
     * @param y 指定点的Y坐标；
     * @return 返回点附近的电荷。
     */
    public SelectedElectrons getSelectedElectrons(int x,int y) {
        SelectedElectrons items=new SelectedElectrons();
        int nIndex;
        for (nIndex=nPETail-1;nIndex>-1;nIndex--){
            if(x>=aPE[nIndex].x&&x<=aPE[nIndex].x+nScale&&y>=aPE[nIndex].y&&y<=aPE[nIndex].y+nScale){
                SelectedElectron item = new SelectedElectron();
                item.aPos = aPE;
                item.nIndex = nIndex;
                items.put(item);
            }
        }
        for (nIndex=nNETail-1;nIndex>-1;nIndex--){
            if(x>=aNE[nIndex].x&&x<=aNE[nIndex].x+nScale&&y>=aNE[nIndex].y&&y<=aNE[nIndex].y+nScale){
                SelectedElectron item = new SelectedElectron();
                item.aPos = aNE;
                item.nIndex = nIndex;
                items.put(item);
            }
        }
        if(items.list.isEmpty()) {
            items = null;
        }
        return items;
    }

    /**
     * 添加一组电荷。
     *
     * @param items 指定需要添加的电荷；
     * @param x 用来计算新的点和原来的点的横坐标的偏移量，偏移量为x-nMinX，其中nMinX为所有电荷中最小的横坐标值；
     * @param y 用来计算新的点和原来的点的纵坐标的偏移量，偏移量为y-nMinY，其中nMinY为所有电荷中最小的纵坐标值；
     * @return 返回新添加的电荷。
     */
    public SelectedElectrons addElectron(SelectedElectrons items,int x,int y) {
        SelectedElectrons newItems = new SelectedElectrons();
        int nMinX = 0x07fffffff;
        int nMinY = 0x07fffffff;
        int dx;
        int dy;
        for(SelectedElectron item : items.list) {
            if (nMinX > item.aPos[item.nIndex].x) nMinX = item.aPos[item.nIndex].x;
            if (nMinY > item.aPos[item.nIndex].y) nMinY = item.aPos[item.nIndex].y;
        }
        dx = x-nMinX;
        dy = y-nMinY;
        for(SelectedElectron item : items.list) {
            SelectedElectron newItem = new SelectedElectron();
            Point point=new Point(dx+item.aPos[item.nIndex].x,dy+item.aPos[item.nIndex].y);
            if(item.aPos==aPE) {
                newItem.aPos = aPE;
                newItem.nIndex = nPETail;
                addPE(point);
            }
            else if (item.aPos==aNE) {
                newItem.aPos = aNE;
                newItem.nIndex = nNETail;
                addNE(point);
            }
            newItems.put(newItem);
        }
        return newItems;
    }

    /**
     * 删除一组电荷。
     *
     * @param items 指定要删除的电荷。
     */
    public void delElectron(SelectedElectrons items) {
        for(SelectedElectron item : items.list){
            if(item.aPos==aPE){
                delPE(item.nIndex);
            }else if(item.aPos==aNE) {
                delNE(item.nIndex);
            }
        }
    }

    /**
     * 使电荷水平方向间隔相等。
     *
     * @param items 指定电荷。
     */
    public void averageHorizontal(SelectedElectrons items) {
        if(items.list.size()>1) {
            double nMinX;
            double nMaxX;
            double step;
            double x;
            items.sortX();
            SelectedElectron item;
            item = items.list.get(0);
            nMinX = item.aPos[item.nIndex].x;
            item = items.list.get(items.list.size()-1);
            nMaxX = item.aPos[item.nIndex].x;
            /*
            for (SelectedElectron item : items.list) {
                if (nMinX > item.aPos[item.nIndex].x) nMinX = item.aPos[item.nIndex].x;
                if (nMaxX < item.aPos[item.nIndex].x) nMaxX = item.aPos[item.nIndex].x;
            }*/
            step = (nMaxX - nMinX)/(items.list.size()-1);
            x = nMinX;
            for (SelectedElectron item2 : items.list) {
                item2.aPos[item2.nIndex].x = (int)x;
                x+=step;
            }
        }
    }

    /**
     * 使电荷垂直方向间隔相等。
     *
     * @param items 指定电荷。
     */
    public void averageVertical(SelectedElectrons items) {
        if(items.list.size()>1) {
            double nMinY;
            double nMaxY;
            double step;
            double y;
            items.sortY();
            SelectedElectron item;
            item = items.list.get(0);
            nMinY = item.aPos[item.nIndex].y;
            item = items.list.get(items.list.size()-1);
            nMaxY = item.aPos[item.nIndex].y;
            /*
            for (SelectedElectron item : items.list) {
                if (nMinY > item.aPos[item.nIndex].y) nMinY = item.aPos[item.nIndex].y;
                if (nMaxY < item.aPos[item.nIndex].y) nMaxY = item.aPos[item.nIndex].y;
            }*/
            step = (nMaxY - nMinY)/(items.list.size()-1);
            y = nMinY;
            for (SelectedElectron item2 : items.list) {
                item2.aPos[item2.nIndex].y = (int)y;
                y+=step;
            }
        }
    }

    /**
     * 使电荷自动间隔相等。
     * 通过第一行电荷计算每行有多少个电荷，然后就可以计算出有多少列，然后分别使这些电荷的水平间隔相等和垂直方向的间隔相等。
     *
     * @param items 指定电荷。
     */
    public void averagerAuto(SelectedElectrons items) {
        if(items.list.size()>0) {
            double nMinX;
            double nMaxX;
            double nMinY;
            double nMaxY;
            double nStepX;
            double nStepY;
            double x,y;
            int nColCount=0;
            int nRowCount=0;
            int nCol;
            items.sortYX();
            SelectedElectron item;
            item = items.list.get(0);
            nMaxX = nMinX = item.aPos[item.nIndex].x;
            nMinY = item.aPos[item.nIndex].y;
            item = items.list.get(items.list.size()-1);
            nMaxY = item.aPos[item.nIndex].y;
            for (SelectedElectron item1 : items.list) {
                if(item1.aPos[item1.nIndex].y!=nMinY) break;
                if (nMinX > item1.aPos[item1.nIndex].x) nMinX = item1.aPos[item1.nIndex].x;
                if (nMaxX < item1.aPos[item1.nIndex].x) nMaxX = item1.aPos[item1.nIndex].x;
                nColCount++;
            }
            if(nColCount>1) nStepX = (nMaxX-nMinX)/(nColCount-1);
            else nStepX = 0;
            nRowCount = (int)Math.ceil(1.0*items.list.size()/nColCount);
            if(nRowCount>1) nStepY = (nMaxY - nMinY)/(nRowCount-1);
            else nStepY = 0;
            y = nMinY;
            x = nMinX;
            nCol = 0;
            for (SelectedElectron item2 : items.list) {
                item2.aPos[item2.nIndex].x = (int)x;
                item2.aPos[item2.nIndex].y = (int)y;
                nCol++;
                if(nCol==nColCount) {
                    nCol = 0;
                    x = nMinX;
                    y+=nStepY;
                } else {
                    x+=nStepX;
                }
            }
        }
    }

    /**
     * 对电荷进行水平方向的反转。
     *
     * @param items 指定电荷。
     */
    public void revertHorizontal(SelectedElectrons items) {
        if(items.list.size()>1) {
            int nForward=0;
            int nBackWard=items.list.size()-1;
            items.sortX();
            for (;nForward<nBackWard;++nForward,--nBackWard) {
                int nTemp;
                SelectedElectron item1=items.list.get(nForward);
                SelectedElectron item2=items.list.get(nBackWard);
                nTemp = item1.aPos[item1.nIndex].x;
                item1.aPos[item1.nIndex].x = item2.aPos[item2.nIndex].x;
                item2.aPos[item2.nIndex].x = nTemp;
            }
        }
    }

    /**
     * 对电荷进行垂直方向的反转。
     *
     * @param items 指定电荷。
     */
    public void revertVertical(SelectedElectrons items) {
        if(items.list.size()>1) {
            int nForward=0;
            int nBackWard=items.list.size()-1;
            items.sortY();
            for (;nForward<nBackWard;++nForward,--nBackWard) {
                int nTemp;
                SelectedElectron item1=items.list.get(nForward);
                SelectedElectron item2=items.list.get(nBackWard);
                nTemp = item1.aPos[item1.nIndex].y;
                item1.aPos[item1.nIndex].y = item2.aPos[item2.nIndex].y;
                item2.aPos[item2.nIndex].y = nTemp;
            }
        }
    }

    /**
     * 对电荷进行正负性的反转。
     *
     * @param items 指定电荷。
     */
    public void revertPN(SelectedElectrons items) {
        if(items.list.size()>0) {
            SelectedElectrons newItems = new SelectedElectrons();
            for (SelectedElectron item : items.list) {
                SelectedElectron newItem = new SelectedElectron();
                if(item.aPos == aPE) { // 正电荷，需要转化为负电荷
                    addNE(item.aPos[item.nIndex]);
                    newItem.aPos = aNE;
                    newItem.nIndex = nNETail;
                    delPE(item.nIndex);
                } else if(item.aPos==aNE){ // 负电荷，需要转化为正电荷
                    addPE(item.aPos[item.nIndex]);
                    newItem.aPos = aPE;
                    newItem.nIndex = nPETail;
                    delNE(item.nIndex);
                } else {
                    break;
                }
                newItems.put(newItem);
            }
            items = null;
            items = newItems;
        }
    }

    /**
     * 对电荷进行向左对齐。
     *
     * @param items 指定电荷。
     */
    public void alignLeft(SelectedElectrons items) {
        int nMinX=0x07fffffff;
        for(SelectedElectron item : items.list){
            if(nMinX>item.aPos[item.nIndex].x) nMinX=item.aPos[item.nIndex].x;
        }
        for(SelectedElectron item : items.list){
            item.aPos[item.nIndex].x = nMinX;
        }
    }

    /**
     * 对电荷进行向右对齐。
     *
     * @param items 指定电荷。
     */
    public void alignRight(SelectedElectrons items) {
        int nMaxX=0;
        for(SelectedElectron item : items.list){
            if(nMaxX<item.aPos[item.nIndex].x) nMaxX=item.aPos[item.nIndex].x;
        }
        for(SelectedElectron item : items.list){
            item.aPos[item.nIndex].x = nMaxX;
        }
    }

    /**
     * 对电荷按顶部对齐。
     *
     * @param items 指定电荷。
     */
    public void alignTop(SelectedElectrons items) {
        int nMinY=0x07fffffff;
        for(SelectedElectron item : items.list){
            if(nMinY>item.aPos[item.nIndex].y) nMinY=item.aPos[item.nIndex].y;
        }
        for(SelectedElectron item : items.list){
            item.aPos[item.nIndex].y = nMinY;
        }
    }

    /**
     * 对电荷按底部对齐。
     *
     * @param items 指定电荷。
     */
    public void alignBottom(SelectedElectrons items) {
        int nMaxY=0;
        for(SelectedElectron item : items.list){
            if(nMaxY<item.aPos[item.nIndex].y) nMaxY=item.aPos[item.nIndex].y;
        }
        for(SelectedElectron item : items.list){
            item.aPos[item.nIndex].y = nMaxY;
        }
    }

    /**
     * 计算两个点的距离。
     *
     * @param p1 指定第一个点。
     * @param p2 指定第二个点。
     * @return 返回点的距离。
     */
    private static double distance(Point p1, Point p2) {
        int x=p1.x - p2.x;
        int y=p1.y - p2.y;
        //return (int)(Math.sqrt((double)(x*x+y*y)));
        return Math.hypot((double)x,(double)y);
    }

    /**
     * 计算二维空间某个点的正电场的强度。
     *
     * @param pos 指定点的位置。
     * @return 返回正电场的强度。
     */
    public double calcPEFieldValue(Point pos){
        double nFieldValue=0.0;        // 临时电场值
        int nIndex;
        for (nIndex=nPETail-1;nIndex>-1;nIndex--){
            if(pos==aPE[nIndex]) {
                nFieldValue += nElectricCenterFieldValue;
                break;
            } else {
                double n = distance(pos,aPE[nIndex]);
                if(n>0) {
                    /*
                    根据库伦定律可知，在三维的空间中，电场的强度与距离的平方成反比。
                    但是，这里是二维空间，即三维空间的一个切面，那么电场的强度应该是与距离成反比。
                    * */
                    n = nElectricCenterFieldValue/n;
                    if (n > 0) nFieldValue += n;
                } else {
                    nFieldValue += nElectricCenterFieldValue;
                }
            }
        }
        return nFieldValue;
    }

    /**
     * 计算二维空间某个点的负电场的强度。
     *
     * @param pos 指定点的位置。
     * @return 返回负电场的强度。
     */
    public double calcNEFieldValue(Point pos){
        double nFieldValue=0.0;        // 临时电场值
        int nIndex;
        for (nIndex=nNETail-1;nIndex>-1;nIndex--){
            if(pos==aNE[nIndex]) {
                nFieldValue += nElectricCenterFieldValue;
                break;
            } else {
                double n = distance(pos,aNE[nIndex]);
                if(n>0) {
                    /*
                    根据库伦定律可知，在三维的空间中，电场的强度与距离的平方成反比。
                    但是，这里是二维空间，即三维空间的一个切面，那么电场的强度应该是与距离成反比。
                    * */
                    n = nElectricCenterFieldValue/n;
                    if (n > 0) nFieldValue += n;
                } else {
                    nFieldValue += nElectricCenterFieldValue;
                }
            }
        }
        return nFieldValue;
    }

    /**
     * 以一定的算法取某个点的值，仅仅用来向用户提示该点的值的大小，绘图不会使用本函数。
     *
     * @param x 指定点的横坐标；
     * @param y 指定点的纵坐标；
     * @param mode 指定画图模式；
     * @return 返回计算出来的值。
     */
    public int getValue(int x,int y,DrawMode mode){
        Point pos=new Point(x,y);
        double nValue;
        double nPEValue = calcPEFieldValue(pos);
        double nNEValue = calcNEFieldValue(pos);
        switch (mode){
            case emORG: // 画原始图
                nValue = nPEValue+nNEValue;
                break;
            case emEA: //电以太图
                nValue = nPEValue+nNEValue;
                break;
            case emLA: //光以太图
                nValue = Math.min(nPEValue,nNEValue);
                break;
            case emGA: //重力以太图
                nValue = nElectricCenterFieldValue*2-nPEValue-nNEValue;
                break;
            default:
                nValue=0;
                break;
        }
        return (int)nValue;
    }

    /**
     * 把本二维空间画到指定的bitmap去。
     *
     * @param canvas 指定目的bitmap;
     * @param lineRect 指定一条直线，需要画出直线上的点对应的场的强度。
     * @param mode 指定需要画什么图。
     */
    public void draw(BitmapCanvas canvas,Rect lineRect,DrawMode mode){
        int nWidth;
        int nHeight;
        nWidth = canvas.getWidth();
        nHeight = canvas.getHeight();
        int nMaxColor = canvas.getMaxColor();
        double nGARatio=4.0;        /**< 重力以太相对电以太的比值，大小未知，这里先设置成4 */
        double nMaxFieldValue=0.0;  /**< 最大电场值 */
        double nMaxPEFieldValue=0.0;  /**< 最大正电场值 */
        double nMaxNEFieldValue=0.0;  /**< 最大负电场值 */
        int x,y;
        int lineDx=0;
        int lineDy=0;
        Point pos=new Point(0,0);
        double aPEFieldValue[][];                  /**< 正电场值 */
        double aNEFieldValue[][];                  /**< 负电场值 */
        aPEFieldValue = new double[nHeight][nWidth];
        aNEFieldValue = new double[nHeight][nWidth];
        if(lineRect!=null) {
            lineDx = lineRect.right-lineRect.left;
            lineDy = lineRect.bottom-lineRect.top;
        }
        // 算每个点的正电场强度
        for(y=0;y<nHeight;y++){
            pos.y = y;
            for(x=0;x<nWidth;x++){
                pos.x = x;
                aPEFieldValue[y][x] = calcPEFieldValue(pos);
                if(aPEFieldValue[y][x]>nMaxPEFieldValue)
                    nMaxPEFieldValue = aPEFieldValue[y][x];
            }
        }
        // 算每个点的负电场强度
        for(y=0;y<nHeight;y++){
            pos.y = y;
            for(x=0;x<nWidth;x++){
                pos.x = x;
                aNEFieldValue[y][x] = calcNEFieldValue(pos);
                if(aNEFieldValue[y][x]>nMaxNEFieldValue)
                    nMaxNEFieldValue = aNEFieldValue[y][x];
            }
        }
        // 画点
        nMaxFieldValue = nMaxPEFieldValue>nMaxNEFieldValue?nMaxPEFieldValue:nMaxNEFieldValue;
        switch (mode){
            case emORG: // 画原始图
                for (y = 0; y < nHeight; y++) {
                    for (x = 0; x < nWidth; x++) {
                        int red=0;
                        int blue=0;
                        if(nMaxPEFieldValue>0) {
                            red = (int)(aPEFieldValue[y][x]*nMaxColor / nMaxFieldValue);
                        }
                        if(nMaxNEFieldValue>0) {
                            blue = (int)(aNEFieldValue[y][x]*nMaxColor / nMaxFieldValue);
                        }
                        if(red>0||blue>0) {
                            canvas.setPixel2(x, y,red, 0, blue);
                        } else canvas.setPixel2(x,y, 0, 0, 0);
                    }
                }
                if(lineDx!=0||lineDy!=0){
                    int nLastX=0;
                    int nLastY=0;
                    int nLastX2=0;
                    int nLastY2=0;
                    if(Math.abs(lineDx)>Math.abs(lineDy)) {
                        double d = 1.0*lineDy/lineDx;
                        for (x = 0; x < nWidth; x++) {
                            int red=0;
                            int blue=0;
                            y =  (int)d*(x-lineRect.left)+lineRect.top;
                            if(nMaxPEFieldValue>0) {
                                red = (int)(aPEFieldValue[y][x]*nMaxColor / nMaxFieldValue);
                            }
                            if(nMaxNEFieldValue>0) {
                                blue = (int)(aNEFieldValue[y][x]*nMaxColor / nMaxFieldValue);
                            }
                            int n = red*(nHeight-1)/nMaxColor;
                            canvas.setHLine(x, n, nLastY, 255, 255, 0);
                            nLastY = n;
                            n = blue*(nHeight-1)/nMaxColor;
                            canvas.setHLine(x, n,  nLastY2, 0, 255, 255);
                            nLastY2 = n;
                        }
                    } else {
                        double d = 1.0*lineDx/lineDy;
                        for (y = 0; y < nHeight; y++) {
                            int red=0;
                            int blue=0;
                            x =  (int)d*(y-lineRect.top)+lineRect.left;
                            if(nMaxPEFieldValue>0) {
                                red = (int)(aPEFieldValue[y][x]*nMaxColor / nMaxFieldValue);
                            }
                            if(nMaxNEFieldValue>0) {
                                blue = (int)(aNEFieldValue[y][x]*nMaxColor / nMaxFieldValue);
                            }
                            int n = red*(nWidth-1)/nMaxColor;
                            canvas.setVLine(n, y, nLastX,255, 255, 0);
                            nLastX = n;
                            n = blue*(nWidth-1)/nMaxColor;
                            canvas.setVLine(n, y, nLastX2,0, 255, 255);
                            nLastX2 = n;
                        }
                    }
                }
                break;
            case emEA: //电以太图
                for (y = 0; y < nHeight; y++) {
                    for (x = 0; x < nWidth; x++) {
                        int red=0;
                        int blue=0;
                        if(nMaxFieldValue>0) {
                            if (aPEFieldValue[y][x] > aNEFieldValue[y][x]) {
                                red = (int)((aPEFieldValue[y][x] - aNEFieldValue[y][x]) * nMaxColor / nMaxFieldValue);
                            } else if (aPEFieldValue[y][x] < aNEFieldValue[y][x]) {
                                blue = (int)((aNEFieldValue[y][x] - aPEFieldValue[y][x]) * nMaxColor / nMaxFieldValue);
                            }
                        }
                        if(red>0||blue>0) {
                            canvas.setPixel2(x, y, red, 0, blue);
                        } else canvas.setPixel2(x,y, 0, 0, 0);
                    }
                }
                if(lineDx!=0||lineDy!=0){
                    int nLastX=0;
                    int nLastY=0;
                    int nLastX2=0;
                    int nLastY2=0;
                    if(Math.abs(lineDx)>Math.abs(lineDy)) {
                        double d = 1.0*lineDy/lineDx;
                        for (x = 0; x < nWidth; x++) {
                            int red=0;
                            int blue=0;
                            y =  (int)d*(x-lineRect.left)+lineRect.top;
                            if(nMaxFieldValue>0) {
                                if (aPEFieldValue[y][x] > aNEFieldValue[y][x]) {
                                    red = (int)((aPEFieldValue[y][x] - aNEFieldValue[y][x]) * nMaxColor / nMaxFieldValue);
                                } else if (aPEFieldValue[y][x] < aNEFieldValue[y][x]) {
                                    blue = (int)((aNEFieldValue[y][x] - aPEFieldValue[y][x]) * nMaxColor / nMaxFieldValue);
                                }
                            }
                            int n = red*(nHeight-1)/nMaxColor;
                            canvas.setHLine(x,n,nLastY, 255,255,0);
                            nLastY = n;
                            n = blue*(nHeight-1)/nMaxColor;
                            canvas.setHLine(x,n,nLastY2, 0,255,255);
                            nLastY2 = n;
                        }
                    } else {
                        double d = 1.0*lineDx/lineDy;
                        for (y = 0; y < nHeight; y++) {
                            int red=0;
                            int blue=0;
                            x =  (int)d*(y-lineRect.top)+lineRect.left;
                            if(nMaxFieldValue>0) {
                                if (aPEFieldValue[y][x] > aNEFieldValue[y][x]) {
                                    red = (int)((aPEFieldValue[y][x] - aNEFieldValue[y][x]) * nMaxColor / nMaxFieldValue);
                                } else if (aPEFieldValue[y][x] < aNEFieldValue[y][x]) {
                                    blue = (int)((aNEFieldValue[y][x] - aPEFieldValue[y][x]) * nMaxColor / nMaxFieldValue);
                                }
                            }
                            int n = red*(nWidth-1)/nMaxColor;
                            canvas.setVLine(n,y,nLastX,255,255,0);
                            nLastX = n;
                            n = blue*(nWidth-1)/nMaxColor;
                            canvas.setVLine(n,y,nLastX2,0,255,255);
                            nLastX2 = n;
                        }
                    }
                }
                break;
            case emLA: //光以太图
                for (y = 0; y < nHeight; y++) {
                    for (x = 0; x < nWidth; x++) {
                        int nColor;
                        double nValue = Math.min(aPEFieldValue[y][x],aNEFieldValue[y][x]);
                        nColor = (int)(nValue*nMaxColor/nMaxFieldValue);
                        if(nColor>0) {
                            canvas.setPixel2(x, y, nColor, 0, nColor);
                        } else canvas.setPixel2(x,y,0, 0, 0);
                    }
                }
                if(lineDx!=0||lineDy!=0){
                    int nLastX=0;
                    int nLastY=0;
                    if(Math.abs(lineDx)>Math.abs(lineDy)) {
                        double d = 1.0*lineDy/lineDx;
                        for (x = 0; x < nWidth; x++) {
                            y =  (int)d*(x-lineRect.left)+lineRect.top;
                            int nColor;
                            double nValue = Math.min(aPEFieldValue[y][x],aNEFieldValue[y][x]);
                            nColor = (int)(nValue*nMaxColor/nMaxFieldValue);
                            int n = nColor*(nHeight-1)/nMaxColor;
                            canvas.setHLine(x, n, nLastY, 0, 255, 0);
                            nLastY = n;
                        }
                    } else {
                        double d = 1.0*lineDx/lineDy;
                        for (y = 0; y < nHeight; y++) {
                            x =  (int)d*(y-lineRect.top)+lineRect.left;
                            int nColor;
                            double nValue = Math.min(aPEFieldValue[y][x],aNEFieldValue[y][x]);
                            nColor = (int)(nValue*nMaxColor/nMaxFieldValue);
                            int n = nColor*(nWidth-1)/nMaxColor;
                            canvas.setVLine(n, y, nLastX, 0, 255, 0);
                            nLastX = n;
                        }
                    }
                }
                break;
            case emGA: //重力以太图
                for (y = 0; y < nHeight; y++) {
                    for (x = 0; x < nWidth; x++) {
                        int nColor;
                        double nValue = nElectricCenterFieldValue*nGARatio-aPEFieldValue[y][x]-aNEFieldValue[y][x];
                        nColor = (int)(nValue*nMaxColor/(nElectricCenterFieldValue*nGARatio));
                        if(nColor>0) {
                            canvas.setPixel2(x, y, nColor, nColor, nColor);
                        }
                        else {
                            canvas.setPixel2(x, y, 0, 0, 0);
                            //Log.i("GA","x:"+x+",y:"+y);
                        }
                    }
                }
                if(lineDx!=0||lineDy!=0){
                    int nLastX=0;
                    int nLastY=0;
                    if(Math.abs(lineDx)>Math.abs(lineDy)) {
                        double d = 1.0*lineDy/lineDx;
                        for (x = 0; x < nWidth; x++) {
                            y =  (int)d*(x-lineRect.left)+lineRect.top;
                            double nValue = nElectricCenterFieldValue*nGARatio-aPEFieldValue[y][x]-aNEFieldValue[y][x];
                            if(nValue>0) {
                                int nColor;
                                nColor = (int) (nValue * nMaxColor / (nElectricCenterFieldValue * nGARatio));
                                int n = nColor * (nHeight - 1) / nMaxColor;
                                canvas.setHLine(x, n, nLastY, 0, 255, 0);
                                nLastY = n;
                            }
                        }
                    } else {
                        double d = 1.0*lineDx/lineDy;
                        for (y = 0; y < nHeight; y++) {
                            x =  (int)d*(y-lineRect.top)+lineRect.left;
                            double nValue = nElectricCenterFieldValue*nGARatio-aPEFieldValue[y][x]-aNEFieldValue[y][x];
                            if(nValue>0) {
                                int nColor;
                                nColor = (int) (nValue * nMaxColor / (nElectricCenterFieldValue * nGARatio));
                                int n = nColor * (nWidth - 1) / nMaxColor;
                                canvas.setVLine(n, y, nLastX, 0, 255, 0);
                                nLastX = n;
                            }
                        }
                    }
                }
                break;
        }
        aPEFieldValue = null;
        aNEFieldValue = null;
    }

    /**
     * 取矩形区域中的电荷的的位置。
     *
     * @param rect 指定矩形区域。
     * @return 返回电荷的的位置的数组。
     */
    public Point[] getPointsInRect(Rect rect) {
        Point [] a = null;
        int nIndex;
        int nCount=0;
        rect.sort();
        for (nIndex=nPETail-1;nIndex>-1;nIndex--) {
            if(rect.contains(aPE[nIndex].x,aPE[nIndex].y)) {
                nCount++;
            }
        }
        for (nIndex=nNETail-1;nIndex>-1;nIndex--) {
            if(rect.contains(aNE[nIndex].x,aNE[nIndex].y)) {
                nCount++;
            }
        }
        if(nCount>0) {
            a = new Point[nCount];
            nCount = 0;
            for (nIndex = nPETail - 1; nIndex > -1; nIndex--) {
                if (rect.contains(aPE[nIndex].x, aPE[nIndex].y)) {
                    a[nCount] = aPE[nIndex];
                    nCount++;
                }
            }
            for (nIndex = nNETail - 1; nIndex > -1; nIndex--) {
                if (rect.contains(aNE[nIndex].x, aNE[nIndex].y)) {
                    a[nCount] = aNE[nIndex];
                    nCount++;
                }
            }
        }
        return a;
    }

    /**
     * 对一组电荷的位置增加指定的偏移量。
     *
     * @param items 指定电荷；
     * @param dx 指定横坐标的偏移量；
     * @param dy 指定纵坐标的偏移量。
     */
    public void move(SelectedElectrons items,int dx,int dy){
        for(SelectedElectron item : items.list){
            if(item.aPos==aPE){
                aPE[item.nIndex].x+=dx;
                aPE[item.nIndex].y+=dy;
            }else if(item.aPos==aNE) {
                aNE[item.nIndex].x+=dx;
                aNE[item.nIndex].y+=dy;
            }
        }
    }

    /**
     * 在这里，电荷本来是不需要大小的，为了确定电荷的位置，对电荷画圆，用来定位电荷。
     *
     * @param canvas 指定画图的画布。
     * @param paint 指定画笔。
     */
    public void drawElectron(Canvas canvas,Paint paint){
        int i;
        int d=nScale/2;
        float r=nElectronRadius*nScale;
        for (i=nPETail-1;i>-1;i--){
            canvas.drawCircle(aPE[i].x*nScale+d,aPE[i].y*nScale+d,r,paint);
        }
        for (i=nNETail-1;i>-1;i--){
            canvas.drawCircle(aNE[i].x*nScale+d,aNE[i].y*nScale+d,r,paint);
        }
    }

    /**
     * 指定放大的倍数。
     *
     * @param scale 指定放大的倍数，不能为0。
     */
    public void setScale(int scale) {
        nScale = scale;
    }

    /**
     * 取放大的倍数。
     *
     * @return 返回放大的倍数。
     */
    public int getScale(){
        return nScale;
    }
}
