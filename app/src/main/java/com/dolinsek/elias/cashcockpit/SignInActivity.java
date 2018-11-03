package com.dolinsek.elias.cashcockpit;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.Arrays;
import java.util.List;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 227;
    private static final String PRIVACY_STATEMENT_LINK = "privacy_statement_link";
    public static final String EXTRA_SHOW_TUTORIAL_ACTIVITY_AFTERWADS = "showVideoTutorialAfterwards";

    private FirebaseRemoteConfig firebaseRemoteConfig;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        firebaseRemoteConfig.fetch().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                firebaseRemoteConfig.activateFetched();
                setPrivacyStatement();
            }
        });

        findViewById(R.id.btn_sign_in_sign_in).setOnClickListener(view -> {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.PhoneBuilder().build()
            );

            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setLogo(R.drawable.ic_account_circle)
                    .setTheme(R.style.AppTheme).build()
                    , RC_SIGN_IN);
        });

        findViewById(R.id.btn_sign_in_continue_without_sign_in).setOnClickListener(view -> {
            FirebaseAuth.getInstance().signInAnonymously();
            showTutorialActivityOrFinish();
        });

        setPrivacyStatement();
    }

    private void setPrivacyStatement(){
        String privacyStatementAsHTML = getString(R.string.label_agree_to_cash_cockpit_privacy_statement_beginning) + " <a href=" + getPrivacyStatementLink() + "><font color='blue'>Privacy Statement</font></a>";
        TextView txvPrivacyStatement = findViewById(R.id.txv_sign_in_privacy_statement);

        txvPrivacyStatement.setText(Html.fromHtml(privacyStatementAsHTML), TextView.BufferType.SPANNABLE);
        txvPrivacyStatement.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getPrivacyStatementLink()));
            startActivity(intent);
        });
    }

    private String getPrivacyStatementLink() {
        return firebaseRemoteConfig.getString(PRIVACY_STATEMENT_LINK);
    }

    private void showTutorialActivityOrFinish(){
        finish();
        if (isTutorialActivityRequested()){
            showTutorialActivity();
        }
    }

    private boolean isTutorialActivityRequested(){
        return getIntent().hasExtra(EXTRA_SHOW_TUTORIAL_ACTIVITY_AFTERWADS) && getIntent().getBooleanExtra(EXTRA_SHOW_TUTORIAL_ACTIVITY_AFTERWADS, false);
    }

    private void showTutorialActivity(){
        Intent intent = new Intent(this, TutorialActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK){
            showTutorialActivityOrFinish();
        }
    }
}
