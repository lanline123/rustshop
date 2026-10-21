// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.print.adapter;

import lombok.Builder;
import lombok.Data;

/**
 * 打印平台适配器 - 推送打印任务结果
 * <p>
 * {@link #mappedStatus} 将平台原始错误码映射为 {@code PrintLogStatusEnum} 的推送结果态：
 * <ul>
 *     <li>1 PUSHED：推送成功</li>
 *     <li>3 PUSH_FAILED：推送失败</li>
 *     <li>4 OFFLINE：打印机离线</li>
 * </ul>
 * 实际 PRINTED(2) 打印成功态由回查 / 回调链路单独写入，此处不填。
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
@Data
@Builder
public class PushResult {

    /**
     * 是否同步成功（推送到云端成功即 true；不代表打印机真实出票）
     */
    private boolean success;

    /**
     * 映射后的 PrintLogStatusEnum code（1 / 3 / 4）
     */
    private Integer mappedStatus;

    /**
     * 平台任务号（用于后续回查 / 回调匹配）
     */
    private String taskNo;

    /**
     * 平台错误码
     */
    private String errorCode;

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 平台原始响应
     */
    private String rawResponse;
}
