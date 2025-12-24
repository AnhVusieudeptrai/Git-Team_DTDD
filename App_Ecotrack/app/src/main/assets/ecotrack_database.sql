-- EcoTrack Database Schema và Dữ liệu Mẫu
-- Tạo ngày: 2024-12-22

-- Tạo bảng Users
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    fullname TEXT NOT NULL,
    email TEXT,
    role TEXT DEFAULT 'user',
    points INTEGER DEFAULT 0,
    level INTEGER DEFAULT 1,
    created_at TEXT
);

-- Tạo bảng Activities
CREATE TABLE IF NOT EXISTS activities (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT,
    points INTEGER DEFAULT 0,
    category TEXT,
    icon TEXT,
    created_at TEXT
);

-- Tạo bảng User Activities
CREATE TABLE IF NOT EXISTS user_activities (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    activity_id INTEGER,
    completed_date TEXT,
    points_earned INTEGER,
    FOREIGN KEY(user_id) REFERENCES users(id),
    FOREIGN KEY(activity_id) REFERENCES activities(id)
);

-- Chèn dữ liệu mặc định cho Users
INSERT INTO users (username, password, fullname, email, role, points, level, created_at) VALUES
('admin', 'admin123', 'Administrator', 'admin@ecotrack.com', 'admin', 0, 1, '2024-12-22 10:00:00'),
('user', 'user123', 'Người dùng mẫu', 'user@ecotrack.com', 'user', 150, 2, '2024-12-22 10:00:00'),
('khoa_zo', 'khoa123', 'Khoa Zo', 'khoa@ecotrack.com', 'user', 280, 3, '2024-12-22 10:00:00'),
('eco_lover', 'eco123', 'Người yêu môi trường', 'ecolover@ecotrack.com', 'user', 420, 5, '2024-12-22 10:00:00'),
('green_hero', 'green123', 'Anh hùng xanh', 'greenhero@ecotrack.com', 'user', 350, 4, '2024-12-22 10:00:00');

-- Chèn dữ liệu mặc định cho Activities
INSERT INTO activities (name, description, points, category, icon, created_at) VALUES
('Đi xe đạp thay xe máy', 'Sử dụng xe đạp để di chuyển thay vì phương tiện có động cơ', 20, 'transport', 'bike', '2024-12-22 10:00:00'),
('Tắt điện khi không dùng', 'Tiết kiệm năng lượng bằng cách tắt đèn và thiết bị điện', 10, 'energy', 'light', '2024-12-22 10:00:00'),
('Sử dụng túi vải', 'Mang theo túi vải khi đi mua sắm thay vì túi nilon', 15, 'waste', 'bag', '2024-12-22 10:00:00'),
('Phân loại rác', 'Phân loại rác thải tại nguồn', 20, 'waste', 'recycle', '2024-12-22 10:00:00'),
('Tắm nước nhanh', 'Giảm thời gian tắm để tiết kiệm nước', 10, 'water', 'shower', '2024-12-22 10:00:00'),
('Trồng cây xanh', 'Trồng và chăm sóc cây xanh', 30, 'green', 'tree', '2024-12-22 10:00:00'),
('Sử dụng đồ tái chế', 'Ưu tiên sử dụng sản phẩm làm từ nguyên liệu tái chế', 15, 'consumption', 'product', '2024-12-22 10:00:00'),
('Không sử dụng ống hút nhựa', 'Từ chối ống hút nhựa khi uống nước', 10, 'waste', 'straw', '2024-12-22 10:00:00'),
('Đi bộ đường ngắn', 'Đi bộ thay vì đi xe cho quãng đường ngắn', 15, 'transport', 'walk', '2024-12-22 10:00:00'),
('Tắt vòi nước khi đánh răng', 'Tiết kiệm nước khi đánh răng', 10, 'water', 'faucet', '2024-12-22 10:00:00'),
('Sử dụng giao thông công cộng', 'Đi xe buýt, tàu điện thay vì xe cá nhân', 25, 'transport', 'bus', '2024-12-22 10:00:00'),
('Tắt máy tính khi không dùng', 'Tắt hoàn toàn máy tính thay vì để chế độ sleep', 15, 'energy', 'computer', '2024-12-22 10:00:00'),
('Sử dụng bóng đèn LED', 'Thay thế bóng đèn thường bằng đèn LED tiết kiệm năng lượng', 20, 'energy', 'led', '2024-12-22 10:00:00'),
('Thu gom pin cũ', 'Thu gom và xử lý pin cũ đúng cách', 25, 'waste', 'battery', '2024-12-22 10:00:00'),
('Tưới cây bằng nước mưa', 'Sử dụng nước mưa để tưới cây thay vì nước máy', 20, 'water', 'rain', '2024-12-22 10:00:00');

