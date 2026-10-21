package com.tigshop.bean.param.o2o;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * @author Tigshop项目组
 * @create 2025年09月10日 13:15
 */
@Data
@Schema(name = "UserPickupParam", description = "用户自提信息参数")
@Builder
public class UserPickupParam {
    @Schema(description = "收银台传入的用户ID")
    private Integer userId;

    @Schema(description = "自提信息ID")
    private Integer userPickupId;

    @Schema(description = "收货人姓名")
    private String consignee;

    @Schema(description = "收货人手机号")
    private String mobile;

    @Schema(description = "手机号区号")
    private String mobileAreaCode;

    @Schema(description = "是否默认地址 0-否 1-是")
    private Integer isDefault;

    @Schema(description = "是否选中 0-否 1-是")
    private Integer isSelected;
}