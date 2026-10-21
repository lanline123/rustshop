package com.tigshop.bean.query.promotion;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 抽奖记录查询参数
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "抽奖记录查询参数")
public class LotteryRecordQuery extends BasePage {

    /**
     * 奖品状态:0-未领取,1-已领取,2-已过期
     */
    @Schema(description = "奖品状态:0-未领取,1-已领取,2-已过期")
    private Integer status;
}


