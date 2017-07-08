/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import helper.FontHelper;
import helper.FormatHelper;
import helper.Helper;
import helper.SQLiteHandler;
import helper.SQLiteHandlerItem;
import helper.SessionManager;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;
import models.Product;
import volley.AppController;

import static helper.Helper.ConvertProductType;

public class ShopCard extends AppCompatActivity {
    private static final String TAG = ShopCard.class.getSimpleName(); // class tag for log
    final static private int CODE_PAYMENT = 100;
    private static final String PAY_PAID = "ALREADY_PAID";
    private static final String PAY_CANCELED = "PAYMENT_CANCELED";
    private static final String PAY_SUCCESS = "PAYMENT_SUCCESS";
    private static final String PAY_EXPIRED = "PAYMENT_EXPIRED";
    public static SQLiteHandlerItem db_item;
    public static SQLiteHandler db_user;
    SessionManager session;
    SweetAlertDialog progressDialog;
    List<Product> Products_List;
    RecyclerView list;
    Vibrator vibrator;
    TextView total_price;
    TextView total_off;
    TextView total_extend;
    TextView total_pay;
    Button pay, clear;
    int tOff = 0;
    int tPrice = 0;
    int tExtend = 0;
    ArrayList<String> arrayList = new ArrayList<>();
    List<String> Item;
    int check = 0;
    int counter = 0;
    private String ORDER_CODE = "-1";
    private String ORDER_AMOUNT = "1000";
    private String SELLER_ID;
    private String STUFFS = "";
    private String DESCRIPTION = "";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shop_card);
        
        list = (RecyclerView) findViewById(R.id.recyclerView);
        total_price = (TextView) findViewById(R.id.CardPrice);
        total_off = (TextView) findViewById(R.id.CardDiscount);
        total_extend = (TextView) findViewById(R.id.CardExtend);
        total_pay = (TextView) findViewById(R.id.CardTotalPrice);
        pay = (Button) findViewById(R.id.btnPay);
        clear = (Button) findViewById(R.id.btnClear);
        
        db_item = new SQLiteHandlerItem(getApplicationContext());
        db_user = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());
        list.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(layoutManager);
        Products_List = new ArrayList<>();
        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.setCancelable(false);
        progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
        
        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(50);
                if (tPrice + tExtend > 0) {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View customView = inflater.inflate(R.layout.custom_dialog, null);
                    final TextView edit_text = (TextView) customView.findViewById(R.id.edit_text);
                    new MaterialStyledDialog.Builder(ShopCard.this)
                            .setTitle(FontHelper.getSpannedString(getApplicationContext(), "تکمیل خرید"))
                            .setDescription(FontHelper.getSpannedString(getApplicationContext(), "توضیحات سفارش :"))
                            .setStyle(Style.HEADER_WITH_TITLE)
                            .setHeaderColor(R.color.green)
                            .setCustomView(customView, 5, 5, 5, 5)
                            .withDarkerOverlay(true)
                            .withDialogAnimation(true)
                            .setCancelable(true)
                            .setPositiveText("تایید")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    DESCRIPTION = edit_text.getText().toString();
                                    Pay(TAGs.API_KEY, String.valueOf(1000));
                                    //onPaySuccess();
                                }
                            })
                            .show();
                } else
                    Helper.MakeToast(getApplicationContext(), "سبد خرید خالی است", TAGs.ERROR);
            }
        });
        
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(50);
                db_item.deleteItems();
                Intent i = new Intent(getApplicationContext(), ShopCard.class);
                startActivity(i);
                finish();
            }
        });
        
        FetchAllProducts();
        //SyncServer();
    }
    
    private void FetchAllProducts() {
        Item = db_item.getItemDetails();
        arrayList.add("null");
        // numbers must be same of database fields !!!!! all numbers Item.size() / n [] i * n + 1
        for (int i = 0; i < (Item.size() / 11); i++) {
            //String id = Item.get(i * 10);
            String uid = Item.get(i * 11 + 1);
            String name = Item.get(i * 11 + 2);
            String price = Item.get(i * 11 + 3);
            String extend = Item.get(i * 11 + 4);
            String info = Item.get(i * 11 + 5);
            String seller_id = Item.get(i * 11 + 6);
            //String seller = Item.get(i * 11 + 7);
            String off = Item.get(i * 11 + 8);
            String count = Item.get(i * 11 + 9);
            String type = Item.get(i * 11 + 10);
            
            tOff += Integer.valueOf(off);
            tPrice += Integer.valueOf(price);
            if (!arrayList.contains(seller_id)) {
                arrayList.add(seller_id);
                tExtend += Integer.valueOf(extend);
                arrayList = new ArrayList<>(new LinkedHashSet<>(arrayList));
            }
    
            //Products_List.add(new Product(uid, name, "", price, Integer.valueOf(off), Integer.valueOf(count), 0.0, 0, info, 0, Integer.valueOf(type)));
            
            SELLER_ID = seller_id;
            STUFFS += name + "-";
        }
        Log.w(TAG, String.valueOf(arrayList));
        total_off.setText(String.valueOf(tOff) + " تومان");
        total_price.setText(String.valueOf(tPrice + tOff) + " تومان");
        total_extend.setText(String.valueOf(tExtend) + " تومان");
        total_pay.setText(String.valueOf(tPrice + tExtend) + " تومان");
        pay.setText("پرداخت - " + FormatHelper.toPersianNumber(String.valueOf(tPrice + tExtend)) + " تومان");
        Adapter_Product adapter = new Adapter_Product(Products_List);
        list.setAdapter(adapter);
    }
    
    private int ConvertToInteger(TextView text) {
        return Integer.valueOf(FormatHelper.toEnglishNumber(text.getText().toString().substring(0, text.getText().toString().length() - 6)));
    }
    
    private void Pay(final String API_KEY, final String AMOUNT) {
        String string_req = "req_fetch";
        progressDialog.setTitleText("لطفا منتظر بمانید");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST, URLs.Check_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Check Response: " + response);
                if (Helper.isNumber(response)) {
                    String Address = URLs.Pay_URL + response;
                    ORDER_CODE = response;
                    ORDER_AMOUNT = AMOUNT;
                    // TODO: i think we can't use webview because there isn't back !!!
                    // TODO: but if we use internal android web client maybe we have a return command to application
                    //Intent i = new Intent(getApplicationContext(), Web.class);
                    //i.putExtra(TAGs.ADDRESS, Address);
                    //Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(Address));
                    //startActivityForResult(i, CODE_PAYMENT);
                } else
                    Helper.MakeToast(getApplicationContext(), response, TAGs.ERROR);
                hideDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Check Error: " + error.getMessage());
                if (error.getMessage() != null)
                    Helper.MakeToast(getApplicationContext(), error.getMessage(), TAGs.ERROR);
                else
                    Helper.MakeToast(getApplicationContext(), "خطایی رخ داده است - اتصال به اینترنت را بررسی نمایید", TAGs.ERROR);
                hideDialog();
                finish();
            }
        }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new HashMap<>();
                params.put("api", API_KEY);
                params.put("amount", AMOUNT);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, string_req);
    }
    
    private void Check(final String CODE, final String AMOUNT) {
        String string_req = "req_fetch";
        progressDialog.setTitleText("لطفا منتظر بمانید");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST, URLs.Verify_URL + CODE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.w("Check", response);
                switch (response) {
                    case PAY_PAID:
                        onPayPaid();
                        break;
                    case PAY_EXPIRED:
                        onPayExpired();
                        break;
                    case PAY_CANCELED:
                        if (counter == 0) {
                            Check(ORDER_CODE, ORDER_AMOUNT);
                            counter++;
                        } else
                            onPayCanceled();
                        break;
                    case PAY_SUCCESS:
                        onPaySuccess();
                        break;
                }
                hideDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Check Error: " + error.getMessage());
                if (error.getMessage() != null)
                    Helper.MakeToast(getApplicationContext(), error.getMessage(), TAGs.ERROR);
                else
                    Helper.MakeToast(getApplicationContext(), "خطایی رخ داده است - اتصال به اینترنت را بررسی نمایید", TAGs.ERROR);
                hideDialog();
                finish();
            }
        }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new HashMap<>();
                Log.w("AMOUNT", "CODE:" + CODE + " Amount:" + AMOUNT);
                params.put("au", CODE);
                params.put("amount", AMOUNT);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, string_req);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_PAYMENT)
            Check(ORDER_CODE, ORDER_AMOUNT);
    }
    
    private void onPaySuccess() {
        SyncServer();
        new MaterialStyledDialog.Builder(ShopCard.this)
                .setTitle(FontHelper.getSpannedString(getApplicationContext(), "پرداخت"))
                .setDescription(FontHelper.getSpannedString(getApplicationContext(), "پرداخت موفقیت آمیز بود. با تشکر از انتخاب شما."))
                .setStyle(Style.HEADER_WITH_TITLE)
                .setHeaderColor(R.color.green)
                .withDarkerOverlay(true)
                .withDialogAnimation(true)
                .setCancelable(true)
                .setPositiveText("باشه")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        db_item.deleteItems();
                        /*Intent i = new Intent(getApplicationContext(), Pay_Log.class);
                        i.putExtra("order_code", ORDER_CODE);
                        startActivity(i);*/
                        finish();
                    }
                })
                .show();
    }
    
    private void onPayPaid() {
        new MaterialStyledDialog.Builder(getApplicationContext())
                .setTitle(FontHelper.getSpannedString(getApplicationContext(), "پرداخت"))
                .setDescription(FontHelper.getSpannedString(getApplicationContext(), "این پرداخت قبلا انجام شده است"))
                .setStyle(Style.HEADER_WITH_TITLE)
                .withDarkerOverlay(true)
                .withDialogAnimation(true)
                .setCancelable(true)
                .setPositiveText("باشه")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(getApplicationContext(), ShopCard.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }
    
    private void onPayCanceled() {
        new MaterialStyledDialog.Builder(ShopCard.this)
                .setTitle(FontHelper.getSpannedString(getApplicationContext(), "پرداخت"))
                .setDescription(FontHelper.getSpannedString(getApplicationContext(), "پرداخت کنسل شده است"))
                .setStyle(Style.HEADER_WITH_TITLE)
                .withDarkerOverlay(true)
                .withDialogAnimation(true)
                .setCancelable(true)
                .setPositiveText("باشه")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(getApplicationContext(), ShopCard.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }
    
    private void onPayExpired() {
        new MaterialStyledDialog.Builder(getApplicationContext())
                .setTitle(FontHelper.getSpannedString(getApplicationContext(), "پرداخت"))
                .setDescription(FontHelper.getSpannedString(getApplicationContext(), "به دلیل کند عمل کردن ، زمان پرداخت منقضی شده است. مجدد تلاش کنید"))
                .setStyle(Style.HEADER_WITH_TITLE)
                .withDarkerOverlay(true)
                .withDialogAnimation(true)
                .setCancelable(true)
                .setPositiveText("باشه")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(getApplicationContext(), ShopCard.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }
    
    private void SyncServer() {
        Log.e("Sync", "Start");
        try {
            HashMap<String, String> user = db_user.getUserDetails();
            String file_name = user.get(TAGs.UID); // use unique_id of user for files to prevent duplicate at same time
            arrayList.add(TAGs.NULL);
            // numbers must be same of database fields !!!!! all numbers Item.size() / n   +++++   i * n + 1
            for (int i = 0; i < (Item.size() / 11); i++) {
                String name = Item.get(i * 11 + 2);
                String price = Item.get(i * 11 + 3);
                String count = Item.get(i * 11 + 9);
                String type = Item.get(i * 11 + 10);
                
                String data = name + "-" +
                        ConvertProductType(Integer.valueOf(type)) + "-" +
                        count + "-" +
                        price;
                
                Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(getExternalFilesDir(null), file_name + ".txt"), true), "UTF-8"));
                out.write(data);
                out.write('\n');
                out.close();
            }
            
            File file = new File(getExternalFilesDir(null), file_name + ".txt");
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(InetAddress.getByName("ftp.zimia.ir"));
            ftpClient.login("zm@zimia.ir", "3920512197");
            ftpClient.changeWorkingDirectory("/include/");
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            BufferedInputStream buffIn = new BufferedInputStream(new FileInputStream(file));
            ftpClient.enterLocalPassiveMode();
            ftpClient.storeFile(file_name + ".txt", buffIn);
            buffIn.close();
            ftpClient.logout();
            ftpClient.disconnect();
            file.delete();
            Log.e("Sync", "Done");
            SetOrder();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void SetOrder() {
        String string_req = "req_fetch";
        progressDialog.setTitleText("لطفا منتظر بمانید");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST, URLs.base_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Set Response: " + response);
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean(TAGs.ERROR);
                    if (!error) {
                        
                    } else {
                        String errorMsg = jObj.getString(TAGs.ERROR_MSG);
                        Helper.MakeToast(ShopCard.this, errorMsg, TAGs.ERROR);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    hideDialog();
                    finish();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Set Error: " + error.getMessage());
                if (error.getMessage() != null)
                    Helper.MakeToast(ShopCard.this, error.getMessage(), TAGs.ERROR);
                else
                    Helper.MakeToast(ShopCard.this, "خطایی رخ داده است - اتصال به اینترنت را بررسی نمایید", TAGs.ERROR);
                hideDialog();
                finish();
            }
        }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new HashMap<>();
                HashMap<String, String> user = db_user.getUserDetails();
                params.put(TAGs.TAG, "payment_done");
                params.put("order_code", ORDER_CODE);
                params.put("order_amount", ORDER_AMOUNT);
                params.put(TAGs.SID, SELLER_ID);
                params.put("user_id", user.get(TAGs.UID));
                params.put("stuffs", STUFFS.substring(0, STUFFS.length() - 1));
                params.put("description", DESCRIPTION);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, string_req);
    }
    
    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }
    
    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
    
    private class Adapter_Product extends RecyclerView.Adapter<Adapter_Product.ProductViewHolder> {
        private final String TAG = Adapter_Product.class.getSimpleName();
        private List<Product> products;
        
        Adapter_Product(List<Product> products) {
            this.products = products;
        }
        
        @Override
        public ProductViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_cart_product, viewGroup, false);
            return new ProductViewHolder(view);
        }
        
        @Override
        public int getItemCount() {
            return products.size();
        }
        
        @Override
        public void onBindViewHolder(ProductViewHolder sellerViewHolder, int i) {
            sellerViewHolder.product_id.setText(products.get(i).uid);
            sellerViewHolder.product_off.setText(String.valueOf(products.get(i).off));
            sellerViewHolder.product_name.setText(products.get(i).name);
            sellerViewHolder.product_description.setText(products.get(i).description);
            if (products.get(i).count > 0)
                sellerViewHolder.product_price.setText(String.valueOf(Integer.valueOf(products.get(i).price) / products.get(i).count) + " تومان");
            sellerViewHolder.product_count.setText(String.valueOf(products.get(i).count));
        }
        
        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
        
        class ProductViewHolder extends RecyclerView.ViewHolder {
            CardView product;
            TextView product_id;
            TextView product_off;
            TextView product_name;
            TextView product_description;
            TextView product_price;
            TextView product_inc;
            TextView product_count;
            TextView product_dec;
            
            ProductViewHolder(View itemView) {
                super(itemView);
                product = (CardView) itemView.findViewById(R.id.product);
                product_id = (TextView) itemView.findViewById(R.id.product_id);
                product_off = (TextView) itemView.findViewById(R.id.product_off);
                product_name = (TextView) itemView.findViewById(R.id.product_name);
                product_description = (TextView) itemView.findViewById(R.id.product_info);
                product_price = (TextView) itemView.findViewById(R.id.product_price);
                product_count = (TextView) itemView.findViewById(R.id.product_count);
                product_dec = (TextView) itemView.findViewById(R.id.product_dec);
                product_inc = (TextView) itemView.findViewById(R.id.product_inc);
                
                product_dec.setOnTouchListener(new TouchListener());
                product_inc.setOnTouchListener(new TouchListener());
                
                product_dec.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        vibrator.vibrate(25);
                        if (Integer.valueOf(product_count.getText().toString()) > 1)
                            product_count.setText(String.valueOf(Integer.valueOf(product_count.getText().toString()) - 1));
                        else {
                            product_count.setText("0");
                            removeAt(getPosition());
                            db_item.deleteItem(product_id.getText().toString());
                            check = products.size();
                        }
                    }
                });
                
                product_inc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        vibrator.vibrate(25);
                        product_count.setText(String.valueOf(Integer.valueOf(product_count.getText().toString()) + 1));
                    }
                });
                
                product_count.addTextChangedListener(new TextWatcher() {
                    int temp;
                    
                    @Override
                    public void afterTextChanged(Editable s) {
                        if (check == products.size()) {
                            Log.w(TAG, "OFF:" + product_off.getText().toString());
                            int new_off = Integer.valueOf(product_off.getText().toString()) / temp * Integer.valueOf(product_count.getText().toString());
                            product_off.setText(String.valueOf(new_off));
                            db_item.updateItem(product_id.getText().toString(), product_count.getText().toString(), String.valueOf(ConvertToInteger(product_price) * Integer.valueOf(product_count.getText().toString())), String.valueOf(new_off));
                            String price = FormatHelper.toPersianNumber(String.valueOf(db_item.TotalPrice() + tExtend)) + " تومان";
                            String off = FormatHelper.toPersianNumber(String.valueOf(db_item.TotalOff())) + " تومان";
                            total_pay.setText(price);
                            pay.setText("پرداخت - " + price);
                            total_off.setText(off);
                            int tPrice = ConvertToInteger(total_pay) - ConvertToInteger(total_extend) + ConvertToInteger(total_off);
                            total_price.setText(String.valueOf(tPrice) + " تومان");
                        } else
                            check++;
                    }
                    
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        temp = Integer.valueOf(product_count.getText().toString());
                    }
                    
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }
                });
            }
            
            void removeAt(int position) {
                products.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, products.size());
            }
        }
    }
    
    private class TouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ((TextView) view).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    ((TextView) view).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                    break;
            }
            return false;
        }
    }
}