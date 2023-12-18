package project.midterm.midtermandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.Manifest;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import project.midterm.midtermandroid.Adapter.UserAdapter;
import project.midterm.midtermandroid.Manager.SessionManager;
import project.midterm.midtermandroid.Model.User;

public class UsersActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private ArrayList<User> users;
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        db = FirebaseFirestore.getInstance();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
        } else {
            getUsersFromFirestore();
        }

        if (!(sessionManager.getUserType().equals("ADMIN"))) {
            Toast.makeText(this, "You don't have permission!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
        } else {
            getUsersFromFirestore();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.users_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        int Id = item.getItemId();
        if (Id == R.id.newUserMenuItem) {
            Intent intent = new Intent(this, NewUserActivity.class);
            startActivity(intent);

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUsersFromFirestore();
            } else {
                Toast.makeText(this, "Permission denied. Cannot access images.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void getUsersFromFirestore() {
        CollectionReference usersCollection = db.collection("users");

        usersCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                users = new ArrayList<>();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        User user = new User();

                        user.setName(document.getString("name"));
                        user.setEmail(document.getString("email"));
                        user.setRole(document.getString("role"));
                        user.setPhoto(document.getString("photo"));
                        user.setStatus(document.getBoolean("status"));

                        getLoginHistoryTimestamp(user);

                        users.add(user);
                    }

                    recyclerView = findViewById(R.id.usersRecyclerView);
                    adapter = new UserAdapter(UsersActivity.this, users);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    recyclerView.setAdapter(adapter);

                } else {
                    Toast.makeText(getApplicationContext(), "There's something wrong!", Toast.LENGTH_SHORT).show();
                    Log.w("Firebase", "Error getting documents.", task.getException());
                }
            }
        });
    }
    private void getLoginHistoryTimestamp(User user) {
        db.collection("users")
                .document(user.getEmail())
                .collection("login_history")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot snapshot = queryDocumentSnapshots.getDocuments().get(0);
                        Timestamp timestamp = snapshot.getTimestamp("timestamp");
                        // Use the timestamp as needed, e.g., store in User object or display
                        // You can set this timestamp to the User object here or pass it to another method
                        // user.setLastLoginTimestamp(timestamp);
                    } else {
                        // If no login history, set a message
                        // user.setLastLoginTimestamp("User doesn't login yet");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }
}