const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');
const crypto = require('crypto');

const userSchema = new mongoose.Schema({
  username: {
    type: String,
    required: true,
    unique: true,
    trim: true,
    minlength: 3
  },
  email: {
    type: String,
    required: true,
    unique: true,
    trim: true,
    lowercase: true
  },
  password: {
    type: String,
    minlength: 6
  },
  fullname: {
    type: String,
    required: true,
    trim: true
  },
  role: {
    type: String,
    enum: ['user', 'admin'],
    default: 'user'
  },
  points: {
    type: Number,
    default: 0
  },
  level: {
    type: Number,
    default: 1
  },
  googleId: {
    type: String,
    sparse: true
  },
  avatar: {
    type: String,
    default: ''
  },
  fcmToken: {
    type: String,
    default: ''
  },
  // Password reset fields
  resetPasswordToken: {
    type: String
  },
  resetPasswordExpires: {
    type: Date
  },
  createdAt: {
    type: Date,
    default: Date.now
  }
});

// Hash password before saving
userSchema.pre('save', async function(next) {
  if (!this.isModified('password') || !this.password) return next();
  this.password = await bcrypt.hash(this.password, 10);
  next();
});

// Compare password method
userSchema.methods.comparePassword = async function(candidatePassword) {
  if (!this.password) return false;
  return bcrypt.compare(candidatePassword, this.password);
};

// Calculate level from points
userSchema.methods.calculateLevel = function() {
  this.level = Math.floor(this.points / 100) + 1;
  return this.level;
};

// Add points and update level
userSchema.methods.addPoints = async function(points) {
  this.points += points;
  this.calculateLevel();
  await this.save();
  return this;
};

// Generate password reset token (6 digit code)
userSchema.methods.generateResetToken = function() {
  // Generate 6 digit code
  const resetToken = Math.floor(100000 + Math.random() * 900000).toString();
  
  // Hash token before saving to database
  this.resetPasswordToken = crypto
    .createHash('sha256')
    .update(resetToken)
    .digest('hex');
  
  // Token expires in 1 hour
  this.resetPasswordExpires = Date.now() + 60 * 60 * 1000;
  
  return resetToken; // Return unhashed token to send via email
};

// Verify reset token
userSchema.methods.verifyResetToken = function(token) {
  const hashedToken = crypto
    .createHash('sha256')
    .update(token)
    .digest('hex');
  
  return this.resetPasswordToken === hashedToken && 
         this.resetPasswordExpires > Date.now();
};

// Clear reset token
userSchema.methods.clearResetToken = function() {
  this.resetPasswordToken = undefined;
  this.resetPasswordExpires = undefined;
};

module.exports = mongoose.model('User', userSchema);
