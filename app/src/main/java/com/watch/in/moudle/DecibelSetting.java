package com.watch.in.moudle;

import android.os.Bundle;

import com.watch.in.Api.MyMediaRecorder;
import com.watch.in.R;
import com.watch.in.uitlis.data.FileUtil;
import com.watch.in.uitlis.data.PublicUtils;
import com.watch.in.uitlis.data.SharedPreferencesUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.watch.in.uitlis.data.PublicUtils.AirplaneModeisOff;
import static com.watch.in.uitlis.data.PublicUtils.GetNextWarnTime;
import static com.watch.in.uitlis.data.PublicUtils.dip2px;
import static com.watch.in.uitlis.data.PublicUtils.getCurrentTime;
import static com.watch.in.uitlis.data.PublicUtils.setSettingsOnHigh;
import static com.watch.in.uitlis.data.PublicUtils.upgradeRootPermission;


public class DecibelSetting extends Activity {

    private MyMediaRecorder mRecorder;
    private String TAG = "MainActivity";
    float volume = 10000;

    // 用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private EditText ac_main_et_minDb;
    private EditText ac_main_et_timer;
    private Button button3;
    private String minDb;//最小分贝
    private String timer;//恢复时间间隔
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRecorder = new MyMediaRecorder();
        upgradeRootPermission(getPackageCodePath());
        setConfigInfoDialog();
        Toast.makeText(this, "修改了下", Toast.LENGTH_SHORT).show();
    }

    /**
     * 设置信息的dialog
     */
    private void setConfigInfoDialog() {

        View v = LayoutInflater.from(this).inflate(R.layout.activity_main_activityxx
                , null);
        final Dialog dialog_toWarn = new Dialog(this, R.style.DialogStyle);
        dialog_toWarn.setCanceledOnTouchOutside(false);
        dialog_toWarn.setCancelable(true);
        dialog_toWarn.show();
        Window window = dialog_toWarn.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        lp.width = dip2px(this, 300); // 宽度
        lp.height = dip2px(this, 210); // 高度
        // lp.alpha = 0.7f; // 透明度
        window.setAttributes(lp);
        window.setContentView(v);
//        dialog_toSet.setOnKeyListener(new DialogInterface.OnKeyListener() {
//            @Override
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//
//                if (keyCode == event.KEYCODE_BACK) {
//                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                        finish();
//                    }
//                }
//                return false;
//            }
//        });
        ac_main_et_minDb = (EditText) v.findViewById(R.id.ac_main_et_minDb);
        ac_main_et_timer = (EditText) v.findViewById(R.id.ac_main_et_timer);
        button3 = (Button) v.findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String minDb = ac_main_et_minDb.getText().toString().trim();
                String timer = ac_main_et_timer.getText().toString().trim();
                if (!TextUtils.isEmpty(minDb) && !TextUtils.isEmpty(timer)) {
                    SharedPreferencesUtils.saveTosp(DecibelSetting.this, "MainActivity", "MINDB", minDb);
                    SharedPreferencesUtils.saveTosp(DecibelSetting.this, "MainActivity", "TIMER", timer);
                    // 点击后台运行
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(DecibelSetting.this, "不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /**
     * 开始记录
     *
     * @param fFile
     */
    public void startRecord(File fFile) {
        try {
            mRecorder.setMyRecAudioFile(fFile);
            if (mRecorder.startRecorder()) {
                startListenAudio();
            } else {
                Toast.makeText(this, "启动录音失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "录音机已被占用或录音权限被禁止", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void startListenAudio() {
        handler.sendEmptyMessageDelayed(0, 100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String time = formatter.format(new Date());
        String fileName = time + ".amr";
        File file = FileUtil.createFile(fileName);
        if (file != null) {
            Log.v("file", "file =" + file.getAbsolutePath());
            startRecord(file);
        } else {
            Toast.makeText(getApplicationContext(), "创建文件失败", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 停止记录
     */
    @Override
    protected void onPause() {
        super.onPause();
//        mRecorder.delete(); //停止记录并删除录音文件
//        handler.removeMessages(0);
    }

    @Override
    protected void onDestroy() {
        handler.removeMessages(0);
        mRecorder.delete();
        super.onDestroy();
    }

    private Handler handler = new Handler() {

        private String nextTime = "";

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (this.hasMessages(0)) {
                return;
            }
            minDb = SharedPreferencesUtils.getDataFromSp(DecibelSetting.this, "MainActivity", "MINDB");
            timer = SharedPreferencesUtils.getDataFromSp(DecibelSetting.this, "MainActivity", "TIMER");
            volume = mRecorder.getMaxAmplitude();  //获取声压值
            if (volume > 0 && volume < 1000000) {
                float soundValue = (float) (Math.log10(volume));
                int dbCount = (int) PublicUtils.setDbCount(20 * soundValue);  //将声压值转为分贝值
                Log.i(TAG,dbCount+">>>>>>>>>>>>>>>>>>>>>>>>");
                if (dbCount > Integer.parseInt(minDb)) {
                    if (AirplaneModeisOff(DecibelSetting.this)) {
                        setSettingsOnHigh(1);
                        nextTime = GetNextWarnTime(Integer.parseInt(timer));
                    }

                }
                if (!AirplaneModeisOff(DecibelSetting.this)) {
                    if (getCurrentTime().equals(nextTime)) {
                        setSettingsOnHigh(0);
                    }
                }
            }
            handler.sendEmptyMessageDelayed(0, 100);
        }
    };


}