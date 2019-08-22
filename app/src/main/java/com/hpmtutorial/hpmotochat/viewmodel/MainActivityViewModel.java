package com.hpmtutorial.hpmotochat.viewmodel;

import android.util.Log;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivityViewModel extends ViewModel {

    public static final String TAG = "SIGN IN";


    public enum uiChange {
        REGISTER,
        SIGNIN,
        HOME,
        NOUSER,
        MAIN
    }

    public enum fieldError {
        ALL,
        EMAIL,
        PASS,
        EMAILPATTERN,
        OK
    }

    public MutableLiveData<String> email = new MutableLiveData<>(),
            password = new MutableLiveData<>();
    public MutableLiveData<uiChange> uiChangeMutableLiveData = new MutableLiveData<>();
    public MutableLiveData<Boolean> googleSignIn = new MutableLiveData<>();
    public MutableLiveData<fieldError> fieldErrorLiveData = new MutableLiveData<>();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public void onRegisterClick() {
        uiChangeMutableLiveData.setValue(uiChange.REGISTER);
    }

    public void onSignInClick() {
        if (email.getValue() == null && password.getValue() == null) {
            fieldErrorLiveData.setValue(fieldError.ALL);
        } else if (email.getValue() == null) {
            fieldErrorLiveData.setValue(fieldError.EMAIL);
        } else if (password.getValue() == null) {
            fieldErrorLiveData.setValue(fieldError.PASS);
        } else {
            if (!Patterns.EMAIL_ADDRESS.matcher(email.getValue()).matches()) {
                fieldErrorLiveData.setValue(fieldError.EMAILPATTERN);
            } else {
                mAuth.signInWithEmailAndPassword(email.getValue(), password.getValue())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    uiChangeMutableLiveData.setValue(uiChange.SIGNIN);
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    uiChangeMutableLiveData.setValue(uiChange.NOUSER);
                                }
                            }
                        });
            }
        }
    }
}
