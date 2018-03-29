package com.dolinsek.elias.cashcockpit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.dolinsek.elias.cashcockpit.components.Database;

public class StartActivity extends AppCompatActivity {

    private static final String TAG = StartActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        new Thread(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                initDatabase();
                long stopTime = System.currentTimeMillis();

                requestStartOfMainActivity(stopTime - startTime);
            }
        }).start();
    }

    private void initDatabase(){
        try {
            Database.load(getApplicationContext());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            if (Database.getPrimaryCategories().size() == 0){
                restorePrimaryCategories();
            }
        }
    }

    private void restorePrimaryCategories(){
        Database.setPrimaryCategories(Database.getDefaultPrimaryCategories());
    }

    private void requestStartOfMainActivity(long durationOfLoadingDatabase){
        long startTime = 1500;
        if (durationOfLoadingDatabase < startTime){
            try {
                Thread.sleep(startTime - durationOfLoadingDatabase);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
