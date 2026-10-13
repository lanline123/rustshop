export interface panelIndexFilterState {
    consoleData: ConsoleData;
    realTimeData: RealTimeData;
    panelStatisticalData: PanelStatisticalData;
    errcode: number;
    message: string;
}

export interface ConsoleData {
    awaitPay: number;
    awaitShip: number;
    awaitAfterSale: number;
    awaitComment: number;
}

export interface PanelStatisticalData {
    horizontalAxis: string[];
    longitudinalAxisAccess: number[];
    longitudinalAxisOrderNum: number[];
    longitudinalAxisOrderAmount: number[];
}

export interface RealTimeData {
    todayOrderAmount: number;
    yesterdayBuyerNum: number;
    yesterdayVisitNum: number;
    yesterdayOrderNum: number;
    yesterdayViewNum: number;
    orderAmountGrowthRate: number;
    yesterdayOrderAmount: number;
    todayVisitNum: number;
    visitGrowthRate: number;
    todayBuyerNum: number;
    buyerGrowthRate: number;
    todayViewNum: number;
    viewGrowthRate: number;
    todayOrderNum: number;
    orderGrowthRate: number;
}
