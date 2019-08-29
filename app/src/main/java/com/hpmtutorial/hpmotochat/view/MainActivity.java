package com.hpmtutorial.hpmotochat.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.hpmtutorial.hpmotochat.R;
import com.hpmtutorial.hpmotochat.databinding.ActivityMainBinding;
import com.hpmtutorial.hpmotochat.viewmodel.MainActivityViewModel;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private MainActivityViewModel mainActivityViewModel;
    private TextInputLayout emailLayout, passLayout;
    private TextInputEditText emailEditText, passEditText;
    public static final String TAG = "Main Login";
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mainActivityViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        emailLayout = findViewById(R.id.sign_in_email_layout);
        passLayout = findViewById(R.id.sign_in_pass_layout);
        emailEditText = findViewById(R.id.sign_in_email_edittext);
        passEditText = findViewById(R.id.sign_in_pass_edittext);

        binding.setLifecycleOwner(this);
        binding.setMainViewModel(mainActivityViewModel);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();

        uiObserver();
        fieldsObserve();
    }

    private void fieldsObserve() {
        mainActivityViewModel.fieldErrorLiveData.observe(this, new Observer<MainActivityViewModel.fieldError>() {
            @Override
            public void onChanged(MainActivityViewModel.fieldError fieldError) {
                emailLayout.setError(null);
                passLayout.setError(null);
                switch (fieldError){
                    case ALL:
                        emailLayout.setError("Fill Both Fields!");
                        passLayout.setError("Fill Both Fields!");
                        break;
                    case PASS:
                        passLayout.setError("Fill Password!");
                        break;
                    case EMAIL:
                        emailLayout.setError("Fill Email!");
                        break;
                    case EMAILPATTERN:
                        emailLayout.setError("Please input correct email!");
                        break;
                        default:
                }
            }
        });
    }

    private void uiObserver() {
        mainActivityViewModel.uiChangeMutableLiveData.observe(this, new Observer<MainActivityViewModel.uiChange>() {
            @Override
            public void onChanged(MainActivityViewModel.uiChange uiChange) {
                switch (uiChange){
                    case REGISTER:
                        Intent registerIntent = new Intent(getApplicationContext(), EmailPasswordActivity.class);
                        startActivityForResult(registerIntent,1);
                        break;
                    case SIGNIN:
                        Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(homeIntent);
                        finish();
                        break;
                    case NOUSER:
                        emailEditText.setText(null);
                        passEditText.setText(null);
                        emailLayout.setError(null);
                        passLayout.setError(null);
                        Toast.makeText(MainActivity.this, "Password invalid or user does not exist", Toast.LENGTH_SHORT).show();
                        break;
                        default:

                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_OK) {
            Toast.makeText(this, "Succes! Sign In!", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Failure!", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    public void signIn(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(homeIntent);
            finish();
        }
    }

}
