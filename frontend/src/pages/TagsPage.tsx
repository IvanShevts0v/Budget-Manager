import { FormEvent, useEffect, useState } from "react";
import { createTag, deleteTag, getTags, getTagByName, patchTag } from "../api/tagsApi";
import type { NamedEntity } from "../api/types";
import { ListFooter, SortOrderFields, useListQuery } from "../components/ListControls";

export default function TagsPage() {
  const [tags, setTags] = useState<NamedEntity[]>([]);
  const [name, setName] = useState("");
  const [filterName, setFilterName] = useState("");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingName, setEditingName] = useState("");
  const [error, setError] = useState<string | null>(null);
  const list = useListQuery("id");
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
    getTags(list.query)
      .then((result) => {
        setTags(result.content);
        setTotalPages(result.totalPages);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load tags"));
  };

  useEffect(() => {
    list.setPage(0);
  }, [filterName]);

  useEffect(load, [filterName, list.page, list.sort, list.direction, list.size]);

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
      {!filterName.trim() && (
        <ListFooter
          page={list.page}
          totalPages={totalPages}
          onPageChange={list.setPage}
          size={list.size}
          onSizeChange={list.changeSize}
        />
      )}
    </div>
  );
}
