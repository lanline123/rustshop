package com.tigshop.service.cart.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.dto.cart.*;
import com.tigshop.bean.dto.product.ProductDetailDTO;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.dto.promotion.SeckillItemInfoDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.cart.CartTypeEnum;
import com.tigshop.bean.enums.order.AftersalesStatusEnum;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.order.PaymentStatus;
import com.tigshop.bean.enums.product.ProductStatus;
import com.tigshop.bean.enums.product.ProductType;
import com.tigshop.bean.enums.promotion.PromotionType;
import com.tigshop.bean.enums.promotion.SendRangeEnum;
import com.tigshop.bean.enums.promotion.ValidityTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.cart.Cart;
import com.tigshop.bean.model.cart.CartGlobalChangePrice;
import com.tigshop.bean.model.o2o.ShopLocalDeliveryConfig;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.AftersalesItem;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.product.ECard;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductAttribute;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.promotion.GiftCardGroups;
import com.tigshop.bean.model.promotion.GiftCards;
import com.tigshop.bean.model.promotion.LotteryRecord;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserCoupon;
import com.tigshop.bean.param.o2o.BaseChangePriceParam;
import com.tigshop.bean.param.order.OrderCheckParam;
import com.tigshop.bean.param.product.ProductSaveParam;
import com.tigshop.bean.param.promotion.giftcard.ExchangeCardParam;
import com.tigshop.bean.vo.cart.CartByStoreVO;
import com.tigshop.bean.vo.cart.CartListByStoreVO;
import com.tigshop.bean.vo.cart.CartVO;
import com.tigshop.bean.vo.content.CartCouponDiscountVO;
import com.tigshop.bean.vo.product.ProductAvailabilityVO;
import com.tigshop.bean.vo.promotion.CouponVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.mapper.cart.CartMapper;
import com.tigshop.mapper.o2o.ShopLocalDeliveryConfigMapper;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.product.ECardMapper;
import com.tigshop.mapper.promotion.GiftCardGroupsMapper;
import com.tigshop.mapper.promotion.GiftCardsMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.cart.CartGlobalChangePriceService;
import com.tigshop.service.cart.CartService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.order.AftersalesItemService;
import com.tigshop.service.order.AftersalesService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.order.OrderOnboardingGuard;
import com.tigshop.service.product.ECardService;
import com.tigshop.service.product.ProductAttributesService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.promotion.*;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.shop.ShopService;
import com.tigshop.service.support.PosCheckoutContext;
import com.tigshop.service.user.UserCouponService;
import com.tigshop.service.user.MemberBenefitService;
import com.tigshop.service.user.UserService;
import com.tigshop.service.wholesale.CartWholesaleEnhancer;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.bean.enums.product.ProductType.PRODUCT_TYPE_CARD;
import static com.tigshop.bean.enums.product.ProductType.PRODUCT_TYPE_PAID;
import static com.tigshop.bean.enums.setting.SettingsEnum.USE_COUPON;
import static com.tigshop.common.constant.order.OrderConstants.ORDER_NOT_EXIST;
import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 购物车
 *
 * @author Tigshop团队
 * @create 2024-09-30 15:16:06
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CartServiceImpl extends BaseServiceImpl<CartMapper, Cart> implements CartService {

    private final ProductService productService;

    private final CouponService couponService;

    private final ProductSkuService productSkuService;

    private final ShopService shopService;

    private final MemberBenefitService memberBenefitService;

    private final UserMapper userMapper;

    private final OrderItemMapper orderItemMapper;

    private final ECardMapper eCardMapper;

    private final ProductAttributesService productAttributesService;

    private final UserCouponService userCouponService;

    private final ProductPromotionService productPromotionService;

    private final TimeDiscountService timeDiscountService;

    private final SeckillService seckillService;

    private final TranslatePackageImpl translatePackage;
    private final MessageHelper messageHelper;

    private final OrderService orderService;

    private final ConfigService configService;

    private final OrderOnboardingGuard orderOnboardingGuard;

    private final UserService userService;

    private final ILotteryRecordService lotteryRecordService;

    private final ECardService eCardService;

    private final AftersalesService aftersalesService;

    private final AftersalesItemService aftersalesItemService;
    private final GrouponPromotionService grouponPromotionService;
    private final TigshopProperties tigshopProperties;

    private final ShopLocalDeliveryConfigMapper shopLocalDeliveryConfigMapper;

    private final GiftCardsMapper giftCardsMapper;

    private final CartGlobalChangePriceService cartGlobalChangePriceService;
    private final GiftCardGroupsMapper giftCardGroupsMapper;

    private final CartWholesaleEnhancer cartWholesaleEnhancer;

    /**
     * M2 带 ctx 的内部返回类型；仅 wholesale profile 存在（CD-024 零技术债：
     * M1 装配复用 M2 prefetch 产物，避免重复 prefetch）。
     */
    @lombok.Value
    @lombok.Builder
    public static class CartListWithCtxResult {
        java.util.List<com.tigshop.bean.vo.cart.CartVO> cartVOList;
        com.tigshop.service.wholesale.CartWholesaleEnhancer.CartWholesaleContext wsCtx;
    }

    @Override
    public CartListByStoreVO getCartListByStore(boolean isChecked, Integer type, Map<String, Object> filter, Integer storeShopId, Integer userId, Boolean excludeQuickProduct) {
        // wholesale profile: 走带 ctx 的新装配路径，复用 prefetch（CD-024 / CD-017，避免 M1 重复 prefetch）。
        // if(true) 包裹防 javac 误判后续语句不可达；B2C profile 下整段被 VORT 剔除，M1 体字面无变化（T-503 javap 通过保障）。
        if (true) {
            return getCartListByStoreWithWholesaleCtx(isChecked, type, filter, storeShopId, userId, excludeQuickProduct);
        }
        List<CartVO> cartVOList = getCartList(isChecked, type, filter, storeShopId, userId, excludeQuickProduct);
        // 按shop_id来分组
        Map<Integer, List<CartVO>> cartMap = cartVOList.stream().collect(java.util.stream.Collectors.groupingBy(CartVO::getShopId));

        CartListByStoreVO cartListByStoreVO = new CartListByStoreVO();
        // 循环处理每个shop的数据
        CartListByStoreVO.CartTotal total = cartListByStoreVO.getTotal();

        cartMap.forEach((shopId, carts) -> {
            CartByStoreVO cartByStoreVO = new CartByStoreVO();
            cartByStoreVO.setShopId(shopId);
            if (carts.getFirst().getShop() != null) {
                cartByStoreVO.setShopTitle(translatePackage.translate(carts.getFirst().getShop().getShopTitle()));
            }
            // 循环carts处理购物车数据
            for (CartVO cart : carts) {
                if (cart.getProductType() == PRODUCT_TYPE_CARD.getCode()) {
                    long count = eCardService.count(new LambdaQueryWrapper<ECard>().eq(ECard::getGroupId, cart.getCardGroupId()));
                    cart.setProductStock((int) count);
                    cart.setStock((int) count);
                } else if (cart.getProductType() == ProductType.PRODUCT_TYPE_GIFT_CARD.getCode()
                        || cart.getProductType() == ProductType.PRODUCT_TYPE_GIFT_CARD_VIRTUAL.getCode()) {
                    Long count = giftCardsMapper.selectCount(new LambdaQueryWrapper<GiftCards>()
                            .eq(GiftCards::getCardGroupsId, cart.getGiftCardGroupsId())
                            .isNull(GiftCards::getUserId));
                    cart.setProductStock(count.intValue());
                    cart.setStock(count.intValue());
                }

                cart.setIsDisabled(false);
                cart.setDisabledReason(null);
                boolean productOffline = cart.getProductStatus() == 0;
                boolean skuDisabled = cart.getSkuId() != null && cart.getSkuId() > 0
                        && (cart.getSku() == null || Objects.equals(cart.getSku().getIsEnabled(), Constants.NO));
                boolean soldOut = cart.getStock() == 0 && !ProductType.isReservationProduct(cart.getProductType());
                if (skuDisabled || ((soldOut || productOffline)
                        && !Objects.equals(type, CartTypeEnum.TYPE_LOTTERY_PRODUCT.getCode()))) {
                    cart.setIsDisabled(true);
                    cart.setDisabledReason(productOffline ? "product_offline" : skuDisabled ? "sku_disabled" : "sold_out");
                    if (cart.getIsChecked()) {
                        this.update(new LambdaUpdateWrapper<Cart>().eq(Cart::getCartId, cart.getCartId()).set(Cart::getIsChecked, 0));
                        cart.setIsChecked(false);
                    }
                }
                List<SeckillItemInfoDTO> seckillInfo = seckillService.getSeckillInfo(cart.getProductId(), cart.getShopId());
                if (CollUtil.isNotEmpty(seckillInfo)) {
                    seckillInfo.stream().filter(item -> Objects.equals(item.getProductId(), cart.getProductId()) && Objects.equals(item.getSkuId(), cart.getSkuId())).findFirst().ifPresent(item -> {
                        if (item.getSeckillStock() == 0) {
                            cart.setIsDisabled(true);
                            cart.setDisabledReason("sold_out");
                            cart.setIsChecked(false);
                        }
                    });

                }
                total.setTotalCount(cartListByStoreVO.getTotal().getTotalCount() + cart.getQuantity());
                if (cart.getIsChecked()) {
                    total.setCheckedCount(cartListByStoreVO.getTotal().getCheckedCount() + cart.getQuantity());
                    total.setProductAmount(cartListByStoreVO.getTotal().getProductAmount().add(cart.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()))));
                }
            }

            boolean noShippingFlag = carts.stream().anyMatch(cart -> Constants.YES.equals(cart.getNoShipping()));
            cartByStoreVO.setNoShipping(noShippingFlag ? Constants.YES : Constants.NO);

            // 优惠信息初始化
            total.setDiscountAfter(total.getProductAmount());
            total.setDiscountSeckillAmount(BigDecimal.ZERO);
            total.setDiscountProductPromotionAmount(BigDecimal.ZERO);
            total.setDiscountCouponAmount(BigDecimal.ZERO);
            total.setDiscountTimeDiscountAmount(BigDecimal.ZERO);
            total.setDiscountDiscountAmount(BigDecimal.ZERO);

            // 设置店铺购物车
            cartByStoreVO.setCarts(carts);
            List<CartByStoreVO> cardList = cartListByStoreVO.getCartList();
            cardList.add(cartByStoreVO);
            cartListByStoreVO.setCartList(cardList);
        });

        cartListByStoreVO.setTotal(total);
        return cartListByStoreVO;
    }

    /**
     * M1 wholesale-only 装配路径（CD-024 / CD-017）：
     * <p>方法体复制自 {@link #getCartListByStore} 的 B2C 主体（option Y：B2C 字面无变化优先于 DRY），
     * 区别仅在两点：
     * <ol>
     *   <li>调 {@link #getCartListWithCtx} 而非 {@link #getCartList} — 复用 prefetch 产物 wsCtx，
     *       M1 不重复 prefetch（CD-017 ≤9 DB 预算保障）。</li>
     *   <li>build 完毕后追加 {@link CartWholesaleEnhancer#applyTopLevel} 与 {@link CartWholesaleEnhancer#applyGroup}
     *       装配顶层 / 组级批发字段（design §5）。</li>
     * </ol>
     * <p>整方法仅在 common.wholesale profile 存在；B2C profile 下被 VORT 剔除。
     */
    private CartListByStoreVO getCartListByStoreWithWholesaleCtx(boolean isChecked, Integer type, Map<String, Object> filter, Integer storeShopId, Integer userId, Boolean excludeQuickProduct) {
        CartListWithCtxResult ctxResult = getCartListWithCtx(isChecked, type, filter, storeShopId, userId, excludeQuickProduct);
        List<CartVO> cartVOList = ctxResult.getCartVOList();
        CartWholesaleEnhancer.CartWholesaleContext wsCtx = ctxResult.getWsCtx();
        // 按shop_id来分组
        Map<Integer, List<CartVO>> cartMap = cartVOList.stream().collect(java.util.stream.Collectors.groupingBy(CartVO::getShopId));

        CartListByStoreVO cartListByStoreVO = new CartListByStoreVO();
        // 循环处理每个shop的数据
        CartListByStoreVO.CartTotal total = cartListByStoreVO.getTotal();

        cartMap.forEach((shopId, carts) -> {
            CartByStoreVO cartByStoreVO = new CartByStoreVO();
            cartByStoreVO.setShopId(shopId);
            if (carts.getFirst().getShop() != null) {
                cartByStoreVO.setShopTitle(translatePackage.translate(carts.getFirst().getShop().getShopTitle()));
            }
            // 循环carts处理购物车数据
            for (CartVO cart : carts) {
                if (cart.getProductType() == PRODUCT_TYPE_CARD.getCode()) {
                    long count = eCardService.count(new LambdaQueryWrapper<ECard>().eq(ECard::getGroupId, cart.getCardGroupId()));
                    cart.setProductStock((int) count);
                    cart.setStock((int) count);
                } else if (cart.getProductType() == ProductType.PRODUCT_TYPE_GIFT_CARD.getCode()
                        || cart.getProductType() == ProductType.PRODUCT_TYPE_GIFT_CARD_VIRTUAL.getCode()) {
                    Long count = giftCardsMapper.selectCount(new LambdaQueryWrapper<GiftCards>()
                            .eq(GiftCards::getCardGroupsId, cart.getGiftCardGroupsId())
                            .isNull(GiftCards::getUserId));
                    cart.setProductStock(count.intValue());
                    cart.setStock(count.intValue());
                }

                cart.setIsDisabled(false);
                cart.setDisabledReason(null);
                boolean productOffline = cart.getProductStatus() == 0;
                boolean skuDisabled = cart.getSkuId() != null && cart.getSkuId() > 0
                        && (cart.getSku() == null || Objects.equals(cart.getSku().getIsEnabled(), Constants.NO));
                boolean soldOut = cart.getStock() == 0 && !ProductType.isReservationProduct(cart.getProductType());
                if (skuDisabled || ((soldOut || productOffline)
                        && !Objects.equals(type, CartTypeEnum.TYPE_LOTTERY_PRODUCT.getCode()))) {
                    cart.setIsDisabled(true);
                    cart.setDisabledReason(productOffline ? "product_offline" : skuDisabled ? "sku_disabled" : "sold_out");
                    if (cart.getIsChecked()) {
                        this.update(new LambdaUpdateWrapper<Cart>().eq(Cart::getCartId, cart.getCartId()).set(Cart::getIsChecked, 0));
                        cart.setIsChecked(false);
                    }
                }
                List<SeckillItemInfoDTO> seckillInfo = seckillService.getSeckillInfo(cart.getProductId(), cart.getShopId());
                if (CollUtil.isNotEmpty(seckillInfo)) {
                    seckillInfo.stream().filter(item -> Objects.equals(item.getProductId(), cart.getProductId()) && Objects.equals(item.getSkuId(), cart.getSkuId())).findFirst().ifPresent(item -> {
                        if (item.getSeckillStock() == 0) {
                            cart.setIsDisabled(true);
                            cart.setDisabledReason("sold_out");
                            cart.setIsChecked(false);
                        }
                    });

                }
                total.setTotalCount(cartListByStoreVO.getTotal().getTotalCount() + cart.getQuantity());
                if (cart.getIsChecked()) {
                    total.setCheckedCount(cartListByStoreVO.getTotal().getCheckedCount() + cart.getQuantity());
                    total.setProductAmount(cartListByStoreVO.getTotal().getProductAmount().add(cart.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()))));
                }
            }

            boolean noShippingFlag = carts.stream().anyMatch(cart -> Constants.YES.equals(cart.getNoShipping()));
            cartByStoreVO.setNoShipping(noShippingFlag ? Constants.YES : Constants.NO);

            // 优惠信息初始化
            total.setDiscountAfter(total.getProductAmount());
            total.setDiscountSeckillAmount(BigDecimal.ZERO);
            total.setDiscountProductPromotionAmount(BigDecimal.ZERO);
            total.setDiscountCouponAmount(BigDecimal.ZERO);
            total.setDiscountTimeDiscountAmount(BigDecimal.ZERO);
            total.setDiscountDiscountAmount(BigDecimal.ZERO);

            // 设置店铺购物车
            cartByStoreVO.setCarts(carts);
            List<CartByStoreVO> cardList = cartListByStoreVO.getCartList();
            cardList.add(cartByStoreVO);
            cartListByStoreVO.setCartList(cardList);
        });

        cartListByStoreVO.setTotal(total);

        // wholesale 装配（顶层 + 组级；CD-024 复用 wsCtx 不再 prefetch）
        cartWholesaleEnhancer.applyTopLevel(cartListByStoreVO, wsCtx);
        if (cartListByStoreVO.getCartList() != null) {
            for (CartByStoreVO group : cartListByStoreVO.getCartList()) {
                cartWholesaleEnhancer.applyGroup(group, wsCtx);
            }
        }

        return cartListByStoreVO;
    }

    @Override
    public List<CartVO> getCartList(boolean isChecked, Integer type, Map<String, Object> filter, Integer storeShopId, Integer currentUserId, Boolean excludeQuickProduct) {
        // 进入购物车页面时，清理残留的快速购买临时行
//        if (Boolean.TRUE.equals(excludeQuickProduct)) {
//            this.remove(new LambdaQueryWrapper<Cart>()
//                    .eq(Cart::getUserId, currentUserId)
//                    .like(Cart::getExtraData, "\"isQuick\":true"));
//        }

        Object productId = filter != null ? filter.get("productId") : null;
        boolean excludeTimeDiscountPrice = filter != null
                && Boolean.TRUE.equals(filter.get("excludeTimeDiscountPrice"));
        //代客下单,不管有没有选中都可以加入
        isChecked = CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode().equals(type) ? false : isChecked;
        List<Cart> cartList = this.lambdaQuery().eq(storeShopId != null, Cart::getShopId, storeShopId)
                .eq(isChecked, Cart::getIsChecked, 1).eq(productId != null, Cart::getProductId, productId).eq(Cart::getType, type).eq(Cart::getUserId, currentUserId).list();

        // 非购物车页面（如结算页）加载已勾选商品时，检测是否混入残留的快速购买临时行
        // 纯快速购买结算：所有已勾选行都是快速购买 → 保留
        // 普通下单混入残留快速购买行：两种共存 → 过滤掉快速购买行
        boolean filterQuickBuyRows = false;
        if (excludeQuickProduct == null && CollUtil.isNotEmpty(cartList)) {
            boolean hasQuickBuy = cartList.stream().anyMatch(this::isQuickBuyItem);
            boolean hasNormal = cartList.stream().anyMatch(c -> !isQuickBuyItem(c));
            filterQuickBuyRows = hasQuickBuy && hasNormal;
        }

        //代客下单都当做选中
        if (CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode().equals(type)) {
            cartList.forEach(cart -> cart.setIsChecked(1));
        }

        // 取出ids对应的product
        Map<Integer, Product> productMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(cartList)) {
            List<Integer> productIds = cartList.stream().map(Cart::getProductId).toList();
            List<Product> products = productService.listByIds(productIds);
            productMap = products.stream().collect(Collectors.toMap(Product::getProductId, Function.identity()));
        }

        // 取出所有 sku_id 不为 0 的 sku_id 数据
        Map<Integer, ProductSku> skuMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(cartList)) {
            List<Integer> skuIds = cartList.stream().map(Cart::getSkuId).filter(skuId -> skuId != 0).toList();
            if (CollUtil.isNotEmpty(skuIds)) {
                List<ProductSku> skuList = productSkuService.listByIds(skuIds);
                skuMap = skuList.stream().collect(Collectors.toMap(ProductSku::getSkuId, Function.identity()));
            }
        }

        // 取出所有shop_id不为0的shop_id数据
        Map<Integer, Shop> shopMap = Collections.emptyMap();
        Map<Integer, ShopLocalDeliveryConfig> shopLocalDeliveryConfigMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(cartList)) {
            List<Integer> shopIds = cartList.stream().map(Cart::getShopId).filter(shopId -> shopId != 0).toList();
            if (CollUtil.isNotEmpty(shopIds)) {
                List<Shop> shopList = shopService.listByIds(shopIds);
                shopMap = shopList.stream().collect(Collectors.toMap(Shop::getShopId, Function.identity()));
            }
            //如果是is_o2o的配置的产品，则多取每个门店的shop_local_delivery_config的Mapper
        }


        List<CartVO> cartVOList = new ArrayList<>();
        CartWholesaleEnhancer.CartWholesaleContext wsCtx =
                cartWholesaleEnhancer.prefetch(cartList, currentUserId);
        // 循环不变量：会员等级列表 + 当前用户等级（整个购物车固定，循环外查一次；null 守卫避免 selectById NPE）
        Integer rankId = null;
        if (currentUserId > 0) {
            var currentUser = userMapper.selectById(currentUserId);
            rankId = currentUser != null ? currentUser.getRankId() : null;
        }
        BigDecimal memberDiscount = memberBenefitService.resolveDiscount(currentUserId, rankId);
        for (Cart cart : cartList) {
            //排除一些特殊的商品
            Product product = productMap.get(cart.getProductId());
            if (product == null) {
                continue;
            }
            if (filterQuickBuyRows && isQuickBuyItem(cart)) {
                continue;
            }
            if (Boolean.TRUE.equals(excludeQuickProduct) && !isAvailable(cart)) {
                continue;
            }
            ProductSku productSku = skuMap.get(cart.getSkuId());
            BigDecimal memberPrice;
            if (productSku != null) {
                memberPrice = productSku.getSkuPrice();
            } else {
                memberPrice = product.getProductPrice();
            }
            if (memberDiscount != null && memberDiscount.compareTo(BigDecimal.ZERO) > 0) {
                memberPrice = memberPrice.multiply(memberDiscount).divide(new BigDecimal(10), 2, RoundingMode.HALF_UP);
            }

            int skuId = cart.getSkuId() == null ? 0 : cart.getSkuId();
            // 商品存在性已由循环开头 productMap 判空保证，无需再查 count
            ProductAvailabilityVO productSkuDetail = excludeTimeDiscountPrice
                    ? productService.getProductFinalPrice(cart.getProductId(), skuId, 0, null, cart.getShopId(), currentUserId, true)
                    : productService.getProductFinalPrice(cart.getProductId(), skuId, 0, null, cart.getShopId(), currentUserId);


            // BigDecimal price = cart.getSkuId() != null && cart.getSkuId() != 0 ? productSku.getSkuPrice() : product.getProductPrice();
//            BigDecimal productPrice = productService.getProductFinalPrice(cart.getProductId(), price, cart.getSkuId());
            Integer stock = getCartStock(cart.getProductId(), cart.getSkuId(), true, cart.getShopId());
            Shop shop = shopMap.get(cart.getShopId());

            List<PromotionVO> promotions = productSkuDetail.getPromotion();
            boolean findGroupon = promotions.stream().anyMatch((promotion) -> Objects.equals(promotion.getType(), PromotionType.TYPE_GROUPON.getCode()));
            BigDecimal price;
            CartVO cartVO;
            //区分拼团活动的直接购买还是开团
            AddToCartDTO.GrouponData grouponData = JSON.parseObject(cart.getExtraData(), AddToCartDTO.GrouponData.class);
            if (findGroupon && grouponData != null && grouponData.getGrouponPromotionId() != null) {
                //走拼团活动
                BigDecimal originPrice;
                if (productSku != null) {
                    memberPrice = originPrice = productSku.getSkuPrice();
                } else {
                    memberPrice = originPrice = product.getProductPrice();
                }

                //算出拼团价
                cartVO = new CartVO(cart, product, productSku, shop, memberPrice, memberPrice, stock);
                grouponPromotionService.parsePrice(Arrays.asList(cartVO),
                        BeanUtil.copyProperties(promotions.stream().filter((promotion) -> Objects.equals(promotion.getType(), PromotionType.TYPE_GROUPON.getCode())).findFirst().get(), PromotionVO.class));
                price = memberPrice = cartVO.getPrice();

                cartVO = new CartVO(cart, product, productSku, shop, price, memberPrice, stock);
                //用于划横线
                cartVO.setOriginPrice(originPrice);
            } else {
                price = productSkuDetail.getPrice();


                cartVO = new CartVO(cart, product, productSku, shop, price, memberPrice, stock);
            }

            cartVO.setStartProductPrice(productSkuDetail.getStartProductPrice());

            cartVO.setLimitNumber(product.getLimitNumber());
            cartWholesaleEnhancer.applyLine(cartVO, cart, wsCtx);
            // CR-20260612-1622：倍数不对齐行（admin 改倍数遗留）自动向上取整 + 回写 + 提示
            cartWholesaleEnhancer.alignLineQuantityOnList(cartVO, cart, wsCtx, stock);
            cartVOList.add(cartVO);
        }

        return cartVOList;
    }

    /**
     * M2 wholesale-only overload（CD-024 / CD-017）：与 {@link #getCartList} 行为一致，
     * 但额外暴露 {@link CartWholesaleEnhancer.CartWholesaleContext}，供 M1
     * {@link #getCartListByStore} 装配时复用，避免重复 prefetch。
     *
     * <p>方法体复制自 {@link #getCartList}（option Y：B2C 字面无变化优先于 DRY）；
     * 整个方法仅在 common.wholesale profile 存在，因此移除内部 {@code @vort-if common.wholesale}
     * 冗余标记，保留 {@code @vort-if o2o / o2o.cycle / overseas} 正交特性标记。
     */
    public CartListWithCtxResult getCartListWithCtx(boolean isChecked, Integer type, Map<String, Object> filter, Integer storeShopId, Integer currentUserId, Boolean excludeQuickProduct) {
        // 进入购物车页面时，清理残留的快速购买临时行
        if (Boolean.TRUE.equals(excludeQuickProduct)) {
            this.remove(new LambdaQueryWrapper<Cart>()
                    .eq(Cart::getUserId, currentUserId)
                    .like(Cart::getExtraData, "\"isQuick\":true"));
        }

        Object productId = filter != null ? filter.get("productId") : null;
        boolean excludeTimeDiscountPrice = filter != null
                && Boolean.TRUE.equals(filter.get("excludeTimeDiscountPrice"));
        //代客下单,不管有没有选中都可以加入
        isChecked = CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode().equals(type) ? false : isChecked;
        List<Cart> cartList = this.lambdaQuery().eq(storeShopId != null, Cart::getShopId, storeShopId)
                .eq(isChecked, Cart::getIsChecked, 1).eq(productId != null, Cart::getProductId, productId).eq(Cart::getType, type).eq(Cart::getUserId, currentUserId).list();

        // 非购物车页面（如结算页）加载已勾选商品时，检测是否混入残留的快速购买临时行
        boolean filterQuickBuyRows = false;
        if (excludeQuickProduct == null && CollUtil.isNotEmpty(cartList)) {
            boolean hasQuickBuy = cartList.stream().anyMatch(this::isQuickBuyItem);
            boolean hasNormal = cartList.stream().anyMatch(c -> !isQuickBuyItem(c));
            filterQuickBuyRows = hasQuickBuy && hasNormal;
        }

        //代客下单都当做选中
        if (CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode().equals(type)) {
            cartList.forEach(cart -> cart.setIsChecked(1));
        }

        // 取出ids对应的product
        Map<Integer, Product> productMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(cartList)) {
            List<Integer> productIds = cartList.stream().map(Cart::getProductId).toList();
            List<Product> products = productService.listByIds(productIds);
            productMap = products.stream().collect(Collectors.toMap(Product::getProductId, Function.identity()));
        }

        // 取出所有 sku_id 不为 0 的 sku_id 数据
        Map<Integer, ProductSku> skuMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(cartList)) {
            List<Integer> skuIds = cartList.stream().map(Cart::getSkuId).filter(skuId -> skuId != 0).toList();
            if (CollUtil.isNotEmpty(skuIds)) {
                List<ProductSku> skuList = productSkuService.listByIds(skuIds);
                skuMap = skuList.stream().collect(Collectors.toMap(ProductSku::getSkuId, Function.identity()));
            }
        }

        // 取出所有shop_id不为0的shop_id数据
        Map<Integer, Shop> shopMap = Collections.emptyMap();
        Map<Integer, ShopLocalDeliveryConfig> shopLocalDeliveryConfigMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(cartList)) {
            List<Integer> shopIds = cartList.stream().map(Cart::getShopId).filter(shopId -> shopId != 0).toList();
            if (CollUtil.isNotEmpty(shopIds)) {
                List<Shop> shopList = shopService.listByIds(shopIds);
                shopMap = shopList.stream().collect(Collectors.toMap(Shop::getShopId, Function.identity()));
            }
        }


        List<CartVO> cartVOList = new ArrayList<>();
        CartWholesaleEnhancer.CartWholesaleContext wsCtx =
                cartWholesaleEnhancer.prefetch(cartList, currentUserId);
        // 循环不变量：会员等级列表 + 当前用户等级（整个购物车固定，循环外查一次；null 守卫避免 selectById NPE）
        Integer rankId = null;
        if (currentUserId > 0) {
            var currentUser = userMapper.selectById(currentUserId);
            rankId = currentUser != null ? currentUser.getRankId() : null;
        }
        BigDecimal memberDiscount = memberBenefitService.resolveDiscount(currentUserId, rankId);
        for (Cart cart : cartList) {
            //排除一些特殊的商品
            Product product = productMap.get(cart.getProductId());
            if (product == null) {
                continue;
            }
            if (filterQuickBuyRows && isQuickBuyItem(cart)) {
                continue;
            }
            if (Boolean.TRUE.equals(excludeQuickProduct) && !isAvailable(cart)) {
                continue;
            }
            ProductSku productSku = skuMap.get(cart.getSkuId());
            BigDecimal memberPrice;
            if (productSku != null) {
                memberPrice = productSku.getSkuPrice();
            } else {
                memberPrice = product.getProductPrice();
            }
            if (memberDiscount != null && memberDiscount.compareTo(BigDecimal.ZERO) > 0) {
                memberPrice = memberPrice.multiply(memberDiscount).divide(new BigDecimal(10), 2, RoundingMode.HALF_UP);
            }

            int skuId = cart.getSkuId() == null ? 0 : cart.getSkuId();
            // 商品存在性已由循环开头 productMap 判空保证，无需再查 count
            ProductAvailabilityVO productSkuDetail = excludeTimeDiscountPrice
                    ? productService.getProductFinalPrice(cart.getProductId(), skuId, 0, null, cart.getShopId(), currentUserId, true)
                    : productService.getProductFinalPrice(cart.getProductId(), skuId, 0, null, cart.getShopId(), currentUserId);

            Integer stock = getCartStock(cart.getProductId(), cart.getSkuId(), true, cart.getShopId());
            Shop shop = shopMap.get(cart.getShopId());

            List<PromotionVO> promotions = productSkuDetail.getPromotion();
            boolean findGroupon = promotions.stream().anyMatch((promotion) -> Objects.equals(promotion.getType(), PromotionType.TYPE_GROUPON.getCode()));
            BigDecimal price;
            CartVO cartVO;
            //区分拼团活动的直接购买还是开团
            AddToCartDTO.GrouponData grouponData = JSON.parseObject(cart.getExtraData(), AddToCartDTO.GrouponData.class);
            if (findGroupon && grouponData != null && grouponData.getGrouponPromotionId() != null) {
                BigDecimal originPrice;
                if (productSku != null) {
                    memberPrice = originPrice = productSku.getSkuPrice();
                } else {
                    memberPrice = originPrice = product.getProductPrice();
                }

                cartVO = new CartVO(cart, product, productSku, shop, memberPrice, memberPrice, stock);
                grouponPromotionService.parsePrice(Arrays.asList(cartVO),
                        BeanUtil.copyProperties(promotions.stream().filter((promotion) -> Objects.equals(promotion.getType(), PromotionType.TYPE_GROUPON.getCode())).findFirst().get(), PromotionVO.class));
                price = memberPrice = cartVO.getPrice();

                cartVO = new CartVO(cart, product, productSku, shop, price, memberPrice, stock);
                cartVO.setOriginPrice(originPrice);
            } else {
                price = productSkuDetail.getPrice();


                cartVO = new CartVO(cart, product, productSku, shop, price, memberPrice, stock);
            }

            cartVO.setStartProductPrice(productSkuDetail.getStartProductPrice());

            cartVO.setLimitNumber(product.getLimitNumber());
            cartWholesaleEnhancer.applyLine(cartVO, cart, wsCtx);
            // CR-20260612-1622：倍数不对齐行（admin 改倍数遗留）自动向上取整 + 回写 + 提示
            cartWholesaleEnhancer.alignLineQuantityOnList(cartVO, cart, wsCtx, stock);
            cartVOList.add(cartVO);
        }

        return CartListWithCtxResult.builder()
                .cartVOList(cartVOList)
                .wsCtx(wsCtx)
                .build();
    }


    @Override
    public Integer getCartCount(Integer shopId, Integer userId) {
        return baseMapper.sumAvailableCartQuantity(
                userId,
                shopId,
                CartTypeEnum.TYPE_NORMAL.getCode()
        );
    }

    private boolean isAvailable(Cart cart) {
        Integer type = cart.getType();
        if (isQuickBuyItem(cart)) {
            return false;
        }
        try {
            AddToCartDTO.GrouponData grouponData = JSON.parseObject(cart.getExtraData(), AddToCartDTO.GrouponData.class);
            if (grouponData != null && grouponData.getGrouponPromotionId() != null) {
                return false;
            }
        } catch (Exception e) {
            //抛出异常,说明不是拼团数据
        }
        if (type == CartTypeEnum.TYPE_PIN.getCode()) {
            return false;
        }
        if (type == CartTypeEnum.TYPE_EXCHANGE.getCode()) {
            return false;
        }
        if (type == CartTypeEnum.TYPE_RESERVATION_STORE.getCode()) {
            return false;
        }
        if (type == CartTypeEnum.TYPE_RESERVATION_HOME.getCode()) {
            return false;
        }
        if (type == CartTypeEnum.TYPE_RESERVATION_PRODUCT.getCode()) {
            return false;
        }
        if (type == CartTypeEnum.TYPE_GIFT_CARD.getCode()) {
            return false;
        }
        if (type == CartTypeEnum.TYPE_LOTTERY_PRODUCT.getCode()) {
            return false;
        }
        if (type == CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode()) {
            return false;
        }
        return true;
    }

    private boolean isQuickBuyItem(Cart cart) {
        try {
            JSONObject extraObj = JSON.parseObject(cart.getExtraData());
            return extraObj != null && Boolean.TRUE.equals(extraObj.getBoolean("isQuick"));
        } catch (Exception e) {
            return false;
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCheck(List<Map<String, Object>> list) {
        // POS/代客下单沿用后台管理员上下文，不绑定会员登录态；C 端则必须绑定当前会员。
        updateCheck(list, SecurityUtils.isAdminUser() ? null : getCurrentUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCheck(List<Map<String, Object>> list, Integer userId) {
        // 用户手动勾选商品说明进入了普通购物车流程，清理残留的快速购买临时行
        if (userId != null) {
            this.remove(new LambdaQueryWrapper<Cart>()
                    .eq(Cart::getUserId, userId)
                    .like(Cart::getExtraData, "\"isQuick\":true"));
        }

        if (CollUtil.isEmpty(list)) {
            return;
        }

        // 先完成归属和状态校验，再批量更新，避免部分更新后才发现非法购物车项。
        List<Cart> carts = new ArrayList<>();
        for (Map<String, Object> map : list) {
            Integer cartId = toInteger(map.get("cartId"));
            Integer isChecked = toInteger(map.get("isChecked"));
            Assert.notNull(cartId, () -> new GlobalException("购物车ID不能为空"));
            Assert.isTrue(Objects.equals(isChecked, Constants.YES) || Objects.equals(isChecked, Constants.NO),
                    () -> new GlobalException("购物车选中状态不正确"));
            Cart existing = this.lambdaQuery()
                    .eq(Cart::getCartId, cartId)
                    .eq(userId != null, Cart::getUserId, userId)
                    .one();
            Assert.notNull(existing, () -> new GlobalException(messageHelper.get("error.cart.item.not.exist")));
            if (Objects.equals(isChecked, Constants.YES)) {
                assertCartCanBeChecked(existing);
            }
            Cart cart = new Cart();
            cart.setCartId(cartId);
            cart.setIsChecked(isChecked);
            carts.add(cart);
        }
        this.updateBatchById(carts);
    }

    @Override
    public void updateCartItem(UpdateItemDTO updateItem) {
        updateCartItem(updateItem, null);
    }

    @Override
    public void updateCartItem(UpdateItemDTO updateItem, Integer userId) {
        Assert.notNull(updateItem.getData(), "请选择购买数量");
        Assert.notNull(updateItem.getData().getQuantity(), "请选择购买数量");
        Assert.isTrue(updateItem.getData().getQuantity() > 0, () -> new GlobalException(messageHelper.get("error.product.quantity.invalid")));

        Cart cart = this.lambdaQuery()
                .eq(Cart::getCartId, updateItem.getCartId())
                .eq(userId != null, Cart::getUserId, userId)
                .one();
        Assert.notNull(cart, () -> new GlobalException(messageHelper.get("error.cart.item.not.exist")));

        assertCartCanChangeQuantity(cart, updateItem.getData().getQuantity());

        int changeQuantity = updateItem.getData().getQuantity() - cart.getQuantity();
        checkProductLimitNumber(cart.getProductId(), cart.getUserId(), changeQuantity, cart.getCartId());

        Cart newCart = new Cart();
        newCart.setCartId(updateItem.getCartId());
        newCart.setQuantity(updateItem.getData().getQuantity());
        this.updateById(newCart);

        Integer cartUserId = cart.getUserId();
        if (Objects.nonNull(cartUserId)) {
            cartGlobalChangePriceService.removeById(cartUserId);
        }
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String string && StrUtil.isNotBlank(string)) {
            try {
                return Integer.valueOf(string);
            } catch (NumberFormatException ignored) {
                // 由调用方统一返回参数错误
            }
        }
        return null;
    }

    /**
     * POS 购物车库存读取：仅在 admin POS 请求下叠加门店 SKU 库存，其他 API/后台链路保持原有平台库存逻辑。
     */
    private Integer getCartStock(Integer productId, Integer skuId, boolean includeActivity, Integer shopId) {
        Integer platformStock = productService.getProductStock(productId, skuId, includeActivity, shopId);
        Integer effectiveStock = platformStock;
        return effectiveStock;
    }

    private boolean isPosAdminRequest() {
        if (RequestContextHolder.getRequestAttributes() == null) {
            return false;
        }
        return AdminTypeEnum.POS.getCode().equals(HeaderUtils.getAdminType());
    }

    private void assertCartCanBeChecked(Cart cart) {
        String disabledReason = getCartDisabledReason(cart);
        if (disabledReason == null) {
            return;
        }
        throw new GlobalException(getCartDisabledMessage(disabledReason));
    }

    private void assertCartCanChangeQuantity(Cart cart, Integer quantity) {
        String disabledReason = getCartDisabledReason(cart);
        if (disabledReason != null) {
            throw new GlobalException(getCartDisabledMessage(disabledReason));
        }
        Integer stock = getCartStock(cart.getProductId(), cart.getSkuId(), true, cart.getShopId());
        Assert.isFalse(stock < quantity, () -> new GlobalException(messageHelper.get("error.product.stock.insufficient")));
    }

    private String getCartDisabledReason(Cart cart) {
        Product product = productService.getById(cart.getProductId());
        if (product == null) {
            return "product_offline";
        }

        if (product.getProductStatus() == ProductStatus.OFF_SALE.getCode()) {
            return "product_offline";
        }
        if (cart.getSkuId() != null && cart.getSkuId() > 0) {
            ProductSku sku = productSkuService.getById(cart.getSkuId());
            if (sku == null || !Objects.equals(sku.getProductId(), cart.getProductId())
                    || !Objects.equals(sku.getIsEnabled(), Constants.YES)) {
                return "sku_disabled";
            }
        }
        Integer stock = getCartStock(cart.getProductId(), cart.getSkuId(), true, cart.getShopId());
        if (stock != null && stock == 0) {
            if (!ProductType.isReservationProduct(product.getProductType())) {
                return "sold_out";
            }
            boolean seckillSoldOut = seckillService.getSeckillInfo(cart.getProductId(), cart.getShopId()).stream()
                    .anyMatch(item -> Objects.equals(item.getProductId(), cart.getProductId())
                            && Objects.equals(item.getSkuId(), cart.getSkuId())
                            && item.getSeckillStock() == 0);
            if (seckillSoldOut) {
                return "sold_out";
            }
        }
        return null;
    }

    private String getCartDisabledMessage(String disabledReason) {
        return switch (disabledReason) {
            case "sku_disabled" -> "商品规格已禁用，请重新选择";
            case "product_offline" -> messageHelper.get("error.product.off.sale");
            case "sold_out" -> messageHelper.get("error.product.stock.insufficient");
            default -> messageHelper.get("error.product.unavailable");
        };
    }

    @Override
    public CartCouponDiscountVO getCouponDiscount(Integer couponId) {
        Integer currentUserId = getCurrentUserId();
        CouponVO coupon = couponService.detail(couponId);
        List<CartVO> cartList = getCartList(true, CartTypeEnum.TYPE_NORMAL.getCode(), null, null, currentUserId, null);
        Integer quantityCount = 0;
        BigDecimal checkedProductPriceSum = BigDecimal.ZERO;
        for (CartVO cartVO : cartList) {
            if (coupon.getSendRange() == SendRangeEnum.CATEGORY.getValue()) {
                if (!coupon.getSendRangeData().contains(cartVO.getCategoryId())) {
                    continue;
                }
            } else if (coupon.getSendRange() == SendRangeEnum.BRAND.getValue()) {
                if (!coupon.getSendRangeData().contains(cartVO.getBrandId())) {
                    continue;
                }
            } else if (coupon.getSendRange() == SendRangeEnum.PRODUCT.getValue()) {
                if (!coupon.getSendRangeData().contains(cartVO.getProductId())) {
                    continue;
                }
            } else if (coupon.getSendRange() == SendRangeEnum.NOT_PRODUCT.getValue()) {
                if (coupon.getSendRangeData().contains(cartVO.getProductId())) {
                    continue;
                }
            }

            quantityCount += cartVO.getQuantity();
            BigDecimal price = cartVO.getSku() != null && cartVO.getSku().getSkuPrice() != null ? new BigDecimal(cartVO.getSku().getSkuPrice()) : cartVO.getProductPrice();
            BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(cartVO.getQuantity()));
            checkedProductPriceSum = checkedProductPriceSum.add(totalPrice);
        }

        BigDecimal discountMoney;

        if (coupon.getCouponType() == 1) {
            discountMoney = coupon.getCouponMoney();
        } else {
            BigDecimal discount = coupon.getCouponDiscount().divide(BigDecimal.valueOf(10), 2, RoundingMode.HALF_UP);
            discount = BigDecimal.valueOf(1).subtract(discount);
            BigDecimal discountRate = checkedProductPriceSum.multiply(discount);
            discountMoney = checkedProductPriceSum.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal finalCheckedProductPriceSum = checkedProductPriceSum;
        Integer finalQuantityCount = quantityCount;
        return CartCouponDiscountVO.builder().minOrderAmount(coupon.getReduceType() == 2 ? BigDecimal.ZERO : coupon.getMinOrderAmount()).couponMoney(coupon.getCouponMoney()).couponUnit(coupon.getCouponUnit()).productPrice(finalCheckedProductPriceSum).quantityCount(finalQuantityCount).discountMoney(discountMoney).build();
    }

    @Override
    public CartListByStoreVO getCartList(ListDTO listDTO, Integer currentUserId) {
        CartListByStoreVO getCartListByStoreVO = getCartListByStore(listDTO.getIsChecked() != null ? listDTO.getIsChecked() : false, CartTypeEnum.TYPE_NORMAL.getCode(), null, listDTO.getShopId(), currentUserId, listDTO.getExcludeQuickProduct());
        return buildCartPromotion(getCartListByStoreVO, listDTO, currentUserId, 1, 1, null);
    }

    @Override
    public CartListByStoreVO getCartList(ListDTO listDTO, Integer currentUserId, Integer cartType) {
        CartListByStoreVO getCartListByStoreVO = getCartListByStore(listDTO.getIsChecked() != null ? listDTO.getIsChecked() : false, cartType, null, listDTO.getShopId(), currentUserId, listDTO.getExcludeQuickProduct());
        return buildCartPromotion(getCartListByStoreVO, listDTO, currentUserId, 1, 1, null);
    }

    private boolean allowCartPromotion(Integer flowType){
//        boolean isNormalFlow = flowType != null && flowType == 1;
//        if (!isNormalFlow && flowType != CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode()) {
//           return false;
//        }
        return true;
    }

    @Override
    public CartListByStoreVO buildCartPromotion(CartListByStoreVO cartListByStore, BaseChangePriceParam changePriceParam, Integer userId, Integer flowType, Integer useDefaultCoupon, List<Integer> useCouponIds) {
        userId = Objects.isNull(userId) ? getCurrentUserId() : userId;
        // 周期购商品使用普通商品的 flowType，通过 cycleSpecId 判断是否为周期购
        // flowType=1 为普通商品
        if(!allowCartPromotion(flowType)){
            return getCartPriceByAttrPrice(cartListByStore);
        }

        // 初始化总优惠金额
        CartListByStoreVO.CartTotal cartTotal = cartListByStore.getTotal();
        cartTotal.setDiscountSeckillAmount(BigDecimal.ZERO);
        cartTotal.setDiscountProductPromotionAmount(BigDecimal.ZERO);
        cartTotal.setDiscountCouponAmount(BigDecimal.ZERO);
        cartTotal.setDiscountTimeDiscountAmount(BigDecimal.ZERO);
        cartTotal.setDiscountDiscountAmount(BigDecimal.ZERO);
        cartTotal.setDiscountGroupBuyAmount(BigDecimal.ZERO);
        cartTotal.setRoundOffCentsAmount(BigDecimal.ZERO);
        cartTotal.setRoundOffDimeAmount(BigDecimal.ZERO);

        List<CartByStoreVO> newCartListByStore = new ArrayList<>();

        for (CartByStoreVO cartByStore : cartListByStore.getCartList()) {
            if (cartByStore.getCarts() == null) {
                continue;
            }

            // 给购物车 shopId 为 0 的重新设置 shopTitle

            List<ProductSaveParam> products = cartByStore.getCarts().stream().filter(CartVO::getIsChecked)
                    // CR-20260612-1520：批发作用域行（命中阶梯价 + 未命中按原始零售价兜底的行）统一退出
                    // C 端营销（促销/满减/券），判定从 isWholesaleLine 收口到 wholesaleScope。
                    .filter(c -> !Boolean.TRUE.equals(c.getWholesaleScope()))
                    .map(cart -> {
                ProductSaveParam product = new ProductSaveParam();
                product.setProductId(cart.getProductId());
                product.setSkuId(cart.getSkuId());
                product.setCartId(cart.getCartId());
                product.setIsChecked(cart.getIsChecked());
                return product;
            }).toList();

            // 获取购物车每个活动对应的购物车商品
            Map<Integer, List<CartVO>> promotionIdToCarts = new HashMap<>();
            // 获取购物车每个活动对应的活动信息
            Map<Integer, PromotionVO> promotionIdToPromotion = new HashMap<>();
            List<CartVO> carts = cartByStore.getCarts();

            Map<Integer, ProductSaveParam> promotions = productService.getProductsPromotion(products, cartByStore.getShopId(), "cart", userId);

            for (Integer cartId : promotions.keySet()) {
                List<PromotionVO> activities = promotions.get(cartId).getActivityInfo();
                if (CollUtil.isEmpty(activities)) {
                    continue;
                }

                CartVO thisCartVO = carts.stream().filter(cart -> cart.getCartId().equals(cartId)).findFirst().orElse(null);
                if (thisCartVO == null) {
                    continue;
                }

                for (PromotionVO promotion : activities) {
                    promotionIdToPromotion.put(promotion.getPromotionId(), promotion);
                    promotionIdToCarts.computeIfAbsent(promotion.getPromotionId(), k -> new ArrayList<>());

                    // 检查是否已存在该商品，避免重复添加
                    List<CartVO> existingCarts = promotionIdToCarts.get(promotion.getPromotionId());
                    boolean alreadyExists = existingCarts.stream()
                            .anyMatch(cart -> cart.getCartId().equals(thisCartVO.getCartId()));

                    if (!alreadyExists) {
                        CartVO newCart = BeanUtil.copyProperties(thisCartVO, CartVO.class);
                        promotionIdToCarts.get(promotion.getPromotionId()).add(newCart);
                    }
                }
            }

            //如果使用推荐默认优惠券，先计算出最大的优惠券
            Map<Integer, PromotionVO> maxCouponPromotion = new HashMap<>();
            for (Integer promotionId : promotionIdToCarts.keySet()) {
                PromotionVO promotion = promotionIdToPromotion.get(promotionId);
                if (promotion.getType() != 2) {
                    continue;
                }
                UserCoupon userCoupon = userCouponService.getUserCouponByCouponId(userId, promotionIdToPromotion.get(promotionId).getRelationId());
                promotion.setIsReceive(userCoupon == null ? 0 : 1);

                if (useDefaultCoupon != null && useDefaultCoupon == 1 && userCoupon != null) {
                    // 创建购物车副本用于计算最优优惠券，避免修改原始数据
                    List<CartVO> cartListCopy = promotionIdToCarts.get(promotionId).stream()
                            .map(cart -> BeanUtil.copyProperties(cart, CartVO.class))
                            .collect(Collectors.toList());
                    CartPromotionParsePriceDTO dto = couponService.parsePrice(cartListCopy, promotion);
                    if (dto != null) {
                        PromotionVO dtoPromotion = dto.getPromotion();
                        if (maxCouponPromotion.get(0) == null || maxCouponPromotion.get(0).getAmount().compareTo(dtoPromotion.getAmount()) < 0) {
                            maxCouponPromotion.put(0, dtoPromotion);
                        }
                    }
                }

            }
            if (maxCouponPromotion.get(0) != null) {
                if (useCouponIds == null) {
                    useCouponIds = new ArrayList<>();
                }
                useCouponIds.add(maxCouponPromotion.get(0).getRelationId());
            }

            // 优惠计算
            List<PromotionVO> enableUsePromotion = new ArrayList<>();
            // 已使用的营销
            List<PromotionVO> usedPromotion = new ArrayList<>();
            // 已使用的营销ID集合（用于去重）
            Set<Integer> usedPromotionIds = new HashSet<>();

            //处理所有购物车列表选择的商品优惠计算
            for (Integer cartId : promotions.keySet()) {
                ProductSaveParam product = promotions.get(cartId);
                if (!product.getIsChecked()) {
                    product.setActivityInfo(List.of());
                    continue;
                }
                List<PromotionVO> activities = product.getActivityInfo();
                if (CollUtil.isEmpty(activities)) {
                    continue;
                }

                // 过滤未领取，已删除的优惠券
                activities.removeIf(promotion -> promotion.getType() == 2 && (promotion.getIsReceive() == null || promotion.getIsReceive() == 0) && promotion.getIsDelete() == 1);

                for (PromotionVO promotion : activities) {
                    // 使用promotionId判断是否已处理，避免同一促销被多次处理
                    if (promotion == null || usedPromotionIds.contains(promotion.getPromotionId())) {
                        continue;
                    }

                    PromotionType promotionType = PromotionType.getStatus(promotion.getType());
                    CartPromotionParsePriceDTO dto = switch (promotionType) {
                        case TYPE_SECKILL ->
                                seckillService.parsePrice(promotionIdToCarts.get(promotion.getPromotionId()), promotion);
                        case TYPE_COUPON -> {
                            ConfigPO config = configService.getConfigByCode(USE_COUPON.getBizCode());
                            if (config.getBizVal() != null && "1".equals(config.getBizVal())) {
                                yield couponService.parsePrice(promotionIdToCarts.get(promotion.getPromotionId()), promotion);
                            }
                            yield null;
                        }
                        case TYPE_REDUCE, TYPE_DISCOUNT, TYPE_GIVE ->
                                productPromotionService.parsePrice(promotionIdToCarts.get(promotion.getPromotionId()), promotion);
                        case TYPE_LIMIT_DISCOUNT ->
                                timeDiscountService.parsePrice(promotionIdToCarts.get(promotion.getPromotionId()), promotion);
                        case TYPE_GROUPON ->
                                grouponPromotionService.parsePrice(promotionIdToCarts.get(promotion.getPromotionId()), promotion);
                        default -> null;
                    };

                    if (promotion.getType() == 1) {
                        carts.forEach(cart -> {
                            if (cart.getCartId().equals(cartId)) {
                                cart.setIsSeckill(1);
                            }
                        });
                    } else if (promotion.getType() == 2 && dto != null) {
                        enableUsePromotion.add(dto.getPromotion());
                        if (useCouponIds == null || useCouponIds.isEmpty() || !useCouponIds.contains(promotion.getRelationId())) {
                            continue;
                        }
                    }

                    if (dto == null) {
                        continue;
                    }

                    usedPromotion.add(dto.getPromotion());
                    usedPromotionIds.add(promotion.getPromotionId()); // 必须添加，否则去重逻辑失效

                    if (dto.getGift() != null) {
                        if (CollUtil.isEmpty(cartByStore.getGift())) {
                            cartByStore.setGift(new ArrayList<>());
                        }
                        cartByStore.getGift().add(dto.getGift());
                    }

                    Map<String, Object> hasDiscountCartId = new HashMap<>();
                    if (dto.getCartList() != null) {
                        for (CartVO cartVO : dto.getCartList()) {
                            String key = cartVO.getCartId() + "_" + promotion.getPromotionId();
                            if (hasDiscountCartId.containsKey(key)) {
                                continue;
                            }
                            hasDiscountCartId.put(key, true);
                            carts.forEach(cart -> {
                                if (cart.getCartId().equals(cartVO.getCartId())) {
                                    cart.setPrice(cartVO.getPrice());
                                    cart.setSubtotal(cart.getPrice().multiply(new BigDecimal(cartVO.getQuantity())));
                                    cart.setActivityInfo(product.getActivityInfo());
                                }
                            });
                            //将promotionIdToCarts里该存在该cartId数据的price设置为cartVO的price不限制promotionId
                            promotionIdToCarts.forEach((promotionId, cartList) -> {
                                cartList.forEach(cart -> {
                                    if (cart.getCartId().equals(cartVO.getCartId())) {
                                        cart.setPrice(cartVO.getPrice());
                                        cart.setSubtotal(cart.getPrice().multiply(new BigDecimal(cartVO.getQuantity())));
                                    }
                                });
                            });

                        }
                    }
                }
            }

            // 计算店铺优惠总额
            cartByStore.setEnableUsePromotion(enableUsePromotion);
            cartByStore.setUsedPromotions(usedPromotion);
            cartByStore.setCarts(carts);

            CartByStoreVO.CartStoreTotal storeTotal = cartByStore.getTotal();
            storeTotal.setDiscountCouponAmount(BigDecimal.ZERO);
            storeTotal.setDiscountSeckillAmount(BigDecimal.ZERO);
            storeTotal.setDiscountTimeDiscountAmount(BigDecimal.ZERO);
            storeTotal.setDiscountProductPromotionAmount(BigDecimal.ZERO);
            storeTotal.setDiscountDiscountAmount(BigDecimal.ZERO);
            storeTotal.setDiscounts(BigDecimal.ZERO);
            storeTotal.setDiscountGroupBuyAmount(BigDecimal.ZERO);

            for (PromotionVO promotion : usedPromotion) {
                if (promotion.getAmount() == null) {
                    promotion.setAmount(BigDecimal.ZERO);
                }

                switch (promotion.getType()) {
                    case 2:
                        if (CollUtil.isNotEmpty(useCouponIds) && useCouponIds.contains(promotion.getRelationId())) {
                            storeTotal.setDiscountCouponAmount(storeTotal.getDiscountCouponAmount().add(promotion.getAmount()));
                            storeTotal.getCouponIds().add(promotion.getRelationId());
                            cartTotal.setDiscountCouponAmount(cartTotal.getDiscountCouponAmount().add(promotion.getAmount()));
                        } else {
                            promotion.setAmount(BigDecimal.ZERO);
                        }
                        break;
                    case 1:
                        storeTotal.setDiscountSeckillAmount(storeTotal.getDiscountSeckillAmount().add(promotion.getAmount()));
                        cartTotal.setDiscountSeckillAmount(cartTotal.getDiscountSeckillAmount().add(promotion.getAmount()));
                        break;
                    case 6:
                        storeTotal.setDiscountTimeDiscountAmount(storeTotal.getDiscountTimeDiscountAmount().add(promotion.getAmount()));
                        cartTotal.setDiscountTimeDiscountAmount(cartTotal.getDiscountTimeDiscountAmount().add(promotion.getAmount()));
                        break;
                    case 7:
                        storeTotal.setDiscountGroupBuyAmount(storeTotal.getDiscountGroupBuyAmount().add(promotion.getAmount()));
                        cartTotal.setDiscountGroupBuyAmount(cartTotal.getDiscountGroupBuyAmount().add(promotion.getAmount()));
                        break;
                    default:
                        storeTotal.setDiscountProductPromotionAmount(storeTotal.getDiscountProductPromotionAmount().add(promotion.getAmount()));
                        cartTotal.setDiscountProductPromotionAmount(cartTotal.getDiscountProductPromotionAmount().add(promotion.getAmount()));
                }

                // 优惠券独立计算
                if (promotion.getType() != 2) {
                    storeTotal.setDiscountProductPromotionAmount(storeTotal.getDiscountProductPromotionAmount().add(promotion.getAmount()));
                    storeTotal.setDiscountDiscountAmount(storeTotal.getDiscountDiscountAmount().add(promotion.getAmount()));
                    cartTotal.setDiscountDiscountAmount(cartTotal.getDiscountDiscountAmount().add(promotion.getAmount()));
                }

                cartTotal.setDiscounts(cartTotal.getDiscounts().add(promotion.getAmount()));
            }

            cartByStore.setTotal(storeTotal);
            newCartListByStore.add(cartByStore);
        }

        // 汇总总额
        BigDecimal discountAfter = cartTotal.getProductAmount().subtract(cartTotal.getDiscounts());
        cartTotal.setDiscountAfter(discountAfter.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : discountAfter);
        if (cartTotal.getDiscountCouponAmount().compareTo(cartTotal.getProductAmount()) > 0) {
            cartTotal.setDiscountCouponAmount(cartTotal.getProductAmount());
        }
        if (cartTotal.getDiscounts().compareTo(cartTotal.getProductAmount()) > 0) {
            cartTotal.setDiscounts(cartTotal.getProductAmount());
        }

        cartTotal.twoDecimalPlaces();
        cartListByStore.setCartList(newCartListByStore);
        cartListByStore.setTotal(cartTotal);

        CartListByStoreVO vo = getCartPriceByAttrPrice(cartListByStore);

        //改价
        return changePrice(vo, changePriceParam, userId);
    }


    /**
     * 改价
     *
     * @param vo
     * @param checkParam
     * @return
     */
    private CartListByStoreVO changePrice(CartListByStoreVO vo, @Nullable BaseChangePriceParam checkParam, @NotNull Integer userId) {
        if (Objects.isNull(checkParam) || Objects.isNull(vo)) {
            return vo;
        }

        boolean isPosAdmin = PosCheckoutContext.isPosCheckout(checkParam);
        boolean isProxyShopping = checkParam instanceof OrderCheckParam ocp
                && CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode().equals(ocp.getFlowType());
        if (!isPosAdmin && !isProxyShopping) {
            return vo;
        }

        if (Boolean.TRUE.equals(checkParam.getIsCartList())) {
            //查询之前的做存储的改价数据，根据db改价数据进行改价
            vo = changePriceByMe(vo, checkParam, userId);
        } else {
            //根据前端数据进行改价
            vo = changePriceByChangeParam(vo, checkParam, userId);
        }

        vo.getTotal().twoDecimalPlaces();


//        Boolean hasChangePrice = vo.getHasChangePrice();
//        if (Boolean.TRUE.equals(hasChangePrice)) {
//
//            //改价的优惠金额(商品金额总金额-改价后的总金额)
//            CartListByStoreVO.CartTotal total = vo.getTotal();
//            BigDecimal discountChangePrice = total.getDiscountChangePrice();
//
//            //优惠金额(排除了改价的情况下)
//            BigDecimal discounts = Optional.ofNullable(total.getDiscounts()).orElse(BigDecimal.ZERO);
//            BigDecimal subtract = discountChangePrice.subtract(discounts);
//            if (subtract.compareTo(BigDecimal.ZERO) > 0) {
//                // 其他优惠的金额
////                由于改价优惠占时不在详情里面展示,进入到其他优惠金额里面
//                BigDecimal discountDiscountAmount = Optional.ofNullable(total.getDiscountDiscountAmount()).orElse(BigDecimal.ZERO);
//                total.setDiscountDiscountAmount(discountDiscountAmount.add(subtract));
//
//                //扣减其他优惠的 改价优惠是 (总优惠(排除改价的优惠)-改价优惠)
//                total.setDiscountChangePrice(subtract);
//            }
//
//            total.twoDecimalPlaces();
//        }
        return vo;
    }


    /**
     *
     * 通过改价参数进行改价
     *
     * @param vo
     * @param checkParam
     * @param userId
     */
    private CartListByStoreVO changePriceByChangeParam(CartListByStoreVO vo, @NotNull BaseChangePriceParam checkParam, Integer userId) {
        Integer totalChangePriceType = checkParam.getTotalChangePriceType();
        BigDecimal changePriceValue = checkParam.getChangePriceValue();
        if (Objects.nonNull(changePriceValue)) {
            //全局改价
            CartGlobalChangePrice cartGlobalChangePrice;
            if (!Boolean.TRUE.equals(checkParam.getOnlyShow())) {
                //同步到数据库
                cartGlobalChangePrice = cartGlobalChangePriceService.getById(userId);
                //不只是展示(进行存储)
                List<Cart> updCarts = new ArrayList<>();
                List<CartByStoreVO> cartList = vo.getCartList();
                for (CartByStoreVO cartByStoreVO : cartList) {
                    List<Cart> list = cartByStoreVO.getCarts().stream().map((cartVO -> {
                        return Cart.builder().cartId(cartVO.getCartId()).changePriceData("{}").build();
                    })).toList();
                    updCarts.addAll(list);
                }
                if (!updCarts.isEmpty()) {
                    this.updateBatchById(updCarts);
                }
                if (Objects.isNull(cartGlobalChangePrice)) {
                    //说明数据库是不存在的,就进行插入
                    cartGlobalChangePrice = new CartGlobalChangePrice();
                    cartGlobalChangePrice.setUserId(userId);
                    cartGlobalChangePrice.setChangePriceType(totalChangePriceType);
                    cartGlobalChangePrice.setChangePriceValue(changePriceValue);
                    cartGlobalChangePriceService.save(cartGlobalChangePrice);
                } else {
                    cartGlobalChangePrice.setUserId(userId);
                    cartGlobalChangePrice.setChangePriceType(totalChangePriceType);
                    cartGlobalChangePrice.setChangePriceValue(changePriceValue);
                    cartGlobalChangePriceService.updateById(cartGlobalChangePrice);
                }
            } else {
                //临时查看
                cartGlobalChangePrice = new CartGlobalChangePrice().builder().changePriceValue(changePriceValue).changePriceType(totalChangePriceType).build();
            }

            vo.setCartGlobalChangePrice(cartGlobalChangePrice);
            globalChangePrice(vo, checkParam, cartGlobalChangePrice);
            vo.setHasChangePrice(true);
            checkParam.setUseChangePrice(true);
        } else {
            //局部改价
            vo.setCartGlobalChangePrice(null);
            List<OrderCheckParam.CartChangePrice> cartChangePrices = Optional.ofNullable(checkParam.getCartChangePrices()).orElse(Collections.emptyList());
            final Map<Integer, OrderCheckParam.CartChangePrice> cartIdMap = cartChangePrices.stream().filter(Objects::nonNull).collect(Collectors.toMap(OrderCheckParam.CartChangePrice::getCartId, Function.identity()));

            BigDecimal lastTotalChangePrice = BigDecimal.ZERO;
            List<CartByStoreVO> cartList = vo.getCartList();
            List<Cart> updCarts = new ArrayList<>();
            boolean isChangePrice = false;
            for (CartByStoreVO cartByStoreVO : cartList) {
                final List<CartVO> carts = cartByStoreVO.getCarts();
                if (Objects.isNull(carts) || carts.isEmpty()) {
                    continue;
                }
                for (CartVO cartVO : carts) {
                    if (cartVO.getIsChecked()) {
                        //选中才能去改价
                        OrderCheckParam.CartChangePrice changePriceParam = cartIdMap.get(cartVO.getCartId());
                        Cart cart = new Cart();
                        cart.setCartId(cartVO.getCartId());
                        if (Objects.isNull(changePriceParam)) {
                            //前端没有传入,就进行取消
                            OrderCheckParam.CartChangePrice dbChangePrice = cartVO.getCartChangePrice();
                            if (Objects.nonNull(dbChangePrice)) {
                                //数据库有改价,然后前端为未传入表示为取消改价
                                cart.setChangePriceData("{}");
                                updCarts.add(cart);
                            }
                        } else {
                            //前端有传入(就用传入的,db也置为传入的值)
                            cart.setChangePriceData(JSONUtil.toJsonStr(changePriceParam));
                            cartVO.changePrice(changePriceParam);
                            updCarts.add(cart);
                            cartVO.setCartChangePrice(changePriceParam);
                            isChangePrice = true;
                        }
                        lastTotalChangePrice = lastTotalChangePrice.add(cartVO.getSubtotal());
                    }
                }
            }

            if (!Boolean.TRUE.equals(checkParam.getOnlyShow())) {
                if (!updCarts.isEmpty()) {
                    this.updateBatchById(updCarts);
                }
                CartGlobalChangePrice cartGlobalChangePrice = cartGlobalChangePriceService.getById(userId);
                if (Objects.nonNull(cartGlobalChangePrice)) {
                    cartGlobalChangePriceService.removeById(userId);
                }
            }


            //重新计算total
            if (isChangePrice) {
                vo.setHasChangePrice(true);
                checkParam.setUseChangePrice(true);
                CartListByStoreVO.CartTotal total = vo.getTotal();
                total.changePrice(lastTotalChangePrice);
            }
        }
        return vo;
    }

    /**
     * 通过自身的参数进行改价(已存入到购物车的数据)
     *
     * @param vo
     * @param checkParam
     * @param userId
     * @return
     */
    @NotNull
    private CartListByStoreVO changePriceByMe(CartListByStoreVO vo, @NotNull BaseChangePriceParam checkParam, Integer userId) {
        CartGlobalChangePrice cartGlobalChangePrice = cartGlobalChangePriceService.getById(userId);
        //是购物车列表,只返回
        if (Objects.nonNull(cartGlobalChangePrice)) {
            //整体改价的元数据
            vo.setCartGlobalChangePrice(cartGlobalChangePrice);
            //然后进行整体改价
            globalChangePrice(vo, checkParam, cartGlobalChangePrice);
            vo.setHasChangePrice(true);
            checkParam.setUseChangePrice(true);
        } else {
            //可能存在局部改价
            List<CartByStoreVO> cartList = vo.getCartList();
            BigDecimal lastTotalChangePrice = BigDecimal.ZERO;
            boolean hasChangePrice = false;
            for (CartByStoreVO cartByStoreVO : cartList) {
                final List<CartVO> carts = cartByStoreVO.getCarts();
                if (Objects.nonNull(carts)) {
                    for (CartVO cartVO : carts) {
                        if (cartVO.getIsChecked()) {
                            //选中才能去改价
                            OrderCheckParam.CartChangePrice cartChangePrice = cartVO.getCartChangePrice();
                            cartVO.changePrice(cartChangePrice);
                            hasChangePrice = hasChangePrice || Objects.nonNull(cartChangePrice);
                            lastTotalChangePrice = lastTotalChangePrice.add(cartVO.getSubtotal());
                        }
                    }
                }
            }
            if (hasChangePrice) {
                //有改价(清除优惠卷)
                CartListByStoreVO.CartTotal total = vo.getTotal();
                total.changePrice(lastTotalChangePrice);
                vo.setHasChangePrice(true);
                checkParam.setUseChangePrice(true);
            }
        }
        return vo;
    }

    private void globalChangePrice(CartListByStoreVO vo, @NotNull BaseChangePriceParam checkParam, @NotNull CartGlobalChangePrice cartGlobalChangePrice) {
        BigDecimal changePriceValue = cartGlobalChangePrice.getChangePriceValue();
        Integer totalChangePriceType = cartGlobalChangePrice.getChangePriceType();
        final CartListByStoreVO.CartTotal total = vo.getTotal();
        final BigDecimal productAmount = total.getProductAmount();
        final BigDecimal originalPrice = checkParam.getOriginalPrice() == null ? total.getProductAmount() : checkParam.getOriginalPrice();
        boolean isProxyShopping = checkParam instanceof OrderCheckParam orderCheckParam
                && CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode().equals(orderCheckParam.getFlowType());
        if (!isProxyShopping && productAmount.setScale(2, RoundingMode.HALF_UP).compareTo(originalPrice.setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new GlobalException(messageHelper.get("error.cart.data.changed"));
        }
        BigDecimal changePriceBase = isProxyShopping ? productAmount : originalPrice;
        BigDecimal totalChangePrice = CartVO.calculateChangePrice(changePriceValue, totalChangePriceType, changePriceBase);
        if (totalChangePrice.compareTo(BigDecimal.ZERO) <= 0) {
            totalChangePrice = BigDecimal.ZERO;
        }
        total.changePrice(totalChangePrice);

        // 将改价优惠按商品金额占比分配到各店铺，以便前端展示
        BigDecimal changePriceDiscount = total.getDiscountChangePrice();
        if (changePriceDiscount != null && changePriceDiscount.compareTo(BigDecimal.ZERO) != 0
                && productAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal distributed = BigDecimal.ZERO;
            List<CartByStoreVO> storeList = vo.getCartList();
            for (int i = 0; i < storeList.size(); i++) {
                CartByStoreVO store = storeList.get(i);
                BigDecimal storeAmount = store.getCarts().stream()
                        .filter(CartVO::getIsChecked)
                        .map(CartVO::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal storeDiscount;
                if (i == storeList.size() - 1) {
                    storeDiscount = changePriceDiscount.subtract(distributed);
                } else {
                    storeDiscount = changePriceDiscount.multiply(storeAmount)
                            .divide(productAmount, 2, RoundingMode.HALF_UP);
                    distributed = distributed.add(storeDiscount);
                }
                CartByStoreVO.CartStoreTotal storeTotal = store.getTotal();
                storeTotal.setDiscountChangePrice(storeDiscount);
                storeTotal.setDiscounts(storeTotal.getDiscounts().add(storeDiscount));
                storeTotal.setDiscountDiscountAmount(
                        (storeTotal.getDiscountDiscountAmount() == null ? BigDecimal.ZERO : storeTotal.getDiscountDiscountAmount())
                                .add(storeDiscount));
            }
        }
    }

    /**
     * 处理购物车内商品附加属性价格
     *
     * @param cart 购物车数据
     * @return 处理后的购物车数据
     */
    public CartListByStoreVO getCartPriceByAttrPrice(CartListByStoreVO cart) {
        // 设置服务费为0
        cart.getTotal().setServiceFee(BigDecimal.ZERO);

        // 遍历购物车每个商店
        for (CartByStoreVO storeCart : cart.getCartList()) {
            if (storeCart.getCarts() == null || storeCart.getCarts().isEmpty()) {
                continue;
            }

            // 遍历每个商品
            for (int i = 0; i < storeCart.getCarts().size(); i++) {
                CartVO item = storeCart.getCarts().get(i);

                BigDecimal totalAttrPrice = BigDecimal.ZERO;
                BigDecimal serviceFee = BigDecimal.ZERO;

                // 获取附加属性数据
                List<CarExtraSkuDataDTO> extraSkuData = item.getExtraSkuData();
                if (extraSkuData != null && !extraSkuData.isEmpty()) {
                    for (CarExtraSkuDataDTO attrItem : extraSkuData) {
                        BigDecimal attrPrice = attrItem.getAttrPrice();
                        BigDecimal quantity = new BigDecimal(item.getQuantity());
                        BigDecimal totalPrice = attrPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
                        attrItem.setTotalAttrPrice(totalPrice);
                    }

                    // 获取附加属性价格
                    for (CarExtraSkuDataDTO attrItem : extraSkuData) {
                        BigDecimal attrPrice = attrItem.getAttrPrice();
                        totalAttrPrice = totalAttrPrice.add(attrPrice).setScale(2, RoundingMode.HALF_UP);
                    }

                    // 乘以商品数量
                    totalAttrPrice = totalAttrPrice.multiply(new BigDecimal(item.getQuantity())).setScale(2, RoundingMode.HALF_UP);

                    // 如果商品被选中，则更新总价和服务费
                    if (item.getIsChecked()) {
                        cart.getTotal().setDiscountAfter(cart.getTotal().getDiscountAfter().add(totalAttrPrice).setScale(2, RoundingMode.HALF_UP));
                        cart.getTotal().setServiceFee(cart.getTotal().getServiceFee().add(totalAttrPrice).setScale(2, RoundingMode.HALF_UP));
                    }

                    storeCart.getCarts().get(i).setServiceFee(serviceFee.add(totalAttrPrice).setScale(2, RoundingMode.HALF_UP).toString());
                }

                // 获取所有附加属性
                List<ProductAttribute> extraSkuAllData = productAttributesService.list(new LambdaQueryWrapper<ProductAttribute>().eq(ProductAttribute::getProductId, item.getProductId()).eq(ProductAttribute::getAttrType, 2));
                List<CarExtraSkuDataDTO> extraSkuAllDataList = CollUtil.isNotEmpty(extraSkuAllData) ? JSONUtil.toList(JSONUtil.toJsonStr(extraSkuAllData), CarExtraSkuDataDTO.class) : null;
                storeCart.getCarts().get(i).setExtraSkuAllData(extraSkuAllDataList);
            }
        }
        return cart;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean buyAgain(Integer id, Integer userId) {
        Order order = orderService.lambdaQuery()
                .eq(Order::getOrderId, id)
                .eq(Order::getUserId, userId)
                .one();
        if (order == null) {
            throw new GlobalException(ORDER_NOT_EXIST);
        }
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getOrderId, order.getOrderId());
        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);
        if (orderItems.isEmpty()) {
            throw new GlobalException(ORDER_NOT_EXIST);
        }
        //将购物车中所有状态改为不选中的
        LambdaQueryWrapper<Cart> cartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        cartLambdaQueryWrapper.eq(Cart::getUserId, userId);
        // 构建更新条件
        Cart updateCart = new Cart();
        updateCart.setIsChecked(0);
        this.update(updateCart, cartLambdaQueryWrapper);
        //循环orderItems，然后将orderItem中的数据插入到购物车中
        orderItems.forEach(orderItem -> {
            AddToCartDTO addToCartDTO = new AddToCartDTO();
            addToCartDTO.setId(orderItem.getProductId());
            addToCartDTO.setNumber(orderItem.getQuantity());
            addToCartDTO.setSkuId(orderItem.getSkuId());
            addToCartDTO.setType(orderItem.getProductType());
            addToCartDTO.setShopId(orderItem.getShopId());
            //添加分销员id
            //addToCartDTO.setSalesmanId();
            addToCartDTO.setIsQuick(false);
            if (orderItem.getExtraSkuData() != null && !"[]".equals(orderItem.getExtraSkuData())) {
                JSONArray extraSkuData = JsonUtil.fromJson(orderItem.getExtraSkuData(), JSONArray.class);
                List<Integer> extraAttrIds = new ArrayList<>();
                if (extraSkuData != null) {
                    for (Object obj : extraSkuData) {
                        if (obj instanceof JSONObject jsonObject) {
                            Integer attributesId = jsonObject.getInteger("attributes_id");
                            if (attributesId != null) {
                                extraAttrIds.add(attributesId);
                            }
                        }
                    }
                }
                //将放入extraAttrIds中转为字符串
                if (!extraAttrIds.isEmpty()) {
                    String extraAttrIdsStr = extraAttrIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                    addToCartDTO.setExtraAttrIds(extraAttrIdsStr);
                }
            }

            // 无论是否存在附加属性，订单商品都必须进入购物车；加购校验失败时事务整体回滚。
            this.addProductToCart(userId, addToCartDTO);

        });
        return true;
    }

    @Transactional
    @Override
    public Boolean addProductToCart(Integer userId, AddToCartDTO dto) {
        // 校验实名认证功能
        String isIdentity = configService.getConfigVal(SettingsEnum.IS_IDENTITY);
        if ("1".equals(isIdentity) && tigshopProperties.getIsB2b() == 1) {
            User user = userService.getById(getCurrentUserId());
            if (user != null && user.getIsCompanyAuth() == 0) {
                throw new GlobalException("请先完成实名认证");
            }
        }
        // 初始化数据
        dto.initParam();

        // 清理该用户残留的快速购买临时行，避免干扰后续 matchedCarts 查询
        this.remove(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .like(Cart::getExtraData, "\"isQuick\":true"));

        List<AddToCartDTO.SkuItem> skuItems = dto.getSkuItem();
        if (CollUtil.isEmpty(skuItems)) {
            skuItems = new ArrayList<>();
            int skuId = dto.getSkuId() != null ? dto.getSkuId() : 0;
            dto.setSkuId(skuId);
            Integer number = dto.getNumber() == null ? 1 : dto.getNumber();
            skuItems.add(new AddToCartDTO.SkuItem(dto.getId(), skuId, number));
        }

        ProductDetailDTO product = productService.detail(dto.getId());
        if (CollUtil.isNotEmpty(product.getProductList()) && product.getProductList().size() == 1) {
            Integer resolvedSkuId = product.getProductList().get(0).getSkuId();
            if (resolvedSkuId != null && (dto.getSkuId() == null || dto.getSkuId() == 0)) {
                dto.setSkuId(resolvedSkuId);
                skuItems.forEach(item -> {
                    if (item.getSkuId() == null || item.getSkuId() == 0) {
                        item.setSkuId(resolvedSkuId);
                    }
                });
            }
        }

        // SPU 兜底归一化：单规格且无 SKU 行（product_sku 无记录）的商品无法解析真实 skuId，
        // 此时 skuId 统一归一为 0（SPU 兜底，与批发 sku_id=0 口径一致），避免 dto.skuId=null 与
        // skuItem.skuId=0 不一致导致下方按 skuId 匹配设置数量失败、cart.quantity 为 null 引发 NPE。
        // 多规格不受影响：其 skuItem.skuId 非空，不会被填 0，cart.skuId 仍由真实 SKU 覆盖。
        if (dto.getSkuId() == null) {
            dto.setSkuId(0);
        }
        skuItems.forEach(item -> {
            if (item.getSkuId() == null) {
                item.setSkuId(0);
            }
        });
        // 分配商品，校验是否分配，未分配不需添加


        // 如果为已经购买的付费商品则报错
        if (product.getProductType().equals(PRODUCT_TYPE_PAID.getCode())) {
            Integer buy = productService.isBuy(userId, product.getProductId());
            if (buy == 1) {
                throw new GlobalException("您已购买该付费商品");
            }
        }


        // 抽奖商品检查抽奖记录
        if (dto.getLotteryRecordId() != null) {
            LotteryRecord lotteryRecord = lotteryRecordService.getById(dto.getLotteryRecordId());
            if (lotteryRecord == null) {
                throw new GlobalException(messageHelper.get("error.lottery.record.not.exist"));
            }
            if (!lotteryRecord.getUserId().equals(userId)) {
                throw new GlobalException(messageHelper.get("error.lottery.no.permission"));
            }
            if (lotteryRecord.getIsWin() != 1) {
                throw new GlobalException(messageHelper.get("error.lottery.not.win"));
            }
            if (lotteryRecord.getPrizeType() != 2) {
                throw new GlobalException(messageHelper.get("error.lottery.prize.not.product"));
            }
            if (lotteryRecord.getStatus() != 0) {
                throw new GlobalException(messageHelper.get("error.lottery.prize.claimed.or.expired"));
            }
        }

        // 检测店铺状态
        orderOnboardingGuard.assertShopCanPurchase(product.getShopId());
        boolean shopFlag = product.getShopId() > 0 && !shopService.checkShopStatus(product.getShopId(), product.getProductName());
        Assert.isFalse(shopFlag, () -> new GlobalException(messageHelper.get("error.shop.closed")));

        // 检测商品状态
        Assert.isFalse(product.getProductStatus() == ProductStatus.OFF_SALE.getCode(), () -> new GlobalException(messageHelper.get("error.product.off.sale")));

        // 商品上下架优先于 SKU 状态，避免商品已下架且 SKU 同时禁用时返回误导性错误。
        skuItems.forEach(item -> productSkuService.checkSkuEnabled(item.getProductId(), item.getSkuId()));

        // 二期(D1096)：加购人群强校验，与下单链路 OrderCheckServiceImpl 同构，覆盖列表/推荐/详情等所有加购入口。
        // 秒杀：复用现成方法，无进行中活动或 member_scope 为空时自动放行，存量行为不变。
        if (!seckillService.canUserJoinSeckill(dto.getId(), dto.getSkuId(), userId)) {
            throw new GlobalException(translatePackage.translate("该商品仅限指定会员购买"));
        }
        // 拼团：仅当本次加购确实要参与拼团（携带 grouponData）时才校验人群。
        // 非人群用户「单独购买」按普通价加购放行，拼团价由 GrouponPromotionServiceImpl.parsePrice 在无 grouponData 时跳过，不会被套用。
        AddToCartDTO.GrouponData addGrouponData = dto.getGrouponData();
        if (addGrouponData != null && addGrouponData.getGrouponPromotionId() != null
                && !grouponPromotionService.canUserJoinGroupon(addGrouponData.getGrouponPromotionId().intValue(), userId)) {
            throw new GlobalException(translatePackage.translate("您不在该拼团活动的参与范围内"));
        }

        // 检查商品数量
        boolean skuNumFlag = skuItems.stream().anyMatch(skuItem -> skuItem.getNum() == null || skuItem.getNum() <= 0);
        Assert.isFalse(skuNumFlag, () -> new GlobalException(messageHelper.get("error.product.quantity.invalid")));

        // 卡券商品检查卡券库存
        if (dto.getType().equals(CartTypeEnum.TYPE_CARD.getCode())) {
            Long cardCount = eCardMapper.selectCount(new LambdaQueryWrapper<ECard>().eq(ECard::getGroupId, product.getCardGroupId()).eq(ECard::getIsUse, 0));
            if (cardCount < dto.getNumber()) {
                throw new GlobalException(messageHelper.get("error.product.card.stock.insufficient"));
            }
        } else if (dto.getType().equals(CartTypeEnum.TYPE_GIFT_CARD.getCode())) {
            GiftCardGroups giftCardGroups = giftCardGroupsMapper.selectById(product.getGiftCardGroupsId());
            //礼品卡库存有两个地方设置 礼品卡可用库存和 礼品卡商品设置的库存，取交集 其中一个库存小于购买的库存都需要报错
            if (giftCardGroups.getAvailabilityStock() < dto.getNumber() || product.getProductStock() < dto.getNumber()) {
                throw new GlobalException(messageHelper.get("error.gift.card.stock.insufficient"));
            }
            if (Objects.equals(giftCardGroups.getValidityType(), ValidityTypeEnum.FIXED.getCode())) {
                if (giftCardGroups.getExpireTime() != null && giftCardGroups.getExpireTime() > 0 && giftCardGroups.getExpireTime() < System.currentTimeMillis() / 1000) {
                    throw new GlobalException(messageHelper.get("error.gift.card.expired", giftCardGroups.getCardGroupName()));
                }
            }
        }

        String adminType = HeaderUtils.getAdminType();
        // 检测是否超过商品购买限制
        Integer currentCartType = adminType != null && dto.getFinalCartType() != null ? dto.getFinalCartType() : dto.getType();
        skuItems.forEach(skuItem -> {
            Cart cart;
            if (skuItem.getSkuId() != null && skuItem.getSkuId() > 0) {
                Integer shopId = dto.getShopId();
                cart = this.lambdaQuery()
                        .eq(Cart::getSkuId, skuItem.getSkuId())
                        .eq(Cart::getUserId, userId)
                        .eq(currentCartType!=null,Cart::getType, currentCartType)
                        .eq(tigshopProperties.getIsO2o() == 1, Cart::getShopId, shopId)
                        .last("limit 1")
                        .one();
            } else {
                Integer shopId = dto.getShopId();
                cart = this.lambdaQuery()
                        .eq(Cart::getProductId, skuItem.getProductId())
                        .eq(Cart::getUserId, userId)
                        .eq(currentCartType!=null,Cart::getType, currentCartType)
                        .eq(Cart::getShopId, shopId)
                        .last("limit 1")
                        .one();
            }

            if (cart != null) {
                if (dto.getIsQuick()) {
                    this.checkOrderProductLimitNumber(cart.getProductId(), userId, skuItem.getNum(), null);
                } else {
                    int changeQuantity = skuItem.getNum() + cart.getQuantity();
                    this.checkProductLimitNumber(cart.getProductId(), userId, changeQuantity, null);
                }
            } else {
                checkProductLimitNumber(dto.getId(), userId, skuItem.getNum(), null);
            }

        });

        // 获取商品规格信息
        List<ProductAvailabilityVO> productAvailabilities = skuItems.stream().map(skuItem -> productService.getProductFinalPrice(skuItem.getProductId(), skuItem.getSkuId(), dto.getSalesmanId(), dto.getExtraAttrIds(), dto.getShopId(), userId)).toList();

        // 商品附加属性
        List<Integer> extraAttrIdList = new ArrayList<>(StringUtils.str2IntList(dto.getExtraAttrIds()));
        List<CarExtraSkuDataDTO> list = new ArrayList<>();
        if (!extraAttrIdList.isEmpty()) {
            List<ProductAttribute> extraSkuData = productAttributesService.getProductAttributesByIds(extraAttrIdList);
            for (ProductAttribute productAttribute : extraSkuData) {
                CarExtraSkuDataDTO carExtraSkuDataDTO = new CarExtraSkuDataDTO();
                carExtraSkuDataDTO.setAttributesId(productAttribute.getAttributesId());
                carExtraSkuDataDTO.setAttrName(productAttribute.getAttrName());
                carExtraSkuDataDTO.setAttrValue(productAttribute.getAttrValue());
                carExtraSkuDataDTO.setAttrPrice(productAttribute.getAttrPrice());
                list.add(carExtraSkuDataDTO);
            }
        }

        // 获取已存在的购物车商品
        List<Integer> skuIds = skuItems.stream().map(AddToCartDTO.SkuItem::getSkuId).toList();
        List<Cart> carts = this.lambdaQuery().eq(Cart::getProductId, dto.getId()).eq(Cart::getUserId, userId)
                .eq(Cart::getType, adminType != null && dto.getFinalCartType() != null ? dto.getFinalCartType() : dto.getType())
                .eq(tigshopProperties.getIsO2o() == 1, Cart::getShopId, dto.getShopId() != null ? dto.getShopId() : product.getShopId())
                .in(Cart::getSkuId, skuIds).list();
        List<Cart> matchedCarts = carts;


        List<Cart> addCarts = new ArrayList<>();
        for (ProductAvailabilityVO productAvailability : productAvailabilities) {
            String picThumb = product.getPicThumb();
            ProductSkuDTO skuDTO = productAvailability.getSku();
            if (skuDTO != null) {
                List<ProductSkuDTO.SkuData> skuData = skuDTO.getSkuData();
                if(CollUtil.isNotEmpty(skuData)){
                    //寻找第一个规格图片
                    Optional<String> first = skuData.stream().map(ProductSkuDTO.SkuData::getAttrPicThumb).filter(StringUtils::isNotBlank).findFirst();
                    picThumb = first.isPresent() ? first.get() : picThumb;
                }
            }

            Cart cart = Cart.builder().userId(userId).productId(dto.getId()).productSn(product.getProductSn()).picThumb(picThumb).marketPrice(product.getMarketPrice()).originalPrice(productAvailability.getPrice()).skuId(dto.getSkuId()).skuData(Optional.ofNullable(productAvailability.getData()).map(JsonUtil::toJson).orElse("[]")).productType(product.getProductType()).isChecked(1).type(dto.getType()).shopId(tigshopProperties.getIsO2o() == 1 && dto.getShopId() != null ? dto.getShopId() : product.getShopId()).updateTime(StringUtils.getCurrentTime()).salesmanId(dto.getSalesmanId()).extraSkuData(CollUtil.isNotEmpty(list) ? JSONUtil.toJsonStr(list) : "").cycleSpecId(dto.getCycleSpecId()).redeemCardId(dto.getRedeemCardId()).build();

            if (dto.getLotteryRecordId() != null) {
                cart.setType(CartTypeEnum.TYPE_LOTTERY_PRODUCT.getCode());
            }

            JSONObject extraData = new JSONObject();
            if (Boolean.TRUE.equals(dto.getIsQuick())) {
                extraData.put("isQuick", true);
            }
            if (dto.getGrouponData() != null) {
                extraData.putAll(JSON.parseObject(JSONUtil.toJsonStr(dto.getGrouponData())));
            }

            if (dto.getGiftCardData() != null) {
                ExchangeCardParam giftCardData = dto.getGiftCardData();
                giftCardData.setUserId(userId);
                extraData.putAll(JSON.parseObject(JSONUtil.toJsonStr(giftCardData)));
            }
            cart.setExtraData(extraData.toJSONString());

            ProductSkuDTO sku = productAvailability.getSku();
            if (sku != null) {
                cart.setOriginalPrice(new BigDecimal(sku.getSkuPrice()));
                cart.setSkuId(sku.getSkuId());
                cart.setSkuData(Optional.ofNullable(sku.getSkuData()).map(JsonUtil::toJson).orElse("[]"));
            }

            Optional<AddToCartDTO.SkuItem> skuItemOptional = skuItems.stream().filter(skuItem -> skuItem.getSkuId().equals(cart.getSkuId())).findFirst();
            skuItemOptional.ifPresent(skuItem -> cart.setQuantity(skuItem.getNum()));

            Optional<Cart> cartOptional = matchedCarts.stream().filter(item -> item.getSkuId().equals(cart.getSkuId()) && (tigshopProperties.getIsO2o() != 1 || item.getShopId().equals(cart.getShopId()))).findFirst();
            if (dto.getIsQuick()) {
                cartOptional.ifPresent(item -> cart.setQuantity(cart.getQuantity()));
            } else {
                cartOptional.ifPresent(item -> cart.setQuantity(cart.getQuantity() + item.getQuantity()));
            }

            addCarts.add(cart);
        }

        // 检查库存（预约类型商品使用日历库存，在提交订单时校验，此处跳过）
        for (Cart cart : addCarts) {
            if (ProductType.isReservationProduct(cart.getProductType())) {
                continue;
            }
            Integer productStock = getCartStock(cart.getProductId(), Optional.ofNullable(cart.getSkuId()).orElse(0), true, cart.getShopId());
            if (cart.getQuantity().compareTo(productStock) > 0) {
                throw new GlobalException(messageHelper.get("error.product.stock.insufficient.named", product.getProductName()));
            }
        }

        if (dto.getIsQuick()) {
            // 先将会员的所有其它购物车商品设置为未选择
            this.lambdaUpdate().set(Cart::getIsChecked, 0).eq(Cart::getUserId, userId).update();
        }

        // 如果购物车中商品已存在（快速购买不删除原行，保留原始数量）
        if (CollUtil.isNotEmpty(matchedCarts) && !Boolean.TRUE.equals(dto.getIsQuick())) {
            // 删除原购物车数据
            List<Integer> cartIds = matchedCarts.stream().map(Cart::getCartId).toList();
            this.removeByIds(cartIds);
        }

        if (adminType != null && dto.getFinalCartType() != null) {
            addCarts.forEach(cart -> cart.setType(dto.getFinalCartType()));
        }

        cartWholesaleEnhancer.markAndValidateOnAdd(addCarts, userId, product.getProductId());

        return this.saveBatch(addCarts);
    }

    @Override
    public Boolean checkProductLimitNumber(Integer productId, Integer userId, Integer quantity, Integer cartId) {
        // 查询产品信息
        Product product = productService.getById(productId);
        if (product == null) {
            return false;
        }

        // 检查店铺状态
        shopService.checkShopStatus(product.getShopId(), product.getProductName());

        Integer limitNumber = product.getLimitNumber();
        if (limitNumber != null && limitNumber > 0) {
            // 查询符合条件的订单ID列表
            QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
            orderWrapper.select("order_id").eq("user_id", userId).in("pay_status", Arrays.asList(PaymentStatus.UNPAID.getCode(), PaymentStatus.PROCESSING.getCode(), PaymentStatus.PAID.getCode())).in("order_status", Arrays.asList(OrderStatusEnum.PENDING.getCode(), OrderStatusEnum.CONFIRMED.getCode(), OrderStatusEnum.PROCESSING.getCode(), OrderStatusEnum.COMPLETED.getCode()));

            List<Integer> orderIds = orderService.list(orderWrapper).stream().map(Order::getOrderId).collect(Collectors.toList());

            // 计算已购买数量
            int quantitySum = 0;
            if (!orderIds.isEmpty()) {
                QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
                itemWrapper.in("order_id", orderIds).eq("product_id", productId);

                List<OrderItem> orderItems = orderItemMapper.selectList(itemWrapper);
                quantitySum = Optional.ofNullable(orderItems).map(list -> list.stream().mapToInt(OrderItem::getQuantity).sum()).orElse(0);

                // 查询售后数量
                List<Aftersales> aftersales = aftersalesService.lambdaQuery().in(Aftersales::getOrderId, orderIds).eq(Aftersales::getStatus, AftersalesStatusEnum.COMPLETE.getCode()).list();
                if (CollUtil.isNotEmpty(aftersales) && CollUtil.isNotEmpty(orderItems)) {
                    List<Integer> aftersaleIds = aftersales.stream().map(Aftersales::getAftersaleId).toList();
                    List<Integer> orderItemIds = orderItems.stream().map(OrderItem::getItemId).toList();

                    List<AftersalesItem> aftersalesItems = aftersalesItemService.lambdaQuery().in(AftersalesItem::getOrderItemId, orderItemIds).in(AftersalesItem::getAftersaleId, aftersaleIds).list();

                    for (AftersalesItem aftersalesItem : aftersalesItems) {
                        quantitySum = quantitySum - aftersalesItem.getNumber();
                    }
                }

            }

            // 计算购物车数量
            Integer cartNum = 0;
            if (cartId != null && cartId > 0) {
                List<Cart> carts = this.lambdaQuery().eq(Cart::getProductId, productId).eq(Cart::getUserId, userId).eq(Cart::getType, CartTypeEnum.TYPE_NORMAL.getCode()).list();
                cartNum = carts.stream().map(Cart::getQuantity).reduce(0, Integer::sum);
            }

            // 检查购买限制
            if (quantity > (limitNumber - quantitySum - cartNum)) {

                if (cartId != null && cartId > 0) {
                    // 更新购物车状态
                    Cart updateCart = new Cart();
                    updateCart.setIsChecked(0);
                    this.update(updateCart, new LambdaUpdateWrapper<Cart>().eq(Cart::getCartId, cartId));
                }

                throw new GlobalException(messageHelper.get("error.product.purchase.limit.exceeded"));
            }
        }
        return true;
    }

    @Override
    public Boolean checkOrderProductLimitNumber(Integer productId, Integer userId, Integer quantity, Integer cartId) {
        // 查询产品信息
        Product product = productService.getById(productId);
        if (product == null) {
            return false;
        }
        Integer limitNumber = product.getLimitNumber();
        if (!(limitNumber != null && limitNumber > 0)) {
            return true;
        }

        // 检查店铺状态
        shopService.checkShopStatus(product.getShopId(), product.getProductName());

        // 查询符合条件的订单ID列表
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.select("order_id").eq("user_id", userId).in("pay_status", Arrays.asList(PaymentStatus.UNPAID.getCode(), PaymentStatus.PROCESSING.getCode(), PaymentStatus.PAID.getCode())).in("order_status", Arrays.asList(OrderStatusEnum.PENDING.getCode(), OrderStatusEnum.CONFIRMED.getCode(), OrderStatusEnum.PROCESSING.getCode(), OrderStatusEnum.COMPLETED.getCode()));

        List<Integer> orderIds = orderService.list(orderWrapper).stream().map(Order::getOrderId).collect(Collectors.toList());

        // 计算已购买数量
        int quantitySum = 0;
        if (!orderIds.isEmpty()) {
            QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
            itemWrapper.in("order_id", orderIds).eq("product_id", productId);

            List<OrderItem> orderItems = orderItemMapper.selectList(itemWrapper);
            quantitySum = Optional.ofNullable(orderItems).map(list -> list.stream().mapToInt(OrderItem::getQuantity).sum()).orElse(0);

            // 查询售后数量
            List<Aftersales> aftersales = aftersalesService.lambdaQuery().in(Aftersales::getOrderId, orderIds).eq(Aftersales::getStatus, AftersalesStatusEnum.COMPLETE.getCode()).list();
            if (CollUtil.isNotEmpty(aftersales) && CollUtil.isNotEmpty(orderItems)) {
                List<Integer> aftersaleIds = aftersales.stream().map(Aftersales::getAftersaleId).toList();
                List<Integer> orderItemIds = orderItems.stream().map(OrderItem::getItemId).toList();

                List<AftersalesItem> aftersalesItems = aftersalesItemService.lambdaQuery().in(AftersalesItem::getOrderItemId, orderItemIds).in(AftersalesItem::getAftersaleId, aftersaleIds).list();

                for (AftersalesItem aftersalesItem : aftersalesItems) {
                    quantitySum = quantitySum - aftersalesItem.getNumber();
                }
            }
        }

        // 检查购买限制
        if (quantity > (limitNumber - quantitySum)) {

            if (cartId != null && cartId > 0) {
                // 更新购物车状态
                Cart updateCart = new Cart();
                updateCart.setIsChecked(0);
                this.update(updateCart, new LambdaUpdateWrapper<Cart>().eq(Cart::getCartId, cartId));
            }

            throw new GlobalException(messageHelper.get("error.product.purchase.limit.exceeded"));
        }
        return true;
    }

    @Override
    public void clearCart(Integer userId, Integer type) {
        this.remove(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId).eq(Cart::getType, type));
        if (Objects.nonNull(userId)) {
            cartGlobalChangePriceService.removeById(userId);
        }
    }

    @Override
    public Integer getCartTypeByProduct(Integer id, Integer type) {
        Product product = productService.getById(id);
        Assert.notNull(product, () -> new GlobalException("商品不存在"));

        // 预售商品优先判断
        Integer saleMode = Optional.ofNullable(product.getSaleMode()).orElse(0);
        if (saleMode > 0) {
            return CartTypeEnum.TYPE_RESERVATION_PRODUCT.getCode();
        }

        return switch (type) {
            case 2 -> CartTypeEnum.TYPE_PIN.getCode();
            case 3 -> CartTypeEnum.TYPE_EXCHANGE.getCode();
            case 4 -> CartTypeEnum.TYPE_GIFT.getCode();
            case 5 -> CartTypeEnum.TYPE_BARGAIN.getCode();
            default -> switch (product.getProductType()) {
                case 1 -> CartTypeEnum.TYPE_NORMAL.getCode();
                case 2 -> CartTypeEnum.TYPE_VIRTUAL.getCode();
                case 3 -> CartTypeEnum.TYPE_CARD.getCode();
                case 4 -> CartTypeEnum.TYPE_PAID.getCode();
                case 5 -> CartTypeEnum.TYPE_RESERVATION_HOME.getCode();
                case 6 -> CartTypeEnum.TYPE_RESERVATION_STORE.getCode();
                case 7, 8 -> CartTypeEnum.TYPE_GIFT_CARD.getCode();
                default -> throw new GlobalException("商品类型错误");
            };
        };
    }
}
