// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 优惠券model
 *
 * @author kidd
 * @create 2025/7/4
 */
@Data
@TableName("coupon")
@Schema(description = "优惠券")
public class Coupon implements Serializable {

    @TableId(value = "coupon_id", type = IdType.AUTO)
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
    @JsonTranslate(dataType = 11)
    private String couponDesc;

    @Schema(description = "优惠券类型，1：满减券，2：折扣券")
    private Integer couponType;

    @Schema(description = "使用范围:0,全部商品;3,指定商品;4,不包含指定商品")
    private Integer sendRange;

    @Schema(description = "[json]使用范围相关信息")
    private String sendRangeData;

    @Schema(description = "最少订单金额")
    private BigDecimal minOrderAmount;

    @Schema(description = "优惠券可领取的开始时间 (已废弃)")
    private Long sendStartDate;

    @Schema(description = "优惠券可领取的结束时间 (已废弃)")
    private Long sendEndDate;

    @Schema(description = "优惠券使用时间方式，0：按领取后多少天内可用，1：按固定时间范围可用")
    private Integer sendType;

    @Schema(description = "优惠券多少天过期")
    private Integer useDay;

    @Schema(description = "优惠券可以使用的开始时间")
    private Long useStartDate;

    @Schema(description = "优惠券可以使用的结束时间")
    private Long useEndDate;

    @Schema(description = "是否展示")
    private Integer isShow;

    @Schema(description = "全场通用券（跨店铺）:0,否;1,是")
    private Integer isGlobal;

    @Schema(description = "是否为新人专享优惠券")
    private Integer isNewUser;

    @Schema(description = "商城点击领取:0,否;1,是")
    private Integer enabledClickGet;

    @Schema(description = "[JSON]仅限会员等级")
    private String limitUserRank;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "是否删除")
    private Integer isDelete;

    @Schema(description = "限制领取数量；0-表示不限制")
    private Integer limitNum;

    @Schema(description = "延迟天数：0 为立即生效 ")
    private Integer delayDay;

    @Schema(description = "发放总量")
    private Integer sendNum;

    @Schema(description = "优惠金额上限")
    private BigDecimal maxOrderAmount;

    @Schema(description = "优惠单位：1 元  2 件")
    private Integer couponUnit;

    @Schema(description = "满减类型：1 满多少 2 无门槛")
    private Integer reduceType;

    @Schema(description = "添加时间")
    private Long addTime;
}