/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.jetbrains.annotations.Contract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import helper.CustomPrimaryDrawerItem;
import helper.EndlessScrollListener;
import helper.FontHelper;
import helper.Helper;
import helper.SQLiteHandlerItem;
import helper.SessionManager;
import helper.SharedPreferencesManager;
import helper.SymmetricProgressBar;
import ir.hatamiarash.adapters.CategoryAdapter_Small;
import ir.hatamiarash.adapters.ProductAdapter_All;
import ir.hatamiarash.interfaces.CardBadge;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;
import ir.hatamiarash.utils.Values;
import models.Category;
import models.Product;

public class Activity_List extends AppCompatActivity implements CardBadge {
    private Vibrator vibrator;
    static Typeface persianTypeface;
    public Drawer result = null;
    SymmetricProgressBar progressBar, p;
    public static SQLiteHandlerItem db_item;
    SessionManager session;
    private SweetAlertDialog progressDialog;
    SharedPreferencesManager SPManager;
    
    @InjectView(R.id.list)
    public RecyclerView list;
    @InjectView(R.id.category_list)
    public RecyclerView category_list;
    @InjectView(R.id.toolbar)
    public Toolbar toolbar;
    @InjectView(R.id.title_product)
    public TextView title_product;
    @InjectView(R.id.title_category)
    public TextView title_category;
    private TextView itemMessagesBadgeTextView;
    
    private String url, category_id, parent_id;
    public int list_category;
    private List<Product> productList;
    private List<Category> categoryList;
    private ProductAdapter_All productAdapter;
    private CategoryAdapter_Small categoryAdapter;
    
