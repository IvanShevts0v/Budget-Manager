import { FormEvent, useEffect, useState } from "react";
import { getCategoriesLookup } from "../api/categoriesApi";
import { getTagsLookup } from "../api/tagsApi";
import { getWalletsLookup } from "../api/walletsApi";
import type { ExpenseRequest, ExpenseResponse, NamedEntity, WalletResponse } from "../api/types";

interface ExpenseModalProps {
  open: boolean;
  userId: number;
  expense?: ExpenseResponse | null;
  onClose: () => void;
  onSave: (body: ExpenseRequest) => Promise<void>;
}

export default function ExpenseModal({ open, userId, expense, onClose, onSave }: ExpenseModalProps) {
  const [description, setDescription] = useState("");
  const [amount, setAmount] = useState("");
  const [date, setDate] = useState("");
  const [walletId, setWalletId] = useState<number | "">("");
  const [categoryId, setCategoryId] = useState<number | "">("");
  const [tagIds, setTagIds] = useState<number[]>([]);
  const [wallets, setWallets] = useState<WalletResponse[]>([]);
  const [categories, setCategories] = useState<NamedEntity[]>([]);
  const [tags, setTags] = useState<NamedEntity[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!open) {
      return;
    }
    Promise.all([getWalletsLookup(userId), getCategoriesLookup(), getTagsLookup()])
      .then(([walletPage, categoryPage, tagPage]) => {
        setWallets(walletPage.content);
        setCategories(categoryPage.content);
        setTags(tagPage.content);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load form data"));
  }, [open, userId]);

  useEffect(() => {
    if (!open) {
      return;
    }
    if (expense) {
      setDescription(expense.description ?? "");
      setAmount(String(expense.amount));
      setDate(expense.date);
      setWalletId(expense.walletId);
      const matchedCategory = categories.find((c) => c.name === expense.category);
      setCategoryId(matchedCategory?.id ?? "");
      const matchedTagIds = tags.filter((t) => expense.tags.includes(t.name)).map((t) => t.id);
      setTagIds(matchedTagIds);
    } else {
      setDescription("");
      setAmount("");
      setDate(new Date().toISOString().slice(0, 10));
      setWalletId(wallets[0]?.id ?? "");
      setCategoryId(categories[0]?.id ?? "");
      setTagIds([]);
    }
    setError(null);
  }, [open, expense, wallets, categories, tags]);

  const toggleTag = (id: number) => {
    setTagIds((prev) => (prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]));
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (walletId === "" || categoryId === "") {
      setError("Wallet and category are required");
      return;
    }
    setSaving(true);
    setError(null);
    try {
      await onSave({
        description: description || undefined,
        amount: Number(amount),
        date,
        walletId,
        categoryId,
        tagIds,
      });
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Save failed");
    } finally {
      setSaving(false);
    }
  };

  if (!open) {
    return null;
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{expense ? "Edit expense" : "New expense"}</h2>
          <button type="button" className="icon-button" onClick={onClose}>
            ×
          </button>
        </div>
        <form className="form-grid" onSubmit={handleSubmit}>
          <label>
            Description
            <input value={description} onChange={(e) => setDescription(e.target.value)} />
          </label>
          <label>
            Amount
            <input type="number" min="0.01" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} required />
          </label>
          <label>
            Date
            <input type="date" value={date} onChange={(e) => setDate(e.target.value)} required />
          </label>
          <label>
            Wallet
            <select value={walletId} onChange={(e) => setWalletId(Number(e.target.value))} required>
              <option value="" disabled>
                Select wallet
              </option>
              {wallets.map((wallet) => (
                <option key={wallet.id} value={wallet.id}>
                  {wallet.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Category
            <select value={categoryId} onChange={(e) => setCategoryId(Number(e.target.value))} required>
              <option value="" disabled>
                Select category
              </option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </label>
          <fieldset className="tag-fieldset">
            <legend>Tags</legend>
            <div className="tag-grid">
              {tags.map((tag) => (
                <label key={tag.id} className="checkbox-chip">
                  <input
                    type="checkbox"
                    checked={tagIds.includes(tag.id)}
                    onChange={() => toggleTag(tag.id)}
                  />
                  {tag.name}
                </label>
              ))}
            </div>
          </fieldset>
          {error && <div className="alert error">{error}</div>}
          <div className="modal-actions">
            <button type="button" className="secondary-button" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="button" disabled={saving}>
              {saving ? "Saving..." : "Save"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
