package com.tigshop.bean.query.finance;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 进件申请列表查询参数
 *
 * @author Tigshop团队
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "进件申请列表查询参数")
public class MerchantApplicationPageQuery extends BasePage {

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "申请主体类型:SHOP店铺/门店 VENDOR供应商")
    private String applicantType;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "状态集合(逗号分隔)，用于按列表场景限定可见状态范围")
    private String statusList;
}
