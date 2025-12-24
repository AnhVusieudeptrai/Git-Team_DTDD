package com.example.app_ecotrack;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

public class AdminManageActivitiesActivity extends AppCompatActivity {
    private EditText etActivityName, etActivityDesc, etActivityPoints;
    private Spinner spActivityCategory;
    private Button btnAddActivity;
    private Button btnShowAddActivityForm, btnCancelAddActivity;
    private CardView cardAddActivityForm;
    private LinearLayout containerActivities;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_activities);

        db = new DatabaseHelper(this);

        setupToolbar();
        initViews();
        setupCategorySpinner();
        loadActivities();

        btnShowAddActivityForm.setOnClickListener(v -> showAddActivityForm());
        btnCancelAddActivity.setOnClickListener(v -> hideAddActivityForm(true));
        btnAddActivity.setOnClickListener(v -> addActivity());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản Lý Hoạt Động");
        }
    }

    private void initViews() {
        etActivityName = findViewById(R.id.etActivityName);
        etActivityDesc = findViewById(R.id.etActivityDesc);
        etActivityPoints = findViewById(R.id.etActivityPoints);
        spActivityCategory = findViewById(R.id.spActivityCategory);
        btnAddActivity = findViewById(R.id.btnAddActivity);
        btnShowAddActivityForm = findViewById(R.id.btnShowAddActivityForm);
        btnCancelAddActivity = findViewById(R.id.btnCancelAddActivity);
        cardAddActivityForm = findViewById(R.id.cardAddActivityForm);
        containerActivities = findViewById(R.id.containerActivities);

        hideAddActivityForm(false);
    }

    private void showAddActivityForm() {
        cardAddActivityForm.setVisibility(View.VISIBLE);
        btnShowAddActivityForm.setVisibility(View.GONE);
    }

    private void hideAddActivityForm(boolean clearInputs) {
        cardAddActivityForm.setVisibility(View.GONE);
        btnShowAddActivityForm.setVisibility(View.VISIBLE);
        if (clearInputs) {
            clearAddActivityInputs();
        }
    }

    private void clearAddActivityInputs() {
        etActivityName.setText("");
        etActivityDesc.setText("");
        etActivityPoints.setText("");
        if (spActivityCategory.getAdapter() != null && spActivityCategory.getAdapter().getCount() > 0) {
            spActivityCategory.setSelection(0);
        }
    }

    private void setupCategorySpinner() {
        String[] categories = {"transport", "energy", "water", "waste", "green", "consumption"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spActivityCategory.setAdapter(adapter);
    }

    private void addActivity() {
        String name = etActivityName.getText().toString().trim();
        String desc = etActivityDesc.getText().toString().trim();
        String pointsStr = etActivityPoints.getText().toString().trim();
        String category = spActivityCategory.getSelectedItem().toString();

        if (name.isEmpty() || pointsStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        int points;
        try {
            points = Integer.parseInt(pointsStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Điểm không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean success = db.insertActivity(name, desc, points, category);

        if (success) {
            Toast.makeText(this, "Thêm hoạt động thành công!", Toast.LENGTH_SHORT).show();
            hideAddActivityForm(true);
            loadActivities();
        } else {
            Toast.makeText(this, "Có lỗi xảy ra!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadActivities() {
        containerActivities.removeAllViews();
        Cursor cursor = db.getAllActivities();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                int points = cursor.getInt(cursor.getColumnIndexOrThrow("points"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));

                View activityView = createActivityItemView(id, name, desc, points, category);
                containerActivities.addView(activityView);
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private View createActivityItemView(int id, String name, String desc, int points, String category) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_admin_activity, containerActivities, false);

        TextView tvName = view.findViewById(R.id.tvActivityName);
        TextView tvDesc = view.findViewById(R.id.tvActivityDesc);
        TextView tvPoints = view.findViewById(R.id.tvActivityPoints);
        TextView tvCategory = view.findViewById(R.id.tvActivityCategory);
        Button btnEdit = view.findViewById(R.id.btnEdit);
        Button btnDelete = view.findViewById(R.id.btnDelete);

        tvName.setText(name);
        tvDesc.setText(desc);
        tvPoints.setText(points + " điểm");
        tvCategory.setText(getCategoryVietnamese(category));

        btnEdit.setOnClickListener(v -> showEditDialog(id, name, desc, points, category));
        btnDelete.setOnClickListener(v -> deleteActivity(id, name));

        return view;
    }

    private String getCategoryVietnamese(String category) {
        switch (category) {
            case "transport": return "🚴 Giao thông";
            case "energy": return "💡 Năng lượng";
            case "water": return "💧 Nước";
            case "waste": return "♻️ Rác thải";
            case "green": return "🌳 Cây xanh";
            case "consumption": return "🛒 Tiêu dùng";
            default: return category;
        }
    }

    private void showEditDialog(int id, String name, String desc, int points, String category) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_activity, null);

        EditText etName = dialogView.findViewById(R.id.etEditName);
        EditText etDesc = dialogView.findViewById(R.id.etEditDesc);
        EditText etPoints = dialogView.findViewById(R.id.etEditPoints);

        etName.setText(name);
        etDesc.setText(desc);
        etPoints.setText(String.valueOf(points));

        new AlertDialog.Builder(this)
                .setTitle("Sửa Hoạt Động")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    String newDesc = etDesc.getText().toString().trim();
                    String newPointsStr = etPoints.getText().toString().trim();

                    if (!newName.isEmpty() && !newPointsStr.isEmpty()) {
                        int newPoints = Integer.parseInt(newPointsStr);
                        boolean success = db.updateActivity(id, newName, newDesc, newPoints, category);
                        if (success) {
                            Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                            loadActivities();
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteActivity(int id, String name) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa Hoạt Động")
                .setMessage("Bạn có chắc muốn xóa: " + name + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boolean success = db.deleteActivity(id);
                    if (success) {
                        Toast.makeText(this, "Xóa thành công!", Toast.LENGTH_SHORT).show();
                        loadActivities();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
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