package com.tigshop.adminapi.controller.proxyshopping;

import com.tigshop.bean.dto.cart.*;
import com.tigshop.bean.enums.cart.CartTypeEnum;
import com.tigshop.bean.enums.product.ProductType;
import com.tigshop.bean.model.cart.Cart;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.vo.cart.CartListByStoreVO;
import com.tigshop.bean.vo.product.ExchangeProductToCartVO;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.service.cart.CartGlobalChangePriceService;
import com.tigshop.service.cart.CartService;
import com.tigshop.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 代客下单购物车管理控制器
 * <p>
 * 只支持普通商品和虚拟商品，虚拟商品只能单独购买
 *
 * @author Tigshop项目组
 */
@RestController
@RequestMapping("/adminapi/proxyShopping/cart")
@RequiredArgsConstructor
@Tag(name = "代客下单购物车管理", description = "代客下单购物车管理")
@Validated
@PreAuthorize("@pms.hasPermission('proxyShoppingManage')")
public class ProxyShoppingCartController {

    private final CartService cartService;
    private final ProductService productService;
    private final MessageHelper messageHelper;
    private final CartGlobalChangePriceService cartGlobalChangePriceService;

    @PostMapping("/list")
    @Operation(summary = "获取购物车列表")
    public CartListByStoreVO list(@RequestBody ListDTO listDTO) {
        validateUserIdNotEmpty(listDTO.getUserId());
        listDTO.setIsCartList(true);
        listDTO.setShopId(HeaderUtils.getShopId());
        return cartService.getCartList(listDTO, listDTO.getUserId(), CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode());
    }

    @PostMapping("/addToCart")
    @Operation(summary = "添加商品到购物车")
    public ExchangeProductToCartVO addProductToCart(@RequestBody @Valid AddToCartDTO addToCartDTO) {
        validateUserIdNotEmpty(addToCartDTO.getUserId());

        // 验证商品类型
        Product product = productService.getById(addToCartDTO.getId());
        if (product == null) {
            throw new GlobalException("商品不存在");
        }

        isCanAddToCart(addToCartDTO,product);

        var cartType = adminShopping2CartType(product);
        addToCartDTO.setType(cartType);
        addToCartDTO.setFinalCartType(CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode());
        // 设置当前门店ID，确保购物车数据按门店隔离
        addToCartDTO.setShopId(HeaderUtils.getShopId());

        addToCartDTO.setIsQuick(false);
        Boolean isAdded = cartService.addProductToCart(addToCartDTO.getUserId(), addToCartDTO);

        if (isAdded && addToCartDTO.getUserId() != null) {
            cartGlobalChangePriceService.removeById(addToCartDTO.getUserId());
        }

        return ExchangeProductToCartVO.builder().item(isAdded)
                //代表加入的是代客下单逻辑,不能影响前端用户的购物车数据
                .flowType(CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode()).build();
    }

    private Integer adminShopping2CartType(Product product) {
        var cartType = switch (product.getProductType()) {
            case 1 -> CartTypeEnum.TYPE_NORMAL.getCode();
            case 2 -> CartTypeEnum.TYPE_VIRTUAL.getCode();
            case 3 -> CartTypeEnum.TYPE_CARD.getCode();
            case 4 -> CartTypeEnum.TYPE_PAID.getCode();
            case 5 -> CartTypeEnum.TYPE_RESERVATION_HOME.getCode();
            case 6 -> CartTypeEnum.TYPE_RESERVATION_STORE.getCode();
            case 7, 8 -> CartTypeEnum.TYPE_GIFT_CARD.getCode();
            default -> throw new GlobalException("商品类型错误");
        };
        return cartType;
    }

    @PostMapping("/clearCart")
    @Operation(summary = "清空购物车")
    public void clearCart(@RequestBody @Valid ProxyShoppingUserParam param) {
        validateUserIdNotEmpty(param.getUserId());
        cartService.clearCart(param.getUserId(), CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode());
    }

    @PostMapping("/removeItem")
    @Operation(summary = "删除购物车商品")
    public void removeItem(@RequestBody RemoveItemDTO removeItem) {
        cartService.removeBatchByIds(removeItem.getCartIds());
    }

