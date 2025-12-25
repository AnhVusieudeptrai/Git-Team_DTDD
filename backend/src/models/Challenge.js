const mongoose = require('mongoose');

const challengeSchema = new mongoose.Schema({
  name: {
    type: String,
    required: true
  },
  description: {
    type: String,
    required: true
  },
  type: {
    type: String,
    enum: ['weekly', 'monthly'],
    required: true
  },
  targetType: {
    type: String,
    enum: ['points', 'activities', 'category', 'streak'],
    required: true
  },
  targetValue: {
    type: Number,
    required: true
  },
  targetCategory: {
    type: String,
    enum: ['transport', 'energy', 'water', 'waste', 'green', 'consumption', null],
    default: null
  },
  rewardPoints: {
    type: Number,
    required: true
  },
  rewardBadge: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Badge',
    default: null
  },
  startDate: {
    type: Date,
    required: true
  },
  endDate: {
    type: Date,
    required: true
  },
  isActive: {
    type: Boolean,
    default: true
  },
  createdAt: {
    type: Date,
    default: Date.now
  }
});

challengeSchema.index({ startDate: 1, endDate: 1 });
challengeSchema.index({ isActive: 1, type: 1 });

module.exports = mongoose.model('Challenge', challengeSchema);
