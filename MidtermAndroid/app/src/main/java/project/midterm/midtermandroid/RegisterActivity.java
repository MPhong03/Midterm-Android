package project.midterm.midtermandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import project.midterm.midtermandroid.Model.User;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText emailRegister, passwordRegister, confirmPasswordRegister, nameRegister;
    private Button registerButton;
    private TextView login;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = FirebaseFirestore.getInstance();

        nameRegister = findViewById(R.id.nameRegister);
        emailRegister = findViewById(R.id.emailRegister);
        passwordRegister = findViewById(R.id.passwordRegister);
        confirmPasswordRegister = findViewById(R.id.confirmPasswordRegister);
        registerButton = findViewById(R.id.registerButton);

        login = findViewById(R.id.loginIntent);
        login.setOnClickListener(view -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        registerButton.setOnClickListener(view -> {
            String name = String.valueOf(nameRegister.getText());
            String email = String.valueOf(emailRegister.getText());
            String password = String.valueOf(passwordRegister.getText());
            String confirm = String.valueOf(confirmPasswordRegister.getText());

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter information", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(this, "Password doesn't match", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users")
                    .document(email)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    // Username is already taken
                                    showToast("Email is already taken. Choose another.");
                                } else {
                                    User user = new User(name, email, password, "ADMIN");

                                    db.collection("users")
                                            .document(email)
                                            .set(user)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        showToast("Registration successful");
                                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        showToast("Failed to register. Please try again.");
                                                    }
                                                }
                                            });
                                }
                            } else {
                                showToast("Failed to check username availability");
                            }
                        }
                    });
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}