package com.tigshop.bean.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 拼团活动开团记录分页列表
 *
 * @author kidd
 * @since 2025/8/29 09:51
 */
@Data
public class GrouponRecordOpenJonPageVO {

    @Schema(description = "拼团记录 ID")
    private Long grouponPromotionRecordId;

    @Schema(description = "开团时间")
    private Long openGroupTime;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "拼团状态；1-待成团，2-拼团成功，3-拼团失败")
    private Integer joinStatus;

    @Schema(description = "团长名称")
    private String groupCaptainName;

    @Schema(description = "成团截止时间")
    private Long joinExpirationTime;

    @Schema(description = "成团最低人数")
    private Integer minGroupNum;

}
