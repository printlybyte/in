package com.watch.in.constant;

import com.watch.in.uitlis.data.SharedPreferencesUtils;

/**
 * Created by Administrator on 2017/6/21.
 */

public class Constantx {
    public static boolean isStartService = true;
    public static final String VOLUME_SILENCE = "##220";
    public static final String VOLUME_VIBRATE = "##221";
    public static boolean socketflag = true;
    //判断socket的连接状态;
    public static boolean socketflagis = true;
    public static boolean socketis = true;


    //判断分贝检测有没有成功成功运行  默认是false
    public  static  boolean   isMediaStart=false;
    /**
     * 配置文件
     */

    //必须
    //ip
    public static String cofigIp;
    //端口号
     public static String cofigPort;
    //上传时间手机信息时间间隔
     public static String cofigIntervalTime;
    //分贝上限值
     public static String cofigDbValues;
    //关闭飞行模式的时间
     public static String cofigCloseFlyModeTime;
    //声音模式 SoundMode   SLIENCE
     public static String cofigSoundMode;

    //非必须
    //wifi名字``````
     public static String cofigWifiName;
    //wifi密码
     public static String configWifiPwd;
    //网络状态
     public static String cofigEncryptionType;

    //热点名字
     public static String cofigWifiHotName;
    //热点密码
     public static String cofigWifiHotPwd;
    //默认开启wifi还是热点
    public static String  cofigWifiModle;

    // 移动数据关闭后的默认开启时间
    public static  String moblieOnTimer;

    public static  final  String startwifi="svc wifi enable";
    public static  final  String stopwifi="svc wifi disable";
}
