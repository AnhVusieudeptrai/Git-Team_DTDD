/**
 * Database Seeder - Tạo dữ liệu mẫu cho EcoTrack
 * Chạy: node src/scripts/seed.js
 */

require('dotenv').config();
const mongoose = require('mongoose');
const User = require('../models/User');
const Activity = require('../models/Activity');
const UserActivity = require('../models/UserActivity');
const Badge = require('../models/Badge');
const UserBadge = require('../models/UserBadge');
const Challenge = require('../models/Challenge');
const UserChallenge = require('../models/UserChallenge');
const Streak = require('../models/Streak');

const seedData = async () => {
  try {
    await mongoose.connect(process.env.MONGODB_URI);
    console.log('✅ Connected to MongoDB');

    // Clear existing data
    await User.deleteMany({});
    await Activity.deleteMany({});
    await UserActivity.deleteMany({});
    await Badge.deleteMany({});
    await UserBadge.deleteMany({});
    await Challenge.deleteMany({});
    await UserChallenge.deleteMany({});
    await Streak.deleteMany({});
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
        points: 520,
        level: 6
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
      { name: 'Tắt máy tính khi không dùng', description: 'Tắt hoàn toàn máy tính thay vì để chế độ sleep', points: 15, category: 'energy', icon: 'computer' }
    ]);
    console.log(`🎯 Created ${activities.length} activities`);

    // Create badges
    const badges = await Badge.create([
      // Streak badges
      { name: 'Người mới bắt đầu', description: 'Duy trì streak 3 ngày liên tiếp', icon: '🌱', type: 'streak', requirement: 3, rarity: 'common' },
      { name: 'Kiên trì', description: 'Duy trì streak 7 ngày liên tiếp', icon: '🔥', type: 'streak', requirement: 7, rarity: 'common' },
      { name: 'Chiến binh xanh', description: 'Duy trì streak 14 ngày liên tiếp', icon: '⚡', type: 'streak', requirement: 14, rarity: 'rare' },
      { name: 'Huyền thoại', description: 'Duy trì streak 30 ngày liên tiếp', icon: '👑', type: 'streak', requirement: 30, rarity: 'epic' },
      // Points badges
      { name: 'Tích lũy 100', description: 'Đạt 100 điểm tổng cộng', icon: '🎯', type: 'points', requirement: 100, rarity: 'common' },
      { name: 'Tích lũy 500', description: 'Đạt 500 điểm tổng cộng', icon: '🏅', type: 'points', requirement: 500, rarity: 'common' },
      { name: 'Tích lũy 1000', description: 'Đạt 1000 điểm tổng cộng', icon: '🥈', type: 'points', requirement: 1000, rarity: 'rare' },
      // Activities badges
      { name: 'Khởi động', description: 'Hoàn thành 10 hoạt động', icon: '🚀', type: 'activities', requirement: 10, rarity: 'common' },
      { name: 'Năng động', description: 'Hoàn thành 50 hoạt động', icon: '💪', type: 'activities', requirement: 50, rarity: 'common' },
      { name: 'Siêu năng động', description: 'Hoàn thành 100 hoạt động', icon: '🌟', type: 'activities', requirement: 100, rarity: 'rare' },
    ]);
    console.log(`🏆 Created ${badges.length} badges`);

    // Create challenges
    const now = new Date();
    const weekStart = new Date(now);
    weekStart.setDate(now.getDate() - now.getDay() + 1);
    weekStart.setHours(0, 0, 0, 0);
    const weekEnd = new Date(weekStart);
    weekEnd.setDate(weekStart.getDate() + 6);
    weekEnd.setHours(23, 59, 59, 999);

    const monthStart = new Date(now.getFullYear(), now.getMonth(), 1);
    const monthEnd = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59, 999);

    const challenges = await Challenge.create([
      {
        name: 'Thử thách tuần này',
        description: 'Hoàn thành 15 hoạt động xanh trong tuần',
        type: 'weekly',
        targetType: 'activities',
        targetValue: 15,
        rewardPoints: 100,
        startDate: weekStart,
        endDate: weekEnd
      },
      {
        name: 'Thử thách tháng 12',
        description: 'Tích lũy 300 điểm trong tháng này',
        type: 'monthly',
        targetType: 'points',
        targetValue: 300,
        rewardPoints: 200,
        startDate: monthStart,
        endDate: monthEnd
      },
      {
        name: 'Tiết kiệm nước',
        description: 'Hoàn thành 10 hoạt động tiết kiệm nước',
        type: 'weekly',
        targetType: 'activities',
        targetValue: 10,
        targetCategory: 'water',
        rewardPoints: 80,
        startDate: weekStart,
        endDate: weekEnd
      }
    ]);
    console.log(`🎯 Created ${challenges.length} challenges`);

    // Create sample user activities and streaks
    const sampleUserActivities = [];
    const regularUsers = users.filter(u => u.role === 'user');
    
    for (const user of regularUsers) {
      // Create streak for each user
      const streakDays = Math.floor(Math.random() * 10) + 1;
      const lastActivityDate = new Date();
      lastActivityDate.setDate(lastActivityDate.getDate() - (Math.random() > 0.5 ? 0 : 1));
      
      await Streak.create({
        user: user._id,
        currentStreak: streakDays,
        longestStreak: streakDays + Math.floor(Math.random() * 5),
        lastActivityDate,
        streakStartDate: new Date(lastActivityDate.getTime() - (streakDays - 1) * 24 * 60 * 60 * 1000)
      });

      // Create activities for last 7 days
      const numActivities = Math.floor(Math.random() * 15) + 10;
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

      // Award some badges to users with enough points
      if (user.points >= 100) {
        await UserBadge.create({ user: user._id, badge: badges[4]._id }); // Tích lũy 100
      }
      if (user.points >= 500) {
        await UserBadge.create({ user: user._id, badge: badges[5]._id }); // Tích lũy 500
      }
    }
    
    await UserActivity.create(sampleUserActivities);
    console.log(`📝 Created ${sampleUserActivities.length} user activities`);
    console.log(`🔥 Created streaks for ${regularUsers.length} users`);

    console.log('\n✅ Database seeded successfully!');
    console.log('\n📋 Test accounts:');
    console.log('   Admin: admin / admin123');
    console.log('   User:  user / user123');
    console.log('   Khoa:  khoa_zo / khoa123');
    
    process.exit(0);
  } catch (error) {
    console.error('❌ Error seeding database:', error);
    process.exit(1);
  }
};

seedData();
