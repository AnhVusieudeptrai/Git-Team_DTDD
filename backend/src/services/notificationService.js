// Lazy load firebase-admin
let admin = null;
let messaging = null;

const initFirebase = () => {
  if (admin) return true;
  
  try {
    admin = require('firebase-admin');
    
    // Initialize only if not already initialized
    if (!admin.apps.length) {
      const serviceAccount = require('../../firebase-service-account.json');
      admin.initializeApp({
        credential: admin.credential.cert(serviceAccount)
      });
    }
    
    messaging = admin.messaging();
    return true;
  } catch (error) {
    console.log('Firebase Admin not available:', error.message);
    return false;
  }
};

// Send notification to a single user
const sendToUser = async (fcmToken, title, body, data = {}) => {
  if (!initFirebase() || !fcmToken) {
    return { success: false, error: 'Firebase not configured or no FCM token' };
  }

  try {
    const message = {
      token: fcmToken,
      notification: { title, body },
      data: { ...data, click_action: 'FLUTTER_NOTIFICATION_CLICK' },
      android: {
        priority: 'high',
        notification: {
          channelId: 'ecotrack_channel',
          icon: 'ic_notification'
        }
      }
    };

    const response = await messaging.send(message);
    return { success: true, messageId: response };
  } catch (error) {
    console.error('Send notification error:', error.message);
    return { success: false, error: error.message };
  }
};

// Send notification to multiple users
const sendToMultiple = async (fcmTokens, title, body, data = {}) => {
  if (!initFirebase() || !fcmTokens.length) {
    return { success: false, error: 'Firebase not configured or no tokens' };
  }

  try {
    const message = {
      notification: { title, body },
      data: { ...data, click_action: 'FLUTTER_NOTIFICATION_CLICK' },
      android: {
        priority: 'high',
        notification: {
          channelId: 'ecotrack_channel',
          icon: 'ic_notification'
        }
      }
    };

    const response = await messaging.sendEachForMulticast({
      tokens: fcmTokens,
      ...message
    });

    return {
      success: true,
      successCount: response.successCount,
      failureCount: response.failureCount
    };
  } catch (error) {
    console.error('Send multicast error:', error.message);
    return { success: false, error: error.message };
  }
};

// Notification types
const notifications = {
  // Daily reminder
  dailyReminder: (fcmToken) => sendToUser(
    fcmToken,
    '🌱 Nhắc nhở hàng ngày',
    'Đừng quên hoàn thành hoạt động xanh hôm nay để duy trì streak!',
    { type: 'daily_reminder' }
  ),

  // Streak at risk
  streakAtRisk: (fcmToken, currentStreak) => sendToUser(
    fcmToken,
    '⚠️ Streak sắp mất!',
    `Streak ${currentStreak} ngày của bạn sẽ mất nếu không hoạt động hôm nay!`,
    { type: 'streak_risk', streak: currentStreak.toString() }
  ),

  // Streak broken
  streakBroken: (fcmToken, lostStreak) => sendToUser(
    fcmToken,
    '💔 Streak đã mất',
    `Streak ${lostStreak} ngày đã kết thúc. Bắt đầu lại nào!`,
    { type: 'streak_broken' }
  ),

  // New streak record
  newStreakRecord: (fcmToken, streak) => sendToUser(
    fcmToken,
    '🎉 Kỷ lục mới!',
    `Chúc mừng! Bạn đã đạt streak ${streak} ngày - kỷ lục mới!`,
    { type: 'streak_record', streak: streak.toString() }
  ),

  // Badge earned
  badgeEarned: (fcmToken, badgeName, badgeIcon) => sendToUser(
    fcmToken,
    '🏆 Huy hiệu mới!',
    `Bạn đã nhận được huy hiệu "${badgeName}" ${badgeIcon}`,
    { type: 'badge_earned', badge: badgeName }
  ),

  // Challenge completed
  challengeCompleted: (fcmToken, challengeName, rewardPoints) => sendToUser(
    fcmToken,
    '🎯 Hoàn thành thử thách!',
    `Bạn đã hoàn thành "${challengeName}" và nhận ${rewardPoints} điểm!`,
    { type: 'challenge_completed', challenge: challengeName }
  ),

  // Challenge ending soon
  challengeEndingSoon: (fcmToken, challengeName, hoursLeft) => sendToUser(
    fcmToken,
    '⏰ Thử thách sắp kết thúc!',
    `"${challengeName}" sẽ kết thúc trong ${hoursLeft} giờ nữa!`,
    { type: 'challenge_ending', challenge: challengeName }
  ),

  // Leaderboard update
  leaderboardUpdate: (fcmToken, newRank, oldRank) => sendToUser(
    fcmToken,
    newRank < oldRank ? '📈 Thăng hạng!' : '📉 Tụt hạng',
    newRank < oldRank 
      ? `Bạn đã lên hạng ${newRank} trên bảng xếp hạng!`
      : `Ai đó đã vượt qua bạn. Hạng hiện tại: ${newRank}`,
    { type: 'leaderboard', rank: newRank.toString() }
  )
};

module.exports = {
  sendToUser,
  sendToMultiple,
  notifications,
  initFirebase
};
