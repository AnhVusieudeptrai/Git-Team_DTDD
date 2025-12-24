package com.example.app_ecotrack;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AdminManageUsersActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private LinearLayout containerUsers;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_users);

        db = new DatabaseHelper(this);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        containerUsers = findViewById(R.id.containerUsers);
        loadUsers();
    }

    private void loadUsers() {
        containerUsers.removeAllViews();
        Cursor cursor = db.getAllUsers();
        
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                String fullname = cursor.getString(cursor.getColumnIndexOrThrow("fullname"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
                int points = cursor.getInt(cursor.getColumnIndexOrThrow("points"));
                int level = cursor.getInt(cursor.getColumnIndexOrThrow("level"));
                int activityCount = db.getUserActivityCount(userId);

                View itemView = LayoutInflater.from(this).inflate(R.layout.item_admin_user, containerUsers, false);

                TextView tvUserName = itemView.findViewById(R.id.tvUserName);
                TextView tvUsername = itemView.findViewById(R.id.tvUsername);
                TextView tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
                TextView tvUserPoints = itemView.findViewById(R.id.tvUserPoints);
                TextView tvUserLevel = itemView.findViewById(R.id.tvUserLevel);
                TextView tvUserActivities = itemView.findViewById(R.id.tvUserActivities);

                tvUserName.setText(fullname + (role.equals("admin") ? " 👑" : ""));
                tvUsername.setText("@" + username);
                tvUserEmail.setText(email != null ? email : "Chưa có email");
                tvUserPoints.setText(points + " điểm");
                tvUserLevel.setText("Cấp " + level);
                tvUserActivities.setText(activityCount + " hoạt động");

                // Long click to delete (except admin)
                if (!role.equals("admin")) {
                    itemView.setOnLongClickListener(v -> {
                        showDeleteDialog(userId, fullname);
                        return true;
                    });
                }

                containerUsers.addView(itemView);
            }
            cursor.close();
        } else {
            TextView emptyText = new TextView(this);
            emptyText.setText("Chưa có người dùng nào");
            emptyText.setTextSize(16);
            emptyText.setPadding(0, 32, 0, 32);
            containerUsers.addView(emptyText);
        }
    }

    private void showDeleteDialog(int userId, String fullname) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa người dùng")
                .setMessage("Bạn có chắc muốn xóa \"" + fullname + "\"?\n\nHành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (db.deleteUser(userId)) {
                        Toast.makeText(this, "Đã xóa người dùng", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    } else {
                        Toast.makeText(this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
