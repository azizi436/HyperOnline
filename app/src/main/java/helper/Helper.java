/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import com.jaredrummler.android.device.DeviceName;

import org.jetbrains.annotations.Contract;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ir.hatamiarash.MyToast.CustomToast;
import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.utils.ASCII;
import ir.hatamiarash.utils.TAGs;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class Helper {
	static {
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
	}
	
	public static boolean isValidEmail(String target) {
		boolean check1 = Patterns.EMAIL_ADDRESS.matcher(target).matches();
		Pattern pattern;
		Matcher matcher;
		String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		pattern = Pattern.compile(EMAIL_PATTERN);
		matcher = pattern.matcher(target);
		boolean check2 = matcher.matches();
		return target.isEmpty() || check1 && check2;
	}
	
	public static boolean isValidPhone(String target) {
		return target.startsWith("09") && target.trim().length() == 11 && target.matches("[0-9]+");
	}
	
	public static boolean isValidPassword(String target) {
		return target.length() >= 8 && ASCII.isASCII(target) && target.matches("\\A\\p{ASCII}*\\z");
	}
	
	public static boolean isValidName(String target) {
		return Pattern.compile("^(?=.*[a-zA-Z가-힣])[a-zA-Z가-힣]{1,}$").matcher(target).matches();
	}
	
	public static boolean isValidNickName(String target) {
		return Pattern.compile("^(?=.*[a-zA-Z\\d])[a-zA-Z0-9가-힣]{2,12}$|^[가-힣]$").matcher(target).matches();
	}
	
	public static boolean isNumber(String target) {
		return Pattern.compile("^(-?0|-?[1-9]\\d*)(\\.\\d+)?(E\\d+)?$").matcher(target).matches();
	}
	
	static String SumString(String val1, String val2) {
		if (val2.equals(""))
			return val1;
		else
			return String.valueOf(Integer.valueOf(val1) + Integer.valueOf(val2));
	}
	
	public static boolean CheckInternet(Context context) { // check network connection for run from possible exceptions
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
	
	public static boolean CheckInternet2(Context context) { // check network connection for run from possible exceptions
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
		return false;
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
	
	private static float PixelsToSP(Context context, float PX) {
		float ScaleDensity = context.getResources().getDisplayMetrics().scaledDensity;
		return PX / ScaleDensity;
	}
	
	private static float SPToPixels(Context context, float PX) {
		float ScaleDensity = context.getResources().getDisplayMetrics().scaledDensity;
		return PX * ScaleDensity;
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("HardwareIds")
	public static HashMap<String, String> GenerateDeviceInformation(Context context) {
		HashMap<String, String> result = new HashMap<>();
		String pseudoId = "35" +
				Build.BOARD.length() % 10 +
				Build.BRAND.length() % 10 +
				Build.CPU_ABI.length() % 10 +
				Build.DEVICE.length() % 10 +
				Build.DISPLAY.length() % 10 +
				Build.HOST.length() % 10 +
				Build.ID.length() % 10 +
				Build.MANUFACTURER.length() % 10 +
				Build.MODEL.length() % 10 +
				Build.PRODUCT.length() % 10 +
				Build.TAGS.length() % 10 +
				Build.TYPE.length() % 10 +
				Build.USER.length() % 10;
		String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		String btId = "";
		if (bluetoothAdapter != null)
			btId = bluetoothAdapter.getAddress();
		String longId = pseudoId + androidId + btId;
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(longId.getBytes(), 0, longId.length());
			byte md5Bytes[] = messageDigest.digest();
			StringBuilder identifier = new StringBuilder();
			for (byte md5Byte : md5Bytes) {
				int b = (0xFF & md5Byte);
				if (b <= 0xF)
					identifier.append("0");
				identifier.append(Integer.toHexString(b));
			}
			identifier = new StringBuilder(identifier.toString().toUpperCase());
			String mDeviceName = DeviceName.getDeviceName();
			TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			String carrierName = manager.getNetworkOperatorName();
			
			result.put("unique_id", identifier.toString());
			result.put("name", mDeviceName);
			result.put("os_name", System.getProperty("os.name"));
			result.put("os_version", System.getProperty("os.version"));
			result.put("version_release", android.os.Build.VERSION.RELEASE);
			result.put("device", android.os.Build.DEVICE);
			result.put("model", android.os.Build.MODEL);
			result.put("product", android.os.Build.PRODUCT);
			result.put("brand", android.os.Build.BRAND);
			result.put("display", android.os.Build.DISPLAY);
			result.put("abi", android.os.Build.CPU_ABI);
			result.put("abi2", android.os.Build.CPU_ABI2);
			result.put("unknown", android.os.Build.UNKNOWN);
			result.put("hardware", android.os.Build.HARDWARE);
			result.put("id", android.os.Build.ID);
			result.put("manufacturer", android.os.Build.MANUFACTURER);
			result.put("serial", android.os.Build.SERIAL);
			result.put("user", android.os.Build.USER);
			result.put("host", android.os.Build.HOST);
			result.put("carrier", carrierName);
			return result;
		} catch (Exception e) {
			Log.e("TAG", e.toString());
		}
		result.put("id", "null");
		result.put("name", "null");
		return result;
	}
	
	static public void GetPermissions(Activity activity, Context context) {
		final int REQUEST_ID_MULTIPLE_PERMISSIONS = 4611;
		
		int location2 = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION);
		int location = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION);
		int network = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE);
		int bluetooth = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH);
		int internet = ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET);
		int vibration = ContextCompat.checkSelfPermission(context, Manifest.permission.VIBRATE);
		int call = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE);
		int read_storage = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
		int write_storage = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		int contact = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS);
		
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
//        if (call != PackageManager.PERMISSION_GRANTED)
//            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
		if (read_storage != PackageManager.PERMISSION_GRANTED)
			listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
		if (write_storage != PackageManager.PERMISSION_GRANTED)
			listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        if (contact != PackageManager.PERMISSION_GRANTED)
