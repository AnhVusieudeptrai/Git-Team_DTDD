package com.example.app_ecotrack.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Request model for registering FCM token with backend
 * Requirements: 8.1
 */
public class FcmTokenRequest {
    
    @SerializedName("fcmToken")
    private String fcmToken;
    
    @SerializedName("deviceType")
    private String deviceType;
    
    public FcmTokenRequest(String fcmToken) {
        this.fcmToken = fcmToken;
        this.deviceType = "android";
    }
    
    public String getFcmToken() {
        return fcmToken;
    }
    
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
    
    public String getDeviceType() {
        return deviceType;
    }
    
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
