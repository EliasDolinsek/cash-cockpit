package com.dolinsek.elias.cashcockpit;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dolinsek.elias.cashcockpit.components.Currency;

public class AmountToSavePreferenceActivity extends AppCompatActivity {

    private static final String PREFERENCE_KEY_AMOUNT_TO_SAVE = "preference_amount_to_save";

    private EditText edtAmountToSave;
    private TextView txvActiveCurrencyShortcut;
    private Button btnSave;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amount_to_save_preference);

        edtAmountToSave = findViewById(R.id.edt_amount_to_save_amount);
        txvActiveCurrencyShortcut = findViewById(R.id.txv_amount_to_save_active_currency_shortcut);
        btnSave = findViewById(R.id.btn_amount_to_save_save);

        displayActiveCurrencyShortcut();
        setupEdtAmountToSave();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredAmount = edtAmountToSave.getText().toString();
                if (!enteredAmount.trim().equals("") || enteredAmount.trim().equals(".")){
                    long formattedAmountToSave = (long) (Double.valueOf(enteredAmount) * 100);
                    saveNewAmountToSave(formattedAmountToSave);
                    Toast.makeText(AmountToSavePreferenceActivity.this, getString(R.string.label_new_amount_to_save_saved_successfully), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void displayActiveCurrencyShortcut(){
        String currencyShortcut = Currency.getActiveCurrency(getApplicationContext()).getCurrencyShortcut();
        txvActiveCurrencyShortcut.setText(currencyShortcut);
    }

    private void setupEdtAmountToSave(){
        long setAmountToSave = getSetAmountToSave();
        TextWatcher textWatcher = Currency.getActiveCurrency(getApplicationContext()).getCurrencyTextWatcher(edtAmountToSave);

        edtAmountToSave.addTextChangedListener(textWatcher);
        edtAmountToSave.setText(Currency.getActiveCurrency(getApplicationContext()).formatAmountToReadableString(setAmountToSave));
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
