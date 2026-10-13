package com.tigshop.api.controller.cart;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import com.tigshop.bean.dto.cart.*;
import com.tigshop.bean.enums.cart.CartTypeEnum;
import com.tigshop.bean.model.cart.Cart;
import com.tigshop.bean.vo.cart.CartByStoreVO;
import com.tigshop.bean.vo.cart.CartListByStoreVO;
import com.tigshop.bean.vo.content.CartCouponDiscountVO;
import com.tigshop.bean.vo.order.OrderCheckVO;
import com.tigshop.bean.vo.product.ExchangeProductToCartVO;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.service.cart.CartService;
import com.tigshop.service.setting.GalleryPicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static com.tigshop.common.constant.ResultTextConstants.ID_CANNOT_IS_NULL;

/**
 * 订单管理控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/api/cart/cart")
@Validated
@Tag(name = "购物车管理", description = "购物车管理")
public class CartController {

    @Resource
    CartService cartService;

    @Resource
    GalleryPicService galleryPicService;

    @Resource
    TranslatePackage translatePackage;

    @Resource
    TigshopProperties tigshopProperties;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public CartListByStoreVO list(ListDTO listDTO) {
        listDTO.setExcludeQuickProduct(true);
        CartListByStoreVO cartList = cartService.getCartList(listDTO, SecurityUtils.getCurrentUserId());
        //翻译操作
        return cartList;
    }

    private void fillOrderItemOverseasImage(CartListByStoreVO orderCheckVO) {
        if (tigshopProperties.getIsOverseas() == 1) {
            if (Objects.isNull(orderCheckVO)) {
                return;
            }
            List<CartByStoreVO> cartList = orderCheckVO.getCartList();
            if (CollUtil.isEmpty(cartList)) {
                return;
            }
            cartList.stream().forEach(store -> {
                if (CollUtil.isEmpty(store.getCarts())) {
                    return;
                }
                store.getCarts().stream().filter(Objects::nonNull).forEach(cart -> {
                    String picThumb = cart.getPicThumb();
                    if (StringUtils.isBlank(picThumb)) {
                        return;
                    }
                    JSONArray skuData = cart.getSkuData();
                    //skuData里面的属性缩略图和picThumb说明是属性缩略图,dataType是8，默认是7
                    int dataType = 7;
                    if (CollUtil.isNotEmpty(skuData) && skuData.stream().anyMatch(sku -> {
                        if (sku instanceof cn.hutool.json.JSONObject) {
                            cn.hutool.json.JSONObject jsonOb = (cn.hutool.json.JSONObject) sku;
                            String attrPicThumb = jsonOb.getStr("attrPicThumb");
                            return attrPicThumb != null && attrPicThumb.equals(picThumb);
                        }
                        return false;
                    })) {
                        dataType = 8;
                    }
                    // 获取原图
                    String originalImage = galleryPicService.getOriginalImage(picThumb);
                    String translate = translatePackage.translate(originalImage, dataType);
                    if (!Objects.equals(originalImage, translate)) {
                        // 有翻译数据，提取尺寸后缀并拼接到翻译后的图片URL
                        String suffix = picThumb.substring(originalImage.length());
                        cart.setPicThumb(translate + suffix);
                        return;
                    }
                    //如果是包含了!!pic,自动加上 400x400 (兼容性代码)
                    if (originalImage.contains("!!pic")) {
                        //原图的格式是 xxx!!pic.xxx 改成 xxx!!pic400x400.xxx
                        String picPrefix = originalImage.substring(0, originalImage.indexOf("!!pic") + 5);
                        String picSuffix = originalImage.substring(originalImage.indexOf("!!pic") + 5);
                        String translatedPic = picPrefix + "400x400" + picSuffix;
                        translate = translatePackage.translate(translatedPic, dataType);
                        if (!Objects.equals(translatedPic, translate)) {
                            // 有翻译数据，提取尺寸后缀并拼接到翻译后的图片URL
                            cart.setPicThumb(translate);
                            return;
                        }
                    }
                    //缩略图就是原图(兼容性代码)
                    translate = translatePackage.translate(picThumb, dataType);
                    if (!Objects.equals(picThumb, translate)) {
                        // 有翻译数据，提取尺寸后缀并拼接到翻译后的图片URL
                        cart.setPicThumb(translate);
                        return;
                    }
                });
            });
        }
    }


    @GetMapping("/getCount")
    @Operation(summary = "获取数量")
    public Integer getCount(@RequestParam(value = "shopId", required = false) Integer shopId) {
        return cartService.getCartCount(shopId, SecurityUtils.getCurrentUserId());
    }

    @PostMapping("/updateCheck")
    @Operation(summary = "更新选中状态")
    public void updateCheck(@RequestBody UpdateCheckDTO updateCheckDTO) {
        cartService.updateCheck(updateCheckDTO.getData(), SecurityUtils.getCurrentUserId());
    }

    @PostMapping("/updateItem")
    @Operation(summary = "更新购物车商品")
    public void updateItem(@Valid @RequestBody UpdateItemDTO updateItem) {
        cartService.updateCartItem(updateItem, SecurityUtils.getCurrentUserId());
    }

    @PostMapping("/removeItem")
    @Operation(summary = "删除购物车商品")
    public void removeItem(@RequestBody RemoveItemDTO removeItem) {
        cartService.removeBatchByIds(removeItem.getCartIds());
    }

    @PostMapping("/clear")
    @Operation(summary = "清空购物车")
    public void clear(@RequestParam(value = "shopId", required = false) Integer shopId) {
        cartService.lambdaUpdate()
                .eq(Cart::getUserId, SecurityUtils.getCurrentUserId())
                .ne(Cart::getType, CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode())
                .eq(shopId != null, Cart::getShopId, shopId)
                .remove();
    }

    @GetMapping("/getCouponDiscount")
    @Operation(summary = "获取优惠券折扣")
    public CartCouponDiscountVO getCouponDiscount(@RequestParam(value = "couponId") @NotNull(message = ID_CANNOT_IS_NULL) Integer couponId) {
        return cartService.getCouponDiscount(couponId);
    }

    @PostMapping("/addToCart")
    @Operation(summary = "添加购物车")
    public ExchangeProductToCartVO addProductToCart(@RequestBody AddToCartDTO addToCartDTO) {
        if (addToCartDTO.getType() == null) {
            addToCartDTO.setType(CartTypeEnum.TYPE_NORMAL.getCode());
        }
        Integer cartType = cartService.getCartTypeByProduct(addToCartDTO.getId(), addToCartDTO.getType());
        addToCartDTO.setType(cartType);

        cartService.addProductToCart(SecurityUtils.getCurrentUserId(), addToCartDTO);

        if (addToCartDTO.getLotteryRecordId() != null) {
            cartType = CartTypeEnum.TYPE_LOTTERY_PRODUCT.getCode();
        }

        return ExchangeProductToCartVO.builder()
                .item(true)
                .flowType(cartType)
                .build();
    }
}