    private static String _CAT;
    private static String _CAT_ID;
    private static String _TITLE;
    private static String _LEVEL;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_complex);
        ButterKnife.inject(this);
        
        session = new SessionManager(getApplicationContext());
        SPManager = new SharedPreferencesManager(getApplicationContext());
        list_category = Integer.valueOf(getIntent().getStringExtra("cat"));
        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        persianTypeface = Typeface.createFromAsset(getAssets(), FontHelper.FontPath);
        progressBar = new SymmetricProgressBar(this);
        progressBar.setId(R.id.id);
        ViewGroup viewGroup = ((ViewGroup) this.findViewById(android.R.id.content));
        viewGroup.addView(progressBar, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5));
        p = viewGroup.findViewById(R.id.color_bar);
        p.setVisibility(View.INVISIBLE);
        progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.setCancelable(false);
        progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
        db_item = new SQLiteHandlerItem(getApplicationContext());
        
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("cat")) _CAT = extras.getString("cat", "");
            if (extras.containsKey("cat_id")) _CAT_ID = extras.getString("cat_id", "");
            if (extras.containsKey("title")) _TITLE = extras.getString("title", "");
            if (extras.containsKey("level")) _LEVEL = extras.getString("level", "");
        }
        
        if (list_category == 1) {
            category_list.setVisibility(View.GONE);
            title_category.setVisibility(View.GONE);
        }
        
        _CAT_ID = category_id = getIntent().getStringExtra("cat_id");
        if (list_category == 1 || list_category == 2) {
            toolbar.setTitle(FontHelper.getSpannedString(getApplicationContext(), getIntent().getStringExtra("title")));
        } else
            toolbar.setTitle(FontHelper.getSpannedString(getApplicationContext(), getResources().getString(R.string.app_name_fa)));
        
        setSupportActionBar(toolbar);
        
        PrimaryDrawerItem item_home = new CustomPrimaryDrawerItem().withIdentifier(1).withName("صفحه اصلی").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_home);
        PrimaryDrawerItem item_categories = new CustomPrimaryDrawerItem().withIdentifier(2).withName("دسته بندی ها").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_grid_on);
        PrimaryDrawerItem item_collections = new CustomPrimaryDrawerItem().withIdentifier(3).withName("سبد غذایی پیشنهادی").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_shopping_cart);
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
        PrimaryDrawerItem item_call = new CustomPrimaryDrawerItem().withIdentifier(17).withName("تماس با ما").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_phone);
        PrimaryDrawerItem item_help = new CustomPrimaryDrawerItem().withIdentifier(18).withName("راهنمای خرید").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_live_help);
        PrimaryDrawerItem item_questions = new CustomPrimaryDrawerItem().withIdentifier(19).withName("پرسش های متداول").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_help);
        PrimaryDrawerItem item_about = new CustomPrimaryDrawerItem().withIdentifier(20).withName("درباره ما").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_business_center);
        PrimaryDrawerItem item_profile = new CustomPrimaryDrawerItem().withIdentifier(21).withName("صفحه کاربر").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_person);
        PrimaryDrawerItem item_inbox = new CustomPrimaryDrawerItem().withIdentifier(22).withName("صندوق پیام").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_inbox);
        PrimaryDrawerItem item_contact = new CustomPrimaryDrawerItem().withIdentifier(23).withName("ارتباط با ما").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_email);
        PrimaryDrawerItem item_login = new CustomPrimaryDrawerItem().withIdentifier(24).withName("ورود").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_exit_to_app);
        PrimaryDrawerItem item_register = new CustomPrimaryDrawerItem().withIdentifier(25).withName("ثبت نام").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_create);
        SectionDrawerItem item_section = new SectionDrawerItem().withName("هایپرآنلاین").withTypeface(persianTypeface);
        SectionDrawerItem item_section2 = new SectionDrawerItem().withName("محصولات").withTypeface(persianTypeface);
        
        BadgeStyle a = new BadgeStyle()
                .withColor(ContextCompat.getColor(getApplicationContext(), R.color.red))
                .withCorners(40)
                .withTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        if (SPManager.isUnreadMessage()) {
            item_inbox.withBadgeStyle(a);
            item_inbox.withBadge("جدید");
        }
        
        IDrawerItem items[] = new IDrawerItem[]{
                item_home,
                item_profile,
                item_cart,
                item_track,
                item_inbox,
                item_comment,
                item_section2,
                item_categories,
                item_collections,
                item_most_sell,
                item_new,
                item_off,
                item_event,
                item_section,
                item_website,
                item_social,
                item_share,
                item_call,
                item_contact,
                item_help,
                item_terms,
                item_about
        };
        
        IDrawerItem items2[] = new IDrawerItem[]{
                item_home,
                item_login,
                item_register,
                item_cart,
                item_track,
                item_inbox,
                item_comment,
                item_section2,
                item_categories,
                item_collections,
                item_most_sell,
                item_new,
                item_off,
                item_event,
                item_section,
                item_website,
                item_social,
                item_share,
                item_call,
                item_contact,
                item_help,
                item_terms,
                item_about
        };
        
        result = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(new AccountHeaderBuilder()
                        .withActivity(this)
                        .withHeaderBackground(R.drawable.drawer_header)
                        .build())
                .addDrawerItems(
                        (IDrawerItem[]) (session.isLoggedIn() ? items : items2)
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
                                Intent i = new Intent(getApplicationContext(), Activity_Social.class);
                                startActivity(i);
                                result.closeDrawer();
                            }
                            if (item == 13) {
                                Intent i = new Intent(getApplicationContext(), Activity_WebPage.class);
                                i.putExtra(TAGs.TITLE, "قوانین و مقررات");
                                i.putExtra(TAGs.ADDRESS, "terms");
                                startActivity(i);
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
                                Intent i = new Intent(getApplicationContext(), Activity_WebPage.class);
                                i.putExtra(TAGs.TITLE, "راهنمای خرید");
                                i.putExtra(TAGs.ADDRESS, "help");
                                startActivity(i);
                            }
                            if (item == 19) {
                                
                            }
                            if (item == 20) {
                                Intent i = new Intent(getApplicationContext(), Activity_WebPage.class);
                                i.putExtra(TAGs.TITLE, "درباره ما");
                                i.putExtra(TAGs.ADDRESS, "about");
                                startActivity(i);
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
                            if (item == 23) {
                                Intent i = new Intent(getApplicationContext(), Activity_WebPage.class);
                                i.putExtra(TAGs.TITLE, "ارتباط با ما");
                                i.putExtra(TAGs.ADDRESS, "contact");
                                startActivity(i);
                            }
                            if (item == 24) {
                                Intent i = new Intent(getApplicationContext(), Login.class);
                                startActivity(i);
                            }
                            if (item == 25) {
                                Intent i = new Intent(getApplicationContext(), Register.class);
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
        
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter_All(this, productList);
        productAdapter.setHasStableIds(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(linearLayoutManager);
        EndlessScrollListener scrollListener = new EndlessScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                p.setVisibility(View.VISIBLE);
                loadProduct(page);
            }
        };
        list.addOnScrollListener(scrollListener);
        list.setItemAnimator(new DefaultItemAnimator());
        list.setAdapter(productAdapter);
        
        if (list_category == 1) {
            loadProduct(1);
        } else if (list_category == 2) {
            categoryList = new ArrayList<>();
            categoryAdapter = new CategoryAdapter_Small(this, categoryList);
            categoryAdapter.setHasStableIds(true);
            LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(Activity_List.this, LinearLayoutManager.HORIZONTAL, false);
            horizontalLayoutManager.setStackFromEnd(true);
            category_list.setLayoutManager(horizontalLayoutManager);
            category_list.setItemAnimator(new DefaultItemAnimator());
            category_list.setAdapter(categoryAdapter);
            loadCategory(1, getIntent().getStringExtra("level"));
        }
    }
    
    private void loadProduct(int page) {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = URLs.base_URL + "products_all";
            JSONObject params = new JSONObject();
            params.put("index", page);
            params.put("cat", category_id);
            final String mRequestBody = params.toString();
            
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    p.setVisibility(View.INVISIBLE);
                    Log.i("LOG_VOLLEY R", response);
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean(TAGs.ERROR);
                        if (!error) {
                            JSONArray products = jObj.getJSONArray("product");
                            
                            for (int i = 0; i < products.length(); i++) {
                                JSONObject product = products.getJSONObject(i);
                                
                                productList.add(new Product(
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
                            
                            productAdapter.notifyDataSetChanged();
                        } else {
                            String errorMsg = jObj.getString(TAGs.ERROR_MSG);
                            Helper.MakeToast(Activity_List.this, errorMsg, TAGs.ERROR);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    p.setVisibility(View.INVISIBLE);
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
        } catch (Exception e) {
            p.setVisibility(View.INVISIBLE);
            e.printStackTrace();
        }
    }
    
    private void loadCategory(int page, String level) {
        p.setVisibility(View.VISIBLE);
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = URLs.base_URL + "categories_all";
            JSONObject params = new JSONObject();
            params.put("index", page);
            params.put("level", level);
            params.put("parent", category_id);
            final String mRequestBody = params.toString();
            
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    p.setVisibility(View.INVISIBLE);
                    Log.i("LOG_VOLLEY R", response);
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean(TAGs.ERROR);
                        if (!error) {
                            JSONArray categories = jObj.getJSONArray("category");
                            JSONArray products = jObj.getJSONArray("product");
                            
                            for (int i = 0; i < categories.length(); i++) {
                                JSONObject category = categories.getJSONObject(i);
                                
                                categoryList.add(new Category(
                                                category.getString("unique_id"),
                                                category.getString("name"),
                                                category.getString("image"),
                                                category.getDouble("point"),
                                                category.getInt("point_count"),
                                                category.getInt("off"),
                                                category.getInt("level")
                                        )
                                );
                            }
                            for (int i = 0; i < products.length(); i++) {
                                JSONObject product = products.getJSONObject(i);
                                
                                Product p = new Product(
                                        product.getString("unique_id"),
                                        product.getString("name"),
                                        product.getString("image"),
                                        product.getString("price"),
                                        product.getInt("off"),
                                        product.getInt("count"),
                                        product.getDouble("point"),
                                        product.getInt("point_count"),
                                        product.getString("description")
                                );
                                
                                if (!productList.contains(p))
                                    productList.add(p);
                            }
                            
                            categoryAdapter.notifyDataSetChanged();
                            productAdapter.notifyDataSetChanged();
                        } else {
                            String errorMsg = jObj.getString(TAGs.ERROR_MSG);
                            Helper.MakeToast(Activity_List.this, errorMsg, TAGs.ERROR);
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    p.setVisibility(View.INVISIBLE);
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
        } catch (Exception e) {
            p.setVisibility(View.INVISIBLE);
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
            result.openDrawer();
            return true;
        } else if (id == R.id.search) {
            Intent i = new Intent(getApplicationContext(), Activity_Search.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void updateCartMenu() {
        int count = db_item.getItemCount();
        this.itemMessagesBadgeTextView.setText("" + count);
        this.itemMessagesBadgeTextView.setVisibility(View.VISIBLE);
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
        } catch (NullPointerException e) {
            Log.i("Badge", "Known Error");
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == 1) {
                Intent intent = new Intent(getApplicationContext(), Activity_List.class);
                intent.putExtra("cat", _CAT);
                intent.putExtra("level", _LEVEL);
                intent.putExtra("cat_id", _CAT_ID);
                intent.putExtra("title", _TITLE);
                startActivity(intent);
                finish();
            }
        }
    }
}

/* for search a new data we should clear view
// 1. First, clear the array of data
listOfItems.clear();
// 2. Notify the adapter of the update
        recyclerAdapterOfItems.notifyDataSetChanged(); // or notifyItemRangeRemoved
// 3. Reset endless scroll listener when performing a new search
        scrollListener.resetState();*/