"use client";
import { useEffect, useState } from "react";

import { Button } from "@/components/ui/button";
import { Page } from "@/types/paginated-params";

import { ChevronLeft, ChevronRight } from "lucide-react";

type PaginationProps = {
  params: Page;
  initialPage?: number;
  onPageChange?: (page: number) => void;
};

const Pagination = ({
  params,
  initialPage = 0,
  onPageChange,
}: PaginationProps) => {
  const { totalPages, size, totalElements } = params;
  const [currentPage, setCurrentPage] = useState<number>(initialPage);

  useEffect(() => {
    setCurrentPage(initialPage);
  }, [initialPage]);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
    onPageChange?.(page);
  };

  const getPageNumbers = () => {
    const pages = [];
    const maxPagesToShow = 5;

    if (totalPages <= maxPagesToShow) {
      for (let i = 0; i < totalPages; i++) {
        pages.push(i);
      }
    } else {
      if (currentPage < 3) {
        for (let i = 0; i < 4; i++) pages.push(i);
        pages.push("...");
        pages.push(totalPages - 1);
      } else if (currentPage >= totalPages - 3) {
        pages.push(0);
        pages.push("...");
        for (let i = totalPages - 4; i < totalPages; i++) pages.push(i);
      } else {
        pages.push(0);
        pages.push("...");
        pages.push(currentPage - 1);
        pages.push(currentPage);
        pages.push(currentPage + 1);
        pages.push("...");
        pages.push(totalPages - 1);
      }
    }

    return pages;
  };

  return (
    <div className="flex flex-col items-center gap-3">
      {totalElements === 0 ? (
        <p className="text-sm text-muted-foreground">Showing 0 results</p>
      ) : (
        <p className="text-sm text-muted-foreground">
          Showing {currentPage * size + 1} to{" "}
          {Math.min((currentPage + 1) * size, totalElements)} of {totalElements}{" "}
          results
        </p>
      )}
      <div
        className="flex items-center justify-center gap-1"
        role="navigation"
        aria-label="Pagination Navigation"
      >
        <Button
          variant="outline"
          size="sm"
          disabled={currentPage === 0}
          onClick={() => handlePageChange(currentPage - 1)}
          className="mr-2"
          aria-label="Go to previous page"
        >
          <ChevronLeft className="h-4 w-4" />
        </Button>
        {getPageNumbers().map((page, index) =>
          page === "..." ? (
            <span
              key={`ellipsis-${index}`}
              className="px-2 text-muted-foreground"
              aria-hidden="true"
            >
              ...
            </span>
          ) : (
            <Button
              key={page}
              variant={currentPage === page ? "default" : "outline"}
              size="sm"
              onClick={() => typeof page === "number" && handlePageChange(page)}
              className={
                currentPage === page
                  ? "bg-sky-500 hover:bg-sky-600 text-white"
                  : ""
              }
              aria-label={`Page ${(page as number) + 1}`}
              aria-current={currentPage === page ? "page" : undefined}
            >
              {(page as number) + 1}
            </Button>
          )
        )}
        <Button
          variant="outline"
          size="sm"
          disabled={currentPage === totalPages - 1 || totalPages === 0}
          onClick={() => handlePageChange(currentPage + 1)}
          className="ml-2"
          aria-label="Go to next page"
        >
          <ChevronRight className="h-4 w-4" />
        </Button>
      </div>
    </div>
  );
};

export { Pagination };
