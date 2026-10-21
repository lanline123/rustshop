package com.tigshop.bean.dto.salesman;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
/**
 * 库存日志列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "分销员明细列表参数")
public class SalesmanStatisticalDTO {
    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "上级手机号")
    private String pidMobile;

    @Schema(description = "分销员组ID")
    private Integer groupId;

    @Schema(description = "等级")
    private Integer level;

}
