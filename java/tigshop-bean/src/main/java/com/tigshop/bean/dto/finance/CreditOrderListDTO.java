package com.tigshop.bean.dto.finance;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 账期订单列表查询DTO
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "账期订单列表查询参数")
public class CreditOrderListDTO extends BasePage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单号")
    private String orderSn;

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "会员用户名")
    private String username;

    @Schema(description = "联系方式手机号")
    private String mobile;

    @Schema(description = "订单状态：0待确定 1待发货 2已发货 3已取消 4无效 5已完成")
    private Integer orderStatus;

    @Schema(description = "结算状态：0-待结算，1-已结算")
    private Integer settlementStatus;

    @Schema(description = "结算状态集合")
    private List<Integer> settlementStatusList;

    @Schema(description = "回款流水单号")
    private String settlementSn;

    @Schema(description = "结算记录ID")
    private Integer settlementId;

    @Schema(description = "下单开始时间（格式：yyyy-MM-dd）")
    private String orderTimeStart;

    @Schema(description = "下单结束时间（格式：yyyy-MM-dd）")
    private String orderTimeEnd;
}
