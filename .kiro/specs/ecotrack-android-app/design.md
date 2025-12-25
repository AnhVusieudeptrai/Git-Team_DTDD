# Design Document: EcoTrack Android App

## Overview

Ứng dụng Android EcoTrack được thiết kế với kiến trúc MVVM (Model-View-ViewModel), sử dụng Material Design 3 với theme xanh lá eco. App hỗ trợ Dark Mode, giao diện Tiếng Việt, và tích hợp đầy đủ với backend API.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                              │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌────────┐│
│  │  Home   │ │ Badges  │ │Challenge│ │Leaderbd │ │Profile ││
│  │Fragment │ │Fragment │ │Fragment │ │Fragment │ │Fragment││
│  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘ └───┬────┘│
│       │           │           │           │          │      │
│  ┌────┴────┐ ┌────┴────┐ ┌────┴────┐ ┌────┴────┐ ┌───┴────┐│
│  │  Home   │ │ Badges  │ │Challenge│ │Leaderbd │ │Profile ││
│  │ViewModel│ │ViewModel│ │ViewModel│ │ViewModel│ │ViewModel│
│  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘ └───┬────┘│
└───────┼───────────┼───────────┼───────────┼──────────┼──────┘
        │           │           │           │          │
┌───────┴───────────┴───────────┴───────────┴──────────┴──────┐
│                     Repository Layer                         │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐         │
│  │ AuthRepository│ │ActivityRepo  │ │ UserRepository│        │
│  └──────┬───────┘ └──────┬───────┘ └──────┬───────┘         │
└─────────┼────────────────┼────────────────┼─────────────────┘
          │                │                │
┌─────────┴────────────────┴────────────────┴─────────────────┐
│                      Data Layer                              │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐         │
│  │  ApiService  │ │SharedPrefs   │ │  FCM Service │         │
│  │  (Retrofit)  │ │(Token Store) │ │(Notifications)│        │
│  └──────────────┘ └──────────────┘ └──────────────┘         │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. Activities (Android)

```java
// MainActivity - Single Activity với Navigation Component
public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private NavController navController;
}

// AuthActivity - Xử lý Login/Register/ForgotPassword
public class AuthActivity extends AppCompatActivity {
    // Chứa AuthFragment, RegisterFragment, ForgotPasswordFragment
}
```

### 2. Fragments

| Fragment | Chức năng |
|----------|-----------|
| HomeFragment | Hiển thị streak, điểm, CO2, danh sách hoạt động |
| BadgesFragment | Hiển thị huy hiệu theo loại |
| ChallengesFragment | Hiển thị thử thách tuần/tháng |
| LeaderboardFragment | Bảng xếp hạng với tabs |
| ProfileFragment | Thông tin cá nhân, settings |
| StatsFragment | Thống kê và báo cáo CO2 |
| HistoryFragment | Lịch sử hoạt động |

### 3. ViewModels

```java
// HomeViewModel
public class HomeViewModel extends ViewModel {
    private MutableLiveData<User> user;
    private MutableLiveData<List<Activity>> activities;
    private MutableLiveData<Streak> streak;
    private MutableLiveData<TodayStats> todayStats;
    
    void loadHomeData();
    void completeActivity(String activityId);
}

// AuthViewModel
public class AuthViewModel extends ViewModel {
    private MutableLiveData<AuthState> authState;
    
    void login(String username, String password);
    void register(RegisterRequest request);
    void forgotPassword(String email);
    void changePassword(String oldPassword, String newPassword);
}
```

### 4. API Service Interface

