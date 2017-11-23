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
import butterknife.BindView;

public class SplashScreen extends AppCompatActivity {
    Thread timerThread;
    
    @BindView(R.id.logo)
    public ImageView logo;
    @BindView(R.id.spinner)
    public ProgressBar spinner;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        ButterKnife.bind(this);
        
        Wave animation = new Wave();
        spinner.setIndeterminateDrawable(animation);
        
        logo.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.logo));
        
        timerThread = new Thread() {
            public void run() {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent i = new Intent(SplashScreen.this, Activity_Main.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }
            }
        };
        
        timerThread.start();
    }
}