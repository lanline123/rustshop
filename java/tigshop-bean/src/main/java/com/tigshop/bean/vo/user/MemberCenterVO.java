package com.tigshop.bean.vo.user;

import cn.hutool.json.JSONArray;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户客户端详情VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "用户客户端详情VO")
public class MemberCenterVO {

    @Schema(description = "用户名掩码")
    private String dimUsername;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "等级ID")
    private Integer rankId;

    @Schema(description = "余额")
    private BigDecimal balance;

    @Schema(description = "分销佣金可用余额（D1107）")
    private BigDecimal commissionBalance;

    @Schema(description = "积分")
    private Integer points;

    @Schema(description = "手机号是否验证")
    private Integer mobileValidated;

    @Schema(description = "邮箱是否验证")
    private Integer emailValidated;

    @Schema(description = "是否SVIP")
    private Integer isSvip;

    @Schema(description = "是否企业认证")
    private Integer isCompanyAuth;

    @Schema(description = "等级名称")
    private String rankName;

    @Schema(description = "最小成长积分")
    private BigDecimal minGrowthPoints;

    @Schema(description = "等级图标")
    private String rankIco;

    @Schema(description = "折扣")
    private BigDecimal discount;

    @Schema(description = "等级类型")
    private Integer rankType;

    @Schema(description = "等级背景")
    private String rankBg;

    @Schema(description = "等级积分")
    private Integer rankPoint;

    @Schema(description = "是否免邮")
    private Integer freeShipping;

    @Schema(description = "权益")
    private JSONArray rights;

    @Schema(description = "等级级别")
    private Integer rankLevel;

    @Schema(description = "安全等级")
    private Integer securityLv;

    @Schema(description = "待支付订单数")
    private Long awaitPay;

    @Schema(description = "待发货订单数")
    private Long awaitShipping;

    @Schema(description = "待收货订单数")
    private Long awaitReceived;

    @Schema(description = "待评价订单数")
    private Long awaitComment;

    @Schema(description = "待使用优惠券数")
    private Long awaitCoupon;
}
