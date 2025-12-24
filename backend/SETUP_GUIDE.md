# Hướng dẫn Setup EcoTrack Backend từ A-Z

## Bước 1: Cài đặt Node.js

1. Truy cập https://nodejs.org/
2. Tải và cài đặt phiên bản LTS (khuyến nghị v18 hoặc v20)
3. Kiểm tra cài đặt:
```bash
node --version
npm --version
```

---

## Bước 2: Tạo MongoDB Atlas (Database Cloud miễn phí)

### 2.1. Tạo tài khoản
1. Truy cập https://www.mongodb.com/cloud/atlas
2. Click "Try Free" và đăng ký tài khoản (có thể dùng Google)

### 2.2. Tạo Cluster
1. Chọn **FREE** tier (M0 Sandbox)
2. Chọn Cloud Provider: **AWS**
3. Chọn Region gần nhất (Singapore hoặc Hong Kong)
4. Click "Create Cluster" (đợi 1-3 phút)

### 2.3. Tạo Database User
1. Vào **Database Access** (menu bên trái)
2. Click "Add New Database User"
3. Chọn **Password** authentication
4. Nhập username và password (GHI NHỚ LẠI!)
5. Database User Privileges: **Read and write to any database**
6. Click "Add User"

### 2.4. Cấu hình Network Access
1. Vào **Network Access** (menu bên trái)
2. Click "Add IP Address"
3. Click "Allow Access from Anywhere" (cho development)
4. Click "Confirm"

### 2.5. Lấy Connection String
1. Vào **Database** → Click "Connect"
2. Chọn "Connect your application"
3. Copy connection string, ví dụ:
```
mongodb+srv://<username>:<password>@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority
```
4. Thay `<username>` và `<password>` bằng thông tin đã tạo
5. Thêm tên database vào URL:
```
mongodb+srv://myuser:mypassword@cluster0.xxxxx.mongodb.net/ecotrack?retryWrites=true&w=majority
```

---

## Bước 3: Tạo Google OAuth2 Credentials

### 3.1. Tạo Google Cloud Project
1. Truy cập https://console.cloud.google.com/
2. Click dropdown project (góc trên trái) → "New Project"
3. Đặt tên: "EcoTrack" → Create

### 3.2. Bật Google+ API
1. Vào **APIs & Services** → **Library**
2. Tìm "Google+ API" → Enable
3. Tìm "Google Identity" → Enable

### 3.3. Cấu hình OAuth Consent Screen
1. Vào **APIs & Services** → **OAuth consent screen**
2. Chọn **External** → Create
3. Điền thông tin:
   - App name: EcoTrack
   - User support email: email của bạn
   - Developer contact: email của bạn
4. Click "Save and Continue" qua các bước

### 3.4. Tạo OAuth2 Credentials
1. Vào **APIs & Services** → **Credentials**
2. Click "Create Credentials" → "OAuth client ID"
3. Application type: **Web application**
4. Name: "EcoTrack Backend"
5. Authorized redirect URIs: 
   - `http://localhost:3000/api/auth/google/callback`
6. Click "Create"
7. **GHI LẠI Client ID và Client Secret**

### 3.5. Tạo Android OAuth Client (cho app)
1. Click "Create Credentials" → "OAuth client ID"
2. Application type: **Android**
3. Name: "EcoTrack Android"
4. Package name: `com.example.app_ecotrack`
5. SHA-1 certificate fingerprint:
   - Mở terminal trong Android Studio
   - Chạy: `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`
   - Copy SHA1 fingerprint
6. Click "Create"
7. **GHI LẠI Client ID cho Android**

---

## Bước 4: Setup Firebase (Push Notifications)

### 4.1. Tạo Firebase Project
1. Truy cập https://console.firebase.google.com/
2. Click "Add project"
3. Chọn project Google Cloud đã tạo (EcoTrack) hoặc tạo mới
4. Disable Google Analytics (không cần thiết)
5. Click "Create project"

### 4.2. Thêm Android App
1. Click icon Android để thêm app
2. Package name: `com.example.app_ecotrack`
3. App nickname: EcoTrack
4. Download `google-services.json`
5. Đặt file vào `App_Ecotrack/app/google-services.json`

### 4.3. Lấy Service Account Key (cho Backend)
1. Vào **Project Settings** (icon bánh răng)
2. Tab **Service accounts**
3. Click "Generate new private key"
4. Download file JSON
5. Đổi tên thành `firebase-service-account.json`
6. Đặt vào folder `backend/`

---

## Bước 5: Cấu hình Backend

### 5.1. Tạo file .env
```bash
cd backend
cp .env.example .env
```

### 5.2. Chỉnh sửa file .env
```env
PORT=3000
NODE_ENV=development

# MongoDB - thay bằng connection string của bạn
MONGODB_URI=mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/ecotrack?retryWrites=true&w=majority

# JWT Secret - tạo chuỗi ngẫu nhiên
JWT_SECRET=your-super-secret-key-at-least-32-characters-long

# Google OAuth2 - từ Google Cloud Console
GOOGLE_CLIENT_ID=xxxxx.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-xxxxx

# Firebase
FIREBASE_PROJECT_ID=ecotrack-xxxxx
```

### 5.3. Cài đặt dependencies
```bash
npm install
```

### 5.4. Seed dữ liệu mẫu
```bash
npm run seed
```

### 5.5. Chạy server
```bash
npm run dev
```

### 5.6. Test API
Mở browser: http://localhost:3000/api/health

Kết quả mong đợi:
```json
{"status":"OK","message":"EcoTrack API is running"}
```

---

## Bước 6: Test với Postman

### Login
```
POST http://localhost:3000/api/auth/login
Content-Type: application/json

{
  "username": "user",
  "password": "user123"
}
```

### Get Activities (cần token)
```
GET http://localhost:3000/api/activities
Authorization: Bearer <token_từ_login>
```

---

## Bước 7: Deploy lên Railway (miễn phí)

### 7.1. Tạo tài khoản Railway
1. Truy cập https://railway.app/
2. Đăng nhập bằng GitHub

### 7.2. Deploy
1. Click "New Project"
2. Chọn "Deploy from GitHub repo"
3. Chọn repository chứa backend
4. Railway sẽ tự động detect Node.js

### 7.3. Cấu hình Environment Variables
1. Vào project → Variables
2. Thêm tất cả biến từ file .env

### 7.4. Lấy URL
- Railway sẽ cung cấp URL dạng: `https://ecotrack-backend.up.railway.app`
- Dùng URL này trong Android app

---

## Troubleshooting

### Lỗi MongoDB connection
- Kiểm tra IP đã được whitelist trong Network Access
- Kiểm tra username/password đúng
- Kiểm tra connection string có tên database

### Lỗi Google OAuth
- Kiểm tra Client ID đúng
- Kiểm tra SHA-1 fingerprint cho Android

### Lỗi "Cannot find module"
```bash
rm -rf node_modules
npm install
```

---

## Tiếp theo

Sau khi backend hoạt động, tôi sẽ hướng dẫn bạn:
1. Cập nhật Android app để kết nối API
2. Tích hợp Google Sign-In
3. Tích hợp Push Notifications
