export default function ProfileSection({ user, profileForm, setProfileForm, handleProfileSave }) {
  if (!user) return null;
  return (
    <section className="panel">
      <h2>My Profile</h2>
      <form className="card form-grid" onSubmit={handleProfileSave}>
        <input placeholder="Name" value={profileForm.name} onChange={(e) => setProfileForm({ ...profileForm, name: e.target.value })} required />
        <textarea placeholder="Skills (comma separated)" value={profileForm.skills} onChange={(e) => setProfileForm({ ...profileForm, skills: e.target.value })} />
        <input placeholder="Experience (years)" type="number" min="0" value={profileForm.experienceYears} onChange={(e) => setProfileForm({ ...profileForm, experienceYears: Number(e.target.value) })} />
        <input placeholder="Education" value={profileForm.education} onChange={(e) => setProfileForm({ ...profileForm, education: e.target.value })} />
        <input placeholder="Portfolio URL" value={profileForm.portfolioUrl} onChange={(e) => setProfileForm({ ...profileForm, portfolioUrl: e.target.value })} />
        <button type="submit">Save Profile</button>
      </form>
    </section>
  );
}
