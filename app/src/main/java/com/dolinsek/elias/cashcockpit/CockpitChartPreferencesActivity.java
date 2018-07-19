package com.dolinsek.elias.cashcockpit;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dolinsek.elias.cashcockpit.components.Currency;

public class CockpitChartPreferencesActivity extends AppCompatActivity {

    private static final String PREFERENCE_KEY_AMOUNT_TO_SAVE = "preference_amount_to_save";

    private AmountInputFragment mFgmAmountToSaveInput;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cockpit_chart_preferences);

        mFgmAmountToSaveInput = (AmountInputFragment) getSupportFragmentManager().findFragmentById(R.id.fgm_cockpit_chart_preferences_amount);

        String setAmountToSave = Currency.getActiveCurrency(getApplicationContext()).formatAmountToReadableString(getSetAmountToSave());
        mFgmAmountToSaveInput.getEdtAmount().setText(setAmountToSave);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.save_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: finish(); return true;
            case R.id.menu_save: saveChangesIfPossible(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void saveChangesIfPossible(){
        String enteredAmount = mFgmAmountToSaveInput.getEnteredAmountAsString();
        if (!enteredAmount.trim().equals("") && !enteredAmount.trim().equals(".")){
            long formattedAmountToSave = mFgmAmountToSaveInput.getEnteredAmountAsLong();
            saveNewAmountToSave(formattedAmountToSave);
            Toast.makeText(CockpitChartPreferencesActivity.this, getString(R.string.label_new_amount_to_save_saved_successfully), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, getString(R.string.label_check_inputs), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNewAmountToSave(long amountToSave){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        editor.putLong(PREFERENCE_KEY_AMOUNT_TO_SAVE, amountToSave);
        editor.apply();
    }

    private long getSetAmountToSave(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getLong(PREFERENCE_KEY_AMOUNT_TO_SAVE, 10000);
    }
}
