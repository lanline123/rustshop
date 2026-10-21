package com.tigshop.bean.vo.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * C 端「我是否批发商」VO
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "我是否批发商")
public class WholesaleMeVO {

    @Schema(description = "是否批发商")
    private Boolean isWholesaler;

    @Schema(description = "批发等级ID")
    private Integer gradeId;

    @Schema(description = "批发等级名称")
    private String gradeName;

    @Schema(description = "批发功能总开关（wholesale.enabled），前端用于条件渲染批发入口")
    private Boolean globalEnabled;
}
