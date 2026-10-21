import type { ModuleFrameType } from "@/types/decorate/decorate";

/** PC 自定义页 - 优惠券模块数据（title/desc 为字符串，区别于其它模块的 ModuleTitleType） */
export interface PcCouponModule {
    /** 显示标题栏：1=显示，0=不显示 */
    showTitle: number;
    /** 标题内容 */
    title: string;
    /** 描述内容 */
    desc: string;
    /** 券色彩样式：1=红色，2=黄色，3=黑色，4=绿色 */
    colorStyle: number;
    itemBackgroundColor: string;
    backgroundColor: string;
    /** 组件边距 */
    itemPadding: number;
    boxRadius: number;
    boxPadding: number;
    boxPaddingTop: number;
    boxPaddingBottom: number;
    /** 标题文字颜色（View 兜底 #000，非 defaults 字段） */
    titleColor?: string;
    frame: ModuleFrameType;
    [key: string]: any;
}
