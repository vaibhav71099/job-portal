export default function AdminSection({ isAdmin, analytics, users, applications, handleUpdateStatus }) {
  if (!isAdmin) return null;
  return (
    <section className="panel">
      <h2>Admin Panel</h2>
      <div className="stats-row">
        <div className="stat-card"><span>Total Users</span><strong>{analytics.totalUsers ?? 0}</strong></div>
        <div className="stat-card"><span>Total Jobs</span><strong>{analytics.totalJobs ?? 0}</strong></div>
        <div className="stat-card"><span>Total Applications</span><strong>{analytics.totalApplications ?? 0}</strong></div>
        <div className="stat-card"><span>Hired</span><strong>{analytics.hired ?? 0}</strong></div>
      </div>

      <div className="admin-block">
        <h3>Users ({users.length})</h3>
        {users.length === 0 ? <p className="muted">No users found.</p> : (
          <ul>{users.map((u) => <li key={u.id}>{u.name} - {u.email} ({u.role})</li>)}</ul>
        )}
      </div>

      <div className="admin-block">
        <h3>Applications ({applications.length})</h3>
        <div className="grid">
          {applications.length === 0 && <article className="card empty-card"><h3>No applications yet</h3></article>}
          {applications.map((application) => (
            <article className="card" key={application.id}>
              <p>App #{application.id}</p><p>Job: {application.jobId}</p><p>Candidate: {application.candidateId}</p>
              <p>ATS Score: {application.atsScore ?? 0}</p>
              <p>Status: <span className={`status-chip status-${application.status?.toLowerCase()}`}>{application.status}</span></p>
              <div className="status-buttons">
                <button onClick={() => handleUpdateStatus(application.id, 'SHORTLISTED')}>Shortlist</button>
                <button onClick={() => handleUpdateStatus(application.id, 'REJECTED')}>Reject</button>
                <button onClick={() => handleUpdateStatus(application.id, 'HIRED')}>Hire</button>
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
