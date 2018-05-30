package ir.hatamiarash.hyperonline.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {
	static int REQUEST_ID_MULTIPLE_PERMISSIONS = 4611;
	static int REQUEST_ID_SMS_PERMISSIONS = 4612;
	static int REQUEST_ID_CAMERA_PERMISSIONS = 4613;
	
	static public void getAllPermissions(Activity activity, Context context) {
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
	
	static public void getSMSPermission(Activity activity) {
		int sms = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_SMS);
		int sms2 = ContextCompat.checkSelfPermission(activity, Manifest.permission.BROADCAST_SMS);
		List<String> listPermissionsNeeded = new ArrayList<>();
		
		if (sms != PackageManager.PERMISSION_GRANTED)
			listPermissionsNeeded.add(Manifest.permission.READ_SMS);
		
		if (sms2 != PackageManager.PERMISSION_GRANTED)
			listPermissionsNeeded.add(Manifest.permission.BROADCAST_SMS);
		
		if (!listPermissionsNeeded.isEmpty())
			ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray
					(new String[listPermissionsNeeded.size()]), REQUEST_ID_SMS_PERMISSIONS);
	}
	
	static public void getCameraPermission(Activity activity) {
		int camera = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
		List<String> listPermissionsNeeded = new ArrayList<>();
		
		if (camera != PackageManager.PERMISSION_GRANTED)
			listPermissionsNeeded.add(Manifest.permission.CAMERA);
		
		
		if (!listPermissionsNeeded.isEmpty())
			ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray
					(new String[listPermissionsNeeded.size()]), REQUEST_ID_CAMERA_PERMISSIONS);
	}
	
	static public boolean checkSMSPermission(Activity activity) {
		int sms = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_SMS);
		int sms2 = ContextCompat.checkSelfPermission(activity, Manifest.permission.BROADCAST_SMS);
		int count = 0;
		if (sms != PackageManager.PERMISSION_GRANTED) count++;
		if (sms2 != PackageManager.PERMISSION_GRANTED) count++;
		return count == 2;
	}
	
	static public boolean checkCameraPermission(Activity activity) {
		int camera = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
		return camera == PackageManager.PERMISSION_GRANTED;
	}
}
