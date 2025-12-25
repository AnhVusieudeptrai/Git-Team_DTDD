// EcoTrack Admin Dashboard - Full CRUD
const API_URL = '/api';
let token = localStorage.getItem('adminToken');
let currentPage = 'dashboard';
let currentUser = null;

// Data stores
let usersData = [];
let activitiesData = [];
let badgesData = [];
let challengesData = [];

// Constants
const CATEGORIES = [
  { value: 'transport', label: 'Giao thông', icon: '🚴' },
  { value: 'energy', label: 'Năng lượng', icon: '💡' },
  { value: 'water', label: 'Nước', icon: '💧' },
  { value: 'waste', label: 'Rác thải', icon: '♻️' },
  { value: 'green', label: 'Xanh', icon: '🌱' },
  { value: 'consumption', label: 'Tiêu dùng', icon: '🛍️' }
];

const BADGE_TYPES = [
  { value: 'streak', label: 'Streak (Chuỗi ngày)' },
  { value: 'points', label: 'Điểm' },
  { value: 'activities', label: 'Số hoạt động' },
  { value: 'challenge', label: 'Thử thách' },
  { value: 'special', label: 'Đặc biệt' }
];

const RARITIES = [
  { value: 'common', label: 'Thường' },
  { value: 'rare', label: 'Hiếm' },
  { value: 'epic', label: 'Sử thi' },
  { value: 'legendary', label: 'Huyền thoại' }
];

const ICONS = ['🚴','🚌','🚶','💡','💻','❄️','💧','🚿','♻️','🗑️','🌱','🌳','🛒','📦','☕','🥗','🏆','🥇','🥈','🥉','⭐','🌟','💎','👑','🔥','⚡','🎯','🎖️','🏅','💪','🌍'];

// Initialize
document.addEventListener('DOMContentLoaded', () => {
  if (token) {
    verifyToken();
  } else {
    renderLogin();
  }
});


