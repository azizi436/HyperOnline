/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ir.hatamiarash.hyperonline.R;
import models.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {
    private Context mContext;
    private List<Message> orderList;
    
    public MessageAdapter(Context mContext, List<Message> orderList) {
        this.mContext = mContext;
        this.orderList = orderList;
    }
    
    @Override
    public MessageAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageAdapter.MyViewHolder(itemView);
    }
    
    @Override
    public void onBindViewHolder(final MessageAdapter.MyViewHolder holder, int position) {
        Message order = orderList.get(position);
        holder.date.setText(order.date);
        holder.title.setText(order.title);
        holder.body.setText(order.body);
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
        TextView date, title, body;
        
        MyViewHolder(View view) {
            super(view);
            date = view.findViewById(R.id.date);
            title = view.findViewById(R.id.title);
            body = view.findViewById(R.id.body);
        }
    }
}