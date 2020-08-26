package com.iview.testbleclient;

import android.app.Activity;
import android.content.Context;

import com.iview.testbleclient.mvpbase.IPresenter;
import com.iview.testbleclient.mvpbase.IView;

import java.util.List;

public interface BleContract {
    public interface IBleView extends IView {
        void onNewDeviceAdd(BleDevice device);
        void onDeviceUpdate(List<BleDevice> devices);

    }

    public interface IBlePresent extends IPresenter<IBleView> {
        public boolean isSupportBle(Context context);
        public boolean isBleEnable(Context context);
        public void startScan();
        public void connect(BleDevice device);
        public void configWifi(String ssid, String pwd);
    }
}
