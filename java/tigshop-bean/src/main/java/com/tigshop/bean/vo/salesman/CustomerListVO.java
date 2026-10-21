// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.salesman;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.config.TimestampToDateSerializer;
import com.tigshop.common.enums.SensitiveType;
import com.tigshop.common.config.TimestampToDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Tigshop团队
 * @create 2025/6/20 10:53
 */
@Data
@Schema(description = "成交客户")
public class CustomerListVO {

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像URL")
    private String avatar;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "注册时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String regTime;

    @Schema(description = "等级名称")
    private String rankName;

    @Schema(description = "客户分类：ordered已支付且无售后订单客户，salesman下级分销员，customer普通客户")
    private String customerType;

    @Schema(description = "已支付且无售后订单数量")
    private Long orderCount;

    @Schema(description = "已支付订单实际成交净额（扣除实际退款）")
    private BigDecimal orderAmount;

    @Schema(description = "最后成交时间，Unix秒")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private Long lastOrderTime;

    @Schema(description = "绑定时间，Unix秒")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private Long bindTime;

    @Schema(description = "客户与分销员建立绑定关系时记录的渠道；无绑定渠道时为空字符串")
    private String channel;

    @Schema(description = "当前分销员对客户的备注；无备注时为空字符串")
    private String remark = "";

}
