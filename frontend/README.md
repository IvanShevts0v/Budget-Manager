# Budget Manager — Frontend (React SPA)

React + TypeScript + Vite client for the Budget Manager REST API (similar to [student-forum](https://github.com/Yakush-A/student-forum) frontend).

## Features

- **SPA** on React 18 + react-router-dom
- **CRUD** for users, wallets, categories, tags, expenses
- **Filtering** via query params (`/expenses`, `/categories/by-name`, `/tags/by-name`, paginated `/expenses/by-wallet-and-category`)
- **Relationships in UI:**
  - **OneToMany:** User → Wallets, Wallet → Expenses, Category → Expenses
  - **ManyToMany:** Expense ↔ Tag (checkboxes + chips)

## Prerequisites

- Node.js 18+
- Backend running on **http://localhost:8080**

## Run (development)

```bash
# terminal 1 — backend
$env:DB_PASSWORD="your_password"
mvn spring-boot:run

# terminal 2 — frontend
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**

API calls go to `/api/*` and are proxied to `http://localhost:8080` (see `vite.config.ts`).

## Build

```bash
cd frontend
npm run build
npm run preview
```

## Pages

| Route | Description |
|-------|-------------|
| `/` | Register or select user |
| `/expenses` | Expense list, filters, create/edit modal with tags |
| `/users` | User list CRUD |
| `/users/:id` | User profile + wallets + nested expenses |
| `/wallets` | Wallets for selected user + expenses per wallet |
| `/categories` | Category CRUD + name filter |
| `/tags` | Tag CRUD + name filter |
