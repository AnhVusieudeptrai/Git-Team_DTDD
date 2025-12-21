package com.example.app_ecotrack;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "EcoTrack.db";
    private static final int DATABASE_VERSION = 1;

    // Tables
    private static final String TABLE_USERS = "users";
    private static final String TABLE_ACTIVITIES = "activities";
    private static final String TABLE_USER_ACTIVITIES = "user_activities";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "fullname TEXT NOT NULL, " +
                "email TEXT, " +
                "role TEXT DEFAULT 'user', " +
                "points INTEGER DEFAULT 0, " +
                "level INTEGER DEFAULT 1, " +
                "created_at TEXT)";
        db.execSQL(createUsersTable);

        // Create Activities table
        String createActivitiesTable = "CREATE TABLE " + TABLE_ACTIVITIES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "points INTEGER DEFAULT 0, " +
                "category TEXT, " +
                "icon TEXT, " +
                "created_at TEXT)";
        db.execSQL(createActivitiesTable);

        // Create User Activities table
        String createUserActivitiesTable = "CREATE TABLE " + TABLE_USER_ACTIVITIES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "activity_id INTEGER, " +
                "completed_date TEXT, " +
                "points_earned INTEGER, " +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id), " +
                "FOREIGN KEY(activity_id) REFERENCES " + TABLE_ACTIVITIES + "(id))";
        db.execSQL(createUserActivitiesTable);

        // Insert default admin
        ContentValues admin = new ContentValues();
        admin.put("username", "admin");
        admin.put("password", "admin123");
        admin.put("fullname", "Administrator");
        admin.put("email", "admin@ecotrack.com");
        admin.put("role", "admin");
        admin.put("points", 0);
        admin.put("level", 1);
        admin.put("created_at", getCurrentDateTime());
        db.insert(TABLE_USERS, null, admin);

        // Insert sample user
        ContentValues user = new ContentValues();
        user.put("username", "user");
        user.put("password", "user123");
        user.put("fullname", "Người dùng mẫu");
        user.put("email", "user@ecotrack.com");
        user.put("role", "user");
        user.put("points", 150);
        user.put("level", 2);
        user.put("created_at", getCurrentDateTime());
        db.insert(TABLE_USERS, null, user);

        // Insert default activities
        insertDefaultActivities(db);
    }

    private void insertDefaultActivities(SQLiteDatabase db) {
        String[][] activities = {
                {"Đi xe đạp thay xe máy", "Sử dụng xe đạp để di chuyển thay vì phương tiện có động cơ", "20", "transport", "bike"},
                {"Tắt điện khi không dùng", "Tiết kiệm năng lượng bằng cách tắt đèn và thiết bị điện", "10", "energy", "light"},
                {"Sử dụng túi vải", "Mang theo túi vải khi đi mua sắm thay vì túi nilon", "15", "waste", "bag"},
                {"Phân loại rác", "Phân loại rác thải tại nguồn", "20", "waste", "recycle"},
                {"Tắm nước nhanh", "Giảm thời gian tắm để tiết kiệm nước", "10", "water", "shower"},
                {"Trồng cây xanh", "Trồng và chăm sóc cây xanh", "30", "green", "tree"},
                {"Sử dụng đồ tái chế", "Ưu tiên sử dụng sản phẩm làm từ nguyên liệu tái chế", "15", "recycle", "product"},
                {"Không sử dụng ống hút nhựa", "Từ chối ống hút nhựa khi uống nước", "10", "waste", "straw"},
                {"Đi bộ đường ngắn", "Đi bộ thay vì đi xe cho quãng đường ngắn", "15", "transport", "walk"},
                {"Tắt vòi nước khi đánh răng", "Tiết kiệm nước khi đánh răng", "10", "water", "faucet"}
        };

        for (String[] activity : activities) {
            ContentValues values = new ContentValues();
            values.put("name", activity[0]);
            values.put("description", activity[1]);
            values.put("points", Integer.parseInt(activity[2]));
            values.put("category", activity[3]);
            values.put("icon", activity[4]);
            values.put("created_at", getCurrentDateTime());
            db.insert(TABLE_ACTIVITIES, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_ACTIVITIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // User methods
    public boolean insertUser(String username, String password, String fullname, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        values.put("fullname", fullname);
        values.put("email", email);
        values.put("role", "user");
        values.put("points", 0);
        values.put("level", 1);
        values.put("created_at", getCurrentDateTime());

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public Cursor checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE username=? AND password=?";
        return db.rawQuery(query, new String[]{username, password});
    }

    public Cursor getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null, "id=?", new String[]{String.valueOf(userId)}, null, null, null);
    }

    public boolean updateUserPoints(int userId, int points) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = getUserById(userId);
        if (cursor != null && cursor.moveToFirst()) {
            int currentPoints = cursor.getInt(cursor.getColumnIndexOrThrow("points"));
            int newPoints = currentPoints + points;
            int newLevel = calculateLevel(newPoints);

            ContentValues values = new ContentValues();
            values.put("points", newPoints);
            values.put("level", newLevel);

            cursor.close();
            int result = db.update(TABLE_USERS, values, "id=?", new String[]{String.valueOf(userId)});
            return result > 0;
        }
        return false;
    }

    private int calculateLevel(int points) {
        return (points / 100) + 1;
    }

    // Activity methods
    public Cursor getAllActivities() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ACTIVITIES, null, null, null, null, null, "name ASC");
    }

    public boolean insertActivity(String name, String description, int points, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        values.put("points", points);
        values.put("category", category);
        values.put("icon", "default");
        values.put("created_at", getCurrentDateTime());

        long result = db.insert(TABLE_ACTIVITIES, null, values);
        return result != -1;
    }

    public boolean updateActivity(int id, String name, String description, int points, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        values.put("points", points);
        values.put("category", category);

        int result = db.update(TABLE_ACTIVITIES, values, "id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public boolean deleteActivity(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_ACTIVITIES, "id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    // User Activity methods
    public boolean completeActivity(int userId, int activityId, int pointsEarned) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("activity_id", activityId);
        values.put("completed_date", getCurrentDateTime());
        values.put("points_earned", pointsEarned);

        long result = db.insert(TABLE_USER_ACTIVITIES, null, values);
        if (result != -1) {
            updateUserPoints(userId, pointsEarned);
            return true;
        }
        return false;
    }

    public Cursor getUserActivities(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT ua.*, a.name, a.description, a.category FROM " + TABLE_USER_ACTIVITIES +
                " ua INNER JOIN " + TABLE_ACTIVITIES + " a ON ua.activity_id = a.id WHERE ua.user_id=? ORDER BY ua.completed_date DESC";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    public Cursor getTodayActivities(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String query = "SELECT ua.*, a.name, a.points FROM " + TABLE_USER_ACTIVITIES +
                " ua INNER JOIN " + TABLE_ACTIVITIES + " a ON ua.activity_id = a.id " +
                "WHERE ua.user_id=? AND DATE(ua.completed_date)=? ORDER BY ua.completed_date DESC";
        return db.rawQuery(query, new String[]{String.valueOf(userId), today});
    }

    public int getTodayPoints(int userId) {
        Cursor cursor = getTodayActivities(userId);
        int totalPoints = 0;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                totalPoints += cursor.getInt(cursor.getColumnIndexOrThrow("points_earned"));
            }
            cursor.close();
        }
        return totalPoints;
    }

    public Cursor getLeaderboard() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null, "role=?", new String[]{"user"}, null, null, "points DESC", "10");
    }

    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null, null, null, null, null, "created_at DESC");
    }

    public int getTotalUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE role='user'", null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getTotalActivitiesCompleted() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USER_ACTIVITIES, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}