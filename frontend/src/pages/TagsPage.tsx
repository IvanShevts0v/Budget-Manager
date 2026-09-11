import { FormEvent, useEffect, useState } from "react";
import { createTag, deleteTag, getTags, getTagByName, patchTag } from "../api/tagsApi";
import { PAGE_SIZE } from "../api/paging";
import type { NamedEntity } from "../api/types";
import PaginationBar from "../components/PaginationBar";

export default function TagsPage() {
  const [tags, setTags] = useState<NamedEntity[]>([]);
  const [name, setName] = useState("");
  const [filterName, setFilterName] = useState("");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingName, setEditingName] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const load = () => {
    if (filterName.trim()) {
      getTagByName(filterName.trim())
        .then((tag) => {
          setTags([tag]);
          setTotalPages(1);
        })
        .catch(() => {
          setTags([]);
          setTotalPages(1);
        });
      return;
    }
    getTags({ page, size: PAGE_SIZE, sort: "id" })
      .then((result) => {
        setTags(result.content);
        setTotalPages(result.totalPages);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load tags"));
  };

  useEffect(() => {
    setPage(0);
  }, [filterName]);

  useEffect(load, [filterName, page]);

  const handleCreate = async (event: FormEvent) => {
    event.preventDefault();
    await createTag(name);
    setName("");
    load();
  };

  const handleSave = async (id: number) => {
    await patchTag(id, editingName);
    setEditingId(null);
    load();
  };

  const handleDelete = async (tag: NamedEntity) => {
    if (!window.confirm(`Delete tag ${tag.name}? It will be detached from expenses.`)) {
      return;
    }
    await deleteTag(tag.id);
    load();
  };

  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Tags</p>
          <h1>Tags</h1>
          <p className="lead">ManyToMany: Expense ↔ Tag (expense_tags)</p>
        </div>
      </div>

      {error && <div className="alert error">{error}</div>}

      <section className="card">
        <h2>Filter by exact name</h2>
        <input
          value={filterName}
          onChange={(e) => setFilterName(e.target.value)}
          placeholder="Search via /tags/by-name"
        />
      </section>

      <section className="card">
        <h2>Create tag</h2>
        <form className="inline-form" onSubmit={handleCreate}>
          <input value={name} onChange={(e) => setName(e.target.value)} required />
          <button type="submit" className="button">
            Create
          </button>
        </form>
      </section>

      <div className="chip-row tag-page-chips">
        {tags.map((tag) => (
          <span key={tag.id} className="chip static large">
            {tag.name}
          </span>
        ))}
      </div>

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
            {tags.map((tag) => (
              <tr key={tag.id}>
                <td>{tag.id}</td>
                <td>
                  {editingId === tag.id ? (
                    <input value={editingName} onChange={(e) => setEditingName(e.target.value)} />
                  ) : (
                    tag.name
                  )}
                </td>
                <td className="actions-cell">
                  {editingId === tag.id ? (
                    <button type="button" className="secondary-button" onClick={() => handleSave(tag.id)}>
                      Save
                    </button>
                  ) : (
                    <button
                      type="button"
                      className="secondary-button"
                      onClick={() => {
                        setEditingId(tag.id);
                        setEditingName(tag.name);
                      }}
                    >
                      Edit
                    </button>
                  )}
                  <button type="button" className="danger-button" onClick={() => handleDelete(tag)}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {!filterName.trim() && <PaginationBar page={page} totalPages={totalPages} onPageChange={setPage} />}
    </div>
  );
}
