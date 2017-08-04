package com.watch.in.Tets;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.watch.in.R;
import com.watch.in.constant.Constantx;
import com.watch.in.moudle.NotifationBuild;
import com.watch.in.service.CheckApService;
import com.watch.in.service.LaunchService;
import com.watch.in.service.TCPServerService;
import com.watch.in.uitlis.ProperUtil;
import com.watch.in.uitlis.data.LocationUtils;
import com.watch.in.uitlis.data.NetWorkUtils;
import com.watch.in.uitlis.data.RegUtil;
import com.watch.in.uitlis.wifi.SPUtils;
import com.watch.in.uitlis.wifi.WifiAPManager;

import static com.watch.in.uitlis.data.PublicUtils.fileIsExists;

public class UniltTest extends Activity implements View.OnClickListener {

    /**
     * phoneinfo
     */
    private Button mUnilttestBtnTestInfo;
    private TextView mTv_text;

    //电量广播初始化
//    private BatteryReceiver receiver = null;
    //电量
    private int battery;

    //扇区
    private int cid;
    //基站
    private int lac;
    //运营商
    private String operator;


    public NotificationManager mNotificationManager;
    // Notification构造器
    NotificationCompat.Builder mBuilder;
    // NotificationID
    int notifyId = 100;


    //高德地图需要的初始化
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;

    //注册码用字符串
    public String strreg;


    private WifiAPManager wifiAPManager;
    private AudioManager audioManager;

    private WifiManager mWifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        wifiAPManager = WifiAPManager.getInstance(getApplicationContext());

        //初始化LocationUtils，系统自带的gps经纬度初始化

        //广播注册
//        initBroadcast();

        //初始化viiew
//        initView();

        //初始化高德地图定位SDK
//        initLocation();


