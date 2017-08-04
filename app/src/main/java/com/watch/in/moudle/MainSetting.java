//package com.watch.in.moudle;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.view.View;
//import android.widget.Button;
//
//import com.watch.in.R;
//import com.watch.in.Tets.Daotest;
//import com.watch.in.Tets.volumeTest;
//
//import org.greenrobot.greendao.generator.DaoUtil;
//
//public class MainSetting extends AppCompatActivity implements View.OnClickListener {
//
//    /**
//     * 分贝
//     */
//    private Button mTest1;
//    /**
//     * wifi
//     */
//    private Button mTest2;
//    /**
//     * 数据库
//     */
//    private Button mTest3;
//    /**
//     * 音量
//     */
//    private Button mTest4;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main_setting);
////        initView();
//    }
//
//    private void initView() {
//        mTest1 = (Button) findViewById(R.id.test1);
//        mTest1.setOnClickListener(this);
//        mTest2 = (Button) findViewById(R.id.test2);
//        mTest2.setOnClickListener(this);
//        mTest3 = (Button) findViewById(R.id.test3);
//        mTest3.setOnClickListener(this);
//        mTest4 = (Button) findViewById(R.id.test4);
//        mTest4.setOnClickListener(this);
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.test1:
//                Intent ii = new Intent(this, DecibelSetting.class);
//                startActivity(ii);
//                break;
//            case R.id.test2:
//                Intent ii2 = new Intent(this, WifiSetting.class);
//                startActivity(ii2);
//                break;
//            case R.id.test3:
//                Intent ii3 = new Intent(this, Daotest.class);
//                startActivity(ii3);
//                break;
//            case R.id.test4:
//                Intent ii4 = new Intent(this, volumeTest.class);
//                startActivity(ii4);
//                break;
//        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        finish();
//        super.onBackPressed();
//    }
//}
