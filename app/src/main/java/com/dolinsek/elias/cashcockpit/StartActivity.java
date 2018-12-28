package com.dolinsek.elias.cashcockpit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.AutoPayPaymentManager;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Toolkit;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class StartActivity extends AppCompatActivity {

    private static final String PREFERENCE_KEY_RESET_PASSWORD_TIME_STAMP = "resetPasswordTimeStamp";
    private static final String PREFERENCE_KEY_PASSWORD_RESET_OPTION = "preference_password_reset_time_option";
    private static final int PASSWORD_RESET_OPTION_NEVER = 5;
    private static final long[] RESET_OPTIONS_AS_TIME_STAMPS = new long[]{21600000 /*6 Hours*/, 43200000 /*12 Hours*/, 86400000 /*1 Day*/, 259200000 /*3 Days*/, 604800000 /*1 Week*/};

    private EditText edtPassword;
    private Button btnLogin;
    private TextView txvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        edtPassword = findViewById(R.id.edt_start_password);
        btnLogin = findViewById(R.id.btn_start_login);
        txvForgotPassword = findViewById(R.id.txv_forgot_password);

        edtPassword.setOnKeyListener(getOnKeyPressedListener());
        btnLogin.setOnClickListener(getOnLoginButtonClickListener());
        txvForgotPassword.setOnClickListener(getOnForgotPasswordClickListener());

        if (!isPinRequiredForLogin()){
            login();
        }

        long timeStampWhenPasswordGetsReset = getTimeStampWhenPasswordGetsReset();
        if (timeStampWhenPasswordGetsReset != 0){
            setupForPasswordResettingMode();
            if (timeStampWhenPasswordGetsReset <= System.currentTimeMillis()){
                resetPassword();
                login();
            }
        }

        setupViews();
    }

    private void setupViews(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int passwordResetOption = sharedPreferences.getInt(PREFERENCE_KEY_PASSWORD_RESET_OPTION, 0);

        if (isPasswordResetOptionNever(passwordResetOption)){
            txvForgotPassword.setEnabled(false);
        }
    }

    private void login(){
        startMainActivity();
        initDatabase();
        manageAutoPayPayments();
    }

    private void initDatabase(){
        try {
            Database.load(getApplicationContext());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void resetPassword(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putLong(PREFERENCE_KEY_RESET_PASSWORD_TIME_STAMP, 0);
        editor.putBoolean("preference_password_required_for_login", false);
        editor.putString("preference_password_for_login", "");

        editor.commit();

        Toast.makeText(this, getString(R.string.toast_password_got_reset_successfully), Toast.LENGTH_SHORT).show();
    }

    private void setupForPasswordResettingMode(){
        txvForgotPassword.setTextColor(Color.GRAY);
        txvForgotPassword.setText(getTimeUntilPasswordResetAsReadableString());
    }

    private View.OnKeyListener getOnKeyPressedListener(){
        return (v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.KEYCODE_ENTER){
                btnLogin.performClick();

                return true;
            }
            return false;
        };
    }

    private View.OnClickListener getOnLoginButtonClickListener(){
        return v -> {
            if (edtPassword.getText().toString().equals(getPasswordForLogin())){
                login();

                if (getTimeStampWhenPasswordGetsReset() != 0){
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    sharedPreferences.edit().putLong(PREFERENCE_KEY_RESET_PASSWORD_TIME_STAMP, 0).commit();
                }
            } else {
                edtPassword.setText("");
                ((TextInputLayout)findViewById(R.id.til_start)).setError(getString(R.string.label_wrong_password));
            }
        };
    }

    private View.OnClickListener getOnForgotPasswordClickListener(){
        return v -> {
            ResetPasswordDialogFragment resetPasswordDialogFragment = new ResetPasswordDialogFragment();
            resetPasswordDialogFragment.setOnDismissListener(dialog -> {
                if (getTimeStampWhenPasswordGetsReset() != 0){
                    setupForPasswordResettingMode();
                }
            });

            resetPasswordDialogFragment.show(getFragmentManager(), "reset_password");
        };
    }

    private void startMainActivity(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private String getPasswordForLogin(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getString("preference_password_for_login", "");
    }

    private String getTimeUntilPasswordResetAsReadableString(){
        long timeUntilPasswordReset = getTimeStampWhenPasswordGetsReset() - System.currentTimeMillis();

        long daysUntilPasswordReset = timeMillisToDays(timeUntilPasswordReset);
        long hoursUntilPasswordReset = timeMillisToHour(timeUntilPasswordReset) % 24;
        int minutesUntilPasswordReset = timeMillisToMinutes(timeUntilPasswordReset) % 60;

        if (timeUntilPasswordReset > RESET_OPTIONS_AS_TIME_STAMPS[2] /**1 day**/){
            return  getString(R.string.label_days_and_hours_until_password_reset, daysUntilPasswordReset, hoursUntilPasswordReset);
        } else {
            return getString(R.string.label_hours_and_minutes_until_password_reset, hoursUntilPasswordReset, minutesUntilPasswordReset);
        }
    }

    private long getTimeStampWhenPasswordGetsReset(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getLong(PREFERENCE_KEY_RESET_PASSWORD_TIME_STAMP, 0);
    }

    private boolean isPinRequiredForLogin(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getBoolean("preference_password_required_for_login", false);
    }

    private void manageAutoPayPayments(){
        AutoPayPaymentManager autoPayPaymentManager = new AutoPayPaymentManager(getApplicationContext());
        ArrayList<AutoPay> autoPaysWherePaymentsAreRequired = autoPayPaymentManager.getAutoPaysWherePaymentsAreRequired();
        autoPayPaymentManager.performPaymentsForAutoPays(autoPaysWherePaymentsAreRequired);
    }

    private int timeMillisToHour(long timeMillis){
        return (int) (timeMillis / 1000 / 60 / 60);
    }

    private int timeMillisToDays(long timeMillis){
        return (int) (timeMillis / 1000 / 60 / 60) / 24;
    }

    private int timeMillisToMinutes(long timeMillis){
        return (int) (timeMillis / 1000 / 60);
    }

    private boolean isPasswordResetOptionNever(int passwordResetOption){
        return passwordResetOption == PASSWORD_RESET_OPTION_NEVER;
    }

    public static class ResetPasswordDialogFragment extends DialogFragment{

        private DialogInterface.OnDismissListener onDismissListener;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
            alertBuilder.setTitle(R.string.dialog_title_reset_password).setMessage(R.string.dialog_msg_reset_password_conformation);

            alertBuilder.setPositiveButton(R.string.dialog_action_reset, (dialog, which) -> {
                long timeStampAtReset = addResetTimeToTimeStamp(System.currentTimeMillis());
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                sharedPreferences.edit().putLong(PREFERENCE_KEY_RESET_PASSWORD_TIME_STAMP, timeStampAtReset).commit();
            }).setNegativeButton(R.string.dialog_action_cancel, null);

            return alertBuilder.create();
        }

        private long addResetTimeToTimeStamp(long timeStamp){
            return timeStamp + getResetTimeForPassword();
        }

        private long getResetTimeForPassword(){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            int resetOptionIndex = sharedPreferences.getInt(PREFERENCE_KEY_PASSWORD_RESET_OPTION, 0);
            if (resetOptionIndex < 5){
                return RESET_OPTIONS_AS_TIME_STAMPS[resetOptionIndex];
            } else {
                return 0;
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            if (onDismissListener != null){
                onDismissListener.onDismiss(dialog);
            }
        }

        public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
            this.onDismissListener = onDismissListener;
        }
    }
}
