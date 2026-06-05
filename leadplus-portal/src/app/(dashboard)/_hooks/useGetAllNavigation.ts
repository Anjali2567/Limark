import { useMemo } from 'react';

import { navigationList as adminNavList } from '@/app/(modules)/admin/_config/navigationList';
import { navigationList as customerNavList } from '@/app/(modules)/customer/_config/navigationList';
import { leadgenNavList, tenantOwnerNavList } from '@/app/(modules)/leadgen/_config/navigationList';
import { navigationList as vendorNavList } from '@/app/(modules)/vendor/_config/navigationList';
import { appRoutes } from '@/config/routes';
import { PageOptions } from '@/constants/config.constants';
import { Module } from '@/constants/modules.constants';
import { useModule } from '@/context/ModuleContext';
import { NavigationItem } from '@/types/navigation.types';

import { Megaphone, Store } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { UserRoles } from '@/constants/user.constants';

const moduleNavigationMap: Record<Module, NavigationItem[]> = {
  [Module.VENDOR]: vendorNavList,
  [Module.LEAD_GENERATION]: [...leadgenNavList, ...tenantOwnerNavList],
  [Module.ADMINISTRATION]: adminNavList,
  [Module.CUSTOMER]: customerNavList,
};

const userRoleModuleNavigationMap: Partial<Record<UserRoles, NavigationItem[]>> = {
  [UserRoles.VENDOR]: [...vendorNavList, ...leadgenNavList],
  [UserRoles.TENANT_OWNER]: [...leadgenNavList, ...tenantOwnerNavList],
  [UserRoles.ADMIN]: adminNavList,
  [UserRoles.CUSTOMER]: customerNavList,
};

const mergeChildren = (existing: NavigationItem[], incoming: NavigationItem[]): NavigationItem[] =>
  Array.from(
    incoming
      .reduce(
        (childMap, child) => childMap.set(child.id, child),
        new Map(existing.map((child) => [child.id, child]))
      )
      .values()
  );

const mergeNavigationItems = (items: NavigationItem[]): NavigationItem[] =>
  Array.from(
    items
      .reduce((map, item) => {
        const existing = map.get(item.id);

        if (!existing) {
          return map.set(
            item.id,
            item.children?.length ? { ...item, children: [...item.children] } : item
          );
        }

        if (item.children?.length) {
          existing.children = mergeChildren(existing.children ?? [], item.children);
        }

        return map;
      }, new Map<string, NavigationItem>())
      .values()
  );

const accessNavigationMap: Partial<Record<Module, NavigationItem>> = {
  [Module.VENDOR]: {
    id: PageOptions.VENDOR_ACCESS,
    label: 'Become a Vendor',
    icon: Store,
    href: appRoutes.vendor.root,
  },
  [Module.LEAD_GENERATION]: {
    id: PageOptions.LEAD_GENERATION_ACCESS,
    label: 'Get LeadGen Access',
    icon: Megaphone,
    href: appRoutes.leadgen.root,
  },
};

const collectAllowedIds = (userRoles: UserRoles[]): Set<PageOptions> => {
  const ids = new Set<PageOptions>();
  const collect = (items: NavigationItem[]) => {
    items.forEach((item) => {
      ids.add(item.id);
      if (item.children?.length) collect(item.children);
    });
  };
  userRoles.forEach((role) => collect(userRoleModuleNavigationMap[role] ?? []));
  return ids;
};

const filterByRoleMap = (
  items: NavigationItem[],
  allowedIds: Set<PageOptions>,
  userRoles: UserRoles[]
): NavigationItem[] =>
  items
    .filter((item) => {
      if (!allowedIds.has(item.id)) return false;
      if (
        item.allowedRoles?.length &&
        !item.allowedRoles.some((r) => userRoles.includes(r))
      )
        return false;
      return true;
    })
    .map((item) =>
      item.children?.length
        ? { ...item, children: filterByRoleMap(item.children, allowedIds, userRoles) }
        : item
    );

export const useGetAllNavigation = (): NavigationItem[] => {
  const { authenticatedUser } = useAuth();
  const { enabledModules } = useModule();

  return useMemo(() => {
    const combined = enabledModules.flatMap((module) => moduleNavigationMap[module] ?? []);
    Object.values(Module)
      .filter((module) => !enabledModules.includes(module))
      .forEach((module) => {
        const accessItem = accessNavigationMap[module];
        if (accessItem) combined.push(accessItem);
      });
    const merged = mergeNavigationItems(combined);
    const userRoles = authenticatedUser?.roles ?? [];
    if (!userRoles.length) return merged;
    const allowedIds = collectAllowedIds(userRoles);
    return filterByRoleMap(merged, allowedIds, userRoles);
  }, [enabledModules, authenticatedUser]);
};
