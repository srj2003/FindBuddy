package com.example.myapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private TextView tvRegister, tvForgotPassword;
    private ImageView ivPasswordToggle;
    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;
    private boolean isPasswordVisible = false; // Moved to class level

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ivPasswordToggle = findViewById(R.id.ivPasswordToggle);
        Button btnLogin = findViewById(R.id.btnLogin);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Set initial password visibility state
        etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        ivPasswordToggle.setImageResource(R.drawable.ic_visibility_off);

        // Password toggle click listener
        ivPasswordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        // Register click listener
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Forgot password click listener
        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter your email first", Toast.LENGTH_SHORT).show();
            } else {
                resetUserPassword(email);
            }
        });

        // Login click listener
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInputs(email, password)) {
                verifyCredentialsWithDatabase(email, password);
            }
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ivPasswordToggle.setImageResource(R.drawable.ic_visibility_off);
        } else {
            // Show password
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            ivPasswordToggle.setImageResource(R.drawable.ic_visibility);
        }
        isPasswordVisible = !isPasswordVisible;
        // Move cursor to the end of the text
        etPassword.setSelection(etPassword.getText().length());
    }

    private void resetUserPassword(String email) {
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Sending reset instructions...");
        progressDialog.show();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this,
                                    "Reset password instructions have been sent to your email",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void verifyCredentialsWithDatabase(String email, String password) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Verifying credentials...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        startActivity(new Intent(this, DashboardActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Login failed: Invalid credentials",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}