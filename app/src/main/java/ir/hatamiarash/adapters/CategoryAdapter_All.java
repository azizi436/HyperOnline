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

public class CategoryAdapter_All extends RecyclerView.Adapter<CategoryAdapter_All.MyViewHolder> {
    
    private Context mContext;
    private List<Category> categoryList;
    
    public CategoryAdapter_All(Context mContext, List<Category> categoryList) {
        this.mContext = mContext;
        this.categoryList = categoryList;
    }
    
    @Override
    public CategoryAdapter_All.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category2, parent, false);
        return new CategoryAdapter_All.MyViewHolder(itemView);
    }
    
    @Override
    public void onBindViewHolder(final CategoryAdapter_All.MyViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.title.setText(category.name);
        holder.info.setText(category.info);
        Glide.with(mContext).load(category.image).into(holder.image);
    }
    
    @Override
    public int getItemCount() {
        return categoryList.size();
    }
    
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, info, id;
        ImageView image;
        
        MyViewHolder(View view) {
            super(view);
            id = view.findViewById(R.id.category_id);
            title = view.findViewById(R.id.category_name);
            info = view.findViewById(R.id.category_info);
            image = view.findViewById(R.id.category_photo);
        }
    }
}