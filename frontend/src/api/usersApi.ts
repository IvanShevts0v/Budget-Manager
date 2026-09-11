import { request, requestVoid } from "./client";
import { LOOKUP_SIZE, pageQuery, type PageParams } from "./paging";
import type { PageResponse, UserRequest, UserResponse } from "./types";

export function getUsers(params: PageParams = {}) {
  return request<PageResponse<UserResponse>>(`/users${pageQuery(params)}`);
}

export function getUsersLookup() {
  return getUsers({ page: 0, size: LOOKUP_SIZE, sort: "id" });
}

export function getUser(id: number) {
  return request<UserResponse>(`/users/${id}`);
}

export function registerUser(body: UserRequest) {
  return request<UserResponse>("/users/register", {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export function updateUser(id: number, body: UserRequest) {
  return request<UserResponse>(`/users/${id}`, {
    method: "PATCH",
    body: JSON.stringify(body),
  });
}

export function deleteUser(id: number) {
  return requestVoid(`/users/${id}`, { method: "DELETE" });
}
