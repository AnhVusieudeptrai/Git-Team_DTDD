import { NavLink } from 'react-router-dom'

function Layout({ children, user, onLogout }) {
  return (
    <div className="app-container">
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="sidebar-logo">
            <span>🌿</span>
            EcoTrack Admin
          </div>
        </div>
        
        <nav>
          <ul className="sidebar-nav">
            <li>
              <NavLink to="/" end>
                <span className="icon">📊</span>
                Dashboard
              </NavLink>
            </li>
            <li>
              <NavLink to="/users">
                <span className="icon">👥</span>
                Người dùng
              </NavLink>
            </li>
            <li>
              <NavLink to="/activities">
                <span className="icon">🌱</span>
                Hoạt động
              </NavLink>
            </li>
            <li>
              <NavLink to="/badges">
                <span className="icon">🏆</span>
                Huy hiệu
              </NavLink>
            </li>
            <li>
              <NavLink to="/challenges">
                <span className="icon">🎯</span>
                Thử thách
              </NavLink>
            </li>
            <li>
              <NavLink to="/notifications">
                <span className="icon">🔔</span>
                Thông báo
              </NavLink>
            </li>
          </ul>
        </nav>

        <div style={{ 
          position: 'absolute', 
          bottom: 0, 
          left: 0, 
          right: 0, 
          padding: '16px 20px',
          borderTop: '1px solid var(--border)',
          background: 'var(--surface)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '12px' }}>
            <div style={{ 
              width: '40px', 
              height: '40px', 
              borderRadius: '50%', 
              background: 'var(--primary-bg)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '18px'
            }}>
              👤
            </div>
            <div>
              <div style={{ fontWeight: '600', fontSize: '14px' }}>{user?.fullname || 'Admin'}</div>
              <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>{user?.email}</div>
            </div>
          </div>
          <button 
            onClick={onLogout}
            className="btn btn-secondary"
            style={{ width: '100%' }}
          >
            🚪 Đăng xuất
          </button>
        </div>
      </aside>

      <main className="main-content">
        {children}
      </main>
    </div>
  )
}

export default Layout
