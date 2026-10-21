// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.print.renderer;

import com.tigshop.bean.enums.print.PrintPaperSizeEnum;
import com.tigshop.bean.param.print.PrintTemplate;

/**
 * 小票渲染器契约
 * <p>
 * 按平台不同输出：
 * <ul>
 *     <li>飞鹅云：富文本标签字符串（&lt;BR&gt;、&lt;C&gt;、&lt;CUT&gt; 等）</li>
 *     <li>商米云：UTF-8 文本转十六进制 + ESC/POS 控制字节</li>
 * </ul>
 * 模板数据 {@link PrintTemplate} 跨平台共用（REQ-006）。
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
public interface ReceiptRenderer {

    /**
     * 返回渲染器对应的平台编码（同 {@code PrintPlatformEnum#getCode()}）
     */
    Integer getPlatformCode();

    /**
     * 按模板渲染订单打印内容（带纸张尺寸）。
     * <p>
     * 各实现按 {@code paperSize} 切换分割线、列宽、表头等常量。{@code null} 视为
     * {@link PrintPaperSizeEnum#SIZE_58}（字节级兼容历史行为）。
     *
     * @param orderId   订单 ID
     * @param template  已反序列化的打印模板
     * @param paperSize 纸张尺寸枚举（null 默认 58mm）
     * @return 平台特定格式的打印内容
     */
    String render(Integer orderId, PrintTemplate template, PrintPaperSizeEnum paperSize);
}
