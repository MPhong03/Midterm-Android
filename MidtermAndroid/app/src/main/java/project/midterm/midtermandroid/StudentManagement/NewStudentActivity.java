package project.midterm.midtermandroid.StudentManagement;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import project.midterm.midtermandroid.R;

public class NewStudentActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private TextInputEditText studentID, fullname, birthdate, phone, email, address, gpa;
    private RadioGroup gender;
    private FirebaseFirestore db;
    private Button create;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_student);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        studentID = findViewById(R.id.idNewStudent);
        fullname = findViewById(R.id.nameNewStudent);
        birthdate = findViewById(R.id.birthNewStudent);
        gender = findViewById(R.id.genderNewStudent);
        phone = findViewById(R.id.phoneNewStudent);
        email = findViewById(R.id.emailNewStudent);
        address = findViewById(R.id.addressNewStudent);
        gpa = findViewById(R.id.gpaNewStudent);

        birthdate.setOnClickListener(v -> {
            // Create a Calendar instance to get the current date
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Create a DatePickerDialog and show it
            DatePickerDialog datePickerDialog = new DatePickerDialog(NewStudentActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Set the selected date to the EditText
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        birthdate.setText(selectedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });

        create = findViewById(R.id.createStudent);
        create.setOnClickListener(v -> {
            createStudent();
        });

        if ((sessionManager.getUserType().equals("EMPLOYEE"))) {
            Toast.makeText(this, "You don't have permission for this action", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void createStudent() {
        // Get the input values
        String id = studentID.getText().toString().trim();
        String name = fullname.getText().toString().trim();
        String birth = birthdate.getText().toString().trim();
        // Get the selected gender
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

        // Create a HashMap to store student information
        Map<String, Object> student = new HashMap<>();
        student.put("studentID", id);
        student.put("fullname", name);
        student.put("birthdate", birth);
        student.put("gender", gender);
        student.put("phone", phoneNumber);
        student.put("email", emailAddress);
        student.put("address", studentAddress);
        student.put("gpa", Double.valueOf(studentGPA));

        // Access Firestore and add the student to the 'students' collection
        db = FirebaseFirestore.getInstance();
        db.collection("students").document(id)
                .set(student)
                .addOnSuccessListener(aVoid -> {
                    showToast("Successfully create new student");
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("CreateStudent", e.getMessage());
                    showToast("Failed to create new student");
                });
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}