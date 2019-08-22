package com.hpmtutorial.hpmotochat.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.hpmtutorial.hpmotochat.R;
import com.hpmtutorial.hpmotochat.databinding.ActivityEmailPasswordBinding;
import com.hpmtutorial.hpmotochat.viewmodel.EmailPassViewModel;
import com.hpmtutorial.hpmotochat.viewmodel.MainActivityViewModel;

public class EmailPasswordActivity extends AppCompatActivity {

    private EmailPassViewModel emailPassViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityEmailPasswordBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_email_password);
        emailPassViewModel = ViewModelProviders.of(this).get(EmailPassViewModel.class);

        binding.setLifecycleOwner(this);
        binding.setEmailPassViewModel(emailPassViewModel);

        observerUI();
    }

    private void observerUI() {
        emailPassViewModel.uiChangeMutableLiveData.observe(this, new Observer<MainActivityViewModel.uiChange>() {
            @Override
            public void onChanged(MainActivityViewModel.uiChange uiChange) {
                switch (uiChange){
                    case MAIN:
                        setResult(RESULT_OK);
                        finish();
                        break;
                        default:
                }
            }
        });
    }
}
