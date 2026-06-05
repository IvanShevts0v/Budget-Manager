import { FormEvent, useEffect, useState } from "react";
import {
  createCategory,
  deleteCategory,
  getCategories,
  patchCategory,
  searchCategoriesByName,
} from "../api/categoriesApi";
import type { NamedEntity } from "../api/types";

export default function CategoriesPage() {
  const [categories, setCategories] = useState<NamedEntity[]>([]);
  const [name, setName] = useState("");
  const [filterName, setFilterName] = useState("");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingName, setEditingName] = useState("");
  const [error, setError] = useState<string | null>(null);

  const loadAll = () => {
    getCategories()
      .then(setCategories)
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load categories"));
  };

  useEffect(loadAll, []);

  useEffect(() => {
    if (!filterName.trim()) {
      loadAll();
      return;
    }
    searchCategoriesByName(filterName.trim())
      .then(setCategories)
      .catch((err) => setError(err instanceof Error ? err.message : "Search failed"));
  }, [filterName]);

  const handleCreate = async (event: FormEvent) => {
    event.preventDefault();
    await createCategory(name);
    setName("");
    loadAll();
  };

  const handleSave = async (id: number) => {
    await patchCategory(id, editingName);
    setEditingId(null);
    loadAll();
  };

  const handleDelete = async (category: NamedEntity) => {
    if (!window.confirm(`Delete category ${category.name}?`)) {
      return;
    }
    await deleteCategory(category.id);
    loadAll();
  };

  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Categories</p>
          <h1>Categories</h1>
          <p className="lead">OneToMany: Category → Expenses</p>
        </div>
      </div>

      {error && <div className="alert error">{error}</div>}

      <section className="card">
        <h2>Filter by name</h2>
        <input
          value={filterName}
          onChange={(e) => setFilterName(e.target.value)}
          placeholder="Exact name search via /categories/by-name"
        />
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
    </div>
  );
}
