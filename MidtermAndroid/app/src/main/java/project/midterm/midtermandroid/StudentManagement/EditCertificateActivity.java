package project.midterm.midtermandroid.StudentManagement;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import project.midterm.midtermandroid.LoginActivity;
import project.midterm.midtermandroid.Manager.SessionManager;
import project.midterm.midtermandroid.R;

public class EditCertificateActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private TextInputEditText name, school, date, description;
    private Button save;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_certificate);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        db = FirebaseFirestore.getInstance();

        name = findViewById(R.id.nameEditCer);
        school = findViewById(R.id.schoolEditCer);
        date = findViewById(R.id.dateEditCer);
        description = findViewById(R.id.descriptionEditCer);

        String studentID = getIntent().getStringExtra("studentID");
        String cerName = getIntent().getStringExtra("cerName");

        fetchCertificateData(studentID, cerName);

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

        save = findViewById(R.id.saveCertificate);
        save.setOnClickListener(v -> {
            updateCertificateData();
        });

        if ((sessionManager.getUserType().equals("EMPLOYEE"))) {
            save.setEnabled(false);
        }
    }
    private void fetchCertificateData(String studentID, String cerName) {
        db.collection("students").document(studentID)
                .collection("certificates")
                .document(formatString(cerName))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String certificateName = documentSnapshot.getString("name");
                        String certificateSchool = documentSnapshot.getString("school");
                        String certificateDate = documentSnapshot.getString("date");
                        String certificateDescription = documentSnapshot.getString("description");

                        getSupportActionBar().setTitle(certificateName);

                        // Update the UI with existing data for editing
                        name.setText(certificateName);
                        school.setText(certificateSchool);
                        date.setText(certificateDate);
                        description.setText(certificateDescription);
                    } else {
                        showToast("Certificate not found");
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to fetch certificate data");
                    Log.e("FetchCertificate", e.getMessage());
                });
    }
    private void updateCertificateData() {
        String editedName = name.getText().toString().trim();
        String editedSchool = school.getText().toString().trim();
        String editedDate = date.getText().toString().trim();
        String editedDescription = description.getText().toString().trim();

        String studentID = getIntent().getStringExtra("studentID");
        String originalCerName = getIntent().getStringExtra("cerName");

        // Update certificate data in Firestore
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", editedName);
        updatedData.put("school", editedSchool);
        updatedData.put("date", editedDate);
        updatedData.put("description", editedDescription);

        db.collection("students").document(studentID)
                .collection("certificates")
                .document(formatString(originalCerName))
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    showToast("Certificate updated successfully");
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to update certificate");
                    Log.e("UpdateCertificate", e.getMessage());
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