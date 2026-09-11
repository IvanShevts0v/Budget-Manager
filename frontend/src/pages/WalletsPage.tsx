import { FormEvent, useEffect, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { getExpensesLookup } from "../api/expensesApi";
import { createWallet, deleteWallet, getWallets, renameWallet } from "../api/walletsApi";
import { PAGE_SIZE } from "../api/paging";
import type { ExpenseResponse, WalletResponse } from "../api/types";
import { useAppContext } from "../state/AppContext";
import PaginationBar from "../components/PaginationBar";

export default function WalletsPage() {
  const { selectedUserId } = useAppContext();
  const [wallets, setWallets] = useState<WalletResponse[]>([]);
  const [expenses, setExpenses] = useState<ExpenseResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [newWalletName, setNewWalletName] = useState("");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingName, setEditingName] = useState("");
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    if (selectedUserId == null) {
      return;
    }
    const [walletPage, expensePage] = await Promise.all([
      getWallets(selectedUserId, { page, size: PAGE_SIZE, sort: "id" }),
      getExpensesLookup({ senderUserId: selectedUserId }),
    ]);
    setWallets(walletPage.content);
    setTotalPages(walletPage.totalPages);
    setExpenses(expensePage.content);
  };

  useEffect(() => {
    load().catch((err) => setError(err instanceof Error ? err.message : "Failed to load wallets"));
  }, [selectedUserId, page]);

  if (selectedUserId == null) {
    return <Navigate to="/" replace />;
  }

  const handleCreate = async (event: FormEvent) => {
    event.preventDefault();
    await createWallet({ userId: selectedUserId, name: newWalletName });
    setNewWalletName("");
    await load();
  };

  const handleRename = async (walletId: number) => {
    await renameWallet(walletId, editingName);
    setEditingId(null);
    await load();
  };

  const handleDelete = async (wallet: WalletResponse) => {
    if (!window.confirm(`Delete wallet ${wallet.name}? Expenses in it will be removed.`)) {
      return;
    }
    await deleteWallet(wallet.id);
    await load();
  };

  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Wallets</p>
          <h1>Wallets for current user</h1>
          <p className="lead">OneToMany: Wallet → Expenses</p>
        </div>
      </div>

      {error && <div className="alert error">{error}</div>}

      <section className="card">
        <h2>Create wallet</h2>
        <form className="inline-form" onSubmit={handleCreate}>
          <input value={newWalletName} onChange={(e) => setNewWalletName(e.target.value)} required />
          <button type="submit" className="button">
            Create
          </button>
        </form>
      </section>

      <div className="relation-grid">
        {wallets.map((wallet) => {
          const walletExpenses = expenses.filter((expense) => expense.walletId === wallet.id);
          return (
            <article key={wallet.id} className="relation-card">
              <div className="relation-card-header">
                {editingId === wallet.id ? (
                  <form
                    className="inline-form"
                    onSubmit={(e) => {
                      e.preventDefault();
                      handleRename(wallet.id);
                    }}
                  >
                    <input value={editingName} onChange={(e) => setEditingName(e.target.value)} required />
                    <button type="submit" className="secondary-button">
                      Save
                    </button>
                  </form>
                ) : (
                  <h3>{wallet.name}</h3>
                )}
                <div className="actions-cell">
                  <button
                    type="button"
                    className="secondary-button"
                    onClick={() => {
                      setEditingId(wallet.id);
                      setEditingName(wallet.name);
                    }}
                  >
                    Rename
                  </button>
                  <button type="button" className="danger-button" onClick={() => handleDelete(wallet)}>
                    Delete
                  </button>
                </div>
              </div>
              <p className="muted">Owner: {wallet.username}</p>
              <p>{walletExpenses.length} expenses</p>
              <ul>
                {walletExpenses.map((expense) => (
                  <li key={expense.id}>
                    {expense.date}: {expense.amount.toFixed(2)} — {expense.category}
                  </li>
                ))}
              </ul>
              <Link to="/expenses" className="text-link">
                View all expenses
              </Link>
            </article>
          );
        })}
      </div>
      <PaginationBar page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}
