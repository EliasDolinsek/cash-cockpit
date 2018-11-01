package com.dolinsek.elias.cashcockpit;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneSignInActivity extends AppCompatActivity {

    private static final int RC_PHONE_PERMISSION = 57;

    private TextInputLayout tilPhoneNumber, tilVerificationCode;
    private EditText edtPhoneNumber, edtVerificationNumber;
    private Button btnContinueSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_sign_in);

        tilPhoneNumber = findViewById(R.id.til_phone_sign_in_phone_number);
        edtPhoneNumber = findViewById(R.id.edt_phone_sign_in_phone_number);

        tilVerificationCode = findViewById(R.id.til_phone_sign_in_verification_code);
        edtVerificationNumber = findViewById(R.id.edt_phone_sign_in_verification_number);

        btnContinueSignIn = findViewById(R.id.btn_phone_sing_in_continue_sign_in);
        findViewById(R.id.btn_phone_sing_in_cancel).setOnClickListener(view -> finish());

        edtPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String editableAsString = editable.toString();
                boolean phoneNumberValid = PhoneNumberUtils.isGlobalPhoneNumber(editableAsString) && editableAsString.length() == 14 || editableAsString.length() == 0;

                btnContinueSignIn.setEnabled(phoneNumberValid);
                tilPhoneNumber.setErrorEnabled(phoneNumberValid);
                tilPhoneNumber.setError(phoneNumberValid ? "" : "TODO (Phone number invalid)");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadPhoneNumberIntoEdt();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_PHONE_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            loadPhoneNumberIntoEdt();
        }
    }

    private void loadPhoneNumberIntoEdt() {
        edtPhoneNumber.setText(getPhoneNumber());
    }

    private String getPhoneNumber() {
        TelephonyManager tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            return tMgr.getLine1Number();
        } else {
            ActivityCompat.requestPermissions(PhoneSignInActivity.this, new String[]{android.Manifest.permission.READ_PHONE_STATE}, RC_PHONE_PERMISSION);
            return null;
        }
    }

    private void sendPhoneVerification(String phoneNumber){
        PhoneAuthProvider.OnVerificationStateChangedCallbacks onVerificationStateChangedCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

            }
        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, onVerificationStateChangedCallbacks);
    }
}
