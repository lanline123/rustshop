package com.tigshop.bean.query.promotion.groupon;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 拼团活动参团团员分页列表查询条件
 *
 * @author kidd
 * @since 2025/8/29 09:58
 */
@Data
public class GrouponRecordOpenJoinMemberQuery {

    @Schema(description = "拼团记录 ID")
    private Long grouponPromotionRecordId;

}
