export default function JobSection({ jobs, appCount, search, setSearch, loadJobs, canPostJobs, jobForm, setJobForm, handleAddJob, isCandidate, resumePath, resumeFile, setResumeFile, handleResumeUpload, handleApply, page, totalPages }) {
  return (
    <section className="panel">
      <div className="panel-header">
        <h2>Explore Jobs</h2>
        <div className="stats-row">
          <div className="stat-card"><span>Total Jobs (Page)</span><strong>{jobs.length}</strong></div>
          <div className="stat-card"><span>My Applications</span><strong>{appCount}</strong></div>
        </div>
      </div>

      <div className="search-row">
        <input placeholder="Keyword" value={search.keyword} onChange={(e) => setSearch({ ...search, keyword: e.target.value })} />
        <input placeholder="Location" value={search.location} onChange={(e) => setSearch({ ...search, location: e.target.value })} />
        <button onClick={() => loadJobs(0, search)}>Search</button>
        <button onClick={() => { const reset = { keyword: '', location: '' }; setSearch(reset); loadJobs(0, reset); }}>Reset</button>
      </div>

      {canPostJobs && (
        <form className="card form-grid" onSubmit={handleAddJob}>
          <h3>Post New Job</h3>
          <input placeholder="Title" value={jobForm.title} onChange={(e) => setJobForm({ ...jobForm, title: e.target.value })} required />
          <input placeholder="Company" value={jobForm.company} onChange={(e) => setJobForm({ ...jobForm, company: e.target.value })} required />
          <input placeholder="Location" value={jobForm.location} onChange={(e) => setJobForm({ ...jobForm, location: e.target.value })} required />
          <input placeholder="Salary" type="number" value={jobForm.salary} onChange={(e) => setJobForm({ ...jobForm, salary: e.target.value })} required />
          <textarea placeholder="Description" value={jobForm.description} onChange={(e) => setJobForm({ ...jobForm, description: e.target.value })} required />
          <button type="submit">Create Job</button>
        </form>
      )}

      {isCandidate && (
        <div className="card form-grid">
          <h3>Resume Upload (PDF)</h3>
          <input type="file" accept="application/pdf" onChange={(e) => setResumeFile(e.target.files?.[0] || null)} />
          <button onClick={handleResumeUpload} type="button">Upload Resume</button>
          {resumeFile && <p className="muted">Selected: {resumeFile.name}</p>}
          {resumePath && <p className="muted">Uploaded: {resumePath}</p>}
        </div>
      )}

      <div className="grid">
        {jobs.length === 0 && <article className="card empty-card"><h3>No jobs yet</h3><p>Try different filters or add jobs as employer/admin.</p></article>}
        {jobs.map((job) => (
          <article className="card job-card" key={job.id}>
            <h3>{job.title}</h3>
            <p className="meta-line"><strong>{job.company}</strong> • {job.location}</p>
            <p className="meta-line">Salary: {job.salary}</p>
            <p>{job.description}</p>
            {isCandidate && <button onClick={() => handleApply(job.id)}>Apply</button>}
          </article>
        ))}
      </div>

      <div className="pagination-row">
        <button disabled={page === 0} onClick={() => loadJobs(page - 1, search)}>Prev</button>
        <span>Page {totalPages === 0 ? 1 : page + 1} of {Math.max(totalPages, 1)}</span>
        <button disabled={page + 1 >= totalPages} onClick={() => loadJobs(page + 1, search)}>Next</button>
      </div>
    </section>
  );
}
