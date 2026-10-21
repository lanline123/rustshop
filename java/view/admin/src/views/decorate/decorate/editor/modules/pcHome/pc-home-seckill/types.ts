/**
 * PC 首页秒杀模块类型定义
 * 说明：PC首页独有的秒杀模块
 */

import type { ModuleFrame, ModuleTitle } from "../_shared/types";

export interface PcHomeSeckillModule {
    /** 框架配置（外边距等） */
    frame?: ModuleFrame;
    seckillTitle: string;
    /** 标题设置（与拼团一致，支持显示/隐藏、样式及倒计时区域） */
    title?: ModuleTitle;
    /** 商品间距（父容器 gap，默认 10） */
    goodsGap?: number;
}
