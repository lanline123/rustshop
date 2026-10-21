import type { ModuleFrameType, ModulePicListType } from "@/types/decorate/decorate";

/** PC 自定义页 - 图文导航模块数据 */
export interface PcImageNavModule {
    /** 显示模式：1=固定显示，2=滑动分页 */
    navType: number;
    /** 样式：1=图片文字，2=仅图片，3=仅文字 */
    navStyle: number;
    /** 一行显示个数 */
    rowNum: number;
    /** 显示行数 */
    colNum: number;
    picList: ModulePicListType[];
    /** 元素边距 */
    imgPadding: number;
    /** 图片边角：1=直角，2=圆角 */
    radioStyle: number;
    /** 指示器样式：1/2 */
    picPageType: number;
    /** 指示器颜色 */
    swiperPageColor: string;
    /** 背景颜色（Edit 写入，非 defaults 字段） */
    backgroundColor?: string;
    frame: ModuleFrameType;
    [key: string]: any;
}
