package com.smallcake.bluetooth.debug;

/*
* 项目名称：蓝牙调试
* 开发者：SmallCake
* Email：66492422@qq.com
* */

import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public ArrayList<Fragment> pMainPage = new ArrayList<Fragment>();

    private FragmentManager manager;
    private TextView tvConnect,tvDebug,tvAbout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tvConnect = findViewById(R.id.tvConnect);
        tvDebug = findViewById(R.id.tvDebug);
        tvAbout = findViewById(R.id.tvAbout);

        if(BluetoothAdapter.getDefaultAdapter() == null) {
            new AlertDialog.Builder(this)
                    .setTitle("错误")
                    .setMessage("您的设备不支持蓝牙！")
                    .setPositiveButton("确定", null).show();
        }

        manager = getSupportFragmentManager();

        pMainPage.add(0,new MainPageConnect());
        pMainPage.add(1,new MainPageDebug());
        pMainPage.add(2,new MainPageAbout());

        ((MainPageConnect)pMainPage.get(0)).pMainPageDebug = ((MainPageDebug)pMainPage.get(1));
        ((MainPageDebug)pMainPage.get(1)).pMainPageConnect = ((MainPageConnect)pMainPage.get(0));


        UpdateCurrentPage(0);
    }


    /*
     * 设置显示的页面
     * PageIndex 页面索引
     * */
    public void UpdateCurrentPage(int NewPage){

        FragmentTransaction transaction = manager.beginTransaction();

        if (!pMainPage.get(NewPage).isAdded())
            transaction.add(R.id.MainPage,pMainPage.get(NewPage));

        for (int i = 0;i < pMainPage.size();i++)
            transaction.hide(pMainPage.get(i));

        transaction.show(pMainPage.get(NewPage)).commit();

        UpdateTextColor(NewPage);
    }

    /*
     * 设置底部导航文本颜色
     * index 索引
     * */
    public void UpdateTextColor(int index){

        /*先还原所有标题*/
        tvConnect.setTextColor(Color.rgb(255,255,255));
        tvDebug.setTextColor(Color.rgb(255,255,255));
        tvAbout.setTextColor(Color.rgb(255,255,255));

        switch (index){
            case 0:{
                tvConnect.setTextColor(Color.rgb(255,0,0));
                break;
            }

            case 1:{
                tvDebug.setTextColor(Color.rgb(255,0,0));
                break;
            }

            case 2:{
                tvAbout.setTextColor(Color.rgb(255,0,0));
                break;
            }
        }

    }

    public void btnOnConnect(View v){
        UpdateCurrentPage(0);

    }

    public void btnOnDebug(View v){
        UpdateCurrentPage(1);
    }

    public void btnOnAbout(View v){
        UpdateCurrentPage(2);
    }
}
