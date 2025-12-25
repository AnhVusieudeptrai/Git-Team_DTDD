require('dotenv').config();
const mongoose = require('mongoose');
const Badge = require('../models/Badge');

const badges = [
  // Streak badges
  { name: 'Người mới bắt đầu', description: 'Duy trì streak 3 ngày liên tiếp', icon: '🌱', type: 'streak', requirement: 3, rarity: 'common' },
  { name: 'Kiên trì', description: 'Duy trì streak 7 ngày liên tiếp', icon: '🔥', type: 'streak', requirement: 7, rarity: 'common' },
  { name: 'Chiến binh xanh', description: 'Duy trì streak 14 ngày liên tiếp', icon: '⚡', type: 'streak', requirement: 14, rarity: 'rare' },
  { name: 'Huyền thoại', description: 'Duy trì streak 30 ngày liên tiếp', icon: '👑', type: 'streak', requirement: 30, rarity: 'epic' },
  { name: 'Bất khả chiến bại', description: 'Duy trì streak 100 ngày liên tiếp', icon: '💎', type: 'streak', requirement: 100, rarity: 'legendary' },

  // Points badges
  { name: 'Tích lũy 100', description: 'Đạt 100 điểm tổng cộng', icon: '🎯', type: 'points', requirement: 100, rarity: 'common' },
  { name: 'Tích lũy 500', description: 'Đạt 500 điểm tổng cộng', icon: '🏅', type: 'points', requirement: 500, rarity: 'common' },
  { name: 'Tích lũy 1000', description: 'Đạt 1000 điểm tổng cộng', icon: '🥈', type: 'points', requirement: 1000, rarity: 'rare' },
  { name: 'Tích lũy 5000', description: 'Đạt 5000 điểm tổng cộng', icon: '🥇', type: 'points', requirement: 5000, rarity: 'epic' },
  { name: 'Triệu phú xanh', description: 'Đạt 10000 điểm tổng cộng', icon: '💰', type: 'points', requirement: 10000, rarity: 'legendary' },

  // Activities badges
  { name: 'Khởi động', description: 'Hoàn thành 10 hoạt động', icon: '🚀', type: 'activities', requirement: 10, rarity: 'common' },
  { name: 'Năng động', description: 'Hoàn thành 50 hoạt động', icon: '💪', type: 'activities', requirement: 50, rarity: 'common' },
  { name: 'Siêu năng động', description: 'Hoàn thành 100 hoạt động', icon: '🌟', type: 'activities', requirement: 100, rarity: 'rare' },
  { name: 'Người hùng môi trường', description: 'Hoàn thành 500 hoạt động', icon: '🦸', type: 'activities', requirement: 500, rarity: 'epic' },
  { name: 'Huyền thoại xanh', description: 'Hoàn thành 1000 hoạt động', icon: '🌍', type: 'activities', requirement: 1000, rarity: 'legendary' },
];

async function seedBadges() {
  try {
    await mongoose.connect(process.env.MONGODB_URI);
    console.log('Connected to MongoDB');

    // Clear existing badges
    await Badge.deleteMany({});
    console.log('Cleared existing badges');

    // Insert new badges
    await Badge.insertMany(badges);
    console.log(`Inserted ${badges.length} badges`);

    console.log('✅ Badge seeding completed!');
    process.exit(0);
  } catch (error) {
    console.error('Error seeding badges:', error);
    process.exit(1);
  }
}

seedBadges();