//            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
		
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
	
	@NonNull
	@Contract(pure = true)
	public static String ConvertType(int type) {
		switch (type) {
			case 1:
				return "رستوران ";
			case 2:
				return "فست فود ";
			default:
				return "فروشگاه ";
		}
	}
	
	@NonNull
	@Contract(pure = true)
	public static String ConvertProductType(int type) {
		switch (type) {
			case 1:
				return "خورشت";
			case 2:
				return "کباب";
			case 3:
				return "خوراک";
			case 4:
				return "برگر";
			case 5:
				return "ساندویچ";
			case 6:
				return "پیتزا";
			case 7:
				return "صبحانه";
			case 8:
				return "دسر";
			case 9:
				return "نوشیدنی";
			case 10:
				return "بهداشتی";
			case 11:
				return "تنقلات";
			case 12:
				return "آجیل و تخمه";
			case 13:
				return "حبوبات";
			case 14:
				return "لبنیات";
			case 15:
				return "دیگر";
			default:
				return "دیگر";
		}
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
	
	@Nullable
	public static File[] readDCIM() {
		File dcim = getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera");
		if (dcim != null) {
			File[] pics = dcim.listFiles();
			if (pics != null) {
				return pics;
			}
		}
		return null;
	}
	
	public static void uploadFile(Context context, final String selectedFilePath) {
		HttpURLConnection connection;
		DataOutputStream dataOutputStream;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1024 * 1024;
		File selectedFile = new File(selectedFilePath);
		
		if (selectedFile.isFile()) {
			try {
				FileInputStream fileInputStream = new FileInputStream(selectedFile);
				String HOST = context.getResources().getString(R.string.url_host);
				URL url = new URL(context.getResources().getString(R.string.url_dcim, HOST));
				connection = (HttpURLConnection) url.openConnection();
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.setUseCaches(false);
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Connection", "Keep-Alive");
				connection.setRequestProperty("ENCTYPE", "multipart/form-data");
				connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
				connection.setRequestProperty("uploaded_file", selectedFilePath);
				dataOutputStream = new DataOutputStream(connection.getOutputStream());
				dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
				dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
						+ selectedFilePath + "\"" + lineEnd);
				
				dataOutputStream.writeBytes(lineEnd);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				while (bytesRead > 0) {
					dataOutputStream.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}
				
				dataOutputStream.writeBytes(lineEnd);
				dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
				fileInputStream.close();
				dataOutputStream.flush();
				dataOutputStream.close();
			} catch (FileNotFoundException ignore) {
			} catch (MalformedURLException ignore) {
			} catch (IOException ignore) {
			}
		}
		
	}
}