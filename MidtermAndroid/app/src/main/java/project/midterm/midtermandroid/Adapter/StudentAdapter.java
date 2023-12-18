package project.midterm.midtermandroid.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import project.midterm.midtermandroid.EditUserActivity;
import project.midterm.midtermandroid.Model.Student;
import project.midterm.midtermandroid.Model.Student;
import project.midterm.midtermandroid.R;
import project.midterm.midtermandroid.StudentManagement.EditStudentActivity;
import project.midterm.midtermandroid.StudentManagement.StudentsActivity;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView id;
        private TextView name;
        private TextView gender;
        private TextView gpa;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.idStudentItem);
            name = itemView.findViewById(R.id.nameStudentItem);
            gender = itemView.findViewById(R.id.genderStudentItem);
            gpa = itemView.findViewById(R.id.gpaStudentItem);
        }
    }
    private Activity context;
    private ArrayList<Student> students;
    public StudentAdapter(Activity context, ArrayList<Student> students) {
        this.context = context;
        this.students = students;
    }

    @NonNull
    @Override
    public StudentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View studentView = inflater.inflate(R.layout.student_item, parent, false);
        StudentAdapter.ViewHolder viewHolder = new StudentAdapter.ViewHolder(studentView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull StudentAdapter.ViewHolder holder, int position) {
        Student student = students.get(position);
        holder.id.setText(student.getStudentID());
        holder.name.setText(student.getFullname());
        holder.gender.setText(student.getGender());
        holder.gpa.setText(String.valueOf(student.getGpa()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditStudentActivity.class);
            intent.putExtra("studentID", student.getStudentID());
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.getMenuInflater().inflate(R.menu.student_context_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.editStudentMenu) {
                    Intent intent = new Intent(context, EditStudentActivity.class);
                    intent.putExtra("studentID", student.getStudentID());
                    context.startActivity(intent);
                    return true;
                } else if (itemId == R.id.deleteStudentMenu) {
                    deleteStudentFromFirestore(student.getStudentID());
                    return true;
                }
                return false;
            });

            popupMenu.show();
            return true;
        });
    }
    private void deleteStudentFromFirestore(String studentID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("students").document(studentID)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Student successfully deleted
                    Toast.makeText(context, "Student deleted", Toast.LENGTH_SHORT).show();
                    // Refresh the list after deletion
                    students.clear();
                    ((StudentsActivity) context).refreshStudentData();
                })
                .addOnFailureListener(e -> {
                    // Handle deletion failure
                    Toast.makeText(context, "Failed to delete student", Toast.LENGTH_SHORT).show();
                    Log.e("DeleteStudent", e.getMessage());
                });
    }
    public void filterList(ArrayList<Student> filteredList) {
        students = filteredList;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return students.size();
    }
}
