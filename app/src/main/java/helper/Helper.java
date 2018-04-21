/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ir.hatamiarash.MyToast.CustomToast;
import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.utils.ASCII;
import ir.hatamiarash.utils.TAGs;

public class Helper {
	static {
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
	}
	
	public static boolean isValidPhone(String target) {
		return target.startsWith("09") && target.trim().length() == 11 && target.matches("[0-9]+");
	}
	
	public static boolean isValidPassword(String target) {
		return target.length() >= 8 && ASCII.isASCII(target) && target.matches("\\A\\p{ASCII}*\\z");
	}
	
	public static boolean CheckInternet(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		PackageManager PM = context.getPackageManager();
		if (PM.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
			if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
					connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
				return true;
		} else {
			if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
				return true;
		}
		Helper.MakeToast(context, "اتصال به اینترنت را بررسی نمایید", TAGs.WARNING);
		return false;
	}
	
	public static boolean CheckInternet2(Context context) {
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			PackageManager PM = context.getPackageManager();
			if (PM.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
				return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
						connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
			} else {
				return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
			}
		} catch (NullPointerException ignore) {
			return false;
		}
	}
	
	public static void MakeToast(Context context, String Message, String TAG) {
		if (TAG.equals(TAGs.WARNING))
			CustomToast.custom(context, Message, R.drawable.ic_alert, ContextCompat.getColor(context, R.color.white), ContextCompat.getColor(context, R.color.md_deep_orange_400), Toast.LENGTH_SHORT, true, true).show();
		if (TAG.equals(TAGs.SUCCESS))
			CustomToast.custom(context, Message, R.drawable.ic_success, ContextCompat.getColor(context, R.color.white), ContextCompat.getColor(context, R.color.green), Toast.LENGTH_SHORT, true, true).show();
		if (TAG.equals(TAGs.ERROR))
			CustomToast.custom(context, Message, R.drawable.ic_error, ContextCompat.getColor(context, R.color.white), ContextCompat.getColor(context, R.color.red), Toast.LENGTH_SHORT, true, true).show();
	}
	
	public static void MakeToast(Context context, String Message, String TAG, int DURATION) {
		if (TAG.equals(TAGs.WARNING))
			CustomToast.custom(context, Message, R.drawable.ic_alert, ContextCompat.getColor(context, R.color.white), ContextCompat.getColor(context, R.color.md_deep_orange_400), DURATION, true, true).show();
		if (TAG.equals(TAGs.SUCCESS))
			CustomToast.custom(context, Message, R.drawable.ic_success, ContextCompat.getColor(context, R.color.white), ContextCompat.getColor(context, R.color.green), DURATION, true, true).show();
		if (TAG.equals(TAGs.ERROR))
			CustomToast.custom(context, Message, R.drawable.ic_error, ContextCompat.getColor(context, R.color.white), ContextCompat.getColor(context, R.color.red), DURATION, true, true).show();
	}
	
	static public void GetPermissions(Activity activity, Context context) {
		final int REQUEST_ID_MULTIPLE_PERMISSIONS = 4611;
		
		int location2 = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION);
		int location = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION);
		int network = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE);
		int bluetooth = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH);
		int internet = ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET);
		int vibration = ContextCompat.checkSelfPermission(context, Manifest.permission.VIBRATE);
		int read_storage = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
		int write_storage = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		
		List<String> listPermissionsNeeded = new ArrayList<>();
		
		if (location != PackageManager.PERMISSION_GRANTED)
			listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
		if (location2 != PackageManager.PERMISSION_GRANTED)
			listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
		if (network != PackageManager.PERMISSION_GRANTED)
			listPermissionsNeeded.add(Manifest.permission.ACCESS_NETWORK_STATE);
		if (bluetooth != PackageManager.PERMISSION_GRANTED)
			listPermissionsNeeded.add(Manifest.permission.BLUETOOTH);
		if (internet != PackageManager.PERMISSION_GRANTED)
			listPermissionsNeeded.add(Manifest.permission.INTERNET);
		if (vibration != PackageManager.PERMISSION_GRANTED)
			listPermissionsNeeded.add(Manifest.permission.VIBRATE);
		if (read_storage != PackageManager.PERMISSION_GRANTED)
			listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
		if (write_storage != PackageManager.PERMISSION_GRANTED)
			listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		
		if (!listPermissionsNeeded.isEmpty())
			ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray
					(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
	}
	
	@NonNull
	public static String CalculatePrice(String price, int off) {
		int temp = Integer.parseInt(price);
		int new_price = temp - (temp * off / 100);
		return String.valueOf(new_price);
	}
	
	public static boolean isAppAvailable(Context context, String appName) {
		PackageManager pm = context.getPackageManager();
		try {
			pm.getPackageInfo(appName, PackageManager.GET_ACTIVITIES);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}
}