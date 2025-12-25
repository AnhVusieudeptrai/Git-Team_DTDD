const express = require('express');
const User = require('../models/User');
const UserActivity = require('../models/UserActivity');
const { auth } = require('../middleware/auth');

const router = express.Router();

// Get global leaderboard
router.get('/', auth, async (req, res) => {
  try {
    const { limit = 10 } = req.query;

    const leaderboard = await User.find({ role: 'user' })
      .select('username fullname points level avatar')
      .sort({ points: -1 })
      .limit(parseInt(limit));

    // Get activity counts for each user
    const leaderboardWithActivities = await Promise.all(
      leaderboard.map(async (user, index) => {
        const totalActivities = await UserActivity.countDocuments({ user: user._id });
        return {
          rank: index + 1,
          id: user._id,
          username: user.username,
          fullname: user.fullname,
          points: user.points,
          level: user.level,
          avatar: user.avatar,
          totalActivities,
          isCurrentUser: user._id.toString() === req.user._id.toString()
        };
      })
    );

    // Get current user's rank
    const userRank = await User.countDocuments({ 
      role: 'user',
      points: { $gt: req.user.points } 
    }) + 1;

    res.json({
      leaderboard: leaderboardWithActivities,
      currentUser: {
        rank: userRank,
        points: req.user.points,
        level: req.user.level
      }
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Get weekly leaderboard
router.get('/weekly', auth, async (req, res) => {
  try {
    const weekAgo = new Date();
    weekAgo.setDate(weekAgo.getDate() - 7);

    const weeklyStats = await UserActivity.aggregate([
      { $match: { completedAt: { $gte: weekAgo } } },
      { $group: { _id: '$user', weeklyPoints: { $sum: '$pointsEarned' } } },
      { $sort: { weeklyPoints: -1 } },
      { $limit: 10 },
      { $lookup: { from: 'users', localField: '_id', foreignField: '_id', as: 'userData' } },
      { $unwind: '$userData' }
    ]);

    const leaderboard = weeklyStats.map((item, index) => ({
      rank: index + 1,
      id: item._id,
      username: item.userData.username,
      fullname: item.userData.fullname,
      weeklyPoints: item.weeklyPoints,
      level: item.userData.level,
      avatar: item.userData.avatar,
      isCurrentUser: item._id.toString() === req.user._id.toString()
    }));

    // Get current user's weekly points
    const userWeeklyActivities = await UserActivity.find({
      user: req.user._id,
      completedAt: { $gte: weekAgo }
    });
    const userWeeklyPoints = userWeeklyActivities.reduce((sum, ua) => sum + ua.pointsEarned, 0);

    res.json({
      leaderboard,
      currentUser: {
        weeklyPoints: userWeeklyPoints
      }
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
