import { useMemo } from 'react';

import { convertDate } from '@/lib/utils/timeConversion';
import { LeadList, LeadListSearch } from '@/types/lead-list.types';
import { ColumnType } from '@/types/table.types';
import { ListRowActions } from '../_components/ListRowActions';

export const useLeadListColumns = (
  onDelete: (list: LeadList) => void,
  onView: (id: string) => void
): ColumnType<LeadListSearch>[] => {
  return useMemo(
    () => [
      {
        id: 'name',
        header: 'List Name',
        sortable: true,
        width: '30%',
        renderCell: (row) => (
          <span className="text-foreground text-sm font-medium">{row.name}</span>
        ),
      },
      {
        id: 'sourceIds',
        header: 'Records',
        width: '20%',
        renderCell: (row) => (
          <span className="text-foreground text-sm">{row.sourceIds.length}</span>
        ),
      },
      {
        id: 'createdBy',
        header: 'Created By',
        width: '20%',
        renderCell: (row) => <span className="text-foreground text-sm">{row.username}</span>,
      },
      {
        id: 'updatedAt',
        header: 'Last Modified',
        width: '20%',
        sortable: true,
        renderCell: (row) => (
          <span className="text-muted-foreground text-sm">
            {convertDate(new Date(row.updatedAt))}
          </span>
        ),
      },
      {
        id: 'actions',
        header: '',
        width: '10%',
        align: 'right',
        renderCell: (row) => <ListRowActions list={row} onView={onView} onDelete={onDelete} />,
      },
    ],
    [onDelete, onView]
  );
};
