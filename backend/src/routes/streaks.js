const express = require('express');
const Streak = require('../models/Streak');
const { auth } = require('../middleware/auth');

const router = express.Router();

// Get user's streak info
router.get('/', auth, async (req, res) => {
  try {
    let streak = await Streak.findOne({ user: req.user._id });
    
    if (!streak) {
      streak = new Streak({ user: req.user._id });
      await streak.save();
    }

    // Check if streak is still valid (not broken)
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    let isActive = false;
    let daysUntilLost = 0;

    if (streak.lastActivityDate) {
      const lastDate = new Date(streak.lastActivityDate);
      lastDate.setHours(0, 0, 0, 0);
      
      const diffDays = Math.floor((today - lastDate) / (1000 * 60 * 60 * 24));
      
      if (diffDays <= 1) {
        isActive = true;
        daysUntilLost = diffDays === 0 ? 1 : 0;
      } else {
        // Streak is broken but not updated yet
        streak.currentStreak = 0;
        streak.streakStartDate = null;
        await streak.save();
      }
    }

    res.json({
      currentStreak: streak.currentStreak,
      longestStreak: streak.longestStreak,
      lastActivityDate: streak.lastActivityDate,
      streakStartDate: streak.streakStartDate,
      isActive,
      daysUntilLost,
      message: isActive 
        ? (daysUntilLost === 0 ? 'Hoàn thành hoạt động hôm nay để duy trì streak!' : 'Streak đang hoạt động!')
        : 'Bắt đầu streak mới bằng cách hoàn thành hoạt động!'
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Get streak leaderboard
router.get('/leaderboard', auth, async (req, res) => {
  try {
    const { limit = 10 } = req.query;

    const streaks = await Streak.find({ currentStreak: { $gt: 0 } })
      .populate('user', 'username fullname avatar')
      .sort({ currentStreak: -1 })
      .limit(parseInt(limit));

    const leaderboard = streaks.map((s, index) => ({
      rank: index + 1,
      userId: s.user._id,
      username: s.user.username,
      fullname: s.user.fullname,
      avatar: s.user.avatar,
      currentStreak: s.currentStreak,
      longestStreak: s.longestStreak,
      isCurrentUser: s.user._id.toString() === req.user._id.toString()
    }));

    // Get current user's streak rank
    const userStreak = await Streak.findOne({ user: req.user._id });
    let userRank = null;
    
    if (userStreak && userStreak.currentStreak > 0) {
      userRank = await Streak.countDocuments({
        currentStreak: { $gt: userStreak.currentStreak }
      }) + 1;
    }

    res.json({
      leaderboard,
      currentUser: {
        rank: userRank,
        currentStreak: userStreak?.currentStreak || 0,
        longestStreak: userStreak?.longestStreak || 0
      }
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
