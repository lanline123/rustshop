package com.tigshop.adminapi.controller.proxyshopping;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.enums.cart.CartTypeEnum;
import com.tigshop.bean.enums.order.OrderDeliveryTypeEnum;
import com.tigshop.bean.enums.order.SelectFromEnum;
import com.tigshop.bean.param.order.OrderCheckParam;
import com.tigshop.bean.query.shop.PickupPageQuery;
import com.tigshop.bean.vo.o2o.PickupListVO;
import com.tigshop.bean.vo.order.OrderCheckVO;
import com.tigshop.bean.vo.order.OrderSubmitVO;
import com.tigshop.bean.vo.setting.config.OrderSettingsVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.order.OrderCheckService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.shop.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static com.tigshop.bean.enums.setting.SettingsEnum.USE_COUPON;

/**
 * 代客下单订单结算控制器
 * <p>
 * 功能说明：
 * 1. 允许使用线上余额、积分支付及其他方式的付款方式
 * 2. 订单来源为“代客下单”
 * 3. 只能生成“普通订单”，无法创建“限时折扣”“积分商城”等活动订单
 * 4. 订单金额按商品原价或会员价计算、满足一定条件后享受优惠券、满减送活动
 * 5. 支持普通商品，虚拟商品下单，虚拟商品只能单独购买
 *
 * @author Tigshop项目组
 */
@RestController
@RequestMapping("/adminapi/proxyShopping/order/check")
@Validated
@RequiredArgsConstructor
@Tag(name = "代客下单订单结算", description = "代客下单订单结算")
@PreAuthorize("@pms.hasPermission('proxyShoppingManage')")
public class ProxyShoppingOrderCheckController {

    /**
     * 代客下单 - 订单来源标识
     */
    public static final String ORDER_SOURCE_PROXY_SHOPPING = "代客下单";

    private final OrderCheckService orderCheckService;
    private final ConfigService configService;
    private final ShopService shopService;

    @GetMapping("/getAvailablePaymentType")
    @Operation(summary = "获取可用支付方式")
    public List<OrderCheckVO.PaymentType> getAvailablePaymentType(@RequestParam(value = "flowType", required = false) Integer flowType) {
        return orderCheckService.getAvailablePaymentType(flowType);
    }

    @GetMapping("/pickupList")
    @Operation(summary = "获取自提点列表")
    public Page<PickupListVO> pickupList(PickupPageQuery query) {
        return shopService.pickupList(query);
    }

    @PostMapping("/index")
    @Operation(summary = "获取结算页信息")
    public OrderCheckVO index(@RequestBody OrderCheckParam param) {
        param.setFlowType(CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode());
        validateUserIdNotEmpty(param);
        param.indexInitParam();
        // 获取是否默认优惠券
        String useCoupon = configService.getConfigVal(USE_COUPON);
        param.setUseDefaultCouponIds(Integer.parseInt(useCoupon));
        param.setIsCartList(true);
        param.setShopId(HeaderUtils.getShopId());

        fillUsePointAndBalance(param);

        return orderCheckService.getOrderCheckData(param);
    }

    private void fillUsePointAndBalance(OrderCheckParam param) {
        String adminType = HeaderUtils.getAdminType();
        if ("admin".equals(adminType)) {
            OrderSettingsVO orderSettings = configService.getOrderSettings();
            if (orderSettings != null) {
                Integer adminShoppingUseBalance = orderSettings.getAdminShoppingUseBalance();
                if (!Integer.valueOf(1).equals(adminShoppingUseBalance)) {
                    param.setUseBalance(BigDecimal.ZERO);
                }
                Integer adminShoppingUsePoint = orderSettings.getAdminShoppingUsePoint();
                if (!Integer.valueOf(1).equals(adminShoppingUsePoint)) {
                    param.setUsePoint(0);
                }
            }
        } else {
            param.setUseBalance(BigDecimal.ZERO);
            param.setUsePoint(0);
        }
    }


