const express = require('express');
const User = require('../models/User');
const Activity = require('../models/Activity');
const UserActivity = require('../models/UserActivity');
const { auth, adminOnly } = require('../middleware/auth');

const router = express.Router();

// Get admin statistics
router.get('/stats', auth, adminOnly, async (req, res) => {
  try {
    const totalUsers = await User.countDocuments({ role: 'user' });
    const totalActivities = await Activity.countDocuments();
    const totalCompleted = await UserActivity.countDocuments();
    
    const pointsAgg = await User.aggregate([
      { $match: { role: 'user' } },
      { $group: { _id: null, total: { $sum: '$points' } } }
    ]);
    const totalPoints = pointsAgg.length > 0 ? pointsAgg[0].total : 0;

    res.json({
      totalUsers,
      totalActivities,
      totalCompleted,
      totalPoints
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

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
    const { name, description, points, category, icon } = req.body;
    
    const activity = new Activity({ 
      name, 
      description, 
      points: parseInt(points), 
      category, 
      icon: icon || '🌱' 
    });
    await activity.save();

    res.status(201).json({ message: 'Hoạt động đã được tạo', activity });
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

    // Also delete related user activities
    await UserActivity.deleteMany({ activity: req.params.id });

    res.json({ message: 'Hoạt động đã được xóa' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
