package com.watch.in.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.watch.in.Api.MyMediaRecorder;
import com.watch.in.constant.Constantx;
import com.watch.in.uitlis.ProperUtil;
import com.watch.in.uitlis.ThreadPoolProxy;
import com.watch.in.uitlis.data.FileUtil;
import com.watch.in.uitlis.data.LocHeperUitls;
import com.watch.in.uitlis.data.LocationUtils;
import com.watch.in.uitlis.data.NetWorkUtils;
import com.watch.in.uitlis.data.PublicUtils;
import com.watch.in.uitlis.data.SharedPreferencesUtils;
import com.watch.in.uitlis.data.SystemUitls;
import com.watch.in.uitlis.wifi.GPRSUtil;
import com.watch.in.uitlis.wifi.SPUtils;
import com.watch.in.uitlis.wifi.WifiAPManager;
import com.watch.in.uitlis.wifi.WifiConnect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static com.watch.in.constant.Constantx.VOLUME_SILENCE;
import static com.watch.in.constant.Constantx.VOLUME_VIBRATE;
import static com.watch.in.constant.Constantx.socketflag;
import static com.watch.in.constant.Constantx.socketflagis;
import static com.watch.in.uitlis.data.NetWorkUtils.getBaseStation;
import static com.watch.in.uitlis.data.NetWorkUtils.getOperator;
import static com.watch.in.uitlis.data.NetWorkUtils.getWifiConnected;
import static com.watch.in.uitlis.data.NetWorkUtils.ping;
import static com.watch.in.uitlis.data.PublicUtils.AirplaneModeisOff;
import static com.watch.in.uitlis.data.PublicUtils.fileIsExists;
import static com.watch.in.uitlis.data.PublicUtils.getCurrentTime;
import static com.watch.in.uitlis.data.PublicUtils.getPingMuSize;
import static com.watch.in.uitlis.data.PublicUtils.isOPenGPS;
import static com.watch.in.uitlis.data.PublicUtils.setSettingsOnHigh;
import static com.watch.in.uitlis.data.PublicUtils.setSettingsWifi;
import static com.watch.in.uitlis.data.PublicUtils.upgradeRootPermission;
import static com.watch.in.uitlis.data.ShellUtil.runCommand;
import static com.watch.in.uitlis.data.SystemUitls.isRoot;
import static com.watch.in.uitlis.wifi.GPRSUtil.gprsIsOpenMethod;
import static java.security.AccessController.getContext;

public class LaunchService extends Service {
    //高德地图需要的初始化
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;

    //socket配置文件出错
    private int socketErrey = 44;
    //电量广播初始化
    private BatteryReceiver receiver = null;
    //电量
    private int battery;
    String line1Number = "";
    //扇区
    private int cid;
    //基站
    private int lac;
    //运营商
    private String operator;
    //运营商标识
    private String operatorNum;
    //截取之后的ssid名字
    private String ssid2;
    //总字段
    private String alwaysResult;
    //接收到的信息
    private String acceptinfo;
    //获取本地IMEI
    private String imeilocality;
    //截取##后面的两个标识
    private String statusclassify;

    //初始化网络状态广播
    private ConnectionChangeReceiver myReceiver;

    // 线程池
    // 为了方便展示,此处直接采用线程池进行线程管理,而没有一个个开线程
    private ExecutorService mThreadPool;


    /**
     * 接收服务器消息 变量
     */
    // 输入流对象
    InputStream is;

    // 输入流读取器对象
    InputStreamReader isr;
    BufferedReader br;

    // 接收服务器发送过来的消息
    String response;


    /**
     * 发送消息到服务器 变量
     */
    // 输出流对象
    OutputStream outputStream;

    /**
     * 分贝相关
     */
    private MyMediaRecorder mRecorder;
    private String TAG = "MainActivity";
    float volume = 10000;
    // 用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private String minDb;//最小分贝
    private String timer;//恢复时间间隔

    //从配置文件获取的分贝值与时间
    private String minDbsconfig;
    private String timersconfig;
    //从配置文件获取到的重重复上传数据的时间
    private int timerintervalconfig;
    private String timerintervalconfig2;
    //数据网络开启后 自动开启的时间
    private String mobTimer;


    //用来判断socket网络连接异常的计数器
    private int erreySocketNum = 0;


    /**
     * 交互相关
     */
    //线程管理对象
    private ThreadPoolProxy threadPoolProxy = new ThreadPoolProxy(1, 1, 3000);
    Socket socket = null;


    //与后台交互的timer timerX发送   timerS接受    timersocket 连接
    private Timer timerX, timerS, timersocket, timerPhoneData;
    //与后台交互的任务
    private TimerTask timerTaskX, timerTaskS, timerTaskSsocket, timerTaskPhoneData;

    /**
     * wifi相关
     */
    String networkType;
    String networkType2;
    private String ssid;
    private WifiConnect.WifiCipherType type;
    private String typeS;
    private String pswd;
    private EditText ssidEt;
    private EditText pswdEt;
    private ConnectivityManager mCM;
    private Button set_wifi_hot;
    private boolean wifiHotIsOpen = false;//wifi热点是否开启
    private WifiAPManager wifiAPManager;
    private String signalquality;

    private WifiManager mWifiManager;

    //上传的默认分贝设置
    private String deaufultShoud;
    //上传的GPS开关
    private int gpsStatus;

     //飞行模式的状态
    private int flyingStatus;
    //数据网络状态
    private  int moblieStatus;

    //分贝检测的开关
    private  int fenbeiStatus;
    /**
     *
     *
     */

