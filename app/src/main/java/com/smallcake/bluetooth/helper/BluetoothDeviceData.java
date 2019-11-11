package com.smallcake.bluetooth.helper;

/*
 * 项目名称：蓝牙调试
 * 开发者：SmallCake
 * Email：66492422@qq.com
 * */

public class BluetoothDeviceData {
    public String Address,Name;

    public BluetoothDeviceData(String Address,String Name){
        this.Address = Address;
        this.Name = Name;
    }

    public String getName() {
        return Name;
    }

    public String getAddress() {
        return Address;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setAddress(String address) {
        Address = address;
    }

    @Override
    public String toString(){
        return Name;
    }
}
