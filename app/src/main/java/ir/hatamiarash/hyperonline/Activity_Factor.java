/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.BindView;
import helper.Helper;
import ir.hatamiarash.utils.TAGs;

public class Activity_Factor extends Activity {
	ProgressDialog mProgressDialog;
	Vibrator vibrator;
	
	@BindView(R.id.pay_log_download)
	Button download;
	@BindView(R.id.pay_log_back)
	Button back;
	@BindView(R.id.animation_view)
	LottieAnimationView animationView;
	@BindView(R.id.pay_msg)
	TextView pay_msg;
	@BindView(R.id.loc_msg)
	TextView loc_msg;
	
	private static String HOST;
	String ORDER_CODE;
	String folder_main = "HO-Factors";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pay_log);
		
		mProgressDialog = new ProgressDialog(Activity_Factor.this);
		mProgressDialog.setMessage("");
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setCancelable(false);
		vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		
		ORDER_CODE = getIntent().getStringExtra("order_code");
		HOST = getResources().getString(R.string.url_host);
		
		pay_msg.setText("از خرید شما متشکریم");
		
		animationView.setAnimation("heart.json");
		animationView.loop(true);
		animationView.playAnimation();
		
		download.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				vibrator.vibrate(50);
				File f = new File(Environment.getExternalStorageDirectory(), folder_main);
				if (!f.exists())
					f.mkdirs();
				String url = getResources().getString(R.string.url_factor, HOST) + ORDER_CODE + ".pdf";
				final DownloadTask downloadTask = new DownloadTask(Activity_Factor.this);
				downloadTask.execute(url);
				back.setVisibility(View.VISIBLE);
				download.setVisibility(View.GONE);
				animationView.setVisibility(View.GONE);
				pay_msg.setVisibility(View.GONE);
				loc_msg.setVisibility(View.GONE);
			}
		});
		
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				vibrator.vibrate(50);
				Intent intent = new Intent(Activity_Factor.this, Activity_Main.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				startActivity(new Intent(getApplicationContext(), Activity_UserOrders.class));
				finish();
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		//App not allowed to go back to Parent activity until correct pin entered. comment following code
		//super.onBackPressed();
	}
	
	private class DownloadTask extends AsyncTask<String, Integer, String> {
		
		private Context context;
		private PowerManager.WakeLock mWakeLock;
		
		private DownloadTask(Context context) {
			this.context = context;
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
				output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/" + folder_main + "/" + ORDER_CODE + ".pdf");
				
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
				return e.toString();
			} finally {
				try {
					if (output != null)
						output.close();
					if (input != null)
						input.close();
				} catch (IOException ignored) {
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
			try {
				PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				assert pm != null;
				mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
				mWakeLock.acquire(2 * 60 * 1000L);
				mProgressDialog.show();
			} catch (NullPointerException ignore) {
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
			vibrator.vibrate(50);
			if (result != null)
				Helper.MakeToast(context, "خطایی رخ داده است مجددا تلاش کنید", TAGs.ERROR);
			else {
				Helper.MakeToast(context, "فاکتور دانلود شد... در حال بازگشایی", TAGs.SUCCESS);
				File file = new File(Environment.getExternalStorageDirectory() + "/" + folder_main + "/" + ORDER_CODE + ".pdf");
				try {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(file), "application/*");
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
					startActivity(Intent.createChooser(intent, "Open"));
				} catch (Exception e) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					Uri mydir = Uri.parse(Environment.getExternalStorageDirectory() + "/HO-Factors/");
					intent.setDataAndType(mydir, "application/*");    // or use */*
					startActivity(Intent.createChooser(intent, "Open"));
				}
			}
		}
	}
}