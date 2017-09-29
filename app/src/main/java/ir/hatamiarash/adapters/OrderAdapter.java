/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ir.hatamiarash.hyperonline.R;
import models.Order;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.MyViewHolder> {
    private Context mContext;
    private List<Order> orderList;
    
    public OrderAdapter(Context mContext, List<Order> orderList) {
        this.mContext = mContext;
        this.orderList = orderList;
    }
    
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new MyViewHolder(itemView);
    }
    
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.id.setText(order.unique_id);
//        holder.date.setText(formatDate(order.date));
        holder.date.setText(order.date);
        holder.stuffs.setText(order.stuffs);
        holder.price.setText(order.price);
        holder.hour.setText(String.valueOf(order.hour) + " الی " + String.valueOf(order.hour + 1));
        
        switch (order.status) {
            case "abort":
                holder.status.setText("لغو شده");
                holder.status.setTextColor(ContextCompat.getColor(mContext, R.color.red));
                break;
            case "pending":
                holder.status.setText("در دست بررسی");
                holder.status.setTextColor(ContextCompat.getColor(mContext, R.color.md_orange_600));
                break;
            case "shipped":
                holder.status.setText("ارسال شده");
                holder.status.setTextColor(ContextCompat.getColor(mContext, R.color.md_cyan_700));
                break;
            case "delivered":
                holder.status.setText("تحویل داده شده");
                holder.status.setTextColor(ContextCompat.getColor(mContext, R.color.green));
                break;
            default:
                holder.status.setText("نامشخص");
                holder.status.setTextColor(ContextCompat.getColor(mContext, R.color.purple));
                break;
        }
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    
    @Override
    public int getItemCount() {
        return orderList.size();
    }
    
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView id, date, stuffs, price, status, hour;
        
        MyViewHolder(View view) {
            super(view);
            id = view.findViewById(R.id.id);
            date = view.findViewById(R.id.date);
            stuffs = view.findViewById(R.id.stuffs);
            price = view.findViewById(R.id.price);
            status = view.findViewById(R.id.status);
            hour = view.findViewById(R.id.hour);
        }
    }
    
    private String formatDate(String Date) {
        String[] split = Date.split(":");
        return split[0] + "     " + split[1];
    }
}