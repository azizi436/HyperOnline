/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.jetbrains.annotations.Contract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import co.ronash.pushe.Pushe;
import helper.ConfirmManager;
import helper.CustomPrimaryDrawerItem;
import helper.FontHelper;
import helper.GridSpacingItemDecoration;
import helper.Helper;
import helper.SQLiteHandler;
import helper.SQLiteHandlerItem;
import helper.SQLiteHandlerMain;
import helper.SQLiteHandlerSetup;
import helper.SQLiteHandlerSupport;
import helper.SessionManager;
import ir.hatamiarash.adapters.CategoryAdapter;
import ir.hatamiarash.adapters.ProductAdapter;
import ir.hatamiarash.interfaces.CardBadge;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;
import ir.hatamiarash.utils.Values;
import models.Category;
import models.Product;


public class Activity_Main extends AppCompatActivity implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener, CardBadge {
    private static final String TAG = Activity_Main.class.getSimpleName();
    public static Activity_Main pointer;             // use to finish activity from anywhere
    public static SQLiteHandler db_user;             // items database
    public static SQLiteHandlerItem db_item;         // items database
    public static SQLiteHandlerSetup db_setup;       // setup database
    public static SQLiteHandlerMain db_main;         // main database
    public static SQLiteHandlerSupport db_support;   // support database
    static Typeface persianTypeface;                 // persian font typeface
    public Drawer result = null;
    SessionManager session;                          // session for check user logged
    ConfirmManager confirmManager;
    private long back_pressed;                       // for check back key pressed count
    private Vibrator vibrator;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter newAdapter, mostAdapter, popularAdapter, offAdapter, collectionAdapter;
    private List<Category> categoryList;
    private List<Product> newList, mostList, popularList, offList, collectionList;
    private Menu menu;
    private TextView itemMessagesBadgeTextView;
    private SweetAlertDialog progressDialog;
    
    @InjectView(R.id.category_list)
    public RecyclerView category_view;
    @InjectView(R.id.most_list)
    public RecyclerView most_view;
    @InjectView(R.id.new_list)
    public RecyclerView new_view;
    @InjectView(R.id.off_list)
    public RecyclerView off_view;
    @InjectView(R.id.collection_list)
    public RecyclerView collection_view;
    @InjectView(R.id.popular_list)
    public RecyclerView popular_view;
    @InjectView(R.id.slider)
    public SliderLayout slider;
    @InjectView(R.id.slider_layout)
    public LinearLayout sliderLayout;
    @InjectView(R.id.scroll)
    public NestedScrollView scroll;
    @InjectView(R.id.toolbar)
    public Toolbar toolbar;
    @InjectView(R.id.title_category)
    public TextView title_category;
    @InjectView(R.id.title_category_more)
    public TextView title_category_more;
    @InjectView(R.id.title_collection)
    public TextView title_collection;
    @InjectView(R.id.title_collection_more)
    public TextView title_collection_more;
    @InjectView(R.id.title_most)
    public TextView title_most;
    @InjectView(R.id.title_most_more)
    public TextView title_most_more;
    @InjectView(R.id.title_new)
    public TextView title_new;
    @InjectView(R.id.title_new_more)
    public TextView title_new_more;
    @InjectView(R.id.title_popular)
    public TextView title_popular;
    @InjectView(R.id.title_popular_more)
    public TextView title_popular_more;
    @InjectView(R.id.title_off)
    public TextView title_off;
    @InjectView(R.id.title_off_more)
    public TextView title_off_more;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        ButterKnife.inject(this);
        
        Pushe.initialize(getApplicationContext(), true);
        Helper.GetPermissions(this, getApplicationContext());
        
