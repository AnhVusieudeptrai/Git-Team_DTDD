const express = require('express');
const Badge = require('../models/Badge');
const UserBadge = require('../models/UserBadge');
const { auth, adminOnly } = require('../middleware/auth');

const router = express.Router();

// Get all badges
router.get('/', auth, async (req, res) => {
  try {
    const badges = await Badge.find({ isActive: true }).sort({ rarity: 1, type: 1 });
    
    // Get user's earned badges
    const userBadges = await UserBadge.find({ user: req.user._id }).select('badge earnedAt');
    const earnedBadgeIds = userBadges.map(ub => ub.badge.toString());
    
    const badgesWithStatus = badges.map(badge => ({
      id: badge._id,
      name: badge.name,
      description: badge.description,
      icon: badge.icon,
      type: badge.type,
      requirement: badge.requirement,
      rarity: badge.rarity,
      earned: earnedBadgeIds.includes(badge._id.toString()),
      earnedAt: userBadges.find(ub => ub.badge.toString() === badge._id.toString())?.earnedAt || null
    }));

    res.json({ badges: badgesWithStatus });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Get user's earned badges
router.get('/my', auth, async (req, res) => {
  try {
    const userBadges = await UserBadge.find({ user: req.user._id })
      .populate('badge')
      .sort({ earnedAt: -1 });

    res.json({
      badges: userBadges.map(ub => ({
        id: ub.badge._id,
        name: ub.badge.name,
        description: ub.badge.description,
        icon: ub.badge.icon,
        type: ub.badge.type,
        rarity: ub.badge.rarity,
        earnedAt: ub.earnedAt
      })),
      total: userBadges.length
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Admin: Create badge
router.post('/', auth, adminOnly, async (req, res) => {
  try {
    const { name, description, icon, type, requirement, rarity } = req.body;
    
    const badge = new Badge({ name, description, icon, type, requirement, rarity });
    await badge.save();

    res.status(201).json({ message: 'Huy hiệu đã được tạo', badge });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Admin: Update badge
router.put('/:id', auth, adminOnly, async (req, res) => {
  try {
    const badge = await Badge.findByIdAndUpdate(req.params.id, req.body, { new: true });
    if (!badge) {
      return res.status(404).json({ error: 'Không tìm thấy huy hiệu' });
    }
    res.json({ message: 'Huy hiệu đã được cập nhật', badge });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Admin: Delete badge
router.delete('/:id', auth, adminOnly, async (req, res) => {
  try {
    await Badge.findByIdAndDelete(req.params.id);
    await UserBadge.deleteMany({ badge: req.params.id });
    res.json({ message: 'Huy hiệu đã được xóa' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
