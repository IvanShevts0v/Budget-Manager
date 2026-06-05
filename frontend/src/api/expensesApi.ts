import { request, requestVoid } from "./client";
import type {
  ExpenseFilters,
  ExpenseRequest,
  ExpenseResponse,
  PageResponse,
  PaginatedExpenseFilters,
} from "./types";

function buildQuery(params: Record<string, string | number | undefined>): string {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== "") {
      search.set(key, String(value));
    }
  });
  const query = search.toString();
  return query ? `?${query}` : "";
}

export function getExpenses(filters: ExpenseFilters = {}) {
  return request<ExpenseResponse[]>(
    `/expenses${buildQuery({
      senderUserId: filters.senderUserId,
      id: filters.id,
      description: filters.description,
      amount: filters.amount,
      category: filters.category,
      date: filters.date,
    })}`
  );
}

export function getExpensesPaginated(filters: PaginatedExpenseFilters = {}) {
  return request<PageResponse<ExpenseResponse>>(
    `/expenses/by-wallet-and-category${buildQuery({
      walletOwnerUserId: filters.walletOwnerUserId,
      categoryName: filters.categoryName,
      page: filters.page,
      size: filters.size,
      sort: filters.sort,
    })}`
  );
}

export function getExpense(id: number) {
  return request<ExpenseResponse>(`/expenses/${id}`);
}

export function createExpense(body: ExpenseRequest) {
  return request<ExpenseResponse>("/expenses", {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export function updateExpense(id: number, body: ExpenseRequest) {
  return request<ExpenseResponse>(`/expenses/${id}`, {
    method: "PUT",
    body: JSON.stringify(body),
  });
}

export function patchExpense(id: number, body: ExpenseRequest) {
  return request<ExpenseResponse>(`/expenses/${id}`, {
    method: "PATCH",
    body: JSON.stringify(body),
  });
}

export function deleteExpense(id: number) {
  return requestVoid(`/expenses/${id}`, { method: "DELETE" });
}
