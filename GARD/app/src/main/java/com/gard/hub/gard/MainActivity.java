package com.gard.hub.gard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewSwitcher;

public class MainActivity extends AppCompatActivity {
    BitmapView bmpView;
    TextView toolbarText;
    String sWorkDir;
    boolean bHelpMode=false;
    boolean bTextMode=false;

    public void showMessage(String s){
        Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);
        setSupportActionBar(toolbar);
        toolbarText = (TextView) findViewById(R.id.toolbarText);
        bmpView = (BitmapView) findViewById(R.id.bmpView);
        ToggleButton toggleButtonHelp = (ToggleButton) findViewById(R.id.toggleButtonHelp);
        toggleButtonHelp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bHelpMode = isChecked;
                buttonView.setChecked(isChecked);
            }
        });
        ToggleButton toggleButtonGrid = (ToggleButton) findViewById(R.id.toggleButtonGrid);
        toggleButtonGrid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(bHelpMode){
                    showMessage("是否画坐标小方格");
                    buttonView.setChecked(!isChecked);
                    return;
                }
                bmpView.setDrawGrid(isChecked);
                buttonView.setChecked(isChecked);
            }
        });
        ToggleButton toggleButtonMoveH = (ToggleButton) findViewById(R.id.toggleButtonMoveH);
        toggleButtonMoveH.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(bHelpMode){
                    showMessage("水平移动选择区域");
                    buttonView.setChecked(!isChecked);
                    return;
                }
                buttonView.setChecked(isChecked);
                bmpView.setMoveH(isChecked);
            }
        });
        ToggleButton toggleButtonMoveV = (ToggleButton) findViewById(R.id.toggleButtonMoveV);
        toggleButtonMoveV.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(bHelpMode){
                    showMessage("垂直移动选择区域");
                    buttonView.setChecked(!isChecked);
                    return;
                }
                buttonView.setChecked(isChecked);
                bmpView.setMoveV(isChecked);
            }
        });
        ToggleButton toggleButton2 = (ToggleButton) findViewById(R.id.toggleButtonLineMode);
        toggleButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(bHelpMode){
                    showMessage("是选择区域还是画线，分析线上粒子的浓度");
                    buttonView.setChecked(!isChecked);
                    return;
                }
                buttonView.setChecked(isChecked);
                bmpView.setLineMode(isChecked);
            }
        });
        ToggleButton toggleButton3 = (ToggleButton) findViewById(R.id.toggleButtonContentMode);
        toggleButton3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(bHelpMode){
                    showMessage("图形模式和文本模式切换");
                    buttonView.setChecked(!isChecked);
                    return;
                }
                ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
                EditText editText = (EditText) findViewById(R.id.editText);
                if(isChecked){
                    bTextMode = true;
                    editText.setText(bmpView.toString());
                    switcher.setDisplayedChild(1);
                } else {
                    switcher.setDisplayedChild(0);
                    try {
                        String s;
                        s = editText.getText().toString();
                        if ( s.isEmpty() == false) {
                            boolean bOK = bmpView.loadFromString(s);
                            if ( bOK == false) {
                                switcher.setDisplayedChild(1);
                                showMessage("文本有误");
                                return;
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    bTextMode = false;
                }
                buttonView.setChecked(isChecked);
            }
        });
        sWorkDir=Environment.getExternalStorageDirectory().getAbsolutePath()+"/GARD";
        SimpleFileDialog.createSubDir(sWorkDir);
        Log.i("GARD", "Work dir:" + sWorkDir);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_file) {
            return true;
        }
        if (id == R.id.action_filter) {
            return true;
        }

        switch (id) {
            case R.id.action_filter_prev:
                if(bHelpMode){
                    showMessage("跳转到前一种图形过滤器");
                    break;
                }
                bmpView.setPrevDrawMode();
                toolbarText.setText(bmpView.getDrawModeName());
                break;
            case R.id.action_filter_next:
                if(bHelpMode){
                    showMessage("跳转到后一种图形过滤器");
                    break;
                }
                bmpView.setNextDrawMode();
                toolbarText.setText(bmpView.getDrawModeName());
                break;
            case R.id.action_filter:
                break;
            case R.id.action_filter_org:
                if(bHelpMode){
                    showMessage("跳转到原始图过滤器");
                    break;
                }
                bmpView.setDrawMode(D2Space.DrawMode.emORG);
                toolbarText.setText(bmpView.getDrawModeName());
                break;
            case R.id.action_filter_e_ether:
                if(bHelpMode){
                    showMessage("跳转到电以太图过滤器");
                    break;
                }
                bmpView.setDrawMode(D2Space.DrawMode.emEA);
                toolbarText.setText(bmpView.getDrawModeName());
                break;
            case R.id.action_filter_l_ether:
                if(bHelpMode){
                    showMessage("跳转到光以太图过滤器");
                    break;
                }
                bmpView.setDrawMode(D2Space.DrawMode.emLA);
                toolbarText.setText(bmpView.getDrawModeName());
                break;
            case R.id.action_filter_g_ether:
                if(bHelpMode){
                    showMessage("跳转到重力以太图过滤器");
                    break;
                }
                bmpView.setDrawMode(D2Space.DrawMode.emGA);
                toolbarText.setText(bmpView.getDrawModeName());
                break;
            case R.id.action_file:
                break;
            case R.id.action_file_open:
                if(bHelpMode){
                    showMessage("打开文件");
                    break;
                }
                onFileOpenClick();
                break;
            case R.id.action_file_save:
                if(bHelpMode){
                    showMessage("保存文件");
                    break;
                }
                if(bTextMode){
                    showMessage("文本模式不能保存文件");
                    break;
                }
                try {
                    if (bmpView.getLastFileName().isEmpty()) onFileSaveClick();
                    else bmpView.saveFile(bmpView.getLastFileName());
                }catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.action_file_saveas:
                if(bHelpMode){
                    showMessage("保存成另外一个文件");
                    break;
                }
                if(bTextMode){
                    showMessage("文本模式不能保存文件");
                    break;
                }
                onFileSaveClick();
                break;
            case R.id.action_file_save_jpg:
                if(bHelpMode){
                    showMessage("保存成一个JPG图片文件");
                    break;
                }
                onJPGFileSaveClick();
                break;
            case R.id.action_file_save_png:
                if(bHelpMode){
                    showMessage("保存成一个PNG图片文件");
                    break;
                }
                onPNGFileSaveClick();
                break;
            case R.id.action_edit:
                break;
            case R.id.action_edit_align:
                break;
            case R.id.action_edit_align_left:
                if(bHelpMode){
                    showMessage("电荷向左对齐");
                    break;
                }
                bmpView.alignLeftElectrons();
                break;
            case R.id.action_edit_align_right:
                if(bHelpMode){
                    showMessage("电荷向右对齐");
                    break;
                }
                bmpView.alignRightElectrons();
                break;
            case R.id.action_edit_align_top:
                if(bHelpMode){
                    showMessage("电荷向上对齐");
                    break;
                }
                bmpView.alignTopElectrons();
                break;
            case R.id.action_edit_align_bottom:
                if(bHelpMode){
                    showMessage("电荷向下对齐");
                    break;
                }
                bmpView.alignBottomElectrons();
                break;
            case R.id.action_edit_average:
                break;
            case R.id.action_edit_average_horizontal:
                if(bHelpMode){
                    showMessage("电荷水平平分");
                    break;
                }
                bmpView.averageElectronsHorizontaly();
                break;
            case R.id.action_edit_average_vertical:
                if(bHelpMode){
                    showMessage("电荷垂直平分");
                    break;
                }
                bmpView.averageElectronsVerticaly();
                break;
            case R.id.action_edit_average_auto:
                if(bHelpMode){
                    showMessage("根据第一行的排列宽度，自动平分选中的电荷");
                    break;
                }
                bmpView.averageElectronsAuto();
                break;
            case R.id.action_edit_revert:
                break;
            case R.id.action_edit_revert_horizontal:
                if(bHelpMode){
                    showMessage("电荷平分反转");
                    break;
                }
                bmpView.revertElectronsHorizontaly();
                break;
            case R.id.action_edit_revert_vertical:
                if(bHelpMode){
                    showMessage("电荷垂直反转");
                    break;
                }
                bmpView.revertElectronsVerticaly();
                break;
            case R.id.action_edit_revert_pn:
                if(bHelpMode){
                    showMessage("电荷正负反转");
                    break;
                }
                bmpView.revertElectronsPN();
                break;
            case R.id.action_edit_copy:
                if(bHelpMode){
                    showMessage("复制选定区域的电荷");
                    break;
                }
                bmpView.copy();
                break;
            case R.id.action_edit_paste:
                if(bHelpMode){
                    showMessage("把复制的电荷粘贴到指定区域");
                    break;
                }
                bmpView.paste();
                break;
            case R.id.action_show_theory:
                Intent intent=new Intent(MainActivity.this,TheoryActivity.class);
                startActivity(intent);
                break;
            case R.id.action_show_about:
                new AboutDialog(this).show();
                break;
            case R.id.action_sample_einstein_space:
                if(bHelpMode){
                    showMessage("本例子揭示爱恩斯坦的空间扭曲的本质");
                    break;
                }
                Sample.showEinsteinSpace(bmpView);
                break;
            case R.id.action_sample_electro_optical_effect:
                if(bHelpMode){
                    showMessage("本例子揭示电光效应的本质");
                    break;
                }
                Sample.showElectroOpticalEffect(bmpView);
                break;
            case R.id.action_sample_coulomb_force1:
                if(bHelpMode){
                    showMessage("本例子揭示同种电荷库仑力的本质");
                    break;
                }
                Sample.showCoulombForce1(bmpView);
                break;
            case R.id.action_sample_coulomb_force2:
                if(bHelpMode){
                    showMessage("本例子揭示异种电荷库仑力的本质");
                    break;
                }
                Sample.showCoulombForce2(bmpView);
                break;
            case R.id.action_sample_coulomb_force3:
                if(bHelpMode){
                    showMessage("本例子揭示混合电荷库仑力的本质");
                    break;
                }
                Sample.showCoulombForce3(bmpView);
                break;
            case R.id.action_sample_parallel_capacitor:
                if(bHelpMode){
                    showMessage("本例子展示平行板电容的重力以太分布，即中间低，两边高，形成一个由外向内的重力场");
                    break;
                }
                Sample.showParallelCapacitor(bmpView);
                break;
            case R.id.action_sample_flying_lifter:
                if(bHelpMode){
                    showMessage("本例子展示飘升机的重力以太分布，即顶部低，底部高");
                    break;
                }
                Sample.showFlyingLifter(bmpView);
                break;
            case R.id.action_sample_flying_lifter2:
                if(bHelpMode){
                    showMessage("本例子展示飘升机的重力以太分布，即顶部低，底部高");
                    break;
                }
                Sample.showFlyingLifter2(bmpView);
                break;
            case R.id.action_sample_flying_saucer:
                if(bHelpMode){
                    showMessage("本例子展示飞碟的重力以太分布，即飞碟内腔产生一个向上的重力场");
                    break;
                }
                Sample.action_sample_flying_saucer(bmpView);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addPEButtonClick(View view) {
        if(bHelpMode){
            showMessage("在指定区域添加一个正电荷");
            return;
        }
        bmpView.addPE();
    }

    public void addNEButtonClick(View view) {
        if(bHelpMode){
            showMessage("在指定区域添加一个负电荷");
            return;
        }
        bmpView.addNE();
    }

    public void deleteButtonClick(View view) {
        if(bHelpMode){
            showMessage("删除指定区域的电荷");
            return;
        }
        bmpView.delElectrons();
    }

    public void redoButtonClick(View view) {
        if(bHelpMode){
            showMessage("选择前一个编辑结果");
            return;
        }
        bmpView.redo();
    }

    public void undoButtonClick(View view) {
        if(bHelpMode){
            showMessage("回退到上次的编辑结果");
            return;
        }
        bmpView.undo();
    }

    public void refleshButtonClick(View view) {
        if(bHelpMode){
            showMessage("刷新");
            return;
        }
        bmpView.reflesh();
    }

    public void scaleUpButtonClick(View view) {
        if(bHelpMode){
            showMessage("放大");
            return;
        }
        bmpView.setScaleUp();
    }

    public void scaleDownButtonClick(View view) {
        if(bHelpMode){
            showMessage("缩小");
            return;
        }
        bmpView.setScaleDown();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void onFileOpenClick() {
        SimpleFileDialog FileOpenDialog =  new SimpleFileDialog(this, "FileOpen",
                new SimpleFileDialog.SimpleFileDialogListener()
                {
                    @Override
                    public void onChosenDir(String chosenDir)
                    {
                        bmpView.loadFile(chosenDir);
                    }
                });
        FileOpenDialog.Default_File_Name = "my_d2pace.json";
        FileOpenDialog.chooseFile_or_Dir(sWorkDir);
    }

    public void onFileSaveClick() {
        SimpleFileDialog FileSaveDialog =  new SimpleFileDialog(MainActivity.this, "FileSave",
                new SimpleFileDialog.SimpleFileDialogListener()
                {
                    @Override
                    public void onChosenDir(String chosenDir)
                    {
                        bmpView.saveFile(chosenDir);
                    }
                });
        FileSaveDialog.Default_File_Name = "my_d2pace.json";
        FileSaveDialog.chooseFile_or_Dir(sWorkDir);
    }

    public void onJPGFileSaveClick() {
        SimpleFileDialog FileSaveDialog =  new SimpleFileDialog(MainActivity.this, "FileSave",
                new SimpleFileDialog.SimpleFileDialogListener()
                {
                    @Override
                    public void onChosenDir(String chosenDir)
                    {
                        bmpView.saveJPGFile(chosenDir);
                    }
                });
        FileSaveDialog.Default_File_Name = "my_d2pace.jpeg";
        FileSaveDialog.chooseFile_or_Dir(sWorkDir);
    }

    public void onPNGFileSaveClick() {
        SimpleFileDialog FileSaveDialog =  new SimpleFileDialog(MainActivity.this, "FileSave",
                new SimpleFileDialog.SimpleFileDialogListener()
                {
                    @Override
                    public void onChosenDir(String chosenDir)
                    {
                        bmpView.savePNGFile(chosenDir);
                    }
                });
        FileSaveDialog.Default_File_Name = "my_d2pace.png";
        FileSaveDialog.chooseFile_or_Dir(sWorkDir);
    }

    public void onFolderChooseeClick() {
        SimpleFileDialog FolderChooseDialog =  new SimpleFileDialog(MainActivity.this, "FolderChoose",
                new SimpleFileDialog.SimpleFileDialogListener()
                {
                    @Override
                    public void onChosenDir(String chosenDir)
                    {
                        String m_chosen;
                        // The code in this function will be executed when the dialog OK button is pushed
                        m_chosen = chosenDir;
                        Toast.makeText(MainActivity.this, "Chosen FileOpenDialog File: " +
                                m_chosen, Toast.LENGTH_LONG).show();
                    }
                });
        FolderChooseDialog.chooseFile_or_Dir(sWorkDir);
    }

    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle("确认退出吗？")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“确认”后的操作
                        MainActivity.this.finish();

                    }
                })
                .setNegativeButton("返回", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“返回”后的操作,这里不设置没有任何操作
                    }
                }).show();
    }
}