    AudioManager audioManager;
    public TelephonyManager mTelephonyManager;
    public PhoneStatListener mListener;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what != 0) {
                switch (msg.what) {

                    //暂定1,2为分贝设置
                    case 1:
                        case1();
                        break;
                    case 2:
                        case2();
                        break;

                    case 2222:
                        mRecorder.delete();
                        mHandler.removeMessages(2);
                        break;
                    case 3:
//                        case3(alwaysResult);
                        //开启循环发送接受数据数据
                        if (timerTaskX == null) {
                            timerTaskX = new TimerTask() {
                                @Override
                                public void run() {
                                    if (socketflagis) {
                                        case3(alwaysResult.trim());
                                        case4();
                                        Log.i("QWEQWE", "发送数据");
                                    } else {
                                        Log.i("qweqwe", "连接 异常 不能发送数据");

                                    }
                                }
                            };
                        }
                        timerX = new Timer(true);
                        timerX.schedule(timerTaskX, 1000, timerintervalconfig);
                        timerTaskX.scheduledExecutionTime();

                        break;


                    case 4:
//
                        if (timerTaskS == null) {
                            timerTaskS = new TimerTask() {
                                @Override
                                public void run() {

                                    erreySocketNum++;
                                    if (ping() == false) {
                                        if (erreySocketNum > 5) {
                                            mHandler.sendEmptyMessage(444);
                                        }
                                    }
                                    if (!socketflagis) {
                                        Log.i("QWEQWE", "socket 正在重新连接....." + erreySocketNum);
                                        socketConneted();
                                        if (mWifiManager != null && mWifiManager.isWifiEnabled() && !ping()) {
//                                            mWifiManager.disableNetwork(mWifiManager.getConnectionInfo().getNetworkId());
                                            mWifiManager.removeNetwork(mWifiManager.getConnectionInfo().getNetworkId());

//                                            mWifiManager.disconnect();
                                            mWifiManager.reconnect();

                                            Log.i("qweqwe", "当前连接的wifi是" + mWifiManager.getConnectionInfo().getNetworkId());
                                        } else {
                                            Log.i("qweqwe", "mWifiManager  null ");
                                        }

                                    } else {
                                        Log.i("QWEQWE", "socket 正常");

                                    }
                                    if (erreySocketNum > 20) {
                                        erreySocketNum = 0;
                                    }
                                }
                            };
                        }
                        timerS = new Timer(true);
                        timerS.schedule(timerTaskS, 1000, 50000);

                        break;
                    case 5:
                        if (timerTaskPhoneData == null) {
                            timerTaskPhoneData = new TimerTask() {
                                @Override
                                public void run() {
                                    Log.i("QWEQWE", "间隔5000秒获取一次手机信息");
                                    writeDatePhone();
                                }
                            };
                        }
                        timerPhoneData = new Timer(true);
                        timerPhoneData.schedule(timerTaskPhoneData, 1000, 50000);
                        break;
                    case 6:
                        //所有数据发送成功后都会发送
                        final String result = ("**" + statusclassify + imeilocality + "0#").toString().trim();

                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                case3(result);
                            }
                        };
                        threadPoolProxy.executeTask(runnable);
                        threadPoolProxy.removeTask(runnable);
//                        if (!isWifiAvailable(getBaseContext())) {
//                            //关闭的时候是没有网络的  所以是
//                            mWifiManager.setWifiEnabled(true);
//                        }
                        Log.i("qweqweq", "case6" + result + "发送成功");
                        break;
                    case 7:
                        //所有数据发送失败后都会发送
                        final String result2 = ("**" + statusclassify + imeilocality + "1#").toString().trim();
                        Runnable runnable2 = new Runnable() {
                            @Override
                            public void run() {
                                case3(result2);
                            }
                        };
                        threadPoolProxy.executeTask(runnable2);
                        threadPoolProxy.removeTask(runnable2);

                        Log.i("qweqweq", "case7" + result2 + "发送失败");
                        break;
                    case 8:


                        break;
                    case 9:
                        break;
                    case WifiAPManager.MESSAGE_AP_STATE_ENABLED:
//                        set_wifi_hot.setText("wifi热点关闭");
                        wifiHotIsOpen = true;
                        break;
                    case WifiAPManager.MESSAGE_AP_STATE_FAILED:
//                        set_wifi_hot.setText("wifi热点开启");
                        wifiHotIsOpen = false;
                        break;
                    case 10:
                        Bundle bb = msg.getData();
                        String aaa = bb.getString("a");
                        String bbb = bb.getString("b");
                        SharedPreferencesUtils.saveTosp(getApplication(), "MainActivity", "MINDB", aaa);
                        SharedPreferencesUtils.saveTosp(getApplication(), "MainActivity", "TIMER", bbb);

                        break;

                    case 1010:
                        SharedPreferencesUtils.saveTosp(getApplication(), "MainActivity", "MINDB", "999");
                        break;
                    case 55:
                        //后台返回的逻辑处理
                        case5();
                        break;
                    case 99:

                        break;
                    case 44:
                        //socket  初始化配置文件是Fiore正常
                        if (ping() == true) {
                            Toast.makeText(LaunchService.this, "请检车配置文件的ip或者端口是否正确", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 444:
                        //socket  初始化配置文件是Fiore正常
                        erreySocketNum = 0;
                        if (isWifiAvailable(getBaseContext())) {
                            //关闭的时候是没有网络的  所以是
//                            Log.i("qweqweq", "开启服务");
//                            removeWifi_1();
//                            startWifi2("qweqwe","",null);
//                            mWifiManager.setWifiEnabled(false);

//                            mWifiManager.disableNetwork(1);
//                            mWifiManager.disconnect();
//                            mWifiManager.removeNetwork(1);


                        }
                        Toast.makeText(LaunchService.this, "请检查网络是否有网络连接", Toast.LENGTH_SHORT).show();
                        break;
                    case 4561:
                        case6();
                        break;
                    case 4562:
                        case6();
                        break;

                    default:
                        break;
                }
            } else

            {
                Toast.makeText(LaunchService.this, "msg what null", Toast.LENGTH_SHORT).show();
            }

        }
    };


    public LaunchService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Constantx.isStartService = false;

        if (!fileIsExists()) {
            //配置文件不存在的情况下
            minDbsconfig = Constantx.cofigDbValues;
            timersconfig = Constantx.cofigCloseFlyModeTime;
            timerintervalconfig = Integer.parseInt(Constantx.cofigIntervalTime) * 1000;


            //默认
            mobTimer = Constantx.moblieOnTimer;

        } else {
            //不能重复开启的全局变量
            minDbsconfig = ProperUtil.getConfigProperties("DbValues");
            timersconfig = ProperUtil.getConfigProperties("CloseFlyModeTime");

            String timerintervalconfigs = ProperUtil.getConfigProperties("IntervalTime");
            timerintervalconfig = Integer.parseInt(timerintervalconfigs) * 1000;

            mobTimer = Constantx.moblieOnTimer;

        }
        Log.d("qweqwe", "onCreat 里面获取到的数据 " + "  ===  " + minDbsconfig + "  ===  " + timersconfig + "  ===  " + timerintervalconfig);

        super.onCreate();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //初始化所有控件
        checkreMoteConfig();
        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * 检查配置文件
     */
    private void checkreMoteConfig() {
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        audioManager = (AudioManager) getApplicationContext().getSystemService(AUDIO_SERVICE);
        wifiAPManager = WifiAPManager.getInstance(getApplicationContext());

////        if (!isWifiAvailable(getBaseContext())) {
////            //关闭的时候是没有网络的  所以是
////            Log.i("qweqweq", "开启服务");
////            mWifiManager.setWifiEnabled(true);
////        }
//        if (!TextUtils.isEmpty(Constantx.cofigSoundMode)) {
//            if (Constantx.cofigSoundMode.equals("SLIENCE")) {
//                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
//            }
//            if (Constantx.cofigSoundMode.equals("SHAKE")) {
//                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
//            }
//        }
//
//        //如果不为空就看看设置的是哪一个
//        if (!TextUtils.isEmpty(Constantx.cofigWifiModle)) {
//            Log.i("QWEQWE", "配置初始化设置的wifi或者热点 " + Constantx.cofigWifiModle);
//            //匹配wifi开始连接
//            if (Constantx.cofigWifiModle.equals("connetedWifi")) {
//                if (!TextUtils.isEmpty(Constantx.cofigWifiName) && !TextUtils.isEmpty(Constantx.configWifiPwd) && !TextUtils.isEmpty(Constantx.cofigEncryptionType)) {
//
//                    if (mWifiManager.isWifiEnabled()) {
//                        String zz = getWifiTypeConfig(Constantx.cofigEncryptionType);
//                        startWifi(Constantx.cofigWifiName, Constantx.configWifiPwd, zz);
//                        Log.i("QWEQWE", "配置文件连接wifi");
//                    } else {
//                        Toast.makeText(this, "请打开wifi开关", Toast.LENGTH_SHORT).show();
//
//                    }
//                }
//            }
//            if (Constantx.cofigWifiModle.equals("connetedWifiHot")) {
//                //暂时就是热点 就连接热点吧
//                if (!TextUtils.isEmpty(Constantx.cofigWifiHotName) && !TextUtils.isEmpty(Constantx.cofigWifiHotPwd)) {
////                    mWifiManager.setWifiEnabled(false);
//
//                    Log.i("QWEQWE", "配置文件设置热点" + Constantx.cofigWifiHotName + "==" + Constantx.cofigWifiHotPwd);
//                    wifiAPManager.turnOnWifiAp(Constantx.cofigWifiHotName, Constantx.cofigWifiHotPwd, WifiAPManager.WifiSecurityType.WIFICIPHER_WPA2);
//                }
//            }
//        }


        socketConneted();
        if (socketflagis) {
            initViews();
        } else {
            Log.i("QWEQWEQ", "初始化失败");
        }

    }

    /**
     * 初始化所有控件
     */
    private void initViews() {

        initBroadcast();
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //开始监听wifi信号质量移动信号质量
        mListener = new PhoneStatListener();
        mTelephonyManager.listen(mListener, PhoneStatListener.LISTEN_SIGNAL_STRENGTHS);

        //声音模式改变
//        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //wifi热点


        //注册handler
        wifiAPManager.regitsterHandler(mHandler);
        mCM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);


        //初始化LocationUtils，系统自带的gps经纬度初始化
        LocationUtils.initLocation(getBaseContext());
        //初始化高德地图定位SDK
        initLocation();
        //启动高的定位
        startLocation();


        //获取手机的数据
        mHandler.sendEmptyMessage(5);
        //初始化分贝
