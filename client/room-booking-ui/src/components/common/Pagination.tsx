import { ChevronLeft, ChevronRight } from "lucide-react";

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
  onPageChange: (page: number) => void;
}

export default function Pagination({
  currentPage,
  totalPages,
  totalElements,
  pageSize,
  onPageChange,
}: PaginationProps) {
  if (totalPages === 0) return null;

  const startItem = currentPage * pageSize + 1;
  const endItem = Math.min((currentPage + 1) * pageSize, totalElements);

  const maxVisible = 5;
  let startPage = Math.max(0, currentPage - Math.floor(maxVisible / 2));
  let endPage = startPage + maxVisible - 1;

  if (endPage >= totalPages) {
    endPage = totalPages - 1;
    startPage = Math.max(0, endPage - maxVisible + 1);
  }

  const pageNumbers: number[] = [];
  for (let i = startPage; i <= endPage; i++) {
    pageNumbers.push(i);
  }

  const isPrevDisabled = currentPage === 0;
  const isNextDisabled = currentPage === totalPages - 1;

  return (
    <div className="flex items-center justify-between px-4 py-3 border-t border-gray-200">
      <div className="text-sm text-gray-500">
        Showing {startItem} to {endItem} of {totalElements} results
      </div>
      <div className="flex items-center gap-1">
        <button
          onClick={() => !isPrevDisabled && onPageChange(currentPage - 1)}
          disabled={isPrevDisabled}
          className="inline-flex items-center justify-center w-8 h-8 rounded-md border border-gray-300 text-sm font-medium text-gray-600 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
        >
          <ChevronLeft className="w-4 h-4" />
        </button>
        {pageNumbers.map((page) => (
          <button
            key={page}
            onClick={() => onPageChange(page)}
            className={`inline-flex items-center justify-center w-8 h-8 rounded-md text-sm font-medium border transition-colors ${
              page === currentPage
                ? "bg-blue-600 text-white border-blue-600"
                : "border-gray-300 text-gray-600 hover:bg-gray-50"
            }`}
          >
            {page + 1}
          </button>
        ))}
        <button
          onClick={() => !isNextDisabled && onPageChange(currentPage + 1)}
          disabled={isNextDisabled}
          className="inline-flex items-center justify-center w-8 h-8 rounded-md border border-gray-300 text-sm font-medium text-gray-600 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
        >
          <ChevronRight className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}
