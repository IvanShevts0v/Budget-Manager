import { request, requestVoid } from "./client";
import { LOOKUP_SIZE, pageQuery, type PageParams } from "./paging";
import type { PageResponse, WalletRequest, WalletResponse } from "./types";

export function getWallets(userId?: number, params: PageParams = {}) {
  return request<PageResponse<WalletResponse>>(
    `/wallets${pageQuery(params, { userId })}`
  );
}

export function getWalletsLookup(userId?: number) {
  return getWallets(userId, { page: 0, size: LOOKUP_SIZE, sort: "id" });
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