//        mHandler.sendEmptyMessage(1);
//        mHandler.sendEmptyMessageDelayed(2222,1000);
        //往后台传送数据
        mHandler.sendEmptyMessage(3);
        //循环检测socket连接状态
        mHandler.sendEmptyMessage(4);


    }


    private void writeDatePhone() {

        //从系统服务获取手机管理者
        TelephonyManager telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        //获取网络类型
        String subscriberId = telManager.getSubscriberId();
        String deviceId = telManager.getDeviceId();
        SharedPreferencesUtils.saveToImei(getBaseContext(), deviceId);
        //
        imeilocality = SharedPreferencesUtils.getToImei(getBaseContext());
//       String dbm =getCurrentNetDBM(getApplicationContext());


        line1Number = telManager.getLine1Number();
        if (line1Number != null && line1Number.length() > 11) {
            line1Number = telManager.getLine1Number().substring(3, 14);
        }

        //获取当前运营商网络
        networkType = NetWorkUtils.getNetworkStatusYesNo(getBaseContext());
        //转化为数字
        networkType2 = NetWorkUtils.getNetWorkSwitchNum(networkType);


        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        List<ScanResult> mlistScanResult = wifiManager.getScanResults();

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        //无线网名称
        String ssid = wifiInfo.getSSID();
        //删除包裹wifi的双引号
        if (!TextUtils.isEmpty(ssid)) {
            StringBuffer bb = new StringBuffer(ssid);
            bb.delete(0, 1).delete(bb.length() - 1, bb.length());
            ssid2 = bb.toString().trim();
            if (!networkType2.equals("1")) {
                ssid2 = "0";
            }
        } else {
            ssid2 = "0";
        }
        //网络信号强度
        int rssi = wifiInfo.getRssi();

        //物理mac地址获取
        String macAddress = wifiInfo.getMacAddress();

        //网络ip地址获取，没有网络是o
        int ipAddress = wifiInfo.getIpAddress();
        String s2 = longToIP(ipAddress);

        //调用封装好的基站数据
        achieveBaseStation();

        if (battery == 0) {
            battery = 00;
            Log.i("QWEQWE", "听着了");
        }
        if ("".equals(signalquality) || networkType2 != null && networkType2.equals("0")) {
            signalquality = "0";
        }

        String cc = SystemUitls.showRAMInfo(getBaseContext());
        String cc2 = SystemUitls.getSDAvailableSize(getBaseContext());
        String s = "手机IMEI :" + deviceId
                + "\n手机号码:" + line1Number
                + "\n网络类型:" + networkType2 + "@" + networkType
                + "\n无线网名称:" + ssid2
                + "\n是否root:" + isRoot()
                + "\n手机电量:" + battery + "%"
                + "\n经度    :" + LocationUtils.longitude
                + "\n维度    :" + LocationUtils.latitude
//                + "\n序列号    :" + Build.SERIAL
                + "\n系统版本    :" + Build.VERSION.RELEASE
//                + "\n版本号    :" + Build.DISPLAY
                + "\n可用运行内存    :" + SystemUitls.showRAMInfo(getBaseContext())
                + "\n可用内存    :" + SystemUitls.getSDAvailableSize(getBaseContext())
                + "\n" + operator + " cid :" + cid
                + "\n" + operator + " lac :" + lac
                + "\n" + "信号强度 " + signalquality
                + "\n" + "物理mac " + macAddress
                + "\n" + "网络ip " + s2
                + "\n手机IMSI:" + subscriberId;
        if (deviceId == null) {
            deviceId = "000000000000000";
        }
        if (subscriberId == null) {
            subscriberId = "000000000000000";
        }
        if (line1Number == null) {
            line1Number = "00000000000";
        }
        if (operatorNum == null) {
            operatorNum = "0";
        }
//        //获取wifi信号强度在多少之上的
//        if (mListWifiResult == null) {
//            mListWifiResult = new ArrayList<>();
//            for (int i = 0; i < mlistScanResult.size(); i++) {
//
//                if (mlistScanResult.get(i).level > -90) {
//                    mListWifiResult.add(mlistScanResult.get(i).SSID);
//                    mListWifiResult.add(String.valueOf(mlistScanResult.get(i).level));
////                    Log.i("QWEQWE", "" + mListWifiResult.get(i));
//                }
//            }
//        }
        //如果配置文件不存在  就是默认值90
        if (!fileIsExists()) {
            deaufultShoud = "90";
        } else {
            deaufultShoud = ProperUtil.getConfigProperties("DbValues");
        }

        if (isOPenGPS(getBaseContext())) {
            gpsStatus = 1;//开启状态为1
        } else {
            //关闭状态为0
            gpsStatus = 0;
        }

      //不是飞行模式返回1  是飞行模式返回0，仅针对root机型
        if (AirplaneModeisOff(getBaseContext())) {
            flyingStatus = 1;
        } else {
            flyingStatus = 0;
        }

        //数据网络的开关状态  开着数据1    没开是0
        if (gprsIsOpenMethod(mCM)) {
             moblieStatus=0;
        }else {
            moblieStatus=1;
        }


        //检测分贝状态 没开是0   开启是1
        if (Constantx.isMediaStart){
            fenbeiStatus=0;
        }else{
            fenbeiStatus=1;
        }

        String cofigtime = ProperUtil.getConfigProperties("IntervalTime");
        alwaysResult = "**21" + deviceId + subscriberId + line1Number + "," + networkType2 + "," + signalquality + "," + macAddress + "," + ssid2 + "," + battery + "," + cc + "," + cc2 + "," + operatorNum + "," + lac + "," + cid + "," + deaufultShoud + "," + soundMode() + "," + gpsStatus + "," + cofigtime + "," + timersconfig + "," + 0+","+moblieStatus+","+flyingStatus+","+fenbeiStatus + "#";
        SharedPreferencesUtils.removePhoneData(getBaseContext());
        SharedPreferencesUtils.saveToPhoneData(getBaseContext(), alwaysResult);
//        Log.i("qweqweq", "上传的分贝模式"+deaufultShoud+"\n"+"当前的声音模式"+soundMode()+"\n"+"GPS状态" + isOPenGPS(getBaseContext()) + "\n" + "数据状态" + gprsIsOpenMethod(mCM) + "\n" + alwaysResult + "\n" + s + "\n" + "屏幕大小  " + getPingMuSize(getBaseContext()));

    }


    /**
     * 手机声音模式
     *
     * @ return 0 为正常模式
     * @ return 1  为静音模式
     * @ return 2   为震动模式
     * @ return 3   为其他模式
     * @ return  9   异常
     */

    private int soundMode() {
        if (audioManager == null) {
            audioManager = (AudioManager) getApplicationContext().getSystemService(AUDIO_SERVICE);
        }
        int mode = audioManager.getRingerMode();//静音值为0，振动值为1，响铃值为2
        int aa = 8;
        if (audioManager != null) {

            //        //减少声音音量
            //        audioManager.adjustVolume(AudioManager.ADJUST_LOWER, 0);
            ////调大声音音量
            //        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, 0);
            if (mode == 0) {
                //mode==0时是静音

                aa = 1;

//            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            } else if (mode == 1) {
                //mode==1时为震动

                aa = 2;
//            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            } else if (mode == 2) {
                //震动模式
                aa = 0;
//            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            } else {
                //其他模式
                aa = 3;
            }
        } else {
            //audiomanager为空
            aa = 9;
        }
        return aa;
    }

    /**
     * 调用封装好的获取基站信息
     */
    private void achieveBaseStation() {
        //获取基站信息
        int[] station = getBaseStation(getBaseContext());

        boolean is = NetWorkUtils.ishasSimCard(getBaseContext());

        if (is) {
            if (station != null) {
                if (station[5] == 00) {
                    operator = "移动";
                    operatorNum = "00";
                    cid = station[3];
                    lac = station[4];
                } else if (station[5] == 01) {
                    operator = "联通";
                    operatorNum = "01";
                    cid = station[3];
                    lac = station[4];
                } else if (station[5] == 02) {
                    operator = "电信";
                    operatorNum = "02";
                    cid = station[3];
                    lac = station[4];
                } else if (station[5] == 07) {
                    operator = "移动";
                    operatorNum = "07";
                    cid = station[3];
                    lac = station[4];
                } else if (station[5] == 06) {
                    operator = "联通";
                    operatorNum = "06";
                    cid = station[3];
                    lac = station[4];
                } else {
                    operatorNum = "0";
//                operator = "电信2";
//                operatorNum = "02";
//                cid = station[3];
//                lac = station[4];
                }
            }

        } else {
            Log.i("QWEQWE", "SIM卡不存在");
        }
    }

    //转换ip的方法
    private String longToIP(long longIp) {
        StringBuffer sb = new StringBuffer("");

        sb.append(String.valueOf((longIp & 0x000000FF)));
        // 将高24位置0
        sb.append(".");
        sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
        //  将高1位置0，然后右移8位
        sb.append(".");
        //将高8位置0，然后右移16位
        sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
        sb.append(".");
        // 直接右移24位
        sb.append(String.valueOf((longIp >>> 24)));
        return sb.toString();
    }

    /**
     * 广播注册
     */
    private void initBroadcast() {
        //电量广播
        receiver = new BatteryReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, filter);//注册BroadcastReceiver

        //网络状态
        IntentFilter filterz = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        myReceiver = new ConnectionChangeReceiver();
        this.registerReceiver(myReceiver, filterz);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //电量广播取消注册
        //停止获取电池电量
        unregisterReceiver(receiver);
        //停止高德的定位
        destroyLocation();

        //分贝的东西
        mHandler.removeMessages(2);


        mRecorder.delete();


        //wifi相关的
        WifiAPManager.getInstance(getApplicationContext()).unregitsterHandler();

        //停止信号的监听
        mTelephonyManager.listen(mListener, PhoneStatListener.LISTEN_NONE);

        timerTaskX.cancel();
        timerX.cancel();
        timerTaskS.cancel();
        timerS.cancel();
        timerTaskSsocket.cancel();
        timersocket.cancel();

        setSettingsOnHigh(0);

        //停止对网络状态监听
        this.unregisterReceiver(myReceiver);

    }

    /**
     * 获取手机电量广播
     */
    private class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int current = intent.getExtras().getInt("level");//获得当前电量
            int total = intent.getExtras().getInt("scale");//获得总电量
            battery = current * 100 / total;
        }
    }


    /**
     * 初始化定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void initLocation() {
        //初始化client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = getDefaultOption();
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }

    /**
     * 默认的定位参数
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(10000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }

    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {

                StringBuffer sb = new StringBuffer();
                //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
                if (location.getErrorCode() == 0) {
                    sb.append("定位成功" + "\n");
                    sb.append("定位类型: " + location.getLocationType() + "\n");
                    sb.append("经    度    : " + location.getLongitude() + "\n");
                    sb.append("纬    度    : " + location.getLatitude() + "\n");
                    sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
                    sb.append("提供者    : " + location.getProvider() + "\n");

                    sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
                    sb.append("角    度    : " + location.getBearing() + "\n");
                    // 获取当前提供定位服务的卫星个数
                    sb.append("星    数    : " + location.getSatellites() + "\n");
                    sb.append("国    家    : " + location.getCountry() + "\n");
                    sb.append("省            : " + location.getProvince() + "\n");
                    sb.append("市            : " + location.getCity() + "\n");
                    sb.append("城市编码 : " + location.getCityCode() + "\n");
                    sb.append("区            : " + location.getDistrict() + "\n");
                    sb.append("区域 码   : " + location.getAdCode() + "\n");
                    sb.append("地    址    : " + location.getAddress() + "\n");
                    sb.append("兴趣点    : " + location.getPoiName() + "\n");
                    //定位完成的时间
                    sb.append("定位时间: " + LocHeperUitls.formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");
                } else {
                    //定位失败
                    sb.append("定位失败" + "\n");
                    sb.append("错误码:" + location.getErrorCode() + "\n");
                    sb.append("错误信息:" + location.getErrorInfo() + "\n");
                    sb.append("错误描述:" + location.getLocationDetail() + "\n");
                }
                //定位之后的回调时间
                sb.append("回调时间: " + LocHeperUitls.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") + "\n");

                //解析定位结果，
                String result = sb.toString();
//                Log.i("QWEQWE", "" + result);
            } else {
                Log.i("QWEQWE", "" + "定位失败，loc is null");
            }
        }
    };

    // 根据控件的选择，重新设置定位参数
    private void resetOption() {
        // 设置是否需要显示地址信息
        locationOption.setNeedAddress(true);
        /**
         * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
         * 注意：只有在高精度模式下的单次定位有效，其他方式无效
         */
        locationOption.setGpsFirst(true);
        // 设置是否开启缓存
        locationOption.setLocationCacheEnable(true);
        // 设置是否单次定位
        locationOption.setOnceLocation(false);
        //设置是否等待设备wifi刷新，如果设置为true,会自动变为单次定位，持续定位时不要使用
