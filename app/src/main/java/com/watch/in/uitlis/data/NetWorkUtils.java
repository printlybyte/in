package com.watch.in.uitlis.data;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

import static com.watch.in.Api.BaseApplication.TAG;

/**
 * 网络工具类
 * Created by alic on 16-4-8.   liuguodong
 */
public class NetWorkUtils {

    private static final int NETWORK_TYPE_UNAVAILABLE = -1;
    // private static final int NETWORK_TYPE_MOBILE = -100;
    private static final int NETWORK_TYPE_WIFI = -101;

    private static final int NETWORK_CLASS_WIFI = -101;
    private static final int NETWORK_CLASS_UNAVAILABLE = -1;
    /**
     * Unknown network class.
     */
    private static final int NETWORK_CLASS_UNKNOWN = 0;
    /**
     * Class of broadly defined "2G" networks.
     */
    private static final int NETWORK_CLASS_2_G = 1;
    /**
     * Class of broadly defined "3G" networks.
     */
    private static final int NETWORK_CLASS_3_G = 2;
    /**
     * Class of broadly defined "4G" networks.
     */
    private static final int NETWORK_CLASS_4_G = 3;

    private static DecimalFormat df = new DecimalFormat("#.##");

    // 适配低版本手机
    /**
     * Network type is unknown
     */
    public static final int NETWORK_TYPE_UNKNOWN = 0;
    /**
     * Current network is GPRS
     */
    public static final int NETWORK_TYPE_GPRS = 1;
    /**
     * Current network is EDGE
     */
    public static final int NETWORK_TYPE_EDGE = 2;
    /**
     * Current network is UMTS
     */
    public static final int NETWORK_TYPE_UMTS = 3;
    /**
     * Current network is CDMA: Either IS95A or IS95B
     */
    public static final int NETWORK_TYPE_CDMA = 4;
    /**
     * Current network is EVDO revision 0
     */
    public static final int NETWORK_TYPE_EVDO_0 = 5;
    /**
     * Current network is EVDO revision A
     */
    public static final int NETWORK_TYPE_EVDO_A = 6;
    /**
     * Current network is 1xRTT
     */
    public static final int NETWORK_TYPE_1xRTT = 7;
    /**
     * Current network is HSDPA
     */
    public static final int NETWORK_TYPE_HSDPA = 8;
    /**
     * Current network is HSUPA
     */
    public static final int NETWORK_TYPE_HSUPA = 9;
    /**
     * Current network is HSPA
     */
    public static final int NETWORK_TYPE_HSPA = 10;
    /**
     * Current network is iDen
     */
    public static final int NETWORK_TYPE_IDEN = 11;
    /**
     * Current network is EVDO revision B
     */
    public static final int NETWORK_TYPE_EVDO_B = 12;
    /**
     * Current network is LTE
     */
    public static final int NETWORK_TYPE_LTE = 13;
    /**
     * Current network is eHRPD
     */
    public static final int NETWORK_TYPE_EHRPD = 14;
    /**
     * Current network is HSPA+
     */
    public static final int NETWORK_TYPE_HSPAP = 15;

    /**
     * @wifiStatus="WIFI已连接" 时，moblie不能用
     * @wifiStatus="移动数据已连接" 时，moblie可用
     * @wifiStatus="移动数据已断开" 时，wifi，moblie都不可用
     */
    public static String getWifiConnected(Context context) {
        String wifiStatus = "";
        //获得ConnectivityManager对象
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //获取ConnectivityManager对象对应的NetworkInfo对象
        //获取WIFI连接的信息
        NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        //获取移动数据连接的信息
        NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiNetworkInfo.isConnected()) {
            wifiStatus = "WIFI已连接";
        } else {
            if (dataNetworkInfo.isConnected()) {
                wifiStatus = "移动数据已连接";
            } else {
                wifiStatus = "移动数据已断开";
            }
        }
        return wifiStatus;

    }
