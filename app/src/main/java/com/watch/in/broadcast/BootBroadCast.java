package com.watch.in.broadcast;

/**
 * Created by Administrator on 2017/6/21.
 */


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.watch.in.Tets.UniltTest;
import com.watch.in.uitlis.wifi.GPRSUtil;

import static com.watch.in.uitlis.data.PublicUtils.isRoot;

/**
 * Created by ${王sir} on 2017/6/16.
 * application 监听手机启动后的广播
 */

public class BootBroadCast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
//            Intent mIntent = new Intent(context, DecibelSetting.class);
//            context.startActivity(mIntent);
//            ProperUtil.writeDateToLocalFile("WifiPwd", "AS1");

        }


//        ProperUtil.writeDateToLocalFile("Port", "AS");

       if (isRoot()){
        GPRSUtil.setGprsOn_Off(true);}
        Intent intent2 = new Intent(context, UniltTest.class);
        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent2);


    }
}