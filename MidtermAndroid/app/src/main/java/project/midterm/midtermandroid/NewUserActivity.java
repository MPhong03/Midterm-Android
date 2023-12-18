package project.midterm.midtermandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import project.midterm.midtermandroid.Manager.SessionManager;
import project.midterm.midtermandroid.Model.User;

public class NewUserActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private TextInputEditText name, email, phone, age, password;
    private Switch status;
    private RadioGroup role;
    private CircleImageView photo;
    private Uri photoUri;
    private Button create;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        db = FirebaseFirestore.getInstance();

        name = findViewById(R.id.nameNewUser);
        email = findViewById(R.id.emailNewUser);
        phone = findViewById(R.id.phoneNewUser);
        age = findViewById(R.id.ageNewUser);
        password = findViewById(R.id.passwordNewUser);
        status = findViewById(R.id.statusNewUser);
        role = findViewById(R.id.roleNewUser);
        photo = findViewById(R.id.photoNewUser);
        create = findViewById(R.id.createUser);

        photo.setOnClickListener(v -> {
            openImageChooser();
        });

        create.setOnClickListener(v -> {
            User user = new User();
            user.setName(name.getText().toString());
            user.setEmail(email.getText().toString());
            user.setPhone(phone.getText().toString());
            user.setAge(Long.valueOf(age.getText().toString()));
            user.setPassword(password.getText().toString());
            user.setStatus(status.isChecked());

            String userRole = "UNDEFINED";
            int radio = role.getCheckedRadioButtonId();
            if (radio == R.id.adminRadioUser) {
                userRole = "ADMIN";
            } else if (radio == R.id.managerRadioUser) {
                userRole = "MANAGER";
            } else if (radio == R.id.employeeRadioUser) {
                userRole = "EMPLOYEE";
            }

            user.setRole(userRole);
            user.setPhoto(photoUri.toString());

            onCreateUser(user);
        });

        if (!(sessionManager.getUserType().equals("ADMIN"))) {
            Toast.makeText(this, "You don't have permission!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void onCreateUser(User user) {
        String email = user.getEmail();
        String name = user.getName();
        String password = user.getPassword();
        String role = user.getRole();
        boolean status = user.getStatus();

        // Check for required fields
        if (email.isEmpty() || name.isEmpty() || password.isEmpty() || role.isEmpty()) {
            showToast("Email, name, password, and role cannot be empty");
            return;
        }

        // Create a reference to the Firebase Storage location
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/" + email + ".jpg");

        // Check if the photo is selected
        if (photoUri != null) {
            // Upload the photo to Firebase Storage
            profileImageRef.putFile(photoUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL of the uploaded photo
                        profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Set the downloaded URL to the user object
                            user.setPhoto(uri.toString());

                            // Add the user to the Firestore collection
                            addUserToFirestore(user);
                        }).addOnFailureListener(e -> {
                            showToast("Failed to upload photo");
                        });
                    })
                    .addOnFailureListener(e -> {
                        showToast("Failed to upload photo");
                    });
        } else {
            // No photo selected, proceed to add user without photo
            addUserToFirestore(user);
        }
    }

    private void addUserToFirestore(User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Add the user to the "users" collection
        db.collection("users")
                .document(user.getEmail())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    showToast("User added successfully!");
                    // Redirect or perform actions after adding user
                    Intent intent = new Intent(this, UsersActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to add user: " + e.getMessage());
                });
    }
    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_PICK);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null && data.getData() != null) {
                photoUri = data.getData();
                Glide.with(this).load(photoUri).into(photo);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE && data != null && data.getExtras() != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                Uri imageUri = getImageUri(this, imageBitmap);
                Glide.with(this).load(imageUri).into(photo);
            }
        }
    }
    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}