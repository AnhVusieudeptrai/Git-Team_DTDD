package com.example.app_ecotrack;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DatabaseManagerActivity extends AppCompatActivity {
    private TextView tvDatabaseInfo, tvUsersList, tvActivitiesList;
    private Button btnResetDatabase, btnRefreshData;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_manager);

        db = new DatabaseHelper(this);
        
        initViews();
        setupToolbar();
        setupListeners();
        loadDatabaseInfo();
    }

    private void initViews() {
        tvDatabaseInfo = findViewById(R.id.tvDatabaseInfo);
        tvUsersList = findViewById(R.id.tvUsersList);
        tvActivitiesList = findViewById(R.id.tvActivitiesList);
        btnResetDatabase = findViewById(R.id.btnResetDatabase);
        btnRefreshData = findViewById(R.id.btnRefreshData);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("🗄️ Quản Lý Database");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupListeners() {
        btnRefreshData.setOnClickListener(v -> loadDatabaseInfo());
        
        btnResetDatabase.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("⚠️ Cảnh báo")
                    .setMessage("Bạn có chắc chắn muốn đặt lại dữ liệu?\nTất cả dữ liệu sẽ bị xóa và khôi phục về mặc định.")
                    .setPositiveButton("Reset", (dialog, which) -> {
                        resetDatabase();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void loadDatabaseInfo() {
        // Hiển thị thông tin tổng quan
        String info = db.getDatabaseInfo();
        tvDatabaseInfo.setText(info);

        // Hiển thị danh sách users
        loadUsersList();

        // Hiển thị danh sách activities
        loadActivitiesList();
    }

    private void loadUsersList() {
        StringBuilder usersList = new StringBuilder();
        usersList.append("📋 DANH SÁCH NGƯỜI DÙNG:\n\n");

        Cursor cursor = db.getAllUsers();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                String fullname = cursor.getString(cursor.getColumnIndexOrThrow("fullname"));
                String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
                int points = cursor.getInt(cursor.getColumnIndexOrThrow("points"));
                int level = cursor.getInt(cursor.getColumnIndexOrThrow("level"));

                String roleIcon = role.equals("admin") ? "👑" : "👤";
                usersList.append(String.format("%s %s (%s)\n", roleIcon, fullname, username));
                usersList.append(String.format("   ID: %d | Điểm: %d | Cấp: %d\n\n", id, points, level));
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            usersList.append("Không có dữ liệu người dùng.\n");
        }

        tvUsersList.setText(usersList.toString());
    }

    private void loadActivitiesList() {
        StringBuilder activitiesList = new StringBuilder();
        activitiesList.append("🎯 DANH SÁCH HOẠT ĐỘNG:\n\n");

        Cursor cursor = db.getAllActivities();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                int points = cursor.getInt(cursor.getColumnIndexOrThrow("points"));

                String categoryIcon = getCategoryIcon(category);
                activitiesList.append(String.format("%s %s\n", categoryIcon, name));
                activitiesList.append(String.format("   ID: %d | Danh mục: %s | Điểm: %d\n\n", id, category, points));
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            activitiesList.append("Không có dữ liệu hoạt động.\n");
        }

        tvActivitiesList.setText(activitiesList.toString());
    }

    private String getCategoryIcon(String category) {
        switch (category) {
            case "transport": return "🚴";
            case "energy": return "💡";
            case "water": return "💧";
            case "waste": return "♻️";
            case "green": return "🌳";
            case "consumption": return "🛒";
            default: return "📋";
        }
    }

    private void resetDatabase() {
        try {
            db.resetDatabase();
            Toast.makeText(this, "✅ Database đã được reset thành công!", Toast.LENGTH_SHORT).show();
            loadDatabaseInfo();
        } catch (Exception e) {
            Toast.makeText(this, "❌ Lỗi khi reset database: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}