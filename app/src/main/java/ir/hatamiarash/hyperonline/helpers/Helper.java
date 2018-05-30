/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.hyperonline.libraries.CustomToast;
import ir.hatamiarash.hyperonline.utils.ASCII;
import ir.hatamiarash.hyperonline.utils.TAGs;

public class Helper {
	public static boolean isValidPhone(@NotNull String target) {
		return target.startsWith("09") && target.trim().length() == 11 && target.matches("[0-9]+");
	}
	
	public static boolean isValidPassword(@NotNull String target) {
		return target.length() >= 8 && ASCII.isASCII(target) && target.matches("\\A\\p{ASCII}*\\z");
	}
	
	public static boolean CheckInternet(@NotNull Context context) {
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
	
	public static void MakeToast(Context context, String Message, @NotNull String TAG) {
		if (TAG.equals(TAGs.WARNING))
			CustomToast.custom(context, Message, R.drawable.ic_alert, ContextCompat.getColor(context, R.color.white), ContextCompat.getColor(context, R.color.md_deep_orange_400), Toast.LENGTH_SHORT, true, true).show();
		if (TAG.equals(TAGs.SUCCESS))
			CustomToast.custom(context, Message, R.drawable.ic_success, ContextCompat.getColor(context, R.color.white), ContextCompat.getColor(context, R.color.green), Toast.LENGTH_SHORT, true, true).show();
		if (TAG.equals(TAGs.ERROR))
			CustomToast.custom(context, Message, R.drawable.ic_error, ContextCompat.getColor(context, R.color.white), ContextCompat.getColor(context, R.color.red), Toast.LENGTH_SHORT, true, true).show();
	}
	
	public static void MakeToast(Context context, String Message, @NotNull String TAG, int DURATION) {
		if (TAG.equals(TAGs.WARNING))
			CustomToast.custom(context, Message, R.drawable.ic_alert, ContextCompat.getColor(context, R.color.white), ContextCompat.getColor(context, R.color.md_deep_orange_400), DURATION, true, true).show();
		if (TAG.equals(TAGs.SUCCESS))
			CustomToast.custom(context, Message, R.drawable.ic_success, ContextCompat.getColor(context, R.color.white), ContextCompat.getColor(context, R.color.green), DURATION, true, true).show();
		if (TAG.equals(TAGs.ERROR))
			CustomToast.custom(context, Message, R.drawable.ic_error, ContextCompat.getColor(context, R.color.white), ContextCompat.getColor(context, R.color.red), DURATION, true, true).show();
	}
	
	@NonNull
	public static String CalculatePrice(String price, int off) {
		int temp = Integer.parseInt(price);
		int new_price = temp - (temp * off / 100);
		return String.valueOf(new_price);
	}
	
	public static boolean isAppAvailable(@NotNull Context context, String appName) {
		PackageManager pm = context.getPackageManager();
		try {
			pm.getPackageInfo(appName, PackageManager.GET_ACTIVITIES);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}
	
	public static int dpToPx(@NotNull Context context, int dp) {
		Resources r = context.getResources();
		return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
	}
}