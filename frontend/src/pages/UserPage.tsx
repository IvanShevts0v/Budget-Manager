import { FormEvent, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getExpenses } from "../api/expensesApi";
import { createWallet, getWallets } from "../api/walletsApi";
import { getUser, updateUser } from "../api/usersApi";
import type { ExpenseResponse, UserResponse, WalletResponse } from "../api/types";
import { useAppContext } from "../state/AppContext";

export default function UserPage() {
  const { id } = useParams();
  const userId = Number(id);
  const { setSelectedUserId } = useAppContext();
  const [user, setUser] = useState<UserResponse | null>(null);
  const [wallets, setWallets] = useState<WalletResponse[]>([]);
  const [expenses, setExpenses] = useState<ExpenseResponse[]>([]);
  const [username, setUsername] = useState("");
  const [newWalletName, setNewWalletName] = useState("");
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    const [userData, walletData, expenseData] = await Promise.all([
      getUser(userId),
      getWallets(userId),
      getExpenses({ senderUserId: userId }),
    ]);
    setUser(userData);
    setUsername(userData.username);
    setWallets(walletData);
    setExpenses(expenseData);
  };

  useEffect(() => {
    load().catch((err) => setError(err instanceof Error ? err.message : "Failed to load user"));
  }, [userId]);

  const handleRename = async (event: FormEvent) => {
    event.preventDefault();
    await updateUser(userId, { username });
    await load();
  };

  const handleCreateWallet = async (event: FormEvent) => {
    event.preventDefault();
    await createWallet({ userId, name: newWalletName });
    setNewWalletName("");
    await load();
  };

  if (!user) {
    return <div className="page">{error ? <div className="alert error">{error}</div> : "Loading..."}</div>;
  }

  const expensesByWallet = wallets.map((wallet) => ({
    wallet,
    items: expenses.filter((expense) => expense.walletId === wallet.id),
  }));

  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">User profile</p>
          <h1>{user.username}</h1>
          <p className="lead">OneToMany: User → Wallets → Expenses</p>
        </div>
        <button type="button" className="button" onClick={() => setSelectedUserId(user.id)}>
          Use as current user
        </button>
      </div>

      <section className="card">
        <h2>Rename user</h2>
        <form className="inline-form" onSubmit={handleRename}>
          <input value={username} onChange={(e) => setUsername(e.target.value)} required />
          <button type="submit" className="secondary-button">
            Save
          </button>
        </form>
      </section>

      <section className="card">
        <h2>Wallets (OneToMany)</h2>
        <form className="inline-form" onSubmit={handleCreateWallet}>
          <input
            value={newWalletName}
            onChange={(e) => setNewWalletName(e.target.value)}
            placeholder="New wallet name"
            required
          />
          <button type="submit" className="secondary-button">
            Add wallet
          </button>
        </form>
        <div className="relation-grid">
          {expensesByWallet.map(({ wallet, items }) => (
            <article key={wallet.id} className="relation-card">
              <h3>
                <Link to="/wallets">{wallet.name}</Link>
              </h3>
              <p className="muted">{items.length} expenses</p>
              <ul>
                {items.slice(0, 5).map((expense) => (
                  <li key={expense.id}>
                    {expense.date}: {expense.description || "Expense"} — {expense.amount.toFixed(2)} ({expense.category})
                    {expense.tags.length > 0 && (
                      <span className="chip-row inline-chips">
                        {expense.tags.map((tag) => (
                          <span key={tag} className="chip static">
                            {tag}
                          </span>
                        ))}
                      </span>
                    )}
                  </li>
                ))}
              </ul>
              {items.length > 5 && <p className="muted">+ {items.length - 5} more</p>}
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}
