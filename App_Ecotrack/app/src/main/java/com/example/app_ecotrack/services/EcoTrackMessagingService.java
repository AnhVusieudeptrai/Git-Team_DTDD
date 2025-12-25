package com.example.app_ecotrack.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.app_ecotrack.R;
import com.example.app_ecotrack.ui.main.MainActivity;
import com.example.app_ecotrack.utils.FCMTokenManager;
import com.example.app_ecotrack.utils.TokenManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Firebase Cloud Messaging Service for EcoTrack
 * Handles incoming push notifications and token refresh
 * Requirements: 8.1-8.6
 */
public class EcoTrackMessagingService extends FirebaseMessagingService {

    private static final String TAG = "EcoTrackFCM";
    
    // Notification channel IDs
    public static final String CHANNEL_ID_DEFAULT = "ecotrack_notifications";
    public static final String CHANNEL_ID_STREAK = "ecotrack_streak";
    public static final String CHANNEL_ID_BADGE = "ecotrack_badge";
    public static final String CHANNEL_ID_CHALLENGE = "ecotrack_challenge";
    
    // Notification types from backend
    public static final String TYPE_DAILY_REMINDER = "daily_reminder";
    public static final String TYPE_STREAK_WARNING = "streak_warning";
    public static final String TYPE_BADGE_EARNED = "badge_earned";
    public static final String TYPE_CHALLENGE_COMPLETED = "challenge_completed";
    
    // Intent extras for navigation
    public static final String EXTRA_NOTIFICATION_TYPE = "notification_type";
    public static final String EXTRA_TARGET_SCREEN = "target_screen";
    
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        
        // Check if message contains data payload
        Map<String, String> data = remoteMessage.getData();
        if (!data.isEmpty()) {
            Log.d(TAG, "Message data payload: " + data);
            handleDataMessage(data);
        }
        
