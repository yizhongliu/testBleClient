package com.iview.testbleclient;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BleDeviceAdapter extends RecyclerView.Adapter<BleDeviceAdapter.ViewHolder> {
    private final static String TAG = "BleDeviceAdapter";

    private List<BleDevice> bleDeviceList;
    private OnitemClick onitemClick;

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameText;
        TextView macText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = (TextView) itemView.findViewById(R.id.deviceName);
            macText = (TextView) itemView.findViewById(R.id.deviceMac);
        }
    }

    public BleDeviceAdapter(List<BleDevice> devices) {
        bleDeviceList = devices;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        Log.e(TAG, "onCreateViewHolder");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAbsoluteAdapterPosition();
                Log.e(TAG, "onClick position:" + position);
                if (onitemClick != null) {
                    onitemClick.onItemClick(position);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BleDevice bleDevice = bleDeviceList.get(position);
        holder.nameText.setText(bleDevice.getName());
        holder.macText.setText(bleDevice.getMac());

        Log.e(TAG, "onBindViewHolder position" + position);
    }

    @Override
    public int getItemCount() {
        return bleDeviceList.size();
    }

    //定义设置点击事件监听的方法
    public void setOnitemClickLintener (OnitemClick onitemClick) {
        this.onitemClick = onitemClick;
    }


    //定义一个点击事件的接口
    public interface OnitemClick {
        void onItemClick(int position);
    }


    public void addData(BleDevice device) {
        int position = getItemCount();
        Log.e(TAG, "add Data position:" + position);

        bleDeviceList.add(device);
        notifyDataSetChanged();
//        notifyItemInserted(0);
//
//        if (position != 0) {
//            notifyItemRangeChanged(0, getItemCount());
//        }
    }


}
