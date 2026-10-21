import type { ShipReceiveContactInfo } from "@/types/setting/shipInfo";

export interface ShopInfoFormState {
    shopId?: number;
    kefuPhone?: string;
    kefuWeixin?: string;
    kefuLink?: string;
    contactMobile?: string;
    status?: number;
    useShopCategory?: number;
    storeShowOtherCityStore?: number;
    kefuInlet?: number[];
    shopTitle?: string;
    addTime?: string;
    description?: string;
    shopLogo?: string;
    shipInfo?: ShipReceiveContactInfo;
    receiveInfo?: ShipReceiveContactInfo;
    shopShowCategory?: {
        ids?: string;
        type?: number | null;
    };
}
