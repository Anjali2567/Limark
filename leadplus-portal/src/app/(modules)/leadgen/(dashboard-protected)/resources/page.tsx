"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";

import { FileRender } from "@/components/FileRender";
import { SearchBar } from "@/components/SearchBar";
import { Cell } from "@/components/tables/Cell";
import DataTable from "@/components/tables/DataTable";
import { SortConfig } from "@/components/tables/SortableHeader";
import { useAuth } from "@/context/AuthContext";
import { useGetAllAttachments } from "@/hooks/useAttachments";
import { formatFileSize } from "@/lib/utils/file";
import { convertDateISO } from "@/lib/utils/timeConversion";
import { Attachment } from "@/types/attachment.types";
import { defaultPaginationParams } from "@/types/paginated-params";
import { ColumnType, FilterOption } from "@/types/table.types";
import { AddResource } from "./AddResource";
import { FILE_TYPE_FILTERS } from "./_data/filterOptions";
import { ActionHandler } from "./_components/ActionHandler";

const ResourcesPage = () => {
  const router = useRouter();
  const { authenticatedUserDetails } = useAuth();
  const searchParams = useSearchParams();

  const page = searchParams.get("page") || "0";

  const [searchQuery, setSearchQuery] = useState<string>("");
  const [sortConfig, setSortConfig] = useState<SortConfig | null>(null);
  const [filterOption, setFilterOption] = useState<FilterOption | null>(null);

  const filtersMap: Record<string, FilterOption | null> = {
    format: filterOption,
  };

  const handlePageChange = (newPage: number) => {
    const params = new URLSearchParams(searchParams.toString());
    params.set("page", newPage.toString());
    router.push(`?${params.toString()}`);
  };

  const onFiltersChange = (key: string, filter: FilterOption | null) => {
    handlePageChange(0);
    if (key === "format") {
      setFilterOption(filter);
    }
  };

  const onSortChange = (sort: SortConfig | null) => {
    handlePageChange(0);
    setSortConfig(sort);
  };

  const { data: attachments, isLoading } = useGetAllAttachments({
    tenantId: authenticatedUserDetails?.tenantId || "",
    workspaceId: authenticatedUserDetails?.workspaceId || "",
    params: {
      ...defaultPaginationParams,
      query: searchQuery,
      page: Number(page),
      sort: sortConfig
        ? `${sortConfig.key},${sortConfig.direction}`
        : defaultPaginationParams.sort,
      ...(filterOption?.value && { fileTypes: [filterOption.value] }),
    },
  });

  const columns: ColumnType<Attachment>[] = [
    {
      id: "filename",
      header: "File Name",
      width: "40%",
      sortable: true,
      renderCell: (row) => (
        <FileRender
          fileName={row.filename}
          fileUrl={row.fileUrl}
          fileExtension={row.fileType}
        />
      ),
    },
    {
      id: "size",
      header: "Size",
      width: "15%",
      renderCell: (row) => <Cell value={formatFileSize(row.sizeBytes)} />,
    },
    {
      id: "format",
      header: "Format",
      width: "15%",
      filterable: true,
      filterOptions: FILE_TYPE_FILTERS,
      renderCell: (row) => (
        <Cell value={row?.fileType?.toUpperCase()} isTag={!!row?.fileType} />
      ),
    },
    {
      id: "updatedAt",
      sortable: true,
      header: "Modified Date",
      width: "20%",
      renderCell: (row) => (
        <Cell value={convertDateISO(new Date(row.updatedAt))} />
      ),
    },
    {
      id: "actions",
      header: "",
      width: "10%",
      renderCell: (row) => <ActionHandler data={row} />,
      align: "right",
    },
  ];

  return (
    <div className="p-8 space-y-6 animate-in fade-in slide-in-from-right-4 duration-500">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-foreground">Resources</h1>
          <p className="text-muted-foreground">
            Manage your documents and assets.
          </p>
        </div>
        <AddResource />
      </div>
      <SearchBar
        value={searchQuery}
        className="flex-1 max-w-sm"
        placeholder="Search documents..."
        onChange={setSearchQuery}
      />
      <DataTable
        columns={columns}
        data={attachments?.content || []}
        pagination={attachments?.page}
        isLoading={isLoading}
        defaultMessage="No Attachments Found"
        sortConfig={sortConfig}
        onSortChange={onSortChange}
        filtersMap={filtersMap}
        onFiltersChange={onFiltersChange}
        initialPage={Number(page)}
        onPageChange={handlePageChange}
      />
    </div>
  );
};

export default ResourcesPage;
