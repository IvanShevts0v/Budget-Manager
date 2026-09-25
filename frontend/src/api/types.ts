export interface UserRequest {
  username: string;
  defaultWalletName?: string;
}

export interface UserResponse {
  id: number;
  username: string;
  walletIds: number[];
  walletNames: string[];
}

export interface WalletRequest {
  userId: number;
  name: string;
}

export interface WalletResponse {
  id: number;
  name: string;
  userId: number;
  username: string;
}

export interface NamedEntity {
  id: number;
  name: string;
}

export interface ExpenseRequest {
  description?: string;
  amount?: number;
  date?: string;
  walletId?: number;
  categoryId?: number;
  tagIds?: number[];
}

export interface ExpenseResponse {
  id: number;
  description?: string;
  amount: number;
  date: string;
  category: string;
  walletName: string;
  walletId: number;
  userName: string;
  userId: number;
  tags: string[];
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface ExpenseFilters {
  senderUserId?: number;
  id?: number;
  description?: string;
  amount?: number;
  category?: string;
  date?: string;
  tags?: string[];
}

export interface PaginatedExpenseFilters {
  walletOwnerUserId?: number;
  categoryName?: string;
  page?: number;
  size?: number;
  sort?: string;
}
