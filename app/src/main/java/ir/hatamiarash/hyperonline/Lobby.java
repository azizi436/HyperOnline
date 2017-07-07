/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import helper.Helper;

public class Lobby extends AppCompatActivity {
    public static Lobby pointer;

    @InjectView(R.id.temp_login)
    public Button temp_login;                        // login button
    @InjectView(R.id.temp_signup)
    public Button temp_signup;                       // signup button
    @InjectView(R.id.lobby)
    public VideoView lobby;                          // video
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temp);

        ButterKnife.inject(Lobby.this);
        Helper.GetPermissions(this, getApplicationContext());
        pointer = this;

        vibrator = (Vibrator) Lobby.this.getSystemService(VIBRATOR_SERVICE);
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.lobby);
        lobby.setVideoURI(uri);
        lobby.start();
        temp_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Helper.CheckInternet(Lobby.this)) {
                    vibrator.vibrate(50);
                    Intent i = new Intent(Lobby.this, Login.class);
                    startActivity(i);
                }
            }
        });
        temp_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Helper.CheckInternet(Lobby.this)) {
                    vibrator.vibrate(50);
                    Intent i = new Intent(Lobby.this, Register.class);
                    startActivity(i);
                }
            }
        });
        lobby.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
            }
        });
    }

    @Override
    protected void onResume() {
        lobby.start();
        super.onResume();
    }

    @Override
    protected void onPause() {
        lobby.pause();
        super.onPause();
    }
}