```java
public interface ApiService {
    // Auth
    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);
    
    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);
    
    @POST("api/auth/forgot-password")
    Call<MessageResponse> forgotPassword(@Body ForgotPasswordRequest request);
    
    @POST("api/auth/change-password")
    Call<MessageResponse> changePassword(@Body ChangePasswordRequest request);
    
    // Activities
    @GET("api/activities")
    Call<ActivitiesResponse> getActivities();
    
    @POST("api/activities/{id}/complete")
    Call<CompleteActivityResponse> completeActivity(@Path("id") String id);
    
    // Badges
    @GET("api/badges")
    Call<BadgesResponse> getBadges();
    
    @GET("api/badges/my")
    Call<MyBadgesResponse> getMyBadges();
    
    // Challenges
    @GET("api/challenges")
    Call<ChallengesResponse> getChallenges();
    
    @POST("api/challenges/{id}/join")
    Call<JoinChallengeResponse> joinChallenge(@Path("id") String id);
    
    @GET("api/challenges/my")
    Call<MyChallengesResponse> getMyChallenges();
    
    // Streaks
    @GET("api/streaks")
    Call<StreakResponse> getStreak();
    
    @GET("api/streaks/leaderboard")
    Call<StreakLeaderboardResponse> getStreakLeaderboard();
    
    // Leaderboard
    @GET("api/leaderboard")
    Call<LeaderboardResponse> getLeaderboard(@Query("limit") int limit);
    
    @GET("api/leaderboard/weekly")
    Call<WeeklyLeaderboardResponse> getWeeklyLeaderboard();
    
    // Users
    @GET("api/users/profile")
    Call<ProfileResponse> getProfile();
    
    @PUT("api/users/profile")
    Call<ProfileResponse> updateProfile(@Body UpdateProfileRequest request);
    
    @GET("api/users/stats")
    Call<StatsResponse> getStats();
}
```

## Data Models

### User Models

```java
public class User {
    String id;
    String username;
    String email;
    String fullname;
    String role;
    int points;
    int level;
    String avatar;
}

public class Streak {
    int currentStreak;
    int longestStreak;
    String lastActivityDate;
    boolean isActive;
    int daysUntilLost;
}
```

### Activity Models

```java
public class Activity {
    String id;
    String name;
    String description;
    int points;
    String category;
    String icon;
    boolean completedToday;
    double co2Saved; // Calculated: points * CO2_FACTOR
}

public class UserActivity {
    String id;
    Activity activity;
    int pointsEarned;
    String completedAt;
}
```

### Badge Models

```java
public class Badge {
    String id;
    String name;
    String description;
    String icon;
    String type; // streak, points, activities
    int requirement;
    String rarity; // common, rare, epic, legendary
    boolean earned;
    String earnedAt;
    int progress;
    int progressPercent;
}
```

### Challenge Models

```java
public class Challenge {
    String id;
    String name;
    String description;
    String type; // weekly, monthly
    String targetType; // points, activities
    int targetValue;
    String targetCategory;
    int rewardPoints;
    Badge rewardBadge;
    String startDate;
    String endDate;
    boolean joined;
    int progress;
    int progressPercent;
    boolean isCompleted;
    long timeRemainingMs;
}
```

### Stats Models

```java
public class Stats {
    TodayStats today;
    WeekStats week;
    TotalStats total;
    List<CategoryStat> categories;
    List<DayData> weeklyChart;
}

public class CO2Report {
    double totalCO2Saved; // in kg
    double treesEquivalent; // totalCO2 / 21.77
    double kmNotDriven; // totalCO2 / 0.21
    double kwhSaved; // totalCO2 / 0.5
}
```

## CO2 Calculation

```java
public class CO2Calculator {
    // CO2 saved per activity category (kg)
    private static final Map<String, Double> CO2_FACTORS = Map.of(
        "transport", 0.5,    // Đi xe đạp thay xe máy
        "energy", 0.3,      // Tiết kiệm điện
        "water", 0.1,       // Tiết kiệm nước
        "waste", 0.2,       // Phân loại rác, tái chế
        "green", 0.4,       // Trồng cây
        "consumption", 0.15 // Tiêu dùng xanh
    );
    
    public static double calculateCO2(String category, int points) {
        double factor = CO2_FACTORS.getOrDefault(category, 0.1);
        return points * factor / 10; // kg CO2
    }
    
    public static CO2Report generateReport(List<UserActivity> activities) {
        double totalCO2 = activities.stream()
            .mapToDouble(a -> calculateCO2(a.activity.category, a.pointsEarned))
            .sum();
        
        return new CO2Report(
            totalCO2,
            totalCO2 / 21.77,  // Trees equivalent
            totalCO2 / 0.21,   // Km not driven
            totalCO2 / 0.5     // kWh saved
        );
    }
}
```

