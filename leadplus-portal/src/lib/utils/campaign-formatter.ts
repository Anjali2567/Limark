export const getFormattedLocation = ({
  hqState,
  hqCountry,
}: {
  hqState?: string | null;
  hqCountry?: string | null;
}) => {
  const state = hqState || "";
  const country = hqCountry || "";
  const location = [state, country].filter(Boolean).join(", ");
  return location;
};
export const getFormattedEmployeeCount = (employees?: string | null) => {
  if (!employees) return employees;
  const parts = employees.trim().split(/\s+/);
  return parts.length > 1 ? parts.slice(0, -1).join(", ") : employees;
};
