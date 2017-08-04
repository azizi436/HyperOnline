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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import ir.hatamiarash.hyperonline.R;
import models.Product;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.MyViewHolder> {
    
    private Context mContext;
    private List<Product> productList;
    
    public ProductAdapter(Context mContext, List<Product> productList) {
        this.mContext = mContext;
        this.productList = productList;
    }
    
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product2, parent, false);
        return new MyViewHolder(itemView);
    }
    
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.title.setText(product.name);
        holder.price.setText(product.price + " تومان");
        Glide.with(mContext).load(R.drawable.nnull).into(holder.image);
        
        holder.add_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.add_layout.setVisibility(View.INVISIBLE);
                holder.change_layout.setVisibility(View.VISIBLE);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return productList.size();
    }
    
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, price;
        ImageView image;
        LinearLayout add_layout, change_layout;
        
        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            price = view.findViewById(R.id.price);
            image = view.findViewById(R.id.image);
            add_layout = view.findViewById(R.id.add_layout);
            change_layout = view.findViewById(R.id.change_layout);
        }
    }
}