// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.user;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.poi.hpsf.Decimal;

import java.math.BigDecimal;

/**
 * 用户优惠券VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "用户优惠券参数")
public class UserCouponVO {
    @Schema(description = "优惠券描述")
    @JsonTranslate(dataType = 11)
    private String couponDesc;

    @Schema(description ="用户优惠折扣")
    private BigDecimal couponDiscount;

    @Schema(description ="coupon_id")
    private Integer couponId;

    @Schema(description ="优惠券金额")
    private BigDecimal couponMoney;

    @Schema(description ="优惠券名称")
    @JsonTranslate(dataType = 10)
    private String couponName;

    @Schema(description ="优惠券SN")
    private String couponSn;

    @Schema(description ="优惠券类型，1：满减券，2：折扣券")
    private Integer couponType;

    @Schema(description ="结束日期")
    private String endDate;

    @Schema(description ="id")
    private Integer id;

    @Schema(description ="全场通用券（跨店铺）:0,否;1,是")
    private Integer isGlobal;

    @Schema(description ="最少订单金额")
    private BigDecimal minOrderAmount;

    @Schema(description ="订单id")
    private Integer orderId;

    @Schema(description ="使用范围相关信息")
    private Integer[] sendRangeData;

    @Schema(description ="店铺id")
    private Integer shopId;

    @Schema(description ="开始日期")
    private String startDate;

    @Schema(description ="优惠券状态")
    private Integer status;

    @Schema(description ="优惠券状态描述")
    private String statusName;

    @Schema(description ="使用时间")
    private Long usedTime;

    @Schema(description ="用户id")
    private Integer userId;

    @Schema(description ="是否展示")
    private Integer isShow;
}