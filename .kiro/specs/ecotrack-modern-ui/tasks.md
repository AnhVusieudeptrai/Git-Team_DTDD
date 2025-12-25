# Implementation Plan: EcoTrack Modern UI

## Overview

Tạo tất cả các file layout XML và resources cho ứng dụng EcoTrack với giao diện Material Design 3 hiện đại. Implementation sẽ theo thứ tự: resources (colors, themes, drawables) → layouts cơ bản → layouts phức tạp.

## Tasks

- [x] 1. Tạo Color và Theme Resources
  - [x] 1.1 Tạo colors.xml với bảng màu xanh lá và category colors
    - Định nghĩa primary colors (#4CAF50, #2E7D32, #81C784)
    - Định nghĩa category colors (transport, energy, water, waste, green, consumption)
    - Định nghĩa admin colors (indigo)
    - _Requirements: 13.1_
  - [x] 1.2 Cập nhật themes.xml với Material Design 3 theme
    - Cấu hình colorPrimary, colorPrimaryVariant, colorOnPrimary
    - Cấu hình colorSecondary, colorBackground, colorSurface
    - _Requirements: 13.2_

- [x] 2. Tạo Drawable Resources
  - [x] 2.1 Tạo bg_gradient_green.xml cho gradient background
    - Gradient từ colorPrimaryDark đến colorPrimary
    - _Requirements: 1.3, 12.2_
  - [x] 2.2 Tạo bg_card_rounded.xml cho CardView background
    - White background với corner radius 16dp
    - _Requirements: 3.4_
  - [x] 2.3 Tạo bg_button_primary.xml và bg_button_secondary.xml
    - Primary: green với ripple effect
    - Secondary: outlined style
    - _Requirements: 1.1_
  - [x] 2.4 Tạo bg_input_field.xml cho TextInputLayout
    - Outlined style với corner radius
    - _Requirements: 1.4_
  - [x] 2.5 Tạo bg_rank_gold.xml, bg_rank_silver.xml, bg_rank_bronze.xml
    - Circle backgrounds với màu vàng, bạc, đồng
    - _Requirements: 7.4_
  - [x] 2.6 Tạo bg_circle_avatar.xml cho avatar background
    - Circle shape với gradient
    - _Requirements: 6.1_

- [x] 3. Tạo Authentication Layouts
  - [x] 3.1 Tạo activity_splash.xml
    - Full screen gradient background
    - Centered logo emoji (🌱), app name, tagline
    - ProgressBar at bottom
    - _Requirements: 12.1, 12.2_
  - [x] 3.2 Tạo activity_login.xml
    - Gradient background với logo section
    - White card container với TextInputLayout cho username, password
    - MaterialButton cho login
    - TextView link cho register
    - ProgressBar overlay
    - _Requirements: 1.1, 1.3, 1.4, 1.5_
  - [x] 3.3 Tạo activity_register.xml
    - Tương tự login với 4 fields: fullname, username, email, password
    - ScrollView cho smaller screens
    - _Requirements: 1.2, 1.4, 1.5_

- [x] 4. Tạo Main Screen và Fragments
  - [x] 4.1 Tạo activity_main.xml
    - Header với gradient background, user greeting, points, level
    - TabLayout với 4 tabs
    - ViewPager2 cho fragments
    - _Requirements: 2.1, 2.4_
  - [x] 4.2 Tạo fragment_home.xml
    - Stats cards: Today Points, Week Points, Total Points
    - Activity stats: Today Activities, Total Activities, Rank
    - Quick action cards: Activities, Rewards, Leaderboard
    - _Requirements: 3.1, 3.2, 3.3, 3.4_
  - [x] 4.3 Tạo fragment_activity.xml
    - Search EditText với icon
    - Category Spinner
    - RecyclerView cho activities
    - ProgressBar
    - _Requirements: 4.1_
  - [x] 4.4 Tạo item_activity.xml
    - CardView với category color accent
    - Icon emoji, name, description
    - Points badge, complete button
    - _Requirements: 4.2, 4.4_

- [ ] 5. Checkpoint - Verify Authentication và Main Screens
  - Build project và verify không có lỗi
  - Test Login, Register, Main screens hiển thị đúng
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Tạo Statistics Fragment
  - [x] 6.1 Tạo fragment_statistics.xml
    - Level progress section với progress bar
    - Stats summary cards (Today, Week, Total)
    - Category breakdown container
    - Weekly chart container
    - Recent activities container
    - _Requirements: 5.1_
  - [x] 6.2 Tạo item_category_stat.xml
    - Icon emoji, category name
    - Count text, progress bar
    - _Requirements: 5.2_
  - [x] 6.3 Tạo item_weekly_bar.xml
    - Day label, bar view, points text
    - _Requirements: 5.3_
  - [x] 6.4 Tạo item_recent_activity.xml
    - Activity name, date, points earned
    - _Requirements: 5.4_

- [x] 7. Tạo Profile Fragment
  - [x] 7.1 Tạo fragment_profile.xml
    - Avatar section với emoji và level badge
    - User info: fullname, username, email
    - Stats row: points, level, activities, rank
    - Achievements container
    - Action cards: Leaderboard, Rewards, Settings, Logout
    - _Requirements: 6.1, 6.2, 6.4_
  - [x] 7.2 Tạo item_achievement.xml
    - Achievement icon, name, description
    - Lock overlay cho unlocked state
    - _Requirements: 6.3_

- [x] 8. Tạo Leaderboard và Rewards Screens
  - [x] 8.1 Tạo activity_leaderboard.xml
    - Toolbar với back button và title
    - ScrollView với LinearLayout container
    - _Requirements: 7.1_
  - [x] 8.2 Tạo item_leaderboard.xml
    - Rank badge (medal cho top 3)
    - User name, level, activities count
    - Points (large, bold)
    - Highlight view cho current user
    - _Requirements: 7.2_
  - [x] 8.3 Tạo activity_rewards.xml
    - Toolbar
    - User points display
    - GridLayout cho reward items
    - _Requirements: 8.1_
  - [x] 8.4 Tạo item_reward.xml
    - Icon emoji, name, description
    - Cost in points
    - Redeem button
    - _Requirements: 8.2_

- [ ] 9. Checkpoint - Verify User Screens
  - Build project và verify không có lỗi
  - Test Statistics, Profile, Leaderboard, Rewards screens
  - Ensure all tests pass, ask the user if questions arise.

- [~] 10. Tạo Admin Screens (SKIPPED - sẽ xử lý sau)
  - [~] 10.1 Tạo activity_admin.xml - SKIPPED
  - [~] 10.2 Tạo activity_admin_manage_activities.xml - SKIPPED
  - [~] 10.3 Tạo item_admin_activity.xml - SKIPPED
  - [~] 10.4 Tạo activity_admin_manage_users.xml - SKIPPED
  - [~] 10.5 Tạo item_admin_user.xml - SKIPPED
  - [~] 10.6 Tạo activity_admin_statistics.xml - SKIPPED
  - [~] 10.7 Tạo dialog_add_activity.xml - SKIPPED

- [x] 11. Tạo Settings Screen
  - [x] 11.1 Tạo activity_settings.xml
    - Toolbar
    - Settings items trong cards
    - _Requirements: 11.1, 11.2_

- [ ] 12. Final Checkpoint - Full App Verification
  - Build project và verify không có lỗi
  - Test tất cả screens và navigation flows
  - Verify view IDs match với Java code
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tất cả layouts sử dụng ConstraintLayout hoặc LinearLayout
- Dimensions sử dụng dp cho spacing, sp cho text size
- Colors reference từ colors.xml
- View IDs phải match với Java code đã có
- Sử dụng Material Components (MaterialButton, TextInputLayout, MaterialCardView)
