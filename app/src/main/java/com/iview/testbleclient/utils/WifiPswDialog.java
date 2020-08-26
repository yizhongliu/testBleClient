package com.iview.testbleclient.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.iview.testbleclient.R;

public class WifiPswDialog extends Dialog {
    private final static String TAG = "WifiPswDialog";
    Button cancelButton;
    Button okButton;
    EditText pwdEdit;
    TextView ssidText;

    String ssid;
    OnWifiPwdListener pwdListener;

    public WifiPswDialog(Context context, String ssid, OnWifiPwdListener listener) {
        super(context);
        this.ssid = ssid;
        pwdListener = listener;

        Log.e(TAG, "WifiPswDialog :" + ssid);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreat");
        setContentView(R.layout.wifi_config_dialog);
        setTitle("请输入密码");

        ssidText = findViewById(R.id.wifissid);
        ssidText.setText(ssid);

        pwdEdit = findViewById(R.id.wifiDialogPsw);
        okButton = findViewById(R.id.wifiDialogCertain);
        okButton.setOnClickListener(buttonDialogListener);
        cancelButton = findViewById(R.id.wifiDialogCancel);
        cancelButton.setOnClickListener(buttonDialogListener);

    }


    private View.OnClickListener buttonDialogListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.wifiDialogCertain: {
                    pwdListener.OnWifiPwdInput(ssid, pwdEdit.getText().toString());
                    dismiss();
                    break;
                }

                case R.id.wifiDialogCancel: {

                    dismiss();
                    break;
                }
            }
        }
    };

    public interface OnWifiPwdListener {
        void OnWifiPwdInput(String ssid, String pwd);
    }
}
