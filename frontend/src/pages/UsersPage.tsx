import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { deleteUser, getUsers } from "../api/usersApi";
import type { UserResponse } from "../api/types";
import { ListFooter, SortOrderFields, useListQuery } from "../components/ListControls";

export default function UsersPage() {
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [error, setError] = useState<string | null>(null);
  const list = useListQuery("id");
  const [totalPages, setTotalPages] = useState(1);

  const load = () => {
    getUsers(list.query)
      .then((result) => {
        setUsers(result.content);
        setTotalPages(result.totalPages);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load users"));
  };

  useEffect(load, [list.page, list.sort, list.direction, list.size]);

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
      <section className="card filters-card">
        <h2>Filters</h2>
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
      </section>
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
      <ListFooter
        page={list.page}
        totalPages={totalPages}
        onPageChange={list.setPage}
        size={list.size}
        onSizeChange={list.changeSize}
      />
    </div>
  );
}
