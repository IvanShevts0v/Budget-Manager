import { FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getUsers, registerUser } from "../api/usersApi";
import type { UserResponse } from "../api/types";
import { useAppContext } from "../state/AppContext";
import { ListFooter, SortOrderFields, useListQuery } from "../components/ListControls";

export default function HomePage() {
  const navigate = useNavigate();
  const { setSelectedUserId } = useAppContext();
  const [users, setUsers] = useState<UserResponse[]>([]);
  const list = useListQuery("id");
  const [totalPages, setTotalPages] = useState(1);
  const [username, setUsername] = useState("");
  const [walletName, setWalletName] = useState("Default");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    getUsers(list.query)
      .then((result) => {
        setUsers(result.content);
        setTotalPages(result.totalPages);
      })
      .catch(() => setUsers([]));
  }, [list.page, list.sort, list.direction, list.size]);

  const handleSelect = (user: UserResponse) => {
    setSelectedUserId(user.id);
    navigate("/expenses");
  };

  const handleRegister = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const user = await registerUser({
        username,
        defaultWalletName: walletName || undefined,
      });
      setUsers((prev) => [...prev, user]);
      setSelectedUserId(user.id);
      navigate("/expenses");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="page-heading">
        <h1>Budget Manager</h1>
      </div>
      <section className="card">
        <h2>Register</h2>
        <form className="form-grid" onSubmit={handleRegister}>
          <label>
            Username
            <input value={username} onChange={(e) => setUsername(e.target.value)} required />
          </label>
          <label>
            Default wallet
            <input value={walletName} onChange={(e) => setWalletName(e.target.value)} />
          </label>
          {error && <div className="alert error">{error}</div>}
          <button type="submit" className="button" disabled={loading}>
            {loading ? "Creating..." : "Register & continue"}
          </button>
        </form>
      </section>
      <section className="card">
        <h2>Select existing user</h2>
        <div className="filters-grid">
          <SortOrderFields
            sort={list.sort}
            direction={list.direction}
            sortOptions={[
              { value: "id", label: "ID" },
              { value: "username", label: "Username" },
            ]}
            onSortChange={list.changeSort}
            onDirectionChange={list.changeDirection}
          />
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Username</th>
                <th>Wallets</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td>{user.id}</td>
                  <td>{user.username}</td>
                  <td>{user.walletNames.join(", ") || "—"}</td>
                  <td>
                    <button type="button" className="secondary-button" onClick={() => handleSelect(user)}>
                      Use this user
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <ListFooter
          page={list.page}
          totalPages={totalPages}
          onPageChange={list.setPage}
          size={list.size}
          onSizeChange={list.changeSize}
        />
      </section>
    </div>
  );
}
