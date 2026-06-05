import { NavLink } from "react-router-dom";
import { useAppContext } from "../state/AppContext";

export default function TopBar() {
  const { selectedUser, setSelectedUserId } = useAppContext();

  return (
    <header className="topbar">
      <div className="topbar-left">
        <NavLink to="/" className="brand">
          Budget Manager
        </NavLink>
        <nav className="nav">
          <NavLink to="/expenses" className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}>
            Expenses
          </NavLink>
          <NavLink to="/users" className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}>
            Users
          </NavLink>
          <NavLink to="/wallets" className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}>
            Wallets
          </NavLink>
          <NavLink to="/categories" className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}>
            Categories
          </NavLink>
          <NavLink to="/tags" className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}>
            Tags
          </NavLink>
        </nav>
      </div>
      <div className="topbar-right">
        {selectedUser ? (
          <>
            <span className="user-email">
              User: <NavLink to={`/users/${selectedUser.id}`}>{selectedUser.username}</NavLink>
            </span>
            <button type="button" className="secondary-button" onClick={() => setSelectedUserId(null)}>
              Switch user
            </button>
          </>
        ) : (
          <NavLink to="/" className="primary-link">
            Select user
          </NavLink>
        )}
      </div>
    </header>
  );
}
