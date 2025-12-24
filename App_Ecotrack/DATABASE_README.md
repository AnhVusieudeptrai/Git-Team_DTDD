# EcoTrack Database Documentation

## Tổng quan
EcoTrack sử dụng SQLite database để lưu trữ dữ liệu ứng dụng. Database được tạo từ file SQL có sẵn trong thư mục assets và có thể được quản lý thông qua DatabaseManagerActivity.

**Database Version hiện tại: 3**

## Cấu trúc Database

### 1. Bảng Users (users)
Lưu trữ thông tin người dùng và admin.

| Cột | Kiểu dữ liệu | Mô tả |
|-----|-------------|-------|
| id | INTEGER PRIMARY KEY | ID duy nhất của người dùng |
| username | TEXT UNIQUE NOT NULL | Tên đăng nhập |
| password | TEXT NOT NULL | Mật khẩu |
| fullname | TEXT NOT NULL | Họ và tên |
| email | TEXT | Email |
| role | TEXT DEFAULT 'user' | Vai trò (user/admin) |
| points | INTEGER DEFAULT 0 | Điểm tích lũy |
| level | INTEGER DEFAULT 1 | Cấp độ |
| created_at | TEXT | Thời gian tạo |

### 2. Bảng Activities (activities)
Lưu trữ các hoạt động bảo vệ môi trường.

| Cột | Kiểu dữ liệu | Mô tả |
|-----|-------------|-------|
| id | INTEGER PRIMARY KEY | ID duy nhất của hoạt động |
| name | TEXT NOT NULL | Tên hoạt động |
| description | TEXT | Mô tả chi tiết |
| points | INTEGER DEFAULT 0 | Điểm thưởng |
| category | TEXT | Danh mục (transport, energy, water, waste, green, consumption) |
| icon | TEXT | Tên icon |
| created_at | TEXT | Thời gian tạo |

### 3. Bảng User Activities (user_activities)
Lưu trữ lịch sử hoạt động đã hoàn thành của người dùng.

| Cột | Kiểu dữ liệu | Mô tả |
|-----|-------------|-------|
| id | INTEGER PRIMARY KEY | ID duy nhất |
| user_id | INTEGER | ID người dùng (FK) |
| activity_id | INTEGER | ID hoạt động (FK) |
| completed_date | TEXT | Thời gian hoàn thành |
| points_earned | INTEGER | Điểm nhận được |

## Dữ liệu mặc định

### Tài khoản mặc định
- **Admin**: username=`admin`, password=`admin123`
- **User mẫu**: username=`user`, password=`user123`
- **Khoa Zo**: username=`khoa_zo`, password=`khoa123`
- **Eco Lover**: username=`eco_lover`, password=`eco123`
- **Green Hero**: username=`green_hero`, password=`green123`

### Hoạt động mặc định (15 hoạt động)
1. **Giao thông (transport)**:
   - Đi xe đạp thay xe máy (20 điểm)
   - Đi bộ đường ngắn (15 điểm)
   - Sử dụng giao thông công cộng (25 điểm)

2. **Năng lượng (energy)**:
   - Tắt điện khi không dùng (10 điểm)
   - Tắt máy tính khi không dùng (15 điểm)
   - Sử dụng bóng đèn LED (20 điểm)

3. **Nước (water)**:
   - Tắm nước nhanh (10 điểm)
   - Tắt vòi nước khi đánh răng (10 điểm)
   - Tưới cây bằng nước mưa (20 điểm)

4. **Rác thải (waste)**:
   - Sử dụng túi vải (15 điểm)
   - Phân loại rác (20 điểm)
   - Không sử dụng ống hút nhựa (10 điểm)
   - Thu gom pin cũ (25 điểm)

5. **Cây xanh (green)**:
   - Trồng cây xanh (30 điểm)

6. **Tiêu dùng (consumption)**:
   - Sử dụng đồ tái chế (15 điểm)

## Cách sử dụng

