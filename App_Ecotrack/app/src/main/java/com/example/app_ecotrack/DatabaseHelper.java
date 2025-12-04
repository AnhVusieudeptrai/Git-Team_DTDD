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

   