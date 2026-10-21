import { isMerchant } from "@/utils/version";
import { isStore } from "@/utils/version";
export const getAdminType = (): string | null => {
    return localStorage.getItem("adminType");
};

export const getShopType = (): number => {
    let shopType = 0
    if(getAdminType() === 'shop' && (isMerchant() || isStore())){
        shopType = Number(localStorage.getItem("shopType")) || 0;
    }
    return shopType;
};

// 当前登录身份对应的"经营主体"称谓（运行时按 token 区分）：供应商 / 门店 / 店铺
export const getShopTypeName = (): string => {
    if (getAdminType() === "vendor") return "供应商";
    return getShopType() === 2 ? "门店" : "店铺";
};

export const loginOut = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("adminType");
    localStorage.removeItem("config");
    localStorage.removeItem("lastOpenTime");
    localStorage.removeItem("licensedData");
    localStorage.removeItem("menus");
    localStorage.removeItem("region");
    localStorage.removeItem("routers");
    localStorage.removeItem("shopId");
    localStorage.removeItem("theme");
    localStorage.removeItem("user");
    localStorage.removeItem("decorate");
    localStorage.removeItem("version");
    localStorage.removeItem("shopInfo");
    localStorage.removeItem("vendorId");
    localStorage.removeItem("shopId");
    localStorage.removeItem("vendorInfo");
    localStorage.removeItem("shopType");
};
