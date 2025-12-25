/**
 * Script tạo tài khoản Admin
 * Chạy: node src/scripts/createAdmin.js
 */
require('dotenv').config();
const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const ADMIN_USERNAME = 'admin';
const ADMIN_EMAIL = 'admin@ecotrack.app';
const ADMIN_PASSWORD = 'admin123';
const ADMIN_FULLNAME = 'Administrator';

async function createAdmin() {
  try {
    console.log('🔌 Connecting to MongoDB...');
    await mongoose.connect(process.env.MONGODB_URI);
    console.log('✅ Connected to MongoDB');

    const User = require('../models/User');

    // Check if admin exists
    const existingAdmin = await User.findOne({ 
      $or: [{ username: ADMIN_USERNAME }, { email: ADMIN_EMAIL }] 
    });

    if (existingAdmin) {
      console.log('⚠️  Admin account already exists:');
      console.log(`   Username: ${existingAdmin.username}`);
      console.log(`   Email: ${existingAdmin.email}`);
      console.log(`   Role: ${existingAdmin.role}`);
      
      // Update to admin role if not already
      if (existingAdmin.role !== 'admin') {
        existingAdmin.role = 'admin';
        await existingAdmin.save();
        console.log('✅ Updated role to admin');
      }
    } else {
      // Create new admin
      const admin = new User({
        username: ADMIN_USERNAME,
        email: ADMIN_EMAIL,
        password: ADMIN_PASSWORD,
        fullname: ADMIN_FULLNAME,
        role: 'admin',
        points: 0,
        level: 1
      });

      await admin.save();
      console.log('✅ Admin account created successfully!');
    }

    console.log('\n📋 Admin Credentials:');
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    console.log(`   Username: ${ADMIN_USERNAME}`);
    console.log(`   Password: ${ADMIN_PASSWORD}`);
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    console.log('\n🔗 Admin URL: https://ecotrack-backend-production.up.railway.app/admin');

  } catch (error) {
    console.error('❌ Error:', error.message);
  } finally {
    await mongoose.disconnect();
    console.log('\n👋 Disconnected from MongoDB');
    process.exit(0);
  }
}

createAdmin();
