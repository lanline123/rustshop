package com.tigshop.bean.query.promotion.groupon;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拼团活动记录列表
 *
 * @author kidd
 * @since 2025/8/29 15:44
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GrouponRecordPageQuery extends BasePage {

    @Schema(description = "参与类型；1-开团，2-参团")
    private Integer joinType;

    @Schema(description = "是否我的团")
    private Boolean isMine;

    @Schema(description = "拼团状态;拼团状态；1-待成团，2-拼团成功，3-拼团失败")
    private Integer joinStatus;

    @Schema(description = "筛选订单时间类型")
    private Integer dateType;

    @Schema(description = "商品id")
    private Integer productId;

    @Schema(description = "是否过滤已结束的拼团（拼团成功/拼团失败）")
    private Boolean excludeEnded;

    public GrouponRecordPageQuery(){
        this.isMine = false;
    }

}
