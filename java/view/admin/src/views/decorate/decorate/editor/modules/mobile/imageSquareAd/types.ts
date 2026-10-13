import type { ModuleFrameType, ModuleTitleType, ModulePicListType } from "@/types/decorate/decorate";

/** 图片魔方模块数据 */
export interface ImageSquareAdModule {
    /** 布局：1=一行两个，2=一行三个，3=一行四个，4=左二右二，5=左一右二，6=左一右三，7=上一下二 */
    picType: number;
    picList: ModulePicListType[];
    /** 轮播一行显示个数（沿用图片广告默认值，魔方布局未使用） */
    swiperPreView: number;
    swiperPageColor: string;
    /** 图片边距 */
    imgPadding: number;
    picPageType: number;
    picRadioStyle: number;
    isFluxWidth: number;
    frame: ModuleFrameType;
    title: ModuleTitleType;
    [key: string]: any;
}
