package com.watch.in.moudle;


import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.watch.in.R;
import com.watch.in.service.CheckApService;
import com.watch.in.uitlis.wifi.GPRSUtil;
import com.watch.in.uitlis.wifi.SPUtils;
import com.watch.in.uitlis.wifi.WifiAPManager;
import com.watch.in.uitlis.wifi.WifiConnect;

import static com.watch.in.uitlis.wifi.GPRSUtil.gprsIsOpenMethod;

public class WifiSetting extends Activity implements OnClickListener {

    private String ssid;
    private WifiConnect.WifiCipherType type;
    private String pswd;
    private EditText ssidEt;
    private EditText pswdEt;
    private ConnectivityManager mCM;
    private Button set_wifi_hot;
    private boolean wifiHotIsOpen = false;//wifi热点是否开启
    private WifiAPManager wifiAPManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_setting);
//        getActionBar().setTitle("设置默认连接热点信息");
        initView();
        wifiAPManager = WifiAPManager.getInstance(getApplicationContext());
        //注册handler
        wifiAPManager.regitsterHandler(mHandler);
        mCM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

    }

    private void initView() {
        ssidEt = (EditText) findViewById(R.id.ap_ssid);
        ssidEt.setText((String) SPUtils.get(this, "SPECIFY_SSID", "wanheng"));
        ((RadioGroup) findViewById(R.id.gp))
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (checkedId) {
                            case R.id.rbnt1:
                                type = WifiConnect.WifiCipherType.WIFICIPHER_WEP;
                                break;
                            case R.id.rbnt2:
                                type = WifiConnect.WifiCipherType.WIFICIPHER_WPA;
                                break;
                            case R.id.rbnt3:
                                type = WifiConnect.WifiCipherType.WIFICIPHER_NOPASS;
                                break;
                            case R.id.rbnt4:
                                type = WifiConnect.WifiCipherType.WIFICIPHER_INVALID;
                                break;

                        }

                    }
                });

        pswdEt = (EditText) findViewById(R.id.ap_pswd);
        Button confire = (Button) findViewById(R.id.confire);
        confire.setOnClickListener(this);
        Button set_Mobile_Data = (Button) findViewById(R.id.set_Mobile_Data);
        set_Mobile_Data.setOnClickListener(this);
        set_wifi_hot = (Button) findViewById(R.id.set_Wifi_Hot);
        set_wifi_hot.setOnClickListener(this);

    }


    private boolean checkInput() {
        ssid = ssidEt.getText().toString().trim();
        if (TextUtils.isEmpty(ssid)) {
            Toast.makeText(this, "ap名称未输入", Toast.LENGTH_SHORT).show();
            return false;
        }

        pswd = pswdEt.getText().toString().trim();
        if (TextUtils.isEmpty(pswd)) {
            Toast.makeText(this, "ap密码未输入", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (type == null) {
            Toast.makeText(this, "ap加密类型未选择", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
    //接收message，做处理
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WifiAPManager.MESSAGE_AP_STATE_ENABLED:
                    set_wifi_hot.setText("wifi热点关闭");
                    wifiHotIsOpen = true;
                    break;
                case WifiAPManager.MESSAGE_AP_STATE_FAILED:
                    set_wifi_hot.setText("wifi热点开启");
                    wifiHotIsOpen = false;
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WifiAPManager.getInstance(getApplicationContext()).unregitsterHandler();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.confire:
                if (checkInput()) {
                    SPUtils.put(WifiSetting.this, "SPECIFY_SSID",
                            ssid);
                    SPUtils.put(WifiSetting.this, "SPECIFY_TYPE",
                            type.toString());
                    SPUtils.put(WifiSetting.this, "SPECIFY_PWD",
                            pswd);

                    CheckApService.SPECIFY_SSID = ssid;
                    CheckApService.SPECIFY_TYPE = type;
                    CheckApService.SPECIFY_PWD = pswd;

                    Toast.makeText(WifiSetting.this, "设置成功",
                            Toast.LENGTH_SHORT).show();
                    CheckApService.isConnectting = false;
                    CheckApService.isStop = false;
                }
                break;
            case  R.id.set_Mobile_Data:
                if (gprsIsOpenMethod(mCM)) {
                    GPRSUtil.setGprsOn_Off(false);
                } else {
                    GPRSUtil.setGprsOn_Off(true);
                }
                break;
            case R.id.set_Wifi_Hot:

                if (wifiHotIsOpen) {//关闭wifi
                    wifiAPManager.closeWifiAp();
                } else {//开启wifi热点
                    wifiAPManager.turnOnWifiAp("ssid", "123123123", WifiAPManager.WifiSecurityType.WIFICIPHER_WPA2);
                }


                break;
            default:
                break;
        }
    }

}
