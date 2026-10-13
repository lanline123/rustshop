import dayjs from "dayjs"
export function formatTimestamp(timestamp: any) {
  var date = new Date(timestamp * 1000);

  var year = date.getFullYear();
  var month = ('0' + (date.getMonth() + 1)).slice(-2);
  var day = ('0' + date.getDate()).slice(-2);

  var hours = ('0' + date.getHours()).slice(-2);
  var minutes = ('0' + date.getMinutes()).slice(-2);
  var seconds = ('0' + date.getSeconds()).slice(-2);

  var formattedDate = year + '-' + month + '-' + day;
  var formattedTime = hours + ':' + minutes + ':' + seconds;

  return formattedDate + ' ' + formattedTime;
}
export const formatTime = (date: Date, hours: number) => {
  const startOfDay = new Date(date);
  startOfDay.setHours(0, 0, 0, 0);
  startOfDay.setHours(startOfDay.getHours() + hours);
  return startOfDay.getTime();
};
export function getDaysLaterEndTime(days: number): number {
  const currentDate = new Date();
  const targetDate = new Date(currentDate);
  targetDate.setDate(currentDate.getDate() + days);
  targetDate.setHours(23, 59, 59, 999);
  return targetDate.getTime();
}

export function formattedDate(date: any, type?: string) {
  return dayjs(date).format(type || 'YYYY-MM-DD HH:mm:ss')
}


export const formatDuration = (seconds: number, forceHours = false): string => {
  if (isNaN(seconds) || !isFinite(seconds)) return '00:00';

  const hrs = Math.floor(seconds / 3600);
  const mins = Math.floor((seconds % 3600) / 60);
  const secs = Math.floor(seconds % 60);

  // 不需要小时且小时为0时，显示MM:SS
  if (!forceHours && hrs === 0) {
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }

  // 显示HH:MM:SS
  return `${hrs.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

/**
 * 日期字符串转时间戳（秒级）
 * @param dateStr 日期字符串，格式：YYYY-MM-DD HH:mm:ss
 * @returns 时间戳（秒）
 */
export function dateToTimestamp(dateStr: string | Date): number {
    if (!dateStr) return 0;
    const date = typeof dateStr === 'string' ? new Date(dateStr) : dateStr;
    return Math.floor(date.getTime() / 1000);
}

/**
 * 时间戳转日期字符串
 * @param timestamp 时间戳（秒）
 * @param format 格式，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns 日期字符串
 */
export function timestampToDate(timestamp: number, format = 'YYYY-MM-DD HH:mm:ss'): string {
    if (!timestamp) return '';
    return dayjs(timestamp * 1000).format(format);
}

/**
 * 计算相对时间的时间戳
 * @param days 天数
 * @param hours 小时数
 * @param baseTime 基准时间，默认当前时间
 * @returns 时间戳（毫秒）
 */
export function calculateRelativeTime(days: number, hours: number, baseTime?: Date): number {
    const base = baseTime || new Date();
    const result = new Date(base);
    result.setDate(result.getDate() + days);
    result.setHours(result.getHours() + hours);
    return result.getTime();
}