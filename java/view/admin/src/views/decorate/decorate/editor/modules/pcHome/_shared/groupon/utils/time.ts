/**
 * 拼团模块共享时间工具（移植自 Vort-Admin _shared/groupon/utils/time）
 * 说明：提供日期/时间字符串与时间戳的转换
 */

/**
 * 将日期字符串转换为时间戳
 * @param dateString 日期字符串，格式为 "YYYY-MM-DD HH:mm:ss" 或 "YYYY-MM-DD"
 * @returns 时间戳（毫秒），无效输入返回 0
 */
export function dateStringToTimestamp(dateString: string | undefined | null): number {
    if (!dateString) return 0;
    const dateParts = dateString.split(" ");
    const datePart = dateParts[0] || "";
    const timePart = dateParts[1] || "";
    const [year, month, day] = datePart.split("-");
    const [hour, minute, second] = timePart ? timePart.split(":") : ["0", "0", "0"];
    const date = new Date(Number(year), Number(month) - 1, Number(day), Number(hour), Number(minute), Number(second));
    return date.getTime();
}

/**
 * 格式化数字为两位字符串（补零）
 * @param n 数字
 * @returns 两位字符串
 */
export function formatNum(n: number): string {
    return String(n).padStart(2, "0");
}

/**
 * 计算倒计时剩余时间各部分
 * @param remainingMs 剩余毫秒数
 * @returns { days, hours, minutes, seconds }
 */
export function parseCountdown(remainingMs: number) {
    const days = Math.floor(remainingMs / (1000 * 60 * 60 * 24));
    const hours = Math.floor((remainingMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((remainingMs % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((remainingMs % (1000 * 60)) / 1000);
    return { days, hours, minutes, seconds };
}

/**
 * 格式化倒计时文本
 * @param remainingMs 剩余毫秒数
 * @param options 配置项
 * @returns 格式化后的倒计时字符串
 */
export function formatCountdownText(remainingMs: number, options: { showDays?: boolean } = {}): string {
    if (remainingMs <= 0) return "";
    const { days, hours, minutes, seconds } = parseCountdown(remainingMs);
    const { showDays = true } = options;
    if (showDays && days > 0) {
        return `${days}天${formatNum(hours)}:${formatNum(minutes)}:${formatNum(seconds)}`;
    }
    return `${formatNum(hours)}:${formatNum(minutes)}:${formatNum(seconds)}`;
}
