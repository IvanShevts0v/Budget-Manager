export const PAGE_SIZE = 10;
export const LOOKUP_SIZE = 100;

export interface PageParams {
  page?: number;
  size?: number;
  sort?: string;
}

export function buildQuery(params: Record<string, string | number | undefined>): string {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== "") {
      search.set(key, String(value));
    }
  });
  const query = search.toString();
  return query ? `?${query}` : "";
}

export function pageQuery(params: PageParams = {}, extra: Record<string, string | number | undefined> = {}): string {
  return buildQuery({
    page: params.page ?? 0,
    size: params.size ?? PAGE_SIZE,
    sort: params.sort,
    ...extra,
  });
}
