package project.midterm.midtermandroid.StudentManagement;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import project.midterm.midtermandroid.Adapter.StudentAdapter;
import project.midterm.midtermandroid.Importer.ExcelImporter;
import project.midterm.midtermandroid.LoginActivity;
import project.midterm.midtermandroid.Manager.SessionManager;
import project.midterm.midtermandroid.Model.Student;
import project.midterm.midtermandroid.R;

public class StudentsActivity extends AppCompatActivity {
    private static final int PICK_FILE_REQUEST_CODE = 101;
    private SessionManager sessionManager;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private ArrayList<Student> students;
    private TextInputEditText searchKeyword; // Find students by Student ID or Email
    private Button sortByGPA, sortByName; // SORTING
    private boolean isNameAscending = true;
    private boolean isGpaAscending = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        db = FirebaseFirestore.getInstance();

        fetchStudentsFromFirestore();

        sortByGPA = findViewById(R.id.sortByGPA);
        sortByName = findViewById(R.id.sortByName);

        sortByGPA.setOnClickListener(v -> {
            isGpaAscending = !isGpaAscending;
            sortStudentsByGPA(isGpaAscending);
        });

        sortByName.setOnClickListener(v -> {
            isNameAscending = !isNameAscending;
            sortStudentsByName(isNameAscending);
        });

        searchKeyword = findViewById(R.id.searchStudent);
        searchKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed for this implementation
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed for this implementation
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterStudents(s.toString());
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        refreshStudentData();
    }
    public void refreshStudentData() {
        fetchStudentsFromFirestore();
    }
    private void filterStudents(String searchText) {
        ArrayList<Student> filteredList = new ArrayList<>();
        for (Student student : students) {
            if (student.getStudentID().toLowerCase().contains(searchText.toLowerCase())
                    || student.getFullname().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(student);
            }
        }
        adapter.filterList(filteredList);
    }
    private void sortStudentsByName(boolean isAscending) {
        Collections.sort(students, (student1, student2) -> {
            int result = student1.getFullname().compareToIgnoreCase(student2.getFullname());
            return isAscending ? result : -result; // Toggle the sorting order
        });
        adapter.notifyDataSetChanged();
    }

    private void sortStudentsByGPA(boolean isAscending) {
        Collections.sort(students, (student1, student2) -> {
            int result = Double.compare(student1.getGpa(), student2.getGpa());
            return isAscending ? result : -result; // Toggle the sorting order
        });
        adapter.notifyDataSetChanged();
    }
    private void fetchStudentsFromFirestore() {
        db.collection("students")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    students = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Student student = new Student();
                        student.setStudentID(documentSnapshot.getString("studentID"));
                        student.setFullname(documentSnapshot.getString("fullname"));
                        student.setGender(documentSnapshot.getString("gender"));
                        student.setGpa(documentSnapshot.getDouble("gpa"));
                        student.setEmail(documentSnapshot.getString("email"));
                        student.setPhone(documentSnapshot.getString("phone"));
                        student.setAddress(documentSnapshot.getString("address"));
                        student.setBirthdate(documentSnapshot.getString("birthdate"));


                        students.add(student);
                    }
                    recyclerView = findViewById(R.id.studentRecyclerView);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    adapter = new StudentAdapter(this, students);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Log.e("FetchStudents", e.getMessage());
                    showToast("Cannot fetch data");
                });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.students_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int Id = item.getItemId();
        if (Id == R.id.addStudent) {
            Intent intent = new Intent(this, NewStudentActivity.class);
            startActivity(intent);
            return true;
        } else if (Id == R.id.importStudentData) {
            if ((sessionManager.getUserType().equals("EMPLOYEE"))) {
                Toast.makeText(this, "You don't have permission for this action", Toast.LENGTH_SHORT).show();
            } else {
                openFilePicker();
            }

            return true;
        } else if (Id == R.id.exportStudentData) {
            if ((sessionManager.getUserType().equals("EMPLOYEE"))) {
                Toast.makeText(this, "You don't have permission for this action", Toast.LENGTH_SHORT).show();
            } else {
                exportStudentsToExcel();
            }

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
    private void exportStudentsToExcel() {
        if (students != null && !students.isEmpty()) {
            ExcelImporter.exportStudentsToExcel(this, students, "output.xlsx");
        } else {

            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
        }
    }
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select a file"), PICK_FILE_REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedFileUri = data.getData();

            importStudentsFromFile(selectedFileUri);
        }
    }
    private void importStudentsFromFile(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);

            if (inputStream != null) {
                ArrayList<Student> importedStudents = ExcelImporter.importStudentsFromExcel(inputStream);

                for (Student student : importedStudents) {
                    addStudentToFirestore(student);
                }
                showToast("Students added to Firestore successfully");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showToast("Error importing file");
        }
    }
    private void addStudentToFirestore(Student student) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("students")
                .document(student.getStudentID())
                .set(student)
                .addOnSuccessListener(aVoid -> {
                    Log.d("IMPORT", student.getStudentID() + " - Successful");
                })
                .addOnFailureListener(e -> {
                    Log.e("AddStudent", "Error adding student", e);
                    showToast("Failed to add student to Firestore");
                });
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}