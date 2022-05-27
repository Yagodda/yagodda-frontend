package com.example.yagodda;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yagodda.databinding.ActivitySignInBinding;
import com.example.yagodda.utilits.Constants;
import com.example.yagodda.utilits.PreferenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    ActivitySignInBinding binding;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        progressBarSpinner();


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setNavigationBarColor(getResources().getColor(R.color.primary));
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary));
        }

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValidSignUpDetails()){
                    signIn();
                }
            }
        });


        binding.toSignUp.setOnClickListener(view -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity2.class));
            finish();
        });

    }

    private void progressBarSpinner(){
        ProgressBar spinner = new android.widget.ProgressBar(
                getApplicationContext(),
                null,
                android.R.attr.progressBarStyle);
    }

    private void signIn() {
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_PHONE, "+7" + binding.inputMobile.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocumentChanges().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_PHONE, documentSnapshot.getString(Constants.KEY_PHONE));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else {
                        loading(false);
                        showToast("Unable to sign in");
                    }

                });

        db.collection(Constants.KEY_COLLECTION_USERS)
                .whereNotEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnSuccessListener(task ->
                        binding.inputPassword.setError("Incorrect password")
                );

    }

    private void loading(Boolean isVisible){
        if(isVisible){
            binding.loginBtn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.loginBtn.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
                .show();
    }

    private Boolean isValidSignUpDetails() {
        if (binding.inputMobile.getText().toString().trim().isEmpty()) {
            binding.inputMobile.setError("Enter phone");
            return false;
        } else if (!Patterns.PHONE.matcher(binding.inputMobile.getText().toString()).matches()) {
            binding.inputMobile.setError("Enter valid phone");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            binding.inputPassword.setError("Enter password");
            return false;
        } else{
            return true;
        }
    }
}
