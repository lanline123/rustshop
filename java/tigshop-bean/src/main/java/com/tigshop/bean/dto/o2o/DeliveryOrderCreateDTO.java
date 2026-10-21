// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.o2o;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建派送订单DTO
 *
 * @author Tigshop团队
 * @create 2024-12-16
 */
@Data
@Schema(description = "创建派送订单参数")
public class DeliveryOrderCreateDTO {

    @Schema(description = "关联商城订单ID")
    @NotNull(message = "订单ID不能为空")
    private Integer orderId;

    @Schema(description = "关联商城订单编号")
    private String orderSn;

    @Schema(description = "门店ID")
    @NotNull(message = "门店ID不能为空")
    private Integer shopId;

    @Schema(description = "类型：1-配送订单，2-服务订单")
    @NotNull(message = "订单类型不能为空")
    private Integer type;

    @Schema(description = "收货人/服务对象姓名")
    private String consignee;

    @Schema(description = "收货人/服务对象电话")
    private String mobile;

    @Schema(description = "配送地址/服务地址")
    private String address;

    @Schema(description = "地区名称（省市区）")
    private String regionNames;

    @Schema(description = "配送地址/服务地址纬度")
    private BigDecimal latitude;

    @Schema(description = "配送地址/服务地址经度")
    private BigDecimal longitude;

    @Schema(description = "取货地址（门店地址）")
    private String pickupAddress;

    @Schema(description = "地区名称（省市区）")
    private String pickupRegionNames;

    @Schema(description = "取货电话")
    private String pickupPhone;

    @Schema(description = "取货地址纬度")
    private BigDecimal pickupLatitude;

    @Schema(description = "取货地址经度")
    private BigDecimal pickupLongitude;

    @Schema(description = "预约服务时间（Unix时间戳）")
    private Integer serviceTime;

    @Schema(description = "订单金额")
    private BigDecimal orderAmount;

    @Schema(description = "买家备注")
    private String buyerNote;
}
