// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.order.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.toolkit.StrUtils;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.cart.AddToCartDTO;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.dto.o2o.DeliveryOrderCreateDTO;
import com.tigshop.bean.dto.order.*;
import com.tigshop.bean.dto.order.deliver.*;
import com.tigshop.bean.dto.print.PrintOrderDTO;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.dto.shop.OrderConfigDataDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.cart.CartTypeEnum;
import com.tigshop.bean.enums.user.AuthorizeTypeEnum;
import com.tigshop.bean.enums.finance.AccountType;
import com.tigshop.bean.enums.finance.EntryType;
import com.tigshop.bean.enums.finance.StatementType;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.enums.msg.MessageTypeEnum;
import com.tigshop.bean.enums.o2o.DeliveryOrderStatusEnum;
import com.tigshop.bean.enums.o2o.DeliveryOrderTypeEnum;
import com.tigshop.bean.enums.o2o.ReservationStatusEnum;
import com.tigshop.bean.enums.o2o.ShipmentStatusEnum;
import com.tigshop.bean.enums.order.*;
import com.tigshop.bean.enums.print.PrintStatusEnum;
import com.tigshop.bean.enums.promotion.JoinStatusEnum;
import com.tigshop.bean.enums.promotion.ValidityTypeEnum;
import com.tigshop.bean.enums.salesman.DistributionModeEnum;
import com.tigshop.bean.enums.salesman.SalesmanConfigTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.shop.ShopType;
import com.tigshop.bean.feign.shipping.ShippingResult;
import com.tigshop.bean.feign.wechat.GetTokenResult;
import com.tigshop.bean.feign.wechat.UploadShippingInfoParam;
import com.tigshop.bean.feign.wechat.UploadShippingInfoResult;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.enums.order.PayMethodType;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.model.finance.RecordRate;
import com.tigshop.bean.model.finance.StatementOutput;
import com.tigshop.bean.model.o2o.*;
import com.tigshop.bean.model.order.*;
import com.tigshop.bean.model.print.Print;
import com.tigshop.bean.model.product.Comment;
import com.tigshop.bean.model.product.ECard;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.promotion.GiftCardGroups;
import com.tigshop.bean.model.promotion.GiftCardOrder;
import com.tigshop.bean.model.promotion.GiftCards;
import com.tigshop.bean.model.content.WechatLiveOrderRel;
import com.tigshop.bean.model.content.WechatLiveSyncLog;
import com.tigshop.bean.model.promotion.GrouponPromotionRecord;
import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.model.salesman.SalesmanConfig;
import com.tigshop.bean.model.salesman.SalesmanOrder;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.setting.LogisticsCompany;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.bean.model.shop.CollectShop;
import com.tigshop.bean.model.shop.OrderConfig;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.*;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.param.finance.statement.StatementOutputSaveParam;
import com.tigshop.bean.param.finance.statement.StatementSaveParam;
import com.tigshop.bean.param.order.*;
import com.tigshop.bean.query.order.AftersalesListPageQuery;
import com.tigshop.bean.query.order.OrderListPageQuery;
import com.tigshop.service.user.UserMemberTagService;
import com.tigshop.bean.query.panel.SalesProductDetailPageQuery;
import com.tigshop.bean.utils.WechatLiveOrderContextUtils;
import com.tigshop.bean.utils.VendorOrderPriceCalculator;
import com.tigshop.bean.vo.config.GetStoreSelfOperatedVO;
import com.tigshop.bean.vo.finance.OrderInvoiceVO;
import com.tigshop.bean.vo.o2o.PickupListVO;
import com.tigshop.bean.vo.order.*;
import com.tigshop.bean.vo.panel.SalesProductOrderItemVO;
import com.tigshop.bean.vo.product.ProductPresaleVO;
import com.tigshop.bean.vo.promotion.card.GiftCardOrderVO;
import com.tigshop.bean.vo.setting.config.OrderSettingsVO;
import com.tigshop.bean.vo.shop.ShopVO;
import com.tigshop.bean.vo.user.UserAddressVO;
import com.tigshop.bean.vo.user.UserBaseVO;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.enums.DataType;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.*;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.feign.WechatApiClient;
import com.tigshop.service.oauth.WechatOAuthService;
import com.tigshop.service.print.PrintRoutingService;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.mapper.finance.PaylogMapper;
import com.tigshop.mapper.finance.StatementOutputMapper;
import com.tigshop.mapper.o2o.UserPickupInfoMapper;
import com.tigshop.mapper.order.AftersalesMapper;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.print.PrintMapper;
import com.tigshop.mapper.product.CommentMapper;
import com.tigshop.mapper.promotion.GiftCardGroupsMapper;
import com.tigshop.mapper.promotion.GiftCardOrderMapper;
import com.tigshop.mapper.promotion.GiftCardsMapper;
import com.tigshop.mapper.salesman.SalesmanConfigMapper;
import com.tigshop.mapper.salesman.SalesmanMapper;
import com.tigshop.mapper.salesman.SalesmanOrderMapper;
import com.tigshop.mapper.setting.LogisticsCompanyMapper;
import com.tigshop.mapper.setting.RegionMapper;
import com.tigshop.mapper.shop.CollectShopMapper;
import com.tigshop.mapper.shop.OrderConfigMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.user.CollectProductMapper;
import com.tigshop.mapper.user.UserAuthorizeMapper;
import com.tigshop.mapper.user.UserCouponMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.mapper.vendor.VendorMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.PaylogService;
import com.tigshop.service.finance.RecordRateService;
import com.tigshop.service.finance.StatementOutputService;
import com.tigshop.service.logistics.LogisticsProvider;
import com.tigshop.service.logistics.LogisticsProviderRouter;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.msgTemplate.MessageService;
import com.tigshop.service.order.*;
import com.tigshop.service.product.ECardService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.product.StockWarningService;
import com.tigshop.service.product.impl.ProductServiceImpl;
import com.tigshop.service.promotion.GrouponPromotionDetailService;
import com.tigshop.service.promotion.GrouponPromotionRecordService;
import com.tigshop.service.promotion.SeckillService;
import com.tigshop.service.content.WechatLiveOrderRelService;
import com.tigshop.service.content.WechatLiveOrderSyncService;
import com.tigshop.service.content.WechatLiveSyncLogService;
import com.tigshop.service.salesman.SalesmanOrderService;
import com.tigshop.service.setting.*;
import com.tigshop.service.shop.PickupService;
import com.tigshop.service.shop.ShopService;
import com.tigshop.service.user.UserAddressService;
import com.tigshop.service.user.UserBalanceService;
import com.tigshop.service.user.UserPointsLogService;
import com.tigshop.service.user.UserRankService;
import com.tigshop.service.user.MemberBenefitService;
import com.tigshop.service.user.impl.UserServiceImpl;
import com.tigshop.service.wholesale.WholesaleSettingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.bean.enums.o2o.ShipmentStatusEnum.NOT_SHIPPED;
import static com.tigshop.bean.enums.o2o.ShipmentStatusEnum.SHIPPED;
import static com.tigshop.bean.enums.order.OrderStatusEnum.PENDING;
import static com.tigshop.bean.enums.product.ProductType.*;
import static com.tigshop.bean.enums.setting.SettingsEnum.COLLECTION_TIME_SETTING;
import static com.tigshop.common.constant.Constants.*;
import static com.tigshop.common.constant.HttpStatus.OPT_ALTER_SUCCESS_CODE;
import static com.tigshop.common.constant.content.WechatLiveOrderSyncConstants.*;
import static com.tigshop.common.constant.order.OrderConstants.*;
import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 订单管理服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class OrderServiceImpl extends BaseServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderItemService orderItemService;
    private final UserMemberTagService userMemberTagService;
    private final UserMapper userMapper;
    private final OrderAftersalesService orderAfterSalesService;
    private final OrderLogService orderLogService;
    private final OrderAmountDetailService orderAmountDetailService;
    private final OrderCouponDetailService orderCouponDetailService;
    private final OrderSpitLogService orderSpitLogService;
    private final RegionService regionService;
    private final HttpServletRequest request;
    private final LogisticsApiLogService logisticsApiLogService;
    private final AdminUserMapper adminUserMapper;
    private final ShopMapper shopMapper;
    private final AftersalesItemService aftersalesItemService;
    private final CollectProductMapper collectProductMapper;
    private final CollectShopMapper collectShopMapper;
    private final LogisticsCompanyMapper logisticsCompanyMapper;
    private final LogisticsProviderRouter logisticsProviderRouter;
    private final LogisticsCompanyService logisticsCompanyService;
    private final ConfigService configService;

    private final AfterSalesReasonConfigService afterSalesReasonConfigService;
    private final CommentMapper commentMapper;
    private final AftersalesMapper aftersalesMapper;
    private final OrderConfigMapper orderConfigMapper;
    private final PaylogMapper paylogMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserBalanceService userBalanceService;
    private final UserPointsLogService userPointsLogService;
    private final UserCouponMapper userCouponMapper;
    private final ProductService productService;
    private final ProductSkuService productSkuService;
    private final SeckillService seckillService;
    private final AdminMsgService adminMsgService;
    private final StockWarningService stockWarningService;
    private final TranslatePackageImpl translatePackage;
    private final com.tigshop.common.i18n.MessageHelper messageHelper;
    private final RabbitTemplate rabbitTemplate;
    private final UserRankService userRankService;
    private final MemberBenefitService memberBenefitService;
    private final MessageService messageService;
    private final WechatApiClient wechatApiClient;
    private final WechatOAuthService wechatOAuthService;
    private final UserAuthorizeMapper userAuthorizeMapper;
    private final AftersalesService aftersalesService;
    private final SalesmanOrderMapper salesmanOrderMapper;
    private final SalesmanMapper salesmanMapper;
    private final SalesmanConfigMapper salesmanConfigMapper;
    private final PrintMapper printMapper;
    private final PrintRoutingService printRoutingService;
    private final WholesaleSettingService wholesaleSettingService;

    // 店铺会员绑定：@Lazy 打破 order→shopMember→user/shop→…→order 循环依赖；
    // 通用能力（不随 wholesale 版型裁剪），故置于条件编译块之外。
    @org.springframework.context.annotation.Lazy
    @jakarta.annotation.Resource
    private com.tigshop.service.user.ShopMemberService shopMemberService;

    private final TigshopProperties tigshopProperties;
    private final VendorMapper vendorMapper;
    private final StatementOutputMapper statementOutputMapper;
    private final StatementOutputService statementOutputService;
    private final RecordRateService recordRateService;
    private final ECardService eCardService;
    private final GrouponPromotionDetailService grouponPromotionDetailService;
    private final GrouponPromotionRecordService grouponPromotionRecordService;
    private final PickupService pickupService;
    private final UserPickupInfoMapper userPickupInfoMapper;
    private final OrderReservationService orderReservationService;
    private final ApplicationContext applicationContext;
    private final PaylogService paylogService;
    private final SalesmanOrderService salesmanOrderService;
    private final WechatLiveOrderRelService wechatLiveOrderRelService;
    private final WechatLiveSyncLogService wechatLiveSyncLogService;
    private final WechatLiveOrderSyncService wechatLiveOrderSyncService;
    private final GiftCardOrderMapper giftCardOrderMapper;
    private final GiftCardsMapper giftCardsMapper;
    private final UserAddressService userAddressService;
    private final OrderPresaleService orderPresaleService;
    private final RegionMapper regionMapper;



    @Autowired
    private ObjectProvider<UserServiceImpl> userServicesProvider;


    @Autowired
    private GiftCardGroupsMapper giftCardGroupsMapper;

    @Autowired
    private GalleryPicService galleryPicService;

    @Autowired
    private ObjectProvider<ShopService> shopService;




    @Override
    public Page<OrderVO> list(OrderListPageQuery pageQuery) {
        return list(pageQuery, pageQuery.getSize());
    }

    @Override
    public Page<OrderVO> list(OrderListPageQuery pageQuery, Integer size) {
        Page<Order> page = buildSortOrder(pageQuery);

        page.setSize(size);

        // 构造查询构造器
        // LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        MPJLambdaWrapper<Order> queryWrapper = new MPJLambdaWrapper<>();
        buildQueryWrapper(queryWrapper, pageQuery);

        // 待晒单
        if (pageQuery.getIsShowed() != null && pageQuery.getIsShowed() > -1) {
            List<Comment> comments = commentMapper.selectList(Wrappers.lambdaQuery(Comment.class).eq(Comment::getUserId, pageQuery.getUserId()).eq(Comment::getIsShowed, pageQuery.getIsShowed()));
            // 取出评论的orderId
            List<Integer> orderIds = comments.stream().map(Comment::getOrderId).toList();
            if (orderIds.isEmpty()) {
                return new Page<>();
            }
            queryWrapper.in(Order::getOrderId, orderIds);
        }
        // 如果查询参数包含了支付方式，那么联表筛选出已支付的相关支付方式的订单,如果是余额支付则不需要联表查询
        if (StrUtil.isNotBlank(pageQuery.getPayCode()) && !pageQuery.getPayCode().equals("balance")) {
            queryWrapper.leftJoin(Paylog.class, Paylog::getOrderId, Order::getOrderId)
                    // 商品订单：orderType=0，避免关联到撞号的充值单支付记录
                    .eq(Paylog::getOrderType, 0)
                    .eq(Paylog::getPayCode, pageQuery.getPayCode())
                    .eq(Paylog::getPayStatus, PayStatusEnum.PAID.getCode())
                    .groupBy(Order::getOrderId);
        }
        // 发货状态不查平团失败的订单 还有过滤掉已付款待成团的订单
        // 发货状态不查平团失败的订单 还有过滤掉已付款待成团的订单
        List<Integer> orderStatusList = pageQuery.getOrderStatus() == null || pageQuery.getOrderStatus().isEmpty()
                ? List.of()
                : Arrays.stream(pageQuery.getOrderStatus().split(","))
                .map(Integer::parseInt)
                .toList();
        if (orderStatusList.contains(OrderStatusEnum.CONFIRMED.getCode())) {
            queryWrapper.leftJoin(GrouponPromotionRecord.class, GrouponPromotionRecord::getOrderId, Order::getOrderId)
                    .and(wrapper -> wrapper
                            .isNull(GrouponPromotionRecord::getJoinStatus)
                            .or()
                            .eq(GrouponPromotionRecord::getJoinStatus, JoinStatusEnum.SUCCESS.getCode())
                    );
        }

        // 执行查询
        Page<Order> orderPage = this.page(page, queryWrapper);

        // 获取查询结果
        List<Order> orderPageRecords = orderPage.getRecords();
        if (orderPageRecords.isEmpty()) {
            return new Page<>();
        }

        List<Integer> userIds = orderPageRecords.stream().map(Order::getUserId).distinct().toList();
        Map<Integer, User> userMap = new HashMap<>();
        if (CollUtil.isNotEmpty(userIds)) {
            List<User> userList = userMapper.selectBatchIds(userIds);
            userMap = userList.stream().collect(Collectors.toMap(User::getUserId, Function.identity()));
        }

        // 调用方法获取订单信息
        List<Integer> orderIds = orderPageRecords.stream().map(Order::getOrderId).toList();
        List<OrderItemVO> itemListNew = getOrderItemList(orderIds);
        Map<Integer, List<WechatLiveOrderRel>> liveRelationMap = wechatLiveOrderRelService.listByOrderIds(orderIds).stream()
                .collect(Collectors.groupingBy(WechatLiveOrderRel::getOrderId));
        Map<Integer, WechatLiveSyncLog> liveSyncLogMap = wechatLiveSyncLogService.listLatestByOrderIds(orderIds).stream()
                .collect(Collectors.toMap(WechatLiveSyncLog::getOrderId, Function.identity(), (left, right) -> left));

        // 插入售后数据
        Map<Integer, List<OrderItemVO>> itemMap = itemListNew.stream().collect(Collectors.groupingBy(OrderItemVO::getOrderId));
        // 取出店铺Id
        List<Integer> shopIds = orderPageRecords.stream().map(Order::getShopId).distinct().toList();
        List<Shop> shopList = shopMapper.selectBatchIds(shopIds);
        Map<Integer, Shop> shopMap = shopList.stream().collect(Collectors.toMap(Shop::getShopId, Function.identity()));

        // 查询店铺结算配置
        List<OrderConfig> orderConfigs = orderConfigMapper.selectList(
                new LambdaQueryWrapper<OrderConfig>()
                        .in(OrderConfig::getShopId, shopIds)
        );
        Map<Integer, OrderConfig> orderConfigMap = orderConfigs.stream().collect(Collectors.toMap(OrderConfig::getShopId, Function.identity()));

        // 获取订单配置
        OrderSettingsVO orderSettings = configService.getSettings(OrderSettingsVO.class);

        // 转换为VO
        Map<Integer, User> finalUserMap = userMap;
        String afterSalesLimitDays = configService.getConfigVal(SettingsEnum.AFTER_SALES_LIMIT_DAYS);
        String autoDeliveryDays = configService.getConfigVal(SettingsEnum.AUTO_DELIVERY_DAYS);

        // ===== N+1 批量预取：按当前页 orderIds/vendorIds 一次性载入，替换循环内逐行查询 =====
        // 售后：一次载入本页订单全部售后行（orderId + status），循环内在 Java 侧分别算 IN_REVIEW / 非终态 / COMPLETE 计数
        List<Aftersales> pageAftersalesList = aftersalesMapper.selectList(
                Wrappers.lambdaQuery(Aftersales.class)
                        .in(Aftersales::getOrderId, orderIds)
                        .select(Aftersales::getAftersaleId, Aftersales::getOrderId, Aftersales::getStatus));
        Map<Integer, List<Aftersales>> aftersalesByOrderId = pageAftersalesList.stream()
                .collect(Collectors.groupingBy(Aftersales::getOrderId));
        // 本页中「已完成但平台财务退款未到账」的售后，一次查出，循环内只做集合判断
        Set<Integer> pendingFinanceRefundIds = aftersalesService.findPendingFinanceRefundIds(pageAftersalesList);
        // 支付日志：一次载入本页已支付支付日志，按 orderId 分组（循环内仍取 getLast 保持原语义）
        Map<Integer, List<Paylog>> paylogByOrderId = paylogMapper.selectList(
                        Wrappers.lambdaQuery(Paylog.class)
                                .eq(Paylog::getPayStatus, PayStatusEnum.PAID.getCode())
                                // 商品订单：orderType=0，避免撞充值单号
                                .eq(Paylog::getOrderType, 0)
                                .in(Paylog::getOrderId, orderIds)
                                .select(Paylog::getOrderId, Paylog::getPaySn, Paylog::getPayCode, Paylog::getTransactionId))
                .stream().collect(Collectors.groupingBy(Paylog::getOrderId));
        // 供应商：一次载入本页 distinct vendorId
        List<Integer> vendorIds = orderPageRecords.stream().map(Order::getVendorId)
                .filter(vid -> vid != null && vid > 0).distinct().toList();
        Map<Integer, Vendor> vendorMap = vendorIds.isEmpty() ? Collections.emptyMap()
                : vendorMapper.selectOnboardedList(Wrappers.lambdaQuery(Vendor.class)
                        .in(Vendor::getVendorId, vendorIds)).stream()
                .collect(Collectors.toMap(Vendor::getVendorId, Function.identity(), (a, b) -> a));
        // 拼团记录：仅 PIN 订单
        List<Integer> pinOrderIds = orderPageRecords.stream()
                .filter(o -> OrderType.PIN.getCode() == o.getOrderType())
                .map(Order::getOrderId).toList();
        Map<Integer, GrouponPromotionRecord> grouponRecordByOrderId = pinOrderIds.isEmpty() ? Collections.emptyMap()
                : grouponPromotionRecordService.lambdaQuery()
                .in(GrouponPromotionRecord::getOrderId, pinOrderIds).list()
                .stream().filter(r -> r.getOrderId() != null)
                .collect(Collectors.toMap(r -> r.getOrderId().intValue(), Function.identity(), (a, b) -> a));
        // 预约信息：仅预约订单
        List<Integer> reservationOrderIds = orderPageRecords.stream()
                .filter(o -> OrderType.isReservationOrder(o.getOrderType()))
                .map(Order::getOrderId).toList();
        Map<Integer, OrderReservation> reservationByOrderId = reservationOrderIds.isEmpty() ? Collections.emptyMap()
                : orderReservationService.list(new LambdaQueryWrapper<OrderReservation>()
                        .in(OrderReservation::getOrderId, reservationOrderIds))
                .stream().collect(Collectors.toMap(OrderReservation::getOrderId, Function.identity(), (a, b) -> a));

        List<OrderVO> orderVOList = orderPageRecords.stream().map(order -> {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);


            orderVO.setRegionIds(JsonUtil.jsonToList(order.getRegionIds(), Integer.class));
            orderVO.setRegionNames(JsonUtil.jsonToList(order.getRegionNames(), String.class));

            Integer orderStatus = order.getOrderStatus();
            boolean isCycleMainOrder = false;
            if (OrderType.isReservationOrder(order.getOrderType()) && !isCycleMainOrder) {
                orderVO.setOrderStatusName(OrderReservationStatusEnum.getStatusName(order.getOrderStatus()));
            } else if (Objects.equals(order.getShippingTypeId(), OrderDeliveryTypeEnum.SAME_CITY.getCode())) {
                orderVO.setOrderStatusName(OrderSameCityEnum.getStatusName(order.getOrderStatus()));
            } else {
                orderVO.setOrderStatusName(OrderStatusEnum.getStatusName(order.getOrderStatus()));
            }

            orderVO.setPayStatusName(PaymentStatus.getStatusName(order.getPayStatus()));
            orderVO.setShippingStatusName(ShippingStatusEnum.getStatusName(order.getShippingStatus()));
            orderVO.setInvoiceData(JSON.parseObject(order.getInvoiceData(), OrderInvoiceVO.class));
            if (orderVO.getInvoiceData() != null && StrUtil.isNotBlank(orderVO.getInvoiceData().getAddTime())) {
                orderVO.getInvoiceData().setAddTime(TigUtils.handelTime(Long.parseLong(orderVO.getInvoiceData().getAddTime())));
            }

            if (orderVO.getRegionNames() != null) {
                orderVO.setUserAddress(String.join(" ", orderVO.getRegionNames()) + " " + order.getAddress());
            }
            UserAddressVO addressData = JsonUtil.fromJson(order.getAddressData(), UserAddressVO.class);
            if (addressData != null && StrUtil.isBlank(addressData.getRegionName()) && CollUtil.isNotEmpty(addressData.getRegionNames())) {
                addressData.setRegionName(String.join(" ", addressData.getRegionNames()));
            }
            orderVO.setAddressData(addressData);

            DateTime addDate = DateUtil.date(order.getAddTime() * 1000);
            orderVO.setAddTime(DateUtil.format(addDate, DATE_FORMAT));

            if (order.getPayTime() > 0) {
                DateTime payDate = DateUtil.date(order.getPayTime() * 1000);
                orderVO.setPayTime(DateUtil.format(payDate, DATE_FORMAT));
            }

            if (order.getShippingTime() > 0) {
                DateTime shippingDate = DateUtil.date(order.getShippingTime() * 1000);
                orderVO.setShippingTime(DateUtil.format(shippingDate, DATE_FORMAT));
            }

            if (order.getReceivedTime() > 0) {
                DateTime receivedDate = DateUtil.date(order.getReceivedTime() * 1000);
                orderVO.setReceivedTime(DateUtil.format(receivedDate, DATE_FORMAT));
            }

            orderVO.setItems(itemMap.get(order.getOrderId()));
            VendorOrderPriceCalculator.apply(orderVO, SecurityUtils.isAdminUser());
            // 虚拟/卡密商品：未发货前买家端不展示虚拟内容与卡号卡密
            maskUndeliveredVirtualContent(orderVO);
            try{
                List<WechatLiveOrderRel> liveOrderRels = liveRelationMap.getOrDefault(order.getOrderId(), Collections.emptyList());
                orderVO.setWechatLiveOrder(!liveOrderRels.isEmpty());
                orderVO.setOrderSubSource(WechatLiveOrderContextUtils.extractSubSource(order.getOrderExtension()));
                orderVO.setWechatLiveContexts(WechatLiveOrderContextUtils.extractFromOrderExtension(order.getOrderExtension()));
                WechatLiveSyncLog liveSyncLog = liveSyncLogMap.get(order.getOrderId());
                if (liveSyncLog != null) {
                    orderVO.setLiveSyncStatus(liveSyncLog.getSyncStatus());
                    orderVO.setLiveSyncStatusName(getSyncStatusName(liveSyncLog.getSyncStatus()));
                    orderVO.setLiveSyncMessage(StrUtil.isNotBlank(liveSyncLog.getErrorMessage()) ? liveSyncLog.getErrorMessage() : liveSyncLog.getEventType());
                }
            }catch (Exception e){

            }

            UserBaseVO userBaseVO = new UserBaseVO();
            User user = finalUserMap.get(order.getUserId());
            if (user != null) {
                BeanUtils.copyProperties(user, userBaseVO);
            }
            orderVO.setUser(userBaseVO);
            orderVO.setAvailableActions(availableActions(orderVO));

            if (order.getShopId() != null && order.getShopId() > 0) {
                OrderVO.ShopVO shopVO = new OrderVO.ShopVO();
                Shop shop = shopMap.get(order.getShopId());
                if (shop != null) {
                    BeanUtils.copyProperties(shop, shopVO);
                    shopVO.setStatusText(ShopType.getTypeName(shop.getStatus()));
                    orderVO.setShop(shopVO);
                    orderVO.setShopName(shopVO.getShopTitle());
                    orderVO.setShopId(shopVO.getShopId());
                }
            }

            // 判断是否可以申请售后；1. 判断店铺结算配置（确认收货后立即不可售后）（自营店不用判断）2. 全局设置（买家申请售后限制）
            boolean toAftersales = orderVO.getAvailableActions().getToAftersales();
            orderVO.setToAftersalses(toAftersales);
            if (toAftersales) {
                // 1. 判断店铺结算配置（确认收货后立即不可售后）（自营店不用判断）
                if (order.getShopId() != null && order.getShopId() > 0) {
                    OrderConfig orderConfig = orderConfigMap.get(order.getShopId());
                    if (orderConfig != null) {
                        OrderConfigDTO orderConfigData = JsonUtil.fromJson(orderConfig.getData(), OrderConfigDTO.class);
                        if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus() && 0 == orderConfigData.getDateType()) {
                            orderVO.setToAftersalses(false);
                        }
                    }
                }
                // 2. 全局设置（买家申请售后限制）
                if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus()) {
                    if (StrUtil.isNotBlank(afterSalesLimitDays)) {
                        long receivedTime = order.getReceivedTime() * 1000;
                        BigDecimal afterSalesLimitDaysTime = new BigDecimal(afterSalesLimitDays).multiply(new BigDecimal(24 * 60 * 60 * 1000));
                        LocalDateTime limitDateTime = Instant.ofEpochMilli(receivedTime + afterSalesLimitDaysTime.longValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                        boolean toAftersalses = limitDateTime.isAfter(LocalDateTime.now());
                        orderVO.setToAftersalses(toAftersalses);
                    }
                }
            }
            orderVO.getAvailableActions().setToAftersales(orderVO.getToAftersalses());

            // 自动发货天数
            List<Aftersales> orderAftersalesList = aftersalesByOrderId.getOrDefault(order.getOrderId(), Collections.emptyList());
            long aftersalesCount = orderAftersalesList.stream()
                    .filter(a -> Objects.equals(a.getStatus(), AftersalesStatusEnum.IN_REVIEW.getCode()))
                    .count();
            if (aftersalesCount == 0 && OrderStatusEnum.PROCESSING.getCode() == order.getOrderStatus() && StrUtil.isNotBlank(autoDeliveryDays)) {
                long shippingTimeMillis = order.getShippingTime() * 1000;
                long elapsedMillis = Instant.now().toEpochMilli() - shippingTimeMillis;

                long elapsedDays = TimeUnit.MILLISECONDS.toDays(elapsedMillis);

                BigDecimal result = new BigDecimal(autoDeliveryDays).subtract(new BigDecimal(elapsedDays)).setScale(0, RoundingMode.UP);

                orderVO.setAutoDeliveryDays(result);
            }

            orderVO.assembleOrderSettings(orderSettings);

            // 支付信息 (由于预售订单有多个paylog)
            List<Paylog> paylogs = paylogByOrderId.get(order.getOrderId());
            if (Objects.nonNull(paylogs) && !paylogs.isEmpty()) {
                orderVO.setPayLog(paylogs.getLast());
            }

            orderVO.setVendorName(Optional.ofNullable(vendorMap.get(order.getVendorId())).map(Vendor::getVendorName).orElse(""));

            if (OrderType.PIN.getCode() == order.getOrderType()) {
                GrouponPromotionRecord record = grouponRecordByOrderId.get(order.getOrderId());
                if (orderStatus == OrderStatusEnum.CANCELLED.getCode()) {
                    orderVO.setJoinStatusDesc("已取消");
                }
                if (record != null) {
                    orderVO.setGrouponPromotionRecordId(record.getGrouponPromotionRecordId());
                    orderVO.setJoinStatus(record.getJoinStatus());
                    if (orderStatus != OrderStatusEnum.CANCELLED.getCode()) {
                        orderVO.setJoinStatusDesc(JoinStatusEnum.getDescription(record.getJoinStatus()));
                    }
                    if (orderStatus == OrderStatusEnum.CONFIRMED.getCode() && JoinStatusEnum.WAIT_GROUP.getCode().equals(record.getJoinStatus())) {
                        orderVO.setOrderStatusName("已付款，待成团");
                    }
                    if (JoinStatusEnum.FAIL.getCode().equals(record.getJoinStatus())) {
                        if (orderStatus == OrderStatusEnum.CANCELLED.getCode()) {
                            orderVO.setOrderStatusName("拼团失败");
                        } else {
                            orderVO.setOrderStatusName("拼团失败，待退款");
                        }
                    }
                }
            }

            // 补充预约相关信息
            if (OrderType.isReservationOrder(order.getOrderType())) {
                OrderReservation one = reservationByOrderId.get(order.getOrderId());
                if (one != null) {
                    orderVO.setOrderReservation(OrderReservationVO.toOrderReservationVO(one));
                }
            }
            // 订单售后状态（复用上方 orderAftersalesList 预取结果，非终态 = status NOT IN terminalStatus，排除 null 与原 SQL 一致）
            // 已完成但平台财务退款未到账的售后仍算售后中，与售后详情 / 列表的状态文案同源
            List<Integer> terminalStatus = AftersalesStatusEnum.terminalStatus();
            long ingCount = orderAftersalesList.stream()
                    .filter(a -> a.getStatus() != null && !terminalStatus.contains(a.getStatus()))
                    .count();
            boolean pendingFinanceRefund = orderAftersalesList.stream()
                    .anyMatch(a -> pendingFinanceRefundIds.contains(a.getAftersaleId()));
            orderVO.setAfterSalesIng(ingCount > 0 || pendingFinanceRefund);
            if (ingCount > 0 || pendingFinanceRefund) {
                orderVO.setAfterSalesStatus(1);
            } else {
                long completedCount = orderAftersalesList.stream()
                        .filter(a -> Objects.equals(a.getStatus(), AftersalesStatusEnum.COMPLETE.getCode()))
                        .count();
                orderVO.setAfterSalesStatus(completedCount > 0 ? 2 : 0);
            }

            fillOrderPresale(order, orderVO);



            return orderVO;
        }).toList();


        return PageUtil.convertPage(orderPage, orderVOList);
    }

    @Override
    public List<OrderItemVO> getOrderItemList(List<Integer> orderIds) {
        List<OrderItemVO> itemList = orderItemService.getItemByOrderIds(orderIds, SecurityUtils.isAdminUser());
        return itemList.stream().map(item -> {
            OrderItemVO orderItemVO = new OrderItemVO();
            BeanUtils.copyProperties(item, orderItemVO);
            orderItemVO.setWechatLiveContext(WechatLiveOrderContextUtils.extractFromPromotionData(item.getPromotionData()));
            LambdaQueryWrapper<AftersalesItem> queryWrapperSales = new LambdaQueryWrapper<>();
            queryWrapperSales.eq(AftersalesItem::getOrderItemId, item.getItemId());
            queryWrapperSales.orderByDesc(AftersalesItem::getAftersalesItemId);
            queryWrapperSales.last("limit 1");
            AftersalesItem aftersalesItem = aftersalesItemService.getOne(queryWrapperSales);
            if (aftersalesItem != null) {
                orderItemVO.setAftersalesItem(aftersalesItem);
            }

            return orderItemVO;
        }).toList();
    }

    private void fillOrderItemOverseasImage(OrderItemVO item) {
        if (tigshopProperties.getIsOverseas() == 1) {
            String picThumb = item.getPicThumb();
            if (StringUtils.isBlank(picThumb)) {
                return;
            }
            List<ProductSkuDTO.SkuData> skuData = item.getSkuData();
            //skuData里面的属性缩略图和picThumb说明是属性缩略图,dataType是8，默认是7
            int dataType = 7;
            if (CollUtil.isNotEmpty(skuData) && skuData.stream().anyMatch(sku -> sku.getAttrPicThumb() != null
                    && sku.getAttrPicThumb().equals(picThumb))) {
                    dataType = 8;
            }
            // 获取原图
            String originalImage = galleryPicService.getOriginalImage(picThumb);
            String translate = translatePackage.translate(originalImage, dataType);
            if(StringUtils.isBlank(translate)){
                item.setPicThumb(picThumb);
                return;
            }
            if (!Objects.equals(originalImage, translate)) {
                // 有翻译数据，提取尺寸后缀并拼接到翻译后的图片URL
                String suffix = picThumb.substring(originalImage.length());
                item.setPicThumb(translate + suffix);
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
                    item.setPicThumb(translate);
                    return;
                }
            }
            //缩略图就是原图(兼容性代码)
            translate = translatePackage.translate(picThumb, dataType);
            if (!Objects.equals(picThumb, translate)) {
                // 有翻译数据，提取尺寸后缀并拼接到翻译后的图片URL
                item.setPicThumb(translate);
                return;
            }
        }
    }

    // 构建查询构造器
    public void buildQueryWrapper(MPJLambdaWrapper<Order> queryWrapper, OrderListPageQuery pageQuery) {
        // 关键词检索 收货人 + 订单号 + 订单id
        String keyword = pageQuery.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.leftJoin(OrderItem.class, OrderItem::getOrderId, Order::getOrderId);
            queryWrapper.and(wrapper -> wrapper
                    .like(Order::getOrderSn, keyword)
                    .or()
                    .like(Order::getConsignee, keyword)
                    .or()
                    .like(OrderItem::getProductName, keyword)
                    .or()
                    .eq(Order::getOrderId, keyword));
            // 由于 LEFT JOIN order_item 表，一个订单有多个商品时会产生多行记录，需要按 order_id 分组去重
            queryWrapper.groupBy(Order::getOrderId);
        }

        if(StringUtils.isNotBlank(pageQuery.getOutTradeNo())){
            queryWrapper.like(Order::getOutTradeNo, pageQuery.getOutTradeNo());
        }

        //订单状态
        if (pageQuery.getOrderStatus() != null && ObjectUtil.isNotEmpty(pageQuery.getOrderStatus())) {
            String[] split = pageQuery.getOrderStatus().split(",");
            if (split.length == 1 && Integer.parseInt(split[0]) >= 0) {
                queryWrapper.eq(Order::getOrderStatus, split[0]);
            }
            if (split.length > 1) {
                queryWrapper.in(Order::getOrderStatus, List.of(split));
            }
        }
        // 发票场景：已完成订单或已提交发票申请的订单
        if (Boolean.TRUE.equals(pageQuery.getInvoiceScope())) {
            queryWrapper.and(wrapper -> wrapper
                    .eq(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode())
                    .or(w -> w
                            .isNotNull(Order::getInvoiceData)
                            .ne(Order::getInvoiceData, "")
                            .ne(Order::getInvoiceData, "[]")));
        }
        if (pageQuery.getOrderStatus() != null && ObjectUtil.equals(pageQuery.getOrderStatus(), "-2")) {
            // 查询删除的订单
            queryWrapper.eq(Order::getIsDel, DeleteType.DELETE.getCode());
        } else {
            queryWrapper.eq(Order::getIsDel, DeleteType.NOT_DELETE.getCode());
        }
        if (pageQuery.getOrderIds() != null && !pageQuery.getOrderIds().isEmpty()) {
            queryWrapper.in(Order::getOrderId, pageQuery.getOrderIds());
        }
        // 二期：按会员标签筛选（解析命中用户后按 userId 过滤）
        if (pageQuery.getMemberTagIds() != null && !pageQuery.getMemberTagIds().isEmpty()) {
            List<Long> tagUserIds = userMemberTagService.resolveUserIdsByTags(pageQuery.getMemberTagIds(), pageQuery.getMemberTagMatchMode());
            if (tagUserIds == null || tagUserIds.isEmpty()) {
                queryWrapper.eq(Order::getUserId, -1);
            } else {
                queryWrapper.in(Order::getUserId, tagUserIds);
            }
        }
        // 默认只查有效订单（拆单后父单 isValid=0 自动隐藏）
        if (!Boolean.TRUE.equals(pageQuery.getIncludeInvalid())) {
            queryWrapper.eq(Order::getIsValid, 1);
        }
        // 平台 Admin / 客户端：排除未支付的拆单子单（shopId/vendorId 过滤的端无需额外条件）
        boolean isShopOrVendorView = (getShopId() != null && getShopId() > 0)
                || (getVendorId() != null && getVendorId() > 0);
        if (!isShopOrVendorView) {
            queryWrapper.and(w -> w
                    .le(Order::getParentOrderId, 0)
                    .or().isNull(Order::getParentOrderId)
                    .or().ne(Order::getPayStatus, PaymentStatus.UNPAID.getCode())
                    .or().eq(Order::getIsCycleOrder, 1)
                    .or().eq(Order::getIsCardOrder, 1)
            );
        }
        // 店铺检索
        if (getShopId() != null && getShopId() > 0) {
            if (getAdminType().equals(AdminTypeEnum.PICKUP.getCode())) {
                queryWrapper.leftJoin(OrderPickupItem.class, OrderPickupItem::getOrderId, Order::getOrderId);
                queryWrapper.eq(OrderPickupItem::getPickupId, getShopId());
            } else {
                queryWrapper.eq(Order::getShopId, getShopId());
            }
        } else {
            if (pageQuery.getShopId() != null && pageQuery.getShopId() > 0) {
                queryWrapper.eq(Order::getShopId, pageQuery.getShopId());
            }
        }
        try {
            if (AdminTypeEnum.fromCode(getAdminType()).equals(AdminTypeEnum.POS)) {
                queryWrapper.eq(Order::getShopId, getShopId());
            }
        } catch (Exception ignored) {

        }
        //供应商筛选
        if (getVendorId() != null && getVendorId() > 0) {
            queryWrapper.eq(Order::getVendorId, getVendorId());
        } else {
            if (pageQuery.getVendorId() != null && pageQuery.getVendorId() > 0) {
                queryWrapper.eq(Order::getVendorId, pageQuery.getVendorId());
            }
            if (pageQuery.getVendorId() != null && pageQuery.getVendorId() == -2) {
                //查询供应商ID大于0的
                queryWrapper.gt(Order::getVendorId, 0);
            }
        }

        // 是否结算检索
        if (pageQuery.getIsSettlement() != null && pageQuery.getIsSettlement() > -1) {
            queryWrapper.eq(Order::getIsSettlement, pageQuery.getIsSettlement());
            if (pageQuery.getIsSettlement() == 0) {
                queryWrapper.notInSql(Order::getOrderId,
                        "SELECT order_id FROM groupon_promotion_record WHERE join_status = " + JoinStatusEnum.WAIT_GROUP.getCode());
            }
        }
        // 是否积分订单
        if (pageQuery.getIsExchangeOrder() != null && pageQuery.getIsExchangeOrder() > -1) {
            queryWrapper.eq(Order::getIsExchangeOrder, pageQuery.getIsExchangeOrder());
        }
        // 支付状态
        if (pageQuery.getPayStatus() != null && pageQuery.getPayStatus() != -1) {
            queryWrapper.eq(Order::getPayStatus, pageQuery.getPayStatus());
        }
        // 发货状态
        if (pageQuery.getShippingStatus() != null && pageQuery.getShippingStatus() != -1) {
            queryWrapper.eq(Order::getShippingStatus, pageQuery.getShippingStatus());
        }
        // 评价状态
        if (pageQuery.getCommentStatus() != null && pageQuery.getCommentStatus() != -1) {
            if (pageQuery.getCommentStatus() > 0) {
                queryWrapper.eq(Order::getCommentStatus, pageQuery.getCommentStatus());
            } else {
                queryWrapper.eq(Order::getCommentStatus, 0).eq(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode());
            }
        }
        // 详情地址
        if (StrUtil.isNotEmpty(pageQuery.getAddress())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Order::getAddress, pageQuery.getAddress())
                    .or()
                    .like(Order::getRegionNames, pageQuery.getAddress()));
        }
        // 收货人的email
        if (StrUtil.isNotEmpty(pageQuery.getEmail())) {
            queryWrapper.like(Order::getEmail, pageQuery.getEmail());
        }
        //手机号
        if (StrUtil.isNotEmpty(pageQuery.getMobile())) {
            queryWrapper.like(Order::getMobile, pageQuery.getMobile());
        }
        //配送物流
        if (pageQuery.getLogisticsId() != null && pageQuery.getLogisticsId() > 0) {
            queryWrapper.eq(Order::getLogisticsId, pageQuery.getLogisticsId());
        }
        //下单时间
        if (StrUtil.isNotEmpty(pageQuery.getAddStartTime())) {
            LocalDateTime startOfDay = LocalDate.parse(pageQuery.getAddStartTime()).atStartOfDay();
            Long addStartTime = startOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000;
            queryWrapper.ge(Order::getAddTime, addStartTime);
        }
        if (StrUtil.isNotEmpty(pageQuery.getAddEndTime())) {
            LocalDateTime endOfDay = LocalDate.parse(pageQuery.getAddEndTime()).atTime(LocalTime.MAX);
            Long addEndTime = endOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000;

            queryWrapper.le(Order::getAddTime, addEndTime);
        }
        //支付时间
        if (StrUtil.isNotBlank(pageQuery.getPayTime())) {
            //逗号隔开分割时间
            String[] payTime = pageQuery.getPayTime().split(",");
            Long payTimeStart = DateUtil.parse(payTime[0]).getTime();
            Long payTimeEnd = DateUtil.parse(payTime[1]).getTime() + 86400;
            queryWrapper.between(Order::getPayTime, payTimeStart, payTimeEnd);
        }

        //用户ID
        if (pageQuery.getUserId() != null) {
            queryWrapper.eq(Order::getUserId, pageQuery.getUserId());
        }
        // PC 端 日期检索
        if (pageQuery.getDataType() != null) {
            Long[] startAndEndTime = TigUtils.getStartAndEndTime(pageQuery.getDataType());
            queryWrapper.between(Order::getAddTime, startAndEndTime[0], startAndEndTime[1]);
        }

        if (pageQuery.getMark() != null && pageQuery.getMark() != -1) {
            queryWrapper.eq(Order::getMark, pageQuery.getMark());
        }
        //筛选支付方式是余额支付的订单
        if (StrUtil.isNotEmpty(pageQuery.getPayCode()) && pageQuery.getPayCode().equals("balance")) {
            queryWrapper.gt(Order::getBalance, BigDecimal.ZERO);
        }
        //订单来源in筛选，先解析string按逗号隔开为数组
        if (StrUtil.isNotBlank(pageQuery.getOrderSource())) {
            List<String> split = StrUtil.split(pageQuery.getOrderSource(), ",");
            queryWrapper.in(Order::getOrderSource, split);
        }
        boolean filterByLive = StrUtil.isNotBlank(pageQuery.getOrderSubSource())
                || pageQuery.getWechatLiveId() != null
                || pageQuery.getWechatLiveRoomId() != null
                || pageQuery.getWechatGoodsId() != null
                || StrUtil.isNotBlank(pageQuery.getLiveShareOpenId())
                || pageQuery.getLiveSyncStatus() != null;
        if (filterByLive) {
            List<Integer> liveOrderIds = wechatLiveOrderRelService.list(Wrappers.lambdaQuery(WechatLiveOrderRel.class)
                            .eq(StrUtil.isNotBlank(pageQuery.getOrderSubSource()), WechatLiveOrderRel::getSubSource, pageQuery.getOrderSubSource())
                            .eq(pageQuery.getWechatLiveId() != null, WechatLiveOrderRel::getWechatLiveId, pageQuery.getWechatLiveId())
                            .eq(pageQuery.getWechatLiveRoomId() != null, WechatLiveOrderRel::getRoomId, pageQuery.getWechatLiveRoomId())
                            .eq(pageQuery.getWechatGoodsId() != null, WechatLiveOrderRel::getWechatGoodsId, pageQuery.getWechatGoodsId())
                            .eq(StrUtil.isNotBlank(pageQuery.getLiveShareOpenId()), WechatLiveOrderRel::getShareOpenId, pageQuery.getLiveShareOpenId()))
                    .stream()
                    .map(WechatLiveOrderRel::getOrderId)
                    .distinct()
                    .collect(Collectors.toList());
            if (pageQuery.getLiveSyncStatus() != null) {
                List<Integer> syncOrderIds = wechatLiveSyncLogService.list(Wrappers.lambdaQuery(WechatLiveSyncLog.class)
                                .eq(WechatLiveSyncLog::getSyncStatus, pageQuery.getLiveSyncStatus())
                                .eq(WechatLiveSyncLog::getDirection, DIRECTION_OUTBOUND))
                        .stream()
                        .map(WechatLiveSyncLog::getOrderId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());
                liveOrderIds = liveOrderIds.isEmpty() ? syncOrderIds : liveOrderIds.stream().filter(syncOrderIds::contains).toList();
            }
            if (liveOrderIds.isEmpty()) {
                queryWrapper.eq(Order::getOrderId, -1);
            } else {
                queryWrapper.in(Order::getOrderId, liveOrderIds);
            }
        }
        // 父订单ID筛选（用于查询周期购子订单列表）
        if (pageQuery.getParentOrderId() != null && pageQuery.getParentOrderId() > 0) {
            queryWrapper.eq(Order::getParentOrderId, pageQuery.getParentOrderId());
        }
    }

    /**
     * 获取订单可用的操作
     */
    @Override
    public OrderActionVO availableActions(OrderVO orderVO) {
        OrderActionVO orderActionVO = new OrderActionVO();
        Integer orderId = orderVO.getOrderId();
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.getStatus(orderVO.getOrderStatus());
        orderVO.setToAftersalses(false);
        Boolean isPickup = orderVO.getIsPickup();
        switch (orderStatusEnum) {
            case PENDING:
                // 待确认，待付款
                orderActionVO.setSetConfirm(true);
                orderActionVO.setCancelOrder(true);
                orderActionVO.setModifyOrder(true);
                orderActionVO.setModifyShippingInfo(true);
                orderActionVO.setModifyOrderMoney(true);
                orderActionVO.setModifyOrderProduct(true);
                orderActionVO.setSetPaid(true);
                orderActionVO.setModifyOrderConsignee(true);
                // 假设 adminUid 是一个可以通过某种方式获取的变量
                if (SecurityUtils.isAdminUser() && orderVO.getPayTypeId() != PayType.OFFLINE.getCode()) {
                    // 后台用户不允许操作非线下支付订单
                    orderActionVO.setSetPaid(false);
                }
                orderActionVO.setToPay(true);
                break;
            case CONFIRMED:
                // 已确认，待发货
                orderActionVO.setSplitOrder(true);
                orderActionVO.setDeliver(true);
                orderActionVO.setModifyOrderConsignee(true);
                if (orderVO.getPayStatus() != PaymentStatus.PAID.getCode()) {
                    orderActionVO.setSetPaid(true);
                    orderActionVO.setCancelOrder(true);
                }
                orderActionVO.setToAftersales(orderAfterSalesService.getOrderAfterSalesToAction(orderId));
                break;
            case PROCESSING:
                // 已发货，处理中
                if (!Boolean.TRUE.equals(isPickup)
                        && !aftersalesService.hasInProgressFullOrderAftersales(orderId)) {
                    //不是自提的话，就不用确认发货；整单售后进行中时不允许确认收货，避免退款未到账订单先完成
                    orderActionVO.setConfirmReceipt(true);
                }
                orderActionVO.setModifyShippingInfo(true);
                orderActionVO.setToAftersales(orderAfterSalesService.getOrderAfterSalesToAction(orderId));
                break;
            case COMPLETED:
                orderActionVO.setRebuy(true);
                orderActionVO.setToAftersales(orderAfterSalesService.getOrderAfterSalesToAction(orderId));
                // D1146：评价功能关闭时不下发待评价入口，避免订单列表 / 详情残留按钮。
                // 这里直读配置而非注入 CommentService，避免订单与评价服务间的构造器循环依赖。
                if (orderVO.getCommentStatus() == PENDING.getCode()
                        && !"0".equals(configService.getConfigVal(SettingsEnum.PRODUCT_COMMENT_ENABLED))) {
                    orderActionVO.setToComment(true);
                }
                break;
            case CANCELLED:
                //已取消之后，就不能再次取消了
                orderActionVO.setCancelOrder(false);
                orderActionVO.setRebuy(true);
                orderActionVO.setDelOrder(true);
                break;
            case INVALID:
                orderActionVO.setDelOrder(true);
                break;
            // 其他状态...
            default:
                break;
        }
        PaymentStatus paymentStatus = PaymentStatus.getStatus(orderVO.getPayStatus());
        switch (paymentStatus) {
            case UNPAID:
                break;
            case PAID:
                orderActionVO.setSetUnpaid(false);
                break;
            default:
                break;
        }
        // 未支付拆单子单：shop/store 端只读，禁止所有操作
        boolean isUnpaidSplitChild = orderVO.getParentOrderId() != null && orderVO.getParentOrderId() > 0
                && orderVO.getPayStatus() == PaymentStatus.UNPAID.getCode()
                ;
        if (isUnpaidSplitChild) {
            orderActionVO.setCancelOrder(false);
            orderActionVO.setDeliver(false);
            orderActionVO.setConfirmReceipt(false);
            orderActionVO.setToPay(false);
            orderActionVO.setRebuy(false);
            orderActionVO.setDelOrder(false);
            orderActionVO.setToAftersales(false);
            orderActionVO.setSplitOrder(false);
            orderActionVO.setSetPaid(false);
            orderActionVO.setSetUnpaid(false);
            orderActionVO.setModifyOrderMoney(false);
            orderActionVO.setModifyOrderProduct(false);
            orderActionVO.setModifyOrder(false);
            orderActionVO.setModifyShippingInfo(false);
            orderActionVO.setModifyOrderConsignee(false);
            orderActionVO.setSetConfirm(false);
            return orderActionVO;
        }
        if (orderVO.getIsStoreSplited() != null && orderVO.getIsStoreSplited() == 1) {
            orderActionVO.setSplitOrder(false);
        } else {
            orderActionVO.setDeliver(false);
        }
        if (orderVO.getIsDel() != null && orderVO.getIsDel() == 1) {
            orderActionVO.setDelOrder(false);
        }
        if (orderVO.getOrderType() != null && orderVO.getOrderType() > 1) {
            orderActionVO.setRebuy(false);
            if (OrderType.getNoAfterSalesType(orderVO.getOrderType())) {
                orderActionVO.setToAftersales(false);
            }
        }

        // 订单内全部商品都不可售时才隐藏再次购买入口；仍有可售商品时允许再次购买可售部分。
        if (Boolean.TRUE.equals(orderActionVO.getRebuy()) && !hasAvailableRebuyItems(orderVO)) {
            orderActionVO.setRebuy(false);
        }

        // 抽奖兑换订单：奖品为 0 元兑换所得，不允许申请售后
        if (Objects.equals(orderVO.getOrderType(), CartTypeEnum.TYPE_LOTTERY_PRODUCT.getCode())) {
            orderActionVO.setToAftersales(false);
        }


        return orderActionVO;
    }

    /**
     * 判断再次购买订单内的商品是否仍然可售。
     * <p>订单商品快照不保存实时上下架状态，因此按商品 ID 批量读取当前商品状态；
     * 只要存在一个仍在售商品，就保留再次购买入口。</p>
     */
    private boolean hasAvailableRebuyItems(OrderVO orderVO) {
        List<OrderItemVO> items = orderVO.getItems();
        if (CollUtil.isEmpty(items)) {
            // 兼容未装配商品明细的内部调用，保留原有订单状态动作结果。
            return true;
        }
        List<Integer> productIds = items.stream()
                .filter(Objects::nonNull)
                .map(OrderItemVO::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (productIds.isEmpty()) {
            return false;
        }
        Map<Integer, Product> productMap = productService.listByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity(), (left, right) -> left));
        return productIds.stream().anyMatch(productId -> {
            Product product = productMap.get(productId);
            return product != null && Objects.equals(product.getProductStatus(), Constants.YES);
        });
    }

    @Transactional
    @Override
    public void setAdminNote(OrderAdminNoteParam param) {
        // 批量操作
        if (StrUtil.isNotBlank(param.getIds())) {
            List<String> ids = StrUtil.split(param.getIds(), StrUtil.COMMA);
            List<Order> orders = ids.stream().map(id -> Order.builder()
                    .orderId(Integer.parseInt(id))
                    .adminNote(param.getAdminNote())
                    .mark(param.getMark())
                    .build()).toList();
            this.updateBatchById(orders);

            for (String id : ids) {
                Order orderModel = this.getById(id);
                orderLogService.quickCreate("订单商家备注已更新", orderModel.getOrderId(), orderModel.getOrderSn());
            }
        } else {
            Order order = Order.builder()
                    .orderId(param.getId())
                    .adminNote(param.getAdminNote())
                    .mark(param.getMark())
                    .build();
            this.updateById(order);

            Order orderModel = this.getById(param.getId());
            orderLogService.quickCreate("订单商家备注已更新", orderModel.getOrderId(), orderModel.getOrderSn());
        }


    }

    @Override
    public Long getOrderStatusCount(int userId, String type, int status) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getIsDel, 0);
        queryWrapper.eq(Order::getIsValid, 1);
        queryWrapper.eq(Order::getUserId, userId);
        // 排除未支付拆单子单（周期购/次卡子单除外）
        queryWrapper.and(w -> w
                .le(Order::getParentOrderId, 0)
                .or().isNull(Order::getParentOrderId)
                .or().ne(Order::getPayStatus, PaymentStatus.UNPAID.getCode())
                .or().eq(Order::getIsCycleOrder, 1)
                .or().eq(Order::getIsCardOrder, 1)
        );
        if (Objects.equals(type, "order_status")) {
            queryWrapper.eq(Order::getOrderStatus, status);
        }

        if (Objects.equals(type, "comment_status")) {
            queryWrapper.eq(Order::getCommentStatus, status);
        }

        if (Objects.equals(type, "order_comment_status")) {
            queryWrapper.eq(Order::getCommentStatus, PENDING.getCode());
            queryWrapper.eq(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode());
        }

        return this.count(queryWrapper);
    }

    /**
     * 确认收货
     */
    @Override
    public boolean setOrderConfirm(Integer id) {
        OrderVO orderVO = this.detail(id);
        if (!orderVO.getAvailableActions().getConfirmReceipt()) {
            return false;
        }
        Order order = new Order();
        order.setOrderId(id);
        order.setOrderStatus(OrderStatusEnum.CONFIRMED.getCode());
        this.save(order);
        orderLogService.quickCreate("订单状态已更新", orderVO.getOrderId(), orderVO.getOrderSn());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean splitStoreOrder(Integer id) {
        OrderVO orderVO = this.detail(id);
        if (orderVO.getIsStoreSplited() == 1) {
            // 提交时已拆单：检查是否有子单需要激活
            long childCount = this.lambdaQuery()
                    .eq(Order::getParentOrderId, id)
                    .ne(Order::getOrderStatus, OrderStatusEnum.CANCELLED.getCode())
                    .count();
            if (childCount > 0) {
                activateChildOrdersOnPayment(id, orderVO);
                return true;
            }
            if (orderVO.getIsPresale() != null && orderVO.getIsPresale() == 1) {
                createDeliveryOrderIfNeeded(orderVO);
            }
            return false;
        }
        Map<Integer, Map<Integer, List<OrderItemVO>>> stores = new HashMap<>();

        if (CollUtil.isNotEmpty(orderVO.getItems())) {
            orderVO.getItems().forEach(item -> {
                Integer shopId = item.getShopId();
                Integer vendorId = item.getVendorId();
                stores.computeIfAbsent(shopId, k -> new HashMap<>())
                        .computeIfAbsent(vendorId, k -> new ArrayList<>())
                        .add(item);
            });
        }

        //计算stores中只有一个店铺且只有一个VID
        boolean isSingleShopAndVendor = stores.size() == 1 && stores.values().iterator().next().size() == 1;
        if (isSingleShopAndVendor) {
            // 所有商品只存在同一个店铺id，直接更新拆分状态
            Order order1 = new Order();
            order1.setIsStoreSplited(1);
            order1.setOrderId(orderVO.getOrderId());
            this.updateById(order1);
            // 为同城配送订单创建派送订单
            createDeliveryOrderIfNeeded(orderVO);
            return true;
        }

        // 收集所有新创建的拆分订单
        List<Order> newSplitOrders = new ArrayList<>();

        for (Map.Entry<Integer, Map<Integer, List<OrderItemVO>>> entry : stores.entrySet()) {
            int shopId = entry.getKey();
            for (Map.Entry<Integer, List<OrderItemVO>> itemEntry : entry.getValue().entrySet()) {
                List<OrderItemVO> items = itemEntry.getValue();
                // 同一个店铺订单，是否是同一个供应商。同一个店铺不同供应商的商品的优惠劵需要分摊
                OrderVO copyOrderVO;
                boolean isSingleShopSingleVendor = entry.getValue().size() == 1;
                if (!isSingleShopSingleVendor) {
                    copyOrderVO = new OrderVO();
                    BeanUtils.copyProperties(orderVO, copyOrderVO);
                    copyOrderVO.setProductAmount(BigDecimal.ZERO);
                    entry.getValue().forEach((key, value) -> value.forEach(orderItemVO -> {
                        copyOrderVO.setProductAmount(copyOrderVO.getProductAmount().add(orderItemVO.getOriginPrice().multiply(BigDecimal.valueOf(orderItemVO.getQuantity()))));
                    }));
                } else {
                    copyOrderVO = orderVO;
                }
                Order newOrder = this.createSplitOrder(copyOrderVO, items, true, shopId, isSingleShopSingleVendor);
                newSplitOrders.add(newOrder);
                createDeliveryOrderIfNeeded(this.detail(newOrder.getOrderId()));
            }

        }

        // 拆分分销订单（在删除父订单前）
        splitSalesmanOrdersForStoreSplit(orderVO, newSplitOrders);

        // 尾差补齐：确保所有子订单的支付金额总和等于父订单
        adjustSplitOrderAmounts(orderVO, newSplitOrders);
        // 税费补齐：确保所有子订单税费总和等于父订单税费
        adjustSplitOrderTaxAmounts(orderVO, newSplitOrders);

        // 删除父订单和订单商品
        orderLogService.quickCreate("订单商品来自不同店铺，已拆分", orderVO.getOrderId(), orderVO.getOrderSn());
        this.removeById(orderVO.getOrderId());
        try{
            wechatLiveOrderRelService.removeByOrderId(orderVO.getOrderId());
        }catch (Exception e){

        }
        orderItemService.remove(
                Wrappers.lambdaQuery(OrderItem.class)
                        .eq(OrderItem::getOrderId, orderVO.getOrderId())
        );

        //自动填充
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean splitOrderAtSubmit(Integer id) {
        OrderVO orderVO = this.detail(id);
        if (orderVO.getIsStoreSplited() == 1) {
            return false;
        }
        Map<Integer, Map<Integer, List<OrderItemVO>>> stores = new HashMap<>();

        if (CollUtil.isNotEmpty(orderVO.getItems())) {
            orderVO.getItems().forEach(item -> {
                Integer shopId = item.getShopId();
                Integer vendorId = item.getVendorId();
                stores.computeIfAbsent(shopId, k -> new HashMap<>())
                        .computeIfAbsent(vendorId, k -> new ArrayList<>())
                        .add(item);
            });
        }

        boolean isSingleShopAndVendor = stores.size() == 1 && stores.values().iterator().next().size() == 1;
        if (isSingleShopAndVendor) {
            Order order1 = new Order();
            order1.setIsStoreSplited(1);
            order1.setIsValid(1);
            order1.setOrderId(orderVO.getOrderId());
            this.updateById(order1);
            return true;
        }

        List<Order> newSplitOrders = new ArrayList<>();

        for (Map.Entry<Integer, Map<Integer, List<OrderItemVO>>> entry : stores.entrySet()) {
            int shopId = entry.getKey();
            for (Map.Entry<Integer, List<OrderItemVO>> itemEntry : entry.getValue().entrySet()) {
                List<OrderItemVO> items = itemEntry.getValue();
                OrderVO copyOrderVO;
                boolean isSingleShopSingleVendor = entry.getValue().size() == 1;
                if (!isSingleShopSingleVendor) {
                    copyOrderVO = new OrderVO();
                    BeanUtils.copyProperties(orderVO, copyOrderVO);
                    copyOrderVO.setProductAmount(BigDecimal.ZERO);
                    entry.getValue().forEach((key, value) -> value.forEach(orderItemVO -> {
                        copyOrderVO.setProductAmount(copyOrderVO.getProductAmount().add(orderItemVO.getOriginPrice().multiply(BigDecimal.valueOf(orderItemVO.getQuantity()))));
                    }));
                } else {
                    copyOrderVO = orderVO;
                }
                Order newOrder = this.createSplitOrder(copyOrderVO, items, true, shopId, isSingleShopSingleVendor);
                newSplitOrders.add(newOrder);
            }
        }

        splitSalesmanOrdersForStoreSplit(orderVO, newSplitOrders);

        adjustSplitOrderAmounts(orderVO, newSplitOrders);
        adjustSplitOrderTaxAmounts(orderVO, newSplitOrders);

        Order parentUpdate = new Order();
        parentUpdate.setOrderId(orderVO.getOrderId());
        parentUpdate.setIsStoreSplited(1);
        parentUpdate.setIsValid(1);
        this.updateById(parentUpdate);

        orderLogService.quickCreate("订单商品来自不同店铺，提交时已拆分", orderVO.getOrderId(), orderVO.getOrderSn());

        return true;
    }

    private void activateChildOrdersOnPayment(Integer parentId, OrderVO parentOrderVO) {
        List<Order> children = this.lambdaQuery()
                .eq(Order::getParentOrderId, parentId)
                .ne(Order::getOrderStatus, OrderStatusEnum.CANCELLED.getCode())
                .list();

        Order parentOrder = this.getById(parentId);

        for (Order child : children) {
            Order childUpdate = new Order();
            childUpdate.setOrderId(child.getOrderId());
            childUpdate.setPayStatus(PaymentStatus.PAID.getCode());
            childUpdate.setOrderStatus(OrderStatusEnum.CONFIRMED.getCode());
            childUpdate.setPayTime(StringUtils.getCurrentTime());

            BigDecimal ratio = parentOrder.getTotalAmount().compareTo(BigDecimal.ZERO) > 0
                    ? child.getTotalAmount().divide(parentOrder.getTotalAmount(), 10, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            if (parentOrder.getPaidAmount() != null && parentOrder.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                childUpdate.setPaidAmount(parentOrder.getPaidAmount().multiply(ratio).setScale(2, java.math.RoundingMode.HALF_UP));
            }
            if (parentOrder.getOnlinePaidAmount() != null && parentOrder.getOnlinePaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                childUpdate.setOnlinePaidAmount(parentOrder.getOnlinePaidAmount().multiply(ratio).setScale(2, java.math.RoundingMode.HALF_UP));
            }
            if (parentOrder.getOfflinePaidAmount() != null && parentOrder.getOfflinePaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                childUpdate.setOfflinePaidAmount(parentOrder.getOfflinePaidAmount().multiply(ratio).setScale(2, java.math.RoundingMode.HALF_UP));
            }

            childUpdate.setUnpaidAmount(BigDecimal.ZERO);
            this.updateById(childUpdate);

            try {
                createDeliveryOrderIfNeeded(this.detail(child.getOrderId()));
            } catch (Exception e) {
                log.error("子单同城配送订单创建失败, childOrderId={}", child.getOrderId(), e);
            }
        }

        // 父单标记为无效（留痕不删除）
        Order parentInvalid = new Order();
        parentInvalid.setOrderId(parentId);
        parentInvalid.setIsValid(0);
        this.updateById(parentInvalid);
    }

    /**
     * 为同城配送订单创建派送订单
     *
     * @param orderVO 订单信息
     */
    @Override
    public void createDeliveryOrderIfNeeded(OrderVO orderVO) {
        // 判断是否为同城配送订单
        if (!Objects.equals(orderVO.getShippingTypeId(), OrderDeliveryTypeEnum.SAME_CITY.getCode())
                && OrderType.RESERVATION_HOME.getCode() != orderVO.getOrderType()) {
            return;
        }


        // 预售订单：只有尾款支付完成后才能创建配送订单
        // 预售状态：0-待支付定金，1-待支付尾款，2-已完成支付
        if (orderVO.getIsPresale() != null && orderVO.getIsPresale() == 1) {
            OrderPresale presale = orderPresaleService.getByOrderId(orderVO.getOrderId());
            if (presale == null || !Objects.equals(presale.getPresaleStatus(), 2)) {
                // 预售订单未支付尾款，不创建配送订单
                return;
            }
        }

        //如果是拼团订单的话,看看是否拼团成功,拼团成功才能创建
        if (OrderType.PIN.getCode() == orderVO.getOrderType()) {
            Integer joinStatus = orderVO.getJoinStatus();
            if (joinStatus == null) {
                GrouponPromotionRecord record = grouponPromotionRecordService.lambdaQuery().eq(GrouponPromotionRecord::getOrderId, orderVO.getOrderId()).one();
                joinStatus = record.getJoinStatus();
            }
            if (joinStatus != 2) {
                //不是拼团成功,都不会创建
                return;
            }
        }

        try {
            // 构建派送订单创建参数
            DeliveryOrderCreateDTO createDTO = new DeliveryOrderCreateDTO();
            createDTO.setOrderId(orderVO.getOrderId());
            createDTO.setOrderSn(orderVO.getOrderSn());
            createDTO.setShopId(orderVO.getShopId());
            createDTO.setType(
                    OrderType.RESERVATION_HOME.getCode() == orderVO.getOrderType()
                            ? DeliveryOrderTypeEnum.SERVICE.getCode() :
                            DeliveryOrderTypeEnum.DELIVERY.getCode());
            createDTO.setConsignee(orderVO.getConsignee());
            createDTO.setMobile(orderVO.getMobile());
            createDTO.setAddress(orderVO.getAddress());
            createDTO.setOrderAmount(orderVO.getTotalAmount());
            createDTO.setBuyerNote(orderVO.getBuyerNote());

            // 设置地区名称
            if (CollUtil.isNotEmpty(orderVO.getRegionNames())) {
                createDTO.setRegionNames(String.join(" ", orderVO.getRegionNames()));
            }

            // 从地址数据中获取经纬度
            UserAddressVO addressData = orderVO.getAddressData();
            if (addressData != null) {
                createDTO.setLatitude(addressData.getLatitude());
                createDTO.setLongitude(addressData.getLongitude());
            }

            // 设置取货地址（门店地址）
            if (orderVO.getShopId() != null && orderVO.getShopId() > 0) {
                Shop shop = shopMapper.selectById(orderVO.getShopId());
                if (shop != null) {
                    if (StrUtil.isNotBlank(shop.getShopRegionId())) {
                        List<Integer> list = JSON.parseArray(shop.getShopRegionId(), Integer.class);
                        if (ObjectUtil.isNotEmpty(list)) {
                            LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
                            queryWrapper.in(Region::getRegionId, list);
                            List<Region> regions = regionMapper.selectList(queryWrapper);
                            String regionNames = regions.stream().map(Region::getRegionName).collect(Collectors.joining(" "));
                            createDTO.setPickupRegionNames(regionNames);
                        }
                    }
                    createDTO.setPickupAddress(shop.getShopDetailedAddress());
                    createDTO.setPickupLongitude(shop.getShopLongitude());
                    createDTO.setPickupLatitude(shop.getShopLatitude());
                    // 取货电话：优先门店联系电话，其次客服电话
                    if (StrUtil.isNotBlank(shop.getContactMobile())) {
                        createDTO.setPickupPhone(shop.getContactMobile());
                    } else if (StrUtil.isNotBlank(shop.getKefuPhone())) {
                        createDTO.setPickupPhone(shop.getKefuPhone());
                    }
                }
            } else {
                //自营门店
                GetStoreSelfOperatedVO selfOperatedVO = configService.getStoreSelfOperatedSettings();
                //填充
                createDTO.setPickupAddress(selfOperatedVO.getStoreSelfOperatedAddress());
                createDTO.setPickupLongitude(new BigDecimal(selfOperatedVO.getStoreSelfOperatedLongitude()));
                createDTO.setPickupLatitude(new BigDecimal(selfOperatedVO.getStoreSelfOperatedLatitude()));
                createDTO.setPickupRegionNames(CollUtil.isEmpty(selfOperatedVO.getStoreSelfOperatedRegions()) ? "" : selfOperatedVO.getStoreSelfOperatedRegions().stream().collect(Collectors.joining(" ")));
                // 自营门店电话
                if (StrUtil.isNotBlank(selfOperatedVO.getStoreSelfOperatedPhone())) {
                    createDTO.setPickupPhone(selfOperatedVO.getStoreSelfOperatedPhone());
                }
            }

            // 创建派送订单
        } catch (Exception e) {
            log.error("订单[{}]创建派送订单失败", orderVO.getOrderSn(), e);
        }
    }

    /**
     * 为同城配送订单和预约上门订单创建派送订单（用于定时任务等无请求上下文的场景）
     *
     * @param order         子订单实体
     * @param parentOrderVO 父订单VO（用于获取地址等信息）
     */
    @Override
    public void createDeliveryOrderIfNeeded(Order order, OrderVO parentOrderVO) {
        // 判断是否为同城配送订单或预约上门订单
        if (!Objects.equals(order.getShippingTypeId(), OrderDeliveryTypeEnum.SAME_CITY.getCode())
                && OrderType.RESERVATION_HOME.getCode() != order.getOrderType()) {
            return;
        }


        // 预售订单：只有尾款支付完成后才能创建配送订单
        // 预售状态：0-待支付定金，1-待支付尾款，2-已完成支付
        if (order.getIsPresale() != null && order.getIsPresale() == 1) {
            OrderPresale presale = orderPresaleService.getByOrderId(order.getOrderId());
            if (presale == null || !Objects.equals(presale.getPresaleStatus(), 2)) {
                // 预售订单未支付尾款，不创建配送订单
                return;
            }
        }

        try {
            DeliveryOrderCreateDTO createDTO = new DeliveryOrderCreateDTO();
            createDTO.setOrderId(order.getOrderId());
            createDTO.setOrderSn(order.getOrderSn());
            createDTO.setShopId(order.getShopId());
            createDTO.setType(
                    OrderType.RESERVATION_HOME.getCode() == order.getOrderType()
                            ? DeliveryOrderTypeEnum.SERVICE.getCode() :
                            DeliveryOrderTypeEnum.DELIVERY.getCode());
            createDTO.setConsignee(order.getConsignee());
            createDTO.setMobile(order.getMobile());
            createDTO.setAddress(order.getAddress());
            createDTO.setOrderAmount(order.getTotalAmount());
            createDTO.setBuyerNote(order.getBuyerNote());

            // 从parentOrderVO获取地区名称和经纬度
            if (parentOrderVO != null) {
                if (CollUtil.isNotEmpty(parentOrderVO.getRegionNames())) {
                    createDTO.setRegionNames(String.join(" ", parentOrderVO.getRegionNames()));
                }
                UserAddressVO addressData = parentOrderVO.getAddressData();
                if (addressData != null) {
                    createDTO.setLatitude(addressData.getLatitude());
                    createDTO.setLongitude(addressData.getLongitude());
                }
            }

            // 设置取货地址（门店地址）
            if (order.getShopId() != null && order.getShopId() > 0) {
                Shop shop = shopMapper.selectById(order.getShopId());
                if (shop != null) {
                    if (StrUtil.isNotBlank(shop.getShopRegionId())) {
                        List<Integer> list = JSON.parseArray(shop.getShopRegionId(), Integer.class);
                        if (ObjectUtil.isNotEmpty(list)) {
                            LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
                            queryWrapper.in(Region::getRegionId, list);
                            List<Region> regions = regionMapper.selectList(queryWrapper);
                            String regionNames = regions.stream().map(Region::getRegionName).collect(Collectors.joining(" "));
                            createDTO.setPickupRegionNames(regionNames);
                        }
                    }
                    createDTO.setPickupAddress(shop.getShopDetailedAddress());
                    createDTO.setPickupLongitude(shop.getShopLongitude());
                    createDTO.setPickupLatitude(shop.getShopLatitude());
                    if (StrUtil.isNotBlank(shop.getContactMobile())) {
                        createDTO.setPickupPhone(shop.getContactMobile());
                    } else if (StrUtil.isNotBlank(shop.getKefuPhone())) {
                        createDTO.setPickupPhone(shop.getKefuPhone());
                    }
                }
            } else {
                GetStoreSelfOperatedVO selfOperatedVO = configService.getStoreSelfOperatedSettings();
                createDTO.setPickupAddress(selfOperatedVO.getStoreSelfOperatedAddress());
                createDTO.setPickupLongitude(new BigDecimal(selfOperatedVO.getStoreSelfOperatedLongitude()));
                createDTO.setPickupLatitude(new BigDecimal(selfOperatedVO.getStoreSelfOperatedLatitude()));
                createDTO.setPickupRegionNames(CollUtil.isEmpty(selfOperatedVO.getStoreSelfOperatedRegions()) ? "" : selfOperatedVO.getStoreSelfOperatedRegions().stream().collect(Collectors.joining(" ")));
                if (StrUtil.isNotBlank(selfOperatedVO.getStoreSelfOperatedPhone())) {
                    createDTO.setPickupPhone(selfOperatedVO.getStoreSelfOperatedPhone());
                }
            }

        } catch (Exception e) {
            log.error("订单[{}]创建派送订单失败", order.getOrderSn(), e);
        }
    }

    public String creatNewOrderSn() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String timestamp = dateFormat.format(new Date());
        Random random = new Random();
        int randomNumber = 100 + random.nextInt(900);
        return timestamp + randomNumber;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createSplitOrder(OrderVO order, List<OrderItemVO> newItems, boolean isSpiltStore, Integer shopId, boolean isSingleShopSingleVendor) {
        // 获取原始订单数据并移除不需要的字段 ,直接使用 OrderVO 会导致时间丢失
        Order waitSplitOrder = getById(order.getOrderId());
        Order newOrder = new Order();
        BeanUtils.copyProperties(waitSplitOrder, newOrder);
        //转换地址信息【JSON】
        String regionIds = "";
        if (CollUtil.isNotEmpty(order.getRegionIds())) {
            regionIds = JSONUtil.toJsonStr(order.getRegionIds());
        }
        newOrder.setRegionIds(regionIds);
        String regionNames = "";
        if (CollUtil.isNotEmpty(order.getRegionNames())) {
            regionNames = JSONUtil.toJsonStr(order.getRegionNames());
        }
        newOrder.setRegionNames(regionNames);
        newOrder.setOrderId(null);
        newOrder.setShopId(shopId);
        BigDecimal productAmount = BigDecimal.valueOf(0);
        BigDecimal totalAmount;

        // 计算商品总金额：使用 originPrice（会员价合计，与父订单 cartTotal.productAmount 口径一致），
        // 避免使用 price（已扣减满减/满折的金额）导致后续 updateOrderMoney 重新减一次 discountAmount，
        // 进而使 totalAmount 比父订单少一份优惠（典型表现：vendor 拆单后 balance > totalAmount）。
        for (OrderItemVO item : newItems) {
            item.setItemId(null);
            newOrder.setVendorId(item.getVendorId());
            BigDecimal itemBasePrice = item.getOriginPrice() != null && item.getOriginPrice().compareTo(BigDecimal.ZERO) > 0
                    ? item.getOriginPrice()
                    : item.getPrice();
            productAmount = productAmount.add(new BigDecimal(item.getQuantity()).multiply(itemBasePrice));
        }
        newOrder.setProductAmount(productAmount);
        totalAmount = productAmount;

        // 检查订单编号是否已存在
        newOrder.setOrderSn(creatNewOrderSn());

        // 设置父订单ID和父订单编号
        Integer parentOrderId = order.getParentOrderId() > 0 ? order.getParentOrderId() : order.getOrderId();
        String parentOrderSn = StringUtils.isNotEmpty(order.getParentOrderSn()) ? order.getParentOrderSn() : order.getOrderSn();
        newOrder.setParentOrderId(parentOrderId);
        newOrder.setParentOrderSn(parentOrderSn);

        // 获取订单金额详情和优惠券详情
        OrderAmountDetail orderAmountDetail = orderAmountDetailService.getOne(
                Wrappers.lambdaQuery(OrderAmountDetail.class)
                        .eq(OrderAmountDetail::getShopId, shopId)
                        .eq(OrderAmountDetail::getOrderId, parentOrderId)
        );
        OrderCouponDetail orderCouponDetail = orderCouponDetailService.getOne(
                Wrappers.lambdaQuery(OrderCouponDetail.class)
                        .eq(OrderCouponDetail::getShopId, shopId)
                        .eq(OrderCouponDetail::getOrderId, parentOrderId)
        );
        if (isSpiltStore) {
            newOrder.setIsStoreSplited(1);
        }

        if (isSpiltStore && isSingleShopSingleVendor) {
            // 取主单里当前店铺的amount信息
            //Map extensionData = JSONUtil.parse(order.getOrderExtension()).toBean(Map.class);
            Map<String, Object> extensionData = JSONUtil.parseObj(order.getOrderExtension());
            // 优惠券金额（按店铺）
            BigDecimal couponAmount = Optional.ofNullable(orderAmountDetail).map(OrderAmountDetail::getCouponAmount).orElse(BigDecimal.valueOf(0.0));
            newOrder.setCouponAmount(couponAmount);
            totalAmount = totalAmount.subtract(couponAmount);

            // 运费（按店铺）
            BigDecimal shippingFee = Optional.ofNullable(orderAmountDetail).map(OrderAmountDetail::getShippingFee).orElse(BigDecimal.valueOf(0.0));
            newOrder.setShippingFee(shippingFee);
            totalAmount = totalAmount.add(shippingFee);

            // 优惠/折扣（按店铺）
            BigDecimal discountAmount = Optional.ofNullable(orderAmountDetail).map(OrderAmountDetail::getDiscountAmount).orElse(BigDecimal.valueOf(0.0));
            newOrder.setDiscountAmount(discountAmount);
            totalAmount = totalAmount.subtract(discountAmount);

            // 配送类型（优先从extension中读取各店铺的配送类型，如果没有则保留父订单的值）
            Map<String, Object> shippingType = (Map<String, Object>) extensionData.getOrDefault("shipping_type", Collections.emptyMap());
            newOrder.setShippingTypeId((Integer) Optional.ofNullable(shippingType.get(String.valueOf(shopId))).map(map -> ((Map<String, Object>) map).get("type_id")).orElse(waitSplitOrder.getShippingTypeId()));
            newOrder.setShippingTypeName((String) Optional.ofNullable(shippingType.get(String.valueOf(shopId))).map(map -> ((Map<String, Object>) map).get("type_name")).orElse(waitSplitOrder.getShippingTypeName()));
        } else {
            // 优惠券金额（平摊）
            BigDecimal couponAmount = newOrder.getCouponAmount().compareTo(BigDecimal.ZERO) > 0 ? allocatedAmount(order.getProductAmount(), productAmount, newOrder.getCouponAmount()) : BigDecimal.valueOf(0);
            newOrder.setCouponAmount(couponAmount);
            totalAmount = totalAmount.subtract(couponAmount);

            // 运费（平摊）
            BigDecimal shippingFee = newOrder.getShippingFee().compareTo(BigDecimal.ZERO) > 0 ? allocatedAmount(order.getProductAmount(), productAmount, newOrder.getShippingFee()) : BigDecimal.valueOf(0);
            newOrder.setShippingFee(shippingFee);
            totalAmount = totalAmount.add(shippingFee);

            // 优惠/折扣（平摊）
            BigDecimal discountAmount = newOrder.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0 ? allocatedAmount(order.getProductAmount(), productAmount, newOrder.getDiscountAmount()) : BigDecimal.valueOf(0);
            newOrder.setDiscountAmount(discountAmount);
            totalAmount = totalAmount.subtract(discountAmount);

            // 配送类型（从orderExtension中读取，与isSingleShopSingleVendor分支保持一致）
            Map<String, Object> extensionData = JSONUtil.parseObj(order.getOrderExtension());
            Map<String, Object> shippingType = (Map<String, Object>) extensionData.getOrDefault("shipping_type", Collections.emptyMap());
            if (!shippingType.isEmpty()) {
                newOrder.setShippingTypeId((Integer) Optional.ofNullable(shippingType.get(String.valueOf(shopId))).map(map -> ((Map<String, Object>) map).get("type_id")).orElse(newOrder.getShippingTypeId()));
                newOrder.setShippingTypeName((String) Optional.ofNullable(shippingType.get(String.valueOf(shopId))).map(map -> ((Map<String, Object>) map).get("type_name")).orElse(newOrder.getShippingTypeName()));
            }
        }

        // 积分抵扣（平摊）
        BigDecimal pointsAmount = newOrder.getPointsAmount().compareTo(BigDecimal.ZERO) > 0 ? allocatedAmount(order.getProductAmount(), productAmount, newOrder.getPointsAmount()) : BigDecimal.valueOf(0);
        newOrder.setPointsAmount(pointsAmount);
        totalAmount = totalAmount.subtract(pointsAmount);

        // 手续费（平摊）
        BigDecimal serviceFee = newOrder.getServiceFee().compareTo(BigDecimal.ZERO) > 0 ? allocatedAmount(order.getProductAmount(), productAmount, newOrder.getServiceFee()) : BigDecimal.valueOf(0);
        newOrder.setServiceFee(serviceFee);
        totalAmount = totalAmount.add(serviceFee);

        // 发票费用（平摊）
        BigDecimal invoiceFee = newOrder.getInvoiceFee().compareTo(BigDecimal.ZERO) > 0 ? allocatedAmount(order.getProductAmount(), productAmount, newOrder.getInvoiceFee()) : BigDecimal.valueOf(0);
        newOrder.setInvoiceFee(invoiceFee);
        totalAmount = totalAmount.add(invoiceFee);

        // 抹分金额（平摊）
        BigDecimal roundOffCentsAmount = newOrder.getRoundOffCentsAmount() != null && newOrder.getRoundOffCentsAmount().compareTo(BigDecimal.ZERO) > 0
                ? allocatedAmount(order.getProductAmount(), productAmount, newOrder.getRoundOffCentsAmount()) : BigDecimal.valueOf(0);
        newOrder.setRoundOffCentsAmount(roundOffCentsAmount);
        totalAmount = totalAmount.subtract(roundOffCentsAmount);

        // 抹角金额（平摊）
        BigDecimal roundOffDimeAmount = newOrder.getRoundOffDimeAmount() != null && newOrder.getRoundOffDimeAmount().compareTo(BigDecimal.ZERO) > 0
                ? allocatedAmount(order.getProductAmount(), productAmount, newOrder.getRoundOffDimeAmount()) : BigDecimal.valueOf(0);
        newOrder.setRoundOffDimeAmount(roundOffDimeAmount);
        totalAmount = totalAmount.subtract(roundOffDimeAmount);

        // 确保 totalAmount 不为负数
        totalAmount = totalAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : totalAmount;

        // 拆单税费按子订单口径独立计算：（商品总金额 + 运费）* 税率
        if (waitSplitOrder.getTaxRate() != null) {
            newOrder.setTaxRate(waitSplitOrder.getTaxRate());
        }
        refreshSplitOrderTaxAmount(newOrder);
        totalAmount = totalAmount.add(Optional.ofNullable(newOrder.getTaxRateAmount()).orElse(BigDecimal.ZERO));

        newOrder.setTotalAmount(totalAmount);

        // 使用的余额（平摊）- 基于订单总金额平摊
        BigDecimal balance = newOrder.getBalance().compareTo(BigDecimal.ZERO) > 0 ? allocatedAmount(order.getTotalAmount(), totalAmount, newOrder.getBalance()) : BigDecimal.valueOf(0);
        newOrder.setBalance(balance);

        // 线上支付金额（平摊）- 基于订单总金额平摊
        BigDecimal onlinePaidAmount = newOrder.getOnlinePaidAmount().compareTo(BigDecimal.ZERO) > 0 ? allocatedAmount(order.getTotalAmount(), totalAmount, newOrder.getOnlinePaidAmount()) : BigDecimal.valueOf(0);
        newOrder.setOnlinePaidAmount(onlinePaidAmount);

        // 线下支付金额（平摊）- 基于订单总金额平摊
        BigDecimal offlinePaidAmount = newOrder.getOfflinePaidAmount().compareTo(BigDecimal.ZERO) > 0 ? allocatedAmount(order.getTotalAmount(), totalAmount, newOrder.getOfflinePaidAmount()) : BigDecimal.valueOf(0);
        newOrder.setOfflinePaidAmount(offlinePaidAmount);

        // 保存新订单
        // 为解决region_ids报错问题，先默认给一个地址
        if (StrUtil.isNotBlank(newOrder.getRegionIds())) {
            newOrder.setRegionIds("[110000,110100,110101]");
        }
        this.save(newOrder);

        // 添加订单商品
        for (OrderItemVO item : newItems) {
            item.setOrderId(newOrder.getOrderId());
        }
        List<OrderItem> splitOrderItems = newItems.stream().map(item -> {
            OrderItem orderItem = new OrderItem();
            String skuData = "";
            if (CollUtil.isNotEmpty(item.getSkuData())) {
                skuData = JSONUtil.toJsonStr(item.getSkuData());
            }
            orderItem.setSkuData(skuData);
            BeanUtils.copyProperties(item, orderItem);
            return orderItem;
        }).collect(Collectors.toList());


        orderItemService.saveBatch(splitOrderItems);

        try{
            wechatLiveOrderSyncService.bindOrderRelations(newOrder, splitOrderItems);
            wechatLiveOrderSyncService.onOrderCreated(newOrder);
            if (Objects.equals(newOrder.getPayStatus(), PaymentStatus.PAID.getCode())) {
                wechatLiveOrderSyncService.onOrderPaid(newOrder);
            }
        }catch (Exception e){

        }


        // 更新新订单的金额
        this.updateOrderMoney(newOrder, true);

        if (orderAmountDetail != null) {
            orderAmountDetail.setOrderDiscountDetailId(null);
            orderAmountDetail.setOrderId(newOrder.getOrderId());
            orderAmountDetailService.save(orderAmountDetail);
        }

        if (orderCouponDetail != null) {
            orderCouponDetail.setOrderCouponDetailId(null);
            orderCouponDetail.setOrderId(newOrder.getOrderId());
            orderCouponDetailService.save(orderCouponDetail);
        }

        // 添加拆份记录
        orderSpitLogService.addSplitLog(order.getOrderId(), newOrder.getOrderId(), order);

        // 添加payLog
        OrderVO detail = this.detail(newOrder.getOrderId());
        // 商品订单：orderType=0，避免撞充值单号
        // 父单可能有多条支付记录（下单后未付又重新发起支付），无 LIMIT 会抛 TooManyResultsException。
        // 这里的 payCode/payStatus/transactionId 要复制给子单，故优先取已支付的那条；
        // 拆单发生在下单时、父单尚未支付也属正常，此时退化为取最新一条。
        Paylog parentPaylog = paylogService.getOne(Wrappers.lambdaQuery(Paylog.class)
                .eq(Paylog::getOrderId, order.getOrderId())
                .eq(Paylog::getOrderType, 0)
                .orderByDesc(Paylog::getPayStatus)
                .orderByDesc(Paylog::getPaylogId)
                .last("LIMIT 1"));
        if (parentPaylog != null) {
            Paylog childPayLog = paylogService.getOne(Wrappers.lambdaQuery(Paylog.class)
                    .eq(Paylog::getOrderSn, detail.getOrderSn())
                    .last("LIMIT 1"));
            if (childPayLog == null) {
                BigDecimal childPayAmount = resolveSplitPaylogAmount(detail, parentPaylog.getPayCode(), parentPaylog.getPayStatus());
                Paylog paylog = paylogService.creatPayLog(parentPaylog.getPaySn(), detail, parentPaylog.getPayCode(), 0, childPayAmount);
                if (paylog != null) {
                    paylog.setPayStatus(parentPaylog.getPayStatus());
                    paylog.setTransactionId(parentPaylog.getTransactionId());
                    paylogService.updateById(paylog);
                }
            }
            syncSplitOrderPaylogAmount(newOrder.getOrderId());
        }
        return newOrder;

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Order updateOrderMoney(Order order, Boolean calculatePaymentAmount) {

        // 订单总金额 (TODO)
        BigDecimal totalAmount = order.getProductAmount()
                .add(order.getServiceFee() == null ? BigDecimal.ZERO : order.getServiceFee())
                .add(order.getShippingFee() == null ? BigDecimal.ZERO : order.getShippingFee())
                .add(order.getInvoiceFee() == null ? BigDecimal.ZERO : order.getInvoiceFee())
                .add(order.getTaxRateAmount() == null ? BigDecimal.ZERO : order.getTaxRateAmount())
                .subtract(order.getPointsAmount() == null ? BigDecimal.ZERO : order.getPointsAmount())
                .subtract(order.getRoundOffCentsAmount() == null ? BigDecimal.ZERO : order.getRoundOffCentsAmount())
                .subtract(order.getRoundOffDimeAmount() == null ? BigDecimal.ZERO : order.getRoundOffDimeAmount())
                .subtract(order.getDiscountAmount() == null ? BigDecimal.ZERO : order.getDiscountAmount())
                .subtract(order.getCouponAmount() == null ? BigDecimal.ZERO : order.getCouponAmount())
                .subtract(order.getGiftCardAmount() == null ? BigDecimal.ZERO : order.getGiftCardAmount());
        order.setTotalAmount(totalAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : totalAmount);
        BigDecimal paidAmount = BigDecimal.ZERO;
        if (calculatePaymentAmount) {
            // 已付款金额（包含余额、账期支付、线上支付、线下支付、现金）
            BigDecimal balance = order.getBalance() == null ? BigDecimal.ZERO : order.getBalance();
            BigDecimal onlinePaidAmount = order.getOnlinePaidAmount() == null ? BigDecimal.ZERO : order.getOnlinePaidAmount();
            BigDecimal offlinePaidAmount = order.getOfflinePaidAmount() == null ? BigDecimal.ZERO : order.getOfflinePaidAmount();
            BigDecimal cashAmount = order.getCashAmount() == null ? BigDecimal.ZERO : order.getCashAmount();
            BigDecimal cashChangeAmount = order.getCashChangeAmount() == null ? BigDecimal.ZERO : order.getCashChangeAmount();

            paidAmount = balance
                    .add(onlinePaidAmount)
                    .add(offlinePaidAmount)
                    .add(cashAmount.subtract(cashChangeAmount));
            BigDecimal creditAmount = order.getCreditAmount() == null ? BigDecimal.ZERO : order.getCreditAmount();
            paidAmount = paidAmount.add(creditAmount);
            order.setPaidAmount(paidAmount);
        }

        // 未付款金额
        BigDecimal unpaidAmount = totalAmount.subtract(paidAmount).max(BigDecimal.ZERO);
        order.setUnpaidAmount(unpaidAmount);

        // 未退款金额
        BigDecimal unrefundAmount = paidAmount.subtract(totalAmount).max(BigDecimal.ZERO);
        order.setUnrefundAmount(unrefundAmount);

        // 更新金额
        this.updateById(order);

        // 如果未支付，且未付款金额为0，则更新订单状态
        if (unpaidAmount.compareTo(BigDecimal.ZERO) <= 0 &&
                order.getOrderStatus().equals(PENDING.getCode()) &&
                order.getPayStatus().equals(PaymentStatus.UNPAID.getCode())) {
            this.setOrderPaid(order.getOrderId());
        }

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order setOrderPaid(Integer id) {
        return setOrderPaid(id, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order setOrderPaid(Integer id, String logDescription) {
        String orderLogDesc = StringUtils.isNotEmpty(logDescription) ? logDescription : "订单支付";

        //判断能否支付
        Order order = this.getById(id);

        Order updateOrder = new Order();
        boolean updPresale = false;
        updateOrder.setOrderId(id);
        Integer presaleType = order.getPresaleType();
        if (Integer.valueOf(2).equals(presaleType)) {
            OrderPresale presale = orderPresaleService.getByOrderId(id);
            Integer presaleStatus = presale.getPresaleStatus();
            updateOrder.setPayTime(StringUtils.getCurrentTime());
            //金额
            if (order.getPayTypeId() == PaymentType.OFFLINE.getCode()) {
                if (Objects.equals(presaleStatus, 0)) {
                    //待支付定金
                    updateOrder.setOfflinePaidAmount(
                            Optional.ofNullable(presale.getDepositAmount())
                                    .orElse(BigDecimal.ZERO).subtract(order.getBalance()));
                } else if (Integer.valueOf(1).equals(presaleStatus)) {
                    //待支付尾款设置为支付成功
                    updateOrder.setOfflinePaidAmount(
                            Optional.ofNullable(order.getOfflinePaidAmount()).orElse(BigDecimal.ZERO).add(
                                    order.getUnpaidAmount()));
                }
                BigDecimal paidAmount = order.getBalance()
                        .add(order.getOnlinePaidAmount())
                        .add(updateOrder.getOfflinePaidAmount())
                        .add(order.getCashAmount().subtract(order.getCashChangeAmount()));
                order.setPaidAmount(paidAmount);
                BigDecimal unpaidAmount = order.getTotalAmount().subtract(paidAmount).max(BigDecimal.ZERO);
                if (unpaidAmount.compareTo(BigDecimal.ZERO) > 0 && Integer.valueOf(1).equals(presaleStatus)) {
                    //待支付尾款设置支付成功后,unpaid金额应该是0
                    throw new GlobalException(messageHelper.get("error.order.pay.amount.exceed"));
                }
                updateOrder.setUnpaidAmount(unpaidAmount);
            }

            OrderPresale updateOrderPresale = new OrderPresale();
            updateOrderPresale.setPresaleOrderId(presale.getPresaleOrderId());

            if (order.getOrderStatus() == PENDING.getCode()) {
                if (Objects.equals(0, presaleStatus)) {
                    //待支付定金 => 待支付尾款
                    updateOrderPresale.setPresaleStatus(1);
                    updPresale = orderPresaleService.updateById(updateOrderPresale);
                } else if (Integer.valueOf(1).equals(presaleStatus)) {
                    //待支付尾款直接设置为待发货
                    updateOrder.setOrderStatus(OrderStatusEnum.CONFIRMED.getCode());
                    updateOrderPresale.setPresaleStatus(2);
                    updateOrderPresale.setFinalPaymentPaidTime(StringUtils.getCurrentTime());
                    updateOrder.setPayStatus(PaymentStatus.PAID.getCode());
                    updPresale = orderPresaleService.updateById(updateOrderPresale);
                }
            }
        } else {
            //其他
            updateOrder.setPayStatus(PaymentStatus.PAID.getCode());
            updateOrder.setPayTime(StringUtils.getCurrentTime());
            if (order.getPayTypeId() == PaymentType.OFFLINE.getCode()) {
                updateOrder.setOfflinePaidAmount(order.getUnpaidAmount());
                updateOrder.setPaidAmount(order.getBalance().add(updateOrder.getOfflinePaidAmount()));
            }
            if (order.getOrderStatus() == PENDING.getCode()) {
                updateOrder.setOrderStatus(OrderStatusEnum.CONFIRMED.getCode());

                //fix(【H5/PC】全款预售商品线下支付成功后订单还显示待支付金额
                //https://e.gitee.com/baixiaoanyaoye/projects/767226/sprints/310193/issues/table?issue=IDBUJN)
                BigDecimal unpaidAmount = order.getTotalAmount().subtract(
                        Optional.ofNullable(updateOrder
                                .getPaidAmount()).orElse(order.getPaidAmount())).max(BigDecimal.ZERO);
                updateOrder.setUnpaidAmount(unpaidAmount);
            }
        }

        if (this.updateById(updateOrder) || updPresale) {
            order = this.getById(id);
            if (Integer.valueOf(2).equals(presaleType)) {
                if (order.getOrderStatus() == OrderStatusEnum.CONFIRMED.getCode()) {
                    orderLogService.quickCreate(orderLogDesc, id, order.getOrderSn());
                    // 待发货节点赠送积分（支付成功后）
                    this.orderSuccessPoint(order.getTotalAmount(), order.getUserId(), id, 1);
                    // 微商模式：支付成功后生成分销佣金记录（待结算）
                    this.addWechatBizSalesmanOrderOnPaid(id);
                }
            } else {
                orderLogService.quickCreate(orderLogDesc, id, order.getOrderSn());
                // 待发货节点赠送积分（支付成功后）
                this.orderSuccessPoint(order.getTotalAmount(), order.getUserId(), id, 1);

                this.orderECard(order);
                this.orderPin(order);
                // 微商模式：支付成功后生成分销佣金记录（待结算）
                this.addWechatBizSalesmanOrderOnPaid(id);
            }
        }
        splitStoreOrder(id);

        Order paidOrder = this.getById(id);
        if (paidOrder == null) {
            // ignore
        } else {

            try{
                wechatLiveOrderSyncService.onOrderPaid(paidOrder);
            }catch (Exception e){
                //
            }
        }

        // 店铺会员：支付成功后按真实订单(含拆单子单)的 shopId 记账；事务提交后执行，异常不阻断支付主流程。
        // 拆单场景只记叶子订单（跳过拆单父单与 isValid=0 的留痕单），避免父/子重复累加。
        TransactionAfterCommitExecutor.execute(() -> {
            try {
                List<Order> shopMemberOrders = list(new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderId, id).or().eq(Order::getParentOrderId, id));
                java.util.Set<Integer> splitParentIds = new java.util.HashSet<>();
                for (Order o : shopMemberOrders) {
                    if (o.getParentOrderId() != null && o.getParentOrderId() > 0) {
                        splitParentIds.add(o.getParentOrderId());
                    }
                }
                for (Order o : shopMemberOrders) {
                    if (splitParentIds.contains(o.getOrderId())) {
                        continue; // 拆单父单跳过（金额已由子单承载）
                    }
                    if (o.getIsValid() != null && o.getIsValid() == 0) {
                        continue; // 拆单留痕无效单跳过
                    }
                    shopMemberService.recordOnPaid(o);
                }
            } catch (Exception e) {
                log.error("店铺会员记账失败，不影响支付主流程，orderId={}: {}", id, e.getMessage(), e);
            }
        });

        // 自动发货与打印、通知等支付后处理使用独立回调，避免被无关失败跳过。
        registerVirtualOrderAutoDeliveryAfterCommit(id);

        Order finalOrder = order;
        TransactionAfterCommitExecutor.execute(() -> {
            try {
                if (this.lambdaQuery().eq(Order::getOrderId, id).count() > 0) {
                    boolean isGrouponWaitGroup = grouponPromotionRecordService.lambdaQuery()
                            .eq(GrouponPromotionRecord::getOrderId, (long) finalOrder.getOrderId())
                            .eq(GrouponPromotionRecord::getJoinStatus, JoinStatusEnum.WAIT_GROUP.getCode())
                            .exists();
                    if (!isGrouponWaitGroup) {
                        batchSaveStatementOutputParam(finalOrder);
                    }

                    // 礼品卡需要的操作
                    linkGiftCard(finalOrder);

//                    posOrderProcess(order);
                }
                List<Order> list = list(new LambdaQueryWrapper<Order>().eq(Order::getOrderId, id).or().eq(Order::getParentOrderId, id));
                for (Order o : list) {
                    // 添加当前订单的费率
                    recordRateService.saveRecordRate(o.getOrderId(), StatementType.ORDER.getCode(),
                            o.getShopId() != null ? o.getShopId() : 0,
                            o.getVendorId() != null ? o.getVendorId() : 0);
                    // 自动打印按 POS 支付时前端选择的设备码路由；旧订单无设备码时仍走默认打印机。
                    String deviceCode = null;
                    List<Print> routePrinters = printRoutingService.resolve(o.getShopId(), deviceCode);

                    // 通知发送失败不应影响支付主流程（双保险，与 MessageServiceImpl 内部容错叠加）
                    try {
                        messageService.sendUserMessage(o.getUserId(), o.getOrderId(), MessageTypeEnum.ORDER_PAY);
                    } catch (Exception ex) {
                        log.error("订单支付通知 ORDER_PAY 发送失败，不影响主流程，orderId={}: {}", o.getOrderId(), ex.getMessage(), ex);
                    }
                    try {
                        messageService.sendUserMessage(o.getUserId(), o.getOrderId(), MessageTypeEnum.ORDER_PAY_SHOP);
                    } catch (Exception ex) {
                        log.error("订单支付通知 ORDER_PAY_SHOP 发送失败，不影响主流程，orderId={}: {}", o.getOrderId(), ex.getMessage(), ex);
                    }

                    for (Print print : routePrinters) {
                        if (print.getAutoPrint() == 1) {
                            PrintOrderDTO printOrderDTO = PrintOrderDTO.builder()
                                    .print(print)
                                    .orderId(o.getOrderId())
                                    .deviceCode(deviceCode)
                                    .build();
                            rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE,
                                    RabbitMQConfig.PRINT_ORDER_ROUTING_KEY, printOrderDTO);
                        }
                    }
                    if (o.getParentOrderId() != null && o.getParentOrderId() > 0) {
                        boolean isChildGrouponWaitGroup = grouponPromotionRecordService.lambdaQuery()
                                .eq(GrouponPromotionRecord::getOrderId, (long) o.getOrderId())
                                .eq(GrouponPromotionRecord::getJoinStatus, JoinStatusEnum.WAIT_GROUP.getCode())
                                .exists();
                        if (!isChildGrouponWaitGroup) {
                            batchSaveStatementOutputParam(o);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("打印小票", e);
            }
        });

        return finalOrder;
    }

    /**
     * 关联礼品卡
     *
     */
    @Transactional
    public void linkGiftCard(Order order) {
        List<OrderItem> orderItems = orderItemMapper.selectList(new LambdaQueryWrapper<>(OrderItem.class)
                .eq(OrderItem::getOrderId, order.getOrderId()));

        for (OrderItem orderItem : orderItems) {
            if (PRODUCT_TYPE_GIFT_CARD_VIRTUAL.getCode() == orderItem.getProductType()) {
                Product product = productService.getById(orderItem.getProductId());
                // 根据数量循环
                Integer cardGroupId = product.getGiftCardGroupsId();
                // 支付前礼品卡库存预校验：统计未分配礼品卡数量
                Long available = giftCardsMapper.selectCount(new LambdaQueryWrapper<GiftCards>()
                        .eq(GiftCards::getCardGroupsId, cardGroupId)
                        .isNull(GiftCards::getUserId));
                if (available < orderItem.getQuantity()) {
                    throw new GlobalException(messageHelper.get("error.gift.card.stock.insufficient"));
                }
                for (int i = 0; i < orderItem.getQuantity(); i++) {
                    GiftCards giftCards = giftCardsMapper.selectOne(new LambdaQueryWrapper<GiftCards>()
                            .eq(GiftCards::getCardGroupsId, cardGroupId)
                            .isNull(GiftCards::getUserId)
                            .orderByAsc(GiftCards::getGiftCardsId)
                            .last("LIMIT 1"));
                    Assert.notNull(giftCards, () -> new GlobalException(messageHelper.get("error.gift.card.stock.insufficient")));
                    // 关联订单卡表
                    GiftCardOrder giftCardsOrder = new GiftCardOrder();
                    giftCardsOrder.setOrderId(order.getOrderId());
                    giftCardsOrder.setCardId(giftCards.getGiftCardsId());
                    giftCardOrderMapper.insert(giftCardsOrder);
                    // 修改卡状态相关信息
                    giftCards.setUserId(order.getUserId());
                    giftCards.setOrderTime(order.getAddTime());
                    GiftCardGroups giftCardGroups = giftCardGroupsMapper.selectById(giftCards.getCardGroupsId());
                    giftCards.setExpiredTime(getExpiredTime(giftCardGroups));
                    giftCardsMapper.updateById(giftCards);
                }
            }
        }


        // 礼品卡商品 与 抽奖兑换流程 中的电子卡订单：均直接置为已完成
        boolean isGiftCardOrder = order.getOrderType() == OrderType.GIFT_CARD.getCode();
        boolean isLotteryOrder = Objects.equals(order.getOrderType(), CartTypeEnum.TYPE_LOTTERY_PRODUCT.getCode());
        if (isGiftCardOrder || isLotteryOrder) {
            List<GiftCardOrderVO.GiftCardOrderInfo> giftCardOrderInfo = giftCardOrderMapper.getGiftCardOrderInfo(order.getOrderId());
            if (Objects.nonNull(giftCardOrderInfo) && !giftCardOrderInfo.isEmpty()) {
                //电子卡直接设置订单状态为已支付
                Order updOrder = new Order();
                updOrder.setOrderId(order.getOrderId());
                updOrder.setOrderStatus(OrderStatusEnum.COMPLETED.getCode());
                this.updateById(updOrder);
            }
        }
    }

    private static long getExpiredTime(GiftCardGroups giftCardGroups) {
        ValidityTypeEnum vte = ValidityTypeEnum.getByCode(giftCardGroups.getValidityType());
        return switch (vte) {
            case PERMANENT -> 0;
            case NUMBER -> {
                long currentTime = System.currentTimeMillis() / 1000;
                // 先计算出过期的那一天
                long expiredDayStart = currentTime + (giftCardGroups.getValidityDays() * 24 * 60 * 60);
                // 转换为LocalDate
                LocalDate expiredDate = Instant.ofEpochSecond(expiredDayStart).atZone(ZoneId.systemDefault()).toLocalDate();
                // 设置为当天的23:59:59
                LocalDateTime expiredDateTime = expiredDate.atTime(23, 59, 59);
                yield expiredDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
            }
            case FIXED -> {
                long expireTime = giftCardGroups.getExpireTime();
                // 获取当天23:59:59的时间戳
                LocalDateTime endOfDay = Instant.ofEpochSecond(expireTime)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .atTime(23, 59, 59);
                yield endOfDay.atZone(ZoneId.systemDefault()).toEpochSecond();
            }
            default -> throw new GlobalException("未知的卡有效期类型");
        };
    }


    /**
     * 支付成功后自动发货所覆盖的商品类型：均为无需物流配送的虚拟类商品。
     */
    private static final Set<Integer> AUTO_DELIVER_PRODUCT_TYPES = Set.of(
            PRODUCT_TYPE_VIRTUAL.getCode(),
            PRODUCT_TYPE_CARD.getCode(),
            PRODUCT_TYPE_PAID.getCode(),
            PRODUCT_TYPE_GIFT_CARD_VIRTUAL.getCode());

    /**
     * 系统自动发货的订单日志描述，用于与商家手动发货区分。
     */
    private static final String AUTO_DELIVER_LOG = "系统自动发货";

    /**
     * 注册独立的虚拟类订单自动发货回调。
     * <p>
     * 每笔真实订单都通过 {@link OrderService} 代理进入新事务；单笔失败仅记录日志，
     * 不阻断同一支付单的其它子单，也不影响后续打印、通知等回调。
     * </p>
     */
    private void registerVirtualOrderAutoDeliveryAfterCommit(Integer paidOrderId) {
        TransactionAfterCommitExecutor.execute(() -> {
            try {
                List<Order> candidateOrders = list(new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderId, paidOrderId)
                        .or()
                        .eq(Order::getParentOrderId, paidOrderId));
                OrderService orderServiceProxy = applicationContext.getBean(OrderService.class);
                for (Order candidate : candidateOrders) {
                    if (Objects.equals(candidate.getIsValid(), 0)) {
                        continue;
                    }
                    try {
                        orderServiceProxy.autoDeliverVirtualOrder(candidate.getOrderId());
                    } catch (Exception e) {
                        log.error("虚拟商品订单自动发货失败，不影响支付主流程，orderId={}: {}",
                                candidate.getOrderId(), e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                log.error("查询待自动发货的虚拟商品订单失败，不影响支付主流程，paidOrderId={}: {}",
                        paidOrderId, e.getMessage(), e);
            }
        });
    }

    /**
     * 虚拟类整单支付成功后自动发货。
     * <p>
     * 仅当「订单设置 - 虚拟商品自动发货」开启，且订单内全部商品都是无需配送的虚拟类商品时执行，
     * 以「无需配送」复用商家手动发货链路，状态流转、已发货积分与自动确认收货延时消息完全一致。
     * 卡密未足额绑定时不发货，改为向商家推送告警并保留待发货状态，转人工处理。
     * </p>
     * <p>
     * 由支付成功的事务提交后钩子通过 Spring 代理调用，并使用独立新事务提交状态；
     * 异常由注册回调逐订单捕获，不影响支付主流程或其它真实订单。
     * </p>
     *
     * @param orderId 订单id
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void autoDeliverVirtualOrder(Integer orderId) {
        if (orderId == null || !"1".equals(configService.getConfigVal(SettingsEnum.VIRTUAL_ORDER_AUTO_DELIVER))) {
            return;
        }
        // 锁定订单行后再判断状态，串行化重复支付回调，避免重复发货及重复副作用。
        Order order = this.lambdaQuery()
                .eq(Order::getOrderId, orderId)
                .last("FOR UPDATE")
                .one();
        if (order == null
                || !Objects.equals(order.getOrderStatus(), OrderStatusEnum.CONFIRMED.getCode())
                || !Objects.equals(order.getPayStatus(), PaymentStatus.PAID.getCode())
                || isExcludedFromAutoDeliver(order)) {
            return;
        }

        List<OrderItem> orderItems = orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, orderId)
                .list();
        if (CollUtil.isEmpty(orderItems) || !isAllAutoDeliverProduct(orderItems)) {
            return;
        }
        if (!isECardAllocated(orderItems)) {
            sendCardShortageMsg(order, orderItems);
            return;
        }

        // 复用发货状态机做幂等：仅「已确认，待发货」允许发货，已发货订单 deliver 为 false
        OrderVO orderVO = this.detail(orderId, null);
        if (Boolean.TRUE.equals(orderVO.getIsPickup())
                || orderVO.getAvailableActions() == null
                || !Boolean.TRUE.equals(orderVO.getAvailableActions().getDeliver())) {
            return;
        }

        List<DeliverInfo> deliverData = orderItems.stream().map(item -> {
            DeliverInfo deliverInfo = new DeliverInfo();
            deliverInfo.setItemId(item.getItemId());
            deliverInfo.setToDeliveryQuantity(item.getQuantity());
            return deliverInfo;
        }).toList();
        DeliverOrderParam param = DeliverOrderParam.builder()
                .id(orderId)
                .shippingMethod(3)
                .deliverData(deliverData)
                .userIdIsNull(true)
                .build();
        applicationContext.getBean(OrderService.class).deliverOrder(param);

        // 支付回调无登录态，日志操作人落系统管理员，避免出现无操作人的发货记录
        orderLogService.quickCreateBySystemAdmin(AUTO_DELIVER_LOG, orderId, order.getOrderSn());
    }

    /**
     * 判断订单是否属于自动发货的排除类型。
     * 预售、周期购、次卡、预约与同城配送订单都有各自的状态机，本期不纳入自动发货；
     * 拼团订单在成团前不能发货，成团后与普通虚拟订单一致，由成团路径再次触发本入口。
     */
    private boolean isExcludedFromAutoDeliver(Order order) {
        if (Integer.valueOf(1).equals(order.getIsPresale()) || Integer.valueOf(2).equals(order.getPresaleType())) {
            return true;
        }
        Integer orderType = order.getOrderType();
        if (orderType != null && OrderType.isReservationOrder(orderType)) {
            return true;
        }
        if (orderType != null && OrderType.PIN.getCode() == orderType && !isGrouponSucceeded(order.getOrderId())) {
            return true;
        }
        return Objects.equals(order.getShippingTypeId(), OrderDeliveryTypeEnum.SAME_CITY.getCode());
    }

    /**
     * 拼团订单是否已成团。记录缺失视为未成团，避免支付回调早于拼团记录写入时误发货。
     */
    private boolean isGrouponSucceeded(Integer orderId) {
        GrouponPromotionRecord record = grouponPromotionRecordService.lambdaQuery()
                .eq(GrouponPromotionRecord::getOrderId, orderId.longValue())
                .last("limit 1")
                .one();
        return record != null && JoinStatusEnum.SUCCESS.getCode().equals(record.getJoinStatus());
    }

    /**
     * 整单判定：订单内全部商品都属于无需配送的虚拟类商品时才自动发货，
     * 含实物、预约或实体礼品卡的混合订单一律走人工发货。
     */
    private boolean isAllAutoDeliverProduct(List<OrderItem> orderItems) {
        return orderItems.stream().allMatch(item -> item.getProductType() != null
                && AUTO_DELIVER_PRODUCT_TYPES.contains(item.getProductType()));
    }

    /**
     * 校验卡密商品是否已足额绑定到订单项。
     * 卡密分配存在多条路径且并非都校验数量，因此按 e_card 实际绑定行数判断，而不是假定分配已成功。
     */
    private boolean isECardAllocated(List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            if (!Objects.equals(item.getProductType(), PRODUCT_TYPE_CARD.getCode())) {
                continue;
            }
            long allocated = eCardService.lambdaQuery().eq(ECard::getOrderItemId, item.getItemId()).count();
            if (allocated < item.getQuantity()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 卡密库存不足导致自动发货未执行时，向商家推送发货提醒，转人工处理。
     */
    private void sendCardShortageMsg(Order order, List<OrderItem> orderItems) {
        String productNames = orderItems.stream()
                .filter(item -> Objects.equals(item.getProductType(), PRODUCT_TYPE_CARD.getCode()))
                .map(OrderItem::getProductName)
                .distinct()
                .collect(Collectors.joining("、"));
        AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
        adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.QUICK_DELIVERY.getCatId());
        adminMsgCreateDTO.setTitle("自动发货失败：" + order.getOrderSn());
        adminMsgCreateDTO.setContent("订单号：" + order.getOrderSn() + "，商品【" + productNames
                + "】卡密库存不足，系统未能自动发货，订单已保留在待发货状态，请补充卡密后手动发货。");
        adminMsgCreateDTO.setShopId(order.getShopId());
        adminMsgCreateDTO.setVendorId(order.getVendorId());
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("orderId", order.getOrderId());
        adminMsgCreateDTO.setRelatedData(relatedData);
        adminMsgService.createMessage(adminMsgCreateDTO);
    }

    private void orderECard(Order order) {
        if (order.getOrderType() == OrderType.CARD.getCode()) {
            List<OrderItem> orderItems = orderItemService.lambdaQuery().eq(OrderItem::getOrderId, order.getOrderId()).list();
            for (OrderItem orderItem : orderItems) {
                if (StrUtil.isNotBlank(orderItem.getCardGroupName())) {
                    Product product = productService.getById(orderItem.getProductId());

                    List<ECard> eCards = eCardService.getNewCardByGroupId(product.getCardGroupId(), orderItem.getQuantity());
                    Assert.isFalse(eCards.size() < orderItem.getQuantity(), () -> new GlobalException(messageHelper.get("error.card.stock.insufficient")));


                    eCards.forEach(eCard -> {
                        eCard.setIsUse(1);
                        eCard.setOrderId(orderItem.getOrderId());
                        eCard.setOrderItemId(orderItem.getItemId());
                    });

                    eCardService.updateBatchById(eCards);
                }
            }
        }
    }

    private void orderPin(Order order) {
        if (OrderType.PIN.getCode() != order.getOrderType()) {
            return;
        }

        List<OrderItem> orderItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getOrderId()));
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getIsPin() == 1) {
                AddToCartDTO.GrouponData grouponData = JSON.parseObject(orderItem.getPromotionData(), AddToCartDTO.GrouponData.class);
                grouponPromotionRecordService.saveRecord(orderItem, grouponData);
            }
        }
    }

    /**
     * 批量保存对账单输出参数
     *
     * @param order 订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveStatementOutputParam(Order order) {
        // 下单出账，添加流水单
        List<StatementOutputSaveParam> statementOutputSaveParamList = new ArrayList<>();

        RecordRate recordRate = recordRateService.lambdaQuery()
                .eq(RecordRate::getRecordId, order.getOrderId())
                .eq(RecordRate::getRecordType, StatementType.ORDER.getCode())
                .orderByDesc(RecordRate::getRecordRateId)
                .last("limit 1")
                .one();
        // 供应商获订单金额和店铺减余额
        boolean isVendorOrder = order.getVendorId() != null && order.getVendorId() > 0;
        BigDecimal paidAmount = order.getPaidAmount();
        if (order.getPayTypeId().equals(PaymentType.OFFLINE.getCode())) {
            paidAmount = order.getTotalAmount();
        }
        if (isVendorOrder) {
            List<OrderItem> orderItems = orderItemService.list(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getOrderId()));
            BigDecimal totalSupplyPrice = orderItems.stream()
                    .map(item -> item.getVendorProductSupplyPrice().multiply(new BigDecimal(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (order.getShopId() > 0) {
                // ①店铺订单加入流水
                BigDecimal amount = paidAmount.subtract(totalSupplyPrice);
                StatementOutputSaveParam shopAddStatementOutput = new StatementOutputSaveParam(order, StatementType.ORDER.getCode(), amount, true, true);
                statementOutputSaveParamList.add(shopAddStatementOutput);
                // ③ 店铺减去服务费
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal serviceFee = amount.multiply(recordRate.getShopServiceFee())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    StatementOutputSaveParam shopSubServiceStatementOutput = new StatementOutputSaveParam(order, StatementType.SERVICE_FEE.getCode(), serviceFee, true, false);
                    statementOutputSaveParamList.add(shopSubServiceStatementOutput);
                }
            }
            // ②供应商加入订单流水
            StatementOutputSaveParam vendorAddStatementOutput = new StatementOutputSaveParam(order, StatementType.ORDER.getCode(), totalSupplyPrice, false, true);
            statementOutputSaveParamList.add(vendorAddStatementOutput);
            // ④ 供应商减去服务费
            BigDecimal serviceFee2 = totalSupplyPrice.multiply(recordRate.getSupplierServiceFee())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            StatementOutputSaveParam vendorSubServiceStatementOutput = new StatementOutputSaveParam(order, StatementType.SERVICE_FEE.getCode(), serviceFee2, false, false);
            statementOutputSaveParamList.add(vendorSubServiceStatementOutput);
        } else {
            // ①店铺订单加入流水
            StatementOutputSaveParam shopAddStatementOutput = new StatementOutputSaveParam(order, StatementType.ORDER.getCode(), paidAmount, true, true);
            statementOutputSaveParamList.add(shopAddStatementOutput);
            // ② 店铺减去服务费
            if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal serviceFee = paidAmount.multiply(recordRate.getShopServiceFee())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                StatementOutputSaveParam shopServiceStatementOutput = new StatementOutputSaveParam(order, StatementType.SERVICE_FEE.getCode(), serviceFee, true, false);
                statementOutputSaveParamList.add(shopServiceStatementOutput);
            }
        }

        // 批量保存出账单
        statementOutputService.batchSaveStatementOutput(statementOutputSaveParamList);
    }

    /**
     * 订单送积分
     * @param orderMoney 订单金额
     * @param userId 用户ID
     * @param id 订单ID
     * @param currentNode 当前节点：1-待发货，2-已发货，3-已完成
     */
    private void orderSuccessPoint(BigDecimal orderMoney, Integer userId, Integer id, int currentNode) {
        // 获取配置的积分赠送节点，默认为1（待发货）
        String nodeVal = configService.getConfigVal(SettingsEnum.ORDER_SEND_POINT_NODE);
        int configNode = StrUtil.isNotBlank(nodeVal) ? Integer.parseInt(nodeVal) : 1;

        // 只有当前节点与配置节点匹配时才赠送积分
        if (currentNode != configNode) {
            return;
        }

        // 已完成节点：支持延迟N天后赠送积分（会员订单已完成 xxx 天赠送积分）
        if (currentNode == 3) {
            String daysVal = configService.getConfigVal(SettingsEnum.ORDER_COMPLETE_SEND_POINT_DAYS);
            int delayDays = StrUtil.isNotBlank(daysVal) ? Integer.parseInt(daysVal) : 0;
            if (delayDays > 0) {
                long delayMs = (long) delayDays * 24 * 60 * 60 * 1000;
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.DELAY_EXCHANGE,
                        RabbitMQConfig.ORDER_COMPLETE_SEND_POINT_ROUTING_KEY,
                        id,
                        message -> {
                            message.getMessageProperties().setHeader("x-delay", (int) Math.min(delayMs, Integer.MAX_VALUE));
                            return message;
                        }
                );
                log.info("订单已完成，积分延迟{}天后赠送，orderId={}", delayDays, id);
                return;
            }
        }

        // design §6 + CD-024：isGivePoints=false 时排除批发行金额（按行过滤）
        orderMoney = adjustOrderMoneyForWholesaleSendPoint(orderMoney, id);
        BigDecimal points = this.getOrderSendPoint(orderMoney);
        User user = userMapper.selectById(userId);
        Integer userRankId = user.getRankId();
        BigDecimal rankPoint = memberBenefitService.resolvePointMultiplier(userId, userRankId);
        points = points.multiply(rankPoint).setScale(0, RoundingMode.UP);

        String configVal = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
        if (points.compareTo(BigDecimal.ZERO) > 0) {
            userPointsLogService.incPoints(points.intValue(), userId, "下单送" + configVal, id);
        }

    }

    private BigDecimal getOrderSendPoint(BigDecimal orderMoney) {
        String orderSendPointVal = configService.getConfigVal(SettingsEnum.ORDER_SEND_POINT);

        BigDecimal orderSendPoint = StrUtil.isNotBlank(orderSendPointVal) ? new BigDecimal(orderSendPointVal) : BigDecimal.ZERO;

        BigDecimal points = BigDecimal.ZERO;
        if (orderSendPoint.compareTo(BigDecimal.ZERO) > 0) {
            points = orderMoney.multiply(orderSendPoint).setScale(0, RoundingMode.UP);
        }
        return points;
    }

    @Override
    public int previewSendPoints(BigDecimal orderMoney, Integer userId) {
        if (orderMoney == null || userId == null || orderMoney.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return 0;
        }
        // 与实际赠送同一公式：getOrderSendPoint(orderMoney) × 会员等级 rankPoint，向上取整
        BigDecimal points = this.getOrderSendPoint(orderMoney);
        BigDecimal rankPoint = memberBenefitService.resolvePointMultiplier(userId, user.getRankId());
        points = points.multiply(rankPoint).setScale(0, RoundingMode.UP);
        return points.compareTo(BigDecimal.ZERO) > 0 ? points.intValue() : 0;
    }

    @Override
    public void doOrderCompleteSendPoint(Integer orderId) {
        Order order = this.getById(orderId);
        if (order == null || !Objects.equals(order.getOrderStatus(), OrderStatusEnum.COMPLETED.getCode())) {
            log.debug("订单非已完成状态，不赠送积分，orderId={}", orderId);
            return;
        }
        Integer userId = order.getUserId();
        BigDecimal orderMoney = order.getTotalAmount();
        if (orderMoney == null) {
            orderMoney = BigDecimal.ZERO;
        }
        // design §6 + CD-024：isGivePoints=false 时排除批发行金额（按行过滤）
        orderMoney = adjustOrderMoneyForWholesaleSendPoint(orderMoney, orderId);
        BigDecimal points = this.getOrderSendPoint(orderMoney);
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("订单赠送积分时用户不存在，orderId={}, userId={}", orderId, userId);
            return;
        }
        Integer userRankId = user.getRankId();
        BigDecimal rankPoint = memberBenefitService.resolvePointMultiplier(userId, userRankId);
        points = points.multiply(rankPoint).setScale(0, RoundingMode.UP);
        String configVal = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
        if (points.compareTo(BigDecimal.ZERO) > 0) {
            userPointsLogService.incPoints(points.intValue(), userId, "下单送" + configVal, orderId);
            log.info("订单完成延迟赠送积分，orderId={}, userId={}, points={}", orderId, userId, points.intValue());
        }
    }

    /**
     * 批发壳：按 design §6 + CD-024 调整下单赠送积分基数。
     *
     * <p>规则：
     * <ul>
     *   <li>{@code wholesaleSettingService.isGivePoints() == true} → orderMoney 不变（含批发行金额）</li>
     *   <li>{@code wholesaleSettingService.isGivePoints() == false} → 扣减订单内批发行金额
     *       （order_item.is_wholesale_line = 1 行的 price × quantity 之和），结果不小于 0</li>
     *   <li>{@code orderId == null} 或订单无 order_item → 原样返回</li>
     * </ul>
     *
     * <p>本方法包级可见，便于单测覆盖。{@link #getOrderSendPoint} 自身保持不变，
     * 确保 B2C profile 编译产物字面无变化。
     *
     * @param orderMoney 订单金额（含批发行）
     * @param orderId    订单 ID（用于按行加载 order_item 判定批发金额）
     * @return 调整后的 orderMoney（用于 {@link #getOrderSendPoint}）
     */
    BigDecimal adjustOrderMoneyForWholesaleSendPoint(BigDecimal orderMoney, Integer orderId) {
        if (orderMoney == null) {
            return BigDecimal.ZERO;
        }
        if (orderId == null) {
            return orderMoney;
        }
        if (wholesaleSettingService.isGivePoints()) {
            return orderMoney;
        }
        List<OrderItem> items = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        if (items == null || items.isEmpty()) {
            return orderMoney;
        }
        BigDecimal wholesaleAmount = BigDecimal.ZERO;
        for (OrderItem item : items) {
            if (item == null || item.getIsWholesaleLine() == null || item.getIsWholesaleLine() != 1) {
                continue;
            }
            BigDecimal price = item.getPrice() == null ? BigDecimal.ZERO : item.getPrice();
            int qty = item.getQuantity() == null ? 0 : item.getQuantity();
            if (qty <= 0) {
                continue;
            }
            wholesaleAmount = wholesaleAmount.add(price.multiply(BigDecimal.valueOf(qty)));
        }
        return orderMoney.subtract(wholesaleAmount).max(BigDecimal.ZERO);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Integer id, Integer userId, String description) {
        OrderVO orderVO = this.detail(id, userId);
        if (Objects.equals(orderVO.getIsPresale(), 1)) {
            OrderPresaleVO presaleInfo = orderVO.getPresaleInfo();
            if (Objects.nonNull(presaleInfo) && Objects.equals(presaleInfo.getPresaleStatus(), 1)) {
                //付尾款无法取消订单
                log.info("预售订单无法取消");
                return false;
            }
        }
        if (!orderVO.getAvailableActions().getCancelOrder()) {
            throw new GlobalException(ORDER_STATUS_ERROR);
        }
        boolean buyerCancelsUnpaidOrder = userId != null
                && Objects.equals(orderVO.getPayStatus(), PaymentStatus.UNPAID.getCode());
        if (buyerCancelsUnpaidOrder) {
            description = afterSalesReasonConfigService.validateNewBuyerReason(description);
        }
        orderLogService.quickCreate(ORDER_CANCEL, id, orderVO.getOrderSn());

        // 积分在提交订单时即已扣除：兑换订单按 2026-03 规则返还兑换积分；
        // 未付款订单取消（含超时自动取消）同样返还抵扣积分，与余额返还保持对称。
        // 已付款订单的积分抵扣不在此返还，仍由售后退款流程决定。
        boolean isExchangeOrder = Objects.equals(orderVO.getIsExchangeOrder(), 1);
        boolean isUnpaidOrder = orderVO.getPayStatus() != null
                && orderVO.getPayStatus() == PaymentStatus.UNPAID.getCode();
        if (orderVO.getUsePoints() > 0 && (isExchangeOrder || isUnpaidOrder)) {
            String configVal = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
            userPointsLogService.incPoints(orderVO.getUsePoints(), orderVO.getUserId(), "订单取消返还" + (StrUtil.isNotBlank(configVal) ? configVal : "积分"), id);
        }
        //使用了余额返余额
        if (orderVO.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            userBalanceService.incBalance(orderVO.getBalance(), orderVO.getUserId(), "订单取消返还余额");
        }
        if (orderVO.getCouponAmount().compareTo(BigDecimal.ZERO) > 0) {
            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setUsedTime(0L);
            userCoupon.setOrderId(0);
            int updated = userCouponMapper.update(userCoupon,
                    Wrappers.lambdaQuery(UserCoupon.class)
                            .eq(UserCoupon::getOrderId, orderVO.getOrderId())
                            .eq(UserCoupon::getUserId, orderVO.getUserId()));
            // 拆单子单：UserCoupon.orderId 仍为原始父单ID，需回溯；
            // 仅检查有优惠券金额的兄弟子单，店铺券只归属一家店时无需等其他店取消
            if (updated == 0 && orderVO.getParentOrderId() != null && orderVO.getParentOrderId() > 0) {
                long activeCouponSiblings = this.lambdaQuery()
                        .eq(Order::getParentOrderId, orderVO.getParentOrderId())
                        .ne(Order::getOrderId, orderVO.getOrderId())
                        .gt(Order::getCouponAmount, BigDecimal.ZERO)
                        .ne(Order::getOrderStatus, OrderStatusEnum.CANCELLED.getCode())
                        .count();
                if (activeCouponSiblings == 0) {
                    userCouponMapper.update(userCoupon,
                            Wrappers.lambdaQuery(UserCoupon.class)
                                    .eq(UserCoupon::getOrderId, orderVO.getParentOrderId())
                                    .eq(UserCoupon::getUserId, orderVO.getUserId()));
                }
            }
        }
        Integer cycleProductId = null;
        Integer cycleTotalQuantity = null;
        boolean isCardMainOrder = false;

        for (OrderItemVO item : orderVO.getItems()) {

            Product product = productService.getById(item.getProductId());
            boolean useCycleTotalQty = cycleTotalQuantity != null && cycleTotalQuantity > 0
                    && cycleProductId != null && Objects.equals(item.getProductId(), cycleProductId);
            Integer originalQty = item.getQuantity();
            Integer stockQty = useCycleTotalQty ? cycleTotalQuantity : originalQty;

                if (product != null) {
                    boolean isReservationItem = item.getProductType() == PRODUCT_TYPE_RESERVATION_HOME.getCode()
                            || item.getProductType() == PRODUCT_TYPE_RESERVATION_STORE.getCode();
                    if (item.getSkuId() > 0) {
                        if (useCycleTotalQty) {
                            item.setQuantity(stockQty);
                        }
                        try {
                            handleSkuStock(item);
                        } finally {
                            if (useCycleTotalQty) {
                                item.setQuantity(originalQty);
                            }
                        }
                    } else {
                        if (useCycleTotalQty) {
                            item.setQuantity(stockQty);
                        }
                        try {
                            handleProductStock(item);
                        } finally {
                            if (useCycleTotalQty) {
                                item.setQuantity(originalQty);
                            }
                        }
                    }
                }
                // O2O 模式下增加门店销量

            // 礼品卡商品下单时会扣减礼品卡组可用库存，取消订单时需同步返还
            if (product != null && product.getGiftCardGroupsId() != null && product.getGiftCardGroupsId() > 0) {
                GiftCardGroups giftCardGroups = giftCardGroupsMapper.selectById(product.getGiftCardGroupsId());
                if (giftCardGroups != null) {
                    giftCardGroups.setAvailabilityStock(giftCardGroups.getAvailabilityStock() + stockQty);
                    giftCardGroupsMapper.updateById(giftCardGroups);
                }
            }


            if (product != null) {
                //减少销量
                if (product.getVirtualSales() != null && product.getVirtualSales() < item.getQuantity()) {
                    productService.lambdaUpdate().eq(Product::getProductId, item.getProductId()).setDecrBy(Product::getVirtualSales, product.getVirtualSales()).update();
                } else {
                    productService.lambdaUpdate().eq(Product::getProductId, item.getProductId()).setDecrBy(Product::getVirtualSales, item.getQuantity()).update();
                }
            }


            if (item.getIsSeckill() != null && item.getIsSeckill() == 1) {
                //秒杀品减少销量
                seckillService.decSales(item.getProductId(), item.getSkuId(), item.getQuantity());
                //秒杀返回库存
                seckillService.incStock(item.getProductId(), item.getSkuId(), item.getQuantity());
            }

            //库存已返还，重新判定库存预警状态
            stockWarningService.evaluate(item.getShopId(), item.getProductId(), item.getSkuId());
        }
        Order order = new Order();
        order.setOrderId(id);
        order.setOrderStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setCancelRemark(description);
        this.updateById(order);

        // 拼团订单取消：同步拼团记录状态 + 恢复限购数量
        if (OrderType.PIN.getCode() == orderVO.getOrderType()) {
            grouponPromotionRecordService.lambdaUpdate()
                    .eq(GrouponPromotionRecord::getOrderId, id.longValue())
                    .eq(GrouponPromotionRecord::getJoinStatus, JoinStatusEnum.WAIT_GROUP.getCode())
                    .set(GrouponPromotionRecord::getJoinStatus, JoinStatusEnum.CANCELLED.getCode())
                    .update();
            for (OrderItemVO item : orderVO.getItems()) {
                if (item.getIsPin() != null && item.getIsPin() == 1 && StrUtil.isNotBlank(item.getPromotionData())) {
                    try {
                        AddToCartDTO.GrouponData grouponData = JSON.parseObject(item.getPromotionData(), AddToCartDTO.GrouponData.class);
                        if (grouponData != null && grouponData.getGrouponDetailId() != null) {
                            grouponPromotionDetailService.incLimitNum(item.getQuantity(), grouponData.getGrouponDetailId());
                        }
                    } catch (Exception e) {
                        log.warn("恢复拼团限购数量异常, orderId={}, itemId={}", id, item.getItemId(), e);
                    }
                }
            }
        }

        // 发送后台信息 -- 取消订单
        Order byId = this.getById(id);
        AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
        adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.ORDER_CANCELED.getCatId());
        adminMsgCreateDTO.setTitle("订单已被取消：" + byId.getOrderSn() + ",金额：" + byId.getTotalAmount());
        adminMsgCreateDTO.setContent("订单编号：" + byId.getOrderSn() + "，订单金额：" + byId.getTotalAmount() + configService.getCurrencyName() + "，订单取消。");
        adminMsgCreateDTO.setShopId(byId.getShopId());
        adminMsgCreateDTO.setOrderId(byId.getOrderId());
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("orderId", byId.getOrderId());
        adminMsgCreateDTO.setRelatedData(relatedData);
        adminMsgService.createMessage(adminMsgCreateDTO);
        if (byId.getShopId() != null && byId.getShopId() > 0) {
            // 店铺订单转发给平台的副本：消息中心可见但恒不播报（平台只播自营）
            adminMsgCreateDTO.setShopId(0);
            adminMsgCreateDTO.setIsBroadcast(0);
            adminMsgService.createMessage(adminMsgCreateDTO);
        }
        if (byId.getVendorId() != null && byId.getVendorId() > 0) {
            // 供应商需处理自己订单 → 正常播报（复用对象，须显式重置 is_broadcast）
            adminMsgCreateDTO.setVendorId(byId.getVendorId());
            adminMsgCreateDTO.setIsBroadcast(1);
            adminMsgService.createMessage(adminMsgCreateDTO);
        }

        // 级联取消子单：提交时拆单的父订单取消时，所有子单仅做状态变更
        if (byId.getIsStoreSplited() != null && byId.getIsStoreSplited() == 1
                && (byId.getParentOrderId() == null || byId.getParentOrderId() == 0)) {
            List<Order> children = this.lambdaQuery()
                    .eq(Order::getParentOrderId, id)
                    .ne(Order::getOrderStatus, OrderStatusEnum.CANCELLED.getCode())
                    .list();
            for (Order child : children) {
                Order childCancel = new Order();
                childCancel.setOrderId(child.getOrderId());
                childCancel.setOrderStatus(OrderStatusEnum.CANCELLED.getCode());
                childCancel.setCancelRemark("父订单取消，级联取消");
                this.updateById(childCancel);
                // 子单"订单已取消"日志已由父单取消日志(ORDER_CANCEL)双写下发，此处不再单独写，避免重复
            }
        }

        return true;
    }


    /**
     * 处理 SKU 商品库存扣减
     */
    private void handleSkuStock(OrderItemVO orderItem) {
            incStockSkuAndProduct(orderItem);
    }

    /**
     * 处理普通商品库存扣减
     */
    private void handleProductStock(OrderItemVO orderItem) {
            incStockProductOnly(orderItem);
    }


    /**
     * 增加 SKU + 商品库存
     */
    private void incStockSkuAndProduct(OrderItemVO orderItem) {
        productSkuService.incStock(orderItem.getSkuId(), orderItem.getQuantity(), null);
        productService.incStock(orderItem.getProductId(), orderItem.getQuantity(), null,
                orderItem.getSkuId() != null && orderItem.getSkuId() > 0 ? false : true);
    }

    /**
     * 增加 商品库存
     */
    private void incStockProductOnly(OrderItemVO orderItem) {
        productService.incStock(orderItem.getProductId(), orderItem.getQuantity(), null);
    }

    /**
     * 增加门店销量
     */
    private void decShopSales(OrderItemVO item) {
        if (item.getShopId() != null && item.getShopId() != 0) {
            try {
                // 减少门店销量
                Shop shop = shopMapper.selectById(item.getShopId());
                shop.setShopSales(shop.getShopSales() - item.getQuantity());
                shopMapper.updateById(shop);
            } catch (Exception e) {
                throw new GlobalException("减少门店销量失败");
            }
        }
    }


    @Override
    public boolean delOrder(Integer id) {
        Integer userId = 0;
        return this.delOrder(id, userId);
    }

    @Override
    public boolean delOrder(Integer id, Integer userId) {
        OrderVO orderVO = this.detail(id, userId);
        if (!orderVO.getAvailableActions().getDelOrder()) {
            throw new GlobalException("订单状态错误");
        }
        Order order = new Order();
        order.setOrderId(id);
        order.setOrderStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setIsDel(DeleteType.DELETE.getCode());
        orderLogService.quickCreate("订单删除", id, orderVO.getOrderSn());
        return this.updateById(order);
    }

    @Override
    @Transactional
    public boolean modifyOrderMoney(OrderModifyMoneyDTO feeData) {
        OrderVO orderVO = this.detail(feeData.getId());
        if (!orderVO.getAvailableActions().getModifyOrderMoney()) {
            throw new GlobalException("订单状态错误");
        }
        Order order = new Order();
        order.setOrderId(feeData.getId());

        // 处理更换订单号逻辑
        if (feeData.getUpdateOrderSn() != null && feeData.getUpdateOrderSn() == 1) {
            // 判断订单状态：支付状态必须是未支付(0)且订单状态必须是待支付(0)
            if (!PayStatusEnum.UNPAID.getCode().equals(orderVO.getPayStatus())
                    || OrderStatusEnum.PENDING.getCode() != orderVO.getOrderStatus()) {
                throw new GlobalException("支付状态异常，仅未支付订单可更换订单号");
            }
            // 生成唯一订单号
            String newOrderSn = creatNewOrderSn();
            while (this.count(new LambdaQueryWrapper<Order>().eq(Order::getOrderSn, newOrderSn)) > 0) {
                newOrderSn = creatNewOrderSn();
            }
            order.setOrderSn(newOrderSn);
        }
        if (feeData.getShippingFee() != null) {
            order.setShippingFee(feeData.getShippingFee());
        }
        if (feeData.getInvoiceFee() != null) {
            order.setInvoiceFee(feeData.getInvoiceFee());
        }
        if (feeData.getServiceFee() != null) {
            order.setServiceFee(feeData.getServiceFee());
        }
        if (feeData.getDiscountAmount() != null) {
            order.setDiscountAmount(feeData.getDiscountAmount());
            BigDecimal discount = feeData.getDiscountAmount();
            if (CollUtil.isNotEmpty(orderVO.getItems())) {
                List<OrderItemVO> itemVOs = orderVO.getItems();
                BigDecimal totalSubtotal = itemVOs.stream()
                        .map(item ->
                                (item.getAdminChangePrePrice() != null ? item.getAdminChangePrePrice() :
                                        item.getPrice()).multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (totalSubtotal.compareTo(BigDecimal.ZERO) > 0) {
                    List<OrderItem> dbItems = orderItemService.lambdaQuery()
                            .eq(OrderItem::getOrderId, feeData.getId())
                            .list();
                    Map<Integer, OrderItem> dbItemMap = dbItems.stream()
                            .collect(Collectors.toMap(OrderItem::getItemId, Function.identity()));

                    BigDecimal allocatedSum = BigDecimal.ZERO;
                    List<OrderItem> toUpdate = new ArrayList<>();

                    for (int i = 0; i < itemVOs.size(); i++) {
                        OrderItemVO itemVO = itemVOs.get(i);
                        OrderItem entity = dbItemMap.get(itemVO.getItemId());
                        if (entity == null) {
                            continue;
                        }

                        BigDecimal quantity = BigDecimal.valueOf(itemVO.getQuantity());
                        BigDecimal originalUnitPrice = (itemVO.getAdminChangePrePrice() != null ? itemVO.getAdminChangePrePrice() : itemVO.getPrice());

                        BigDecimal lineDiscount;
                        if (i < itemVOs.size() - 1) {
                            lineDiscount = discount.multiply((itemVO.getAdminChangePrePrice() != null ? itemVO.getAdminChangePrePrice() :
                                            itemVO.getPrice()).multiply(BigDecimal.valueOf(itemVO.getQuantity())))
                                    .divide(totalSubtotal, 2, RoundingMode.HALF_UP);
                            allocatedSum = allocatedSum.add(lineDiscount);
                        } else {
                            lineDiscount = discount.subtract(allocatedSum);
                        }

                        BigDecimal perUnitDiscount = lineDiscount.divide(quantity, 2, RoundingMode.HALF_UP);
                        BigDecimal newUnitPrice = originalUnitPrice.subtract(perUnitDiscount);
                        if (newUnitPrice.compareTo(BigDecimal.ZERO) < 0) {
                            newUnitPrice = BigDecimal.ZERO;
                        }
                        entity.setAdminChangePrePrice(originalUnitPrice);
                        entity.setPrice(newUnitPrice);
                        toUpdate.add(entity);
                    }

                    // 如果所有 item price 都小于等于 0
                    Optional<BigDecimal> reduce = toUpdate.stream().map(OrderItem::getPrice).reduce(BigDecimal::add);
                    if (reduce.isPresent() && reduce.get().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new GlobalException("商品价格总和减去优惠价格（优惠券抵扣，优惠折扣）必须大于0");
                    }

                    if (!toUpdate.isEmpty()) {
                        orderItemService.updateBatchById(toUpdate);
                        List<OrderAmountDetail> list = orderAmountDetailService
                                .list(new LambdaQueryWrapper<OrderAmountDetail>().eq(OrderAmountDetail::getOrderId, order.getOrderId()));
                        if (CollUtil.isNotEmpty(list)) {
                            Map<Integer, BigDecimal> shopSubtotalMap = itemVOs.stream()
                                    .collect(Collectors.groupingBy(OrderItemVO::getShopId,
                                            Collectors.mapping(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())), Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
                            BigDecimal allocated = BigDecimal.ZERO;
                            for (int i = 0; i < list.size(); i++) {
                                OrderAmountDetail detail = list.get(i);
                                BigDecimal shopSubtotal = shopSubtotalMap.getOrDefault(detail.getShopId(), BigDecimal.ZERO);
                                BigDecimal part;
                                if (i < list.size() - 1) {
                                    part = discount.multiply(shopSubtotal).divide(totalSubtotal, 2, RoundingMode.HALF_UP);
                                    allocated = allocated.add(part);
                                } else {
                                    part = discount.subtract(allocated);
                                }
                                detail.setDiscountAmount(part);
                            }
                            orderAmountDetailService.updateBatchById(list);
                        }
                    }
                }
            }
        }
        boolean b = this.updateById(order);
        Order orderSelect = this.getById(order.getOrderId());
        updateOrderMoney(orderSelect, true);
        orderLogService.quickCreate("订单金额修改", feeData.getId(), orderVO.getOrderSn());
        return b;
    }

    @Override
    public boolean modifyOrderConsignee(OrderModifyConsigneeDTO modifyConsigneeDTO) {
        OrderVO orderVO = this.detail(modifyConsigneeDTO.getId());
        if (!orderVO.getAvailableActions().getModifyOrderConsignee()) {
            throw new GlobalException("订单状态错误");
        }
        Order order = new Order();
        order.setOrderId(modifyConsigneeDTO.getId());
        order.setConsignee(modifyConsigneeDTO.getConsignee());
        order.setMobile(modifyConsigneeDTO.getMobile());
        order.setEmail(modifyConsigneeDTO.getEmail());
        order.setAddress(modifyConsigneeDTO.getAddress());
        order.setRegionIds(JSONUtil.toJsonStr(modifyConsigneeDTO.getRegionIds()));
        List<String> regionNames = regionService.getRegionNamesByRegionIds(modifyConsigneeDTO.getRegionIds());
        order.setRegionNames(JSONUtil.toJsonStr(regionNames));
        UserAddressVO userAddress = new UserAddressVO();
        userAddress.setAddress(modifyConsigneeDTO.getAddress());
        userAddress.setRegionIds(modifyConsigneeDTO.getRegionIds());
        userAddress.setPostcode(modifyConsigneeDTO.getPostcode());
        userAddress.setTelephone(modifyConsigneeDTO.getTelephone());
        userAddress.setMobile(modifyConsigneeDTO.getMobile());
        userAddress.setEmail(modifyConsigneeDTO.getEmail());
        userAddress.setRegionNames(regionNames);
        if (orderVO.getAddressData() != null) {
            userAddress.setAddressTag(orderVO.getAddressData().getAddressTag());
            userAddress.setUserId(orderVO.getAddressData().getUserId());
            userAddress.setAddressId(orderVO.getAddressData().getAddressId());
            userAddress.setIsDefault(orderVO.getAddressData().getIsDefault());
            userAddress.setIsSelected(orderVO.getAddressData().getIsSelected());
        }
        order.setAddressData(JSONUtil.toJsonStr(userAddress));
        orderLogService.quickCreate("修改收货人信息", orderVO.getOrderId(), orderVO.getOrderSn());
        return this.updateById(order);
    }


    @Override
    @Transactional
    public boolean confirmReceipt(Integer orderId) {
        return this.confirmReceipt(orderId, null);

    }

    @Override
    public boolean confirmReceipt(Integer orderId, Integer userId) {
        OrderVO orderVO = this.detail(orderId, userId);
        Assert.isFalse(!orderVO.getAvailableActions().getConfirmReceipt(), () -> new GlobalException("订单状态错误"));

        sendOrderStatement(orderVO);


        //这里增加一个判断, 如果是预约上门的话, 变成完成上门服务
        if (Objects.equals(OrderType.RESERVATION_HOME.getCode(),orderVO.getOrderType())) {
            orderLogService.quickCreate("完成上门服务", orderId, orderVO.getOrderSn());
        } else {
            orderLogService.quickCreate("确认收货", orderId, orderVO.getOrderSn());
        }
        //分销结算
        autoSettlement(orderVO);

        // 分销佣金由店铺承担：非自营店铺订单，创建佣金扣减出账记录
        if (orderVO.getShopId() > 0) {
            BigDecimal totalCommission = salesmanOrderService.getTotalCommissionByOrderId(orderId);
            if (totalCommission.compareTo(BigDecimal.ZERO) > 0) {
                Order currentOrderForOutput = getById(orderId);
                StatementOutputSaveParam commissionOutput = new StatementOutputSaveParam(
                        currentOrderForOutput, StatementType.DISTRIBUTION_COMMISSION.getCode(),
                        totalCommission, true, false);
                statementOutputService.saveStatementOutput(commissionOutput);
            }
        }

        //店铺结算
        this.storeSettlement(orderVO.getOrderId());

        Order currentOrder = getById(orderId);
        //平台端出售供应商商品结算

        // 创建管理员日志
        AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
        adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.ORDER_COMPLETED.getCatId());
        adminMsgCreateDTO.setTitle("订单已完成通知：" + orderVO.getOrderSn() + ",金额：" + orderVO.getTotalAmount());
        adminMsgCreateDTO.setContent("您有订单【" + orderVO.getOrderSn() + "】已完成,请注意查看。");
        adminMsgCreateDTO.setShopId(orderVO.getShopId());
        adminMsgCreateDTO.setOrderId(orderVO.getOrderId());

        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("orderId", orderVO.getOrderId());
        adminMsgCreateDTO.setRelatedData(relatedData);


        adminMsgService.createMessage(adminMsgCreateDTO);

        if (adminMsgCreateDTO.getShopId() != null && adminMsgCreateDTO.getShopId() > 0) {
            //如果是店铺订单则给平台订单也发一条
            adminMsgCreateDTO.setShopId(0);
            adminMsgService.createMessage(adminMsgCreateDTO);
        }

        if (orderVO.getVendorId() != null && orderVO.getVendorId() > 0) {
            //如果是供应商订单则给供应商订单也发一条
            adminMsgCreateDTO.setVendorId(orderVO.getVendorId());
            adminMsgService.createMessage(adminMsgCreateDTO);
        }

        // 更新订单状态
        Order order = new Order();
        order.setOrderId(orderId);
        order.setShippingStatus(ShippingStatusEnum.SHIPPED.getCode());
        order.setOrderStatus(OrderStatusEnum.COMPLETED.getCode());
        order.setReceivedTime(StringUtils.getCurrentTime());
        boolean b = this.updateById(order);

        // 次卡兑换子单只有在实际确认完成/核销后，才推进并结算主订单。
        completeCycleCardParentOrderForChild(orderVO);

        // 已完成节点赠送积分（确认收货时）
        this.orderSuccessPoint(orderVO.getTotalAmount(), orderVO.getUserId(), orderId, 3);

        // 订单成交，用于分销员等级升级
        List<SalesmanOrder> salesmanOrders = salesmanOrderMapper.selectList(new LambdaQueryWrapper<SalesmanOrder>().eq(SalesmanOrder::getOrderId, orderId));
        salesmanOrders.forEach(salesmanOrder -> {
            if (salesmanOrder != null) {
                // 分销员分销订单
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.DIRECT_EXCHANGE,
                        RabbitMQConfig.SALESMAN_UPDATE_ROUTING_KEY,
                        salesmanOrder.getSalesmanId());
            }
        });
        Salesman salesman = salesmanMapper.selectOne(new LambdaQueryWrapper<Salesman>().eq(Salesman::getUserId, orderVO.getUserId()));
        if (salesman != null) {
            // 分销员自购订单
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.DIRECT_EXCHANGE,
                    RabbitMQConfig.SALESMAN_UPDATE_ROUTING_KEY,
                    salesman.getSalesmanId());
        }
        // 增加客户成长值，升级客户等级
        userRankService.getRankGrowth(orderVO.getUserId());
        return b;
    }

    private void completeCycleCardParentOrderForChild(OrderVO childOrder) {
        if (childOrder.getParentOrderId() == null || childOrder.getParentOrderId() <= 0) {
            return;
        }
        Order parentOrder = this.getById(childOrder.getParentOrderId());
        if (parentOrder == null
                || !Objects.equals(parentOrder.getIsCardOrder(), Constants.YES)
                || (parentOrder.getParentOrderId() != null && parentOrder.getParentOrderId() > 0)) {
            return;
        }
        this.completeCycleCardParentOrder(parentOrder.getOrderId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeCycleCardParentOrder(Integer orderId) {
        if (orderId == null || orderId <= 0) {
            return false;
        }
        Order parentOrder = this.getById(orderId);
        if (parentOrder == null
                || !Objects.equals(parentOrder.getIsCardOrder(), Constants.YES)
                || (parentOrder.getParentOrderId() != null && parentOrder.getParentOrderId() > 0)
                || Objects.equals(parentOrder.getOrderStatus(), OrderStatusEnum.CANCELLED.getCode())
                || Objects.equals(parentOrder.getOrderStatus(), OrderStatusEnum.INVALID.getCode())
                || !Objects.equals(parentOrder.getIsSettlement(), Constants.NO)) {
            return false;
        }

        long completedTime = StringUtils.getCurrentTime();
        boolean claimed = this.lambdaUpdate()
                .eq(Order::getOrderId, orderId)
                .eq(Order::getIsSettlement, Constants.NO)
                .notIn(Order::getOrderStatus, List.of(
                        OrderStatusEnum.CANCELLED.getCode(),
                        OrderStatusEnum.INVALID.getCode()))
                .and(wrapper -> wrapper.ne(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode())
                        .or().isNull(Order::getReceivedTime))
                .set(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode())
                .set(Order::getReceivedTime, completedTime)
                .update();
        if (!claimed) {
            return false;
        }

        parentOrder.setOrderStatus(OrderStatusEnum.COMPLETED.getCode());
        parentOrder.setReceivedTime(completedTime);
        OrderVO parentOrderVO = new OrderVO();
        parentOrderVO.setOrderId(parentOrder.getOrderId());
        parentOrderVO.setPayTypeId(parentOrder.getPayTypeId());
        parentOrderVO.setBalance(parentOrder.getBalance());
        parentOrderVO.setShopId(parentOrder.getShopId());
        parentOrderVO.setVendorId(parentOrder.getVendorId());
        TransactionAfterCommitExecutor.execute(() -> {
            sendOrderStatement(parentOrderVO);
            storeSettlement(orderId);
        });
        return true;
    }

    private void sendOrderStatement(OrderVO orderVO) {
        // 对账单生成
        String collectionTime = configService.getConfigVal(COLLECTION_TIME_SETTING);
        if (StringUtils.isEmpty(collectionTime)) {
            collectionTime = "10";
        }
        BigDecimal delayTime = new BigDecimal(collectionTime).multiply(new BigDecimal(24 * 60 * 60 * 1000));
        StatementSaveParam statementSaveParam = new StatementSaveParam();
        statementSaveParam.setAccountType(AccountType.ACCOUNT_BALANCE.getCode());
        statementSaveParam.setType(StatementType.ORDER.getCode());
        statementSaveParam.setEntryType(EntryType.AUTO.getCode());
        Paylog paylog = paylogMapper.selectOne(new LambdaQueryWrapper<Paylog>()
                .eq(Paylog::getOrderId, orderVO.getOrderId())
                // 商品订单：orderType=0，避免撞充值单号
                .eq(Paylog::getOrderType, 0)
                .orderByDesc(Paylog::getPaylogId)
                .last("LIMIT 1")
        );
        if (paylog != null) {
            statementSaveParam.setPaymentType(paylog.getPayCode());
        } else if (Objects.equals(orderVO.getPayTypeId(), PaymentType.OFFLINE.getCode())) {
            statementSaveParam.setPaymentType(PayMethodType.OFFLINE.getCode());
        } else if (orderVO.getBalance() != null && orderVO.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            statementSaveParam.setPaymentType(PayMethodType.BALANCE.getCode());
        } else {
            statementSaveParam.setPaymentType(PayMethodType.OTHER.getCode());
        }
        statementSaveParam.setShopId(orderVO.getShopId());
        statementSaveParam.setVendorId(orderVO.getVendorId());
        statementSaveParam.setRecordId(orderVO.getOrderId());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DELAY_EXCHANGE,
                RabbitMQConfig.GENERATE_STATEMENT_ROUTING_KEY,
                statementSaveParam,
                message -> {
                    message.getMessageProperties().setHeader("x-delay", delayTime.longValue());
                    return message;
                }
        );
    }


    private void autoSettlement(OrderVO orderVO) {
        // 查询结算设置
        SalesmanConfig salesmanConfig = salesmanConfigMapper.selectOne(new LambdaQueryWrapper<SalesmanConfig>()
                .eq(SalesmanConfig::getCode, SalesmanConfigTypeEnum.SALESMAN_SETTLEMENT.getCode()));
        if (salesmanConfig == null) {
            return;
        }

        JSONObject jsonObject = JSONUtil.parseObj(salesmanConfig.getData());
        // 1.自动结算
        if (jsonObject.containsKey("settlementType") && jsonObject.getInt("settlementType") == 1) {
            if (jsonObject.containsKey("dateType") && jsonObject.getInt("dateType") == 0) {
                // 延时15天结算
//                BigDecimal delayTime = new BigDecimal(15).multiply(new BigDecimal(24 * 60 * 60 * 1000));
                // 延时5分钟结算（5分钟 = 5 * 60秒 * 1000毫秒 = 300000毫秒）临时测试
                BigDecimal delayTime = new BigDecimal(5 * 60 * 1000);
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.DELAY_EXCHANGE,
                        RabbitMQConfig.SALESMAN_SETTLEMENT_ROUTING_KEY,
                        orderVO.getOrderId(),
                        message -> {
                            message.getMessageProperties().setHeader("x-delay", delayTime.longValue());
                            return message;
                        }
                );
            } else {
                // 收货立即结算
                settlement(orderVO.getOrderId(), 1);
            }
        }
        // 2.人工结算：收货时不自动结算，分销记录保持"待结算"，由管理员在后台手动结算
        // （管理员触发的人工结算与自动结算资金流转一致：标记已结算并发放佣金到佣金账户）。见 SalesmanOrderServiceImpl.manualSettle
    }

    @Override
    @Transactional
    public void settlement(Integer orderId, Integer settlementType) {
        List<SalesmanOrder> salesmanOrders = salesmanOrderMapper.selectList(new LambdaQueryWrapper<SalesmanOrder>().eq(SalesmanOrder::getOrderId, orderId));
        for (SalesmanOrder salesmanOrder : salesmanOrders) {
            OrderVO orderVO = this.detail(orderId);
            if (orderVO == null) {
                return;
            }
            if (salesmanOrder != null) {
                // 一单可能有多条分销记录（如一级+二级分销），已结算/已失效的跳过继续处理后续记录，
                // 不能 return 提前中断整个循环，否则会漏结后面的待结算记录。
                if (salesmanOrder.getStatus() != 0) {
                    continue;
                }
                salesmanOrder.setStatus(1);
                salesmanOrder.setSettlementTime(StringUtils.getCurrentTime());
                salesmanOrderMapper.updateById(salesmanOrder);
                Salesman salesman = salesmanMapper.selectById(salesmanOrder.getSalesmanId());
                if (salesman != null) {
                    UpdateWrapper<Salesman> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("salesman_id", salesman.getSalesmanId())
                            .setSql("sale_amount = sale_amount + " + salesmanOrder.getOrderAmount());
                    salesmanMapper.update(null, updateWrapper);
                    if (settlementType == 1) {
                        // 自动结算进入佣金账户（D1107：与会员余额隔离，单独提现）
                        userBalanceService.incCommissionBalance(salesmanOrder.getAmount(), salesman.getUserId(), "订单" + orderVO.getOrderSn() + "结算佣金");
                    }
                    // 推广金额已结算累加后，重新判断分销员是否可升级（推广金额条件仅统计已结算订单）
                    rabbitTemplate.convertAndSend(
                            RabbitMQConfig.DIRECT_EXCHANGE,
                            RabbitMQConfig.SALESMAN_UPDATE_ROUTING_KEY,
                            salesman.getSalesmanId());
                }
            }
        }
    }

    /**
     * 微商分销模式：支付成功后生成分销佣金记录（待结算）。
     * <p>仅微商模式生效；按订单幂等，避免支付回调重复触发重复发佣。</p>
     *
     * @param orderId 订单ID
     */
    private void addWechatBizSalesmanOrderOnPaid(Integer orderId) {
        SalesmanConfig salesmanConfig = salesmanConfigMapper.selectOne(new LambdaQueryWrapper<SalesmanConfig>()
                .eq(SalesmanConfig::getCode, SalesmanConfigTypeEnum.SALESMAN_CONFIG.getCode()));
        if (salesmanConfig == null) {
            return;
        }
        Integer distributionMode = JSONUtil.parseObj(salesmanConfig.getData()).getInt("distributionMode");
        if (!DistributionModeEnum.isWechatBiz(distributionMode)) {
            return;
        }
        Order order = this.getById(orderId);
        if (order == null) {
            return;
        }
        // 幂等：该订单已生成过分销佣金记录则跳过（防止支付回调重复触发）
        long existed = salesmanOrderService.lambdaQuery()
                .eq(SalesmanOrder::getOrderId, orderId)
                .count();
        if (existed > 0) {
            return;
        }
        List<OrderItem> orderItemList = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        if (CollUtil.isEmpty(orderItemList)) {
            return;
        }
        salesmanOrderService.addSalesmanOrderForWechatBiz(orderItemList, order.getUserId());
    }


    private void storeSettlement(Integer orderId) {
        String collectionTimeSetting = configService.getConfigVal(COLLECTION_TIME_SETTING);
        int useDays = StrUtil.isNotBlank(collectionTimeSetting) ? Integer.parseInt(collectionTimeSetting) : 0;
        BigDecimal delayTime = new BigDecimal(useDays).multiply(new BigDecimal(24 * 60 * 60 * 1000)).add(new BigDecimal(3000));
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DELAY_EXCHANGE,
                RabbitMQConfig.STORE_SETTLEMENT_ROUTING_KEY,
                orderId,
                message -> {
                    message.getMessageProperties().setHeader("x-delay", delayTime.longValue());
                    return message;
                }
        );

    }

    @Override
    public void vendorSettlement(Integer orderId, Integer vendorId, Integer shopId) {
        int useDays = 0;
        if (shopId == 0) {
            String configVal = configService.getConfigVal(SettingsEnum.COLLECTION_TIME_SETTING);
            useDays = StrUtil.isNotBlank(configVal) ? Integer.parseInt(configVal) : 0;
        }
        BigDecimal delayTime = new BigDecimal(useDays).multiply(new BigDecimal(24 * 60 * 60 * 1000));
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DIRECT_EXCHANGE,
                RabbitMQConfig.VENDOR_SETTLEMENT_ROUTING_KEY,
                orderId,
                message -> {
                    message.getMessageProperties().setHeader("x-delay", delayTime.longValue());
                    return message;
                }
        );
    }

    @Override
    public boolean modifyOrderShipping(OrderModifyShippingDTO modifyShippingDTO) {
        OrderVO orderVO = this.detail(modifyShippingDTO.getId());
        if (!orderVO.getAvailableActions().getModifyShippingInfo()) {
            throw new GlobalException("订单状态错误");
        }
        Order order = new Order();
        order.setOrderId(modifyShippingDTO.getId());
        order.setShippingMethod(modifyShippingDTO.getShippingMethod());

        if (modifyShippingDTO.getShippingMethod() == 1) {
            order.setLogisticsProvider(logisticsProviderRouter.getActive().providerType());
            order.setLogisticsId(modifyShippingDTO.getLogisticsId());
            LogisticsCompany logisticsCompany = logisticsCompanyService.getById(modifyShippingDTO.getLogisticsId());
            if (logisticsCompany != null) {
                order.setLogisticsName(logisticsCompany.getLogisticsName());
            }
            order.setTrackingNo(modifyShippingDTO.getTrackingNo());
        } else if (modifyShippingDTO.getShippingMethod() == 2) {
            order.setLogisticsProvider(LogisticsProviderType.UNKNOWN.getCode());
            order.setLogisticsId(0);
            order.setTrackingNo("");
            order.setLogisticsName("商家配送");
        } else if (modifyShippingDTO.getShippingMethod() == 3) {
            order.setLogisticsProvider(LogisticsProviderType.UNKNOWN.getCode());
            order.setLogisticsId(0);
            order.setTrackingNo("");
            order.setLogisticsName("无需配送");
        } else if (modifyShippingDTO.getShippingMethod() == 4) {
            // 电子面单
            order.setLogisticsProvider(logisticsProviderRouter.getActive().providerType());
            order.setLogisticsId(modifyShippingDTO.getLogisticsId());
            LogisticsCompany logisticsCompany = logisticsCompanyService.getById(modifyShippingDTO.getLogisticsId());
            if (logisticsCompany != null) {
                order.setLogisticsName(logisticsCompany.getLogisticsName());
            }
            order.setTrackingNo("");
        }
        boolean result = this.updateById(order);
        if (result) {
            orderLogService.quickCreate("修改配送信息", modifyShippingDTO.getId(), orderVO.getOrderSn());
        }
        return false;
    }

    @Override
    public ShippingResult getShippingInfo(Integer id, Integer userId) {
        Order order = this.lambdaQuery()
                .eq(Order::getOrderId, id)
                .eq(userId != null && userId != 0, Order::getUserId, userId)
                .one();
        if (order == null || order.getTrackingNo() == null || StringUtils.isEmpty(order.getTrackingNo())) {
            return null;
        }
        LogisticsCompany logisticsCompany = logisticsCompanyMapper.selectById(order.getLogisticsId());
        if (logisticsCompany == null) {
            return null;
        }
        int providerSnapshot = order.getLogisticsProvider() == null
                ? LogisticsProviderType.UNKNOWN.getCode()
                : order.getLogisticsProvider();
        LogisticsProvider provider = logisticsProviderRouter.resolveForQuery(providerSnapshot, logisticsCompany);
        if (provider == null) {
            return null;
        }
        return provider.queryTrack(logisticsCompany, order.getTrackingNo(), order.getMobile());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchOperation(Integer id, String type, BatchDeliverData data) {
        if ("deliver".equals(type)) {
            OrderVO orderVO = this.detail(id);
            if (!orderVO.getAvailableActions().getDeliver()) {
                throw new GlobalException("状态错误");
            }
            if (data == null) {
                throw new GlobalException("请选择发货信息");
            }
            String trackingNo = "";
            List<DeliverInfo> deliverinfo = List.of();
            for (DeliverDataList deliverDataListList : data.getDeliverData()) {
                if (orderVO.getOrderId().equals(deliverDataListList.getId())) {
                    deliverinfo = deliverDataListList.getDeliverInfo();
                    trackingNo = deliverDataListList.getTrackingNo();
                    break;
                }
            }
            DeliverOrderParam param = DeliverOrderParam.builder()
                    .id(id)
                    .deliverData(deliverinfo)
                    .shippingMethod(data.getShippingMethod())
                    .logisticsId(data.getLogisticsId())
                    .trackingNo(trackingNo)
                    .build();
            this.deliverOrder(param);
        }
    }

    @Override
    public List<OrderVO> printSeveral(List<Integer> ids) {
        List<OrderVO> orderPrint = new ArrayList<>();
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        // 构建URL
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        // 如果端口不是默认的80或443，则添加端口号
        if (!(serverPort == 80 && "http".equals(scheme)) && !(serverPort == 443 && "https".equals(scheme))) {
            url.append(":").append(serverPort);
        }
        Date date = DateUtil.date(System.currentTimeMillis());
        for (Integer id : ids) {
            OrderVO orderVO = this.detail(id);
            orderVO.setHost(url.toString());
            orderVO.setPrintTime(String.valueOf(date));
            orderPrint.add(orderVO);
        }
        return orderPrint;
    }

    @Override
    public void deliverOrder(OrderVO orderVO, DeliverOrderParam param) {
        Assert.notNull(orderVO, () -> new GlobalException("未知的订单"));
        Assert.isTrue(orderVO.getAvailableActions().getDeliver(), () -> new GlobalException("状态错误"));

        // 平台只能给平台自己的订单发货，不能操作店铺/门店/供应商的订单
        if ("admin".equals(HeaderUtils.getAdminType())) {
            boolean isShopOrder = orderVO.getShopId() != null && orderVO.getShopId() > 0;
            boolean isVendorOrder = orderVO.getVendorId() != null && orderVO.getVendorId() > 0;
            if (isShopOrder || isVendorOrder) {
                throw new GlobalException("平台不能给店铺/门店/供应商的订单发货，请由对应商家操作");
            }
        }

        // 查询订单信息
        List<Aftersales> aftersalesList = aftersalesService.lambdaQuery().eq(Aftersales::getOrderId, param.getId()).list();

        // 仅当售后按数量完整覆盖整单时才禁止发货：整单售后审核通过后订单维持原状态（待发货/已发货）
        // 直到财务退款到账。部分售后不拦截，售后数量在发货明细中单独扣减，剩余商品仍可正常发出。
        boolean hasAftersales = aftersalesService.hasInProgressFullOrderAftersales(param.getId());
        // 预约到店订单核销时，提示"无法核销"而不是"无法发货"
        String actionText = OrderType.isReservationOrder(orderVO.getOrderType()) ? "核销" : "发货";
        if(Objects.equals(param.getFrom(),1)){
            actionText = "领取任务";
        }
        final String finalActionText = actionText;
        Assert.isFalse(hasAftersales, () -> new GlobalException("该订单存在售后，无法" + finalActionText));

        List<AftersalesItem> aftersalesItems = new ArrayList<>();
        if (CollUtil.isNotEmpty(aftersalesList)) {
            List<Integer> aftersalesIds = aftersalesList.stream().map(Aftersales::getAftersaleId).toList();
            aftersalesItems = aftersalesItemService.lambdaQuery().in(AftersalesItem::getAftersaleId, aftersalesIds).list();
        }

        // 已取消 / 已驳回的售后不再占用可发数量，数量计算只取有效状态的售后明细；
        // 下方售后迁移仍用全量明细，避免遗留明细的 orderItemId 指向被删除的原订单项
        Set<Integer> validAftersalesIds = aftersalesList.stream()
                .filter(aftersales -> AftersalesStatusEnum.validStatus().contains(aftersales.getStatus()))
                .map(Aftersales::getAftersaleId)
                .collect(Collectors.toSet());
        List<AftersalesItem> validAftersalesItems = aftersalesItems.stream()
                .filter(item -> validAftersalesIds.contains(item.getAftersaleId()))
                .toList();

        List<OrderItemVO> orderItems = orderVO.getItems();
        List<OrderItemVO> remainItems = param.getRemainItems(orderItems, validAftersalesItems);
        List<OrderItemVO> deliverItems = param.getDeliverItems(orderItems, validAftersalesItems);

        // 3. 拆分订单逻辑（如果有部分发货或多仓库）
        Integer deliverOrderId = param.getId();
        if (CollUtil.isNotEmpty(remainItems)) {
            // 创建发货订单
            List<OrderItemVO> newOrderItems = BeanUtil.copyToList(deliverItems, OrderItemVO.class);
            Order deliverOrder = this.createSplitOrder(orderVO, newOrderItems, false, orderVO.getShopId(), false);
            deliverOrderId = deliverOrder.getOrderId();

            // 将原订单的售后记录迁移到发货订单
            if (CollUtil.isNotEmpty(aftersalesList)) {
                Integer finalDeliverOrderId = deliverOrderId;
                aftersalesList.forEach(aftersales -> aftersales.setOrderId(finalDeliverOrderId));
                aftersalesService.updateBatchById(aftersalesList);

                List<OrderItem> items = orderItemService.lambdaQuery().eq(OrderItem::getOrderId, deliverOrderId).list();
                Map<Integer, Integer> newOrderItemMap = items.stream().collect(
                        Collectors.toMap(OrderItem::getProductId, OrderItem::getItemId, (v1, v2) -> v1)
                );
                Map<Integer, Integer> itemIdMap = deliverItems.stream().collect(Collectors.toMap(OrderItemVO::getItemId, item -> newOrderItemMap.get(item.getProductId())));

                List<AftersalesItem> delAftersalesItems = aftersalesItems.stream()
                        .filter(item -> itemIdMap.get(item.getOrderItemId()) == null || item.getNumber() == 0)
                        .toList();

                List<AftersalesItem> updateAftersalesItems = aftersalesItems.stream()
                        .filter(item -> itemIdMap.get(item.getOrderItemId()) != null && item.getNumber() > 0)
                        .peek(item -> {
                            Integer newOrderItemId = itemIdMap.get(item.getOrderItemId());
                            item.setOrderItemId(newOrderItemId);
                        })
                        .collect(Collectors.toList());

                aftersalesItemService.updateBatchById(updateAftersalesItems);
                aftersalesItemService.removeByIds(delAftersalesItems);
            }

            Order remainOrder = this.createSplitOrder(orderVO, remainItems, false, orderVO.getShopId(), false);

            // 拆分分销订单（在删除原订单前）
            splitSalesmanOrdersForDeliver(orderVO, deliverOrder, remainOrder, deliverItems, remainItems);
            // 税费补齐：确保发货拆单后子订单税费总和等于原订单税费
            adjustSplitOrderTaxAmounts(orderVO, Arrays.asList(deliverOrder, remainOrder));

            // 记录拆单日志
            orderLogService.quickCreate("订单商品来自不同店铺，已拆分", orderVO.getOrderId(), orderVO.getOrderSn());
            // 删除原订单
            this.removeById(orderVO.getOrderId());
            try{
                wechatLiveOrderRelService.removeByOrderId(orderVO.getOrderId());
            }catch (Exception e){

            }
            orderItemService.remove(new QueryWrapper<OrderItem>().eq("order_id", orderVO.getOrderId()));
        }

        // 4. 更新订单状态
        Order order = new Order();
        order.setOrderId(deliverOrderId);
        //如果是同城配送的单
        //   1. 并且还未抢单,  设置状态为  已发货,待配送(同城配送)
        //   2. 已被抢单, 设置状态为 配送中
        if (Objects.equals(orderVO.getShippingTypeId(), OrderDeliveryTypeEnum.SAME_CITY.getCode())) {
        } else {
            order.setShippingStatus(ShippingStatusEnum.SENT.getCode());
        }
        order.setShippingMethod(param.getShippingMethod());
        order.setShippingTime(StringUtils.getCurrentTime());
        order.setOrderStatus(OrderStatusEnum.PROCESSING.getCode());
        order.setOrderSource(orderVO.getOrderSource());
        LogisticsProvider logisticsProvider = null;
        if (param.getShippingMethod() == 1 || param.getShippingMethod() == 4) {
            logisticsProvider = logisticsProviderRouter.getActive();
        }
        if (param.getShippingMethod() == 1) {
            order.setLogisticsProvider(logisticsProvider.providerType());
            order.setLogisticsId(param.getLogisticsId());
            Assert.isTrue((param.getLogisticsId() != null && param.getLogisticsId() > 0), () -> new GlobalException("物流公司不能为空"));
            LogisticsCompany logisticsCompany = logisticsCompanyService.getById(param.getLogisticsId());
            if (logisticsCompany != null) {
                order.setLogisticsName(logisticsCompany.getLogisticsName());
            }
            Assert.isTrue(StrUtils.isNotEmpty(param.getTrackingNo()), () -> new GlobalException("物流快递单号不能为空"));
            order.setTrackingNo(param.getTrackingNo());
        } else if (param.getShippingMethod() == 2) {
            order.setLogisticsProvider(LogisticsProviderType.UNKNOWN.getCode());
            order.setLogisticsId(0);
            order.setTrackingNo("");
            order.setLogisticsName("商家配送");
        } else if (param.getShippingMethod() == 3) {
            order.setLogisticsProvider(LogisticsProviderType.UNKNOWN.getCode());
            order.setLogisticsId(0);
            order.setTrackingNo("");
            order.setLogisticsName("无需配送");
        } else if (param.getShippingMethod() == 4) {
            order.setLogisticsProvider(logisticsProvider.providerType());
            order.setLogisticsId(param.getLogisticsId());
            LogisticsCompany logisticsCompany = logisticsCompanyService.getById(param.getLogisticsId());
            if (logisticsCompany != null) {
                order.setLogisticsName(logisticsCompany.getLogisticsName());
            }
            order.setTrackingNo("");
        }
        if (param.getShippingMethod() != 4) {
            this.updateById(order);
        }

         // 已发货节点赠送积分
         this.orderSuccessPoint(orderVO.getTotalAmount(), orderVO.getUserId(), deliverOrderId, 2);

         // 5. 发送自动确认收货消息
             TransactionAfterCommitExecutor.execute(() -> {
                 try {
                     // 计算延迟时间
                     String autoDeliveryDays = configService.getConfigVal(SettingsEnum.AUTO_DELIVERY_DAYS);
                     if (StrUtil.isNotBlank(autoDeliveryDays)) {
                         // long delayTime = Long.parseLong(autoDeliveryDays) * 24 * 60 * 60 * 1000;
                         BigDecimal delayTime = new BigDecimal(autoDeliveryDays).multiply(new BigDecimal(24 * 60 * 60 * 1000));
                         rabbitTemplate.convertAndSend(
                                 RabbitMQConfig.DELAY_EXCHANGE,
                                 RabbitMQConfig.ORDER_CONFIRM_RECEIPT_ROUTING_KEY,
                                 order.getOrderId(),
                                 message -> {
                                     message.getMessageProperties().setHeader("x-delay", delayTime.longValue());
                                     return message;
                                 }
                         );
                     }
                 } catch (Exception e) {
                     log.error("确认收货", e);
                 }
             });

        // 电子面单：必须先获取物流单号并回写，后续消息通知与微信发货信息录入才能拿到真实 trackingNo
        if (param.getShippingMethod() == 4) {
            //如果是电子面单调用电子面单服务
            OrderVO waybillOrder = this.detail(param.getId());
            waybillOrder.setLogisticsId(order.getLogisticsId());
            waybillOrder.setLogisticsName(order.getLogisticsName());
            Map<String, Object> item = logisticsProvider.createElectronicWaybill(waybillOrder, param.getBillRemark());
            Map<String, Object> orderInfo = (Map<String, Object>) item.get("Order");
            String logisticCode = (String) orderInfo.get("LogisticCode");
            order.setTrackingNo(logisticCode);
            this.updateById(order);
        }

        // 发送消息通知（通知失败不应影响发货主流程）
        if (StrUtil.isNotBlank(order.getLogisticsName()) && StrUtil.isNotBlank(order.getTrackingNo())) {
            try {
                messageService.sendUserMessage(orderVO.getUserId(), order.getOrderId(), MessageTypeEnum.ORDER_SHIPPING);
            } catch (Exception ex) {
                log.error("发货通知 ORDER_SHIPPING 发送失败，不影响主流程，orderId={}: {}", order.getOrderId(), ex.getMessage(), ex);
            }
        }

        // 微信小程序发货信息录入接口
        if ("miniProgram".equals(order.getOrderSource())) {
            try {
                Order fullOrder = this.getById(order.getOrderId());
                this.uploadShippingInfo(fullOrder, deliverItems);
            } catch (Exception e) {
                log.error("微信小程序发货信息录入接口异常", e);
            }
        }

        // 如果为预约商品，则自动确认收货，并核销
        if (Boolean.TRUE.equals(param.getIsAutoWriteOff()) || param.getIsAutoWriteOff() == null) {
            if (OrderType.isReservationOrder(getById(order.getOrderId()).getOrderType())) {
                applicationContext.getBean(OrderService.class).confirmReceipt(order.getOrderId());

            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deliverOrder(DeliverOrderParam param) {
        OrderVO orderVO = Boolean.TRUE.equals(param.getUserIdIsNull()) ? this.detail(param.getId(), null) : this.detail(param.getId());
        deliverOrder(orderVO, param);
    }

    /**
     * 店铺拆单时拆分分销订单
     * <p>
     * 当订单按店铺/供应商拆分时，需要将原分销订单迁移到对应的子订单中。
     * </p>
     *
     * @param originalOrderVO 原订单信息
     * @param newSplitOrders  新拆分的订单列表
     */
    private void splitSalesmanOrdersForStoreSplit(OrderVO originalOrderVO, List<Order> newSplitOrders) {
        try {
            if (CollUtil.isEmpty(newSplitOrders)) {
                return;
            }

            // 收集原订单项ID
            List<Integer> oldItemIds = originalOrderVO.getItems().stream()
                    .map(OrderItemVO::getItemId)
                    .collect(Collectors.toList());

            // 构建新订单ID -> 新订单项列表的映射
            Map<Integer, List<OrderItem>> newOrderItemsMap = new HashMap<>();

            for (Order newOrder : newSplitOrders) {
                List<OrderItem> newOrderItems = orderItemService.lambdaQuery()
                        .eq(OrderItem::getOrderId, newOrder.getOrderId())
                        .list();
                if (CollUtil.isNotEmpty(newOrderItems)) {
                    newOrderItemsMap.put(newOrder.getOrderId(), newOrderItems);
                }
            }

            // 调用分销订单拆分服务
            if (CollUtil.isNotEmpty(oldItemIds) && CollUtil.isNotEmpty(newOrderItemsMap)) {
                salesmanOrderService.splitSalesmanOrders(originalOrderVO, oldItemIds, newOrderItemsMap);
            }
        } catch (Exception e) {
            log.error("店铺拆单时拆分分销订单失败，原订单ID={}，错误信息={}", originalOrderVO.getOrderId(), e.getMessage(), e);
            // 不影响主流程，仅记录日志
        }
    }

    /**
     * 尾差补齐：确保拆分后所有子订单的金额总和等于父订单
     * <p>
     * 由于每个子订单的金额是按比例平摊并四舍五入的，可能存在精度误差。
     * 此方法将误差补齐到最后一个子订单，确保总和一致。
     * </p>
     *
     * @param parentOrderVO 父订单信息
     * @param splitOrders   拆分后的子订单列表
     */
    private void adjustSplitOrderAmounts(OrderVO parentOrderVO, List<Order> splitOrders) {
        if (CollUtil.isEmpty(splitOrders) || splitOrders.size() < 2) {
            // 只有一个子订单时不需要补齐
            return;
        }

        try {
            // 计算前 N-1 个子订单的各项金额总和
            BigDecimal sumTotalAmount = BigDecimal.ZERO;
            BigDecimal sumBalance = BigDecimal.ZERO;
            BigDecimal sumOnlinePaidAmount = BigDecimal.ZERO;
            BigDecimal sumOfflinePaidAmount = BigDecimal.ZERO;
            BigDecimal sumCouponAmount = BigDecimal.ZERO;
            BigDecimal sumDiscountAmount = BigDecimal.ZERO;
            BigDecimal sumPointsAmount = BigDecimal.ZERO;
            BigDecimal sumShippingFee = BigDecimal.ZERO;
            BigDecimal sumServiceFee = BigDecimal.ZERO;
            BigDecimal sumInvoiceFee = BigDecimal.ZERO;

            for (int i = 0; i < splitOrders.size() - 1; i++) {
                Order order = splitOrders.get(i);
                sumTotalAmount = sumTotalAmount.add(nullToZero(order.getTotalAmount()));
                sumBalance = sumBalance.add(nullToZero(order.getBalance()));
                sumOnlinePaidAmount = sumOnlinePaidAmount.add(nullToZero(order.getOnlinePaidAmount()));
                sumOfflinePaidAmount = sumOfflinePaidAmount.add(nullToZero(order.getOfflinePaidAmount()));
                sumCouponAmount = sumCouponAmount.add(nullToZero(order.getCouponAmount()));
                sumDiscountAmount = sumDiscountAmount.add(nullToZero(order.getDiscountAmount()));
                sumPointsAmount = sumPointsAmount.add(nullToZero(order.getPointsAmount()));
                sumShippingFee = sumShippingFee.add(nullToZero(order.getShippingFee()));
                sumServiceFee = sumServiceFee.add(nullToZero(order.getServiceFee()));
                sumInvoiceFee = sumInvoiceFee.add(nullToZero(order.getInvoiceFee()));
            }

            // 获取最后一个子订单，用差值补齐
            Order lastOrder = splitOrders.get(splitOrders.size() - 1);
            boolean needUpdate = false;

            // 计算并设置差值
            BigDecimal adjustedBalance = nullToZero(parentOrderVO.getBalance()).subtract(sumBalance);
            if (adjustedBalance.compareTo(nullToZero(lastOrder.getBalance())) != 0) {
                lastOrder.setBalance(adjustedBalance.max(BigDecimal.ZERO));
                needUpdate = true;
            }

            BigDecimal adjustedOnlinePaid = nullToZero(parentOrderVO.getOnlinePaidAmount()).subtract(sumOnlinePaidAmount);
            if (adjustedOnlinePaid.compareTo(nullToZero(lastOrder.getOnlinePaidAmount())) != 0) {
                lastOrder.setOnlinePaidAmount(adjustedOnlinePaid.max(BigDecimal.ZERO));
                needUpdate = true;
            }

            BigDecimal adjustedOfflinePaid = nullToZero(parentOrderVO.getOfflinePaidAmount()).subtract(sumOfflinePaidAmount);
            if (adjustedOfflinePaid.compareTo(nullToZero(lastOrder.getOfflinePaidAmount())) != 0) {
                lastOrder.setOfflinePaidAmount(adjustedOfflinePaid.max(BigDecimal.ZERO));
                needUpdate = true;
            }

            BigDecimal adjustedCoupon = nullToZero(parentOrderVO.getCouponAmount()).subtract(sumCouponAmount);
            if (adjustedCoupon.compareTo(nullToZero(lastOrder.getCouponAmount())) != 0) {
                lastOrder.setCouponAmount(adjustedCoupon.max(BigDecimal.ZERO));
                needUpdate = true;
            }

            BigDecimal adjustedDiscount = nullToZero(parentOrderVO.getDiscountAmount()).subtract(sumDiscountAmount);
            if (adjustedDiscount.compareTo(nullToZero(lastOrder.getDiscountAmount())) != 0) {
                lastOrder.setDiscountAmount(adjustedDiscount.max(BigDecimal.ZERO));
                needUpdate = true;
            }

            BigDecimal adjustedPoints = nullToZero(parentOrderVO.getPointsAmount()).subtract(sumPointsAmount);
            if (adjustedPoints.compareTo(nullToZero(lastOrder.getPointsAmount())) != 0) {
                lastOrder.setPointsAmount(adjustedPoints.max(BigDecimal.ZERO));
                needUpdate = true;
            }

            BigDecimal adjustedShipping = nullToZero(parentOrderVO.getShippingFee()).subtract(sumShippingFee);
            if (adjustedShipping.compareTo(nullToZero(lastOrder.getShippingFee())) != 0) {
                lastOrder.setShippingFee(adjustedShipping.max(BigDecimal.ZERO));
                needUpdate = true;
            }

            BigDecimal adjustedService = nullToZero(parentOrderVO.getServiceFee()).subtract(sumServiceFee);
            if (adjustedService.compareTo(nullToZero(lastOrder.getServiceFee())) != 0) {
                lastOrder.setServiceFee(adjustedService.max(BigDecimal.ZERO));
                needUpdate = true;
            }

            BigDecimal adjustedInvoice = nullToZero(parentOrderVO.getInvoiceFee()).subtract(sumInvoiceFee);
            if (adjustedInvoice.compareTo(nullToZero(lastOrder.getInvoiceFee())) != 0) {
                lastOrder.setInvoiceFee(adjustedInvoice.max(BigDecimal.ZERO));
                needUpdate = true;
            }

            // 更新最后一个子订单的金额并重新计算totalAmount
            if (needUpdate) {
                // 子订单税费口径：（商品总金额 + 运费）* 税率，尾差补齐后需重算税费
                refreshSplitOrderTaxAmount(lastOrder);
                this.updateOrderMoney(lastOrder, true);
                syncSplitOrderPaylogAmount(lastOrder.getOrderId());
                log.info("拆单尾差补齐完成，父订单ID={}，最后子订单ID={}", parentOrderVO.getOrderId(), lastOrder.getOrderId());
            }
        } catch (Exception e) {
            log.error("拆单尾差补齐失败，父订单ID={}，错误信息={}", parentOrderVO.getOrderId(), e.getMessage(), e);
            // 不影响主流程，仅记录日志
        }
    }

    /**
     * 空值转为ZERO
     */
    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 拆单税费补齐：确保所有子订单税费总和等于父订单税费
     */
    private void adjustSplitOrderTaxAmounts(OrderVO parentOrderVO, List<Order> splitOrders) {
        if (parentOrderVO == null || CollUtil.isEmpty(splitOrders)) {
            return;
        }

        try {
            BigDecimal parentTaxAmount = nullToZero(parentOrderVO.getTaxRateAmount()).setScale(2, RoundingMode.HALF_UP);
            List<BigDecimal> computedTaxAmounts = new ArrayList<>();
            BigDecimal computedTaxTotal = BigDecimal.ZERO;

            for (Order splitOrder : splitOrders) {
                refreshSplitOrderTaxAmount(splitOrder);
                BigDecimal taxAmount = nullToZero(splitOrder.getTaxRateAmount()).setScale(2, RoundingMode.HALF_UP);
                computedTaxAmounts.add(taxAmount);
                computedTaxTotal = computedTaxTotal.add(taxAmount);
            }

            BigDecimal diff = parentTaxAmount.subtract(computedTaxTotal).setScale(2, RoundingMode.HALF_UP);
            BigDecimal originalDiff = diff;
            if (diff.compareTo(BigDecimal.ZERO) != 0) {
                for (int i = splitOrders.size() - 1; i >= 0 && diff.compareTo(BigDecimal.ZERO) != 0; i--) {
                    BigDecimal currentTaxAmount = computedTaxAmounts.get(i);
                    BigDecimal adjustedTaxAmount = currentTaxAmount.add(diff);
                    if (adjustedTaxAmount.compareTo(BigDecimal.ZERO) < 0) {
                        diff = adjustedTaxAmount;
                        adjustedTaxAmount = BigDecimal.ZERO;
                    } else {
                        diff = BigDecimal.ZERO;
                    }
                    computedTaxAmounts.set(i, adjustedTaxAmount.setScale(2, RoundingMode.HALF_UP));
                }
            }

            for (int i = 0; i < splitOrders.size(); i++) {
                Order splitOrder = splitOrders.get(i);
                splitOrder.setTaxRateAmount(computedTaxAmounts.get(i));
                this.updateOrderMoney(splitOrder, true);
            }

            BigDecimal finalTaxTotal = computedTaxAmounts.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal finalDiff = parentTaxAmount.subtract(finalTaxTotal).setScale(2, RoundingMode.HALF_UP);
            String childTaxSummary = splitOrders.stream()
                    .map(order -> order.getOrderId() + ":" + nullToZero(order.getTaxRateAmount()).setScale(2, RoundingMode.HALF_UP))
                    .collect(Collectors.joining(","));
            log.info("拆单税费补齐完成，父订单ID={}，父单税费={}，子单税费初算合计={}，补齐差值={}，子单税费最终合计={}，最终差值={}，子单税费明细={}",
                    parentOrderVO.getOrderId(), parentTaxAmount, computedTaxTotal.setScale(2, RoundingMode.HALF_UP),
                    originalDiff, finalTaxTotal, finalDiff, childTaxSummary);
        } catch (Exception e) {
            log.error("拆单税费补齐失败，父订单ID={}，错误信息={}", parentOrderVO.getOrderId(), e.getMessage(), e);
        }
    }

    /**
     * 拆单税费按子订单口径独立计算：（商品总金额 + 运费 - 营销/会员优惠）* 税率
     */
    private void refreshSplitOrderTaxAmount(Order order) {
        if (order == null) {
            return;
        }
        BigDecimal taxRate = Optional.ofNullable(order.getTaxRate()).orElse(BigDecimal.ZERO);
        BigDecimal taxRateForCalculation = normalizeSplitTaxRateForCalculation(taxRate);
        BigDecimal taxBaseAmount = nullToZero(order.getProductAmount())
                .add(nullToZero(order.getShippingFee()))
                .subtract(nullToZero(order.getDiscountAmount()));
        if (taxBaseAmount.compareTo(BigDecimal.ZERO) < 0) {
            taxBaseAmount = BigDecimal.ZERO;
        }
        BigDecimal taxRateAmount = BigDecimal.ZERO;
        if (taxRateForCalculation.compareTo(BigDecimal.ZERO) > 0
                && taxBaseAmount.compareTo(BigDecimal.ZERO) > 0) {
            taxRateAmount = taxBaseAmount.multiply(taxRateForCalculation).setScale(2, RoundingMode.CEILING);
        }
        order.setTaxRateAmount(taxRateAmount);
    }

    /**
     * 税率统一按百分比语义处理，例如 10 表示 10%
     */
    private BigDecimal normalizeSplitTaxRateForCalculation(BigDecimal taxRate) {
        if (taxRate == null || taxRate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return taxRate.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
    }

    /**
     * 计算拆单子订单的支付日志金额
     */
    private BigDecimal resolveSplitPaylogAmount(OrderVO orderDetail, String payCode, Integer payStatus) {
        if (Objects.equals(payStatus, PayStatusEnum.PAID.getCode())) {
            if ("balance".equals(payCode)) {
                return nullToZero(orderDetail.getBalance());
            }
            if ("offline".equals(payCode)) {
                return nullToZero(orderDetail.getOfflinePaidAmount());
            }
            return nullToZero(orderDetail.getOnlinePaidAmount());
        }
        return nullToZero(orderDetail.getUnpaidAmount());
    }

    /**
     * 同步拆单后订单 paylog 的 orderAmount 与 payAmount
     */
    private void syncSplitOrderPaylogAmount(Integer orderId) {
        if (orderId == null) {
            return;
        }
        Paylog paylog = paylogService.getOne(
                Wrappers.lambdaQuery(Paylog.class)
                        .eq(Paylog::getOrderId, orderId)
                        // 商品订单：orderType=0，避免撞充值单号
                        .eq(Paylog::getOrderType, 0)
                        .orderByDesc(Paylog::getPaylogId)
                        .last("LIMIT 1")
        );
        if (paylog == null) {
            return;
        }
        OrderVO orderDetail = this.detail(orderId);
        if (orderDetail == null) {
            return;
        }
        Paylog updatePaylog = new Paylog();
        updatePaylog.setPaylogId(paylog.getPaylogId());
        updatePaylog.setOrderAmount(nullToZero(orderDetail.getTotalAmount()));
        updatePaylog.setPayAmount(resolveSplitPaylogAmount(orderDetail, paylog.getPayCode(), paylog.getPayStatus()));
        paylogService.updateById(updatePaylog);
    }

    /**
     * 发货拆单时拆分分销订单
     * <p>
     * 当部分发货导致订单拆分时，需要将原分销订单按比例拆分到发货订单和剩余订单中。
     * </p>
     *
     * @param originalOrderVO 原订单信息
     * @param deliverOrder    发货订单
     * @param remainOrder     剩余订单
     * @param deliverItems    发货订单项
     * @param remainItems     剩余订单项
     */
    private void splitSalesmanOrdersForDeliver(OrderVO originalOrderVO, Order deliverOrder, Order remainOrder,
                                               List<OrderItemVO> deliverItems, List<OrderItemVO> remainItems) {
        try {
            // 收集原订单项ID
            List<Integer> oldItemIds = originalOrderVO.getItems().stream()
                    .map(OrderItemVO::getItemId)
                    .collect(Collectors.toList());

            // 构建新订单ID -> 新订单项列表的映射
            Map<Integer, List<OrderItem>> newOrderItemsMap = new HashMap<>();

            // 查询发货订单的订单项
            List<OrderItem> deliverOrderItems = orderItemService.lambdaQuery()
                    .eq(OrderItem::getOrderId, deliverOrder.getOrderId())
                    .list();
            if (CollUtil.isNotEmpty(deliverOrderItems)) {
                newOrderItemsMap.put(deliverOrder.getOrderId(), deliverOrderItems);
            }

            // 查询剩余订单的订单项
            List<OrderItem> remainOrderItems = orderItemService.lambdaQuery()
                    .eq(OrderItem::getOrderId, remainOrder.getOrderId())
                    .list();
            if (CollUtil.isNotEmpty(remainOrderItems)) {
                newOrderItemsMap.put(remainOrder.getOrderId(), remainOrderItems);
            }

            // 调用分销订单拆分服务
            if (CollUtil.isNotEmpty(oldItemIds) && CollUtil.isNotEmpty(newOrderItemsMap)) {
                salesmanOrderService.splitSalesmanOrders(originalOrderVO, oldItemIds, newOrderItemsMap);
            }
        } catch (Exception e) {
            log.error("发货拆单时拆分分销订单失败，原订单ID={}，错误信息={}", originalOrderVO.getOrderId(), e.getMessage(), e);
            // 不影响主流程，仅记录日志
        }
    }

    private void uploadShippingInfo(Order order, List<OrderItemVO> deliverItems) {
        try{
            wechatLiveOrderSyncService.onShippingSyncStart(order, deliverItems);
        }catch (Exception e){

        }
        String accessToken = wechatOAuthService.getMiniAccessToken();

        // 查询订单支付记录：重复回调等场景可能留下多条已支付记录，无 LIMIT 会抛 TooManyResultsException
        Paylog paylog = paylogMapper.selectOne(
                Wrappers.lambdaQuery(Paylog.class).eq(Paylog::getOrderId, order.getOrderId()).eq(Paylog::getOrderType, 0).eq(Paylog::getPayStatus, PayStatusEnum.PAID.getCode())
                        .orderByDesc(Paylog::getPaylogId).last("LIMIT 1")
        );

        String wechatPayMchId = configService.getConfigVal(SettingsEnum.WECHAT_PAY_MCHID);
        UploadShippingInfoParam.OrderKey orderKey = UploadShippingInfoParam.OrderKey.builder()
                .orderNumberType(1)
                .mchid(wechatPayMchId)
                .outTradeNo(paylog.getPaySn())
                .build();


        // 根据订单类型设置物流类型：1-实体物流配送 2-同城配送 3-虚拟商品 4-用户自提
        int logisticsType;
        if (order.getLogisticsId() != null && order.getLogisticsId() != 0) {
            logisticsType = 1; // 实体物流配送
        } else {
            logisticsType = 3; // 虚拟商品/无需配送
        }

        // 无拆单
        boolean isAllDelivered = order.getParentOrderId() == null;
        // 拆单情况
        if (!isAllDelivered) {
            Long pendingCount = this.lambdaQuery()
                    .eq(Order::getParentOrderId, order.getParentOrderId())
                    .eq(Order::getShippingStatus, ShippingStatusEnum.PENDING.getCode())
                    .ne(Order::getOrderId, order.getOrderId())
                    .count();
            isAllDelivered = pendingCount == 0;
        }

        // 构建物流信息
        UploadShippingInfoParam.Shipping shipping;
            // 普通订单：需要物流单号和物流公司
            LogisticsCompany logisticsCompany = logisticsCompanyService.lambdaQuery()
                    .eq(LogisticsCompany::getLogisticsId, order.getLogisticsId()).one();
            String expressCompany = logisticsCompany != null ? logisticsCompany.getLogisticsCode() : "";

            shipping = UploadShippingInfoParam.Shipping.builder()
                    .trackingNo(order.getTrackingNo())
                    .expressCompany(expressCompany)
                    .itemDesc(deliverItems.getFirst() != null ? deliverItems.getFirst().getProductName() : "")
                    .build();
        shipping.assembleContact(null, order.getMobile());
        List<UploadShippingInfoParam.Shipping> shippingList = Collections.singletonList(shipping);

        ZonedDateTime zdt = Instant.ofEpochSecond(order.getShippingTime()).atZone(ZoneId.of("Asia/Shanghai"));
        String uploadTime = zdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        // 获取用户 openid
        UserAuthorize userAuthorize = userAuthorizeMapper.selectOne(
                Wrappers.lambdaQuery(UserAuthorize.class)
                .eq(UserAuthorize::getUserId, order.getUserId())
                .eq(UserAuthorize::getAuthorizeType, AuthorizeTypeEnum.MINI_PROGRAM.getCode())  // 指定小程序类型
                .isNotNull(UserAuthorize::getOpenId)
                .ne(UserAuthorize::getOpenId, "")  // 排除空字符串
                .last("limit 1")
        );
        if (Objects.isNull(userAuthorize)) {
            return;
        }
        String openid = userAuthorize.getOpenId();

        UploadShippingInfoParam uploadShippingInfoParam = UploadShippingInfoParam.builder()
                .orderKey(orderKey)
                .logisticsType(logisticsType)
                .deliveryMode(1)
                .isAllDelivered(isAllDelivered)
                .shippingList(shippingList)
                .uploadTime(uploadTime)
                .payer(new UploadShippingInfoParam.Payer(openid))
                .build();

        UploadShippingInfoResult result = wechatApiClient.uploadShippingInfo(accessToken, uploadShippingInfoParam);

        String requestData = JSONUtil.toJsonStr(uploadShippingInfoParam);
        try {
            if (result == null || ObjectUtil.notEqual(result.getErrcode(), 0)) {
                throw new GlobalException("发货信息录入失败：" + (result != null ? result.getErrmsg() : ""));
            }
            try{
                wechatLiveOrderSyncService.onShippingSyncSuccess(order, deliverItems, requestData, JSONUtil.toJsonStr(result));
            }catch (Exception e){

            }
        } catch (Exception e) {
            try{
                wechatLiveOrderSyncService.onShippingSyncFailed(order, e.getMessage(), requestData);
            }catch (Exception e1){

            }
            throw e;
        }
    }

    @Override
    public OrderVO getOrderPrintInfo(Integer id) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        // 构建URL
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        // 如果端口不是默认的80或443，则添加端口号
        if (!(serverPort == 80 && "http".equals(scheme)) && !(serverPort == 443 && "https".equals(scheme))) {
            url.append(":").append(serverPort);
        }
        Date date = DateUtil.date(System.currentTimeMillis());
        OrderVO orderVO = this.detail(id);
        orderVO.setHost(url.toString());
        orderVO.setPrintTime(String.valueOf(date));
        return orderVO;
    }

    @Override
    public LogisticsApiLog getOrderPrintWaybillInfo(Integer id) {
        return logisticsApiLogService.getOrderLogisticsApiLog(id);
    }

    @Override
    public List<OrderVO> getSeveralDetail(List<Integer> ids, Integer suppliersId) {
        if (CollUtil.isEmpty(ids)) {
            throw new GlobalException("请选择有效订单");
        }

        List<OrderVO> orders = new ArrayList<>();
        for (Integer id : ids) {
            orders.add(detail(id));
        }

        if (orders.isEmpty()) {
            throw new GlobalException("请选择有效订单");
        }

        // 如果传入了 suppliersId，过滤订单项
//        if (suppliersId != null && suppliersId > 0) {
//            for (OrderVO order : orders) {
//                List<OrderItem> filteredItems = order.getItems().stream()
//                        .filter(item -> Objects.equals(item.gets(), suppliersId))
//                        .collect(Collectors.toList());
//                order.setItems(filteredItems); // 重设过滤后的 items
//            }
//        }

        return orders;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean modifyOrderProduct(OrderModifyProductDTO modifyProductDTO) {
        OrderVO orderVO = this.detail(modifyProductDTO.getId());
        if (!orderVO.getAvailableActions().getModifyOrderProduct()) {
            throw new GlobalException("当前订单状态不可执行此操作");
        }
        BigDecimal totalPrice = modifyProductDTO.getItems().stream()
                .map(OrderItemDTO::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPrice.subtract(orderVO.getDiscountAmount())
                .subtract(orderVO.getCouponAmount())
                .compareTo(BigDecimal.ZERO) <= 0) {
            // 小于或等于 0 的逻辑
            throw new GlobalException("商品价格总和减去优惠价格（优惠券抵扣，优惠折扣）必须大于0");
        }

        orderItemService.modifyOrderItem(orderVO, modifyProductDTO);
        Order order = this.getById(modifyProductDTO.getId());

        // 计算商品修改后的总价
        BigDecimal totalAmount = modifyProductDTO.getItems().stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        order.setProductAmount(totalAmount);
        orderLogService.quickCreate("订单商品信息修改", modifyProductDTO.getId(), orderVO.getOrderSn());
        this.updateOrderMoney(order, true);
        return false;
    }

    @Override
    public BigDecimal getOrderUnSettlementAmount(Integer shopId) {
        LambdaQueryWrapper<StatementOutput> eq = new LambdaQueryWrapper<StatementOutput>()
                .in(StatementOutput::getSettlementStatus, StatementType.SERVICE_FEE.getCode(), StatementType.ORDER.getCode(), StatementType.ORDER_REFUND.getCode(), StatementType.DISTRIBUTION_COMMISSION.getCode())
                .eq(StatementOutput::getRealSettlement, NO);
        if (AdminTypeEnum.fromCode(HeaderUtils.getAdminType()) == AdminTypeEnum.ADMIN) {
            if (shopId > 0) {
                eq.eq(StatementOutput::getShopId, shopId);
            } else {
                eq.gt(StatementOutput::getShopId, 0);
            }
        } else {
            eq.eq(StatementOutput::getShopId, shopId);
        }
        List<StatementOutput> statementOutputs = statementOutputMapper.selectList(eq);
        if (ObjectUtil.isEmpty(statementOutputs)) {
            return BigDecimal.ZERO;
        }
        BigDecimal in = statementOutputs.stream().map(StatementOutput::getIncome).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        BigDecimal out = statementOutputs.stream().map(StatementOutput::getExpenditure).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        return in.subtract(out);
    }

    @Override
    public BigDecimal getOrderUnSettlementAmountByVendor(Integer vendorId) {
        LambdaQueryWrapper<StatementOutput> eq = new LambdaQueryWrapper<StatementOutput>()
                .in(StatementOutput::getSettlementStatus, StatementType.SERVICE_FEE.getCode(), StatementType.ORDER.getCode(), StatementType.ORDER_REFUND.getCode(), StatementType.DISTRIBUTION_COMMISSION.getCode())
                .eq(StatementOutput::getRealSettlement, NO);
        if (AdminTypeEnum.fromCode(HeaderUtils.getAdminType()) == AdminTypeEnum.ADMIN) {
            if (vendorId > 0) {
                eq.eq(StatementOutput::getVendorId, vendorId);
            } else {
                eq.gt(StatementOutput::getVendorId, 0);
            }
        } else {
            eq.eq(StatementOutput::getVendorId, vendorId);
        }
        List<StatementOutput> statementOutputs = statementOutputMapper.selectList(eq);
        if (ObjectUtil.isEmpty(statementOutputs)) {
            return BigDecimal.ZERO;
        }
        BigDecimal in = statementOutputs.stream().map(StatementOutput::getIncome).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        BigDecimal out = statementOutputs.stream().map(StatementOutput::getExpenditure).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        return in.subtract(out);
    }

    @Override
    public BigDecimal getTodayOrderSettlementAmountByVendor(Integer vendorId, Long todayStart, Long todayEnd) {
        List<StatementOutput> statementOutputs = statementOutputMapper.selectList(new LambdaQueryWrapper<StatementOutput>()
                .eq(StatementOutput::getVendorId, vendorId)
                .in(StatementOutput::getSettlementStatus, StatementType.SERVICE_FEE.getCode(), StatementType.ORDER.getCode(), StatementType.ORDER_REFUND.getCode(), StatementType.DISTRIBUTION_COMMISSION.getCode())
                .eq(StatementOutput::getRealSettlement, YES)
                .between(StatementOutput::getRealSettlementTime, todayStart, todayEnd));
        if (ObjectUtil.isEmpty(statementOutputs)) {
            return BigDecimal.ZERO;
        }
        BigDecimal in = statementOutputs.stream().map(StatementOutput::getIncome).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        BigDecimal out = statementOutputs.stream().map(StatementOutput::getExpenditure).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        return in.subtract(out);
    }

    @Override
    public Map<String, String> getExportItemList() {
        boolean isO2o = tigshopProperties.getIsO2o() == 1;
        String shopNameVal = "店铺名称";
        if (isO2o) {
            shopNameVal = "门店名称";
        }
        return Map.ofEntries(
                Map.entry("orderSn", "订单编号"),
                Map.entry("addTime", "下单时间"),
                Map.entry("orderStatusName", "订单状态"),
                Map.entry("payStatusName", "支付状态"),
                Map.entry("shopName", shopNameVal),
                Map.entry("username", "会员名称"),
                Map.entry("consignee", "收件人姓名"),
                Map.entry("mobile", "收件人电话"),
                Map.entry("address", "收件人地址"),
                Map.entry("postcode", "收件人邮编"),
                Map.entry("totalAmount", "订单总价"),
                Map.entry("balance", "使用余额"),
                Map.entry("discountAmount", "折扣"),
                Map.entry("pointsAmount", "积分抵金额"),
                Map.entry("couponAmount", "优惠券金额"),
                Map.entry("payTime", "支付时间"),
                Map.entry("payTypeId", "支付类型"),
                Map.entry("payCode", "支付方式"),
                Map.entry("outTradeNo", "交易单号"),
                Map.entry("paidAmount", "支付金额"),
                Map.entry("shippingTime", "发货时间"),
                Map.entry("shippingFee", "运费"),
                Map.entry("logisticsName", "物流名称"),
                Map.entry("trackingNo", "发货单号"),
                Map.entry("buyerNote", "订单备注"),
                Map.entry("adminNote", "商家备注"),
                Map.entry("productInfo", "商品信息"),
                Map.entry("shippingStatusName", "发货状态"),
                Map.entry("productWeight", "总重量(KG)"),
                Map.entry("usePoints", "使用积分")
        );
    }

    @Override
    public void saveExportItem(ExportItemSaveParam param) {
        adminUserMapper.update(
                Wrappers.lambdaUpdate(AdminUser.class)
                        .eq(AdminUser::getAdminId, SecurityUtils.getCurrentAdminId())
                        .set(AdminUser::getOrderExport, JSONUtil.toJsonStr(param.getExportItem().split(",")))
        );
    }

    @Override
    public Map<String, String> exportItemInfo() {
        AdminUser adminUser = adminUserMapper.selectById(SecurityUtils.getCurrentAdminId());
        if (adminUser.getOrderExport() != null) {
            List<String> orderExport = JSONUtil.toList(adminUser.getOrderExport(), String.class);
            return this.getExportItemList().entrySet().stream().filter(entry -> orderExport.contains(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return Map.of();
    }

    @Override
    public void orderExport(OrderListPageQuery pageQuery, HttpServletResponse response) {
        // 分批流式加载：避免一次性 list(pageQuery, 99999) 全量载入导致 OOM/超时/打满连接池。
        // 按固定批大小逐页读取并富化，累加到导出列表，读到不足一页即止。
        final int exportBatchSize = 2000;
        List<OrderExportVO> orderExportList = new ArrayList<>();
        int exportPageNo = 1;
        while (true) {
            pageQuery.setPage(exportPageNo);
            List<OrderVO> list = this.list(pageQuery, exportBatchSize).getRecords();
            if (CollUtil.isEmpty(list)) {
                break;
            }
            for (OrderVO order : list) {
                OrderExportVO orderExportVO = new OrderExportVO();
                BeanUtils.copyProperties(order, orderExportVO);
                orderExportVO.setAddress(order.getUserAddress());
                // 邮编只存在于地址快照中，OrderVO 顶层无该字段，copyProperties 取不到
                if (order.getAddressData() != null) {
                    orderExportVO.setPostcode(order.getAddressData().getPostcode());
                }

                //支付类型
                orderExportVO.setPayTypeName(translatePackage.translate(
                        order.getPayTypeId() == 1 ? "在线支付" : order.getPayTypeId() == 2 ? "货到付款" : "线下支付",
                        DataType.API.getCode()));

                // 支付方式
                if (order.getPayLog() != null && StrUtil.isNotBlank(order.getPayLog().getPayCode())) {
                    orderExportVO.setPayCode(translatePackage.translate(PayMethodType.getTypeName(order.getPayLog().getPayCode()), DataType.API.getCode()));
                } else {
                    //没有的话,可能是余额支付
                    orderExportVO.setPayCode(translatePackage.translate(PayMethodType.BALANCE.getDescription(), DataType.API.getCode()));
                }

                //orderList.item下面的重量相加
                if (order.getItems() != null) {
                    orderExportVO.setProductWeight(
                            order.getItems().stream()
                                    .map(item -> Optional.ofNullable(item.getProductWeight()).orElse(BigDecimal.ZERO))
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    );
                    //取list.item下面的商品循环商品名称，商品属性，商品编号，购买数量，购买价格，小计用竖线隔开，商品之间用换行符隔开
                    for (OrderItemVO item : order.getItems()) {
                        OrderExportVO newOrderExportVO = BeanUtil.copyProperties(orderExportVO, OrderExportVO.class);

                        newOrderExportVO.setProductName(translatePackage.translate(item.getProductName(), DataType.PRODUCT.getCode()));
                        if (CollUtil.isNotEmpty(item.getSkuData())) {
                            String skuData = item.getSkuData().stream()
                                    .map(skudata -> StrUtil.format("{}:{}",
                                            translatePackage.translate(skudata.getName(), DataType.SKU.getCode()),
                                            translatePackage.translate(skudata.getValue(), DataType.SKU.getCode())))
                                    .collect(Collectors.joining("|"));
                            newOrderExportVO.setSkuData(skuData);
                        }
                        newOrderExportVO.setProductSn(item.getProductSn());
                        newOrderExportVO.setQuantity(item.getQuantity());
                        newOrderExportVO.setPrice(item.getPrice());
                        newOrderExportVO.setTotalPrice(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                        newOrderExportVO.setUsername(order.getUser().getUsername());
                        orderExportList.add(newOrderExportVO);
                    }

                }
            }
            if (list.size() < exportBatchSize) {
                break;
            }
            exportPageNo++;
        }

        ExcelUtils<OrderExportVO> excelUtils = new ExcelUtils<>(orderExportList, OrderExportVO.class);
        Set<String> exportItemList = Arrays.stream(pageQuery.getExportItem().split(",")).collect(Collectors.toSet());
        //如果选择了商品信息，则添加商品对应字段信息
        if (exportItemList.contains("productInfo")) {
            exportItemList.remove("productInfo");
            exportItemList.add("productName");
            exportItemList.add("skuData");
            exportItemList.add("productSn");
            exportItemList.add("quantity");
            exportItemList.add("price");
            exportItemList.add("totalPrice");
        }
        if(exportItemList.contains("payTypeId")){
            exportItemList.add("payTypeName");
        }
        String fileName = URLEncoder.encode("订单列表信息" + StringUtils.getCurrentTime() + XLSX, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        excelUtils.exportExcelWebByTitle(response, exportItemList, fileName, "订单列表信息");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addLog(Integer orderId, String desc) {

        if (desc != null) {
            orderLogService.quickCreate(desc, orderId, "");
        }
    }

    @Override
    public Page<AfterSalesClientVO> afterSalesList(AftersalesListPageQuery listDTO) {
        // 分页
        Page<Order> page = buildSortOrder(listDTO);

        //不需要查出来的orderId
        HashSet<Integer> filterIds = new HashSet<>();
        //礼品卡兑换订单
        List<OrderItem> giftCardOrder = orderItemService.list(Wrappers.lambdaQuery(OrderItem.class).eq(OrderItem::getUserId, listDTO.getUserId()).ne(OrderItem::getGiftCardId, 0));
        if (CollUtil.isNotEmpty(giftCardOrder)) {
            filterIds.addAll(giftCardOrder.stream().map(OrderItem::getOrderId).toList());
        }
        //礼品卡订单

        Page<Order> orderPage = this.lambdaQuery()
                .notIn(CollUtil.isNotEmpty(filterIds), Order::getOrderId, filterIds)
                .eq(Order::getIsDel, 0)
                .eq(Order::getUserId, listDTO.getUserId())
                .notIn(Order::getOrderType, OrderType.VIRTUAL.getCode(), OrderType.PAID.getCode(), OrderType.CARD.getCode(), OrderType.GIFT_CARD.getCode())
                .in(Order::getOrderStatus, OrderStatusEnum.CONFIRMED.getCode(), OrderStatusEnum.PROCESSING.getCode(), OrderStatusEnum.COMPLETED.getCode())
                .page(page);

        // 获取查询结果
        List<Order> orderPageRecords = orderPage.getRecords();
        if (CollUtil.isEmpty(orderPageRecords)) {
            return new Page<>();
        }

        // 取出订单ID
        List<Integer> orderIds = orderPageRecords.stream().map(Order::getOrderId).toList();
        List<OrderItemVO> itemListNew = getOrderItemList(orderIds);

        // 获取产品数据
        List<Integer> productIds = itemListNew.stream().map(OrderItemVO::getProductId).toList();
        Map<Integer, Product> productMap = Map.of();
        if (CollUtil.isNotEmpty(productIds)) {
            List<Product> productList = productService.listByIds(productIds);
            productMap = productList.stream().collect(Collectors.toMap(Product::getProductId, Function.identity()));
        }

        // 查询店铺结算配置
        List<Integer> shopIds = itemListNew.stream().map(OrderItemVO::getShopId).distinct().toList();
        if (CollUtil.isEmpty(shopIds)) {
            return new Page<>();
        }
        List<OrderConfig> orderConfigs = orderConfigMapper.selectList(
                new LambdaQueryWrapper<OrderConfig>()
                        .in(OrderConfig::getShopId, shopIds)
        );
        Map<Integer, OrderConfig> orderConfigMap = orderConfigs.stream().collect(Collectors.toMap(OrderConfig::getShopId, Function.identity()));

        // 插入售后数据
        Map<Integer, List<OrderItemVO>> itemMap = itemListNew.stream().collect(Collectors.groupingBy(OrderItemVO::getOrderId));
        Map<Integer, Product> finalProductMap = productMap;
        String afterSalesLimitDays = configService.getConfigVal(SettingsEnum.AFTER_SALES_LIMIT_DAYS);

        // 批量预加载次卡数据，避免 N+1 查询
        Map<Integer, Order> parentOrderMap = new HashMap<>();
        Set<Integer> redeemSubOrderIds = new HashSet<>();
        final Set<Integer> finalRedeemSubOrderIds = redeemSubOrderIds;

        List<AfterSalesClientVO> orderVOList = orderPageRecords.stream()
                .map(order -> {
                    AfterSalesClientVO afterSalesClientVO = new AfterSalesClientVO();
                    BeanUtils.copyProperties(order, afterSalesClientVO);
                    afterSalesClientVO.setShippingTime(TigUtils.handelTime(order.getShippingTime()));

                    List<OrderItemVO> orderItems = itemMap.get(order.getOrderId());
                    //循环orderItems ，将每个值复制给AfterSalesClientVO.ITEM
                    List<AfterSalesClientVO.Item> items = new ArrayList<>();
                    orderItems.forEach(orderItem -> {
                        AfterSalesClientVO.Item item = new AfterSalesClientVO.Item();
                        BeanUtils.copyProperties(orderItem, item);

                        Product product = finalProductMap.get(orderItem.getProductId());
                        if (product != null) {
                            item.setProductPicThumb(product.getPicThumb());
                            item.setProductWeight(product.getProductWeight());
                            item.setProductStock(product.getProductStock());
                            item.setVirtualSample(product.getVirtualSample());
                            item.setPaidContent(JsonUtil.checkJsonType(product.getPaidContent()));
                            item.setCardGroupId(product.getCardGroupId());
                        }

                        item = getAfterSalesItem(order.getOrderId(), orderItem, item);

                        // 判断是否可以申请售后；1. 判断店铺结算配置（确认收货后立即不可售后）（自营店不用判断）2. 全局设置（买家申请售后限制）
                        /*Long count = aftersalesMapper.selectCount(new LambdaQueryWrapper<Aftersales>()
                                .eq(Aftersales::getOrderId, item.getOrderId())
                                .in(Aftersales::getStatus, AftersalesStatusEnum.validStatus())
                        );*/
                        // 一个订单多个商品，或者多个数量时，这里先默认为可申请
                        item.setToAftersalses(true);
                        // 1. 判断店铺结算配置（确认收货后立即不可售后）（自营店不用判断）
                        if (orderItem.getShopId() != null && orderItem.getShopId() > 0) {
                            OrderConfig orderConfig = orderConfigMap.get(orderItem.getShopId());
                            if (orderConfig != null) {
                                OrderConfigDTO orderConfigData = JsonUtil.fromJson(orderConfig.getData(), OrderConfigDTO.class);
                                if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus() && 0 == orderConfigData.getDateType()) {
                                    item.setToAftersalses(false);
                                }
                            }
                        }
                        // 2. 全局设置（买家申请售后限制）
                        if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus()) {
                            if (StrUtil.isNotBlank(afterSalesLimitDays)) {
                                long receivedTime = order.getReceivedTime() * 1000;
                                BigDecimal afterSalesLimitDaysTime = new BigDecimal(afterSalesLimitDays).multiply(new BigDecimal(24 * 60 * 60 * 1000));
                                LocalDateTime limitDateTime = Instant.ofEpochMilli(receivedTime + afterSalesLimitDaysTime.longValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                                boolean toAftersalses = limitDateTime.isAfter(LocalDateTime.now());
                                item.setToAftersalses(toAftersalses);
                            }
                        }


                        items.add(item);
                    });
                    afterSalesClientVO.setItems(items);
                    return afterSalesClientVO;
                }).toList();
        return PageUtil.convertPage(orderPage, orderVOList);
    }

    @Override
    public List<AfterSalesDetailVO> applyData(Integer itemId, Integer orderId) {
        // 判断是否支持售后
        Order order = this.getById(orderId);

        // 获取是否有店铺订单设置
        OrderConfig orderConfig = orderConfigMapper.selectOne(
                Wrappers.lambdaQuery(OrderConfig.class)
                        .eq(OrderConfig::getShopId, order.getShopId())
                        .eq(OrderConfig::getCode, "order_config")
        );

        // 确认收货订单多少天之后不可在申请售后
        if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus() && orderConfig != null) {
            OrderConfigDataDTO orderConfigDataDTO = JsonUtil.fromJson(orderConfig.getData(), OrderConfigDataDTO.class);
            if (orderConfigDataDTO.getDataType() == 1) {
                // 当前时间 - order.getReceivedTime()
                long time = StringUtils.getCurrentTime() / 1000 - order.getReceivedTime();
                if (time > orderConfigDataDTO.getUseDay() * 24 * 60 * 60) {
                    throw new GlobalException(messageHelper.get("error.order.aftersales.time.expired"));
                }
            }
        }

        List<OrderItem> orderItems = orderItemMapper.selectList(
                Wrappers.lambdaQuery(OrderItem.class)
                        .eq(OrderItem::getOrderId, orderId)
                        .eq(itemId != null, OrderItem::getItemId, itemId)
        );
        Assert.notEmpty(orderItems, () -> new GlobalException(messageHelper.get("error.order.item.order.not.exist")));


        //预售订单存在多个paylogs
        List<Paylog> paylogs = paylogMapper.selectList(Wrappers.lambdaQuery(Paylog.class).eq(Paylog::getOrderId, orderId).eq(Paylog::getOrderType, 0).eq(Paylog::getPayStatus, 1));
        List<String> payCodes = List.of("yunpay_wechat", "yunpay_alipay", "yunpay_yunshanfu");
        for (Paylog paylog : paylogs) {
            Assert.isFalse(paylog != null && payCodes.contains(paylog.getPayCode()) && itemId != null, () -> new GlobalException(messageHelper.get("error.order.item.full.refund.only")));
        }

        List<Integer> status = getAfterSalseStatusList();
        BigDecimal orderTaxRate = order.getTaxRate() != null ? order.getTaxRate() : BigDecimal.ZERO;
        return orderItems.stream()
                .map(orderItem -> {
                    AfterSalesDetailVO afterSalesDetailVO = new AfterSalesDetailVO();
                    BeanUtils.copyProperties(orderItem, afterSalesDetailVO);
                    BigDecimal itemPrice = orderItem.getPrice() != null ? orderItem.getPrice() : BigDecimal.ZERO;
                    Integer quantity = orderItem.getQuantity() != null ? orderItem.getQuantity() : 0;
                    BigDecimal unitTaxAmount = orderItem.getPrice() != null && orderItem.getPrice().compareTo(BigDecimal.ZERO) > 0 && orderTaxRate.compareTo(BigDecimal.ZERO) > 0
                            ? orderItem.getPrice().multiply(orderTaxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.CEILING)
                            : BigDecimal.ZERO;
                    afterSalesDetailVO.setTaxRateAmount(unitTaxAmount);
                    // price 回显为含税单价，便于前端部分退款直接按单价计算
                    BigDecimal priceWithTax = itemPrice.add(unitTaxAmount);
                    afterSalesDetailVO.setPrice(priceWithTax);
                    afterSalesDetailVO.setSubtotal(priceWithTax.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.CEILING));
                    afterSalesDetailVO.setSkuData(JsonUtil.jsonToList(orderItem.getSkuData(), ProductSkuDTO.SkuData.class));
                    List<Aftersales> aftersalesList = aftersalesMapper.selectList(
                            Wrappers.lambdaQuery(Aftersales.class)
                                    .in(Aftersales::getStatus, status)
                                    .eq(Aftersales::getOrderId, orderId)
                    );
                    Integer canApplyQuantity = orderItem.getQuantity();
                    if (CollUtil.isNotEmpty(aftersalesList)) {
                        canApplyQuantity = handleSumValidNum(aftersalesList, orderItem.getItemId(), orderItem.getQuantity());
                    }
                    afterSalesDetailVO.setCanApplyQuantity(canApplyQuantity);
                    return afterSalesDetailVO;
                }).toList();
    }

    public AfterSalesClientVO.Item getAfterSalesItem(Integer orderId, OrderItemVO orderItem, AfterSalesClientVO.Item item) {
        List<Integer> status = getAfterSalseStatusList();
        LambdaQueryWrapper<Aftersales> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Aftersales::getStatus, status);
        queryWrapper.eq(Aftersales::getOrderId, orderId);
        List<Aftersales> aftersalesList = aftersalesMapper.selectList(queryWrapper);
        if (!aftersalesList.isEmpty()) {
            int canApplyQuantity = handleSumValidNum(aftersalesList, orderItem.getItemId(), orderItem.getQuantity());
            if (item.getAftersalesItem() != null) {
                LambdaQueryWrapper<Aftersales> queryWrapperAfter = new LambdaQueryWrapper<>();
                queryWrapperAfter.eq(Aftersales::getAftersaleId, item.getAftersalesItem().getAftersaleId());
                Aftersales aftersales = aftersalesMapper.selectOne(queryWrapperAfter);
                if (aftersales != null) {
                    if (aftersales.getStatus() == AftersalesStatusEnum.COMPLETE.getCode()) {
                        if (canApplyQuantity == 0) {
                            // 不可申请--跳转详情
                            item.setAftersaleFlag(0);
                        } else {
                            // 可申请
                            item.setAftersaleFlag(1);
                        }
                    } else if (aftersales.getStatus() == AftersalesStatusEnum.CANCEL.getCode()
                            || aftersales.getStatus() == AftersalesStatusEnum.REFUSE.getCode()) {
                        // 售后取消 / 审核未通过（终态，额度已释放）-- 可申请
                        item.setAftersaleFlag(1);
                    } else {
                        item.setAftersaleFlag(0);
                    }
                } else {
                    item.setAftersaleFlag(1);
                }
            } else {
                item.setAftersaleFlag(1);
            }
        } else {
            item.setAftersaleFlag(1);
        }
        return item;
    }

    private Integer handleSumValidNum(List<Aftersales> aftersalesList, Integer itemId, Integer quantity) {
        List<Integer> aftersalesIds = aftersalesList.stream().map(Aftersales::getAftersaleId).toList();

        List<AftersalesItem> aftersalesItems = aftersalesItemService.lambdaQuery()
                .in(AftersalesItem::getAftersaleId, aftersalesIds)
                .eq(AftersalesItem::getOrderItemId, itemId)
                .list();

        if (CollUtil.isNotEmpty(aftersalesItems)) {
            Integer afterSalesNum = aftersalesItems.stream().map(AftersalesItem::getNumber).reduce(0, Integer::sum);
            return quantity - afterSalesNum;
        }

        return quantity;
    }

    /**
     * 获取售后状态。
     * 审核未通过（REFUSE）为终态，不占用可申请售后的数量，故不计入。
     *
     * @return List
     */
    private List<Integer> getAfterSalseStatusList() {
        List<Integer> status = new ArrayList<>();
        status.add(AftersalesStatusEnum.IN_REVIEW.getCode());
        status.add(AftersalesStatusEnum.APPROVED_FOR_PROCESSING.getCode());
        status.add(AftersalesStatusEnum.SEND_BACK.getCode());
        status.add(AftersalesStatusEnum.RETURNED.getCode());
        status.add(AftersalesStatusEnum.COMPLETE.getCode());
        Integer isVendor = tigshopProperties.getIsVendor();
        if (isVendor == 1) {
            status.add(AftersalesStatusEnum.WAIT_FOR_SUPPLIER_AUDIT.getCode());
            status.add(AftersalesStatusEnum.WAIT_FOR_SUPPLIER_RECEIPT.getCode());
        }
        return status;
    }

    private BigDecimal allocatedAmount(BigDecimal originalAmount, BigDecimal newAmount, BigDecimal amount) {
        // 实现金额平摊逻辑
        BigDecimal ratio = amount.divide(originalAmount, 10, RoundingMode.HALF_UP);
        return newAmount.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 订单进度条
     */
    @Override
    public OrderStepVO stepStatus(OrderVO orderVO) {
        OrderStepVO orderStepVO = new OrderStepVO();
        orderStepVO.setCurrent(0);
        orderStepVO.setStatus("process");
        List<OrderStepVO.OrderStepDetailVO> steps = new ArrayList<>();

        if (orderVO == null) {
            orderStepVO.setSteps(steps);
            return orderStepVO;
        }

        // 取消/无效：优先返回（避免被周期购“直接完成”逻辑覆盖）
        if (orderVO.getOrderStatus() != null) {
            OrderStatusEnum statusEnum = OrderStatusEnum.getStatus(orderVO.getOrderStatus());
            if (statusEnum == OrderStatusEnum.CANCELLED || statusEnum == OrderStatusEnum.INVALID) {
                // 第一步：提交订单
                OrderStepVO.OrderStepDetailVO step1 = new OrderStepVO.OrderStepDetailVO();
                step1.setTitle("提交订单");
                step1.setDescription(orderVO.getAddTime());
                steps.add(step1);

                // 第二步：已取消/无效
                OrderStepVO.OrderStepDetailVO step2 = new OrderStepVO.OrderStepDetailVO();
                step2.setTitle(OrderStatusEnum.getStatusName(orderVO.getOrderStatus()));
                step2.setDescription("");
                steps.add(step2);

                orderStepVO.setCurrent(1);
                orderStepVO.setStatus("error");
                orderStepVO.setSteps(steps);
                return orderStepVO;
            }
        }


        // 第一步：提交订单
        OrderStepVO.OrderStepDetailVO submitOrderStep = new OrderStepVO.OrderStepDetailVO();
        submitOrderStep.setTitle("提交订单");
        submitOrderStep.setDescription(orderVO.getAddTime());
        steps.add(submitOrderStep);

        // 第二步：支付状态
        OrderStepVO.OrderStepDetailVO paymentStep = new OrderStepVO.OrderStepDetailVO();
        paymentStep.setTitle(PaymentStatus.getStatusName(orderVO.getPayStatus()));
        paymentStep.setDescription("");
        steps.add(paymentStep);

        // 支付状态判断
        switch (PaymentStatus.getStatus(orderVO.getPayStatus())) {
            case UNPAID:
                orderStepVO.setCurrent(1);
                break;
            case PAID:
                orderStepVO.setCurrent(2);
                paymentStep.setDescription(orderVO.getPayTime());
                break;
            default:
                break;
        }

        // 第三步：物流状态
        OrderStepVO.OrderStepDetailVO shippingStep = new OrderStepVO.OrderStepDetailVO();
        shippingStep.setTitle(ShippingStatusEnum.getStatusName(orderVO.getShippingStatus()));
        // 同城配送在"待取货"阶段主订单 shipping_status 被并入 IN_TRANSIT(配送中)，但派送单此刻仍是"待取货"，
        // 直接按 shipping_status 渲染进度条会误显"配送中"，与订单状态名/配送员端不一致。
        // 仅对该歧义阶段(同城配送 + shipping_status=IN_TRANSIT + 派送单存在)改用派送单真实状态文案，保持三处一致。
        if (Boolean.TRUE.equals(orderVO.getIsPickup())) {
            if (OrderType.isReservationOrder(orderVO.getOrderType())) {
                shippingStep.setTitle(ReservationStatusEnum.getStatusName(orderVO.getShippingStatus() + 1));
            } else {
                shippingStep.setTitle(ShipmentStatusEnum.getStatusName(orderVO.getShippingStatus() + 1));
            }
        }
        if (JoinStatusEnum.WAIT_GROUP.getCode().equals(orderVO.getJoinStatus())) {
            shippingStep.setTitle("已付款，待成团");
        }
        if (JoinStatusEnum.FAIL.getCode().equals(orderVO.getJoinStatus())) {
            shippingStep.setTitle("拼团失败, 待退款");
        }
        if (JoinStatusEnum.FAIL.getCode().equals(orderVO.getJoinStatus())) {
            shippingStep.setTitle("拼团失败, 待退款");
        }
        // 礼品卡商品 与 抽奖兑换流程 中的电子卡订单：物流步骤展示为「已完成」
        if (OrderType.GIFT_CARD.getCode() == orderVO.getOrderType()
                || Objects.equals(orderVO.getOrderType(), CartTypeEnum.TYPE_LOTTERY_PRODUCT.getCode())) {
            //如果判定实体礼品卡和虚拟礼品卡
            List<OrderItemVO> items = orderVO.getItems();
            if (CollUtil.isNotEmpty(items)) {
                OrderItemVO first = items.getFirst();
                if (first.getProductType() == PRODUCT_TYPE_GIFT_CARD_VIRTUAL.getCode()) {
                    shippingStep.setTitle("已完成");
                }
            }
        }

        shippingStep.setDescription("");
        steps.add(shippingStep);

        // 物流状态判断
        switch (ShippingStatusEnum.getStatus(orderVO.getShippingStatus())) {
            case PENDING:
                log.info("PENDING");
                break;
            case SENT:
                orderStepVO.setCurrent(2);
                shippingStep.setDescription(orderVO.getShippingTime());
                break;
            case SHIPPED:
                orderStepVO.setCurrent(2);
                shippingStep.setDescription(orderVO.getReceivedTime());
                orderStepVO.setStatus("finish");
                break;
            default:
                break;
        }

        // 订单状态判断
        switch (OrderStatusEnum.getStatus(orderVO.getOrderStatus())) {
            case CANCELLED:
                orderStepVO.setCurrent(1);
                steps.get(1).setTitle(OrderStatusEnum.getStatusName(OrderStatusEnum.CANCELLED.getCode()));
                steps.remove(2);
                orderStepVO.setStatus("error");
                break;
            case INVALID:
                orderStepVO.setCurrent(1);
                steps.get(1).setDescription("");
                orderStepVO.setStatus("error");
                break;
            case PENDING, CONFIRMED, PROCESSING, COMPLETED:
            default:
                break;
        }

        orderStepVO.setSteps(steps);
        return orderStepVO;
    }

    @Override
    public OrderVO detail(Integer id) {
        Integer userId = getCurrentUserId();

        OrderVO detail = this.detail(id, userId);

        if (Objects.isNull(detail) || detail.getOrderId() == null) {
            throw new GlobalException(messageHelper.get("error.order.not.exist"));
        }

        OrderSettingsVO orderSettings = configService.getSettings(OrderSettingsVO.class);

        detail.assembleOrderSettings(orderSettings);

        // 管理端订单详情状态卡副文案（买家端由前端本地兜底，不在此设置）
        detail.setOrderStatusTip(OrderStatusEnum.getStatusTip(detail.getOrderStatus()));

        // 管理端订单详情状态头名称：仅标准流程订单覆盖，不动预约/同城/拼团等特殊状态名，也不影响买家端
        String adminStatusName = OrderStatusEnum.getAdminStatusName(detail.getOrderStatus());
        if (adminStatusName != null
                && Objects.equals(detail.getOrderStatusName(), OrderStatusEnum.getStatusName(detail.getOrderStatus()))) {
            detail.setOrderStatusName(adminStatusName);
        }

        if (OrderType.PIN.getCode() == detail.getOrderType()) {
            GrouponPromotionRecord record = grouponPromotionRecordService.lambdaQuery().eq(GrouponPromotionRecord::getOrderId, detail.getOrderId()).one();
            if (detail.getOrderStatus() == OrderStatusEnum.CANCELLED.getCode()) {
                detail.setJoinStatusDesc("已取消");
            }
            if (record != null) {
                detail.setJoinStatus(record.getJoinStatus());
                detail.setGrouponPromotionRecordId(record.getGrouponPromotionRecordId());
                if (detail.getOrderStatus() != OrderStatusEnum.CANCELLED.getCode()) {
                    detail.setJoinStatusDesc(JoinStatusEnum.getDescription(record.getJoinStatus()));
                }
                if (detail.getOrderStatus() == OrderStatusEnum.CONFIRMED.getCode() && JoinStatusEnum.WAIT_GROUP.getCode().equals(record.getJoinStatus())) {
                    detail.setOrderStatusName("已付款，待成团");
                    Optional.ofNullable(detail.getAvailableActions()).ifPresent((action)->{
                        //待成团不能发货
                        action.setDeliver(false);
                    });
                }
                if (JoinStatusEnum.FAIL.getCode().equals(record.getJoinStatus())) {
                    detail.setOrderStatusName("拼团失败，待退款");
                }
            }
        }


        return detail;
    }

    /**
     * 虚拟商品 / 卡密商品：订单未发货前，买家端（PC/H5）不展示虚拟内容与卡号卡密。
     * 后台/商家请求（带 adminId）不受限；订单一旦不是“待发货”状态即正常展示。
     */
    private void maskUndeliveredVirtualContent(OrderVO orderVO) {
        if (orderVO == null || CollUtil.isEmpty(orderVO.getItems())) {
            return;
        }
        // 后台/商家请求不屏蔽
        if (SecurityUtils.isAdminUser()) {
            return;
        }
        // 非“待发货”（已发货 / 配送中 / 已收货等）正常展示
        Integer shippingStatus = orderVO.getShippingStatus();
        boolean delivered = shippingStatus != null
                && shippingStatus != ShippingStatusEnum.PENDING.getCode();
        if (delivered) {
            return;
        }
        for (OrderItemVO item : orderVO.getItems()) {
            Integer productType = item.getProductType();
            if (productType == null) {
                continue;
            }
            if (productType == PRODUCT_TYPE_VIRTUAL.getCode()
                    || productType == PRODUCT_TYPE_CARD.getCode()) {
                item.setVirtualSample(null);
                item.setECard(new ArrayList<>());
            }
        }
    }

    @Override
    public OrderVO detail(Integer id, Integer userId) {
        Order order = this.lambdaQuery().eq(Order::getOrderId, id).eq(userId != null && userId != 0, Order::getUserId, userId).one();

        // 检查是否拆分订单（isValid=0 的已失效父单才触发拆单错误检查）
        boolean isCycleOrCardMainOrder = false;
        if (order != null && !isCycleOrCardMainOrder
                && (order.getIsValid() != null && order.getIsValid() == 0)) {
            long count = this.lambdaQuery().eq(Order::getParentOrderId, id).count();
            Assert.isTrue(count == 0, () -> new GlobalException(ORDER_SPLIT_ERROR,OPT_ALTER_SUCCESS_CODE));
        }
        // Assert.notNull(order, () -> new GlobalException(ORDER_NOT_EXIST));
        if (order == null) {
            return new OrderVO();
        }

        OrderVO orderVO = new OrderVO(order);
        // 待支付订单：支付截止时间 = 下单时间 + 订单超时分钟数，用于买家端支付倒计时
        if (order.getOrderStatus() != null && order.getOrderStatus() == PENDING.getCode()
                && order.getAddTime() != null && order.getAddTime() > 0) {
            int cancelMinute = 30;
            try {
                String cancelMinuteVal = configService.getConfigVal(SettingsEnum.AUTO_CANCEL_ORDER_MINUTE);
                if (StrUtil.isNotBlank(cancelMinuteVal)) {
                    cancelMinute = Integer.parseInt(cancelMinuteVal.trim());
                }
            } catch (Exception ignored) {
            }
            if (cancelMinute > 0) {
                orderVO.setPayEndTime(TigUtils.handelTime(order.getAddTime() + (long) cancelMinute * 60));
            }
        }
        // 服务器当前时间：买家端倒计时以服务端时间为准，规避本地时钟偏差
        orderVO.setServerTime(TigUtils.handelTime(System.currentTimeMillis() / 1000));
        orderVO.setOrderSubSource(WechatLiveOrderContextUtils.extractSubSource(order.getOrderExtension()));

        try{
            orderVO.setWechatLiveContexts(WechatLiveOrderContextUtils.extractFromOrderExtension(order.getOrderExtension()));
            orderVO.setWechatLiveOrder(!orderVO.getWechatLiveContexts().isEmpty());
            WechatLiveSyncLog latestSyncLog = wechatLiveSyncLogService.getOne(Wrappers.lambdaQuery(WechatLiveSyncLog.class)
                    .eq(WechatLiveSyncLog::getOrderId, order.getOrderId())
                    .orderByDesc(WechatLiveSyncLog::getId)
                    .last("limit 1"));
            if (latestSyncLog != null) {
                orderVO.setLiveSyncStatus(latestSyncLog.getSyncStatus());
                orderVO.setLiveSyncStatusName(getSyncStatusName(latestSyncLog.getSyncStatus()));
                orderVO.setLiveSyncMessage(StrUtil.isNotBlank(latestSyncLog.getErrorMessage()) ? latestSyncLog.getErrorMessage() : latestSyncLog.getEventType());
            }
        }catch (Exception e){

        }


        if (tigshopProperties.getIsOverseas() == 1 && orderVO.getRegionNames() != null && !orderVO.getRegionNames().isEmpty()) {
            List<String> list = orderVO.getRegionNames().stream().map(translatePackage::translate).toList();
            orderVO.setRegionNames(list);
            orderVO.setUserAddressByRegionNames(list);
        }

        // 调用方法获取会员信息
        User user = userMapper.selectById(order.getUserId());
        UserBaseVO userBaseVO = new UserBaseVO(user);
        orderVO.setUser(userBaseVO);

        //设置头像
        String avatar = userBaseVO.getAvatar();
        userBaseVO.setAvatar(userServicesProvider.getIfAvailable().getUserAvatar(avatar));

        List<OrderItemVO> orderItems = orderItemService.getItemByOrderIds(Collections.singletonList(order.getOrderId()), SecurityUtils.isAdminUser());

        try{
            orderItems.forEach(item -> item.setWechatLiveContext(WechatLiveOrderContextUtils.extractFromPromotionData(item.getPromotionData())));
        }catch (Exception e){

        }


        Map<Integer, List<AftersalesItem>> aftersalesItemMap = new HashMap<>();
        if (CollUtil.isNotEmpty(orderItems)) {
            List<Integer> orderItemIds = orderItems.stream().map(OrderItemVO::getItemId).toList();
            List<AftersalesItem> aftersalesItems = aftersalesItemService.validAftersalesItems(orderItemIds);
            aftersalesItemMap = aftersalesItems.stream().collect(Collectors.groupingBy(AftersalesItem::getOrderItemId));
        }

        BigDecimal orderTaxRate = order.getTaxRate() != null ? order.getTaxRate() : BigDecimal.ZERO;
        BigDecimal orderTaxRateAmount = order.getTaxRateAmount() != null ? order.getTaxRateAmount() : BigDecimal.ZERO;
        Map<Integer, List<AftersalesItem>> finalAftersalesItemMap = aftersalesItemMap;
        orderItems = orderItems.stream()
                .peek(item -> {
                    List<AftersalesItem> currAftersalesItems = finalAftersalesItemMap.get(item.getItemId());
                    int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
                    item.setAllowDeliverNum(quantity);
                    item.setAftersalesNumber(0);
                    if (CollUtil.isNotEmpty(currAftersalesItems)) {
                        // 净可发货数 = 购买数量 - 处于锁定状态的售后累计数量。
                        // 锁定状态：进行中 + 已完成（validAftersalesItems 已排除已取消 7）；
                        // 已拒绝(REFUSE=3) 视为释放，对应商品重新可发货，不计入扣减。
                        int lockedNum = 0;
                        for (AftersalesItem currAftersalesItem : currAftersalesItems) {
                            Aftersales aftersales = aftersalesService.getById(currAftersalesItem.getAftersaleId());
                            if (aftersales != null && aftersales.getStatus() != AftersalesStatusEnum.REFUSE.getCode()) {
                                lockedNum += currAftersalesItem.getNumber() == null ? 0 : currAftersalesItem.getNumber();
                            }
                        }
                        item.setAllowDeliverNum(Math.max(quantity - lockedNum, 0));
                        item.setAftersalesNumber(lockedNum);
                        currAftersalesItems = currAftersalesItems.stream().sorted(Comparator.comparing(AftersalesItem::getAftersaleId)).toList();
                        item.setAftersalesItem(currAftersalesItems.getLast());
                    }
                    Shop shop = shopMapper.selectById(item.getShopId());
                    if (shop != null) {
                        item.setShopTitle(shop.getShopTitle());
                    }
                    if (item.getShopId() == 0 && tigshopProperties.getIsO2o() == 1) {
                        item.setShopTitle(configService.getConfigVal(SettingsEnum.STORE_SELF_OPERATED_NAME));
                    }
                    item.setTaxRate(orderTaxRate);
                }).toList();

        // 按商品行实际金额占比分摊税费，尾差归入最后一行
        if (orderTaxRateAmount.compareTo(BigDecimal.ZERO) > 0 && CollUtil.isNotEmpty(orderItems)) {
            BigDecimal totalLineAmount = BigDecimal.ZERO;
            for (OrderItemVO item : orderItems) {
                BigDecimal lineAmount = item.getPrice() != null && item.getQuantity() != null
                        ? item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                        : (item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO);
                totalLineAmount = totalLineAmount.add(lineAmount);
            }
            BigDecimal allocatedTax = BigDecimal.ZERO;
            for (int i = 0; i < orderItems.size(); i++) {
                OrderItemVO item = orderItems.get(i);
                BigDecimal lineAmount = item.getPrice() != null && item.getQuantity() != null
                        ? item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                        : (item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO);
                BigDecimal lineTaxAmount;
                if (i == orderItems.size() - 1) {
                    lineTaxAmount = orderTaxRateAmount.subtract(allocatedTax);
                } else {
                    lineTaxAmount = totalLineAmount.compareTo(BigDecimal.ZERO) > 0
                            ? orderTaxRateAmount.multiply(lineAmount).divide(totalLineAmount, 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    allocatedTax = allocatedTax.add(lineTaxAmount);
                }
                item.setTaxRateAmount(lineTaxAmount);
                item.setSubtotal(lineAmount.add(lineTaxAmount));
            }
        } else {
            for (OrderItemVO item : orderItems) {
                BigDecimal lineAmount = item.getPrice() != null && item.getQuantity() != null
                        ? item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                        : (item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO);
                item.setTaxRateAmount(BigDecimal.ZERO);
                item.setSubtotal(lineAmount);
            }
        }
        orderVO.setItems(orderItems);
        VendorOrderPriceCalculator.apply(orderVO, SecurityUtils.isAdminUser());

        // 虚拟/卡密商品：未发货前买家端不展示虚拟内容与卡号卡密
        maskUndeliveredVirtualContent(orderVO);

        orderVO.setAvailableActions(availableActions(orderVO));

        if (order.getShopId() > 0) {
            Shop shop = shopMapper.selectById(order.getShopId());
            if (shop != null) {
                orderVO.setShop(new OrderVO.ShopVO(shop));
            }
        }

        // 判断是否可以申请售后；1. 判断店铺结算配置（确认收货后立即不可售后）（自营店不用判断）2. 全局设置（买家申请售后限制）
        boolean toAftersales = orderVO.getAvailableActions().getToAftersales();
        orderVO.setToAftersalses(toAftersales);
        Integer orderStatus = order.getOrderStatus();
        if (toAftersales) {
            // 1. 判断店铺结算配置（确认收货后立即不可售后）（自营店不用判断）
            if (order.getShopId() != null && order.getShopId() > 0) {
                // 查询店铺结算配置
                OrderConfig orderConfig = orderConfigMapper.selectOne(
                        new LambdaQueryWrapper<OrderConfig>()
                                .eq(OrderConfig::getShopId, order.getShopId())
                );
                if (orderConfig != null) {
                    OrderConfigDTO orderConfigData = JsonUtil.fromJson(orderConfig.getData(), OrderConfigDTO.class);
                    if (OrderStatusEnum.COMPLETED.getCode() == orderStatus && 0 == orderConfigData.getDateType()) {
                        orderVO.setToAftersalses(false);
                    }
                }
            }
            // 2. 全局设置（买家申请售后限制）
            if (OrderStatusEnum.COMPLETED.getCode() == orderStatus) {
                String afterSalesLimitDays = configService.getConfigVal(SettingsEnum.AFTER_SALES_LIMIT_DAYS);
                if (StrUtil.isNotBlank(afterSalesLimitDays)) {
                    long receivedTime = order.getReceivedTime() * 1000;
                    BigDecimal afterSalesLimitDaysTime = new BigDecimal(afterSalesLimitDays).multiply(new BigDecimal(24 * 60 * 60 * 1000));
                    LocalDateTime limitDateTime = Instant.ofEpochMilli(receivedTime + afterSalesLimitDaysTime.longValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                    boolean toAftersalses = limitDateTime.isAfter(LocalDateTime.now());
                    orderVO.setToAftersalses(toAftersalses);
                }
            }
        }
        orderVO.getAvailableActions().setToAftersales(orderVO.getToAftersalses());

        // 自动发货天数
        Long aftersalesCount = aftersalesMapper.selectCount(
                Wrappers.lambdaQuery(Aftersales.class)
                        .eq(Aftersales::getOrderId, order.getOrderId())
                        .eq(Aftersales::getStatus, AftersalesStatusEnum.IN_REVIEW.getCode())
        );
        String autoDeliveryDays = configService.getConfigVal(SettingsEnum.AUTO_DELIVERY_DAYS);
        if (aftersalesCount == 0 && OrderStatusEnum.PROCESSING.getCode() == orderStatus && StrUtil.isNotBlank(autoDeliveryDays)) {
            long shippingTimeMillis = order.getShippingTime() * 1000;
            long elapsedMillis = Instant.now().toEpochMilli() - shippingTimeMillis;

            long elapsedDays = TimeUnit.MILLISECONDS.toDays(elapsedMillis);

            BigDecimal result = new BigDecimal(autoDeliveryDays).subtract(new BigDecimal(elapsedDays)).setScale(0, RoundingMode.UP);

            orderVO.setAutoDeliveryDays(result);
        }

        orderVO.getItems().forEach(item -> {
            List<ProductSkuDTO.SkuData> skuData = JSONUtil.toList(JSONUtil.parseArray(item.getSkuData()), ProductSkuDTO.SkuData.class);
            item.setSkuValue(skuData
                    .stream()
                    .map(sku -> translatePackage.translate(sku.getName(), DataType.SKU.getCode()) + ":"
                            + translatePackage.translate(sku.getValue(), DataType.SKU.getCode()))
                    .collect(Collectors.joining("|")));

            // 翻译缩略图
        });

        orderVO.setWayBill(logisticsProviderRouter.getActive().isConfigured());
        if (orderVO.getOrderType() == OrderType.VIRTUAL.getCode()) {
            if (orderStatus == OrderStatusEnum.CANCELLED.getCode() || orderStatus == PENDING.getCode()) {
                orderVO.getItems().forEach(item -> item.setVirtualSample(null));
            }
        }

        // 订单卡
        List<GiftCardOrder> giftCardOrder = giftCardOrderMapper.selectList(new LambdaQueryWrapper<>(GiftCardOrder.class)
                .eq(GiftCardOrder::getOrderId, order.getOrderId()));
        if (giftCardOrder != null) {
            List<GiftCardOrderVO.GiftCardOrderInfo> giftCardOrderInfo = giftCardOrderMapper.getGiftCardOrderInfo(order.getOrderId());

            if (CollUtil.isNotEmpty(giftCardOrderInfo)) {
                giftCardOrderInfo.forEach(item -> {
                    item.setCardNumber(StringUtils.maskCardNumber(item.getCardNumber()));
                    item.setCardPassword(StringUtils.maskCardNumber(item.getCardPassword()));
                    if (item.getShopId() == 0) {
                        String shopName = configService.getConfigVal(SettingsEnum.SHOP_NAME);
                        String shopLogo = configService.getConfigVal(SettingsEnum.SHOP_LOGO);
                        item.setShopTitle(shopName);
                        item.setShopLogo(shopLogo);
                    }
                });
            }

            orderVO.setGiftCardOrder(GiftCardOrderVO.builder()
                    .num(giftCardOrderInfo.size())
                    .giftCardOrderInfo(giftCardOrderInfo)
                    .build());
        }

        if (OrderType.PIN.getCode() == order.getOrderType()) {
            GrouponPromotionRecord record = grouponPromotionRecordService.lambdaQuery().eq(GrouponPromotionRecord::getOrderId, order.getOrderId()).one();
            if (orderStatus == OrderStatusEnum.CANCELLED.getCode()) {
                orderVO.setJoinStatusDesc("已取消");
            }
            if (record != null) {
                orderVO.setJoinStatus(record.getJoinStatus());
                orderVO.setGrouponPromotionRecordId(record.getGrouponPromotionRecordId());
                if (orderStatus != OrderStatusEnum.CANCELLED.getCode()) {
                    orderVO.setJoinStatusDesc(JoinStatusEnum.getDescription(record.getJoinStatus()));
                }
                if (orderStatus == OrderStatusEnum.CONFIRMED.getCode() && JoinStatusEnum.WAIT_GROUP.getCode().equals(record.getJoinStatus())) {
                    orderVO.setOrderStatusName("已付款，待成团");
                }
                if (JoinStatusEnum.FAIL.getCode().equals(record.getJoinStatus())) {
                    orderVO.setOrderStatusName("拼团失败，待退款");
                }
            }

        }

        //礼品卡下单逻辑
//        if (OrderType.GIFT_CARD.getCode() == order.getOrderType()) {
//            List<GiftCardOrder> giftCardOrders = giftCardOrderMapper.selectList(Wrappers.lambdaQuery(GiftCardOrder.class).eq(GiftCardOrder::getOrderId, order.getOrderId()));
//            if (CollUtil.isNotEmpty(giftCardOrders)) {
//                List<Integer> cardIds = giftCardOrders.stream().map(GiftCardOrder::getCardId).distinct().toList();
//                List<GiftCards> giftCards = giftCardsMapper.selectList(Wrappers.lambdaQuery(GiftCards.class).in(GiftCards::getGiftCardsId, cardIds));
//                long noUseCard = giftCards.stream().filter(card -> card.getStatus() == 0).count();
//                if (noUseCard > 0) {
//                    orderVO.setOrderStatusName("礼品卡待使用");
//                } else {
//                    orderVO.setOrderStatusName("礼品卡已使用");
//                }
//            }
//        }
        orderVO.setStepStatus(stepStatus(orderVO));

        fillOrderPresale(order, orderVO);

        // 填充派送订单信息（同城配送和预约上门）；详情页需要骑手实时坐标


        Integer shopId = orderVO.getShopId();
        ShopVO detail = shopService.getIfAvailable().detail(shopId);
        if (detail != null) {
            orderVO.setShopName(detail.getShopTitle());
        }

        return orderVO;
    }

    /**
     * 填充派送订单信息（同城配送和预约上门）
     * 同城配送状态流程：待支付—>待备货—>待取货—>配送中—>已送达/已完成
     * 预约上门状态流程：待支付—>预约完成—>待上门—>服务中—>已完成
     *
     * @param order   订单
     * @param orderVO 订单VO
     * @param withRiderLocation 是否查询美团骑手实时坐标（详情页 true，列表 false）
     */


    /**
     * 获取同城配送订单状态名称（有派送订单）
     * 同城配送状态流程：待支付—>待备货—>待取货—>配送中—>已送达/已完成
     */
    private String getSameCityDeliveryStatusName(Order order, DeliveryOrder deliveryOrder) {
        // 根据派送订单状态判断
        Integer deliveryStatus = deliveryOrder.getStatus();
        if (Objects.equals(deliveryStatus, DeliveryOrderStatusEnum.PENDING_PICKUP.getCode())) {
            return "待取货";
        } else if (Objects.equals(deliveryStatus, DeliveryOrderStatusEnum.IN_PROGRESS.getCode())) {
            return "配送中";
        }
        //TODO
        else if (Objects.equals(deliveryStatus, DeliveryOrderStatusEnum.CANCELLED.getCode())) {
            return "已取消";
        }
        if (Objects.equals(ShippingStatusEnum.SENT_WAITING_DELIVERY.getCode(), order.getShippingStatus())) {
            return ShippingStatusEnum.SENT_WAITING_DELIVERY.getDescription();
        }
        return null;
    }


    /**
     * 获取预约上门订单状态名称（有派送订单）
     * 预约上门状态流程：待支付—>预约完成—>待上门—>服务中—>已完成
     */
    private String getReservationHomeStatusName(Order order, DeliveryOrder deliveryOrder) {


        // 根据派送订单状态判断
        Integer deliveryStatus = deliveryOrder.getStatus();
        if (Objects.equals(deliveryStatus, DeliveryOrderStatusEnum.PENDING_PICKUP.getCode())) {
            return "待上门";
        } else if (Objects.equals(deliveryStatus, DeliveryOrderStatusEnum.IN_PROGRESS.getCode())) {
            return "服务中";
        }
        //TODO
        else if (Objects.equals(deliveryStatus, DeliveryOrderStatusEnum.CANCELLED.getCode())) {
            return "已取消";
        }

        return null;
    }

    /**
     * 获取预约上门订单状态名称（无派送订单）
     */
    private String getReservationHomeStatusNameWithoutDeliveryOrder(Order order) {
        if (order.getOrderStatus() == OrderStatusEnum.PENDING.getCode()) {
            return "待支付";
        }
        if (order.getOrderStatus() == OrderStatusEnum.CONFIRMED.getCode()) {
            return "预约完成";
        }
        if (order.getOrderStatus() == OrderStatusEnum.CANCELLED.getCode()) {
            return "已取消";
        }
        if (order.getOrderStatus() == OrderStatusEnum.COMPLETED.getCode()) {
            return "已完成";
        }
        return OrderStatusEnum.getStatusName(order.getOrderStatus());
    }

    private void fillOrderPresale(Order order, OrderVO orderVO) {
        if (order.getIsPresale() != null && order.getIsPresale() == 1) {
            try {
                OrderPresaleVO presaleVO = orderPresaleService.getPresaleVOByOrderId(order.getOrderId());
                if (presaleVO != null) {
                    orderVO.setPresaleInfo(presaleVO);
                    ProductPresaleVO productPresale = presaleVO.getProductPresale();
                    if(Objects.nonNull(productPresale)){
                        try{
                            productPresale.setFinalPaymentStartTimeText(productService.buildPresaleFinalPaymentTimeText(productPresale,
                                    HeaderUtils.getClientType(), ProductServiceImpl.PRESALE_SOURCE_ORDER_DETAIL));
                            productPresale.setDeliveryTimeText(productService.buildPresaleDeliveryTimeText(productPresale,
                                    HeaderUtils.getClientType(), ProductServiceImpl.PRESALE_SOURCE_ORDER_DETAIL));
                        }catch (Exception e){

                        }
                    }
                    log.debug("订单[{}]预售信息已加载，状态: {}", order.getOrderId(), presaleVO.getPresaleStatus());
                }

                //定金预售模式
                if (presaleVO.getSaleMode() != 1) {
                    Integer presaleStatus = presaleVO.getPresaleStatus();
                    Integer orderStatus = order.getOrderStatus();

                    if (!Integer.valueOf(2).equals(presaleStatus)
                            && Integer.valueOf(0).equals(orderStatus)) {
                        //修改订单状态,代表已完成支付
                        orderVO.setOrderStatusName(presaleVO.getPresaleStatusText());
                    }
                }

                Integer presaleStatus = presaleVO.getPresaleStatus();
                if (Integer.valueOf(0).equals(presaleStatus) && Integer.valueOf(2).equals(order.getPresaleType())) {
                    Optional.ofNullable(orderVO.getAvailableActions()).ifPresent((av) -> av.setCancelOrder(true));
                }

                //待支付尾款不能取消订单
                if (Integer.valueOf(2).equals(order.getPresaleType()) && Integer.valueOf(1).equals(presaleStatus)) {
                    Optional.ofNullable(orderVO.getAvailableActions()).ifPresent((av) -> av.setCancelOrder(false));
                }

                //预售商品一次只能购买一件,将定金和尾款加入到orderItemVos当中去
                orderVO.setItems(orderVO.getItems().stream().peek(item -> {
                    item.setDepositAmount(presaleVO.getDepositAmount());

                    // presaleVO 查询出来的尾款是 订单总金额-定金, 订单金额是:（商品总价+运费等-优惠券-各种优惠活动）
                    //item.setFinalPaymentAmount(presaleVO.getFinalPaymentAmount());
                    item.setFinalPaymentAmount(item.getSubtotal().subtract(presaleVO.getDepositAmount()));
                }).toList());
            } catch (Exception e) {
                log.error("查询订单[{}]预售信息失败", order.getOrderId(), e);
                // 不影响订单详情的正常返回
            }
        }
    }

    @Override
    public OrderNumVO orderNum() {
        OrderNumVO orderNumVO = new OrderNumVO();
        int userId = getCurrentUserId();
        orderNumVO.setAwaitPay(this.getOrderStatusCount(userId, "order_status", PENDING.getCode()));
        orderNumVO.setAwaitShipping(this.getOrderStatusCount(userId, "order_status",
                OrderStatusEnum.CONFIRMED.getCode()));
        orderNumVO.setAwaitReceived(this.getOrderStatusCount(userId, "order_status",
                OrderStatusEnum.PROCESSING.getCode()));
        orderNumVO.setAwaitComment(this.getOrderStatusCount(userId, "order_comment_status",
                CommentStatus.PENDING.getCode()));
        orderNumVO.setOrderCompleted(this.getOrderStatusCount(userId, "order_status",
                OrderStatusEnum.COMPLETED.getCode()));
        Long aftersalesCount = aftersalesService.count(new LambdaQueryWrapper<Aftersales>()
                .eq(Aftersales::getUserId, userId)
                .in(Aftersales::getStatus, AftersalesStatusEnum.inProgressStatus()));
        orderNumVO.setAwaitAftersalesCollect(aftersalesCount);

        Long collectProductCount = collectProductMapper.selectCount(new LambdaQueryWrapper<CollectProduct>().eq(CollectProduct::getUserId, userId));
        orderNumVO.setProductCollect(collectProductCount);
        Long collectShopCount = collectShopMapper.selectCount(new LambdaQueryWrapper<CollectShop>().eq(CollectShop::getUserId, userId));
        orderNumVO.setShopCollect(collectShopCount);
        return orderNumVO;
    }

    @Override
    public boolean create(OrderCreateDTO createDTO) {
        if (createDTO != null) {
            Order order = new Order();
            BeanUtils.copyProperties(createDTO, order);
            return this.save(order);
        }
        return false;
    }

    @Override
    public boolean update(OrderUpdateDTO updateDTO) {
        if (updateDTO != null) {
            Order order = new Order();
            BeanUtils.copyProperties(updateDTO, order);
            return this.updateById(order);
        }
        return false;
    }

    @Override
    public Long getNewOrderCount(String adminType, Integer shopId, Integer vendorId, Long startTime) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        if (AdminTypeEnum.fromCode(adminType) == AdminTypeEnum.VENDOR) {
            queryWrapper.eq(Order::getVendorId, vendorId);
        } else {
            queryWrapper.eq(Order::getShopId, shopId);
        }
        queryWrapper.ge(Order::getAddTime, startTime);
        return this.count(queryWrapper);

    }

    @Override
    public Long getPaidOrderCount(String adminType, Integer shopId, Integer vendorId, Long startTime) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        if (AdminTypeEnum.fromCode(adminType) == AdminTypeEnum.VENDOR) {
            queryWrapper.eq(Order::getVendorId, vendorId);
        } else {
            queryWrapper.eq(Order::getShopId, shopId);
        }
        // 语音播报基于"前后两帧计数差值"判断是否发声，要求计数单调递增。
        // 故此处仅按"已付款 + payTime>=startTime"统计，不再叠加 shippingStatus=待发货，
        // 避免发货后计数回落导致后续新付款单 delta<=0 而漏播。
        queryWrapper.eq(Order::getPayStatus, PayStatusEnum.PAID.getCode());
        queryWrapper.ge(Order::getPayTime, startTime);
        return this.count(queryWrapper);
    }

    @Override
    public Long getCancelOrderCount(String adminType, Integer shopId, Integer vendorId, Long startTime) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        if (AdminTypeEnum.fromCode(adminType) == AdminTypeEnum.VENDOR) {
            queryWrapper.eq(Order::getVendorId, vendorId);
        } else {
            queryWrapper.eq(Order::getShopId, shopId);
        }
        queryWrapper.eq(Order::getOrderStatus, OrderStatusEnum.CANCELLED.getCode());
        queryWrapper.ge(Order::getAddTime, startTime);
        return this.count(queryWrapper);
    }

    @Override
    public BigDecimal getOrderBalance(String startTime, String endTime, String type) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
            Date startTimeDate = DateUtil.parse(startTime);
            Date endTimeDate = DateUtil.parse(endTime);
            // 将时间设置为当天的结束时间
            endTimeDate = DateUtil.endOfDay(endTimeDate);
            // 添加时间范围的条件，假设 changeTime 是 Unix 时间戳
            queryWrapper.ge(Order::getAddTime, startTimeDate.getTime() / 1000)
                    .le(Order::getAddTime, endTimeDate.getTime() / 1000);
        }
        if (Objects.equals(type, "balance")) {
            // 查询所有符合条件的余额变动
            queryWrapper.select(Order::getBalance);
            List<Order> orderList = this.list(queryWrapper);
            return orderList.stream()
                    .map(Order::getBalance)
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
        } else if (Objects.equals(type, "use_points")) {
            queryWrapper.select(Order::getUsePoints);
            List<Order> orderList = this.list(queryWrapper);
            return orderList.stream()
                    .map(order -> BigDecimal.valueOf(order.getUsePoints()))
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public Page<SalesProductOrderItemVO> getSalesProductDetail(SalesProductDetailPageQuery pageQuery) {
        Page<SalesProductOrderItemVO> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        return this.baseMapper.getSalesProductDetail(page, pageQuery);
    }

    @Override
    public OrderPageConfigVO getOrderPageConfig() {
        return configService.getSettings(OrderPageConfigVO.class);
    }

    @Override
    public OrderStatusCountVO getStatusCount(OrderListPageQuery pageQuery) {
        OrderStatusCountVO vo = new OrderStatusCountVO();
        // -2 为已删除虚拟状态，buildQueryWrapper 会转为 is_del=1 查询，复用全部筛选条件单独统计
        vo.setDeletedCount(countByOrderStatus(pageQuery, "-2"));
        if (hasJoinFilter(pageQuery)) {
            // 关键词检索(关联 order_item)或自提端(关联 order_pickup_item)会触发 groupBy(order_id) 去重，
            // 此时 count() 会自动嵌套以按订单去重；改单条 GROUP BY 会丢失去重导致多算，故逐状态 COUNT 保证正确。
            vo.setAllCount(countByOrderStatus(pageQuery, null));
            vo.setPendingCount(countByOrderStatus(pageQuery, String.valueOf(OrderStatusEnum.PENDING.getCode())));
            vo.setConfirmedCount(countByOrderStatus(pageQuery, String.valueOf(OrderStatusEnum.CONFIRMED.getCode())));
            vo.setProcessingCount(countByOrderStatus(pageQuery, String.valueOf(OrderStatusEnum.PROCESSING.getCode())));
            vo.setCancelledCount(countByOrderStatus(pageQuery, String.valueOf(OrderStatusEnum.CANCELLED.getCode())));
            vo.setCompletedCount(countByOrderStatus(pageQuery, String.valueOf(OrderStatusEnum.COMPLETED.getCode())));
        } else {
            // 无关联场景：用单条 GROUP BY order_status 聚合替代多次 COUNT
            OrderListPageQuery query = BeanUtil.copyProperties(pageQuery, OrderListPageQuery.class);
            query.setOrderStatus(null);
            MPJLambdaWrapper<Order> wrapper = new MPJLambdaWrapper<>();
            buildQueryWrapper(wrapper, query);
            wrapper.select("order_status AS orderStatus", "COUNT(*) AS cnt").groupBy("order_status");
            long all = 0L;
            for (Map<String, Object> row : this.listMaps(wrapper)) {
                Object statusObj = row.get("orderStatus");
                Object cntObj = row.get("cnt");
                if (statusObj == null || cntObj == null) {
                    continue;
                }
                int status = ((Number) statusObj).intValue();
                long cnt = ((Number) cntObj).longValue();
                all += cnt;
                if (status == OrderStatusEnum.PENDING.getCode()) {
                    vo.setPendingCount(cnt);
                } else if (status == OrderStatusEnum.CONFIRMED.getCode()) {
                    vo.setConfirmedCount(cnt);
                } else if (status == OrderStatusEnum.PROCESSING.getCode()) {
                    vo.setProcessingCount(cnt);
                } else if (status == OrderStatusEnum.CANCELLED.getCode()) {
                    vo.setCancelledCount(cnt);
                } else if (status == OrderStatusEnum.COMPLETED.getCode()) {
                    vo.setCompletedCount(cnt);
                }
            }
            vo.setAllCount(all);
        }
        return vo;
    }

    /**
     * 列表筛选是否会触发关联查询（order_item / order_pickup_item）从而产生按 order_id 去重。
     * 命中则需逐状态 count（依赖 MyBatis-Plus 对 groupBy 的嵌套去重）以保证统计正确。
     */
    private boolean hasJoinFilter(OrderListPageQuery pageQuery) {
        if (StringUtils.isNotEmpty(pageQuery.getKeyword())) {
            return true;
        }
        try {
            return AdminTypeEnum.PICKUP.getCode().equals(getAdminType());
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * 按指定订单状态统计数量，复用列表的筛选条件。
     * 复制入参后再覆盖 orderStatus，避免改写调用方共享的 pageQuery 实例（消除副作用）。
     */
    private Long countByOrderStatus(OrderListPageQuery pageQuery, String orderStatus) {
        OrderListPageQuery query = BeanUtil.copyProperties(pageQuery, OrderListPageQuery.class);
        query.setOrderStatus(orderStatus);
        MPJLambdaWrapper<Order> queryWrapper = new MPJLambdaWrapper<>();
        buildQueryWrapper(queryWrapper, query);
        return this.count(queryWrapper);
    }

    @Transactional
    @Override
    public void changeOrderStatus(OrderChangeStatusParam param) {
        Order order = this.getById(param.getId());
        Integer oldOrderStatus = order.getOrderStatus();
        Assert.notNull(order, () -> new GlobalException("订单不存在"));


        if (param.getField().equals("orderStatus") && param.getVal().equals(OrderStatusEnum.CANCELLED.getCode())) {
            AftersalesApplyAdminParam adminParam = new AftersalesApplyAdminParam();
            adminParam.setOrderId(param.getId());
            adminParam.setDescription(param.getAdminNote());
            this.cancelOrderByAdmin(adminParam);
            return;
        }
        Integer shopId = HeaderUtils.getShopId();

        OrderSettingsVO orderSettings = configService.getSettings(OrderSettingsVO.class);
        Integer isChangeOrderStatus = Constants.NO;
        Integer changeOrderStatus = order.getOrderStatus();

        if (shopId != null && shopId == 0) {
            if (Constants.YES.equals(orderSettings.getIsPlatformCancelPaidOrder()) && OrderStatusEnum.CONFIRMED.getCode() == order.getOrderStatus()) {
                isChangeOrderStatus = Constants.YES;
                OrderStatusEnum preStatus = OrderStatusEnum.getPreStatus(order.getOrderStatus());
                if (preStatus != null) {
                    changeOrderStatus = preStatus.getCode();
                }
            }
            if (Constants.YES.equals(orderSettings.getIsPlatformCancelDeliverOrder()) && OrderStatusEnum.PROCESSING.getCode() == order.getOrderStatus()) {
                isChangeOrderStatus = Constants.YES;
                OrderStatusEnum preStatus = OrderStatusEnum.getPreStatus(order.getOrderStatus());
                if (preStatus != null) {
                    changeOrderStatus = preStatus.getCode();
                }
            }
        }

        if (shopId != null && shopId > 0) {
            if (Constants.YES.equals(orderSettings.getIsShopCancelDeliverOrder()) && OrderStatusEnum.PROCESSING.getCode() == order.getOrderStatus()) {
                isChangeOrderStatus = Constants.YES;
                OrderStatusEnum preStatus = OrderStatusEnum.getPreStatus(order.getOrderStatus());
                if (preStatus != null) {
                    changeOrderStatus = preStatus.getCode();
                }
            }
        }

        if (Constants.NO.equals(isChangeOrderStatus)) {
            return;
        }

        // 取消支付
        if (OrderStatusEnum.CANCELLED.getCode() == changeOrderStatus) {
            order.setAdminNote(param.getAdminNote());
            this.updateById(order);

            // 走仅退款的逻辑
            this.changeCancelOrder(order.getOrderId(), order.getUserId());
        }
        // 取消发货
        if (OrderStatusEnum.CONFIRMED.getCode() == changeOrderStatus) {
            // 修改订单状态、发货状态
            order.setOrderStatus(changeOrderStatus);
            order.setShippingStatus(ShippingStatusEnum.PENDING.getCode());
            order.setShippingTime(0L);
            order.setLogisticsId(0);
            order.setTrackingNo("");
            order.setLogisticsName("");
            order.setAdminNote(param.getAdminNote());
            this.updateById(order);
        }

        // 3. 添加订单日志
        OrderLog orderLog = OrderLog.builder()
                .orderId(order.getOrderId())
                .orderSn(order.getOrderSn())
                .adminId(SecurityUtils.getCurrentAdminId())
                .description("订单状态由" + OrderStatusEnum.getStatusName(oldOrderStatus) + "改为" + OrderStatusEnum.getStatusName(param.getVal()))
                .logTime(StringUtils.getCurrentTime())
                .shopId(HeaderUtils.getShopId())
                .build();
        orderLogService.save(orderLog);
    }

    private boolean changeCancelOrder(Integer id, Integer userId) {
        OrderVO orderVO = this.detail(id, userId);
        orderLogService.quickCreate(ORDER_CANCEL, id, orderVO.getOrderSn());

        // 管理员取消与买家/超时取消保持一致：兑换订单或未付款订单返还已扣积分；
        // 已付款普通订单仍不在此返还，由售后退款流程决定。
        boolean isExchangeOrder = Objects.equals(orderVO.getIsExchangeOrder(), 1);
        boolean isUnpaidOrder = orderVO.getPayStatus() != null
                && orderVO.getPayStatus() == PaymentStatus.UNPAID.getCode();
        if (orderVO.getUsePoints() > 0 && (isExchangeOrder || isUnpaidOrder)) {
            String configVal = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
            userPointsLogService.incPoints(orderVO.getUsePoints(), orderVO.getUserId(), "订单取消返还" + (StrUtil.isNotBlank(configVal) ? configVal : "积分"), id);
        }
        //使用了余额返余额
        if (orderVO.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            userBalanceService.incBalance(orderVO.getBalance(), orderVO.getUserId(), "订单取消返还余额");
        }
        if (orderVO.getCouponAmount().compareTo(BigDecimal.ZERO) > 0) {
            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setUsedTime(0L);
            userCoupon.setOrderId(0);
            int updated = userCouponMapper.update(userCoupon,
                    Wrappers.lambdaQuery(UserCoupon.class)
                            .eq(UserCoupon::getOrderId, orderVO.getOrderId())
                            .eq(UserCoupon::getUserId, orderVO.getUserId()));
            // 拆单子单：UserCoupon.orderId 仍为原始父单ID，需回溯；
            // 仅检查有优惠券金额的兄弟子单，店铺券只归属一家店时无需等其他店取消
            if (updated == 0 && orderVO.getParentOrderId() != null && orderVO.getParentOrderId() > 0) {
                long activeCouponSiblings = this.lambdaQuery()
                        .eq(Order::getParentOrderId, orderVO.getParentOrderId())
                        .ne(Order::getOrderId, orderVO.getOrderId())
                        .gt(Order::getCouponAmount, BigDecimal.ZERO)
                        .ne(Order::getOrderStatus, OrderStatusEnum.CANCELLED.getCode())
                        .count();
                if (activeCouponSiblings == 0) {
                    userCouponMapper.update(userCoupon,
                            Wrappers.lambdaQuery(UserCoupon.class)
                                    .eq(UserCoupon::getOrderId, orderVO.getParentOrderId())
                                    .eq(UserCoupon::getUserId, orderVO.getUserId()));
                }
            }
        }
        for (OrderItemVO item : orderVO.getItems()) {
            if (item.getSkuId() > 0) {
                productSkuService.incStock(item.getSkuId(), item.getQuantity(), null);
                productService.incStock(item.getProductId(), item.getQuantity(), null, false);
            } else {
                productService.incStock(item.getProductId(), item.getQuantity(), null);
            }
            //减少销量
            Product product = productService.getById(item.getProductId());
            if (product.getVirtualSales() != null && product.getVirtualSales() < item.getQuantity()) {
                productService.lambdaUpdate().eq(Product::getProductId, item.getProductId()).setDecrBy(Product::getVirtualSales, product.getVirtualSales()).update();
            } else {
                productService.lambdaUpdate().eq(Product::getProductId, item.getProductId()).setDecrBy(Product::getVirtualSales, item.getQuantity()).update();
            }
            //秒杀品减少销量
            seckillService.decSales(item.getProductId(), item.getSkuId(), item.getQuantity());
            //秒杀返回库存
            seckillService.incStock(item.getProductId(), item.getSkuId(), item.getQuantity());
        }
        Order order = new Order();
        order.setOrderId(id);
        order.setOrderStatus(OrderStatusEnum.CANCELLED.getCode());
        this.updateById(order);
        // 发送后台信息 -- 取消订单
        Order byId = this.getById(id);
        AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
        adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.ORDER_CANCELED.getCatId());
        adminMsgCreateDTO.setTitle("订单已被取消：" + byId.getOrderSn() + ",金额：" + byId.getTotalAmount());
        adminMsgCreateDTO.setContent("订单编号：" + byId.getOrderSn() + "，订单金额：" + byId.getTotalAmount() + configService.getCurrencyName() + "，订单取消。");
        adminMsgCreateDTO.setShopId(byId.getShopId());
        adminMsgCreateDTO.setOrderId(byId.getOrderId());
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("orderId", byId.getOrderId());
        adminMsgCreateDTO.setRelatedData(relatedData);
        adminMsgService.createMessage(adminMsgCreateDTO);
        if (byId.getShopId() != null && byId.getShopId() > 0) {
            // 店铺订单转发给平台的副本：消息中心可见但恒不播报（平台只播自营）
            adminMsgCreateDTO.setShopId(0);
            adminMsgCreateDTO.setIsBroadcast(0);
            adminMsgService.createMessage(adminMsgCreateDTO);
        }
        if (byId.getVendorId() != null && byId.getVendorId() > 0) {
            // 供应商需处理自己订单 → 正常播报（复用对象，须显式重置 is_broadcast）
            adminMsgCreateDTO.setVendorId(byId.getVendorId());
            adminMsgCreateDTO.setIsBroadcast(1);
            adminMsgService.createMessage(adminMsgCreateDTO);
        }
        return true;
    }

    @Override
    public void remindDeliver(Integer id) {
        OrderVO orderVO = this.detail(id);
        if (orderVO.getVendorId() == null || orderVO.getVendorId() == 0) {
            throw new GlobalException("非供应商订单不允许操作");
        }
        if (orderVO.getOrderStatus() != OrderStatusEnum.CONFIRMED.getCode()) {
            throw new GlobalException("订单状态错误");
        }

        AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
        adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.QUICK_DELIVERY.getCatId());
        adminMsgCreateDTO.setTitle("发货提醒：" + orderVO.getOrderSn() + ",金额：" + orderVO.getTotalAmount());
        adminMsgCreateDTO.setVendorId(orderVO.getVendorId());
        adminMsgCreateDTO.setContent("收到订单号：" + orderVO.getOrderSn() + "，金额：" + orderVO.getTotalAmount() + "，的催发货提醒，请尽快进行发货");
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("orderId", orderVO.getOrderId());
        adminMsgCreateDTO.setRelatedData(relatedData);
        adminMsgService.createMessage(adminMsgCreateDTO);
    }


    @Override
    public void cancelOrderByAdmin(AftersalesApplyAdminParam param) {

        Order order = getById(param.getOrderId());
        Assert.isTrue(order != null, () -> new GlobalException("订单不存在"));

        Assert.isTrue(order.getOrderStatus() == OrderStatusEnum.CONFIRMED.getCode(), () -> new GlobalException("已发货订单不能取消"));

        param.setAftersaleType(AftersalesTypeEnum.PAYRETURN.getCode());
        param.setUserId(order.getUserId());
        param.setAftersaleReason("其他");
        if (StrUtil.isEmpty(param.getDescription())) {
            param.setDescription("商家取消订单");
        }

        List<AftersalesApplyParam.ItemsDTO> items = new ArrayList<>();
        List<OrderItemVO> itemByOrderIds = orderItemService.getItemByOrderIds(List.of(param.getOrderId()));
        for (OrderItemVO item : itemByOrderIds) {
            AftersalesApplyParam.ItemsDTO itemsDTO = new AftersalesApplyParam.ItemsDTO();
            itemsDTO.setOrderItemId(item.getItemId());
            itemsDTO.setNumber(item.getQuantity());
            items.add(itemsDTO);
        }

        AftersalesApplyParam aftersalesApplyParam = new AftersalesApplyParam();
        BeanUtils.copyProperties(param, aftersalesApplyParam);
        aftersalesApplyParam.setItems(items);
        aftersalesApplyParam.setIsAdminCancel(true);
        aftersalesService.afterSalesApply(aftersalesApplyParam);
    }

    @Override
    public OrderDeliverImportResultVO batchDeliverImport(MultipartFile file) {
        Assert.notNull(file, () -> new GlobalException("请上传文件"));

        // 读取Excel，headRowNumber=2 跳过第一行说明行（第0行说明，第1行表头，第2行开始是数据）
        List<OrderDeliverExcelDTO> importList;
        try {
            importList = EasyExcel.read(file.getInputStream())
                    .head(OrderDeliverExcelDTO.class)
                    .sheet()
                    .headRowNumber(2)
                    .doReadSync();
        } catch (IOException e) {
            throw new GlobalException("读取Excel文件失败");
        }
        Assert.isTrue(CollUtil.isNotEmpty(importList), () -> new GlobalException("表格数据为空"));

        List<OrderDeliverImportResultVO.FailDetail> failDetails = new ArrayList<>();
        int successCount = 0;

        // 按订单号分组，保留行号信息
        Map<String, List<Map.Entry<Integer, OrderDeliverExcelDTO>>> orderGroupMap = new LinkedHashMap<>();
        for (int i = 0; i < importList.size(); i++) {
            OrderDeliverExcelDTO dto = importList.get(i);
            int rowNum = i + 3; // Excel行号从3开始（第1行说明，第2行表头，第3行开始是数据）
            String orderSn = StrUtil.isNotBlank(dto.getOrderSn()) ? dto.getOrderSn().trim() : "";
            orderGroupMap.computeIfAbsent(orderSn, k -> new ArrayList<>()).add(Map.entry(rowNum, dto));
        }

        // 按订单处理，每个订单只发货一次
        for (Map.Entry<String, List<Map.Entry<Integer, OrderDeliverExcelDTO>>> orderEntry : orderGroupMap.entrySet()) {
            String orderSn = orderEntry.getKey();
            List<Map.Entry<Integer, OrderDeliverExcelDTO>> rowList = orderEntry.getValue();

            try {
                // 取第一行作为订单级别的发货参数（配送方式、物流公司、运单号）
                Map.Entry<Integer, OrderDeliverExcelDTO> firstRow = rowList.get(0);
                int firstRowNum = firstRow.getKey();
                OrderDeliverExcelDTO firstDto = firstRow.getValue();

                // 基础校验
                String validateResult = validateDeliverExcelData(firstDto);
                if (validateResult != null) {
                    for (Map.Entry<Integer, OrderDeliverExcelDTO> row : rowList) {
                        failDetails.add(OrderDeliverImportResultVO.FailDetail.builder()
                                .rowNum(row.getKey())
                                .orderSn(orderSn)
                                .reason(validateResult)
                                .build());
                    }
                    continue;
                }

                // 查询订单
                Order order = this.lambdaQuery().eq(Order::getOrderSn, orderSn).one();
                if (order == null) {
                    for (Map.Entry<Integer, OrderDeliverExcelDTO> row : rowList) {
                        failDetails.add(OrderDeliverImportResultVO.FailDetail.builder()
                                .rowNum(row.getKey())
                                .orderSn(orderSn)
                                .reason("订单不存在")
                                .build());
                    }
                    continue;
                }

                // 检查订单状态
                OrderVO orderVO = this.detail(order.getOrderId());
                if (!orderVO.getAvailableActions().getDeliver()) {
                    for (Map.Entry<Integer, OrderDeliverExcelDTO> row : rowList) {
                        failDetails.add(OrderDeliverImportResultVO.FailDetail.builder()
                                .rowNum(row.getKey())
                                .orderSn(orderSn)
                                .reason("订单状态不允许发货")
                                .build());
                    }
                    continue;
                }

                // 解析配送方式（从第一行获取）
                Integer shippingMethod = parseShippingMethod(firstDto.getShippingMethod());
                if (shippingMethod == null) {
                    for (Map.Entry<Integer, OrderDeliverExcelDTO> row : rowList) {
                        failDetails.add(OrderDeliverImportResultVO.FailDetail.builder()
                                .rowNum(row.getKey())
                                .orderSn(orderSn)
                                .reason("无效的配送方式，有效值：物流配送、商家配送、无需配送、电子面单")
                                .build());
                    }
                    continue;
                }

                // 根据发货方式校验必填项
                String methodValidateResult = validateShippingMethodFields(firstDto, shippingMethod);
                if (methodValidateResult != null) {
                    for (Map.Entry<Integer, OrderDeliverExcelDTO> row : rowList) {
                        failDetails.add(OrderDeliverImportResultVO.FailDetail.builder()
                                .rowNum(row.getKey())
                                .orderSn(orderSn)
                                .reason(methodValidateResult)
                                .build());
                    }
                    continue;
                }

                // 校验物流公司是否存在（如果需要）
                Integer logisticsId = null;
                if (shippingMethod == 1 || shippingMethod == 4) {
                    LogisticsCompany logisticsCompany = logisticsCompanyMapper.selectOne(
                            new LambdaQueryWrapper<LogisticsCompany>()
                                    .eq(LogisticsCompany::getLogisticsName, firstDto.getLogisticsName().trim())
                                    .last("LIMIT 1")
                    );
                    if (logisticsCompany == null) {
                        for (Map.Entry<Integer, OrderDeliverExcelDTO> row : rowList) {
                            failDetails.add(OrderDeliverImportResultVO.FailDetail.builder()
                                    .rowNum(row.getKey())
                                    .orderSn(orderSn)
                                    .reason("物流公司不存在：" + firstDto.getLogisticsName())
                                    .build());
                        }
                        continue;
                    }
                    logisticsId = logisticsCompany.getLogisticsId();
                }

                // 构建发货参数，合并所有商品行
                List<DeliverInfo> deliverInfoList = new ArrayList<>();
                boolean hasError = false;
                for (Map.Entry<Integer, OrderDeliverExcelDTO> row : rowList) {
                    OrderDeliverExcelDTO dto = row.getValue();
                    int rowNum = row.getKey();

                    // 解析发货数量
                    Integer deliveryQuantity = null;
                    if (StrUtil.isNotBlank(dto.getDeliveryQuantity())) {
                        try {
                            deliveryQuantity = Integer.parseInt(dto.getDeliveryQuantity().trim());
                            if (deliveryQuantity <= 0) {
                                failDetails.add(OrderDeliverImportResultVO.FailDetail.builder()
                                        .rowNum(rowNum)
                                        .orderSn(orderSn)
                                        .reason("发货数量必须大于0")
                                        .build());
                                hasError = true;
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            failDetails.add(OrderDeliverImportResultVO.FailDetail.builder()
                                    .rowNum(rowNum)
                                    .orderSn(orderSn)
                                    .reason("发货数量格式错误，必须为正整数")
                                    .build());
                            hasError = true;
                            continue;
                        }
                    }

                    // 根据商品名称和规格匹配订单商品项
                    OrderItemVO matchedItem = matchOrderItem(orderVO.getItems(), dto.getProductName(), dto.getSkuName());
                    if (matchedItem == null) {
                        failDetails.add(OrderDeliverImportResultVO.FailDetail.builder()
                                .rowNum(rowNum)
                                .orderSn(orderSn)
                                .reason("未找到匹配的商品：" + dto.getProductName() + (StrUtil.isNotBlank(dto.getSkuName()) ? "(" + dto.getSkuName() + ")" : ""))
                                .build());
                        hasError = true;
                        continue;
                    }

                    DeliverInfo deliverInfo = new DeliverInfo();
                    deliverInfo.setItemId(matchedItem.getItemId());
                    deliverInfo.setToDeliveryQuantity(deliveryQuantity != null ? deliveryQuantity : matchedItem.getQuantity());
                    deliverInfoList.add(deliverInfo);
                }

                if (hasError || deliverInfoList.isEmpty()) {
                    continue;
                }

                // 构建发货参数（发货信息从第一行获取）
                DeliverOrderParam param = DeliverOrderParam.builder()
                        .id(order.getOrderId())
                        .shippingMethod(shippingMethod)
                        .logisticsId(logisticsId)
                        .trackingNo(StrUtil.isNotBlank(firstDto.getTrackingNo()) ? firstDto.getTrackingNo().trim() : null)
                        .deliverData(deliverInfoList)
                        .build();

                // 执行发货（每个订单只调用一次）
                applicationContext.getBean(OrderService.class).deliverOrder(param);
                successCount++;

            } catch (Exception e) {
                log.error("批量发货异常，订单编号：{}，错误：{}", orderSn, e.getMessage(), e);
                for (Map.Entry<Integer, OrderDeliverExcelDTO> row : rowList) {
                    failDetails.add(OrderDeliverImportResultVO.FailDetail.builder()
                            .rowNum(row.getKey())
                            .orderSn(orderSn)
                            .reason("发货失败：" + e.getMessage())
                            .build());
                }
            }
        }

        return OrderDeliverImportResultVO.builder()
                .totalCount(importList.size())
                .successCount(successCount)
                .failCount(failDetails.size())
                .failDetails(failDetails)
                .build();
    }

    /**
     * 根据商品名称和规格匹配订单商品项
     */
    private OrderItemVO matchOrderItem(List<OrderItemVO> items, String productName, String skuName) {
        if (CollUtil.isEmpty(items) || StrUtil.isBlank(productName)) {
            return null;
        }
        String trimmedProductName = productName.trim();
        // 处理规格名称，"-" 表示无规格，等同于空
        String trimmedSkuName = StrUtil.isNotBlank(skuName) ? skuName.trim() : null;
        if ("-".equals(trimmedSkuName)) {
            trimmedSkuName = null;
        }

        for (OrderItemVO item : items) {
            // 匹配商品名称
            if (!trimmedProductName.equals(item.getProductName())) {
                continue;
            }
            // 如果指定了规格，还需匹配规格
            String itemSkuValue = item.getSkuValue();
            if (trimmedSkuName != null) {
                if (trimmedSkuName.equals(itemSkuValue)) {
                    return item;
                }
            } else {
                // 未指定规格，匹配无规格的商品或直接返回第一个名称匹配的商品
                if (StrUtil.isBlank(itemSkuValue) || "-".equals(itemSkuValue)) {
                    return item;
                }
            }
        }
        // 如果没有精确匹配，尝试只按商品名称匹配第一个
        for (OrderItemVO item : items) {
            if (trimmedProductName.equals(item.getProductName())) {
                return item;
            }
        }
        return null;
    }

    @Override
    public void getBatchDeliverImportErrorData(MultipartFile file, HttpServletResponse response) {
        Assert.notNull(file, () -> new GlobalException("请上传文件"));

        // 读取Excel，headRowNumber=2 跳过第一行说明行
        List<OrderDeliverExcelDTO> importList;
        try {
            importList = EasyExcel.read(file.getInputStream())
                    .head(OrderDeliverExcelDTO.class)
                    .sheet()
                    .headRowNumber(2)
                    .doReadSync();
        } catch (IOException e) {
            throw new GlobalException("读取Excel文件失败");
        }
        Assert.isTrue(CollUtil.isNotEmpty(importList), () -> new GlobalException("表格数据为空"));

        List<OrderDeliverImportErrorExcelVO> errorList = new ArrayList<>();

        // 按订单号分组，保留行号信息
        Map<String, List<Map.Entry<Integer, OrderDeliverExcelDTO>>> orderGroupMap = new LinkedHashMap<>();
        for (int i = 0; i < importList.size(); i++) {
            OrderDeliverExcelDTO dto = importList.get(i);
            int rowNum = i + 3; // Excel行号从3开始（第1行说明，第2行表头，第3行开始是数据）
            String orderSn = StrUtil.isNotBlank(dto.getOrderSn()) ? dto.getOrderSn().trim() : "";
            orderGroupMap.computeIfAbsent(orderSn, k -> new ArrayList<>()).add(Map.entry(rowNum, dto));
        }

        // 按订单处理
        for (Map.Entry<String, List<Map.Entry<Integer, OrderDeliverExcelDTO>>> orderEntry : orderGroupMap.entrySet()) {
            String orderSn = orderEntry.getKey();
            List<Map.Entry<Integer, OrderDeliverExcelDTO>> rowList = orderEntry.getValue();

            // 取第一行的发货参数作为订单级别的发货信息
            Map.Entry<Integer, OrderDeliverExcelDTO> firstRow = rowList.get(0);
            int firstRowNum = firstRow.getKey();
            OrderDeliverExcelDTO firstDto = firstRow.getValue();

            String shippingMethodName = StrUtil.isNotBlank(firstDto.getShippingMethod()) ? firstDto.getShippingMethod().trim() : firstDto.getShippingMethod();
            String logisticsName = StrUtil.isNotBlank(firstDto.getLogisticsName()) ? firstDto.getLogisticsName().trim() : firstDto.getLogisticsName();
            String trackingNo = StrUtil.isNotBlank(firstDto.getTrackingNo()) ? firstDto.getTrackingNo().trim() : firstDto.getTrackingNo();

            String orderLevelError = null;

            try {
                // 基础校验
                orderLevelError = validateDeliverExcelData(firstDto);
                if (orderLevelError != null) {
                    for (Map.Entry<Integer, OrderDeliverExcelDTO> row : rowList) {
                        OrderDeliverExcelDTO dto = row.getValue();
                        errorList.add(buildDeliverImportErrorRow(row.getKey(), orderSn, dto.getProductName(), dto.getSkuName(),
                                dto.getDeliveryQuantity(), shippingMethodName, logisticsName, trackingNo, orderLevelError));
                    }
                    continue;
                }

                // 查询订单
                Order order = this.lambdaQuery().eq(Order::getOrderSn, orderSn).one();
                if (order == null) {
                    for (Map.Entry<Integer, OrderDeliverExcelDTO> row : rowList) {
                        OrderDeliverExcelDTO dto = row.getValue();
                        errorList.add(buildDeliverImportErrorRow(row.getKey(), orderSn, dto.getProductName(), dto.getSkuName(),
                                dto.getDeliveryQuantity(), shippingMethodName, logisticsName, trackingNo, "订单不存在"));
                    }
                    continue;
                }

                // 检查订单状态
                OrderVO orderVO = this.detail(order.getOrderId());
                if (!orderVO.getAvailableActions().getDeliver()) {
                    for (Map.Entry<Integer, OrderDeliverExcelDTO> row : rowList) {
                        OrderDeliverExcelDTO dto = row.getValue();
                        errorList.add(buildDeliverImportErrorRow(row.getKey(), orderSn, dto.getProductName(), dto.getSkuName(),
                                dto.getDeliveryQuantity(), shippingMethodName, logisticsName, trackingNo, "订单状态不允许发货"));
                    }
                    continue;
                }

                // 解析配送方式
                Integer shippingMethod = parseShippingMethod(shippingMethodName);
                if (shippingMethod == null) {
                    for (Map.Entry<Integer, OrderDeliverExcelDTO> row : rowList) {
                        OrderDeliverExcelDTO dto = row.getValue();
                        errorList.add(buildDeliverImportErrorRow(row.getKey(), orderSn, dto.getProductName(), dto.getSkuName(),
                                dto.getDeliveryQuantity(), shippingMethodName, logisticsName, trackingNo,
                                "无效的配送方式，有效值：物流配送、商家配送、无需配送、电子面单"));
                    }
                    continue;
                }

                // 根据发货方式校验必填项
                String methodValidateResult = validateShippingMethodFields(firstDto, shippingMethod);
                if (methodValidateResult != null) {
                    for (Map.Entry<Integer, OrderDeliverExcelDTO> row : rowList) {
                        OrderDeliverExcelDTO dto = row.getValue();
                        errorList.add(buildDeliverImportErrorRow(row.getKey(), orderSn, dto.getProductName(), dto.getSkuName(),
                                dto.getDeliveryQuantity(), shippingMethodName, logisticsName, trackingNo, methodValidateResult));
                    }
                    continue;
                }

                // 校验物流公司是否存在
                if (shippingMethod == 1 || shippingMethod == 4) {
                    LogisticsCompany logisticsCompany = logisticsCompanyMapper.selectOne(
                            new LambdaQueryWrapper<LogisticsCompany>()
                                    .eq(LogisticsCompany::getLogisticsName, logisticsName)
                                    .last("LIMIT 1")
                    );
                    if (logisticsCompany == null) {
                        for (Map.Entry<Integer, OrderDeliverExcelDTO> row : rowList) {
                            OrderDeliverExcelDTO dto = row.getValue();
                            errorList.add(buildDeliverImportErrorRow(row.getKey(), orderSn, dto.getProductName(), dto.getSkuName(),
                                    dto.getDeliveryQuantity(), shippingMethodName, logisticsName, trackingNo, "物流公司不存在：" + logisticsName));
                        }
                        continue;
                    }
                }

                // 校验每行商品的发货数量和商品匹配
                for (Map.Entry<Integer, OrderDeliverExcelDTO> row : rowList) {
                    OrderDeliverExcelDTO dto = row.getValue();
                    int rowNum = row.getKey();
                    String deliveryQuantity = StrUtil.isNotBlank(dto.getDeliveryQuantity()) ? dto.getDeliveryQuantity().trim() : dto.getDeliveryQuantity();

                    // 校验发货数量格式
                    if (StrUtil.isNotBlank(deliveryQuantity)) {
                        try {
                            int qty = Integer.parseInt(deliveryQuantity);
                            if (qty <= 0) {
                                errorList.add(buildDeliverImportErrorRow(rowNum, orderSn, dto.getProductName(), dto.getSkuName(),
                                        deliveryQuantity, shippingMethodName, logisticsName, trackingNo, "发货数量必须大于0"));
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            errorList.add(buildDeliverImportErrorRow(rowNum, orderSn, dto.getProductName(), dto.getSkuName(),
                                    deliveryQuantity, shippingMethodName, logisticsName, trackingNo, "发货数量格式错误，必须为正整数"));
                            continue;
                        }
                    }

                    // 校验商品是否存在于订单中
                    OrderItemVO matchedItem = matchOrderItem(orderVO.getItems(), dto.getProductName(), dto.getSkuName());
                    if (matchedItem == null) {
                        errorList.add(buildDeliverImportErrorRow(rowNum, orderSn, dto.getProductName(), dto.getSkuName(),
                                deliveryQuantity, shippingMethodName, logisticsName, trackingNo,
                                "未找到匹配的商品：" + dto.getProductName() + (StrUtil.isNotBlank(dto.getSkuName()) ? "(" + dto.getSkuName() + ")" : "")));
                    }
                }
            } catch (Exception e) {
                log.error("批量发货导入校验异常，订单编号：{}，错误：{}", orderSn, e.getMessage(), e);
                for (Map.Entry<Integer, OrderDeliverExcelDTO> row : rowList) {
                    OrderDeliverExcelDTO dto = row.getValue();
                    errorList.add(buildDeliverImportErrorRow(row.getKey(), orderSn, dto.getProductName(), dto.getSkuName(),
                            dto.getDeliveryQuantity(), shippingMethodName, logisticsName, trackingNo, "校验异常：" + e.getMessage()));
                }
            }
        }

        try {
            String fileName = "批量发货导入失败数据.xlsx";
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            EasyExcel.write(response.getOutputStream(), OrderDeliverImportErrorExcelVO.class)
                    .registerWriteHandler(DeliverOrderMergeStrategy.forErrorExcel(errorList))
                    .sheet("批量发货导入失败数据")
                    .doWrite(errorList);
        } catch (Exception e) {
            log.error("导出批量发货导入失败数据失败", e);
            throw new GlobalException("导出失败：" + e.getMessage());
        }
    }

    private OrderDeliverImportErrorExcelVO buildDeliverImportErrorRow(Integer rowNum,
                                                                      String orderSn,
                                                                      String productName,
                                                                      String skuName,
                                                                      String deliveryQuantity,
                                                                      String shippingMethod,
                                                                      String logisticsName,
                                                                      String trackingNo,
                                                                      String errorMsg) {
        return OrderDeliverImportErrorExcelVO.builder()
                .rowNum(rowNum)
                .orderSn(orderSn)
                .productName(productName)
                .skuName(skuName)
                .deliveryQuantity(deliveryQuantity)
                .shippingMethod(shippingMethod)
                .logisticsName(logisticsName)
                .trackingNo(trackingNo)
                .errorMsg(errorMsg)
                .build();
    }

    /**
     * 解析配送方式名称为数字
     */
    private Integer parseShippingMethod(String shippingMethodName) {
        if (StrUtil.isBlank(shippingMethodName)) {
            return null;
        }
        String name = shippingMethodName.trim();
        return switch (name) {
            case "物流配送" -> 1;
            case "商家配送" -> 2;
            case "无需配送" -> 3;
            case "电子面单" -> 4;
            default -> null;
        };
    }

    /**
     * 校验Excel导入数据的基础字段
     */
    private String validateDeliverExcelData(OrderDeliverExcelDTO dto) {
        if (StrUtil.isBlank(dto.getOrderSn())) {
            return "订单编号不能为空";
        }
        if (StrUtil.isBlank(dto.getShippingMethod())) {
            return "配送方式不能为空";
        }
        return null;
    }

    /**
     * 根据发货方式校验必填项
     */
    private String validateShippingMethodFields(OrderDeliverExcelDTO dto, Integer shippingMethod) {
        switch (shippingMethod) {
            case 1: // 物流配送
                if (StrUtil.isBlank(dto.getLogisticsName())) {
                    return "配送方式为物流配送时，物流公司必填";
                }
                if (StrUtil.isBlank(dto.getTrackingNo())) {
                    return "配送方式为物流配送时，物流单号必填";
                }
                break;
            case 4: // 电子面单
                if (StrUtil.isBlank(dto.getLogisticsName())) {
                    return "配送方式为电子面单时，物流公司必填";
                }
                break;
            case 2: // 商家配送
            case 3: // 无需配送
                // 这两种方式不需要额外的必填项
                break;
            default:
                return "无效的配送方式";
        }
        return null;
    }

    @Override
    public void downloadDeliverTemplate(List<Integer> orderIds, HttpServletResponse response) {
        Assert.isTrue(CollUtil.isNotEmpty(orderIds), () -> new GlobalException("请选择要发货的订单"));

        try {
            String fileName = "批量发货模板.xlsx";
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            // 根据订单ID查询订单信息
            List<OrderDeliverExcelDTO> templateData = new ArrayList<>();

            for (Integer orderId : orderIds) {
                OrderVO orderVO = this.detail(orderId);
                if (orderVO == null) {
                    continue;
                }

                // 查询用户信息
                User user = userMapper.selectById(orderVO.getUserId());
                String nickName = user != null ? user.getUsername() : "";

                // 拼接收货地址
                String fullAddress = "";
                if (CollUtil.isNotEmpty(orderVO.getRegionNames())) {
                    fullAddress = String.join(" ", orderVO.getRegionNames()) + " " + (orderVO.getAddress() != null ? orderVO.getAddress() : "");
                }

                // 格式化下单时间
                String addTimeStr = "";
                if (StrUtil.isNotBlank(orderVO.getAddTime())) {
                    try {
                        long addTimeLong = Long.parseLong(orderVO.getAddTime());
                        addTimeStr = DateUtil.format(new Date(addTimeLong * 1000), "yyyy/M/d H:mm");
                    } catch (NumberFormatException e) {
                        addTimeStr = orderVO.getAddTime();
                    }
                }

                // 遍历订单商品，每个商品生成一行
                List<OrderItemVO> items = orderVO.getItems();
                if (CollUtil.isNotEmpty(items)) {
                    for (OrderItemVO item : items) {
                        OrderDeliverExcelDTO dto = OrderDeliverExcelDTO.builder()
                                .orderSn(orderVO.getOrderSn())
                                .shippingMethod("") // 配送方式为空，待用户填写
                                .logisticsName("") // 物流公司为空，待用户填写
                                .logisticsCode("") // 物流代号为空，待用户填写
                                .trackingNo("") // 物流单号为空，待用户填写
                                .username(nickName)
                                .consignee(orderVO.getConsignee())
                                .mobile(orderVO.getMobile())
                                .address(fullAddress.trim())
                                .totalAmount(orderVO.getTotalAmount() != null ? orderVO.getTotalAmount().toString() : "0")
                                .productName(translatePackage.translate(item.getProductName(), 2))
                                .skuName(StringUtils.isNotBlank(item.getSkuValue()) ? item.getSkuValue() : "-")
                                .quantity(String.valueOf(item.getQuantity()))
                                .deliveryQuantity(String.valueOf(item.getQuantity())) // 发货数量默认等于商品数量
                                .addTime(addTimeStr)
                                .build();
                        templateData.add(dto);
                    }
                } else {
                    // 订单没有商品项，只生成一行
                    OrderDeliverExcelDTO dto = OrderDeliverExcelDTO.builder()
                            .orderSn(orderVO.getOrderSn())
                            .shippingMethod("")
                            .logisticsName("")
                            .logisticsCode("")
                            .trackingNo("")
                            .username(nickName)
                            .consignee(orderVO.getConsignee())
                            .mobile(orderVO.getMobile())
                            .address(fullAddress.trim())
                            .totalAmount(orderVO.getTotalAmount() != null ? orderVO.getTotalAmount().toString() : "0")
                            .productName("")
                            .skuName("")
                            .deliveryQuantity("")
                            .quantity("")
                            .addTime(addTimeStr)
                            .build();
                    templateData.add(dto);
                }
            }

            EasyExcel.write(response.getOutputStream(), OrderDeliverExcelDTO.class)
                    .registerWriteHandler(DeliverOrderMergeStrategy.forOrderDeliverExcelWithNotice(
                            templateData,
                            "说明: Excel表批量发货，不支持拆单操作，如需拆单请手动发货",
                            15)) // 15列合并
                    .relativeHeadRowIndex(1) // 第0行是说明，第1行是表头
                    .sheet("批量发货模板")
                    .doWrite(templateData);
        } catch (Exception e) {
            log.error("下载批量发货模板失败", e);
            throw new GlobalException("下载模板失败：" + e.getMessage());
        }
    }
}
