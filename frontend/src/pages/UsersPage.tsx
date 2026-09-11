import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { deleteUser, getUsers } from "../api/usersApi";
import { PAGE_SIZE } from "../api/paging";
import type { UserResponse } from "../api/types";
import PaginationBar from "../components/PaginationBar";

export default function UsersPage() {
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const load = () => {
    getUsers({ page, size: PAGE_SIZE, sort: "id" })
      .then((result) => {
        setUsers(result.content);
        setTotalPages(result.totalPages);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load users"));
  };

  useEffect(load, [page]);

  const handleDelete = async (user: UserResponse) => {
    if (!window.confirm(`Delete user ${user.username}? Wallets and expenses will be removed.`)) {
      return;
    }
    await deleteUser(user.id);
    load();
  };

  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Users</p>
          <h1>Users</h1>
        </div>
        <Link to="/" className="button">
          Register user
        </Link>
      </div>
      {error && <div className="alert error">{error}</div>}
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Username</th>
              <th>Wallets (OneToMany)</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id}>
                <td>{user.id}</td>
                <td>
                  <Link to={`/users/${user.id}`}>{user.username}</Link>
                </td>
                <td>{user.walletNames.join(", ") || "—"}</td>
                <td className="actions-cell">
                  <Link to={`/users/${user.id}`} className="secondary-button">
                    Open
                  </Link>
                  <button type="button" className="danger-button" onClick={() => handleDelete(user)}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <PaginationBar page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}
