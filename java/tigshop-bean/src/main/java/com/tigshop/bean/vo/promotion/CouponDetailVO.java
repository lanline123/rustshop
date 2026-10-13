package com.tigshop.bean.vo.promotion;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 优惠券详情VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "优惠券详情")
public class CouponDetailVO {
    @Schema(description = "领取数量")
    private long receiveNum;

    @Schema(description = "优惠券ID")
    private Integer couponId;

    @Schema(description = "优惠券名称")
    @JsonTranslate(dataType = 10)
    private String couponName;

    @Schema(description = "优惠券金额")
    private BigDecimal couponMoney;

    @Schema(description = "优惠券折扣")
    private BigDecimal couponDiscount;

    @Schema(description = "优惠券描述")
    @JsonTranslate(dataType = 10)
    private String couponDesc;

    @Schema(description = "优惠券类型")
    private int couponType;

    @Schema(description = "发放范围")
    private int sendRange;

    @Schema(description = "发放范围数据")
    private Integer[] sendRangeData;

    @Schema(description = "最低订单金额")
    private BigDecimal minOrderAmount;

    @Schema(description = "发放开始日期")
    private Long sendStartDate;

    @Schema(description = "发放结束日期")
    private Long sendEndDate;

    @Schema(description = "发放类型")
    private int sendType;

    @Schema(description = "使用天数")
    private int useDay;

    @Schema(description = "使用开始日期")
    private String useStartDate;

    @Schema(description = "使用结束日期")
    private String useEndDate;

    @Schema(description = "是否显示")
    private int isShow;

    @Schema(description = "是否全局")
    private int isGlobal;

    @Schema(description = "是否新用户")
    private int isNewUser;

    @Schema(description = "是否可点击领取")
    private int enabledClickGet;

    @Schema(description = "限制用户等级")
    private Integer[] limitUserRank;

    @Schema(description = "店铺ID")
    private int shopId;

    @Schema(description = "是否删除")
    private int isDelete;

    @Schema(description = "限制数量")
    private int limitNum;

    @Schema(description = "延迟天数")
    private int delayDay;

    @Schema(description = "发放数量")
    private int sendNum;

    @Schema(description = "最大订单金额")
    private BigDecimal maxOrderAmount;

    @Schema(description = "优惠券单位")
    private int couponUnit;

    @Schema(description = "减免类型")
    private int reduceType;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "是否已领取")
    private int isReceive;
}
