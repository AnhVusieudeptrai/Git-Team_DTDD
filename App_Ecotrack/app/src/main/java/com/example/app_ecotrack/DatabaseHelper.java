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
    private static final int DATABASE_VERSION = 2;

    // Tables
    private static final String TABLE_USERS = "users";
    private static final String TABLE_ACTIVITIES = "activities";
    private static final String TABLE_USER_ACTIVITIES = "user_activities";
    private static final String TABLE_DAILY_CHALLENGES = "daily_challenges";
    private static final String TABLE_USER_STREAKS = "user_streaks";
    private static final String TABLE_USER_BADGES = "user_badges";

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

        // Create Daily Challenges table
        String createDailyChallengesTable = "CREATE TABLE " + TABLE_DAILY_CHALLENGES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "target_count INTEGER DEFAULT 1, " +
                "bonus_points INTEGER DEFAULT 50, " +
                "category TEXT, " +
                "difficulty TEXT DEFAULT 'easy')";
        db.execSQL(createDailyChallengesTable);

        // Create User Streaks table
        String createUserStreaksTable = "CREATE TABLE " + TABLE_USER_STREAKS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER UNIQUE, " +
                "current_streak INTEGER DEFAULT 0, " +
                "longest_streak INTEGER DEFAULT 0, " +
                "last_activity_date TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id))";
        db.execSQL(createUserStreaksTable);

        // Create User Badges table
        String createUserBadgesTable = "CREATE TABLE " + TABLE_USER_BADGES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "badge_id TEXT, " +
                "earned_date TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id))";
        db.execSQL(createUserBadgesTable);

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
        
        // Insert default daily challenges
        insertDefaultDailyChallenges(db);
    }

    private void insertDefaultDailyChallenges(SQLiteDatabase db) {
        String[][] challenges = {
                {"Chiến binh xanh", "Hoàn thành 3 hoạt động bất kỳ", "3", "50", "all", "easy"},
                {"Tiết kiệm năng lượng", "Hoàn thành 2 hoạt động tiết kiệm điện", "2", "40", "energy", "easy"},
                {"Bảo vệ nguồn nước", "Hoàn thành 2 hoạt động tiết kiệm nước", "2", "40", "water", "easy"},
                {"Giao thông xanh", "Di chuyển xanh 2 lần", "2", "45", "transport", "medium"},
                {"Siêu anh hùng môi trường", "Hoàn thành 5 hoạt động", "5", "100", "all", "hard"},
                {"Người trồng cây", "Hoàn thành 1 hoạt động trồng cây", "1", "35", "green", "easy"},
                {"Zero Waste Hero", "Hoàn thành 3 hoạt động giảm rác", "3", "60", "waste", "medium"}
        };

        for (String[] challenge : challenges) {
            ContentValues values = new ContentValues();
            values.put("title", challenge[0]);
            values.put("description", challenge[1]);
            values.put("target_count", Integer.parseInt(challenge[2]));
            values.put("bonus_points", Integer.parseInt(challenge[3]));
            values.put("category", challenge[4]);
            values.put("difficulty", challenge[5]);
            db.insert(TABLE_DAILY_CHALLENGES, null, values);
        }
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
        if (oldVersion < 2) {
            // Add new tables for version 2
            String createDailyChallengesTable = "CREATE TABLE IF NOT EXISTS " + TABLE_DAILY_CHALLENGES + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT, " +
                    "target_count INTEGER DEFAULT 1, " +
                    "bonus_points INTEGER DEFAULT 50, " +
                    "category TEXT, " +
                    "difficulty TEXT DEFAULT 'easy')";
            db.execSQL(createDailyChallengesTable);

            String createUserStreaksTable = "CREATE TABLE IF NOT EXISTS " + TABLE_USER_STREAKS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER UNIQUE, " +
                    "current_streak INTEGER DEFAULT 0, " +
                    "longest_streak INTEGER DEFAULT 0, " +
                    "last_activity_date TEXT, " +
                    "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id))";
            db.execSQL(createUserStreaksTable);

            String createUserBadgesTable = "CREATE TABLE IF NOT EXISTS " + TABLE_USER_BADGES + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "badge_id TEXT, " +
                    "earned_date TEXT, " +
                    "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id))";
            db.execSQL(createUserBadgesTable);

            insertDefaultDailyChallenges(db);
        }
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

    public boolean updateUserPasswordByUsernameOrEmail(String identifier, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPassword);
        int rows = db.update(TABLE_USERS, values, "username=? OR email=?", new String[]{identifier, identifier});
        return rows > 0;
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
            updateStreak(userId); // Update streak when completing activity
            checkAndAwardBadges(userId); // Check for new badges
            return true;
        }
        return false;
    }

    private void checkAndAwardBadges(int userId) {
        Cursor userCursor = getUserById(userId);
        if (userCursor != null && userCursor.moveToFirst()) {
            int points = userCursor.getInt(userCursor.getColumnIndexOrThrow("points"));
            userCursor.close();

            // Points-based badges
            if (points >= 100) awardBadge(userId, "points_100");
            if (points >= 500) awardBadge(userId, "points_500");
            if (points >= 1000) awardBadge(userId, "points_1000");
        }

        // Activity count badges
        Cursor actCursor = getUserActivities(userId);
        int actCount = actCursor != null ? actCursor.getCount() : 0;
        if (actCursor != null) actCursor.close();

        if (actCount >= 10) awardBadge(userId, "activities_10");
        if (actCount >= 50) awardBadge(userId, "activities_50");
        if (actCount >= 100) awardBadge(userId, "activities_100");

        // Streak badges
        int[] streak = getUserStreak(userId);
        if (streak[0] >= 7) awardBadge(userId, "streak_7");
        if (streak[0] >= 30) awardBadge(userId, "streak_30");
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

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    // ==================== STREAK METHODS ====================
    
    public int[] getUserStreak(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER_STREAKS, null, "user_id=?", 
                new String[]{String.valueOf(userId)}, null, null, null);
        
        int[] streakData = {0, 0}; // [currentStreak, longestStreak]
        
        if (cursor != null && cursor.moveToFirst()) {
            streakData[0] = cursor.getInt(cursor.getColumnIndexOrThrow("current_streak"));
            streakData[1] = cursor.getInt(cursor.getColumnIndexOrThrow("longest_streak"));
            cursor.close();
        }
        return streakData;
    }

    public void updateStreak(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = getCurrentDate();
        
        Cursor cursor = db.query(TABLE_USER_STREAKS, null, "user_id=?",
                new String[]{String.valueOf(userId)}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            String lastDate = cursor.getString(cursor.getColumnIndexOrThrow("last_activity_date"));
            int currentStreak = cursor.getInt(cursor.getColumnIndexOrThrow("current_streak"));
            int longestStreak = cursor.getInt(cursor.getColumnIndexOrThrow("longest_streak"));
            cursor.close();
            
            if (lastDate == null || lastDate.isEmpty()) {
                currentStreak = 1;
            } else if (lastDate.equals(today)) {
                return; // Already updated today
            } else {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date last = sdf.parse(lastDate);
                    Date now = sdf.parse(today);
                    long diff = (now.getTime() - last.getTime()) / (1000 * 60 * 60 * 24);
                    
                    if (diff == 1) {
                        currentStreak++;
                    } else {
                        currentStreak = 1;
                    }
                } catch (Exception e) {
                    currentStreak = 1;
                }
            }
            
            if (currentStreak > longestStreak) {
                longestStreak = currentStreak;
            }
            
            ContentValues values = new ContentValues();
            values.put("current_streak", currentStreak);
            values.put("longest_streak", longestStreak);
            values.put("last_activity_date", today);
            db.update(TABLE_USER_STREAKS, values, "user_id=?", new String[]{String.valueOf(userId)});
        } else {
            if (cursor != null) cursor.close();
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("current_streak", 1);
            values.put("longest_streak", 1);
            values.put("last_activity_date", today);
            db.insert(TABLE_USER_STREAKS, null, values);
        }
    }

    // ==================== DAILY CHALLENGE METHODS ====================
    
    public Cursor getDailyChallenge() {
        SQLiteDatabase db = this.getReadableDatabase();
        int dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR);
        Cursor cursor = db.query(TABLE_DAILY_CHALLENGES, null, null, null, null, null, null);
        
        if (cursor != null && cursor.getCount() > 0) {
            int index = dayOfYear % cursor.getCount();
            cursor.moveToPosition(index);
        }
        return cursor;
    }

    public int getTodayCategoryCount(int userId, String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = getCurrentDate();
        
        String query;
        String[] args;
        
        if (category.equals("all")) {
            query = "SELECT COUNT(*) FROM " + TABLE_USER_ACTIVITIES + 
                    " WHERE user_id=? AND DATE(completed_date)=?";
            args = new String[]{String.valueOf(userId), today};
        } else {
            query = "SELECT COUNT(*) FROM " + TABLE_USER_ACTIVITIES + " ua " +
                    "INNER JOIN " + TABLE_ACTIVITIES + " a ON ua.activity_id = a.id " +
                    "WHERE ua.user_id=? AND DATE(ua.completed_date)=? AND a.category=?";
            args = new String[]{String.valueOf(userId), today, category};
        }
        
        Cursor cursor = db.rawQuery(query, args);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    // ==================== BADGE METHODS ====================
    
    public boolean hasBadge(int userId, String badgeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER_BADGES, null, 
                "user_id=? AND badge_id=?", 
                new String[]{String.valueOf(userId), badgeId}, null, null, null);
        boolean has = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return has;
    }

    public boolean awardBadge(int userId, String badgeId) {
        if (hasBadge(userId, badgeId)) return false;
        
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("badge_id", badgeId);
        values.put("earned_date", getCurrentDateTime());
        
        long result = db.insert(TABLE_USER_BADGES, null, values);
        return result != -1;
    }

    public Cursor getUserBadges(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USER_BADGES, null, "user_id=?", 
                new String[]{String.valueOf(userId)}, null, null, "earned_date DESC");
    }

    public int getUserBadgeCount(int userId) {
        Cursor cursor = getUserBadges(userId);
        int count = cursor != null ? cursor.getCount() : 0;
        if (cursor != null) cursor.close();
        return count;
    }

    // ==================== ECO TIPS ====================
    
    public String getRandomEcoTip() {
        String[] tips = {
                "💡 Tắt đèn khi ra khỏi phòng có thể tiết kiệm đến 10% hóa đơn điện!",
                "🚿 Tắm nhanh 5 phút thay vì 10 phút tiết kiệm 50 lít nước mỗi ngày!",
                "🌳 Một cây xanh có thể hấp thụ 22kg CO2 mỗi năm!",
                "♻️ Tái chế 1 lon nhôm tiết kiệm đủ năng lượng để xem TV 3 giờ!",
                "🚴 Đi xe đạp 10km thay vì xe máy giảm 2.5kg CO2!",
                "🛍️ Túi vải có thể thay thế 700 túi nilon trong vòng đời sử dụng!",
                "💧 Sửa vòi nước rỉ có thể tiết kiệm 11,000 lít nước/năm!",
                "🌿 Trồng cây trong nhà giúp lọc không khí và giảm stress!",
                "📱 Sạc điện thoại đầy rồi rút sạc tiết kiệm 5% điện năng!",
                "🥗 Ăn chay 1 ngày/tuần giảm 1 tấn CO2/năm!",
                "🚶 Đi bộ 30 phút mỗi ngày vừa khỏe vừa xanh!",
                "🌡️ Điều hòa 26°C thay vì 24°C tiết kiệm 20% điện!"
        };
        
        int dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR);
        return tips[dayOfYear % tips.length];
    }
}