# Implementation Plan: EcoTrack Android App

## Overview

Triển khai ứng dụng Android EcoTrack với Material Design 3, MVVM architecture, Retrofit cho API calls, và Firebase Cloud Messaging cho push notifications.

## Tasks

- [x] 1. Setup Project Structure và Dependencies
  - Cập nhật build.gradle với Material Design 3, Retrofit, ViewModel, Navigation
  - Tạo cấu trúc packages: ui, viewmodel, repository, api, models, utils
  - Setup theme colors (eco-green) cho Light và Dark mode
  - _Requirements: 9.1, 9.2, 9.3_

- [x] 2. Implement Data Layer
  - [x] 2.1 Tạo API Models (Request/Response classes)
    - AuthResponse, LoginRequest, RegisterRequest
    - ActivitiesResponse, CompleteActivityResponse
    - BadgesResponse, ChallengesResponse, StreakResponse
    - LeaderboardResponse, StatsResponse, ProfileResponse
    - _Requirements: All API interactions_

  - [x] 2.2 Cập nhật ApiService interface
    - Thêm endpoints cho badges, challenges, streaks
    - Thêm forgot-password, change-password endpoints
    - _Requirements: 1.5, 1.6, 6.4, 6.5_

  - [x] 2.3 Implement TokenManager và AuthInterceptor
    - SharedPreferences để lưu JWT token
    - OkHttp Interceptor để tự động thêm Authorization header
    - _Requirements: 1.9_

- [x] 3. Implement Authentication
  - [x] 3.1 Tạo AuthActivity và layouts
    - activity_auth.xml với NavHostFragment
    - fragment_login.xml với Material TextFields
    - fragment_register.xml
    - fragment_forgot_password.xml
    - _Requirements: 1.1, 1.3, 1.5_

  - [x] 3.2 Implement AuthViewModel
    - Login, register, forgotPassword functions
    - LiveData cho auth state và errors
    - _Requirements: 1.2, 1.4, 1.6, 1.7_

  - [x] 3.3 Implement Login Fragment
    - Form validation
    - Error handling và display
    - Navigation to Register/ForgotPassword
    - _Requirements: 1.2, 1.7_

  - [x] 3.4 Implement Register Fragment
    - Form với username, email, password, fullname
    - Validation và error handling
    - Auto-login sau khi đăng ký thành công
    - _Requirements: 1.4_

  - [x] 3.5 Implement Forgot Password Fragment
    - Email input và submit
    - Success/error feedback
    - _Requirements: 1.5, 1.6_

- [x] 4. Implement Main Navigation
  - [x] 4.1 Tạo MainActivity với Bottom Navigation
    - activity_main.xml với BottomNavigationView
    - Navigation graph với 5 destinations
    - _Requirements: 9.4_

  - [x] 4.2 Setup Navigation Component
    - nav_graph.xml với fragments
    - Bottom navigation integration
    - _Requirements: 9.4_

- [x] 5. Implement Home Screen
  - [x] 5.1 Tạo HomeFragment layout
    - Streak card với fire icon
    - Today stats cards (points, CO2)
    - Active challenges section
    - Activities list grouped by category
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.9_

  - [x] 5.2 Implement HomeViewModel
    - Load user data, streak, activities, challenges
    - Complete activity function
    - _Requirements: 2.1-2.9_

  - [x] 5.3 Implement Activity RecyclerView
    - ActivityAdapter với category headers
    - Click to complete với animation
    - _Requirements: 2.4, 2.5, 2.6_

  - [x] 5.4 Implement Badge/Challenge dialogs
    - Dialog khi nhận badge mới
    - Dialog khi hoàn thành challenge
    - _Requirements: 2.7, 2.8_

- [x] 6. Implement Badges Screen
  - [x] 6.1 Tạo BadgesFragment layout
    - Tabs hoặc chips cho filter by type
    - Grid layout cho badges
    - _Requirements: 3.1_

  - [x] 6.2 Implement BadgesViewModel
    - Load all badges với earned status
    - _Requirements: 3.1, 3.2_

  - [x] 6.3 Implement BadgeAdapter
    - Earned badges full color, unearned grayed
    - Rarity colors (common, rare, epic, legendary)
    - _Requirements: 3.2, 3.5_

  - [x] 6.4 Implement Badge Detail Dialog
    - Show requirement, progress, rarity
    - Progress bar toward earning
    - _Requirements: 3.3, 3.4_

