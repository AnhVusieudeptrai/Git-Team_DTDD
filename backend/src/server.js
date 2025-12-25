require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const passport = require('passport');
const path = require('path');

const app = express();

// Middleware
app.use(cors());
app.use(express.json());
app.use(passport.initialize());

// Passport config
require('./config/passport')(passport);

// Health check (không cần DB)
app.get('/api/health', (req, res) => {
  res.json({ 
    status: 'OK', 
    message: 'EcoTrack API is running', 
    timestamp: new Date().toISOString(),
    dbStatus: mongoose.connection.readyState === 1 ? 'connected' : 'disconnected'
  });
});

// Setup admin (one-time use)
app.get('/api/setup-admin', async (req, res) => {
  try {
    const User = require('./models/User');
    
    // Check if admin exists
    let admin = await User.findOne({ username: 'admin' });
    
    if (admin) {
      admin.role = 'admin';
      await admin.save();
      return res.json({ message: 'Admin role updated', username: 'admin' });
    }
    
    // Create new admin
    admin = new User({
      username: 'admin',
      email: 'admin@ecotrack.app',
      password: 'admin123',
      fullname: 'Administrator',
      role: 'admin'
    });
    await admin.save();
    
    res.json({ 
      message: 'Admin created successfully',
      username: 'admin',
      password: 'admin123'
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Routes (lazy load sau khi DB connect)
const authRoutes = require('./routes/auth');
const userRoutes = require('./routes/users');
const activityRoutes = require('./routes/activities');
const leaderboardRoutes = require('./routes/leaderboard');
const adminRoutes = require('./routes/admin');
const badgeRoutes = require('./routes/badges');
const challengeRoutes = require('./routes/challenges');
const streakRoutes = require('./routes/streaks');

app.use('/api/auth', authRoutes);
app.use('/api/users', userRoutes);
app.use('/api/activities', activityRoutes);
app.use('/api/leaderboard', leaderboardRoutes);
app.use('/api/admin', adminRoutes);
app.use('/api/badges', badgeRoutes);
app.use('/api/challenges', challengeRoutes);
app.use('/api/streaks', streakRoutes);

// Serve Admin Dashboard
const adminPublicPath = path.join(__dirname, '../public/admin');
app.use('/admin', express.static(adminPublicPath));
app.get('/admin', (req, res) => {
  res.sendFile(path.join(adminPublicPath, 'index.html'));
});
app.get('/admin/*', (req, res) => {
  res.sendFile(path.join(adminPublicPath, 'index.html'));
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error('Error:', err.message);
  res.status(500).json({ error: 'Internal server error' });
});

// Start server first, then connect to MongoDB
const PORT = process.env.PORT || 3000;
const server = app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 Server running on port ${PORT}`);
});

// Connect to MongoDB with options
const mongoOptions = {
  serverSelectionTimeoutMS: 10000,
  socketTimeoutMS: 45000,
  maxPoolSize: 10,
  retryWrites: true
};

mongoose.connect(process.env.MONGODB_URI, mongoOptions)
  .then(() => {
    console.log('✅ Connected to MongoDB');
    
    // Initialize scheduler after DB connection
    const { initScheduler } = require('./services/schedulerService');
    initScheduler();
  })
  .catch(err => {
    console.error('❌ MongoDB connection error:', err.message);
  });

// Handle MongoDB connection events
mongoose.connection.on('error', err => {
  console.error('MongoDB error:', err.message);
});

mongoose.connection.on('disconnected', () => {
  console.log('MongoDB disconnected, attempting reconnect...');
});
