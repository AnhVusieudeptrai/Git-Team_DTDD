import { useState, useEffect } from 'react'
import api from '../utils/api'
import Modal from '../components/Modal'

const CHALLENGE_TYPES = [
  { value: 'weekly', label: 'Tuần' },
  { value: 'monthly', label: 'Tháng' }
]

const TARGET_TYPES = [
  { value: 'points', label: 'Điểm' },
  { value: 'activities', label: 'Số hoạt động' },
  { value: 'category', label: 'Danh mục cụ thể' },
  { value: 'streak', label: 'Chuỗi ngày' }
]

const CATEGORIES = [
  { value: '', label: 'Tất cả' },
  { value: 'transport', label: 'Giao thông' },
  { value: 'energy', label: 'Năng lượng' },
  { value: 'water', label: 'Nước' },
  { value: 'waste', label: 'Rác thải' },
  { value: 'green', label: 'Xanh' },
  { value: 'consumption', label: 'Tiêu dùng' }
]

const emptyChallenge = {
  name: '',
  description: '',
  type: 'weekly',
  targetType: 'points',
  targetValue: 100,
  targetCategory: '',
  rewardPoints: 50,
  startDate: new Date().toISOString().split('T')[0],
  endDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
  isActive: true
}

function Challenges() {
  const [challenges, setChallenges] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [editChallenge, setEditChallenge] = useState(null)

  useEffect(() => {
    loadChallenges()
  }, [])

  const loadChallenges = async () => {
    try {
      const res = await api.get('/admin/challenges')
      setChallenges(res.data.challenges)
    } catch (err) {
      console.error('Error loading challenges:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleAdd = () => {
    setEditChallenge({ ...emptyChallenge })
    setShowModal(true)
  }

  const handleEdit = (challenge) => {
    setEditChallenge({
      ...challenge,
      startDate: challenge.startDate?.split('T')[0],
      endDate: challenge.endDate?.split('T')[0]
    })
    setShowModal(true)
  }

  const handleSave = async () => {
    try {
      if (editChallenge._id) {
        await api.put(`/admin/challenges/${editChallenge._id}`, editChallenge)
      } else {
        await api.post('/admin/challenges', editChallenge)
      }
      setShowModal(false)
      loadChallenges()
    } catch (err) {
      alert('Lỗi: ' + err.response?.data?.error)
    }
  }

  const handleDelete = async (challenge) => {
    if (!confirm(`Bạn có chắc muốn xóa thử thách "${challenge.name}"?`)) return
    
    try {
      await api.delete(`/admin/challenges/${challenge._id}`)
      loadChallenges()
    } catch (err) {
      alert('Lỗi: ' + err.response?.data?.error)
    }
  }

  const getStatus = (challenge) => {
    const now = new Date()
    const start = new Date(challenge.startDate)
    const end = new Date(challenge.endDate)
    
    if (!challenge.isActive) return { label: 'Tắt', class: 'badge-error' }
    if (now < start) return { label: 'Sắp diễn ra', class: 'badge-info' }
    if (now > end) return { label: 'Đã kết thúc', class: 'badge-warning' }
    return { label: 'Đang diễn ra', class: 'badge-success' }
  }

  const formatDate = (date) => new Date(date).toLocaleDateString('vi-VN')

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Quản lý thử thách</h1>
          <p className="page-subtitle">{challenges.length} thử thách</p>
        </div>
        <button className="btn btn-primary" onClick={handleAdd}>
          ➕ Thêm thử thách
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
                  <th>Tên thử thách</th>
                  <th>Loại</th>
                  <th>Mục tiêu</th>
                  <th>Phần thưởng</th>
                  <th>Thời gian</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {challenges.map(challenge => {
                  const status = getStatus(challenge)
                  return (
                    <tr key={challenge._id}>
                      <td>
                        <div style={{ fontWeight: '500' }}>{challenge.name}</div>
                        <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                          {challenge.description}
                        </div>
                      </td>
                      <td>
                        <span className="badge badge-info">
                          {challenge.type === 'weekly' ? '📅 Tuần' : '📆 Tháng'}
                        </span>
                      </td>
                      <td>
                        <div>{challenge.targetValue} {TARGET_TYPES.find(t => t.value === challenge.targetType)?.label}</div>
                        {challenge.targetCategory && (
                          <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                            {CATEGORIES.find(c => c.value === challenge.targetCategory)?.label}
                          </div>
                        )}
                      </td>
                      <td style={{ fontWeight: '600', color: 'var(--primary)' }}>
                        +{challenge.rewardPoints} điểm
                      </td>
                      <td>
                        <div>{formatDate(challenge.startDate)}</div>
                        <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                          → {formatDate(challenge.endDate)}
                        </div>
                      </td>
                      <td>
                        <span className={`badge ${status.class}`}>{status.label}</span>
                      </td>
                      <td>
                        <div className="flex gap-2">
                          <button className="btn-icon" onClick={() => handleEdit(challenge)} title="Sửa">✏️</button>
                          <button className="btn-icon" onClick={() => handleDelete(challenge)} title="Xóa">🗑️</button>
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal */}
      {showModal && editChallenge && (
        <Modal 
          title={editChallenge._id ? 'Chỉnh sửa thử thách' : 'Thêm thử thách mới'} 
          onClose={() => setShowModal(false)}
        >
          <div className="form-group">
            <label className="form-label">Tên thử thách *</label>
            <input
              type="text"
              className="form-input"
              value={editChallenge.name}
              onChange={(e) => setEditChallenge({ ...editChallenge, name: e.target.value })}
              placeholder="VD: Tuần lễ xanh"
            />
          </div>
          <div className="form-group">
            <label className="form-label">Mô tả *</label>
            <textarea
              className="form-textarea"
              value={editChallenge.description}
              onChange={(e) => setEditChallenge({ ...editChallenge, description: e.target.value })}
              placeholder="Mô tả thử thách"
            />
          </div>
          <div className="grid-2">
            <div className="form-group">
              <label className="form-label">Loại thử thách</label>
              <select
                className="form-select"
                value={editChallenge.type}
                onChange={(e) => setEditChallenge({ ...editChallenge, type: e.target.value })}
              >
                {CHALLENGE_TYPES.map(t => (
                  <option key={t.value} value={t.value}>{t.label}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Loại mục tiêu</label>
              <select
                className="form-select"
                value={editChallenge.targetType}
                onChange={(e) => setEditChallenge({ ...editChallenge, targetType: e.target.value })}
              >
                {TARGET_TYPES.map(t => (
                  <option key={t.value} value={t.value}>{t.label}</option>
                ))}
              </select>
            </div>
          </div>
          <div className="grid-2">
            <div className="form-group">
              <label className="form-label">Giá trị mục tiêu *</label>
              <input
                type="number"
                className="form-input"
                value={editChallenge.targetValue}
                onChange={(e) => setEditChallenge({ ...editChallenge, targetValue: parseInt(e.target.value) })}
                min="1"
              />
            </div>
            {editChallenge.targetType === 'category' && (
              <div className="form-group">
                <label className="form-label">Danh mục</label>
                <select
                  className="form-select"
                  value={editChallenge.targetCategory}
                  onChange={(e) => setEditChallenge({ ...editChallenge, targetCategory: e.target.value })}
                >
                  {CATEGORIES.slice(1).map(c => (
                    <option key={c.value} value={c.value}>{c.label}</option>
                  ))}
                </select>
              </div>
            )}
          </div>
          <div className="form-group">
            <label className="form-label">Điểm thưởng *</label>
            <input
              type="number"
              className="form-input"
              value={editChallenge.rewardPoints}
              onChange={(e) => setEditChallenge({ ...editChallenge, rewardPoints: parseInt(e.target.value) })}
              min="1"
            />
          </div>
          <div className="grid-2">
            <div className="form-group">
              <label className="form-label">Ngày bắt đầu *</label>
              <input
                type="date"
                className="form-input"
                value={editChallenge.startDate}
                onChange={(e) => setEditChallenge({ ...editChallenge, startDate: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Ngày kết thúc *</label>
              <input
                type="date"
                className="form-input"
                value={editChallenge.endDate}
                onChange={(e) => setEditChallenge({ ...editChallenge, endDate: e.target.value })}
              />
            </div>
          </div>
          <div className="form-group">
            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={editChallenge.isActive}
                onChange={(e) => setEditChallenge({ ...editChallenge, isActive: e.target.checked })}
              />
              Kích hoạt thử thách
            </label>
          </div>
          <div className="modal-footer">
            <button className="btn btn-secondary" onClick={() => setShowModal(false)}>Hủy</button>
            <button className="btn btn-primary" onClick={handleSave}>
              {editChallenge._id ? 'Cập nhật' : 'Thêm mới'}
            </button>
          </div>
        </Modal>
      )}
    </div>
  )
}

export default Challenges
