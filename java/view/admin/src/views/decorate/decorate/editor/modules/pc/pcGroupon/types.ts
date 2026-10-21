import type { ModuleFrameType, ModuleTitleType } from "@/types/decorate/decorate";

/** PC 首页 - 拼团模块数据 */
export interface PcGrouponModule {
    /** 模板：2=一行四个，3=一行五个，6=横向滑动 */
    style: number;
    /** 商品卡样式：1=标准，2=投影，3=描边，4=透明底 */
    goodsStyle: number;
    /** 商品卡边角：1=直角，2=圆角 */
    goodsRadioStyle: number;
    /** 文本对齐：1=左对齐，2=居中对齐 */
    textAlign: number;
    /** 文字粗细：1=常规体，2=加粗体 */
    textWeight: number;
    /** 商品名称行数 */
    goodsNameRow: number;
    /** 商品信息边距：1=有边距，0=无边距 */
    goodsNamePadding: number;
    showName: number;
    showBrief: number;
    showPrice: number;
    /** 商品边距 */
    goodsPadding: number;
    buyBtnStyle: number;
    backgroundColor: string;
    boxRadius: number;
    innerPadding: number;
    boxPadding: number;
    boxPaddingTop: number;
    boxPaddingBottom: number;
    frame: ModuleFrameType;
    title: ModuleTitleType;
    /** 拼团类型：1=普通拼团，2=阶梯拼团 */
    groupType: number;
    /** 已选拼团活动（选中后回写 groupType） */
    grouponActivityItem?: any;
    /** 商品卡片样式：1=样式一，2=样式二（Settings 侧烧录） */
    cardStyle?: number;
    [key: string]: any;
}
