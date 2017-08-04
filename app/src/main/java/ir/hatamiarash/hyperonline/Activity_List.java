/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

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
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
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
import helper.CustomPrimaryDrawerItem;
import helper.EndlessScrollListener;
import helper.FontHelper;
import helper.GridSpacingItemDecoration;
import helper.Helper;
import helper.SymmetricProgressBar;
import ir.hatamiarash.adapters.CategoryAdapter_All;
import ir.hatamiarash.adapters.ProductAdapter_All;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;
import models.Category;
import models.Product;

public class Activity_List extends AppCompatActivity {
    private Vibrator vibrator;
    static Typeface persianTypeface;
    public Drawer result = null;
    SymmetricProgressBar progressBar, p;
    
    public String url;
    public int list_category;
    private List<Product> productList;
    private List<Category> categoryList;
    private ProductAdapter_All productAdapter;
    private CategoryAdapter_All categoryAdapter;
    
    @InjectView(R.id.list)
    public RecyclerView list;
    @InjectView(R.id.toolbar)
    public Toolbar toolbar;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        ButterKnife.inject(this);
        
        list_category = Integer.valueOf(getIntent().getStringExtra("cat"));
        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        persianTypeface = Typeface.createFromAsset(getAssets(), FontHelper.FontPath);
        progressBar = new SymmetricProgressBar(this);
        progressBar.setId(R.id.bar);
        ViewGroup viewGroup = ((ViewGroup) this.findViewById(android.R.id.content));
        viewGroup.addView(progressBar, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5));
        p = viewGroup.findViewById(R.id.color_bar);
        p.setVisibility(View.INVISIBLE);
        
        if (list_category == 1)
            toolbar.setTitle(FontHelper.getSpannedString(getApplicationContext(), "محصولات "));
        else if (list_category == 2)
            toolbar.setTitle(FontHelper.getSpannedString(getApplicationContext(), "دسته بندی "));
        else
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
                        vibrator.vibrate(50);
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
        
        if (list_category == 1) {
            productList = new ArrayList<>();
            productAdapter = new ProductAdapter_All(this, productList);
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
            loadProduct(1);
        } else if (list_category == 2) {
            categoryList = new ArrayList<>();
            categoryAdapter = new CategoryAdapter_All(this, categoryList);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            list.setLayoutManager(linearLayoutManager);
            EndlessScrollListener scrollListener = new EndlessScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    p.setVisibility(View.VISIBLE);
                    loadCategory(page);
                }
            };
            list.addOnScrollListener(scrollListener);
            list.setItemAnimator(new DefaultItemAnimator());
            list.setAdapter(categoryAdapter);
            loadCategory(1);
        }
    }
    
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
    
    private void loadProduct(int page) {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = URLs.base_URL + "products_all";
            JSONObject params = new JSONObject();
            params.put("index", page);
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
    
    private void loadCategory(int page) {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = URLs.base_URL + "categories_all";
            JSONObject params = new JSONObject();
            params.put("index", page);
            params.put("level", "3");
            params.put("parent", "n");
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
                            
                            for (int i = 0; i < categories.length(); i++) {
                                JSONObject category = categories.getJSONObject(i);
                                
                                categoryList.add(new Category(
                                                category.getString("unique_id"),
                                                category.getString("name"),
                                                category.getString("image"),
                                                category.getDouble("point"),
                                                category.getInt("point_count"),
                                                category.getInt("off")
                                        )
                                );
                            }
                            
                            categoryAdapter.notifyDataSetChanged();
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
    
}
/* for search a new data we should clear view
// 1. First, clear the array of data
listOfItems.clear();
// 2. Notify the adapter of the update
        recyclerAdapterOfItems.notifyDataSetChanged(); // or notifyItemRangeRemoved
// 3. Reset endless scroll listener when performing a new search
        scrollListener.resetState();*/