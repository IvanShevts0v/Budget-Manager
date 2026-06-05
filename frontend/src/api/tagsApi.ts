import { request, requestVoid } from "./client";
import type { NamedEntity } from "./types";

export function getTags() {
  return request<NamedEntity[]>("/tags");
}

export function getTag(id: number) {
  return request<NamedEntity>(`/tags/${id}`);
}

export function getTagByName(name: string) {
  return request<NamedEntity>(`/tags/by-name?name=${encodeURIComponent(name)}`);
}

export function createTag(name: string) {
  return request<NamedEntity>("/tags", {
    method: "POST",
    body: JSON.stringify({ name }),
  });
}

export function patchTag(id: number, name: string) {
  return request<NamedEntity>(`/tags/${id}`, {
    method: "PATCH",
    body: JSON.stringify({ name }),
  });
}

export function deleteTag(id: number) {
  return requestVoid(`/tags/${id}`, { method: "DELETE" });
}
