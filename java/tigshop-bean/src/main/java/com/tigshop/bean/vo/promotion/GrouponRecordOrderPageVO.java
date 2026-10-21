package com.tigshop.bean.vo.promotion;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 拼团活动记录订单分页列表
 *
 * @author kidd
 * @since 2025/8/29 09:51
 */
@Data
public class GrouponRecordOrderPageVO {

    @Schema(description = "参与类型；1-开团，2-参团")
    private Integer joinType;

    @Schema(description = "团长名称")
    private String groupCaptainName;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "订单号")
    private String orderSn;

    @Schema(description = "创建时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String addTime;

    @Schema(description = "拼团状态；1-待成团，2-拼团成功，3-拼团失败")
    private Integer joinStatus;

    // *** Other ***

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "订单的状态：0 待确定，待支付 1 已确定，待发货 2处理中，已发货 3 已取消 4 无效 5 已完成")
    private Integer orderStatus;

    @Schema(description = "订单状态名称")
    private String orderStatusName;

}
