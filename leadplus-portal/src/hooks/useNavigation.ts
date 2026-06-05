import { useMemo } from 'react';

import { useAuth } from '@/context/AuthContext';
import { NavigationItem } from '@/types/navigation.types';
import { roleAccessMap } from '@/app/(modules)/leadgen/(dashboard-protected)/_components/roleToMapNavigation';
import { leadgenNavList, tenantOwnerNavList } from '@/app/(modules)/leadgen/_config/navigationList';

export const useNavigation = () => {
  const { loggedInUser } = useAuth();
  const roles = loggedInUser?.roles;

  const filteredPages = useMemo(() => {
    if (!roles) return [];

    const allowedIds = roles.flatMap(
      (role) => roleAccessMap[role as keyof typeof roleAccessMap] ?? []
    );

    return [...leadgenNavList, ...tenantOwnerNavList]
      .map((item) => {
        if (item.children?.length) {
          const filteredChildren = item.children.filter((child) => allowedIds.includes(child.id));

          if (filteredChildren.length === 0) return null;

          return {
            ...item,
            children: filteredChildren,
          };
        }
        return allowedIds.includes(item.id) ? item : null;
      })
      .filter(Boolean) as NavigationItem[];
  }, [roles]);

  return { filteredPages };
};
