const User = require('../models/User');
const { notifications, sendToMultiple } = require('./notificationService');
const { checkStreaksAtRisk, checkExpiredStreaks } = require('./streakService');
const { createRecurringChallenges } = require('./challengeService');

// Store interval IDs for cleanup
let intervals = [];

// Send daily reminder to all users
const sendDailyReminders = async () => {
  try {
    const users = await User.find({ 
      fcmToken: { $exists: true, $ne: '' } 
    }).select('fcmToken');

    const tokens = users.map(u => u.fcmToken).filter(Boolean);
    
    if (tokens.length > 0) {
      const result = await sendToMultiple(
        tokens,
        '🌱 Nhắc nhở hàng ngày',
        'Đừng quên hoàn thành hoạt động xanh hôm nay!',
        { type: 'daily_reminder' }
      );
      console.log(`Daily reminder sent: ${result.successCount || 0} success, ${result.failureCount || 0} failed`);
    }
  } catch (error) {
    console.error('Daily reminder error:', error.message);
  }
};

// Run all scheduled tasks
const runScheduledTasks = async () => {
  console.log('Running scheduled tasks...');
  
  // Check and create recurring challenges
  await createRecurringChallenges();
  
  // Check streaks at risk (users who haven't done activity today)
  await checkStreaksAtRisk();
  
  // Break expired streaks
  await checkExpiredStreaks();
};

// Initialize scheduler
const initScheduler = () => {
  console.log('Initializing scheduler...');

  // Run tasks immediately on startup
  setTimeout(runScheduledTasks, 5000);

  // Daily reminder at 9:00 AM (adjust based on server timezone)
  const now = new Date();
  const reminderTime = new Date(now);
  reminderTime.setHours(9, 0, 0, 0);
  
  if (reminderTime <= now) {
    reminderTime.setDate(reminderTime.getDate() + 1);
  }
  
  const msUntilReminder = reminderTime - now;
  
  // Schedule first reminder
  setTimeout(() => {
    sendDailyReminders();
    // Then repeat every 24 hours
    const dailyInterval = setInterval(sendDailyReminders, 24 * 60 * 60 * 1000);
    intervals.push(dailyInterval);
  }, msUntilReminder);

  // Run scheduled tasks every hour
  const hourlyInterval = setInterval(runScheduledTasks, 60 * 60 * 1000);
  intervals.push(hourlyInterval);

  console.log(`Scheduler initialized. Next daily reminder in ${Math.round(msUntilReminder / 1000 / 60)} minutes`);
};

// Cleanup intervals
const stopScheduler = () => {
  intervals.forEach(interval => clearInterval(interval));
  intervals = [];
  console.log('Scheduler stopped');
};

module.exports = {
  initScheduler,
  stopScheduler,
  sendDailyReminders,
  runScheduledTasks
};
