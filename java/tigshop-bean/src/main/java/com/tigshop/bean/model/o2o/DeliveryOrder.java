// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.model.o2o;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 派送订单表
 *
 * @author Tigshop团队
 * @create 2024-12-16
 */
@TableName(value = "delivery_order")
@Data
public class DeliveryOrder {
    /**
     * 派送订单ID
     */
    @TableId(type = IdType.AUTO)
    private Integer deliveryOrderId;

    /**
     * 派送订单编号
     */
    private String deliveryOrderSn;

    /**
     * 关联商城订单ID
     */
    private Integer orderId;

    /**
     * 关联商城订单编号
     */
    private String orderSn;

    /**
     * 门店ID
     */
    private Integer shopId;

    /**
     * 类型：1-配送订单，2-服务订单
     */
    private Integer type;

    /**
     * 派单模式：1-抢单模式，2-手动派单
     */
    private Integer dispatchMode;

    /**
     * 状态：10-待派发，20-待取货/待服务，30-配送中/服务中，40-已完成，50-已取消
     */
    private Integer status;

    /**
     * 配送员/服务员ID
     */
    private Integer staffId;

    /**
     * 配送员/服务员姓名
     */
    private String staffName;

    /**
     * 配送员/服务员电话
     */
    private String staffPhone;

    /**
     * 收货人/服务对象姓名
     */
    private String consignee;

    /**
     * 收货人/服务对象电话
     */
    private String mobile;

    /**
     * 配送地址/服务地址
     */
    private String address;

    /**
     * 地区名称（省市区）
     */
    private String regionNames;

    /**
     * 配送地址/服务地址纬度
     */
    private BigDecimal latitude;

    /**
     * 配送地址/服务地址经度
     */
    private BigDecimal longitude;

    /**
     * 取货地址区域名称
     */
    private String pickupRegionNames;

    /**
     * 取货地址（门店地址）
     */
    private String pickupAddress;

    /**
     * 取货电话
     */
    private String pickupPhone;

    /**
     * 取货地址纬度
     */
    private BigDecimal pickupLatitude;

    /**
     * 取货地址经度
     */
    private BigDecimal pickupLongitude;

    /**
     * 预约服务时间（Unix时间戳）
     */
    private Integer serviceTime;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 买家备注
     */
    private String buyerNote;

    /**
     * 派单时间（Unix时间戳）
     */
    private Integer dispatchTime;

    /**
     * 抢单时间（Unix时间戳）
     */
    private Integer grabTime;

    /**
     * 取货时间/开始服务时间（Unix时间戳）
     */
    private Integer pickupTime;

    /**
     * 完成时间（Unix时间戳）
     */
    private Integer completeTime;

    /**
     * 取消时间（Unix时间戳）
     */
    private Integer cancelTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 取消方：1-配送员/服务员，2-后台管理员，3-系统
     */
    private Integer cancelBy;

    /**
     * 取货照片（JSON数组）
     */
    private String pickupPhoto;

    /**
     * 完成照片（JSON数组）
     */
    private String completePhoto;

    /**
     * 服务前照片（JSON数组）
     */
    private String beforeServicePhoto;

    /**
     * 服务后照片（JSON数组）
     */
    private String afterServicePhoto;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间（Unix时间戳）
     */
    private Integer createTime;

    /**
     * 更新时间（Unix时间戳）
     */
    private Integer updateTime;

    /**
     * 是否删除：0-否，1-是
     */
    @TableLogic
    private Integer isDelete;

    // ============ 美团配送运单字段（carrierType=2 时有效；美团子模块关闭时列仍存在但恒为自建） ============

    @Schema(description = "运力类型：1-自建配送，2-美团配送")
    private Integer carrierType;

    @Schema(description = "合作方配送单号（发给美团的 delivery_id，本系统唯一）")
    private String mtDeliveryId;

    @Schema(description = "美团配送单号（美团返回的 mt_peisong_id）")
    private String mtPeisongId;

    @Schema(description = "美团订单状态：0-待调度 20-已接单 30-已取货 50-已送达 99-已取消")
    private Integer mtStatus;

    @Schema(description = "美团配送服务代码（飞速达/快速达/及时达等）")
    private String mtDeliveryServiceCode;

    @Schema(description = "美团取消/异常代码")
    private Integer mtCancelCode;

    @Schema(description = "美团配送距离（米）")
    private Integer mtDistance;

    @Schema(description = "美团配送费（元）")
    private BigDecimal mtFee;
}
