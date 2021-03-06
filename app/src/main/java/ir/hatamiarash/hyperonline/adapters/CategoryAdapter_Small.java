/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import ir.hatamiarash.hyperonline.activities.Activity_List;
import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.hyperonline.utils.TAGs;
import ir.hatamiarash.hyperonline.models.Category;

public class CategoryAdapter_Small extends RecyclerView.Adapter<CategoryAdapter_Small.MyViewHolder> {
	private static String HOST;
	private Context mContext;
	private List<Category> categoryList;
	private Vibrator vibrator;
	
	public CategoryAdapter_Small(@NotNull Context mContext, List<Category> categoryList) {
		this.mContext = mContext;
		this.categoryList = categoryList;
		vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
		HOST = mContext.getResources().getString(R.string.url_host);
	}
	
	@Override
	@NonNull
	public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_small, parent, false);
		return new MyViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
		Category category = categoryList.get(position);
		holder.id.setText(category.unique_id);
		holder.name.setText(category.name);
		holder.point.setText(String.valueOf(category.point));
		holder.point_count.setText(String.valueOf(category.point_count));
		holder.off.setText(String.valueOf(category.off));
		if (String.valueOf(category.image).equals(TAGs.NULL))
			Glide.with(mContext).load(R.drawable.nnull).into(holder.image);
		else
			Glide.with(mContext).load(mContext.getResources().getString(R.string.url_image, HOST) + String.valueOf(category.image)).into(holder.image);
		
		holder.name.setOnClickListener(new MyClickListener(category.unique_id, category.name, category.level));
		holder.image.setOnClickListener(new MyClickListener(category.unique_id, category.name, category.level));
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
		return categoryList.size();
	}
	
	class MyViewHolder extends RecyclerView.ViewHolder {
		TextView name, id, point, point_count, off;
		ImageView image;
		
		MyViewHolder(View view) {
			super(view);
			id = view.findViewById(R.id.category_id);
			point = view.findViewById(R.id.category_point);
			point_count = view.findViewById(R.id.category_point_count);
			off = view.findViewById(R.id.category_off);
			name = view.findViewById(R.id.category_name);
			image = view.findViewById(R.id.category_image);
		}
	}
	
	private class MyClickListener implements View.OnClickListener {
		private String id;
		private String name;
		private int level;
		
		MyClickListener(String id, String name, int level) {
			this.id = id;
			this.name = name;
			this.level = level;
		}
		
		@Override
		public void onClick(View v) {
			vibrator.vibrate(50);
			Intent i = new Intent(mContext, Activity_List.class);
			// we are in level 3 and we want products
			if (level == 3)
				i.putExtra(TAGs.CATEGORY, "1");
			else {
				i.putExtra(TAGs.CATEGORY, "2");
				// go to next level category
				i.putExtra(TAGs.LEVEL, String.valueOf(level + 1));
			}
			i.putExtra(TAGs.CATEGORY_ID, id);
			i.putExtra(TAGs.TITLE, name);
			mContext.startActivity(i);
		}
	}
}