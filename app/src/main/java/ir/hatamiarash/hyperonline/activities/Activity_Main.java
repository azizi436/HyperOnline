/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.crashlytics.android.Crashlytics;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.flurry.android.FlurryAgent;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.jetbrains.annotations.Contract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.ronash.pushe.Pushe;
import ir.hatamiarash.hyperonline.BuildConfig;
import ir.hatamiarash.hyperonline.HyperOnline;
import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.hyperonline.adapters.CategoryAdapter;
import ir.hatamiarash.hyperonline.adapters.ProductAdapter;
import ir.hatamiarash.hyperonline.databases.SQLiteHandler;
import ir.hatamiarash.hyperonline.databases.SQLiteHandlerItem;
import ir.hatamiarash.hyperonline.databases.SQLiteHandlerMain;
import ir.hatamiarash.hyperonline.databases.SQLiteHandlerSupport;
import ir.hatamiarash.hyperonline.helpers.ConfirmManager;
import ir.hatamiarash.hyperonline.helpers.FontHelper;
import ir.hatamiarash.hyperonline.helpers.Helper;
import ir.hatamiarash.hyperonline.helpers.PermissionHelper;
import ir.hatamiarash.hyperonline.helpers.SessionManager;
import ir.hatamiarash.hyperonline.helpers.SharedPreferencesManager;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import ir.hatamiarash.hyperonline.interfaces.CardBadge;
import ir.hatamiarash.hyperonline.libraries.CustomPrimaryDrawerItem;
import ir.hatamiarash.hyperonline.libraries.GridSpacingItemDecoration;
import ir.hatamiarash.hyperonline.models.Category;
import ir.hatamiarash.hyperonline.models.Product;
import ir.hatamiarash.hyperonline.utils.TAGs;
import ir.hatamiarash.hyperonline.utils.URLs;
import ir.hatamiarash.hyperonline.utils.Values;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static ir.hatamiarash.hyperonline.HyperOnline.HOST;
import static ir.hatamiarash.hyperonline.helpers.FormatHelper.fixResponse;
import static ir.hatamiarash.hyperonline.helpers.Helper.dpToPx;

