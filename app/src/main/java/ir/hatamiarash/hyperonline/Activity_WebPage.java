/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import helper.FontHelper;
import ir.hatamiarash.utils.TAGs;

public class Activity_WebPage extends AppCompatActivity {
    WebView page_content;
    TextView page_title;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webpage);
        Intent i = getIntent();
        String title = i.getStringExtra(TAGs.TITLE);
        String address = i.getStringExtra(TAGs.ADDRESS);
        page_content = (WebView) findViewById(R.id.page_content);
        page_title = (TextView) findViewById(R.id.page_title);
        WebSettings webSettings = page_content.getSettings();
        webSettings.setJavaScriptEnabled(false);
        webSettings.setSupportZoom(false);
        page_content.loadUrl("file:///android_asset/" + address + ".html");
        page_title.setText(FontHelper.getSpannedString(this, title));
    }
}