import type { ModuleFrameType, ModuleTitleType, ModulePicListType } from "@/types/decorate/decorate";

/** 图片广告1 模块数据 */
export interface ImageAdModule {
    /** 模板：1=一行一个，2=轮播，3=横滑大图，4=横滑小图，5=一行两个，6=一行三个，7=一行四个 */
    picType: number;
    picList: ModulePicListType[];
    /** 轮播一行显示个数 */
    swiperPreView: number;
    /** 轮播指示器颜色 */
    swiperPageColor: string;
    /** 图片边距 */
    imgPadding: number;
    /** 指示器样式：1/2/3 */
    picPageType: number;
    /** 图片边角：1=直角，2=圆角 */
    picRadioStyle: number;
    /** 是否通宽排版（仅 PC 端有效） */
    isFluxWidth: number;
    frame: ModuleFrameType;
    title: ModuleTitleType;
    [key: string]: any;
}