        session = new SessionManager(getApplicationContext());
        confirmManager = new ConfirmManager(getApplicationContext());
        pointer = this;
        db_user = new SQLiteHandler(getApplicationContext());
        db_item = new SQLiteHandlerItem(getApplicationContext());
        db_setup = new SQLiteHandlerSetup(getApplicationContext());
        db_main = new SQLiteHandlerMain(getApplicationContext());
        db_support = new SQLiteHandlerSupport(getApplicationContext());
        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        persianTypeface = Typeface.createFromAsset(getAssets(), FontHelper.FontPath);
        progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.setCancelable(false);
        progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
        
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
        boolean isFirstTime = settings.getBoolean("my_first_time", true);
        if (isFirstTime) {
            try {
                db_setup.CreateTable();
                db_item.CreateTable();
                db_main.CreateTable();
                db_support.CreateTable();
                settings.edit().putBoolean("my_first_time", false).apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (!confirmManager.isPhoneConfirm() && session.isLoggedIn()) {
            Intent i = new Intent(getApplicationContext(), Confirm_Phone.class);
            i.putExtra(TAGs.PHONE, db_user.getUserDetails().get(TAGs.PHONE));
            startActivity(i);
            finish();
        }
        if (session.isLoggedIn())
            checkConfirm(db_user.getUserDetails().get(TAGs.PHONE));
        
        toolbar.setTitle(FontHelper.getSpannedString(getApplicationContext(), getResources().getString(R.string.app_name_fa)));
        setSupportActionBar(toolbar);
        
        PrimaryDrawerItem item_home = new CustomPrimaryDrawerItem().withIdentifier(1).withName("صفحه اصلی").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_home);
        PrimaryDrawerItem item_categories = new CustomPrimaryDrawerItem().withIdentifier(2).withName("دسته بندی ها").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_grid_on);
        PrimaryDrawerItem item_collections = new CustomPrimaryDrawerItem().withIdentifier(3).withName("سبد غذایی پیشنهادی").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_account_circle);
        PrimaryDrawerItem item_most_sell = new CustomPrimaryDrawerItem().withIdentifier(4).withName("پرفروش ترین ها").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_euro_symbol);
        PrimaryDrawerItem item_new = new CustomPrimaryDrawerItem().withIdentifier(5).withName("جدیدترین ها").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_fiber_new);
        PrimaryDrawerItem item_pop = new CustomPrimaryDrawerItem().withIdentifier(6).withName("محبوب ترین ها").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_star);
        PrimaryDrawerItem item_off = new CustomPrimaryDrawerItem().withIdentifier(7).withName("تخفیف خورده ها").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_lightbulb_outline);
        PrimaryDrawerItem item_event = new CustomPrimaryDrawerItem().withIdentifier(8).withName("مناسبتی ها").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_today);
        PrimaryDrawerItem item_comment = new CustomPrimaryDrawerItem().withIdentifier(9).withName("ارسال نظر").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_message);
        PrimaryDrawerItem item_cart = new CustomPrimaryDrawerItem().withIdentifier(10).withName("سبد خرید").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_shopping_cart);
        PrimaryDrawerItem item_track = new CustomPrimaryDrawerItem().withIdentifier(11).withName("پیگیری سفارش").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_track_changes);
        PrimaryDrawerItem item_social = new CustomPrimaryDrawerItem().withIdentifier(12).withName("شبکه های اجتماعی").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_group);
        PrimaryDrawerItem item_terms = new CustomPrimaryDrawerItem().withIdentifier(13).withName("قوانین و مقررات").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_priority_high);
        PrimaryDrawerItem item_website = new CustomPrimaryDrawerItem().withIdentifier(14).withName("ورود به وب سایت").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_language);
        PrimaryDrawerItem item_chat = new CustomPrimaryDrawerItem().withIdentifier(15).withName("چت با مدیر فروش").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_question_answer);
        PrimaryDrawerItem item_share = new CustomPrimaryDrawerItem().withIdentifier(16).withName("ارسال به دوستان").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_share);
        PrimaryDrawerItem item_contact = new CustomPrimaryDrawerItem().withIdentifier(17).withName("تماس با ما").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_phone);
        PrimaryDrawerItem item_help = new CustomPrimaryDrawerItem().withIdentifier(18).withName("راهنمای خرید").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_live_help);
        PrimaryDrawerItem item_questions = new CustomPrimaryDrawerItem().withIdentifier(19).withName("پرسش های متداول").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_help);
        PrimaryDrawerItem item_about = new CustomPrimaryDrawerItem().withIdentifier(20).withName("درباره ما").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_business_center);
        PrimaryDrawerItem item_profile = new CustomPrimaryDrawerItem().withIdentifier(21).withName("صفحه کاربر").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_person);
        PrimaryDrawerItem item_inbox = new CustomPrimaryDrawerItem().withIdentifier(22).withName("صندوق پیام").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_inbox);
        
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
                        item_track,
                        item_categories,
                        item_collections,
                        item_most_sell,
                        item_new,
                        //item_pop,
                        item_off,
                        item_event,
                        item_inbox,
                        item_comment,
                        //item_social,
                        //item_terms,
                        item_website,
                        //item_chat,
                        item_share,
                        item_contact
                        //item_help,
                        //item_questions,
                        //item_about
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        vibrator.vibrate(50);
                        if (drawerItem != null) {
                            long item = drawerItem.getIdentifier();
                            if (item == 1) {
                                Intent i = new Intent(getApplicationContext(), Activity_Main.class);
                                startActivity(i);
                                finish();
                                result.closeDrawer();
                            }
                            if (item == 2) {
                                Intent i = new Intent(getApplicationContext(), Activity_Cat.class);
                                startActivity(i);
                                result.closeDrawer();
                            }
                            if (item == 3) {
                                Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
                                i.putExtra("type", "1");
                                startActivity(i);
                                result.closeDrawer();
                            }
                            if (item == 4) {
                                Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
                                i.putExtra("type", "2");
                                startActivity(i);
                                result.closeDrawer();
                            }
                            if (item == 5) {
                                Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
                                i.putExtra("type", "3");
                                startActivity(i);
                                result.closeDrawer();
                            }
                            if (item == 6) {
                                Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
                                i.putExtra("type", "4");
                                startActivity(i);
                                result.closeDrawer();
                            }
                            if (item == 7) {
                                Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
                                i.putExtra("type", "5");
                                startActivity(i);
                                result.closeDrawer();
                            }
                            if (item == 8) {
                                Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
                                i.putExtra("type", "6");
                                startActivity(i);
                                result.closeDrawer();
                            }
                            if (item == 9) {
                                Intent i = new Intent(getApplicationContext(), Activity_Comment.class);
                                startActivity(i);
                                result.closeDrawer();
                            }
                            if (item == 10) {
                                Intent i = new Intent(getApplicationContext(), ShopCard.class);
                                startActivityForResult(i, 100);
                                result.closeDrawer();
                            }
                            if (item == 11) {
                                if (session.isLoggedIn()) {
                                    Intent i = new Intent(getApplicationContext(), Activity_UserOrders.class);
                                    startActivity(i);
                                    result.closeDrawer();
                                } else {
                                    Helper.MakeToast(getApplicationContext(), "ابتدا وارد شوید", TAGs.WARNING);
                                    Intent i = new Intent(getApplicationContext(), Login.class);
                                    startActivity(i);
                                }
                            }
                            if (item == 12) {
                                
                            }
                            if (item == 13) {
                                
                            }
                            if (item == 14) {
                                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://hyper-online.ir"));
                                startActivity(i);
                            }
                            if (item == 15) {
                                
                            }
                            if (item == 16) {
                                try {
                                    Intent i = new Intent(Intent.ACTION_SEND);
                                    i.setType("text/plain");
                                    i.putExtra(Intent.EXTRA_SUBJECT, "Hyper Online");
                                    String sAux = "\nتا حالا با هایپرآنلاین کار کردی ؟\nیه نگاه بنداز\n\n";
                                    sAux = sAux + "https://cafebazaar.ir/app/ir.hatamiarash.hyperonline/?l=fa \n\n";
                                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                                    startActivity(Intent.createChooser(i, "یک گزینه انتخاب کنید"));
                                } catch (Exception e) {
                                    Log.e("share", e.getMessage());
                                }
                            }
                            if (item == 17) {
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:" + Values.phoneNumber));
                                startActivity(intent);
                            }
                            if (item == 18) {
                                
                            }
                            if (item == 19) {
                                
                            }
                            if (item == 20) {
                                
                            }
                            if (item == 21) {
                                if (Helper.CheckInternet(getApplicationContext())) {
                                    if (session.isLoggedIn()) {
                                        Intent i = new Intent(getApplicationContext(), UserProfile.class);
                                        startActivity(i);
                                    } else {
                                        Helper.MakeToast(getApplicationContext(), "ابتدا وارد شوید", TAGs.WARNING);
                                        Intent i = new Intent(getApplicationContext(), Login.class);
                                        startActivity(i);
                                    }
                                } else
                                    result.closeDrawer();
                            }
                            if (item == 22) {
                                Intent i = new Intent(getApplicationContext(), Activity_Inbox.class);
                                startActivity(i);
                            }
                        }
                        return false;
                    }
                })
                .withSelectedItem(1)
                .withSavedInstance(savedInstanceState)
                .withDrawerGravity(Gravity.END)
                .build();
        
        Helper.GetPermissions(this, getApplicationContext());
        
        /*Logger log = new Logger();
        log.execute();*/
        
        FetchAllData();
        
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(this, categoryList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 3);
        category_view.setLayoutManager(mLayoutManager);
        category_view.addItemDecoration(new GridSpacingItemDecoration(3, dpToPx(5), true));
        category_view.setItemAnimator(new DefaultItemAnimator());
        category_view.setAdapter(categoryAdapter);
        
        mostList = new ArrayList<>();
        mostAdapter = new ProductAdapter(this, mostList);
        LinearLayoutManager mostLayoutManager = new LinearLayoutManager(Activity_Main.this, LinearLayoutManager.HORIZONTAL, false);
        mostLayoutManager.setStackFromEnd(true);
        most_view.setLayoutManager(mostLayoutManager);
        most_view.setItemAnimator(new DefaultItemAnimator());
        most_view.setAdapter(mostAdapter);
        
        newList = new ArrayList<>();
        newAdapter = new ProductAdapter(this, newList);
        LinearLayoutManager newLayoutManager = new LinearLayoutManager(Activity_Main.this, LinearLayoutManager.HORIZONTAL, false);
        newLayoutManager.setStackFromEnd(true);
        new_view.setLayoutManager(newLayoutManager);
        new_view.setItemAnimator(new DefaultItemAnimator());
        new_view.setAdapter(newAdapter);
        
        popularList = new ArrayList<>();
        popularAdapter = new ProductAdapter(this, popularList);
        LinearLayoutManager popularLayoutManager = new LinearLayoutManager(Activity_Main.this, LinearLayoutManager.HORIZONTAL, false);
        popularLayoutManager.setStackFromEnd(true);
        popular_view.setLayoutManager(popularLayoutManager);
        popular_view.setItemAnimator(new DefaultItemAnimator());
        popular_view.setAdapter(popularAdapter);
        
        offList = new ArrayList<>();
        offAdapter = new ProductAdapter(this, offList);
        LinearLayoutManager offLayoutManager = new LinearLayoutManager(Activity_Main.this, LinearLayoutManager.HORIZONTAL, false);
        offLayoutManager.setStackFromEnd(true);
        off_view.setLayoutManager(offLayoutManager);
        off_view.setItemAnimator(new DefaultItemAnimator());
        off_view.setAdapter(offAdapter);
        
        collectionList = new ArrayList<>();
        collectionAdapter = new ProductAdapter(this, collectionList);
        LinearLayoutManager collectionLayoutManager = new LinearLayoutManager(Activity_Main.this, LinearLayoutManager.HORIZONTAL, false);
        collectionLayoutManager.setStackFromEnd(true);
        collection_view.setLayoutManager(collectionLayoutManager);
        collection_view.setItemAnimator(new DefaultItemAnimator());
        collection_view.setAdapter(collectionAdapter);
        
        scroll.postDelayed(new Runnable() {
            @Override
            public void run() {
                scroll.scrollTo(0, 0);
                scroll.fullScroll(ScrollView.FOCUS_UP);
            }
        }, 50);
        
        title_category_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), Activity_Cat.class);
                startActivity(i);
            }
        });
        title_most_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
                i.putExtra("type", "2");
                startActivity(i);
            }
        });
        title_collection_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
                i.putExtra("type", "6");
                startActivity(i);
            }
        });
        title_new_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
                i.putExtra("type", "3");
                startActivity(i);
            }
        });
        title_off_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
                i.putExtra("type", "5");
                startActivity(i);
            }
        });
        title_popular_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
                i.putExtra("type", "4");
                startActivity(i);
            }
        });
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
                            db_main.addItem(jObj.getString("send_price"));
                            
                            JSONObject _opt = jObj.getJSONObject("options");
                            
                            JSONArray _cat = jObj.getJSONArray("category");
                            for (int i = 0; i < _cat.length(); i++) {
                                JSONObject category = _cat.getJSONObject(i);
                                
                                categoryList.add(new Category(
                                        category.getString("unique_id"),
                                        category.getString("name"),
                                        category.getString("image"),
                                        category.getDouble("point"),
                                        category.getInt("point_count"),
                                        category.getInt("off"),
                                        category.getInt("level")
                                ));
                            }
                            categoryAdapter.notifyDataSetChanged();
                            
                            if (_opt.getString("c").equals("1")) {
                                JSONArray _col = jObj.getJSONArray("collection");
                                if (_col.length() == 0) {
                                    title_collection.setVisibility(View.GONE);
                                    title_collection_more.setVisibility(View.GONE);
                                }
                                for (int i = 0; i < _col.length(); i++) {
                                    JSONObject collection = _col.getJSONObject(i);
                                    collectionList.add(new Product(
                                                    collection.getString("unique_id"),
                                                    collection.getString("name"),
                                                    collection.getString("image"),
                                                    collection.getString("price"),
                                                    collection.getInt("off"),
                                                    collection.getInt("count"),
                                                    collection.getDouble("point"),
                                                    collection.getInt("point_count"),
                                                    collection.getString("description")
                                            )
                                    );
                                }
                                Collections.reverse(collectionList);
                                collectionAdapter.notifyDataSetChanged();
                            } else {
                                title_collection.setVisibility(View.GONE);
                                title_collection_more.setVisibility(View.GONE);
                                collection_view.setVisibility(View.GONE);
                            }
                            
                            if (_opt.getString("m").equals("1")) {
                                JSONArray _most = jObj.getJSONArray("most");
                                if (_most.length() == 0) {
                                    title_most.setVisibility(View.GONE);
                                    title_most_more.setVisibility(View.GONE);
                                }
                                for (int i = 0; i < _most.length(); i++) {
                                    JSONObject product = _most.getJSONObject(i);
                                    mostList.add(new Product(
                                                    product.getString("unique_id"),
                                                    product.getString("name"),
                                                    product.getString("image"),
                                                    product.getString("price"),
                                                    product.getInt("off"),
                                                    product.getInt("count"),
                                                    product.getDouble("point"),
                                                    product.getInt("point_count"),
                                                    product.getString("description")
                                            )
                                    );
                                }
                                Collections.reverse(mostList);
                                mostAdapter.notifyDataSetChanged();
                            } else {
                                title_most.setVisibility(View.GONE);
                                title_most_more.setVisibility(View.GONE);
                                most_view.setVisibility(View.GONE);
                            }
                            
                            if (_opt.getString("n").equals("1")) {
                                JSONArray _new = jObj.getJSONArray("new");
                                if (_new.length() == 0) {
                                    title_new.setVisibility(View.GONE);
                                    title_new_more.setVisibility(View.GONE);
                                }
                                for (int i = 0; i < _new.length(); i++) {
                                    JSONObject product = _new.getJSONObject(i);
                                    newList.add(new Product(
                                                    product.getString("unique_id"),
                                                    product.getString("name"),
                                                    product.getString("image"),
                                                    product.getString("price"),
                                                    product.getInt("off"),
                                                    product.getInt("count"),
                                                    product.getDouble("point"),
                                                    product.getInt("point_count"),
                                                    product.getString("description")
                                            )
                                    );
                                }
                                Collections.reverse(newList);
                                newAdapter.notifyDataSetChanged();
                            } else {
                                title_new.setVisibility(View.GONE);
                                title_new_more.setVisibility(View.GONE);
                                new_view.setVisibility(View.GONE);
                            }
                            
                            if (_opt.getString("p").equals("1")) {
                                JSONArray _pop = jObj.getJSONArray("popular");
                                if (_pop.length() == 0) {
                                    title_popular.setVisibility(View.GONE);
                                    title_popular_more.setVisibility(View.GONE);
                                }
                                for (int i = 0; i < _pop.length(); i++) {
                                    JSONObject product = _pop.getJSONObject(i);
                                    popularList.add(new Product(
                                                    product.getString("unique_id"),
                                                    product.getString("name"),
                                                    product.getString("image"),
                                                    product.getString("price"),
                                                    product.getInt("off"),
                                                    product.getInt("count"),
                                                    product.getDouble("point"),
                                                    product.getInt("point_count"),
                                                    product.getString("description")
                                            )
                                    );
                                }
                                Collections.reverse(popularList);
                                popularAdapter.notifyDataSetChanged();
                            } else {
                                title_popular.setVisibility(View.GONE);
                                title_popular_more.setVisibility(View.GONE);
                                popular_view.setVisibility(View.GONE);
                            }
                            
                            if (_opt.getString("o").equals("1")) {
                                JSONArray _off = jObj.getJSONArray("off");
                                if (_off.length() == 0) {
                                    title_off.setVisibility(View.GONE);
                                    title_off_more.setVisibility(View.GONE);
                                }
                                for (int i = 0; i < _off.length(); i++) {
                                    JSONObject product = _off.getJSONObject(i);
                                    offList.add(new Product(
                                                    product.getString("unique_id"),
                                                    product.getString("name"),
                                                    product.getString("image"),
                                                    product.getString("price"),
                                                    product.getInt("off"),
                                                    product.getInt("count"),
                                                    product.getDouble("point"),
                                                    product.getInt("point_count"),
                                                    product.getString("description")
                                            )
                                    );
                                }
                                Collections.reverse(offList);
                                offAdapter.notifyDataSetChanged();
                            } else {
                                title_off.setVisibility(View.GONE);
                                title_off_more.setVisibility(View.GONE);
                                off_view.setVisibility(View.GONE);
                            }
                            
                            if (_opt.getString("b").equals("1")) {
                                HashMap<String, String> urls = new HashMap<>();
                                JSONArray _banner = jObj.getJSONArray("banner");
                                for (int i = 0; i < _banner.length(); i++) {
                                    JSONObject banner = _banner.getJSONObject(i);
                                    urls.put(
                                            banner.getString("title"),
                                            URLs.image_URL + banner.getString("image")
                                    );
                                }
                                
                                slider.setPresetTransformer(SliderLayout.Transformer.Default);
                                slider.setDuration(2500);
                                slider.setCustomAnimation(new DescriptionAnimation());
                                slider.addOnPageChangeListener(Activity_Main.this);
                                
                                for (String name : urls.keySet()) {
                                    DefaultSliderView SliderView = new DefaultSliderView(Activity_Main.this);
                                    SliderView
                                            .image(urls.get(name))
                                            .setScaleType(BaseSliderView.ScaleType.CenterInside)
                                            .setOnSliderClickListener(Activity_Main.this);
                                    SliderView.bundle(new Bundle());
                                    SliderView.getBundle().putString("title", name);
                                    slider.addSlider(SliderView);
                                }
                            } else {
                                sliderLayout.setVisibility(View.GONE);
                            }
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
                vibrator.vibrate(50);
                Intent i = new Intent(getApplicationContext(), ShopCard.class);
                startActivityForResult(i, 100);
            }
        });
        updateBadge();
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
            vibrator.vibrate(50);
            result.openDrawer();
            return true;
        } else if (id == R.id.search) {
            vibrator.vibrate(50);
            Intent i = new Intent(getApplicationContext(), Activity_Search.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void updateBadge() {
        updateCartMenu();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        try {
            updateCartMenu();
            //FetchAllData();
        } catch (NullPointerException e) {
            Log.i("Badge", "Known Error");
        }
    }
    
    @Override
    public void onStart() {
        super.onStart();
    }
    
    @Override
    protected void onStop() {
        slider.stopAutoCycle();
        super.onStop();
    }
    
    @Override
    public void onSliderClick(BaseSliderView slider) {
        Helper.MakeToast(getApplicationContext(), String.valueOf(slider.getBundle().get("title")), TAGs.SUCCESS);
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
    
    public void updateCartMenu() {
        int count = db_item.getItemCount();
        this.itemMessagesBadgeTextView.setText("" + count);
        this.itemMessagesBadgeTextView.setVisibility(View.VISIBLE);
    }
    
    private void checkConfirm(String phone) {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = URLs.base_URL + "checkConfirm";
            JSONObject params = new JSONObject();
            params.put(TAGs.PHONE, phone);
            final String mRequestBody = params.toString();
            
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("LOG_VOLLEY R", response);
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean(TAGs.ERROR);
                        if (!error) {
                            String check = jObj.getString("c");
                            if (check.equals("OK")) {
                                if (!confirmManager.isInfoConfirm())
                                    Helper.MakeToast(getApplicationContext(), "حساب شما تایید شد", TAGs.SUCCESS, Toast.LENGTH_LONG);
                                confirmManager.setInfoConfirm(true);
                            } else {
                                Helper.MakeToast(getApplicationContext(), "حساب شما تایید نشده است", TAGs.WARNING, Toast.LENGTH_LONG);
                                confirmManager.setInfoConfirm(false);
                            }
                        } else {
                            String errorMsg = jObj.getString(TAGs.ERROR_MSG);
                            Helper.MakeToast(Activity_Main.this, errorMsg, TAGs.ERROR);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("LOG_VOLLEY E", error.toString());
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
        } catch (JSONException e) {
            e.printStackTrace();
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
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == 1) {
                categoryList.clear();
                newList.clear();
                mostList.clear();
                popularList.clear();
                offList.clear();
                collectionList.clear();
                
                categoryAdapter.notifyDataSetChanged();
                newAdapter.notifyDataSetChanged();
                mostAdapter.notifyDataSetChanged();
                popularAdapter.notifyDataSetChanged();
                offAdapter.notifyDataSetChanged();
                collectionAdapter.notifyDataSetChanged();
                
                category_view.setAdapter(categoryAdapter);
                new_view.setAdapter(newAdapter);
                most_view.setAdapter(mostAdapter);
                popular_view.setAdapter(popularAdapter);
                off_view.setAdapter(offAdapter);
                collection_view.setAdapter(collectionAdapter);
                
                FetchAllData();
            }
        }
    }
}