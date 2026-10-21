import type { ModuleFrameType, ModuleTitleType, ModulePicListType } from "@/types/decorate/decorate";

/** PC 自定义页 - 图片魔方模块数据 */
export interface PcImageSquareAdModule {
    /** 模板：1=一行两个，2=一行三个，3=一行四个，4=左二右二，5=左一右二，6=左一右三，7=上一下二 */
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
    /** 是否通宽排版 */
    isFluxWidth: number;
    frame: ModuleFrameType;
    title: ModuleTitleType;
    [key: string]: any;
}
