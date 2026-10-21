import request from "@/utils/request";
import type { WholesaleSettingVO, WholesaleSettingSaveDTO } from "@/types/wholesale/setting";

/** 设置查询 GET /setting/get */
export const getWholesaleSetting = () =>
    request<WholesaleSettingVO>({ url: "wholesale/setting/get", method: "get" });

/** 设置保存 POST /setting/save */
export const saveWholesaleSetting = (data: WholesaleSettingSaveDTO) =>
    request({ url: "wholesale/setting/save", method: "post", data });
