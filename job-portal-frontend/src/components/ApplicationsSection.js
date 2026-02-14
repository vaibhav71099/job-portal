export default function ApplicationsSection({ applications }) {
  return (
    <section className="panel">
      <h2>My Applications</h2>
      <div className="grid">
        {applications.length === 0 && <article className="card empty-card"><h3>No applications yet</h3><p>Apply to jobs and track status here.</p></article>}
        {applications.map((application) => (
          <article className="card" key={application.id}>
            <p>Application ID: {application.id}</p>
            <p>Job ID: {application.jobId}</p>
            <p>ATS Score: {application.atsScore ?? 0}</p>
            <p>Status: <span className={`status-chip status-${application.status?.toLowerCase()}`}>{application.status}</span></p>
            <p>Resume: {application.resume}</p>
          </article>
        ))}
      </div>
    </section>
  );
}
