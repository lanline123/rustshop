package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 自提联系人创建参数
 *
 * @author Tigshop
 */
@Data
@Schema(description = "自提联系人创建参数")
public class PickupContactCreateDTO {

    @Schema(description = "客户ID")
    private Integer userId;

    @Schema(description = "联系人名称")
    private String contactName;

    @Schema(description = "联系人电话")
    private String contactPhone;
}
