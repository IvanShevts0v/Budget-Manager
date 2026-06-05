import { request, requestVoid } from "./client";
import type { UserRequest, UserResponse } from "./types";

export function getUsers() {
  return request<UserResponse[]>("/users");
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
