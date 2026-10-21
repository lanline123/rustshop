import type { ModuleType } from "@/types/decorate/decorate.d";
import type { WechatLiveRoomState } from "@/types/content/wechatLive.d";

/** 小程序直播模块数据（沿用旧 View/Edit 内联的 LiveDecorateModule 形状） */
export interface LiveModule extends ModuleType {
    titleText?: string;
    displayMode?: "list" | "bigImage";
    sourceType?: "auto" | "manual";
    autoLiveNum?: number;
    selectedLiveList?: WechatLiveRoomState[];
    [key: string]: any;
}
