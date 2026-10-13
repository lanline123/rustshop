export interface panelIndexFilterState {
    consoleData: ConsoleData;
    realTimeData: RealTimeData;
    panelStatisticalData: PanelStatisticalData;
    errcode: number;
    message: string;
}

export interface ConsoleData {
    awaitSettlement: number;
    awaitShip: number;
    awaitAfterSale: number;
    saleOutProductNum: number;
}

export interface PanelStatisticalData {
    horizontalAxis: string[];
    longitudinalAxisAccess: number[];
    longitudinalAxisOrderNum: number[];
    longitudinalAxisIncome: number[];
}

export interface RealTimeData {
    todaySettlementAmount: number;
    todaySettlementNum: number;
    saleProductNum: number;
    outageProductNum: number;
    accountBalance: number;
    awaitSettlementAmount: number;
    bindShopNum: number;
}
