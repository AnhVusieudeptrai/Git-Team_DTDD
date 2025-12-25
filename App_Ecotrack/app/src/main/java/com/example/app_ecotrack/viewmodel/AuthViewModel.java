package com.example.app_ecotrack.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.app_ecotrack.api.ApiClient;
import com.example.app_ecotrack.api.ApiService;
import com.example.app_ecotrack.api.models.AuthResponse;
import com.example.app_ecotrack.api.models.ForgotPasswordRequest;
import com.example.app_ecotrack.api.models.LoginRequest;
import com.example.app_ecotrack.api.models.MessageResponse;
import com.example.app_ecotrack.api.models.RegisterRequest;
import com.example.app_ecotrack.api.models.ResetPasswordRequest;
import com.example.app_ecotrack.api.models.UserData;
import com.example.app_ecotrack.utils.FCMTokenManager;
import com.example.app_ecotrack.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AuthViewModel - ViewModel xử lý logic xác thực người dùng
 * Bao gồm: đăng nhập, đăng ký, quên mật khẩu
 * Requirements: 1.2, 1.4, 1.5, 1.6, 1.7, 8.1
 */
public class AuthViewModel extends AndroidViewModel {

    // Auth states
    public enum AuthState {
        IDLE,
        LOADING,
        SUCCESS,
        ERROR
    }

    private final ApiService apiService;
    private final TokenManager tokenManager;
    private final FCMTokenManager fcmTokenManager;

