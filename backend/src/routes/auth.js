const express = require('express');
const { body, validationResult } = require('express-validator');
const User = require('../models/User');
const { generateToken, auth } = require('../middleware/auth');
const emailService = require('../services/emailService');

const router = express.Router();

// Lazy load Google OAuth client
let OAuth2Client = null;
const getGoogleClient = () => {
  if (!OAuth2Client) {
    try {
      OAuth2Client = require('google-auth-library').OAuth2Client;
    } catch (e) {
      console.log('Google Auth Library not available');
    }
  }
  return OAuth2Client;
};

// Register with email/password
router.post('/register', [
  body('username').trim().isLength({ min: 3 }).withMessage('Username must be at least 3 characters'),
  body('email').isEmail().withMessage('Invalid email'),
  body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters'),
  body('fullname').trim().notEmpty().withMessage('Full name is required')
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { username, email, password, fullname } = req.body;

    // Check if user exists
    const existingUser = await User.findOne({ $or: [{ email }, { username }] });
    if (existingUser) {
      return res.status(400).json({ error: 'Username or email already exists' });
    }

    const user = new User({ username, email, password, fullname });
    await user.save();

    const token = generateToken(user);

    res.status(201).json({
      message: 'Registration successful',
      token,
      user: {
        id: user._id,
        username: user.username,
        email: user.email,
        fullname: user.fullname,
        role: user.role,
        points: user.points,
        level: user.level
      }
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Login with email/password
router.post('/login', [
  body('username').trim().notEmpty(),
  body('password').notEmpty()
], async (req, res) => {
  try {
    const { username, password } = req.body;

    const user = await User.findOne({ 
      $or: [{ username }, { email: username }] 
    });

    if (!user || !(await user.comparePassword(password))) {
      return res.status(401).json({ error: 'Invalid credentials' });
    }

    const token = generateToken(user);

    res.json({
      message: 'Login successful',
      token,
      user: {
        id: user._id,
        username: user.username,
        email: user.email,
        fullname: user.fullname,
        role: user.role,
        points: user.points,
        level: user.level,
        avatar: user.avatar
      }
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Google Sign-In (verify ID token from Android)
router.post('/google', async (req, res) => {
  try {
    const GoogleOAuth2Client = getGoogleClient();
    if (!GoogleOAuth2Client || !process.env.GOOGLE_CLIENT_ID) {
      return res.status(501).json({ error: 'Google Sign-In not configured' });
    }
    
    const { idToken } = req.body;
    
    const client = new GoogleOAuth2Client(process.env.GOOGLE_CLIENT_ID);
    const ticket = await client.verifyIdToken({
      idToken,
      audience: process.env.GOOGLE_CLIENT_ID
    });
    
    const payload = ticket.getPayload();
    const { sub: googleId, email, name, picture } = payload;

    let user = await User.findOne({ googleId });
    
    if (!user) {
      user = await User.findOne({ email });
      
      if (user) {
        user.googleId = googleId;
        user.avatar = picture || user.avatar;
        await user.save();
      } else {
        user = await User.create({
          googleId,
          email,
          fullname: name,
          username: email.split('@')[0] + '_' + Date.now().toString(36),
          avatar: picture || ''
        });
      }
    }

    const token = generateToken(user);

    res.json({
      message: 'Google login successful',
      token,
      user: {
        id: user._id,
        username: user.username,
        email: user.email,
        fullname: user.fullname,
        role: user.role,
        points: user.points,
        level: user.level,
        avatar: user.avatar
      }
    });
  } catch (error) {
    res.status(401).json({ error: 'Invalid Google token' });
  }
});

// Get current user
router.get('/me', auth, async (req, res) => {
  res.json({
    user: {
      id: req.user._id,
      username: req.user.username,
      email: req.user.email,
      fullname: req.user.fullname,
      role: req.user.role,
      points: req.user.points,
      level: req.user.level,
      avatar: req.user.avatar
    }
  });
});

// Update FCM token for push notifications
router.post('/fcm-token', auth, async (req, res) => {
  try {
    const { fcmToken } = req.body;
    req.user.fcmToken = fcmToken;
    await req.user.save();
    res.json({ message: 'FCM token updated' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Forgot password - send reset code via email
router.post('/forgot-password', [
  body('email').isEmail().withMessage('Invalid email')
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { email } = req.body;
    
    const user = await User.findOne({ email });
    if (!user) {
      return res.status(404).json({ error: 'Email not found', message: 'Không tìm thấy tài khoản với email này' });
    }

    // Generate reset token (6 digit code)
    const resetToken = user.generateResetToken();
    await user.save();

    // Try to send email
    let emailSent = false;
    try {
      const emailResult = await emailService.sendPasswordResetEmail(
        user.email, 
        resetToken, 
        user.fullname || user.username
      );
      emailSent = emailResult.success;
    } catch (emailError) {
      console.error('Email send error:', emailError.message);
    }

    // Response
    const response = { 
      message: emailSent 
        ? 'Mã xác nhận đã được gửi đến email của bạn.' 
        : 'Mã xác nhận đã được tạo.',
      success: true 
    };

    // In development hoặc nếu email không gửi được, trả về token để test
    if (process.env.NODE_ENV === 'development' || !emailSent) {
      response.resetCode = resetToken;
      response.note = 'Mã xác nhận (chỉ hiển thị khi email không gửi được)';
    }

    res.json(response);
  } catch (error) {
    console.error('Forgot password error:', error);
    res.status(500).json({ error: error.message });
  }
});

// Verify reset token
router.post('/verify-reset-token', [
  body('email').isEmail().withMessage('Invalid email'),
  body('token').isLength({ min: 6, max: 6 }).withMessage('Token must be 6 digits')
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { email, token } = req.body;
    
    const user = await User.findOne({ email });
    if (!user) {
      return res.status(404).json({ error: 'User not found', message: 'Không tìm thấy tài khoản' });
    }

    if (!user.verifyResetToken(token)) {
      return res.status(400).json({ error: 'Invalid or expired token', message: 'Mã xác nhận không hợp lệ hoặc đã hết hạn' });
    }

    res.json({ 
      message: 'Mã xác nhận hợp lệ',
      success: true,
      valid: true
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Reset password with token
router.post('/reset-password', [
  body('email').isEmail().withMessage('Invalid email'),
  body('token').isLength({ min: 6, max: 6 }).withMessage('Token must be 6 digits'),
  body('newPassword').isLength({ min: 6 }).withMessage('Password must be at least 6 characters')
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { email, token, newPassword } = req.body;
    
    const user = await User.findOne({ email });
    if (!user) {
      return res.status(404).json({ error: 'User not found', message: 'Không tìm thấy tài khoản' });
    }

    if (!user.verifyResetToken(token)) {
      return res.status(400).json({ error: 'Invalid or expired token', message: 'Mã xác nhận không hợp lệ hoặc đã hết hạn' });
    }

    // Update password and clear reset token
    user.password = newPassword;
    user.clearResetToken();
    await user.save();

    res.json({ 
      message: 'Đặt lại mật khẩu thành công. Vui lòng đăng nhập với mật khẩu mới.',
      success: true 
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Change password (requires authentication)
router.post('/change-password', auth, [
  body('oldPassword').notEmpty().withMessage('Old password is required'),
  body('newPassword').isLength({ min: 6 }).withMessage('New password must be at least 6 characters')
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { oldPassword, newPassword } = req.body;
    
    // Verify old password
    const isMatch = await req.user.comparePassword(oldPassword);
    if (!isMatch) {
      return res.status(400).json({ error: 'Incorrect old password', message: 'Mật khẩu hiện tại không đúng' });
    }

    // Update password
    req.user.password = newPassword;
    await req.user.save();

    res.json({ message: 'Đổi mật khẩu thành công', success: true });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
