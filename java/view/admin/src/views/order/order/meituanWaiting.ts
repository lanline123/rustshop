export type MeituanDispatchTime = string | number | null | undefined;

const getDispatchTimeMilliseconds = (dispatchTime: MeituanDispatchTime): number => {
    if (dispatchTime === null || dispatchTime === undefined || dispatchTime === "") return Number.NaN;

    const numericTime = Number(dispatchTime);
    if (Number.isFinite(numericTime) && numericTime > 0) {
        return numericTime < 1_000_000_000_000 ? numericTime * 1000 : numericTime;
    }

    if (typeof dispatchTime !== "string") return Number.NaN;
    return Date.parse(dispatchTime.replace(/-/g, "/"));
};

export const getMeituanWaitingText = (dispatchTime: MeituanDispatchTime, now = Date.now(), isDelivery = true): string => {
    const dispatchTimeMs = getDispatchTimeMilliseconds(dispatchTime);
    if (!Number.isFinite(dispatchTimeMs)) return "";

    const diffSeconds = Math.max(0, Math.floor((now - dispatchTimeMs) / 1000));
    const minutes = Math.floor(diffSeconds / 60);
    const seconds = diffSeconds % 60;
    const duration =
        diffSeconds >= 3600
            ? `${String(Math.floor(diffSeconds / 3600)).padStart(2, "0")} : ${String(Math.floor((diffSeconds % 3600) / 60)).padStart(2, "0")} : ${String(seconds).padStart(2, "0")}`
            : `${minutes} : ${seconds}`;

    return isDelivery ? `耗时：${duration}` : `骑手待接单,已发货${duration}`;
};