//            if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
//                Toast.makeText(context, "WIFI已连接,移动数据已连接", Toast.LENGTH_SHORT).show();
//            } else if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
//                Toast.makeText(context, "WIFI已连接,移动数据已断开", Toast.LENGTH_SHORT).show();
//            } else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
//                Toast.makeText(context, "WIFI已断开,移动数据已连接", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(context, "WIFI已断开,移动数据已断开", Toast.LENGTH_SHORT).show();
//            }


    public static String getNetWorkSwitchNum(String type) {
        if (!type.isEmpty() && type.equals("网络连接断开")) {
            type = "0";
        } else if (!type.isEmpty() && type.equals("中国联通2g")) {
            type = "5";
        } else if (!type.isEmpty() && type.equals("中国联通3g")) {
            type = "6";
        } else if (!type.isEmpty() && type.equals("中国联通4g")) {
            type = "7";
        } else if (!type.isEmpty() && type.equals("中国移动2g")) {
            type = "2";
        } else if (!type.isEmpty() && type.equals("中国移动3g")) {
            type = "3";
        } else if (!type.isEmpty() && type.equals("中国移动4g")) {
            type = "4";
        } else if (!type.isEmpty() && type.equals("WIFI已连接")) {
            type = "1";
        } else {
            type = "8";
        }
        return type;
    }

    /**
     * 获取当前网络连接的类型信息如  GPRS UDGP...
     *
     * @param context
     * @return
     */
    public static String getSubtypeName(Context context) {
        if (context != null) {
            //获取手机所有连接管理对象
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            //获取NetworkInfo对象
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                //返回NetworkInfo的类型
                return networkInfo.getSubtypeName();
            }
        }
        return null;
    }


    /**
     * 获取当前的运营商
     *
     * @param context
     * @return 运营商名字
     */
    public static String getOperator(Context context) {


        String ProvidersName = "";
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String IMSI = telephonyManager.getSubscriberId();
        if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
            ProvidersName = "中国移动";
        } else if (IMSI.startsWith("46001")) {
            ProvidersName = "中国联通";
        } else if (IMSI.startsWith("46003")) {
            ProvidersName = "中国电信";
        }
        return ProvidersName;
    }

    /**
     * 得到当前的手机蜂窝网络信号强度
     * 获取LTE网络和3G/2G网络的信号强度的方式有一点不同，
     * LTE网络强度是通过解析字符串获取的，
     * 3G/2G网络信号强度是通过API接口函数完成的。
     * asu 与 dbm 之间的换算关系是 dbm=-113 + 2*asu
     */
    public static String getCurrentNetDBM(final Context context) {
        final String xx [] = new String[0];
        final String sas = getWifiConnected(context);
        final TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        //网络信号强度
        final int rssi = wifiInfo.getRssi();
        PhoneStateListener mylistener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                String signalInfo = signalStrength.toString();
                String[] params = signalInfo.split(" ");

                if (sas!=null&&sas.equals("WIFI已连接")) {
                    Log.i("qweqwe", "WIFI信号强度是" + rssi);
                    xx[0]= String.valueOf(rssi).toString();
                    return;
                }
                if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
                    //4G网络 最佳范围   >-90dBm 越大越好
                    int Itedbm = Integer.parseInt(params[9]);
                    Log.i("qweqwe", "4G网络 " + Itedbm);

                } else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_TD_SCDMA||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_0) {
                    //3G网络最佳范围  >-90dBm  越大越好  ps:中国移动3G获取不到  返回的无效dbm值是正数（85dbm）
                    //在这个范围的已经确定是3G，但不同运营商的3G有不同的获取方法，故在此需做判断 判断运营商与网络类型的工具类在最下方
                    String yys = getOperator(context);//获取当前运营商
                    if (yys == "中国移动") {

                        Log.i("qweqwe", "/中国移动3G不可获取，故在此返回0" + 0);
                    } else if (yys == "中国联通") {

                        int cdmaDbm = signalStrength.getCdmaDbm();
                        xx[0]=String.valueOf(cdmaDbm).toString();
                        Log.i("qweqwe", "中国联通" + cdmaDbm);
                    } else if (yys == "中国电信") {
                        int evdoDbm = signalStrength.getEvdoDbm();
                        xx[0]=String.valueOf(evdoDbm).toString();
                        Log.i("qweqwe", "中国电信" + evdoDbm);
                    }

                } else {
                    //2G网络最佳范围>-90dBm 越大越好
                    int asu = signalStrength.getGsmSignalStrength();
                    int dbm = -113 + 2 * asu;
                    Log.i("qweqwe", "2G" + dbm);
                    xx[0]=String.valueOf(dbm).toString();
                }

            }
        };
        //开始监听
        tm.listen(mylistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        return xx[0].toString();
    }

    /**
    *判断当前的网络连接状态是否能用
    *
    */
    public static final boolean ping() {

        String result = null;
        try {
            String ip = "www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次
            // 读取ping的内容，可以不加
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            Log.d("------ping-----", "result content : " + stringBuffer.toString());
            // ping的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "success";
                return true;
            } else {
                result = "failed";
            }
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } finally {
            Log.d("----result---", "result = " + result);
        }
        return false;

    }
    /**
     * 判断是否包含SIM卡
     *
     * @return 状态
     */
    public static boolean ishasSimCard(Context context) {
        TelephonyManager telMgr = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        boolean result = true;
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                result = false; // 没有SIM卡
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                result = false;
                break;
        }
        Log.d(TAG, result ? "有SIM卡" : "无SIM卡");
        return result;
    }

    /**
     * @ifNetworkStatusYesNo 判断当前的网络状态
     * @wifiStatus="WIFI已连接" 时，moblie不能用
     * @wifiStatus="移动数据已连接" 时，moblie可用
     * @wifiStatus="移动数据已断开" 时，wifi，moblie都不可用
     */
    public static String getNetworkStatusYesNo(Context context) {
        String networkstatus = "";
        String nameStatus = getWifiConnected(context);
        if (nameStatus.equals("WIFI已连接")) {
            networkstatus = "WIFI已连接";
//            Toast.makeText(context, "WIFI已连接", Toast.LENGTH_SHORT).show();
        } else if (nameStatus.equals("移动数据已连接")) {
            networkstatus = getNetworkStatusSys(context);
//            Toast.makeText(context, "" + getNetworkStatusSys(context), Toast.LENGTH_SHORT).show();
        } else if (nameStatus.equals("移动数据已断开")) {
            networkstatus = "网络连接断开";
//            Toast.makeText(context, "网络连接断开", Toast.LENGTH_SHORT).show();
        } else {
            networkstatus = "网络连接异常";
//            Toast.makeText(context, "异常", Toast.LENGTH_SHORT).show();
        }
        return networkstatus;
    }

    /**
     * 判断当前的cid lac sid
     *
     * @ parms  PHONE_TYPE_CDMA 电信
     * @ parms  PHONE_TYPE_GSM   移动联通
     * @ info[6 ]  国家
     * @ info[5]  三个值 00 移动；  01 联通；  02 电信
     * @ info [3,4] 移动或者联通
     */
    public static int[] getBaseStation(Context context) {
        int[] info = new int[7];
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String operator = telManager.getNetworkOperator();
        if (!operator.isEmpty()) {
            String mcc = operator.substring(0, 3); //国家
            String mnc = operator.substring(3);// 运营商

            info[5] = Integer.valueOf(mnc);

            info[6] = Integer.parseInt(mcc);


            //以下是电信的卡
            if (telManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
                CdmaCellLocation cdmaCellLocation = (CdmaCellLocation)
                        telManager.getCellLocation();
                info[0] = cdmaCellLocation.getBaseStationId(); //获取cdma基站识别标号 BID
                info[1] = cdmaCellLocation.getNetworkId(); //获取cdma网络编号NID
                info[2] = cdmaCellLocation.getSystemId(); //用谷
                // 歌API的话cdma网络的mnc要用这个getSystemId()取得→SID
            } else {
                GsmCellLocation gsmCellLocation = (GsmCellLocation) telManager.getCellLocation();
                info[3] = gsmCellLocation.getCid(); //获取gsm基站识别标号
                info[4] = gsmCellLocation.getLac(); //获取gsm网络编号
            }
        } else {
            Log.i("qweqwe","请检查是否有sim卡");
//            Toast.makeText(context, "请检查是否有sim卡", Toast.LENGTH_SHORT).show();
        }
        return info;
    }

    /**
     * 判断当前的网络状态制式
     */
    public static String getNetworkStatusSys(Context context) {
        String imsi = getOperator(context);
        String name = NetWorkUtils.getSubtypeName(context);
        String xx = "";
        if ("".equals(imsi) && "".equals(name)) {
            Toast.makeText(context, "获取数据失败", Toast.LENGTH_SHORT).show();

        } else {
            if (name.equals("GPRS")) {

                xx = imsi + "2g";
            } else if (name.equals("TD-SCDMA")) {
                xx = imsi + "3g";
            } else if (name.equals("EDGE")) {
                xx = imsi + "2g";
            } else if (name.equals("HSPA+")) {
                //            xx = "联通2.8g";
                xx = imsi + "3g";
            } else if (name.equals("HSPA")) {
                xx = imsi + "3g";
                //            xx = "联通2.5g";
            } else if (name.equals("CDMA")) {
                xx = imsi + "2g";
            } else if (name.equals("UMTS")) {
                xx = imsi + "3g";
            } else if (name.equals("HSDPA")) {
                xx = imsi + "3g";
            } else if (name.equals("EVDO")) {
                xx = imsi + "3g";
            } else if (name.equals("LTE")) {
                xx = imsi + "4g";
            } else {
                xx = "未知网络";
            }
        }
        return xx;
    }


    /**
     * 判断是否有网络连接
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            // 获取手机所有连接管理对象(包括对wi-fi,net等连接的管理)
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            // 获取NetworkInfo对象
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            //判断NetworkInfo对象是否为空
            if (networkInfo != null)
                return networkInfo.isAvailable();
        }
        return false;
    }



    /**
     * 判断WIFI网络是否可用
     *
     * @param context
     * @param context
     * @return
     */
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            //获取手机所有连接管理对象(包括对wi-fi,net等连接的管理)
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            //获取NetworkInfo对象
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            //判断NetworkInfo对象是否为空 并且类型是否为MOBILE
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return networkInfo.isAvailable();
            }

        }
        return false;
    }

    /**
     * 获取当前网络连接的类型信息
     * 原生
     *
     * @param context
     * @return
     */
    public static int getConnectedType(Context context) {
        if (context != null) {
            //获取手机所有连接管理对象
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            //获取NetworkInfo对象
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                //返回NetworkInfo的类型
                return networkInfo.getType();
            }
        }
        return -1;
    }

    /**
     * 判断GPS是否打开
     * ACCESS_FINE_LOCATION权限
     *
     * @param context
     * @return
     */
    public static boolean isGPSEnabled(Context context) {
        //获取手机所有连接LOCATION_SERVICE对象
        LocationManager locationManager = ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * 获取当前的网络状态 ：没有网络-0：WIFI网络1：4G网络-4：3G网络-3：2G网络-2
     * 自定义
     *
     * @param context
     * @return
     */
    public static int getAPNType(Context context) {
        //结果返回值
        int netType = 0;
        //获取手机所有连接管理对象
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //获取NetworkInfo对象
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        //NetworkInfo对象为空 则代表没有网络
        if (networkInfo == null) {
            return netType;
        }
        //否则 NetworkInfo对象不为空 则获取该networkInfo的类型
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI) {
            //WIFI
            netType = 1;
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            int nSubType = networkInfo.getSubtype();
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //3G   联通的3G为UMTS或HSDPA 电信的3G为EVDO
            if (nSubType == TelephonyManager.NETWORK_TYPE_LTE
                    && !telephonyManager.isNetworkRoaming()) {
                netType = 4;
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS
                    || nSubType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || nSubType == TelephonyManager.NETWORK_TYPE_EVDO_0
                    && !telephonyManager.isNetworkRoaming()) {
                netType = 3;
                //2G 移动和联通的2G为GPRS或EGDE，电信的2G为CDMA
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_GPRS
                    || nSubType == TelephonyManager.NETWORK_TYPE_EDGE
                    || nSubType == TelephonyManager.NETWORK_TYPE_CDMA
                    && !telephonyManager.isNetworkRoaming()) {
                netType = 2;
            } else {
                netType = 2;
            }
        }
        return netType;
    }
