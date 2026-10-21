export const getMaxGroupNumFromJson = (groupNum: string) => {
    if (!groupNum) {
        return 0;
    }

    try {
        const parsedGroupNum = JSON.parse(groupNum);
        if (!Array.isArray(parsedGroupNum)) {
            return 0;
        }

        return parsedGroupNum.reduce((maxGroupNum, item) => {
            const currentGroupNum = Number(item?.groupNum);
            if (!Number.isFinite(currentGroupNum)) {
                return maxGroupNum;
            }
            return Math.max(maxGroupNum, currentGroupNum);
        }, 0);
    } catch {
        return 0;
    }
};
