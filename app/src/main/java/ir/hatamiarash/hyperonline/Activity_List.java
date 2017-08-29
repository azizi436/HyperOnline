/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import helper.Helper;
import helper.SQLiteHandlerItem;
import helper.SymmetricProgressBar;
import ir.hatamiarash.adapters.CategoryAdapter_Small;
import ir.hatamiarash.adapters.ProductAdapter_All;
import ir.hatamiarash.interfaces.CardBadge;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;
import models.Category;
import models.Product;

public class Activity_List extends AppCompatActivity implements CardBadge {
    private Vibrator vibrator;
    static Typeface persianTypeface;
    public Drawer result = null;
    SymmetricProgressBar progressBar, p;
    public static SQLiteHandlerItem db_item;
    
    private String url, category_id, parent_id;
    public int list_category;
    private List<Product> productList;
    private List<Category> categoryList;
    private ProductAdapter_All productAdapter;
    private CategoryAdapter_Small categoryAdapter;
    
    private TextView itemMessagesBadgeTextView;
    
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
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_complex);
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
        db_item = new SQLiteHandlerItem(getApplicationContext());
        
        if (list_category == 1) {
            category_list.setVisibility(View.GONE);
            title_category.setVisibility(View.GONE);
        }
        
        category_id = getIntent().getStringExtra("cat_id");
        if (list_category == 1 || list_category == 2) {
            toolbar.setTitle(FontHelper.getSpannedString(getApplicationContext(), getIntent().getStringExtra("title")));
        } else
            toolbar.setTitle(FontHelper.getSpannedString(getApplicationContext(), getResources().getString(R.string.app_name_fa)));
        
        setSupportActionBar(toolbar);
        
        //PrimaryDrawerItem item_home = new CustomPrimaryDrawerItem().withIdentifier(1).withName("خانه").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_home);
        //PrimaryDrawerItem item_profile = new CustomPrimaryDrawerItem().withIdentifier(2).withName("حساب کاربری").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_account_circle);
        PrimaryDrawerItem item_cart = new CustomPrimaryDrawerItem().withIdentifier(3).withName("کل محصولات").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_shopping_cart);
        //PrimaryDrawerItem item_comment = new CustomPrimaryDrawerItem().withIdentifier(4).withName("ارسال نظر").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_message);
        
        result = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(new AccountHeaderBuilder()
                        .withActivity(this)
                        .withHeaderBackground(R.drawable.drawer_header)
                        .build())
                .addDrawerItems(
                        //item_home,
                        //item_profile,
                        item_cart
                        //item_comment
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
                            
                            categoryAdapter.notifyDataSetChanged();
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
                Intent i = new Intent(getApplicationContext(), ShopCard.class);
                startActivity(i);
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
}
/* for search a new data we should clear view
// 1. First, clear the array of data
listOfItems.clear();
// 2. Notify the adapter of the update
        recyclerAdapterOfItems.notifyDataSetChanged(); // or notifyItemRangeRemoved
// 3. Reset endless scroll listener when performing a new search
        scrollListener.resetState();*/