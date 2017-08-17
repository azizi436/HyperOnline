/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.github.ybq.android.spinkit.style.Wave;

import butterknife.ButterKnife;
import butterknife.InjectView;
import helper.SessionManager;

public class SplashScreen extends AppCompatActivity {
    SessionManager session;
    @InjectView(R.id.logo)
    public ImageView logo;
    @InjectView(R.id.spinner)
    public ProgressBar spinner;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        ButterKnife.inject(this);
        
        session = new SessionManager(getApplicationContext());
        
        Wave animation = new Wave();
        spinner.setIndeterminateDrawable(animation);
        
        logo.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.motor));
        
        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    startActivity(new Intent(SplashScreen.this, Activity_Main.class));
                    finish();
                }
            }
        };
        timerThread.start();
    }
}