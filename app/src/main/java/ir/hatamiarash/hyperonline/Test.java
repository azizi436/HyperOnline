/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import butterknife.ButterKnife;
import butterknife.InjectView;
import helper.CustomPrimaryDrawerItem;
import helper.FontHelper;

public class Test extends AppCompatActivity {
    static Typeface persianTypeface;                 // persian font typeface
    public Drawer result = null;
    @InjectView(R.id.list)
    public RecyclerView recyclerView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        
        ButterKnife.inject(this);
        
        
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
                            /*Intent i = new Intent(getApplicationContext(), Test.class);
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
        }
        return super.onOptionsItemSelected(item);
    }
}