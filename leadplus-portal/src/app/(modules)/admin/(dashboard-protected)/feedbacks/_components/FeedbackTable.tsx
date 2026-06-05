import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useMemo, useState } from 'react';

import { SearchBar } from '@/components/SearchBar';
import { StarRating } from '@/components/StarRating';
import { Cell } from '@/components/tables/Cell';
import DataTable from '@/components/tables/DataTable';
import { ImageCell } from '@/components/tables/ImageCell';
import {
  FEEDBACK_CATEGORY_CONFIG,
  FEEDBACK_STATUS_CONFIG,
  FeedbackStatus,
  FeedbackType,
} from '@/constants/user-feedback.constant';
import { useGetAllFeedbacks } from '@/hooks/useFeedback';
import { getInitials } from '@/lib/utils/helpers';
import { convertDateISO } from '@/lib/utils/timeConversion';
import { defaultPaginationParams } from '@/types/paginated-params';
import { ColumnType, FilterOption } from '@/types/table.types';
import { Feedback } from '@/types/user-feedback.types';
import { CategoryBadge, StatusBadge } from './FeedbackBadges';
import { FeedbackDetailDialog } from './FeedbackDetailDialog';
import { FeedbackReplyDialog } from './FeedbackReplyDialog';
import { FeedbackTableActions } from './FeedbackTableActions';

const FeedbackTable = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const page = searchParams.get('page') || '0';

  const [searchQuery, setSearchQuery] = useState<string>('');
  const [filtersMap, setFiltersMap] = useState<Record<string, FilterOption | null>>({});
  const [selectedFeedback, setSelectedFeedback] = useState<Feedback | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [replyOpen, setReplyOpen] = useState(false);

  const { data, isLoading } = useGetAllFeedbacks({
    ...defaultPaginationParams,
    page,
    query: searchQuery,
    ...(filtersMap.status?.value ? { status: [filtersMap.status.value as FeedbackStatus] } : {}),
    ...(filtersMap.type?.value ? { types: [filtersMap.type.value as FeedbackType] } : {}),
  });

  const handlePageChange = useCallback(
    (newPage: number) => {
      const params = new URLSearchParams(searchParams.toString());
      params.set('page', newPage.toString());
      router.push(`?${params.toString()}`);
    },
    [router, searchParams]
  );

  const handleSearch = useCallback(
    (query: string) => {
      handlePageChange(0);
      setSearchQuery(query);
    },
    [handlePageChange]
  );

  const handleFiltersChange = useCallback(
    (key: string, filter: FilterOption | null) => {
      handlePageChange(0);
      setFiltersMap((prev) => ({ ...prev, [key]: filter }));
    },
    [handlePageChange]
  );

  const handleRowClick = useCallback(
    (rowId: string) => {
      const feedback = data?.content.find((f) => f.id === rowId) ?? null;
      if (!feedback) return;
      setSelectedFeedback(feedback);
      setDetailOpen(true);
    },
    [data?.content]
  );

  const handleDetailClose = useCallback(() => {
    setDetailOpen(false);
    setSelectedFeedback(null);
  }, []);

  const handleOpenReply = useCallback(() => {
    setDetailOpen(false);
    setReplyOpen(true);
  }, []);

  const handleReplyClose = useCallback(() => {
    setReplyOpen(false);
    setSelectedFeedback(null);
  }, []);

  const columns: ColumnType<Feedback>[] = useMemo(
    () => [
      {
        id: 'username',
        header: 'User',
        width: '20%',
        renderCell: (row) => (
          <ImageCell
            text={row.username}
            subText={row.companyName}
            size={35}
            initials={getInitials(row.username)}
          />
        ),
      },
      {
        id: 'type',
        header: 'Category',
        width: '15%',
        filterable: true,
        filterOptions: [
          { label: 'All', value: null },
          ...Object.entries(FEEDBACK_CATEGORY_CONFIG).map(([value, config]) => ({
            label: config.label,
            value,
          })),
        ],
        renderCell: (row) => <CategoryBadge type={row.type} />,
      },
      {
        id: 'message',
        header: 'Message',
        width: '25%',
        renderCell: (row) => <Cell value={row.message} />,
      },
      {
        id: 'rating',
        header: 'Rating',
        width: '12%',
        renderCell: (row) => <StarRating rating={row.rating} />,
      },
      {
        id: 'status',
        header: 'Status',
        width: '12%',
        filterable: true,
        filterOptions: [
          { label: 'All', value: null },
          ...Object.entries(FEEDBACK_STATUS_CONFIG).map(([value, config]) => ({
            label: config.label,
            value,
          })),
        ],
        renderCell: (row) => <StatusBadge status={row.status} />,
      },
      {
        id: 'feedbackDate',
        header: 'Date',
        width: '10%',
        renderCell: (row) => (
          <Cell value={row.feedbackDate ? convertDateISO(new Date(row.feedbackDate)) : ''} />
        ),
      },
      {
        id: 'actions',
        header: '',
        align: 'right',
        width: '6%',
        renderCell: (row) => <FeedbackTableActions feedback={row} />,
      },
    ],
    []
  );

  return (
    <div className="space-y-6">
      <SearchBar
        className="max-w-md"
        placeholder="Search feedback..."
        value={searchQuery}
        onChange={handleSearch}
      />
      <DataTable
        rowClassName="h-14"
        columns={columns}
        data={data?.content || []}
        pagination={data?.page}
        isLoading={isLoading}
        defaultMessage="No feedback found"
        initialPage={Number(page)}
        onPageChange={handlePageChange}
        onRowClick={handleRowClick}
        filtersMap={filtersMap}
        onFiltersChange={handleFiltersChange}
      />
      {selectedFeedback && (
        <>
          <FeedbackDetailDialog
            feedback={selectedFeedback}
            open={detailOpen}
            onOpenChange={(open) => !open && handleDetailClose()}
            onReply={handleOpenReply}
          />
          <FeedbackReplyDialog
            feedback={selectedFeedback}
            open={replyOpen}
            onOpenChange={(open) => !open && handleReplyClose()}
          />
        </>
      )}
    </div>
  );
};

export { FeedbackTable };
