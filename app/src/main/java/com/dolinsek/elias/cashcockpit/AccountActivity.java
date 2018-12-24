package com.dolinsek.elias.cashcockpit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class AccountActivity extends AppCompatActivity {

    private static final String HELP_EMAIL_KEY = "help_email";

    private TextView txvEmail;
    private FirebaseUser firebaseUser;
    private Button btnSignInOut, btnDeleteData, btnHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        txvEmail = findViewById(R.id.txv_account_email);

        btnSignInOut = findViewById(R.id.btn_account_sing_in_out);
        btnDeleteData = findViewById(R.id.btn_account_delete_data);
        btnHelp = findViewById(R.id.btn_account_help);

        btnDeleteData.setOnClickListener(v -> showDeleteDataDialogFragment());
        btnHelp.setOnClickListener(v -> openEmailApp());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null && !firebaseAuth.getCurrentUser().isAnonymous()){
            firebaseUser = firebaseAuth.getCurrentUser();
            setupForSignedInStatus();
        } else {
            setupForNotSignedInStatus();
        }
    }

    private String getEmailForRemoteConfig(){
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setDefaults(R.xml.remote_config_defaults);
        remoteConfig.fetch().addOnSuccessListener(aVoid -> remoteConfig.activateFetched());

        return remoteConfig.getString(HELP_EMAIL_KEY);
    }

    private void setupForSignedInStatus(){
        txvEmail.setText(firebaseUser.getEmail());
        btnSignInOut.setText(getString(R.string.btn_sing_out));
        btnSignInOut.setOnClickListener(view -> signOut(true));
    }

    private void showDeleteDataDialogFragment(){
        new DeleteDataDialogFragment()
                .show(getSupportFragmentManager(), "delete_data");
    }

    private void setupForNotSignedInStatus(){
        txvEmail.setText(getString(R.string.label_anonymously));

        btnSignInOut.setText(getString(R.string.btn_sing_in));
        btnSignInOut.setOnClickListener(view -> singIn());
        btnHelp.setEnabled(false);
    }

    private void signOut(boolean showToasts) {
        AuthUI.getInstance().signOut(this).addOnCompleteListener(task -> {
            if (showToasts){
                if(task.isSuccessful()){
                    Toast.makeText(this, getString(R.string.toast_singed_out_successfully), Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, getString(R.string.toast_something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void singIn(){
        finish();
        startActivity(new Intent(this, SignInActivity.class));
    }

    private void openEmailApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getEmailForRemoteConfig()});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Help with CashCockpit");

        startActivity(Intent.createChooser(intent, "Send Email"));
    }
}
