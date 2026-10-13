// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户优惠券VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户优惠券VO")
public class UserCoupon implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "会员优惠券id")
    private Integer id;

    @Schema(description = "优惠券类型ID")
    private Integer couponId;

    @Schema(description = "优惠券SN")
    private String couponSn;

    @Schema(description = "会员id")
    private Integer userId;

    @Schema(description = "优惠券使用的时间")
    private Long usedTime;

    @Schema(description = "优惠券所使用的订单ID")
    private Integer orderId;

    @Schema(description = "优惠券使用的开始时间")
    private Long startDate;

    @Schema(description = "优惠券可使用的结束时间")
    private Long endDate;

    @Schema(description = "来源类型:0-普通领取,1-抽奖中奖,2-活动赠送")
    private Integer sourceType;
}
