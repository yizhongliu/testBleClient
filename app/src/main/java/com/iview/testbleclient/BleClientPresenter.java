package com.iview.testbleclient;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanRecord;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.iview.testbleclient.mvpbase.BasePresenter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

public class BleClientPresenter extends BasePresenter<BleContract.IBleView> implements BleContract.IBlePresent {
    private final static String TAG = "BleClientPresenter";

    private Context appContext;

    List<BleDevice> deviceList;
    HashMap<String, BleDevice> deviceHashMap;
    BleDevice selectDevice;

    boolean bStartScan = false; //作为点击搜索刷新整个列表的标记；

    String ssid;
    String pwd;
    boolean bRetry = false;

    BluetoothGattCharacteristic ssidCharacteristic;
    BluetoothGattCharacteristic pwdCharacteristic;
    BluetoothGattCharacteristic ipCharacteristic;

    BluetoothGatt bluetoothGatt;

    BleClientPresenter(Context context) {
        appContext = context.getApplicationContext();

        deviceList = new ArrayList<>();
        deviceHashMap = new HashMap<>();
    }

    @Override
    public void onMvpDestroy() {
        super.onMvpDestroy();

        closeGatt();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void startScan() {
        Log.e(TAG, "startScan");

        deviceHashMap.clear();

        BluetoothManager bluetoothManager = (BluetoothManager) appContext.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothAdapter.startLeScan(new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                //对扫描到的设备进行处理，可以依据BluetoothDevice中的信息、信号强度rssi以及广播包和响应包组成的scanRecord字节数组进行分析
                //device : 识别的远程设备
                //rssi : RSSI的值作为对远程蓝牙设备的报告; 0代表没有蓝牙设备;
                //scanRecod: 远程设备提供的配对号(公告) The content of the advertisement record offered by the remote device.

           //     String struuid = bytes2HexString(reverseBytes(scanRecord)).replace("-", "").toLowerCase();
//                Log.e(TAG, "device name:" + device.getName() + " , mac:" + device.getAddress());
//                Log.e(TAG, "scanRecord" + bytesToHex(scanRecord));
             //   ScanRecord scanRecord1 = parseFromBytes

                if (device.getName() != null && device.getName().equals("BigMirror")) {
                    if (!deviceHashMap.containsKey(device.getAddress())) {
                        BleDevice bleDevice = new BleDevice(device.getName(), device.getAddress(), device);
                        deviceHashMap.put(device.getAddress(), bleDevice);
                        Log.e(TAG, "Add device:" + device.getAddress());
                        getView().onNewDeviceAdd(bleDevice);
                    }
                }



            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void connect(BleDevice device) {
        if (device.getDevice() == null) {
            Log.e(TAG, "device is null");
        }

        selectDevice = device;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothGatt = device.getDevice().connectGatt(appContext,
                    false, mBluetoothGattCallback, TRANSPORT_LE);  //
        } else {
            bluetoothGatt = device.getDevice().connectGatt(appContext,
                    false, mBluetoothGattCallback);  //
        }

    }

    @Override
    public void configWifi(String ssid, String pwd) {
        this.ssid = ssid;
        this.pwd = pwd;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            //连接状态改变回调

            Log.e(TAG, "onConnectionStateChange status：" + status + ", newState:" + newState);
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                if (status == 0) {
                    gatt.connect();
                } else {
                    if (!bRetry) {
                        refreshGattCache(gatt);
                        gatt.disconnect();
                        gatt.close();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            bluetoothGatt = selectDevice.getDevice().connectGatt(appContext,
                                    false, mBluetoothGattCallback, TRANSPORT_LE);  //
                        } else {
                            bluetoothGatt = selectDevice.getDevice().connectGatt(appContext,
                                    false, mBluetoothGattCallback);  //
                        }
                       // selectDevice.getDevice().connectGatt(appContext, true, mBluetoothGattCallback);
                        bRetry = true;
                    }

                }
            } else if (newState == BluetoothGatt.STATE_CONNECTED) {
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            //连接回调，status为0时连接成功
            Log.e(TAG, "onServicesDiscovered status:" + status);
            if (status == 0) {
                UUID uuid = UUID.fromString("10000000-0000-1000-8000-008000000000");
                BluetoothGattService service = gatt.getService(uuid);
                List<BluetoothGattCharacteristic> characteristicsList = service.getCharacteristics();
                for(BluetoothGattCharacteristic charac : characteristicsList) {
                    Log.e(TAG, "find charic:" + charac.getUuid().toString());
                }
                ssidCharacteristic = service.getCharacteristic(UUID.fromString("10000000-0000-1000-8000-008000000001"));
                pwdCharacteristic = service.getCharacteristic(UUID.fromString("10000000-0000-1000-8000-008000000002"));
                ipCharacteristic = service.getCharacteristic(UUID.fromString("10000000-0000-1000-8000-008000000005"));
                gatt.setCharacteristicNotification(ipCharacteristic, true);

                ssidCharacteristic.setValue(ssid);
                gatt.writeCharacteristic(ssidCharacteristic);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.e(TAG, "onCharacteristicWrite characteristic : " + characteristic.getUuid().toString() +",status:" + status);
            if (characteristic.getUuid().toString().equals("10000000-0000-1000-8000-008000000001")) {
                if (status == 0) {

                    pwdCharacteristic.setValue(pwd);
                    gatt.writeCharacteristic(pwdCharacteristic);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            String value = new String(characteristic.getValue());
            Log.e(TAG, "onCharacteristicChanged characteristic : " + characteristic.getUuid().toString() + ", character value:" + value);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    //是否支持
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean isSupportBle(Context context) {
        if (context == null || !context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        return manager.getAdapter() != null;
    }
    //是否开启
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean isBleEnable(Context context) {
        if (!isSupportBle(context)) {
            return false;
        }
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        return manager.getAdapter().isEnabled();
    }

    //scanRecords的格式转换
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void closeGatt(){
        if (bluetoothGatt != null) {
            refreshGattCache(bluetoothGatt);
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public  boolean refreshGattCache(BluetoothGatt gatt) {
        boolean result = false;
        try {
            if (gatt != null) {
                Method refresh = BluetoothGatt.class.getMethod("refresh");
                if (refresh != null) {
                    refresh.setAccessible(true);
                    result = (boolean) refresh.invoke(gatt, new Object[0]);
                }
            }
        } catch (Exception e) {
        }
        return result;
    }
}
