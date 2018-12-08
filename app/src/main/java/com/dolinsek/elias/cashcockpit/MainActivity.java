package com.dolinsek.elias.cashcockpit;

import android.support.design.bottomappbar.BottomAppBar;
import android.support.v4.app.FragmentManager;
import 	android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.dolinsek.elias.cashcockpit.components.Database;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private BottomAppBar bottomAppBar;

    private CockpitStatisticsFragment cockpitStatisticsFragment;
    private HistoryFragment historyFragment;
    private StatisticsFragment statisticsFragment;
    private DatabaseFragment databaseFragment;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        initDatabase();
        setupFragments();


        if(savedInstanceState == null){
            //Show CockpitFragment
            replaceFragment(cockpitStatisticsFragment);
        }

        findViewById(R.id.fab_main).setOnClickListener(v -> {
            Intent intent = new Intent(this, BillActivity.class);
            startActivity(intent);
        });

        bottomAppBar = findViewById(R.id.bab_main);
        bottomAppBar.replaceMenu(R.menu.navigation);
        bottomAppBar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()){
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

            return true;
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

    private void setupFragments(){
        cockpitStatisticsFragment = new CockpitStatisticsFragment();
        historyFragment = new HistoryFragment();
        statisticsFragment = new StatisticsFragment();
        databaseFragment = new DatabaseFragment();
        settingsFragment = new SettingsFragment();

    }
}
