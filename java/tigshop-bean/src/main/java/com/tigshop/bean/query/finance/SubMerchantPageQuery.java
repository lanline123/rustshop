package com.tigshop.bean.query.finance;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 子商户列表查询参数
 *
 * @author Tigshop团队
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "子商户列表查询参数")
public class SubMerchantPageQuery extends BasePage {

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "子商户号")
    private String subMchId;

    @Schema(description = "支付渠道")
    private String payChannel;

    @Schema(description = "状态:0待激活 1已激活 2已冻结")
    private Integer status;
}
