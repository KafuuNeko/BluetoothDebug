package com.smallcake.bluetooth.debug;

/*
 * 项目名称：蓝牙调试
 * 开发者：SmallCake
 * Email：66492422@qq.com
 * */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.smallcake.bluetooth.helper.BluetoothDeviceData;

import java.util.LinkedList;

public class MainPageConnect extends Fragment {

    /* 变量定义 */
    private View rootView;

    public MainPageDebug    pMainPageDebug = null;
    public BluetoothAdapter pBluetoothAdapter = null;
    public BluetoothSocket pBluetoothSocket = null;

    private Button pBtnSearch,pBtnCloseConnect;
    private ListView pConnectList;
    private TextView pConnectName;
    private LinkedList<BluetoothDeviceData> BluetoothDeviceList = new LinkedList<BluetoothDeviceData>();
    private int nowClickItem = -1;

    private Handler mHandler;

    /*
     * 广播接收*/
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(),dName;
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(!SearchDeviceAddr(device.getAddress())) {
                    dName = device.getName();
                    if (dName == null) dName = "null";
                    BluetoothDeviceList.add(new BluetoothDeviceData(device.getAddress(), dName));
                    DeviceListAdapter.notifyDataSetChanged();//更新

                }
            }
        }

    };

    /*
     * 蓝牙设备列表 */
    private final BaseAdapter DeviceListAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return BluetoothDeviceList.size();
        }

        @Override
        public BluetoothDeviceData getItem(int position) {
            return BluetoothDeviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = null;
            if (convertView == null)
                v = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_bluetooth_device,null);
            else
                v = convertView;

            ((TextView)v.findViewById(R.id.ListItemDeviceName)).setText(getItem(position).getName());

            ((TextView)v.findViewById(R.id.ListItemDeviceAddr)).setText(getItem(position).getAddress());
            return v;

        }
    };

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
    public void onDestroyView() {
        rootView = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();//解除注册
        getActivity().unregisterReceiver(bluetoothReceiver);
    }

    private View CreatePageView(@NonNull LayoutInflater inflater){
        View PageView = inflater.inflate(R.layout.fragment_main_page_connect, null);

        mHandler = new Handler();
        pBtnSearch = (Button) PageView.findViewById(R.id.btnConnectSearch);
        pBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDiscovry();
            }
        });

        pBtnCloseConnect = (Button) PageView.findViewById(R.id.btnPageCloseConnect);

        //关闭蓝牙连接事件
        pBtnCloseConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pBtnCloseConnect.getText().equals("断开连接")) {
                    try {
                        pBluetoothSocket.close();
                        pBluetoothSocket = null;
                        ResetComponentState();
                        if (pMainPageDebug != null) {
                            pMainPageDebug.CloseBluetoothConnect();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "关闭蓝牙连接失败", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    ResetComponentState();
                    if(pBluetoothSocket != null) {
                        if (pBluetoothSocket.isConnected()) {
                            try {
                                pBluetoothSocket.close();
                            }catch (Exception e){

                            }
                        }
                        pBluetoothSocket = null;
                    }
                }
            }
        });

        pConnectName = (TextView) PageView.findViewById(R.id.tvPageConnectName);


        pConnectList = (ListView) PageView.findViewById(R.id.BluetoothConnectList);
        pConnectList.setAdapter(DeviceListAdapter);

        //列表项被单击，连接选中单蓝牙
        pConnectList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(pBluetoothSocket != null) {
                    if (pBluetoothSocket.isConnected()) {
                        try {
                            pBluetoothSocket.close();
                        }catch (Exception e){

                        }
                    }
                    pBluetoothSocket = null;
                }
                nowClickItem = position;

                /*开始禁止操作*/
                pBtnCloseConnect.setEnabled(true);
                pBtnSearch.setEnabled(false);
                pConnectList.setEnabled(false);
                pConnectName.setText("正在连接...");
                pBtnCloseConnect.setText("取消连接");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //通过地址获取连接设备
                            final BluetoothDeviceData ConnectDeviceItem = ((BluetoothDeviceData) DeviceListAdapter.getItem(nowClickItem));
                            final BluetoothDevice ConnectDevice = pBluetoothAdapter.getRemoteDevice(ConnectDeviceItem.getAddress());

                            if (!ConnectDevice.getAddress().equals(ConnectDeviceItem.getAddress())) {
                                ResetComponentState();
                                return;
                            }
                            pBluetoothSocket = (BluetoothSocket) ConnectDevice.getClass().getDeclaredMethod("createRfcommSocket", new Class[]{int.class}).invoke(ConnectDevice, 1);
                            if (pBluetoothSocket == null) {
                                ResetComponentState();
                                return;
                            }
                            if (pBluetoothAdapter.isDiscovering())
                                pBluetoothAdapter.cancelDiscovery();

                            //新建一个线程进行连接操作
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        pBluetoothSocket.connect();
                                        if (!pBluetoothSocket.isConnected()) {
                                            ResetComponentState();
                                            Toast.makeText(getActivity(), "连接失败", Toast.LENGTH_SHORT).show();
                                        } else {
                                            mHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    pBtnCloseConnect.setEnabled(true);
                                                    pBtnSearch.setEnabled(false);
                                                    pConnectName.setText(ConnectDeviceItem.getName());
                                                    pMainPageDebug.OpenBluetoothConnect();
                                                    pConnectList.setEnabled(true);
                                                    pBtnCloseConnect.setText("断开连接");
                                                    Toast.makeText(getActivity(), "连接成功", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }catch (Exception e){
                                        Looper.prepare();
                                        Toast.makeText(getActivity(),"连接失败",Toast.LENGTH_SHORT).show();
                                        Looper.loop();
                                        ResetComponentState();//复位组件状态
                                        System.out.println("蓝牙连接过程中出现异常:"+e.toString());
                                    }

                                }
                            }).start();//开始连接操作线程

                        }catch (Exception e){
                            Looper.prepare();
                            Toast.makeText(getActivity(),"连接失败",Toast.LENGTH_SHORT).show();
                            Looper.loop();
                            ResetComponentState();//复位组件状态
                            System.out.println("蓝牙配置连接过程中出现异常："+e.toString());
                        }
                    }
                }).start();//开始连接线程
            }
        });

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);//注册广播接收信号
        getActivity().registerReceiver(bluetoothReceiver, intentFilter);//用BroadcastReceiver 来取得结果
        pBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (pBluetoothAdapter == null) {
            pBtnSearch.setEnabled(false);
            pBtnSearch.setText("设备不支持");
            Toast.makeText(getActivity(), "设备不支持蓝牙或蓝牙未成功开启", Toast.LENGTH_SHORT).show();
        } else {
            if (pBluetoothAdapter.isEnabled()) {
                pBluetoothAdapter.enable();//如果蓝牙没开启则自动开启蓝牙
            }
        }

        return PageView;
    }

    /*
     * 搜索蓝牙*/
    public void doDiscovry() {
        if (pBluetoothAdapter.isDiscovering()) {
            Toast.makeText(getActivity(),"取消蓝牙扫描",Toast.LENGTH_SHORT).show();
            pBluetoothAdapter.cancelDiscovery();
        } else {
            BluetoothDeviceList.clear();
            DeviceListAdapter.notifyDataSetChanged();//更新
            Toast.makeText(getActivity(),"正在扫描蓝牙",Toast.LENGTH_SHORT).show();
            pBluetoothAdapter.startDiscovery();
        }
    }

    /*
     * 查找蓝牙地址是否已存在 */
    public boolean SearchDeviceAddr(String addr){
        for (int i = 0;i < BluetoothDeviceList.size();i++){

            if(BluetoothDeviceList.get(i).Address.equals(addr)){
                return true;
            }

        }

        return false;
    }

    /*
    * 组件状态复位，用在蓝牙连接失败后复位组件*/
    public void ResetComponentState(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                pConnectList.setEnabled(true);
                pBtnCloseConnect.setEnabled(false);
                pBtnSearch.setEnabled(true);
                pConnectName.setText("未连接");
                pMainPageDebug.OpenBluetoothConnect();
                pBtnCloseConnect.setText("断开连接");
            }
        });
    }

}
