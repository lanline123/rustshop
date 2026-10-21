import { t } from "@/utils/i18n";
export const getOrderPickupParams = (data: any[]) => {
    return data?.map((item: any) => {
        return { shopId: item.shopId, pickupId: item.pickups[0].shopId, pickupTitle: item.pickups[0].shopTitle };
    });
};

export const getBusinessHours = (data: any) => {
    const formatWeeklyTime = (times: any[]) => {
        if (!times || times.length === 0) return "";

        const weekDayMap: { [key: string]: string } = {
            "1": t("周一"),
            "2": t("周二"),
            "3": t("周三"),
            "4": t("周四"),
            "5": t("周五"),
            "6": t("周六"),
            "7": t("周日")
        };

        return times
            .map((timeItem) => {
                let weekText = "";
                if (timeItem.dayOfWeek.includes(",")) {
                    // 多个星期，如 "2,3,4"
                    const days = timeItem.dayOfWeek.split(",");
                    weekText = days.map((day: string) => weekDayMap[day]).join("、");
                } else {
                    // 单个星期，如 "1"
                    weekText = weekDayMap[timeItem.dayOfWeek];
                }

                // 处理时间段
                const rangesText = timeItem.ranges.map((range: any) => `${range.start}-${range.end}`).join("，");

                return `${weekText} ${rangesText}`;
            })
            .join("；");
    };

    let str = "";

    switch (data?.type) {
        case 1:
            str = "0:00-24:00";
            break;
        case 2:
            if (!data.times) break;
            str = (data.times as Array<{ start: string; end: string }>).map(({ start, end }) => `${start}-${end}`).join(",");
            break;
        case 3:
            if (!data.times) break;
            str = formatWeeklyTime(data.times);
            break;
    }

    return str;
};

// 过滤掉已经过了的时间段
// 过滤掉已经过了的时间段
export const filterDayList = (list: any[], appointmentTime?: { timeNum: number; timeUnit: "d" | "h" | "m" }) => {
    const now = new Date();

    // 计算需要提前的时间（转换为分钟）
    let advanceMinutes = 0;
    if (appointmentTime) {
        switch (appointmentTime.timeUnit) {
            case "h":
                advanceMinutes = appointmentTime.timeNum * 60;
                break;
            case "m":
                advanceMinutes = appointmentTime.timeNum;
                break;
        }
    }

    // 计算截止时间
    const deadlineTime = new Date(now.getTime() + advanceMinutes * 60 * 1000);
    const deadlineHour = deadlineTime.getHours();
    const deadlineMinute = deadlineTime.getMinutes();

    return list.filter((item: any) => {
        let itemHour = 0,
            itemMinute = 0;
        if (item.endTime) {
            const [h, m] = item.endTime.split(":").map(Number);
            itemHour = h;
            itemMinute = m;
        }

        // 保留大于截止时间的时间段
        return itemHour > deadlineHour || (itemHour === deadlineHour && itemMinute > deadlineMinute);
    });
};
