package com.dolinsek.elias.cashcockpit;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

public class PasswordPreferenceActivity extends AppCompatActivity {

    private static final String PREFERENCE_PASSWORD_RESET_TIME_OPTION = "preference_password_reset_time_option";

    private EditText edtCurrentPassword, edtNewPassword, edtNewPasswordConfirmation;
    private TextInputLayout tilCurrentPassword, tilNewPassword, tilNewPasswordConfirmation;
    private Spinner spnPasswordResetTime;
    private Switch swUsePasswordForLogin;
    private Button btnSaveNewPassword, btnSaveSecuritySettings;
    private int selectedPasswordResetTimeOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_preference);

        edtCurrentPassword = findViewById(R.id.edt_password_preference_current_password);
        edtNewPassword = findViewById(R.id.edt_password_preference_new_password);
        edtNewPasswordConfirmation = findViewById(R.id.edt_password_preference_new_password_confirmation);

        tilCurrentPassword = findViewById(R.id.til_password_preference_current_password);
        tilNewPassword = findViewById(R.id.til_password_preference_new_password);
        tilNewPasswordConfirmation = findViewById(R.id.til_password_preference_new_password_confirmation);

        spnPasswordResetTime = findViewById(R.id.spn_password_preference_password_reset_time);
        swUsePasswordForLogin = findViewById(R.id.sw_password_preference_use_password_for_login);

        btnSaveNewPassword = findViewById(R.id.btn_password_preference_save_new_password);
        btnSaveSecuritySettings = findViewById(R.id.btn_password_preference_save_security_settings);

        setupCurrentPasswordViews();
        setupSpnPasswordResetTime();
        setupSwUsePasswordForLogin();

        btnSaveNewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredCurrentPassword = edtCurrentPassword.getText().toString();
                String enteredNewPassword = edtNewPassword.getText().toString();

                hideAllErrors();
                if (!doesEnteredCurrentPasswordMatchWitchCurrentSetPassword(enteredCurrentPassword)){
                    displayCurrentEnteredPasswordError();
                } else if (!doEnteredNewPasswordAndEnteredConformationOfNewPasswordMatch()){
                    displayNewPasswordViewsErrors();
                } else if(enteredNewPassword.equals("")){
                    displayNoNewPasswordEnteredError();
                } else {
                    saveNewPassword();
                    displayToastThatNewPasswordGotSavedSuccessfully();
                    removeInputsFromEditTexts();
                }
            }
        });

        btnSaveSecuritySettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSecuritySettings();
                displayToastThatSecuritySettingsGotSavedSuccessfully();
            }
        });
    }

    private void saveNewPassword(){
        String newEnteredPassword = edtNewPassword.getText().toString();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        editor.putString("preference_password_for_login", newEnteredPassword);
        editor.apply();
    }

    private void setupCurrentPasswordViews(){
        boolean isAPasswordSet = isAPasswordSet();

        edtCurrentPassword.setEnabled(isAPasswordSet);
        tilCurrentPassword.setEnabled(isAPasswordSet);
    }

    private boolean isAPasswordSet(){
        return !getPasswordForLogin().equals("");
    }

    private boolean doesEnteredCurrentPasswordMatchWitchCurrentSetPassword(String enteredPassword){
        return getPasswordForLogin().equals(enteredPassword);
    }

    private boolean doEnteredNewPasswordAndEnteredConformationOfNewPasswordMatch(){
        String enteredNewPassword = edtNewPassword.getText().toString();
        String enteredNewPasswordConformation = edtNewPasswordConfirmation.getText().toString();

        return enteredNewPassword.equals(enteredNewPasswordConformation);
    }

    private String getPasswordForLogin(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getString("preference_password_for_login", "");
    }

    private void displayNewPasswordViewsErrors(){
        tilNewPassword.setErrorEnabled(true);
        tilNewPasswordConfirmation.setErrorEnabled(true);

        tilNewPassword.setError(getString(R.string.label_entered_passwords_dont_match));
        tilNewPasswordConfirmation.setError(getString(R.string.label_entered_passwords_dont_match));
    }

    private void displayCurrentEnteredPasswordError(){
        tilCurrentPassword.setErrorEnabled(true);
        tilCurrentPassword.setError(getString(R.string.label_wrong_password));
    }

    private void displayNoNewPasswordEnteredError(){
        tilNewPassword.setErrorEnabled(true);
        tilNewPassword.setError(getString(R.string.label_please_enter_a_password));
    }

    private void hideNewPasswordViewsErrors(){
        tilNewPassword.setError(null);
        tilNewPasswordConfirmation.setError(null);

        tilNewPassword.setErrorEnabled(false);
        tilNewPasswordConfirmation.setErrorEnabled(false);
    }

    private void hideCurrentEnteredPasswordError(){
        tilCurrentPassword.setError(null);
        tilCurrentPassword.setErrorEnabled(false);
    }

    private void hideNoNewPasswordEnteredError(){
        tilNewPassword.setError(null);
        tilNewPassword.setErrorEnabled(false);
    }

    private void removeInputsFromEditTexts(){
        edtCurrentPassword.setText("");
        edtNewPassword.setText("");
        edtNewPasswordConfirmation.setText("");
    }

    private void hideAllErrors(){
        hideNewPasswordViewsErrors();
        hideCurrentEnteredPasswordError();
        hideNoNewPasswordEnteredError();
    }

    private void saveSecuritySettings(){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        editor.putBoolean("preference_password_required_for_login", swUsePasswordForLogin.isChecked());
        editor.putInt(PREFERENCE_PASSWORD_RESET_TIME_OPTION, selectedPasswordResetTimeOption);
        editor.apply();
    }

    private void setupSpnPasswordResetTime(){
        ArrayAdapter<String> passwordResetTimesAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.password_reset_time_options));
        passwordResetTimesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnPasswordResetTime.setAdapter(passwordResetTimesAdapter);
        spnPasswordResetTime.setSelection(getPasswordResetTimeOption());
        spnPasswordResetTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPasswordResetTimeOption = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupSwUsePasswordForLogin(){
        swUsePasswordForLogin.setChecked(getIfPasswordIsRequiredForLogin());
    }

    private boolean getIfPasswordIsRequiredForLogin(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getBoolean("preference_password_required_for_login", false);
    }

    private int getPasswordResetTimeOption(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getInt(PREFERENCE_PASSWORD_RESET_TIME_OPTION, 0);
    }

    private void displayToastThatNewPasswordGotSavedSuccessfully(){
        Toast.makeText(getApplicationContext(), getString(R.string.label_new_password_got_saved_successfully), Toast.LENGTH_SHORT).show();
    }

    private void displayToastThatSecuritySettingsGotSavedSuccessfully(){
        Toast.makeText(getApplicationContext(), getString(R.string.label_security_settings_got_saved_successfully), Toast.LENGTH_SHORT).show();
    }
}
