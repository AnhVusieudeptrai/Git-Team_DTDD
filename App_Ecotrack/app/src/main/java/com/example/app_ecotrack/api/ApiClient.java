package com.example.app_ecotrack.api;

import android.content.Context;

import com.example.app_ecotrack.utils.TokenManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final String BASE_URL = "https://ecotrack-backend-production.up.railway.app/";
    private static ApiClient instance;
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;
    private static TokenManager tokenManager = null;

    private ApiClient(Context context) {
        tokenManager = TokenManager.getInstance(context);
    }

    /**
     * Lấy singleton instance của ApiClient
     */
    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Khởi tạo ApiClient với Context
     * Phải gọi trước khi sử dụng getApiService()
     */
    public static void init(Context context) {
        tokenManager = TokenManager.getInstance(context);
        // Reset retrofit để sử dụng TokenManager mới
        retrofit = null;
        apiService = null;
    }

    /**
     * Lấy TokenManager instance
     */
    public static TokenManager getTokenManager() {
        return tokenManager;
    }

    /**
     * Set auth token (backward compatibility)
     */
    public static void setAuthToken(String token) {
        if (tokenManager != null) {
            tokenManager.saveToken(token);
        }
    }

    /**
     * Get auth token (backward compatibility)
     */
    public static String getAuthToken() {
        return tokenManager != null ? tokenManager.getToken() : null;
    }

    /**
     * Clear auth token (backward compatibility)
     */
    public static void clearAuthToken() {
        if (tokenManager != null) {
            tokenManager.clearToken();
        }
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS);

            // Thêm AuthInterceptor nếu TokenManager đã được khởi tạo
            if (tokenManager != null) {
                clientBuilder.addInterceptor(new AuthInterceptor(tokenManager));
            }

            OkHttpClient client = clientBuilder.build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public ApiService getApiService() {
        if (apiService == null) {
            apiService = getClient().create(ApiService.class);
        }
        return apiService;
    }

    /**
     * Static method for backward compatibility
     */
    public static ApiService getApiServiceStatic() {
        if (apiService == null) {
            apiService = getClient().create(ApiService.class);
        }
        return apiService;
    }

    /**
     * Load token from SharedPreferences (backward compatibility)
     */
    public static void loadToken(Context context) {
        if (tokenManager == null) {
            init(context);
        }
        // Token is automatically loaded by TokenManager
    }

    /**
     * Save token to SharedPreferences (backward compatibility)
     */
    public static void saveToken(Context context, String token) {
        if (tokenManager == null) {
            init(context);
        }
        tokenManager.saveToken(token);
    }

    /**
     * Reset client (useful for testing or re-initialization)
     */
    public static void reset() {
        retrofit = null;
        apiService = null;
    }
}
