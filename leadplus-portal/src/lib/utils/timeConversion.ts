export function convertTime(time: Date) {
  return time
    ? time.toLocaleString("en-US", {
        hour: "2-digit",
        minute: "2-digit",
        hour12: true,
      })
    : null;
}

export function convertDate(time: Date) {
  return time?.toLocaleDateString("en-US", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

export function convertDateISO(time: Date) {
  return time?.toISOString().split("T")[0];
}

export function convertDateTime(time: Date) {
  return time?.toLocaleString("en-US", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    hour12: true,
  });
}

export const formatDateTimeToReadable = (time: Date): string => {
  return (
    time.toLocaleDateString("en-US", {
      day: "2-digit",
      month: "long",
      year: "numeric",
    }) +
    " at " +
    time.toLocaleTimeString("en-US", {
      hour: "2-digit",
      minute: "2-digit",
      hour12: true,
    })
  );
};

export function timeAgo(time: Date): string {
  const seconds = Math.floor((Date.now() - time.getTime()) / 1000);
  if (seconds < 60) return "just now";
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes} min ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours} hr${hours !== 1 ? "s" : ""} ago`;
  const days = Math.floor(hours / 24);
  if (days < 7) return `${days} day${days !== 1 ? "s" : ""} ago`;
  const weeks = Math.floor(days / 7);
  if (weeks < 5) return `${weeks} week${weeks !== 1 ? "s" : ""} ago`;
  const months = Math.floor(days / 30);
  if (months < 12) return `${months} month${months !== 1 ? "s" : ""} ago`;
  const years = Math.floor(days / 365);
  return `${years} year${years !== 1 ? "s" : ""} ago`;
}

export const toDateWithTime = (baseDate: Date, timeStr: string): Date => {
  const trimmed = timeStr.trim();

  const timeRegex = /^(\d{1,2}):(\d{2})(?:\s?(AM|PM))?$/i;
  const match = trimmed.match(timeRegex);

  if (!match) {
    throw new Error(
      `Invalid time format: "${timeStr}". Expected "HH:MM" or "HH:MM AM/PM".`
    );
  }

  let hours = parseInt(match[1], 10);
  const minutes = parseInt(match[2], 10);
  const modifier = match[3] ? match[3].toUpperCase() : null;

  if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
    throw new Error(
      `Invalid time values: "${timeStr}". Hours must be 0-23 and minutes 0-59.`
    );
  }

  if (modifier) {
    if (hours === 0 || hours > 12) {
      throw new Error(
        `Invalid hour for 12-hour format: "${timeStr}". Hours must be 1-12.`
      );
    }
    if (modifier === "PM" && hours < 12) hours += 12;
    if (modifier === "AM" && hours === 12) hours = 0;
  }

  const result = new Date(baseDate);
  result.setHours(hours, minutes, 0, 0);
  return result;
};
