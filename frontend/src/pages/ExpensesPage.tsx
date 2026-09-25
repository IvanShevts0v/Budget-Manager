import { useEffect, useRef, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import {
  createExpense,
  deleteExpense,
  getExpenses,
  patchExpense,
} from "../api/expensesApi";
import { getCategoriesLookup } from "../api/categoriesApi";
import { getTagsLookup } from "../api/tagsApi";
import type { ExpenseRequest, ExpenseResponse, NamedEntity } from "../api/types";
import ExpenseModal from "../components/ExpenseModal";
import { ListFooter, SortOrderFields, useListQuery } from "../components/ListControls";
import { useAppContext } from "../state/AppContext";

export default function ExpensesPage() {
  const { selectedUserId } = useAppContext();
  const [expenses, setExpenses] = useState<ExpenseResponse[]>([]);
  const [categories, setCategories] = useState<NamedEntity[]>([]);
  const [tags, setTags] = useState<NamedEntity[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<ExpenseResponse | null>(null);
  const list = useListQuery("date", "desc");
  const [totalPages, setTotalPages] = useState(0);
  const [filters, setFilters] = useState({
    description: "",
    category: "",
    date: "",
    tags: [] as string[],
  });
  const [tagsOpen, setTagsOpen] = useState(false);
  const tagsMenuRef = useRef<HTMLDivElement>(null);

  const load = async () => {
    if (selectedUserId == null) {
      return;
    }
    setError(null);
    try {
      const result = await getExpenses(
        {
          senderUserId: selectedUserId,
          description: filters.description || undefined,
          category: filters.category || undefined,
          date: filters.date || undefined,
          tags: filters.tags,
        },
        list.query
      );
      setExpenses(result.content);
      setTotalPages(result.totalPages);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load expenses");
    }
  };

  useEffect(() => {
    getCategoriesLookup()
      .then((result) => setCategories(result.content))
      .catch(() => setCategories([]));
    getTagsLookup()
      .then((result) => setTags(result.content))
      .catch(() => setTags([]));
  }, []);

  useEffect(() => {
    list.setPage(0);
  }, [filters, selectedUserId]);

  useEffect(() => {
    if (!tagsOpen) {
      return;
    }
    const close = (event: MouseEvent) => {
      if (!tagsMenuRef.current?.contains(event.target as Node)) {
        setTagsOpen(false);
      }
    };
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, [tagsOpen]);

  const toggleTag = (tag: string) => {
    setFilters((prev) => ({
      ...prev,
      tags: prev.tags.includes(tag) ? prev.tags.filter((item) => item !== tag) : [...prev.tags, tag],
    }));
  };

  useEffect(() => {
    load();
  }, [selectedUserId, filters, list.page, list.sort, list.direction, list.size]);

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
            />
          </label>
          <div className="filter-field">
            Tags
            <div className="multi-select" ref={tagsMenuRef}>
              <button
                type="button"
                className="multi-select-trigger"
                aria-expanded={tagsOpen}
                onClick={() => setTagsOpen((open) => !open)}
              >
                {filters.tags.length ? filters.tags.join(", ") : "All"}
              </button>
              {tagsOpen && (
                <div className="multi-select-menu">
                  {tags.length ? (
                    tags.map((tag) => (
                      <label key={tag.id} className="multi-select-option">
                        <input
                          type="checkbox"
                          checked={filters.tags.includes(tag.name)}
                          onChange={() => toggleTag(tag.name)}
                        />
                        {tag.name}
                      </label>
                    ))
                  ) : (
                    <span className="muted">No tags</span>
                  )}
                </div>
              )}
            </div>
          </div>
          <SortOrderFields
            sort={list.sort}
            direction={list.direction}
            sortOptions={[
              { value: "date", label: "Date" },
              { value: "amount", label: "Amount" },
              { value: "description", label: "Description" },
              { value: "category.name", label: "Category" },
              { value: "wallet.name", label: "Wallet" },
              { value: "wallet.user.username", label: "User" },
              { value: "id", label: "ID" },
            ]}
            onSortChange={list.changeSort}
            onDirectionChange={list.changeDirection}
          />
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
              <th>Wallet</th>
              <th>Category</th>
              <th>Tags</th>
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
                          className={filters.tags.includes(tag) ? "chip active" : "chip"}
                          onClick={() => toggleTag(tag)}
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

      <ListFooter
        page={list.page}
        totalPages={totalPages}
        onPageChange={list.setPage}
        size={list.size}
        onSizeChange={list.changeSize}
      />

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
