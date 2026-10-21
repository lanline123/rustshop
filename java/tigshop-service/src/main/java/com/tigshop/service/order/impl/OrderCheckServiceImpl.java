package com.tigshop.service.order.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.dto.cart.AddToCartDTO;
import com.tigshop.bean.dto.cart.CartPromotionParsePriceDTO;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.dto.order.OrderPresaleDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.cart.CartTypeEnum;
import com.tigshop.bean.enums.finance.StatementType;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.enums.msg.MessageTypeEnum;
import com.tigshop.bean.enums.order.*;
import com.tigshop.bean.enums.product.ProductType;
import com.tigshop.bean.enums.promotion.GiftCardClaimedEnum;
import com.tigshop.bean.enums.promotion.PromotionType;
import com.tigshop.bean.enums.promotion.ValidityTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.vendor.VendorProductSkuStockBizEnum;
import com.tigshop.bean.model.cart.Cart;
import com.tigshop.bean.model.cart.CartGlobalChangePrice;
import com.tigshop.bean.model.finance.CreditUser;
import com.tigshop.bean.model.finance.OrderInvoice;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.model.o2o.*;
import com.tigshop.bean.model.order.*;
import com.tigshop.bean.model.product.ECard;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductPresale;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.promotion.*;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.bean.model.setting.ShippingTplInfo;
import com.tigshop.bean.model.setting.TaxRate;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserAddress;
import com.tigshop.bean.model.user.UserCoupon;
import com.tigshop.bean.model.user.UserRank;
import com.tigshop.bean.param.finance.orderinvoice.OrderInvoiceSaveParam;
import com.tigshop.bean.param.order.OrderCheckParam;
import com.tigshop.bean.param.promotion.giftcard.ExchangeCardParam;
import com.tigshop.bean.param.settings.shippingtpl.RegionDataParam;
import com.tigshop.bean.param.vendor.product.VendorProductSkuStockParam;
import com.tigshop.bean.query.order.StoreShippingTypeQuery;
import com.tigshop.bean.vo.cart.CartByStoreVO;
import com.tigshop.bean.vo.cart.CartListByStoreVO;
import com.tigshop.bean.vo.cart.CartVO;
import com.tigshop.bean.vo.finance.UserCreditInfoVO;
import com.tigshop.bean.vo.o2o.DeliveryOptionVO;
import com.tigshop.bean.vo.order.OrderCheckVO;
import com.tigshop.bean.vo.order.OrderCheckVO.ShippingFee;
import com.tigshop.bean.vo.order.OrderSubmitVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.product.ProductPresaleVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.bean.vo.user.UserAddressVO;
import com.tigshop.service.user.MemberAudienceService;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.*;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.promotion.GiftCardGroupsMapper;
import com.tigshop.mapper.promotion.PointsExchangeMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.user.UserAddressMapper;
import com.tigshop.service.cart.CartGlobalChangePriceService;
import com.tigshop.service.cart.CartService;
import com.tigshop.service.finance.*;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.msgTemplate.MessageService;
import com.tigshop.service.order.*;
import com.tigshop.service.pay.PayService;
import com.tigshop.service.pay.PaymentService;
import com.tigshop.service.lang.LocalesService;
import com.tigshop.service.product.*;
import com.tigshop.service.product.impl.ProductServiceImpl;
import com.tigshop.service.promotion.*;
import com.tigshop.bean.enums.salesman.DistributionModeEnum;
import com.tigshop.service.salesman.SalesmanConfigService;
import com.tigshop.service.salesman.SalesmanOrderService;
import com.tigshop.service.salesman.WechatBizRecruitService;
import com.tigshop.bean.model.lang.Currency;
import com.tigshop.bean.model.lang.Locales;
import com.tigshop.service.setting.*;
import com.tigshop.service.shop.ShopService;
import com.tigshop.service.support.PosCheckoutContext;
import com.tigshop.service.user.*;
import com.tigshop.bean.model.wholesale.ProductWholesaleConfig;
import com.tigshop.bean.model.wholesale.ProductWholesaleSkuConfig;
import com.tigshop.bean.model.wholesale.Wholesaler;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.service.wholesale.WholesaleBuyRuleValidator;
import com.tigshop.service.wholesale.WholesaleProductConfigService;
import com.tigshop.service.wholesale.WholesaleSkuConfigService;
import com.tigshop.service.wholesale.WholesalerService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.tigshop.bean.enums.product.ProductType.*;
import static com.tigshop.bean.enums.setting.SettingsEnum.USE_COUPON;
import static com.tigshop.bean.enums.setting.SettingsEnum.USE_SURPLUS;
import static com.tigshop.common.constant.Constants.YES;
import static com.tigshop.common.utils.RoundOffUtil.clearDecimal;

/**
 * 订单结算
 *
 * @author Tigshop团队
 * @create 2024-09-30 15:16:06
 */
@Slf4j
@Service
public class OrderCheckServiceImpl implements OrderCheckService {

    @Resource
    OrderOnboardingGuard orderOnboardingGuard;

    @Resource
    OrderService orderService;

    @Resource
    AdminMsgService adminMsgService;

    @Resource
    StockWarningService stockWarningService;

    @Resource
    OrderItemService orderItemService;

    @Resource
    OrderInvoiceService orderInvoiceService;

    @Resource
    ProductPresaleService productPresaleService;

    @Resource
    SalesmanOrderService salesmanOrderService;

    @Resource
    SalesmanConfigService salesmanConfigService;

    @Resource
    WechatBizRecruitService wechatBizRecruitService;

    @Resource
    CartService cartService;

    @Resource
    ShippingTplService shippingTplService;

    @Resource
    ShippingTplInfoService shippingTplInfoService;

    @Resource
    SeckillService seckillService;

    @Resource
    ILotteryRecordService lotteryRecordService;

    @Resource
    OrderPresaleService orderPresaleService;

    @Resource
    PaylogService paylogService;


    @Resource
    ConfigService configService;

    @Resource
    TaxRateService taxRateService;

    @Resource
    RegionService regionService;

    @Resource
    ProductService productService;

    @Resource
    ProductSkuService productSkuService;


    @Resource
    UserAddressService userAddressService;

    @Resource
    UserService userService;

    @Resource
    UserRankService userRankService;

    @Resource
    MemberBenefitService memberBenefitService;

    @Resource
    MessageTemplateService messageTemplateService;

    @Resource
    UserCouponService userCouponService;

    @Resource
    OrderAmountDetailService orderAmountDetailService;

    @Resource
    OrderCouponDetailService orderCouponDetailService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private PointsExchangeMapper pointsExchangeMapper;

    @Resource
    private TranslatePackageImpl translatePackage;

    @Resource
    private com.tigshop.common.i18n.MessageHelper messageHelper;

    @Resource
    private ECardGroupService eCardCardService;

    @Resource
    private ECardService eCardService;

    @Resource
    private ShopService shopService;

    @Resource
    private MessageService messageService;


    @Resource
    private ProductGiftService productGiftService;

    @Resource
    private UserAuthorizeService userAuthorizeService;

    @Resource
    private RecordRateService recordRateService;
    @Autowired
    private TigshopProperties tigshopProperties;
    @Autowired
    private OrderReservationService orderReservationService;
    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;
    @Autowired
    private UserCashLogService userCashLogService;

    @Resource
    private CartGlobalChangePriceService cartGlobalChangePriceService;

    @Resource
    private GrouponPromotionDetailService grouponPromotionDetailService;
    @Autowired
    private GiftCardsService giftCardsService;
    @Autowired
    private GiftCardGroupsMapper giftCardGroupsMapper;
    @Resource
    private CreditUserService creditUserService;
    @Resource
    private CreditOrderService creditOrderService;

    @Resource
    @Qualifier("AliPayService")
    private PayService aliPayService;
    @Autowired
    private PaymentService paymentService;


    @Resource
    private PromotionService promotionService;

    @Resource
    private MemberAudienceService memberAudienceService;

    @Resource
    private GrouponPromotionRecordService grouponPromotionRecordService;

    @Resource
    private GalleryPicService galleryPicService;

    @Autowired
    private com.tigshop.mapper.cart.CartMapper cartMapper;

    @Resource
    private WholesalerService wholesalerService;

    @Resource
    private WholesaleProductConfigService wholesaleProductConfigService;

    @Resource
    private WholesaleSkuConfigService wholesaleSkuConfigService;

    @Resource
    private com.tigshop.service.wholesale.WholesalePriceService wholesalePriceService;

    @Resource
    private LocalesService localesService;


    @Override
    public OrderInvoice getLastInvoice(Integer invoiceType, Integer titleType) {
        if (invoiceType == null) {
            invoiceType = 0;
        }
        if (titleType == null) {
            titleType = 0;
        }
        return orderInvoiceService.lambdaQuery().eq(OrderInvoice::getInvoiceType, invoiceType).eq(OrderInvoice::getTitleType, titleType).eq(OrderInvoice::getUserId, SecurityUtils.getCurrentUserId()).orderByDesc(OrderInvoice::getId).last("limit 1").one();
    }

    private void checkDisabledCart(CartListByStoreVO cartListByStore) {
        if (cartListByStore == null || CollUtil.isEmpty(cartListByStore.getCartList())) {
            return;
        }
        Optional<CartVO> disabledCart = cartListByStore.getCartList().stream()
                .filter(Objects::nonNull)
                .map(CartByStoreVO::getCarts)
                .filter(CollUtil::isNotEmpty)
                .flatMap(List::stream)
                .filter(cart -> Boolean.TRUE.equals(cart.getIsDisabled()))
                .findFirst();
        if (disabledCart.isEmpty()) {
            return;
        }
        String reason = disabledCart.get().getDisabledReason();
        if ("sku_disabled".equals(reason)) {
            throw new GlobalException("商品规格已禁用，请重新选择");
        }
        if ("product_offline".equals(reason)) {
            throw new GlobalException(messageHelper.get("error.product.off.sale.name", disabledCart.get().getProductName()));
        }
        if ("sold_out".equals(reason)) {
            throw new GlobalException(messageHelper.get("error.product.stock.insufficient.name", disabledCart.get().getProductName()));
        }
    }

