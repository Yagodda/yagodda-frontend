package com.example.yagodda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.yagodda.databinding.ActivityProfileBinding;
import com.example.yagodda.models.User;
import com.example.yagodda.utilits.Constants;
import com.example.yagodda.utilits.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    PreferenceManager preferenceManager;
    ActivityProfileBinding binding;
    private SlidrInterface slidrInterface;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        user = (User) getIntent().getSerializableExtra("user");
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        slidrInterface = Slidr.attach(this);
        loadUserDetails();
        setListeners();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setNavigationBarColor(getResources().getColor(R.color.received_message_bg));
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary));
        }
    }

    private void setListeners(){
        binding.deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("Are you shor about this?");
                builder.setMessage("Deleting is permament...");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteAccount();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                AlertDialog mDialog = builder.create();
                mDialog.show();

            }
        });
        binding.back.setOnClickListener(view -> { onBackPressed();});
        binding.logout.setOnClickListener(v -> {signOut();});
    }

    private void deleteAccount(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        documentReference.delete();
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                });
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()){
//                            showToast("Account deleted");
//                            startActivity(new Intent(getApplicationContext(), MeetingActivity.class));
//                            finish();
//                        }
//                    }
//                });
    }

    private void loadUserDetails(){
        // User Name
        binding.userName.setText(preferenceManager.getString(Constants.KEY_NAME));
        // User Phone
        binding.userPhone.setText(preferenceManager.getString(Constants.KEY_PHONE));
        // User Profile Image
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.profileImage.setImageBitmap(bitmap);
    }
    private void signOut(){
        showToast("Singing out");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToast("Unable to sign out");
                });
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
                .show();
    }
}