        if (!fileIsExists()) {
            //配置文件不存在的情况下
            Constantx.cofigIp = "218.246.35.198";
            Constantx.cofigPort = "9000";
            Constantx.cofigIntervalTime = "10";
            Constantx.cofigDbValues = "90";
            Constantx.cofigCloseFlyModeTime = "1";
            Constantx.cofigSoundMode = "SLIENCE";
            Toast.makeText(this, "配置文件不存在，默认配置", Toast.LENGTH_SHORT).show();
        } else {
            //配置文件存在
            Constantx.cofigIp = ProperUtil.getConfigProperties("Ip");
            Constantx.cofigPort = ProperUtil.getConfigProperties("Port");
            Constantx.cofigIntervalTime = ProperUtil.getConfigProperties("IntervalTime");
            Constantx.cofigDbValues = ProperUtil.getConfigProperties("DbValues");
            Constantx.cofigCloseFlyModeTime = ProperUtil.getConfigProperties("CloseFlyModeTime");
            Constantx.cofigSoundMode = ProperUtil.getConfigProperties("SoundMode");

        }
        checkreMoteConfig();

    }

    /**
     * 检查配置文件
     */
    private void checkreMoteConfig() {

        //非必须
//        Constantx.cofigWifiName = ProperUtil.getConfigProperties("WifiName");
//        Constantx.configWifiPwd = ProperUtil.getConfigProperties("WifiPwd");
//        Constantx.cofigEncryptionType = ProperUtil.getConfigProperties("EncryptionType");
        Constantx.cofigWifiHotName = ProperUtil.getConfigProperties("WifiHotName");
        Constantx.cofigWifiHotPwd = ProperUtil.getConfigProperties("WifiHotPwd");
        Constantx.cofigWifiModle = ProperUtil.getConfigProperties("wifimodle");
        Constantx.moblieOnTimer= ProperUtil.getConfigProperties("shangbao_timer");
//        if (!NetWorkUtils.ishasSimCard(getApplicationContext())){
//            Toast.makeText(this, "请检查是佛有sim卡", Toast.LENGTH_SHORT).show();
//
//            finish();
//            return;
//        }
//        if (!NetWorkUtils.ping()) {
//            Toast.makeText(this, "请检查您的网络连接是否正常", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
        if (Constantx.cofigWifiModle.equals("connetedWifi") && !mWifiManager.isWifiEnabled()) {
            Toast.makeText(this, "请开启wifi在试试", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (TextUtils.isEmpty(Constantx.cofigIp) || TextUtils.isEmpty(Constantx.cofigPort) || TextUtils.isEmpty(Constantx.cofigIntervalTime) || TextUtils.isEmpty(Constantx.cofigDbValues) || TextUtils.isEmpty(Constantx.cofigCloseFlyModeTime) || TextUtils.isEmpty(Constantx.cofigSoundMode)) {
            Toast.makeText(this, "请检查配置文件配置信息稍后再再试", Toast.LENGTH_SHORT).show();
            Log.i("QWEQWEQ", "配置文件信息不正常");
            finish();
            return;
        } else {

//        if (!isWifiAvailable(getBaseContext())) {

//            //关闭的时候是没有网络的  所以是
//            Log.i("qweqweq", "开启服务");
//            mWifiManager.setWifiEnabled(true);
//        }
            if (!TextUtils.isEmpty(Constantx.cofigSoundMode)) {
                if (Constantx.cofigSoundMode.equals("SLIENCE")) {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                }
                if (Constantx.cofigSoundMode.equals("SHAKE")) {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                }
            }

            //如果不为空就看看设置的是哪一个
            if (!TextUtils.isEmpty(Constantx.cofigWifiModle)) {
                Log.i("QWEQWE", "配置初始化设置的wifi或者热点 " + Constantx.cofigWifiModle);
                //匹配wifi开始连接
//                if (Constantx.cofigWifiModle.equals("connetedWifi")) {
//                    if (!TextUtils.isEmpty(Constantx.cofigWifiName) && !TextUtils.isEmpty(Constantx.configWifiPwd) && !TextUtils.isEmpty(Constantx.cofigEncryptionType)) {
//
//                        if (mWifiManager.isWifiEnabled()) {
//                            String zz = getWifiTypeConfig(Constantx.cofigEncryptionType);
//                            startWifi(Constantx.cofigWifiName, Constantx.configWifiPwd, zz);
//                            Log.i("QWEQWE", "配置文件连接wifi");
//                        }
//
//                    }
//                }
                if (Constantx.cofigWifiModle.equals("connetedWifiHot")) {
                    //暂时就是热点 就连接热点吧
                    if (!TextUtils.isEmpty(Constantx.cofigWifiHotName) && !TextUtils.isEmpty(Constantx.cofigWifiHotPwd)) {
//                    mWifiManager.setWifiEnabled(false);

                        Log.i("QWEQWE", "配置文件设置热点" + Constantx.cofigWifiHotName + "==" + Constantx.cofigWifiHotPwd);
                        wifiAPManager.turnOnWifiAp(Constantx.cofigWifiHotName, Constantx.cofigWifiHotPwd, WifiAPManager.WifiSecurityType.WIFICIPHER_WPA2);
                    }
                }
            }


            Log.i("QWEQWEQ", "获取到的配置文件信息如下：" + '\n' + "ip :" + Constantx.cofigIp + '\n' + "port :" + Constantx.cofigPort + '\n' + "flytime :" + Constantx.cofigIntervalTime + '\n' + "分贝上限 :" + Constantx.cofigDbValues + '\n' + "分贝fly重启时间 :" + Constantx.cofigCloseFlyModeTime + '\n' + "声音模式 :" + Constantx.cofigSoundMode);
            LocationUtils.initLocation(UniltTest.this);
            start();
        }

    }

    /**
     * 转换配置文件网络类型
     */

    private String getWifiTypeConfig(String aa) {
        String xx = "";
        if (aa != null) {
            if (aa.equals("WPA")) {
                xx = "WIFICIPHER_WPA";
            } else if (aa.equals("WEP")) {
                xx = "WIFICIPHER_WEP";
            } else if (aa.equals("NOPASS")) {
                xx = "WIFICIPHER_NOPASS";
            } else {
                xx = "zzz";
            }
        } else {
            Log.i("QWEQWE", "转换配置文件类型失败");
        }
        return xx;
    }

    /**
     * 开启wifi  判断可用wifi列表中是是否有可用的wifi信息
     */

    private void startWifi(String ssid, String pswd, String typeS) {
        Log.i("qweqwe", "startwifi  启动了几次");
//        for (String s : mListWifiResult) {
//            if (s.equals(ssid)) {
        SPUtils.put(getApplication(), "SPECIFY_SSID",
                ssid);
        SPUtils.put(getApplication(), "SPECIFY_PWD",
                pswd);
        SPUtils.put(getApplication(), "SPECIFY_TYPE",
                typeS.toString());
        CheckApService.SPECIFY_SSID = ssid;
        CheckApService.SPECIFY_PWD = pswd;
        CheckApService.SPECIFY_TYPES = typeS;
        CheckApService.isConnectting = false;
        CheckApService.isStop = false;

//            } else {
//                case7();
//            }
//        }

    }

    private void start() {
        if (Constantx.isStartService) {

            Toast.makeText(this, "开启中...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UniltTest.this, LaunchService.class);
            startService(intent);
//            Intent intent2 = new Intent(UniltTest.this, TCPServerService.class);
//            startService(intent2);
            initNotify();
            finish();
        } else {
            Toast.makeText(this, "已经开启了", Toast.LENGTH_SHORT).show();
            finish();
        }

    }


    /**
     * 广播注册
     */
//    private void initBroadcast() {
//        //电量广播
//        receiver = new BatteryReceiver();
//        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
//        registerReceiver(receiver, filter);//注册BroadcastReceiver
//
//        //
//
//    }

    /**
     * 初始化viiew
     */
    private void initView() {
        mUnilttestBtnTestInfo = (Button) findViewById(R.id.unilttest_btn_test_info);
        mUnilttestBtnTestInfo.setOnClickListener(this);
        mTv_text = (TextView) findViewById(R.id.unilttest_txt_test_info);


    }


    /**
     * 初始化notification
     */
    private void initNotify() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("专用侦查终端系统")
                .setContentText("点此管理定位服务")
                .setContentIntent(
                        getDefalutIntent(Notification.FLAG_AUTO_CANCEL))
                .setTicker("通知")// 通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
                .setOngoing(true)// ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setSmallIcon(R.mipmap.start_icon_zz)
                .setAutoCancel(true);
        mBuilder.setContentIntent(getContentIntent());
        mNotificationManager.notify(notifyId, mBuilder.build());
    }

    private PendingIntent getContentIntent() {
        Intent resultIntent = new Intent(this, NotifationBuild.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    public PendingIntent getDefalutIntent(int flags) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1,
                new Intent(), flags);
        return pendingIntent;
    }


    /**
     * 获取注册码信息
     */
    private void getRegInfo() {
        SharedPreferences sp = getSharedPreferences("RegCode", MODE_PRIVATE);
        strreg = sp.getString("REGCODE", "");
        if (strreg != null && !TextUtils.isEmpty(strreg)) {
        } else {
            initRegCode();
        }
    }

    /**
     * 初始化注册码认证
     */
    private void initRegCode() {
        RegUtil ru = new RegUtil(this);
        ru.SetDialogCancelCallBack(new RegUtil.DialogCancelInterface() {
            @Override
            public void ToFinishActivity() {
                finish();
            }

            @Override
            public void toShowDialog() {
            }
        });
    }


    /**
     * 调用封装好的获取基站信息
     */
//    private void achieveBaseStation() {
//        //获取基站信息
//        int[] station = getBaseStation(UniltTest.this);
//
//        if (station != null) {
//            if (station[5] == 00) {
//                operator = "移动";
//                cid = station[3];
//                lac = station[4];
//            } else if (station[5] == 01) {
//                operator = "联通";
//                cid = station[3];
//                lac = station[4];
//            } else {
//                operator = "电信";
//                cid = station[3];
//                lac = station[4];
//            }
//        }
//    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.unilttest_btn_test_info:
//
//                //启动高的定位
////                startLocation();
//
//
//                //从系统服务获取手机管理者
//                TelephonyManager telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//                //获取网络类型
//                String subscriberId = telManager.getSubscriberId();
//                String deviceId = telManager.getDeviceId();
//                String line1Number = telManager.getLine1Number();
//                String networkType = NetWorkUtils.getNetworkStatusYesNo(UniltTest.this);
//
//                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//
//                //无线网名称
//                String ssid = wifiInfo.getSSID();
//
//                //网络信号强度
//                int rssi = wifiInfo.getRssi();
//
//                //物理mac地址获取
//                String macAddress = wifiInfo.getMacAddress();
//
//                //网络ip地址获取，没有网络是o
//                int ipAddress = wifiInfo.getIpAddress();
//                String s2 = longToIP(ipAddress);
//
//                //调用封装好的基站数据
//                achieveBaseStation();
//
//                if (battery == 0) {
//                    battery = 00;
//                    return;
//                }
//                String s = "手机IMEI :" + deviceId
//                        + "\n手机号码:" + line1Number
//                        + "\n网络类型:" + networkType
//                        + "\n无线网名称:" + ssid
//                        + "\n是否root:" + SystemUitls.isRoot()
//                        + "\n手机电量:" + battery + "%"
//                        + "\n经度    :" + LocationUtils.longitude
//                        + "\n维度    :" + LocationUtils.latitude
//                        + "\n序列号    :" + Build.SERIAL
//                        + "\n系统版本    :" + Build.VERSION.RELEASE
//                        + "\n版本号    :" + Build.DISPLAY
//                        + "\n可用运行内存    :" + SystemUitls.showRAMInfo(UniltTest.this)
//                        + "\n可用内存    :" + SystemUitls.getSDAvailableSize(UniltTest.this)
//                        + "\n" + operator + " cid :" + cid
//                        + "\n" + operator + " lac :" + lac
//                        + "\n" + "信号强度 " + rssi
//                        + "\n" + "物理mac " + macAddress
//                        + "\n" + "网络ip " + s2
//                        + "\n手机IMSI:" + subscriberId;
//
//                mTv_text.setText(s);
                break;
        }
    }


