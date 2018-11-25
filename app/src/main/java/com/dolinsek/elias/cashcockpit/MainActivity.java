package com.dolinsek.elias.cashcockpit;

import android.support.v4.app.FragmentManager;
import 	android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.dolinsek.elias.cashcockpit.components.Database;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * BottomNavigationView for navigating
     */
    private BottomNavigationView mBottomNavigationView;

    private CockpitStatisticsFragment cockpitStatisticsFragment = new CockpitStatisticsFragment();
    private HistoryFragment historyFragment = new HistoryFragment();
    private StatisticsFragment statisticsFragment = new StatisticsFragment();
    private DatabaseFragment databaseFragment = new DatabaseFragment();
    private SettingsFragment settingsFragment = new SettingsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDatabase();

        if(savedInstanceState == null){
            //Show CockpitFragment
            replaceFragment(cockpitStatisticsFragment);
        }

        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.bnv_main);
        mBottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            //Switch between fragments
            switch (item.getItemId()){
                case R.id.navigation_database:
                    replaceFragment(databaseFragment);
                        return true;
                case R.id.navigation_cockpit:
                    replaceFragment(cockpitStatisticsFragment);
                        return true;
                case R.id.navigation_history:
                    replaceFragment(historyFragment);
                        return true;
                case R.id.navigation_statistics:
                    replaceFragment(statisticsFragment);
                        return true;
                case R.id.navigation_settings:
                    replaceFragment(settingsFragment);
                        return true;

            }

            return false;
        });

        showSignInActivityIfUserIsNotSignedIn();
    }
    /**
     * Replaces current Fragment with new Fragment
     * @param fragment new Fragment
     */
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.ll_main, fragment);
        fragmentTransaction.commit();
    }

    private void showSignInActivityIfUserIsNotSignedIn(){
        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            Intent intent = new Intent(this, SignInActivity.class);
            intent.putExtra(SignInActivity.EXTRA_SHOW_TUTORIAL_ACTIVITY_AFTERWADS, true);
            startActivity(intent);
        }
    }

    private void initDatabase(){
        try {
            Database.load(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
