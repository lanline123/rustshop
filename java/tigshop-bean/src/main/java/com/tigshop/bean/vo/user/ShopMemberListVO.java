package com.tigshop.bean.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 店铺会员列表行 VO。
 *
 * <p>会员等级/店铺积分/成长值本期不实现（占位 null）。
 * 店铺端 mobile 恒脱敏；冻结态 nickname 也脱敏（服务层处理）。
 */
@Data
@Schema(description = "店铺会员列表行")
public class ShopMemberListVO {

    @Schema(description = "客户ID")
    private Integer userId;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "该店累计已支付订单数")
    private Integer orderCount;

    @Schema(description = "该店累计消费金额")
    private BigDecimal orderAmount;

    @Schema(description = "入会时间（Unix秒）")
    private Long joinTime;

    @Schema(description = "最近消费时间（Unix秒）")
    private Long lastConsumeTime;

    @Schema(description = "会员等级（本期占位，恒为 null）")
    private String rankName;

    @Schema(description = "店铺积分（本期占位，恒为 null）")
    private Integer shopPoints;

    @Schema(description = "成长值（本期占位，恒为 null）")
    private Integer growth;
}
