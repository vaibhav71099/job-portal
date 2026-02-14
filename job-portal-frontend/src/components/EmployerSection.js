export default function EmployerSection({ isVisible, analytics, myJobs, pipelineFilter, setPipelineFilter, loadPipeline, pipeline, handleUpdateStatus }) {
  if (!isVisible) return null;
  return (
    <section className="panel">
      <h2>Employer Pipeline</h2>
      <div className="stats-row">
        <div className="stat-card"><span>My Jobs</span><strong>{analytics.myJobs ?? myJobs.length}</strong></div>
        <div className="stat-card"><span>Total Applications</span><strong>{analytics.myApplications ?? 0}</strong></div>
        <div className="stat-card"><span>Shortlisted</span><strong>{analytics.shortlisted ?? 0}</strong></div>
        <div className="stat-card"><span>Hire Conversion</span><strong>{analytics.conversionRate ?? 0}%</strong></div>
      </div>

      <div className="search-row">
        <select value={pipelineFilter.jobId} onChange={(e) => setPipelineFilter({ ...pipelineFilter, jobId: e.target.value })}>
          <option value="">All Jobs</option>
          {myJobs.map((job) => <option key={job.id} value={job.id}>{job.title}</option>)}
        </select>
        <select value={pipelineFilter.status} onChange={(e) => setPipelineFilter({ ...pipelineFilter, status: e.target.value })}>
          <option value="">All Statuses</option>
          <option value="APPLIED">Applied</option>
          <option value="SHORTLISTED">Shortlisted</option>
          <option value="REJECTED">Rejected</option>
          <option value="HIRED">Hired</option>
        </select>
        <button onClick={() => loadPipeline(pipelineFilter.jobId, pipelineFilter.status)}>Filter</button>
        <button onClick={() => { const reset = { jobId: '', status: '' }; setPipelineFilter(reset); loadPipeline('', ''); }}>Reset</button>
      </div>

      <div className="grid">
        {pipeline.length === 0 && <article className="card empty-card"><h3>No pipeline records</h3></article>}
        {pipeline.map((application) => (
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
    </section>
  );
}
