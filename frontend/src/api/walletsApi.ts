import { request, requestVoid } from "./client";
import type { WalletRequest, WalletResponse } from "./types";

export function getWallets(userId?: number) {
  const query = userId != null ? `?userId=${userId}` : "";
  return request<WalletResponse[]>(`/wallets${query}`);
}

export function getWallet(id: number) {
  return request<WalletResponse>(`/wallets/${id}`);
}

export function createWallet(body: WalletRequest) {
  return request<WalletResponse>("/wallets", {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export function renameWallet(id: number, name: string) {
  return request<WalletResponse>(`/wallets/${id}`, {
    method: "PATCH",
    body: JSON.stringify({ name }),
  });
}

export function deleteWallet(id: number) {
  return requestVoid(`/wallets/${id}`, { method: "DELETE" });
}
