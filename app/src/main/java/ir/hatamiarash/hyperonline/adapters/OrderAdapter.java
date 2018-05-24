/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;

import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.hyperonline.helpers.Helper;
import ir.hatamiarash.hyperonline.models.Order;
import ir.hatamiarash.hyperonline.utils.TAGs;

import static ir.hatamiarash.hyperonline.HyperOnline.HOST;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.MyViewHolder> {
	private ProgressDialog mProgressDialog;
	private Context mContext;
	private List<Order> orderList;
	
	public OrderAdapter(Context mContext, List<Order> orderList) {
		this.mContext = mContext;
		this.orderList = orderList;
		
		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setMessage("دانلود");
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setCancelable(true);
	}
	
	private static String formatCurrency(String value) {
		String pattern = "###,###";
		DecimalFormat myFormatter = new DecimalFormat(pattern);
		return myFormatter.format(Double.valueOf(value));
	}
	
	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
		return new MyViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
		final Order order = orderList.get(position);
		holder.id.setText(order.unique_id);
//        holder.date.setText(formatDate(order.date));
		holder.date.setText(order.date);
		holder.stuffs.setText(order.stuffs);
		holder.price.setText(formatCurrency(order.price) + " تومان");
		holder.hour.setText(String.valueOf(order.hour) + " الی " + String.valueOf(order.hour + 1));
		
		switch (order.status) {
			case "abort":
				holder.status.setText("لغو شده");
				holder.status.setTextColor(ContextCompat.getColor(mContext, R.color.red));
				break;
			case "pending":
				holder.status.setText("در دست بررسی");
				holder.status.setTextColor(ContextCompat.getColor(mContext, R.color.md_orange_600));
				break;
			case "shipped":
				holder.status.setText("ارسال شده");
				holder.status.setTextColor(ContextCompat.getColor(mContext, R.color.md_cyan_700));
				break;
			case "delivered":
				holder.status.setText("تحویل داده شده");
				holder.status.setTextColor(ContextCompat.getColor(mContext, R.color.green));
				break;
			default:
				holder.status.setText("نامشخص");
				holder.status.setTextColor(ContextCompat.getColor(mContext, R.color.purple));
				break;
		}
		
		holder.download.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				File f = new File(Environment.getExternalStorageDirectory(), "HO-Factors");
				if (!f.exists())
					f.mkdirs();
				String url = mContext.getResources().getString(R.string.url_factor, HOST) + order.code + ".pdf";
				final OrderAdapter.DownloadTask downloadTask = new OrderAdapter.DownloadTask(mContext, order.code);
				downloadTask.execute(url);
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
		TextView id, date, stuffs, price, status, hour;
		Button download;
		
		MyViewHolder(View view) {
			super(view);
			id = view.findViewById(R.id.id);
			date = view.findViewById(R.id.date);
			stuffs = view.findViewById(R.id.stuffs);
			price = view.findViewById(R.id.price);
			status = view.findViewById(R.id.status);
			hour = view.findViewById(R.id.hour);
			download = view.findViewById(R.id.download);
		}
	}
	
	private class DownloadTask extends AsyncTask<String, Integer, String> {
		
		private Context context;
		private PowerManager.WakeLock mWakeLock;
		private String code;
		
		private DownloadTask(Context context, String code) {
			this.context = context;
			this.code = code;
		}
		
		@Override
		protected String doInBackground(String... sUrl) {
			InputStream input = null;
			OutputStream output = null;
			HttpURLConnection connection = null;
			try {
				URL url = new URL(sUrl[0]);
				connection = (HttpURLConnection) url.openConnection();
				connection.connect();
				
				// expect HTTP 200 OK, so we don't mistakenly save error report instead of the file
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
					return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
				
				// this will be useful to display download percentage
				// might be -1: server did not report the length
				int fileLength = connection.getContentLength();
				
				// download the file
				input = connection.getInputStream();
				output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/" + "HO-Factors" + "/" + code + ".pdf");
				
				byte data[] = new byte[4096];
				long total = 0;
				int count;
				while ((count = input.read(data)) != -1) {
					// allow canceling with back button
					if (isCancelled()) {
						input.close();
						return null;
					}
					total += count;
					// publishing the progress....
					if (fileLength > 0) // only if total length is known
						publishProgress((int) (total * 100 / fileLength));
					output.write(data, 0, count);
				}
			} catch (Exception e) {
				Crashlytics.logException(e);
				return e.toString();
			} finally {
				try {
					if (output != null)
						output.close();
					if (input != null)
						input.close();
				} catch (IOException e) {
					Crashlytics.logException(e);
				}
				
				if (connection != null)
					connection.disconnect();
			}
			return null;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// take CPU lock to prevent CPU from going off if the user
			// presses the power button during download
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			if (pm != null) {
				mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
				mWakeLock.acquire(10000);
				mProgressDialog.show();
			}
		}
		
		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			// if we get here, length is known, now set indeterminate to false
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setMax(100);
			mProgressDialog.setProgress(progress[0]);
		}
		
		@Override
		protected void onPostExecute(String result) {
			mWakeLock.release();
			mProgressDialog.dismiss();
//            vibrator.vibrate(50);
			if (result != null)
				Helper.MakeToast(context, "خطایی رخ داده است مجددا تلاش کنید", TAGs.ERROR);
			else {
				Helper.MakeToast(context, "فاکتور دانلود شد... در حال بازگشایی", TAGs.SUCCESS);
				File file = new File(Environment.getExternalStorageDirectory() + "/" + "HO-Factors" + "/" + code + ".pdf");
				
				Intent target = new Intent(Intent.ACTION_VIEW);
				target.setDataAndType(Uri.fromFile(file), "application/pdf");
				target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				
				Intent intent = Intent.createChooser(target, "Open File");
				try {
					mContext.startActivity(intent);
				} catch (Exception e) {
					Crashlytics.logException(e);
					Helper.MakeToast(context, "نرم افزار مربوطه پیدا نشد. فاکتور در پوشه " + "HO-Factors" + " ذخیره شده است", TAGs.ERROR, Toast.LENGTH_LONG);
				}
			}
		}
	}
}