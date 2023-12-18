package project.midterm.midtermandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import project.midterm.midtermandroid.Manager.SessionManager;
import project.midterm.midtermandroid.Model.User;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText emailLogin, passwordLogin;
    private Button loginButton;
    private TextView register;
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        emailLogin = findViewById(R.id.emailRegister);
        passwordLogin = findViewById(R.id.passwordRegister);
        loginButton = findViewById(R.id.registerButton);
        register = findViewById(R.id.registerIntent);

        if (sessionManager.isLoggedIn()) {
            handleLoginSuccess(emailLogin.getText().toString(), sessionManager.getUserType());
        }

        register.setOnClickListener(view -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        loginButton.setOnClickListener(view -> {
            String email = String.valueOf(emailLogin.getText());
            String password = String.valueOf(passwordLogin.getText());

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter information", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users")
                    .document(email)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                User user = document.toObject(User.class);
                                if (user != null && user.getPassword().equals(password)) {
                                    // Password is correct, login successful
                                    String userType = user.getRole();
                                    sessionManager.createLoginSession(email, userType);
                                    handleLoginSuccess(email, userType);
                                } else {
                                    // Incorrect password
                                    showToast("Incorrect password");
                                }
                            } else {
                                // User does not exist
                                showToast("User not found");
                            }
                        } else {
                            // Firestore query failed
                            showToast("Failed to retrieve user data");
                        }
                    });
        });
    }
    private void handleLoginSuccess(String userEmail, String userType) {
        // Handle the login success based on user type
        if ("admin".equals(userType)) {
            // Admin login logic
            showToast("Admin login successful");
        } else if ("manager".equals(userType)) {
            // Manager login logic
            showToast("Manager login successful");
        } else if ("employee".equals(userType)) {
            // Employee login logic
            showToast("Employee login successful");
        }

        // Add login event to Firestore
        Map<String, Object> loginEventData = new HashMap<>();
        loginEventData.put("timestamp", FieldValue.serverTimestamp());
        // Add other relevant login event data like device info, IP address, etc.

        db.collection("users")
                .document(userEmail)
                .collection("login_history")
                .add(loginEventData)
                .addOnSuccessListener(documentReference -> {
                    showToast("Login event added to history");
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to add login event: " + e.getMessage());
                });

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}