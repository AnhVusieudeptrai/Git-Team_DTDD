package com.example.app_ecotrack.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * TokenManager - Quản lý JWT token và FCM token cho authentication
 * Sử dụng SharedPreferences để lưu trữ token
 */
public class TokenManager {
    private static final String PREF_NAME = "EcoTrackPrefs";
    private static final String KEY_AUTH_TOKEN = "authToken";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FCM_TOKEN = "fcmToken";
    private static final String KEY_FCM_TOKEN_SENT = "fcmTokenSent";

    private static TokenManager instance;
    private SharedPreferences prefs;
    private String cachedToken;

    public TokenManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        cachedToken = prefs.getString(KEY_AUTH_TOKEN, null);
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    /**
     * Lưu token vào SharedPreferences
     */
    public void saveToken(String token) {
        cachedToken = token;
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply();
    }

    /**
     * Lấy token từ cache hoặc SharedPreferences
     */
    public String getToken() {
        if (cachedToken == null) {
            cachedToken = prefs.getString(KEY_AUTH_TOKEN, null);
        }
        return cachedToken;
    }

    /**
     * Kiểm tra xem có token hay không
     */
    public boolean hasToken() {
        return getToken() != null && !getToken().isEmpty();
    }

    /**
     * Xóa token (logout)
     */
    public void clearToken() {
        cachedToken = null;
        prefs.edit()
                .remove(KEY_AUTH_TOKEN)
                .remove(KEY_USER_ID)
                .remove(KEY_USERNAME)
                .remove(KEY_FCM_TOKEN_SENT)
                .apply();
    }

    /**
     * Lưu thông tin user cơ bản
     */
    public void saveUserInfo(String userId, String username) {
        prefs.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    /**
     * Lấy user ID
     */
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Lấy username
     */
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }
    
    /**
     * Lưu FCM token
     */
    public void saveFcmToken(String fcmToken) {
        prefs.edit()
                .putString(KEY_FCM_TOKEN, fcmToken)
                .putBoolean(KEY_FCM_TOKEN_SENT, false)
                .apply();
    }
    
    /**
     * Lấy FCM token
     */
    public String getFcmToken() {
        return prefs.getString(KEY_FCM_TOKEN, null);
    }
    
    /**
     * Đánh dấu FCM token đã được gửi lên server
     */
    public void setFcmTokenSent(boolean sent) {
        prefs.edit().putBoolean(KEY_FCM_TOKEN_SENT, sent).apply();
    }
    
    /**
     * Kiểm tra FCM token đã được gửi lên server chưa
     */
    public boolean isFcmTokenSent() {
        return prefs.getBoolean(KEY_FCM_TOKEN_SENT, false);
    }
}