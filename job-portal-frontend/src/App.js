import { useCallback, useEffect, useMemo, useState } from 'react';
import './App.css';
import NavTabs from './components/NavTabs';
import JobSection from './components/JobSection';
import ApplicationsSection from './components/ApplicationsSection';
import ProfileSection from './components/ProfileSection';
import EmployerSection from './components/EmployerSection';
import AdminSection from './components/AdminSection';
import ChatSection from './components/ChatSection';
import NotificationsSection from './components/NotificationsSection';
import { LoginSection, RegisterSection } from './components/AuthSection';

const API_BASE = process.env.REACT_APP_API_BASE || 'http://localhost:9090/api/v1';

const routeFromHash = () => (window.location.hash.replace('#/', '') || 'jobs');

function App() {
  const [activeTab, setActiveTab] = useState(routeFromHash());
  const [message, setMessage] = useState('');
  const [token, setToken] = useState(() => localStorage.getItem('jobPortalToken') || '');
  const [refreshToken, setRefreshToken] = useState(() => localStorage.getItem('jobPortalRefreshToken') || '');
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('jobPortalUser');
    return saved ? JSON.parse(saved) : null;
  });

  const [registerForm, setRegisterForm] = useState({ name: '', email: '', password: '', role: 'CANDIDATE' });
  const [loginForm, setLoginForm] = useState({ email: '', password: '' });
  const [jobForm, setJobForm] = useState({ title: '', company: '', location: '', salary: '', description: '' });
  const [profileForm, setProfileForm] = useState({ name: '', skills: '', experienceYears: 0, education: '', portfolioUrl: '' });
  const [search, setSearch] = useState({ keyword: '', location: '' });
  const [resumeFile, setResumeFile] = useState(null);
  const [resumePath, setResumePath] = useState('');

  const [jobs, setJobs] = useState([]);
  const [applications, setApplications] = useState([]);
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [pipeline, setPipeline] = useState([]);
  const [analytics, setAnalytics] = useState({});
  const [myJobs, setMyJobs] = useState([]);
  const [pipelineFilter, setPipelineFilter] = useState({ jobId: '', status: '' });
  const [notifications, setNotifications] = useState([]);
  const [notificationUnread, setNotificationUnread] = useState(0);
  const [chatUnread, setChatUnread] = useState(0);
  const [chatPeerId, setChatPeerId] = useState('');
  const [chatMessage, setChatMessage] = useState('');
  const [chatMessages, setChatMessages] = useState([]);

  const isAdmin = user?.role === 'ADMIN';
  const isEmployer = user?.role === 'EMPLOYER';
  const isCandidate = user?.role === 'CANDIDATE';
  const canPostJobs = isEmployer || isAdmin;
  const appCount = useMemo(() => (isCandidate ? applications.length : 0), [applications.length, isCandidate]);

  const setAndClearMessage = (text) => {
    setMessage(text);
    setTimeout(() => setMessage(''), 3000);
  };

  const setRoute = (tab) => {
    window.location.hash = `/${tab}`;
    setActiveTab(tab);
  };

  useEffect(() => {
    const onChange = () => setActiveTab(routeFromHash());
    window.addEventListener('hashchange', onChange);
    return () => window.removeEventListener('hashchange', onChange);
  }, []);

  const refreshSession = useCallback(async () => {
    if (!refreshToken) throw new Error('Session expired. Please login again.');

    const response = await fetch(`${API_BASE}/auth/refresh-token`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    });

    const payload = await response.json();
    if (!response.ok) throw new Error(payload?.message || 'Session expired. Please login again.');

    setToken(payload.token);
    setRefreshToken(payload.refreshToken);
    setUser(payload.user);
    localStorage.setItem('jobPortalToken', payload.token);
    localStorage.setItem('jobPortalRefreshToken', payload.refreshToken);
    localStorage.setItem('jobPortalUser', JSON.stringify(payload.user));
    return payload.token;
  }, [refreshToken]);

  const apiFetch = useCallback(async (path, options = {}, withAuth = false, retry = true, overrideToken = '') => {
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    if (withAuth && (overrideToken || token)) headers.Authorization = `Bearer ${overrideToken || token}`;

    const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
    const isJson = (response.headers.get('content-type') || '').includes('application/json');
    const payload = isJson ? await response.json() : null;

    if (!response.ok) {
      if (withAuth && response.status === 401 && retry) {
        const newToken = await refreshSession();
        return apiFetch(path, options, withAuth, false, newToken);
      }
      const details = payload?.errors ? Object.values(payload.errors).join(', ') : '';
      throw new Error(payload?.message || details || `Request failed (${response.status})`);
    }
    return payload;
  }, [refreshSession, token]);

  const loadJobs = useCallback(async (targetPage = 0, filters = search) => {
    const query = new URLSearchParams({ page: String(targetPage), size: '6' });
    if (filters.keyword.trim()) query.append('keyword', filters.keyword.trim());
    if (filters.location.trim()) query.append('location', filters.location.trim());

    const data = await apiFetch(`/jobs?${query.toString()}`);
    setJobs(data.content || []);
    setPage(data.number || 0);
    setTotalPages(data.totalPages || 0);
  }, [apiFetch, search]);

  const loadMyApplications = useCallback(async () => {
    if (!isCandidate) return;
    setApplications(await apiFetch('/applications/candidate/me', {}, true) || []);
  }, [apiFetch, isCandidate]);

  const loadNotifications = useCallback(async () => {
    if (!user) return;
    const [items, unreadRes, unreadChatRes] = await Promise.all([
      apiFetch('/notifications?page=0&size=30', {}, true),
      apiFetch('/notifications/unread-count', {}, true),
      apiFetch('/chat/unread-count', {}, true)
    ]);
    setNotifications(items?.content || items || []);
    setNotificationUnread(unreadRes?.unread || 0);
    setChatUnread(unreadChatRes?.unread || 0);
  }, [apiFetch, user]);

  const loadProfile = useCallback(async () => {
    if (!user) return;
    const data = await apiFetch('/auth/profile', {}, true);
    setProfileForm({ name: data.name || '', skills: data.skills || '', experienceYears: data.experienceYears ?? 0, education: data.education || '', portfolioUrl: data.portfolioUrl || '' });
    setUser((prev) => ({ ...(prev || {}), ...data }));
  }, [apiFetch, user]);

  const loadEmployerData = useCallback(async () => {
    if (!(isEmployer || isAdmin)) return;
    const [jobsData, analyticsData] = await Promise.all([
      apiFetch('/jobs/employer/me?page=0&size=100', {}, true),
      apiFetch('/analytics/overview', {}, true)
    ]);
    setMyJobs(jobsData?.content || []);
    setAnalytics(analyticsData || {});
  }, [apiFetch, isAdmin, isEmployer]);

  const loadPipeline = useCallback(async (jobId = pipelineFilter.jobId, status = pipelineFilter.status) => {
    const query = new URLSearchParams();
    if (jobId) query.append('jobId', jobId);
    if (status) query.append('status', status);
    setPipeline(await apiFetch(`/applications/pipeline${query.toString() ? `?${query.toString()}` : ''}`, {}, true) || []);
  }, [apiFetch, pipelineFilter.jobId, pipelineFilter.status]);

  const loadAdminData = useCallback(async () => {
    if (!isAdmin) return;
    const [usersData, applicationsData, analyticsData] = await Promise.all([
      apiFetch('/auth/users', {}, true),
      apiFetch('/applications', {}, true),
      apiFetch('/analytics/overview', {}, true)
    ]);
    setUsers(usersData || []);
    setApplications(applicationsData || []);
    setAnalytics(analyticsData || {});
  }, [apiFetch, isAdmin]);

  const loadConversation = useCallback(async (peerId) => {
    if (!peerId) return;
    setChatMessages(await apiFetch(`/chat/conversation/${peerId}`, {}, true) || []);
  }, [apiFetch]);

  useEffect(() => { loadJobs(0, search).catch((e) => setAndClearMessage(e.message)); }, [loadJobs, search]);
  useEffect(() => { if (isCandidate) loadMyApplications().catch(() => {}); }, [isCandidate, loadMyApplications]);
  useEffect(() => { if (user) { loadProfile().catch(() => {}); loadNotifications().catch(() => {}); } }, [loadNotifications, loadProfile, user]);

  const saveAuth = (authResponse) => {
    setToken(authResponse.token);
    setRefreshToken(authResponse.refreshToken || '');
    setUser(authResponse.user);
    localStorage.setItem('jobPortalToken', authResponse.token);
    localStorage.setItem('jobPortalRefreshToken', authResponse.refreshToken || '');
    localStorage.setItem('jobPortalUser', JSON.stringify(authResponse.user));
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      saveAuth(await apiFetch('/auth/register', { method: 'POST', body: JSON.stringify(registerForm) }));
      setRegisterForm({ name: '', email: '', password: '', role: 'CANDIDATE' });
      setAndClearMessage('Registered and logged in successfully.');
      setRoute('jobs');
    } catch (error) { setAndClearMessage(error.message); }
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await apiFetch('/auth/login', { method: 'POST', body: JSON.stringify(loginForm) });
      saveAuth(response);
      setLoginForm({ email: '', password: '' });
      setAndClearMessage(`Welcome ${response.user.name}`);
      setRoute('jobs');
    } catch (error) { setAndClearMessage(error.message); }
  };

  const handleOAuthDevLogin = async () => {
    const email = prompt('Enter Google/LinkedIn email');
    const name = prompt('Enter display name');
    if (!email || !name) return;
    try {
      saveAuth(await apiFetch('/auth/oauth/login', { method: 'POST', body: JSON.stringify({ provider: 'GOOGLE', email, name }) }));
      setAndClearMessage('OAuth dev login successful.');
      setRoute('jobs');
    } catch (error) { setAndClearMessage(error.message); }
  };

  const handleLogout = () => {
    setUser(null); setToken(''); setRefreshToken(''); setApplications([]); setUsers([]);
    localStorage.removeItem('jobPortalUser'); localStorage.removeItem('jobPortalToken'); localStorage.removeItem('jobPortalRefreshToken');
    setAndClearMessage('Logged out successfully.');
  };

  const handleAddJob = async (e) => {
    e.preventDefault();
    if (!canPostJobs) return setAndClearMessage('Only employer/admin can post jobs.');
    try {
      await apiFetch('/jobs', { method: 'POST', body: JSON.stringify({ ...jobForm, salary: Number(jobForm.salary) }) }, true);
      setJobForm({ title: '', company: '', location: '', salary: '', description: '' });
      await loadJobs(0, search);
      setAndClearMessage('Job posted successfully.');
    } catch (error) { setAndClearMessage(error.message); }
  };

  const handleResumeUpload = async () => {
    if (!resumeFile) return setAndClearMessage('Please select a PDF resume first.');
    const formData = new FormData(); formData.append('file', resumeFile);
    try {
      const response = await fetch(`${API_BASE}/applications/upload-resume`, { method: 'POST', headers: { Authorization: `Bearer ${token}` }, body: formData });
      const payload = await response.json();
      if (!response.ok) throw new Error(payload?.message || 'Resume upload failed');
      setResumePath(payload.resumePath);
      setAndClearMessage('Resume uploaded successfully.');
    } catch (error) { setAndClearMessage(error.message); }
  };

  const handleApply = async (jobId) => {
    if (!isCandidate) { setAndClearMessage('Login as candidate to apply.'); setRoute('login'); return; }
    try {
      await apiFetch('/applications', { method: 'POST', body: JSON.stringify({ jobId, resume: resumePath || 'No resume uploaded' }) }, true);
      await loadMyApplications();
      setAndClearMessage('Application submitted.');
    } catch (error) { setAndClearMessage(error.message); }
  };

  const handleUpdateStatus = async (applicationId, status) => {
    try {
      await apiFetch(`/applications/${applicationId}/status`, { method: 'PUT', body: JSON.stringify({ status }) }, true);
      await loadAdminData();
      await loadPipeline();
      setAndClearMessage('Application status updated.');
    } catch (error) { setAndClearMessage(error.message); }
  };

  const handleProfileSave = async (e) => {
    e.preventDefault();
    try {
      const data = await apiFetch('/auth/profile', { method: 'PUT', body: JSON.stringify(profileForm) }, true);
      setUser((prev) => ({ ...(prev || {}), ...data }));
      localStorage.setItem('jobPortalUser', JSON.stringify({ ...(user || {}), ...data }));
      setAndClearMessage('Profile updated successfully.');
    } catch (error) { setAndClearMessage(error.message); }
  };

  const openEmployer = async () => {
    if (!(isEmployer || isAdmin)) return;
    try { await loadEmployerData(); await loadPipeline(); setRoute('employer'); } catch (e) { setAndClearMessage(e.message); }
  };

  const openAdmin = async () => {
    if (!isAdmin) return;
    try { await loadAdminData(); setRoute('admin'); } catch (e) { setAndClearMessage(e.message); }
  };

  const openNotifications = async () => {
    try { await loadNotifications(); setRoute('notifications'); } catch (e) { setAndClearMessage(e.message); }
  };

  const openChat = async () => {
    setRoute('chat');
    if (chatPeerId) await loadConversation(chatPeerId);
  };

  const handleMarkNotificationRead = async (id) => {
    try { await apiFetch(`/notifications/${id}/read`, { method: 'PUT' }, true); await loadNotifications(); } catch (e) { setAndClearMessage(e.message); }
  };

  const markAllNotificationsRead = async () => {
    try { await apiFetch('/notifications/read-all', { method: 'PUT' }, true); await loadNotifications(); } catch (e) { setAndClearMessage(e.message); }
  };

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!chatPeerId || !chatMessage.trim()) return setAndClearMessage('Provide receiver id and message.');
    try {
      await apiFetch('/chat/send', { method: 'POST', body: JSON.stringify({ receiverId: Number(chatPeerId), content: chatMessage }) }, true);
      setChatMessage('');
      await loadConversation(chatPeerId);
      await loadNotifications();
    } catch (error) { setAndClearMessage(error.message); }
  };

  return (
    <div className="App">
      <header className="topbar">
        <div><p className="eyebrow">Recruitment Workspace</p><h1>Job Portal</h1></div>
        <div className="topbar-actions">
          {user ? <><span className="user-chip">{user.name} ({user.role})</span><span className="user-chip">Notif {notificationUnread}</span><span className="user-chip">Chat {chatUnread}</span><button onClick={handleLogout}>Logout</button></> : <span className="user-chip">Guest</span>}
        </div>
      </header>

      <NavTabs
        activeTab={activeTab}
        setRoute={setRoute}
        isCandidate={isCandidate}
        isEmployer={isEmployer}
        isAdmin={isAdmin}
        user={user}
        openEmployer={openEmployer}
        openAdmin={openAdmin}
        openNotifications={openNotifications}
        openChat={openChat}
      />

      {message && <p className="message">{message}</p>}

      {activeTab === 'jobs' && <JobSection jobs={jobs} appCount={appCount} search={search} setSearch={setSearch} loadJobs={loadJobs} canPostJobs={canPostJobs} jobForm={jobForm} setJobForm={setJobForm} handleAddJob={handleAddJob} isCandidate={isCandidate} resumePath={resumePath} resumeFile={resumeFile} setResumeFile={setResumeFile} handleResumeUpload={handleResumeUpload} handleApply={handleApply} page={page} totalPages={totalPages} />}
      {activeTab === 'register' && <RegisterSection registerForm={registerForm} setRegisterForm={setRegisterForm} handleRegister={handleRegister} />}
      {activeTab === 'login' && <LoginSection loginForm={loginForm} setLoginForm={setLoginForm} handleLogin={handleLogin} handleOAuthDevLogin={handleOAuthDevLogin} />}
      {activeTab === 'profile' && <ProfileSection user={user} profileForm={profileForm} setProfileForm={setProfileForm} handleProfileSave={handleProfileSave} />}
      {activeTab === 'applications' && <ApplicationsSection applications={applications} />}
      {activeTab === 'chat' && <ChatSection user={user} chatPeerId={chatPeerId} setChatPeerId={setChatPeerId} chatMessage={chatMessage} setChatMessage={setChatMessage} handleSendMessage={handleSendMessage} loadConversation={loadConversation} chatMessages={chatMessages} />}
      {activeTab === 'notifications' && <NotificationsSection user={user} notifications={notifications} handleMarkNotificationRead={handleMarkNotificationRead} markAllNotificationsRead={markAllNotificationsRead} />}
      {activeTab === 'employer' && <EmployerSection isVisible={isEmployer || isAdmin} analytics={analytics} myJobs={myJobs} pipelineFilter={pipelineFilter} setPipelineFilter={setPipelineFilter} loadPipeline={loadPipeline} pipeline={pipeline} handleUpdateStatus={handleUpdateStatus} />}
      {activeTab === 'admin' && <AdminSection isAdmin={isAdmin} analytics={analytics} users={users} applications={applications} handleUpdateStatus={handleUpdateStatus} />}
    </div>
  );
}

export default App;