//        locationOption.setOnceLocationLatest(cbOnceLastest.isChecked());
        //设置是否使用传感器
//        locationOption.setSensorEnable(cbSensorAble.isChecked());
        //设置是否开启wifi扫描，如果设置为false时同时会停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
//        String strInterval = etInterval.getText().toString();
        String strInterval = "2000";
        if (!TextUtils.isEmpty(strInterval)) {
            try {
                // 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
                locationOption.setInterval(Long.valueOf(strInterval));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

//        String strTimeout = etHttpTimeout.getText().toString();
        String strTimeout = "30000";
        if (!TextUtils.isEmpty(strTimeout)) {
            try {
                // 设置网络请求超时时间
                locationOption.setHttpTimeOut(Long.valueOf(strTimeout));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 开始定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void startLocation() {
        //根据控件的选择，重新设置定位参数
        //resetOption();
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
    }

    /**
     * 停止定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void stopLocation() {
        // 停止定位
        locationClient.stopLocation();
    }

    /**
     * 销毁定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void destroyLocation() {
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }

    private void case1() {
        if (mRecorder == null) {

            mRecorder = new MyMediaRecorder();
        }

        upgradeRootPermission(getPackageCodePath());
//                        setConfigInfoDialog();
        //获取上线分贝值
        String xx = ProperUtil.getConfigProperties("DbValues");
        //获取飞行模式重启时间间隔
        String xxx = ProperUtil.getConfigProperties("CloseFlyModeTime");
        Log.i("QWEQWE", "分贝初始化设置    " + xx + "    " + xxx);
        SharedPreferencesUtils.saveTosp(getApplication(), "MainActivity", "MINDB", minDbsconfig);
        SharedPreferencesUtils.saveTosp(getApplication(), "MainActivity", "TIMER", timersconfig);
        Toast.makeText(LaunchService.this, "修改了下", Toast.LENGTH_SHORT).show();
        String time = formatter.format(new Date());
        String fileName = time + ".amr";
        File file = FileUtil.createFile(fileName);
        if (file != null) {
            Log.v("file", "file =" + file.getAbsolutePath());
            startRecord(file);
        } else {
            Toast.makeText(getBaseContext(), "创建文件失败", Toast.LENGTH_LONG).show();
        }
    }

    private void case2() {
        String nextTime = "";
        minDb = SharedPreferencesUtils.getDataFromSp(getApplication(), "MainActivity", "MINDB");
        timer = SharedPreferencesUtils.getDataFromSp(getApplication(), "MainActivity", "TIMER");
        volume = mRecorder.getMaxAmplitude();  //获取声压值
        if (volume > 0 && volume < 1000000) {
            float soundValue = (float) (Math.log10(volume));
            int dbCount = (int) PublicUtils.setDbCount(20 * soundValue);  //将声压值转为分贝值
            Log.i(TAG, dbCount + ">>>>>>>>>>>>>>>>>>>>>>>>");
            if (dbCount > Integer.parseInt(minDb)) {
                if (AirplaneModeisOff(getApplication())) {
                    setSettingsOnHigh(1);
                    Log.e("qweqweq", "走了几遍");
//                    nextTime = GetNextWarnTime(Integer.parseInt(timer));
                    timerExcuterFeibei(Integer.parseInt(timer));
                }

            }
            if (!AirplaneModeisOff(getApplication())) {
                if (getCurrentTime().equals(nextTime)) {
                    setSettingsOnHigh(0);
                }
            }
        }
        mHandler.sendEmptyMessageDelayed(2, 100);
    }

    public void startRecord(File fFile) {
        try {
            mRecorder.setMyRecAudioFile(fFile);
            if (mRecorder.startRecorder()) {
                startListenAudio();
            } else {
                startListenAudio();
                Toast.makeText(this, "启动录音失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "录音机已被占用或录音权限被禁止", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void startListenAudio() {
        mHandler.sendEmptyMessageDelayed(2, 100);
    }

    private class PhoneStatListener extends PhoneStateListener {

        final String sas = getWifiConnected(getBaseContext());
        final TelephonyManager tm = (TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        //网络信号强度
        final int rssi = wifiInfo.getRssi();

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            String signalInfo = signalStrength.toString();
            String[] params = signalInfo.split(" ");
            if (networkType2 != null && networkType2.equals("0")) {
                signalquality = "0";
                return;
            }

            if (sas != null && sas.equals("WIFI已连接")) {
                Log.i("qweqwe", "WIFI信号强度是" + rssi);
                signalquality = String.valueOf(rssi).toString();
                return;
            }
            if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
                //4G网络 最佳范围   >-90dBm 越大越好
                int Itedbm = Integer.parseInt(params[9]);
                if (Itedbm == -1) {
                    signalquality = "-82";
                } else {
                    signalquality = "-97";
                }
//                Log.i("qweqwe", "4G网络 " + Itedbm);

            } else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA ||
                    tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA ||
                    tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA ||
                    tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS ||
                    tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_TD_SCDMA ||
                    tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_0) {
                //3G网络最佳范围  >-90dBm  越大越好  ps:中国移动3G获取不到  返回的无效dbm值是正数（85dbm）
                //在这个范围的已经确定是3G，但不同运营商的3G有不同的获取方法，故在此需做判断 判断运营商与网络类型的工具类在最下方
                String yys = getOperator(getBaseContext());//获取当前运营商

                if (yys == "中国移动") {

                    Log.i("qweqwe", "/中国移动3G不可获取，故在此返回0" + 0);
                } else if (yys == "中国联通") {

                    int cdmaDbm = signalStrength.getCdmaDbm();
                    signalquality = String.valueOf(cdmaDbm).toString();
                    Log.i("qweqwe", "中国联通" + cdmaDbm);
                } else if (yys == "中国电信") {
                    int evdoDbm = signalStrength.getEvdoDbm();
                    signalquality = String.valueOf(evdoDbm).toString();
                    Log.i("qweqwe", "中国电信" + evdoDbm);
                }

            } else {
                //2G网络最佳范围>-90dBm 越大越好
                int asu = signalStrength.getGsmSignalStrength();
                int dbm = -113 + 2 * asu;
                Log.i("qweqwe", "2G" + dbm);
                signalquality = String.valueOf(dbm).toString();
            }

        }

    }

    /**
     * socket网络交互
     *
     * @param alwaysResulta 往后台传送的字段信息
     */

    private void case3(final String alwaysResulta) {
        // 利用线程池直接开启一个线程 & 执行该线程
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socket != null) {
                        // 步骤1：从Socket 获得输出流对象OutputStream
                        // 该对象作用：发送数据
                        outputStream = socket.getOutputStream();
                        if (alwaysResulta != null) {
                            // 步骤2：写入需要发送的数据到输出流对象中
                            outputStream.write((alwaysResulta.toString() + "\n").getBytes("utf-8"));
                            // 特别注意：数据的结尾加上换行符才可让服务器端的readline()停止阻塞
                            // 步骤3：发送数据到服务端
                            outputStream.flush();
                        }
                    }
                } catch (IOException e) {
                    socketflagis = false;
                    e.printStackTrace();
                }
                //连接后启动监听socket的状态
                if (!socketflagis) {
                    socketflag = false;
                    Log.i("QWEQWEQ", "未连接socket case 4");
                    //连接断开了 ，可以在这里处理重新连接

                } else {
                    Log.i("QWEQWEQ", "已连接socket case 3");
                }


            }
        });
    }

    /**
     * 循环的读取服务器的数据  一定要将此方法
     * 写在循环之中  不然服务器断开以后就会不能自动重新连接
     */
    private void case4() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    if (socket == null) {
//                        socket = new Socket("218.246.35.198", 70001);
//                    }
                    if (socket != null) {
                        InputStream inputStream = socket.getInputStream();
                        DataInputStream input = new DataInputStream(inputStream);
                        byte[] b = new byte[10000];
                        while (true) {
                            int length = input.read(b);
                            acceptinfo = new String(b, 0, length, "gb2312").trim();
                            Log.d("qweqweq", acceptinfo);

                            mHandler.sendEmptyMessage(55);

                        }
                    }
                } catch (Exception ex) {
                    socketflagis = false;
                    ex.printStackTrace();
                }
                //连接后启动监听socket的状态
                if (!socketflagis) {
                    socketflag = false;
//                    erreySocketNum++;
                    Log.i("QWEQWEQ", "未连接socket case 4");

//                    if (ping() == false) {
//                        //连接断开了 ，可以在这里处理重新连接
//                        if (isWifiAvailable(getBaseContext())) {
//                            //关闭的时候是没有网络的  所以是
//                            mWifiManager.setWifiEnabled(false);
//                        }
//                    }

                } else {
                    Log.i("QWEQWEQ", "已连接socket case4");
                }

            }
        }).start();
    }

    /**
     * 接受服务器来的数据处理相关逻辑
     */
    private void case5() {

        if (acceptinfo == null || ("").equals(acceptinfo) || acceptinfo.contains(" ") || acceptinfo.length() < 21) {
            case7();
            Log.i("QWEQWE", "服务器发送的数据为空");
            return;
        }
        //截取收到指定是的imei
        String cutimei = acceptinfo.substring(5, 20);
        //将获取到的字符串截取掉转义字符
//        acceptinfo = acceptinfo.substring(0, acceptinfo.length() - 4);

        statusclassify = acceptinfo.substring(2, 5);
        //分贝设置相关
        String splitresulta = new StringBuffer(acceptinfo.substring(0, 5)).append(acceptinfo.substring(acceptinfo.length() - 1, acceptinfo.length())).toString().trim();
        //移动数据相关
        String splitresulta2 = new StringBuffer(acceptinfo.substring(0, 4)).append(acceptinfo.substring(acceptinfo.length())).toString().trim();
        Log.i("qweqwer", "+分贝相关+" + splitresulta + "标识相关" + statusclassify + "移动数据相关" + splitresulta2 + "截取后的字符串" + cutimei);
        if (imeilocality.equals("no")) {
            Log.i("qweqweq", "本地序列号获取失败");
        } else {
            //静音模式设置
            if (acceptinfo != null && acceptinfo.equals(VOLUME_SILENCE + imeilocality + "#")) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                Log.i("qweqweq", "静音模式设置");
                case6();
            }
            //震动模式设置
            else if (acceptinfo != null && acceptinfo.equals(VOLUME_VIBRATE + imeilocality + "#")) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                Log.i("qweqweq", "震动模式设置");
                Toast.makeText(this, "震动模式设置", Toast.LENGTH_SHORT).show();
                case6();
            }
            //设备重启
            else if (acceptinfo != null && (splitresulta.equals("##270#")) && cutimei.equals(imeilocality) && acceptinfo.length() == 21) {
                if (isRoot()) {
                    try {
                        case6();
                        Runtime.getRuntime().exec("su -c reboot");
                    } catch (IOException e) {
                        case7();
                        e.printStackTrace();
                    }

                } else {
                    case7();
                }
            }

            //设置设备上传设备信息的时间
            else if (acceptinfo != null && (splitresulta.equals("##280#")) && cutimei.equals(imeilocality) && acceptinfo.length() > 22 && acceptinfo.length() < 26 && acceptinfo.contains(",")) {
                String str[] = acceptinfo.split(",");
                if (str.length == 2) {
                    //拆分字符串后，不截取最后一位
                    String str2 = str[1].substring(0, str[1].length() - 1).toString().trim();
                    if (isNum(str2)) {
                        ProperUtil.writeDateToLocalFile("IntervalTime", str2);
                        timerintervalconfig = Integer.parseInt(str2);
                        case6();
                        Log.i("QWEQWEQ", "后台设备发送的时间是" + str2);
                    } else {
                        case7();
                    }
                } else {
                    case7();
                }
            }

            //分贝设置
            else if (acceptinfo != null && splitresulta.length() == 6 && acceptinfo.contains(",") && splitresulta.equals("##230#")) {
                if (acceptinfo.length() < 30 && acceptinfo.length() > 25) {
                    String[] splitresult = acceptinfo.split(",");
                    if (splitresult != null && splitresult.length == 3 && splitresult[0].length() == 20) {
                        String aa = splitresult[1];
                        String bb = splitresult[2].substring(0, splitresult[2].length() - 1);
                        //截取之后的分贝值与分贝时长
                        if (isNum(aa) && isNum(bb)) {
                            //指令正确后发送数据

                            mHandler.sendEmptyMessage(1);
                            startListenAudio();
                            Message mm = new Message();
                            mm.what = 10;
                            Bundle bundle = new Bundle();
                            bundle.putString("a", aa);
                            bundle.putString("b", bb);
                            mm.setData(bundle);
                            mHandler.sendMessageDelayed(mm, 2000);
                            case6();
                            Log.i("qweqweq", "===============分贝设置" + aa + bb);
                        } else {
                            case7();
                        }
                    } else {
                        Log.i("qweqweq", "分贝split请检查字段是佛正确");
                        case7();
                    }
                } else {
                    case7();
                    Log.i("qweqweq", "分贝length请检查字段是佛正确");
                }
            }
            //取消分贝设置
            else if (acceptinfo != null && splitresulta.equals("##231#") && cutimei.equals(imeilocality) && acceptinfo.length() == 21) {
//                mHandler.sendEmptyMessage(1010);


                mRecorder.delete();
                mHandler.removeMessages(2);
                case6();
                Log.i("QWEQWEq", "取消分贝监听");
            } else if (acceptinfo != null && splitresulta.equals("##232#") && cutimei.equals(imeilocality) && acceptinfo.length() == 21) {
                mHandler.sendEmptyMessage(1);
                case6();
            }

            //开启数据
            else if (acceptinfo != null && (splitresulta.equals("##240#")) && cutimei.equals(imeilocality) && acceptinfo.length() == 21) {

                GPRSUtil.setGprsOn_Off(true);
                Log.i("QWEQWEq", "移动数据开启");

            }
            //开启飞行模式
            else if (acceptinfo != null && splitresulta.equals("##211#") && cutimei.equals(imeilocality) && acceptinfo.length() == 21) {
                setSettingsOnHigh(0);
                Log.i("qweqweq", "飞行模式关闭成功");


                case6();

            }

            //关闭热点
            else if (acceptinfo != null && splitresulta.equals("##251#") && cutimei.equals(imeilocality) && acceptinfo.length() == 21) {
                Log.i("QWEQWEQ", "关闭热点");
                wifiAPManager.closeWifiAp();
                case6();
            }
            ///关闭wifi
            else if (acceptinfo != null && splitresulta.equals("##261#") && cutimei.equals(imeilocality) && acceptinfo.length() == 21) {
                mWifiManager.setWifiEnabled(false);
                Log.i("QWEQWEQ", "关闭wifi");
                case6();
            }
            //开启wifi判断屏幕的物理尺寸的大小
            else if (acceptinfo != null && splitresulta.equals("##262#") && cutimei.equals(imeilocality) && acceptinfo.length() == 21) {
                if (!mWifiManager.isWifiEnabled()) {
                    if (getPingMuSize(getBaseContext()) > 3.0) {
                        setSettingsWifi();
                    } else {
                        mWifiManager.setWifiEnabled(true);
                    }
                    case6();
                } else {
                    case7();
                }
                Log.i("QWEQWEQ", "开启wifi");
            }
            //开启飞行模式
            else if (acceptinfo != null && acceptinfo.length() > 22 && acceptinfo.length() < 26 && acceptinfo.contains(",") && splitresulta.length() == 6 && splitresulta.equals("##210#") && cutimei.equals(imeilocality)) {
                String[] splitresult = acceptinfo.split(",");

                if (splitresult == null || splitresult.length != 2) {
                    case7();
                    return;
                }

                String aa = splitresult[0];
                String bb = splitresult[1].substring(0, splitresult[1].length() - 1);
                if (aa.equals("") || bb.equals("") || bb.length() > 3 || aa.length() != 20) {
                    case7();
                    Log.i("QWEQWEQ", " **** 失败" + aa + " **** " + bb);
                    return;

                }
                if (isNum(bb)) {
                    case6();
                    setSettingsOnHigh(1);
                    //多长时间后自动关闭
                    timerExcuter(Integer.valueOf(bb));
                    Log.i("QWEQWEQ", " 飞行模式开启成功  !!!!  " + aa + " !!!!!  " + bb);
                } else {
                    case7();
                }

            }
            //移动数据关闭
            else if (acceptinfo != null && acceptinfo.length() > 22 && acceptinfo.length() < 26 && acceptinfo.contains(",") && splitresulta.length() == 6 && splitresulta.equals("##241#") && cutimei.equals(imeilocality)) {
                String[] splitresultx = acceptinfo.split(",");

                if (splitresultx == null || splitresultx.length != 2) {
                    case7();
                    return;
                }

                String aa = splitresultx[0];
                String bb = splitresultx[1].substring(0, splitresultx[1].length() - 1);
                if (aa.equals("") || bb.equals("") || bb.length() > 3 || aa.length() != 20) {
                    case7();
                    Log.i("QWEQWEQ", " **** 失败" + aa + " **** " + bb);
                    return;
                    //将其他字母提取粗来
                }
                if (isNum(bb)) {
                    case6();
                    //多长时间后自动关闭
                    timerExcuterMob(Integer.valueOf(bb));
                    GPRSUtil.setGprsOn_Off(false);

                } else {
                    case7();
                }

            }
            //开启热点
            else if (acceptinfo != null && acceptinfo.contains(",") && !acceptinfo.contains(" ") && splitresulta.length() == 6 && splitresulta.equals("##250#") && cutimei.equals(imeilocality)) {

                if (acceptinfo != null && acceptinfo.length() > 31 && acceptinfo.length() < 40) {
                    String[] splitresult = acceptinfo.split(",");
                    if (splitresult != null && splitresult.length == 3 && splitresult[0].length() == 20) {
                        String aa = splitresult[1];
                        String bb = splitresult[2].substring(0, splitresult[2].length() - 1);

                        if (aa != null && aa.length() > 0 && bb != null && bb.length() >= 8) {
                            //截取之后的热点名字与密码
                            wifiAPManager.turnOnWifiAp(aa, bb, WifiAPManager.WifiSecurityType.WIFICIPHER_WPA2);
                            Log.i("qweqweq", "===============开启热点" + aa + bb);
                            case6();
                        } else {
                            case7();
                        }

                    } else {
                        Log.i("qweqweq", "热点split请检查字段是佛正确");
                        case7();
                    }
                } else {
                    case7();
                    Log.i("qweqweq", "热点length请检查字段是佛正确");
                }
            }
            //连接指定wifi
            else if (acceptinfo != null && acceptinfo.contains(",") && splitresulta.length() == 6 && splitresulta.equals("##260#") && cutimei.equals(imeilocality)) {

                if (acceptinfo != null && acceptinfo.length() > 32) {
                    String[] splitresult = acceptinfo.split(",");
                    if (splitresult != null && splitresult.length == 4 && splitresult[0].length() == 20 && splitresult[3].length() == 2) {

                        String aa = splitresult[1];
                        String bb = splitresult[2];
                        String zz = getWifiType(Integer.valueOf(splitresult[3].substring(0, 1)));
                        //为zzz的时候是网络类型不对，为空或者密码长度小于8，或者wifi在当前存在
                        if (zz.equals("zzz") || aa.equals("") || bb.equals("") || bb.length() < 8) {
                            case7();
                            return;
                        }


                        //如果wifi没有打开  先打开
                        if (!mWifiManager.isWifiEnabled()) {
                            if (getPingMuSize(getBaseContext()) > 3.0) {
                                setSettingsWifi();
                            } else {
                                mWifiManager.setWifiEnabled(true);
                            }
                        }


                        startWifi(aa, bb, zz);
                        //截取之后的热点名字与密码

                        Log.i("qweqweq", "===============开启wifi " + aa + bb + zz);
                        case6();

                    } else {
                        Log.i("qweqweq", "wifisplit请检查字段是佛正确");
                        case7();
                    }
                } else {
                    case7();
                    Log.i("qweqweq", "wifilength请检查字段是佛正确");
                }
            } else {
                case7();

            }


        }
    }

    /**
     * 暂定为往服务器发送数据
     */
    private void case6() {
        mHandler.sendEmptyMessage(6);
    }

    /**
     * 暂定为往服务器发送数据
     */
    private void case7() {
        mHandler.sendEmptyMessage(7);
    }

    /**
     *
     */
    private void case8() {
        mHandler.sendEmptyMessage(8);
    }

    /**
     * 初始化socket连接
     */

    private void socketConneted() {
        // 初始化线程池
        mThreadPool = Executors.newCachedThreadPool();
        // 利用线程池直接开启一个线程 & 执行该线程


        // 利用线程池直接开启一个线程 & 执行该线程
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    // 创建Socket对象 & 指定服务端的IP 及 端口号
                    String ip = Constantx.cofigIp;
                    String pory = Constantx.cofigPort;
                    if (ip == null || pory == null) {
                        socket = new Socket("192.168.1.7", 7000);
                    }
                    socket = new Socket(Constantx.cofigIp, Integer.parseInt(Constantx.cofigPort));
//                    socket = new Socket("192.168.1.8", 7000);
                    //连接断开时设为flase 重新调用此方法
                    socketflagis = true;
                    // 判断客户端和服务器是否连接成功

                    Constantx.socketis = false;
                    Log.i("QWEQWEQ", " 连接成功  初始化" + socket.isConnected());
                } catch (IOException e) {
                    socketflagis = false;
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(44);

                    Constantx.socketis = true;
                    Log.i("QWEQWEQ", "连接异常  初始化 " + erreySocketNum);
                }
            }
        });


    }

    /**
     * 转换网络类型
     */
    private String getWifiType(int a) {
        String xx = "";
        if (a == 0) {
            xx = "WIFICIPHER_WPA";
        } else if (a == 1) {
            xx = "WIFICIPHER_WEP";
        } else if (a == 2) {
            xx = "WIFICIPHER_NOPASS";
        } else if (a == 3) {
            xx = "WIFICIPHER_INVALID";
        } else {
            xx = "zzz";
        }
        return xx;
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

    /**
     * 开启wifi  判断可用wifi列表中是是否有可用的wifi信息
     */

    private void startWifi2(String ssid, String pswd, String typeS) {
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

    /**
     * 监听网络状态广播
     */
    public class ConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
                Log.i("qweqweq", "网络不可用");
                //如果当前连接的是wifi而且网络不可用
                socketflag = false;
                socketflagis = false;  //如果这个不设置就会循环检测socket是否正常就会不正常,只针对在连接途中网络中断


            } else {
                socketflag = true;
                //如果这个不设置就会循环检测socket是否正常就会不正常，只针对在连接途中网络中断
                //初始化全局变量 默认为tuew  没有网络socket断开 设为flase  有网络下次网络可用调用case4
                String data = SharedPreferencesUtils.getToPhoneData(getBaseContext());
                if (data != null && !data.equals("no")) {
                    alwaysResult = data;
                    Log.i("qweqwe", "获取本地数据成功");
                } else {
                    Log.i("qweqwe", "获取本地数据失败");
                }
            }
        }
    }

    /**
     * 关闭socket   在有网络的情况下才可以正常关闭整条tcp连接
     */

    private void socketGunbi() {
        try {
//                     断开 客户端发送到服务器 的连接，即关闭输出流对象OutputStream
            outputStream.close();
//
//                     断开 服务器发送到客户端 的连接，即关闭输入流读取器对象BufferedReader
            br.close();
//
//                     最终关闭整个Socket连接
            socket.close();
//
//                     判断客户端和服务器是否已经断开连接
            System.out.println(socket.isConnected());
//
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 普通飞行开启模式之后的间隔
     */

    private void timerExcuter(int aa) {
        aa = aa * 60000;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Log.i("qweqweq", "飞行自动关闭执行了没有");
                setSettingsOnHigh(0);
                //延迟五秒后发送指令
                mHandler.sendEmptyMessageDelayed(4561, 5000);
            }
        }, aa);
    }

    private void timerExcuterMob(int aa) {
        aa = aa * 60000;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Log.i("qweqweq", "移动数据自动开启执行了没有");
                GPRSUtil.setGprsOn_Off(true);
                mHandler.sendEmptyMessageDelayed(4562, 5000);
            }
        }, aa);
    }

    /**
     * 分贝之后设置的自动关闭飞行模式的时间间隔
     */
    private void timerExcuterFeibei(int aa) {
        aa = aa * 60000;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Log.i("qweqweq", "飞行自动关闭执行了没有分贝设置");
                setSettingsOnHigh(0);
            }
        }, aa);
    }


    /**
     * 判断是不是纯字符串
     */
    public static boolean isNum(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断wifi连接状态
     *
     * @param ctx
     * @return 1是开启中
     * 2是关闭
     */
    public boolean isWifiAvailable(Context ctx) {
        ConnectivityManager conMan = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState();
        if (NetworkInfo.State.CONNECTED == wifi) {
            return true;
        } else {
            return false;
        }
    }

    public void removeWifi_1() {

        List<WifiConfiguration> conlist = mWifiManager.getConfiguredNetworks();//获取保存的配置信息
        for (int i = 0; i < conlist.size(); i++) {
            Log.e("qweqweq", "i = " + String.valueOf(i) + "SSID = " + conlist.get(i).SSID + " netId = " + String.valueOf(conlist.get(i).networkId));
            //忘记所有wifi密码
            //mWifiManager.forget(conlist.get(i).networkId, null);
            //忘记当前wifi密码
            if (i == 0) {
                mWifiManager.removeNetwork(conlist.get(i).networkId);

            }
        }
    }

}
