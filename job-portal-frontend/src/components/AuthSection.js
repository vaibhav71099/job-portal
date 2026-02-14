export function RegisterSection({ registerForm, setRegisterForm, handleRegister }) {
  return (
    <section className="panel">
      <h2>Register</h2>
      <form className="card form-grid" onSubmit={handleRegister}>
        <input placeholder="Name" value={registerForm.name} onChange={(e) => setRegisterForm({ ...registerForm, name: e.target.value })} required />
        <input placeholder="Email" type="email" value={registerForm.email} onChange={(e) => setRegisterForm({ ...registerForm, email: e.target.value })} required />
        <input placeholder="Password" type="password" value={registerForm.password} onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })} required />
        <select value={registerForm.role} onChange={(e) => setRegisterForm({ ...registerForm, role: e.target.value })}>
          <option value="CANDIDATE">Candidate</option>
          <option value="EMPLOYER">Employer</option>
          <option value="ADMIN">Admin</option>
        </select>
        <button type="submit">Register</button>
      </form>
    </section>
  );
}

export function LoginSection({ loginForm, setLoginForm, handleLogin, handleOAuthDevLogin }) {
  return (
    <section className="panel">
      <h2>Login</h2>
      <form className="card form-grid" onSubmit={handleLogin}>
        <input placeholder="Email" type="email" value={loginForm.email} onChange={(e) => setLoginForm({ ...loginForm, email: e.target.value })} required />
        <input placeholder="Password" type="password" value={loginForm.password} onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })} required />
        <button type="submit">Login</button>
        <button type="button" onClick={handleOAuthDevLogin}>Login with Google (Dev)</button>
      </form>
    </section>
  );
}
