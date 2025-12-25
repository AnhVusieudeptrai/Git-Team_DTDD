const Badge = require('../models/Badge');
const UserBadge = require('../models/UserBadge');
const UserActivity = require('../models/UserActivity');
const Streak = require('../models/Streak');
const { notifications } = require('./notificationService');

// Check and award badges for a user
const checkAndAwardBadges = async (user) => {
  const earnedBadges = [];
  
  try {
    // Get all active badges
    const badges = await Badge.find({ isActive: true });
    
    // Get user's already earned badges
    const userBadges = await UserBadge.find({ user: user._id });
    const earnedBadgeIds = userBadges.map(ub => ub.badge.toString());

    // Get user stats
    const totalActivities = await UserActivity.countDocuments({ user: user._id });
    const streak = await Streak.findOne({ user: user._id });

    for (const badge of badges) {
      // Skip if already earned
      if (earnedBadgeIds.includes(badge._id.toString())) continue;

      let qualified = false;

      switch (badge.type) {
        case 'points':
          qualified = user.points >= badge.requirement;
          break;
          
        case 'activities':
          qualified = totalActivities >= badge.requirement;
          break;
          
        case 'streak':
          qualified = streak && streak.currentStreak >= badge.requirement;
          break;
      }

      if (qualified) {
        // Award badge
        const userBadge = new UserBadge({
          user: user._id,
          badge: badge._id
        });
        await userBadge.save();
        
        earnedBadges.push({
          id: badge._id,
          name: badge.name,
          icon: badge.icon,
          rarity: badge.rarity
        });

        // Send notification
        if (user.fcmToken) {
          await notifications.badgeEarned(user.fcmToken, badge.name, badge.icon);
        }
      }
    }

    return earnedBadges;
  } catch (error) {
    console.error('Badge check error:', error.message);
    return earnedBadges;
  }
};

// Get badge progress for a user
const getBadgeProgress = async (user) => {
  try {
    const badges = await Badge.find({ isActive: true });
    const userBadges = await UserBadge.find({ user: user._id });
    const earnedBadgeIds = userBadges.map(ub => ub.badge.toString());

    const totalActivities = await UserActivity.countDocuments({ user: user._id });
    const streak = await Streak.findOne({ user: user._id });

    return badges.map(badge => {
      let current = 0;
      
      switch (badge.type) {
        case 'points':
          current = user.points;
          break;
        case 'activities':
          current = totalActivities;
          break;
        case 'streak':
          current = streak?.currentStreak || 0;
          break;
      }

      return {
        id: badge._id,
        name: badge.name,
        icon: badge.icon,
        type: badge.type,
        rarity: badge.rarity,
        requirement: badge.requirement,
        current,
        progress: Math.min(100, Math.round((current / badge.requirement) * 100)),
        earned: earnedBadgeIds.includes(badge._id.toString())
      };
    });
  } catch (error) {
    console.error('Badge progress error:', error.message);
    return [];
  }
};

module.exports = {
  checkAndAwardBadges,
  getBadgeProgress
};
