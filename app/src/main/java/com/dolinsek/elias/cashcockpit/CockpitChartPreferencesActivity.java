package com.dolinsek.elias.cashcockpit;

import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
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
import com.dolinsek.elias.cashcockpit.components.Toolkit;

public class CockpitChartPreferencesActivity extends AppCompatActivity {

    private static final String PREFERENCE_KEY_AMOUNT_TO_SAVE = "preference_amount_to_save";

    private TextInputLayout tilAmount;
    private TextInputEditText edtAmount;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cockpit_chart_preferences);

        tilAmount = findViewById(R.id.til_cockpit_chart_preferences_amount);
        edtAmount = findViewById(R.id.edt_cockpit_chart_preferences_amount);

        String setAmountToSave = Currency.getActiveCurrency(getApplicationContext()).formatAmountToReadableString(getSetAmountToSave());
        edtAmount.setText(setAmountToSave);

        findViewById(R.id.btn_cockpit_chart_preferences_save).setOnClickListener(view -> saveChangesIfPossible());
    }

    private void saveChangesIfPossible(){
        String enteredAmount = getEnteredAmountAsString();
        long enteredAmountAsLong = getEnteredAmountAsLong();
        if (!enteredAmount.trim().equals("") && !enteredAmount.trim().equals(".") && enteredAmountAsLong != 0){
            saveNewAmountToSave(enteredAmountAsLong);
            Toast.makeText(CockpitChartPreferencesActivity.this, getString(R.string.toast_amount_to_save_got_saved_successfully), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            tilAmount.setError(getString(R.string.label_enter_valid_amount));
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

    private long getEnteredAmountAsLong(){
        try {
            String enteredAmount = edtAmount.getText().toString();
            return (long) (Double.valueOf(enteredAmount) * 100);
        } catch (Exception e){
            return 0;
        }
    }

    private String getEnteredAmountAsString(){
        return edtAmount.getText().toString();
    }
}
