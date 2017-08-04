package com.watch.in.uitlis.data;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import com.watch.in.Tets.UniltTest;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by Administrator on 2017/6/21.
 */

public class SystemUitls {
    /**
     * @RAM 970MB/2,915MB "RAM "+available[0]+available[1]+"/"+total[0]+total[1])
     * 显示RAM的可用和总容量，RAM相当于电脑的内存条
     *  可用RAM
     */
    public static String showRAMInfo(Context context) {
        String aa = null;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        String[] available = fileSize(mi.availMem);
        String[] total = fileSize(mi.totalMem);
        aa = available[0];
        if (aa.length() < 4) {
            StringBuilder ss = new StringBuilder();
            ss.append("0.").append(aa);
            aa = ss.toString().trim();
        }else {
            StringBuilder ss = new StringBuilder(aa);
            ss.replace(1,2,".");
            aa = ss.toString().trim();
        }
        Log.i("QWEQWE", "RAM " + available[0] + available[1] + "/" + total[0] + total[1]);
        return aa;

    }

    /*返回为字符串数组[0]为大小[1]为单位KB或者MB*/
    public static String[] fileSize(long size) {
        String str = "";
        if (size >= 1000) {
            str = "KB";
            size /= 1000;
            if (size >= 1000) {
                str = "MB";
                size /= 1000;
            }
        }
        /*将每3个数字用,分隔如:1,000*/
        DecimalFormat formatter = new DecimalFormat();
        formatter.setGroupingSize(3);
        String result[] = new String[2];
        result[0] = formatter.format(size);
        result[1] = str;
        return result;
    }


    /**
     * 获得sd卡剩余容量，即可用大小
     *
     * @return
     */
    public static String getSDAvailableSize(Context context) {
        String SDAvailableSize = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            SDAvailableSize = Formatter.formatFileSize(context, blockSize * availableBlocks);
            String ss = SDAvailableSize;
            String aa[] = ss.split(".GB");
            SDAvailableSize = aa[0];
        } else {
            Toast.makeText(context, "SD不存在，请检查以后再试", Toast.LENGTH_SHORT).show();
        }

        return SDAvailableSize;
    }



    /**
     * 判断手机是否root
     */

    public static boolean isRoot() {
        boolean bool = false;
        try {
            if ((!new File("/system/bin/su").exists()) && (!new File("/system/xbin/su").exists())) {
                bool = false;
            } else {
                bool = true;
            }
        } catch (Exception e) {
        }
        return bool;
    }
}
