import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import ErrorBoundary from "./components/ErrorBoundary";
import TopBar from "./components/TopBar";
import CategoriesPage from "./pages/CategoriesPage";
import ExpensesPage from "./pages/ExpensesPage";
import HomePage from "./pages/HomePage";
import TagsPage from "./pages/TagsPage";
import UserPage from "./pages/UserPage";
import UsersPage from "./pages/UsersPage";
import WalletsPage from "./pages/WalletsPage";
import { AppProvider } from "./state/AppContext";

function AppLayout() {
  return (
    <div className="app-shell">
      <TopBar />
      <ErrorBoundary>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/expenses" element={<ExpensesPage />} />
          <Route path="/users" element={<UsersPage />} />
          <Route path="/users/:id" element={<UserPage />} />
          <Route path="/wallets" element={<WalletsPage />} />
          <Route path="/categories" element={<CategoriesPage />} />
          <Route path="/tags" element={<TagsPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </ErrorBoundary>
    </div>
  );
}

export default function App() {
  return (
    <AppProvider>
      <BrowserRouter>
        <AppLayout />
      </BrowserRouter>
    </AppProvider>
  );
}
