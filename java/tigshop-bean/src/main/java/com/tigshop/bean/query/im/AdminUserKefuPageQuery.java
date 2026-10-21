package com.tigshop.bean.query.im;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 客服人员查询参数
 *
 * @author Tigshop团队
 */
@Schema(description = "客服人员查询参数")
@Setter
@Getter
public class AdminUserKefuPageQuery extends BasePage {

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "是否使用")
    private Integer isUsing;

    @Schema(description = "开始创建时间 yyyy-MM-dd")
    private String searchStartTime;

    @Schema(description = "结束创建时间 yyyy-MM-dd")
    private String searchEndTime;

    @Schema(description = "开始创建时间", hidden = true)
    private Long startCreateTime;

    @Schema(description = "结束创建时间", hidden = true)
    private Long endCreateTime;
}
