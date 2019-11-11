package com.smallcake.bluetooth.debug;

/*
 * 项目名称：蓝牙调试
 * 开发者：SmallCake
 * Email：66492422@qq.com
 * */

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.smallcake.bluetooth.helper.HelperSystem;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;


public class MainPageDebug extends Fragment {
    public MainPageConnect pMainPageConnect = null;

    private View rootView;
    private EditText pReceiveDataList,etSendData,etSendDataList;
    private StringBuffer receiveBuffer = new StringBuffer();
    private Switch swHexSend,swHexReceive;
    private Button btnDebugEmpty;

    private Handler pHandler = null;
    private final Runnable  receiveRunnable = new Thread(new Runnable() {
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            InputStream mmInStream = null;

            int bytes;
            try {
                 mmInStream = pMainPageConnect.pBluetoothSocket.getInputStream();
            }catch (Exception e){
                System.out.println("获取输入流异常："+e.toString());
            }

            while(true) {
                if (pMainPageConnect == null) {
                    System.out.println("pMainPageConnect为空");
                    break;
                }
                //无连接，退出
                if (pMainPageConnect.pBluetoothSocket == null) {
                    System.out.println("pBluetoothSocket为空");
                    break;
                }

                try {
                    //如果没连接就循环等待
                    while (!pMainPageConnect.pBluetoothSocket.isConnected());

                    bytes = mmInStream.read(buffer);
                    System.out.println("收到蓝牙数据");
                    String data = null;
                    if(!swHexReceive.isChecked()) {
                        data = new String(buffer, 0, bytes, "UTF-8");

                    }else{
                        data = HelperSystem.bytesToHex(Arrays.copyOf(buffer,bytes));
                        data = getFileAddSpace(data);
                    }

                    if (data.length() > 0 && data != null && receiveBuffer != null)
                        receiveBuffer.append(data);

                    if (pHandler != null){
                        pHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                //将内容显示出来
                                pReceiveDataList.setText(receiveBuffer.toString());
                                int offset = pReceiveDataList.getLineCount() * pReceiveDataList.getLineHeight();
                                if (offset > pReceiveDataList.getHeight()) {
                                    pReceiveDataList.scrollTo(0, offset - pReceiveDataList.getHeight());
                                }
                            }
                        });
                    }

                } catch (Exception e) {

                }

                buffer = null;
                buffer = new byte[1024];
            }
        }

    });

    private View CreatePageView(@NonNull LayoutInflater inflater){
        View PageView = inflater.inflate(R.layout.fragment_main_page_debug,null);
        pHandler = new Handler();
        pReceiveDataList = PageView.findViewById(R.id.etReceiveDataList);
        etSendDataList = PageView.findViewById(R.id.etSendDataList);
        etSendData = PageView.findViewById(R.id.etSendData);
        swHexSend = PageView.findViewById(R.id.swHexSend);
        swHexReceive = PageView.findViewById(R.id.swHexReceive);
        btnDebugEmpty = PageView.findViewById(R.id.btnDebugEmpty);


        //发送信息按钮被点击
        ((Button)PageView.findViewById(R.id.btnSendData)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String SendStr = etSendData.getText().toString();
                byte[] SendBytes = null;

                if(SendStr.length() == 0)
                    return;

                try {
                    OutputStream os = pMainPageConnect.pBluetoothSocket.getOutputStream();//获取输出流
                    //判断输出流是否为空
                    if (os != null) {
                        if(swHexSend.isChecked()){
                            //将十六进制转为字节集再发送
                            SendStr.toUpperCase();//转为大写
                            SendBytes = HelperSystem.toBytes(SendStr);
                            if (SendBytes.length > 0 && SendBytes != null)
                                etSendDataList.append(HelperSystem.bytesToHex(SendBytes)+"\n");
                        }else{
                            //直接把Str转为字节集发送
                            SendBytes = SendStr.getBytes("UTF-8");
                            if (SendBytes.length > 0 && SendBytes != null)
                                etSendDataList.append(SendStr+"\n");
                        }
                        if (SendBytes.length > 0 && SendBytes != null)
                            os.write(SendBytes);
                    }
                    os.flush();//将输出流的数据强制提交

                }catch (Exception e){

                }
            }
        });

        btnDebugEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSendDataList.setText("");
                pReceiveDataList.setText("");
                receiveBuffer = null;
                receiveBuffer = new StringBuffer();
            }
        });
        return PageView;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }
        } else {
            rootView = CreatePageView(inflater);
        }

        return rootView;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        rootView = null;
        super.onDestroyView();
    }

    //打开蓝牙连接
    public void OpenBluetoothConnect(){
        new Thread(receiveRunnable).start();
    }

    //关闭蓝牙连接
    public void CloseBluetoothConnect(){

    }

    public static String getFileAddSpace(String replace) {
        String regex = "(.{2})";
        replace = replace.replaceAll(regex, "$1 ");
        return replace;
    }

}
