import { useState } from "react";
import { PAGE_SIZE } from "../api/paging";
import PaginationBar from "./PaginationBar";

export const PAGE_SIZE_OPTIONS = [5, 10, 20, 50];

export interface SortOption {
  value: string;
  label: string;
}

interface SortOrderProps {
  sort: string;
  direction: "asc" | "desc";
  sortOptions: SortOption[];
  onSortChange: (sort: string) => void;
  onDirectionChange: (direction: "asc" | "desc") => void;
}

interface PageSizeProps {
  size: number;
  onSizeChange: (size: number) => void;
}

export function useListQuery(initialSort: string, initialDirection: "asc" | "desc" = "asc") {
  const [page, setPage] = useState(0);
  const [sort, setSort] = useState(initialSort);
  const [direction, setDirection] = useState<"asc" | "desc">(initialDirection);
  const [size, setSize] = useState(PAGE_SIZE);

  return {
    page,
    setPage,
    sort,
    direction,
    size,
    query: { page, size, sort: `${sort},${direction}` },
    changeSort: (value: string) => {
      setSort(value);
      setPage(0);
    },
    changeDirection: (value: "asc" | "desc") => {
      setDirection(value);
      setPage(0);
    },
    changeSize: (value: number) => {
      setSize(value);
      setPage(0);
    },
  };
}

export function SortOrderFields({
  sort,
  direction,
  sortOptions,
  onSortChange,
  onDirectionChange,
}: SortOrderProps) {
  return (
    <>
      <label>
        Sort
        <select value={sort} onChange={(event) => onSortChange(event.target.value)}>
          {sortOptions.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </label>
      <label>
        Order
        <select
          value={direction}
          onChange={(event) => onDirectionChange(event.target.value as "asc" | "desc")}
        >
          <option value="asc">Ascending</option>
          <option value="desc">Descending</option>
        </select>
      </label>
    </>
  );
}

export function ListFooter({
  page,
  totalPages,
  onPageChange,
  size,
  onSizeChange,
}: PageSizeProps & {
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}) {
  return (
    <div className="list-footer">
      <PaginationBar page={page} totalPages={totalPages} onPageChange={onPageChange} />
      <PageSizeField size={size} onSizeChange={onSizeChange} />
    </div>
  );
}

export function PageSizeField({ size, onSizeChange }: PageSizeProps) {
  return (
    <label className="page-size-field">
      Page size
      <select value={size} onChange={(event) => onSizeChange(Number(event.target.value))}>
        {PAGE_SIZE_OPTIONS.map((option) => (
          <option key={option} value={option}>
            {option}
          </option>
        ))}
      </select>
    </label>
  );
}