//    /**
//     * 获取网络类型
//     *
//     * @return
//     */
//    public static String getCurrentNetworkType(Context  context) {
//        int networkClass = getNetworkClass(context);
//        String type = "未知";
//        switch (networkClass) {
//            case NETWORK_CLASS_UNAVAILABLE:
//                type = "无";
//                break;
//            case NETWORK_CLASS_WIFI:
//                type = "Wi-Fi";
//                break;
//            case NETWORK_CLASS_2_G:
//                type = "2G";
//                break;
//            case NETWORK_CLASS_3_G:
//                type = "3G";
//                break;
//            case NETWORK_CLASS_4_G:
//                type = "4G";
//                break;
//            case NETWORK_CLASS_UNKNOWN:
//                type = "未知";
//                break;
//        }
//        return type;
//    }
//    private static int getNetworkClass(Context context) {
//
//
//        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        //获取NetworkInfo对象
//        NetworkInfo network = manager.getActiveNetworkInfo();
//        int networkType = NETWORK_TYPE_UNKNOWN;
//        try {
//
//            if (network != null && network.isAvailable()
//                    && network.isConnected()) {
//                int type = network.getType();
//                if (type == ConnectivityManager.TYPE_WIFI) {
//                    networkType = NETWORK_TYPE_WIFI;
//                } else if (type == ConnectivityManager.TYPE_MOBILE) {
//                    TelephonyManager telephonyManager = (TelephonyManager) context
//                            .getSystemService(
//                                    Context.TELEPHONY_SERVICE);
//                    networkType = telephonyManager.getNetworkType();
//                }
//            } else {
//                networkType = NETWORK_TYPE_UNAVAILABLE;
//            }
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return getNetworkClassByType(networkType);
//
//    }  private static int getNetworkClassByType(int networkType) {
//        switch (networkType) {
//            case NETWORK_TYPE_UNAVAILABLE:
//                return NETWORK_CLASS_UNAVAILABLE;
//            case NETWORK_TYPE_WIFI:
//                return NETWORK_CLASS_WIFI;
//            case NETWORK_TYPE_GPRS:
//            case NETWORK_TYPE_EDGE:
//            case NETWORK_TYPE_CDMA:
//            case NETWORK_TYPE_1xRTT:
//            case NETWORK_TYPE_IDEN:
//                return NETWORK_CLASS_2_G;
//            case NETWORK_TYPE_UMTS:
//            case NETWORK_TYPE_EVDO_0:
//            case NETWORK_TYPE_EVDO_A:
//            case NETWORK_TYPE_HSDPA:
//            case NETWORK_TYPE_HSUPA:
//            case NETWORK_TYPE_HSPA:
//            case NETWORK_TYPE_EVDO_B:
//            case NETWORK_TYPE_EHRPD:
//            case NETWORK_TYPE_HSPAP:
//                return NETWORK_CLASS_3_G;
//            case NETWORK_TYPE_LTE:
//                return NETWORK_CLASS_4_G;
//            default:
//                return NETWORK_CLASS_UNKNOWN;
//        }
//    }

}