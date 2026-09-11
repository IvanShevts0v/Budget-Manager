import { request, requestVoid } from "./client";
import { LOOKUP_SIZE, pageQuery, type PageParams } from "./paging";
import type { NamedEntity, PageResponse } from "./types";

export function getCategories(params: PageParams = {}) {
  return request<PageResponse<NamedEntity>>(`/categories${pageQuery(params)}`);
}

export function getCategoriesLookup() {
  return getCategories({ page: 0, size: LOOKUP_SIZE, sort: "id" });
}

export function getCategory(id: number) {
  return request<NamedEntity>(`/categories/${id}`);
}

export function searchCategoriesByName(name: string, params: PageParams = {}) {
  return request<PageResponse<NamedEntity>>(
    `/categories/by-name${pageQuery(params, { name })}`
  );
}

export function createCategory(name: string) {
  return request<NamedEntity>("/categories", {
    method: "POST",
    body: JSON.stringify({ name }),
  });
}

export function updateCategory(id: number, name: string) {
  return request<NamedEntity>(`/categories/${id}`, {
    method: "PUT",
    body: JSON.stringify({ name }),
  });
}

export function patchCategory(id: number, name: string) {
  return request<NamedEntity>(`/categories/${id}`, {
    method: "PATCH",
    body: JSON.stringify({ name }),
  });
}

export function deleteCategory(id: number) {
  return requestVoid(`/categories/${id}`, { method: "DELETE" });
}
