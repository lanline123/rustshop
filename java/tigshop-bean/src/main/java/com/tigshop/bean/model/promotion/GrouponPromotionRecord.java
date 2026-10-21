package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 拼团记录
 *
 * @TableName groupon_promotion_record
 */
@TableName(value = "groupon_promotion_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrouponPromotionRecord implements Serializable {

    @Schema(description = "拼团记录 ID")
    @TableId(type = IdType.AUTO)
    private Long grouponPromotionRecordId;

    @Schema(description = "拼团活动 ID")
    private Long grouponPromotionId;

    @Schema(description = "拼团活动商品 ID")
    private Long grouponPromotionItemId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "参与类型；1-开团，2-参团")
    private Integer joinType;

    @Schema(description = "拼团状态；1-待成团，2-拼团成功，3-拼团失败")
    private Integer joinStatus;

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "订单 ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderSn;

    @Schema(description = "是否虚拟；0-否，1-是")
    private Integer isVirtual;

    @Schema(description = "支付金额")
    private BigDecimal paidAmount;

    @Schema(description = "开团记录 ID")
    private Long openGroupRecordId;

    @Schema(description = "开团时间")
    private Long openGroupTime;

    @Schema(description = "团长名称")
    private String groupCaptainName;

    @Schema(description = "成团截止时间")
    private Long joinExpirationTime;

    @Schema(description = "成团最低人数")
    private Integer minGroupNum;

    @Schema(description = "所属店铺")
    private Integer shopId;

    @Schema(description = "创建时间")
    private Long addTime;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像")
    private String avatar;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}