    @Override
    public OrderCheckVO getOrderCheckData(OrderCheckParam param) {
        Integer currentUserId = param.getUserId() == null ? SecurityUtils.getCurrentUserId() : param.getUserId();

        DeliveryOptionVO deliveryOption;
        UserAddressVO selectedAddress = resolveSelectedAddress(param.getFlowType(), param.getUserId(), param.getAddressId());
        if (CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode().equals(param.getFlowType()) && param.getUserId() != null) {
            List<Cart> carts = cartMapper.selectList(Wrappers.lambdaQuery(Cart.class)
                    .eq(Cart::getUserId, param.getUserId())
                    .eq(Cart::getType, CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode())
                    .eq(param.getShopId() != null, Cart::getShopId, param.getShopId()));
            deliveryOption = productService.getDeliveryOption(carts, param.getLatitude(), param.getLongitude(), param.getDeliveryType(), selectedAddress);
        } else {
            deliveryOption = productService.getDeliveryOption(param.getShopId(), param.getLatitude(), param.getLongitude(), param.getDeliveryType());
        }
        if (ObjectUtil.isNull(param.getDeliveryType())) {
            // 获取支持的配送方式
            String configVal = configService.getConfigVal(SettingsEnum.STORE_CUSTOM_SUBMIT_SHIPPING_TYPE);

            // 检查各种配送方式是否有可用商品
            boolean hasDeliveryProducts = deliveryOption.getDeliveryProducts() != null && !deliveryOption.getDeliveryProducts().isEmpty();
            boolean hasPickupProducts = deliveryOption.getPickupProducts() != null && !deliveryOption.getPickupProducts().isEmpty();
            boolean hasShopDeliveryProducts = deliveryOption.getShopDeliveryProducts() != null && !deliveryOption.getShopDeliveryProducts().isEmpty();

            // 根据可用商品情况选择配送方式
            if (hasDeliveryProducts && hasPickupProducts && hasShopDeliveryProducts) {
                // 全部支持，根据配置选择
                if (ObjectUtil.equals(configVal, "0")) {
                    param.setDeliveryType(OrderDeliveryTypeEnum.PICKUP.getCode());
                } else if (ObjectUtil.equals(configVal, "1")) {
                    param.setDeliveryType(OrderDeliveryTypeEnum.LOGISTICS.getCode());
                } else if (ObjectUtil.equals(configVal, "2")) {
                    param.setDeliveryType(OrderDeliveryTypeEnum.SAME_CITY.getCode());
                }
            } else if (hasDeliveryProducts && hasPickupProducts) {
                // 支持配送和自提，根据配置选择
                if (ObjectUtil.equals(configVal, "0")) {
                    param.setDeliveryType(OrderDeliveryTypeEnum.PICKUP.getCode());
                } else {
                    param.setDeliveryType(OrderDeliveryTypeEnum.LOGISTICS.getCode());
                }
            } else if (hasDeliveryProducts && hasShopDeliveryProducts) {
                // 支持配送和同城，根据配置选择
                if (ObjectUtil.equals(configVal, "2")) {
                    param.setDeliveryType(OrderDeliveryTypeEnum.SAME_CITY.getCode());
                } else {
                    param.setDeliveryType(OrderDeliveryTypeEnum.LOGISTICS.getCode());
                }
            } else if (hasPickupProducts && hasShopDeliveryProducts) {
                // 支持自提和同城，根据配置选择
                if (ObjectUtil.equals(configVal, "0")) {
                    param.setDeliveryType(OrderDeliveryTypeEnum.PICKUP.getCode());
                } else {
                    param.setDeliveryType(OrderDeliveryTypeEnum.SAME_CITY.getCode());
                }
            } else if (hasDeliveryProducts) {
                // 只支持配送
                param.setDeliveryType(OrderDeliveryTypeEnum.LOGISTICS.getCode());
            } else if (hasPickupProducts) {
                // 只支持自提
                param.setDeliveryType(OrderDeliveryTypeEnum.PICKUP.getCode());
            } else if (hasShopDeliveryProducts) {
                // 只支持同城
                param.setDeliveryType(OrderDeliveryTypeEnum.SAME_CITY.getCode());
            }
        }
        if (tigshopProperties.getIsO2o() == 0) {
            param.setDeliveryType(OrderDeliveryTypeEnum.LOGISTICS.getCode());
        }


        // 抽奖商品检查抽奖记录
        if (param.getLotteryRecordId() != null) {
            LotteryRecord lotteryRecord = lotteryRecordService.getById(param.getLotteryRecordId());
            if (lotteryRecord == null) {
                throw new GlobalException(messageHelper.get("error.lottery.record.not.exist"));
            }
            if (!lotteryRecord.getUserId().equals(currentUserId)) {
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

        // 检查用户是否为企业认证用户与B2B类型
        userService.checkUserCompanyAuth();

        CartListByStoreVO cartListByStore = cartService.getCartListByStore(true, param.getFlowType(), Map.of("excludeTimeDiscountPrice", true), param.getShopId(), currentUserId, null);
        checkDisabledCart(cartListByStore);
        String childAreaNeedRegion = configService.getConfigByCode("childAreaNeedRegion").getBizVal();
        if ("1".equals(childAreaNeedRegion) && param.getAddressId()!=null) {
            cartListByStore.getCartList().stream().flatMap(store -> store.getCarts().stream()).filter(cart -> cart.getFixedShippingType() == 2)
                    .forEach(cart -> shippingTplInfoService.verifyUserAddress(Math.toIntExact(cart.getShippingTplId()), param.getAddressId()));
        }

        // 代客下单：根据选择的配送方式设置购物车商品的选中状态
        if (CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode().equals(param.getFlowType()) && param.getDeliveryType() != null) {
            Set<Integer> supportedProductIds = getDeliveryTypeSupportedProductIds(deliveryOption, param.getDeliveryType());
            BigDecimal recalcProductAmount = BigDecimal.ZERO;
            int recalcCheckedCount = 0;
            for (CartByStoreVO store : cartListByStore.getCartList()) {
                for (CartVO cart : store.getCarts()) {
                    boolean supported = supportedProductIds.contains(cart.getProductId());
                    cart.setIsChecked(supported);
                    if (supported) {
                        recalcCheckedCount += cart.getQuantity();
                        recalcProductAmount = recalcProductAmount.add(
                                cart.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
                    }
                }
            }
            cartListByStore.getTotal().setProductAmount(recalcProductAmount);
            cartListByStore.getTotal().setCheckedCount(recalcCheckedCount);
            cartListByStore.getTotal().setDiscountAfter(recalcProductAmount);
        }

        // 代客下单：移除未选中的商品，只保留选中的商品进入结算
        if (CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode().equals(param.getFlowType())) {
            for (CartByStoreVO store : cartListByStore.getCartList()) {
                store.getCarts().removeIf(cart -> !Boolean.TRUE.equals(cart.getIsChecked()));
            }
            cartListByStore.getCartList().removeIf(store -> store.getCarts().isEmpty());
        }

        CartListByStoreVO cartList = cartService.buildCartPromotion(cartListByStore, param, param.getUserId(), param.getFlowType(), param.getUseDefaultCouponIds(), param.getUseCouponIds());

        // 给购物车 shopId 为 0 的重新设置 shopTitle

        List<Integer> useCouponIds = new ArrayList<>();
        List<Integer> selectUserCouponIds = CollUtil.isNotEmpty(param.getSelectUserCouponIds()) ? param.getSelectUserCouponIds() : new ArrayList<>();
        if (!CartTypeEnum.TYPE_LOTTERY_PRODUCT.getCode().equals(param.getFlowType()) && (param.getSelectFrom() == SelectFromEnum.FIRST_LOAD.getValue() || (param.getSelectFrom() == SelectFromEnum.CHECK_UPDATE.getValue() && CollUtil.isEmpty(param.getSelectUserCouponIds())))) {
            for (CartByStoreVO cartByStoreVO : cartList.getCartList()) {
                if (cartByStoreVO.getUsedPromotions() != null) {
                    cartByStoreVO.getUsedPromotions().forEach(promotionVO -> {
                        if (promotionVO.getType() == 2) {
                            useCouponIds.add(promotionVO.getRelationId());
                            UserCoupon userCoupon = userCouponService.getUserCouponByCouponId(currentUserId, promotionVO.getRelationId());
                            selectUserCouponIds.add(userCoupon.getId());
                        }
                    });
                }
                //批量预取本店铺购物车商品，避免下方循环内逐条 getById 判断礼品卡（取数与原 getById 完全一致）
                List<Integer> giftCheckPids = cartByStoreVO.getCarts().stream().map(CartVO::getProductId).toList();
                Map<Integer, Product> giftCheckProductMap = giftCheckPids.isEmpty() ? new HashMap<>() :
                        productService.listByIds(giftCheckPids).stream()
                                .collect(Collectors.toMap(Product::getProductId, p -> p, (a, b) -> a));
                for (CartVO cart : cartByStoreVO.getCarts()) {
                    //库存不足不让添加 礼品卡
                    Product product = giftCheckProductMap.get(cart.getProductId());
                    if (product != null && product.getGiftCardGroupsId() != null && product.getGiftCardGroupsId() > 0) {
                        GiftCardGroups giftCardGroups = giftCardGroupsMapper.selectById(product.getGiftCardGroupsId());
                        if (Objects.equals(giftCardGroups.getValidityType(), ValidityTypeEnum.FIXED.getCode())) {
                            if (giftCardGroups.getExpireTime() != null && giftCardGroups.getExpireTime() > 0 && giftCardGroups.getExpireTime() < System.currentTimeMillis() / 1000) {
                                throw new GlobalException(messageHelper.get("error.gift.card.expired", giftCardGroups.getCardGroupName()));
                            }
                        }
                        if (giftCardGroups == null || giftCardGroups.getAvailabilityStock() <= 0 || giftCardGroups.getAvailabilityStock() < cart.getQuantity()) {
                            throw new GlobalException(messageHelper.get("error.product.stock.insufficient.name", product.getProductName()));
                        }
                        if (product.getProductStock() < cart.getQuantity()) {
                            throw new GlobalException(messageHelper.get("error.product.stock.insufficient.name", product.getProductName()));
                        }
                    }
                }
            }
            param.setUseCouponIds(useCouponIds);
            param.setSelectUserCouponIds(selectUserCouponIds);
        }

        //获得用户数据
        User user = userService.getById(currentUserId);
        if (Objects.isNull(user)) {
            throw new GlobalException(messageHelper.get("error.user.not.exist"));
        }

        OrderCheckVO orderCheckVO = new OrderCheckVO();
        orderCheckVO.setCartList(cartList.getCartList());

        if (CartTypeEnum.TYPE_LOTTERY_PRODUCT.getCode().equals(param.getFlowType())) {
            orderCheckVO.setCouponList(null);
        } else {
            OrderCheckVO.CouponList couponList = getCouponListByPromotion(cartList, param.getUseCouponIds(), param.getSelectUserCouponIds(), currentUserId);
            orderCheckVO.setCouponList(couponList);
        }

        orderCheckVO.setSelectUserCouponIds(selectUserCouponIds);


        fillPresalesProductDepositAmount(param, cartList, orderCheckVO);

        OrderCheckVO.Total total = getTotalFee(param, cartList, user);
        orderCheckVO.setTotal(total);

        if (CartTypeEnum.TYPE_RESERVATION_PRODUCT.getCode().equals(param.getFlowType())) {
            BigDecimal depositAmount = param.getDepositAmount();
            orderCheckVO.getTotal().setDepositAmount(depositAmount);
            //实际支付的定金金额
            orderCheckVO.getTotal().setActualPayDepositAmount(depositAmount.subtract(Optional.ofNullable(total.getBalance()).orElse(BigDecimal.ZERO)));
            //订单金额
            BigDecimal totalAmount = orderCheckVO.getTotal().getTotalAmount();
            //尾款金额 (总金额-定金)
            orderCheckVO.getTotal().setFinalPaymentAmount(totalAmount.subtract(depositAmount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
        }

        OrderCheckVO.Item item = new OrderCheckVO.Item();
        BeanUtil.copyProperties(param, item);
        fillChangePriceInfo(orderCheckVO, item, cartListByStore);
        orderCheckVO.setItem(item);

        List<String> miniProgramTemplateIds = messageTemplateService.getMiniProgramTemplateIds();
        orderCheckVO.setTmplIds(miniProgramTemplateIds);

        orderCheckVO.setFlowType(param.getFlowType());
        orderCheckVO.setUseCouponIds(param.getUseCouponIds());

        if (CartTypeEnum.TYPE_LOTTERY_PRODUCT.getCode().equals(param.getFlowType())) {
            orderCheckVO.setAvailablePoints(0);
        } else {
            Integer userPoints = getOrderAvailablePoints(cartListByStore, user);
            orderCheckVO.setAvailablePoints(userPoints);
        }


        // INSERT_YOUR_CODE
        // 检查每个店铺下面的cart里的price总和，对比shop_local_delivery_config里的起送价
        orderCheckVO.setDeliveryOption(deliveryOption);


        // ========== 账期支付信息填充 ==========
        try {
            String useCredit = configService.getConfigVal(SettingsEnum.USE_CREDIT);
            if ("1".equals(useCredit)) {
                BigDecimal orderTotalAmount = orderCheckVO.getTotal() != null ? orderCheckVO.getTotal().getTotalAmount() : BigDecimal.ZERO;
                UserCreditInfoVO creditPaymentInfo = creditUserService.getUserCreditInfo(currentUserId, orderTotalAmount);
                orderCheckVO.setCreditPaymentInfo(creditPaymentInfo);
            }
        } catch (Exception e) {
            log.error("填充账期支付信息失败", e);
            // 即使失败也不影响结算页正常展示
        }
        // ========== 账期支付信息填充结束 ==========


        return orderCheckVO;
    }

    private void fillOrderItemOverseasImage(OrderCheckVO orderCheckVO) {
        if (tigshopProperties.getIsOverseas() == 1) {
            List<CartByStoreVO> cartList = orderCheckVO.getCartList();
            if(CollUtil.isEmpty(cartList)){
                return;
            }
            cartList.stream().forEach(store ->{
                if(CollUtil.isEmpty(store.getCarts())){
                    return;
                }
                store.getCarts().stream().filter(Objects::nonNull).forEach(cart ->{
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

    /**
     * 移除URL中的域名部分，保留路径
     * 例如：https://example.com/path/to/image.jpg -> /path/to/image.jpg
     */
    private String removeUrlDomain(String url) {
        if (StringUtils.isBlank(url)) {
            return url;
        }
        try {
            java.net.URL parsedUrl = new java.net.URL(url);
            return parsedUrl.getPath();
        } catch (Exception e) {
            return url;
        }
    }



    /**
     * 填充预售商品定金金额
     *
     * @param param
     * @param cartList
     * @param orderCheckVO
     */
    private void fillPresalesProductDepositAmount(OrderCheckParam param, CartListByStoreVO cartList, OrderCheckVO orderCheckVO) {
        if (CartTypeEnum.TYPE_RESERVATION_PRODUCT.getCode().equals(param.getFlowType())) {
            //预售流程：仅支持单商品单规格一单，数量可大于1
            List<CartVO> presaleCarts = cartList.getCartList().stream().flatMap(cartListVo -> cartListVo.getCarts().stream()).toList();
            if (presaleCarts.size() != 1) {
                throw new GlobalException(translatePackage.translate("预售商品只能单独购买一种商品"));
            }
            CartVO cartVo = presaleCarts.get(0);
            Integer presaleQuantity = Optional.ofNullable(cartVo.getQuantity()).orElse(0);
            if (presaleQuantity < 1) {
                throw new GlobalException(translatePackage.translate("商品数量错误"));
            }
            //计算定金（单件定金 × 数量）
            ProductPresale productPresale = productPresaleService.getByProductId(cartVo.getProductId());
            //定金预售
            BigDecimal depositAmount = productPresaleService.calculateDepositAmount(cartVo.getProductId(), cartVo.getSkuId(), productPresale)
                    .multiply(BigDecimal.valueOf(presaleQuantity));
            param.setDepositAmount(depositAmount);

            ProductPresaleVO presaleVOByProductId = productPresaleService.getPresaleVOByProductId(cartVo.getProductId());
            if (Objects.nonNull(orderCheckVO)) {
                orderCheckVO.setProductPresale(presaleVOByProductId);
                try{
                    presaleVOByProductId.setFinalPaymentStartTimeText(productService.buildPresaleFinalPaymentTimeText(presaleVOByProductId,
                            HeaderUtils.getClientType(), ProductServiceImpl.PRESALE_SOURCE_ORDER_SUBMIT));
                    presaleVOByProductId.setDeliveryTimeText(productService.buildPresaleDeliveryTimeText(presaleVOByProductId,
                            HeaderUtils.getClientType(), ProductServiceImpl.PRESALE_SOURCE_ORDER_SUBMIT));
                }catch (Exception e){

                }
            }
            param.setProductPresale(presaleVOByProductId);
        } else {
            param.setDepositAmount(null);
            param.setProductPresale(null);
        }
    }



    private BigDecimal getUserBalance(User user) {
        return user.getBalance().compareTo(BigDecimal.valueOf(0)) > 0 ? user.getBalance() : BigDecimal.valueOf(0);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public OrderSubmitVO submit(OrderCheckParam param) {
        Integer currentUserId = SecurityUtils.getCurrentUserId();


        String closeOrder = configService.getConfigVal(SettingsEnum.CLOSE_ORDER);
        Assert.isFalse("1".equals(closeOrder), () -> new GlobalException(messageHelper.get("error.mall.maintenance")));
        // 服务端兜底：发票功能关闭时忽略发票数据，防止绕过前端隐藏直接提交
        if (param.getInvoiceData() != null && !"1".equals(configService.getConfigVal(SettingsEnum.CAN_INVOICE))) {
            param.setInvoiceData(null);
        }
        // 获取是否默认优惠券，清空使用的优惠券
        String useCoupon = configService.getConfigVal(USE_COUPON);
        param.submitInitParam(useCoupon);
        Integer payTypeId = param.getPayTypeId();

        // 获得用户数据
        Integer userId = Optional.ofNullable(param.getUserId()).orElse(currentUserId);
        User user = userService.getById(userId);
        if (Objects.isNull(user)) {
            throw new GlobalException(messageHelper.get("error.user.not.exist"));
        }

        if ("wechat".equals(HeaderUtils.getClientType())) {
            if (!userAuthorizeService.checkUserIsAuthorize(userId, 1)) {
                throw new GlobalException(messageHelper.get("error.auth.login.required"), HttpStatus.USER_NOT_BIND_WECHAT);
            }
        }

        // 抽奖商品订单校验
        LotteryRecord lotteryRecord = null;
        if (param.getLotteryRecordId() != null && param.getLotteryRecordId() > 0) {
            lotteryRecord = lotteryRecordService.getById(param.getLotteryRecordId());
            if (lotteryRecord == null) {
                throw new GlobalException("抽奖记录不存在");
            }
            if (!lotteryRecord.getUserId().equals(userId)) {
                throw new GlobalException("无权操作此抽奖记录");
            }
            if (lotteryRecord.getIsWin() != 1) {
                throw new GlobalException("该抽奖记录未中奖");
            }
            if (lotteryRecord.getPrizeType() != 2) {
                throw new GlobalException("只有商品奖品需要下单领取");
            }
            if (lotteryRecord.getStatus() != 0) {
                throw new GlobalException("该奖品已领取或已过期");
            }
        }

//        String closeOrder = configService.getConfigVal(SettingsEnum.CLOSE_ORDER);
        Assert.isFalse("1".equals(closeOrder), () -> new GlobalException(messageHelper.get("error.mall.maintenance")));

        // 代客下单：将提交请求中的改价数据持久化到购物车，确保后续 buildCartPromotion 能正确读取
        if (CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode().equals(param.getFlowType())) {
            if (param.getCartChangePrices() != null && !param.getCartChangePrices().isEmpty()) {
                List<Cart> updCarts = param.getCartChangePrices().stream()
                        .filter(Objects::nonNull)
                        .map(cp -> Cart.builder()
                                .cartId(cp.getCartId())
                                .changePriceData(JSONUtil.toJsonStr(cp))
                                .build())
                        .toList();
                if (!updCarts.isEmpty()) {
                    cartService.updateBatchById(updCarts);
                }
                cartGlobalChangePriceService.removeById(userId);
            } else if (param.getChangePriceValue() != null) {
                CartGlobalChangePrice gcp = cartGlobalChangePriceService.getById(userId);
                if (gcp == null) {
                    gcp = CartGlobalChangePrice.builder()
                            .userId(userId)
                            .changePriceType(param.getTotalChangePriceType())
                            .changePriceValue(param.getChangePriceValue())
                            .build();
                    cartGlobalChangePriceService.save(gcp);
                } else {
                    gcp.setChangePriceType(param.getTotalChangePriceType());
                    gcp.setChangePriceValue(param.getChangePriceValue());
                    cartGlobalChangePriceService.updateById(gcp);
                }
            }
        }

        CartListByStoreVO cartListByStore = cartService.getCartListByStore(true, param.getFlowType(), Map.of("excludeTimeDiscountPrice", true), param.getShopId(), userId, null);
        checkDisabledCart(cartListByStore);
        String childAreaNeedRegion = configService.getConfigByCode("childAreaNeedRegion").getBizVal();
        if ("1".equals(childAreaNeedRegion)) {
            cartListByStore.getCartList().stream().flatMap(store -> store.getCarts().stream()).filter(cart -> cart.getFixedShippingType() == 2).forEach(cart -> {
                Assert.isTrue(shippingTplInfoService.verifyUserAddress(Math.toIntExact(cart.getShippingTplId()), param.getAddressId()), () -> new GlobalException("该地区不支持配送"));
            });
        }

        // 代客下单：根据配送方式过滤商品，只保留支持该配送方式的商品参与下单
        if (CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode().equals(param.getFlowType())) {
            List<Cart> deliveryCarts = cartMapper.selectList(Wrappers.lambdaQuery(Cart.class)
                    .eq(Cart::getUserId, userId)
                    .eq(Cart::getType, CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode())
                    .eq(param.getShopId() != null, Cart::getShopId, param.getShopId()));
            UserAddressVO selectedAddress = resolveSelectedAddress(param.getFlowType(), param.getUserId(), param.getAddressId());
            DeliveryOptionVO deliveryOption = productService.getDeliveryOption(deliveryCarts, null, null, param.getDeliveryType(), selectedAddress);

            // 配送方式校正
            if (tigshopProperties.getIsO2o() == 1) {
                boolean hasDeliveryProducts = CollUtil.isNotEmpty(deliveryOption.getDeliveryProducts());
                boolean hasShopDeliveryProducts = CollUtil.isNotEmpty(deliveryOption.getShopDeliveryProducts());
                if (hasShopDeliveryProducts && !hasDeliveryProducts) {
                    param.setDeliveryType(OrderDeliveryTypeEnum.SAME_CITY.getCode());
                }
            }

            if (param.getDeliveryType() != null) {
                Set<Integer> supportedProductIds = getDeliveryTypeSupportedProductIds(deliveryOption, param.getDeliveryType());
                for (CartByStoreVO store : cartListByStore.getCartList()) {
                    for (CartVO cart : store.getCarts()) {
                        cart.setIsChecked(supportedProductIds.contains(cart.getProductId()));
                    }
                }
            }

            for (CartByStoreVO store : cartListByStore.getCartList()) {
                store.getCarts().removeIf(cart -> !Boolean.TRUE.equals(cart.getIsChecked()));
            }
            cartListByStore.getCartList().removeIf(store -> store.getCarts().isEmpty());
        }

        CartListByStoreVO cartList = cartService.buildCartPromotion(cartListByStore, param, userId, param.getFlowType(), 0, param.getUseCouponIds());
        Assert.isFalse(ObjectUtil.isEmpty(cartList.getCartList()), () -> new GlobalException("请添加商品"));

        // design §12 防绕过：批发行起批量 + 倍数二次硬校验（防止用户从加车到下单期间篡改数量或配置变更）
        validateWholesaleSubmitRules(cartList, userId);

        // 校验使用积分不超过可用积分上限（改价后可用积分可能减少）
        // 积分兑换商品的积分用量由积分商品配置（exchangeIntegral）决定，不受积分支付比例约束，故排除
        if (param.getUsePoint() != null && param.getUsePoint() > 0
                && !CartTypeEnum.TYPE_LOTTERY_PRODUCT.getCode().equals(param.getFlowType())
                && !CartTypeEnum.TYPE_EXCHANGE.getCode().equals(param.getFlowType())) {
            Integer availablePoints = getOrderAvailablePoints(cartList, user);
            if (param.getUsePoint() > availablePoints) {
                String integralName = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
                throw new GlobalException(translatePackage.translate(
                        "使用" + (StrUtil.isNotBlank(integralName) ? integralName : "积分") + "超出可用数量"));
            }
        }

        fillPresalesProductDepositAmount(param, cartList, null);
        OrderCheckVO.Total totalFee = getTotalFee(param, cartList, user);

        // 订单店铺金额明细
        List<OrderAmountDetail> orderAmountList = new ArrayList<>();
        // 订单店铺优惠券明细
        List<OrderCouponDetail> orderCouponDetailList = new ArrayList<>();
        Map<String, Integer> salesmanOrder = new HashMap<>();
        List<Integer> cartIds = new ArrayList<>();
        List<OrderItem> orderItemList = new ArrayList<>();
        List<ECard> eCards = new ArrayList<>();
        Integer defaultVendorId = 0;

        boolean isProxyShoppingWithChangePrice = CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode().equals(param.getFlowType())
                && Boolean.TRUE.equals(cartList.getHasChangePrice());
        BigDecimal proxyTotalOriginalAmount = BigDecimal.ZERO;
        BigDecimal proxyCouponAmount = BigDecimal.ZERO;
        if (isProxyShoppingWithChangePrice) {
            proxyTotalOriginalAmount = cartList.getCartList().stream()
                    .flatMap(store -> store.getCarts().stream())
                    .filter(CartVO::getIsChecked)
                    .map(c -> c.getProductPrice().multiply(BigDecimal.valueOf(c.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            proxyCouponAmount = Optional.ofNullable(cartList.getTotal().getDiscountCouponAmount()).orElse(BigDecimal.ZERO);
        }

        // 代客下单改价：预计算总商品数和合并优惠，用于尾差处理
        int proxyTotalItemCount = 0;
        BigDecimal proxyCombinedDiscount = BigDecimal.ZERO;
        if (isProxyShoppingWithChangePrice && proxyTotalOriginalAmount.compareTo(BigDecimal.ZERO) > 0) {
            proxyTotalItemCount = (int) cartList.getCartList().stream()
                    .flatMap(store -> store.getCarts().stream()).count();
            CartGlobalChangePrice preCalcGcp = cartList.getCartGlobalChangePrice();
            if (preCalcGcp != null) {
                BigDecimal totalChangePriceResult = CartVO.calculateChangePrice(
                        preCalcGcp.getChangePriceValue(), preCalcGcp.getChangePriceType(), proxyTotalOriginalAmount);
                proxyCombinedDiscount = proxyTotalOriginalAmount.subtract(totalChangePriceResult).add(proxyCouponAmount);
            }
        }
        int proxyProcessedItemIdx = 0;
        BigDecimal proxyDistributedDiscount = BigDecimal.ZERO;
        BigDecimal proxyDistributedCoupon = BigDecimal.ZERO;

        // design §6：批发等级快照（一单一查，避免 N+1；零批发行时跳过 DB 查询）
        Integer wholesaleGradeSnapshot = resolveWholesaleGradeSnapshot(cartList, userId);

        for (CartByStoreVO cartByStore : cartList.getCartList()) {
            OrderAmountDetail orderAmountDetail = cartByStore.creatOrderAmountDetail(totalFee);
            orderAmountList.add(orderAmountDetail);

            List<OrderCouponDetail> orderCouponDetails = cartByStore.createOrderCouponDetails();
            orderCouponDetailList.addAll(orderCouponDetails);

            for (CartVO cart : cartByStore.getCarts()) {
                Shop shop = shopService.getById(cart.getShopId());
                Assert.isFalse(shop != null && shop.getStatus() != 1, () -> new GlobalException("该店铺暂停运营，所属商品无法购买"));
                shopService.checkShopStatus(cart.getShopId(), cart.getProductName());
                orderOnboardingGuard.assertShopCanPurchase(cart.getShopId());

                cartService.checkOrderProductLimitNumber(cart.getProductId(), user.getUserId(), cart.getQuantity(), null);
                // 电子礼品卡商品特殊提示
                boolean isVirtualGiftCard = cart.getProductType() != null && cart.getProductType() == ProductType.PRODUCT_TYPE_GIFT_CARD_VIRTUAL.getCode();
                if (isVirtualGiftCard) {
                    Assert.isFalse(cart.getProductStatus() == 0 || cart.getStock() < cart.getQuantity(), () -> new GlobalException(messageHelper.get("error.product.unavailable")));
                } else if (!Objects.equals(param.getFlowType(), CartTypeEnum.TYPE_LOTTERY_PRODUCT.getCode())) {
                    Assert.isFalse(cart.getProductStatus() == 0, () -> new GlobalException(messageHelper.get("error.product.off.sale.name", cart.getProductName())));

                    // ========== 商品库存校验 ==========
                    int requiredStock = cart.getQuantity();
                    boolean skipStockCheck = false;

                    if (!skipStockCheck) {
                        final int finalRequiredStock = requiredStock;
                        Assert.isFalse(cart.getStock() < finalRequiredStock, () -> new GlobalException(messageHelper.get("error.product.stock.shortage.tilde", cart.getProductName())));
                    }
                    // ========== 商品库存校验结束 ==========
                }

                cartIds.add(cart.getCartId());
                OrderItem orderItem = new OrderItem();


                //如果存在局部改价数据就存入到order_item当中去
                CartGlobalChangePrice cartGlobalChangePrice = cartList.getCartGlobalChangePrice();
                OrderCheckParam.CartChangePrice cartChangePrice = cart.getCartChangePrice();
                if (Objects.isNull(cartGlobalChangePrice) && Objects.nonNull(cartChangePrice)) {
                    orderItem.setChangePriceData(JSONUtil.toJsonStr(cartChangePrice));
                }

                orderItem.setUserId(userId);
                if (isProxyShoppingWithChangePrice && proxyTotalOriginalAmount.compareTo(BigDecimal.ZERO) > 0) {
                    proxyProcessedItemIdx++;
                    boolean isLastItem = proxyProcessedItemIdx == proxyTotalItemCount;
                    BigDecimal itemOriginalTotal = cart.getProductPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
                    CartGlobalChangePrice globalChangePriceData = cartList.getCartGlobalChangePrice();
                    if (globalChangePriceData != null) {
                        // 整单改价：改价优惠合并到优惠券一起按占比平摊，最后一个商品取差额消除尾差
                        BigDecimal itemShare;
                        if (isLastItem) {
                            itemShare = proxyCombinedDiscount.subtract(proxyDistributedDiscount);
                        } else {
                            itemShare = proxyCombinedDiscount.multiply(itemOriginalTotal)
                                    .divide(proxyTotalOriginalAmount, 2, RoundingMode.HALF_UP);
                            proxyDistributedDiscount = proxyDistributedDiscount.add(itemShare);
                        }
                        orderItem.setPrice(itemOriginalTotal.subtract(itemShare)
                                .divide(BigDecimal.valueOf(cart.getQuantity()), 2, RoundingMode.HALF_UP));
                    } else {
                        // 单商品改价：优惠券按原始金额占比平摊，最后一个商品取差额消除尾差
                        BigDecimal couponShare;
                        if (isLastItem) {
                            couponShare = proxyCouponAmount.subtract(proxyDistributedCoupon);
                        } else {
                            couponShare = proxyCouponAmount.multiply(itemOriginalTotal)
                                    .divide(proxyTotalOriginalAmount, 2, RoundingMode.HALF_UP);
                            proxyDistributedCoupon = proxyDistributedCoupon.add(couponShare);
                        }
                        BigDecimal itemChangeDiscount = BigDecimal.ZERO;
                        OrderCheckParam.CartChangePrice itemChangePrice = cart.getCartChangePrice();
                        if (itemChangePrice != null) {
                            BigDecimal couponAdjustedSubtotal = itemOriginalTotal.subtract(couponShare);
                            BigDecimal changePriceResult = CartVO.calculateChangePrice(
                                    itemChangePrice.getChangePriceValue(),
                                    itemChangePrice.getChangePriceType(),
                                    couponAdjustedSubtotal);
                            itemChangeDiscount = couponAdjustedSubtotal.subtract(changePriceResult);
                        }
                        orderItem.setPrice(itemOriginalTotal.subtract(couponShare).subtract(itemChangeDiscount)
                                .divide(BigDecimal.valueOf(cart.getQuantity()), 2, RoundingMode.HALF_UP));
                    }
                } else {
                    orderItem.setPrice(cart.getPrice());
                }
                // 防御性兜底：优惠分摊后单价不应为负（如优惠券面值超过商品金额，89-200=-111）
                if (orderItem.getPrice() == null || orderItem.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                    orderItem.setPrice(BigDecimal.ZERO);
                }
                orderItem.setQuantity(cart.getQuantity());
                orderItem.setProductId(cart.getProductId());
                orderItem.setProductName(cart.getProductName());
                orderItem.setProductSn(cart.getProductSn());
                orderItem.setPicThumb(cart.getPicThumb());
                orderItem.setSkuId(cart.getSkuId());
                orderItem.setSkuData(cart.getOriginSkuData().toJSONString(1));
                orderItem.setProductType(cart.getProductType());
                orderItem.setShopId(cart.getShopId());
                String promotionData = cart.getActivityInfo() != null ? JSONUtil.toJsonStr(cart.getActivityInfo()) : "";
                orderItem.setPromotionData(promotionData);
                orderItem.setOriginPrice(cart.getOriginPrice() != null ? cart.getOriginPrice() : BigDecimal.ZERO);
                orderItem.setIsSeckill(cart.getIsSeckill() != null ? cart.getIsSeckill() : 0);
                orderItem.setExtraSkuData(cart.getExtraSkuData() != null ? JSONUtil.toJsonStr(cart.getExtraSkuData()) : "");
                orderItem.setIsGift(0);
                orderItem.setSuppliersId(cart.getSuppliersId() != null ? cart.getSuppliersId() : 0);
                orderItem.setCardGroupName(cart.getProductType() == 3 ? eCardCardService.getById(cart.getCardGroupId()).getGroupName() : "");
                if (cart.getSalesmanId() != null) {
                    salesmanOrder.put(cart.getProductId() + "_" + cart.getSkuId(), cart.getSalesmanId());
                }
                // 为卡密商品订单时分配电子卡券
                if (cart.getProductType() == PRODUCT_TYPE_CARD.getCode()) {
                    eCards = eCardService.getNewCardByGroupId(cart.getCardGroupId(), cart.getQuantity());
                    Assert.isFalse(eCards.size() < cart.getQuantity(), () -> new GlobalException(messageHelper.get("error.card.stock.insufficient")));
                }
                orderItem.setVendorId(cart.getVendorId());
                orderItem.setVendorProductId(cart.getVendorProductId());
                orderItem.setVendorProductSkuId(cart.getVendorProductSkuId());

                // 二期（D1096）：秒杀商品下单人群强校验，防止前台置灰被绕过；member_scope 非空且不匹配 → 拦截，存量活动行为不变
                if (cart.getIsSeckill() != null && cart.getIsSeckill() == 1
                        && !seckillService.canUserJoinSeckill(cart.getProductId(), cart.getSkuId(), userId)) {
                    throw new GlobalException(translatePackage.translate("该商品仅限指定会员购买"));
                }

                String extraData = cart.getExtraData();
                if (StrUtil.isNotBlank(extraData)) {
                    JSONObject entries = JsonUtil.fromJson(extraData, JSONObject.class);
                    if (entries != null && !entries.isEmpty() && entries.containsKey("grouponPromotionId")) {
                        Integer grouponPromotionId = entries.getInt("grouponPromotionId");
                        // 校验拼团活动人群限制（二期 D1096：member_scope 标签/等级人群优先，回退旧 memberRange/memberMarks）
                        Promotion promotion = promotionService.getOne(new LambdaQueryWrapper<Promotion>().eq(Promotion::getRelationId, grouponPromotionId).eq(Promotion::getType, PromotionType.TYPE_GROUPON.getCode()).last("limit 1"));
                        if (promotion != null) {
                            String memberScope = promotion.getMemberScope();
                            if (StrUtil.isNotBlank(memberScope)) {
                                if (!memberAudienceService.matchesScope(Long.valueOf(userId), memberScope)) {
                                    throw new GlobalException(translatePackage.translate("您不在该拼团活动的参与范围内"));
                                }
                            } else if (Objects.equals(1, promotion.getMemberRange())) {
                                String memberMarks = promotion.getMemberMarks();
                                List<Integer> allowedMarks = Optional.ofNullable(JSON.parseArray(memberMarks, Integer.class)).orElse(List.of());
                                if (!allowedMarks.isEmpty() && !allowedMarks.contains(user.getMark())) {
                                    // 存量场景沿用改造前文案
                                    throw new GlobalException(translatePackage.translate("该拼团活动仅限指定会员参与"));
                                }
                            }
                        }

                        // 校验参团限制：不能加入自己开的团，不能重复参团
                        Long grouponRecordId = entries.getLong("grouponPromotionRecordId");
                        if (grouponRecordId != null && grouponRecordId > 0) {
                            // 获取开团记录
                            GrouponPromotionRecord openRecord = grouponPromotionRecordService.getById(grouponRecordId);
                            if (openRecord != null) {
                                // 校验：不能加入自己开的团
                                if (openRecord.getUserId() != null && openRecord.getUserId().equals(Long.valueOf(userId))) {
                                    throw new GlobalException(messageHelper.get("error.groupon.own.team"));
                                }
                                // 校验：不能重复参团（检查是否已经有参团记录）
                                long existingJoinCount = grouponPromotionRecordService.lambdaQuery().eq(GrouponPromotionRecord::getOpenGroupRecordId, grouponRecordId).eq(GrouponPromotionRecord::getUserId, Long.valueOf(userId)).count();
                                if (existingJoinCount > 0) {
                                    throw new GlobalException(messageHelper.get("error.groupon.already.joined"));
                                }
                            }
                        }

                        orderItem.setIsPin(1);
                        orderItem.setPromotionData(cart.getExtraData());
                    }
                    // 处理礼品卡兑换逻辑
                    if (entries != null && !entries.isEmpty() && entries.containsKey("cardNumber")) {
                        ExchangeCardParam exchangeCardParam = JsonUtil.fromJson(extraData, ExchangeCardParam.class);
                        GiftCards card = giftCardsService.lambdaQuery().eq(GiftCards::getCardNumber, exchangeCardParam.getCardNumber()).eq(GiftCards::getCardPassword, exchangeCardParam.getCardPassword()).last("limit 1").one();
                        card.setUsedTime(StringUtils.getCurrentTime());
                        card.setUserId(userId);
                        card.setClaimedUserId(userId);
                        card.setClaimedStatus(GiftCardClaimedEnum.RECEIVED.getCode());
                        card.setStatus(YES);
                        giftCardsService.updateById(card);
                        orderItem.setGiftCardId(card.getGiftCardsId());
                    }
                }
                // O2O 服务费快照推迟到 order 类型/物流方式定型后回填（CR-20260513-153000）
                // design §6：批发行快照（is_wholesale_line / wholesale_grade_id / wholesale_base_price 冻结）
                applyWholesaleSnapshotToOrderItem(orderItem, cart, wholesaleGradeSnapshot);
                orderItemList.add(orderItem);
                defaultVendorId = cart.getVendorId();
            }

            if (!cartByStore.getGift().isEmpty()) {
                for (CartPromotionParsePriceDTO.Gift gift : cartByStore.getGift()) {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setUserId(userId);
                    orderItem.setPrice(BigDecimal.ZERO);
                    orderItem.setQuantity(gift.getNum());
                    orderItem.setProductId(gift.getProductId());
                    orderItem.setProductName(gift.getProductName());
                    orderItem.setProductSn(gift.getProductSn());
                    orderItem.setPicThumb(gift.getPicThumb());
                    orderItem.setSkuId(gift.getSkuId());
                    orderItem.setSkuData(gift.getSkuData().toString());
                    orderItem.setProductType(gift.getProductType());
                    orderItem.setShopId(gift.getShopId());
                    orderItem.setProductType(gift.getProductType());
                    orderItem.setIsGift(1);
                    orderItem.setPromotionData(JSONUtil.toJsonStr(gift));
                    orderItemList.add(orderItem);
                }
            }

        }
        boolean isGiftExchangeReservationOrder = Objects.equals(param.getFlowType(), CartTypeEnum.TYPE_GIFT_EXCHANGE.getCode())
                && orderItemList.stream().anyMatch(item -> item.getProductType() != null
                && ProductType.isReservationProduct(item.getProductType()));
        boolean isGiftExchangeReservationHome = isGiftExchangeReservationOrder
                && orderItemList.stream().anyMatch(item -> Objects.equals(item.getProductType(), PRODUCT_TYPE_RESERVATION_STORE.getCode()));
        boolean isGiftExchangeReservationStore = isGiftExchangeReservationOrder
                && orderItemList.stream().anyMatch(item -> Objects.equals(item.getProductType(), PRODUCT_TYPE_RESERVATION_HOME.getCode()));

        UserAddressVO address = null;
        boolean isCardOrderForAddress = false;
        if (isCardOrderForAddress) {
            if (param.getAddressId() != null) {
                address = userAddressService.detail(param.getAddressId());
            }
        } else if (tigshopProperties.getIsO2o() == 1) {
            if (PosCheckoutContext.isPosCheckout(param)
                    || (param.getFlowType() != null && (param.getFlowType().equals(CartTypeEnum.TYPE_RESERVATION_HOME.getCode())
                    || param.getFlowType().equals(CartTypeEnum.TYPE_RESERVATION_STORE.getCode())))
                    || isGiftExchangeReservationOrder) {
                // ignore
                if (Objects.equals(param.getFlowType(), CartTypeEnum.TYPE_RESERVATION_HOME.getCode())
                        || isGiftExchangeReservationHome) {
                    if (param.getAddressId() != null) {
                        address = userAddressService.detail(param.getAddressId());
                    } else if (param.getAddress() != null) {
                        address = new UserAddressVO();
                        BeanUtil.copyProperties(param.getAddress(), address);
                        address.setRegionIds(param.getAddress().getRegionIds());
                    } else {
                        address = userAddressService.getUserSelectedAddress();
                    }
                }
            } else if (CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode().equals(param.getFlowType())) {
                if (Objects.equals(param.getDeliveryType(), OrderDeliveryTypeEnum.LOGISTICS.getCode())
                        || Objects.equals(param.getDeliveryType(), OrderDeliveryTypeEnum.SAME_CITY.getCode())) {
                    Integer addressId = param.getAddressId();
                    if (addressId != null) {
                        address = userAddressService.detail(addressId);
                    } else {
                        address = userAddressService.getUserSystemSelectedAddress(userId);
                    }
                    Assert.notNull(address != null ? address.getAddressId() : null, () -> new GlobalException("请选择收货地址"));
                }
            } else {
                // deliveryType 1:物流 2:自提 3:同城配送
                if (Objects.equals(param.getDeliveryType(), OrderDeliveryTypeEnum.LOGISTICS.getCode())
                        || Objects.equals(param.getDeliveryType(), OrderDeliveryTypeEnum.SAME_CITY.getCode())) {
                    address = userAddressService.getUserSelectedAddress();
                    Assert.notNull(address.getAddressId(), () -> new GlobalException("请选择收货地址"));
                }
            }
        } else {
            if (CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode().equals(param.getFlowType())) {
                Integer addressId = param.getAddressId();
                if (addressId != null) {
                    address = userAddressService.detail(addressId);
                } else {
                    address = userAddressService.getUserSystemSelectedAddress(userId);
                    if (address == null) {
                        address = userAddressService.getUserSelectedAddress();
                    }
                }
                Assert.notNull(address.getAddressId(), () -> new GlobalException("请选择收货地址"));
            } else {
                address = userAddressService.getUserSelectedAddress();
                Assert.notNull(address.getAddressId(), () -> new GlobalException("请选择收货地址"));
            }
        }



        Order order = new Order();
        order.setOrderSn(creatNewOrderSn());
        order.setUserId(userId);
        order.setOrderStatus(OrderStatusEnum.PENDING.getCode());
        order.setShippingStatus(ShippingStatusEnum.PENDING.getCode());
        order.setPayStatus(PaymentStatus.UNPAID.getCode());
        order.setAddTime(StringUtils.getCurrentTime());
        order.setTaxRate(totalFee.getTaxRate());
        order.setTaxRateAmount(totalFee.getTaxRateAmount());

        if (tigshopProperties.getIsO2o() == 1) {
            if (Objects.equals(param.getDeliveryType(), OrderDeliveryTypeEnum.PICKUP.getCode())) {
                log.info("自提商品订单，不设置收货地址");
            } else {
                order.setConsignee(address != null ? address.getConsignee() : "");
                order.setRegionIds(address != null ? JsonUtil.toJson(address.getRegionIds()) : "");
                order.setRegionNames(address != null ? JsonUtil.toJson(address.getRegionNames()) : "");
                order.setAddress(address != null ? address.getAddress() : "");
                order.setAddressData(address != null ? JSONUtil.toJsonStr(address) : "");
                order.setMobile(address != null ? address.getMobile() : "");
                order.setEmail(address != null ? address.getEmail() : "");
            }
        } else {
            order.setConsignee(address != null ? address.getConsignee() : "");
            order.setRegionIds(address != null ? JsonUtil.toJson(address.getRegionIds()) : "");
            order.setRegionNames(address != null ? JsonUtil.toJson(address.getRegionNames()) : "");
            order.setAddress(address != null ? address.getAddress() : "");
            order.setAddressData(address != null ? JSONUtil.toJsonStr(address) : "");
            order.setMobile(address != null ? address.getMobile() : "");
            order.setEmail(address != null ? address.getEmail() : "");
        }

        order.setPosUserId(param.getPosUserId());
        order.setBuyerNote(param.getBuyerNote());
        order.setPayTypeId(param.getPayTypeId());
        order.setUsePoints(param.getUsePoint());
        order.setReferrerUserId(userService.getUserReferrerId(userId));
        order.setShopId(0);
        order.setIsStoreSplited(0);

        BigDecimal totalAmount = totalFee.getTotalAmount();
        order.setTotalAmount(totalAmount);

        BigDecimal useBalance = Optional.ofNullable(param.getUseBalance()).orElse(BigDecimal.ZERO);
        // 是否现金
        boolean isCash = PaymentType.CASH.getCode() == payTypeId;
        // 是否余额
        boolean isUseBalance = useBalance.compareTo(BigDecimal.ZERO) > 0;
        // 是否积分
        boolean isUsePoint = Optional.ofNullable(param.getUsePoint()).orElse(0) > 0;
        // 是否使用账期支付
        boolean isUseCredit = param.getUseCredit() != null && param.getUseCredit() == 1;

        // 账期支付与余额支付互斥校验
        if (isUseCredit && isUseBalance) {
            throw new GlobalException(messageHelper.get("error.credit.balance.conflict"));
        }
        // 仅现金
//        if (isCash && !isUseBalance && !isUsePoint) {
//            if (totalAmount.compareTo(param.getCashAmount()) > 0) {
//                throw new GlobalException(translatePackage.translate("支付金额不能小于订单金额"));
//            }
//        }

        //将改价元数据存入到订单表
        CartGlobalChangePrice cartGlobalChangePrice = cartList.getCartGlobalChangePrice();
        if (Objects.nonNull(cartGlobalChangePrice)) {
            order.setChangePriceData(JSONUtil.toJsonStr(cartGlobalChangePrice));
        }

        order.setPaidAmount(totalFee.getPaidAmount());
        order.setUnpaidAmount(totalFee.getUnpaidAmount());
        order.setProductAmount(totalFee.getProductAmount());
        order.setDiscountChangePrice(totalFee.getDiscountChangePrice());
        order.setCouponAmount(totalFee.getCouponAmount());
        order.setPointsAmount(totalFee.getPointsAmount());
        order.setGiftCardAmount(totalFee.getGiftCardAmount());
        order.setBalance(totalFee.getBalance());
        order.setCreditAmount(totalFee.getCreditAmount() == null ? BigDecimal.ZERO : totalFee.getCreditAmount());
        order.setIsCreditOrder(0);
        order.setCashAmount(totalFee.getCashAmount());
        order.setCashChangeAmount(totalFee.getCashChangeAmount());
        order.setServiceFee(totalFee.getServiceFee());
        order.setShippingFee(totalFee.getShippingFee());
        order.setInvoiceFee(BigDecimal.ZERO);
        order.setDiscountAmount(totalFee.getDiscountAmount());
        order.setRoundOffDimeAmount(totalFee.getRoundOffDimeAmount());
        order.setRoundOffCentsAmount(totalFee.getRoundOffCentsAmount());
        order.setOrderExtension(new JSONObject().toString());
        order.setOnlinePaidAmount(BigDecimal.ZERO);
        order.setBalance(totalFee.getBalance());
        order.setOfflinePaidAmount(BigDecimal.ZERO);
        order.setOrderSource(HeaderUtils.getClientType());
        if (PosCheckoutContext.isPosCheckout(param)) {
            order.setOrderSource(AdminTypeEnum.POS.getCode());
        }
        // 支持自定义订单来源（代客下单等场景）
        if (StrUtil.isNotBlank(param.getOrderSource())) {
            order.setOrderSource(param.getOrderSource());
        }
        order.setInvoiceData(param.getInvoiceData() != null && param.getInvoiceData().getInvoiceType() != null ? JSONUtil.toJsonStr(param.getInvoiceData()) : "");
        order.setIsExchangeOrder(totalFee.getExchangePoints() > 0 ? 1 : 0);

        //进行处理,代客下单
        if (CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode().equals(param.getFlowType())) {
            if (orderItemList.size() > 1) {
                //说明是普通商品
                order.setOrderType(CartTypeEnum.TYPE_NORMAL.getCode());
            } else {
                OrderItem first = orderItemList.getFirst();
                Integer productType = first.getProductType();
                var cartType = switch (productType) {
                    case 1 -> CartTypeEnum.TYPE_NORMAL.getCode();
                    case 2 -> CartTypeEnum.TYPE_VIRTUAL.getCode();
                    case 3 -> CartTypeEnum.TYPE_CARD.getCode();
                    case 4 -> CartTypeEnum.TYPE_PAID.getCode();
                    case 5 -> CartTypeEnum.TYPE_RESERVATION_HOME.getCode();
                    case 6 -> CartTypeEnum.TYPE_RESERVATION_STORE.getCode();
                    case 7, 8 -> CartTypeEnum.TYPE_GIFT_CARD.getCode();
                    default -> throw new GlobalException("商品类型错误");
                };
                order.setOrderType(cartType);
            }
        } else {
            order.setOrderType(param.getFlowType());
        }

        order.setVendorId(defaultVendorId);
        order.setIsStoreSplited(0);

        if (cartList.getCartList().size() == 1) {
            // 所有商品都是来自同一店铺时
//
            order.setShopId(cartList.getCartList().getFirst().getShopId());

            if (Objects.equals(param.getDeliveryType(), OrderDeliveryTypeEnum.SAME_CITY.getCode())) {
                order.setShippingTypeId(param.getDeliveryType());
                order.setShippingTypeName("同城配送");
            } else if (Objects.equals(param.getDeliveryType(), OrderDeliveryTypeEnum.PICKUP.getCode())) {
                order.setShippingTypeId(param.getDeliveryType());
                order.setShippingTypeName("到店自提");
            } else {
                OrderCheckParam.ShippingType shippingType = null;
                if (param.getShippingType() != null) {
                    shippingType = param.getShippingType().get(order.getShopId());
                }
                if (shippingType != null) {
                    order.setShippingTypeId(shippingType.getTypeId());
                    order.setShippingTypeName(shippingType.getTypeName());
                    if (tigshopProperties.getIsOverseas() == 1) {
                        Integer typeId = shippingType.getTypeId();
                        if (Integer.valueOf(1).equals(typeId)) {
                            order.setShippingTypeName(configService.getConfigVal(SettingsEnum.DEFAULT_LOGISTICS_NAME));
                        }
                    }
                } else {
                    order.setShippingTypeId(param.getDeliveryType());
                    order.setShippingTypeName(OrderDeliveryTypeEnum.getDesc(param.getDeliveryType()));
                }
            }

        } else {
            // 多个门店的情况：设置配送方式到主订单
            if (Objects.equals(param.getDeliveryType(), OrderDeliveryTypeEnum.SAME_CITY.getCode())) {
                order.setShippingTypeId(param.getDeliveryType());
                order.setShippingTypeName("同城配送");
            } else {
                order.setShippingTypeId(param.getDeliveryType());
                order.setShippingTypeName(OrderDeliveryTypeEnum.getDesc(param.getDeliveryType()));
            }
            // 将每个店铺的配送方式信息写入orderExtension，供拆单时使用
            JSONObject extensionData = JSONUtil.parseObj(order.getOrderExtension());
            JSONObject shippingTypeData = new JSONObject();
            for (CartByStoreVO cartByStore : cartList.getCartList()) {
                Integer shopId = cartByStore.getShopId();
                JSONObject shopShippingType = new JSONObject();
                if (Objects.equals(param.getDeliveryType(), OrderDeliveryTypeEnum.SAME_CITY.getCode())) {
                    shopShippingType.put("type_id", param.getDeliveryType());
                    shopShippingType.put("type_name", "同城配送");
                } else if (Objects.equals(param.getDeliveryType(), OrderDeliveryTypeEnum.PICKUP.getCode())) {
                    shopShippingType.put("type_id", param.getDeliveryType());
                    shopShippingType.put("type_name", "到店自提");
                } else if (param.getShippingType() != null && param.getShippingType().get(shopId) != null) {
                    OrderCheckParam.ShippingType st = param.getShippingType().get(shopId);
                    shopShippingType.put("type_id", st.getTypeId());
                    shopShippingType.put("type_name", st.getTypeName());
                }
                shippingTypeData.put(String.valueOf(shopId), shopShippingType);
            }
            extensionData.put("shipping_type", shippingTypeData);
            order.setOrderExtension(extensionData.toString());
        }
        while (orderService.count(new LambdaQueryWrapper<Order>().eq(Order::getOrderSn, order.getOrderSn())) > 0) {
            order.setOrderSn(creatNewOrderSn());
        }

        try {
            if (ObjectUtil.isEmpty(order.getAddress())) {
                order.setAddress("");
            }
            if (ObjectUtil.isEmpty(order.getRegionIds())) {
                order.setRegionIds("");
            }
            if (ObjectUtil.isEmpty(order.getRegionNames())) {
                order.setRegionNames("");
            }
            boolean isPin = orderItemList.stream().anyMatch(orderItem -> orderItem.getIsPin() != null && orderItem.getIsPin().equals(1));
            if (isPin) {
                order.setOrderType(OrderType.PIN.getCode());
            }


            // 礼品卡商品订单 / 抽奖兑换流程：如果只有一个商品且商品类型为8(电子礼品卡)，设置为无需配送
            boolean isGiftCardFlow = Objects.equals(order.getOrderType(), OrderType.GIFT_CARD.getCode());
            boolean isLotteryFlow = Objects.equals(order.getOrderType(), CartTypeEnum.TYPE_LOTTERY_PRODUCT.getCode());
            if ((isGiftCardFlow || isLotteryFlow) && orderItemList.size() == 1) {
                OrderItem singleOrderItem = orderItemList.getFirst();
                Product product = productService.getById(singleOrderItem.getProductId());
                if (product != null && Objects.equals(product.getProductType(), ProductType.PRODUCT_TYPE_GIFT_CARD_VIRTUAL.getCode())) {
                    order.setShippingMethod(3);
                }
            }

            if (Objects.equals(param.getDeliveryType(), OrderDeliveryTypeEnum.PICKUP.getCode())) {
                order.setShippingMethod(2);
            }

            boolean save = orderService.save(order);
            Assert.isTrue(save, () -> new GlobalException(messageHelper.get("error.order.create.failed")));
            if (param.getUsePoint() != null && param.getUsePoint() > 0) {
                String configVal = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
                userService.decPoints(param.getUsePoint(), userId, "订单支付扣除" + (StrUtil.isNotBlank(configVal) ? configVal : "积分"));
            }
            if (param.getUseBalance() != null && param.getUseBalance().compareTo(BigDecimal.ZERO) > 0) {
                userService.decBalance(order.getBalance(), userId, "订单支付");
            }

            //清除购物车相关数据(包含全局改价)
            if (!cartIds.isEmpty()) {
                cartService.removeBatchByIds(cartIds, true);
            }
            if (Objects.nonNull(cartGlobalChangePrice)) {
                cartGlobalChangePriceService.removeById(userId);
            }

            if (param.getUseCouponIds() != null && !param.getUseCouponIds().isEmpty()) {
                userCouponService.useCoupon(param.getUseCouponIds(), param.getSelectUserCouponIds(), userId, order.getOrderId());
            }

            Integer cycleStockProductId = null;
            boolean isCycleMainOrder = false;
            boolean isReservationCycleMainOrder = false;
            boolean isCardMainOrder = false;
            boolean isReservationCardMainOrder = false;
            int cycleRequiredStock = 0;

            //循环orderItems
            for (OrderItem orderItem : orderItemList) {
                orderItem.setOrderId(order.getOrderId());
                orderItem.setOrderSn(order.getOrderSn());

                // 仅在“扣库存”这一步按周期购总量预扣，后续逻辑（销量、限购、秒杀等）仍使用订单原始数量
                Integer originalQty = orderItem.getQuantity();
                int deductQty = originalQty != null ? originalQty : 0;
                boolean shouldDeductCycleStock = ((isCycleMainOrder && !isReservationCycleMainOrder) || (isCardMainOrder && !isReservationCardMainOrder))
                        && cycleRequiredStock > 0 && cycleStockProductId != null && Objects.equals(orderItem.getProductId(), cycleStockProductId);
                if (shouldDeductCycleStock) {
                    deductQty = cycleRequiredStock;
                    orderItem.setQuantity(deductQty);
                }
                try {
                    {
                        boolean isReservationProduct = orderItem.getProductType() == PRODUCT_TYPE_RESERVATION_HOME.getCode()
                                || orderItem.getProductType() == PRODUCT_TYPE_RESERVATION_STORE.getCode();
                        if (isReservationProduct && isCardMainOrder) {
                            // reservation card order: no stock deduction at purchase, deduct calendar stock at redeem
                        }
                        else if (orderItem.getSkuId() > 0) {
                            handleSkuStock(orderItem);
                        } else {
                            handleProductStock(orderItem, param);
                        }
                    }
                } finally {
                    if (shouldDeductCycleStock) {
                        orderItem.setQuantity(originalQty);
                    }
                }
                // O2O 模式下增加门店销量

                if (orderItem.getIsPin() != null && orderItem.getIsPin().equals(1)) {
                    AddToCartDTO.GrouponData grouponData = JSON.parseObject(orderItem.getPromotionData(), AddToCartDTO.GrouponData.class);
                    grouponPromotionDetailService.decLimitNum(orderItem.getQuantity(), grouponData.getGrouponDetailId());
                }

                //增加秒杀销量
                if (orderItem.getIsSeckill() != null && orderItem.getIsSeckill() == 1) {
                    seckillService.incSales(orderItem.getProductId(), orderItem.getSkuId(), orderItem.getQuantity());
                }
                //减少秒杀库存
                if (orderItem.getIsSeckill() != null && orderItem.getIsSeckill() == 1) {
                    try {
                        seckillService.decStock(orderItem.getProductId(), orderItem.getSkuId(), orderItem.getQuantity());
                    } catch (Exception e) {
                        throw new GlobalException(messageHelper.get("error.seckill.stock.insufficient"));
                    }
                }
                //增加商品销量
                productService.incSales(orderItem.getProductId(), orderItem.getQuantity());
                //减少赠品库存
                if (orderItem.getIsGift() == 1) {
                    CartPromotionParsePriceDTO.Gift gift = JSONUtil.toBean(orderItem.getPromotionData(), CartPromotionParsePriceDTO.Gift.class);
                    productGiftService.decStock(gift.getGiftId(), orderItem.getQuantity());
                }

                //如果是礼品卡则减礼品卡组可用库存
                Product product = productService.getById(orderItem.getProductId());
                if (product.getGiftCardGroupsId() != null && product.getGiftCardGroupsId() > 0) {
                    GiftCardGroups giftCardGroups = giftCardGroupsMapper.selectById(product.getGiftCardGroupsId());
                    if (giftCardGroups != null) {
                        int currentStock = giftCardGroups.getAvailabilityStock() - orderItem.getQuantity();
                        if (currentStock < 0) {
                            throw new GlobalException(messageHelper.get("error.gift.card.group.stock.insufficient"));
                        }
                        if (product.getProductStock() < orderItem.getQuantity()) {
                            throw new GlobalException(messageHelper.get("error.product.stock.shortage", product.getProductName()));
                        }
                        giftCardGroups.setAvailabilityStock(currentStock);
                        giftCardGroupsMapper.updateById(giftCardGroups);
                    }
                }

                //商品库存预警 --- 按主体开关与商品/SKU 配置的库存上下限判定
                stockWarningService.evaluate(orderItem.getShopId(), orderItem.getProductId(), orderItem.getSkuId());

                //商品无货 --- 发送后台信息
                int productStock = productSkuService.getProductStock(orderItem.getProductId(), orderItem.getSkuId());
                if (productStock <= 0) {
                    AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
                    adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.OUT_OF_STOCK.getCatId());
                    adminMsgCreateDTO.setTitle("商品无货:" + orderItem.getProductName());
                    adminMsgCreateDTO.setContent("您的商品【" + orderItem.getProductName() + "】库存已经无货，请及时补充库存!");
                    adminMsgCreateDTO.setShopId(orderItem.getShopId());
                    adminMsgCreateDTO.setProductId(orderItem.getProductId());
                    Map<String, Object> relatedData = new HashMap<>();
                    relatedData.put("productId", orderItem.getProductId());
                    adminMsgCreateDTO.setRelatedData(relatedData);
                    adminMsgService.createMessage(adminMsgCreateDTO);
                }

            }
            orderItemService.saveBatch(orderItemList);


            // ========== 账期支付处理 ==========
            boolean isUseCreditPayment = param.getUseCredit() != null && param.getUseCredit() == 1;
            if (isUseCreditPayment) {
                try {
                    BigDecimal creditAmount = order.getTotalAmount();

                    // 基于最新账期规则校验可用性（含：上期未结清、授信禁用、额度不足）
                    UserCreditInfoVO creditInfo = creditUserService.getUserCreditInfo(userId, creditAmount);
                    if (creditInfo == null || !Boolean.TRUE.equals(creditInfo.getCanUseCredit())) {
                        String reason = creditInfo != null && StrUtil.isNotBlank(creditInfo.getDisableReason())
                                ? creditInfo.getDisableReason()
                                : "您不是授信会员，无法使用账期支付";
                        throw new GlobalException(reason);
                    }

                    // 获取授信会员信息
                    CreditUser creditUser = creditUserService.getByUserId(userId);
                    if (creditUser == null) {
                        throw new GlobalException("您不是授信会员，无法使用账期支付");
                    }

                    // 扣减账期额度
                    creditUserService.deductAmount(userId, creditAmount, "订单支付，订单号：" + order.getOrderSn());

                    // 创建账期订单记录
                    creditOrderService.createCreditOrder(order.getOrderId(), order.getOrderSn(), userId, creditUser.getCreditUserId(), creditAmount);

                    // 设置订单账期支付相关字段
                    order.setCreditAmount(creditAmount);
                    order.setIsCreditOrder(1);

                    log.info("账期支付成功, orderId={}, orderSn={}, creditAmount={}", order.getOrderId(), order.getOrderSn(), creditAmount);
                } catch (GlobalException e) {
                    throw e;
                } catch (Exception e) {
                    log.error("账期支付处理失败, orderId={}", order.getOrderId(), e);
                    throw new GlobalException("账期支付处理失败，请重试");
                }
            }
            // ========== 账期支付处理结束 ==========

            boolean wechatBizMode = false;
            wechatBizMode = DistributionModeEnum.isWechatBiz(
                    salesmanConfigService.detail("salesmanConfig").getDistributionMode());
            if (wechatBizMode) {
                // 微商模式：下单时仅按需自动开通下级（建立买家上级绑定），佣金记录在支付成功后生成
                wechatBizRecruitService.autoRecruitOnOrderSubmit(userId, salesmanOrder.values());
            }
            if (!wechatBizMode) {
                salesmanOrderService.addSalesmanOrder(orderItemList, salesmanOrder);
            }

            for (OrderItem item : orderItemList) {
                //封装卡密信息
                if (!eCards.isEmpty() && item.getProductType() == 3) {
                    for (ECard eCard : eCards) {
                        eCard.setIsUse(1);
                        eCard.setOrderId(order.getOrderId());
                        eCard.setOrderItemId(item.getItemId());
                    }
                }
            }

            // 更新抽奖记录状态（如果是抽奖商品订单）
            if (lotteryRecord != null) {
                lotteryRecord.setStatus(1); // 已领取
                lotteryRecord.setReceiveTime(System.currentTimeMillis() / 1000);
                lotteryRecordService.updateById(lotteryRecord);
                log.info("抽奖奖品领取成功，抽奖记录ID:{}，订单ID:{}", lotteryRecord.getRecordId(), order.getOrderId());
            }

            if (!orderAmountList.isEmpty()) {
                for (OrderAmountDetail orderAmountDetail : orderAmountList) {
                    orderAmountDetail.setOrderId(order.getOrderId());
                }
                orderAmountDetailService.saveBatch(orderAmountList);
            }
            if (!orderCouponDetailList.isEmpty()) {
                for (OrderCouponDetail orderCouponDetail : orderCouponDetailList) {
                    orderCouponDetail.setOrderId(order.getOrderId());
                }
                orderCouponDetailService.saveBatch(orderCouponDetailList);
            }

            // 提交时拆单：多店铺订单立即按 shopId × vendorId 生成子单
            if (cartList.getCartList().size() > 1) {
                orderService.splitOrderAtSubmit(order.getOrderId());
                // 同步本地拆分标记，避免后续 updateOrderMoney→updateById 用过期对象把 isStoreSplited 覆盖回 0，
                // 进而导致 setOrderPaid→splitStoreOrder 误判为未拆单而重复拆单（余额全额抵扣场景）
                order.setIsStoreSplited(1);
            }

            // 拆单完成后再记录提交日志：此时子单已存在，syncParentLogToChildren 会把该日志同步到各子单
            if (CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode().equals(param.getFlowType())) {
                orderService.addLog(order.getOrderId(), "管理员代客下单");
            } else {
                orderService.addLog(order.getOrderId(), "会员提交订单");
            }

            if (param.getInvoiceData() != null && param.getInvoiceData().getInvoiceType() != null) {
                OrderInvoiceSaveParam orderInvoiceSaveParam = new OrderInvoiceSaveParam();
                BeanUtils.copyProperties(param.getInvoiceData(), orderInvoiceSaveParam);
                orderInvoiceSaveParam.setUserId(user.getUserId());
                orderInvoiceSaveParam.setOrderId(order.getOrderId());
                orderInvoiceSaveParam.setStatus(0);
                orderInvoiceSaveParam.setAmount(order.getTotalAmount());
                if (param.getInvoiceData().getInvoiceType() == 2) {
                    orderInvoiceSaveParam.setTitleType(2);
                }
                orderInvoiceSaveParam.setAddTime(StringUtils.getCurrentTime());
                orderInvoiceService.create(orderInvoiceSaveParam);
                OrderInvoice orderInvoice = orderInvoiceService.lambdaQuery().eq(OrderInvoice::getOrderId, order.getOrderId()).one();
                order.setInvoiceData(JSONUtil.toJsonStr(orderInvoice));
                orderService.updateById(order);

                AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
                adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.INVOICE_REQUEST.getCatId());
                adminMsgCreateDTO.setTitle("您有新的发票申请,申请金额：" + order.getTotalAmount() + "发票抬头:" + orderInvoice.getCompanyName());
                adminMsgCreateDTO.setContent("用户【" + user.getUsername() + "】针对订单【" + order.getOrderSn() + "】提交了发票申请");
                adminMsgCreateDTO.setShopId(0);
                adminMsgCreateDTO.setOrderId(order.getOrderId());
                Map<String, Object> relatedData = new HashMap<>();
                relatedData.put("orderId", order.getOrderId());
                relatedData.put("orderInvoiceId", orderInvoice.getId());
                adminMsgCreateDTO.setRelatedData(relatedData);
                adminMsgService.createMessage(adminMsgCreateDTO);
            }
            if (order.getUnpaidAmount().compareTo(BigDecimal.ZERO) <= 0) {
                orderService.updateOrderMoney(order, true);

                if (!eCards.isEmpty()) {
                    eCardService.updateBatchById(eCards);
                }
            }

            //加入自动取消队列
            if (order.getUnpaidAmount().compareTo(BigDecimal.ZERO) > 0 && order.getPayTypeId() != PaymentType.OFFLINE.getCode()) {
                ConfigPO autoCancelOrderMinuteConfig = configService.getConfigByCode(SettingsEnum.AUTO_CANCEL_ORDER_MINUTE.getBizCode());
                int autoCancelOrderMinute = Integer.parseInt(autoCancelOrderMinuteConfig.getBizVal());
                if (autoCancelOrderMinute > 0) {
                    rabbitTemplate.convertAndSend(RabbitMQConfig.DELAY_EXCHANGE, RabbitMQConfig.ORDER_CANCEL_ROUTING_KEY, order.getOrderId(), message -> {
                        message.getMessageProperties().setHeader("x-delay", autoCancelOrderMinute * 60000);
                        return message;
                    });
                }
            }
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            log.error("订单提交失败", e);
            throw new GlobalException(messageHelper.get("error.order.submit.failed"));
        }
        OrderSubmitVO orderSubmitVO = new OrderSubmitVO();
        orderSubmitVO.setOrderId(order.getOrderId());
        orderSubmitVO.setReturnType(order.getUnpaidAmount().compareTo(BigDecimal.ZERO) > 0 ? 1 : 2);
        // 仅拼团单需要回传 grouponPromotionRecordId；拆单父单一定不是拼团单。
        // 直接按订单类型查拼团记录，避免调用 detail() 命中"订单已被拆分"(5004)校验把提交流程打断。
        if (Objects.equals(OrderType.PIN.getCode(), order.getOrderType())) {
            GrouponPromotionRecord grouponRecord = grouponPromotionRecordService.lambdaQuery()
                    .eq(GrouponPromotionRecord::getOrderId, order.getOrderId())
                    .one();
            if (grouponRecord != null) {
                orderSubmitVO.setGrouponPromotionRecordId(grouponRecord.getGrouponPromotionRecordId());
            }
        }

        // 添加当前订单的费率
        recordRateService.saveRecordRate(order.getOrderId(), StatementType.ORDER.getCode(), order.getShopId() != null ? order.getShopId() : 0, order.getVendorId() != null ? order.getVendorId() : 0);


        boolean isCardOrderForReservation = false;

        // 处理预售订单创建
        handlePresaleOrder(order, cartList, orderSubmitVO);

        if (order.getUnpaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            // 通知失败不应影响下单主流程
            // 多店铺拆单：父单 + 各店铺/供应商子单各发一条 NEW_ORDER，使子单店铺在消息中心收到新订单消息
            sendNewOrderMsgWithChildren(order);
            try {
                messageService.sendUserMessage(order.getUserId(), order.getOrderId(), MessageTypeEnum.NEW_ORDER_SHOP);
            } catch (Exception ex) {
                log.error("新订单通知 NEW_ORDER_SHOP 发送失败，不影响主流程，orderId={}: {}", order.getOrderId(), ex.getMessage(), ex);
            }
        } else {
            // 提交即付清（余额/优惠券/礼品卡/账期等全额抵扣，未付金额=0）的订单不会进入上方分支；
            // 周期购/次卡主单已在“提交即完成”分支各自补发过 NEW_ORDER，此处仅为其余即付清订单补发，
            // 保证后台新订单消息(msgType=11)与普通订单一致；已付款消息(12)由 setOrderPaid 触发，无需重复
            boolean cycleOrCardInstantPaid =
                    (order.getIsCycleOrder() != null && order.getIsCycleOrder() == 1)
                            || (order.getIsCardOrder() != null && order.getIsCardOrder() == 1);
            if (!cycleOrCardInstantPaid) {
                // 多店铺拆单：父单 + 各店铺/供应商子单各发一条 NEW_ORDER，使子单店铺在消息中心收到新订单消息
                sendNewOrderMsgWithChildren(order);
                try {
                    messageService.sendUserMessage(order.getUserId(), order.getOrderId(), MessageTypeEnum.NEW_ORDER_SHOP);
                } catch (Exception ex) {
                    log.error("新订单通知 NEW_ORDER_SHOP 发送失败，不影响主流程，orderId={}: {}", order.getOrderId(), ex.getMessage(), ex);
                }
            }
        }

        orderSubmitVO.setBalance(Optional.ofNullable(order.getBalance()).orElse(BigDecimal.ZERO));

        orderSubmitVO.setOrderSn(order.getOrderSn());
        return orderSubmitVO;
    }

    /**
     * 发送新订单后台消息(admin_msg, msgType=11)：遍历"父单 + 提交时拆出的店铺/供应商子单"逐个发送，
     * 使各店铺/供应商在消息中心能收到自己子单的新订单消息（多店铺拆单场景）。
     * <p>
     * 多店铺订单提交时已拆单：父单 shopId=0，子单各带真实 shopId/vendorId；只发父单会导致店铺/供应商收不到。
     * 单店铺订单无子单，targets 只含自身，行为与改造前一致。
     * <p>
     * 仅遍历 NEW_ORDER；NEW_ORDER_SHOP（统一店铺手机号短信）维持只发父单，避免重复短信。
     * 提交瞬间 parentOrderId 子单只可能是刚拆出的店铺子单（周期购期次子单此时尚未生成），故无需额外过滤。
     */
    private void sendNewOrderMsgWithChildren(Order order) {
        List<Order> targets = orderService.lambdaQuery()
                .eq(Order::getOrderId, order.getOrderId())
                .or()
                .eq(Order::getParentOrderId, order.getOrderId())
                .list();
        for (Order o : targets) {
            try {
                messageService.sendUserMessage(o.getUserId(), o.getOrderId(), MessageTypeEnum.NEW_ORDER);
            } catch (Exception ex) {
                log.error("新订单通知 NEW_ORDER 发送失败，不影响主流程，orderId={}: {}", o.getOrderId(), ex.getMessage(), ex);
            }
        }
    }


    /**
     * 处理 SKU 商品库存扣减
     */
    private void handleSkuStock(OrderItem orderItem) {
        if (tigshopProperties.getIsO2o() != 1) {
            decStockSkuAndProduct(orderItem);
            return;
        }
    }

    /**
     * 处理普通商品库存扣减
     */
    private void handleProductStock(OrderItem orderItem, OrderCheckParam param) {
        // 卡券商品不用减库存
        if (orderItem.getProductType() == PRODUCT_TYPE_CARD.getCode()) {
            return;
        }

        if (tigshopProperties.getIsO2o() != 1) {
            decStockProductOnly(orderItem);
            return;
        }
    }


    /**
     * 扣减 SKU + 商品库存
     */
    private void decStockSkuAndProduct(OrderItem orderItem) {
        productSkuService.decStock(orderItem.getSkuId(), orderItem.getQuantity(), null);
        productService.decStock(orderItem.getProductId(), orderItem.getQuantity(), null, orderItem.getSkuId() != null && orderItem.getSkuId() > 0 ? false : true);
    }

    /**
     * 扣减 商品库存
     */
    private void decStockProductOnly(OrderItem orderItem) {
        productService.decStock(orderItem.getProductId(), orderItem.getQuantity(), null);
    }

    /**
     * 增加门店销量
     */
    private void increaseShopSales(OrderItem orderItem) {
        Shop shop = shopService.getById(orderItem.getShopId());
        if (shop != null) {
            shop.setShopSales(shop.getShopSales() + orderItem.getQuantity());
            shopService.updateById(shop);
        }

    }

    private String creatNewOrderSn() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        String formattedDate = LocalDateTime.now().format(formatter);
        Random random = new Random();
        int randomNumber = 10000000 + random.nextInt(90000000);
        return formattedDate + randomNumber;
    }

    //计算可用积分
    private Integer getOrderAvailablePoints(CartListByStoreVO cartList, User user) {
        // design §6：批发行金额排除出积分抵扣基数（按行过滤，非整单 0）
        BigDecimal wholesaleAmount = computeWholesaleAmount(cartList);
        if (wholesaleAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal base = Boolean.TRUE.equals(cartList.getHasChangePrice())
                    ? cartList.getTotal().getDiscountAfter()
                    : cartList.getTotal().getProductAmount();
            if (base == null) {
                base = BigDecimal.ZERO;
            }
            BigDecimal adjustedBase = base.subtract(wholesaleAmount).max(BigDecimal.ZERO);
            Integer availablePoints = getAmountValuePoints(adjustedBase);
            Integer amountMaxValuePoints = getAmountMaxValuePoints(adjustedBase);
            availablePoints = Math.min(availablePoints, amountMaxValuePoints);
            return Math.min(availablePoints, getUserPoints(user));
        }
        if (Boolean.TRUE.equals(cartList.getHasChangePrice())) {
            Integer availablePoints = getAmountValuePoints(cartList.getTotal().getDiscountAfter());
            Integer amountMaxValuePoints = getAmountMaxValuePoints(cartList.getTotal().getDiscountAfter());
            availablePoints = Math.min(availablePoints, amountMaxValuePoints);
            return Math.min(availablePoints, getUserPoints(user));
        } else {
            Integer availablePoints = getAmountValuePoints(cartList.getTotal().getProductAmount());
            Integer amountMaxValuePoints = getAmountMaxValuePoints(cartList.getTotal().getProductAmount());
            availablePoints = Math.min(availablePoints, amountMaxValuePoints);
            return Math.min(availablePoints, getUserPoints(user));
        }
    }

    // ============================================================
    // B2B 批发壳辅助方法（T-401 / design §6 §12）
    // ============================================================
    // 包级可见，便于单测直接覆盖；勿在 common.wholesale 之外调用。

    /**
     * 计算购物车批发作用域行的金额合计（仅 isChecked + wholesaleScope=true 行；含命中阶梯价行与未命中按原始零售价兜底行）。
     *
     * <p>用于积分抵扣基数排除（getOrderAvailablePoints）。
     *
     * @return 批发金额合计；空购物车或无批发行返回 {@link BigDecimal#ZERO}
     */
    static BigDecimal computeWholesaleAmount(CartListByStoreVO cartList) {
        if (cartList == null || cartList.getCartList() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (CartByStoreVO store : cartList.getCartList()) {
            if (store == null || store.getCarts() == null) {
                continue;
            }
            for (CartVO c : store.getCarts()) {
                // CR-20260612-1520：积分抵扣基数排除「批发作用域」全部行（命中阶梯价 + 未命中按原始零售价兜底的行），
                // 判定从 isWholesaleLine 收口到 wholesaleScope；未命中行 price 已是原始零售价。
                if (c == null || !Boolean.TRUE.equals(c.getWholesaleScope())) {
                    continue;
                }
                if (!Boolean.TRUE.equals(c.getIsChecked())) {
                    continue;
                }
                BigDecimal price = c.getPrice() == null ? BigDecimal.ZERO : c.getPrice();
                int qty = c.getQuantity() == null ? 0 : c.getQuantity();
                if (qty <= 0) {
                    continue;
                }
                total = total.add(price.multiply(BigDecimal.valueOf(qty)));
            }
        }
        return total;
    }

    /**
     * 取批发等级快照（用于 order_item.wholesale_grade_id）。
     *
     * <p>CD-016：cart 不冻结，下单时锁的是用户当前等级（与 cart 行批发价 re-resolve 所用一致）。
     * 一单一查，避免 N+1：无任何批发行 → 跳过 DB；非批发商 / 等级缺失 → 返回 0。
     *
     * @return 当前等级 ID；无批发行 / 非批发商 / 等级缺失返回 0
     */
    Integer resolveWholesaleGradeSnapshot(CartListByStoreVO cartList, Integer userId) {
        if (cartList == null || cartList.getCartList() == null || userId == null) {
            return 0;
        }
        boolean hasAnyWholesaleLine = cartList.getCartList().stream()
                .filter(Objects::nonNull)
                .flatMap(s -> s.getCarts() == null ? java.util.stream.Stream.empty() : s.getCarts().stream())
                .anyMatch(c -> c != null && Boolean.TRUE.equals(c.getIsWholesaleLine()));
        if (!hasAnyWholesaleLine) {
            return 0;
        }
        if (!wholesalerService.isWholesaleBuyer(userId)) {
            return 0;
        }
        Wholesaler w = wholesalerService.getByUserId(userId);
        if (w == null || w.getGradeId() == null) {
            return 0;
        }
        return w.getGradeId();
    }

    /**
     * 批发快照赋值到 OrderItem（design §6.1）。
     *
     * <p>命中批发行 → 写 1 / gradeSnapshot / cart 当前价（已是批发壳算出的 finalPrice）；
     * 零售行 → 显式回填 0/0/0.00，避免 W0 hotfix 字段 NOT NULL 数据库异常。
     *
     * @param orderItem 待装配的订单行（必须非 null）
     * @param cart      cart 行（CartVO，从 buildCartPromotion 结果取，已含 isWholesaleLine 标记）
     * @param gradeSnapshot {@link #resolveWholesaleGradeSnapshot} 产物
     */
    static void applyWholesaleSnapshotToOrderItem(OrderItem orderItem, CartVO cart, Integer gradeSnapshot) {
        if (orderItem == null || cart == null) {
            return;
        }
        if (Boolean.TRUE.equals(cart.getIsWholesaleLine())) {
            orderItem.setIsWholesaleLine(1);
            orderItem.setWholesaleGradeId(gradeSnapshot == null ? 0 : gradeSnapshot);
            orderItem.setWholesaleBasePrice(cart.getPrice() == null ? BigDecimal.ZERO : cart.getPrice());
        } else {
            orderItem.setIsWholesaleLine(0);
            orderItem.setWholesaleGradeId(0);
            orderItem.setWholesaleBasePrice(BigDecimal.ZERO);
        }
    }

    /**
     * 下单二次起批量 + 倍数硬校验（design §12 防绕过）。
     *
     * <p>按 productId 聚合批发行：SPU 维度（moq_dimension=1）按 product 加总判定 {@code config.moq}；
     * SKU 维度（moq_dimension=2）按行判 {@code config.moq}（CR-20260604-0937 统一字段）；
     * 倍数永远按 SKU 校验（C12 与起批维度正交）。
     * 任一不达标 → 抛 {@link GlobalException}，触发 submit 的 {@code @Transactional} 回滚。
     *
     * <p>复用静态 {@link WholesaleBuyRuleValidator} 规则集，与加车阶段（CartWholesaleEnhancer）
     * 同源。批量预取 config / skuConfig 一次（按 productIds IN），避免 N+1。
     *
     * @throws GlobalException 5320 = MOQ 不达标 / 5321 = 倍数不达标
     */
    void validateWholesaleSubmitRules(CartListByStoreVO cartList, Integer userId) {
        if (cartList == null || cartList.getCartList() == null) {
            return;
        }
        // 0. CR-20260604-1430：混批 mode=1 未达档阻断（独立于 is_wholesale_line，按 isWholesaleBuyer 门控保留零售零 DB）
        validateMixModeTierMet(cartList, userId);
        // 1. 收集批发行 + 按 productId 加总 SPU 数量
        List<CartVO> wholesaleLines = new ArrayList<>();
        Map<Integer, Integer> spuTotalQty = new HashMap<>();
        for (CartByStoreVO store : cartList.getCartList()) {
            if (store == null || store.getCarts() == null) {
                continue;
            }
            for (CartVO c : store.getCarts()) {
                if (c == null || !Boolean.TRUE.equals(c.getIsWholesaleLine())) {
                    continue;
                }
                if (!Boolean.TRUE.equals(c.getIsChecked())) {
                    continue;
                }
                Integer pid = c.getProductId();
                int qty = c.getQuantity() == null ? 0 : c.getQuantity();
                if (pid == null || qty <= 0) {
                    continue;
                }
                wholesaleLines.add(c);
                spuTotalQty.merge(pid, qty, Integer::sum);
            }
        }
        if (wholesaleLines.isEmpty()) {
            return;
        }
        // 2. 批量取 config + skuConfig（按 productIds IN）
        List<Integer> productIds = new ArrayList<>(spuTotalQty.keySet());
        Map<Integer, ProductWholesaleConfig> configMap = wholesaleProductConfigService.mapByProductIds(productIds);
        Map<String, ProductWholesaleSkuConfig> skuConfigMap = wholesaleSkuConfigService.mapByProductSkuKeys(productIds);
        if (configMap == null) {
            configMap = Collections.emptyMap();
        }
        if (skuConfigMap == null) {
            skuConfigMap = Collections.emptyMap();
        }
        // 3. 逐行校验（顺序：倍数 → 起批量；与 CartWholesaleEnhancer.markAndValidateOnAdd 一致）
        for (CartVO c : wholesaleLines) {
            Integer pid = c.getProductId();
            Integer skuId = c.getSkuId();
            int qty = c.getQuantity() == null ? 0 : c.getQuantity();
            ProductWholesaleConfig cfg = configMap.get(pid);
            // 精确 SKU 优先，未命中回退 SPU 兜底 sku_id=0（单规格倍数）
            ProductWholesaleSkuConfig skuCfg =
                    WholesaleBuyRuleValidator.resolveSkuConfig(skuConfigMap, pid, skuId);
            // CR-20260612-1622：倍数已在加车 / 购物车列表加载时向上取整对齐（CartWholesaleEnhancer），
            // 此处保留硬校验作为「防绕过安全网」（正常路径已对齐，不会触发）；不在下单处静默改数。
            String mErr = WholesaleBuyRuleValidator.checkMultiple(qty, skuCfg);
            if (mErr != null) {
                throw new GlobalException(mErr, HttpStatus.WHOLESALE_MULTIPLE_NOT_MET);
            }
            boolean spuDimension = cfg != null && cfg.getMoqDimension() != null && cfg.getMoqDimension() == 1;
            // CR-20260604-0937：SKU 维度也读 config.moq 统一字段（不再读 skuCfg.moqSku）
            String moqErr = spuDimension
                    ? WholesaleBuyRuleValidator.checkMoqSpu(spuTotalQty.getOrDefault(pid, 0), cfg)
                    : WholesaleBuyRuleValidator.checkMoqSku(qty, cfg);
            if (moqErr != null) {
                throw new GlobalException(moqErr, HttpStatus.WHOLESALE_MOQ_NOT_MET);
            }
        }
    }

    /**
     * 混批（multi_spec_price_mode=1）未达档阻断（CR-20260604-1430，错误码 5323，Q-A=b2）。
     *
     * <p>独立于 {@code is_wholesale_line}：未达档的 SKU 不会被标记为批发行，必须单独检测。
     * 对每个 mode=1 商品按 Q_total（该 product 下所有<b>已勾选</b>购买 SKU 数量之和，D2=all_sku）判断：
     * 若某 SKU 存在可命中的批发价行（高数量探针命中）却在 Q_total 下无命中阶梯档 → 阻断下单。
     *
     * <p>性能：先经 {@link WholesalerService#isWholesaleBuyer} 门控（Redis），非批发买家零 DB；
     * 仅批发买家且存在 mode=1 商品时各加载一次 config + price（订单提交非热点路径）。
     */
    private void validateMixModeTierMet(CartListByStoreVO cartList, Integer userId) {
        if (userId == null || !wholesalerService.isWholesaleBuyer(userId)) {
            return;
        }
        // 收集所有勾选行 + 按 product 聚合 Q_total（含未配批发价的零售 SKU，D2=all_sku）
        Map<Integer, Integer> qTotalMap = new HashMap<>();
        Map<Integer, List<CartVO>> linesByProduct = new HashMap<>();
        for (CartByStoreVO store : cartList.getCartList()) {
            if (store == null || store.getCarts() == null) {
                continue;
            }
            for (CartVO c : store.getCarts()) {
                if (c == null || !Boolean.TRUE.equals(c.getIsChecked()) || c.getProductId() == null) {
                    continue;
                }
                int qty = c.getQuantity() == null ? 0 : c.getQuantity();
                if (qty <= 0) {
                    continue;
                }
                qTotalMap.merge(c.getProductId(), qty, Integer::sum);
                linesByProduct.computeIfAbsent(c.getProductId(), k -> new ArrayList<>()).add(c);
            }
        }
        if (qTotalMap.isEmpty()) {
            return;
        }
        // 找出 mode=1 商品
        Map<Integer, ProductWholesaleConfig> configMap =
                wholesaleProductConfigService.mapByProductIds(new ArrayList<>(qTotalMap.keySet()));
        if (configMap == null || configMap.isEmpty()) {
            return;
        }
        List<Integer> mixProductIds = new ArrayList<>();
        for (Map.Entry<Integer, ProductWholesaleConfig> ce : configMap.entrySet()) {
            ProductWholesaleConfig cfg = ce.getValue();
            if (cfg != null && cfg.getMultiSpecPriceMode() != null && cfg.getMultiSpecPriceMode() == 1) {
                mixProductIds.add(ce.getKey());
            }
        }
        if (mixProductIds.isEmpty()) {
            return;
        }
        // 直接取买家当前等级（不复用 resolveWholesaleGradeSnapshot：它耦合 is_wholesale_line，
        // 而未达档 SKU 恰恰不是批发行，会导致漏判）
        Wholesaler buyer = wholesalerService.getByUserId(userId);
        if (buyer == null || buyer.getGradeId() == null || buyer.getGradeId() <= 0) {
            return;
        }
        Integer gradeId = buyer.getGradeId();
        Map<Integer, List<com.tigshop.bean.model.wholesale.ProductWholesalePrice>> priceMap =
                wholesalePriceService.mapByProductIdsAndGrade(mixProductIds, gradeId);
        if (priceMap == null) {
            return;
        }
        java.math.BigDecimal probeRetail = java.math.BigDecimal.ONE;
        for (Integer pid : mixProductIds) {
            List<com.tigshop.bean.model.wholesale.ProductWholesalePrice> candidates = priceMap.get(pid);
            if (candidates == null || candidates.isEmpty()) {
                continue;
            }
            int qTotal = qTotalMap.getOrDefault(pid, 0);
            boolean o2oChannel = tigshopProperties != null
                    && Integer.valueOf(1).equals(tigshopProperties.getIsO2o());
            for (CartVO c : linesByProduct.getOrDefault(pid, Collections.emptyList())) {
                // O2O 渠道：商品归属与本行门店不一致（平台分配商品）→ 不参与批发，不受混批未达档拦截
                if (!com.tigshop.service.wholesale.WholesaleBuyRuleValidator.isClientEligible(
                        configMap.get(pid), o2oChannel, c.getShopId())) {
                    continue;
                }
                // 高数量探针：该 SKU 是否存在可命中的批发价行（区分零售 SKU）
                com.tigshop.service.wholesale.resolver.WholesalePriceResolver.ResolveResult eligible =
                        com.tigshop.service.wholesale.resolver.WholesalePriceResolver.resolve(
                                com.tigshop.service.wholesale.resolver.WholesalePriceResolver.ResolveInput.builder()
                                        .productId(pid).skuId(c.getSkuId()).gradeId(gradeId)
                                        .quantity(1).tierMatchQty(Integer.MAX_VALUE)
                                        .retailPrice(probeRetail).candidates(candidates).build());
                if (eligible == null) {
                    continue;
                }
                com.tigshop.service.wholesale.resolver.WholesalePriceResolver.ResolveResult hit =
                        com.tigshop.service.wholesale.resolver.WholesalePriceResolver.resolve(
                                com.tigshop.service.wholesale.resolver.WholesalePriceResolver.ResolveInput.builder()
                                        .productId(pid).skuId(c.getSkuId()).gradeId(gradeId)
                                        .quantity(1).tierMatchQty(qTotal)
                                        .retailPrice(probeRetail).candidates(candidates).build());
                if (hit == null) {
                    throw new GlobalException(
                            "混批商品未达最低起订档，请增加购买数量",
                            HttpStatus.WHOLESALE_TIER_NOT_MET);
                }
            }
        }
    }

    private void fillChangePriceInfo(OrderCheckVO orderCheckVO, OrderCheckVO.Item item, CartListByStoreVO cartListByStore) {
        if (Objects.isNull(orderCheckVO) || Objects.isNull(item) || Objects.isNull(cartListByStore)) {
            return;
        }

        Boolean hasChangePrice = Boolean.TRUE.equals(cartListByStore.getHasChangePrice());
        orderCheckVO.setHasChangePrice(hasChangePrice);
        item.setHasChangePrice(hasChangePrice);
        if (!hasChangePrice) {
            return;
        }

        CartGlobalChangePrice cartGlobalChangePrice = cartListByStore.getCartGlobalChangePrice();
        if (Objects.nonNull(cartGlobalChangePrice)) {
            orderCheckVO.setChangePriceType(1);
            orderCheckVO.setPriceChangeType(cartGlobalChangePrice.getChangePriceType());
            item.setChangePriceType(1);
            item.setPriceChangeType(cartGlobalChangePrice.getChangePriceType());
            return;
        }

        Integer cartChangePriceType = resolveCartChangePriceType(cartListByStore);
        orderCheckVO.setChangePriceType(2);
        orderCheckVO.setCartChangePriceType(cartChangePriceType);
        item.setChangePriceType(2);
        item.setCartChangePriceType(cartChangePriceType);
    }

    private Integer resolveCartChangePriceType(CartListByStoreVO cartListByStore) {
        Set<Integer> cartChangePriceTypes = new LinkedHashSet<>();
        for (CartByStoreVO cartByStoreVO : Optional.ofNullable(cartListByStore.getCartList()).orElse(Collections.emptyList())) {
            for (CartVO cartVO : Optional.ofNullable(cartByStoreVO.getCarts()).orElse(Collections.emptyList())) {
                OrderCheckParam.CartChangePrice cartChangePrice = cartVO.getCartChangePrice();
                if (Objects.nonNull(cartChangePrice) && Objects.nonNull(cartChangePrice.getChangePriceType())) {
                    cartChangePriceTypes.add(cartChangePrice.getChangePriceType());
                }
            }
        }
        return cartChangePriceTypes.size() == 1 ? cartChangePriceTypes.iterator().next() : null;
    }


    private OrderCheckVO.CouponList getCouponListByPromotion(CartListByStoreVO cartList, List<Integer> useCouponIds, List<Integer> selectCouponIds, Integer userId) {
        OrderCheckVO.CouponList couponList = new OrderCheckVO.CouponList();
        List<OrderCheckVO.Coupon> enableCoupons = new ArrayList<>();
        List<OrderCheckVO.Coupon> disableCoupons = new ArrayList<>();
        for (CartByStoreVO store : cartList.getCartList()) {
            // 获取当前用户在当前店铺下，符合时间要求且未使用的所有优惠券
            List<OrderCheckVO.Coupon> userCoupons = userCouponService.listUserCouponList(userId, store.getShopId());

            //进行一次过滤
            List<OrderCheckVO.Coupon> usedCoupons = new ArrayList<>();

            // 遍历每张优惠券，判断是否能用于当前促销
            for (OrderCheckVO.Coupon coupon : userCoupons) {
                if (CollUtil.isNotEmpty(store.getEnableUsePromotion())) {
                    for (PromotionVO promotionVO : store.getEnableUsePromotion()) {
                        if (ObjectUtil.equals(promotionVO.getRelationId(), coupon.getCouponId())) {
                            boolean selected = CollUtil.isNotEmpty(useCouponIds) && useCouponIds.contains(coupon.getCouponId()) && CollUtil.isNotEmpty(selectCouponIds) && selectCouponIds.contains(coupon.getUserCouponId());
                            coupon.setSelected(selected);
                            enableCoupons.add(coupon);
                            usedCoupons.add(coupon);
                            break; // 一张券只匹配一次促销
                        }
                    }
                }
            }

            // 不在 enableCoupons 中的，才是 disableCoupons
            for (OrderCheckVO.Coupon coupon : userCoupons) {
                if (!usedCoupons.contains(coupon)) {
                    disableCoupons.add(coupon);
                }
            }
        }

        couponList.setEnableCoupons(CollUtil.distinct(enableCoupons));
        couponList.setDisableCoupons(CollUtil.distinct(disableCoupons));
        return couponList;
    }


    //计算积分能抵多少金额
    private BigDecimal getPointValueAmount(BigDecimal point) {
        ConfigPO integralScaleConfig = configService.getConfigByCode(SettingsEnum.INTEGRAL_SCALE.getBizCode());
        BigDecimal integralScale = new BigDecimal(integralScaleConfig.getBizVal());
        BigDecimal amount = integralScale.multiply(point.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        return amount;
    }


    //获取用户积分
    private Integer getUserPoints(User user) {
        return user.getPoints() > 0 ? user.getPoints() : 0;
    }

    //获取金额对应的积分值
    private Integer getAmountValuePoints(BigDecimal amount) {
        String integralScaleVal = configService.getConfigVal(SettingsEnum.INTEGRAL_SCALE);
        BigDecimal integralScale = new BigDecimal(integralScaleVal);
        if (integralScale.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        // integralScale 为「每 100 积分可抵扣的金额」；积分只能使用整数，需向上取整才能覆盖全部可抵扣金额。
        // 实际抵扣金额会在总金额计算时按可抵扣商品金额封顶。
        return amount.multiply(BigDecimal.valueOf(100)).divide(integralScale, 0, RoundingMode.CEILING).intValue();
    }

    //获取金额最多使用积分值（integralPercent：每 integralPercentBaseAmount 元最多可使用的积分数）
    private Integer getAmountMaxValuePoints(BigDecimal amount) {
        String integralPercentVal = configService.getConfigVal(SettingsEnum.INTEGRAL_PERCENT);
        String integralPercentBaseAmountVal = configService.getConfigVal(SettingsEnum.INTEGRAL_PERCENT_BASE_AMOUNT);
        BigDecimal integralPercent = new BigDecimal(integralPercentVal);
        BigDecimal integralPercentBaseAmount = StrUtil.isBlank(integralPercentBaseAmountVal)
                ? BigDecimal.valueOf(100)
                : new BigDecimal(integralPercentBaseAmountVal);
        if (integralPercent.compareTo(BigDecimal.ZERO) <= 0 || integralPercentBaseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return amount.multiply(integralPercent).divide(integralPercentBaseAmount, 0, RoundingMode.DOWN).intValue();
    }

    public OrderCheckVO.Total getTotalFee(OrderCheckParam param, CartListByStoreVO cartList, User user) {
        OrderCheckVO.Total total = new OrderCheckVO.Total();
        //初始化total
        // 订单总金额
        total.setTotalAmount(BigDecimal.valueOf(0));
        // 已支付金额
        total.setPaidAmount(BigDecimal.valueOf(0));
        // 未付款金额
        total.setUnpaidAmount(BigDecimal.valueOf(0));
//        total.setUnrefundAmount(0); // 未退款金额
        // 使用优惠券金额
        total.setCouponAmount(BigDecimal.valueOf(0));
        // 使用积分金额
        total.setPointsAmount(BigDecimal.valueOf(0));
        // 使用余额金额
        total.setBalance(BigDecimal.valueOf(0));
        // 使用账期支付金额
        total.setCreditAmount(BigDecimal.valueOf(0));
        // 服务费
        total.setServiceFee(BigDecimal.valueOf(0));
        // 配送费用
        total.setShippingFee(BigDecimal.valueOf(0));
        // 使用现金金额
        total.setCashAmount(BigDecimal.valueOf(0));
        // 找零金额
        total.setCashChangeAmount(BigDecimal.valueOf(0));
        // 抹分金额
        total.setRoundOffCentsAmount(BigDecimal.valueOf(0));
        // 抹角金额
        total.setRoundOffDimeAmount(BigDecimal.valueOf(0));
        total.setDiscountChangePrice(cartList.getTotal().getDiscountChangePrice());
//        total.setInvoiceFee(0); // 发票费用
        // 各种优惠活动金额
        total.setDiscountAmount(BigDecimal.valueOf(0));
        total.setOrderSendPoint(0);
        total.setExchangePoints(0);
        total.setStoreShippingFee(new HashMap<>());
        total.setGiftCardAmount(BigDecimal.ZERO);
        //如果cartList.getTotal()里有值的则覆盖到total中
        total.setDiscountCouponAmount(cartList.getTotal().getDiscountCouponAmount());
        total.setCouponAmount(cartList.getTotal().getDiscountCouponAmount());
        total.setDiscountAmount(cartList.getTotal().getDiscountDiscountAmount());
        total.setExchangePoints(0);
        total.setPointsAmount(getPointValueAmount(param.getUsePoint() != null ? BigDecimal.valueOf(param.getUsePoint()) : BigDecimal.valueOf(0)));
        // 如果是抽奖商品直接返回
        if (CartTypeEnum.TYPE_LOTTERY_PRODUCT.getCode().equals(param.getFlowType())) {
            total.setProductAmount(BigDecimal.ZERO);
            return total;
        }
        BeanUtils.copyProperties(cartList.getTotal(), total);

        // 计算各个店铺的优惠
        Map<String, Map<Integer, BigDecimal>> storeDiscountMap = new HashMap<>();

//        for (CartByStoreVO cartListVO : cartList.getCartList()) {
//            for (PromotionVO promotion : cartListVO.getUsedPromotions()) {
//                if(promotion.getType() == 2){
//
//                }
//            }
//        }*/
        // 判断是否自动抹零
        boolean isRoundOffDime = Optional.ofNullable(param.getRoundOffDime()).orElse(false);
        boolean isRoundOffCents = Optional.ofNullable(param.getRoundOffCents()).orElse(false);

        param.setRoundOffCents(isRoundOffCents);
        param.setRoundOffDime(isRoundOffDime);


        if (param.getFlowType() == 3) {
            Integer exchangeProductId = null;
            Integer exchangeProductSkuId = null;
            Integer exchangeProductNumber = 0;


            for (CartByStoreVO cartListVO : cartList.getCartList()) {
                for (CartVO cartVO : cartListVO.getCarts()) {
                    exchangeProductId = cartVO.getProductId();
                    exchangeProductSkuId = cartVO.getSkuId();
                    exchangeProductNumber += cartVO.getQuantity();
                }
            }
            if (exchangeProductId > 0) {
                PointsExchange pointsExchangeVO = pointsExchangeMapper.selectOne(new LambdaQueryWrapper<PointsExchange>().eq(PointsExchange::getProductId, exchangeProductId).eq(PointsExchange::getSkuId, exchangeProductSkuId));
                if (pointsExchangeVO != null) {
                    total.setExchangePoints(pointsExchangeVO.getExchangeIntegral() * exchangeProductNumber);
                    total.setPointsAmount(pointsExchangeVO.getPointsDeductedAmount().multiply(BigDecimal.valueOf(exchangeProductNumber)));
                    param.setUsePoint(total.getExchangePoints());
                }
            }
        }

        // 礼品卡兑换逻辑
        if (ObjectUtil.equals(param.getFlowType(), CartTypeEnum.TYPE_GIFT_EXCHANGE.getCode())) {
            total.setGiftCardAmount(total.getProductAmount() != null ? total.getProductAmount() : BigDecimal.ZERO);

        }

        // 整数积分向上取整后，其理论价值可能略高于可抵扣商品金额；实际抵扣必须以该金额封顶。
        // 积分兑换商品有独立的固定抵扣金额，不进入普通订单封顶逻辑。
        if (param.getFlowType() != 3) {
            BigDecimal pointsDeductibleAmount = Boolean.TRUE.equals(cartList.getHasChangePrice())
                    ? cartList.getTotal().getDiscountAfter()
                    : cartList.getTotal().getProductAmount();
            if (pointsDeductibleAmount == null) {
                pointsDeductibleAmount = BigDecimal.ZERO;
            }
            pointsDeductibleAmount = pointsDeductibleAmount.subtract(computeWholesaleAmount(cartList)).max(BigDecimal.ZERO);
            total.setPointsAmount(total.getPointsAmount().min(pointsDeductibleAmount));
        }

        if (!PosCheckoutContext.isPosCheckout(param)) {
            //运费费用处理
            OrderCheckVO.ShippingFee shippingFee = new OrderCheckVO.ShippingFee();
            if (Objects.equals(param.getDeliveryType(), OrderDeliveryTypeEnum.LOGISTICS.getCode())) {
                shippingFee = getShippingFee(cartList, user, param);
            } else if (Objects.equals(param.getDeliveryType(), OrderDeliveryTypeEnum.PICKUP.getCode())) {
                shippingFee.setTotal(BigDecimal.ZERO);
            }
            total.setShippingFee(shippingFee.getTotal());
            total.setStoreShippingFee(shippingFee.getStoreShippingFee());
            //礼品卡兑换订单免运费
            if (ObjectUtil.equals(param.getFlowType(), CartTypeEnum.TYPE_GIFT_EXCHANGE.getCode())) {
                total.setShippingFee(BigDecimal.ZERO);
                Map<Integer, BigDecimal> storeShippingFee = shippingFee.getStoreShippingFee() != null ? shippingFee.getStoreShippingFee() : new HashMap<>();
                storeShippingFee.forEach((k, v) -> storeShippingFee.put(k, BigDecimal.ZERO));
                total.setStoreShippingFee(storeShippingFee);
            }
        }

        // 代客下单整单改价：改价金额已包含运费，将运费追加到改价优惠中
        if (CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode().equals(param.getFlowType())
                && cartList.getCartGlobalChangePrice() != null) {
            BigDecimal actualShippingFee = total.getShippingFee() == null ? BigDecimal.ZERO : total.getShippingFee();
            if (actualShippingFee.compareTo(BigDecimal.ZERO) > 0) {
                total.setDiscountAmount(
                        (total.getDiscountAmount() == null ? BigDecimal.ZERO : total.getDiscountAmount())
                                .add(actualShippingFee));
                total.setDiscountChangePrice(
                        (total.getDiscountChangePrice() == null ? BigDecimal.ZERO : total.getDiscountChangePrice())
                                .add(actualShippingFee));
                total.setDiscountDiscountAmount(
                        (total.getDiscountDiscountAmount() == null ? BigDecimal.ZERO : total.getDiscountDiscountAmount())
                                .add(actualShippingFee));
                total.setDiscounts(
                        (total.getDiscounts() == null ? BigDecimal.ZERO : total.getDiscounts())
                                .add(actualShippingFee));
                // 同步更新店铺级别的改价优惠（按运费占比分配到各店铺）
                BigDecimal totalProductAmount = total.getProductAmount();
                if (totalProductAmount.compareTo(BigDecimal.ZERO) > 0) {
                    List<CartByStoreVO> storeList = cartList.getCartList();
                    BigDecimal distributed = BigDecimal.ZERO;
                    for (int i = 0; i < storeList.size(); i++) {
                        CartByStoreVO store = storeList.get(i);
                        BigDecimal storeShippingShare;
                        if (i == storeList.size() - 1) {
                            storeShippingShare = actualShippingFee.subtract(distributed);
                        } else {
                            BigDecimal storeAmount = store.getCarts().stream()
                                    .filter(CartVO::getIsChecked)
                                    .map(CartVO::getSubtotal)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                            storeShippingShare = actualShippingFee.multiply(storeAmount)
                                    .divide(totalProductAmount, 2, RoundingMode.HALF_UP);
                            distributed = distributed.add(storeShippingShare);
                        }
                        CartByStoreVO.CartStoreTotal storeTotal = store.getTotal();
                        storeTotal.setDiscountChangePrice(
                                (storeTotal.getDiscountChangePrice() == null ? BigDecimal.ZERO : storeTotal.getDiscountChangePrice())
                                        .add(storeShippingShare));
                        storeTotal.setDiscounts(
                                (storeTotal.getDiscounts() == null ? BigDecimal.ZERO : storeTotal.getDiscounts())
                                        .add(storeShippingShare));
                        storeTotal.setDiscountDiscountAmount(
                                (storeTotal.getDiscountDiscountAmount() == null ? BigDecimal.ZERO : storeTotal.getDiscountDiscountAmount())
                                        .add(storeShippingShare));
                    }
                }
            }
        }

        total.setTotalAmount(total.getProductAmount().add(total.getShippingFee() == null ? BigDecimal.ZERO : total.getShippingFee()).add(total.getServiceFee() == null ? BigDecimal.ZERO : total.getServiceFee()).subtract(total.getPointsAmount() == null ? BigDecimal.ZERO : total.getPointsAmount()).subtract(total.getDiscountAmount() == null ? BigDecimal.ZERO : total.getDiscountAmount()).subtract(total.getCouponAmount() == null ? BigDecimal.ZERO : total.getCouponAmount()).subtract(total.getGiftCardAmount() == null ? BigDecimal.ZERO : total.getGiftCardAmount()));


        total.setTotalAmount(total.getTotalAmount().compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total.getTotalAmount());
        total.setTotalAmount(total.getTotalAmount().subtract(total.getRoundOffDimeAmount() == null ? BigDecimal.ZERO : total.getRoundOffDimeAmount()).subtract(total.getRoundOffCentsAmount() == null ? BigDecimal.ZERO : total.getRoundOffCentsAmount()));

        if (Boolean.TRUE.equals(param.getRoundOffDime())) {
            // 抹角 + 抹分
            RoundOffUtil.Result result1 = clearDecimal(total.getTotalAmount(), 1);
            RoundOffUtil.Result result2 = clearDecimal(result1.getNewValue(), 2);

            total.setRoundOffDimeAmount(result1.getReducedAmount().add(result2.getReducedAmount()));
            total.setTotalAmount(total.getTotalAmount().subtract(total.getRoundOffDimeAmount()));
        } else if (Boolean.TRUE.equals(param.getRoundOffCents())) {
            RoundOffUtil.Result result = clearDecimal(total.getTotalAmount(), 2);
            total.setRoundOffCentsAmount(result.getReducedAmount());
            total.setTotalAmount(total.getTotalAmount().subtract(total.getRoundOffCentsAmount()));
        }
        // 现金抵扣
        total.setCashAmount(param.getCashAmount());

        //余额抵扣
        if (param.getUseBalance() != null && param.getUseBalance().compareTo(BigDecimal.valueOf(0)) > 0) {
            BigDecimal userBalance = getUserBalance(user);
            if (CartTypeEnum.TYPE_RESERVATION_PRODUCT.getCode().equals(param.getFlowType())) {
                //预售流程的话,余额不能大于定金
                ProductPresaleVO productPresale = param.getProductPresale();
                Assert.isFalse(productPresale == null, () -> new GlobalException("订单预售错误"));
                Integer saleMode = productPresale.getSaleMode();
                if (Integer.valueOf(2).equals(saleMode)) {
                    //定金预售
                    if (param.getDepositAmount().compareTo(userBalance) <= 0) {
                        total.setBalance(param.getDepositAmount());
                    } else {
                        total.setBalance(userBalance);
                    }
                } else {
                    //全款预售
                    if (total.getTotalAmount().compareTo(userBalance) <= 0) {
                        total.setBalance(total.getTotalAmount());
                    } else {
                        total.setBalance(userBalance);
                    }
                }
            } else {
                if (total.getTotalAmount().compareTo(userBalance) <= 0) {
                    total.setBalance(total.getTotalAmount());
                } else {
                    total.setBalance(userBalance);
                }
            }
        }

        // 账期支付抵扣（与余额支付互斥，useCredit=1时使用账期支付）
        if (param.getUseCredit() != null && param.getUseCredit() == 1) {
            String useCredit = configService.getConfigVal(SettingsEnum.USE_CREDIT);
            if (!"1".equals(useCredit)) {
                throw new GlobalException(messageHelper.get("error.credit.disabled"));
            }
            // 检查是否同时使用余额支付（互斥校验）
            if (param.getUseBalance() != null && param.getUseBalance().compareTo(BigDecimal.ZERO) > 0) {
                throw new GlobalException(messageHelper.get("error.credit.balance.conflict"));
            }
            // 基于账期规则校验可用性（含：上期未结清、授信禁用、额度不足）
            UserCreditInfoVO creditInfo = creditUserService.getUserCreditInfo(user.getUserId(), total.getTotalAmount());
            if (creditInfo == null || !Boolean.TRUE.equals(creditInfo.getCanUseCredit())) {
                String reason = creditInfo != null && StrUtil.isNotBlank(creditInfo.getDisableReason())
                        ? creditInfo.getDisableReason()
                        : messageHelper.get("error.credit.not.member");
                throw new GlobalException(reason);
            }
            // 计算需要抵扣的金额（账期支付全额抵扣订单金额）
            BigDecimal orderAmount = total.getTotalAmount();
            // 账期支付全额抵扣
            total.setCreditAmount(orderAmount);
        }

        if (total.getCashAmount() == null) {
            total.setCashAmount(BigDecimal.ZERO);
        }

        total.setBalance(total.getBalance().compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total.getBalance());
        total.setCreditAmount(total.getCreditAmount() == null ? BigDecimal.ZERO : total.getCreditAmount());
        total.setPaidAmount(BigDecimal.valueOf(0));
        BigDecimal cashAmount = total.getCashAmount() == null ? BigDecimal.ZERO : total.getCashAmount();
        total.setProductAmount(cartList.getTotal().getProductAmount());

        // 找零金额 (已经移动到create,这里直接设定为0)
//        BigDecimal cashChangeAmount = unpaidAmount.abs();
//        total.setCashChangeAmount(cashChangeAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : cashChangeAmount);
        // 现金金额仅在“现金支付”场景下参与抵扣未付金额；线下支付(OFFLINE)不应在提交时视为已收款
        boolean deductCashAmount = Objects.equals(param.getPayTypeId(), PaymentType.CASH.getCode());
        total.setCashChangeAmount(BigDecimal.ZERO);

        boolean isPosCheckout = PosCheckoutContext.isPosCheckout(param);
        BigDecimal taxRate = isPosCheckout ? BigDecimal.ZERO : resolveOrderTaxRate(param, user);
        param.setTaxRate(taxRate);
        BigDecimal taxRateForCalculation = isPosCheckout ? BigDecimal.ZERO : normalizeTaxRateForCalculation(taxRate);
        BigDecimal taxRateAmount = BigDecimal.ZERO;
        if (taxRateForCalculation != null && taxRateForCalculation.compareTo(BigDecimal.ZERO) > 0) {
            // 税基 = 商品金额 + 运费 + 服务费 - 营销/会员优惠（不扣除优惠券/余额/礼品卡等支付抵扣）
            BigDecimal taxBaseAmount = Optional.ofNullable(total.getProductAmount()).orElse(BigDecimal.ZERO)
                    .add(Optional.ofNullable(total.getShippingFee()).orElse(BigDecimal.ZERO))
                    .add(Optional.ofNullable(total.getServiceFee()).orElse(BigDecimal.ZERO))
                    .subtract(Optional.ofNullable(total.getDiscountAmount()).orElse(BigDecimal.ZERO));
            // 积分兑换场景：积分抵扣金额属于实际价格减免，需从税基中扣除
            if (CartTypeEnum.TYPE_EXCHANGE.getCode().equals(param.getFlowType())) {
                taxBaseAmount = taxBaseAmount.subtract(Optional.ofNullable(total.getPointsAmount()).orElse(BigDecimal.ZERO));
            }
            if (taxBaseAmount.compareTo(BigDecimal.ZERO) < 0) {
                taxBaseAmount = BigDecimal.ZERO;
            }
            taxRateAmount = taxBaseAmount.multiply(taxRateForCalculation).setScale(2, RoundingMode.CEILING);
            total.setTotalAmount(total.getTotalAmount().add(taxRateAmount));
        }
        total.setTaxRate(taxRate);
        total.setTaxRateAmount(taxRateAmount);
        total.setTotalAmount(total.getTotalAmount().compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total.getTotalAmount());

        // 税费加入后，余额可抵扣上限需按最新应付金额重算，确保税费也支持余额支付
        if (param.getUseBalance() != null && param.getUseBalance().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal userBalance = getUserBalance(user);
            if (CartTypeEnum.TYPE_RESERVATION_PRODUCT.getCode().equals(param.getFlowType())) {
                // 定金预售场景保持原规则：余额抵扣不超过定金金额
                ProductPresaleVO productPresale = param.getProductPresale();
                if (productPresale != null && Integer.valueOf(2).equals(productPresale.getSaleMode())) {
                    BigDecimal depositAmount = param.getDepositAmount() != null ? param.getDepositAmount() : BigDecimal.ZERO;
                    BigDecimal maxBalance = depositAmount.compareTo(total.getTotalAmount()) > 0 ? total.getTotalAmount() : depositAmount;
                    total.setBalance(userBalance.compareTo(maxBalance) < 0 ? userBalance : maxBalance);
                } else {
                    total.setBalance(userBalance.compareTo(total.getTotalAmount()) < 0 ? userBalance : total.getTotalAmount());
                }
            } else {
                total.setBalance(userBalance.compareTo(total.getTotalAmount()) < 0 ? userBalance : total.getTotalAmount());
            }
        }

        BigDecimal unpaidAmount = total.getTotalAmount()
                .subtract(total.getBalance())
                .subtract(deductCashAmount ? cashAmount : BigDecimal.ZERO);
        BigDecimal creditAmount = total.getCreditAmount();
        unpaidAmount = unpaidAmount.subtract(creditAmount);
        total.setUnpaidAmount(unpaidAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : unpaidAmount);
        total.setProductAmount(cartList.getTotal()
                .getProductAmount());
        return total;
    }

    private BigDecimal resolveOrderTaxRate(OrderCheckParam param, User user) {
        Integer userId = user == null ? null : user.getUserId();
        List<Integer> regionIds = resolveTaxRegionIds(param, userId);
        if (CollUtil.isEmpty(regionIds)) {
            return null;
        }
        List<Integer> validRegionIds = regionIds.stream()
                .filter(id -> id != null && id > 0)
                .toList();
        if (validRegionIds.isEmpty()) {
            return null;
        }
        // 单次 IN 查询替代逐级回退的 N 次查询
        List<TaxRate> taxRates = taxRateService.list(new LambdaQueryWrapper<TaxRate>()
                .in(TaxRate::getRegionId, validRegionIds)
                .eq(TaxRate::getIsActive, YES)
                .orderByDesc(TaxRate::getUpdateTime)
                .orderByDesc(TaxRate::getCreateTime)
                .orderByDesc(TaxRate::getTaxRateId));
        if (CollUtil.isEmpty(taxRates)) {
            return null;
        }
        Map<Integer, BigDecimal> rateByRegion = new LinkedHashMap<>();
        for (TaxRate tr : taxRates) {
            rateByRegion.putIfAbsent(tr.getRegionId(), tr.getRate());
        }
        // 从最细粒度向上回退匹配（区/县 -> 市 -> 省 -> 国家）
        for (int i = regionIds.size() - 1; i >= 0; i--) {
            Integer regionId = regionIds.get(i);
            if (regionId == null || regionId <= 0) {
                continue;
            }
            BigDecimal rate = rateByRegion.get(regionId);
            if (rate != null) {
                return rate;
            }
        }
        return null;
    }

    private List<Integer> resolveTaxRegionIds(OrderCheckParam param, Integer userId) {
        if (CollUtil.isNotEmpty(param.getRegionIds())) {
            return completeRegionIdsToRoot(param.getRegionIds());
        }
        if (param.getAddressId() != null && param.getAddressId() > 0) {
            UserAddress rawAddress = userAddressService.getById(param.getAddressId());
            if (rawAddress != null && StrUtil.isNotBlank(rawAddress.getRegionIds())) {
                List<Integer> regionIds = JsonUtil.jsonToList(rawAddress.getRegionIds(), Integer.class);
                if (CollUtil.isNotEmpty(regionIds)) {
                    return completeRegionIdsToRoot(regionIds);
                }
            }
        }
        if (userId == null) {
            return Collections.emptyList();
        }
        UserAddressVO selectedAddress = userAddressService.getUserSystemSelectedAddress(userId);
        if (selectedAddress == null || CollUtil.isEmpty(selectedAddress.getRegionIds())) {
            selectedAddress = userAddressService.getUserSelectedAddress();
        }
        if (selectedAddress == null || CollUtil.isEmpty(selectedAddress.getRegionIds())) {
            return Collections.emptyList();
        }
        return completeRegionIdsToRoot(selectedAddress.getRegionIds());
    }

    /**
     * 向上补齐地区链路直到国家根节点，兼容两种地址格式：
     * 老版地址 regionIds 首位即国家，跨境地址把国家单独存在 countryCode、regionIds 只有国家以下层级，
     * 不补齐时配置在国家节点上的税率无法命中。
     */
    private List<Integer> completeRegionIdsToRoot(List<Integer> regionIds) {
        Integer firstRegionId = regionIds.stream()
                .filter(id -> id != null && id > 0)
                .findFirst()
                .orElse(null);
        if (firstRegionId == null) {
            return regionIds;
        }
        LinkedList<Integer> ancestorIds = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();
        visited.add(firstRegionId);
        Region current = regionService.getById(firstRegionId);
        while (current != null && current.getParentId() != null && current.getParentId() > 0
                && visited.add(current.getParentId())) {
            ancestorIds.addFirst(current.getParentId());
            current = regionService.getById(current.getParentId());
        }
        if (ancestorIds.isEmpty()) {
            return regionIds;
        }
        List<Integer> completedRegionIds = new ArrayList<>(ancestorIds.size() + regionIds.size());
        completedRegionIds.addAll(ancestorIds);
        completedRegionIds.addAll(regionIds);
        return completedRegionIds;
    }

    private BigDecimal normalizeTaxRateForCalculation(BigDecimal taxRate) {
        if (taxRate == null) {
            return null;
        }
        if (taxRate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        // taxRate 统一按百分比语义处理，例如 10 表示 10%
        return taxRate.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
    }





    private UserAddress getReservationHomeAddress(OrderCheckParam param) {
        if (param == null) {
            return null;
        }
        if (param.getAddressId() != null) {
            return userAddressMapper.selectById(param.getAddressId());
        }
        if (param.getAddress() == null) {
            return null;
        }
        UserAddress userAddress = new UserAddress();
        userAddress.setLatitude(param.getAddress().getLatitude());
        userAddress.setLongitude(param.getAddress().getLongitude());
        userAddress.setAddress(param.getAddress().getAddress());
        userAddress.setConsignee(param.getAddress().getConsignee());
        userAddress.setMobile(param.getAddress().getMobile());
        return userAddress;
    }



    private OrderCheckVO.ShippingFee getShippingFee(CartListByStoreVO cartList, User user, OrderCheckParam param) {

        int userRankId = user.getRankId();
        boolean freeShipping = memberBenefitService.resolveFreeShipping(user.getUserId(), userRankId);

        Map<Integer, Integer> selectedTypeIds = new HashMap<>();
        if (CollUtil.isNotEmpty(param.getShippingType())) {

            for (Integer key : param.getShippingType().keySet()) {
                selectedTypeIds.put(param.getShippingType().get(key).getShopId(), param.getShippingType().get(key).getTypeId());
            }

        }

        OrderCheckVO.ShippingFee result = new OrderCheckVO.ShippingFee();
        result.setTotal(BigDecimal.ZERO);
        Map<Integer, BigDecimal> storeShippingFee = new HashMap<>();

        // 收货地区解析为 param 级别、循环内不变：原实现会在每个非默认运费规则上重复查地址表，这里改为整单只解析一次
        List<Integer> resolvedRegionIds = null;
        boolean regionResolved = false;

        for (CartByStoreVO storeCart : cartList.getCartList()) {
            int shopId = storeCart.getShopId();
            int typeId = 0;
            if (selectedTypeIds.containsKey(shopId)) {
                typeId = selectedTypeIds.get(shopId);
            }
            Long defaultTplId = shippingTplService.getDefaultShippingTplId(shopId);
            BigDecimal thisStoreShippingFee = BigDecimal.ZERO;
            storeShippingFee.put(shopId, thisStoreShippingFee);

            // 自提订单或者免运费订单则不计费
            if (freeShipping || storeCart.getNoShipping() == 1) {
                continue;
            }
            Map<Long, Map<String, BigDecimal>> storeShippingTplMap = new HashMap<>();
            //批量预取本店铺购物车商品，避免下方循环内逐条 getById（取数与原 getById 完全一致）
            List<Integer> storeProductIds = storeCart.getCarts().stream().map(CartVO::getProductId).toList();
            Map<Integer, Product> shippingProductMap = storeProductIds.isEmpty() ? new HashMap<>() :
                    productService.listByIds(storeProductIds).stream()
                            .collect(Collectors.toMap(Product::getProductId, p -> p, (a, b) -> a));
            //计算重量重新计算运费
            for (CartVO cartVO : storeCart.getCarts()) {
                if (cartVO.getFreeShipping() == 1) {
                    continue;
                }
                //获取运费模板类型
                // 如果为固定模板
                Product product = shippingProductMap.get(cartVO.getProductId());
                if (product.getFixedShippingType() == 1) {
                    storeShippingFee.put(shopId, storeShippingFee.get(shopId).add(product.getFixedShippingFee()));
                    result.setTotal(result.getTotal().add(product.getFixedShippingFee()));
                    continue;
                }
                // 如果为非固定模板
                Long shippingTplId = cartVO.getShippingTplId() == null || cartVO.getShippingTplId() == 0 ? defaultTplId : cartVO.getShippingTplId();
                //如果没有值则初始化一个map
                if (!storeShippingTplMap.containsKey(shippingTplId)) {
                    Map<String, BigDecimal> shippingTplMap = new HashMap<>();
                    shippingTplMap.put("count", BigDecimal.valueOf(0));
                    shippingTplMap.put("fee", BigDecimal.valueOf(0));
                    shippingTplMap.put("weight", BigDecimal.valueOf(0));
                    storeShippingTplMap.put(shippingTplId, shippingTplMap);
                }

                Map<String, BigDecimal> oldShippingTplMap = storeShippingTplMap.get(shippingTplId);
                BigDecimal productWeight = cartVO.getProductWeight() != null ? cartVO.getProductWeight() : BigDecimal.ONE;
                BigDecimal quantity = new BigDecimal(cartVO.getQuantity());
                BigDecimal weight = oldShippingTplMap.get("weight").add(productWeight.multiply(quantity));
                BigDecimal count = oldShippingTplMap.get("count").add(quantity);
                oldShippingTplMap.put("weight", weight);
                oldShippingTplMap.put("count", count);
                storeShippingTplMap.put(shippingTplId, oldShippingTplMap);

            }


            //批量预取本店铺涉及的运费模板信息，避免下方循环内逐个模板查询（取数与原逐个 list 完全一致）
            Map<Long, List<ShippingTplInfo>> tplInfoMap = new HashMap<>();
            if (!storeShippingTplMap.isEmpty()) {
                tplInfoMap = shippingTplInfoService.list(new LambdaQueryWrapper<ShippingTplInfo>()
                                .in(ShippingTplInfo::getShippingTplId, storeShippingTplMap.keySet())
                                .eq(ShippingTplInfo::getShippingTypeId, typeId))
                        .stream().collect(Collectors.groupingBy(ShippingTplInfo::getShippingTplId));
            }
            //for循环storeShippingTplMap
            for (Map.Entry<Long, Map<String, BigDecimal>> entry : storeShippingTplMap.entrySet()) {
                List<ShippingTplInfo> allTplInfo = tplInfoMap.getOrDefault(entry.getKey(), Collections.emptyList());
                ShippingTplInfo tplInfo = null;
                for (ShippingTplInfo v : allTplInfo) {
                    if (v.getIsDefault() == 1) {
                        tplInfo = v;
                    } else {
                        List<Integer> objects;
                        if (regionResolved) {
                            objects = resolvedRegionIds;
                        } else if (param.getAddressId() != null) {
                            UserAddress userAddress = userAddressService.getById(param.getAddressId());
                            if (userAddress == null) {
                                return result;
                            }
                            String regionIds = userAddress.getRegionIds();
                            objects = JSONUtil.parseArray(regionIds, new JSONConfig() {
                            }).toList(Integer.class);
                            resolvedRegionIds = objects;
                            regionResolved = true;
                        } else if (param.getAddress() != null && param.getAddress().getRegionIds() != null) {
                            objects = param.getAddress().getRegionIds();
                            resolvedRegionIds = objects;
                            regionResolved = true;
                        } else {
                            return result;
                        }
                        if (fetchRegion(objects, v.getRegionData())) {
                            tplInfo = v;
                        }
                    }
                }
                Map<String, BigDecimal> oldShippingTplMap = entry.getValue();
                if (tplInfo != null) {
                    BigDecimal count = oldShippingTplMap.get("count");
                    BigDecimal weight = oldShippingTplMap.get("weight");
                    entry.getValue().put("fee", count.compareTo(BigDecimal.ZERO) > 0 ? tplInfo.getStartPrice() : BigDecimal.ZERO);

                    if (cartList.getTotal().getProductAmount().compareTo(tplInfo.getFreePrice()) >= 0 && tplInfo.getFreePrice().compareTo(BigDecimal.ZERO) > 0 || tplInfo.getIsFree() == 1) {
                        oldShippingTplMap.put("fee", BigDecimal.ZERO);
                    } else {
                        if (tplInfo.getPricingType() == 1) {
                            if (count.compareTo(tplInfo.getStartNumber()) >= 0) {
                                BigDecimal addCount = (count.subtract(tplInfo.getStartNumber())).divide(tplInfo.getAddNumber(), 0, RoundingMode.UP);
                                oldShippingTplMap.put("fee", oldShippingTplMap.get("fee").add(addCount.multiply(tplInfo.getAddPrice())));
                            }
                        } else if (tplInfo.getPricingType() == 2) {
                            if (weight.compareTo(tplInfo.getStartNumber()) >= 0) {
                                BigDecimal addWeight = (weight.subtract(tplInfo.getStartNumber())).divide(tplInfo.getAddNumber(), 0, RoundingMode.UP);
                                oldShippingTplMap.put("fee", oldShippingTplMap.get("fee").add(addWeight.multiply(tplInfo.getAddPrice())));
                            }
                        }
                    }
                    result.setTotal(result.getTotal().add(oldShippingTplMap.get("fee")));
                    storeShippingFee.put(shopId, storeShippingFee.get(shopId).add(oldShippingTplMap.get("fee")));
                }
            }
        }
        result.setStoreShippingFee(storeShippingFee);
        return result;
    }

    @Override
    public Map<Integer, List<OrderCheckVO.ShippingType>> getStoreShippingType(StoreShippingTypeQuery query) {
        Integer currentUserId = Optional.ofNullable(query.getUserId()).orElse(SecurityUtils.getCurrentUserId());
        CartListByStoreVO carts;
        if (query.getShopId() != null) {
            carts = cartService.getCartListByStore(true, query.getFlowType(), null, query.getShopId(), currentUserId, query.getExcludeQuickProduct());
        } else {
            carts = cartService.getCartListByStore(true, query.getFlowType(), null, null, currentUserId, query.getExcludeQuickProduct());
        }

        UserAddressVO selectedAddress = resolveSelectedAddress(query.getFlowType(), query.getUserId(), query.getAddressId());

        //region_ids json数组转成list
        List<Integer> regionIds = new ArrayList<>();
        if (selectedAddress != null && selectedAddress.getRegionIds() != null) {
            regionIds = selectedAddress.getRegionIds();
        }
        if (carts.getCartList() == null) {
            return null;
        }

        Map<Integer, List<OrderCheckVO.ShippingType>> availableShippingTypeList = new HashMap<>();
        // 地区配送校验（childAreaNeedRegion）与具体店铺无关：原实现放在店铺循环内，会对每个店铺重复拉取一次全量购物车并重复做相同校验。
        // 这里改为整个请求只在需要时计算一次，校验结果与原逻辑完全等价（任一固定运费商品地址不可达则返回空 Map）。
        boolean hasAnyStoreCarts = carts.getCartList().stream().anyMatch(storeCart -> storeCart.getCarts() != null);
        if (hasAnyStoreCarts && selectedAddress != null && selectedAddress.getAddressId() != null
                && "1".equals(configService.getConfigByCode("childAreaNeedRegion").getBizVal())) {
            CartListByStoreVO cartListByStore = cartService.getCartListByStore(true, query.getFlowType(), null, null, currentUserId, null);
            for (CartByStoreVO cartByStoreVO : cartListByStore.getCartList()) {
                for (CartVO cart : cartByStoreVO.getCarts()) {
                    if (cart.getFixedShippingType() == 2) {
                        if (!shippingTplInfoService.verifyUserAddress(Math.toIntExact(cart.getShippingTplId()), selectedAddress.getAddressId())) {
                            return Map.of();
                        }
                    }
                }
            }
        }
        for (CartByStoreVO storeCart : carts.getCartList()) {
            if (storeCart.getCarts() == null) {
                continue;
            }
            //取出cart里所有的商品id集合
            List<Integer> productIds = storeCart.getCarts().stream().map(CartVO::getProductId).toList();
            List<Long> shippingTplIds = getShippingTplIds(storeCart.getShopId(), productIds);
            List<OrderCheckVO.ShippingType> availableShippingType = getAvailableShippingType(storeCart.getShopId(), shippingTplIds, regionIds, storeCart.getCarts(), selectedAddress);
            //获取可用的配送方式并全部插入到availableShippingTypeList中
            availableShippingTypeList.put(storeCart.getShopId(), availableShippingType);
        }
        if (Objects.equals(query.getFlowType(), CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode())) {
            renameProxyShoppingPickup(availableShippingTypeList);
        }
        return availableShippingTypeList;
    }

    private void renameProxyShoppingPickup(Map<Integer, List<OrderCheckVO.ShippingType>> availableShippingTypeList) {
        for (List<OrderCheckVO.ShippingType> shippingTypes : Optional.ofNullable(availableShippingTypeList)
                .orElse(Collections.emptyMap()).values()) {
            for (OrderCheckVO.ShippingType shippingType : Optional.ofNullable(shippingTypes).orElse(Collections.emptyList())) {
                if (Objects.equals(shippingType.getShippingTypeId(), OrderDeliveryTypeEnum.PICKUP.getCode())) {
                    shippingType.setShippingTypeName("到店自提");
                    shippingType.setOriginShippingTypeName("到店自提");
                }
            }
        }
    }

    @Override
    public List<OrderCheckVO.PaymentType> getAvailablePaymentType(Integer flowType) {

        ConfigPO useOfflineConfig = configService.getConfigByCode(SettingsEnum.USE_OFFLINE.getBizCode());
        Boolean isOffline = "1".equals(useOfflineConfig.getBizVal());
        List<OrderCheckVO.PaymentType> result = new ArrayList<>();

        if (flowType != null && flowType == 12) {
            OrderCheckVO.PaymentType gitCardPayment = new OrderCheckVO.PaymentType();
            gitCardPayment.setTypeId(PaymentType.ONLINE.getCode());
            gitCardPayment.setTypeName(messageHelper.get("payment.type.gift.card"));
            gitCardPayment.setDisabled(false);
            gitCardPayment.setDisabledDesc("");
            gitCardPayment.setIsShow(true);
            result.add(gitCardPayment);
            return result;
        }


        boolean isSurplus;
        try {
            String useSurplus = configService.getConfigByCode(USE_SURPLUS.getBizCode()).getBizVal();
            isSurplus = "1".equals(useSurplus);
        } catch (Exception e) {
            isSurplus = false;
        }

        if (isSurplus || CollUtil.isNotEmpty(paymentService.getAvailablePayment(""))) {
            OrderCheckVO.PaymentType onlinePayment = new OrderCheckVO.PaymentType();
            onlinePayment.setTypeId(PaymentType.ONLINE.getCode());
            onlinePayment.setTypeName(messageHelper.get("payment.type.online"));
            onlinePayment.setDisabled(false);
            onlinePayment.setDisabledDesc("");
            onlinePayment.setIsShow(true);
            result.add(onlinePayment);
        }


        OrderCheckVO.PaymentType offlinePayment = new OrderCheckVO.PaymentType();
        offlinePayment.setTypeId(PaymentType.OFFLINE.getCode());
        offlinePayment.setTypeName(messageHelper.get("payment.type.offline"));
        offlinePayment.setDisabled(false);
        offlinePayment.setDisabledDesc("");
        offlinePayment.setIsShow(isOffline);
        result.add(offlinePayment);

        return result;
    }

    public List<Long> getShippingTplIds(Integer shopId, List<Integer> productIds) {
        // Long defaultId = shippingTplService.getDefaultShippingTplId(shopId);
        List<Product> productList = productService.list(new LambdaQueryWrapper<Product>().in(Product::getProductId, productIds).select(Product::getShippingTplId));
        List<Long> tplIds = productList.stream().map(Product::getShippingTplId).collect(Collectors.toList());
        if (tplIds.contains(0L)) {
//            tplIds.add(defaultId);
            tplIds = tplIds.stream().filter(item -> item != 0).toList();
        }
        return new ArrayList<>(new HashSet<>(tplIds));
    }

//    public int getAmountValuePoints(double amount) {
//        JSONObject shoppingSetting = configService.getConfigByCode("base_shopping").getData();
//        Integer scale = shoppingSetting.get("points_setting").get("integral_scale");
//        return scale > 0 ? (int) (amount / scale * 100) : 0;
//    }

    public List<OrderCheckVO.ShippingType> getAvailableShippingType(Integer shopId, List<Long> shippingTplIds, List<Integer> regionIds, List<CartVO> storeCarts, UserAddressVO selectedAddress) {
        if (tigshopProperties.getIsO2o() == 1) {
            return buildO2oShippingTypes(shopId, storeCarts, selectedAddress);
        }
//
//        ConfigPO childAreaNeedRegionConfig = configService.getConfigByCode(SettingsEnum.CHILD_AREA_NEED_REGION.getBizCode());
//        String needRegion = StrUtil.isNotBlank(childAreaNeedRegionConfig.getBizVal()) ? childAreaNeedRegionConfig.getBizVal() : "0";
//        List<Long> availableTplTypeIds = new ArrayList<>();
//        List<Long> allTplTypeIds = new ArrayList<>();
//        List<Long> resultTplIds = new ArrayList<>();
//        int i = 0;
//        for (Long tplId : shippingTplIds) {
//            if ("1".equals(needRegion)) {
//                List<ShippingTplInfo> shippingTplInfos = shippingTplInfoService.list(new LambdaUpdateWrapper<ShippingTplInfo>().eq(ShippingTplInfo::getShippingTypeId, tplId).eq(ShippingTplInfo::getIsDefault, 1));
//                if (shippingTplInfos != null) {
//                    for (ShippingTplInfo shippingTplInfo : shippingTplInfos) {
//                        if (fetchRegion(regionIds, shippingTplInfo.getRegionData())) {
//                            availableTplTypeIds.add(shippingTplInfo.getShippingTypeId());
//                        }
//                    }
//                }
//            }
//            List<ShippingTplInfo> list = shippingTplInfoService.list(new LambdaQueryWrapper<ShippingTplInfo>().eq(ShippingTplInfo::getShippingTplId, tplId));
//            if (list != null) {
//                for (ShippingTplInfo shippingTplInfo : list) {
//                    allTplTypeIds.add(shippingTplInfo.getShippingTypeId());
//                }
//            }
//            allTplTypeIds = allTplTypeIds.stream().distinct().collect(Collectors.toList());
//            resultTplIds = i != 0 ? resultTplIds.stream().filter(availableTplTypeIds::contains).toList() : allTplTypeIds;
//            i++;
//        }
//        if ("1".equals(needRegion)) {
//            resultTplIds = resultTplIds.stream().filter(availableTplTypeIds::contains).toList();
//        }
//        List<OrderCheckVO.ShippingType> shippingTypeList = new ArrayList<>();
//
//        OrderCheckVO.ShippingType shippingType = new OrderCheckVO.ShippingType();
//        shippingType.setShippingTypeId(1);
//        shippingType.setShopId(shopId);
//        shippingType.setShippingTypeName(configService.getConfigVal(SettingsEnum.DEFAULT_LOGISTICS_NAME));
//        shippingTypeList.add(shippingType);
//        if (resultTplIds.isEmpty()) {
//            return shippingTypeList;
//        }
//        List<ShippingType> shippingTypes = shippingTypeService.list(new LambdaQueryWrapper<ShippingType>().in(ShippingType::getShippingTypeId, resultTplIds));
//        for (ShippingType shippingType : shippingTypes) {
//            OrderCheckVO.ShippingType shippingType1 = new OrderCheckVO.ShippingType();
//            BeanUtil.copyProperties(shippingType, shippingType1);
//            shippingTypeList.add(shippingType1);
//        }

        List<OrderCheckVO.ShippingType> shippingTypeList = new ArrayList<>();

        OrderCheckVO.ShippingType shippingType = new OrderCheckVO.ShippingType();
        shippingType.setShippingTypeId(1);
        shippingType.setShopId(shopId);
        shippingType.setShippingTypeName(configService.getConfigVal(SettingsEnum.DEFAULT_LOGISTICS_NAME));
        shippingType.setOriginShippingTypeName(shippingType.getShippingTypeName());
        shippingTypeList.add(shippingType);
        return shippingTypeList;
    }

    private UserAddressVO resolveSelectedAddress(Integer flowType, Integer userId, Integer addressId) {
        if (addressId != null) {
            return userAddressService.detail(addressId);
        }
        if (Objects.equals(flowType, CartTypeEnum.TYPE_ADMIN_SHOPPING.getCode()) && userId != null) {
            return userAddressService.getUserSystemSelectedAddress(userId);
        }
        return userAddressService.getUserSelectedAddress();
    }

    /**
     * 根据配送方式获取支持的商品ID集合
     */
    private Set<Integer> getDeliveryTypeSupportedProductIds(DeliveryOptionVO deliveryOption, Integer deliveryType) {
        if (deliveryOption == null) {
            return Collections.emptySet();
        }
        if (Objects.equals(deliveryType, OrderDeliveryTypeEnum.LOGISTICS.getCode())) {
            return CollUtil.isNotEmpty(deliveryOption.getDeliveryProducts())
                    ? deliveryOption.getDeliveryProducts().stream().map(Product::getProductId).collect(Collectors.toSet())
                    : Collections.emptySet();
        } else if (Objects.equals(deliveryType, OrderDeliveryTypeEnum.SAME_CITY.getCode())) {
            return CollUtil.isNotEmpty(deliveryOption.getShopDeliveryProducts())
                    ? deliveryOption.getShopDeliveryProducts().stream().map(Product::getProductId).collect(Collectors.toSet())
                    : Collections.emptySet();
        } else if (Objects.equals(deliveryType, OrderDeliveryTypeEnum.PICKUP.getCode())) {
            if (CollUtil.isNotEmpty(deliveryOption.getPickupProducts())) {
                return deliveryOption.getPickupProducts().stream()
                        .flatMap(info -> info.getProducts().stream())
                        .map(Product::getProductId)
                        .collect(Collectors.toSet());
            }
            return Collections.emptySet();
        }
        return Collections.emptySet();
    }

    private List<OrderCheckVO.ShippingType> buildO2oShippingTypes(Integer shopId, List<CartVO> storeCarts, UserAddressVO selectedAddress) {
        if (CollUtil.isEmpty(storeCarts)) {
            return Collections.emptyList();
        }
        List<Cart> carts = storeCarts.stream().map(this::toDeliveryCart).toList();
        DeliveryOptionVO deliveryOption = productService.getDeliveryOption(carts, null, null, null, selectedAddress);
        List<OrderCheckVO.ShippingType> shippingTypeList = new ArrayList<>();
        if (CollUtil.isNotEmpty(deliveryOption.getDeliveryProducts())) {
            addShippingType(shippingTypeList, shopId, OrderDeliveryTypeEnum.LOGISTICS.getCode(), configService.getConfigVal(SettingsEnum.DEFAULT_LOGISTICS_NAME));
        }
        if (CollUtil.isNotEmpty(deliveryOption.getPickupProducts())) {
            addShippingType(shippingTypeList, shopId, OrderDeliveryTypeEnum.PICKUP.getCode(), OrderDeliveryTypeEnum.PICKUP.getDesc());
        }
        if (CollUtil.isNotEmpty(deliveryOption.getShopDeliveryProducts())) {
            addShippingType(shippingTypeList, shopId, OrderDeliveryTypeEnum.SAME_CITY.getCode(), OrderDeliveryTypeEnum.SAME_CITY.getDesc());
        }
        return shippingTypeList;
    }

    private Cart toDeliveryCart(CartVO cartVO) {
        Cart cart = new Cart();
        cart.setProductId(cartVO.getProductId());
        cart.setShopId(cartVO.getShopId());
        return cart;
    }

    private void addShippingType(List<OrderCheckVO.ShippingType> shippingTypeList, Integer shopId, Integer shippingTypeId, String shippingTypeName) {
        boolean exists = shippingTypeList.stream().anyMatch(item -> Objects.equals(item.getShippingTypeId(), shippingTypeId));
        if (exists) {
            return;
        }
        OrderCheckVO.ShippingType shippingType = new OrderCheckVO.ShippingType();
        shippingType.setShippingTypeId(shippingTypeId);
        shippingType.setShopId(shopId);
        shippingType.setShippingTypeName(shippingTypeName);
        shippingType.setOriginShippingTypeName(shippingTypeName);
        shippingTypeList.add(shippingType);
    }

    boolean fetchRegion(List<Integer> addressRegions, String regionData) {
        // 空值检查
        if (addressRegions == null || addressRegions.isEmpty()) {
            return false;
        }

        // 解析区域数据
        RegionDataParam regionDataBean = JSONUtil.toBean(regionData, RegionDataParam.class);
        if (regionDataBean == null || regionDataBean.getAreaRegions() == null) {
            return false;
        }

        // 检查所有区域ID是否匹配
        for (List<Long> region : regionDataBean.getAreaRegions()) {
            Long last = region.getLast();
            if (last != null && last.intValue() == 1) {
                return true;
            }
            if (last != null && addressRegions.contains(last.intValue())) {
                return true; // 只要有一个匹配就返回true
            }
        }
        return false;
    }



    private Integer resolveReservationSkuId(Integer productId, Integer skuId) {
        if (skuId != null && skuId > 0) {
            return skuId;
        }
        List<ProductSku> productSkus = productSkuService.lambdaQuery()
                .eq(ProductSku::getProductId, productId)
                .list();
        if (CollUtil.isEmpty(productSkus)) {
            return 0;
        }
        if (productSkus.size() == 1) {
            return productSkus.get(0).getSkuId();
        }
        throw new GlobalException("多规格预约商品必须选择规格");
    }

    /**
     * 处理预售订单创建
     *
     * @param order    订单
     * @param cartList 购物车列表
     */
    private void handlePresaleOrder(Order order, CartListByStoreVO cartList, OrderSubmitVO orderSubmitVO) {
        // 检查是否有预售商品
        boolean hasPresaleProduct = false;
        Integer presaleSaleMode = null;
        Integer presaleProductId = null;
        Integer presaleProductSkuId = null;
        Integer presaleQuantity = null;

        for (CartByStoreVO cartByStore : cartList.getCartList()) {
            for (CartVO cart : cartByStore.getCarts()) {
                if (cart.getSaleMode() != null && cart.getSaleMode() > 0) {
                    hasPresaleProduct = true;
                    presaleSaleMode = cart.getSaleMode();
                    presaleProductId = cart.getProductId();
                    presaleProductSkuId = cart.getSkuId();
                    presaleQuantity = cart.getQuantity();
                    break;
                }
            }
            if (hasPresaleProduct) {
                break;
            }
        }

        // 如果不是预售商品，直接返回
        if (!hasPresaleProduct) {
            return;
        }

        // 更新订单预售标记
        Order updOrder = new Order();
        updOrder.setIsPresale(1);
        updOrder.setPresaleType(presaleSaleMode);
        updOrder.setOrderId(order.getOrderId());
        orderService.updateById(updOrder);

        // 查询商品预售配置
        ProductPresale productPresaleConfig = productPresaleService.getByProductId(presaleProductId);

        if (productPresaleConfig == null) {
            log.warn("预售商品[{}]没有预售配置", presaleProductId);
            return;
        }

        // 构建预售订单DTO
        OrderPresaleDTO presaleDTO = new OrderPresaleDTO();
        presaleDTO.setProductId(presaleProductId);
        presaleDTO.setSaleMode(presaleSaleMode);

        BigDecimal actualPayDepositAmount = BigDecimal.ZERO;
        // 根据预售模式设置相关信息
        if (presaleSaleMode == 1) {
            // 全款预售
            presaleDTO.setDepositAmount(BigDecimal.ZERO);
            presaleDTO.setFinalPaymentAmount(BigDecimal.ZERO);
        } else if (presaleSaleMode == 2) {
            // 定金预售 - 实时计算定金金额（单件定金 × 数量）
            BigDecimal depositAmount = productPresaleService.calculateDepositAmount(presaleProductId, presaleProductSkuId, productPresaleConfig)
                    .multiply(BigDecimal.valueOf(Optional.ofNullable(presaleQuantity).orElse(1)));

            // 尾款金额 = 订单总金额 - 定金金额 - 余额
            BigDecimal finalPaymentAmount = order.getTotalAmount().subtract(depositAmount);
            if (finalPaymentAmount.compareTo(BigDecimal.ZERO) < 0) {
                finalPaymentAmount = BigDecimal.ZERO;
            }

            //定金
            presaleDTO.setDepositAmount(depositAmount);
            //实际支付定金
            actualPayDepositAmount = depositAmount.subtract(Optional.ofNullable(order.getBalance()).orElse(BigDecimal.ZERO));
            //尾款
            presaleDTO.setFinalPaymentAmount(finalPaymentAmount);
            presaleDTO.setFinalPaymentRelativeDays(productPresaleConfig.getFinalPaymentRelativeDays());
        }

        // 设置发货信息
        if (productPresaleConfig.getDeliveryType() == 1) {
            // 相对时间
            presaleDTO.setDeliveryDays(productPresaleConfig.getDeliveryRelativeDays());
        } else if (productPresaleConfig.getDeliveryType() == 2) {
            // 指定日期
            presaleDTO.setDeliveryDate(productPresaleConfig.getDeliveryDate());
        }

        // 创建预售订单记录
        boolean created = orderPresaleService.createPresaleOrder(order.getOrderId(), order.getOrderSn(), presaleDTO);

        if (!created) {
            log.error("创建预售订单记录失败，订单ID：{}", order.getOrderId());
        } else {
            log.info("创建预售订单成功，订单ID：{}，预售类型：{}", order.getOrderId(), presaleSaleMode);
        }

        //如果实际的定金金额<=0的话,就创建一个余额的paylog
        if (actualPayDepositAmount.compareTo(BigDecimal.ZERO) <= 0 && presaleSaleMode == 2) {
            Paylog paylog = paylogService.creatPayLog(null, new OrderVO(order), "balance", 0, presaleDTO.getDepositAmount());
            aliPayService.paySuccess(paylog.getPaySn(), "0");
            orderSubmitVO.setReturnType(2);
        }
    }

}