    // LiveData cho trạng thái xác thực
    private final MutableLiveData<AuthState> authState = new MutableLiveData<>(AuthState.IDLE);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<UserData> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> forgotPasswordSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> resetPasswordSuccess = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        apiService = ApiClient.getInstance(application).getApiService();
        tokenManager = TokenManager.getInstance(application);
        fcmTokenManager = new FCMTokenManager(application);
    }

    // Getters cho LiveData
    public LiveData<AuthState> getAuthState() {
        return authState;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<UserData> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getForgotPasswordSuccess() {
        return forgotPasswordSuccess;
    }

    public LiveData<Boolean> getResetPasswordSuccess() {
        return resetPasswordSuccess;
    }

    /**
     * Đăng nhập với username và password
     * Requirements: 1.2, 1.7, 8.1
     */
    public void login(String username, String password) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            errorMessage.setValue("Vui lòng nhập tên đăng nhập");
            authState.setValue(AuthState.ERROR);
            return;
        }
        if (password == null || password.isEmpty()) {
            errorMessage.setValue("Vui lòng nhập mật khẩu");
            authState.setValue(AuthState.ERROR);
            return;
        }

        authState.setValue(AuthState.LOADING);

        LoginRequest request = new LoginRequest(username.trim(), password);
        apiService.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    
                    // Lưu token
                    if (authResponse.token != null) {
                        tokenManager.saveToken(authResponse.token);
                    }
                    
                    // Lưu thông tin user
                    if (authResponse.user != null) {
                        tokenManager.saveUserInfo(
                            authResponse.user.id,
                            authResponse.user.username
                        );
                        currentUser.setValue(authResponse.user);
                    }
                    
                    // Register FCM token after successful login - Requirement 8.1
                    fcmTokenManager.onUserLogin();
                    
                    authState.setValue(AuthState.SUCCESS);
                } else {
                    // Xử lý lỗi từ server - Requirement 1.7: không xóa password field
                    String error = parseErrorMessage(response);
                    errorMessage.setValue(error);
                    authState.setValue(AuthState.ERROR);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                errorMessage.setValue(getNetworkErrorMessage(t));
                authState.setValue(AuthState.ERROR);
            }
        });
    }

    /**
     * Đăng ký tài khoản mới
     * Requirements: 1.4 - Auto-login sau khi đăng ký thành công, 8.1
     */
    public void register(String fullname, String username, String email, String password) {
        // Validate input
        if (fullname == null || fullname.trim().isEmpty()) {
            errorMessage.setValue("Vui lòng nhập họ và tên");
            authState.setValue(AuthState.ERROR);
            return;
        }
        if (username == null || username.trim().isEmpty()) {
            errorMessage.setValue("Vui lòng nhập tên đăng nhập");
            authState.setValue(AuthState.ERROR);
            return;
        }
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("Vui lòng nhập email");
            authState.setValue(AuthState.ERROR);
            return;
        }
        if (!isValidEmail(email)) {
            errorMessage.setValue("Email không hợp lệ");
            authState.setValue(AuthState.ERROR);
            return;
        }
        if (password == null || password.length() < 6) {
            errorMessage.setValue("Mật khẩu phải có ít nhất 6 ký tự");
            authState.setValue(AuthState.ERROR);
            return;
        }

        authState.setValue(AuthState.LOADING);

        RegisterRequest request = new RegisterRequest(
            username.trim(),
            password,
            fullname.trim(),
            email.trim()
        );

        apiService.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    
                    // Auto-login: Lưu token sau khi đăng ký thành công
                    if (authResponse.token != null) {
                        tokenManager.saveToken(authResponse.token);
                    }
                    
                    // Lưu thông tin user
                    if (authResponse.user != null) {
                        tokenManager.saveUserInfo(
                            authResponse.user.id,
                            authResponse.user.username
                        );
                        currentUser.setValue(authResponse.user);
                    }
                    
                    // Register FCM token after successful registration - Requirement 8.1
                    fcmTokenManager.onUserLogin();
                    
                    authState.setValue(AuthState.SUCCESS);
                } else {
                    String error = parseErrorMessage(response);
                    errorMessage.setValue(error);
                    authState.setValue(AuthState.ERROR);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                errorMessage.setValue(getNetworkErrorMessage(t));
                authState.setValue(AuthState.ERROR);
            }
        });
    }

    /**
     * Gửi yêu cầu đặt lại mật khẩu
     * Requirements: 1.5, 1.6
     */
    public void forgotPassword(String email) {
        // Validate input
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("Vui lòng nhập email");
            authState.setValue(AuthState.ERROR);
            return;
        }
        if (!isValidEmail(email)) {
            errorMessage.setValue("Email không hợp lệ");
            authState.setValue(AuthState.ERROR);
            return;
        }

        authState.setValue(AuthState.LOADING);
        forgotPasswordSuccess.setValue(false);

        ForgotPasswordRequest request = new ForgotPasswordRequest(email.trim());

        apiService.forgotPassword(request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(@NonNull Call<MessageResponse> call, @NonNull Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    forgotPasswordSuccess.setValue(true);
                    authState.setValue(AuthState.SUCCESS);
                } else {
                    String error = parseErrorMessage(response);
                    errorMessage.setValue(error);
                    authState.setValue(AuthState.ERROR);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                errorMessage.setValue(getNetworkErrorMessage(t));
                authState.setValue(AuthState.ERROR);
            }
        });
    }

    /**
     * Đặt lại mật khẩu với mã xác nhận
     */
    public void resetPassword(String email, String token, String newPassword) {
        // Validate input
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("Email không hợp lệ");
            authState.setValue(AuthState.ERROR);
            return;
        }
        if (token == null || token.length() != 6) {
            errorMessage.setValue("Mã xác nhận phải có 6 chữ số");
            authState.setValue(AuthState.ERROR);
            return;
        }
        if (newPassword == null || newPassword.length() < 6) {
            errorMessage.setValue("Mật khẩu phải có ít nhất 6 ký tự");
            authState.setValue(AuthState.ERROR);
            return;
        }

        authState.setValue(AuthState.LOADING);
        resetPasswordSuccess.setValue(false);

        ResetPasswordRequest request = new ResetPasswordRequest(email.trim(), token, newPassword);

        apiService.resetPassword(request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(@NonNull Call<MessageResponse> call, @NonNull Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    resetPasswordSuccess.setValue(true);
                    authState.setValue(AuthState.SUCCESS);
                } else {
                    String error = parseErrorMessage(response);
                    errorMessage.setValue(error);
                    authState.setValue(AuthState.ERROR);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                errorMessage.setValue(getNetworkErrorMessage(t));
                authState.setValue(AuthState.ERROR);
            }
        });
    }

    /**
     * Reset trạng thái về IDLE
     */
    public void resetState() {
        authState.setValue(AuthState.IDLE);
        errorMessage.setValue(null);
        forgotPasswordSuccess.setValue(false);
        resetPasswordSuccess.setValue(false);
    }

    /**
     * Kiểm tra email hợp lệ
     */
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Parse error message từ response
     */
    private String parseErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                // Có thể parse JSON error nếu cần
                if (errorBody.contains("message")) {
                    // Simple parsing - có thể dùng Gson để parse chính xác hơn
                    int start = errorBody.indexOf("\"message\":\"") + 11;
                    int end = errorBody.indexOf("\"", start);
                    if (start > 10 && end > start) {
                        return errorBody.substring(start, end);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Default error messages based on HTTP status code
        switch (response.code()) {
            case 400:
                return "Dữ liệu không hợp lệ";
            case 401:
                return "Tên đăng nhập hoặc mật khẩu không đúng";
            case 403:
                return "Không có quyền truy cập";
            case 404:
                return "Không tìm thấy tài khoản";
            case 409:
                return "Tên đăng nhập hoặc email đã tồn tại";
            case 500:
                return "Lỗi máy chủ. Vui lòng thử lại sau.";
            default:
                return "Đã xảy ra lỗi. Vui lòng thử lại.";
        }
    }

    /**
     * Lấy thông báo lỗi mạng
     */
    private String getNetworkErrorMessage(Throwable t) {
        if (t instanceof java.net.UnknownHostException) {
            return "Không có kết nối mạng";
        } else if (t instanceof java.net.SocketTimeoutException) {
            return "Kết nối quá thời gian. Vui lòng thử lại.";
        } else if (t instanceof java.io.IOException) {
            return "Lỗi kết nối. Vui lòng kiểm tra mạng.";
        }
        return "Đã xảy ra lỗi. Vui lòng thử lại.";
    }
}
