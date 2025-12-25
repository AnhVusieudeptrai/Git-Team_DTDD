import { useState, useEffect } from 'react'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts'
import api from '../utils/api'

const COLORS = ['#2E7D32', '#4CAF50', '#66BB6A', '#81C784', '#A5D6A7', '#C8E6C9']
const CATEGORY_NAMES = {
  transport: 'Giao thông',
  energy: 'Năng lượng',
  water: 'Nước',
  waste: 'Rác thải',
  green: 'Xanh',
  consumption: 'Tiêu dùng'
}

function Dashboard() {
  const [stats, setStats] = useState(null)
  const [charts, setCharts] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      const [statsRes, chartsRes] = await Promise.all([
        api.get('/admin/stats'),
        api.get('/admin/stats/charts')
      ])
      setStats(statsRes.data)
      setCharts(chartsRes.data)
    } catch (err) {
      console.error('Error loading dashboard:', err)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return <div className="loading"><div className="spinner"></div></div>
  }

  const categoryData = charts?.activitiesByCategory?.map(item => ({
    name: CATEGORY_NAMES[item._id] || item._id,
    value: item.count
  })) || []

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Dashboard</h1>
          <p className="page-subtitle">Tổng quan hệ thống EcoTrack</p>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="icon green">👥</div>
          <div className="value">{stats?.totalUsers || 0}</div>
          <div className="label">Tổng người dùng</div>
        </div>
        <div className="stat-card">
          <div className="icon blue">🌱</div>
          <div className="value">{stats?.totalActivities || 0}</div>
          <div className="label">Hoạt động xanh</div>
        </div>
        <div className="stat-card">
          <div className="icon orange">✅</div>
          <div className="value">{stats?.totalCompleted || 0}</div>
          <div className="label">Lượt hoàn thành</div>
        </div>
        <div className="stat-card">
          <div className="icon purple">⭐</div>
          <div className="value">{stats?.totalPoints?.toLocaleString() || 0}</div>
          <div className="label">Tổng điểm</div>
        </div>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="icon green">🏆</div>
          <div className="value">{stats?.totalBadges || 0}</div>
          <div className="label">Huy hiệu</div>
        </div>
        <div className="stat-card">
          <div className="icon blue">🎯</div>
          <div className="value">{stats?.activeChallenges || 0}</div>
          <div className="label">Thử thách đang diễn ra</div>
        </div>
        <div className="stat-card">
          <div className="icon orange">📈</div>
          <div className="value">{stats?.newUsersThisWeek || 0}</div>
          <div className="label">Người dùng mới (7 ngày)</div>
        </div>
        <div className="stat-card">
          <div className="icon purple">📅</div>
          <div className="value">{stats?.activitiesToday || 0}</div>
          <div className="label">Hoạt động hôm nay</div>
        </div>
      </div>

      {/* Charts */}
      <div className="grid-2">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">Hoạt động 7 ngày qua</h3>
          </div>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={charts?.activitiesPerDay || []}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis 
                dataKey="date" 
                tickFormatter={(value) => {
                  const date = new Date(value)
                  return `${date.getDate()}/${date.getMonth() + 1}`
                }}
              />
              <YAxis />
              <Tooltip 
                labelFormatter={(value) => {
                  const date = new Date(value)
                  return date.toLocaleDateString('vi-VN')
                }}
              />
              <Bar dataKey="count" fill="#4CAF50" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="card">
          <div className="card-header">
            <h3 className="card-title">Phân loại hoạt động</h3>
          </div>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={categoryData}
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={100}
                paddingAngle={2}
                dataKey="value"
                label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
              >
                {categoryData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  )
}

export default Dashboard
