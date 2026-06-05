import { useEffect, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import {
  createExpense,
  deleteExpense,
  getExpenses,
  getExpensesPaginated,
  patchExpense,
} from "../api/expensesApi";
import { getCategories } from "../api/categoriesApi";
import { getTags } from "../api/tagsApi";
import type { ExpenseRequest, ExpenseResponse, NamedEntity } from "../api/types";
import ExpenseModal from "../components/ExpenseModal";
import { useAppContext } from "../state/AppContext";

export default function ExpensesPage() {
  const { selectedUserId } = useAppContext();
  const [expenses, setExpenses] = useState<ExpenseResponse[]>([]);
  const [categories, setCategories] = useState<NamedEntity[]>([]);
  const [tags, setTags] = useState<NamedEntity[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<ExpenseResponse | null>(null);
  const [usePagination, setUsePagination] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filters, setFilters] = useState({
    description: "",
    category: "",
    date: "",
    tagFilter: "",
  });

  const load = async () => {
    if (selectedUserId == null) {
      return;
    }
    setError(null);
    try {
      if (usePagination) {
        const result = await getExpensesPaginated({
          walletOwnerUserId: selectedUserId,
          categoryName: filters.category || undefined,
          page,
          size: 10,
          sort: "date,desc",
        });
        setExpenses(result.content);
        setTotalPages(result.totalPages);
      } else {
        const result = await getExpenses({
          senderUserId: selectedUserId,
          description: filters.description || undefined,
          category: filters.category || undefined,
          date: filters.date || undefined,
        });
        let filtered = result;
        if (filters.tagFilter) {
          filtered = result.filter((expense) =>
            expense.tags.some((tag) => tag.toLowerCase().includes(filters.tagFilter.toLowerCase()))
          );
        }
        setExpenses(filtered);
        setTotalPages(1);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load expenses");
    }
  };

  useEffect(() => {
    getCategories().then(setCategories).catch(() => setCategories([]));
    getTags().then(setTags).catch(() => setTags([]));
  }, []);

  useEffect(() => {
    load();
  }, [selectedUserId, filters, usePagination, page]);

  if (selectedUserId == null) {
    return <Navigate to="/" replace />;
  }

  const handleSave = async (body: ExpenseRequest) => {
    if (editing) {
      await patchExpense(editing.id, body);
    } else {
      await createExpense(body);
    }
    await load();
  };

  const handleDelete = async (expense: ExpenseResponse) => {
    if (!window.confirm(`Delete expense #${expense.id}?`)) {
      return;
    }
    await deleteExpense(expense.id);
    await load();
  };

  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Expenses</p>
          <h1>Expense list & filters</h1>
        </div>
        <button
          type="button"
          className="button"
          onClick={() => {
            setEditing(null);
            setModalOpen(true);
          }}
        >
          New expense
        </button>
      </div>

      <section className="card filters-card">
        <h2>Filters</h2>
        <div className="filters-grid">
          <label>
            Description
            <input
              value={filters.description}
              onChange={(e) => setFilters((prev) => ({ ...prev, description: e.target.value }))}
              disabled={usePagination}
            />
          </label>
          <label>
            Category
            <select
              value={filters.category}
              onChange={(e) => setFilters((prev) => ({ ...prev, category: e.target.value }))}
            >
              <option value="">All</option>
              {categories.map((category) => (
                <option key={category.id} value={category.name}>
                  {category.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Date
            <input
              type="date"
              value={filters.date}
              onChange={(e) => setFilters((prev) => ({ ...prev, date: e.target.value }))}
              disabled={usePagination}
            />
          </label>
          <label>
            Tag contains
            <input
              value={filters.tagFilter}
              onChange={(e) => setFilters((prev) => ({ ...prev, tagFilter: e.target.value }))}
              disabled={usePagination}
              placeholder="client-side filter"
            />
          </label>
          <label className="checkbox-row">
            <input type="checkbox" checked={usePagination} onChange={(e) => setUsePagination(e.target.checked)} />
            Paginated API (`/expenses/by-wallet-and-category`)
          </label>
        </div>
      </section>

      {error && <div className="alert error">{error}</div>}

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Description</th>
              <th>Amount</th>
              <th>Date</th>
              <th>Wallet (1:N)</th>
              <th>Category (1:N)</th>
              <th>Tags (M:N)</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {expenses.map((expense) => (
              <tr key={expense.id}>
                <td>{expense.id}</td>
                <td>{expense.description || "—"}</td>
                <td>{expense.amount.toFixed(2)}</td>
                <td>{expense.date}</td>
                <td>
                  <Link to="/wallets">{expense.walletName}</Link>
                </td>
                <td>{expense.category}</td>
                <td>
                  <div className="chip-row">
                    {expense.tags.length ? (
                      expense.tags.map((tag) => (
                        <button
                          key={tag}
                          type="button"
                          className="chip"
                          onClick={() => setFilters((prev) => ({ ...prev, tagFilter: tag }))}
                        >
                          {tag}
                        </button>
                      ))
                    ) : (
                      <span className="muted">—</span>
                    )}
                  </div>
                </td>
                <td className="actions-cell">
                  <button
                    type="button"
                    className="secondary-button"
                    onClick={() => {
                      setEditing(expense);
                      setModalOpen(true);
                    }}
                  >
                    Edit
                  </button>
                  <button type="button" className="danger-button" onClick={() => handleDelete(expense)}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {usePagination && (
        <div className="pagination">
          <button type="button" className="secondary-button" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
            Previous
          </button>
          <span>
            Page {page + 1} / {Math.max(totalPages, 1)}
          </span>
          <button
            type="button"
            className="secondary-button"
            disabled={page + 1 >= totalPages}
            onClick={() => setPage((p) => p + 1)}
          >
            Next
          </button>
        </div>
      )}

      <p className="muted helper-text">Available tags: {tags.map((t) => t.name).join(", ") || "none"}</p>

      <ExpenseModal
        open={modalOpen}
        userId={selectedUserId}
        expense={editing}
        onClose={() => {
          setModalOpen(false);
          setEditing(null);
        }}
        onSave={handleSave}
      />
    </div>
  );
}