// API Helper
async function api(endpoint, options = {}) {
  const res = await fetch(`${API_URL}${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      ...options.headers
    },
    body: options.body ? JSON.stringify(options.body) : undefined
  });
  
  if (res.status === 401) {
    logout();
    throw new Error('Phiên đăng nhập hết hạn');
  }
  
  const data = await res.json();
  if (!res.ok) throw new Error(data.error || data.message || 'Có lỗi xảy ra');
  return data;
}

// Auth Functions
async function verifyToken() {
  try {
    const data = await api('/auth/me');
    if (data.user && data.user.role === 'admin') {
      currentUser = data.user;
      renderApp();
    } else {
      logout();
    }
  } catch {
    logout();
  }
}

async function login(username, password) {
  const data = await api('/auth/login', {
    method: 'POST',
    body: { username, password }
  });
  
  if (data.user.role !== 'admin') {
    throw new Error('Bạn không có quyền truy cập trang quản trị');
  }
  
  token = data.token;
  currentUser = data.user;
  localStorage.setItem('adminToken', token);
  renderApp();
}

function logout() {
  localStorage.removeItem('adminToken');
  token = null;
  currentUser = null;
  renderLogin();
}

// Render Functions
function renderLogin() {
  document.getElementById('app').innerHTML = `
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <div class="login-logo">🌿</div>
          <h1 class="login-title">EcoTrack Admin</h1>
          <p class="login-subtitle">Đăng nhập để quản lý hệ thống</p>
        </div>
        <div id="loginError" class="alert alert-error" style="display:none"></div>
        <form id="loginForm">
          <div class="form-group">
            <label class="form-label">Tên đăng nhập</label>
            <input type="text" id="username" class="form-input" placeholder="admin" required>
          </div>
          <div class="form-group">
            <label class="form-label">Mật khẩu</label>
            <input type="password" id="password" class="form-input" placeholder="••••••" required>
          </div>
          <button type="submit" class="btn btn-primary btn-block" id="loginBtn">Đăng nhập</button>
        </form>
      </div>
    </div>
  `;
  
  document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = document.getElementById('loginBtn');
    const error = document.getElementById('loginError');
    
    btn.disabled = true;
    btn.textContent = 'Đang đăng nhập...';
    error.style.display = 'none';
    
    try {
      await login(
        document.getElementById('username').value,
        document.getElementById('password').value
      );
    } catch (err) {
      error.textContent = err.message;
      error.style.display = 'block';
      btn.disabled = false;
      btn.textContent = 'Đăng nhập';
    }
  });
}


function renderApp() {
  document.getElementById('app').innerHTML = `
    <div class="app-container">
      <aside class="sidebar">
        <div class="sidebar-header">
          <div class="sidebar-logo"><span>🌿</span> EcoTrack</div>
        </div>
        <nav>
          <ul class="sidebar-nav">
            <li><a href="#" data-page="dashboard" class="active"><span>📊</span> Dashboard</a></li>
            <li><a href="#" data-page="users"><span>👥</span> Người dùng</a></li>
            <li><a href="#" data-page="activities"><span>🌱</span> Hoạt động</a></li>
            <li><a href="#" data-page="badges"><span>🏆</span> Huy hiệu</a></li>
            <li><a href="#" data-page="challenges"><span>🎯</span> Thử thách</a></li>
            <li><a href="#" data-page="notifications"><span>🔔</span> Thông báo</a></li>
          </ul>
        </nav>
        <div class="sidebar-footer">
          <div class="user-info">
            <span class="user-avatar">👤</span>
            <div class="user-details">
              <div class="user-name">${currentUser?.fullname || 'Admin'}</div>
              <div class="user-role">Quản trị viên</div>
            </div>
          </div>
          <button class="btn btn-secondary btn-block" onclick="logout()">🚪 Đăng xuất</button>
        </div>
      </aside>
      <main class="main-content">
        <div id="pageContent"></div>
      </main>
    </div>
    <div id="modal" class="modal-overlay" style="display:none"></div>
  `;
  
  // Navigation
  document.querySelectorAll('.sidebar-nav a').forEach(link => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      document.querySelectorAll('.sidebar-nav a').forEach(l => l.classList.remove('active'));
      link.classList.add('active');
      loadPage(link.dataset.page);
    });
  });
  
  loadPage('dashboard');
}

async function loadPage(page) {
  currentPage = page;
  const content = document.getElementById('pageContent');
  content.innerHTML = '<div class="loading"><div class="spinner"></div> Đang tải...</div>';
  
  try {
    switch(page) {
      case 'dashboard': await renderDashboard(); break;
      case 'users': await renderUsers(); break;
      case 'activities': await renderActivities(); break;
      case 'badges': await renderBadges(); break;
      case 'challenges': await renderChallenges(); break;
      case 'notifications': await renderNotifications(); break;
    }
  } catch (err) {
    content.innerHTML = `<div class="alert alert-error">Lỗi: ${err.message}</div>`;
  }
}

// Dashboard
async function renderDashboard() {
  const [stats, charts] = await Promise.all([
    api('/admin/stats'),
    api('/admin/stats/charts')
  ]);
  
  document.getElementById('pageContent').innerHTML = `
    <div class="page-header">
      <h1 class="page-title">📊 Dashboard</h1>
      <p class="page-subtitle">Tổng quan hệ thống EcoTrack</p>
    </div>
    
    <div class="stats-grid">
      <div class="stat-card"><div class="stat-icon green">👥</div><div class="stat-value">${stats.totalUsers}</div><div class="stat-label">Người dùng</div></div>
      <div class="stat-card"><div class="stat-icon blue">🌱</div><div class="stat-value">${stats.totalActivities}</div><div class="stat-label">Hoạt động</div></div>
      <div class="stat-card"><div class="stat-icon orange">✅</div><div class="stat-value">${stats.totalCompleted}</div><div class="stat-label">Lượt hoàn thành</div></div>
      <div class="stat-card"><div class="stat-icon purple">⭐</div><div class="stat-value">${(stats.totalPoints || 0).toLocaleString()}</div><div class="stat-label">Tổng điểm</div></div>
    </div>
    
    <div class="stats-grid">
      <div class="stat-card"><div class="stat-icon green">🏆</div><div class="stat-value">${stats.totalBadges}</div><div class="stat-label">Huy hiệu</div></div>
      <div class="stat-card"><div class="stat-icon blue">🎯</div><div class="stat-value">${stats.activeChallenges}</div><div class="stat-label">Thử thách đang diễn ra</div></div>
      <div class="stat-card"><div class="stat-icon orange">📈</div><div class="stat-value">${stats.newUsersThisWeek}</div><div class="stat-label">User mới (7 ngày)</div></div>
      <div class="stat-card"><div class="stat-icon purple">📅</div><div class="stat-value">${stats.activitiesToday}</div><div class="stat-label">Hoạt động hôm nay</div></div>
    </div>
    
    <div class="card">
      <h3 class="card-title">📈 Hoạt động 7 ngày qua</h3>
      <div class="chart-container">
        ${charts.activitiesPerDay?.map(d => `
          <div class="chart-bar">
            <div class="bar" style="height: ${Math.max(d.count * 10, 5)}px"></div>
            <div class="bar-label">${new Date(d.date).getDate()}/${new Date(d.date).getMonth()+1}</div>
            <div class="bar-value">${d.count}</div>
          </div>
        `).join('') || '<p>Không có dữ liệu</p>'}
      </div>
    </div>
  `;
}


// Users Management
async function renderUsers() {
  const data = await api('/admin/users?limit=100');
  usersData = data.users;
  
  document.getElementById('pageContent').innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">👥 Quản lý người dùng</h1>
        <p class="page-subtitle">${data.pagination.total} người dùng</p>
      </div>
      <div class="header-actions">
        <input type="text" id="userSearch" class="form-input" placeholder="🔍 Tìm kiếm..." style="width:250px" onkeyup="filterUsers()">
      </div>
    </div>
    
    <div class="card">
      <div class="table-container">
        <table>
          <thead>
            <tr>
              <th>Người dùng</th>
              <th>Email</th>
              <th>Điểm</th>
              <th>Level</th>
              <th>Vai trò</th>
              <th>Trạng thái</th>
              <th>Ngày tạo</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody id="usersTable"></tbody>
        </table>
      </div>
    </div>
  `;
  
  renderUsersTable(usersData);
}

function renderUsersTable(users) {
  document.getElementById('usersTable').innerHTML = users.map(u => `
    <tr>
      <td>
        <div class="user-cell">
          <span class="avatar">${u.avatar ? `<img src="${u.avatar}" alt="">` : '👤'}</span>
          <div>
            <strong>${u.fullname}</strong>
            <small>@${u.username}</small>
          </div>
        </div>
      </td>
      <td>${u.email}</td>
      <td><strong>${u.points?.toLocaleString() || 0}</strong></td>
      <td>Lv.${u.level || 1}</td>
      <td><span class="badge ${u.role === 'admin' ? 'badge-info' : 'badge-default'}">${u.role === 'admin' ? '👑 Admin' : '👤 User'}</span></td>
      <td><span class="badge ${u.isLocked ? 'badge-error' : 'badge-success'}">${u.isLocked ? '🔒 Khóa' : '✅ Hoạt động'}</span></td>
      <td>${new Date(u.createdAt).toLocaleDateString('vi-VN')}</td>
      <td>
        <div class="action-buttons">
          <button class="btn-icon" onclick="viewUser('${u._id}')" title="Xem chi tiết">👁️</button>
          <button class="btn-icon" onclick="editUser('${u._id}')" title="Sửa">✏️</button>
          ${u.role !== 'admin' ? `
            <button class="btn-icon" onclick="toggleUserLock('${u._id}')" title="${u.isLocked ? 'Mở khóa' : 'Khóa'}">${u.isLocked ? '🔓' : '🔒'}</button>
            <button class="btn-icon" onclick="deleteUser('${u._id}')" title="Xóa">🗑️</button>
          ` : ''}
        </div>
      </td>
    </tr>
  `).join('');
}

function filterUsers() {
  const search = document.getElementById('userSearch').value.toLowerCase();
  const filtered = usersData.filter(u => 
    u.fullname.toLowerCase().includes(search) ||
    u.username.toLowerCase().includes(search) ||
    u.email.toLowerCase().includes(search)
  );
  renderUsersTable(filtered);
}

async function viewUser(id) {
  const data = await api(`/admin/users/${id}`);
  const u = data.user;
  const s = data.stats;
  
  showModal(`
    <div class="modal-header">
      <h3>👤 Chi tiết người dùng</h3>
      <button class="btn-icon" onclick="closeModal()">✕</button>
    </div>
    <div class="modal-body">
      <div class="user-detail-header">
        <span class="avatar-large">${u.avatar ? `<img src="${u.avatar}" alt="">` : '👤'}</span>
        <div>
          <h2>${u.fullname}</h2>
          <p>@${u.username}</p>
        </div>
      </div>
      
      <div class="detail-grid">
        <div class="detail-item"><label>Email</label><span>${u.email}</span></div>
        <div class="detail-item"><label>Vai trò</label><span class="badge ${u.role === 'admin' ? 'badge-info' : 'badge-default'}">${u.role}</span></div>
        <div class="detail-item"><label>Điểm</label><span><strong>${u.points?.toLocaleString() || 0}</strong></span></div>
        <div class="detail-item"><label>Level</label><span>Lv.${u.level || 1}</span></div>
        <div class="detail-item"><label>Trạng thái</label><span class="badge ${u.isLocked ? 'badge-error' : 'badge-success'}">${u.isLocked ? 'Đã khóa' : 'Hoạt động'}</span></div>
        <div class="detail-item"><label>Ngày tạo</label><span>${new Date(u.createdAt).toLocaleString('vi-VN')}</span></div>
      </div>
      
      <h4 style="margin-top:20px">📊 Thống kê</h4>
      <div class="stats-mini">
        <div class="stat-mini"><span class="stat-mini-value">${s.activitiesCount}</span><span class="stat-mini-label">Hoạt động</span></div>
        <div class="stat-mini"><span class="stat-mini-value">${s.badgesCount}</span><span class="stat-mini-label">Huy hiệu</span></div>
        <div class="stat-mini"><span class="stat-mini-value">${s.challengesJoined}</span><span class="stat-mini-label">Thử thách</span></div>
      </div>
    </div>
    <div class="modal-footer">
      <button class="btn btn-secondary" onclick="closeModal()">Đóng</button>
      <button class="btn btn-primary" onclick="editUser('${u._id}')">✏️ Chỉnh sửa</button>
    </div>
  `);
}

async function editUser(id) {
  const u = usersData.find(x => x._id === id) || (await api(`/admin/users/${id}`)).user;
  
  showModal(`
    <div class="modal-header">
      <h3>✏️ Chỉnh sửa người dùng</h3>
      <button class="btn-icon" onclick="closeModal()">✕</button>
    </div>
    <form id="editUserForm">
      <div class="modal-body">
        <div class="form-group">
          <label class="form-label">Họ tên</label>
          <input type="text" class="form-input" name="fullname" value="${u.fullname}" required>
        </div>
        <div class="form-group">
          <label class="form-label">Email</label>
          <input type="email" class="form-input" name="email" value="${u.email}" required>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Điểm</label>
            <input type="number" class="form-input" name="points" value="${u.points || 0}">
          </div>
          <div class="form-group">
            <label class="form-label">Level</label>
            <input type="number" class="form-input" name="level" value="${u.level || 1}" min="1">
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">Vai trò</label>
          <select class="form-select" name="role">
            <option value="user" ${u.role === 'user' ? 'selected' : ''}>User</option>
            <option value="admin" ${u.role === 'admin' ? 'selected' : ''}>Admin</option>
          </select>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" onclick="closeModal()">Hủy</button>
        <button type="submit" class="btn btn-primary">💾 Lưu thay đổi</button>
      </div>
    </form>
  `);
  
  document.getElementById('editUserForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = new FormData(e.target);
    try {
      await api(`/admin/users/${id}`, {
        method: 'PUT',
        body: Object.fromEntries(form)
      });
      closeModal();
      showToast('Cập nhật thành công!', 'success');
      renderUsers();
    } catch (err) {
      showToast(err.message, 'error');
    }
  });
}

