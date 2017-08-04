package com.watch.in.broadcast;

/**
 * Created by Administrator on 2017/7/14.
 */


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 类名：ShutdownBroadcastReceiver
 * 功能描述：在系统即将关闭时发出的广播的接收器
 * @author android_ls
 */
public class ShutdownBroadcastReceiver extends BroadcastReceiver {


    private static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(ACTION_SHUTDOWN)) {
        }
    }
}