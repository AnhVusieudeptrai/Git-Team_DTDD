const express = require('express');
const Challenge = require('../models/Challenge');
const UserChallenge = require('../models/UserChallenge');
const UserActivity = require('../models/UserActivity');
const { auth, adminOnly } = require('../middleware/auth');

const router = express.Router();

// Get active challenges
router.get('/', auth, async (req, res) => {
  try {
    const now = new Date();
    
    const challenges = await Challenge.find({
      isActive: true,
      startDate: { $lte: now },
      endDate: { $gte: now }
    }).populate('rewardBadge', 'name icon rarity');

    // Get user's challenge progress
    const userChallenges = await UserChallenge.find({
      user: req.user._id,
      challenge: { $in: challenges.map(c => c._id) }
    });

    const challengesWithProgress = challenges.map(challenge => {
      const userChallenge = userChallenges.find(
        uc => uc.challenge.toString() === challenge._id.toString()
      );
      
      return {
        id: challenge._id,
        name: challenge.name,
        description: challenge.description,
        type: challenge.type,
        targetType: challenge.targetType,
        targetValue: challenge.targetValue,
        targetCategory: challenge.targetCategory,
        rewardPoints: challenge.rewardPoints,
        rewardBadge: challenge.rewardBadge,
        startDate: challenge.startDate,
        endDate: challenge.endDate,
        joined: !!userChallenge,
        progress: userChallenge?.progress || 0,
        isCompleted: userChallenge?.isCompleted || false,
        completedAt: userChallenge?.completedAt || null,
        progressPercent: Math.min(100, Math.round(((userChallenge?.progress || 0) / challenge.targetValue) * 100))
      };
    });

    res.json({ challenges: challengesWithProgress });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Join a challenge
router.post('/:id/join', auth, async (req, res) => {
  try {
    const challenge = await Challenge.findById(req.params.id);
    
    if (!challenge) {
      return res.status(404).json({ error: 'Không tìm thấy thử thách' });
    }

    const now = new Date();
    if (now > challenge.endDate) {
      return res.status(400).json({ error: 'Thử thách đã kết thúc' });
    }

    // Check if already joined
    const existing = await UserChallenge.findOne({
      user: req.user._id,
      challenge: challenge._id
    });

    if (existing) {
      return res.status(400).json({ error: 'Bạn đã tham gia thử thách này' });
    }

    // Calculate initial progress based on activities since challenge start
    let initialProgress = 0;
    
    if (challenge.targetType === 'points') {
      const activities = await UserActivity.find({
        user: req.user._id,
        completedAt: { $gte: challenge.startDate }
      });
      initialProgress = activities.reduce((sum, a) => sum + a.pointsEarned, 0);
    } else if (challenge.targetType === 'activities') {
      const query = {
        user: req.user._id,
        completedAt: { $gte: challenge.startDate }
      };
      
      if (challenge.targetCategory) {
        const Activity = require('../models/Activity');
        const categoryActivities = await Activity.find({ category: challenge.targetCategory });
        query.activity = { $in: categoryActivities.map(a => a._id) };
      }
      
      initialProgress = await UserActivity.countDocuments(query);
    }

    const userChallenge = new UserChallenge({
      user: req.user._id,
      challenge: challenge._id,
      progress: initialProgress,
      isCompleted: initialProgress >= challenge.targetValue
    });

    if (userChallenge.isCompleted) {
      userChallenge.completedAt = new Date();
    }

    await userChallenge.save();

    res.json({
      message: 'Đã tham gia thử thách',
      progress: initialProgress,
      isCompleted: userChallenge.isCompleted
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Get my challenges
router.get('/my', auth, async (req, res) => {
  try {
    const userChallenges = await UserChallenge.find({ user: req.user._id })
      .populate({
        path: 'challenge',
        populate: { path: 'rewardBadge', select: 'name icon rarity' }
      })
      .sort({ joinedAt: -1 });

    const now = new Date();
    
    const challenges = userChallenges.map(uc => ({
      id: uc.challenge._id,
      name: uc.challenge.name,
      description: uc.challenge.description,
      type: uc.challenge.type,
      targetType: uc.challenge.targetType,
      targetValue: uc.challenge.targetValue,
      rewardPoints: uc.challenge.rewardPoints,
      rewardBadge: uc.challenge.rewardBadge,
      startDate: uc.challenge.startDate,
      endDate: uc.challenge.endDate,
      progress: uc.progress,
      isCompleted: uc.isCompleted,
      completedAt: uc.completedAt,
      isExpired: now > uc.challenge.endDate,
      progressPercent: Math.min(100, Math.round((uc.progress / uc.challenge.targetValue) * 100))
    }));

    res.json({
      active: challenges.filter(c => !c.isExpired && !c.isCompleted),
      completed: challenges.filter(c => c.isCompleted),
      expired: challenges.filter(c => c.isExpired && !c.isCompleted)
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Admin: Create challenge
router.post('/', auth, adminOnly, async (req, res) => {
  try {
    const challenge = new Challenge(req.body);
    await challenge.save();
    res.status(201).json({ message: 'Thử thách đã được tạo', challenge });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Admin: Update challenge
router.put('/:id', auth, adminOnly, async (req, res) => {
  try {
    const challenge = await Challenge.findByIdAndUpdate(req.params.id, req.body, { new: true });
    if (!challenge) {
      return res.status(404).json({ error: 'Không tìm thấy thử thách' });
    }
    res.json({ message: 'Thử thách đã được cập nhật', challenge });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Admin: Delete challenge
router.delete('/:id', auth, adminOnly, async (req, res) => {
  try {
    await Challenge.findByIdAndDelete(req.params.id);
    await UserChallenge.deleteMany({ challenge: req.params.id });
    res.json({ message: 'Thử thách đã được xóa' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
