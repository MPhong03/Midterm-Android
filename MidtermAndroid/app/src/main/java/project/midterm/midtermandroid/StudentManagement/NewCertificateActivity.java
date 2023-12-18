package project.midterm.midtermandroid.StudentManagement;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import project.midterm.midtermandroid.LoginActivity;
import project.midterm.midtermandroid.Manager.SessionManager;
import project.midterm.midtermandroid.R;

public class NewCertificateActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private TextInputEditText name, school, date, description;
    private Button create;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_certificate);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        db = FirebaseFirestore.getInstance();

        name = findViewById(R.id.nameNewCer);
        school = findViewById(R.id.schoolNewCer);
        date = findViewById(R.id.dateNewCer);
        description = findViewById(R.id.descriptionNewCer);

        date.setOnClickListener(v -> {
            // Create a Calendar instance to get the current date
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Create a DatePickerDialog and show it
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Set the selected date to the EditText
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        date.setText(selectedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });

        create = findViewById(R.id.createCertificate);
        create.setOnClickListener(v -> {
            createCertificate();
        });

        if ((sessionManager.getUserType().equals("EMPLOYEE"))) {
            Toast.makeText(this, "You don't have permission for this action", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void createCertificate() {
        // Get the input values
        String cerName = name.getText().toString().trim();
        String cerSchool = school.getText().toString().trim();
        String cerDate = date.getText().toString().trim();
        String cerDescription = description.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(cerName) || TextUtils.isEmpty(cerSchool) || TextUtils.isEmpty(cerDate) ||
                TextUtils.isEmpty(cerDescription)) {
            showToast("Please fill all information");
            return;
        }

        String studentID = getIntent().getStringExtra("studentID");

        // Create a HashMap to store certificate information
        Map<String, Object> certificateData = new HashMap<>();
        certificateData.put("name", cerName);
        certificateData.put("school", cerSchool);
        certificateData.put("date", cerDate);
        certificateData.put("description", cerDescription);

        // Access Firestore and add the certificate under the student's collection
        db.collection("students").document(studentID)
                .collection("certificates")
                .whereEqualTo("name", cerName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // No existing certificate with the same name, proceed to add the certificate
                        db.collection("students").document(studentID)
                                .collection("certificates")
                                .document(formatString(cerName))
                                .set(certificateData)
                                .addOnSuccessListener(documentReference -> {
                                    showToast("Certificate added successfully");
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    showToast("Failed to add certificate");
                                    Log.e("AddCertificate", e.getMessage());
                                });
                    } else {
                        // Certificate with the same name already exists, notify the user
                        showToast("Certificate with the same name already exists");
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to check certificate existence");
                    Log.e("CheckCertificate", e.getMessage());
                });
    }
    private String formatString(String input) {
        String lowercaseString = input.toLowerCase();

        String formattedString = lowercaseString.replace(" ", "_");

        return formattedString;
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}