const mongoose = require('mongoose');

const streakSchema = new mongoose.Schema({
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true,
    unique: true
  },
  currentStreak: {
    type: Number,
    default: 0
  },
  longestStreak: {
    type: Number,
    default: 0
  },
  lastActivityDate: {
    type: Date,
    default: null
  },
  streakStartDate: {
    type: Date,
    default: null
  },
  updatedAt: {
    type: Date,
    default: Date.now
  }
});

// Check and update streak
streakSchema.methods.updateStreak = function(activityDate) {
  const today = new Date(activityDate);
  today.setHours(0, 0, 0, 0);
  
  if (!this.lastActivityDate) {
    // First activity ever
    this.currentStreak = 1;
    this.longestStreak = 1;
    this.lastActivityDate = today;
    this.streakStartDate = today;
    return { streakUpdated: true, currentStreak: 1, isNewRecord: true };
  }

  const lastDate = new Date(this.lastActivityDate);
  lastDate.setHours(0, 0, 0, 0);
  
  const diffDays = Math.floor((today - lastDate) / (1000 * 60 * 60 * 24));

  if (diffDays === 0) {
    // Same day, no change
    return { streakUpdated: false, currentStreak: this.currentStreak, isNewRecord: false };
  } else if (diffDays === 1) {
    // Consecutive day
    this.currentStreak += 1;
    this.lastActivityDate = today;
    
    const isNewRecord = this.currentStreak > this.longestStreak;
    if (isNewRecord) {
      this.longestStreak = this.currentStreak;
    }
    
    return { streakUpdated: true, currentStreak: this.currentStreak, isNewRecord };
  } else {
    // Streak broken
    this.currentStreak = 1;
    this.lastActivityDate = today;
    this.streakStartDate = today;
    return { streakUpdated: true, currentStreak: 1, isNewRecord: false, streakBroken: true };
  }
};

module.exports = mongoose.model('Streak', streakSchema);
