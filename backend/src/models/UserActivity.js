const mongoose = require('mongoose');

const userActivitySchema = new mongoose.Schema({
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  activity: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Activity',
    required: true
  },
  pointsEarned: {
    type: Number,
    required: true
  },
  completedAt: {
    type: Date,
    default: Date.now
  }
});

// Index for faster queries
userActivitySchema.index({ user: 1, completedAt: -1 });
userActivitySchema.index({ completedAt: -1 });

module.exports = mongoose.model('UserActivity', userActivitySchema);
