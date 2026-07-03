import dayjs from "dayjs";

export function formatDate(dateString: string): string {
  return dayjs(dateString).format("ddd, DD MMM YYYY");
}

export function formatTime(dateTimeString: string): string {
  return dayjs(dateTimeString).format("hh:mm A");
}

export function formatDateTime(dateTimeString: string): string {
  return dayjs(dateTimeString).format("DD MMM YYYY hh:mm A");
}

export function formatDateForApi(date: Date): string {
  return dayjs(date).format("YYYY-MM-DD");
}

export function formatTimeForApi(time: string): string {
  return `${time}:00`;
}

export function isToday(dateTimeString: string): boolean {
  return dayjs(dateTimeString).isSame(dayjs(), "day");
}

export function isPast(dateTimeString: string): boolean {
  return dayjs(dateTimeString).isBefore(dayjs(), "day");
}
