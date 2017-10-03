/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import ir.hatamiarash.utils.TAGs;

public class Web extends AppCompatActivity {
    WebView page_content;
    ProgressBar progressBar;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web);
        Intent i = getIntent();
        String address = i.getStringExtra(TAGs.ADDRESS);
        page_content = (WebView) findViewById(R.id.web_view);
        progressBar = (ProgressBar) findViewById(R.id.pbar);
        WebSettings webSettings = page_content.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
        page_content.setWebViewClient(new MyWebViewClient());
        //page_content.setWebChromeClient(new MyWebChromeClient());
        page_content.loadUrl(address);
    }
    
    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
            Log.d("WebView", "page started : " + url);
        }
        
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
        
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.e("WebView", "page finished" + url);
            progressBar.setVisibility(View.GONE);
            if (url.equals("http://hyper-online.ir/api/callback"))
                finish();
        }
        
        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }
    }
}