// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 售后申请表model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("aftersales")
@Schema(description = "售后申请表")
public class OrderFinanceAftersales implements Serializable {

    @Schema(description = "售后申请表ID")
    @TableId(type = IdType.AUTO)
    private Integer aftersaleId;

    @Schema(description = "申请类型：1、退货退款，2、换货，3、维修，4、其它，5，仅退款")
    private Integer aftersaleType;

    @Schema(description = "状态：1、正在审核处理，2、售后审核通过,待处理，3、售后审核未通过，4、售后要求寄回，5、快递已寄回，6、已完成，7、已取消")
    private Integer status;

    @Schema(description = "用户凭证")
    private String pics;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "回复")
    private String reply;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "物流编号")
    private String trackingNo;

    @Schema(description = "物流公司")
    private String logisticsName;

    @Schema(description = "退款地址和联系电话")
    private String returnAddress;

    @Schema(description = "售后原因")
    private String aftersaleReason;

    @Schema(description = "售后编号")
    private String aftersalesSn;

    @Schema(description = "订单id")
    private Integer orderId;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "可退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "审核时间")
    private Long auditTime;

    @Schema(description = "处理时间")
    private Long dealTime;

    @Schema(description = "完结时间")
    private Long finalTime;


}