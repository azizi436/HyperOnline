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
import com.github.florent37.viewtooltip.ViewTooltip;

import java.util.List;

import ir.hatamiarash.hyperonline.R;
import models.Product;

import static com.github.florent37.viewtooltip.ViewTooltip.ALIGN.CENTER;
import static com.github.florent37.viewtooltip.ViewTooltip.Position.BOTTOM;

public class ProductAdapter_All extends RecyclerView.Adapter<ProductAdapter_All.MyViewHolder> {
    
    private Context mContext;
    private List<Product> productList;
    
    public ProductAdapter_All(Context mContext, List<Product> productList) {
        this.mContext = mContext;
        this.productList = productList;
    }
    
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new MyViewHolder(itemView);
    }
    
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.id.setText(product.unique_id);
        holder.name.setText(product.name);
        holder.price.setText(product.price + " تومان");
        holder.price_backup.setText(product.price);
        holder.point.setText(String.valueOf(product.point));
        holder.point_count.setText(String.valueOf(product.point_count));
        holder.off.setText(String.valueOf(product.off));
        holder.info.setText(String.valueOf(product.description));
        holder.count.setText(String.valueOf(product.count));
        Glide.with(mContext).load(R.drawable.nnull).into(holder.image);
        
        holder.info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewTooltip
                        .on(holder.info)
                        .autoHide(false, 5000)
                        .clickToHide(true)
                        .align(CENTER)
                        .position(BOTTOM)
                        .text("The text")
                        .corner(10)
                        .show();
            }
        });
        
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
        TextView name, price, price_off, price_backup, id, count, point, point_count, off, info;
        ImageView image;
        LinearLayout add_layout, change_layout, price_layout;
    
        MyViewHolder(View view) {
            super(view);
            id = view.findViewById(R.id.product_id);
            price_backup = view.findViewById(R.id.product_price_backup);
            count = view.findViewById(R.id.product_count);
            point = view.findViewById(R.id.product_point);
            point_count = view.findViewById(R.id.product_point_count);
            off = view.findViewById(R.id.product_off);
            info = view.findViewById(R.id.product_info);
            name = view.findViewById(R.id.product_name);
            price = view.findViewById(R.id.product_price);
            price_off = view.findViewById(R.id.product_price_off);
            image = view.findViewById(R.id.product_image);
            add_layout = view.findViewById(R.id.add_layout);
            change_layout = view.findViewById(R.id.change_layout);
            price_layout = view.findViewById(R.id.price_layout);
        }
    }
}