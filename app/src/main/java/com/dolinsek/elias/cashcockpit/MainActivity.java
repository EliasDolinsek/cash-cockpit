package com.dolinsek.elias.cashcockpit;

import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dolinsek.elias.cashcockpit.components.Database;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SING_IN = 241;

    /**
     * BottomNavigationView for navigating
     */
    private BottomNavigationView mBottomNavigationView;

    private CockpitFragment cockpitFragment = new CockpitFragment();
    private HistoryFragment historyFragment = new HistoryFragment();
    private StatisticsFragment statisticsFragment = new StatisticsFragment();
    private DatabaseFragment databaseFragment = new DatabaseFragment();
    private SettingsFragment settingsFragment = new SettingsFragment();

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        initDatabase();

        if(savedInstanceState == null){
            //Show CockpitFragment
            replaceFragment(cockpitFragment);
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
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null){
                    //TODO
                } else {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.TwitterBuilder().build(),
                                            new AuthUI.IdpConfig.FacebookBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .build(),
                            RC_SING_IN);
                }
            }
        };

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (authStateListener != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SING_IN){
            if (resultCode == RESULT_OK){
                //TODO
            } else if (resultCode == RESULT_CANCELED){
                finish();
            }
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
        fragmentTransaction.commit();
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
