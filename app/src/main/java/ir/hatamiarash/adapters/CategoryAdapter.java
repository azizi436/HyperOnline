/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import ir.hatamiarash.hyperonline.R;
import models.Category;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {
    private Context mContext;
    private List<Category> categoryList;
    
    public CategoryAdapter(Context mContext, List<Category> categoryList) {
        this.mContext = mContext;
        this.categoryList = categoryList;
    }
    
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new MyViewHolder(itemView);
    }
    
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.id.setText(category.unique_id);
        holder.name.setText(category.name);
        holder.point.setText(String.valueOf(category.point));
        holder.point_count.setText(String.valueOf(category.point_count));
        holder.off.setText(String.valueOf(category.off));
        Glide.with(mContext).load(R.drawable.nnull).into(holder.image);
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
}