//    //转换ip的方法
//    private String longToIP(long longIp) {
//        StringBuffer sb = new StringBuffer("");
//
//        sb.append(String.valueOf((longIp & 0x000000FF)));
//        // 将高24位置0
//        sb.append(".");
//        sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
//        //  将高1位置0，然后右移8位
//        sb.append(".");
//        //将高8位置0，然后右移16位
//        sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
//        sb.append(".");
//        // 直接右移24位
//        sb.append(String.valueOf((longIp >>> 24)));
//        return sb.toString();
//    }


//    /**
//     * 获取手机电量广播
//     */
//    private class BatteryReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            int current = intent.getExtras().getInt("level");//获得当前电量
//            int total = intent.getExtras().getInt("scale");//获得总电量
//            battery = current * 100 / total;
//        }
//    }


//    /**
//     * 初始化定位
//     *
//     * @author hongming.wang
//     * @since 2.8.0
//     */
//    private void initLocation() {
//        //初始化client
//        locationClient = new AMapLocationClient(this.getApplicationContext());
//        locationOption = getDefaultOption();
//        //设置定位参数
//        locationClient.setLocationOption(locationOption);
//        // 设置定位监听
//        locationClient.setLocationListener(locationListener);
//    }
//
//    /**
//     * 默认的定位参数
//     *
//     * @author hongming.wang
//     * @since 2.8.0
//     */
//    private AMapLocationClientOption getDefaultOption() {
//        AMapLocationClientOption mOption = new AMapLocationClientOption();
//        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
//        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
//        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
//        mOption.setInterval(10000);//可选，设置定位间隔。默认为2秒
//        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
//        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
//        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
//        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
//        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
//        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
//        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
//        return mOption;
//    }
//
//    /**
//     * 定位监听
//     */
//    AMapLocationListener locationListener = new AMapLocationListener() {
//        @Override
//        public void onLocationChanged(AMapLocation location) {
//            if (null != location) {
//
//                StringBuffer sb = new StringBuffer();
//                //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
//                if (location.getErrorCode() == 0) {
//                    sb.append("定位成功" + "\n");
//                    sb.append("定位类型: " + location.getLocationType() + "\n");
//                    sb.append("经    度    : " + location.getLongitude() + "\n");
//                    sb.append("纬    度    : " + location.getLatitude() + "\n");
//                    sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
//                    sb.append("提供者    : " + location.getProvider() + "\n");
//
//                    sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
//                    sb.append("角    度    : " + location.getBearing() + "\n");
//                    // 获取当前提供定位服务的卫星个数
//                    sb.append("星    数    : " + location.getSatellites() + "\n");
//                    sb.append("国    家    : " + location.getCountry() + "\n");
//                    sb.append("省            : " + location.getProvince() + "\n");
//                    sb.append("市            : " + location.getCity() + "\n");
//                    sb.append("城市编码 : " + location.getCityCode() + "\n");
//                    sb.append("区            : " + location.getDistrict() + "\n");
//                    sb.append("区域 码   : " + location.getAdCode() + "\n");
//                    sb.append("地    址    : " + location.getAddress() + "\n");
//                    sb.append("兴趣点    : " + location.getPoiName() + "\n");
//                    //定位完成的时间
//                    sb.append("定位时间: " + LocHeperUitls.formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");
//                } else {
//                    //定位失败
//                    sb.append("定位失败" + "\n");
//                    sb.append("错误码:" + location.getErrorCode() + "\n");
//                    sb.append("错误信息:" + location.getErrorInfo() + "\n");
//                    sb.append("错误描述:" + location.getLocationDetail() + "\n");
//                }
//                //定位之后的回调时间
//                sb.append("回调时间: " + LocHeperUitls.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") + "\n");
//
//                //解析定位结果，
//                String result = sb.toString();
//                Log.i("QWEQWE", "" + result);
//            } else {
//                Log.i("QWEQWE", "" + "定位失败，loc is null");
//            }
//        }
//    };
//
//    // 根据控件的选择，重新设置定位参数
//    private void resetOption() {
//        // 设置是否需要显示地址信息
//        locationOption.setNeedAddress(true);
//        /**
//         * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
//         * 注意：只有在高精度模式下的单次定位有效，其他方式无效
//         */
//        locationOption.setGpsFirst(true);
//        // 设置是否开启缓存
//        locationOption.setLocationCacheEnable(true);
//        // 设置是否单次定位
//        locationOption.setOnceLocation(false);
//        //设置是否等待设备wifi刷新，如果设置为true,会自动变为单次定位，持续定位时不要使用
////        locationOption.setOnceLocationLatest(cbOnceLastest.isChecked());
//        //设置是否使用传感器
////        locationOption.setSensorEnable(cbSensorAble.isChecked());
//        //设置是否开启wifi扫描，如果设置为false时同时会停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
////        String strInterval = etInterval.getText().toString();
//        String strInterval = "2000";
//        if (!TextUtils.isEmpty(strInterval)) {
//            try {
//                // 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
//                locationOption.setInterval(Long.valueOf(strInterval));
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
//        }
//
////        String strTimeout = etHttpTimeout.getText().toString();
//        String strTimeout = "30000";
//        if (!TextUtils.isEmpty(strTimeout)) {
//            try {
//                // 设置网络请求超时时间
//                locationOption.setHttpTimeOut(Long.valueOf(strTimeout));
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    /**
//     * 开始定位
//     *
//     * @author hongming.wang
//     * @since 2.8.0
//     */
//    private void startLocation() {
//        //根据控件的选择，重新设置定位参数
//        //resetOption();
//        // 设置定位参数
//        locationClient.setLocationOption(locationOption);
//        // 启动定位
//        locationClient.startLocation();
//    }
//
//    /**
//     * 停止定位
//     *
//     * @author hongming.wang
//     * @since 2.8.0
//     */
//    private void stopLocation() {
//        // 停止定位
//        locationClient.stopLocation();
//    }
//
//    /**
//     * 销毁定位
//     *
//     * @author hongming.wang
//     * @since 2.8.0
//     */
//    private void destroyLocation() {
//        if (null != locationClient) {
//            /**
//             * 如果AMapLocationClient是在当前Activity实例化的，
//             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
//             */
//            locationClient.onDestroy();
//            locationClient = null;
//            locationOption = null;
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        //电量广播取消注册
//        //停止获取电池电量
//        unregisterReceiver(receiver);
//
//        destroyLocation();
//    }
}
