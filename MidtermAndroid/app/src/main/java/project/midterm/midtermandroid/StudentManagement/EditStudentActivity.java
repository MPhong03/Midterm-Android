package project.midterm.midtermandroid.StudentManagement;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import project.midterm.midtermandroid.LoginActivity;
import project.midterm.midtermandroid.Manager.SessionManager;
import project.midterm.midtermandroid.Model.Student;
import project.midterm.midtermandroid.R;

public class EditStudentActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private TextInputEditText studentID, fullname, birthdate, phone, email, address, gpa;
    private RadioGroup gender;
    private FirebaseFirestore db;
    private Button save;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        studentID = findViewById(R.id.idEditStudent);
        fullname = findViewById(R.id.nameEditStudent);
        birthdate = findViewById(R.id.birthEditStudent);
        gender = findViewById(R.id.genderEditStudent);
        phone = findViewById(R.id.phoneEditStudent);
        email = findViewById(R.id.emailEditStudent);
        address = findViewById(R.id.addressEditStudent);
        gpa = findViewById(R.id.gpaEditStudent);

        studentID.setEnabled(false);

        fetchStudentProfile();

        birthdate.setOnClickListener(v -> {
            // Create a Calendar instance to get the current date
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Create a DatePickerDialog and show it
            DatePickerDialog datePickerDialog = new DatePickerDialog(EditStudentActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Set the selected date to the EditText
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        birthdate.setText(selectedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });

        save = findViewById(R.id.saveStudent);
        save.setOnClickListener(v -> {
            saveStudent();
        });

        if ((sessionManager.getUserType().equals("EMPLOYEE"))) {
            save.setEnabled(false);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.student_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int Id = item.getItemId();
        if (Id == R.id.newCertificate) {
            Intent intent = new Intent(this, NewCertificateActivity.class);
            intent.putExtra("studentID", studentID.getText().toString());
            startActivity(intent);
            return true;
        } else if (Id == R.id.certificatesList) {
            Intent intent = new Intent(this, CertificatesActivity.class);
            intent.putExtra("studentID", studentID.getText().toString());
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
    private void fetchStudentProfile() {

        String studentId = getIntent().getStringExtra("studentID"); // Assuming you passed the ID via intent
        db = FirebaseFirestore.getInstance();
        db.collection("students").document(studentId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        studentID.setText(document.getString("studentID"));
                        fullname.setText(document.getString("fullname"));
                        birthdate.setText(document.getString("birthdate"));
                        phone.setText(document.getString("phone"));
                        email.setText(document.getString("email"));
                        address.setText(document.getString("address"));
                        gpa.setText(String.valueOf(document.getDouble("gpa")));

                        getSupportActionBar().setTitle(document.getString("fullname"));

                        switch (document.getString("gender")) {
                            case "MALE":
                                gender.check(R.id.maleEdit);
                                break;
                            case "FEMALE":
                                gender.check(R.id.femaleEdit);
                                break;
                            default:
                                break;
                        }
                    } else {
                        showToast("Student data not found");
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to fetch student data");
                    Log.e("FetchStudent", e.getMessage());
                });
    }
    private void saveStudent() {
        // Update student data in Firestore with the modified values
        String id = studentID.getText().toString().trim();
        String name = fullname.getText().toString().trim();
        String birth = birthdate.getText().toString().trim();
        int selectedGenderId = gender.getCheckedRadioButtonId();
        RadioButton selectedGender = findViewById(selectedGenderId);
        String gender = selectedGender.getText().toString().trim();
        String phoneNumber = phone.getText().toString().trim();
        String emailAddress = email.getText().toString().trim();
        String studentAddress = address.getText().toString().trim();
        String studentGPA = gpa.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(name) || TextUtils.isEmpty(birth) ||
                TextUtils.isEmpty(gender) || TextUtils.isEmpty(phoneNumber) ||
                TextUtils.isEmpty(emailAddress) || TextUtils.isEmpty(studentAddress) ||
                TextUtils.isEmpty(studentGPA)) {
            showToast("Please fill all information");
            return;
        }

        // Check if ID contains space
        if (id.contains(" ")) {
            showToast("Student ID cannot contain space");
            return;
        }

        // Create a HashMap to store updated student information
        Map<String, Object> updatedStudent = new HashMap<>();
        updatedStudent.put("fullname", name);
        updatedStudent.put("birthdate", birth);
        updatedStudent.put("gender", gender);
        updatedStudent.put("phone", phoneNumber);
        updatedStudent.put("email", emailAddress);
        updatedStudent.put("address", studentAddress);
        updatedStudent.put("gpa", Double.valueOf(studentGPA));

        // Access Firestore and update the student in the 'students' collection
        db = FirebaseFirestore.getInstance();
        db.collection("students").document(id)
                .update(updatedStudent)
                .addOnSuccessListener(aVoid -> {
                    showToast("Student data updated successfully");
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to update student data");
                    Log.e("UpdateStudent", e.getMessage());
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}