        // Check if message contains notification payload
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            Log.d(TAG, "Message Notification Body: " + notification.getBody());
            String title = notification.getTitle() != null ? notification.getTitle() : getString(R.string.app_name);
            String body = notification.getBody() != null ? notification.getBody() : "";
            String type = data.get("type");
            showNotification(title, body, type, data);
        }
    }


    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed FCM token: " + token);
        
        // Use FCMTokenManager to handle token refresh
        FCMTokenManager fcmTokenManager = new FCMTokenManager(this);
        fcmTokenManager.onTokenRefresh(token);
    }
    
    /**
     * Handle data-only messages (when app is in foreground or background)
     */
    private void handleDataMessage(Map<String, String> data) {
        String type = data.get("type");
        String title = data.get("title");
        String body = data.get("body");
        
        if (title != null && body != null) {
            showNotification(title, body, type, data);
        }
    }
    
    /**
     * Show notification with appropriate channel and navigation intent
     * Requirements: 8.2-8.6
     */
    private void showNotification(String title, String body, String type, Map<String, String> data) {
        // Create intent for notification tap
        Intent intent = createNavigationIntent(type, data);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 
                0, 
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Get appropriate channel ID based on notification type
        String channelId = getChannelIdForType(type);
        
        // Build notification
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_eco_leaf)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(getResources().getColor(R.color.eco_green, null));
        
        // Add big text style for longer messages
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(body));
        
        // Add icon based on notification type
        addNotificationIcon(notificationBuilder, type);
        
        NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        // Create notification channel for Android O+
        createNotificationChannel(notificationManager, channelId);
        
        // Generate unique notification ID
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
    
    /**
     * Create intent that navigates to the relevant screen when notification is tapped
     * Requirements: 8.6
     */
    private Intent createNavigationIntent(String type, Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);
        
        if (type != null) {
            intent.putExtra(EXTRA_NOTIFICATION_TYPE, type);
            
            switch (type) {
                case TYPE_DAILY_REMINDER:
                case TYPE_STREAK_WARNING:
                    // Navigate to Home screen (default)
                    intent.putExtra(EXTRA_TARGET_SCREEN, "home");
                    break;
                    
                case TYPE_BADGE_EARNED:
                    // Navigate to Badges screen
                    intent.putExtra(EXTRA_TARGET_SCREEN, "badges");
                    if (data.containsKey("badge_id")) {
                        intent.putExtra("badge_id", data.get("badge_id"));
                    }
                    break;
                    
                case TYPE_CHALLENGE_COMPLETED:
                    // Navigate to Challenges screen
                    intent.putExtra(EXTRA_TARGET_SCREEN, "challenges");
                    if (data.containsKey("challenge_id")) {
                        intent.putExtra("challenge_id", data.get("challenge_id"));
                    }
                    break;
                    
                default:
                    intent.putExtra(EXTRA_TARGET_SCREEN, "home");
                    break;
            }
        }
        
        return intent;
    }

    
    /**
     * Get notification channel ID based on notification type
     */
    private String getChannelIdForType(String type) {
        if (type == null) return CHANNEL_ID_DEFAULT;
        
        switch (type) {
            case TYPE_STREAK_WARNING:
            case TYPE_DAILY_REMINDER:
                return CHANNEL_ID_STREAK;
            case TYPE_BADGE_EARNED:
                return CHANNEL_ID_BADGE;
            case TYPE_CHALLENGE_COMPLETED:
                return CHANNEL_ID_CHALLENGE;
            default:
                return CHANNEL_ID_DEFAULT;
        }
    }
    
    /**
     * Add appropriate icon based on notification type
     */
    private void addNotificationIcon(NotificationCompat.Builder builder, String type) {
        if (type == null) return;
        
        switch (type) {
            case TYPE_STREAK_WARNING:
            case TYPE_DAILY_REMINDER:
                builder.setSmallIcon(R.drawable.ic_streak_fire);
                break;
            case TYPE_BADGE_EARNED:
                builder.setSmallIcon(R.drawable.ic_badge);
                break;
            case TYPE_CHALLENGE_COMPLETED:
                builder.setSmallIcon(R.drawable.ic_challenge);
                break;
            default:
                builder.setSmallIcon(R.drawable.ic_eco_leaf);
                break;
        }
    }
    
    /**
     * Create notification channels for Android O and above
     */
    private void createNotificationChannel(NotificationManager notificationManager, String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName;
            String channelDescription;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            
            switch (channelId) {
                case CHANNEL_ID_STREAK:
                    channelName = "Streak & Nhắc nhở";
                    channelDescription = "Thông báo về streak và nhắc nhở hàng ngày";
                    break;
                case CHANNEL_ID_BADGE:
                    channelName = "Huy hiệu";
                    channelDescription = "Thông báo khi nhận được huy hiệu mới";
                    break;
                case CHANNEL_ID_CHALLENGE:
                    channelName = "Thử thách";
                    channelDescription = "Thông báo về thử thách và phần thưởng";
                    break;
                default:
                    channelName = "EcoTrack";
                    channelDescription = "Thông báo từ EcoTrack";
                    break;
            }
            
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    importance
            );
            channel.setDescription(channelDescription);
            channel.enableLights(true);
            channel.setLightColor(getResources().getColor(R.color.eco_green, null));
            channel.enableVibration(true);
            
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    /**
     * Send FCM token to backend server
     * This will be called when token is refreshed or user logs in
     */
    private void sendTokenToServer(String token) {
        // Token will be sent via API when user logs in
        // See FCMTokenManager for implementation
        Log.d(TAG, "Token ready to send to server: " + token);
    }
    
    /**
     * Create all notification channels (call this on app startup)
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = 
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            // Default channel
            NotificationChannel defaultChannel = new NotificationChannel(
                    CHANNEL_ID_DEFAULT,
                    "EcoTrack",
                    NotificationManager.IMPORTANCE_HIGH
            );
            defaultChannel.setDescription("Thông báo từ EcoTrack");
            notificationManager.createNotificationChannel(defaultChannel);
            
            // Streak channel
            NotificationChannel streakChannel = new NotificationChannel(
                    CHANNEL_ID_STREAK,
                    "Streak & Nhắc nhở",
                    NotificationManager.IMPORTANCE_HIGH
            );
            streakChannel.setDescription("Thông báo về streak và nhắc nhở hàng ngày");
            notificationManager.createNotificationChannel(streakChannel);
            
            // Badge channel
            NotificationChannel badgeChannel = new NotificationChannel(
                    CHANNEL_ID_BADGE,
                    "Huy hiệu",
                    NotificationManager.IMPORTANCE_HIGH
            );
            badgeChannel.setDescription("Thông báo khi nhận được huy hiệu mới");
            notificationManager.createNotificationChannel(badgeChannel);
            
            // Challenge channel
            NotificationChannel challengeChannel = new NotificationChannel(
                    CHANNEL_ID_CHALLENGE,
                    "Thử thách",
                    NotificationManager.IMPORTANCE_HIGH
            );
            challengeChannel.setDescription("Thông báo về thử thách và phần thưởng");
            notificationManager.createNotificationChannel(challengeChannel);
        }
    }
}
