package com.tigshop.bean.vo.order;

import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.annotation.TimestampFormat;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 派送订单信息VO（用于订单详情展示）
 *
 * @author Tigshop团队
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "派送订单信息")
public class DeliveryOrderInfoVO {

    @Schema(description = "派送订单ID")
    private Integer deliveryOrderId;

    @Schema(description = "派送订单编号")
    private String deliveryOrderSn;

    @Schema(description = "派送订单状态：10-待派发，20-待取货/待服务，30-配送中/服务中，40-已完成，50-已取消")
    private Integer status;

    @Schema(description = "派送订单状态名称（根据订单类型动态显示：同城配送-待备货/待取货/配送中/已送达；预约上门-预约完成/待上门/服务中/已完成）")
    private String statusName;

    @Schema(description = "配送员/服务员ID")
    private Integer staffId;

    @Schema(description = "配送员/服务员姓名；carrierType=2（美团）时复用为美团骑手姓名")
    private String staffName;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "配送员/服务员电话；carrierType=2（美团）时复用为美团骑手电话")
    private String staffPhone;

    @Schema(description = "配送员/服务员头像")
    private String staffAvatar;

    @Schema(description = "派送订单类型：1-配送订单，2-服务订单")
    private Integer type;

    @Schema(description = "派送订单类型名称")
    private String typeName;

    @Schema(description = "取货照片（JSON数组）")
    private List<String> pickupPhoto;

    @Schema(description = " 完成照片（JSON数组）")
    private List<String> completePhoto;

    @Schema(description = "服务前照片（JSON数组）")
    private List<String> beforeServicePhoto;

    @Schema(description = "服务后照片（JSON数组）")
    private List<String> afterServicePhoto;


    /**
     * 取货时间/开始服务时间（Unix时间戳）
     */
    @TimestampFormat
    private Integer pickupTime;

    /**
     * 完成时间（Unix时间戳）
     */
    @TimestampFormat
    private Integer completeTime;

    @Schema(description = "员工收益总额（配送费 + 服务费）")
    private BigDecimal staffEarnings;

    @Schema(description = "运力类型：1-自建配送，2-美团配送")
    private Integer carrierType;

    @Schema(description = "配送类型名称：商家配送 / 美团配送")
    private String carrierTypeName;

    /**
     * 美团发单时间 / 订单进入「已发货待配送」的时间点（Unix时间戳，carrierType=2 发单成功后回填）
     */
    @TimestampFormat
    @Schema(description = "美团发单时间（订单进入「已发货待配送」的时间点，前端可据此展示已等待时长；carrierType=2 有效）")
    private Integer mtDispatchTime;

    @Schema(description = "美团订单状态：0-待调度 20-已接单 30-已取货 50-已送达 99-已取消（carrierType=2 有效）")
    private Integer mtStatus;

    @Schema(description = "美团订单状态名称（carrierType=2 有效）")
    private String mtStatusName;

    @Schema(description = "美团配送距离（米）（carrierType=2 有效，发单成功后回填）")
    private Integer mtDistance;

    @Schema(description = "美团配送费（元）（carrierType=2 有效，发单成功后回填）")
    private BigDecimal mtFee;

    @Schema(description = "美团取消原因码（carrierType=2 且 mtStatus=99 有效；1201=无骑手接单被系统取消）")
    private Integer mtCancelCode;

    @Schema(description = "美团取消原因文案（carrierType=2 且 mtStatus=99 有效，供订单详情展示取消缘由）")
    private String mtCancelReason;

    @Schema(description = "是否因长时间无骑手接单被美团系统取消（true 时可引导门店改为商家自送）")
    private Boolean mtNoRiderCancelled;

    @Schema(description = "商家取货点纬度（取货地址纬度，用于C端地图追踪）")
    private BigDecimal senderLatitude;

    @Schema(description = "商家取货点经度（取货地址经度，用于C端地图追踪）")
    private BigDecimal senderLongitude;

    @Schema(description = "收货点纬度（配送/服务地址纬度，用于C端地图追踪）")
    private BigDecimal receiverLatitude;

    @Schema(description = "收货点经度（配送/服务地址经度，用于C端地图追踪）")
    private BigDecimal receiverLongitude;

    @Schema(description = "骑手实时纬度（carrierType=2 且骑手在途时实时回填，否则为空）")
    private BigDecimal riderLatitude;

    @Schema(description = "骑手实时经度（carrierType=2 且骑手在途时实时回填，否则为空）")
    private BigDecimal riderLongitude;
}

