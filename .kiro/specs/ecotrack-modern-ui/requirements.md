# Requirements Document

## Introduction

Hoàn thiện giao diện người dùng cho ứng dụng EcoTrack - một ứng dụng theo dõi hoạt động bảo vệ môi trường. Giao diện cần được thiết kế hiện đại, đẹp mắt với phong cách Material Design 3, màu sắc xanh lá chủ đạo phù hợp với chủ đề môi trường. Tất cả các layout XML hiện đang trống và cần được tạo mới để kết nối với các Activity/Fragment Java đã có sẵn.

## Glossary

- **EcoTrack_App**: Ứng dụng Android theo dõi hoạt động bảo vệ môi trường
- **Layout_System**: Hệ thống các file XML layout trong thư mục res/layout
- **Material_Design**: Hệ thống thiết kế của Google cho Android
- **Activity**: Màn hình chính trong Android (Login, Register, Main, Admin, etc.)
- **Fragment**: Thành phần UI có thể tái sử dụng (Home, Activities, Statistics, Profile)
- **CardView**: Component hiển thị nội dung trong khung bo tròn với shadow
- **RecyclerView**: Component hiển thị danh sách có thể cuộn
- **BottomNavigation**: Thanh điều hướng ở dưới màn hình
- **TabLayout**: Thanh tab để chuyển đổi giữa các Fragment

## Requirements

### Requirement 1: Authentication Screens

**User Story:** As a user, I want beautiful and intuitive login/register screens, so that I can easily access the app.

#### Acceptance Criteria

1. THE Layout_System SHALL provide activity_login.xml with EditText for username, password, login button, and register link
2. THE Layout_System SHALL provide activity_register.xml with EditText for username, email, password, fullname, register button, and login link
3. WHEN displaying authentication screens, THE EcoTrack_App SHALL show a green gradient background with app logo
4. THE Layout_System SHALL use Material Design TextInputLayout with outlined style for all input fields
5. THE Layout_System SHALL include ProgressBar for loading states during authentication

### Requirement 2: Main Screen with Tab Navigation

**User Story:** As a user, I want a main screen with easy navigation between sections, so that I can quickly access different features.

#### Acceptance Criteria

