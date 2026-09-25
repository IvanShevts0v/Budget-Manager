import { FormEvent, useEffect, useState } from "react";
import {
  createCategory,
  deleteCategory,
  getCategories,
  patchCategory,
  searchCategoriesByName,
} from "../api/categoriesApi";
import { getExpensesLookup } from "../api/expensesApi";
import type { ExpenseResponse, NamedEntity } from "../api/types";
import { ListFooter, SortOrderFields, useListQuery } from "../components/ListControls";

export default function CategoriesPage() {
  const [categories, setCategories] = useState<NamedEntity[]>([]);
  const [expenses, setExpenses] = useState<ExpenseResponse[]>([]);
  const [name, setName] = useState("");
  const [filterName, setFilterName] = useState("");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingName, setEditingName] = useState("");
  const [error, setError] = useState<string | null>(null);
  const list = useListQuery("id");
  const [totalPages, setTotalPages] = useState(1);

  const load = () => {
    const request = filterName.trim()
      ? searchCategoriesByName(filterName.trim(), list.query)
      : getCategories(list.query);
    request
      .then((result) => {
        setCategories(result.content);
        setTotalPages(result.totalPages);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load categories"));
    getExpensesLookup()
      .then((result) => setExpenses(result.content))
      .catch(() => setExpenses([]));
  };

  useEffect(() => {
    list.setPage(0);
  }, [filterName]);

  useEffect(load, [filterName, list.page, list.sort, list.direction, list.size]);

  const handleCreate = async (event: FormEvent) => {
    event.preventDefault();
    await createCategory(name);
    setName("");
    load();
  };

  const handleSave = async (id: number) => {
    await patchCategory(id, editingName);
    setEditingId(null);
    load();
  };

  const handleDelete = async (category: NamedEntity) => {
    if (!window.confirm(`Delete category ${category.name}?`)) {
      return;
    }
    await deleteCategory(category.id);
    load();
  };

  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Categories</p>
          <h1>Categories</h1>
        </div>
      </div>

      {error && <div className="alert error">{error}</div>}

      <section className="card">
        <h2>Filters</h2>
        <div className="filters-grid">
          <label>
            Name
            <input
              value={filterName}
              onChange={(e) => setFilterName(e.target.value)}
              placeholder="Exact name"
            />
          </label>
          <SortOrderFields
            sort={list.sort}
            direction={list.direction}
            sortOptions={[
              { value: "id", label: "ID" },
              { value: "name", label: "Name" },
            ]}
            onSortChange={list.changeSort}
            onDirectionChange={list.changeDirection}
          />
        </div>
      </section>

      <section className="card">
        <h2>Create category</h2>
        <form className="inline-form" onSubmit={handleCreate}>
          <input value={name} onChange={(e) => setName(e.target.value)} required />
          <button type="submit" className="button">
            Create
          </button>
        </form>
      </section>

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {categories.map((category) => (
              <tr key={category.id}>
                <td>{category.id}</td>
                <td>
                  {editingId === category.id ? (
                    <input value={editingName} onChange={(e) => setEditingName(e.target.value)} />
                  ) : (
                    category.name
                  )}
                </td>
                <td className="actions-cell">
                  {editingId === category.id ? (
                    <button type="button" className="secondary-button" onClick={() => handleSave(category.id)}>
                      Save
                    </button>
                  ) : (
                    <button
                      type="button"
                      className="secondary-button"
                      onClick={() => {
                        setEditingId(category.id);
                        setEditingName(category.name);
                      }}
                    >
                      Edit
                    </button>
                  )}
                  <button type="button" className="danger-button" onClick={() => handleDelete(category)}>
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

      <section className="card">
        <h2>Expenses in these categories</h2>
        <div className="relation-grid">
          {categories.map((category) => {
            const items = expenses.filter((expense) => expense.category === category.name);
            return (
              <article key={category.id} className="relation-card">
                <h3>{category.name}</h3>
                <p className="muted">{items.length} expenses</p>
                <ul>
                  {items.slice(0, 5).map((expense) => (
                    <li key={expense.id}>
                      {expense.date}: {expense.description || "Expense"} — {expense.amount.toFixed(2)}
                      {expense.tags.length > 0 && ` · ${expense.tags.join(", ")}`}
                    </li>
                  ))}
                </ul>
                {items.length === 0 && <p className="muted">No expenses</p>}
                {items.length > 5 && <p className="muted">+ {items.length - 5} more</p>}
              </article>
            );
          })}
        </div>
      </section>
    </div>
  );
}
