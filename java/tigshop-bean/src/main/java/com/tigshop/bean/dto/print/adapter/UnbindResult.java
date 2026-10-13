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
 * 打印平台适配器 - 解绑打印机结果
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
@Data
@Builder
public class UnbindResult {

    /**
     * 是否解绑成功（商米云端"打印机已不存在"也视为成功，幂等语义）
     */
    private boolean success;

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
