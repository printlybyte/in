//package com.watch.in.Tets;
//
//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ListView;
//
//import com.watch.in.R;
//import com.watch.in.entity.NetInfo;
//import com.watch.in.uitlis.data.PublicUtils;
//
//
//public class Daotest extends AppCompatActivity implements View.OnClickListener {
//
//    private ListView mListItemDaoTets;
//    private Button mXxxxxxx;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_daotest);
//        initView();
//
//    }
//
//
//    public void onClick2(View view) {
//        DaoHolperUtils.NetInfoDaoDelete();
//    }
//
//    public void onClick3(View view) {
//
//    }
//
//    public void onClick4(View view) {
//        adaptere aa = new adaptere(DaoHolperUtils.NetInfoDaoQurey(), Daotest.this);
//        mListItemDaoTets.setAdapter(aa);
//    }
//
//    private void initView() {
//        mListItemDaoTets = (ListView) findViewById(R.id.list_item_dao_tets);
//        mXxxxxxx = (Button) findViewById(R.id.xxxxxxx);
//        mXxxxxxx.setOnClickListener(this);
//    }
//
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.xxxxxxx:
//                DaoHolperUtils.NetInfoDaoInsert(new NetInfo(null, "10%", PublicUtils.getCurrentTime()));
//                break;
//            case R.id.list_item_dao_tets:
//                break;
//        }
//    }
//}
