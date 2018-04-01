package com.dolinsek.elias.cashcockpit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.dolinsek.elias.cashcockpit.components.Database;

public class StartActivity extends AppCompatActivity {

    private static final long TIME_UNTIL_MAIN_ACTIVITY_START = 1000;
    private static final String TAG = StartActivity.class.getSimpleName();

    private View rootView;
    private Thread automaticallyMainActivityStartThread;
    private boolean mainActivityAlreadyStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        rootView = (RelativeLayout) findViewById(R.id.rl_start_root);

        initDatabase();
        initAutomaticallyStartThread();
        automaticallyMainActivityStartThread.start();

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestStartOfMainActivity();
            }
        });
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

    private void initAutomaticallyStartThread(){
        automaticallyMainActivityStartThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(TIME_UNTIL_MAIN_ACTIVITY_START);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                requestStartOfMainActivity();
            }
        });
    }

    private void requestStartOfMainActivity(){
        if (!mainActivityAlreadyStarted){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        mainActivityAlreadyStarted = true;
    }
}
