package com.tigshop.bean.feign.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 发货信息录入结果
 *
 * @author kidd
 * @since 2025/5/29 11:36
 */
@Schema(description = "发货信息录入结果")
@Data
public class UploadShippingInfoResult {

    @Schema(description = "错误码")
    private Integer errcode;

    @Schema(description = "错误原因")
    private String errmsg;
}
