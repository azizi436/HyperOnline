/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro2;

public class Intro extends AppIntro2 {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*addSlide(Fragment_intro_1.Instance());
        addSlide(Fragment_intro_2.Instance());
        addSlide(Fragment_intro_3.Instance());
        addSlide(Fragment_intro_4.Instance());
        addSlide(Fragment_intro_5.Instance());*/
        showSkipButton(false);
        setProgressButtonEnabled(true);
        setVibrate(true);
        setVibrateIntensity(30);
    }
    
    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
    }
    
    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }
    
    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }
}