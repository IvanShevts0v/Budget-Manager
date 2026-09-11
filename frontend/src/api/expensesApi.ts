import { request, requestVoid } from "./client";
import { LOOKUP_SIZE, buildQuery, pageQuery, type PageParams } from "./paging";
import type {
  ExpenseFilters,
  ExpenseRequest,
  ExpenseResponse,
  PageResponse,
  PaginatedExpenseFilters,
} from "./types";

export function getExpenses(filters: ExpenseFilters = {}, params: PageParams = {}) {
  return request<PageResponse<ExpenseResponse>>(
    `/expenses${pageQuery(params, {
      senderUserId: filters.senderUserId,
      id: filters.id,
      description: filters.description,
      amount: filters.amount,
      category: filters.category,
      date: filters.date,
    })}`
  );
}

export function getExpensesLookup(filters: ExpenseFilters = {}) {
  return getExpenses(filters, { page: 0, size: LOOKUP_SIZE, sort: "id" });
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
