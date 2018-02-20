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
import android.util.Log;
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

import helper.Helper;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;

public class Activity_Factor extends Activity {
	Button download, back;
	LottieAnimationView animationView;
	TextView pay_msg, loc_msg;
	String ORDER_CODE;
	long downloadId;
	ProgressDialog mProgressDialog;
	private Vibrator vibrator;
	String folder_main = "HO-Factors";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pay_log);
		
		download = findViewById(R.id.pay_log_download);
		back = findViewById(R.id.pay_log_back);
		animationView = findViewById(R.id.animation_view);
		pay_msg = findViewById(R.id.pay_msg);
		loc_msg = findViewById(R.id.loc_msg);
		
		Intent intent = getIntent();
		ORDER_CODE = intent.getStringExtra("order_code");
		mProgressDialog = new ProgressDialog(Activity_Factor.this);
		mProgressDialog.setMessage("A message");
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setCancelable(true);
		vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		
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
				String url = URLs.factor_URL + ORDER_CODE + ".pdf";
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
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
			mWakeLock.acquire();
			mProgressDialog.show();
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
					Log.e("Factor", "open");
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(file), "application/*");
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
					startActivity(Intent.createChooser(intent, "Open"));
				} catch (Exception e) {
					Log.e("Factor", "Can not open");
					e.printStackTrace();
//                    Helper.MakeToast(context, "نرم افزار مربوطه پیدا نشد. فاکتور در پوشه " + folder_main + " ذخیره شده است", TAGs.ERROR, Toast.LENGTH_LONG);
					Intent intent = new Intent(Intent.ACTION_VIEW);
					Uri mydir = Uri.parse(Environment.getExternalStorageDirectory() + "/HO-Factors/");
					intent.setDataAndType(mydir, "application/*");    // or use */*
					startActivity(Intent.createChooser(intent, "Open"));
				}
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		//App not allowed to go back to Parent activity until correct pin entered. comment following code
		//super.onBackPressed();
	}
}