package com.hpmtutorial.hpmotochat.viewmodel;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hpmtutorial.hpmotochat.model.User;

public class EmailPassViewModel extends ViewModel {

    public static final String TAG = "Register";

//    MUTABLE LIVE DATA
    public MutableLiveData<String> email = new MutableLiveData<>(),
            password = new MutableLiveData<>(),
            passwordCheck = new MutableLiveData<>();
    public MutableLiveData<MainActivityViewModel.uiChange> uiChangeMutableLiveData = new MutableLiveData<>();
//    ################

//    FIREBASE
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
//    ################

    public void onRegisterClick() {
        mAuth.createUserWithEmailAndPassword(email.getValue(), password.getValue())
                .addOnCompleteListener(/*executor, */ new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
//                            add user to db
                            Log.d("UserNameRegister", "onComplete: " + email.getValue());
                            User newUser = new User(email.getValue());
                            RootRef.child("Users").child(mAuth.getCurrentUser().getUid()).setValue(newUser);
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            uiChangeMutableLiveData.setValue(MainActivityViewModel.uiChange.MAIN);

//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                            updateUI(null);
                        }
                    }
                });
    }
}
