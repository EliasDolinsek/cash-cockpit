package com.dolinsek.elias.cashcockpit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.Database;

import java.util.Calendar;

public class StartActivity extends AppCompatActivity {

    private static final String TAG = StartActivity.class.getSimpleName();
    private static final String PREFERENCE_KEY_RESET_PASSWORD_TIME_STAMP = "resetPasswordTimeStamp";

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
    }

    private void login(){
        initDatabase();
        startMainActivity();
    }

    private void resetPassword(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putLong(PREFERENCE_KEY_RESET_PASSWORD_TIME_STAMP, 0);
        editor.putBoolean("preference_password_required_for_login", false);
        editor.putString("preference_password_for_login", "");

        editor.commit();
    }

    private void setupForPasswordResettingMode(){
        txvForgotPassword.setTextColor(Color.GRAY);
        txvForgotPassword.setText(getTimeUntilPasswordResetAsReadableString());
    }

    private View.OnKeyListener getOnKeyPressedListener(){
        return new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.KEYCODE_ENTER){
                    btnLogin.performClick();

                    return true;
                }
                return false;
            }
        };
    }

    private View.OnClickListener getOnLoginButtonClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtPassword.getText().toString().equals(getPasswordForLogin())){
                    login();

                    if (getTimeStampWhenPasswordGetsReset() != 0){
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        sharedPreferences.edit().putLong(PREFERENCE_KEY_RESET_PASSWORD_TIME_STAMP, 0).commit();
                    }
                } else {
                    edtPassword.setText("");
                }
            }
        };
    }

    private View.OnClickListener getOnForgotPasswordClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResetPasswordDialogFragment resetPasswordDialogFragment = new ResetPasswordDialogFragment();
                resetPasswordDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (getTimeStampWhenPasswordGetsReset() != 0){
                            setupForPasswordResettingMode();
                        }
                    }
                });
                resetPasswordDialogFragment.show(getFragmentManager(), "reset_password");
            }
        };
    }

    private void startMainActivity(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void initDatabase(){
        try {
            Database.load(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
            if (Database.getPrimaryCategories().size() == 0){
                restorePrimaryCategories();
            }
        }
    }

    private String getPasswordForLogin(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getString("preference_password_for_login", "");
    }

    private String getTimeUntilPasswordResetAsReadableString(){
        long timeUntilPasswordReset = getTimeStampWhenPasswordGetsReset() - System.currentTimeMillis();
        return timeMillisToHour(timeUntilPasswordReset) + " " + getString(R.string.label_hours_until_password_reset);
    }

    private long getTimeStampWhenPasswordGetsReset(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getLong(PREFERENCE_KEY_RESET_PASSWORD_TIME_STAMP, 0);
    }

    private boolean isPinRequiredForLogin(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getBoolean("preference_password_required_for_login", false);
    }

    private void restorePrimaryCategories(){
        Database.setPrimaryCategories(Database.getDefaultPrimaryCategories());
    }

    private int timeMillisToHour(long timeMillis){
        return (int) (timeMillis / 1000 / 60 / 60);
    }

    public static class ResetPasswordDialogFragment extends DialogFragment{

        private DialogInterface.OnDismissListener onDismissListener;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
            alertBuilder.setTitle(R.string.dialog_title_reset_password).setMessage(R.string.dialog_msg_reset_password_description);

            alertBuilder.setPositiveButton(R.string.dialog_action_reset_password, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    long timeStampAtReset = addTwoHoursToTimeStamp(System.currentTimeMillis());
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    sharedPreferences.edit().putLong(PREFERENCE_KEY_RESET_PASSWORD_TIME_STAMP, timeStampAtReset).commit();
                }
            }).setNegativeButton(R.string.dialog_action_cancel, null);

            return alertBuilder.create();
        }

        private long addTwoHoursToTimeStamp(long timeStamp){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeStamp);
            calendar.add(Calendar.HOUR, 12);

            return calendar.getTimeInMillis();
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
