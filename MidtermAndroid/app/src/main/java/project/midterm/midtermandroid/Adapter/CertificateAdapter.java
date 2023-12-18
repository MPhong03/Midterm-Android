package project.midterm.midtermandroid.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import project.midterm.midtermandroid.Model.Certificate;
import project.midterm.midtermandroid.Model.Student;
import project.midterm.midtermandroid.R;
import project.midterm.midtermandroid.StudentManagement.CertificatesActivity;
import project.midterm.midtermandroid.StudentManagement.EditCertificateActivity;
import project.midterm.midtermandroid.StudentManagement.EditStudentActivity;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView school;
        private TextView date;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nameCerItem);
            school = itemView.findViewById(R.id.schoolCerItem);
            date = itemView.findViewById(R.id.dateCerItem);
        }
    }
    private Activity context;
    private ArrayList<Certificate> cers;

    public CertificateAdapter(Activity context, ArrayList<Certificate> cers) {
        this.context = context;
        this.cers = cers;
    }

    @NonNull
    @Override
    public CertificateAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View cerView = inflater.inflate(R.layout.certificate_item, parent, false);
        CertificateAdapter.ViewHolder viewHolder = new CertificateAdapter.ViewHolder(cerView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CertificateAdapter.ViewHolder holder, int position) {
        Certificate cer = cers.get(position);
        holder.name.setText(cer.getName());
        holder.date.setText(cer.getDate());
        holder.school.setText(cer.getSchool());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditCertificateActivity.class);
            intent.putExtra("studentID", context.getIntent().getStringExtra("studentID"));
            intent.putExtra("cerName", cer.getName());
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.inflate(R.menu.certificate_context_menu); // Assuming you have a context menu defined
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.editCerItem) {
                    Intent intent = new Intent(context, EditCertificateActivity.class);
                    intent.putExtra("studentID", context.getIntent().getStringExtra("studentID"));
                    intent.putExtra("cerName", cer.getName());
                    context.startActivity(intent);
                    return true;
                } else if (id == R.id.deleteCerItem) {
                    // Handle delete option here
                    int adapterPosition = holder.getAdapterPosition();
                    Certificate certificateToDelete = cers.get(adapterPosition);
                    deleteCertificate(certificateToDelete);
                    return true;
                }
                return false;
            });
            popupMenu.show();
            return true;
        });
    }
    private void deleteCertificate(Certificate certificate) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String studentID = context.getIntent().getStringExtra("studentID");

        db.collection("students").document(studentID)
                .collection("certificates")
                .document(formatString(certificate.getName()))
                .delete()
                .addOnSuccessListener(aVoid -> {
                    showToast("Certificate deleted successfully");
                    // Handle UI update if needed
                    ((CertificatesActivity) context).refreshData();
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to delete certificate");
                    Log.e("DeleteCertificate", e.getMessage());
                });
    }
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    private String formatString(String input) {
        String lowercaseString = input.toLowerCase();

        String formattedString = lowercaseString.replace(" ", "_");

        return formattedString;
    }
    @Override
    public int getItemCount() {
        return cers.size();
    }
}
