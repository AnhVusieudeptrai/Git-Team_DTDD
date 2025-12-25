const express = require('express');
const Activity = require('../models/Activity');
const UserActivity = require('../models/UserActivity');
const { auth, adminOnly } = require('../middleware/auth');
const { updateStreakOnActivity } = require('../services/streakService');
const { checkAndAwardBadges } = require('../services/badgeService');
const { updateChallengeProgress } = require('../services/challengeService');

const router = express.Router();

// Get all activities
router.get('/', auth, async (req, res) => {
  try {
    const activities = await Activity.find({ isActive: true }).sort({ category: 1, name: 1 });
    
    // Get today's completed activities for this user
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    const completedToday = await UserActivity.find({
      user: req.user._id,
      completedAt: { $gte: today }
    }).select('activity');
    
    const completedIds = completedToday.map(ua => ua.activity.toString());
    
    const activitiesWithStatus = activities.map(activity => ({
      id: activity._id,
      name: activity.name,
      description: activity.description,
      points: activity.points,
      category: activity.category,
      icon: activity.icon,
      completedToday: completedIds.includes(activity._id.toString())
    }));

    res.json({ activities: activitiesWithStatus });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Complete an activity
router.post('/:id/complete', auth, async (req, res) => {
  try {
    const activity = await Activity.findById(req.params.id);
    
    if (!activity) {
      return res.status(404).json({ error: 'Activity not found' });
    }

    // Create user activity record
    const userActivity = new UserActivity({
      user: req.user._id,
      activity: activity._id,
      pointsEarned: activity.points
    });
    await userActivity.save();

    // Update user points
    await req.user.addPoints(activity.points);

    // Update streak
    const streakResult = await updateStreakOnActivity(req.user);

    // Check and award badges
    const newBadges = await checkAndAwardBadges(req.user);

    // Update challenge progress
    const completedChallenges = await updateChallengeProgress(req.user, activity, activity.points);

    res.json({
      message: 'Activity completed!',
      pointsEarned: activity.points,
      totalPoints: req.user.points,
      level: req.user.level,
      streak: streakResult,
      newBadges,
      completedChallenges
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Get user's activity history
router.get('/history', auth, async (req, res) => {
  try {
    const { page = 1, limit = 20 } = req.query;
    
    const history = await UserActivity.find({ user: req.user._id })
      .populate('activity', 'name description category points icon')
      .sort({ completedAt: -1 })
      .skip((page - 1) * limit)
      .limit(parseInt(limit));

    const total = await UserActivity.countDocuments({ user: req.user._id });

    res.json({
      activities: history.map(h => ({
        id: h._id,
        activity: h.activity,
        pointsEarned: h.pointsEarned,
        completedAt: h.completedAt
      })),
      total,
      page: parseInt(page),
      limit: parseInt(limit)
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Get today's activities
router.get('/today', auth, async (req, res) => {
  try {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    // Get today's activities
    const todayActivities = await UserActivity.find({
      user: req.user._id,
      completedAt: { $gte: today }
    }).populate('activity', 'name points category');

    const todayPoints = todayActivities.reduce((sum, ua) => sum + ua.pointsEarned, 0);
    const todayCount = todayActivities.length;

    // Get week's points
    const weekAgo = new Date();
    weekAgo.setDate(weekAgo.getDate() - 7);
    weekAgo.setHours(0, 0, 0, 0);

    const weekActivities = await UserActivity.find({
      user: req.user._id,
      completedAt: { $gte: weekAgo }
    });

    const weekPoints = weekActivities.reduce((sum, ua) => sum + ua.pointsEarned, 0);

    res.json({
      activities: todayActivities,
      count: todayCount,
      totalPoints: todayPoints,
      todayPoints,
      todayCount,
      weekPoints
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Admin: Create activity
router.post('/', auth, adminOnly, async (req, res) => {
  try {
    const { name, description, points, category, icon } = req.body;
    
    const activity = new Activity({ name, description, points, category, icon });
    await activity.save();

    res.status(201).json({ message: 'Activity created', activity });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Admin: Update activity
router.put('/:id', auth, adminOnly, async (req, res) => {
  try {
    const { name, description, points, category, icon, isActive } = req.body;
    
    const activity = await Activity.findByIdAndUpdate(
      req.params.id,
      { name, description, points, category, icon, isActive },
      { new: true }
    );

    if (!activity) {
      return res.status(404).json({ error: 'Activity not found' });
    }

    res.json({ message: 'Activity updated', activity });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Admin: Delete activity
router.delete('/:id', auth, adminOnly, async (req, res) => {
  try {
    const activity = await Activity.findByIdAndDelete(req.params.id);
    
    if (!activity) {
      return res.status(404).json({ error: 'Activity not found' });
    }

    res.json({ message: 'Activity deleted' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
