package project.midterm.midtermandroid;

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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import project.midterm.midtermandroid.Manager.SessionManager;
import project.midterm.midtermandroid.Model.User;

public class EditUserActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private FirebaseFirestore db;
    private TextInputEditText name, email, phone, age, password;
    private StorageReference storageRef;
    private Switch status;
    private RadioGroup role;
    private CircleImageView photo;
    private Uri photoUri;
    private Button saveChanges;
    private String currentPhoto;
    private SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        name = findViewById(R.id.nameEditUser);
        email = findViewById(R.id.emailEditUser);
        phone = findViewById(R.id.phoneEditUser);
        age = findViewById(R.id.ageEditUser);
        password = findViewById(R.id.passwordEditUser);
        status = findViewById(R.id.statusEditUser);
        role = findViewById(R.id.roleEditUser);
        photo = findViewById(R.id.photoEditUser);
        saveChanges = findViewById(R.id.editUser);

        // Get user email from Intent extra
        String userEmail = getIntent().getStringExtra("email");

        storageRef = FirebaseStorage.getInstance().getReference();

        // Load user data based on email
        loadUserData(userEmail);

        photo.setOnClickListener(v -> {
            openImageChooser();
        });

        saveChanges.setOnClickListener(v -> {
            // Get updated user data and save changes
            updateUserDetails(userEmail);
        });

        if (!(sessionManager.getUserType().equals("ADMIN"))) {
            Toast.makeText(this, "You don't have permission!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUserData(String userEmail) {
        db.collection("users")
                .document(userEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            name.setText(user.getName());
                            email.setText(user.getEmail());
                            phone.setText(user.getPhone());
                            age.setText(String.valueOf(user.getAge()));
                            password.setText(user.getPassword());
                            status.setChecked(user.getStatus());

                            getSupportActionBar().setTitle(user.getName());

                            currentPhoto = user.getPhoto();

                            // Set the role based on the user's role
                            switch (user.getRole()) {
                                case "ADMIN":
                                    role.check(R.id.adminRadioUser);
                                    break;
                                case "MANAGER":
                                    role.check(R.id.managerRadioUser);
                                    break;
                                case "EMPLOYEE":
                                    role.check(R.id.employeeRadioUser);
                                    break;
                            }

                            // Load and set user's photo if available
                            if (user.getPhoto() != null && !user.getPhoto().isEmpty()) {
                                Glide.with(this).load(Uri.parse(user.getPhoto())).into(photo);
                            } else {
                                // Set a placeholder or default image if no photo is available
                                photo.setImageResource(R.mipmap.ic_launcher_round); // Change to your default photo resource
                            }
                        }
                    } else {
                        Intent intent = new Intent(this, MainActivity.class);
                        showToast("User doesn't exist!");
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    showToast("Can't fetch data");
                    startActivity(intent);
                    finish();
                });
    }

    private void updateUserDetails(String userEmail) {
        User updatedUser = new User();
        updatedUser.setName(name.getText().toString());
        updatedUser.setEmail(email.getText().toString());
        updatedUser.setPhone(phone.getText().toString());
        updatedUser.setAge(Long.parseLong(age.getText().toString()));
        updatedUser.setPassword(password.getText().toString());
        updatedUser.setStatus(status.isChecked());

        // Get the selected role
        String userRole = "UNDEFINED";
        int radio = role.getCheckedRadioButtonId();
        if (radio == R.id.adminRadioUser) {
            userRole = "ADMIN";
        } else if (radio == R.id.managerRadioUser) {
            userRole = "MANAGER";
        } else if (radio == R.id.employeeRadioUser) {
            userRole = "EMPLOYEE";
        }
        updatedUser.setRole(userRole);

        if (photoUri != null) {
            uploadImageAndUserDetails(userEmail, updatedUser);
        } else {
            // If no new photo, update user details without uploading the image
            updatedUser.setPhoto(currentPhoto);
            updateUserFirestore(userEmail, updatedUser);
        }
    }


    private void uploadImageAndUserDetails(String userEmail, User updatedUser) {
        StorageReference userImageRef = storageRef.child("profile_images/" + userEmail + ".jpg");

        // Put file to Storage
        userImageRef.putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    userImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updatedUser.setPhoto(uri.toString());
                        updateUserFirestore(userEmail, updatedUser);
                    }).addOnFailureListener(e -> {
                        showToast("Failed to retrieve image URL: " + e.getMessage());
                    });
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to upload image: " + e.getMessage());
                });
    }
    private void updateUserFirestore(String userEmail, User updatedUser) {
        db.collection("users")
                .document(userEmail)
                .set(updatedUser)
                .addOnSuccessListener(aVoid -> {
                    showToast("User details updated successfully!");
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to update user details: " + e.getMessage());
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
