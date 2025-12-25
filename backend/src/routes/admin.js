const express = require('express');
const User = require('../models/User');
const Activity = require('../models/Activity');
const UserActivity = require('../models/UserActivity');
const Badge = require('../models/Badge');
const UserBadge = require('../models/UserBadge');
const Challenge = require('../models/Challenge');
const UserChallenge = require('../models/UserChallenge');
const { auth, adminOnly } = require('../middleware/auth');

const router = express.Router();

// ==================== DASHBOARD STATS ====================

// Get admin statistics
router.get('/stats', auth, adminOnly, async (req, res) => {
  try {
    const totalUsers = await User.countDocuments({ role: 'user' });
    const totalActivities = await Activity.countDocuments();
    const totalCompleted = await UserActivity.countDocuments();
    const totalBadges = await Badge.countDocuments();
    const totalChallenges = await Challenge.countDocuments();
    const activeChallenges = await Challenge.countDocuments({ 
      isActive: true, 
      endDate: { $gte: new Date() } 
    });
    
    const pointsAgg = await User.aggregate([
      { $match: { role: 'user' } },
      { $group: { _id: null, total: { $sum: '$points' } } }
    ]);
    const totalPoints = pointsAgg.length > 0 ? pointsAgg[0].total : 0;

    // Recent registrations (last 7 days)
    const weekAgo = new Date();
    weekAgo.setDate(weekAgo.getDate() - 7);
    const newUsersThisWeek = await User.countDocuments({ 
      role: 'user', 
      createdAt: { $gte: weekAgo } 
    });

    // Activities completed today
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const activitiesToday = await UserActivity.countDocuments({
      completedAt: { $gte: today }
    });

    res.json({
      totalUsers,
      totalActivities,
      totalCompleted,
      totalPoints,
      totalBadges,
      totalChallenges,
      activeChallenges,
      newUsersThisWeek,
      activitiesToday
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Get chart data
router.get('/stats/charts', auth, adminOnly, async (req, res) => {
  try {
    // Activities per day (last 7 days)
    const days = [];
    for (let i = 6; i >= 0; i--) {
      const date = new Date();
      date.setDate(date.getDate() - i);
      date.setHours(0, 0, 0, 0);
      const nextDate = new Date(date);
      nextDate.setDate(nextDate.getDate() + 1);
      
      const count = await UserActivity.countDocuments({
        completedAt: { $gte: date, $lt: nextDate }
      });
      
      days.push({
        date: date.toISOString().split('T')[0],
        count
      });
    }

    // Users by level
    const levelStats = await User.aggregate([
      { $match: { role: 'user' } },
      { $group: { _id: '$level', count: { $sum: 1 } } },
      { $sort: { _id: 1 } }
    ]);

    // Activities by category
    const categoryStats = await UserActivity.aggregate([
      {
        $lookup: {
          from: 'activities',
          localField: 'activity',
          foreignField: '_id',
          as: 'activityData'
        }
      },
      { $unwind: '$activityData' },
      { $group: { _id: '$activityData.category', count: { $sum: 1 } } }
    ]);

    res.json({
      activitiesPerDay: days,
      usersByLevel: levelStats,
      activitiesByCategory: categoryStats
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// ==================== USER MANAGEMENT ====================

// Get all users
router.get('/users', auth, adminOnly, async (req, res) => {
  try {
    const { page = 1, limit = 20, search = '', role = '' } = req.query;
    
    const query = {};
    if (search) {
      query.$or = [
        { username: { $regex: search, $options: 'i' } },
        { email: { $regex: search, $options: 'i' } },
        { fullname: { $regex: search, $options: 'i' } }
      ];
    }
    if (role) {
      query.role = role;
    }

    const users = await User.find(query)
      .select('-password -resetPasswordToken -resetPasswordExpires')
      .sort({ createdAt: -1 })
      .skip((page - 1) * limit)
      .limit(parseInt(limit));

    const total = await User.countDocuments(query);

    res.json({
      users,
      pagination: {
        page: parseInt(page),
        limit: parseInt(limit),
        total,
        pages: Math.ceil(total / limit)
      }
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Get single user
router.get('/users/:id', auth, adminOnly, async (req, res) => {
  try {
    const user = await User.findById(req.params.id)
      .select('-password -resetPasswordToken -resetPasswordExpires');
    
    if (!user) {
      return res.status(404).json({ error: 'Không tìm thấy người dùng' });
    }

    // Get user stats
    const activitiesCount = await UserActivity.countDocuments({ user: user._id });
    const badgesCount = await UserBadge.countDocuments({ user: user._id });
    const challengesJoined = await UserChallenge.countDocuments({ user: user._id });

    res.json({ 
      user,
      stats: {
        activitiesCount,
        badgesCount,
        challengesJoined
      }
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Update user
router.put('/users/:id', auth, adminOnly, async (req, res) => {
  try {
    const { fullname, email, role, points, level, isLocked } = req.body;
    
    const user = await User.findById(req.params.id);
    if (!user) {
      return res.status(404).json({ error: 'Không tìm thấy người dùng' });
    }

    if (fullname) user.fullname = fullname;
    if (email) user.email = email;
    if (role) user.role = role;
    if (points !== undefined) user.points = points;
    if (level !== undefined) user.level = level;
    if (isLocked !== undefined) user.isLocked = isLocked;

    await user.save();

    res.json({ message: 'Cập nhật thành công', user });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Delete user
router.delete('/users/:id', auth, adminOnly, async (req, res) => {
  try {
    const user = await User.findById(req.params.id);
    if (!user) {
      return res.status(404).json({ error: 'Không tìm thấy người dùng' });
    }

    // Don't allow deleting admin
    if (user.role === 'admin') {
      return res.status(403).json({ error: 'Không thể xóa tài khoản admin' });
    }

    // Delete related data
    await UserActivity.deleteMany({ user: user._id });
    await UserBadge.deleteMany({ user: user._id });
    await UserChallenge.deleteMany({ user: user._id });
    await User.findByIdAndDelete(req.params.id);

    res.json({ message: 'Đã xóa người dùng' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Lock/Unlock user
router.post('/users/:id/toggle-lock', auth, adminOnly, async (req, res) => {
  try {
    const user = await User.findById(req.params.id);
    if (!user) {
      return res.status(404).json({ error: 'Không tìm thấy người dùng' });
    }

    if (user.role === 'admin') {
      return res.status(403).json({ error: 'Không thể khóa tài khoản admin' });
    }

    user.isLocked = !user.isLocked;
    await user.save();

    res.json({ 
      message: user.isLocked ? 'Đã khóa tài khoản' : 'Đã mở khóa tài khoản',
      isLocked: user.isLocked
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// ==================== ACTIVITY MANAGEMENT ====================

// Get all activities (admin)
router.get('/activities', auth, adminOnly, async (req, res) => {
  try {
    const activities = await Activity.find().sort({ category: 1, name: 1 });
    res.json({ activities });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Create activity
router.post('/activities', auth, adminOnly, async (req, res) => {
  try {
    const { name, description, points, category, icon, isActive } = req.body;
    
    const activity = new Activity({ 
      name, 
      description, 
      points: parseInt(points), 
      category, 
      icon: icon || '🌱',
      isActive: isActive !== false
    });
    await activity.save();

    res.status(201).json({ message: 'Hoạt động đã được tạo', activity });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Update activity
router.put('/activities/:id', auth, adminOnly, async (req, res) => {
  try {
    const { name, description, points, category, icon, isActive } = req.body;
    
    const activity = await Activity.findById(req.params.id);
    if (!activity) {
      return res.status(404).json({ error: 'Không tìm thấy hoạt động' });
    }

    if (name) activity.name = name;
    if (description !== undefined) activity.description = description;
    if (points !== undefined) activity.points = parseInt(points);
    if (category) activity.category = category;
    if (icon) activity.icon = icon;
    if (isActive !== undefined) activity.isActive = isActive;

    await activity.save();

    res.json({ message: 'Cập nhật thành công', activity });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Delete activity
router.delete('/activities/:id', auth, adminOnly, async (req, res) => {
  try {
    const activity = await Activity.findByIdAndDelete(req.params.id);
    
    if (!activity) {
      return res.status(404).json({ error: 'Không tìm thấy hoạt động' });
    }

    await UserActivity.deleteMany({ activity: req.params.id });

    res.json({ message: 'Hoạt động đã được xóa' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// ==================== BADGE MANAGEMENT ====================

// Get all badges
router.get('/badges', auth, adminOnly, async (req, res) => {
  try {
    const badges = await Badge.find().sort({ type: 1, requirement: 1 });
    res.json({ badges });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Create badge
router.post('/badges', auth, adminOnly, async (req, res) => {
  try {
    const { name, description, icon, type, requirement, rarity, isActive } = req.body;
    
    const badge = new Badge({
      name,
      description,
      icon: icon || '🏆',
      type,
      requirement: parseInt(requirement),
      rarity: rarity || 'common',
      isActive: isActive !== false
    });
    await badge.save();

    res.status(201).json({ message: 'Huy hiệu đã được tạo', badge });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Update badge
router.put('/badges/:id', auth, adminOnly, async (req, res) => {
  try {
    const { name, description, icon, type, requirement, rarity, isActive } = req.body;
    
    const badge = await Badge.findById(req.params.id);
    if (!badge) {
      return res.status(404).json({ error: 'Không tìm thấy huy hiệu' });
    }

    if (name) badge.name = name;
    if (description) badge.description = description;
    if (icon) badge.icon = icon;
    if (type) badge.type = type;
    if (requirement !== undefined) badge.requirement = parseInt(requirement);
    if (rarity) badge.rarity = rarity;
    if (isActive !== undefined) badge.isActive = isActive;

    await badge.save();

    res.json({ message: 'Cập nhật thành công', badge });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Delete badge
router.delete('/badges/:id', auth, adminOnly, async (req, res) => {
  try {
    const badge = await Badge.findByIdAndDelete(req.params.id);
    
    if (!badge) {
      return res.status(404).json({ error: 'Không tìm thấy huy hiệu' });
    }

    await UserBadge.deleteMany({ badge: req.params.id });

    res.json({ message: 'Huy hiệu đã được xóa' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// ==================== CHALLENGE MANAGEMENT ====================

// Get all challenges
router.get('/challenges', auth, adminOnly, async (req, res) => {
  try {
    const challenges = await Challenge.find()
      .populate('rewardBadge')
      .sort({ startDate: -1 });
    res.json({ challenges });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Create challenge
router.post('/challenges', auth, adminOnly, async (req, res) => {
  try {
    const { 
      name, description, type, targetType, targetValue, 
      targetCategory, rewardPoints, rewardBadge, startDate, endDate, isActive 
    } = req.body;
    
    const challenge = new Challenge({
      name,
      description,
      type,
      targetType,
      targetValue: parseInt(targetValue),
      targetCategory: targetCategory || null,
      rewardPoints: parseInt(rewardPoints),
      rewardBadge: rewardBadge || null,
      startDate: new Date(startDate),
      endDate: new Date(endDate),
      isActive: isActive !== false
    });
    await challenge.save();

    res.status(201).json({ message: 'Thử thách đã được tạo', challenge });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Update challenge
router.put('/challenges/:id', auth, adminOnly, async (req, res) => {
  try {
    const { 
      name, description, type, targetType, targetValue, 
      targetCategory, rewardPoints, rewardBadge, startDate, endDate, isActive 
    } = req.body;
    
    const challenge = await Challenge.findById(req.params.id);
    if (!challenge) {
      return res.status(404).json({ error: 'Không tìm thấy thử thách' });
    }

    if (name) challenge.name = name;
    if (description) challenge.description = description;
    if (type) challenge.type = type;
    if (targetType) challenge.targetType = targetType;
    if (targetValue !== undefined) challenge.targetValue = parseInt(targetValue);
    if (targetCategory !== undefined) challenge.targetCategory = targetCategory || null;
    if (rewardPoints !== undefined) challenge.rewardPoints = parseInt(rewardPoints);
    if (rewardBadge !== undefined) challenge.rewardBadge = rewardBadge || null;
    if (startDate) challenge.startDate = new Date(startDate);
    if (endDate) challenge.endDate = new Date(endDate);
    if (isActive !== undefined) challenge.isActive = isActive;

    await challenge.save();

    res.json({ message: 'Cập nhật thành công', challenge });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Delete challenge
router.delete('/challenges/:id', auth, adminOnly, async (req, res) => {
  try {
    const challenge = await Challenge.findByIdAndDelete(req.params.id);
    
    if (!challenge) {
      return res.status(404).json({ error: 'Không tìm thấy thử thách' });
    }

    await UserChallenge.deleteMany({ challenge: req.params.id });

    res.json({ message: 'Thử thách đã được xóa' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// ==================== PUSH NOTIFICATIONS ====================

// Send push notification to all users
router.post('/notifications/send', auth, adminOnly, async (req, res) => {
  try {
    const { title, body, targetUsers } = req.body;
    
    let users;
    if (targetUsers && targetUsers.length > 0) {
      users = await User.find({ 
        _id: { $in: targetUsers },
        fcmToken: { $exists: true, $ne: '' }
      });
    } else {
      users = await User.find({ 
        fcmToken: { $exists: true, $ne: '' }
      });
    }

    // Try to load firebase-admin
    let admin;
    try {
      admin = require('firebase-admin');
      if (!admin.apps.length) {
        const serviceAccount = require('../../firebase-service-account.json');
        admin.initializeApp({
          credential: admin.credential.cert(serviceAccount)
        });
      }
    } catch (e) {
      return res.status(501).json({ 
        error: 'Firebase Admin SDK not configured',
        message: 'Push notifications require Firebase Admin SDK setup'
      });
    }

    const tokens = users.map(u => u.fcmToken).filter(t => t);
    
    if (tokens.length === 0) {
      return res.status(400).json({ error: 'Không có người dùng nào có FCM token' });
    }

    const message = {
      notification: { title, body },
      tokens
    };

    const response = await admin.messaging().sendEachForMulticast(message);
    
    res.json({ 
      message: 'Đã gửi thông báo',
      success: response.successCount,
      failure: response.failureCount
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
