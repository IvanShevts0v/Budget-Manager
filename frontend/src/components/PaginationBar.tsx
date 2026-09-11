interface PaginationBarProps {
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export default function PaginationBar({ page, totalPages, onPageChange }: PaginationBarProps) {
  const pages = Math.max(totalPages, 1);
  return (
    <div className="pagination">
      <button type="button" className="secondary-button" disabled={page === 0} onClick={() => onPageChange(page - 1)}>
        Previous
      </button>
      <span>
        Page {page + 1} / {pages}
      </span>
      <button
        type="button"
        className="secondary-button"
        disabled={page + 1 >= pages}
        onClick={() => onPageChange(page + 1)}
      >
        Next
      </button>
    </div>
  );
}
