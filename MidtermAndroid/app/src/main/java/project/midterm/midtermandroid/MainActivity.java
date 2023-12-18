package project.midterm.midtermandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import project.midterm.midtermandroid.Manager.SessionManager;
import project.midterm.midtermandroid.StudentManagement.StudentsActivity;

public class MainActivity extends AppCompatActivity {
    private TextView email, role, historyLogin;
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private Button logoutButton, profileButton, userListButton, studentManageButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        email = findViewById(R.id.email);
        role = findViewById(R.id.role);
        historyLogin = findViewById(R.id.historyLogin);
        db = FirebaseFirestore.getInstance();

        email.setText(sessionManager.getEmail());
        role.setText(sessionManager.getUserType());
        displayLoginHistoryTimestamp();

        logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        userListButton = findViewById(R.id.userListButton);
        userListButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, UsersActivity.class);
            startActivity(intent);
        });

        studentManageButton = findViewById(R.id.studentManageButton);
        studentManageButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentsActivity.class);
            startActivity(intent);
        });

    }
    private void displayLoginHistoryTimestamp() {
        String userEmail = sessionManager.getEmail();

        db.collection("users")
                .document(userEmail)
                .collection("login_history")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot snapshot = queryDocumentSnapshots.getDocuments().get(0);
                        Timestamp timestamp = snapshot.getTimestamp("timestamp");

                        if (timestamp != null) { // Check if timestamp is not null
                            // Calculate time ago and set it in TextView
                            String timeAgo = getTimeAgo(timestamp.getSeconds() * 1000);
                            historyLogin.setText("Login " + timeAgo);
                        } else {
                            historyLogin.setText("No login history");
                        }
                    } else {
                        historyLogin.setText("No login history");
                    }
                })
                .addOnFailureListener(e -> {
                    historyLogin.setText("Failed to retrieve login history");
                });
    }

    private String getTimeAgo(long timeInMillis) {
        long now = System.currentTimeMillis();
        long diff = now - timeInMillis;
        return DateUtils.getRelativeTimeSpanString(timeInMillis, now, DateUtils.MINUTE_IN_MILLIS).toString();
    }
}