async function toggleUserLock(id) {
  const u = usersData.find(x => x._id === id);
  if (!confirm(`Bạn có chắc muốn ${u.isLocked ? 'mở khóa' : 'khóa'} tài khoản "${u.fullname}"?`)) return;
  
  try {
    await api(`/admin/users/${id}/toggle-lock`, { method: 'POST' });
    showToast(u.isLocked ? 'Đã mở khóa tài khoản' : 'Đã khóa tài khoản', 'success');
    renderUsers();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function deleteUser(id) {
  const u = usersData.find(x => x._id === id);
  if (!confirm(`⚠️ Bạn có chắc muốn XÓA người dùng "${u.fullname}"?\n\nHành động này không thể hoàn tác!`)) return;
  
  try {
    await api(`/admin/users/${id}`, { method: 'DELETE' });
    showToast('Đã xóa người dùng', 'success');
    renderUsers();
  } catch (err) {
    showToast(err.message, 'error');
  }
}


// Activities Management
async function renderActivities() {
  const data = await api('/admin/activities');
  activitiesData = data.activities;
  
  document.getElementById('pageContent').innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">🌱 Quản lý hoạt động</h1>
        <p class="page-subtitle">${activitiesData.length} hoạt động xanh</p>
      </div>
      <div class="header-actions">
        <input type="text" id="activitySearch" class="form-input" placeholder="🔍 Tìm kiếm..." style="width:200px" onkeyup="searchActivities()">
        <button class="btn btn-primary" onclick="createActivity()">➕ Thêm hoạt động</button>
      </div>
    </div>
    
    <div class="filter-bar">
      <button class="btn btn-sm ${!window.activityFilter ? 'btn-primary' : 'btn-secondary'}" onclick="filterActivities('')">Tất cả</button>
      ${CATEGORIES.map(c => `
        <button class="btn btn-sm ${window.activityFilter === c.value ? 'btn-primary' : 'btn-secondary'}" onclick="filterActivities('${c.value}')">${c.icon} ${c.label}</button>
      `).join('')}
    </div>
    
    <div class="card">
      <div class="table-container">
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
          <tbody id="activitiesTable"></tbody>
        </table>
      </div>
    </div>
  `;
  
  renderActivitiesTable(activitiesData);
}

function searchActivities() {
  const search = document.getElementById('activitySearch').value.toLowerCase();
  let filtered = activitiesData.filter(a => 
    a.name.toLowerCase().includes(search) ||
    (a.description && a.description.toLowerCase().includes(search))
  );
  if (window.activityFilter) {
    filtered = filtered.filter(a => a.category === window.activityFilter);
  }
  renderActivitiesTable(filtered);
}

function renderActivitiesTable(activities) {
  document.getElementById('activitiesTable').innerHTML = activities.map(a => {
    const cat = CATEGORIES.find(c => c.value === a.category) || {};
    return `
      <tr>
        <td style="font-size:28px">${a.icon || '🌱'}</td>
        <td>
          <strong>${a.name}</strong>
          ${a.description ? `<br><small class="text-muted">${a.description}</small>` : ''}
        </td>
        <td><span class="badge badge-success">${cat.icon || ''} ${cat.label || a.category}</span></td>
        <td><strong class="text-primary">+${a.points}</strong></td>
        <td><span class="badge ${a.isActive !== false ? 'badge-success' : 'badge-error'}">${a.isActive !== false ? '✅ Hoạt động' : '❌ Tắt'}</span></td>
        <td>
          <div class="action-buttons">
            <button class="btn-icon" onclick="viewActivity('${a._id}')" title="Xem chi tiết">👁️</button>
            <button class="btn-icon" onclick="editActivity('${a._id}')" title="Sửa">✏️</button>
            <button class="btn-icon" onclick="toggleActivity('${a._id}')" title="${a.isActive !== false ? 'Tắt' : 'Bật'}">${a.isActive !== false ? '🔴' : '🟢'}</button>
            <button class="btn-icon" onclick="deleteActivity('${a._id}')" title="Xóa">🗑️</button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

function viewActivity(id) {
  const a = activitiesData.find(x => x._id === id);
  const cat = CATEGORIES.find(c => c.value === a.category) || {};
  
  showModal(`
    <div class="modal-header">
      <h3>🌱 Chi tiết hoạt động</h3>
      <button class="btn-icon" onclick="closeModal()">✕</button>
    </div>
    <div class="modal-body">
      <div class="user-detail-header">
        <span class="avatar-large">${a.icon || '🌱'}</span>
        <div>
          <h2>${a.name}</h2>
          <p>${cat.icon || ''} ${cat.label || a.category}</p>
        </div>
      </div>
      
      <div class="detail-grid">
        <div class="detail-item"><label>Điểm thưởng</label><span class="text-primary"><strong>+${a.points} điểm</strong></span></div>
        <div class="detail-item"><label>Trạng thái</label><span class="badge ${a.isActive !== false ? 'badge-success' : 'badge-error'}">${a.isActive !== false ? '✅ Hoạt động' : '❌ Tắt'}</span></div>
        <div class="detail-item" style="grid-column: span 2"><label>Mô tả</label><span>${a.description || 'Không có mô tả'}</span></div>
        <div class="detail-item"><label>ID</label><span style="font-family: monospace; font-size: 12px">${a._id}</span></div>
        <div class="detail-item"><label>Ngày tạo</label><span>${a.createdAt ? new Date(a.createdAt).toLocaleString('vi-VN') : 'N/A'}</span></div>
      </div>
    </div>
    <div class="modal-footer">
      <button class="btn btn-secondary" onclick="closeModal()">Đóng</button>
      <button class="btn btn-primary" onclick="editActivity('${a._id}')">✏️ Chỉnh sửa</button>
    </div>
  `);
}

function filterActivities(category) {
  window.activityFilter = category;
  const search = document.getElementById('activitySearch')?.value?.toLowerCase() || '';
  let filtered = activitiesData;
  if (search) {
    filtered = filtered.filter(a => 
      a.name.toLowerCase().includes(search) ||
      (a.description && a.description.toLowerCase().includes(search))
    );
  }
  if (category) {
    filtered = filtered.filter(a => a.category === category);
  }
  renderActivitiesTable(filtered);
  
  // Update filter buttons
  document.querySelectorAll('.filter-bar .btn').forEach(btn => {
    btn.classList.remove('btn-primary');
    btn.classList.add('btn-secondary');
  });
  event.target.classList.remove('btn-secondary');
  event.target.classList.add('btn-primary');
}

function createActivity() {
  showActivityForm();
}

function editActivity(id) {
  const activity = activitiesData.find(a => a._id === id);
  showActivityForm(activity);
}

function showActivityForm(activity = null) {
  const isEdit = !!activity;
  
  showModal(`
    <div class="modal-header">
      <h3>${isEdit ? '✏️ Chỉnh sửa' : '➕ Thêm'} hoạt động</h3>
      <button class="btn-icon" onclick="closeModal()">✕</button>
    </div>
    <form id="activityForm">
      <div class="modal-body">
        <div class="form-group">
          <label class="form-label">Tên hoạt động *</label>
          <input type="text" class="form-input" name="name" value="${activity?.name || ''}" placeholder="VD: Đi xe đạp đi làm" required>
        </div>
        <div class="form-group">
          <label class="form-label">Mô tả</label>
          <textarea class="form-textarea" name="description" placeholder="Mô tả ngắn về hoạt động">${activity?.description || ''}</textarea>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Danh mục *</label>
            <select class="form-select" name="category" required>
              ${CATEGORIES.map(c => `<option value="${c.value}" ${activity?.category === c.value ? 'selected' : ''}>${c.icon} ${c.label}</option>`).join('')}
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">Điểm thưởng *</label>
            <input type="number" class="form-input" name="points" value="${activity?.points || 10}" min="1" required>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">Icon</label>
          <div class="icon-picker" id="iconPicker">
            ${ICONS.slice(0, 16).map(icon => `
              <button type="button" class="icon-btn ${activity?.icon === icon ? 'selected' : ''}" data-icon="${icon}">${icon}</button>
            `).join('')}
          </div>
          <input type="hidden" name="icon" id="selectedIcon" value="${activity?.icon || '🌱'}">
        </div>
        <div class="form-group">
          <label class="form-checkbox">
            <input type="checkbox" name="isActive" ${activity?.isActive !== false ? 'checked' : ''}>
            Kích hoạt hoạt động
          </label>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" onclick="closeModal()">Hủy</button>
        <button type="submit" class="btn btn-primary">${isEdit ? '💾 Cập nhật' : '➕ Thêm mới'}</button>
      </div>
    </form>
  `);
  
  // Icon picker
  document.querySelectorAll('.icon-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.icon-btn').forEach(b => b.classList.remove('selected'));
      btn.classList.add('selected');
      document.getElementById('selectedIcon').value = btn.dataset.icon;
    });
  });
  
  document.getElementById('activityForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = new FormData(e.target);
    const data = {
      name: form.get('name'),
      description: form.get('description'),
      category: form.get('category'),
      points: parseInt(form.get('points')),
      icon: form.get('icon'),
      isActive: form.has('isActive')
    };
    
    try {
      if (isEdit) {
        await api(`/admin/activities/${activity._id}`, { method: 'PUT', body: data });
        showToast('Cập nhật thành công!', 'success');
      } else {
        await api('/admin/activities', { method: 'POST', body: data });
        showToast('Thêm hoạt động thành công!', 'success');
      }
      closeModal();
      renderActivities();
    } catch (err) {
      showToast(err.message, 'error');
    }
  });
}

