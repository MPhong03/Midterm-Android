package project.midterm.midtermandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import project.midterm.midtermandroid.Manager.SessionManager;

public class ProfileActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private SessionManager sessionManager;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private TextInputEditText name, email, phone, age;
    private TextView role;
    private Switch status;
    private CircleImageView avatar;
    private Button saveProfile;
    private Uri avatarUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        name = findViewById(R.id.displayNameProfile);
        email = findViewById(R.id.emailProfile);
        role = findViewById(R.id.roleProfile);
        avatar = findViewById(R.id.photoNewUser);
        phone = findViewById(R.id.phoneProfile);
        age = findViewById(R.id.ageProfile);
        status = findViewById(R.id.statusProfile);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        email.setEnabled(false);
        getUserProfile(sessionManager.getEmail());

        avatar.setOnClickListener(v -> {
            openImageChooser();
        });

        saveProfile = findViewById(R.id.saveProfile);
        saveProfile.setOnClickListener(v -> {
            onUpdateUserProfile(
                    name.getText().toString(),
                    avatarUri,
                    phone.getText().toString(),
                    Long.parseLong(age.getText().toString()),
                    status.isChecked()
            );
        });

        if (!(sessionManager.getUserType().equals("ADMIN"))) {
            saveProfile.setEnabled(false);
        }

//        getSupportActionBar().setTitle(name.getText().toString());
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
                avatarUri = data.getData();
                Glide.with(this).load(avatarUri).into(avatar);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE && data != null && data.getExtras() != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                Uri imageUri = getImageUri(this, imageBitmap);
                Glide.with(this).load(imageUri).into(avatar);
            }
        }
    }
    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }
    private void getUserProfile(String userEmail) {
        CollectionReference rolesCollection = db.collection("users");

        Query query = rolesCollection.whereEqualTo("email", userEmail);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String userName = document.getString("name");
                    String userRole = document.getString("role");
                    String userPhone = document.getString("phone");
                    long userAge = document.getLong("age");
                    boolean userStatus = document.getBoolean("status");
                    String photoString = document.getString("photo");

                    name.setText(userName);
                    email.setText(userEmail);
                    role.setText("ROLE: " + userRole);
                    phone.setText(userPhone);
                    age.setText(String.valueOf(userAge));
                    status.setChecked(userStatus);

                    // Check if the user has a photo in Firestore
                    if (photoString != null && !photoString.isEmpty()) {
                        // Load the image from Firebase Storage using the download URL
                        loadProfileImage(photoString);
                    } else {
                        // Use a default image or leave it empty based on your requirement
                        Glide.with(this)
                                .load(R.mipmap.ic_launcher)
                                .into(avatar);
                    }

                    getSupportActionBar().setTitle(userName);
                }
            } else {
                // Handle errors
                Log.w("ERROR", "Error getting documents.", task.getException());
            }
        });
    }
    private void loadProfileImage(String photoUrl) {
        Uri uri = Uri.parse(photoUrl);
        Log.d("FireStorage", photoUrl);
        Glide.with(this)
                .load(uri)
                .into(avatar);
    }
    private void onUpdateUserProfile(String updateName, Uri updateAvatar, String updatePhone, long updateAge, boolean updateStatus) {
        // Assuming you have a Firestore collection named "users"
        CollectionReference usersCollection = db.collection("users");

        // Assuming your documents have a field "email" to uniquely identify users
        Query query = usersCollection.whereEqualTo("email", email.getText().toString());

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference userRef = db.collection("users").document(email.getText().toString());

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("phone", updatePhone);
                    updates.put("age", updateAge);
                    updates.put("status", updateStatus);
                    updates.put("name", updateName);

                    if (updateAvatar != null) {
                        // Upload the image to Firebase Storage
                        uploadImageToStorage(updateAvatar, userRef, updates);
                    } else {
                        // If no new image, update the user document directly
                        updateUserDocument(userRef, updates);
                    }

                    userRef.update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getApplicationContext(), "Saved additional information!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                // Handle the error
                                Log.e("ERROR", "Error updating document", e);
                            });
                }
            } else {
                // Handle errors
                Log.w("ERROR", "Error getting documents.", task.getException());
            }
        });
    }

    private void uploadImageToStorage(Uri imageUri, DocumentReference userRef, Map<String, Object> updates) {
        // Get a reference to the storage location
        StorageReference storageReference = storageRef.child("profile_images/" + email.getText().toString() + ".jpg");

        // Upload the image to Firebase Storage
        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL of the uploaded image
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Add the photo URL to the updates map
                        updates.put("photo", uri.toString());

                        // Update the user document with the new information
                        updateUserDocument(userRef, updates);
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                    Log.e("ERROR", "Error uploading image to storage", e);
                });
    }

    private void updateUserDocument(DocumentReference userRef, Map<String, Object> updates) {
        // Update the user document with the new information
        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplicationContext(), "Saved additional information!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                    Log.e("ERROR", "Error updating document", e);
                });
    }
}