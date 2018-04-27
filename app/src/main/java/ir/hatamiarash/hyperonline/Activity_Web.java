/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import ir.hatamiarash.utils.TAGs;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Activity_Web extends AppCompatActivity {
	WebView page_content;
	ProgressBar progressBar;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web);
		
		String address = getIntent().getStringExtra(TAGs.ADDRESS);
		page_content = findViewById(R.id.web_view);
		progressBar = findViewById(R.id.pbar);
		WebSettings webSettings = page_content.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportZoom(false);
		page_content.setWebViewClient(new MyWebViewClient());
		page_content.loadUrl(address);
	}
	
	private class MyWebViewClient extends WebViewClient {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			progressBar.setVisibility(View.VISIBLE);
		}
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			progressBar.setVisibility(View.GONE);
			if (url.equals("http://hyper-online.ir/api/callback"))
				finish();
		}
		
		@Override
		public void onLoadResource(WebView view, String url) {
			super.onLoadResource(view, url);
		}
	}
	
	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}
}