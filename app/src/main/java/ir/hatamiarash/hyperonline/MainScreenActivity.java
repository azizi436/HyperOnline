/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import co.ronash.pushe.Pushe;
import helper.CustomPrimaryDrawerItem;
import helper.FontHelper;
import helper.Helper;
import helper.SQLiteHandlerItem;
import helper.SQLiteHandlerSetup;
import helper.SessionManager;
import ir.hatamiarash.adapters.CategoryAdapter;
import ir.hatamiarash.adapters.ProductAdapter;
import ir.hatamiarash.utils.TAGs;
import models.Category;
import models.Product;

public class MainScreenActivity extends AppCompatActivity implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {
    private static final String TAG = MainScreenActivity.class.getSimpleName();
    public static MainScreenActivity pointer;        // use to finish activity from anywhere
    //public static SQLiteHandler db_user;             // items database
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
    private ProductAdapter productAdapter;
    private List<Category> categoryList;
    private List<Product> mostList;
    
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
                            /*Intent i = new Intent(getApplicationContext(), MainScreenActivity.class);
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
        
        invalidateOptionsMenu();
        
        //Logger logger = new Logger();
        //logger.execute();
        
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
        
        category_view = (RecyclerView) findViewById(R.id.category_list);
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(this, categoryList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 3);
        category_view.setLayoutManager(mLayoutManager);
        category_view.addItemDecoration(new GridSpacingItemDecoration(3, dpToPx(5), true));
        category_view.setItemAnimator(new DefaultItemAnimator());
        category_view.setAdapter(categoryAdapter);
        prepareCategories();
        
        most_view = (RecyclerView) findViewById(R.id.most_list);
        mostList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, mostList);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(MainScreenActivity.this, LinearLayoutManager.HORIZONTAL, false);
        horizontalLayoutManager.setStackFromEnd(true);
        most_view.setLayoutManager(horizontalLayoutManager);
        most_view.setItemAnimator(new DefaultItemAnimator());
        most_view.setAdapter(productAdapter);
        
        LinearLayoutManager horizontalLayoutManager2 = new LinearLayoutManager(MainScreenActivity.this, LinearLayoutManager.HORIZONTAL, false);
        horizontalLayoutManager2.setStackFromEnd(true);
        new_view = (RecyclerView) findViewById(R.id.new_list);
        new_view.setLayoutManager(horizontalLayoutManager2);
        new_view.setItemAnimator(new DefaultItemAnimator());
        new_view.setAdapter(productAdapter);
        prepareProducts();
        
        scroll.postDelayed(new Runnable() {
            @Override
            public void run() {
                scroll.scrollTo(0, 0);
                scroll.fullScroll(ScrollView.FOCUS_UP);
            }
        }, 50);
    }
    
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
    
    private void prepareCategories() {
        int[] covers = new int[]{
                R.drawable.nnull,
                R.drawable.nnull,
                R.drawable.nnull,
                R.drawable.nnull,
                R.drawable.nnull,
                R.drawable.nnull
        };
        
        Category a = new Category("دسته بندی 1", covers[0]);
        categoryList.add(a);
        
        a = new Category("دسته بندی 2", covers[1]);
        categoryList.add(a);
        
        a = new Category("دسته بندی 3", covers[2]);
        categoryList.add(a);
        
        a = new Category("دسته بندی 4", covers[3]);
        categoryList.add(a);
        
        a = new Category("دسته بندی 5", covers[4]);
        categoryList.add(a);
        
        a = new Category("دسته بندی 6", covers[5]);
        categoryList.add(a);
        
        categoryAdapter.notifyDataSetChanged();
    }
    
    private void prepareProducts() {
        int cover = R.drawable.nnull;
        
        Product a = new Product("1", "محصول 1", cover, "1000", 0, 10, 5.5, 10, "توضیحات", 0);
        mostList.add(a);
        
        a = new Product("1", "محصول 2", cover, "1000", 0, 10, 5.5, 10, "توضیحات", 0);
        mostList.add(a);
        
        a = new Product("1", "محصول 3", cover, "1000", 0, 10, 5.5, 10, "توضیحات", 0);
        mostList.add(a);
        
        a = new Product("1", "محصول 4", cover, "1000", 0, 10, 5.5, 10, "توضیحات", 0);
        mostList.add(a);
        
        a = new Product("1", "محصول 5", cover, "1000", 0, 10, 5.5, 10, "توضیحات", 0);
        mostList.add(a);
        
        a = new Product("1", "محصول 6", cover, "1000", 0, 10, 5.5, 10, "توضیحات", 0);
        mostList.add(a);
        
        categoryAdapter.notifyDataSetChanged();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.my_menu, menu);
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
        } else if (id == R.id.search) {
            Intent i = new Intent(getApplicationContext(), Test.class);
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
}