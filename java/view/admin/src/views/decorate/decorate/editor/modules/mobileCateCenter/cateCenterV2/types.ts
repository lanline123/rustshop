import type { ModuleFrameType } from "@/types/decorate/decorate";

/** 分类中心V2 模块数据（旧 View 为 defineModel<any>，字段来自 defaults + Edit 实际读写） */
export interface CateCenterV2Module {
    /** 展示布局：2=双列网格，3=图文列表 */
    styleType?: number;
    /** 二级分类标签风格：1=实心高亮，2=描边高亮 */
    subTabStyle?: number;
    /** 文本对齐：1=左对齐，2=居中对齐 */
    textAlign?: number;
    /** 文字粗细：1=常规体，2=加粗体 */
    textWeight?: number;
    /** 商品名称行数：1/2 */
    goodsNameRow?: number;
    /** 显示商品名称：1=显示，0=不显示 */
    showName?: number;
    /** 购买按钮样式：0=不显示，1~8=样式 */
    buyBtnStyle?: number;
    /** 主题色；价格 / 购买按钮 / 选中态未单独配置时继承它。空串=跟随店铺主题色 */
    themeColor?: string;
    /** 左侧一级分类栏背景色 */
    sideBgColor?: string;
    /** 左侧一级分类文字色 */
    sideTextColor?: string;
    /** 左侧一级分类选中文字色 */
    sideActiveTextColor?: string;
    /** 左侧一级分类选中背景色 */
    sideActiveBgColor?: string;
    /** 左侧一级分类选中指示条颜色 */
    sideIndicatorColor?: string;
    /** 二级标签文字色 */
    subTabTextColor?: string;
    /** 二级标签背景色 */
    subTabBgColor?: string;
    /** 二级标签选中文字色（描边风格下为文字与边框色） */
    subTabActiveTextColor?: string;
    /** 二级标签选中背景色（描边风格下为边框色） */
    subTabActiveBgColor?: string;
    /** 商品区背景色 */
    goodsListBgColor?: string;
    /** 商品标题色 */
    productTitleColor?: string;
    /** 商品价格色 */
    priceColor?: string;
    /** 购买按钮色 */
    buyBtnColor?: string;
    frame?: ModuleFrameType;
    [key: string]: any;
}
