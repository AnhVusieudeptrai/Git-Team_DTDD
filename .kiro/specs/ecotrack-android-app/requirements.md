# Requirements Document

## Introduction

Ứng dụng Android EcoTrack - Theo dõi hoạt động bảo vệ môi trường. App sử dụng Material Design 3 với theme xanh lá (eco), hỗ trợ Dark Mode, giao diện Tiếng Việt. Tích hợp đầy đủ các tính năng: đăng nhập/đăng ký, hoạt động xanh, huy hiệu, thử thách, streak, bảng xếp hạng, thống kê CO2, và thông báo đẩy.

## Glossary

- **App**: Ứng dụng Android EcoTrack
- **User**: Người dùng đã đăng nhập
- **Activity**: Hoạt động xanh bảo vệ môi trường
- **Badge**: Huy hiệu đạt được khi hoàn thành mục tiêu
- **Challenge**: Thử thách tuần/tháng
- **Streak**: Chuỗi ngày hoạt động liên tiếp
- **CO2_Report**: Báo cáo lượng CO2 tiết kiệm được
- **Bottom_Navigation**: Thanh điều hướng dưới cùng màn hình

## Requirements

### Requirement 1: Xác thực người dùng

**User Story:** As a user, I want to login/register to the app, so that I can track my eco activities.

#### Acceptance Criteria

1. WHEN the App starts without saved token, THE App SHALL display the Login screen
2. WHEN a user enters valid credentials and taps Login, THE App SHALL authenticate and navigate to Home screen
3. WHEN a user taps "Đăng ký", THE App SHALL navigate to Register screen
4. WHEN a user completes registration form with valid data, THE App SHALL create account and auto-login
5. WHEN a user taps "Quên mật khẩu", THE App SHALL navigate to Forgot Password screen
6. WHEN a user submits email for password reset, THE App SHALL send reset email and show confirmation
7. IF login fails, THEN THE App SHALL display error message without clearing password field
8. WHEN a user taps Google Sign-In button, THE App SHALL initiate Google OAuth flow
9. WHEN the App has valid saved token, THE App SHALL auto-login and navigate to Home screen

### Requirement 2: Màn hình chính (Home)

**User Story:** As a user, I want to see my daily progress on home screen, so that I can track my eco activities quickly.

#### Acceptance Criteria

1. WHEN Home screen loads, THE App SHALL display user's current streak prominently
2. WHEN Home screen loads, THE App SHALL display today's points and activity count
3. WHEN Home screen loads, THE App SHALL display CO2 saved today in kg
4. WHEN Home screen loads, THE App SHALL display list of available activities grouped by category
5. WHEN a user taps an activity, THE App SHALL complete it and update points/streak immediately
6. WHEN an activity is completed, THE App SHALL show animation and points earned
7. IF a new badge is earned, THEN THE App SHALL show congratulation dialog with badge info
8. IF a challenge is completed, THEN THE App SHALL show congratulation dialog with reward info
9. WHEN Home screen loads, THE App SHALL display active challenges with progress bars

### Requirement 3: Huy hiệu (Badges)

**User Story:** As a user, I want to view and collect badges, so that I feel motivated to continue eco activities.

#### Acceptance Criteria

1. WHEN Badges screen loads, THE App SHALL display all badges grouped by type (streak, points, activities)
2. WHEN displaying badges, THE App SHALL show earned badges with full color and unearned badges grayed out
3. WHEN a user taps a badge, THE App SHALL show badge details with progress toward earning it
4. WHEN displaying badge details, THE App SHALL show requirement, current progress, and rarity
5. THE App SHALL display badge rarity with distinct colors (common=gray, rare=blue, epic=purple, legendary=gold)

### Requirement 4: Thử thách (Challenges)

**User Story:** As a user, I want to participate in challenges, so that I can earn extra rewards.

#### Acceptance Criteria

1. WHEN Challenges screen loads, THE App SHALL display active weekly and monthly challenges
2. WHEN displaying a challenge, THE App SHALL show name, description, progress bar, and reward
3. WHEN a user taps "Tham gia" on a challenge, THE App SHALL join the challenge and start tracking progress
4. WHEN displaying joined challenges, THE App SHALL show real-time progress percentage
5. WHEN a challenge ends, THE App SHALL show result (completed/failed) and rewards earned
6. THE App SHALL display time remaining for each challenge