async function toggleActivity(id) {
  const activity = activitiesData.find(a => a._id === id);
  try {
    await api(`/admin/activities/${id}`, {
      method: 'PUT',
      body: { isActive: activity.isActive === false }
    });
    showToast(activity.isActive === false ? 'Đã bật hoạt động' : 'Đã tắt hoạt động', 'success');
    renderActivities();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function deleteActivity(id) {
  const activity = activitiesData.find(a => a._id === id);
  if (!confirm(`Bạn có chắc muốn xóa hoạt động "${activity.name}"?`)) return;
  
  try {
    await api(`/admin/activities/${id}`, { method: 'DELETE' });
    showToast('Đã xóa hoạt động', 'success');
    renderActivities();
  } catch (err) {
    showToast(err.message, 'error');
  }
}


// Badges Management
async function renderBadges() {
  const data = await api('/admin/badges');
  badgesData = data.badges;
  
  document.getElementById('pageContent').innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">🏆 Quản lý huy hiệu</h1>
        <p class="page-subtitle">${badgesData.length} huy hiệu</p>
      </div>
      <div class="header-actions">
        <input type="text" id="badgeSearch" class="form-input" placeholder="🔍 Tìm kiếm..." style="width:200px" onkeyup="searchBadges()">
        <button class="btn btn-primary" onclick="createBadge()">➕ Thêm huy hiệu</button>
      </div>
    </div>
    
    <div class="filter-bar">
      <button class="btn btn-sm ${!window.badgeFilter ? 'btn-primary' : 'btn-secondary'}" onclick="filterBadges('')">Tất cả</button>
      ${BADGE_TYPES.map(t => `
        <button class="btn btn-sm ${window.badgeFilter === t.value ? 'btn-primary' : 'btn-secondary'}" onclick="filterBadges('${t.value}')">${t.label}</button>
      `).join('')}
    </div>
    
    <div class="card">
      <div class="table-container">
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
          <tbody id="badgesTable"></tbody>
        </table>
      </div>
    </div>
  `;
  
  renderBadgesTable(badgesData);
}

function searchBadges() {
  const search = document.getElementById('badgeSearch').value.toLowerCase();
  let filtered = badgesData.filter(b => 
    b.name.toLowerCase().includes(search) ||
    b.description.toLowerCase().includes(search)
  );
  if (window.badgeFilter) {
    filtered = filtered.filter(b => b.type === window.badgeFilter);
  }
  renderBadgesTable(filtered);
}

function filterBadges(type) {
  window.badgeFilter = type;
  const search = document.getElementById('badgeSearch')?.value?.toLowerCase() || '';
  let filtered = badgesData;
  if (search) {
    filtered = filtered.filter(b => 
      b.name.toLowerCase().includes(search) ||
      b.description.toLowerCase().includes(search)
    );
  }
  if (type) {
    filtered = filtered.filter(b => b.type === type);
  }
  renderBadgesTable(filtered);
  
  // Update filter buttons
  document.querySelectorAll('.filter-bar .btn').forEach(btn => {
    btn.classList.remove('btn-primary');
    btn.classList.add('btn-secondary');
  });
  event.target.classList.remove('btn-secondary');
  event.target.classList.add('btn-primary');
}

function renderBadgesTable(badges) {
  document.getElementById('badgesTable').innerHTML = badges.map(b => {
    const type = BADGE_TYPES.find(t => t.value === b.type) || {};
    const rarity = RARITIES.find(r => r.value === b.rarity) || {};
    const reqText = b.type === 'streak' ? `${b.requirement} ngày` : 
                    b.type === 'points' ? `${b.requirement} điểm` : 
                    b.type === 'activities' ? `${b.requirement} hoạt động` : b.requirement;
    
    return `
      <tr>
        <td style="font-size:36px">${b.icon || '🏆'}</td>
        <td>
          <strong>${b.name}</strong>
          <br><small class="text-muted">${b.description}</small>
        </td>
        <td>${type.label || b.type}</td>
        <td><strong>${reqText}</strong></td>
        <td><span class="badge badge-${b.rarity}">${rarity.label || b.rarity}</span></td>
        <td><span class="badge ${b.isActive !== false ? 'badge-success' : 'badge-error'}">${b.isActive !== false ? '✅' : '❌'}</span></td>
        <td>
          <div class="action-buttons">
            <button class="btn-icon" onclick="viewBadge('${b._id}')" title="Xem chi tiết">👁️</button>
            <button class="btn-icon" onclick="editBadge('${b._id}')" title="Sửa">✏️</button>
            <button class="btn-icon" onclick="toggleBadge('${b._id}')" title="${b.isActive !== false ? 'Tắt' : 'Bật'}">${b.isActive !== false ? '🔴' : '🟢'}</button>
            <button class="btn-icon" onclick="deleteBadge('${b._id}')" title="Xóa">🗑️</button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

function viewBadge(id) {
  const b = badgesData.find(x => x._id === id);
  const type = BADGE_TYPES.find(t => t.value === b.type) || {};
  const rarity = RARITIES.find(r => r.value === b.rarity) || {};
  const reqText = b.type === 'streak' ? `${b.requirement} ngày liên tiếp` : 
                  b.type === 'points' ? `${b.requirement} điểm` : 
                  b.type === 'activities' ? `${b.requirement} hoạt động` : b.requirement;
  
  showModal(`
    <div class="modal-header">
      <h3>🏆 Chi tiết huy hiệu</h3>
      <button class="btn-icon" onclick="closeModal()">✕</button>
    </div>
    <div class="modal-body">
      <div class="user-detail-header">
        <span class="avatar-large">${b.icon || '🏆'}</span>
        <div>
          <h2>${b.name}</h2>
          <p><span class="badge badge-${b.rarity}">${rarity.label || b.rarity}</span></p>
        </div>
      </div>
      
      <div class="detail-grid">
        <div class="detail-item"><label>Loại</label><span>${type.label || b.type}</span></div>
        <div class="detail-item"><label>Yêu cầu</label><span><strong>${reqText}</strong></span></div>
        <div class="detail-item"><label>Trạng thái</label><span class="badge ${b.isActive !== false ? 'badge-success' : 'badge-error'}">${b.isActive !== false ? '✅ Hoạt động' : '❌ Tắt'}</span></div>
        <div class="detail-item"><label>Độ hiếm</label><span class="badge badge-${b.rarity}">${rarity.label || b.rarity}</span></div>
        <div class="detail-item" style="grid-column: span 2"><label>Mô tả</label><span>${b.description}</span></div>
        <div class="detail-item"><label>ID</label><span style="font-family: monospace; font-size: 12px">${b._id}</span></div>
      </div>
    </div>
    <div class="modal-footer">
      <button class="btn btn-secondary" onclick="closeModal()">Đóng</button>
      <button class="btn btn-primary" onclick="editBadge('${b._id}')">✏️ Chỉnh sửa</button>
    </div>
  `);
}

function createBadge() {
  showBadgeForm();
}

function editBadge(id) {
  const badge = badgesData.find(b => b._id === id);
  showBadgeForm(badge);
}

function showBadgeForm(badge = null) {
  const isEdit = !!badge;
  
  showModal(`
    <div class="modal-header">
      <h3>${isEdit ? '✏️ Chỉnh sửa' : '➕ Thêm'} huy hiệu</h3>
      <button class="btn-icon" onclick="closeModal()">✕</button>
    </div>
    <form id="badgeForm">
      <div class="modal-body">
        <div class="form-group">
          <label class="form-label">Tên huy hiệu *</label>
          <input type="text" class="form-input" name="name" value="${badge?.name || ''}" placeholder="VD: Người tiên phong" required>
        </div>
        <div class="form-group">
          <label class="form-label">Mô tả *</label>
          <textarea class="form-textarea" name="description" placeholder="Mô tả cách đạt được huy hiệu" required>${badge?.description || ''}</textarea>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Loại *</label>
            <select class="form-select" name="type" required>
              ${BADGE_TYPES.map(t => `<option value="${t.value}" ${badge?.type === t.value ? 'selected' : ''}>${t.label}</option>`).join('')}
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">Yêu cầu *</label>
            <input type="number" class="form-input" name="requirement" value="${badge?.requirement || 7}" min="1" required>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">Độ hiếm</label>
          <select class="form-select" name="rarity">
            ${RARITIES.map(r => `<option value="${r.value}" ${badge?.rarity === r.value ? 'selected' : ''}>${r.label}</option>`).join('')}
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">Icon</label>
          <div class="icon-picker" id="iconPicker">
            ${ICONS.map(icon => `
              <button type="button" class="icon-btn ${badge?.icon === icon ? 'selected' : ''}" data-icon="${icon}">${icon}</button>
            `).join('')}
          </div>
          <input type="hidden" name="icon" id="selectedIcon" value="${badge?.icon || '🏆'}">
        </div>
        <div class="form-group">
          <label class="form-checkbox">
            <input type="checkbox" name="isActive" ${badge?.isActive !== false ? 'checked' : ''}>
            Kích hoạt huy hiệu
          </label>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" onclick="closeModal()">Hủy</button>
        <button type="submit" class="btn btn-primary">${isEdit ? '💾 Cập nhật' : '➕ Thêm mới'}</button>
      </div>
    </form>
  `);
  
  // Icon picker
  document.querySelectorAll('.icon-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.icon-btn').forEach(b => b.classList.remove('selected'));
      btn.classList.add('selected');
      document.getElementById('selectedIcon').value = btn.dataset.icon;
    });
  });
  
  document.getElementById('badgeForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = new FormData(e.target);
    const data = {
      name: form.get('name'),
      description: form.get('description'),
      type: form.get('type'),
      requirement: parseInt(form.get('requirement')),
      rarity: form.get('rarity'),
      icon: form.get('icon'),
      isActive: form.has('isActive')
    };
    
    try {
      if (isEdit) {
        await api(`/admin/badges/${badge._id}`, { method: 'PUT', body: data });
        showToast('Cập nhật thành công!', 'success');
      } else {
        await api('/admin/badges', { method: 'POST', body: data });
        showToast('Thêm huy hiệu thành công!', 'success');
      }
      closeModal();
      renderBadges();
    } catch (err) {
      showToast(err.message, 'error');
    }
  });
}

