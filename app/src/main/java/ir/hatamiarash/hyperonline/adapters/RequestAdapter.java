/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ir.hatamiarash.hyperonline.R;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.MyViewHolder> {
	private List<String> requests;
	
	public RequestAdapter(List<String> requests) {
		this.requests = requests;
	}
	
	@Override
	@NonNull
	public RequestAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
		return new RequestAdapter.MyViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(@NonNull RequestAdapter.MyViewHolder holder, int position) {
		String request = requests.get(position);
		holder.count.setText(request);
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
		return requests.size();
	}
	
	class MyViewHolder extends RecyclerView.ViewHolder {
		TextView title, count, description;
		
		MyViewHolder(View view) {
			super(view);
			title = view.findViewById(R.id.title);
			count = view.findViewById(R.id.count);
			description = view.findViewById(R.id.description);
		}
	}
}