### Requirement 5: Bảng xếp hạng (Leaderboard)

**User Story:** As a user, I want to see leaderboard, so that I can compare my progress with others.

#### Acceptance Criteria

1. WHEN Leaderboard screen loads, THE App SHALL display top 10 users by total points
2. WHEN displaying leaderboard, THE App SHALL highlight current user's position
3. WHEN a user taps "Tuần này" tab, THE App SHALL show weekly leaderboard
4. WHEN a user taps "Streak" tab, THE App SHALL show streak leaderboard
5. WHEN displaying each user, THE App SHALL show rank, avatar, name, points/streak, and level

### Requirement 6: Hồ sơ cá nhân (Profile)

**User Story:** As a user, I want to view and edit my profile, so that I can personalize my account.

#### Acceptance Criteria

1. WHEN Profile screen loads, THE App SHALL display user info (avatar, name, email, level, total points)
2. WHEN Profile screen loads, THE App SHALL display statistics (total activities, rank, CO2 saved)
3. WHEN a user taps "Chỉnh sửa", THE App SHALL allow editing fullname
4. WHEN a user taps "Đổi mật khẩu", THE App SHALL navigate to Change Password screen
5. WHEN a user submits valid password change, THE App SHALL update password and show confirmation
6. WHEN a user taps "Đăng xuất", THE App SHALL clear token and navigate to Login screen
7. WHEN Profile screen loads, THE App SHALL display earned badges summary

### Requirement 7: Thống kê & Báo cáo CO2

**User Story:** As a user, I want to see my statistics and CO2 impact, so that I understand my environmental contribution.

#### Acceptance Criteria

1. WHEN Stats screen loads, THE App SHALL display weekly activity chart (last 7 days)
2. WHEN Stats screen loads, THE App SHALL display category breakdown with points per category
3. WHEN Stats screen loads, THE App SHALL display total CO2 saved (calculated from activities)
4. WHEN Stats screen loads, THE App SHALL display CO2 equivalents (trees planted, km not driven)
5. WHEN displaying stats, THE App SHALL show today, week, month, and all-time statistics
6. THE App SHALL calculate CO2 based on activity type (transport=0.5kg, energy=0.3kg, etc.)

### Requirement 8: Thông báo đẩy (Push Notifications)

**User Story:** As a user, I want to receive notifications, so that I don't forget to do eco activities.

#### Acceptance Criteria

1. WHEN the App starts, THE App SHALL request notification permission and register FCM token
2. WHEN a daily reminder is received, THE App SHALL display notification with streak info
3. WHEN streak is at risk, THE App SHALL display warning notification
4. WHEN a new badge is earned, THE App SHALL display congratulation notification
5. WHEN a challenge is completed, THE App SHALL display reward notification
6. WHEN a user taps notification, THE App SHALL navigate to relevant screen

### Requirement 9: Giao diện Material Design 3

**User Story:** As a user, I want a modern and beautiful UI, so that I enjoy using the app.

#### Acceptance Criteria

1. THE App SHALL use Material Design 3 components throughout
2. THE App SHALL use eco-green color scheme (#4CAF50 primary)
3. THE App SHALL support Dark Mode following system settings
4. THE App SHALL use Bottom Navigation with 5 tabs (Home, Badges, Challenges, Leaderboard, Profile)
5. THE App SHALL display Vietnamese text for all UI elements
6. THE App SHALL use smooth animations for transitions and interactions
7. THE App SHALL display loading indicators during API calls
8. IF network error occurs, THEN THE App SHALL display retry option with error message

### Requirement 10: Lịch sử hoạt động

**User Story:** As a user, I want to view my activity history, so that I can track what I've done.

#### Acceptance Criteria

1. WHEN History screen loads, THE App SHALL display paginated list of completed activities
2. WHEN displaying activity history, THE App SHALL show activity name, points, date/time
3. WHEN a user scrolls to bottom, THE App SHALL load more activities (infinite scroll)
4. WHEN displaying history, THE App SHALL group activities by date
