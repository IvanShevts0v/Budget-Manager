import { request, requestVoid } from "./client";
import type { NamedEntity } from "./types";

export function getCategories() {
  return request<NamedEntity[]>("/categories");
}

export function getCategory(id: number) {
  return request<NamedEntity>(`/categories/${id}`);
}

export function searchCategoriesByName(name: string) {
  return request<NamedEntity[]>(`/categories/by-name?name=${encodeURIComponent(name)}`);
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
