import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useState, useEffect } from 'react'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Users from './pages/Users'
import Activities from './pages/Activities'
import Badges from './pages/Badges'
import Challenges from './pages/Challenges'
import Notifications from './pages/Notifications'
import Layout from './components/Layout'

function App() {
  const [token, setToken] = useState(localStorage.getItem('adminToken'))
  const [user, setUser] = useState(null)

  useEffect(() => {
    if (token) {
      // Verify token
      fetch('/api/auth/me', {
        headers: { 'Authorization': `Bearer ${token}` }
      })
        .then(res => res.json())
        .then(data => {
          if (data.user && data.user.role === 'admin') {
            setUser(data.user)
          } else {
            handleLogout()
          }
        })
        .catch(() => handleLogout())
    }
  }, [token])

  const handleLogin = (newToken, userData) => {
    localStorage.setItem('adminToken', newToken)
    setToken(newToken)
    setUser(userData)
  }

  const handleLogout = () => {
    localStorage.removeItem('adminToken')
    setToken(null)
    setUser(null)
  }

  if (!token) {
    return <Login onLogin={handleLogin} />
  }

  return (
    <BrowserRouter>
      <Layout user={user} onLogout={handleLogout}>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/users" element={<Users />} />
          <Route path="/activities" element={<Activities />} />
          <Route path="/badges" element={<Badges />} />
          <Route path="/challenges" element={<Challenges />} />
          <Route path="/notifications" element={<Notifications />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  )
}

export default App