    @PostMapping("/update")
    @Operation(summary = "更新结算信息")
    public OrderCheckVO update(@RequestBody OrderCheckParam param) {
        param.setFlowType(CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode());
        validateUserIdNotEmpty(param);
        param.setSelectFrom(SelectFromEnum.CHECK_INDEX.getValue());
        if (Boolean.FALSE.equals(param.getEnableChangePrice())) {
            param.setIsCartList(false);
            param.setOnlyShow(false);
        } else {
            param.setIsCartList(true);
        }
        param.setShopId(HeaderUtils.getShopId());
        fillUsePointAndBalance(param);
        return orderCheckService.getOrderCheckData(param);
    }

    @PostMapping("/updateCoupon")
    @Operation(summary = "更新优惠券")
    public OrderCheckVO updateCoupon(@RequestBody OrderCheckParam param) {
        param.setFlowType(CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode());
        validateUserIdNotEmpty(param);
        param.setSelectFrom(SelectFromEnum.CHECK_UPDATE.getValue());
        param.setIsCartList(true);
        param.setShopId(HeaderUtils.getShopId());
        fillUsePointAndBalance(param);
        return orderCheckService.getOrderCheckData(param);
    }

    @PostMapping("/changePrice")
    @Operation(summary = "改价")
    public OrderCheckVO changePrice(@RequestBody OrderCheckParam param) {
        param.setFlowType(CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode());
        validateUserIdNotEmpty(param);
        validateChangePriceValue(param);
        param.setSelectFrom(SelectFromEnum.CHECK_INDEX.getValue());
        param.setShopId(HeaderUtils.getShopId());
        param.setIsCartList(false);
        param.setOnlyShow(false);
        fillUsePointAndBalance(param);
        return orderCheckService.getOrderCheckData(param);
    }

    @PostMapping("/changePriceShow")
    @Operation(summary = "改价展示")
    public OrderCheckVO changePriceShow(@RequestBody OrderCheckParam param) {
        param.setFlowType(CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode());
        validateUserIdNotEmpty(param);
        validateChangePriceValue(param);
        param.setShopId(HeaderUtils.getShopId());
        if (param.getChangePriceValue() == null
                && (param.getCartChangePrices() == null || param.getCartChangePrices().isEmpty())) {
            param.setIsCartList(true);
        } else {
            param.setIsCartList(false);
        }
        param.setOnlyShow(true);
        param.indexInitParam();
        fillUsePointAndBalance(param);
        return orderCheckService.getOrderCheckData(param);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交订单")
    public OrderSubmitVO submit(@RequestBody OrderCheckParam param) {

        param.setFlowType(CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode());

        // 自提订单不要求收货地址，其他配送方式必须选择收货地址
        if (!Objects.equals(param.getDeliveryType(), OrderDeliveryTypeEnum.PICKUP.getCode())) {
            if (Objects.isNull(param.getAddressId())) {
                throw new GlobalException("请选择收货地址");
            }
        }

        param.setPosUserId(SecurityUtils.getCurrentAdminId());
        param.setIsCartList(true);
        param.setOrderSource(ORDER_SOURCE_PROXY_SHOPPING);
        param.setCashAmount(BigDecimal.ZERO);
        param.setCashChangeAmount(BigDecimal.ZERO);
        param.setShopId(HeaderUtils.getShopId());

        fillUsePointAndBalance(param);
        return orderCheckService.submit(param);
    }

    /**
     * 验证改价参数：减价模式下，减价金额不能大于应付金额
     */
    private void validateChangePriceValue(OrderCheckParam param) {
        if (Integer.valueOf(3).equals(param.getTotalChangePriceType())
                && param.getChangePriceValue() != null
                && param.getOriginalPrice() != null
                && param.getChangePriceValue().compareTo(param.getOriginalPrice()) > 0) {
            throw new GlobalException("减价金额不能大于应付金额");
        }
    }

    /**
     * 验证用户ID不能为空
     *
     * @param param 参数
     * @return 用户ID
     */
    private Integer validateUserIdNotEmpty(OrderCheckParam param) {
        Integer userId = param.getUserId();
        if (Objects.isNull(userId) || userId <= 0) {
            throw new GlobalException("请选择用户");
        }
        return userId;
    }
}
