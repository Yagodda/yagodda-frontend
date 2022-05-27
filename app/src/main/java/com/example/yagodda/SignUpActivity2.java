package com.example.yagodda;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.yagodda.databinding.ActivitySignUp2Binding;
import com.example.yagodda.utilits.PreferenceManager;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SignUpActivity2 extends AppCompatActivity {

    ProgressDialog dialog;
    Button btnSignUp;
    FirebaseAuth firebaseAuth;
    ActivitySignUp2Binding binding;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUp2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.primary));
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary));
        }


        binding.toSignIn.setOnClickListener(view -> {
            startActivity(new Intent(SignUpActivity2.this, SignInActivity.class));
        });

        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValid()) {
                    signUp();
                }
            }
        });
    }

    private boolean isValid() {
        if (binding.inputMobile.getText().toString().isEmpty()) {
            showToast("Enter Your Phone Number");
            return false;
        } else if (!Patterns.PHONE.matcher(binding.inputMobile.getText().toString()).matches()) {
            showToast("Enter valid phone");
            return false;
        }
        return true;
    }

    private void signUp() {
        loading(true);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+7" + binding.inputMobile.getText().toString(),
                60,
                TimeUnit.SECONDS,
                SignUpActivity2.this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        binding.progressBar.setVisibility(View.GONE);
                        btnSignUp.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        binding.progressBar.setVisibility(View.GONE);
                        btnSignUp.setVisibility(View.VISIBLE);
                        Toast.makeText(SignUpActivity2.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        Intent intent = new Intent(SignUpActivity2.this, VerifyOTPActivity.class);
                        intent.putExtra("mobile", binding.inputMobile.getText().toString());
                        intent.putExtra("verificationId", verificationId);
                        startActivity(intent);
                        finish();
                    }
                }
        );


    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
                .show();
    }

    private void loading(Boolean isVisible) {
        if (isVisible) {
            binding.btnSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.btnSignUp.setVisibility(View.VISIBLE);
        }
    }
}