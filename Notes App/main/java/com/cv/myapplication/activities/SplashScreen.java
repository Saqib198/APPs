package com.cv.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.cv.myapplication.R;

public class SplashScreen extends AppCompatActivity {

    private int SPLASH_TIME_OUT = 3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);


    new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
            Intent i = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }, SPLASH_TIME_OUT);


}}