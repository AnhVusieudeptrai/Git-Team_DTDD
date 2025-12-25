# EcoTrack Backend API

Backend API cho ứng dụng EcoTrack - Theo dõi hoạt động bảo vệ môi trường.

## Tech Stack
- Node.js + Express
- MongoDB (Atlas)
- JWT Authentication
- Google OAuth2
- Firebase Cloud Messaging (Push Notifications)

## Cài đặt

### 1. Cài đặt dependencies
```bash
cd backend
npm install
```

### 2. Cấu hình môi trường
```bash
cp .env.example .env
```

Chỉnh sửa file `.env` với thông tin của bạn:
- `MONGODB_URI`: Connection string từ MongoDB Atlas
- `JWT_SECRET`: Chuỗi bí mật cho JWT
- `GOOGLE_CLIENT_ID`: Client ID từ Google Cloud Console

### 3. Cấu hình Firebase (cho Push Notifications)
- Tải file `firebase-service-account.json` từ Firebase Console
- Đặt vào thư mục `backend/`

### 4. Seed dữ liệu mẫu
```bash
npm run seed:all
```

### 5. Chạy server
```bash
# Development
npm run dev

# Production
npm start
```

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Đăng ký tài khoản |
| POST | `/api/auth/login` | Đăng nhập |
| POST | `/api/auth/google` | Đăng nhập bằng Google |
| GET | `/api/auth/me` | Lấy thông tin user hiện tại |
| POST | `/api/auth/fcm-token` | Cập nhật FCM token |

### Activities
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/activities` | Lấy danh sách hoạt động |
| POST | `/api/activities/:id/complete` | Hoàn thành hoạt động |
| GET | `/api/activities/history` | Lịch sử hoạt động |
| GET | `/api/activities/today` | Hoạt động hôm nay |

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/profile` | Lấy profile |
| PUT | `/api/users/profile` | Cập nhật profile |
| GET | `/api/users/stats` | Thống kê cá nhân |

### Leaderboard
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/leaderboard` | Bảng xếp hạng tổng |
| GET | `/api/leaderboard/weekly` | Bảng xếp hạng tuần |

### Badges (Huy hiệu)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/badges` | Lấy tất cả huy hiệu |
| GET | `/api/badges/my` | Huy hiệu đã đạt được |
| POST | `/api/badges` | [Admin] Tạo huy hiệu |
| PUT | `/api/badges/:id` | [Admin] Cập nhật huy hiệu |
| DELETE | `/api/badges/:id` | [Admin] Xóa huy hiệu |

### Challenges (Thử thách)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/challenges` | Lấy thử thách đang hoạt động |
| POST | `/api/challenges/:id/join` | Tham gia thử thách |
| GET | `/api/challenges/my` | Thử thách của tôi |
| POST | `/api/challenges` | [Admin] Tạo thử thách |
| PUT | `/api/challenges/:id` | [Admin] Cập nhật thử thách |
| DELETE | `/api/challenges/:id` | [Admin] Xóa thử thách |

### Streaks (Chuỗi ngày)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/streaks` | Lấy thông tin streak |
| GET | `/api/streaks/leaderboard` | Bảng xếp hạng streak |

## Tính năng mới

### 🏆 Hệ thống Huy hiệu
- Huy hiệu theo streak (3, 7, 14, 30, 100 ngày)
- Huy hiệu theo điểm (100, 500, 1000, 5000, 10000 điểm)
- Huy hiệu theo số hoạt động (10, 50, 100, 500, 1000)
- Độ hiếm: Common, Rare, Epic, Legendary

### 🎯 Thử thách
- Thử thách tuần/tháng tự động tạo
- Theo dõi tiến độ real-time
- Phần thưởng điểm và huy hiệu

### 🔥 Streak
- Theo dõi chuỗi ngày hoạt động liên tiếp
- Kỷ lục streak cá nhân
- Bảng xếp hạng streak

### 🔔 Push Notifications
- Nhắc nhở hàng ngày (9:00 AM)
- Cảnh báo streak sắp mất
- Thông báo huy hiệu mới
- Thông báo hoàn thành thử thách

## Test Accounts
- **Admin**: admin / admin123
- **User**: user / user123
