package project.midterm.midtermandroid.StudentManagement;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import project.midterm.midtermandroid.Adapter.CertificateAdapter;
import project.midterm.midtermandroid.Importer.ExcelImporter;
import project.midterm.midtermandroid.LoginActivity;
import project.midterm.midtermandroid.Manager.SessionManager;
import project.midterm.midtermandroid.Model.Certificate;
import project.midterm.midtermandroid.Model.Student;
import project.midterm.midtermandroid.R;

public class CertificatesActivity extends AppCompatActivity {
    private static final int PICK_FILE_REQUEST_CODE = 101;
    private SessionManager sessionManager;
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private CertificateAdapter adapter;
    private ArrayList<Certificate> certificateList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificates);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        db = FirebaseFirestore.getInstance();
        String studentID = getIntent().getStringExtra("studentID");

        fetchCertificates(studentID);
    }
    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.certificates_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int Id = item.getItemId();
        if (Id == R.id.createCerItem) {
            Intent intent = new Intent(this, NewCertificateActivity.class);
            intent.putExtra("studentID", getIntent().getStringExtra("studentID"));
            startActivity(intent);
            return true;
        } else if (Id == R.id.importCerData) {
            if ((sessionManager.getUserType().equals("EMPLOYEE"))) {
                Toast.makeText(this, "You don't have permission for this action", Toast.LENGTH_SHORT).show();
            } else {
                openFilePicker();
            }

            return true;
        } else if (Id == R.id.exportCerData) {
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
    public void refreshData() {
        String studentID = getIntent().getStringExtra("studentID");
        fetchCertificates(studentID);
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
            importCertificatesFromExcel(selectedFileUri);
        }
    }
    private void importCertificatesFromExcel(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);

            if (inputStream != null) {
                ArrayList<Certificate> importedCers = ExcelImporter.importCertificatesFromExcel(inputStream);

                for (Certificate cer : importedCers) {
                    addCertificateToFirestore(cer);
                }
                showToast("Students added to Firestore successfully");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showToast("Error importing file");
        }
    }
    private void addCertificateToFirestore(Certificate cer) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("students").document(getIntent().getStringExtra("studentID"))
                .collection("certificates")
                .document(formatString(cer.getName()))
                .set(cer)
                .addOnSuccessListener(aVoid -> {
                    Log.d("IMPORT", cer.getName() + " - Successful");
                })
                .addOnFailureListener(e -> {
                    Log.e("AddCer", "Error adding cer", e);
                    showToast("Failed to add certificate to Firestore");
                });
    }
    private void exportStudentsToExcel() {
        if (certificateList != null && !certificateList.isEmpty()) {
            ExcelImporter.exportCertificatesToExcel(this, certificateList, "output.xlsx");
        } else {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
        }
    }
    private void fetchCertificates(String studentID) {
        db.collection("students").document(studentID)
                .collection("certificates")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    certificateList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Certificate certificate = new Certificate();
                        certificate.setName(documentSnapshot.getString("name"));
                        certificate.setDate(documentSnapshot.getString("date"));
                        certificate.setDescription(documentSnapshot.getString("description"));
                        certificate.setSchool(documentSnapshot.getString("school"));
                        certificateList.add(certificate);
                    }
                    recyclerView = findViewById(R.id.certificatesRecyclerView);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));

                    adapter = new CertificateAdapter(this, certificateList);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Log.e("FetchCertificates", "Error fetching certificates: " + e.getMessage());
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