package com.tigshop.bean.vo.finance;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 分账单视图对象
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "分账单视图对象")
public class ProfitSharingOrderVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "商户分账单号")
    private String outOrderNo;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderSn;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "供应商ID(0表示店铺分账单)")
    private Integer vendorId;

    @Schema(description = "供应商名称")
    private String vendorName;

    @Schema(description = "结算主体类型:1店铺 2供应商")
    private Integer settlementType;

    @Schema(description = "微信交易号")
    private String transactionId;

    @Schema(description = "订单金额(元)")
    private BigDecimal totalAmount;

    @Schema(description = "分账金额(元)")
    private BigDecimal splitAmount;

    @Schema(description = "服务费(元)")
    private BigDecimal serviceFee;

    @Schema(description = "分销佣金(元)")
    private BigDecimal commissionFee;

    @Schema(description = "分账状态 PENDING/PROCESSING/SUCCESS/FAILED")
    private String status;

    @Schema(description = "分账状态描述")
    private String statusDesc;

    @Schema(description = "完结状态 UNFINISHED/FINISHED")
    private String finishStatus;

    @Schema(description = "完结状态描述")
    private String finishStatusDesc;

    @Schema(description = "触发方式 MANUAL/AUTO")
    private String triggerType;

    @Schema(description = "是否允许手动完结(仅分账中/分账成功且未完结时为true，前端据此控制手动完结按钮显隐)")
    private Boolean canFinish;

    @Schema(description = "是否允许作废(仅待分账且无渠道交易号时为true；作废只改本地状态、不调渠道，走同一 manualFinish 接口)")
    private Boolean canCancel;

    @Schema(description = "结算单ID(单体下为订单ID)")
    private Long settlementId;

    @Schema(description = "结果描述")
    private String resultDesc;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Schema(description = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifyTime;
}
