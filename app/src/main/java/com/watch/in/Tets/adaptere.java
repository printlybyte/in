package com.watch.in.Tets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.watch.in.R;
import com.watch.in.entity.NetInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/22.
 */

public class adaptere extends BaseAdapter {
    private List<NetInfo> mlist = new ArrayList<>();
    private Context context;

    public adaptere(List<NetInfo> mlist, Context context) {
        this.context = context;
        this.mlist = mlist;
    }

    @Override
    public int getCount() {
        return mlist.size();
    }

    @Override
    public Object getItem(int position) {
        return mlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHodeler viewHodeler = null;
        if (viewHodeler == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.daotestadapter, null);
            viewHodeler = new ViewHodeler();
            viewHodeler.textView1 = (TextView) convertView.findViewById(R.id.daotext1);
            viewHodeler.textView2 = (TextView) convertView.findViewById(R.id.daotext2);
            convertView.setTag(viewHodeler);
        } else {
            viewHodeler = (ViewHodeler) convertView.getTag();
        }
        viewHodeler.textView1.setText(mlist.get(position).getElectric());
        viewHodeler.textView2.setText(mlist.get(position).getTime()+"");


        return convertView;
    }

    class ViewHodeler {
        TextView textView1;
        TextView textView2;
    }
}
