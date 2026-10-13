package com.tigshop.bean.query.o2o;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户自提信息查询参数
 * @author Tigshop项目组
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "UserPickupQuery", description = "用户自提信息查询参数")
public class UserPickupQuery extends BasePage {

    @Schema(description = "收货人姓名（模糊查询）")
    private String consignee;

    @Schema(description = "收货人手机号")
    private String mobile;
}
