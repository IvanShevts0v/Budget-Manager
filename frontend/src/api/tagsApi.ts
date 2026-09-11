import { request, requestVoid } from "./client";
import { LOOKUP_SIZE, pageQuery, type PageParams } from "./paging";
import type { NamedEntity, PageResponse } from "./types";

export function getTags(params: PageParams = {}) {
  return request<PageResponse<NamedEntity>>(`/tags${pageQuery(params)}`);
}

export function getTagsLookup() {
  return getTags({ page: 0, size: LOOKUP_SIZE, sort: "id" });
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
