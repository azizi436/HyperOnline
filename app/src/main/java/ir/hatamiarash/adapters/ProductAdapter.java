/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import helper.Helper;
import helper.SQLiteHandlerItem;
import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.interfaces.CardBadge;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;
import models.Product;

import static android.content.Context.VIBRATOR_SERVICE;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.MyViewHolder> {
	private CardBadge cardBadge;
	private Context mContext;
	private List<Product> productList;
	private SQLiteHandlerItem db_item;
	private Vibrator vibrator;
	
	public ProductAdapter(Context mContext, List<Product> productList) {
		this.mContext = mContext;
		this.productList = productList;
		db_item = new SQLiteHandlerItem(mContext);
		try {
			this.cardBadge = ((CardBadge) mContext);
		} catch (ClassCastException e) {
			throw new ClassCastException("Activity must implement AdapterCallback.");
		}
		vibrator = (Vibrator) mContext.getSystemService(VIBRATOR_SERVICE);
	}
	
	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product2, parent, false);
		return new MyViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(final MyViewHolder holder, int position) {
		final Product product = productList.get(position);
		holder.id.setText(product.unique_id);
		holder.name.setText(product.name);
		holder.price.setText(product.price + " تومان");
		holder.price_backup.setText(product.price);
		holder.point.setText(String.valueOf(product.point));
		holder.point_count.setText(String.valueOf(product.point_count));
		holder.off.setText(String.valueOf(product.off));
		holder.count.setText(String.valueOf(product.count));
		holder.count_cart.setText(String.valueOf(db_item.getCount(product.unique_id)));
		if (String.valueOf(product.image).equals("null"))
			Glide.with(mContext).load(R.drawable.nnull).into(holder.image);
		else
			Glide.with(mContext).load(URLs.image_URL + String.valueOf(product.image)).into(holder.image);
		
		if (product.off == 0)
			holder.price_off.setVisibility(View.INVISIBLE);
		else {
			holder.price_off.setText(Helper.CalculatePrice(product.price, product.off) + " تومان");
			holder.price_backup.setText(Helper.CalculatePrice(product.price, product.off));
			holder.price.setPaintFlags(holder.price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			holder.price.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
			holder.price_off.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
		}
		
		if (product.count == 0) {
			holder.add_layout.setVisibility(View.INVISIBLE);
			holder.price.setVisibility(View.VISIBLE);
			holder.price.setText("موجود نمی باشد");
			holder.price.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
		}
		
		if (db_item.isExistsID(product.unique_id) && db_item.isExists(product.name)) {
			holder.add_layout.setVisibility(View.INVISIBLE);
			holder.price.setVisibility(View.VISIBLE);
			holder.change_layout.setVisibility(View.VISIBLE);
			//holder.price.setText("در سبد موجود است");
			//holder.price.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
		}
		
		holder.add_layout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				vibrator.vibrate(50);
				holder.add_layout.setVisibility(View.INVISIBLE);
				holder.change_layout.setVisibility(View.VISIBLE);
				//holder.price.setText("اضافه شد");
				//holder.price.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
				int off = product.off * Integer.valueOf(product.price) / 100;
				int fPrice = Integer.valueOf(product.price) - off;
				db_item.addItem(
						product.unique_id,
						product.name,
						String.valueOf(fPrice),
						product.description,
						String.valueOf(off),
						String.valueOf(1),
						String.valueOf(product.count)
				);
				try {
					cardBadge.updateBadge();
				} catch (ClassCastException e) {
					Log.w("CallBack", e.getMessage());
				}
				holder.count_cart.setText("1");
			}
		});
		
		holder.inc.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				vibrator.vibrate(50);
				int pCount = Integer.valueOf(db_item.getItemDetails(product.unique_id).get(6));
				
				if (pCount < Integer.valueOf(holder.count.getText().toString())) {
					int count = pCount + 1;
					int off = (product.off * Integer.valueOf(product.price) / 100) * count;
					int fPrice = Integer.valueOf(product.price) * count - off;
					db_item.updateItem(
							product.unique_id,
							String.valueOf(count),
							String.valueOf(fPrice),
							String.valueOf(off)
					);
					try {
						cardBadge.updateBadge();
					} catch (ClassCastException e) {
						Log.w("CallBack", e.getMessage());
					}
					holder.count_cart.setText(String.valueOf(count));
				} else
					Helper.MakeToast(mContext, "تعداد بیشتر موجود نمی باشد", TAGs.ERROR);
			}
		});
		
		holder.dec.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				vibrator.vibrate(50);
				int pCount = Integer.valueOf(db_item.getItemDetails(product.unique_id).get(6));
				if (pCount == 1) {
					db_item.deleteItem(
							product.unique_id
					);
					holder.add_layout.setVisibility(View.VISIBLE);
					holder.change_layout.setVisibility(View.INVISIBLE);
					holder.price.setTextColor(ContextCompat.getColor(mContext, R.color.black));
					if (product.off == 0)
						holder.price_off.setVisibility(View.INVISIBLE);
					else {
						holder.price_off.setText(Helper.CalculatePrice(product.price, product.off) + " تومان");
						holder.price_backup.setText(Helper.CalculatePrice(product.price, product.off));
						holder.price.setPaintFlags(holder.price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
						holder.price.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
						holder.price_off.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
					}
				} else {
					int count = pCount - 1;
					int off = (product.off * Integer.valueOf(product.price) / 100) * count;
					int fPrice = Integer.valueOf(product.price) * count - off;
					db_item.updateCount(
							product.unique_id,
							String.valueOf(count),
							String.valueOf(fPrice),
							String.valueOf(off)
					);
					holder.count_cart.setText(String.valueOf(count));
				}
				try {
					cardBadge.updateBadge();
				} catch (ClassCastException e) {
					Log.w("CallBack", e.getMessage());
				}
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
		return productList.size();
	}
	
	class MyViewHolder extends RecyclerView.ViewHolder {
		TextView name, price, price_backup, price_off, id, count, count_cart, point, point_count, off;
		ImageView image, inc, dec;
		LinearLayout add_layout, change_layout;
		
		MyViewHolder(View view) {
			super(view);
			id = view.findViewById(R.id.product_id);
			price_backup = view.findViewById(R.id.product_price_backup);
			count = view.findViewById(R.id.product_count);
			count_cart = view.findViewById(R.id.product_count_cart);
			point = view.findViewById(R.id.product_point);
			point_count = view.findViewById(R.id.product_point_count);
			off = view.findViewById(R.id.product_off);
			name = view.findViewById(R.id.product_name);
			price = view.findViewById(R.id.product_price);
			price_off = view.findViewById(R.id.product_price_off);
			image = view.findViewById(R.id.product_image);
			add_layout = view.findViewById(R.id.add_layout);
			change_layout = view.findViewById(R.id.change_layout);
			inc = view.findViewById(R.id.inc);
			dec = view.findViewById(R.id.dec);
		}
	}
}