package com.iview.testbleclient;

import android.bluetooth.BluetoothDevice;

public class BleDevice {
    String name;
    String mac;
    BluetoothDevice device;

    public BleDevice(String name, String mac, BluetoothDevice device) {
        this.name = name;
        this.mac = mac;
        this.device = device;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }
}