1. THE Layout_System SHALL provide activity_main.xml with header showing user info (name, points, level) and TabLayout with ViewPager2
2. THE Layout_System SHALL display 4 tabs: Home (🏠), Activities (🌱), Statistics (📊), Profile (👤)
3. WHEN user switches tabs, THE EcoTrack_App SHALL smoothly transition between fragments
4. THE Layout_System SHALL use green color scheme (#4CAF50 primary, #2E7D32 dark, #81C784 light)

### Requirement 3: Home Fragment

**User Story:** As a user, I want to see my daily progress and quick access to features on the home screen, so that I can track my environmental impact.

#### Acceptance Criteria

1. THE Layout_System SHALL provide fragment_home.xml with stats cards showing today points, week points, total points
2. THE Layout_System SHALL display activity count, total activities, and user rank
3. THE Layout_System SHALL include quick action cards for Activities, Rewards, and Leaderboard
4. WHEN displaying stats, THE EcoTrack_App SHALL use CardView with rounded corners and elevation

### Requirement 4: Activities Fragment

**User Story:** As a user, I want to browse and complete eco-friendly activities, so that I can earn points and help the environment.

#### Acceptance Criteria

1. THE Layout_System SHALL provide fragment_activity.xml with search EditText, category Spinner, and RecyclerView
2. THE Layout_System SHALL provide item_activity.xml for each activity showing icon, name, description, points, and complete button
3. WHEN activity is completed today, THE EcoTrack_App SHALL show checkmark and disable the complete button
4. THE Layout_System SHALL use different colors for each category (transport, energy, water, waste, green, consumption)

### Requirement 5: Statistics Fragment

**User Story:** As a user, I want to see detailed statistics about my activities, so that I can understand my environmental contribution.

#### Acceptance Criteria

1. THE Layout_System SHALL provide fragment_statistics.xml with level progress bar, stats summary, category breakdown, weekly chart, and recent activities
2. THE Layout_System SHALL provide item_category_stat.xml showing category icon, name, count, and progress bar
3. THE Layout_System SHALL provide item_weekly_bar.xml for weekly chart bars
4. THE Layout_System SHALL provide item_recent_activity.xml showing activity name, date, and points earned

### Requirement 6: Profile Fragment

**User Story:** As a user, I want to view and manage my profile, so that I can see my achievements and access settings.

#### Acceptance Criteria

1. THE Layout_System SHALL provide fragment_profile.xml with avatar, user info (fullname, username, email), stats (points, level, activities, rank)
2. THE Layout_System SHALL display achievements section with unlocked/locked badges
3. THE Layout_System SHALL provide item_achievement.xml showing achievement icon, name, description, and lock overlay
4. THE Layout_System SHALL include action cards for Leaderboard, Rewards, Settings, and Logout

### Requirement 7: Leaderboard Screen

**User Story:** As a user, I want to see the leaderboard, so that I can compare my progress with other users.

#### Acceptance Criteria

1. THE Layout_System SHALL provide activity_leaderboard.xml with toolbar and scrollable leaderboard list
2. THE Layout_System SHALL provide item_leaderboard.xml showing rank (with medals for top 3), user name, level, activities count, and points
3. WHEN displaying current user, THE EcoTrack_App SHALL highlight their row with different background color
4. THE Layout_System SHALL provide drawable resources for rank badges (gold, silver, bronze)

### Requirement 8: Rewards Screen

**User Story:** As a user, I want to redeem my points for rewards, so that I feel motivated to continue eco-friendly activities.

#### Acceptance Criteria

1. THE Layout_System SHALL provide activity_rewards.xml with user points display and grid of reward items
2. THE Layout_System SHALL provide item_reward.xml showing reward icon, name, description, cost, and redeem button
3. WHEN user cannot afford reward, THE EcoTrack_App SHALL disable and dim the redeem button

### Requirement 9: Admin Dashboard

**User Story:** As an admin, I want a dashboard to manage the app, so that I can monitor and control app content.

#### Acceptance Criteria

1. THE Layout_System SHALL provide activity_admin.xml with admin greeting, stats overview (users, activities, completed, points), and management cards
2. THE Layout_System SHALL include cards for Manage Activities, Manage Users, Statistics, Database, and Logout
3. THE Layout_System SHALL use admin-specific color scheme (indigo/purple accents)

### Requirement 10: Admin Management Screens

**User Story:** As an admin, I want to manage activities and users, so that I can maintain app content quality.

#### Acceptance Criteria

1. THE Layout_System SHALL provide activity_admin_manage_activities.xml with activity list and add button
2. THE Layout_System SHALL provide activity_admin_manage_users.xml with user list
3. THE Layout_System SHALL provide activity_admin_statistics.xml with detailed statistics
4. THE Layout_System SHALL provide dialog_add_activity.xml for creating new activities

### Requirement 11: Settings Screen

**User Story:** As a user, I want to access app settings, so that I can customize my experience.

#### Acceptance Criteria

1. THE Layout_System SHALL provide activity_settings.xml with settings options (notifications, theme, about, etc.)
2. THE Layout_System SHALL use consistent card-based layout for settings items

### Requirement 12: Splash Screen

**User Story:** As a user, I want to see a branded splash screen when opening the app, so that I have a professional first impression.

#### Acceptance Criteria

1. THE Layout_System SHALL provide activity_splash.xml with app logo, app name, and loading indicator
2. THE Layout_System SHALL use full-screen green gradient background

### Requirement 13: Color and Style Resources

**User Story:** As a developer, I want consistent color and style resources, so that the app has a unified look.

#### Acceptance Criteria

1. THE Layout_System SHALL provide colors.xml with green color palette and category-specific colors
2. THE Layout_System SHALL provide themes.xml with Material Design 3 theme configuration
3. THE Layout_System SHALL provide drawable resources for backgrounds, buttons, and icons