public class Activity_Main extends AppCompatActivity implements
		BaseSliderView.OnSliderClickListener,
		ViewPagerEx.OnPageChangeListener,
		CardBadge {
	private static final String CLASS = Activity_Main.class.getSimpleName();
	
	Activity_Main pointer;             // use to finish activity from anywhere
	SQLiteHandler db_user;             // items database
	SQLiteHandlerItem db_item;         // items database
	SQLiteHandlerMain db_main;         // main database
	SQLiteHandlerSupport db_support;   // support database
	Typeface persianTypeface;                 // persian font typeface
	Drawer result = null;
	HyperOnline application;
	Analytics analytics;
	
	@BindView(R.id.category_list)
	RecyclerView category_view;
	@BindView(R.id.most_list)
	RecyclerView most_view;
	@BindView(R.id.new_list)
	RecyclerView new_view;
	@BindView(R.id.off_list)
	RecyclerView off_view;
	@BindView(R.id.collection_list)
	RecyclerView collection_view;
	@BindView(R.id.popular_list)
	RecyclerView popular_view;
	@BindView(R.id.slider)
	SliderLayout slider;
	@BindView(R.id.slider_layout)
	LinearLayout sliderLayout;
	@BindView(R.id.scroll)
	NestedScrollView scroll;
	@BindView(R.id.toolbar)
	Toolbar toolbar;
	@BindView(R.id.title_category)
	TextView title_category;
	@BindView(R.id.title_category_more)
	TextView title_category_more;
	@BindView(R.id.title_collection)
	TextView title_collection;
	@BindView(R.id.title_collection_more)
	TextView title_collection_more;
	@BindView(R.id.title_most)
	TextView title_most;
	@BindView(R.id.title_most_more)
	TextView title_most_more;
	@BindView(R.id.title_new)
	TextView title_new;
	@BindView(R.id.title_new_more)
	TextView title_new_more;
	@BindView(R.id.title_popular)
	TextView title_popular;
	@BindView(R.id.title_popular_more)
	TextView title_popular_more;
	@BindView(R.id.title_off)
	TextView title_off;
	@BindView(R.id.title_off_more)
	TextView title_off_more;
	
	SessionManager session;                          // session for check user logged
	ConfirmManager confirmManager;
	SharedPreferencesManager SPManager;
	Vibrator vibrator;
	CategoryAdapter categoryAdapter;
	ProductAdapter newAdapter, mostAdapter, popularAdapter, offAdapter, collectionAdapter;
	List<Category> categoryList;
	List<Product> newList, mostList, popularList, offList, collectionList;
	TextView itemMessagesBadgeTextView;
	SweetAlertDialog progressDialog;
	long back_pressed;                       // for check back key pressed count
	int VERSION = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_screen);
		
		ButterKnife.bind(this);
		application = (HyperOnline) getApplication();
		analytics = application.getAnalytics();
		
		Helper.CheckInternet(getApplicationContext());
		PermissionHelper.getAllPermissions(this, getApplicationContext());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			getPermission(this);
		
		pointer = this;
		session = new SessionManager(getApplicationContext());
		confirmManager = new ConfirmManager(getApplicationContext());
		SPManager = new SharedPreferencesManager(getApplicationContext());
		db_user = new SQLiteHandler(getApplicationContext());
		db_item = new SQLiteHandlerItem(getApplicationContext());
		db_main = new SQLiteHandlerMain(getApplicationContext());
		db_support = new SQLiteHandlerSupport(getApplicationContext());
		vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		persianTypeface = Typeface.createFromAsset(getAssets(), FontHelper.FontPath);
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		
		SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
		if (settings.getBoolean("my_first_time", true)) {
			try {
				db_item.CreateTable();
				db_main.CreateTable();
				db_support.CreateTable();
				settings.edit().putBoolean("my_first_time", false).apply();
			} catch (Exception e) {
				Crashlytics.logException(e);
			}
		}
		
		if (confirmManager.isPhoneConfirm() && session.isLoggedIn()) {
			Intent i = new Intent(getApplicationContext(), Activity_ConfirmPhone.class);
			i.putExtra(TAGs.PHONE, db_user.getUserDetails().get(TAGs.PHONE));
			startActivity(i);
			finish();
		}
		if (session.isLoggedIn()) {
			Timber.i("User Logged in");
			CheckInfoConfirm(db_user.getUserDetails().get(TAGs.PHONE));
			SyncServer(db_user.getUserDetails().get(TAGs.UID));
		}
		
		Drawable logo = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_logo_wide);
		toolbar.setLogo(logo);
		for (int i = 0; i < toolbar.getChildCount(); i++) {
			View child = toolbar.getChildAt(i);
			if (child != null)
				if (child.getClass() == ImageView.class) {
					ImageView iv2 = (ImageView) child;
					if (iv2.getDrawable() == logo) {
						iv2.setAdjustViewBounds(true);
					}
				}
		}
		
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		
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
		PrimaryDrawerItem item_wallet = new CustomPrimaryDrawerItem().withIdentifier(26).withName("کیف پول").withTypeface(persianTypeface).withIcon(GoogleMaterial.Icon.gmd_account_balance_wallet);
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
				item_wallet,
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
								i.putExtra(TAGs.TYPE, "1");
								startActivity(i);
								result.closeDrawer();
							}
							if (item == 4) {
								Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
								i.putExtra(TAGs.TYPE, "2");
								startActivity(i);
								result.closeDrawer();
							}
							if (item == 5) {
								Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
								i.putExtra(TAGs.TYPE, "3");
								startActivity(i);
								result.closeDrawer();
							}
							if (item == 6) {
								Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
								i.putExtra(TAGs.TYPE, "4");
								startActivity(i);
								result.closeDrawer();
							}
							if (item == 7) {
								Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
								i.putExtra(TAGs.TYPE, "5");
								startActivity(i);
								result.closeDrawer();
							}
							if (item == 8) {
								Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
								i.putExtra(TAGs.TYPE, "6");
								startActivity(i);
								result.closeDrawer();
							}
							if (item == 9) {
								Intent i = new Intent(getApplicationContext(), Activity_Comment.class);
								startActivity(i);
								result.closeDrawer();
							}
							if (item == 10) {
								Intent i = new Intent(getApplicationContext(), Activity_ShopCard.class);
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
									Intent i = new Intent(getApplicationContext(), Activity_Login.class);
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
							if (item == 16) {
								try {
									Intent i = new Intent(Intent.ACTION_SEND);
									i.setType("text/plain");
									i.putExtra(Intent.EXTRA_SUBJECT, "Hyper Online");
									String sAux = "\nتا حالا با هایپرآنلاین کار کردی ؟\nیه نگاه بنداز\n\n";
									sAux = sAux + URLs.Share_App + "\n\n";
									i.putExtra(Intent.EXTRA_TEXT, sAux);
									startActivity(Intent.createChooser(i, "یک گزینه انتخاب کنید"));
								} catch (Exception e) {
									Timber.tag(CLASS).e(e);
									Crashlytics.logException(e);
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
							if (item == 20) {
								Intent i = new Intent(getApplicationContext(), Activity_WebPage.class);
								i.putExtra(TAGs.TITLE, "درباره ما");
								i.putExtra(TAGs.ADDRESS, "about");
								startActivity(i);
							}
							if (item == 21) {
								if (Helper.CheckInternet(getApplicationContext())) {
									if (session.isLoggedIn()) {
										Intent i = new Intent(getApplicationContext(), Activity_UserProfile.class);
										startActivity(i);
									} else {
										Helper.MakeToast(getApplicationContext(), "ابتدا وارد شوید", TAGs.WARNING);
										Intent i = new Intent(getApplicationContext(), Activity_Login.class);
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
								Intent i = new Intent(getApplicationContext(), Activity_Login.class);
								startActivity(i);
							}
							if (item == 25) {
								Intent i = new Intent(getApplicationContext(), Activity_Register.class);
								startActivity(i);
							}
							if (item == 26) {
								Intent i = new Intent(getApplicationContext(), Activity_Wallet.class);
								i.putExtra(TAGs.UID, db_user.getUserDetails().get(TAGs.UID));
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
		
		FetchAllData();
		
		categoryList = new ArrayList<>();
		categoryAdapter = new CategoryAdapter(this, categoryList);
		RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 3);
		category_view.setLayoutManager(mLayoutManager);
		category_view.addItemDecoration(new GridSpacingItemDecoration(3, dpToPx(getApplicationContext(), 5), true));
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
				vibrator.vibrate(50);
				Intent i = new Intent(getApplicationContext(), Activity_Cat.class);
				startActivity(i);
			}
		});
		
		title_most_more.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				vibrator.vibrate(50);
				Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
				i.putExtra(TAGs.TYPE, "2");
				startActivity(i);
			}
		});
		
		title_collection_more.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				vibrator.vibrate(50);
				Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
				i.putExtra(TAGs.TYPE, "6");
				startActivity(i);
			}
		});
		
		title_new_more.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				vibrator.vibrate(50);
				Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
				i.putExtra(TAGs.TYPE, "3");
				startActivity(i);
			}
		});
		
		title_off_more.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				vibrator.vibrate(50);
				Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
				i.putExtra(TAGs.TYPE, "5");
				startActivity(i);
			}
		});
		
		title_popular_more.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				vibrator.vibrate(50);
				Intent i = new Intent(getApplicationContext(), Activity_ListDetails.class);
				i.putExtra(TAGs.TYPE, "4");
				startActivity(i);
			}
		});
		
		analyticsReport();
	}
	
	private void FetchAllData() {
		showDialog();
		try {
			String URL = getResources().getString(R.string.url_api, HOST) + "main";
			final String mRequestBody = "";
			
			StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
				@Override
				public void onResponse(String response) {
					response = fixResponse(response);
					Timber.tag(CLASS).d(response);
					hideDialog();
					try {
						JSONObject jObj = new JSONObject(response);
						boolean error = jObj.getBoolean(TAGs.ERROR);
						if (!error) {
							JSONObject status = jObj.getJSONObject("status");
							
							if (status.getInt("offline_S") == 1 && !BuildConfig.DEBUG) {
								title_category.setVisibility(View.GONE);
								title_category_more.setVisibility(View.GONE);
								category_view.setVisibility(View.GONE);
								//
								title_collection.setVisibility(View.GONE);
								title_collection_more.setVisibility(View.GONE);
								collection_view.setVisibility(View.GONE);
								//
								title_most.setVisibility(View.GONE);
								title_most_more.setVisibility(View.GONE);
								most_view.setVisibility(View.GONE);
								//
								title_new.setVisibility(View.GONE);
								title_new_more.setVisibility(View.GONE);
								new_view.setVisibility(View.GONE);
								//
								title_popular.setVisibility(View.GONE);
								title_popular_more.setVisibility(View.GONE);
								popular_view.setVisibility(View.GONE);
								//
								title_off.setVisibility(View.GONE);
								title_off_more.setVisibility(View.GONE);
								off_view.setVisibility(View.GONE);
								//
								sliderLayout.setVisibility(View.GONE);
								//
								toolbar.setVisibility(View.GONE);
								String msg = "در حال حاضر به دلیل \" " + status.getString("offline") + " \" امکان سرویس دهی وجود ندارد. به زودی با شما خواهیم بود.";
								new MaterialStyledDialog.Builder(Activity_Main.this)
										.setTitle(FontHelper.getSpannedString(getApplicationContext(), "هایپرآنلاین"))
										.setDescription(FontHelper.getSpannedString(getApplicationContext(), msg))
										.setStyle(Style.HEADER_WITH_TITLE)
										.setHeaderColor(R.color.green)
										.withDarkerOverlay(true)
										.withDialogAnimation(true)
										.setCancelable(false)
										.setPositiveText("باشه")
										.onPositive(new MaterialDialog.SingleButtonCallback() {
											@Override
											public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
												android.os.Process.killProcess(android.os.Process.myPid());
											}
										})
										.show();
							} else {
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
												getResources().getString(R.string.url_image, HOST) + banner.getString("image")
										);
									}
									
									slider.setPresetTransformer(SliderLayout.Transformer.Default);
									slider.setDuration(3000);
									slider.setCustomAnimation(new DescriptionAnimation());
									
									for (String name : urls.keySet()) {
										DefaultSliderView SliderView = new DefaultSliderView(Activity_Main.this);
										SliderView
												.image(urls.get(name))
												.setScaleType(BaseSliderView.ScaleType.CenterCrop)
												.setOnSliderClickListener(Activity_Main.this);
										slider.addSlider(SliderView);
									}
								} else {
									sliderLayout.setVisibility(View.GONE);
								}
								
								if (_opt.has("v")) {
									VERSION = _opt.getInt("v");
									checkVersion(VERSION);
								}
							}
						} else {
							String errorMsg = jObj.getString(TAGs.ERROR_MSG);
							Helper.MakeToast(Activity_Main.this, errorMsg, TAGs.ERROR);
						}
					} catch (JSONException e) {
						Crashlytics.logException(e);
						hideDialog();
					}
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					Crashlytics.logException(error);
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
				public byte[] getBody() {
					try {
						return mRequestBody.getBytes("utf-8");
					} catch (UnsupportedEncodingException e) {
						Crashlytics.logException(e);
						return null;
					}
				}
			};
			stringRequest.setRetryPolicy(new DefaultRetryPolicy(
					0,
					DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
			));
			application.addToRequestQueue(stringRequest);
		} catch (Exception e) {
			Crashlytics.logException(e);
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
				Intent i = new Intent(getApplicationContext(), Activity_ShopCard.class);
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
		} catch (NullPointerException ignore) {
		}
		
		if (Helper.isRooted(Activity_Main.this)) {
			rootedDevice(Activity_Main.this);
		}
		
		if (Helper.isDebugging(getContentResolver())) {
			usbDebuggingDevice(Activity_Main.this);
		}
		
		if (VERSION != 0) {
			checkVersion(VERSION);
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(getApplicationContext());
	}
	
	@Override
	protected void onStop() {
		slider.stopAutoCycle();
		super.onStop();
		FlurryAgent.onEndSession(getApplicationContext());
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	public void onSliderClick(BaseSliderView slider) {
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
	
	private void CheckInfoConfirm(final String phone) {
		analytics.reportEvent("Server - Check Info Confirmation");
		try {
			String URL = getResources().getString(R.string.url_api, HOST) + "checkConfirm";
			JSONObject params = new JSONObject();
			params.put(TAGs.PHONE, phone);
			final String mRequestBody = params.toString();
			
			StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
				@Override
				public void onResponse(String response) {
					response = fixResponse(response);
					Timber.tag(CLASS).d(response);
					try {
						JSONObject jObj = new JSONObject(response);
						boolean error = jObj.getBoolean(TAGs.ERROR);
						if (!error) {
							String info_check = jObj.getString("c");
							String phone_check = jObj.getString("p");
							if (info_check.equals("OK")) {
								if (confirmManager.isInfoConfirm())
									Helper.MakeToast(getApplicationContext(), "حساب شما تایید شد", TAGs.SUCCESS, Toast.LENGTH_LONG);
								confirmManager.setInfoConfirm(true);
							} else {
								Helper.MakeToast(getApplicationContext(), "حساب شما تایید نشده است", TAGs.WARNING, Toast.LENGTH_LONG);
								confirmManager.setInfoConfirm(false);
							}
							if (phone_check.equals("0")) {
								confirmManager.setPhoneConfirm(false);
								new MaterialStyledDialog.Builder(Activity_Main.this)
										.setTitle(FontHelper.getSpannedString(Activity_Main.this, "تایید حساب"))
										.setDescription(FontHelper.getSpannedString(Activity_Main.this, "لطفا شماره تلفن خود را تایید کنید"))
										.setStyle(Style.HEADER_WITH_TITLE)
										.withDarkerOverlay(true)
										.withDialogAnimation(true)
										.setCancelable(false)
										.setPositiveText("باشه")
										.onPositive(new MaterialDialog.SingleButtonCallback() {
											@Override
											public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
												Intent intent = new Intent(Activity_Main.this, Activity_ConfirmPhone.class);
												intent.putExtra(TAGs.PHONE, phone);
												startActivity(intent);
												finish();
											}
										})
										.show();
							}
						} else {
							String errorMsg = jObj.getString(TAGs.ERROR_MSG);
							Helper.MakeToast(Activity_Main.this, errorMsg, TAGs.ERROR);
						}
					} catch (JSONException e) {
						Crashlytics.logException(e);
					}
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					Crashlytics.logException(error);
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
				public byte[] getBody() {
					try {
						return mRequestBody.getBytes("utf-8");
					} catch (UnsupportedEncodingException e) {
						Crashlytics.logException(e);
						return null;
					}
				}
			};
			stringRequest.setRetryPolicy(new DefaultRetryPolicy(
					0,
					DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
			));
			application.addToRequestQueue(stringRequest);
		} catch (JSONException e) {
			Crashlytics.logException(e);
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
				
				slider.removeAllSliders();
				
				FetchAllData();
			}
		}
	}
	
	private void checkVersion(int version) {
		try {
			PackageInfo pInfo = getApplicationContext()
					.getPackageManager()
					.getPackageInfo(getPackageName(), 0);
			int version2 = pInfo.versionCode;
			if (version > version2 && version != 0) {
				analytics.reportEvent("Application - Update");
				new MaterialStyledDialog.Builder(Activity_Main.this)
						.setTitle(FontHelper.getSpannedString(getApplicationContext(), "به روزرسانی"))
						.setDescription(FontHelper.getSpannedString(getApplicationContext(), "نسخه جدید هایپرآنلاین منتشر شده است. لطفا به روزرسانی کنید"))
						.setStyle(Style.HEADER_WITH_TITLE)
						.setHeaderColor(R.color.green)
						.withDarkerOverlay(true)
						.withDialogAnimation(true)
						.setCancelable(false)
						.setPositiveText("دانلود")
						.onPositive(new MaterialDialog.SingleButtonCallback() {
							@Override
							public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
								Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URLs.Update_App));
								startActivity(intent);
							}
						})
						.show();
			}
		} catch (PackageManager.NameNotFoundException e) {
			Crashlytics.logException(e);
		}
	}
	
	private void SyncServer(String id) {
		analytics.reportEvent("Server - Synchronize");
		try {
			String pushe = Pushe.getPusheId(getApplicationContext());
			String firebase = FirebaseInstanceId.getInstance().getToken();
			Timber.tag("PUSHE").i(pushe);
			Timber.tag("FIREBASE").i(firebase);
			
			String URL = getResources().getString(R.string.url_api, HOST) + "sync_id";
			JSONObject params = new JSONObject();
			params.put("u", id);
			params.put("p", pushe);
			params.put("f", firebase);
			params.put("v", Build.VERSION.SDK_INT);
			final String mRequestBody = params.toString();
			
			StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
				@Override
				public void onResponse(String response) {
					response = fixResponse(response);
					Timber.tag(CLASS).d(response);
					try {
						JSONObject jObj = new JSONObject(response);
						if (jObj.getBoolean(TAGs.ERROR))
							Timber.tag(CLASS).e(jObj.getString(TAGs.ERROR_MSG));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					Crashlytics.logException(error);
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
				public byte[] getBody() {
					try {
						return mRequestBody.getBytes("utf-8");
					} catch (UnsupportedEncodingException e) {
						Crashlytics.logException(e);
						return null;
					}
				}
			};
			stringRequest.setRetryPolicy(new DefaultRetryPolicy(
					0,
					DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
			));
			application.addToRequestQueue(stringRequest);
		} catch (JSONException e) {
			Crashlytics.logException(e);
		}
	}
	
	public void getPermission(final Activity activity) {
		if (PermissionHelper.checkSMSPermission(activity))
			new MaterialStyledDialog.Builder(activity)
					.setTitle(FontHelper.getSpannedString(activity, "تایید پیامکی"))
					.setDescription(FontHelper.getSpannedString(activity, "جهت تایید خودکار شماره تلفن هایپرآنلاین نیاز به دسترسی دارد"))
					.setStyle(Style.HEADER_WITH_TITLE)
					.withDarkerOverlay(true)
					.withDialogAnimation(true)
					.setCancelable(false)
					.setPositiveText("باشه")
					.onPositive(new MaterialDialog.SingleButtonCallback() {
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
							PermissionHelper.getSMSPermission(activity);
						}
					})
					.show();
	}
	
	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}
	
	private void rootedDevice(Activity activity) {
		Timber.e("Rooted Device");
		new MaterialStyledDialog.Builder(activity)
				.setTitle(FontHelper.getSpannedString(activity, "متاسفیم"))
				.setDescription(FontHelper.getSpannedString(activity, "هایپرآنلاین از ارائه خدمات به دستگاه های روت شده معذور است."))
				.setStyle(Style.HEADER_WITH_TITLE)
				.withDarkerOverlay(true)
				.withDialogAnimation(true)
				.setCancelable(false)
				.setPositiveText("باشه")
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						android.os.Process.killProcess(android.os.Process.myPid());
					}
				})
				.show();
	}
	
	private void usbDebuggingDevice(Activity activity) {
		Timber.e("Debugging Device");
		new MaterialStyledDialog.Builder(activity)
				.setTitle(FontHelper.getSpannedString(activity, "متاسفیم"))
				.setDescription(FontHelper.getSpannedString(activity, "هایپرآنلاین از ارائه خدمات به دستگاه های در حال Debug معذور است."))
				.setStyle(Style.HEADER_WITH_TITLE)
				.withDarkerOverlay(true)
				.withDialogAnimation(true)
				.setCancelable(false)
				.setPositiveText("باشه")
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						android.os.Process.killProcess(android.os.Process.myPid());
					}
				})
				.show();
	}
	
	private void analyticsReport() {
		analytics.reportScreen(CLASS);
		analytics.reportEvent("Open " + CLASS);
	}
}