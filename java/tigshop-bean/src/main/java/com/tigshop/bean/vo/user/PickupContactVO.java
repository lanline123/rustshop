package com.tigshop.bean.vo.user;

import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 自提联系人视图对象
 *
 * @author Tigshop
 */
@Data
@Schema(name = "自提联系人信息")
public class PickupContactVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "客户ID")
    private Integer userId;

    @Schema(description = "联系人名称")
    private String contactName;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "联系人电话")
    private String contactPhone;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;
}
