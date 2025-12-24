const mongoose = require('mongoose');

const activitySchema = new mongoose.Schema({
  name: {
    type: String,
    required: true,
    trim: true
  },
  description: {
    type: String,
    trim: true
  },
  points: {
    type: Number,
    required: true,
    default: 0
  },
  category: {
    type: String,
    enum: ['transport', 'energy', 'water', 'waste', 'green', 'consumption'],
    required: true
  },
  icon: {
    type: String,
    default: 'default'
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

module.exports = mongoose.model('Activity', activitySchema);