-- Chèn dữ liệu mẫu cho User Activities (hoạt động đã hoàn thành)
INSERT INTO user_activities (user_id, activity_id, completed_date, points_earned) VALUES
-- User 2 (user) - 150 điểm
(2, 1, '2024-12-20 08:30:00', 20),
(2, 2, '2024-12-20 09:15:00', 10),
(2, 3, '2024-12-20 14:20:00', 15),
(2, 4, '2024-12-21 07:45:00', 20),
(2, 5, '2024-12-21 19:30:00', 10),
(2, 6, '2024-12-21 16:00:00', 30),
(2, 7, '2024-12-22 10:30:00', 15),
(2, 8, '2024-12-22 12:15:00', 10),
(2, 9, '2024-12-22 08:00:00', 15),
(2, 10, '2024-12-22 07:30:00', 10),

-- User 3 (khoa_zo) - 280 điểm
(3, 1, '2024-12-19 08:00:00', 20),
(3, 2, '2024-12-19 09:00:00', 10),
(3, 3, '2024-12-19 14:00:00', 15),
(3, 4, '2024-12-19 16:00:00', 20),
(3, 5, '2024-12-20 07:30:00', 10),
(3, 6, '2024-12-20 15:00:00', 30),
(3, 7, '2024-12-20 17:00:00', 15),
(3, 8, '2024-12-21 12:00:00', 10),
(3, 9, '2024-12-21 08:30:00', 15),
(3, 10, '2024-12-21 07:45:00', 10),
(3, 11, '2024-12-21 18:00:00', 25),
(3, 12, '2024-12-22 09:00:00', 15),
(3, 13, '2024-12-22 10:30:00', 20),
(3, 14, '2024-12-22 14:00:00', 25),
(3, 15, '2024-12-22 16:30:00', 20),

-- User 4 (eco_lover) - 420 điểm
(4, 1, '2024-12-18 08:00:00', 20),
(4, 2, '2024-12-18 09:00:00', 10),
(4, 3, '2024-12-18 14:00:00', 15),
(4, 4, '2024-12-18 16:00:00', 20),
(4, 5, '2024-12-18 19:00:00', 10),
(4, 6, '2024-12-19 15:00:00', 30),
(4, 7, '2024-12-19 17:00:00', 15),
(4, 8, '2024-12-19 12:00:00', 10),
(4, 9, '2024-12-19 08:30:00', 15),
(4, 10, '2024-12-19 07:45:00', 10),
(4, 11, '2024-12-20 18:00:00', 25),
(4, 12, '2024-12-20 09:00:00', 15),
(4, 13, '2024-12-20 10:30:00', 20),
(4, 14, '2024-12-20 14:00:00', 25),
(4, 15, '2024-12-20 16:30:00', 20),
(4, 1, '2024-12-21 08:00:00', 20),
(4, 2, '2024-12-21 09:00:00', 10),
(4, 3, '2024-12-21 14:00:00', 15),
(4, 4, '2024-12-21 16:00:00', 20),
(4, 5, '2024-12-21 19:00:00', 10),
(4, 6, '2024-12-22 15:00:00', 30),
(4, 7, '2024-12-22 17:00:00', 15),
(4, 8, '2024-12-22 12:00:00', 10),
(4, 9, '2024-12-22 08:30:00', 15),
(4, 10, '2024-12-22 07:45:00', 10),

-- User 5 (green_hero) - 350 điểm
(5, 1, '2024-12-19 08:15:00', 20),
(5, 2, '2024-12-19 09:30:00', 10),
(5, 3, '2024-12-19 14:45:00', 15),
(5, 4, '2024-12-19 16:20:00', 20),
(5, 5, '2024-12-19 19:15:00', 10),
(5, 6, '2024-12-20 15:30:00', 30),
(5, 7, '2024-12-20 17:45:00', 15),
(5, 8, '2024-12-20 12:30:00', 10),
(5, 9, '2024-12-20 08:45:00', 15),
(5, 10, '2024-12-20 07:50:00', 10),
(5, 11, '2024-12-21 18:15:00', 25),
(5, 12, '2024-12-21 09:30:00', 15),
(5, 13, '2024-12-21 10:45:00', 20),
(5, 14, '2024-12-21 14:30:00', 25),
(5, 15, '2024-12-21 16:45:00', 20),
(5, 1, '2024-12-22 08:20:00', 20),
(5, 2, '2024-12-22 09:35:00', 10),
(5, 3, '2024-12-22 14:50:00', 15),
(5, 4, '2024-12-22 16:25:00', 20),
(5, 5, '2024-12-22 19:20:00', 10);

-- Tạo index để tối ưu hóa truy vấn
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_activities_category ON activities(category);
CREATE INDEX IF NOT EXISTS idx_user_activities_user_id ON user_activities(user_id);
CREATE INDEX IF NOT EXISTS idx_user_activities_activity_id ON user_activities(activity_id);
CREATE INDEX IF NOT EXISTS idx_user_activities_date ON user_activities(completed_date);