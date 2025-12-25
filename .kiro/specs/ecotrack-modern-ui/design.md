# Design Document: EcoTrack Modern UI

## Overview

Thiết kế giao diện hiện đại cho ứng dụng EcoTrack sử dụng Material Design 3 với màu xanh lá chủ đạo. Giao diện tập trung vào trải nghiệm người dùng mượt mà, dễ sử dụng và phù hợp với chủ đề bảo vệ môi trường.

### Design Principles

1. **Green-First**: Màu xanh lá (#4CAF50) là màu chủ đạo, tượng trưng cho môi trường
2. **Card-Based Layout**: Sử dụng CardView với bo tròn và shadow để tạo chiều sâu
3. **Emoji Icons**: Sử dụng emoji thay vì icon vector để đơn giản và thân thiện
4. **Consistent Spacing**: Padding 16dp, margin 8-16dp, corner radius 12-16dp
5. **Accessibility**: Contrast ratio đủ cao, touch targets tối thiểu 48dp

## Architecture

```
res/
├── layout/
│   ├── activity_splash.xml
│   ├── activity_login.xml
│   ├── activity_register.xml
│   ├── activity_main.xml
│   ├── activity_leaderboard.xml
│   ├── activity_rewards.xml
│   ├── activity_settings.xml
│   ├── activity_admin.xml
│   ├── activity_admin_manage_activities.xml
│   ├── activity_admin_manage_users.xml
│   ├── activity_admin_statistics.xml
│   ├── fragment_home.xml
│   ├── fragment_activity.xml
│   ├── fragment_statistics.xml
│   ├── fragment_profile.xml
│   ├── item_activity.xml
│   ├── item_leaderboard.xml
│   ├── item_reward.xml
│   ├── item_category_stat.xml
│   ├── item_weekly_bar.xml
│   ├── item_recent_activity.xml
│   ├── item_achievement.xml
│   ├── item_admin_activity.xml
│   ├── item_admin_user.xml
│   └── dialog_add_activity.xml
├── values/
│   ├── colors.xml
│   ├── strings.xml
│   └── themes.xml
└── drawable/
    ├── bg_gradient_green.xml
    ├── bg_card_rounded.xml
    ├── bg_button_primary.xml
    ├── bg_button_secondary.xml
    ├── bg_input_field.xml
    ├── bg_rank_gold.xml
    ├── bg_rank_silver.xml
    ├── bg_rank_bronze.xml
    ├── bg_tab_indicator.xml
    └── bg_circle_avatar.xml
```

## Components and Interfaces

### Color Palette

```xml
<!-- Primary Colors -->
colorPrimary: #4CAF50 (Green 500)
colorPrimaryDark: #2E7D32 (Green 800)
colorPrimaryLight: #81C784 (Green 300)
colorAccent: #8BC34A (Light Green 500)

<!-- Background Colors -->
colorBackground: #F5F5F5
colorSurface: #FFFFFF
colorSurfaceVariant: #E8F5E9 (Green 50)

<!-- Text Colors -->
colorOnPrimary: #FFFFFF
colorOnBackground: #212121
colorOnSurface: #424242
colorTextSecondary: #757575

<!-- Category Colors -->
colorTransport: #4CAF50
colorEnergy: #FFC107
colorWater: #03A9F4
colorWaste: #8BC34A
colorGreen: #009688
colorConsumption: #9C27B0

<!-- Admin Colors -->
colorAdmin: #3F51B5 (Indigo)
colorAdminDark: #303F9F
```

### Layout Components

#### 1. Splash Screen (activity_splash.xml)
- Full screen gradient background (green)
- Centered app logo (emoji 🌱 size 80sp)
- App name "EcoTrack" (bold, white, 32sp)
- Tagline "Bảo vệ môi trường mỗi ngày"
- ProgressBar at bottom

#### 2. Login Screen (activity_login.xml)
- Gradient background
- Logo section at top (30% height)
- White card container with rounded corners (24dp)
- TextInputLayout with outlined style for username/password
- MaterialButton for login (full width, rounded)
- "Chưa có tài khoản? Đăng ký" link
- ProgressBar overlay

#### 3. Register Screen (activity_register.xml)
- Similar to login but with 4 fields: fullname, username, email, password
- Scrollable content for smaller screens

#### 4. Main Screen (activity_main.xml)
- Header section with gradient background
  - User greeting "Xin chào, [name]!"
  - Points badge and Level badge
- TabLayout with 4 tabs (icons + text)
- ViewPager2 for fragments

#### 5. Home Fragment (fragment_home.xml)
- Stats row: Today Points | Week Points | Total Points
- Activity stats: Today Activities | Total Activities | Rank
- Quick action cards (3 columns):
  - 🌱 Hoạt động
  - 🎁 Phần thưởng
  - 🏆 Xếp hạng

#### 6. Activities Fragment (fragment_activity.xml)
- Search bar with icon
- Category spinner/dropdown
- RecyclerView with item_activity.xml
- ProgressBar for loading

#### 7. Activity Item (item_activity.xml)
- CardView with category color accent
- Left: Icon (emoji, 40sp)
- Center: Name (bold) + Description (secondary)
- Right: Points badge + Complete button
- Completed state: checkmark, disabled button

#### 8. Statistics Fragment (fragment_statistics.xml)
- Level progress section
  - Circular progress or horizontal bar
  - "X/100 điểm đến cấp tiếp theo"
- Stats summary cards (Today | Week | Total)
- Category breakdown (LinearLayout with item_category_stat.xml)
- Weekly chart (horizontal LinearLayout with item_weekly_bar.xml)
- Recent activities list

#### 9. Profile Fragment (fragment_profile.xml)
- Avatar section
  - Large emoji avatar (60sp)
  - Level badge overlay
- User info: Fullname, @username, email
- Stats row: Points | Level | Activities | Rank
- Achievements grid (2 columns)
- Action cards: Leaderboard, Rewards, Settings, Logout

#### 10. Leaderboard Screen (activity_leaderboard.xml)
- Toolbar with back button
- Title "Bảng Xếp Hạng"
- ScrollView with LinearLayout for items
- item_leaderboard.xml:
  - Rank badge (medal emoji for top 3)
  - Avatar + Name
  - Level + Activities count
  - Points (large, bold)
  - Highlight for current user

#### 11. Rewards Screen (activity_rewards.xml)
- Toolbar
- User points display (prominent)
- GridLayout (2 columns) with item_reward.xml
- item_reward.xml:
  - Icon (emoji, 48sp)
  - Name + Description
  - Cost in points
  - Redeem button

#### 12. Admin Dashboard (activity_admin.xml)
- Admin header with greeting
- Stats overview (4 cards in 2x2 grid):
  - Total Users
  - Total Activities
  - Completed Activities
  - Total Points
- Management cards:
  - 📋 Quản lý hoạt động
  - 👥 Quản lý người dùng
  - 📊 Thống kê
  - 💾 Database
  - 🚪 Đăng xuất

#### 13. Settings Screen (activity_settings.xml)
- Toolbar
- Settings items in cards:
  - Notifications toggle
  - Theme selection
  - About app
  - Privacy policy
  - Version info

## Data Models

Các data models đã được định nghĩa trong Java code:
- `Activity.java`: id, apiId, name, description, points, category, icon, isCompleted
- `User.java`: id, username, email, fullname, role, points, level, avatar
- API Response models trong package `api/models/`

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

Vì đây là dự án UI/Layout thuần túy (XML files), không có logic business phức tạp cần property-based testing. Các acceptance criteria chủ yếu liên quan đến:
- Cấu trúc layout XML
- Styling và theming
- View IDs matching với Java code

Các tiêu chí này được verify thông qua:
1. Build thành công (XML syntax valid)
2. Runtime không crash (view IDs tồn tại)
3. Visual inspection (UI hiển thị đúng)

## Error Handling

### Layout Errors
- Sử dụng `tools:` namespace để preview trong Android Studio
- Fallback text cho empty states
- Default values cho tất cả attributes

### Missing Resources
- Tất cả drawable resources phải được tạo trước khi reference
- Colors phải được định nghĩa trong colors.xml
- Strings nên được externalize (nhưng có thể hardcode cho MVP)

### Compatibility
- minSdk 24 (Android 7.0)
- Sử dụng AndroidX và Material Components
- Tránh deprecated attributes

## Testing Strategy

### Manual Testing
1. Build và run app trên emulator/device
2. Verify tất cả screens hiển thị đúng
3. Test navigation flow
4. Test responsive trên các screen sizes khác nhau

### Automated Testing
- Layout validation qua Android Lint
- Espresso UI tests cho critical flows (optional)

### Checklist
- [ ] Tất cả view IDs match với Java code
- [ ] Không có hardcoded dimensions (sử dụng dp/sp)
- [ ] Colors consistent với palette
- [ ] Touch targets >= 48dp
- [ ] Text readable (contrast ratio)
