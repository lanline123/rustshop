package com.tigshop.bean.vo.o2o;

import com.tigshop.bean.model.o2o.UserPickupInfo;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.vo.order.OrderItemVO;
import com.tigshop.bean.vo.user.UserAddressVO;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Tigshop项目组
 * @create 2025年08月28日 19:39
 */
@Schema(description = "订单自提信息")
@Data
public class OrderPickupShipmentVO {
    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "自提手机号")
    private String mobile;
    @Schema(description = "核销二维码")
    private String pickupQrCode;
    @Schema(description = "核销sn")
    private String pickupSn;
    @Schema(description = "提货状态")
    private Integer shipmentStatus;
    @Schema(description = "提货状态名称")
    private String shipmentStatusName;
    @Schema(description = "订单商品信息")
    private List<OrderItemVO> orderItems;
    @Schema(description = "自提地址")
    private String shopDetailedAddress;
    @Schema(description = "自提点名称")
    private String pickupName;
    @Schema(description = "联系人信息")
    private UserPickupInfo userPickupInfo;
    @Schema(description = "门店")
    private Shop store;
    @Schema(description = "自提时间")
    private String expectPickupTime;
    @Schema(description = "到店联系人")
    private UserAddressVO userAddressInfo;
}