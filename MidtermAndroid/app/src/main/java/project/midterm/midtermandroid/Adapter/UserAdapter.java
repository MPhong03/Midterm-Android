package project.midterm.midtermandroid.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import org.w3c.dom.Text;

import java.util.ArrayList;

import project.midterm.midtermandroid.EditUserActivity;
import project.midterm.midtermandroid.Model.User;
import project.midterm.midtermandroid.R;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView photo;
        private TextView name;
        private TextView email;
        private TextView role;
        private Switch status;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.photoUserItem);
            name = itemView.findViewById(R.id.nameUserItem);
            email = itemView.findViewById(R.id.emailUserItem);
            role = itemView.findViewById(R.id.roleUserItem);
            status = itemView.findViewById(R.id.statusUserItem);
        }
    }

    private Activity context;
    private ArrayList<User> users;

    public UserAdapter(Activity context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View userView = inflater.inflate(R.layout.user_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(userView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        if (user.getPhoto() != null && !user.getPhoto().isEmpty()) {
            Glide.with(context)
                    .load(Uri.parse(user.getPhoto()))
                    .apply(new RequestOptions().override(200, 200))
                    .into(holder.photo);
        } else {
            Glide.with(context)
                    .load(R.mipmap.ic_launcher)
                    .apply(new RequestOptions().override(200, 200))
                    .into(holder.photo);
        }
        holder.name.setText(user.getName());
        holder.email.setText(user.getEmail());
        holder.role.setText(user.getRole());
        holder.status.setChecked(user.getStatus());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditUserActivity.class);
            intent.putExtra("email", user.getEmail());
            context.startActivity(intent);

        });

        holder.itemView.setOnLongClickListener(v -> {
            showContextMenu(v, user);
            return true;
        });
    }
    private void showContextMenu(View v, User user) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        popupMenu.getMenuInflater().inflate(R.menu.user_context_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.editUserMenuItem) {
                // Open the EditUserActivity
                Intent intent = new Intent(context, EditUserActivity.class);
                intent.putExtra("email", user.getEmail());
                context.startActivity(intent);
                return true;
            } else if (id == R.id.deleteUserMenuItem) {
                // Delete the user from Firestore
                deleteUser(user);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }
    private void deleteUser(User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(user.getEmail())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove the user from the RecyclerView
                    users.remove(user);
                    notifyDataSetChanged();
                    Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    public int getItemCount() {
        return users.size();
    }
}
