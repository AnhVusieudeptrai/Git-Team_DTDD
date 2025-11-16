package com.example.app_ecotrack;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AdminManageUsersActivity extends AppCompatActivity {
    private LinearLayout containerUsers;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_users);

        db = new DatabaseHelper(this);

        setupToolbar();
        initViews();
        loadUsers();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản Lý Người Dùng");
        }
    }

    private void initViews() {
        containerUsers = findViewById(R.id.containerUsers);
    }

    private void loadUsers() {
        containerUsers.removeAllViews();
        Cursor cursor = db.getAllUsers();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
                if (role.equals("user")) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String fullname = cursor.getString(cursor.getColumnIndexOrThrow("fullname"));
                    String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                    String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                    int points = cursor.getInt(cursor.getColumnIndexOrThrow("points"));
                    int level = cursor.getInt(cursor.getColumnIndexOrThrow("level"));

                    Cursor actCursor = db.getUserActivities(id);
                    int activitiesCount = actCursor != null ? actCursor.getCount() : 0;
                    if (actCursor != null) actCursor.close();

                    View userView = createUserItemView(fullname, username, email, points, level, activitiesCount);
                    containerUsers.addView(userView);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private View createUserItemView(String fullname, String username, String email, int points, int level, int activities) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_admin_user, containerUsers, false);

        TextView tvName = view.findViewById(R.id.tvUserName);
        TextView tvUsername = view.findViewById(R.id.tvUsername);
        TextView tvEmail = view.findViewById(R.id.tvUserEmail);
        TextView tvPoints = view.findViewById(R.id.tvUserPoints);
        TextView tvLevel = view.findViewById(R.id.tvUserLevel);
        TextView tvActivities = view.findViewById(R.id.tvUserActivities);

        tvName.setText(fullname);
        tvUsername.setText("@" + username);
        tvEmail.setText(email);
        tvPoints.setText(points + " điểm");
        tvLevel.setText("Cấp " + level);
        tvActivities.setText(activities + " hoạt động");

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}