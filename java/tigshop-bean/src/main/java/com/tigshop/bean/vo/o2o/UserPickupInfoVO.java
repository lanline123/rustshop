package com.tigshop.bean.vo.o2o;

import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户自提信息 VO（用于前端展示）
 * @author Tigshop项目组
 */
@Data
@Schema(name = "UserPickupInfoVO", description = "用户自提信息展示对象")
public class UserPickupInfoVO implements Serializable {

    @Schema(description = "自提信息ID")
    private Integer userPickupId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "收货人姓名")
    private String consignee;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "收货人手机号")
    private String mobile;

    @Schema(description = "手机号区号")
    private String mobileAreaCode;

    @Schema(description = "是否默认地址 0-否 1-是")
    private Integer isDefault;

    @Schema(description = "是否选中 0-否 1-是")
    private Integer isSelected;
}
