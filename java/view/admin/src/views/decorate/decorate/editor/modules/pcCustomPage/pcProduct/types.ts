import type { ModuleFrameType, ModuleTitleType, ModuleProductsType } from "@/types/decorate/decorate";

/** PC 自定义页 - 商品模块数据 */
export interface PcProductModule {
    /** 展示样式：1=一行四个，2=一行五个 */
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
    /** 购买按钮样式 */
    buyBtnStyle: number;
    backgroundColor: string;
    boxRadius: number;
    innerPadding: number;
    boxPadding: number;
    boxPaddingTop: number;
    boxPaddingBottom: number;
    imgPadding: number;
    waterfall: number;
    swiperPageColor: string;
    frame: ModuleFrameType;
    title: ModuleTitleType;
    /** 商品选取配置 */
    products: ModuleProductsType;
    [key: string]: any;
}
