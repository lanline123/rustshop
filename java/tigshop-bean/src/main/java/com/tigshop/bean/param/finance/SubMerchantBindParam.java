package com.tigshop.bean.param.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 子商户绑定参数
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "子商户绑定参数")
public class SubMerchantBindParam {

    @NotNull(message = "请选择店铺")
    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "支付渠道")
    private String payChannel = "WECHAT";

    @NotBlank(message = "请输入子商户号")
    @Schema(description = "子商户号")
    private String subMchId;

    @Schema(description = "子商户AppID")
    private String subAppid;

    @Schema(description = "商户名称")
    private String merchantName;
}
