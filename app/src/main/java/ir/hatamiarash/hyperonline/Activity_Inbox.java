/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.mikepenz.materialdrawer.Drawer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import helper.FontHelper;
import helper.SQLiteHandlerSupport;
import ir.hatamiarash.adapters.MessageAdapter;
import ir.hatamiarash.interfaces.Refresh;
import models.Message;

public class Activity_Inbox extends AppCompatActivity implements Refresh {
    public Drawer result = null;
    SweetAlertDialog progressDialog;
    public static SQLiteHandlerSupport db_support;
    
    private List<Message> messageList;
    private MessageAdapter messageAdapter;
    
    @InjectView(R.id.toolbar)
    public Toolbar toolbar;
    @InjectView(R.id.list)
    public RecyclerView list;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_simple);
        ButterKnife.inject(this);
        
        db_support = new SQLiteHandlerSupport(getApplicationContext());
        progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.setCancelable(false);
        progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
        progressDialog.setTitleText("لطفا منتظر بمانید");
        
        toolbar.setTitle(FontHelper.getSpannedString(getApplicationContext(), "صندوق پیام"));
        setSupportActionBar(toolbar);
        
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        messageAdapter.setHasStableIds(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(linearLayoutManager);
        list.setItemAnimator(new DefaultItemAnimator());
        list.setAdapter(messageAdapter);
        
        loadMessages();
    }
    
    private void loadMessages() {
        showDialog();
        try {
            List<String> List = db_support.GetMessages();
            for (int i = 0; i < (List.size() / 3); i++) {
                String title = List.get(i * 3 + 0);
                String body = List.get(i * 3 + 1);
                String date = List.get(i * 3 + 2);
                messageList.add(new Message(
                        title,
                        body,
                        date
                ));
                Collections.reverse(messageList);
                messageAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            hideDialog();
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
    public void refresh() {
        messageList.clear();
        messageAdapter.notifyDataSetChanged();
        loadMessages();
    }
}