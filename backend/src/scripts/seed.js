/**
 * Database Seeder - Tạo dữ liệu mẫu cho EcoTrack
 * Chạy: node src/scripts/seed.js
 */

require('dotenv').config();
const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');
const User = require('../models/User');
const Activity = require('../models/Activity');
const UserActivity = require('../models/UserActivity');

const seedData = async () => {
  try {
    await mongoose.connect(process.env.MONGODB_URI);
    console.log('✅ Connected to MongoDB');

    // Clear existing data
    await User.deleteMany({});
    await Activity.deleteMany({});
    await UserActivity.deleteMany({});
    console.log('🗑️ Cleared existing data');

    // Create users
    const users = await User.create([
      {
        username: 'admin',
        email: 'admin@ecotrack.com',
        password: 'admin123',
        fullname: 'Administrator',
        role: 'admin',
        points: 0,
        level: 1
      },
      {
        username: 'user',
        email: 'user@ecotrack.com',
        password: 'user123',
        fullname: 'Người dùng mẫu',
        role: 'user',
        points: 150,
        level: 2
      },
      {
        username: 'khoa_zo',
        email: 'khoa@ecotrack.com',
        password: 'khoa123',
        fullname: 'Khoa Zo',
        role: 'user',
        points: 280,
        level: 3
      },
      {
        username: 'eco_lover',
        email: 'ecolover@ecotrack.com',
        password: 'eco123',
        fullname: 'Người yêu môi trường',
        role: 'user',
        points: 420,
        level: 5
      },
      {
        username: 'green_hero',
        email: 'greenhero@ecotrack.com',
        password: 'green123',
        fullname: 'Anh hùng xanh',
        role: 'user',
        points: 350,
        level: 4
      }
    ]);
    console.log(`👥 Created ${users.length} users`);

    // Create activities
    const activities = await Activity.create([
      { name: 'Đi xe đạp thay xe máy', description: 'Sử dụng xe đạp để di chuyển thay vì phương tiện có động cơ', points: 20, category: 'transport', icon: 'bike' },
      { name: 'Tắt điện khi không dùng', description: 'Tiết kiệm năng lượng bằng cách tắt đèn và thiết bị điện', points: 10, category: 'energy', icon: 'light' },
      { name: 'Sử dụng túi vải', description: 'Mang theo túi vải khi đi mua sắm thay vì túi nilon', points: 15, category: 'waste', icon: 'bag' },
      { name: 'Phân loại rác', description: 'Phân loại rác thải tại nguồn', points: 20, category: 'waste', icon: 'recycle' },
      { name: 'Tắm nước nhanh', description: 'Giảm thời gian tắm để tiết kiệm nước', points: 10, category: 'water', icon: 'shower' },
      { name: 'Trồng cây xanh', description: 'Trồng và chăm sóc cây xanh', points: 30, category: 'green', icon: 'tree' },
      { name: 'Sử dụng đồ tái chế', description: 'Ưu tiên sử dụng sản phẩm làm từ nguyên liệu tái chế', points: 15, category: 'consumption', icon: 'product' },
      { name: 'Không sử dụng ống hút nhựa', description: 'Từ chối ống hút nhựa khi uống nước', points: 10, category: 'waste', icon: 'straw' },
      { name: 'Đi bộ đường ngắn', description: 'Đi bộ thay vì đi xe cho quãng đường ngắn', points: 15, category: 'transport', icon: 'walk' },
      { name: 'Tắt vòi nước khi đánh răng', description: 'Tiết kiệm nước khi đánh răng', points: 10, category: 'water', icon: 'faucet' },
      { name: 'Sử dụng giao thông công cộng', description: 'Đi xe buýt, tàu điện thay vì xe cá nhân', points: 25, category: 'transport', icon: 'bus' },
      { name: 'Tắt máy tính khi không dùng', description: 'Tắt hoàn toàn máy tính thay vì để chế độ sleep', points: 15, category: 'energy', icon: 'computer' },
      { name: 'Sử dụng bóng đèn LED', description: 'Thay thế bóng đèn thường bằng đèn LED tiết kiệm năng lượng', points: 20, category: 'energy', icon: 'led' },
      { name: 'Thu gom pin cũ', description: 'Thu gom và xử lý pin cũ đúng cách', points: 25, category: 'waste', icon: 'battery' },
      { name: 'Tưới cây bằng nước mưa', description: 'Sử dụng nước mưa để tưới cây thay vì nước máy', points: 20, category: 'water', icon: 'rain' }
    ]);
    console.log(`🎯 Created ${activities.length} activities`);

    // Create sample user activities
    const sampleUserActivities = [];
    const regularUsers = users.filter(u => u.role === 'user');
    
    for (const user of regularUsers) {
      const numActivities = Math.floor(Math.random() * 10) + 5;
      for (let i = 0; i < numActivities; i++) {
        const randomActivity = activities[Math.floor(Math.random() * activities.length)];
        const daysAgo = Math.floor(Math.random() * 7);
        const completedAt = new Date();
        completedAt.setDate(completedAt.getDate() - daysAgo);
        
        sampleUserActivities.push({
          user: user._id,
          activity: randomActivity._id,
          pointsEarned: randomActivity.points,
          completedAt
        });
      }
    }
    
    await UserActivity.create(sampleUserActivities);
    console.log(`📝 Created ${sampleUserActivities.length} user activities`);

    console.log('\n✅ Database seeded successfully!');
    console.log('\n📋 Test accounts:');
    console.log('   Admin: admin / admin123');
    console.log('   User:  user / user123');
    
    process.exit(0);
  } catch (error) {
    console.error('❌ Error seeding database:', error);
    process.exit(1);
  }
};

seedData();
