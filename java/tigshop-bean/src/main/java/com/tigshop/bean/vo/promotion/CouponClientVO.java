package com.tigshop.bean.vo.promotion;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.model.promotion.Coupon;
import com.tigshop.bean.model.user.UserCoupon;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.config.TimestampToDateSerializer;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.utils.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 前端优惠券VO
 *
 * @author kidd
 * @create 2025/7/4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "前端优惠券VO")
public class CouponClientVO {

    // *** Coupon ***

    @Schema(description = "优惠券ID")
    private Integer couponId;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "优惠券金额")
    private BigDecimal couponMoney;

    @Schema(description = "优惠券折扣")
    private BigDecimal couponDiscount;

    @Schema(description = "优惠券描述")
    @JsonTranslate(dataType = 0)
    private String couponDesc;

    @Schema(description = "优惠券类型")
    private Integer couponType;

    @Schema(description = "发放范围")
    private Integer sendRange;

    @Schema(description = "最低订单金额")
    private BigDecimal minOrderAmount;

    @Schema(description = "发放类型")
    private Integer sendType;

    @Schema(description = "使用天数")
    private Integer useDay;

    @Schema(description = "是否显示")
    private Integer isShow;

    @Schema(description = "是否全局")
    private Integer isGlobal;

    @Schema(description = "是否新用户")
    private Integer isNewUser;

    @Schema(description = "是否可点击领取")
    private Integer enabledClickGet;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "是否删除")
    private Integer isDelete;

    @Schema(description = "限制数量")
    private Integer limitNum;

    @Schema(description = "延迟天数")
    private Integer delayDay;

    @Schema(description = "发放数量")
    private Integer sendNum;

    @Schema(description = "最大订单金额")
    private BigDecimal maxOrderAmount;

    @Schema(description = "优惠券单位")
    private Integer couponUnit;

    @Schema(description = "减免类型")
    private Integer reduceType;

    // *** Other ***

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "发放开始日期")
    private String sendStartDate;

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "发放结束日期")
    private String sendEndDate;

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "使用开始日期")
    private String useStartDate;

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "使用结束日期")
    private String useEndDate;

    @Schema(description = "发放范围数据")
    private Integer[] sendRangeData;

    @Schema(description = "限制用户等级")
    private Integer[] limitUserRank;

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "是否已领取")
    private Integer isReceive;

    @Schema(description = "领取数量")
    private Long receiveNum;

    public CouponClientVO(Coupon coupon, List<UserCoupon> userCoupons) {
        this.couponId = coupon.getCouponId();
        this.couponName = coupon.getCouponName();
        this.couponMoney = coupon.getCouponMoney();
        this.couponDiscount = coupon.getCouponDiscount();
        this.couponDesc = coupon.getCouponDesc();
        this.couponType = coupon.getCouponType();
        this.sendRange = coupon.getSendRange();
        this.minOrderAmount = coupon.getMinOrderAmount();
        this.sendType = coupon.getSendType();
        this.useDay = coupon.getUseDay();
        this.isShow = coupon.getIsShow();
        this.isGlobal = coupon.getIsGlobal();
        this.isNewUser = coupon.getIsNewUser();
        this.enabledClickGet = coupon.getEnabledClickGet();
        this.shopId = coupon.getShopId();
        this.isDelete = coupon.getIsDelete();
        this.limitNum = coupon.getLimitNum();
        this.delayDay = coupon.getDelayDay();
        this.sendNum = coupon.getSendNum();
        this.maxOrderAmount = coupon.getMaxOrderAmount();
        this.couponUnit = coupon.getCouponUnit();
        this.reduceType = coupon.getReduceType();

        this.sendStartDate = coupon.getSendStartDate().toString();
        this.sendEndDate = coupon.getSendEndDate().toString();
        this.useStartDate = coupon.getUseStartDate().toString();
        this.useEndDate = coupon.getUseEndDate().toString();

        if (coupon.getSendRangeData() != null && !"[]".equals(coupon.getSendRangeData())) {
            this.sendRangeData = JsonUtil.fromJson(coupon.getSendRangeData(), Integer[].class);
        } else {
            this.sendRangeData = new Integer[]{};
        }
        if (coupon.getLimitUserRank() != null && !"[]".equals(coupon.getLimitUserRank())) {
            this.limitUserRank = JsonUtil.fromJson(coupon.getLimitUserRank(), Integer[].class);
        } else {
            this.limitUserRank = new Integer[]{};
        }

        this.addTime = coupon.getAddTime().toString();

        this.receiveNum = userCoupons.stream()
                .filter(userCoupon -> userCoupon.getCouponId().equals(coupon.getCouponId()))
                .count();
        this.isReceive = this.limitNum == 0 || this.limitNum > this.receiveNum ? Constants.NO : Constants.YES;
    }
}

