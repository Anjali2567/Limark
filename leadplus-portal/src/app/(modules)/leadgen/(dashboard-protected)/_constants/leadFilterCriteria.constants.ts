import {
  Award,
  Briefcase,
  Building2,
  CircleDollarSign,
  Cpu,
  Flag,
  Globe,
  Hash,
  Layers,
  LucideIcon,
  MailOpen,
  MapPin,
  Tag,
  User,
  UserCheck,
  UserCog,
  Users,
  Wrench,
} from 'lucide-react';

import { LeadFilterCriteria } from '@/types/leadSearch.types';

export type CriteriaDisplayConfig = {
  title: string;
  icon: LucideIcon;
  criteriaKeys: ReadonlyArray<keyof LeadFilterCriteria>;
  companyOnly?: boolean;
};

export const LEAD_FILTER_CRITERIA_DISPLAY_CONFIG: CriteriaDisplayConfig[] = [
  { title: 'Companies', icon: Building2, criteriaKeys: ['companyNames'] },
  { title: 'Contact Names', icon: User, criteriaKeys: ['contactNames'] },
  { title: 'Industries', icon: Briefcase, criteriaKeys: ['industries'] },
  {
    title: 'Company Location',
    icon: MapPin,
    criteriaKeys: ['companyCities', 'companyStates', 'companyCountries'],
  },
  { title: 'Contact Location', icon: MapPin, criteriaKeys: ['cities', 'states', 'countries'] },
  { title: 'Regions', icon: Globe, criteriaKeys: ['regions'] },
  { title: 'Employee Range', icon: Users, criteriaKeys: ['employeeRanges'] },
  {
    title: 'Revenue Range',
    icon: CircleDollarSign,
    criteriaKeys: ['revenueRanges'],
    companyOnly: true,
  },
  { title: 'Job Titles', icon: Award, criteriaKeys: ['titles'] },
  { title: 'Seniority', icon: Layers, criteriaKeys: ['seniority'] },
  { title: 'Departments', icon: Briefcase, criteriaKeys: ['departments'] },
  { title: 'Technologies', icon: Cpu, criteriaKeys: ['technologies'] },
  { title: 'Tools & Services', icon: Wrench, criteriaKeys: ['toolsServices'] },
  { title: 'Keywords', icon: Tag, criteriaKeys: ['keywords'] },
  { title: 'Postal Codes', icon: MailOpen, criteriaKeys: ['postalCodes'], companyOnly: true },
  { title: 'SIC Codes', icon: Hash, criteriaKeys: ['sicCodes'], companyOnly: true },
  { title: 'NAICS Codes', icon: Hash, criteriaKeys: ['naicsCodes'], companyOnly: true },
  { title: 'BD Name', icon: UserCheck, criteriaKeys: ['bdNames'] },
  { title: 'ISR Name', icon: UserCog, criteriaKeys: ['isrNames'] },
  { title: 'Priority', icon: Flag, criteriaKeys: ['priorities'] },
  { title: 'Title Category', icon: Tag, criteriaKeys: ['titleCategories'] },
];

const COMPUTED_CRITERIA_KEYS = new Set<keyof LeadFilterCriteria>(['companyNames']);

export const PEOPLE_SEARCH_DIRECT_CRITERIA_KEYS: ReadonlyArray<keyof LeadFilterCriteria> =
  LEAD_FILTER_CRITERIA_DISPLAY_CONFIG.filter((c) => !c.companyOnly)
    .flatMap((c) => c.criteriaKeys)
    .filter((k) => !COMPUTED_CRITERIA_KEYS.has(k))
    .filter((k, i, arr) => arr.indexOf(k) === i); // deduplicate
