export default function NavTabs({ activeTab, setRoute, isCandidate, isEmployer, isAdmin, user, openEmployer, openAdmin, openNotifications, openChat }) {
  return (
    <nav className="tabs">
      <button className={activeTab === 'jobs' ? 'active' : ''} onClick={() => setRoute('jobs')}>Jobs</button>
      <button className={activeTab === 'register' ? 'active' : ''} onClick={() => setRoute('register')}>Register</button>
      <button className={activeTab === 'login' ? 'active' : ''} onClick={() => setRoute('login')}>Login</button>
      <button className={activeTab === 'profile' ? 'active' : ''} onClick={() => setRoute('profile')} disabled={!user}>Profile</button>
      <button className={activeTab === 'applications' ? 'active' : ''} onClick={() => setRoute('applications')} disabled={!isCandidate}>My Applications</button>
      <button className={activeTab === 'chat' ? 'active' : ''} onClick={openChat} disabled={!user}>Chat</button>
      <button className={activeTab === 'notifications' ? 'active' : ''} onClick={openNotifications} disabled={!user}>Notifications</button>
      <button className={activeTab === 'employer' ? 'active' : ''} onClick={openEmployer} disabled={!(isEmployer || isAdmin)}>Employer</button>
      <button className={activeTab === 'admin' ? 'active' : ''} onClick={openAdmin} disabled={!isAdmin}>Admin</button>
    </nav>
  );
}
