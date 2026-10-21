package com.tigshop.adminapi.controller.proxyshopping;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.enums.cart.CartTypeEnum;
import com.tigshop.bean.model.cart.Cart;
import com.tigshop.bean.query.order.StoreShippingTypeQuery;
import com.tigshop.bean.vo.o2o.DeliveryOptionVO;
import com.tigshop.bean.vo.order.OrderCheckVO;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.mapper.cart.CartMapper;
import com.tigshop.service.order.OrderCheckService;
import com.tigshop.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 代客下单配送方式控制器
 *
 * @author Tigshop项目组
 */
@Tag(name = "代客下单配送方式", description = "代客下单配送方式")
@RestController
@RequestMapping("/adminapi/proxyShopping/delivery")
@RequiredArgsConstructor
@PreAuthorize("@pms.hasPermission('proxyShoppingManage')")
public class ProxyShoppingDeliveryController {

    private final OrderCheckService orderCheckService;
    private final MessageHelper messageHelper;
    private final CartMapper cartMapper;
    private final ProductService productService;

    @GetMapping("/getDeliveryOptions")
    @Operation(summary = "获取代客下单购物车商品配送方式")
    public DeliveryOptionVO getDeliveryOptions(@RequestParam(name = "latitude", required = false) BigDecimal latitude,
                                               @RequestParam(name = "longitude", required = false) BigDecimal longitude,
                                               @RequestParam(name = "userId") Integer userId) {
        if (Objects.isNull(userId)) {
            throw new GlobalException(messageHelper.get("error.user.select.required"));
        }
        // 使用当前登录门店ID做数据隔离，与购物车列表保持一致
        Integer shopId = HeaderUtils.getShopId();
        List<Cart> carts = cartMapper.selectList(Wrappers.lambdaQuery(Cart.class)
                .eq(Cart::getUserId, userId)
                .eq(Cart::getType, CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode())
                .eq(shopId != null, Cart::getShopId, shopId));
        return productService.getDeliveryOption(carts, latitude, longitude, null);
    }

    @GetMapping("/getStoreShippingType")
    @Operation(summary = "获取店铺配送方式")
    public Map<Integer, List<OrderCheckVO.ShippingType>> getStoreShippingType(StoreShippingTypeQuery query) {
        query.setFlowType(CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode());
        query.setExcludeQuickProduct(false);
        Integer userId = query.getUserId();
        if (Objects.isNull(userId)) {
            throw new GlobalException(messageHelper.get("error.user.select.required"));
        }
        // 使用当前登录门店ID做数据隔离
        if (query.getShopId() == null) {
            query.setShopId(HeaderUtils.getShopId());
        }
        return orderCheckService.getStoreShippingType(query);
    }
}
