# EcoTrack Backend API

Backend API cho ứng dụng EcoTrack - Theo dõi hoạt động bảo vệ môi trường.

## Tech Stack
- Node.js + Express
- MongoDB (Atlas)
- JWT Authentication
- Google OAuth2

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

### 3. Seed dữ liệu mẫu
```bash
npm run seed
```

### 4. Chạy server
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

## Test Accounts
- **Admin**: admin / admin123
- **User**: user / user123