## UI Design

### Color Scheme (Material Design 3)

```xml
<!-- Light Theme -->
<color name="md_theme_light_primary">#2E7D32</color>
<color name="md_theme_light_onPrimary">#FFFFFF</color>
<color name="md_theme_light_primaryContainer">#A5D6A7</color>
<color name="md_theme_light_secondary">#66BB6A</color>
<color name="md_theme_light_background">#FAFAFA</color>
<color name="md_theme_light_surface">#FFFFFF</color>

<!-- Dark Theme -->
<color name="md_theme_dark_primary">#81C784</color>
<color name="md_theme_dark_onPrimary">#1B5E20</color>
<color name="md_theme_dark_primaryContainer">#2E7D32</color>
<color name="md_theme_dark_secondary">#A5D6A7</color>
<color name="md_theme_dark_background">#121212</color>
<color name="md_theme_dark_surface">#1E1E1E</color>

<!-- Badge Rarity Colors -->
<color name="rarity_common">#9E9E9E</color>
<color name="rarity_rare">#2196F3</color>
<color name="rarity_epic">#9C27B0</color>
<color name="rarity_legendary">#FFD700</color>
```

### Screen Layouts

```
┌─────────────────────────────┐
│  🔥 Streak: 7 ngày          │  ← Home Screen
│  ┌─────────┬─────────┐      │
│  │ 85 pts  │ 2.5 kg  │      │  ← Today stats cards
│  │ Hôm nay │   CO2   │      │
│  └─────────┴─────────┘      │
│                             │
│  Thử thách đang tham gia    │
│  ┌─────────────────────┐    │
│  │ Tuần này    ████░ 60%│   │
│  └─────────────────────┘    │
│                             │
│  Hoạt động xanh             │
│  ┌─────────────────────┐    │
│  │ 🚴 Đi xe đạp    +20 │    │
│  │ 💡 Tắt điện     +10 │    │
│  │ 🛍️ Túi vải      +15 │    │
│  └─────────────────────┘    │
│                             │
│ [🏠] [🏆] [🎯] [📊] [👤]   │  ← Bottom Navigation
└─────────────────────────────┘
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Activity Completion Updates Points Correctly

*For any* activity with points P, when a user completes that activity, the user's total points should increase by exactly P, and the response should contain the correct new total.

**Validates: Requirements 2.5**

### Property 2: Challenge Progress Calculation

*For any* joined challenge with target value T and current progress P, the progress percentage should equal min(100, floor(P/T * 100)), and time remaining should be calculated correctly from endDate.

**Validates: Requirements 4.4, 4.6**

### Property 3: CO2 Calculation Accuracy

*For any* completed activity with category C and points P, the CO2 saved should equal P * CO2_FACTOR[C] / 10, where CO2_FACTOR is defined per category. The total CO2 report should be the sum of all individual activity CO2 values.

**Validates: Requirements 7.3, 7.4, 7.6**

## Error Handling

```java
public class ApiErrorHandler {
    public static String handleError(Throwable error) {
        if (error instanceof HttpException) {
            int code = ((HttpException) error).code();
            switch (code) {
                case 401: return "Phiên đăng nhập hết hạn";
                case 403: return "Không có quyền truy cập";
                case 404: return "Không tìm thấy dữ liệu";
                case 500: return "Lỗi máy chủ";
                default: return "Đã xảy ra lỗi";
            }
        } else if (error instanceof IOException) {
            return "Không có kết nối mạng";
        }
        return "Đã xảy ra lỗi không xác định";
    }
}
```

## Testing Strategy

### Unit Tests
- ViewModel logic tests
- CO2Calculator tests
- Date/time formatting tests
- API response parsing tests

### Property-Based Tests
- Activity completion point calculation
- Challenge progress percentage calculation
- CO2 calculation accuracy

### Integration Tests
- API integration tests with mock server
- Navigation flow tests
- Authentication flow tests

### UI Tests (Espresso)
- Login/Register flow
- Activity completion flow
- Navigation between screens
