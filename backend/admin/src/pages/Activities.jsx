import { useState, useEffect } from 'react'
import api from '../utils/api'
import Modal from '../components/Modal'

const CATEGORIES = [
  { value: 'transport', label: 'Giao thông', icon: '🚴' },
  { value: 'energy', label: 'Năng lượng', icon: '💡' },
  { value: 'water', label: 'Nước', icon: '💧' },
  { value: 'waste', label: 'Rác thải', icon: '♻️' },
  { value: 'green', label: 'Xanh', icon: '🌱' },
  { value: 'consumption', label: 'Tiêu dùng', icon: '🛍️' }
]

const ICONS = ['🚴', '🚌', '🚶', '💡', '💻', '❄️', '💧', '🚿', '♻️', '🗑️', '🌱', '🌳', '🛒', '📦', '☕', '🥗']

const emptyActivity = {
  name: '',
  description: '',
  points: 10,
  category: 'transport',
  icon: '🌱',
  isActive: true
}

function Activities() {
  const [activities, setActivities] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [editActivity, setEditActivity] = useState(null)
  const [filter, setFilter] = useState('')

  useEffect(() => {
    loadActivities()
  }, [])

  const loadActivities = async () => {
    try {
      const res = await api.get('/admin/activities')
      setActivities(res.data.activities)
    } catch (err) {
      console.error('Error loading activities:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleAdd = () => {
    setEditActivity({ ...emptyActivity })
    setShowModal(true)
  }

  const handleEdit = (activity) => {
    setEditActivity({ ...activity })
    setShowModal(true)
  }

  const handleSave = async () => {
    try {
      if (editActivity._id) {
        await api.put(`/admin/activities/${editActivity._id}`, editActivity)
      } else {
        await api.post('/admin/activities', editActivity)
      }
      setShowModal(false)
      loadActivities()
    } catch (err) {
      alert('Lỗi: ' + err.response?.data?.error)
    }
  }

  const handleDelete = async (activity) => {
    if (!confirm(`Bạn có chắc muốn xóa hoạt động "${activity.name}"?`)) return
    
    try {
      await api.delete(`/admin/activities/${activity._id}`)
      loadActivities()
    } catch (err) {
      alert('Lỗi: ' + err.response?.data?.error)
    }
  }

  const handleToggleActive = async (activity) => {
    try {
      await api.put(`/admin/activities/${activity._id}`, { isActive: !activity.isActive })
      loadActivities()
    } catch (err) {
      alert('Lỗi: ' + err.response?.data?.error)
    }
  }

  const filteredActivities = filter 
    ? activities.filter(a => a.category === filter)
    : activities

  const getCategoryInfo = (category) => CATEGORIES.find(c => c.value === category) || {}

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Quản lý hoạt động</h1>
          <p className="page-subtitle">{activities.length} hoạt động xanh</p>
        </div>
        <button className="btn btn-primary" onClick={handleAdd}>
          ➕ Thêm hoạt động
        </button>
      </div>

      {/* Filter */}
      <div className="card mb-4">
        <div className="flex gap-2">
          <button 
            className={`btn ${!filter ? 'btn-primary' : 'btn-secondary'} btn-sm`}
            onClick={() => setFilter('')}
          >
            Tất cả
          </button>
          {CATEGORIES.map(cat => (
            <button
              key={cat.value}
              className={`btn ${filter === cat.value ? 'btn-primary' : 'btn-secondary'} btn-sm`}
              onClick={() => setFilter(cat.value)}
            >
              {cat.icon} {cat.label}
            </button>
          ))}
        </div>
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
                  <th>Tên hoạt động</th>
                  <th>Danh mục</th>
                  <th>Điểm</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {filteredActivities.map(activity => (
                  <tr key={activity._id}>
                    <td style={{ fontSize: '24px' }}>{activity.icon}</td>
                    <td>
                      <div style={{ fontWeight: '500' }}>{activity.name}</div>
                      <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                        {activity.description}
                      </div>
                    </td>
                    <td>
                      <span className="badge badge-success">
                        {getCategoryInfo(activity.category).icon} {getCategoryInfo(activity.category).label}
                      </span>
                    </td>
                    <td style={{ fontWeight: '600', color: 'var(--primary)' }}>+{activity.points}</td>
                    <td>
                      <span className={`badge ${activity.isActive ? 'badge-success' : 'badge-error'}`}>
                        {activity.isActive ? '✅ Hoạt động' : '❌ Tắt'}
                      </span>
                    </td>
                    <td>
                      <div className="flex gap-2">
                        <button className="btn-icon" onClick={() => handleEdit(activity)} title="Sửa">✏️</button>
                        <button 
                          className="btn-icon" 
                          onClick={() => handleToggleActive(activity)}
                          title={activity.isActive ? 'Tắt' : 'Bật'}
                        >
                          {activity.isActive ? '🔴' : '🟢'}
                        </button>
                        <button className="btn-icon" onClick={() => handleDelete(activity)} title="Xóa">🗑️</button>
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
      {showModal && editActivity && (
        <Modal 
          title={editActivity._id ? 'Chỉnh sửa hoạt động' : 'Thêm hoạt động mới'} 
          onClose={() => setShowModal(false)}
        >
          <div className="form-group">
            <label className="form-label">Tên hoạt động *</label>
            <input
              type="text"
              className="form-input"
              value={editActivity.name}
              onChange={(e) => setEditActivity({ ...editActivity, name: e.target.value })}
              placeholder="VD: Đi xe đạp đi làm"
            />
          </div>
          <div className="form-group">
            <label className="form-label">Mô tả</label>
            <textarea
              className="form-textarea"
              value={editActivity.description}
              onChange={(e) => setEditActivity({ ...editActivity, description: e.target.value })}
              placeholder="Mô tả ngắn về hoạt động"
            />
          </div>
          <div className="grid-2">
            <div className="form-group">
              <label className="form-label">Danh mục *</label>
              <select
                className="form-select"
                value={editActivity.category}
                onChange={(e) => setEditActivity({ ...editActivity, category: e.target.value })}
              >
                {CATEGORIES.map(cat => (
                  <option key={cat.value} value={cat.value}>{cat.icon} {cat.label}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Điểm thưởng *</label>
              <input
                type="number"
                className="form-input"
                value={editActivity.points}
                onChange={(e) => setEditActivity({ ...editActivity, points: parseInt(e.target.value) })}
                min="1"
              />
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">Icon</label>
            <div className="flex gap-2" style={{ flexWrap: 'wrap' }}>
              {ICONS.map(icon => (
                <button
                  key={icon}
                  type="button"
                  onClick={() => setEditActivity({ ...editActivity, icon })}
                  style={{
                    width: '40px',
                    height: '40px',
                    fontSize: '20px',
                    border: editActivity.icon === icon ? '2px solid var(--primary)' : '1px solid var(--border)',
                    borderRadius: '8px',
                    background: editActivity.icon === icon ? 'var(--primary-bg)' : 'white',
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
              {editActivity._id ? 'Cập nhật' : 'Thêm mới'}
            </button>
          </div>
        </Modal>
      )}
    </div>
  )
}

export default Activities
