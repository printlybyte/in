package com.watch.in.Tets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.watch.in.R;
import com.watch.in.uitlis.data.NetWorkUtils;

import static android.telephony.CellSignalStrength.SIGNAL_STRENGTH_GOOD;
import static android.telephony.CellSignalStrength.SIGNAL_STRENGTH_GREAT;
import static android.telephony.CellSignalStrength.SIGNAL_STRENGTH_MODERATE;
import static android.telephony.CellSignalStrength.SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
import static android.telephony.CellSignalStrength.SIGNAL_STRENGTH_POOR;

public class volumeTest extends AppCompatActivity {
    private AudioManager audioManager;
    private static final int NETWORKTYPE_WIFI = 0;
    private static final int NETWORKTYPE_4G = 1;
    private static final int NETWORKTYPE_2G = 2;
    private static final int NETWORKTYPE_NONE = 3;
    public TextView mTextView;
    public TelephonyManager mTelephonyManager;
    public PhoneStatListener mListener;
    public int mGsmSignalStrength;
    private NetWorkBroadCastReciver mNetWorkBroadCastReciver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volume_test);
        mTextView = (TextView) findViewById(R.id.textview);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //获取telephonyManager
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //开始监听
        mListener = new PhoneStatListener();
        /**由于信号值变化不大时，监听反应不灵敏，所以通过广播的方式同时监听wifi和信号改变更灵敏*/
        mNetWorkBroadCastReciver = new NetWorkBroadCastReciver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        registerReceiver(mNetWorkBroadCastReciver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTelephonyManager.listen(mListener, PhoneStatListener.LISTEN_SIGNAL_STRENGTHS);

    }

    @Override
    protected void onPause() {
        super.onPause();
        //用户不在当前页面时，停止监听
        mTelephonyManager.listen(mListener, PhoneStatListener.LISTEN_NONE);
    }

    public void aa(View view) {
        audioManager.adjustVolume(AudioManager.ADJUST_LOWER, 0);
        //adjustStreamVolume: 调整指定声音类型的音量
//        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
//                AudioManager.ADJUST_RAISE,
//                AudioManager.FLAG_SHOW_UI);  //调高声音
        //响铃
//        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    public void ab(View view) {
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, 0);
//        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
//                AudioManager.ADJUST_LOWER,
//                AudioManager.FLAG_SHOW_UI);//调低声音
        //震动
//        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }

    public void abc(View view) {
        //静音
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    //    public int getGsmLevel() {
//        int level;
//        // ASU ranges from 0 to 31 - TS 27.007 Sec 8.5
//        // asu = 0 (-113dB or less) is very weak
//        // signal, its better to show 0 bars to the user in such cases.
//        // asu = 99 is a special case, where the signal strength is unknown.
//        int asu = getGsmSignalStrength();
//        if (asu <= 2 || asu == 99) level = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
//        else if (asu >= 12) level = SIGNAL_STRENGTH_GREAT;
//        else if (asu >= 8) level = SIGNAL_STRENGTH_GOOD;
//        else if (asu >= 5) level = SIGNAL_STRENGTH_MODERATE;
//        else level = SIGNAL_STRENGTH_POOR;
//        return level;
//    }
    private class PhoneStatListener extends PhoneStateListener {
        //获取信号强度

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            //获取网络信号强度
            //获取0-4的5种信号级别，越大信号越好,但是api23开始才能用
//            int level = signalStrength.getLevel();
            mGsmSignalStrength = signalStrength.getGsmSignalStrength();
            //网络信号改变时，获取网络信息
            getNetWorkInfo();
        }
    }

    /**
     * 暂时不用这个方法
     */
    public int getNetWorkType(Context context) {
        int mNetWorkType = -1;
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            String type = networkInfo.getTypeName();
            if (type.equalsIgnoreCase("WIFI")) {
                mNetWorkType = NETWORKTYPE_WIFI;
            } else if (type.equalsIgnoreCase("MOBILE")) {
                return isFastMobileNetwork() ? NETWORKTYPE_4G : NETWORKTYPE_2G;
            }
        } else {
            mNetWorkType = NETWORKTYPE_NONE;//没有网络
        }
        return mNetWorkType;
    }

    /**
     * 判断网络速度
     */
    private boolean isFastMobileNetwork() {
        if (mTelephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
            //这里只简单区分两种类型网络，认为4G网络为快速，但最终还需要参考信号值
            return true;
        }
        return false;
    }

    //接收网络状态改变的广播
    class NetWorkBroadCastReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            getNetWorkInfo();
        }
    }

    /**
     * 获取网络的信息
     */
    private void getNetWorkInfo() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            switch (info.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    //wifi
                    WifiManager manager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                    WifiInfo connectionInfo = manager.getConnectionInfo();
                    int rssi = connectionInfo.getRssi();
                    mTextView.setText("当前为wifi网络，信号强度=" + rssi);
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    //移动网络,可以通过TelephonyManager来获取具体细化的网络类型
                    String netWorkStatus = isFastMobileNetwork() ? "4G网络" : "2G网络";
                    mTextView.setText("当前为" + netWorkStatus + "，信号强度=" + mGsmSignalStrength);
                    break;
            }
        } else {
            mTextView.setText("没有可用网络");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNetWorkBroadCastReciver);
    }
}