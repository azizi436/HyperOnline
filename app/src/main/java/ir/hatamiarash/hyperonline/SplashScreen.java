/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.Intent;
import android.os.Bundle;
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

        logo.setImageDrawable(getResources().getDrawable(R.drawable.logo));

        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent intent;
                    if (session.isLoggedIn())
                        intent = new Intent(SplashScreen.this, Activity_Main.class);
                    else
                        intent = new Intent(SplashScreen.this, Lobby.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        timerThread.start();
    }
}