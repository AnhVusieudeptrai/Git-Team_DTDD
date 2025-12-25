const Streak = require('../models/Streak');
const User = require('../models/User');
const { notifications } = require('./notificationService');

// Update streak when activity is completed
const updateStreakOnActivity = async (user) => {
  try {
    let streak = await Streak.findOne({ user: user._id });
    
    if (!streak) {
      streak = new Streak({ user: user._id });
    }

    const result = streak.updateStreak(new Date());
    await streak.save();

    // Send notification for new record
    if (result.isNewRecord && user.fcmToken) {
      await notifications.newStreakRecord(user.fcmToken, streak.currentStreak);
    }

    return {
      currentStreak: streak.currentStreak,
      longestStreak: streak.longestStreak,
      ...result
    };
  } catch (error) {
    console.error('Streak update error:', error.message);
    return null;
  }
};

// Check streaks at risk and send notifications
const checkStreaksAtRisk = async () => {
  try {
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    yesterday.setHours(0, 0, 0, 0);

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    // Find users who had activity yesterday but not today (streak at risk)
    const streaksAtRisk = await Streak.find({
      currentStreak: { $gt: 0 },
      lastActivityDate: { $gte: yesterday, $lt: today }
    }).populate('user', 'fcmToken');

    for (const streak of streaksAtRisk) {
      if (streak.user.fcmToken) {
        await notifications.streakAtRisk(streak.user.fcmToken, streak.currentStreak);
      }
    }

    console.log(`Sent streak at risk notifications to ${streaksAtRisk.length} users`);
    return streaksAtRisk.length;
  } catch (error) {
    console.error('Check streaks at risk error:', error.message);
    return 0;
  }
};

// Check and break expired streaks
const checkExpiredStreaks = async () => {
  try {
    const twoDaysAgo = new Date();
    twoDaysAgo.setDate(twoDaysAgo.getDate() - 2);
    twoDaysAgo.setHours(23, 59, 59, 999);

    // Find streaks that should be broken
    const expiredStreaks = await Streak.find({
      currentStreak: { $gt: 0 },
      lastActivityDate: { $lt: twoDaysAgo }
    }).populate('user', 'fcmToken');

    for (const streak of expiredStreaks) {
      const lostStreak = streak.currentStreak;
      
      streak.currentStreak = 0;
      streak.streakStartDate = null;
      await streak.save();

      // Send notification
      if (streak.user.fcmToken) {
        await notifications.streakBroken(streak.user.fcmToken, lostStreak);
      }
    }

    console.log(`Broke ${expiredStreaks.length} expired streaks`);
    return expiredStreaks.length;
  } catch (error) {
    console.error('Check expired streaks error:', error.message);
    return 0;
  }
};

module.exports = {
  updateStreakOnActivity,
  checkStreaksAtRisk,
  checkExpiredStreaks
};
