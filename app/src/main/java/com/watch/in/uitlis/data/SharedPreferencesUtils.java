package com.watch.in.uitlis.data;

/**
 * Created by Administrator on 2017/6/21.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * Created by ${王sir} on 2017/6/16.
 * application  sp工具类
 */

public class SharedPreferencesUtils {

    public static void saveTosp(Context context, String className, String key, String value) {

        SharedPreferences sp = context.getSharedPreferences(className, context.MODE_PRIVATE);
        SharedPreferences.Editor et = sp.edit();
        et.putString(key, value);
        et.commit();
        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
    }

    public static String getDataFromSp(Context context, String className, String key) {
        SharedPreferences sp = context.getSharedPreferences(className, context.MODE_PRIVATE);

        return sp.getString(key, "-1");
    }

    public static void saveToImei(Context context, String imei) {
        SharedPreferences sp = context.getSharedPreferences("Facilityimei", context.MODE_PRIVATE);
        SharedPreferences.Editor et = sp.edit();
        et.putString("facilityimei", imei);
        et.commit();
    }

    public static String getToImei(Context context) {
        SharedPreferences sp = context.getSharedPreferences("Facilityimei", context.MODE_PRIVATE);
        return sp.getString("facilityimei", "no");
    }

    public static void saveToPhoneData(Context context, String data) {
        SharedPreferences sp = context.getSharedPreferences("phonedatas", context.MODE_PRIVATE);
        SharedPreferences.Editor et = sp.edit();
        et.putString("phonedatasa", data);
        et.commit();
    }

    public static String getToPhoneData(Context context) {
        SharedPreferences sp = context.getSharedPreferences("phonedatas", context.MODE_PRIVATE);
        return sp.getString("phonedatasa", "no");
    }

    public static void removePhoneData(Context context) {
        SharedPreferences.Editor sp = context.getSharedPreferences("phonedatas", context.MODE_PRIVATE).edit();
        sp.remove("phonedatasa");
        sp.commit();

    }
}
