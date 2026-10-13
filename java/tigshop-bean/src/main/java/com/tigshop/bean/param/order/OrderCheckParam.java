// **---------------------------------------------------------------------+
// ** 文件 -- ${DESCRIPTION}
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.param.order;

import com.tigshop.bean.dto.user.UserAddressCreateDTO;
import com.tigshop.bean.enums.cart.CartTypeEnum;
import com.tigshop.bean.enums.order.SelectFromEnum;
import com.tigshop.bean.param.o2o.BaseChangePriceParam;
import com.tigshop.bean.vo.product.ProductPresaleVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 结算操作
 *
 * @author Jayce
 * @create 2024年10月22日 13:50
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "结算操作")
public class OrderCheckParam extends BaseChangePriceParam {

    @Schema(description = "抽奖记录id")
    private Integer lotteryRecordId;

    @Schema(description = "定金金额",hidden = true)
    BigDecimal depositAmount;

    @Schema(description = "用户id,收银台")
    private Integer userId;

    @Schema(description = "预约人")
    private String reservationName;

    @Schema(description = "预约手机")
    private String reservationMobile;

    @Schema(description = "是否启用改价(如果为false的话,就是不启用,不传或者true就是启用)")
    private Boolean enableChangePrice;

    @Schema(description = "预售商品信息",hidden = true)
    private ProductPresaleVO productPresale;


    /**
     * 配送方式 index 接口
     * 1. 取优先配送方式
     * 2. 取支持的配送方式
     * 3. 设置配送方式
     */
    @Schema(description = "配送方式 1 物流配送，2 自提，3 同城配送")
    private Integer deliveryType;


    @Schema(description = "地址ID")
    private Integer addressId;

    @Schema(description = "地址ID")
    private UserAddressCreateDTO address;

    @Schema(description = "配送方式")
    private Map<Integer, ShippingType> shippingType;

    @Schema(description = "支付方式ID")
    private Integer payTypeId;

    @Schema(description = "是否使用积分")
    private Integer usePoint;

    @Schema(description = "是否使用余额")
    private BigDecimal useBalance;

    @Schema(description = "是否使用账期支付：0-不使用，1-使用")
    private Integer useCredit;

    @Schema(description = "是否使用礼品卡")
    private BigDecimal useGiftCard;

    @Schema(description = "使用的优惠券ID列表")
    private List<Integer> useCouponIds;

    @Schema(description = "备注")
    private String buyerNote;

    @Schema(description = "发票")
    private InvoiceData invoiceData;

    @Schema(description = "流程类型")
    private Integer flowType;

    // *** UNKNOWN ***

    @Schema(description = "是否使用默认优惠劵")
    private Integer useDefaultCouponIds;

    @Schema(description = "选择的用户优惠券ID列表")
    private List<Integer> selectUserCouponIds;

    @Schema(description = "附加属性")
    private Map<String, String> productExtra;

    @Schema(description = "区域")
    private List<Integer> regionIds;

    @Schema(description = "从哪个页面跳转 1.首次加载 2.更新结算数据（配送类型，使用积分等） 3.更新优惠券 4.提交")
    private Integer selectFrom;

    @Schema(description = "门店id")
    private Integer shopId;

    @Schema(description = "产品预约时间ID")
    private Integer productReservationTimeId;

    @Schema(description = "预约日期")
    private String reservationDate;

    @Schema(description = "经度 ")
    private BigDecimal longitude;

    @Schema(description = "纬度 ")
    private BigDecimal latitude;

    @Schema(description = "后台管理员id")
    private Integer posUserId;


    @Schema(description = "订单来源（可选，用于代客下单等场景）")
    private String orderSource;

    @Schema(description = "自提点ID（代客下单自提时使用）")
    private Integer pickupId;

    @Schema(description = "自提联系人ID（代客下单自提时使用）")
    private Integer pickupContactId;

    @Schema(description = "自提联系人姓名（代客下单自提时使用）")
    private String pickupContactName;

    @Schema(description = "自提联系人手机号（代客下单自提时使用）")
    private String pickupContactMobile;

    @Data
    @Schema(description = "发票")
    public static class InvoiceData {
        @Schema(description = "发票类型")
        private Integer invoiceType;

        @Schema(description = "抬头类型")
        private Integer titleType;

        @Schema(description = "抬头")
        private String title;

        @Schema(description = "税号")
        private String taxNo;

        @Schema(description = "内容")
        private String content;

        @Schema(description = "电话")
        private String taxPhone;

        @Schema(description = "个人/单位名称")
        private String companyName;

        @Schema(description = "邮箱")
        private String email;

        @Schema(description = "手机号")
        private String mobile;

    }

    @Schema(description = "税率",hidden = true)
    private BigDecimal taxRate;

    @Data
    @Schema(description = "配送方式")
    public static class ShippingType {

        @Schema(description = "配送方式ID")
        private Integer typeId;

        @Schema(description = "店铺ID")
        private Integer shopId;

        @Schema(description = "配送方式名称")
        private String typeName;

        @Schema(description = "自提点id")
        private Integer pickupId;

        @Schema(description = "自提时间")
        private String expectPickupTime;
    }


    @Data
    @Schema(description = "改价信息")
    public static class CartChangePrice {
        @Schema(description = "购物车ID")
        private Integer cartId;

        @Schema(description = "SKU ID")
        private Integer skuId;

        @Schema(description = "商品ID")
        private Integer productId;

        @Schema(description = "原价(改价前的值) 总计的值(单个商品的价格*个数)")
        private BigDecimal originalPrice;

        @Schema(description = "改价值(如果是一口价的话,就是价格,折扣的话,就是折扣值,减少就是减少值)")
        private BigDecimal changePriceValue;

        @Schema(description = "改价类型(1 一口价 2 折扣 3 减少)")
        private Integer changePriceType;
    }

    public void indexInitParam() {
        this.selectFrom = SelectFromEnum.FIRST_LOAD.getValue();
        this.useBalance = BigDecimal.ZERO;
        this.useGiftCard = BigDecimal.ZERO;
        this.usePoint = 0;
    }

    public void submitInitParam(String useCoupon) {
        this.selectFrom = SelectFromEnum.CHECK_SUBMIT.getValue();
        this.useCouponIds = "0".equals(useCoupon) ? Collections.emptyList() : this.useCouponIds;
        this.flowType = this.flowType == null ? CartTypeEnum.TYPE_NORMAL.getCode() : this.flowType;
    }

}
