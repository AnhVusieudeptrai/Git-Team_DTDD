package com.example.app_ecotrack.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.app_ecotrack.api.ApiClient;
import com.example.app_ecotrack.api.ApiService;
import com.example.app_ecotrack.api.models.FcmTokenRequest;
import com.example.app_ecotrack.api.models.MessageResponse;
import com.example.app_ecotrack.services.EcoTrackMessagingService;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * FCMTokenManager - Quản lý FCM token registration
 * Handles requesting notification permission, getting FCM token, and sending to backend
 * Requirements: 8.1
 */
public class FCMTokenManager {
    
    private static final String TAG = "FCMTokenManager";
    public static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    
    private final Context context;
    private final TokenManager tokenManager;
    private final ApiService apiService;
    
    public FCMTokenManager(Context context) {
        this.context = context;
        this.tokenManager = TokenManager.getInstance(context);
        ApiClient.init(context);
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }
    
    /**
     * Initialize FCM - request permission and get token
     * Call this on app startup or after login
     */
    public void initialize() {
        // Create notification channels
        EcoTrackMessagingService.createNotificationChannels(context);
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, will need to request from Activity
                Log.d(TAG, "Notification permission not granted");
                return;
            }
        }
        
        // Get FCM token
        getAndRegisterToken();
    }
    
    /**
     * Request notification permission (call from Activity)
     */
    public static void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                );
            }
        }
    }

    
    /**
     * Check if notification permission is granted
     */
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Permission not required for Android < 13
    }
    
    /**
     * Get FCM token and register with backend
     */
    public void getAndRegisterToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    
                    // Get new FCM registration token
                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);
                    
                    // Save token locally
                    tokenManager.saveFcmToken(token);
                    
                    // Send to backend if user is logged in
                    if (tokenManager.hasToken()) {
                        sendTokenToServer(token);
                    }
                });
    }
    
    /**
     * Send FCM token to backend server
     */
    public void sendTokenToServer(String fcmToken) {
        if (fcmToken == null || fcmToken.isEmpty()) {
            Log.w(TAG, "FCM token is null or empty");
            return;
        }
        
        // Check if token was already sent
        if (tokenManager.isFcmTokenSent() && fcmToken.equals(tokenManager.getFcmToken())) {
            Log.d(TAG, "FCM token already sent to server");
            return;
        }
        
        FcmTokenRequest request = new FcmTokenRequest(fcmToken);
        
        apiService.registerFcmToken(request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(@NonNull Call<MessageResponse> call, 
                                   @NonNull Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "FCM token registered successfully");
                    tokenManager.setFcmTokenSent(true);
                } else {
                    Log.e(TAG, "Failed to register FCM token: " + response.code());
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error registering FCM token", t);
            }
        });
    }
    
    /**
     * Register token after successful login
     * Call this after user logs in
     */
    public void onUserLogin() {
        String fcmToken = tokenManager.getFcmToken();
        if (fcmToken != null && !fcmToken.isEmpty()) {
            // Reset sent flag to ensure token is sent for new user
            tokenManager.setFcmTokenSent(false);
            sendTokenToServer(fcmToken);
        } else {
            // Get new token if not available
            getAndRegisterToken();
        }
    }
    
    /**
     * Handle token refresh
     * Called when FCM token is refreshed
     */
    public void onTokenRefresh(String newToken) {
        tokenManager.saveFcmToken(newToken);
        tokenManager.setFcmTokenSent(false);
        
        if (tokenManager.hasToken()) {
            sendTokenToServer(newToken);
        }
    }
}
