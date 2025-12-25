import { useState, useEffect } from 'react'
import api from '../utils/api'

function Notifications() {
  const [title, setTitle] = useState('')
  const [body, setBody] = useState('')
  const [sending, setSending] = useState(false)
  const [result, setResult] = useState(null)
  const [users, setUsers] = useState([])
  const [selectedUsers, setSelectedUsers] = useState([])
  const [sendToAll, setSendToAll] = useState(true)
  const [loadingUsers, setLoadingUsers] = useState(false)

  useEffect(() => {
    if (!sendToAll) {
      loadUsers()
    }
  }, [sendToAll])

  const loadUsers = async () => {
    setLoadingUsers(true)
    try {
      const res = await api.get('/admin/users', { params: { limit: 100 } })
      setUsers(res.data.users.filter(u => u.fcmToken))
    } catch (err) {
      console.error('Error loading users:', err)
    } finally {
      setLoadingUsers(false)
    }
  }

  const handleSend = async (e) => {
    e.preventDefault()
    
    if (!title.trim() || !body.trim()) {
      alert('Vui lòng nhập tiêu đề và nội dung')
      return
    }

    setSending(true)
    setResult(null)

    try {
      const payload = { title, body }
      if (!sendToAll && selectedUsers.length > 0) {
        payload.targetUsers = selectedUsers
      }

      const res = await api.post('/admin/notifications/send', payload)
      setResult({
        success: true,
        message: `Đã gửi thành công ${res.data.success} thông báo${res.data.failure > 0 ? `, ${res.data.failure} thất bại` : ''}`
      })
      setTitle('')
      setBody('')
      setSelectedUsers([])
    } catch (err) {
      setResult({
        success: false,
        message: err.response?.data?.error || 'Lỗi khi gửi thông báo'
      })
    } finally {
      setSending(false)
    }
  }

  const toggleUser = (userId) => {
    setSelectedUsers(prev => 
      prev.includes(userId) 
        ? prev.filter(id => id !== userId)
        : [...prev, userId]
    )
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Gửi thông báo</h1>
          <p className="page-subtitle">Gửi push notification đến người dùng</p>
        </div>
      </div>

      <div className="grid-2">
        <div className="card">
          <h3 className="card-title mb-4">Soạn thông báo</h3>
          
          <form onSubmit={handleSend}>
            <div className="form-group">
              <label className="form-label">Tiêu đề *</label>
              <input
                type="text"
                className="form-input"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="VD: Thử thách mới!"
                maxLength={100}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Nội dung *</label>
              <textarea
                className="form-textarea"
                value={body}
                onChange={(e) => setBody(e.target.value)}
                placeholder="Nội dung thông báo..."
                maxLength={500}
              />
            </div>

            <div className="form-group">
              <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                <input
                  type="checkbox"
                  checked={sendToAll}
                  onChange={(e) => setSendToAll(e.target.checked)}
                />
                Gửi đến tất cả người dùng
              </label>
            </div>

            {result && (
              <div style={{
                padding: '12px',
                borderRadius: '8px',
                marginBottom: '16px',
                background: result.success ? 'var(--primary-bg)' : '#FFEBEE',
                color: result.success ? 'var(--primary)' : 'var(--error)'
              }}>
                {result.success ? '✅' : '❌'} {result.message}
              </div>
            )}

            <button 
              type="submit" 
              className="btn btn-primary"
              disabled={sending}
              style={{ width: '100%' }}
            >
              {sending ? '⏳ Đang gửi...' : '🔔 Gửi thông báo'}
            </button>
          </form>
        </div>

        {!sendToAll && (
          <div className="card">
            <h3 className="card-title mb-4">
              Chọn người nhận ({selectedUsers.length} đã chọn)
            </h3>
            
            {loadingUsers ? (
              <div className="loading"><div className="spinner"></div></div>
            ) : users.length === 0 ? (
              <div className="empty-state">
                <div className="icon">📱</div>
                <p>Không có người dùng nào có FCM token</p>
              </div>
            ) : (
              <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
                {users.map(user => (
                  <label 
                    key={user._id}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '12px',
                      padding: '12px',
                      borderBottom: '1px solid var(--border)',
                      cursor: 'pointer'
                    }}
                  >
                    <input
                      type="checkbox"
                      checked={selectedUsers.includes(user._id)}
                      onChange={() => toggleUser(user._id)}
                    />
                    <div style={{
                      width: '36px',
                      height: '36px',
                      borderRadius: '50%',
                      background: 'var(--primary-bg)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center'
                    }}>
                      👤
                    </div>
                    <div>
                      <div style={{ fontWeight: '500' }}>{user.fullname}</div>
                      <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                        @{user.username}
                      </div>
                    </div>
                  </label>
                ))}
              </div>
            )}
          </div>
        )}

        {sendToAll && (
          <div className="card">
            <h3 className="card-title mb-4">Xem trước</h3>
            
            <div style={{
              background: 'var(--background)',
              borderRadius: '12px',
              padding: '16px',
              border: '1px solid var(--border)'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '12px' }}>
                <div style={{
                  width: '40px',
                  height: '40px',
                  borderRadius: '10px',
                  background: 'var(--primary)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '20px'
                }}>
                  🌿
                </div>
                <div>
                  <div style={{ fontWeight: '600', fontSize: '14px' }}>EcoTrack</div>
                  <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>now</div>
                </div>
              </div>
              <div style={{ fontWeight: '600', marginBottom: '4px' }}>
                {title || 'Tiêu đề thông báo'}
              </div>
              <div style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
                {body || 'Nội dung thông báo sẽ hiển thị ở đây...'}
              </div>
            </div>

            <div style={{ marginTop: '16px', padding: '12px', background: '#FFF8E1', borderRadius: '8px' }}>
              <div style={{ fontWeight: '500', marginBottom: '4px' }}>💡 Lưu ý</div>
              <ul style={{ fontSize: '13px', color: 'var(--text-secondary)', paddingLeft: '16px', margin: 0 }}>
                <li>Thông báo sẽ được gửi đến tất cả người dùng có FCM token</li>
                <li>Người dùng cần cài đặt app và cho phép thông báo</li>
                <li>Thông báo có thể mất vài giây để đến tất cả thiết bị</li>
              </ul>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default Notifications
