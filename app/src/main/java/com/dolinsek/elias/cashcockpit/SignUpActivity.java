package com.dolinsek.elias.cashcockpit;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.answers.SignUpEvent;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.SignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignUpActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN_GOOGLE = 523;
    private static final int RC_SIGN_IN_PHONE = 306;

    private SignInButton googleSignInButton;
    private Button btnSignInEmail, btnSignInPhone;
    private TextView txvContinueWithoutLogin;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();

        googleSignInButton = findViewById(R.id.btn_sign_up_google);
        btnSignInEmail = findViewById(R.id.btn_sign_up_email);
        btnSignInPhone = findViewById(R.id.btn_sign_up_phone);
        txvContinueWithoutLogin = findViewById(R.id.txv_sign_up_continue_without_sign_in);

        googleSignInButton.setOnClickListener(view -> signInWithGoogle());
        btnSignInPhone.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpActivity.this, PhoneSignInActivity.class);
            startActivityForResult(intent, RC_SIGN_IN_PHONE);
        });

        txvContinueWithoutLogin.setOnClickListener(view -> firebaseAuth.signInAnonymously());
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null){
            //TODO go back to main activity
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_SIGN_IN_GOOGLE){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                //TODO show message that sign in with google wasn't successful
                Log.e(SignInAccount.class.getName(), e.getMessage());
            }
        }
    }

    private void signInWithGoogle(){
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        Intent signInWithGoogleIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInWithGoogleIntent, RC_SIGN_IN_GOOGLE);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(task -> {
            if (!task.isSuccessful()){
                //TODO show message that sign in with google wasn't successful
                System.out.println("Sign in wasn't successful");
            }
        });
    }

}
