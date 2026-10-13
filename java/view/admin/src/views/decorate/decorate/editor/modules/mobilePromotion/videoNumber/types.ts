import type { ModuleFrameType } from "@/types/decorate/decorate";

/** 商品图片项（picKey 为拖拽排序的稳定 key，仅编辑态生成） */
export interface VideoNumberProductImageItem {
    picId?: number | string;
    picThumb?: string;
    picUrl?: string;
    picName?: string;
    picKey?: string;
}

/** 视频号模块数据 */
export interface VideoNumberModule {
    titleText: string;
    /** 内容类型：video=视频，live=直播 */
    contentType: "video" | "live";
    videoNumberId: string;
    videoId: string;
    liveTitle: string;
    /** 标题颜色来源：default=默认，theme=主题色，custom=自定义 */
    titleColorType: "default" | "theme" | "custom";
    titleColor: string;
    /** 直播卡片商品图（最多 3 张） */
    productImages: VideoNumberProductImageItem[];
    frame: ModuleFrameType;
    [key: string]: any;
}
