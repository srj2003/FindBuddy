package com.example.myapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword, etName;
    private TextView tvLogin;
    private ImageView ivPasswordToggle;
    private ProgressDialog progressDialog;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ivPasswordToggle = findViewById(R.id.ivPasswordToggle);
        Button btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

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

        // Register button click handler
        btnRegister.setOnClickListener(v -> attemptRegistration());

        // Login text click handler
        tvLogin.setOnClickListener(v -> navigateToLogin());
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

    private void attemptRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInputs(name, email, password)) {
            return;
        }

        showProgress("Creating account...");
        registerUser(name, email, password);
    }

    private boolean validateInputs(String name, String email, String password) {
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return false;
        }

        if (password.length() < 8) {
            etPassword.setError("Password must be at least 8 characters");
            return false;
        }

        if (!password.matches(".*[A-Z].*")) {
            etPassword.setError("Password must contain at least one uppercase letter");
            return false;
        }

        if (!password.matches(".*[a-z].*")) {
            etPassword.setError("Password must contain at least one lowercase letter");
            return false;
        }

        if (!password.matches(".*\\d.*")) {
            etPassword.setError("Password must contain at least one number");
            return false;
        }

        return true;
    }

    private void registerUser(String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            updateUserProfile(user, name, email);
                            callFlaskAPI();
                        }
                    } else {
                        hideProgress();
                        Toast.makeText(RegisterActivity.this,
                                "Registration failed: " + getFirebaseErrorMessage(task.getException()),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void callFlaskAPI() {
        String url = "https://my-flask-api-1s94.onrender.com/user";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    Log.d("FlaskAPI", "Response: " + response);
                    // You can parse or use the response if needed
                },
                error -> {
                    Log.e("FlaskAPI", "Error: " + error.toString());
                }
        );

        queue.add(stringRequest);
    }

    private void updateUserProfile(FirebaseUser user, String name, String email) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        sendEmailVerification(user, email);
                    } else {
                        hideProgress();
                        Toast.makeText(RegisterActivity.this,
                                "Profile setup failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user, String email) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    hideProgress();
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this,
                                "Registration successful! Verification email sent to " + email,
                                Toast.LENGTH_LONG).show();
                        verifyCredentials(email);
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Failed to send verification email: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verifyCredentials(String email) {
        showProgress("Verifying credentials...");
        String password = etPassword.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    hideProgress();
                    if (task.isSuccessful()) {
                        navigateToDashboard();
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Automatic verification failed. Please login manually",
                                Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    }
                });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finish();
    }

    private String getFirebaseErrorMessage(Exception exception) {
        if (exception == null) return "Unknown error";
        String error = exception.getMessage();
        if (error.contains("email address is already in use")) {
            return "This email is already registered";
        } else if (error.contains("password is invalid")) {
            return "The password is too weak";
        } else if (error.contains("network error")) {
            return "No internet connection";
        }
        return error;
    }

    private void showProgress(String message) {
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void hideProgress() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}