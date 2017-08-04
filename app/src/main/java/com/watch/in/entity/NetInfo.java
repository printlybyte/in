package com.watch.in.entity;

import android.util.Log;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Transient;

/**
 * Created by Administrator on 2017/6/20.
 *
 * @Entity 定义实体
 * @nameInDb 在数据库中的名字，如不写则为实体中类名
 * @indexes 索引
 * @createInDb 是否创建表，默认为true,false时不创建
 * @schema 指定架构名称为实体
 * @active 无论是更新生成都刷新
 * @Id
 * @NotNull 不为null
 * @Unique 唯一约束
 * @ToMany 一对多
 * @OrderBy 排序
 * @ToOne 一对一
 * @Transient 不存储在数据库中
 * @generated 由greendao产生的构造函数或方法
 */
@Entity
public class NetInfo {


        @Id
        private Long id;
        private String electric;
        private String time;
        @Transient
        private int tempUsageCount;
        public String getTime() {
                return this.time;
        }
        public void setTime(String time) {
                this.time = time;
        }
        public String getElectric() {
                return this.electric;
        }
        public void setElectric(String electric) {
                this.electric = electric;
        }
        public Long getId() {
                return this.id;
        }
        public void setId(Long id) {
                this.id = id;
        }
        @Generated(hash = 897412742)
        public NetInfo(Long id, String electric, String time) {
                this.id = id;
                this.electric = electric;
                this.time = time;
        }
        @Generated(hash = 1179980607)
        public NetInfo() {
        }

    }



