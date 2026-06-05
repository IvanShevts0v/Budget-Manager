import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import type { UserResponse } from "../api/types";
import { getUser } from "../api/usersApi";

interface AppContextValue {
  selectedUserId: number | null;
  selectedUser: UserResponse | null;
  setSelectedUserId: (id: number | null) => void;
  refreshSelectedUser: () => Promise<void>;
}

const AppContext = createContext<AppContextValue | null>(null);
const STORAGE_KEY = "budget-manager-user-id";

export function AppProvider({ children }: { children: ReactNode }) {
  const [selectedUserId, setSelectedUserIdState] = useState<number | null>(() => {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? Number(raw) : null;
  });
  const [selectedUser, setSelectedUser] = useState<UserResponse | null>(null);

  const setSelectedUserId = (id: number | null) => {
    setSelectedUserIdState(id);
    if (id == null) {
      localStorage.removeItem(STORAGE_KEY);
      setSelectedUser(null);
    } else {
      localStorage.setItem(STORAGE_KEY, String(id));
    }
  };

  const refreshSelectedUser = async () => {
    if (selectedUserId == null) {
      setSelectedUser(null);
      return;
    }
    const user = await getUser(selectedUserId);
    setSelectedUser(user);
  };

  useEffect(() => {
    refreshSelectedUser().catch(() => setSelectedUser(null));
  }, [selectedUserId]);

  const value = useMemo(
    () => ({ selectedUserId, selectedUser, setSelectedUserId, refreshSelectedUser }),
    [selectedUserId, selectedUser]
  );

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useAppContext() {
  const ctx = useContext(AppContext);
  if (!ctx) {
    throw new Error("useAppContext must be used within AppProvider");
  }
  return ctx;
}
