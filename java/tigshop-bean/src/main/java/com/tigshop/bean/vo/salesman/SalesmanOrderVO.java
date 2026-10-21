package com.tigshop.bean.vo.salesman;

import cn.hutool.core.util.ObjectUtil;
import com.tigshop.bean.model.order.Order;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 库存日志VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */

@Data
@Schema(description = "销售员订单参数")
public class SalesmanOrderVO {

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "销售员订单ID")
    private Integer salesmanOrderId;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "销售员ID")
    private Integer salesmanId;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "商品ID")
    private Integer itemId;

    @Schema(description = "商品运营数据")
    private SalesmanProductDataVO salesmanProductData;

    @Schema(description = "订单金额")
    private BigDecimal orderAmount;

    @Schema(description = "结算配置数据")
    private SalesmanSettingVO salesmanSettlementData;

    @Schema(description = "结算时间")
    private String settlementTime;

    @Schema(description = "产品ID")
    private Integer productId;

    @Schema(description = "利润组成")
    private String profitComposition;

    @Schema(description = "销售员信息")
    private SalesmanStatisticalVO salesman;

    @Schema(description = "订单用户信息")
    private OrderUserInfoVO orderUserInfo;

    @Schema(description = "用户订单信息")
    private Order userOrder;

    @Schema(description = "用户订单项信息")
    private UserOrderItemVO userOrderItem;

    @Schema(description = "退款时间")
    private String refundTime;

    @Data
    public static class SalesmanSettingVO {

        @Schema(description = "代码")
        private String code;

        @Schema(description = "类型")
        private Integer dateType;

        @Schema(description = "描述")
        private String desc;

        @Schema(description = "类型")
        private Integer settlementType;

    }

    @Data
    public static class SalesmanProductDataVO {

        @Schema(description = "产品配置")
        private CommissionVO productCommission;

        @Schema(description = "销售员产品ID")
        private Integer salesmanProductId;

        @Schema(description = "产品ID")
        private Integer productId;

        @Schema(description = "是否加入")
        private Integer isJoin;

        @Schema(description = "佣金类型")
        private Integer commissionType;

        @Schema(description = "佣金数据")
        private List<CommissionDataVO> commissionData;

        @Schema(description = "添加时间")
        private Date addTime;

        @Schema(description = "更新时间")
        private Date updateTime;

        @Schema(description = "店铺ID")
        private Integer shopId;

        @Schema(description = "下单时分销员对象")
        private SalesmanJsonVO salesman;

        @Data
        public static class CommissionVO {
            @Schema(description = "佣金类型")
            private String productCommission;

            @Schema(description = "佣金数据")
            private String subCommission;
        }

        @Data
        public static class CommissionDataVO {
            @Schema(description = "等级数组")
            private List<LevelVO> levelArr;

            @Data
            public static class LevelVO {
                @Schema(description = "等级")
                private Integer level;

                @Schema(description = "下级销售员比率")
                private String downSalesmanRate;

                @Schema(description = "比率")
                private String rate;
            }
        }
    }

    @Data
    public static class OrderUserInfoVO {

        @Schema(description = "订单ID")
        private Integer orderId;

        @Schema(description = "订单号")
        private String orderSn;

        @Schema(description = "用户ID")
        private Integer userId;

        @Schema(description = "总金额")
        private BigDecimal totalAmount;

        @Schema(description = "添加时间")
        private String addTime;

        @Schema(description = "支付时间")
        private String payTime;

        @Schema(description = "订单状态")
        private Integer orderStatus;

        @Schema(description = "订单来源")
        private String orderSource;

        @Schema(description = "用户信息")
        private UserInfoVO user;
    }

    @Data
    public static class UserInfoVO {

        @Schema(description = "用户ID")
        private Integer userId;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "用户昵称")
        private String nickname;

        @Sensitive(type = SensitiveType.PHONE)
        @Schema(description = "手机号")
        private String mobile;

        public String getNickname() {
            return ObjectUtil.isEmpty(nickname) ? username : nickname;
        }
    }

    @Data
    public static class UserOrderItemVO {

        @Schema(description = "商品ID")
        private Integer itemId;

        @Schema(description = "订单ID")
        private Integer orderId;

        @Schema(description = "订单号")
        private String orderSn;

        @Schema(description = "用户ID")
        private Integer userId;

        @Schema(description = "价格")
        private BigDecimal price;

        @Schema(description = "数量")
        private Integer quantity;

        @Schema(description = "产品ID")
        private Integer productId;

        @Schema(description = "产品名称")
        private String productName;

        @Schema(description = "产品编号")
        private String productSn;

        @Schema(description = "缩略图")
        private String picThumb;

        @Schema(description = "SKU ID")
        private Integer skuId;

        @Schema(description = "SKU 数据")
        private String skuData;

        @Schema(description = "发货数量")
        private Integer deliveryQuantity;

        @Schema(description = "产品类型")
        private Integer productType;

        @Schema(description = "是否赠品")
        private Integer isGift;

        @Schema(description = "店铺ID")
        private Integer shopId;

        @Schema(description = "是否拼团")
        private Integer isPin;

        @Schema(description = "预付价格")
        private BigDecimal prepayPrice;

        @Schema(description = "佣金")
        private BigDecimal commission;

        @Schema(description = "原始价格")
        private BigDecimal originPrice;

        @Schema(description = "是否秒杀")
        private Integer isSeckill;

        @Schema(description = "额外SKU数据")
        private String extraSkuData;

        @Schema(description = "供应商ID")
        private Integer suppliersId;

        @Schema(description = "卡组名称")
        private String cardGroupName;

        @Schema(description = "总产品金额")
        private BigDecimal totalProductMoney;
    }
}

