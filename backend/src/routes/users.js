const express = require('express');
const User = require('../models/User');
const UserActivity = require('../models/UserActivity');
const { auth, adminOnly } = require('../middleware/auth');

const router = express.Router();

// Get user profile
router.get('/profile', auth, async (req, res) => {
  try {
    const totalActivities = await UserActivity.countDocuments({ user: req.user._id });
    
    // Get rank
    const rank = await User.countDocuments({ 
      role: 'user',
      points: { $gt: req.user.points } 
    }) + 1;

    res.json({
      user: {
        id: req.user._id,
        username: req.user.username,
        email: req.user.email,
        fullname: req.user.fullname,
        role: req.user.role,
        points: req.user.points,
        level: req.user.level,
        avatar: req.user.avatar,
        createdAt: req.user.createdAt
      },
      stats: {
        totalActivities,
        rank
      }
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Update user profile
router.put('/profile', auth, async (req, res) => {
  try {
    const { fullname, avatar } = req.body;
    
    if (fullname) req.user.fullname = fullname;
    if (avatar) req.user.avatar = avatar;
    
    await req.user.save();

    res.json({ 
      message: 'Profile updated',
      user: {
        id: req.user._id,
        username: req.user.username,
        email: req.user.email,
        fullname: req.user.fullname,
        avatar: req.user.avatar
      }
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Get user statistics
router.get('/stats', auth, async (req, res) => {
  try {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    const weekAgo = new Date();
    weekAgo.setDate(weekAgo.getDate() - 7);
    weekAgo.setHours(0, 0, 0, 0);

    // Today's stats
    const todayActivities = await UserActivity.find({
      user: req.user._id,
      completedAt: { $gte: today }
    });
    const todayPoints = todayActivities.reduce((sum, ua) => sum + ua.pointsEarned, 0);

    // Week's stats
    const weekActivities = await UserActivity.find({
      user: req.user._id,
      completedAt: { $gte: weekAgo }
    });
    const weekPoints = weekActivities.reduce((sum, ua) => sum + ua.pointsEarned, 0);

    // Total stats
    const totalActivities = await UserActivity.countDocuments({ user: req.user._id });

    // Category breakdown
    const categoryStats = await UserActivity.aggregate([
      { $match: { user: req.user._id } },
      { $lookup: { from: 'activities', localField: 'activity', foreignField: '_id', as: 'activityData' } },
      { $unwind: '$activityData' },
      { $group: { _id: '$activityData.category', count: { $sum: 1 }, points: { $sum: '$pointsEarned' } } }
    ]);

    // Weekly chart data (last 7 days)
    const weeklyData = [];
    for (let i = 6; i >= 0; i--) {
      const date = new Date();
      date.setDate(date.getDate() - i);
      date.setHours(0, 0, 0, 0);
      
      const nextDate = new Date(date);
      nextDate.setDate(nextDate.getDate() + 1);

      const dayActivities = await UserActivity.find({
        user: req.user._id,
        completedAt: { $gte: date, $lt: nextDate }
      });
      
      weeklyData.push({
        date: date.toISOString().split('T')[0],
        points: dayActivities.reduce((sum, ua) => sum + ua.pointsEarned, 0),
        count: dayActivities.length
      });
    }

    res.json({
      today: { points: todayPoints, activities: todayActivities.length },
      week: { points: weekPoints, activities: weekActivities.length },
      total: { points: req.user.points, activities: totalActivities, level: req.user.level },
      categories: categoryStats,
      weeklyChart: weeklyData
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Admin: Get all users
router.get('/', auth, adminOnly, async (req, res) => {
  try {
    const users = await User.find().select('-password').sort({ createdAt: -1 });
    res.json({ users });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Admin: Delete user
router.delete('/:id', auth, adminOnly, async (req, res) => {
  try {
    if (req.params.id === req.user._id.toString()) {
      return res.status(400).json({ error: 'Cannot delete yourself' });
    }

    await UserActivity.deleteMany({ user: req.params.id });
    await User.findByIdAndDelete(req.params.id);

    res.json({ message: 'User deleted' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
