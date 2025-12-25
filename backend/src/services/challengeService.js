const Challenge = require('../models/Challenge');
const UserChallenge = require('../models/UserChallenge');
const UserBadge = require('../models/UserBadge');
const { notifications } = require('./notificationService');

// Update challenge progress after activity completion
const updateChallengeProgress = async (user, activity, pointsEarned) => {
  const completedChallenges = [];
  
  try {
    // Get user's active challenges
    const userChallenges = await UserChallenge.find({
      user: user._id,
      isCompleted: false
    }).populate('challenge');

    const now = new Date();

    for (const uc of userChallenges) {
      const challenge = uc.challenge;
      
      // Skip expired challenges
      if (now > challenge.endDate) continue;

      let progressIncrement = 0;

      switch (challenge.targetType) {
        case 'points':
          progressIncrement = pointsEarned;
          break;
          
        case 'activities':
          if (!challenge.targetCategory || challenge.targetCategory === activity.category) {
            progressIncrement = 1;
          }
          break;
      }

      if (progressIncrement > 0) {
        uc.progress += progressIncrement;

        // Check if completed
        if (uc.progress >= challenge.targetValue) {
          uc.isCompleted = true;
          uc.completedAt = new Date();

          // Award reward points
          user.points += challenge.rewardPoints;
          user.calculateLevel();
          await user.save();

          // Award reward badge if any
          if (challenge.rewardBadge) {
            const existingBadge = await UserBadge.findOne({
              user: user._id,
              badge: challenge.rewardBadge
            });

            if (!existingBadge) {
              await UserBadge.create({
                user: user._id,
                badge: challenge.rewardBadge
              });
            }
          }

          completedChallenges.push({
            id: challenge._id,
            name: challenge.name,
            rewardPoints: challenge.rewardPoints
          });

          // Send notification
          if (user.fcmToken) {
            await notifications.challengeCompleted(
              user.fcmToken,
              challenge.name,
              challenge.rewardPoints
            );
          }
        }

        await uc.save();
      }
    }

    return completedChallenges;
  } catch (error) {
    console.error('Challenge update error:', error.message);
    return completedChallenges;
  }
};

// Auto-create weekly/monthly challenges
const createRecurringChallenges = async () => {
  try {
    const now = new Date();
    
    // Check for weekly challenge
    const weekStart = new Date(now);
    weekStart.setDate(now.getDate() - now.getDay() + 1); // Monday
    weekStart.setHours(0, 0, 0, 0);
    
    const weekEnd = new Date(weekStart);
    weekEnd.setDate(weekStart.getDate() + 6);
    weekEnd.setHours(23, 59, 59, 999);

    const existingWeekly = await Challenge.findOne({
      type: 'weekly',
      startDate: { $gte: weekStart, $lte: weekEnd }
    });

    if (!existingWeekly) {
      await Challenge.create({
        name: `Thử thách tuần ${weekStart.toLocaleDateString('vi-VN')}`,
        description: 'Hoàn thành 20 hoạt động xanh trong tuần này',
        type: 'weekly',
        targetType: 'activities',
        targetValue: 20,
        rewardPoints: 100,
        startDate: weekStart,
        endDate: weekEnd
      });
      console.log('Created weekly challenge');
    }

    // Check for monthly challenge
    const monthStart = new Date(now.getFullYear(), now.getMonth(), 1);
    const monthEnd = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59, 999);

    const existingMonthly = await Challenge.findOne({
      type: 'monthly',
      startDate: { $gte: monthStart, $lte: monthEnd }
    });

    if (!existingMonthly) {
      await Challenge.create({
        name: `Thử thách tháng ${now.getMonth() + 1}/${now.getFullYear()}`,
        description: 'Tích lũy 500 điểm trong tháng này',
        type: 'monthly',
        targetType: 'points',
        targetValue: 500,
        rewardPoints: 200,
        startDate: monthStart,
        endDate: monthEnd
      });
      console.log('Created monthly challenge');
    }
  } catch (error) {
    console.error('Create recurring challenges error:', error.message);
  }
};

module.exports = {
  updateChallengeProgress,
  createRecurringChallenges
};
