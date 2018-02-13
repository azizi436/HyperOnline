/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static helper.Helper.isAppAvailable;

public class Activity_Social extends AppCompatActivity {
	
	@BindView(R.id.telegram)
	public ImageView telegram;
	@BindView(R.id.instagram)
	public ImageView instagram;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.social);
		ButterKnife.bind(this);
		
		telegram.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String appName = "org.telegram.messenger";
				boolean isAppInstalled = isAppAvailable(getApplicationContext(), appName);
				if (isAppInstalled) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=online_hyper"));
					startActivity(intent);
				} else {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://t.me/online_hyper")));
				}
			}
		});
		
		instagram.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Uri uri = Uri.parse("http://instagram.com/_u/online.hyper");
				Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
				likeIng.setPackage("com.instagram.android");
				try {
					startActivity(likeIng);
				} catch (ActivityNotFoundException e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/online.hyper")));
				}
			}
		});
	}
}
