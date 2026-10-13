export interface ShipReceiveContactInfo {
    sender?: string;
    mobile?: string;
    provinceName?: string;
    cityName?: string;
    areaName?: string;
    address?: string;
}

export interface ShipReceiveInfoFormState {
    shopId?: number;
    shipInfo?: ShipReceiveContactInfo;
    receiveInfo?: ShipReceiveContactInfo;
}
