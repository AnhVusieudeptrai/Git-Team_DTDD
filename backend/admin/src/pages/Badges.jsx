import { useState, useEffect } from 'react'
import api from '../utils/api'
import Modal from '../components/Modal'

const BADGE_TYPES = [
  { value: 'streak', label: 'Streak (Chuỗi ngày)' },
  { value: 'points', label: 'Điểm' },
  { value: 'activities', label: 'Số hoạt động' },
  { value: 'challenge', label: 'Thử thách' },
  { value: 'special', label: 'Đặc biệt' }
]

const RARITIES = [
  { value: 'common', label: 'Thường', color: 'badge-common' },
  { value: 'rare', label: 'Hiếm', color: 'badge-rare' },
  { value: 'epic', label: 'Sử thi', color: 'badge-epic' },
  { value: 'legendary', label: 'Huyền thoại', color: 'badge-legendary' }
]

const ICONS = ['🏆', '🥇', '🥈', '🥉', '⭐', '🌟', '💎', '👑', '🔥', '⚡', '🎯', '🎖️', '🏅', '💪', '🌍', '🌱']

const emptyBadge = {
  name: '',
  description: '',
  icon: '🏆',
  type: 'streak',
  requirement: 7,
  rarity: 'common',
  isActive: true
}

function Badges() {
  const [badges, setBadges] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [editBadge, setEditBadge] = useState(null)

  useEffect(() => {
    loadBadges()
  }, [])

  const loadBadges = async () => {
    try {
      const res = await api.get('/admin/badges')
      setBadges(res.data.badges)
    } catch (err) {
      console.error('Error loading badges:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleAdd = () => {
    setEditBadge({ ...emptyBadge })
    setShowModal(true)
  }

  const handleEdit = (badge) => {
    setEditBadge({ ...badge })
    setShowModal(true)
  }

  const handleSave = async () => {
    try {
      if (editBadge._id) {
        await api.put(`/admin/badges/${editBadge._id}`, editBadge)
      } else {
        await api.post('/admin/badges', editBadge)
      }
      setShowModal(false)
      loadBadges()
    } catch (err) {
      alert('Lỗi: ' + err.response?.data?.error)
    }
  }

  const handleDelete = async (badge) => {
    if (!confirm(`Bạn có chắc muốn xóa huy hiệu "${badge.name}"?`)) return
    
    try {
      await api.delete(`/admin/badges/${badge._id}`)
      loadBadges()
    } catch (err) {
      alert('Lỗi: ' + err.response?.data?.error)
    }
  }

  const getTypeLabel = (type) => BADGE_TYPES.find(t => t.value === type)?.label || type
  const getRarityInfo = (rarity) => RARITIES.find(r => r.value === rarity) || RARITIES[0]

  const getRequirementText = (badge) => {
    switch (badge.type) {
      case 'streak': return `${badge.requirement} ngày liên tiếp`
      case 'points': return `${badge.requirement} điểm`
      case 'activities': return `${badge.requirement} hoạt động`
      default: return badge.requirement
    }
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Quản lý huy hiệu</h1>
          <p className="page-subtitle">{badges.length} huy hiệu</p>
        </div>
        <button className="btn btn-primary" onClick={handleAdd}>
          ➕ Thêm huy hiệu
        </button>
      </div>

      {/* Table */}
      <div className="card">
        {loading ? (
          <div className="loading"><div className="spinner"></div></div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Icon</th>
                  <th>Tên huy hiệu</th>
                  <th>Loại</th>
                  <th>Yêu cầu</th>
                  <th>Độ hiếm</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {badges.map(badge => (
                  <tr key={badge._id}>
                    <td style={{ fontSize: '32px' }}>{badge.icon}</td>
                    <td>
                      <div style={{ fontWeight: '500' }}>{badge.name}</div>
                      <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                        {badge.description}
                      </div>
                    </td>
                    <td>{getTypeLabel(badge.type)}</td>
                    <td style={{ fontWeight: '500' }}>{getRequirementText(badge)}</td>
                    <td>
                      <span className={`badge ${getRarityInfo(badge.rarity).color}`}>
                        {getRarityInfo(badge.rarity).label}
                      </span>
                    </td>
                    <td>
                      <span className={`badge ${badge.isActive ? 'badge-success' : 'badge-error'}`}>
                        {badge.isActive ? '✅ Hoạt động' : '❌ Tắt'}
                      </span>
                    </td>
                    <td>
                      <div className="flex gap-2">
                        <button className="btn-icon" onClick={() => handleEdit(badge)} title="Sửa">✏️</button>
                        <button className="btn-icon" onClick={() => handleDelete(badge)} title="Xóa">🗑️</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal */}
      {showModal && editBadge && (
        <Modal 
          title={editBadge._id ? 'Chỉnh sửa huy hiệu' : 'Thêm huy hiệu mới'} 
          onClose={() => setShowModal(false)}
        >
          <div className="form-group">
            <label className="form-label">Tên huy hiệu *</label>
            <input
              type="text"
              className="form-input"
              value={editBadge.name}
              onChange={(e) => setEditBadge({ ...editBadge, name: e.target.value })}
              placeholder="VD: Người tiên phong"
            />
          </div>
          <div className="form-group">
            <label className="form-label">Mô tả *</label>
            <textarea
              className="form-textarea"
              value={editBadge.description}
              onChange={(e) => setEditBadge({ ...editBadge, description: e.target.value })}
              placeholder="Mô tả cách đạt được huy hiệu"
            />
          </div>
          <div className="grid-2">
            <div className="form-group">
              <label className="form-label">Loại *</label>
              <select
                className="form-select"
                value={editBadge.type}
                onChange={(e) => setEditBadge({ ...editBadge, type: e.target.value })}
              >
                {BADGE_TYPES.map(type => (
                  <option key={type.value} value={type.value}>{type.label}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Yêu cầu *</label>
              <input
                type="number"
                className="form-input"
                value={editBadge.requirement}
                onChange={(e) => setEditBadge({ ...editBadge, requirement: parseInt(e.target.value) })}
                min="1"
              />
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">Độ hiếm</label>
            <select
              className="form-select"
              value={editBadge.rarity}
              onChange={(e) => setEditBadge({ ...editBadge, rarity: e.target.value })}
            >
              {RARITIES.map(r => (
                <option key={r.value} value={r.value}>{r.label}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Icon</label>
            <div className="flex gap-2" style={{ flexWrap: 'wrap' }}>
              {ICONS.map(icon => (
                <button
                  key={icon}
                  type="button"
                  onClick={() => setEditBadge({ ...editBadge, icon })}
                  style={{
                    width: '40px',
                    height: '40px',
                    fontSize: '20px',
                    border: editBadge.icon === icon ? '2px solid var(--primary)' : '1px solid var(--border)',
                    borderRadius: '8px',
                    background: editBadge.icon === icon ? 'var(--primary-bg)' : 'white',
                    cursor: 'pointer'
                  }}
                >
                  {icon}
                </button>
              ))}
            </div>
          </div>
          <div className="modal-footer">
            <button className="btn btn-secondary" onClick={() => setShowModal(false)}>Hủy</button>
            <button className="btn btn-primary" onClick={handleSave}>
              {editBadge._id ? 'Cập nhật' : 'Thêm mới'}
            </button>
          </div>
        </Modal>
      )}
    </div>
  )
}

export default Badges
