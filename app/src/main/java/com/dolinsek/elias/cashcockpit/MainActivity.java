package com.dolinsek.elias.cashcockpit;

import android.support.v4.app.FragmentManager;
import 	android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.dolinsek.elias.cashcockpit.components.Database;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * BottomNavigationView for navigating
     */
    private BottomNavigationView mBottomNavigationView;

    private CockpitFragment cockpitFragment = new CockpitFragment();
    private DatabaseFragment databaseFragment = new DatabaseFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null){
            //Shows CockpitFragment
            replaceFragment(new CockpitFragment());
        }

        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.bnv_main);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                //Switch between fragments
                switch (item.getItemId()){
                    case R.id.navigation_database:
                            replaceFragment(databaseFragment);
                            return true;
                    case R.id.navigation_cockpit:
                            replaceFragment(cockpitFragment);
                            return true;
                }

                return false;
            }
        });

        try {
            //Loads database from json
            Database.load(getApplicationContext());
        } catch (Exception e) {
            Log.e(MainActivity.TAG, "", e);
        }
    }

    /**
     * Replaces current Fragment with new Fragment
     * @param fragment new Fragment
     */
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.ll_main, fragment);

        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_activity_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_settings){

            //Start SettingsActivity
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