    @PostMapping("/updateItem")
    @Operation(summary = "更新购物车商品")
    public void updateItem(@Valid @RequestBody UpdateItemDTO updateItem) {
        cartService.updateCartItem(updateItem);
    }

    @PostMapping("/updateCheck")
    @Operation(summary = "更新选中状态")
    public void updateCheck(@RequestBody UpdateCheckDTO updateCheckDTO) {
        validateUserIdNotEmpty(updateCheckDTO.getUserId());
        cartService.updateCheck(updateCheckDTO.getData(), updateCheckDTO.getUserId());
        cartGlobalChangePriceService.removeById(updateCheckDTO.getUserId());
    }

    @GetMapping("/getCount")
    @Operation(summary = "获取购物车商品数量")
    public Integer getCount(@RequestParam(value = "shopId", required = false) Integer shopId, @RequestParam(value = "userId") Integer userId) {
        validateUserIdNotEmpty(userId);
        List<Cart> carts = cartService.lambdaQuery()
                .eq(shopId != null, Cart::getShopId, shopId)
                .eq(Cart::getType, CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode())
                .eq(Cart::getUserId, userId).list();
        return carts.stream().map(Cart::getQuantity).reduce(0, Integer::sum);
    }

    /**
     * 验证用户ID不能为空
     */
    private void validateUserIdNotEmpty(Integer userId) {
        if (Objects.isNull(userId) || userId <= 0) {
            throw new GlobalException("请选择用户");
        }
    }

    /**
     * 验证商品类型是否支持代客下单
     * 代客下单只支持普通商品(1)、虚拟商品(2)、卡密商品(3)
     */
    private void validateProductTypeForProxyShopping(Integer productType) {
        if (productType == null) {
            return;
        }
        if (!ProxyShoppingProductController.ADMIN_PROXY_SHOPPING_PRODUCT_TYPES.contains(productType)) {
            throw new GlobalException("代客下单不支持该商品类型");
        }
    }

    /**
     * 验证虚拟商品是否可以添加到购物车
     * 虚拟商品只能单独购买，购物车中已有其他商品时不能添加虚拟商品
     * 购物车中已有虚拟商品时不能添加其他商品
     */
    private void isCanAddToCart(AddToCartDTO addToCartDTO, Product product) {
        validateProductTypeForProxyShopping(product.getProductType());
        // 查询用户购物车中的商品
        List<Cart> cartList = cartService.lambdaQuery().eq(Cart::getUserId, addToCartDTO.getUserId())
                .eq(Cart::getShopId, HeaderUtils.getShopId())
                .eq(Cart::getType, CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode()).list();
        if (cartList == null || cartList.isEmpty()) {
            return;
        }
        boolean isSingleBuy = cartList.stream().anyMatch(cart -> isSingleBuy(cart.getProductType()));
        if (isSingleBuy) {
            Cart cart = cartList.stream().findFirst().get();
            Integer productId = cart.getProductId();
            Integer skuId =Optional.ofNullable(cart.getSkuId()).orElse(0);
            Integer curSkuId = Optional.ofNullable(addToCartDTO.getSkuId()).orElse(0);
            Integer curProductId = addToCartDTO.getId();
            if (!curProductId.equals(productId) || !curSkuId.equals(skuId)) {
                throw new GlobalException(messageHelper.get("error.cart.product.conflict"));
            }
            return;
        }
        if (cartList.size() > 0 && isSingleBuy(product.getProductType())) {
            throw new GlobalException(messageHelper.get("error.cart.product.conflict"));
        }
    }


    /**
     * 是否是单个购买商品
     *
     * @param productType
     * @return
     */
    private boolean isSingleBuy(Integer productType) {
        return productType == ProductType.PRODUCT_TYPE_VIRTUAL.getCode() || productType == ProductType.PRODUCT_TYPE_CARD.getCode() || productType == ProductType.PRODUCT_TYPE_GIFT_CARD.getCode() || productType == ProductType.PRODUCT_TYPE_GIFT_CARD_VIRTUAL.getCode() || productType == ProductType.PRODUCT_TYPE_PAID.getCode();
    }

    /**
     * 代客下单用户参数
     */
    @lombok.Data
    public static class ProxyShoppingUserParam {
        private Integer userId;
    }
}