- [x] 7. Implement Challenges Screen
  - [x] 7.1 Tạo ChallengesFragment layout
    - Tabs: Đang diễn ra, Đã tham gia, Hoàn thành
    - Challenge cards với progress bars
    - _Requirements: 4.1, 4.2_

  - [x] 7.2 Implement ChallengesViewModel
    - Load active challenges
    - Join challenge function
    - Load my challenges
    - _Requirements: 4.1, 4.3_

  - [x] 7.3 Implement ChallengeAdapter
    - Progress bar với percentage
    - Time remaining countdown
    - Join button
    - _Requirements: 4.2, 4.4, 4.6_

  - [x] 7.4 Implement Challenge Detail
    - Full description, rewards
    - Progress tracking
    - _Requirements: 4.2, 4.5_

- [x] 8. Implement Leaderboard Screen
  - [x] 8.1 Tạo LeaderboardFragment layout
    - TabLayout: Tổng, Tuần này, Streak
    - RecyclerView cho rankings
    - Current user highlight
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

  - [x] 8.2 Implement LeaderboardViewModel
    - Load global, weekly, streak leaderboards
    - _Requirements: 5.1, 5.3, 5.4_

  - [x] 8.3 Implement LeaderboardAdapter
    - Rank, avatar, name, points/streak, level
    - Highlight current user row
    - _Requirements: 5.5, 5.2_

- [x] 9. Implement Profile Screen
  - [x] 9.1 Tạo ProfileFragment layout
    - User info card (avatar, name, email, level)
    - Stats summary (activities, rank, CO2)
    - Earned badges preview
    - Settings buttons
    - _Requirements: 6.1, 6.2, 6.7_

  - [x] 9.2 Implement ProfileViewModel
    - Load profile và stats
    - Update profile function
    - Logout function
    - _Requirements: 6.1-6.7_

  - [x] 9.3 Implement Edit Profile
    - Edit fullname dialog/screen
    - _Requirements: 6.3_

  - [x] 9.4 Implement Change Password
    - Change password screen
    - Old password, new password, confirm
    - _Requirements: 6.4, 6.5_

  - [x] 9.5 Implement Logout
    - Clear token và navigate to login
    - _Requirements: 6.6_

- [x] 10. Implement Stats & CO2 Report
  - [x] 10.1 Tạo StatsFragment layout
    - Weekly chart (MPAndroidChart hoặc custom)
    - Category breakdown
    - CO2 report cards
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

  - [x] 10.2 Implement StatsViewModel
    - Load stats từ API
    - Calculate CO2 report
    - _Requirements: 7.1-7.6_

  - [x] 10.3 Implement CO2Calculator utility
    - Calculate CO2 per activity
    - Generate CO2 report với equivalents
    - _Requirements: 7.3, 7.4, 7.6_

  - [ ] 10.4 Write property test for CO2 calculation

    - **Property 3: CO2 Calculation Accuracy**
    - **Validates: Requirements 7.3, 7.4, 7.6**

- [x] 11. Implement Activity History
  - [x] 11.1 Tạo HistoryFragment layout
    - RecyclerView với date headers
    - Activity items với points và time
    - _Requirements: 10.1, 10.2, 10.4_

  - [x] 11.2 Implement HistoryViewModel
    - Load paginated history
    - _Requirements: 10.1_

  - [x] 11.3 Implement infinite scroll
    - Load more on scroll to bottom
    - _Requirements: 10.3_

- [x] 12. Implement Push Notifications
  - [x] 12.1 Setup Firebase Cloud Messaging
    - Add google-services.json
    - FCM dependencies
    - _Requirements: 8.1_

  - [x] 12.2 Implement FCM Service
    - Handle incoming notifications
    - Navigate to relevant screen on tap
    - _Requirements: 8.2-8.6_

  - [x] 12.3 Register FCM token với backend
    - Send token on login
    - Update token on refresh
    - _Requirements: 8.1_

- [x] 13. Polish UI/UX
  - [x] 13.1 Add loading states
    - Shimmer loading placeholders
    - Progress indicators
    - _Requirements: 9.7_

  - [x] 13.2 Add error handling UI
    - Error states với retry buttons
    - Snackbar messages
    - _Requirements: 9.8_

  - [x] 13.3 Add animations
    - Activity completion animation
    - Screen transitions
    - _Requirements: 9.6_

  - [x] 13.4 Vietnamese localization
    - strings.xml với tất cả text Tiếng Việt
    - _Requirements: 9.5_

- [ ] 14. Checkpoint - Test toàn bộ app
  - Ensure all tests pass, ask the user if questions arise.
  - Test login/register flow
  - Test activity completion
  - Test all screens navigation
  - Test dark mode

## Notes

- Tasks marked with `*` are optional property-based tests
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Use Material Design 3 components throughout
- All text in Vietnamese
