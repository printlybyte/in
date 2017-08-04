//package com.watch.in.uitlis.data;
//
//import com.watch.in.Api.BaseApplication;
//import com.watch.in.entity.NetInfo;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by Administrator on 2017/6/20.
// */
//
//public class DaoHolperUtils {
//
//    /**
//     * 网络信息数据库插入操作
//     */
//    public static Object NetInfoDaoInsert(Object o) {
//        BaseApplication.getDaoInstant().getNetInfoDao().insert((NetInfo) o);
//        return o;
//    }
//
//    /**
//     * 网络信息数据库删除操作
//     */
//    public static void NetInfoDaoDelete() {
//        BaseApplication.getDaoInstant().getNetInfoDao().deleteAll();
//    }
//
//    /**
//     * 网络信息数据库删除指定id
//     */
//    public static void NetInfoDaoDeleteBykey(long xx) {
//        BaseApplication.getDaoInstant().getNetInfoDao().deleteByKey(xx);
//    }
//
//    /**
//    *查询当前表的所有数据
//    *
//    */
//    public static List<NetInfo> NetInfoDaoQurey() {
//        List<NetInfo> liss = BaseApplication.getDaoInstant().getNetInfoDao().queryBuilder().list();
//        return liss;
//    }
//}
