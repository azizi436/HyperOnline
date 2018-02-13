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

import helper.SQLiteHandlerSupport;
import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.interfaces.Refresh;
import models.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {
	private Context mContext;
	private List<Message> orderList;
	private SQLiteHandlerSupport db_support;
	private Refresh refresh;
	
	public MessageAdapter(Context mContext, List<Message> orderList) {
		this.mContext = mContext;
		this.orderList = orderList;
		db_support = new SQLiteHandlerSupport(mContext);
		try {
			this.refresh = ((Refresh) mContext);
		} catch (ClassCastException e) {
			throw new ClassCastException("Activity must implement AdapterCallback.");
		}
	}
	
	@Override
	public MessageAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
		return new MessageAdapter.MyViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(final MessageAdapter.MyViewHolder holder, int position) {
		final Message message = orderList.get(position);
		holder.date.setText(message.date);
		holder.title.setText(message.title);
		holder.body.setText(message.body);
		
		holder.delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				db_support.deleteMessage(message.date);
				refresh.refresh();
			}
		});
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
		TextView date, title, body, delete;
		
		MyViewHolder(View view) {
			super(view);
			date = view.findViewById(R.id.date);
			title = view.findViewById(R.id.title);
			body = view.findViewById(R.id.body);
			delete = view.findViewById(R.id.delete);
		}
	}
}