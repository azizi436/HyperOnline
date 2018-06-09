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
import ir.hatamiarash.hyperonline.models.Transaction;
import ir.hatamiarash.hyperonline.utils.TAGs;

import static ir.hatamiarash.hyperonline.helpers.FormatHelper.formatCardNumber;
import static ir.hatamiarash.hyperonline.helpers.PriceHelper.formatPrice;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.MyViewHolder> {
	private List<Transaction> transactionList;
	
	public TransactionAdapter(List<Transaction> transactionList) {
		this.transactionList = transactionList;
	}
	
	@Override
	@NonNull
	public TransactionAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
		return new TransactionAdapter.MyViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(@NonNull TransactionAdapter.MyViewHolder holder, int position) {
		final Transaction transaction = transactionList.get(position);
		holder.date.setText(transaction.date);
		holder.price.setText(formatPrice(transaction.price) + " تومان");
		holder.description.setText(transaction.description);
		if (!transaction.card.equals(TAGs.NULL))
			holder.card.setText(formatCardNumber(transaction.card).trim());
		else
			holder.card.setText("-");
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
		return transactionList.size();
	}
	
	class MyViewHolder extends RecyclerView.ViewHolder {
		TextView date, price, description, card;
		
		MyViewHolder(View view) {
			super(view);
			date = view.findViewById(R.id.date);
			price = view.findViewById(R.id.price);
			description = view.findViewById(R.id.desc);
			card = view.findViewById(R.id.card);
		}
	}
}