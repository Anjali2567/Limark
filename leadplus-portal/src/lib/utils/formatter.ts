const capitalize = (value: string) => value.charAt(0).toUpperCase() + value.slice(1);

export const formatName = (fullName: {
  title?: string | null;
  firstName?: string | null;
  lastName?: string | null;
}): string => {
  const { title, firstName, lastName } = fullName;
  const parts: string[] = [];

  const trimmedTitle = title?.trim();
  const trimmedFirstName = firstName?.trim();
  const trimmedLastName = lastName?.trim();

  const hasFirstNameOrLastName = trimmedFirstName || trimmedLastName;

  if (trimmedTitle && hasFirstNameOrLastName) {
    const formattedTitle = capitalize(trimmedTitle);
    parts.push(formattedTitle.endsWith('.') ? formattedTitle : `${formattedTitle}.`);
  }

  if (trimmedFirstName) {
    parts.push(capitalize(trimmedFirstName));
  }

  if (trimmedLastName) {
    parts.push(capitalize(trimmedLastName));
  }

  return parts.join(' ');
};

export const formatDecimal = (value: number, dp: number = 2): number => {
  return Math.round(value * Math.pow(10, dp)) / Math.pow(10, dp);
};

export function formatTime(time: Date | string | null | undefined): string | null {
  if (!time) return null;
  const d = new Date(time);
  if (isNaN(d.getTime())) return null;
  return d.toLocaleString('en-US', { hour: '2-digit', minute: '2-digit', hour12: true });
}

export function formatDate(date: Date | string | null | undefined): string | null {
  if (!date) return null;
  const d = new Date(date);
  if (isNaN(d.getTime())) return null;
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

export const trimValues = <T>(data: T): T => {
  if (typeof data === 'string') {
    return data.trim() as T;
  }

  if (Array.isArray(data)) {
    return data.map((item) => trimValues(item)) as T;
  }

  if (data && typeof data === 'object' && !Array.isArray(data)) {
    const newEntries = Object.entries(data).map(([key, value]) => [key, trimValues(value)]);

    return Object.fromEntries(newEntries) as T;
  }

  return data;
};

export const formatString = (value?: string): string => {
  if (!value) return '';

  return value
    .toString()
    .replace(/_/g, ' ')
    .toLowerCase()
    .split(' ')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
};

export const formatLocation = (location: {
  street?: string | null;
  city?: string | null;
  state?: string | null;
  postalCode?: string | null;
  country?: string | null;
}): string => {
  const { street, city, state, postalCode, country } = location;

  const parts: string[] = [];

  const sanitize = (val: string | null | undefined) => {
    const trimmed = val?.trim();
    return trimmed && trimmed.toLowerCase() !== 'null' ? trimmed : undefined;
  };

  const trimmedStreet = sanitize(street);
  const trimmedCity = sanitize(city);
  const trimmedState = sanitize(state);
  const trimmedCountry = sanitize(country);
  const trimmedPostalCode = sanitize(postalCode);

  if (trimmedStreet) {
    parts.push(capitalize(trimmedStreet));
  }

  if (trimmedCity) {
    parts.push(capitalize(trimmedCity));
  }

  if (trimmedState) {
    parts.push(capitalize(trimmedState));
  }

  if (trimmedCountry) {
    parts.push(capitalize(trimmedCountry));
  }

  if (trimmedPostalCode) {
    parts.push(trimmedPostalCode.toUpperCase());
  }

  return parts.join(', ');
};

export const formatCompactNumber = (value: string | number | null | undefined) => {
  if (value === null || value === undefined || value === '') return '-';

  const strValue = value.toString().trim();

  if (/[kmb]$/i.test(strValue)) {
    return strValue.toUpperCase();
  }

  const numberValue = Number(strValue);

  if (Number.isNaN(numberValue)) return '-';

  if (numberValue >= 1_000_000_000) {
    return `${(numberValue / 1_000_000_000).toFixed(1)}B`;
  }

  if (numberValue >= 1_000_000) {
    return `${(numberValue / 1_000_000).toFixed(1)}M`;
  }

  if (numberValue >= 1_000) {
    return `${(numberValue / 1_000).toFixed(1)}K`;
  }

  return `${numberValue.toFixed(0)}`;
};

export const formatRevenue = (revenue: string | number | null | undefined) => {
  const formatted = formatCompactNumber(revenue);
  if (formatted === '-') return '-';
  return `$${formatted}`;
};

export const formatRevenueRange = (revenue: string | number | null | undefined): string => {
  if (revenue === null || revenue === undefined || revenue === '') return '-';

  let valueInUsd: number;
  const strValue = revenue.toString().trim();
  if (!strValue) return '-';

  const match = strValue.match(/^(\d+(?:\.\d+)?)([KMBkmb])$/);
  if (match) {
    const num = parseFloat(match[1]);
    const suffix = match[2].toUpperCase();
    if (suffix === 'K') valueInUsd = num * 1_000;
    else if (suffix === 'M') valueInUsd = num * 1_000_000;
    else if (suffix === 'B') valueInUsd = num * 1_000_000_000;
    else return '-';
  } else {
    valueInUsd = Number(strValue);
  }

  if (!Number.isFinite(valueInUsd)) return '-';

  if (valueInUsd <= 1_000_000) return '< $1M';
  if (valueInUsd <= 5_000_000) return '$1M - $5M';
  if (valueInUsd <= 10_000_000) return '$5M - $10M';
  if (valueInUsd <= 50_000_000) return '$10M - $50M';
  if (valueInUsd <= 100_000_000) return '$50M - $100M';
  if (valueInUsd <= 500_000_000) return '$100M - $500M';
  if (valueInUsd <= 1_000_000_000) return '$500M - $1B';
  return '$1B+';
};

export const normalizePath = (path: string) => path.replace(/\/+$/, '');

const EMPLOYEE_RANGES: { label: string; min: number; max?: number }[] = [
  { label: '0-500', min: 0, max: 500 },
  { label: '500-1,000', min: 501, max: 1000 },
  { label: '1,000-5,000', min: 1001, max: 5000 },
  { label: '5,000-10,000', min: 5001, max: 10000 },
  { label: '10,000+', min: 10001 },
];

export const formatEmployeeRange = (value: string | number | null | undefined): string => {
  if (value == null || value === '') return '-';

  const num = Number(value.toString().replace(/,/g, '').match(/\d+/)?.[0]);

  if (!Number.isFinite(num)) return '-';

  const range = EMPLOYEE_RANGES.find((r) => num >= r.min && (r.max === undefined || num <= r.max));

  return range?.label ?? '-';
};
