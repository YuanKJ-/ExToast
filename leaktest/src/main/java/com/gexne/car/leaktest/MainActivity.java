package com.gexne.car.leaktest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.squareup.leakcanary.RefWatcher;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

//    private String test = "TEST_STR";
//    private Handler handler = new Handler();
//
//    static class MyRunnable implements Runnable {
//        final MainActivity this$0;
//
//        public MyRunnable(MainActivity activity) {
//            this$0 = activity;
//        }
//
//        @Override
//        public void run() {
//            String test = MainActivity.access$000(this$0);
//            Log.d("test", test);
//        }
//    }
//
//    static String access$000(MainActivity activity) {
//        return activity.test;
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        MyRunnable mRunnable = new MyRunnable(this);
//        handler.postDelayed(mRunnable, 10000);
//    }

    /**1. 演示dumsys空activity*/
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }


    /**2. 演示dumpsys有handler泄漏的activity*/
    private String test = "TEST_STR";
    private Handler handler = new Handler();
    MyRunnable mRunnable = new MyRunnable();

     class MyRunnable implements Runnable {
        @Override
        public void run() {
            Log.d("test", test);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler.postDelayed(mRunnable, 10000);
    }


}
