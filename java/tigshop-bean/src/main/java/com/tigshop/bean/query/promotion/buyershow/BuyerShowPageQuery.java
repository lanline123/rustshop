package com.tigshop.bean.query.promotion.buyershow;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wzh
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "买家秀查询参数")
public class BuyerShowPageQuery extends BasePage {

    @Schema(description = "状态（-1：待审核，0：隐藏，1：公开）")
    private Integer status;

    @Schema(description = "分组ID")
    private Long goodsId;

    @Schema(description = "创建开始时间")
    private String startTime;

    @Schema(description = "创建结束时间")
    private String endTime;

}