async function deleteBadge(id) {
  const badge = badgesData.find(b => b._id === id);
  if (!confirm(`Bạn có chắc muốn xóa huy hiệu "${badge.name}"?`)) return;
  
  try {
    await api(`/admin/badges/${id}`, { method: 'DELETE' });
    showToast('Đã xóa huy hiệu', 'success');
    renderBadges();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function toggleBadge(id) {
  const badge = badgesData.find(b => b._id === id);
  try {
    await api(`/admin/badges/${id}`, {
      method: 'PUT',
      body: { isActive: badge.isActive === false }
    });
    showToast(badge.isActive === false ? 'Đã bật huy hiệu' : 'Đã tắt huy hiệu', 'success');
    renderBadges();
  } catch (err) {
    showToast(err.message, 'error');
  }
}


// Challenges Management
async function renderChallenges() {
  const data = await api('/admin/challenges');
  challengesData = data.challenges;
  
  document.getElementById('pageContent').innerHTML = `
    <div class="page-header">
      <div>
        <h1 class="page-title">🎯 Quản lý thử thách</h1>
        <p class="page-subtitle">${challengesData.length} thử thách</p>
      </div>
      <div class="header-actions">
        <input type="text" id="challengeSearch" class="form-input" placeholder="🔍 Tìm kiếm..." style="width:200px" onkeyup="searchChallenges()">
        <button class="btn btn-primary" onclick="createChallenge()">➕ Thêm thử thách</button>
      </div>
    </div>
    
    <div class="filter-bar">
      <button class="btn btn-sm ${!window.challengeFilter ? 'btn-primary' : 'btn-secondary'}" onclick="filterChallenges('')">Tất cả</button>
      <button class="btn btn-sm ${window.challengeFilter === 'active' ? 'btn-primary' : 'btn-secondary'}" onclick="filterChallenges('active')">🔥 Đang diễn ra</button>
      <button class="btn btn-sm ${window.challengeFilter === 'upcoming' ? 'btn-primary' : 'btn-secondary'}" onclick="filterChallenges('upcoming')">⏳ Sắp diễn ra</button>
      <button class="btn btn-sm ${window.challengeFilter === 'ended' ? 'btn-primary' : 'btn-secondary'}" onclick="filterChallenges('ended')">✅ Đã kết thúc</button>
    </div>
    
    <div class="card">
      <div class="table-container">
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
          <tbody id="challengesTable"></tbody>
        </table>
      </div>
    </div>
  `;
  
  renderChallengesTable(challengesData);
}

function searchChallenges() {
  const search = document.getElementById('challengeSearch').value.toLowerCase();
  let filtered = challengesData.filter(c => 
    c.name.toLowerCase().includes(search) ||
    c.description.toLowerCase().includes(search)
  );
  if (window.challengeFilter) {
    filtered = filterChallengesByStatus(filtered, window.challengeFilter);
  }
  renderChallengesTable(filtered);
}

function filterChallengesByStatus(challenges, status) {
  const now = new Date();
  return challenges.filter(c => {
    const start = new Date(c.startDate);
    const end = new Date(c.endDate);
    if (status === 'active') return c.isActive && now >= start && now <= end;
    if (status === 'upcoming') return c.isActive && now < start;
    if (status === 'ended') return now > end || !c.isActive;
    return true;
  });
}

function filterChallenges(status) {
  window.challengeFilter = status;
  const search = document.getElementById('challengeSearch')?.value?.toLowerCase() || '';
  let filtered = challengesData;
  if (search) {
    filtered = filtered.filter(c => 
      c.name.toLowerCase().includes(search) ||
      c.description.toLowerCase().includes(search)
    );
  }
  if (status) {
    filtered = filterChallengesByStatus(filtered, status);
  }
  renderChallengesTable(filtered);
  
  // Update filter buttons
  document.querySelectorAll('.filter-bar .btn').forEach(btn => {
    btn.classList.remove('btn-primary');
    btn.classList.add('btn-secondary');
  });
  event.target.classList.remove('btn-secondary');
  event.target.classList.add('btn-primary');
}

function renderChallengesTable(challenges) {
  document.getElementById('challengesTable').innerHTML = challenges.map(c => {
    const now = new Date();
    const start = new Date(c.startDate);
    const end = new Date(c.endDate);
    let status, statusClass;
    
    if (!c.isActive) { status = '❌ Tắt'; statusClass = 'badge-error'; }
    else if (now < start) { status = '⏳ Sắp diễn ra'; statusClass = 'badge-info'; }
    else if (now > end) { status = '✅ Đã kết thúc'; statusClass = 'badge-warning'; }
    else { status = '🔥 Đang diễn ra'; statusClass = 'badge-success'; }
    
    const targetTypes = { points: 'điểm', activities: 'hoạt động', category: 'danh mục', streak: 'ngày streak' };
    
    return `
      <tr>
        <td>
          <strong>${c.name}</strong>
          <br><small class="text-muted">${c.description}</small>
        </td>
        <td><span class="badge badge-info">${c.type === 'weekly' ? '📅 Tuần' : '📆 Tháng'}</span></td>
        <td>${c.targetValue} ${targetTypes[c.targetType] || c.targetType}</td>
        <td><strong class="text-primary">+${c.rewardPoints} điểm</strong></td>
        <td>
          ${start.toLocaleDateString('vi-VN')}<br>
          <small>→ ${end.toLocaleDateString('vi-VN')}</small>
        </td>
        <td><span class="badge ${statusClass}">${status}</span></td>
        <td>
          <div class="action-buttons">
            <button class="btn-icon" onclick="viewChallenge('${c._id}')" title="Xem chi tiết">👁️</button>
            <button class="btn-icon" onclick="editChallenge('${c._id}')" title="Sửa">✏️</button>
            <button class="btn-icon" onclick="toggleChallenge('${c._id}')" title="${c.isActive ? 'Tắt' : 'Bật'}">${c.isActive ? '🔴' : '🟢'}</button>
            <button class="btn-icon" onclick="deleteChallenge('${c._id}')" title="Xóa">🗑️</button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

function viewChallenge(id) {
  const c = challengesData.find(x => x._id === id);
  const now = new Date();
  const start = new Date(c.startDate);
  const end = new Date(c.endDate);
  let status, statusClass;
  
  if (!c.isActive) { status = '❌ Tắt'; statusClass = 'badge-error'; }
  else if (now < start) { status = '⏳ Sắp diễn ra'; statusClass = 'badge-info'; }
  else if (now > end) { status = '✅ Đã kết thúc'; statusClass = 'badge-warning'; }
  else { status = '🔥 Đang diễn ra'; statusClass = 'badge-success'; }
  
  const targetTypes = { points: 'điểm', activities: 'hoạt động', category: 'danh mục cụ thể', streak: 'ngày streak liên tiếp' };
  
  showModal(`
    <div class="modal-header">
      <h3>🎯 Chi tiết thử thách</h3>
      <button class="btn-icon" onclick="closeModal()">✕</button>
    </div>
    <div class="modal-body">
      <div class="user-detail-header">
        <span class="avatar-large">🎯</span>
        <div>
          <h2>${c.name}</h2>
          <p><span class="badge ${statusClass}">${status}</span></p>
        </div>
      </div>
      
      <div class="detail-grid">
        <div class="detail-item"><label>Loại thử thách</label><span class="badge badge-info">${c.type === 'weekly' ? '📅 Tuần' : '📆 Tháng'}</span></div>
        <div class="detail-item"><label>Loại mục tiêu</label><span>${targetTypes[c.targetType] || c.targetType}</span></div>
        <div class="detail-item"><label>Giá trị mục tiêu</label><span><strong>${c.targetValue} ${targetTypes[c.targetType] || ''}</strong></span></div>
        <div class="detail-item"><label>Phần thưởng</label><span class="text-primary"><strong>+${c.rewardPoints} điểm</strong></span></div>
        <div class="detail-item"><label>Ngày bắt đầu</label><span>${start.toLocaleDateString('vi-VN')}</span></div>
        <div class="detail-item"><label>Ngày kết thúc</label><span>${end.toLocaleDateString('vi-VN')}</span></div>
        <div class="detail-item" style="grid-column: span 2"><label>Mô tả</label><span>${c.description}</span></div>
        <div class="detail-item"><label>ID</label><span style="font-family: monospace; font-size: 12px">${c._id}</span></div>
      </div>
    </div>
    <div class="modal-footer">
      <button class="btn btn-secondary" onclick="closeModal()">Đóng</button>
      <button class="btn btn-primary" onclick="editChallenge('${c._id}')">✏️ Chỉnh sửa</button>
    </div>
  `);
}

function createChallenge() {
  const today = new Date().toISOString().split('T')[0];
  const nextWeek = new Date(Date.now() + 7*24*60*60*1000).toISOString().split('T')[0];
  showChallengeForm(null, today, nextWeek);
}

function editChallenge(id) {
  const challenge = challengesData.find(c => c._id === id);
  showChallengeForm(challenge);
}

function showChallengeForm(challenge = null, defaultStart = '', defaultEnd = '') {
  const isEdit = !!challenge;
  const startDate = challenge ? challenge.startDate.split('T')[0] : defaultStart;
  const endDate = challenge ? challenge.endDate.split('T')[0] : defaultEnd;
  
  showModal(`
    <div class="modal-header">
      <h3>${isEdit ? '✏️ Chỉnh sửa' : '➕ Thêm'} thử thách</h3>
      <button class="btn-icon" onclick="closeModal()">✕</button>
    </div>
    <form id="challengeForm">
      <div class="modal-body">
        <div class="form-group">
          <label class="form-label">Tên thử thách *</label>
          <input type="text" class="form-input" name="name" value="${challenge?.name || ''}" placeholder="VD: Tuần lễ xanh" required>
        </div>
        <div class="form-group">
          <label class="form-label">Mô tả *</label>
          <textarea class="form-textarea" name="description" placeholder="Mô tả thử thách" required>${challenge?.description || ''}</textarea>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Loại thử thách</label>
            <select class="form-select" name="type">
              <option value="weekly" ${challenge?.type === 'weekly' ? 'selected' : ''}>📅 Tuần</option>
              <option value="monthly" ${challenge?.type === 'monthly' ? 'selected' : ''}>📆 Tháng</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">Loại mục tiêu</label>
            <select class="form-select" name="targetType">
              <option value="points" ${challenge?.targetType === 'points' ? 'selected' : ''}>Điểm</option>
              <option value="activities" ${challenge?.targetType === 'activities' ? 'selected' : ''}>Số hoạt động</option>
              <option value="category" ${challenge?.targetType === 'category' ? 'selected' : ''}>Danh mục cụ thể</option>
              <option value="streak" ${challenge?.targetType === 'streak' ? 'selected' : ''}>Chuỗi ngày</option>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Giá trị mục tiêu *</label>
            <input type="number" class="form-input" name="targetValue" value="${challenge?.targetValue || 100}" min="1" required>
          </div>
          <div class="form-group">
            <label class="form-label">Điểm thưởng *</label>
            <input type="number" class="form-input" name="rewardPoints" value="${challenge?.rewardPoints || 50}" min="1" required>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Ngày bắt đầu *</label>
            <input type="date" class="form-input" name="startDate" value="${startDate}" required>
          </div>
          <div class="form-group">
            <label class="form-label">Ngày kết thúc *</label>
            <input type="date" class="form-input" name="endDate" value="${endDate}" required>
          </div>
        </div>
        <div class="form-group">
          <label class="form-checkbox">
            <input type="checkbox" name="isActive" ${challenge?.isActive !== false ? 'checked' : ''}>
            Kích hoạt thử thách
          </label>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" onclick="closeModal()">Hủy</button>
        <button type="submit" class="btn btn-primary">${isEdit ? '💾 Cập nhật' : '➕ Thêm mới'}</button>
      </div>
    </form>
  `);
  
  document.getElementById('challengeForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = new FormData(e.target);
    const data = {
      name: form.get('name'),
      description: form.get('description'),
      type: form.get('type'),
      targetType: form.get('targetType'),
      targetValue: parseInt(form.get('targetValue')),
      rewardPoints: parseInt(form.get('rewardPoints')),
      startDate: form.get('startDate'),
      endDate: form.get('endDate'),
      isActive: form.has('isActive')
    };
    
    try {
      if (isEdit) {
        await api(`/admin/challenges/${challenge._id}`, { method: 'PUT', body: data });
        showToast('Cập nhật thành công!', 'success');
      } else {
        await api('/admin/challenges', { method: 'POST', body: data });
        showToast('Thêm thử thách thành công!', 'success');
      }
      closeModal();
      renderChallenges();
    } catch (err) {
      showToast(err.message, 'error');
    }
  });
}

async function deleteChallenge(id) {
  const challenge = challengesData.find(c => c._id === id);
  if (!confirm(`Bạn có chắc muốn xóa thử thách "${challenge.name}"?`)) return;
  
  try {
    await api(`/admin/challenges/${id}`, { method: 'DELETE' });
    showToast('Đã xóa thử thách', 'success');
    renderChallenges();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function toggleChallenge(id) {
  const challenge = challengesData.find(c => c._id === id);
  try {
    await api(`/admin/challenges/${id}`, {
      method: 'PUT',
      body: { isActive: !challenge.isActive }
    });
    showToast(challenge.isActive ? 'Đã tắt thử thách' : 'Đã bật thử thách', 'success');
    renderChallenges();
  } catch (err) {
    showToast(err.message, 'error');
  }
}


// Notifications
async function renderNotifications() {
  document.getElementById('pageContent').innerHTML = `
    <div class="page-header">
      <h1 class="page-title">🔔 Gửi thông báo</h1>
      <p class="page-subtitle">Gửi push notification đến người dùng</p>
    </div>
    
    <div class="grid-2">
      <div class="card">
        <h3 class="card-title">📝 Soạn thông báo</h3>
        <form id="notificationForm">
          <div class="form-group">
            <label class="form-label">Tiêu đề *</label>
            <input type="text" class="form-input" name="title" placeholder="VD: Thử thách mới!" maxlength="100" required>
          </div>
          <div class="form-group">
            <label class="form-label">Nội dung *</label>
            <textarea class="form-textarea" name="body" placeholder="Nội dung thông báo..." maxlength="500" required></textarea>
          </div>
          <div class="form-group">
            <label class="form-checkbox">
              <input type="checkbox" name="sendToAll" checked>
              Gửi đến tất cả người dùng
            </label>
          </div>
          <div id="notificationResult"></div>
          <button type="submit" class="btn btn-primary btn-block" id="sendBtn">🔔 Gửi thông báo</button>
        </form>
      </div>
      
      <div class="card">
        <h3 class="card-title">📱 Xem trước</h3>
        <div class="notification-preview">
          <div class="preview-header">
            <span class="preview-icon">🌿</span>
            <div>
              <strong>EcoTrack</strong>
              <small>now</small>
            </div>
          </div>
          <div class="preview-title" id="previewTitle">Tiêu đề thông báo</div>
          <div class="preview-body" id="previewBody">Nội dung thông báo sẽ hiển thị ở đây...</div>
        </div>
        
        <div class="info-box">
          <strong>💡 Lưu ý:</strong>
          <ul>
            <li>Thông báo sẽ được gửi đến người dùng có FCM token</li>
            <li>Người dùng cần cài app và cho phép thông báo</li>
            <li>Có thể mất vài giây để đến tất cả thiết bị</li>
          </ul>
        </div>
      </div>
    </div>
  `;
  
  // Live preview
  const titleInput = document.querySelector('input[name="title"]');
  const bodyInput = document.querySelector('textarea[name="body"]');
  
  titleInput.addEventListener('input', () => {
    document.getElementById('previewTitle').textContent = titleInput.value || 'Tiêu đề thông báo';
  });
  
  bodyInput.addEventListener('input', () => {
    document.getElementById('previewBody').textContent = bodyInput.value || 'Nội dung thông báo sẽ hiển thị ở đây...';
  });
  
  document.getElementById('notificationForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = document.getElementById('sendBtn');
    const result = document.getElementById('notificationResult');
    
    btn.disabled = true;
    btn.textContent = '⏳ Đang gửi...';
    result.innerHTML = '';
    
    const form = new FormData(e.target);
    
    try {
      const data = await api('/admin/notifications/send', {
        method: 'POST',
        body: {
          title: form.get('title'),
          body: form.get('body')
        }
      });
      
      result.innerHTML = `<div class="alert alert-success">✅ Đã gửi ${data.success} thông báo${data.failure > 0 ? `, ${data.failure} thất bại` : ''}</div>`;
      e.target.reset();
      document.getElementById('previewTitle').textContent = 'Tiêu đề thông báo';
      document.getElementById('previewBody').textContent = 'Nội dung thông báo sẽ hiển thị ở đây...';
    } catch (err) {
      result.innerHTML = `<div class="alert alert-error">❌ ${err.message}</div>`;
    } finally {
      btn.disabled = false;
      btn.textContent = '🔔 Gửi thông báo';
    }
  });
}

// Modal Functions
function showModal(content) {
  const modal = document.getElementById('modal');
  modal.innerHTML = `<div class="modal">${content}</div>`;
  modal.style.display = 'flex';
  document.body.style.overflow = 'hidden';
}

function closeModal() {
  const modal = document.getElementById('modal');
  modal.style.display = 'none';
  document.body.style.overflow = '';
}

// Toast Notifications
function showToast(message, type = 'info') {
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `${type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️'} ${message}`;
  document.body.appendChild(toast);
  
  setTimeout(() => toast.classList.add('show'), 10);
  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}

// Close modal on overlay click
document.addEventListener('click', (e) => {
  if (e.target.classList.contains('modal-overlay')) {
    closeModal();
  }
});

// Close modal on Escape key
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') {
    closeModal();
  }
});
