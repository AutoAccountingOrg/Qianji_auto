package cn.dreamn.qianji_auto.ui.adapter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.adapter.SmartViewHolder;

import cn.dreamn.qianji_auto.R;
import cn.dreamn.qianji_auto.ui.base.BaseAdapter;

public class RemoteSortListAdapter extends BaseAdapter {

    public RemoteSortListAdapter() {
        super(R.layout.adapter_remote_sort_item);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onBindViewHolder(SmartViewHolder holder, Bundle item, int position) {


        TextView app_name = (TextView) holder.findView(R.id.app_name);
        TextView app_update = (TextView) holder.findView(R.id.app_update);
        app_name.setText(item.getString("name"));
        app_update.setText(item.getString("date"));
    }

}
