import { useState, useEffect } from 'react'
import api from '../utils/api'
import Modal from '../components/Modal'

function Users() {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [pagination, setPagination] = useState({ page: 1, pages: 1, total: 0 })
  const [editUser, setEditUser] = useState(null)
  const [showModal, setShowModal] = useState(false)

  useEffect(() => {
    loadUsers()
  }, [pagination.page, search])

  const loadUsers = async () => {
    try {
      setLoading(true)
      const res = await api.get('/admin/users', {
        params: { page: pagination.page, limit: 20, search }
      })
      setUsers(res.data.users)
      setPagination(res.data.pagination)
    } catch (err) {
      console.error('Error loading users:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = (e) => {
    e.preventDefault()
    setPagination(prev => ({ ...prev, page: 1 }))
  }

  const handleToggleLock = async (user) => {
    if (!confirm(`Bạn có chắc muốn ${user.isLocked ? 'mở khóa' : 'khóa'} tài khoản này?`)) return
    
    try {
      await api.post(`/admin/users/${user._id}/toggle-lock`)
      loadUsers()
    } catch (err) {
      alert('Lỗi: ' + err.response?.data?.error)
    }
  }

  const handleDelete = async (user) => {
    if (!confirm(`Bạn có chắc muốn xóa người dùng "${user.username}"? Hành động này không thể hoàn tác.`)) return
    
    try {
      await api.delete(`/admin/users/${user._id}`)
      loadUsers()
    } catch (err) {
      alert('Lỗi: ' + err.response?.data?.error)
    }
  }

  const handleEdit = (user) => {
    setEditUser({ ...user })
    setShowModal(true)
  }

  const handleSave = async () => {
    try {
      await api.put(`/admin/users/${editUser._id}`, editUser)
      setShowModal(false)
      loadUsers()
    } catch (err) {
      alert('Lỗi: ' + err.response?.data?.error)
    }
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Quản lý người dùng</h1>
          <p className="page-subtitle">{pagination.total} người dùng</p>
        </div>
      </div>

      {/* Search */}
      <div className="card mb-4">
        <form onSubmit={handleSearch} className="flex gap-4">
          <div className="search-box" style={{ flex: 1 }}>
            <span>🔍</span>
            <input
              type="text"
              placeholder="Tìm theo tên, email..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <button type="submit" className="btn btn-primary">Tìm kiếm</button>
        </form>
      </div>

      {/* Table */}
      <div className="card">
        {loading ? (
          <div className="loading"><div className="spinner"></div></div>
        ) : (
          <>
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Người dùng</th>
                    <th>Email</th>
                    <th>Điểm</th>
                    <th>Cấp độ</th>
                    <th>Vai trò</th>
                    <th>Trạng thái</th>
                    <th>Ngày tạo</th>
                    <th>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map(user => (
                    <tr key={user._id}>
                      <td>
                        <div className="flex items-center gap-2">
                          <div style={{
                            width: '36px',
                            height: '36px',
                            borderRadius: '50%',
                            background: 'var(--primary-bg)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center'
                          }}>
                            {user.avatar ? (
                              <img src={user.avatar} alt="" style={{ width: '100%', height: '100%', borderRadius: '50%', objectFit: 'cover' }} />
                            ) : '👤'}
                          </div>
                          <div>
                            <div style={{ fontWeight: '500' }}>{user.fullname}</div>
                            <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>@{user.username}</div>
                          </div>
                        </div>
                      </td>
                      <td>{user.email}</td>
                      <td>{user.points?.toLocaleString()}</td>
                      <td>Lv.{user.level}</td>
                      <td>
                        <span className={`badge ${user.role === 'admin' ? 'badge-info' : 'badge-success'}`}>
                          {user.role === 'admin' ? 'Admin' : 'User'}
                        </span>
                      </td>
                      <td>
                        <span className={`badge ${user.isLocked ? 'badge-error' : 'badge-success'}`}>
                          {user.isLocked ? '🔒 Đã khóa' : '✅ Hoạt động'}
                        </span>
                      </td>
                      <td>{new Date(user.createdAt).toLocaleDateString('vi-VN')}</td>
                      <td>
                        <div className="flex gap-2">
                          <button className="btn-icon" onClick={() => handleEdit(user)} title="Sửa">✏️</button>
                          {user.role !== 'admin' && (
                            <>
                              <button 
                                className="btn-icon" 
                                onClick={() => handleToggleLock(user)}
                                title={user.isLocked ? 'Mở khóa' : 'Khóa'}
                              >
                                {user.isLocked ? '🔓' : '🔒'}
                              </button>
                              <button className="btn-icon" onClick={() => handleDelete(user)} title="Xóa">🗑️</button>
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            {pagination.pages > 1 && (
              <div className="flex justify-between items-center mt-4">
                <span style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
                  Trang {pagination.page} / {pagination.pages}
                </span>
                <div className="flex gap-2">
                  <button 
                    className="btn btn-secondary btn-sm"
                    disabled={pagination.page <= 1}
                    onClick={() => setPagination(prev => ({ ...prev, page: prev.page - 1 }))}
                  >
                    ← Trước
                  </button>
                  <button 
                    className="btn btn-secondary btn-sm"
                    disabled={pagination.page >= pagination.pages}
                    onClick={() => setPagination(prev => ({ ...prev, page: prev.page + 1 }))}
                  >
                    Sau →
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </div>

      {/* Edit Modal */}
      {showModal && editUser && (
        <Modal title="Chỉnh sửa người dùng" onClose={() => setShowModal(false)}>
          <div className="form-group">
            <label className="form-label">Họ tên</label>
            <input
              type="text"
              className="form-input"
              value={editUser.fullname}
              onChange={(e) => setEditUser({ ...editUser, fullname: e.target.value })}
            />
          </div>
          <div className="form-group">
            <label className="form-label">Email</label>
            <input
              type="email"
              className="form-input"
              value={editUser.email}
              onChange={(e) => setEditUser({ ...editUser, email: e.target.value })}
            />
          </div>
          <div className="grid-2">
            <div className="form-group">
              <label className="form-label">Điểm</label>
              <input
                type="number"
                className="form-input"
                value={editUser.points}
                onChange={(e) => setEditUser({ ...editUser, points: parseInt(e.target.value) })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Cấp độ</label>
              <input
                type="number"
                className="form-input"
                value={editUser.level}
                onChange={(e) => setEditUser({ ...editUser, level: parseInt(e.target.value) })}
              />
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">Vai trò</label>
            <select
              className="form-select"
              value={editUser.role}
              onChange={(e) => setEditUser({ ...editUser, role: e.target.value })}
            >
              <option value="user">User</option>
              <option value="admin">Admin</option>
            </select>
          </div>
          <div className="modal-footer">
            <button className="btn btn-secondary" onClick={() => setShowModal(false)}>Hủy</button>
            <button className="btn btn-primary" onClick={handleSave}>Lưu</button>
          </div>
        </Modal>
      )}
    </div>
  )
}

export default Users
