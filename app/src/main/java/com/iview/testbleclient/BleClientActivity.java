package com.iview.testbleclient;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.iview.testbleclient.mvpbase.BaseView;
import com.iview.testbleclient.mvpbase.IPresenter;
import com.iview.testbleclient.utils.WifiPswDialog;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;

public class BleClientActivity extends BaseView<BleContract.IBlePresent> implements BleContract.IBleView {
    private final static String TAG = "BleClientActivity";
    private final static int REQUEST_ENABLE_BT = 1;

    private List<BleDevice> bleDeviceList;
    BleDeviceAdapter adapter;
    AdapatItemClickListener adapatItemClickListener;
    RecyclerView recyclerView;

    BleDevice selectDevice;

    @Override
    protected BleContract.IBlePresent createPresenter(Context context) {
        return new BleClientPresenter(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapatItemClickListener = new AdapatItemClickListener();

        bleDeviceList = new ArrayList<>();
        recyclerView = findViewById(R.id.bledevicesRecycleView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new BleDeviceAdapter(bleDeviceList);
        adapter.setOnitemClickLintener(adapatItemClickListener);

        recyclerView.setAdapter(adapter);
    }



    @Override
    protected void onResume() {
        super.onResume();
        checkRxPermission();

    }
    //开启蓝牙
    public static void enableBluetooth(Activity activity, int requestCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            Log.e(TAG, "resultCode:" + resultCode);
            if (resultCode == RESULT_OK) {
                mPresenter.startScan();
            } else {
                finish();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void checkRxPermission() {
        RxPermissions rxPermission = new RxPermissions(this);
        rxPermission
                .request(
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            Log.d(TAG, " permission accept");

                            //因为算力棒是在接到开机广播后进行配置的
                            //这个应用也是监听开机广播启动的,所以需要延时2s等算力棒配置完后才启动逻辑
                           // handler.sendEmptyMessageDelayed(0,5000);

                            if (!mPresenter.isSupportBle(BleClientActivity.this)) {
                                finish();
                            }

                            if (mPresenter.isBleEnable(BleClientActivity.this)) {
                                mPresenter.startScan();
                            } else {
                                enableBluetooth(BleClientActivity.this , REQUEST_ENABLE_BT);
                            }

                        } else {
                            // 用户拒绝了该权限，并且选中『不再询问』
                            Log.d(TAG, "perssion is denied.");
                            finish();
                        }
                    }
                });
    }

    public class AdapatItemClickListener implements BleDeviceAdapter.OnitemClick {

        @Override
        public void onItemClick(int position) {
            Log.e(TAG, "onItemClick:" + position);

            Log.e(TAG, "devicelist = " + bleDeviceList.size());
            selectDevice = bleDeviceList.get(position);

            WifiManager mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            String ssid = mWifiManager.getConnectionInfo().getSSID();

            ssid = ssid.replace("\"", "");



            WifiPswDialog pswDialog = new WifiPswDialog(BleClientActivity.this, ssid, new WifiDialogLisntenr());
            pswDialog.show();
        }
    }

    @Override
    public void onNewDeviceAdd(BleDevice device) {
        Log.e(TAG, "onNewDeviceAdd");
        adapter.addData(device);
    }

    @Override
    public void onDeviceUpdate(List<BleDevice> devices) {

    }

    class WifiDialogLisntenr implements WifiPswDialog.OnWifiPwdListener {

        @Override
        public void OnWifiPwdInput(String ssid, String pwd) {
            Log.e(TAG, "pwd:" + pwd);

            if (pwd != null) {

                mPresenter.configWifi(ssid, pwd);
                mPresenter.connect(selectDevice);
            }
        }
    }
}