### 1. Tạo Database từ SQL File
Database được tự động tạo từ file `assets/ecotrack_database.sql` khi ứng dụng chạy lần đầu.

### 2. Quản lý Database
Truy cập **Admin Panel** → **Quản lý Database** để:
- Xem thông tin database
- Xem danh sách users và activities
- Reset database về trạng thái mặc định
- Làm mới dữ liệu

### 3. Backup và Restore
- File SQL gốc: `app/src/main/assets/ecotrack_database.sql`
- Database file: `/data/data/com.example.app_ecotrack/databases/EcoTrack.db`

## Tính năng DatabaseHelper

### Phương thức chính
- `onCreate()`: Tạo database từ SQL file
- `onUpgrade()`: Nâng cấp database
- `resetDatabase()`: Reset về trạng thái mặc định
- `getDatabaseInfo()`: Lấy thông tin database
- `isDatabaseEmpty()`: Kiểm tra database có dữ liệu không

### Quản lý Users
- `insertUser()`: Thêm người dùng mới
- `checkUser()`: Xác thực đăng nhập
- `getUserById()`: Lấy thông tin user theo ID
- `updateUserPoints()`: Cập nhật điểm và level
- `updateUser()`: Cập nhật thông tin user (fullname, email, role)
- `deleteUser()`: Xóa user và các hoạt động liên quan
- `getAllUsers()`: Lấy tất cả users
- `getTotalUsers()`: Đếm tổng số users (không tính admin)
- `getLeaderboard()`: Lấy bảng xếp hạng
- `isUsernameExists()`: Kiểm tra username đã tồn tại chưa

### Quản lý Activities
- `getAllActivities()`: Lấy tất cả hoạt động
- `insertActivity()`: Thêm hoạt động mới
- `updateActivity()`: Cập nhật hoạt động
- `deleteActivity()`: Xóa hoạt động

### Quản lý User Activities
- `completeActivity()`: Đánh dấu hoàn thành hoạt động
- `getUserActivities()`: Lấy lịch sử hoạt động của user
- `getTodayActivities()`: Lấy hoạt động hôm nay
- `getTodayPoints()`: Tính điểm hôm nay
- `getTotalCompletedActivities()`: Tổng số hoạt động đã hoàn thành của tất cả users
- `getTotalPointsAllUsers()`: Tổng điểm của tất cả users

## Lưu ý quan trọng

1. **Database Version**: Hiện tại là version 3. Khi thay đổi cấu trúc, cần tăng version và cập nhật `onUpgrade()`.

2. **Backup**: Luôn backup file SQL trước khi thay đổi cấu trúc database.

3. **Performance**: Database có các index được tối ưu cho các truy vấn thường dùng.

4. **Security**: Mật khẩu được lưu dạng plain text (cần cải thiện bằng hash trong production).

5. **Data Source**: Tất cả dữ liệu được load từ file `ecotrack_database.sql` trong thư mục assets. Không còn sử dụng local data hardcoded trong code.

## Troubleshooting

### Database không tạo được
- Kiểm tra file `ecotrack_database.sql` có tồn tại trong assets không
- Xem log để kiểm tra lỗi SQL syntax
- Sử dụng fallback method `createDatabaseManually()`

### Dữ liệu bị mất
- Sử dụng chức năng Reset Database trong DatabaseManagerActivity
- Hoặc xóa app và cài đặt lại

### Performance chậm
- Kiểm tra các index đã được tạo chưa
- Tối ưu các truy vấn SQL phức tạp
- Sử dụng pagination cho dữ liệu lớn

## Phát triển tương lai

1. **Encryption**: Mã hóa mật khẩu và dữ liệu nhạy cảm
2. **Cloud Sync**: Đồng bộ dữ liệu với cloud database
3. **Migration**: Hệ thống migration tự động cho database updates
4. **Backup**: Tự động backup dữ liệu định kỳ
5. **Analytics**: Thêm bảng để lưu trữ analytics data