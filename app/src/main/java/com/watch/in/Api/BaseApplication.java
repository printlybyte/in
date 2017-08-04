package com.watch.in.Api;

/**
 * Created by Administrator on 2017/6/20.
 */
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.watch.in.entity.gen.DaoMaster;
import com.watch.in.entity.gen.DaoSession;
import com.watch.in.service.CheckApService;
import com.watch.in.uitlis.CrashHandler;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static com.watch.in.uitlis.data.PublicUtils.setSettingsWifi;


/**
 * Created by liuguodong on 2017/6/17.
 */
public class BaseApplication extends Application {

    private static DaoSession daoSession;
    public static final String TAG = BaseApplication.class.getSimpleName();
    @Override
    public void onCreate() {
        super.onCreate();
        //配置数据库
//        setupDatabase();
        xxx();

        //wifi管理
        checkService();

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());

    }
    private  void xxx(){
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addInterceptor(new LoggerInterceptor("TAG"))
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                //其他配置
                .build();

        OkHttpUtils.initClient(okHttpClient);
    }

    // 如果wifi开关已打开
    private void checkService() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        if (wifiManager.isWifiEnabled()) {
//            Log.i(TAG, "开启服务");
////            setSettingsWifi();
//            wifiManager.setWifiEnabled(false);
//        }
        // 开启服务
        startService(new Intent(this, CheckApService.class));
    }

    /**
     * 配置数据库
     */
    private void setupDatabase() {
        //创建数据库shop.db" 创建SQLite数据库的SQLiteOpenHelper的具体实现
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "notes-db", null);
        //获取可写数据库
            SQLiteDatabase db = helper.getWritableDatabase();
        //获取数据库对象  GreenDao的顶级对象，作为数据库对象、用于创建表和删除表
        DaoMaster daoMaster = new DaoMaster(db);
        //获取Dao对象管理者  管理所有的Dao对象，Dao对象中存在着增删改查等API
        daoSession = daoMaster.newSession();
    }

//    public static DaoSession getDaoInstant() {
//        return daoSession;
//    }
}