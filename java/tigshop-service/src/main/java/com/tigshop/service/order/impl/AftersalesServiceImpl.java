// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.dto.order.*;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.finance.*;
import com.tigshop.bean.enums.order.*;
import com.tigshop.bean.enums.product.ProductType;
import com.tigshop.bean.enums.promotion.JoinStatusEnum;
import com.tigshop.bean.enums.promotion.JoinTypeEnum;
import com.tigshop.bean.model.promotion.GrouponPromotionRecord;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.*;
import com.tigshop.bean.model.order.*;
import com.tigshop.bean.model.content.WechatLiveOrderRel;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserCoupon;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.param.finance.refundapply.RefundApplyAuditParam;
import com.tigshop.bean.param.finance.statement.StatementOutputSaveParam;
import com.tigshop.bean.param.finance.statement.StatementSaveParam;
import com.tigshop.bean.param.order.AftersalesApplyParam;
import com.tigshop.bean.param.order.AftersalesEditParam;
import com.tigshop.bean.query.order.AftersalesListPageQuery;
import com.tigshop.bean.vo.finance.refundapply.RefundApplyDetailVO;
import com.tigshop.bean.vo.order.*;
import com.tigshop.bean.vo.setting.config.ShippingSettingsVO;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.*;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.mapper.finance.PaylogMapper;
import com.tigshop.mapper.finance.RefundApplyMapper;
import com.tigshop.mapper.order.AftersalesLogMapper;
import com.tigshop.mapper.order.AftersalesMapper;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.user.UserCouponMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.mapper.vendor.VendorMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.*;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.order.*;
import com.tigshop.service.promotion.GrouponPromotionRecordService;
import com.tigshop.service.content.WechatLiveOrderRelService;
import com.tigshop.service.content.WechatLiveOrderSyncService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.AfterSalesReasonConfigService;
import com.tigshop.service.setting.GalleryPicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.bean.enums.order.AftersalesStatusEnum.*;
import static com.tigshop.common.constant.content.WechatLiveOrderSyncConstants.EVENT_AFTERSALE_CREATED;
import static com.tigshop.common.constant.content.WechatLiveOrderSyncConstants.EVENT_AFTERSALE_UPDATED;
import static com.tigshop.common.constant.Constants.DATE_FORMAT;

/**
 * 软件许可服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AftersalesServiceImpl extends BaseServiceImpl<AftersalesMapper, Aftersales> implements AftersalesService {

    /**
     * 协商记录文案：供应商已关闭售后审核，供应商审核环节由系统代为通过。
     * 以「系统自动审核通过」开头，可被 AftersalesLogServiceImpl 识别为系统日志并以系统管理员身份展示。
     */
    private static final String VENDOR_AUDIT_SKIPPED_LOG = "系统自动审核通过（供应商售后审核已关闭，跳过供应商审核环节）";

    /** 协商记录文案：平台侧自动审核通过，仍需供应商人工审核 */
    private static final String AUTO_APPROVE_TO_VENDOR_LOG = "系统自动审核通过，已转供应商审核";

    private final RefundApplyMapper refundApplyMapper;

    private final AdminLogService adminLogService;

    private final AftersalesLogService aftersalesLogService;

    private final OrderLogService orderLogService;

    private final OrderMapper orderMapper;

    private final ShopMapper shopMapper;

    private final VendorMapper vendorMapper;

    private final AftersalesItemService aftersalesItemService;

    private final OrderItemMapper orderItemMapper;

    private final UserMapper userMapper;

    private final AftersalesLogMapper aftersalesLogMapper;

    private final AdminMsgService adminMsgService;

    private final TranslatePackageImpl translatePackage;
    private final MessageHelper messageHelper;

    private final ConfigService configService;

    private final AfterSalesReasonConfigService afterSalesReasonConfigService;

    private final RabbitTemplate rabbitTemplate;

    private final OrderItemService orderItemService;

    private final TigshopProperties tigshopProperties;
    private final StatementOutputService statementOutputService;
    private final PaylogMapper paylogMapper;
    private final RecordRateService recordRateService;
    private final ObjectProvider<StatementService> statementService;
    private final WechatLiveOrderRelService wechatLiveOrderRelService;
    private final WechatLiveOrderSyncService wechatLiveOrderSyncService;
    private final CreditOrderService creditOrderService;
    private final CreditUserService creditUserService;
    private final UserCouponMapper userCouponMapper;

    private final GalleryPicService galleryPicService;

    @Autowired
    private ObjectProvider<RefundApplyService> refundApplyService;
    @Autowired
    private OrderReservationService orderReservationService;
    @Autowired
    @Lazy
    private GrouponPromotionRecordService grouponPromotionRecordService;

    @Override
    public Page<AftersalesVO> list(AftersalesListPageQuery pageQuery) {
        // 分页
        Page<Aftersales> page = buildSortOrder(pageQuery);

        Integer aftersaleType = pageQuery.getAftersaleType();
        boolean aftersalesTypeFlag = aftersaleType != null && (
                aftersaleType.equals(AftersalesTypeEnum.RETURN.getCode()) || aftersaleType.equals(AftersalesTypeEnum.PAYRETURN.getCode())
        );

        Integer vendorId = this.getVendorId();
        boolean vendorFlag = vendorId != null && vendorId > 0;

        boolean statusFlag = pageQuery.getStatus() != null && pageQuery.getStatus() > 0;

        Integer shopId = HeaderUtils.getShopId();
        boolean shopFlag = shopId != null && shopId > 0;

        List<Integer> searchOrderIds = new ArrayList<>();
        String keyword = pageQuery.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            List<Order> orderList = orderMapper.selectList(new LambdaQueryWrapper<Order>().like(Order::getOrderSn, keyword));
            searchOrderIds = orderList.stream()
                    .map(Order::getOrderId)
                    .toList();
            if (CollUtil.isEmpty(searchOrderIds)) {
                return PageUtil.convertPage(new Page<Aftersales>(page.getCurrent(), page.getSize(), 0), Collections.emptyList());
            }
        }
        String outTradeNo = pageQuery.getOutTradeNo();
        if (StringUtils.isNotEmpty(outTradeNo)) {
            List<Integer> outTradeOrderIds = orderMapper.selectList(new LambdaQueryWrapper<Order>().like(Order::getOutTradeNo, outTradeNo))
                    .stream()
                    .map(Order::getOrderId)
                    .toList();
            if (CollUtil.isEmpty(outTradeOrderIds)) {
                return PageUtil.convertPage(new Page<Aftersales>(page.getCurrent(), page.getSize(), 0), Collections.emptyList());
            }
            searchOrderIds = CollUtil.isEmpty(searchOrderIds)
                    ? outTradeOrderIds
                    : searchOrderIds.stream().filter(outTradeOrderIds::contains).toList();
            if (CollUtil.isEmpty(searchOrderIds)) {
                return PageUtil.convertPage(new Page<Aftersales>(page.getCurrent(), page.getSize(), 0), Collections.emptyList());
            }
        }
        boolean filterByLive = pageQuery.getWechatLiveId() != null
                || pageQuery.getWechatLiveRoomId() != null
                || pageQuery.getWechatGoodsId() != null
                || StrUtil.isNotBlank(pageQuery.getLiveShareOpenId());
        if (filterByLive) {
            List<Integer> liveOrderIds = wechatLiveOrderRelService.list(Wrappers.lambdaQuery(WechatLiveOrderRel.class)
                            .eq(pageQuery.getWechatLiveId() != null, WechatLiveOrderRel::getWechatLiveId, pageQuery.getWechatLiveId())
                            .eq(pageQuery.getWechatLiveRoomId() != null, WechatLiveOrderRel::getRoomId, pageQuery.getWechatLiveRoomId())
                            .eq(pageQuery.getWechatGoodsId() != null, WechatLiveOrderRel::getWechatGoodsId, pageQuery.getWechatGoodsId())
                            .eq(StrUtil.isNotBlank(pageQuery.getLiveShareOpenId()), WechatLiveOrderRel::getShareOpenId, pageQuery.getLiveShareOpenId()))
                    .stream()
                    .map(WechatLiveOrderRel::getOrderId)
                    .distinct()
                    .toList();
            if (CollUtil.isEmpty(liveOrderIds)) {
                return PageUtil.convertPage(new Page<Aftersales>(page.getCurrent(), page.getSize(), 0), Collections.emptyList());
            }
            searchOrderIds = CollUtil.isEmpty(searchOrderIds)
                    ? liveOrderIds
                    : searchOrderIds.stream().filter(liveOrderIds::contains).toList();
            if (CollUtil.isEmpty(searchOrderIds)) {
                return PageUtil.convertPage(new Page<Aftersales>(page.getCurrent(), page.getSize(), 0), Collections.emptyList());
            }
        }
        Integer status = pageQuery.getStatus();
        List<Integer> vendorStatuses = Arrays.asList(21, 4, 22, 6);
        boolean isVendorAdmin = false;
        if (getAdminType() != null) {
            isVendorAdmin = AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.VENDOR;
        }
        Page<Aftersales> aftersalesPage = this.lambdaQuery()
                .eq(aftersalesTypeFlag, Aftersales::getAftersaleType, aftersaleType)
                .eq(shopFlag, Aftersales::getShopId, shopId)
                .eq(ObjectUtil.isNotEmpty(pageQuery.getUserId()), Aftersales::getUserId, pageQuery.getUserId())
                .eq(vendorFlag, Aftersales::getVendorId, vendorId)
                .like(StrUtil.isNotBlank(pageQuery.getAftersalesSn()), Aftersales::getAftersalesSn, pageQuery.getAftersalesSn())
                .in(CollUtil.isNotEmpty(searchOrderIds), Aftersales::getOrderId, searchOrderIds)
                // 普通后台按 status 查询
                .eq(!isVendorAdmin && statusFlag, Aftersales::getStatus, status)
                // 供应商后台没传 status => 默认 21,4,22,6
                .in(isVendorAdmin && status == null, Aftersales::getStatus, vendorStatuses)
                // 供应商后台传了 status => 必须在 21,4,22,6 内
                .eq(isVendorAdmin && statusFlag && vendorStatuses.contains(status), Aftersales::getStatus, status)
                // 供应商后台传了非法 status => 查不到数据
                .eq(isVendorAdmin && statusFlag && !vendorStatuses.contains(status), Aftersales::getStatus, -999)
                .page(page);
        List<Aftersales> aftersalesPageRecords = aftersalesPage.getRecords();

        //循环records获得售后ID
        List<Integer> aftersaleIds = aftersalesPageRecords.stream()
                .map(Aftersales::getAftersaleId)
                .toList();
        //通过Ids查询aftersalesItem
        List<AftersalesItemVO> itemList = aftersalesItemService.getItemByIds(aftersaleIds, false);
        Map<Integer, List<AftersalesItemVO>> itemMap = itemList.stream().collect(Collectors.groupingBy(AftersalesItemVO::getAftersaleId));
        //获取订单id
        List<Integer> orderIds = aftersalesPageRecords.stream()
                .map(Aftersales::getOrderId)
                .toList();
        Map<Integer, Order> orderMap;
        if (!orderIds.isEmpty()) {
            List<Order> orderList = orderMapper.selectBatchIds(orderIds);
            orderMap = orderList.stream().collect(Collectors.toMap(Order::getOrderId, Function.identity()));
        } else {
            orderMap = new HashMap<>();
        }

        // 本页中已置「已完成」但平台财务退款未处理完的售后，列表状态改按「待财务退款」展示
        Set<Integer> pendingFinanceRefundIds = findPendingFinanceRefundIds(aftersalesPageRecords);
        // 平台财务已拒绝的退款申请同样不能沿用售后主单的「已完成」展示。
        Set<Integer> financeRejectedRefundIds = findFinanceRejectedRefundIds(aftersalesPageRecords);
        boolean isAdminEnd = getAdminType() != null;

        // 转换为VO
        List<AftersalesVO> aftersalesVOList = aftersalesPageRecords.stream()
                .map(aftersales -> {
                    AftersalesVO aftersalesVO = new AftersalesVO();
                    BeanUtils.copyProperties(aftersales, aftersalesVO);

                    aftersalesVO.setAftersalesTypeName(AftersalesTypeEnum.getTypeName(aftersales.getAftersaleType()));
                    aftersalesVO.setStatusName(resolveListStatusName(aftersales,
                            pendingFinanceRefundIds.contains(aftersales.getAftersaleId()),
                            financeRejectedRefundIds.contains(aftersales.getAftersaleId()), isAdminEnd));

                    if (aftersales.getAddTime() > 0) {
                        DateTime date = DateUtil.date(aftersales.getAddTime() * 1000);
                        aftersalesVO.setAddTime(DateUtil.format(date, DATE_FORMAT));
                    } else {
                        aftersalesVO.setAddTime("");
                    }

                    Order orderForRow = orderMap.get(aftersales.getOrderId());
                    if (orderForRow != null) {
                        aftersalesVO.setOrderSn(orderForRow.getOrderSn());
                        // 表头「订单详情」「标记」入口回显
                        aftersalesVO.setMark(orderForRow.getMark());
                        aftersalesVO.setAdminNote(orderForRow.getAdminNote());
                    } else {
                        aftersalesVO.setOrderSn("");
                    }

                    List<AftersalesItemVO> rowItems = itemMap.get(aftersales.getAftersaleId());
                    aftersalesVO.setAftersalesItems(rowItems);

                    // 退款金额（ConfirmReceive 退款明细展示）
                    Integer rowStatus = aftersales.getStatus();
                    aftersalesVO.setRefundAmount(Optional.ofNullable(aftersales.getRefundAmount()).orElse(BigDecimal.ZERO).toString());
                    if (rowStatus != null
                            && (IN_REVIEW.getCode() == rowStatus || REFUSE.getCode() == rowStatus || CANCEL.getCode() == rowStatus)) {
                        aftersalesVO.setActualRefundAmount(BigDecimal.ZERO);
                    } else {
                        aftersalesVO.setActualRefundAmount(Optional.ofNullable(aftersales.getRefundAmount()).orElse(BigDecimal.ZERO));
                    }

                    // 建议退款金额：仅待处理/待供应商审核行需要（内联「同意退款」金额上限校验），避免整页 N+1
                    if (orderForRow != null && rowStatus != null
                            && (IN_REVIEW.getCode() == rowStatus || WAIT_FOR_SUPPLIER_AUDIT.getCode() == rowStatus)) {
                        BigDecimal suggest = calcRefundProductAmount(orderForRow,
                                rowItems == null ? Collections.emptyList() : rowItems);
                        aftersalesVO.setSuggestRefundAmount(calculateFinalRefundAmount(orderForRow, aftersales, suggest, true));
                    }

                    return aftersalesVO;
                }).toList();
        return PageUtil.convertPage(aftersalesPage, aftersalesVOList);
    }

    @Override
    public AftersalesVO detail(Integer id) {
        Aftersales aftersales = this.getById(id);
        AftersalesVO aftersalesVO = new AftersalesVO();
        BeanUtils.copyProperties(aftersales, aftersalesVO);
        if (Objects.equals(aftersales.getAftersaleType(), AftersalesTypeEnum.RETURN.getCode())
                && StrUtil.isBlank(aftersalesVO.getReturnAddress())) {
            aftersalesVO.setReturnAddress(resolveReturnAddressForDetail(aftersales));
        }
        aftersalesVO.setRefundAmount(Optional.ofNullable(aftersales.getRefundAmount()).orElse(BigDecimal.ZERO).toString());
        Integer status = aftersalesVO.getStatus();
        //如果是未审核状态,实际退款金额设置为0
        if (IN_REVIEW.getCode() == status || REFUSE.getCode() == status || CANCEL.getCode() == status) {
            aftersalesVO.setActualRefundAmount(BigDecimal.ZERO);
        } else {
            aftersalesVO.setActualRefundAmount(Optional.ofNullable(aftersales.getRefundAmount()).orElse(BigDecimal.ZERO));
        }
        aftersalesVO.setAftersalesTypeName(AftersalesTypeEnum.getTypeName(aftersales.getAftersaleType()));
        // 后台售后详情状态头：按 类型/状态/供应商/关闭子类型 区分文案；仅退款财务驳回由关联 RefundApply 推导
        String financeRejectReason = getFinanceRejectReason(aftersales);
        boolean financeRejected = financeRejectReason != null;
        Integer statusForDisplay = resolveDisplayStatus(aftersales);
        aftersalesVO.setStatusName(AftersalesStatusEnum.getAdminStatusName(
                aftersales.getAftersaleType(), statusForDisplay, aftersales.getVendorId(), financeRejected));
        aftersalesVO.setStatusDescription(AftersalesStatusEnum.getAdminStatusDescription(
                aftersales.getAftersaleType(), statusForDisplay, aftersales.getVendorId(),
                aftersales.getCloseType(), financeRejected ? financeRejectReason : aftersales.getReply(), financeRejected));
        // 带倒计时状态（待审核/待供应商审核）返回自动同意截止时间 + 服务端时间，副文案置空由前端渲染倒计时
        aftersalesVO.setAutoAgreeEndTime(resolveAutoAgreeEndTime(aftersales));
        aftersalesVO.setServerTime(TigUtils.handelTime(System.currentTimeMillis() / 1000));

        DateTime date = DateUtil.date(aftersales.getAddTime() * 1000);
        aftersalesVO.setAddTime(DateUtil.format(date, DATE_FORMAT));

        List<Integer> aftersaleIds = Collections.singletonList(aftersales.getAftersaleId());
        List<AftersalesItemVO> itemList = aftersalesItemService.getItemByIds(aftersaleIds, false);
        Map<Integer, List<AftersalesItemVO>> itemMap = itemList.stream().collect(Collectors.groupingBy(AftersalesItemVO::getAftersaleId));
        aftersalesVO.setAftersalesItems(itemMap.get(aftersales.getAftersaleId()));

        Order order = orderMapper.selectById(aftersales.getOrderId());
        if (order != null) {
            // 表头「订单详情」「标记」入口回显
            aftersalesVO.setMark(order.getMark());
            aftersalesVO.setAdminNote(order.getAdminNote());
        }
        Long orderItemCount = orderItemMapper.selectCount(new QueryWrapper<OrderItem>().eq("order_id", order.getOrderId()));
        BigDecimal suggestRefundAmount = calcRefundProductAmount(order, itemList);
        //如果售后订单总金额大于或等于优惠卷金额就进行最大退款金额扣减
//        suggestRefundAmount = suggestRefundAmount.compareTo(order.getCouponAmount()) >= 0 ? suggestRefundAmount.subtract(order.getCouponAmount()) : suggestRefundAmount;
        aftersalesVO.setSuggestRefundAmount(calculateFinalRefundAmount(order, aftersales, suggestRefundAmount, true));

        //【后台】售后订单退款的金额有误
        //https://e.gitee.com/baixiaoanyaoye/projects/767226/sprints/315201/issues/table?issue=IDCB5Q
        //如果是整单售后的话,退款金额直接用订单金额
        // 获取订单项数量Map
        List<OrderItem> orderItems = orderItemMapper.selectList(new QueryWrapper<OrderItem>().eq("order_id", order.getOrderId()));
        Map<Integer, Integer> orderItemQuantityMap = orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getItemId, OrderItem::getQuantity));
        // 获取售后项数量Map
        Map<Integer, Integer> aftersalesItemQuantityMap = itemList.stream()
                .collect(Collectors.toMap(AftersalesItemVO::getOrderItemId, AftersalesItemVO::getNumber, Integer::sum));
        // 判断是否整单售后：订单项数量一致 且 每个商品的售后数量等于购买数量
        boolean isFullOrderRefund = orderItemCount == itemList.size() 
                && orderItemQuantityMap.entrySet().stream()
                        .allMatch(entry -> Objects.equals(entry.getValue(), aftersalesItemQuantityMap.get(entry.getKey())));
        BigDecimal refundTaxAmount = itemList.stream()
                .map(item -> Optional.ofNullable(item.getTaxTotalAmount()).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.CEILING);
        if (isFullOrderRefund) {
            //整单售后（订单总价已含税，无需再摊税费）
            refundTaxAmount = Optional.ofNullable(order.getTaxRateAmount()).orElse(BigDecimal.ZERO).setScale(2, RoundingMode.CEILING);
            Integer orderStatus = order.getOrderStatus();
            if (orderStatus >= OrderStatusEnum.PROCESSING.getCode()) {
                //已发货不退运费
                aftersalesVO.setSuggestRefundAmount(order.getTotalAmount().subtract(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO));
            } else {
                //未发货退运费
                aftersalesVO.setSuggestRefundAmount(order.getTotalAmount());
            }
        } else {
            // 部分售后：基础金额为商品金额
            BigDecimal partialRefundAmount = suggestRefundAmount;
            BigDecimal shippingFee = Optional.ofNullable(order.getShippingFee()).orElse(BigDecimal.ZERO);
            if (shippingFee.compareTo(BigDecimal.ZERO) > 0
                    && isCompletingLastItems(order, aftersales)
                    && !isShippingAlreadyRefunded(order, aftersales, partialRefundAmount)) {
                boolean shouldAddShipping = false;
                if (ShippingStatusEnum.PENDING.getCode() == order.getShippingStatus()) {
                    shouldAddShipping = true;
                } else if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus()
                        && ShippingStatusEnum.SHIPPED.getCode() == order.getShippingStatus()
                        && (order.getShippingTime() == null || order.getShippingTime() <= 0)) {
                    shouldAddShipping = true;
                }
                if (shouldAddShipping) {
                    partialRefundAmount = partialRefundAmount.add(shippingFee);
                }
            }
            aftersalesVO.setSuggestRefundAmount(partialRefundAmount);
        }
        aftersalesVO.setRefundTaxAmount(refundTaxAmount);

        List<AftersalesLogVO> aftersalesLogVOList = aftersalesLogService.getDetailLog(aftersales.getAftersaleId());
        if (CollUtil.isNotEmpty(aftersalesLogVOList)) {
            aftersalesVO.setAftersalesLog(aftersalesLogVOList);
        }

        return aftersalesVO;
    }

    @Override
    public boolean create(AftersalesApplyParam createDTO) {
        if (createDTO != null) {
            Aftersales aftersales = new Aftersales();
            BeanUtils.copyProperties(createDTO, aftersales);
            boolean saved = this.save(aftersales);
            if (saved) {
                try{
                    wechatLiveOrderSyncService.onAftersaleChanged(aftersales, EVENT_AFTERSALE_CREATED);
                }catch (Exception e){

                }
            }
            return saved;
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(AftersalesEditParam param) {
        // 1. 查询售后
        Aftersales aftersales = this.getById(param.getId());
        Assert.notNull(aftersales, () -> new GlobalException("售后信息不存在"));
        Assert.isTrue(IN_REVIEW.getCode() == aftersales.getStatus() || WAIT_FOR_SUPPLIER_AUDIT.getCode() == aftersales.getStatus(), () -> new GlobalException("该售后状态不能操作"));

        // 2. 校验参数
        param.validParam(aftersales);

        // 审核通过日志是否已在退款处理前写入（仅退款分支），避免方法末尾重复写入
        boolean approvedLogWritten = false;
        // 供应商订单退货退款，本次审核通过后是否由系统代为通过供应商审核环节（供应商已关闭售后审核）
        boolean vendorAuditSkipped = false;
        int itemFlag;
        // 审核通过
        if (param.getStatus() == APPROVED_FOR_PROCESSING.getCode()) {
            // 售后商品数量校验
            itemFlag = checkAfterSaleProduct(aftersales.getAftersaleId(), aftersales.getOrderId());
            Order order = orderMapper.selectById(aftersales.getOrderId());

            boolean vendorReviewRequired = isVendorAftersalesReviewRequired(aftersales);
            vendorAuditSkipped = !vendorReviewRequired
                    && isVendorReturnAftersales(aftersales)
                    && IN_REVIEW.getCode() == aftersales.getStatus();

            // 3. 更新售后（按退款商品金额占比重算退款金额，含分摊税费）
            Aftersales updateAftersales = param.createUpdateAftersales(aftersales, vendorReviewRequired);

            // 供应商已关闭售后审核：本次审核通过直接进入「待回寄」，退货地址不会有人再填，
            // 按供应商 → 店铺 → 平台的收货信息自动回填，避免买家拿不到回寄地址
            if (vendorAuditSkipped && StrUtil.isBlank(updateAftersales.getReturnAddress())) {
                String autoReturnAddress = resolveReturnAddress(aftersales);
                updateAftersales.setReturnAddress(autoReturnAddress);
                updateAftersales.setReturnGoodsTip(
                        "您的售后申请已通过，请按以下地址回寄商品，并在回寄后填写物流信息：" + autoReturnAddress);
            }

            Order orderForRefund = orderMapper.selectById(aftersales.getOrderId());
            if (orderForRefund != null) {
                List<AftersalesItemVO> aftersalesItems = aftersalesItemService.getItemByIds(Collections.singletonList(aftersales.getAftersaleId()), false);
                BigDecimal refundProductAmount = calcRefundProductAmount(orderForRefund, aftersalesItems);
                if (refundProductAmount.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal adminRefundAmount = updateAftersales.getRefundAmount();
                    // 仅在管理员未填写时兜底：0 是合法输入（如积分全额兑换订单实收为 0），不能当作未填写
                    if (adminRefundAmount == null) {
                        updateAftersales.setRefundAmount(refundProductAmount);
                    }
                }
            }
            this.updateById(updateAftersales);
            aftersales = this.getById(param.getId());

            // 退货退款供应商订单：审核通过后流转至"待供应商审核"，此时售后才进入供应商可见列表，
            // 在此时机才通知供应商（消息中心 + 语音播报），与列表可见性保持一致。
            // 供应商已关闭售后审核时直接流转至"待回寄"，同样属供应商可见状态，也需在此通知。
            if (order != null && order.getVendorId() != null && order.getVendorId() > 0
                    && (WAIT_FOR_SUPPLIER_AUDIT.getCode() == aftersales.getStatus() || vendorAuditSkipped)) {
                notifyVendorAftersale(order, aftersales);
            }

            // 全退：审核通过不改订单状态，订单维持审核前状态（未发货仍"待发货"、已发货仍"已发货"），
            // 待财务退款到账后由 RefundApplyServiceImpl.handleRefundPostProcess 统一置为已取消
            if (itemFlag == 1) {
                if (order != null) {

                }
            }

            // 如果该售后订单为仅退款
            if (aftersales.getAftersaleType().equals(AftersalesTypeEnum.PAYRETURN.getCode())) {
                if (!Objects.equals(aftersales.getStatus(), COMPLETE.getCode())) {
                    adminLogService.createByLogInfo("更新退换货:" + param.getId());
                    if ("系统自动审核通过".equals(param.getReply())) {
                        aftersalesLogService.addSystemAftersaleLog(param.getId(),
                                buildAuditLogInfo(param),
                                aftersales.getShopId(), 0);
                    } else {
                        aftersalesLogService.addAftersaleLog(param.getId(),
                                buildAuditLogInfo(param),
                                aftersales.getShopId(), aftersales.getVendorId());
                    }
                    return;
                }

                boolean isCreditPaymentOrder = false;
                CreditOrder creditOrder = creditOrderService.getByOrderId(aftersales.getOrderId());
                isCreditPaymentOrder = creditOrder != null;

                // 先写"售后审核通过"日志，确保排在"财务退款"日志之前（与售后审核在前、财务审核在后的业务顺序一致）
                addAuditResultLog(param, aftersales);
                approvedLogWritten = true;

                createRefundApplyAndHandleFinance(aftersales, isCreditPaymentOrder ? "账期支付退款申请" : "售后申请通过");
                restoreOrderCouponUsageIfFullAftersales(aftersales, itemFlag == 1);

                // 下单出账，添加流水
                if (updateAftersales.getRefundAmount().compareTo(BigDecimal.ZERO) > 0) {
                    saveAftersalesStatementOutput(aftersales);
                    // 对账单
                    StatementOutput statementOutput = statementOutputService.lambdaQuery()
                            .eq(StatementOutput::getRecordId, aftersales.getOrderId())
                            .eq(StatementOutput::getRecordType, StatementType.ORDER.getCode())
                            .last("LIMIT 1")
                            .one();
                    if (statementOutput != null) {
                        Integer realSettlement = statementOutput.getRealSettlement();
                        if (realSettlement > 0) {
                            this.generateStatement(aftersales);
                        }
                    }

                    // 非账期支付订单：如果为已结算订单，退款时直接扣减账户余额
                    if (!isCreditPaymentOrder) {
                        Aftersales finalAftersales = aftersales;
                        TransactionAfterCommitExecutor.execute(() -> {
                            rabbitTemplate.convertAndSend(
                                    RabbitMQConfig.DIRECT_EXCHANGE,
                                    RabbitMQConfig.REFUND_SETTLEMENT_ROUTING_KEY,
                                    finalAftersales.getAftersaleId());
                        });
                    }
                }



                // 拼团订单：团长仅退款审核通过 → 整团解散，为其他团员自动创建售后
                if (order != null && OrderType.PIN.getCode() == order.getOrderType()) {
                    grouponPromotionRecordService.dissolveGroupByCaptainRefund(order.getOrderId());
                }
            }
        }

        // 审核未通过
        if (param.getStatus() == REFUSE.getCode()) {
            aftersales.setStatus(param.getStatus());
            aftersales.setReply(param.getReply());
            // 关闭子类型=审核驳回，驱动前后台「退货退款申请已被拒绝/退款拒绝」文案，区别于拒收
            aftersales.setCloseType(CLOSE_TYPE_AUDIT_REJECT);
            this.updateById(aftersales);

            // 售后驳回：若该订单没有其他“处理中”的售后，则恢复账期订单为待结算
            try {
                CreditOrder creditOrder = creditOrderService.getByOrderId(aftersales.getOrderId());
                if (creditOrder != null
                        && CreditOrderSettlementStatusEnum.REFUNDING.getCode().equals(creditOrder.getSettlementStatus())) {
                    long processingCount = this.lambdaQuery()
                            .eq(Aftersales::getOrderId, aftersales.getOrderId())
                            .in(Aftersales::getStatus, IN_REVIEW.getCode(),
                                    APPROVED_FOR_PROCESSING.getCode(),
                                    SEND_BACK.getCode(),
                                    RETURNED.getCode(),
                                    WAIT_FOR_SUPPLIER_AUDIT.getCode(),
                                    WAIT_FOR_SUPPLIER_RECEIPT.getCode())
                            .count();
                    if (processingCount <= 0) {
                        CreditOrder creditOrderUpdate = new CreditOrder();
                        creditOrderUpdate.setCreditOrderId(creditOrder.getCreditOrderId());
                        creditOrderUpdate.setSettlementStatus(CreditOrderSettlementStatusEnum.UNSETTLED.getCode());
                        creditOrderUpdate.setUpdateTime(StringUtils.getCurrentTime());
                        creditOrderService.updateById(creditOrderUpdate);
                    }
                }
            } catch (Exception e) {
                // 不阻断售后驳回流程
            }
        }

        adminLogService.createByLogInfo("更新退换货:" + param.getId());
        Aftersales latestAftersales = this.getById(param.getId());
        try{
            wechatLiveOrderSyncService.onAftersaleChanged(latestAftersales, EVENT_AFTERSALE_UPDATED);
        }catch (Exception e){

        }
        if (!approvedLogWritten) {
            addAuditResultLog(param, aftersales);
        }
        // 供应商审核环节被系统代为通过时留痕，避免该环节在售后详情的协商记录里凭空消失
        if (vendorAuditSkipped) {
            addVendorAuditSkippedLog(param.getId(), aftersales);
        }
    }

    /**
     * 供应商审核环节被系统代为通过的留痕。该环节的责任方是供应商，故归属供应商而非平台。
     */
    private void addVendorAuditSkippedLog(Integer aftersalesId, Aftersales aftersales) {
        aftersalesLogService.addSystemAftersaleLog(aftersalesId, VENDOR_AUDIT_SKIPPED_LOG,
                0, aftersales.getVendorId());
    }

    /**
     * 是否为供应商订单的退货退款售后。仅退款不经供应商审核，故不计入。
     */
    private boolean isVendorReturnAftersales(Aftersales aftersales) {
        if (aftersales == null
                || !Objects.equals(AftersalesTypeEnum.RETURN.getCode(), aftersales.getAftersaleType())) {
            return false;
        }
        if (!Objects.equals(tigshopProperties.getIsVendor(), 1)) {
            return false;
        }
        Integer vendorId = aftersales.getVendorId();
        return vendorId != null && vendorId > 0;
    }

    /**
     * 该售后是否还需经供应商人工审核。
     * 由供应商自身的「售后审核」开关（vendor.after_sales_review）独立决定，与平台的
     * 「售后审核」开关（config.afterSalesReview）无关：平台开关只管平台/店铺那一环。
     * 开关未设置时按关闭处理，与存量供应商的既有行为一致。
     */
    private boolean isVendorAftersalesReviewRequired(Aftersales aftersales) {
        if (!isVendorReturnAftersales(aftersales)) {
            return false;
        }
        Vendor vendor = vendorMapper.selectOnboardedById(aftersales.getVendorId());
        return vendor != null && Objects.equals(vendor.getAfterSalesReview(), 1);
    }

    /**
     * 写入售后审核结果日志（审核通过/驳回）。
     * 仅退款分支会在执行财务退款前提前调用，保证"售后审核"日志排在"财务退款"日志之前。
     */
    /**
     * 拼装售后审核结果日志文案。审核备注（reply）为空时仅返回状态名，避免出现 "审核通过:null"。
     */
    private String buildAuditLogInfo(AftersalesEditParam param) {
        String statusName = getStatusName(param.getStatus());
        return StrUtil.isBlank(param.getReply()) ? statusName : statusName + ":" + param.getReply();
    }

    private void addAuditResultLog(AftersalesEditParam param, Aftersales aftersales) {
        if ("系统自动审核通过".equals(param.getReply())) {
            // 售后审核环节的责任方是店铺（自营时 shopId 为 0，前端显示为平台），与供应商环节无关
            aftersalesLogService.addSystemAftersaleLog(param.getId(),
                    buildAuditLogInfo(param),
                    aftersales.getShopId(), 0);
        } else {
            aftersalesLogService.addAftersaleLog(param.getId(),
                    buildAuditLogInfo(param),
                    aftersales.getShopId(), aftersales.getVendorId());
        }
    }




    /**
     * 售后商品数量校验
     *
     * @param aftersalesId 售后订单id
     * @param orderId      订单id
     * @return int
     */
    public int checkAfterSaleProduct(int aftersalesId, int orderId) {
        return isOrderItemsFullyCovered(orderId, validStatus()) ? 1 : 0;
    }

    /**
     * 订单全部商品数量是否被指定状态的售后完全覆盖
     *
     * @param orderId          订单id
     * @param aftersalesStatus 参与统计的售后状态
     * @return boolean
     */
    private boolean isOrderItemsFullyCovered(int orderId, List<Integer> aftersalesStatus) {
        List<OrderItem> orderItemList = orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, orderId)
                .list();
        if (CollUtil.isEmpty(orderItemList)) {
            return false;
        }

        Map<Integer, Integer> orderItems = orderItemList.stream()
                .collect(Collectors.toMap(OrderItem::getItemId, OrderItem::getQuantity));

        int validSum = aftersalesItemService.lambdaQuery()
                .inSql(AftersalesItem::getAftersaleId,
                        "SELECT aftersale_id FROM aftersales WHERE order_id = " + orderId + " AND status IN (" +
                                aftersalesStatus.stream().map(String::valueOf).collect(Collectors.joining(",")) +
                                ")")
                .in(AftersalesItem::getOrderItemId, orderItemList.stream().map(OrderItem::getItemId).toList())
                .list()
                .stream()
                .mapToInt(AftersalesItem::getNumber)
                .sum();

        return validSum == orderItems.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public boolean hasInProgressFullOrderAftersales(Integer orderId) {
        if (orderId == null) {
            return false;
        }
        Order order = orderMapper.selectById(orderId);
        if (order == null || Objects.equals(order.getOrderStatus(), OrderStatusEnum.CANCELLED.getCode())) {
            return false;
        }
        return isOrderItemsFullyCovered(orderId, validStatus());
    }

    @Override
    public boolean isFullOrderAftersalesCompleted(Integer orderId) {
        if (orderId == null) {
            return false;
        }
        return isOrderItemsFullyCovered(orderId, List.of(COMPLETE.getCode()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receive(Integer id) {
        return completeReturnAftersales(id, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectReceive(Integer id, String reply) {
        Aftersales aftersales = this.getById(id);
        if (aftersales == null) {
            throw new GlobalException("售后信息不存在");
        }

        // 状态门：与确认收货同节点、同角色（商家取 5，供应商取 22）
        Integer status = aftersales.getStatus();
        boolean canReject;
        Integer isVendor = tigshopProperties.getIsVendor();
        if (isVendor == 1 && (aftersales.getVendorId() != null && aftersales.getVendorId() > 0)) {
            canReject = Objects.equals(status, WAIT_FOR_SUPPLIER_RECEIPT.getCode());
        } else {
            canReject = Objects.equals(status, RETURNED.getCode());
        }
        if (!canReject) {
            throw new GlobalException("该售后状态不能操作");
        }

        // 拒收：售后置为已拒绝（待关闭），不发起任何退款/结算，货品由买卖双方线下协商处理。
        // reply 作为拒绝原因，买家前台在“已拒绝”态直接展示；商家后续可走 complete 关闭为已取消。
        Aftersales updateAftersales = new Aftersales();
        updateAftersales.setAftersaleId(id);
        updateAftersales.setStatus(REFUSE.getCode());
        updateAftersales.setReply(reply);
        // 关闭子类型=拒收，驱动前后台「商家/供应商已拒收」文案，区别于审核驳回
        updateAftersales.setCloseType(CLOSE_TYPE_REJECT_RECEIVE);
        this.updateById(updateAftersales);

        adminLogService.createByLogInfo("拒收退货:" + id);
        // 写入协商记录（操作人按登录态自动记录商家/供应商）
        aftersalesLogService.addAftersaleLog(id, "拒收退货：" + reply,
                aftersales.getShopId(), aftersales.getVendorId());

        try {
            wechatLiveOrderSyncService.onAftersaleChanged(this.getById(id), EVENT_AFTERSALE_UPDATED);
        } catch (Exception e) {

        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoApproveReturnAftersales(Integer aftersalesId) {
        autoApproveReturnAftersales(aftersalesId, false);
    }

    @Transactional(rollbackFor = Exception.class)
    protected void autoApproveReturnAftersales(Integer aftersalesId, boolean directToReceiptWaiting) {
        Aftersales aftersales = this.getById(aftersalesId);
        Assert.notNull(aftersales, () -> new GlobalException("售后信息不存在"));
        Assert.isTrue(IN_REVIEW.getCode() == aftersales.getStatus(), () -> new GlobalException("该售后状态不能操作"));
        Assert.isTrue(AftersalesTypeEnum.RETURN.getCode() == aftersales.getAftersaleType(), () -> new GlobalException("仅支持退货退款自动审核"));

        int itemFlag = checkAfterSaleProduct(aftersales.getAftersaleId(), aftersales.getOrderId());

        // 供应商已开启售后审核：平台侧自动审核只走到「待供应商审核」，供应商这一环仍需人工。
        // 退货地址与回寄提示留待供应商审核通过时填写，此处不预填。
        boolean vendorReviewRequired = isVendorAftersalesReviewRequired(aftersales);
        String returnAddress = vendorReviewRequired ? null : resolveReturnAddress(aftersales);
        String returnGoodsTip = vendorReviewRequired
                ? null
                : "您的售后申请已通过，请按以下地址回寄商品，并在回寄后填写物流信息：" + returnAddress;
        Integer approvedStatus;
        if (vendorReviewRequired) {
            approvedStatus = WAIT_FOR_SUPPLIER_AUDIT.getCode();
        } else {
            approvedStatus = directToReceiptWaiting ? getWaitingReceiptStatus(aftersales) : SEND_BACK.getCode();
        }

        BigDecimal calculatedRefundAmount = aftersales.getRefundAmount();
        Order orderForRefund = orderMapper.selectById(aftersales.getOrderId());
        if (orderForRefund != null) {
            List<AftersalesItemVO> aftersalesItemVOs = aftersalesItemService.getItemByIds(
                    Collections.singletonList(aftersales.getAftersaleId()), false);
            BigDecimal refundProductAmount = calcRefundProductAmount(orderForRefund, aftersalesItemVOs);
            if (refundProductAmount.compareTo(BigDecimal.ZERO) > 0) {
                calculatedRefundAmount = refundProductAmount;
            }
        }

        Aftersales updateAftersales = Aftersales.builder()
                .aftersaleId(aftersalesId)
                .auditTime(StringUtils.getCurrentTime())
                .status(approvedStatus)
                .reply("系统自动审核通过")
                .returnAddress(returnAddress)
                .returnGoodsTip(returnGoodsTip)
                .refundAmount(calculatedRefundAmount)
                .build();
        this.updateById(updateAftersales);

        // 全退：与人工审核一致，审核通过不改订单状态，待财务退款到账后统一收口为已取消
        if (itemFlag == 1) {
            Order order = orderMapper.selectById(aftersales.getOrderId());
            if (order != null) {
            }
        }

        Order order = orderMapper.selectById(aftersales.getOrderId());
        if (order != null) {
            orderLogService.quickCreateBySystemAdmin("售后自动审核通过", order.getOrderId(), order.getOrderSn());
        }

        // 自动审核通过后，退货退款供应商订单已流转进入供应商可见状态（待供应商审核/待回寄/待供应商收货），
        // 此时才通知供应商（消息中心 + 语音播报），与列表可见性保持一致。
        if (order != null && order.getVendorId() != null && order.getVendorId() > 0) {
            notifyVendorAftersale(order, aftersales);
        }

        // 售后审核环节归店铺（自营时 shopId 为 0，前端显示为平台）
        aftersalesLogService.addSystemAftersaleLog(aftersalesId,
                resolveAutoApproveLogInfo(vendorReviewRequired, directToReceiptWaiting),
                aftersales.getShopId(), 0);
        // 供应商审核环节同时被跳过时另起一条，两个环节各归各的责任方，避免一条记录同时代表两方
        if (!vendorReviewRequired && isVendorReturnAftersales(aftersales)) {
            addVendorAuditSkippedLog(aftersalesId, aftersales);
        }
    }

    /**
     * 拼装售后审核环节自动通过的协商记录文案。供应商开启审核时交代去向为转供应商人工审核；
     * 供应商审核被跳过的情形另起一条归属供应商的记录，不并入此处。
     */
    private String resolveAutoApproveLogInfo(boolean vendorReviewRequired, boolean directToReceiptWaiting) {
        if (vendorReviewRequired) {
            return AUTO_APPROVE_TO_VENDOR_LOG;
        }
        return directToReceiptWaiting
                ? "系统自动审核通过，售后已进入待收货状态"
                : "系统自动审核通过（已自动回填回寄地址）";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean autoReceive(Integer id) {
        return completeReturnAftersales(id, true);
    }

    private boolean completeReturnAftersales(Integer id, boolean systemAuto) {
        Aftersales aftersales = this.getById(id);
        if (aftersales == null) {
            throw new GlobalException("售后信息不存在");
        }

        Integer status = aftersales.getStatus();
        boolean canReceive;
        if (systemAuto) {
            canReceive = Objects.equals(status, RETURNED.getCode()) || Objects.equals(status, WAIT_FOR_SUPPLIER_RECEIPT.getCode());
            if (!canReceive) {
                return false;
            }
        } else {
            Integer isVendor = tigshopProperties.getIsVendor();
            if (isVendor == 1 && (aftersales.getVendorId() != null && aftersales.getVendorId() > 0)) {
                canReceive = Objects.equals(status, WAIT_FOR_SUPPLIER_RECEIPT.getCode());
            } else {
                canReceive = Objects.equals(status, RETURNED.getCode());
            }
            if (!canReceive) {
                throw new GlobalException("该售后状态不能操作");
            }
        }

        Aftersales updateAftersales = new Aftersales();
        updateAftersales.setAftersaleId(id);
        updateAftersales.setStatus(COMPLETE.getCode());
        updateAftersales.setFinalTime(StringUtils.getCurrentTime());
        Order order = orderMapper.selectById(aftersales.getOrderId());
        BigDecimal finalRefundAmount = calculateFinalRefundAmount(order, aftersales, aftersales.getRefundAmount(), true);
        updateAftersales.setRefundAmount(finalRefundAmount);
        aftersales.setRefundAmount(finalRefundAmount);
        boolean isFullAftersales = checkAfterSaleProduct(aftersales.getAftersaleId(), aftersales.getOrderId()) == 1;

        try {
            log.info("售后确认收货开始，aftersaleId={}, orderId={}, systemAuto={}, currentStatus={}",
                    id, aftersales.getOrderId(), systemAuto, status);
            boolean result = this.updateById(updateAftersales);
            if (!result) {
                log.warn("售后确认收货更新状态失败，aftersaleId={}", id);
                return false;
            }

            // 先写"售后完成"日志，确保排在"财务退款"日志之前（与售后完成在前、财务退款在后的业务顺序一致）
            if (systemAuto) {
                // 确认收货环节的责任方即收货方：供应商订单归供应商，否则归店铺（自营显示为平台）
                boolean vendorReceipt = isVendorReturnAftersales(aftersales);
                aftersalesLogService.addSystemAftersaleLog(id,
                        "系统自动确认收货，售后申请已完成",
                        vendorReceipt ? 0 : aftersales.getShopId(),
                        vendorReceipt ? aftersales.getVendorId() : 0);
            } else {
                aftersalesLogService.addAftersaleLog(id,
                        getStatusName(COMPLETE.getCode()) + ": 售后申请通过",
                        aftersales.getShopId(), aftersales.getVendorId());
            }

            restoreOrderCouponUsageIfFullAftersales(aftersales, isFullAftersales);

            if (aftersales.getRefundAmount().compareTo(BigDecimal.ZERO) > 0) {
                log.info("售后确认收货开始处理退款出账，aftersaleId={}, refundAmount={}",
                        id, aftersales.getRefundAmount());
                saveAftersalesStatementOutput(aftersales);
                TransactionAfterCommitExecutor.execute(() -> rabbitTemplate.convertAndSend(
                        RabbitMQConfig.DIRECT_EXCHANGE,
                        RabbitMQConfig.REFUND_SETTLEMENT_ROUTING_KEY,
                        aftersales.getAftersaleId()));
            }

            log.info("售后确认收货开始创建退款申请，aftersaleId={}", id);
            createRefundApplyAfterReceive(aftersales);

            StatementOutput statementOutputForReceive = statementOutputService.lambdaQuery()
                    .eq(StatementOutput::getRecordId, aftersales.getOrderId())
                    .eq(StatementOutput::getRecordType, StatementType.ORDER.getCode())
                    .last("LIMIT 1")
                    .one();
            if (statementOutputForReceive != null && statementOutputForReceive.getRealSettlement() != null
                    && statementOutputForReceive.getRealSettlement() > 0) {
                log.info("售后确认收货开始生成退款结算单，aftersaleId={}, orderId={}", id, aftersales.getOrderId());
                this.generateStatement(aftersales);
            }

            if (!systemAuto) {
                adminLogService.createByLogInfo("完成退换货:" + id);
            }


            log.info("售后确认收货完成，aftersaleId={}, orderId={}, systemAuto={}",
                    id, aftersales.getOrderId(), systemAuto);

            try{
                wechatLiveOrderSyncService.onAftersaleChanged(this.getById(id), EVENT_AFTERSALE_UPDATED);
            }catch (Exception e){

            }
            return result;
        } catch (Exception exception) {
            log.error("售后确认收货失败，aftersaleId={}, orderId={}, systemAuto={}, currentStatus={}, error={}",
                    id, aftersales.getOrderId(), systemAuto, status, exception.getMessage(), exception);
            throw new GlobalException(exception.getMessage());
        }
    }

    private void createRefundApplyAfterReceive(Aftersales aftersales) throws Exception {
        RefundApply refundApply = refundApplyMapper.selectOne(new QueryWrapper<RefundApply>()
                .eq("aftersale_id", aftersales.getAftersaleId())
                .last("LIMIT 1"));
        if (refundApply != null) {
            return;
        }

        createRefundApplyAndHandleFinance(aftersales, "售后申请通过");
    }

    private Integer getWaitingReceiptStatus(Aftersales aftersales) {
        Integer isVendor = tigshopProperties.getIsVendor();
        if (isVendor == 1 && (aftersales.getVendorId() != null && aftersales.getVendorId() > 0)) {
            return WAIT_FOR_SUPPLIER_RECEIPT.getCode();
        }
        return RETURNED.getCode();
    }

    /**
     * 售后经店铺/平台审核通过、流转进入供应商可见状态（待供应商审核/待回寄/待供应商收货）后，
     * 给供应商发送一条售后消息。消息中心与语音播报（type=32）同源，故发此消息即同时触发供应商端的
     * 消息中心展示与语音播报。仅供应商订单触发。
     *
     * @param order      订单
     * @param aftersales 售后单
     */
    private void notifyVendorAftersale(Order order, Aftersales aftersales) {
        if (order == null || order.getVendorId() == null || order.getVendorId() <= 0) {
            return;
        }
        List<AftersalesItemVO> items = aftersalesItemService.getItemByIds(
                Collections.singletonList(aftersales.getAftersaleId()), false);
        String productName = CollUtil.isNotEmpty(items) ? items.getFirst().getProductName() : "";
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("aftersaleId", aftersales.getAftersaleId());
        relatedData.put("aftersalesSn", aftersales.getAftersalesSn());
        // 供应商端售后播报由「需要人工处理」决定：平台售后审核开启，或供应商自身开启了售后审核
        // （售后会停在待供应商审核等人工处理）则播报；两者都关闭时静音，与申请阶段保持一致。
        String afterSalesReview = configService.getConfigVal(SettingsEnum.AFTER_SALES_REVIEW);
        Integer broadcast = ("1".equals(afterSalesReview) || isVendorAftersalesReviewRequired(aftersales)) ? 1 : 0;
        AdminMsgCreateDTO adminMsgCreateDTO = AdminMsgCreateDTO.builder()
                .msgType(AdminMsgTypeEnum.AFTER_SALE_REQUEST.getCatId())
                .shopId(order.getShopId())
                .vendorId(order.getVendorId())
                .orderId(order.getOrderId())
                .title("售后申请通知:" + productName)
                .content("您的订单【" + order.getOrderSn() + "】发起售后申请，请及时处理。")
                .isBroadcast(broadcast)
                .relatedData(relatedData)
                .build();
        adminMsgService.createMessage(adminMsgCreateDTO);
    }

    private RefundApply createRefundApplyAndHandleFinance(Aftersales aftersales, String refundNote) {
        RefundApply refundApply = createRefundApply(aftersales, refundNote);
        String financialReview = configService.getConfigVal(SettingsEnum.FINANCIAL_REVIEW);
        if (!"1".equals(financialReview)
                && Objects.equals(refundApply.getRefundStatus(), RefundStatusEnum.REFUND_STATUS_WAIT.getCode())) {
            autoAuditRefundApply(refundApply.getRefundId(), refundNote);
            // 财务退款是平台职能，即便供应商订单也归平台
            aftersalesLogService.addSystemAftersaleLog(aftersales.getAftersaleId(),
                    "财务审核已关闭，系统已自动执行退款处理",
                    0, 0);
        }
        return refundApply;
    }

    private RefundApply createRefundApply(Aftersales aftersales, String refundNote) {
        RefundApply existedRefundApply = refundApplyMapper.selectOne(new QueryWrapper<RefundApply>()
                .eq("aftersale_id", aftersales.getAftersaleId())
                .last("LIMIT 1"));
        if (existedRefundApply != null) {
            return existedRefundApply;
        }

        RefundApply applyData = new RefundApply();
        applyData.setRefundType(aftersales.getOrderId() != null ? 1 : 2);
        applyData.setOrderId(aftersales.getOrderId());
        applyData.setUserId(aftersales.getUserId());
        applyData.setAftersaleId(aftersales.getAftersaleId());
        applyData.setShopId(aftersales.getShopId());
        applyData.setAddTime(StringUtils.getCurrentTime());
        applyData.setRefundStatus(RefundStatusEnum.REFUND_STATUS_WAIT.getCode());
        applyData.setRefundBalance(BigDecimal.ZERO);
        applyData.setOnlineBalance(BigDecimal.ZERO);
        applyData.setOfflineBalance(BigDecimal.ZERO);
        applyData.setRefundNote(refundNote);
        refundApplyMapper.insert(applyData);
        if ("1".equals(configService.getConfigVal(SettingsEnum.FINANCIAL_REVIEW))) {
            notifyPlatformRefund(aftersales, applyData);
        }
        return applyData;
    }

    /**
     * 售后退款申请进入待财务审核时，通知总后台财务人员。
     */
    private void notifyPlatformRefund(Aftersales aftersales, RefundApply refundApply) {
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("refundId", refundApply.getRefundId());
        relatedData.put("aftersaleId", aftersales.getAftersaleId());
        relatedData.put("orderId", aftersales.getOrderId());
        relatedData.put("route", "aftersale");
        AdminMsgCreateDTO message = AdminMsgCreateDTO.builder()
                .msgType(AdminMsgTypeEnum.REFUND.getCatId())
                .shopId(0)
                .vendorId(0)
                .isBroadcast(1)
                .orderId(aftersales.getOrderId())
                .title("您有新的售后退款申请")
                .content("售后单【" + aftersales.getAftersalesSn() + "】申请退款,退款金额："
                        + aftersales.getRefundAmount() + configService.getCurrencyName())
                .relatedData(relatedData)
                .build();
        adminMsgService.createMessage(message);
    }

    private void autoAuditRefundApply(Integer refundId, String refundNote) {
        RefundApplyService refundApplyServiceBean = refundApplyService.getIfAvailable();
        if (refundApplyServiceBean == null) {
            throw new GlobalException("退款服务不可用");
        }
        refundApplyServiceBean.audit(buildAutoRefundAuditParam(refundId, refundNote));
    }

    private RefundApplyAuditParam buildAutoRefundAuditParam(Integer refundId, String refundNote) {
        RefundApplyService refundApplyServiceBean = refundApplyService.getIfAvailable();
        if (refundApplyServiceBean == null) {
            throw new GlobalException("退款服务不可用");
        }

        RefundApply refundApply = refundApplyMapper.selectById(refundId);
        if (refundApply == null) {
            throw new GlobalException("退款申请不存在");
        }

        RefundApplyDetailVO detail = refundApplyServiceBean.detail(refundId);
        if (detail == null || detail.getAftersales() == null) {
            throw new GlobalException("退款申请详情不存在");
        }
        Order order = orderMapper.selectById(refundApply.getOrderId());

        RefundApplyAuditParam auditParam = new RefundApplyAuditParam();
        auditParam.setRefundId(Long.valueOf(refundId));
        auditParam.setRefundStatus(RefundStatusEnum.REFUND_STATUS_PROCESSING.getCode());
        auditParam.setRefundNote(StrUtil.blankToDefault(refundNote, "系统自动退款"));
        auditParam.setOnlineBalance("0");
        auditParam.setOfflineBalance("0");
        auditParam.setRefundBalance("0");
        auditParam.setCreditRefundAmount("0");
        auditParam.setSystemAutoRefund(1);

        BigDecimal refundAmount = safeAmount(detail.getAftersales().getRefundAmount());
        if (detail.getIsCreditOrder() != null && detail.getIsCreditOrder() == 1) {
            BigDecimal creditRefundAmount = safeAmount(detail.getCreditRefundAmount());
            if (creditRefundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                creditRefundAmount = refundAmount;
            }
            auditParam.setCreditRefundAmount(toAmountString(creditRefundAmount.min(refundAmount)));
            return auditParam;
        }

        List<RefundApply> processedRefundApplies = refundApplyMapper.selectList(new QueryWrapper<RefundApply>()
                .eq("order_id", refundApply.getOrderId())
                .ne("refund_id", refundId)
                .in("refund_status", RefundStatusEnum.REFUND_STATUS_PROCESSING.getCode(), RefundStatusEnum.REFUND_STATUS_PROCESSED.getCode()));

        BigDecimal processedBalance = processedRefundApplies.stream()
                .map(item -> safeAmount(item.getRefundBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal processedOffline = processedRefundApplies.stream()
                .map(item -> safeAmount(item.getOfflineBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingAmount = refundAmount;

        BigDecimal onlineBalance = safeAmount(detail.getEffectiveOnlineBalance()).min(remainingAmount);
        remainingAmount = remainingAmount.subtract(onlineBalance);

        BigDecimal balanceAvailable = safeAmount(detail.getBalance()).subtract(processedBalance).max(BigDecimal.ZERO);
        BigDecimal refundBalance = balanceAvailable.min(remainingAmount);
        remainingAmount = remainingAmount.subtract(refundBalance);

        BigDecimal offlineAvailable = safeAmount(detail.getOfflinePaidAmount()).subtract(processedOffline).max(BigDecimal.ZERO);
        BigDecimal offlineBalance = offlineAvailable.min(remainingAmount);
        remainingAmount = remainingAmount.subtract(offlineBalance);

        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            boolean hasPointRelatedAmount = order != null
                    && ((order.getPointsAmount() != null && order.getPointsAmount().compareTo(BigDecimal.ZERO) > 0)
                    || (order.getUsePoints() != null && order.getUsePoints() > 0)
                    || Integer.valueOf(1).equals(order.getIsExchangeOrder()));
            boolean hasOrderLevelDiscount = order != null
                    && ((order.getDiscountAmount() != null && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0)
                    || (order.getCouponAmount() != null && order.getCouponAmount().compareTo(BigDecimal.ZERO) > 0));
            if (!hasPointRelatedAmount && !hasOrderLevelDiscount) {
                throw new GlobalException("系统自动退款失败：未匹配到完整的退款金额");
            }
        }

        auditParam.setOnlineBalance(toAmountString(onlineBalance));
        auditParam.setRefundBalance(toAmountString(refundBalance));
        auditParam.setOfflineBalance(toAmountString(offlineBalance));
        return auditParam;
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String toAmountString(BigDecimal amount) {
        BigDecimal safeValue = safeAmount(amount).stripTrailingZeros();
        if (safeValue.scale() < 0) {
            safeValue = safeValue.setScale(0);
        }
        return safeValue.toPlainString();
    }

    private String resolveReturnAddress(Aftersales aftersales) {
        Order order = orderMapper.selectById(aftersales.getOrderId());
        if (order == null) {
            throw new GlobalException("订单不存在");
        }

        if (order.getVendorId() != null && order.getVendorId() > 0) {
            Vendor vendor = vendorMapper.selectOnboardedById(order.getVendorId());
            String vendorAddress = parseDeliveryAddress(vendor != null ? vendor.getReceiveInfo() : null);
            if (StrUtil.isNotBlank(vendorAddress)) {
                return vendorAddress;
            }
        }

        if (order.getShopId() != null && order.getShopId() > 0) {
            Shop shop = shopMapper.selectById(order.getShopId());
            String shopAddress = parseDeliveryAddress(shop != null ? shop.getReceiveInfo() : null);
            if (StrUtil.isNotBlank(shopAddress)) {
                return shopAddress;
            }
        }

        ShippingSettingsVO shippingSettings = configService.getSettings(ShippingSettingsVO.class);
        if (shippingSettings == null) {
            throw new GlobalException("未配置可用的收货地址，请先配置店铺、供应商或平台收货信息");
        }
        String platformAddress = buildDeliveryAddress(
                shippingSettings.getReceiveSender(),
                shippingSettings.getReceiveMobile(),
                shippingSettings.getReceiveProvinceName(),
                shippingSettings.getReceiveCityName(),
                shippingSettings.getReceiveAreaName(),
                shippingSettings.getReceiveAddress()
        );
        if (StrUtil.isBlank(platformAddress)) {
            throw new GlobalException("未配置可用的收货地址，请先配置店铺、供应商或平台收货信息");
        }
        return platformAddress;
    }

    private String resolveReturnAddressForDetail(Aftersales aftersales) {
        try {
            return resolveReturnAddress(aftersales);
        } catch (Exception e) {
            log.warn("售后详情回显收货地址失败，aftersaleId={}, error={}", aftersales.getAftersaleId(), e.getMessage());
            return null;
        }
    }

    private String parseDeliveryAddress(String deliveryInfoJson) {
        if (StrUtil.isBlank(deliveryInfoJson) || !JSONUtil.isTypeJSON(deliveryInfoJson)) {
            return "";
        }
        try {
            cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(deliveryInfoJson);
            return buildDeliveryAddress(
                    jsonObject.getStr("sender"),
                    jsonObject.getStr("mobile"),
                    jsonObject.getStr("provinceName"),
                    jsonObject.getStr("cityName"),
                    jsonObject.getStr("areaName"),
                    jsonObject.getStr("address")
            );
        } catch (Exception e) {
            return "";
        }
    }

    private String buildDeliveryAddress(String sender, String mobile, String provinceName, String cityName, String areaName, String address) {
        List<String> addressParts = new ArrayList<>();
        if (StrUtil.isNotBlank(sender)) {
            addressParts.add(sender);
        }
        if (StrUtil.isNotBlank(mobile)) {
            addressParts.add(mobile);
        }
        String detailAddress = StrUtil.nullToEmpty(provinceName)
                + StrUtil.nullToEmpty(cityName)
                + StrUtil.nullToEmpty(areaName)
                + StrUtil.nullToEmpty(address);
        if (StrUtil.isNotBlank(detailAddress)) {
            addressParts.add(detailAddress);
        }
        return StrUtil.join(" ", addressParts);
    }

    /**
     * 保存退款的出账
     *
     * @param aftersales 退款记录
     */
    @Transactional
    public void saveAftersalesStatementOutput(Aftersales aftersales) {
        List<StatementOutputSaveParam> statementOutputSaveParamList = new ArrayList<>();
        Integer aftersaleId = aftersales.getAftersaleId();
        RecordRate recordRate = recordRateService.lambdaQuery()
                .eq(RecordRate::getRecordId, aftersales.getOrderId())
                .eq(RecordRate::getRecordType, StatementType.ORDER.getCode())
                .orderByDesc(RecordRate::getRecordRateId)
                .last("limit 1")
                .one();
        if (recordRate == null) {
            log.warn("售后单[{}]对应的订单[{}]未找到费率记录，跳过出账处理", aftersales.getAftersaleId(), aftersales.getOrderId());
            return;
        }
        // 获取订单的状态
        StatementOutput statementOutput = statementOutputService.lambdaQuery()
                .eq(StatementOutput::getRecordId, aftersales.getOrderId())
                .eq(StatementOutput::getRecordType, StatementType.ORDER.getCode())
                .last("limit 1")
                .one();

        Integer settlementStatus = 0;
        if (statementOutput != null) {
            settlementStatus = statementOutput.getRealSettlement();
        }

        settlementStatus = settlementStatus == null ? Constants.NO : settlementStatus;

        if (aftersales.getVendorId() != null && aftersales.getVendorId() > 0) {
            BigDecimal totalAmount = this.baseMapper.selectTotalAmount(aftersaleId, COMPLETE.getCode());
            totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
            StatementOutputSaveParam vendorStatementOutputSaveParam =
                    new StatementOutputSaveParam(aftersales, StatementType.ORDER_REFUND.getCode(), totalAmount, false, false);
            // 判断订单是否已结算，如果结算则将真实的结算状态改为已结算
            vendorStatementOutputSaveParam.setRealSettlement(settlementStatus);
            statementOutputSaveParamList.add(vendorStatementOutputSaveParam);

            BigDecimal serviceFee = totalAmount.multiply(recordRate.getSupplierServiceFee())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            StatementOutputSaveParam vendorServiceStatementOutputSaveParam =
                    new StatementOutputSaveParam(aftersales, StatementType.SERVICE_FEE.getCode(), serviceFee, false, true);
            vendorServiceStatementOutputSaveParam.setRealSettlement(settlementStatus);
            statementOutputSaveParamList.add(vendorServiceStatementOutputSaveParam);

            if (aftersales.getShopId() > 0) {
                BigDecimal shopAmount = aftersales.getRefundAmount().subtract(totalAmount);
                StatementOutputSaveParam shopStatementOutputSaveParam =
                        new StatementOutputSaveParam(aftersales, StatementType.ORDER_REFUND.getCode(), shopAmount, true, false);
                shopStatementOutputSaveParam.setRealSettlement(settlementStatus);
                statementOutputSaveParamList.add(shopStatementOutputSaveParam);

                BigDecimal shopServiceFee = shopAmount.multiply(recordRate.getShopServiceFee())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                StatementOutputSaveParam shopServiceStatementOutputSaveParam =
                        new StatementOutputSaveParam(aftersales, StatementType.SERVICE_FEE.getCode(), shopServiceFee, true, true);
                shopServiceStatementOutputSaveParam.setRealSettlement(settlementStatus);
                statementOutputSaveParamList.add(shopServiceStatementOutputSaveParam);
            }
        } else {
            if (aftersales.getShopId() > 0) {
                BigDecimal refundAmount = aftersales.getRefundAmount();
                StatementOutputSaveParam shopStatementOutputSaveParam =
                        new StatementOutputSaveParam(aftersales, StatementType.ORDER_REFUND.getCode(), refundAmount, true, false);
                shopStatementOutputSaveParam.setRealSettlement(settlementStatus);
                statementOutputSaveParamList.add(shopStatementOutputSaveParam);

                BigDecimal shopServiceFee = refundAmount.multiply(recordRate.getShopServiceFee())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                StatementOutputSaveParam shopServiceStatementOutputSaveParam =
                        new StatementOutputSaveParam(aftersales, StatementType.SERVICE_FEE.getCode(), shopServiceFee, true, true);
                shopServiceStatementOutputSaveParam.setRealSettlement(settlementStatus);
                statementOutputSaveParamList.add(shopServiceStatementOutputSaveParam);
            }
        }
        statementOutputService.batchSaveStatementOutput(statementOutputSaveParamList);
    }

    // 生成对账单
    private void generateStatement(Aftersales aftersales) {
        StatementSaveParam statementSaveParam = new StatementSaveParam();
        statementSaveParam.setAccountType(AccountType.ACCOUNT_BALANCE.getCode());
        statementSaveParam.setType(StatementType.ORDER_REFUND.getCode());
        statementSaveParam.setEntryType(EntryType.AUTO.getCode());

        // 可以改成订单各种支付类型的值做判断
        // 商品订单：orderType=0，避免撞充值单号
        // 一笔订单可能留下多条支付记录（下单后未付又重新发起支付），原写法无 LIMIT 会抛
        // TooManyResultsException；此处要的是实际付款所用渠道，故只取已支付的最新一条
        Paylog paylog = paylogMapper.selectOne(new LambdaQueryWrapper<Paylog>()
                .eq(Paylog::getOrderId, aftersales.getOrderId())
                .eq(Paylog::getOrderType, 0)
                .eq(Paylog::getPayStatus, PayStatusEnum.PAID.getCode())
                .orderByDesc(Paylog::getPaylogId)
                .last("LIMIT 1"));
        if (paylog != null) {
            statementSaveParam.setPaymentType(paylog.getPayCode());
        } else {
            statementSaveParam.setPaymentType(PayMethodType.OTHER.getCode());
        }

        statementSaveParam.setShopId(aftersales.getShopId());
        statementSaveParam.setVendorId(aftersales.getVendorId());
        statementSaveParam.setRecordId(aftersales.getAftersaleId());

        statementService.getIfAvailable().saveStatement(statementSaveParam);
    }

    @Override
    public boolean submitFeedbackRecord(Integer aftersaleId, AftersalesRecordCreateDTO aftersalesRecordCreateDTO, Integer currentAdminId) {
        Aftersales aftersales = this.getById(aftersaleId);
        if (aftersales == null) {
            throw new GlobalException("售后信息不存在");
        }
        AftersalesLog aftersalesLog = new AftersalesLog();
        aftersalesLog.setAftersaleId(aftersaleId);
        aftersalesLog.setLogInfo(aftersalesRecordCreateDTO.getLogInfo());
        aftersalesLog.setAdminName(currentAdminId != null ? SecurityUtils.getCurrentUsername() : "");
        aftersalesLog.setAddTime(StringUtils.getCurrentTime().toString());
        aftersalesLog.setUserName(SecurityUtils.getCurrentUsername());
        aftersalesLog.setReturnPic(JsonUtil.toJson(aftersalesRecordCreateDTO.getReturnPic()));

        if (aftersales.getStatus() == SEND_BACK.getCode() && (currentAdminId != null && currentAdminId != 0)) {
            Aftersales updateAftersales = new Aftersales();
            updateAftersales.setAftersaleId(aftersaleId);
            updateAftersales.setTrackingNo(aftersalesRecordCreateDTO.getTrackingNo());
            updateAftersales.setLogisticsName(aftersalesRecordCreateDTO.getLogisticsName());
            updateAftersales.setDealTime(StringUtils.getCurrentTime());
            this.save(updateAftersales);
        }

        return aftersalesLogService.save(aftersalesLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void afterSalesApply(AftersalesApplyParam param) {
        if (ObjectUtil.isEmpty(param.getItems()) || param.getItems().stream().mapToInt(AftersalesApplyParam.ItemsDTO::getNumber).sum() == 0) {
            throw new GlobalException("请填写申请售后数量");
        }

        param.setItems(
                param.getItems().stream()
                        .filter(item -> item.getNumber() != 0)
                        .collect(Collectors.toList())
        );

        for (AftersalesApplyParam.ItemsDTO item : param.getItems()) {
            OrderItem orderItem = orderItemMapper.selectById(item.getOrderItemId());
            if (orderItem != null && orderItem.getGiftCardId() != null && orderItem.getGiftCardId() > 0) {
                throw new GlobalException("该商品时礼品卡兑换商品不可进行售后");
            }
        }


        // 1. 查询订单
        Order order = orderMapper.selectById(param.getOrderId());
        Assert.notNull(order, () -> new GlobalException(messageHelper.get("error.order.not.exist")));

        // 电子礼品卡不支持售后；实体礼品卡按普通实体商品流程处理。
        // 按申请商品行判断，避免礼品卡订单中的实体卡被订单类型整体拦截。
        List<Integer> orderItemIds = param.getItems().stream()
                .map(AftersalesApplyParam.ItemsDTO::getOrderItemId)
                .filter(Objects::nonNull)
                .toList();
        if (!orderItemIds.isEmpty()) {
            List<OrderItem> applyOrderItems = orderItemMapper.selectList(new QueryWrapper<OrderItem>()
                    .eq("order_id", order.getOrderId())
                    .in("item_id", orderItemIds));
            boolean containsVirtualGiftCard = applyOrderItems.stream()
                    .anyMatch(item -> Objects.equals(item.getProductType(), ProductType.PRODUCT_TYPE_GIFT_CARD_VIRTUAL.getCode()));
            Assert.isFalse(containsVirtualGiftCard,
                    () -> new GlobalException(messageHelper.get("error.aftersales.giftcard.not.allowed")));
        }
        Assert.isTrue(order.getPayStatus() == PaymentStatus.PAID.getCode(), () -> new GlobalException(messageHelper.get("error.aftersales.unpaid.not.allowed")));

        //如果pos的话,无法进行售后
        if (Objects.equals(order.getOrderSource(), "pos")) {
            throw new GlobalException(messageHelper.get("error.aftersales.pos.not.allowed"));
        }

        // 拼团订单团长售后规则校验
        if (OrderType.PIN.getCode() == order.getOrderType()) {
            validateGrouponCaptainRefundRule(order, param.getAftersaleType());
        }

        // 买家确认收货 afterSalesLimitDays 天后，不支持买家申请售后
        if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus()) {
            String afterSalesLimitDays = configService.getConfigVal(SettingsEnum.AFTER_SALES_LIMIT_DAYS);
            if (StrUtil.isNotBlank(afterSalesLimitDays)) {
                long receivedTime = order.getReceivedTime() * 1000;
                BigDecimal afterSalesLimitDaysTime = new BigDecimal(afterSalesLimitDays).multiply(new BigDecimal(24 * 60 * 60 * 1000));
                LocalDateTime limitDateTime = Instant.ofEpochMilli(receivedTime + afterSalesLimitDaysTime.longValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                boolean toAftersalses = limitDateTime.isAfter(LocalDateTime.now());
                if (!toAftersalses) {
                    throw new GlobalException("买家确认收货" + afterSalesLimitDays + "天后，不支持买家申请售后");
                }
            }
        }

        if (!Boolean.TRUE.equals(param.getIsAdminCancel())) {
            param.setAftersaleReason(afterSalesReasonConfigService.validateNewBuyerReason(param.getAftersaleReason()));
        }

        // 2. 校验商品可申请数量不足
        validateOrderQuantity(param.getItems(), param.getOrderId(), true, null, param.getIsAdminCancel());

        // 3. 创建售后
        Aftersales aftersales = param.createAfterSales(order.getOrderId(), order.getShopId(), param.getUserId());
        // 如果该订单有绑定供应商
        if (order.getVendorId() != null && order.getVendorId() > 0) {
            aftersales.setVendorId(order.getVendorId());
        }
        this.save(aftersales);

        try{
            wechatLiveOrderSyncService.onAftersaleChanged(aftersales, EVENT_AFTERSALE_CREATED);
        }catch (Exception e){

        }

        // 账期支付订单：申请售后时将账期订单置为“退款中”，避免被结算模块选中进行结算
        try {
            CreditOrder creditOrder = creditOrderService.getByOrderId(order.getOrderId());
            if (creditOrder != null
                    && CreditOrderSettlementStatusEnum.UNSETTLED.getCode().equals(creditOrder.getSettlementStatus())) {
                CreditOrder creditOrderUpdate = new CreditOrder();
                creditOrderUpdate.setCreditOrderId(creditOrder.getCreditOrderId());
                creditOrderUpdate.setSettlementStatus(CreditOrderSettlementStatusEnum.REFUNDING.getCode());
                creditOrderUpdate.setUpdateTime(StringUtils.getCurrentTime());
                creditOrderService.updateById(creditOrderUpdate);
            }
        } catch (Exception e) {
            // 不阻断售后申请流程
            // ignore
        }

        // 4. 创建售后项
        List<AftersalesItem> afterSalesItems = param.createAfterSalesItems(aftersales.getAftersaleId());
        aftersalesItemService.saveBatch(afterSalesItems);

        // 5. 创建日志
        AftersalesLog aftersalesLog = param.createAftersalesLog(aftersales.getAftersaleId());
        aftersalesLogMapper.insert(aftersalesLog);

        // 6. 发送后台消息 -- 售后申请
        // 6.1. 获取订单商品
        Integer orderItemId = param.getItems().getFirst().getOrderItemId();
        OrderItem orderItem = orderItemMapper.selectById(orderItemId);
        // 6.2. 售后播报静音判断：仅由「售后审核」开关控制——开启（afterSalesReview="1"，需人工处理）则播报；
        // 关闭（申请即自动通过、无需人工处理）则静音。「自动同意退货」不再影响播报。
        // 平台仅播自营、店铺订单的平台副本恒不播报（多店铺可见性规则，与本开关无关）。
        // 消息均写入消息中心，仅 is_broadcast 控制是否语音播报。
        // afterSalesReview 供第 7 步复用，autoReturnGoods 供第 8 步复用
        String afterSalesReview = configService.getConfigVal(SettingsEnum.AFTER_SALES_REVIEW);
        String autoReturnGoods = configService.getConfigVal(SettingsEnum.AUTO_RETURN_GOODS);
        Integer baseBroadcast = "1".equals(afterSalesReview) ? 1 : 0;
        // 6.3. 创建管理员消息（店铺、平台各发一条）。
        // 供应商订单不在申请阶段通知供应商：售后此时仍处于店铺/平台审核环节（status=IN_REVIEW），
        // 供应商查不到该售后；待店铺/平台审核通过、售后流转进入供应商可见状态后，
        // 再由 notifyVendorAftersale 通知供应商，保证供应商消息中心/语音播报与售后列表可见性一致。
        AdminMsgCreateDTO adminMsgCreateDTO = param.createAdminMsgCreateDTO(order, orderItem, aftersales);
        // 条1：店铺/自营，自身需处理 → 跟随审核开关
        adminMsgCreateDTO.setIsBroadcast(baseBroadcast);
        adminMsgService.createMessage(adminMsgCreateDTO);
        if (order.getShopId() != null && order.getShopId() > 0) {
            // 条2：店铺订单转发给平台的副本 → 消息中心可见但恒不播报（平台只播自营）
            adminMsgCreateDTO.setShopId(0);
            adminMsgCreateDTO.setIsBroadcast(0);
            adminMsgService.createMessage(adminMsgCreateDTO);
        }

        // 7. 售后审核开关判断
        if (!"1".equals(afterSalesReview)) {
            // 前端未传退款金额时，按订单商品计算建议退款金额（与管理员手动审核时看到的 suggestRefundAmount 一致）
            if (aftersales.getRefundAmount() == null) {
                BigDecimal calcRefundAmount = calculateSuggestRefundAmount(order, param.getItems());
                // 自动审核仅退款：整单或最后一次补齐整单时，按发货状态补计尚未退还的运费。
                if (AftersalesTypeEnum.PAYRETURN.getCode() == aftersales.getAftersaleType()) {
                    calcRefundAmount = calculateFinalRefundAmount(order, aftersales, calcRefundAmount, true);
                }
                aftersales.setRefundAmount(calcRefundAmount);
                this.lambdaUpdate()
                        .eq(Aftersales::getAftersaleId, aftersales.getAftersaleId())
                        .set(Aftersales::getRefundAmount, calcRefundAmount)
                        .update();
            } else if (AftersalesTypeEnum.PAYRETURN.getCode() == aftersales.getAftersaleType()) {
                BigDecimal calcRefundAmount = calculateFinalRefundAmount(order, aftersales, aftersales.getRefundAmount(), true);
                aftersales.setRefundAmount(calcRefundAmount);
                this.lambdaUpdate()
                        .eq(Aftersales::getAftersaleId, aftersales.getAftersaleId())
                        .set(Aftersales::getRefundAmount, calcRefundAmount)
                        .update();
            } else if (AftersalesTypeEnum.RETURN.getCode() == aftersales.getAftersaleType()) {
                List<AftersalesItemVO> aftersalesItemVOs = aftersalesItemService.getItemByIds(
                        Collections.singletonList(aftersales.getAftersaleId()), false);
                BigDecimal recalcAmount = calcRefundProductAmount(order, aftersalesItemVOs);
                if (recalcAmount.compareTo(BigDecimal.ZERO) > 0) {
                    aftersales.setRefundAmount(recalcAmount);
                    this.lambdaUpdate()
                            .eq(Aftersales::getAftersaleId, aftersales.getAftersaleId())
                            .set(Aftersales::getRefundAmount, recalcAmount)
                            .update();
                }
            }

            if (AftersalesTypeEnum.RETURN.getCode() == aftersales.getAftersaleType()) {
                autoApproveReturnAftersales(aftersales.getAftersaleId());
                return;
            }
            AftersalesEditParam editParam = AftersalesEditParam.builder()
                    .id(aftersales.getAftersaleId())
                    .status(APPROVED_FOR_PROCESSING.getCode())
                    .refundAmount(aftersales.getRefundAmount())
                    .reply("系统自动审核通过")
                    .returnGoodsTip("您的售后申请已通过")
                    .build();
            this.update(editParam);
            orderLogService.quickCreateBySystemAdmin("售后自动审核通过", order.getOrderId(), order.getOrderSn());
            return;
        }

        // 8. 发送自动同意退货申请（仅售后审核开启时生效；autoReturnGoods 已在第 6.2 步读取）
        if (StrUtil.isNotBlank(autoReturnGoods) && "1".equals(autoReturnGoods)) {
            String autoReturnGoodsDays = configService.getConfigVal(SettingsEnum.AUTO_RETURN_GOODS_DAYS);
            BigDecimal delayTime = new BigDecimal(autoReturnGoodsDays).multiply(new BigDecimal(24 * 60 * 60 * 1000));
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.DELAY_EXCHANGE,
                    RabbitMQConfig.AUTO_RETURN_GOODS_QUEUE_ROUTING_KEY,
                    aftersales.getAftersaleId(),
                    message -> {
                        message.getMessageProperties().setHeader("x-delay", delayTime.longValue());
                        return message;
                    }
            );
        }

    }

    /**
     * 计算建议退款金额，与管理员在售后详情页看到的 suggestRefundAmount 逻辑一致。
     * 整单售后时根据发货状态决定是否扣减运费，部分售后按商品单价×数量累加。
     */
    private BigDecimal calculateSuggestRefundAmount(Order order, List<AftersalesApplyParam.ItemsDTO> items) {
        List<OrderItem> orderItems = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", order.getOrderId()));
        Map<Integer, OrderItem> orderItemMap = orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getItemId, Function.identity()));

        BigDecimal refundAmount = items.stream()
                .map(item -> {
                    OrderItem oi = orderItemMap.get(item.getOrderItemId());
                    if (oi == null) {
                        return BigDecimal.ZERO;
                    }
                    BigDecimal unitPrice = oi.getPrice();
                    if (order.getOrderStatus() == OrderStatusEnum.CONFIRMED.getCode()
                            && StrUtil.isNotBlank(oi.getExtraSkuData())) {
                        List<OrderExtraSkuDataDTO> extraList =
                                JSONUtil.toList(oi.getExtraSkuData(), OrderExtraSkuDataDTO.class);
                        BigDecimal extraAmount = extraList.stream()
                                .map(OrderExtraSkuDataDTO::getAttrPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        unitPrice = unitPrice.add(extraAmount);
                    }
                    return unitPrice.multiply(new BigDecimal(item.getNumber()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Integer, Integer> orderItemQuantityMap = orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getItemId, OrderItem::getQuantity));
        Map<Integer, Integer> aftersalesQuantityMap = items.stream()
                .collect(Collectors.toMap(
                        AftersalesApplyParam.ItemsDTO::getOrderItemId,
                        AftersalesApplyParam.ItemsDTO::getNumber,
                        Integer::sum));
        boolean isFullOrderRefund = orderItems.size() == items.size()
                && orderItemQuantityMap.entrySet().stream()
                .allMatch(e -> Objects.equals(e.getValue(), aftersalesQuantityMap.get(e.getKey())));
        if (isFullOrderRefund) {
            refundAmount = order.getTotalAmount().subtract(
                    Optional.ofNullable(order.getShippingFee()).orElse(BigDecimal.ZERO));
        }

        return refundAmount;
    }

    private BigDecimal calculateFinalRefundAmount(Order order, Aftersales aftersales, BigDecimal inputRefundAmount,
                                                  boolean includeCurrentAftersales) {
        if (order == null || aftersales == null) {
            return Optional.ofNullable(inputRefundAmount).orElse(BigDecimal.ZERO);
        }

        BigDecimal baseRefundAmount = calculateBaseRefundAmount(order, aftersales);
        BigDecimal refundAmount = Optional.ofNullable(inputRefundAmount).orElse(baseRefundAmount);
        BigDecimal shippingFee = Optional.ofNullable(order.getShippingFee()).orElse(BigDecimal.ZERO);
        if (shippingFee.compareTo(BigDecimal.ZERO) <= 0) {
            return refundAmount;
        }

        // 如果当前金额已经大于基础商品退款额，可能是优惠券/折扣未按比例扣除导致虚高，
        // 封顶为 baseRefundAmount + shippingFee（即订单最大可退金额），避免超额分配
        if (refundAmount.compareTo(baseRefundAmount) > 0) {
            return refundAmount.min(baseRefundAmount.add(shippingFee));
        }

        boolean shouldAddShipping = false;
        if (ShippingStatusEnum.PENDING.getCode() == order.getShippingStatus()) {
            shouldAddShipping = isFullRefundCoverage(order, aftersales, includeCurrentAftersales);
        } else if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus()
                && ShippingStatusEnum.SHIPPED.getCode() == order.getShippingStatus()
                && (order.getShippingTime() == null || order.getShippingTime() <= 0)) {
            shouldAddShipping = isFullRefundCoverage(order, aftersales, includeCurrentAftersales);
        }

        if (shouldAddShipping) {
            shouldAddShipping = !isShippingAlreadyRefunded(order, aftersales, refundAmount);
        }

        return shouldAddShipping ? refundAmount.add(shippingFee) : refundAmount;
    }

    private BigDecimal calculateBaseRefundAmount(Order order, Aftersales aftersales) {
        List<AftersalesItem> aftersalesItems = aftersalesItemService.lambdaQuery()
                .eq(AftersalesItem::getAftersaleId, aftersales.getAftersaleId())
                .list();
        if (CollUtil.isEmpty(aftersalesItems)) {
            return Optional.ofNullable(aftersales.getRefundAmount()).orElse(BigDecimal.ZERO);
        }

        List<OrderItem> orderItems = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", order.getOrderId()));
        Map<Integer, OrderItem> orderItemMap = orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getItemId, Function.identity(), (left, right) -> left));

        BigDecimal refundAmount = aftersalesItems.stream()
                .map(item -> {
                    OrderItem orderItem = orderItemMap.get(item.getOrderItemId());
                    if (orderItem == null) {
                        return BigDecimal.ZERO;
                    }
                    BigDecimal unitPrice = orderItem.getPrice();
                    if (order.getOrderStatus() == OrderStatusEnum.CONFIRMED.getCode()
                            && StrUtil.isNotBlank(orderItem.getExtraSkuData())) {
                        List<OrderExtraSkuDataDTO> extraList = JSONUtil.toList(orderItem.getExtraSkuData(), OrderExtraSkuDataDTO.class);
                        BigDecimal extraAmount = extraList.stream()
                                .map(OrderExtraSkuDataDTO::getAttrPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        unitPrice = unitPrice.add(extraAmount);
                    }
                    return unitPrice.multiply(BigDecimal.valueOf(item.getNumber()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Integer, Integer> orderItemQuantityMap = orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getItemId, OrderItem::getQuantity, Integer::sum));
        Map<Integer, Integer> aftersalesQuantityMap = aftersalesItems.stream()
                .collect(Collectors.toMap(AftersalesItem::getOrderItemId, AftersalesItem::getNumber, Integer::sum));
        boolean isFullOrderRefund = orderItems.size() == aftersalesItems.size()
                && orderItemQuantityMap.entrySet().stream()
                .allMatch(entry -> Objects.equals(entry.getValue(), aftersalesQuantityMap.get(entry.getKey())));
        if (isFullOrderRefund) {
            return order.getTotalAmount().subtract(Optional.ofNullable(order.getShippingFee()).orElse(BigDecimal.ZERO));
        }
        return refundAmount;
    }

    /**
     * 检查运费是否已在之前的售后退款中退过。
     * 基于 RefundApply 实际退款记录（onlineBalance + refundBalance + offlineBalance）计算已完成退款总额，
     * 如果 (当前退款 + 运费 + 已完成实际退款) > 订单总金额，说明运费已经被退过，不应再追加。
     */
    private boolean isShippingAlreadyRefunded(Order order, Aftersales currentAftersales, BigDecimal currentRefundAmount) {
        BigDecimal shippingFee = Optional.ofNullable(order.getShippingFee()).orElse(BigDecimal.ZERO);
        if (shippingFee.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        List<RefundApply> refundApplies = refundApplyMapper.selectList(
                new QueryWrapper<RefundApply>()
                        .eq("order_id", order.getOrderId())
                        .ne("aftersale_id", currentAftersales.getAftersaleId())
                        .in("refund_status", RefundStatusEnum.REFUND_STATUS_PROCESSING.getCode(),
                                RefundStatusEnum.REFUND_STATUS_PROCESSED.getCode()));
        BigDecimal completedRefundTotal = refundApplies.stream()
                .map(r -> safeAmount(r.getOnlineBalance()).add(safeAmount(r.getRefundBalance())).add(safeAmount(r.getOfflineBalance())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return currentRefundAmount.add(shippingFee).add(completedRefundTotal)
                .compareTo(order.getTotalAmount()) > 0;
    }

    private boolean isFullRefundCoverage(Order order, Aftersales currentAftersales, boolean includeCurrentAftersales) {
        List<OrderItem> orderItems = orderItemMapper.selectList(new QueryWrapper<OrderItem>().eq("order_id", order.getOrderId()));
        if (CollUtil.isEmpty(orderItems)) {
            return false;
        }

        List<Aftersales> completedAftersales = this.lambdaQuery()
                .eq(Aftersales::getOrderId, order.getOrderId())
                .eq(Aftersales::getStatus, COMPLETE.getCode())
                .list();
        List<Integer> completedIds = completedAftersales.stream()
                .map(Aftersales::getAftersaleId)
                .filter(id -> !Objects.equals(id, currentAftersales.getAftersaleId()))
                .toList();
        List<AftersalesItem> completedItems = CollUtil.isEmpty(completedIds)
                ? new ArrayList<>()
                : aftersalesItemService.lambdaQuery().in(AftersalesItem::getAftersaleId, completedIds).list();
        if (includeCurrentAftersales) {
            completedItems.addAll(aftersalesItemService.lambdaQuery()
                    .eq(AftersalesItem::getAftersaleId, currentAftersales.getAftersaleId())
                    .list());
        }

        Map<Integer, List<AftersalesItem>> aftersalesItemMap = completedItems.stream()
                .collect(Collectors.groupingBy(AftersalesItem::getOrderItemId));
        return orderItems.stream().allMatch(item -> {
            List<AftersalesItem> curr = aftersalesItemMap.get(item.getItemId());
            if (CollUtil.isEmpty(curr)) {
                return false;
            }
            Integer itemNum = curr.stream().map(AftersalesItem::getNumber).reduce(0, Integer::sum);
            return Objects.equals(item.getQuantity(), itemNum);
        });
    }

    /**
     * 判断当前售后是否是"补齐最后缺口"的那一单：
     * 只看比当前售后更早创建且未取消/拒绝的售后，加上当前售后后恰好覆盖全部订单商品，
     * 但它们单独不能全覆盖——说明当前这单补齐了最后的缺口，运费应归入该单。
     */
    private boolean isCompletingLastItems(Order order, Aftersales currentAftersales) {
        List<OrderItem> orderItems = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", order.getOrderId()));
        if (CollUtil.isEmpty(orderItems)) {
            return false;
        }

        List<Aftersales> earlierAftersales = this.lambdaQuery()
                .eq(Aftersales::getOrderId, order.getOrderId())
                .lt(Aftersales::getAftersaleId, currentAftersales.getAftersaleId())
                .ne(Aftersales::getStatus, CANCEL.getCode())
                .ne(Aftersales::getStatus, REFUSE.getCode())
                .list();
        List<Integer> earlierIds = earlierAftersales.stream()
                .map(Aftersales::getAftersaleId).toList();

        List<AftersalesItem> earlierItems = CollUtil.isEmpty(earlierIds)
                ? new ArrayList<>()
                : aftersalesItemService.lambdaQuery().in(AftersalesItem::getAftersaleId, earlierIds).list();
        List<AftersalesItem> currentItems = aftersalesItemService.lambdaQuery()
                .eq(AftersalesItem::getAftersaleId, currentAftersales.getAftersaleId()).list();

        List<AftersalesItem> allItems = new ArrayList<>(earlierItems);
        allItems.addAll(currentItems);

        if (!coversAllOrderItems(orderItems, allItems)) {
            return false;
        }
        return !coversAllOrderItems(orderItems, earlierItems);
    }

    private boolean coversAllOrderItems(List<OrderItem> orderItems, List<AftersalesItem> aftersalesItems) {
        if (CollUtil.isEmpty(aftersalesItems)) {
            return false;
        }
        Map<Integer, Integer> aftersalesQtyMap = aftersalesItems.stream()
                .collect(Collectors.toMap(AftersalesItem::getOrderItemId, AftersalesItem::getNumber, Integer::sum));
        return orderItems.stream().allMatch(item -> {
            Integer refundedQty = aftersalesQtyMap.get(item.getItemId());
            return refundedQty != null && refundedQty >= item.getQuantity();
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void afterSalesApplyUpdate(ClientAfterSalesUpdateDTO updateDTO) {
        Aftersales aftersales = this.getById(updateDTO.getAftersaleId());
        Assert.notNull(aftersales, () -> new GlobalException(messageHelper.get("error.aftersales.record.not.exist")));
        Assert.isFalse(aftersales.getStatus() != IN_REVIEW.getCode(), () -> new GlobalException(messageHelper.get("error.aftersales.status.cannot.modify")));
        Assert.isFalse(updateDTO.getItems().isEmpty(), () -> new GlobalException(messageHelper.get("error.aftersales.quantity.required")));
        int totalNumber = updateDTO.getItems().stream().mapToInt(AftersalesApplyParam.ItemsDTO::getNumber).sum();
        Assert.isTrue(totalNumber > 0, () -> new GlobalException(messageHelper.get("error.aftersales.quantity.zero")));

        updateDTO.setAftersaleReason(afterSalesReasonConfigService.resolveUpdateReason(
                aftersales.getAftersaleReason(), updateDTO.getAftersaleReason()));
        validateOrderQuantity(updateDTO.getItems(), updateDTO.getOrderId(), false, aftersales.getAftersaleId(), false);

        BeanUtils.copyProperties(updateDTO, aftersales);
        if (updateDTO.getPics() != null) {
            aftersales.setPics(JsonUtil.toJson(updateDTO.getPics()));
        }
        this.updateById(aftersales);

        // 删除
        aftersalesItemService.lambdaUpdate().eq(AftersalesItem::getAftersaleId, aftersales.getAftersaleId()).remove();

        for (AftersalesApplyParam.ItemsDTO item : updateDTO.getItems()) {
            AftersalesItem aftersalesItem = new AftersalesItem();
            aftersalesItem.setAftersaleId(aftersales.getAftersaleId());
            aftersalesItem.setOrderItemId(item.getOrderItemId());
            aftersalesItem.setNumber(item.getNumber());
            aftersalesItemService.save(aftersalesItem);
        }

        //记录日志
        User user = userMapper.selectById(updateDTO.getUserId());
        AftersalesLog aftersalesLog = new AftersalesLog();
        aftersalesLog.setAftersaleId(aftersales.getAftersaleId());
        aftersalesLog.setLogInfo("会员发起了" + AftersalesTypeEnum.getTypeName(updateDTO.getAftersaleType()) +
                ": " + updateDTO.getDescription());
        aftersalesLog.setUserName(user.getUsername());
        aftersalesLog.setAddTime(StringUtils.getCurrentTime().toString());
        if (!updateDTO.getItems().isEmpty()) {
            aftersalesLog.setReturnPic(JsonUtil.toJson(updateDTO.getPics()));
        } else {
            aftersalesLog.setReturnPic("");
        }
        aftersalesLogMapper.insert(aftersalesLog);
    }

    @Override
    public AfterSalesClientDetailVO clientDetail(Integer id, Integer userId) {
        if (id != null) {
            Aftersales aftersales = this.getById(id);
            // 校验当前用户是否是售后申请者
            if (userId != null && !userId.equals(aftersales.getUserId())) {
                throw new GlobalException("无权限查看此售后记录");
            }
            AfterSalesClientDetailVO afterSalesVO = new AfterSalesClientDetailVO();
            BeanUtils.copyProperties(aftersales, afterSalesVO);
            afterSalesVO.setReturnGoodsTip(null);
            afterSalesVO.setAftersalesTypeName(AftersalesTypeEnum.getTypeName(aftersales.getAftersaleType()));
            // 前台买家状态头：按 类型/状态/关闭子类型 区分；仅退款财务驳回由关联 RefundApply 推导
            String financeRejectReason = getFinanceRejectReason(aftersales);
            boolean financeRejected = financeRejectReason != null;
            Integer statusForDisplay = resolveDisplayStatus(aftersales);
            String statusNameKey = AftersalesStatusEnum.getClientStatusNameKey(
                    aftersales.getAftersaleType(), statusForDisplay, aftersales.getCloseType(), financeRejected);
            afterSalesVO.setStatusName(statusNameKey == null ? null : messageHelper.get(statusNameKey));
            afterSalesVO.setAddTime(TigUtils.handelTime(aftersales.getAddTime()));

            afterSalesVO.setAftersalesItems(aftersalesItemService.getItemByIds(Collections.singletonList(id), true));
            //查询afterSalesLog
            LambdaQueryWrapper<AftersalesLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AftersalesLog::getAftersaleId, id);
            List<AftersalesLogVO> afterSalesLogVOList = aftersalesLogService.getDetailLog(id);
            this.translateForLogInfo(afterSalesLogVOList);
            afterSalesVO.setAftersalesLog(afterSalesLogVOList);
            afterSalesVO.setCanCancel(aftersales.getStatus() == IN_REVIEW.getCode());
            afterSalesVO.setStepStatus(getStep(aftersales));
            if (aftersales.getStatus() == SEND_BACK.getCode()) {
                afterSalesVO.setReturnGoodsTip(aftersales.getReturnGoodsTip());
            }
            afterSalesVO.setPics(JSON.parseArray(aftersales.getPics(), ReturnPicDTO.class));
            BigDecimal displayRefundAmount = Optional.ofNullable(aftersales.getRefundAmount()).orElse(BigDecimal.ZERO);
            if (aftersales.getStatus() == IN_REVIEW.getCode()
                    || aftersales.getStatus() == WAIT_FOR_SUPPLIER_AUDIT.getCode()) {
                Order order = orderMapper.selectById(aftersales.getOrderId());
                if (order != null) {
                    List<AftersalesItemVO> itemList = afterSalesVO.getAftersalesItems();
                    List<OrderItem> orderItems = orderItemMapper.selectList(
                            new QueryWrapper<OrderItem>().eq("order_id", order.getOrderId()));
                    Map<Integer, Integer> orderItemQtyMap = orderItems.stream()
                            .collect(Collectors.toMap(OrderItem::getItemId, OrderItem::getQuantity));
                    Map<Integer, Integer> asItemQtyMap = itemList.stream()
                            .collect(Collectors.toMap(AftersalesItemVO::getOrderItemId, AftersalesItemVO::getNumber, Integer::sum));
                    boolean isFullOrderRefund = orderItems.size() == itemList.size()
                            && orderItemQtyMap.entrySet().stream()
                            .allMatch(e -> Objects.equals(e.getValue(), asItemQtyMap.get(e.getKey())));
                    if (isFullOrderRefund) {
                        displayRefundAmount = order.getOrderStatus() >= OrderStatusEnum.PROCESSING.getCode()
                                ? order.getTotalAmount().subtract(Optional.ofNullable(order.getShippingFee()).orElse(BigDecimal.ZERO))
                                : order.getTotalAmount();
                    } else {
                        displayRefundAmount = calcRefundProductAmount(order, itemList);
                    }
                }
            }
            afterSalesVO.setRefundAmount(displayRefundAmount.toString());

            // 买家端状态副文案（按 类型/状态/关闭子类型 区分；缺失时前端按 status 本地兜底）
            String statusDescKey = AftersalesStatusEnum.getClientStatusDescKey(
                    aftersales.getAftersaleType(), statusForDisplay, aftersales.getCloseType(), financeRejected);
            String rejectReply = financeRejected ? financeRejectReason : aftersales.getReply();
            afterSalesVO.setStatusDesc(statusDescKey == null ? null
                    : messageHelper.get(statusDescKey, rejectReply == null ? "" : rejectReply));

            // 处理中且已开启自动同意退货：返回自动同意退款截止时间，用于买家端倒计时
            afterSalesVO.setAutoAgreeEndTime(resolveAutoAgreeEndTime(aftersales));
            // 服务器当前时间：买家端倒计时以服务端时间为准，规避本地时钟偏差
            afterSalesVO.setServerTime(TigUtils.handelTime(System.currentTimeMillis() / 1000));
            return afterSalesVO;

        }
        return null;
    }

    /**
     * 仅退款是否已被平台财务驳回：类型=仅退款，且关联最新退款申请为已拒绝(refundStatus=4)。
     *
     * 仅退款审核通过时，历史流程会先将售后主单置为已完成(6)，再创建财务退款申请；
     * 因此不能用售后主状态限制财务拒绝的识别，否则会把“已拒绝且未退款”错误显示为“退款成功”。
     */
    private boolean isFinanceRejected(Aftersales aftersales) {
        return getFinanceRejectReason(aftersales) != null;
    }

    private String getFinanceRejectReason(Aftersales aftersales) {
        if (aftersales == null
                || aftersales.getAftersaleType() == null
                || aftersales.getAftersaleType() != AftersalesTypeEnum.PAYRETURN.getCode()) {
            return null;
        }
        RefundApply refundApply = refundApplyMapper.selectOne(new QueryWrapper<RefundApply>()
                .eq("aftersale_id", aftersales.getAftersaleId())
                .orderByDesc("refund_id")
                .last("LIMIT 1"));
        return refundApply != null
                && Objects.equals(refundApply.getRefundStatus(), RefundStatusEnum.REFUND_STATUS_REJECTED.getCode())
                ? StrUtil.blankToDefault(refundApply.getRefundNote(), "未提供拒绝原因")
                : null;
    }

    /**
     * 售后已置「已完成(6)」但平台财务退款尚未处理完成：关联退款申请存在且状态为待处理/处理中。
     * 判定口径与售后进度条（getStep 中 refundStatus != 2 进入财务审核中）保持一致。
     * 查不到退款申请的历史数据不覆盖，仍按「已完成」展示。
     */
    private boolean isPendingFinanceRefund(Aftersales aftersales) {
        if (aftersales == null
                || aftersales.getStatus() == null
                || aftersales.getStatus() != COMPLETE.getCode()) {
            return false;
        }
        RefundApply refundApply = refundApplyMapper.selectOne(new QueryWrapper<RefundApply>()
                .eq("aftersale_id", aftersales.getAftersaleId())
                .orderByDesc("refund_id")
                .last("LIMIT 1"));
        return refundApply != null
                && (Objects.equals(refundApply.getRefundStatus(), RefundStatusEnum.REFUND_STATUS_WAIT.getCode())
                || Objects.equals(refundApply.getRefundStatus(), RefundStatusEnum.REFUND_STATUS_PROCESSING.getCode()));
    }

    /**
     * 展示用售后状态：待平台财务退款时按「审核通过待处理(2)」出文案（后台「平台处理中」/ 前台「财务退款」），
     * 其余保持真实状态。仅影响展示，不改动库中状态与状态机。
     */
    private Integer resolveDisplayStatus(Aftersales aftersales) {
        // 财务拒绝优先于售后主单的历史完成状态，避免 status=6 被映射为退款成功。
        if (isFinanceRejected(aftersales)) {
            return APPROVED_FOR_PROCESSING.getCode();
        }
        return isPendingFinanceRefund(aftersales)
                ? APPROVED_FOR_PROCESSING.getCode()
                : aftersales.getStatus();
    }

    /**
     * 列表批量版：一次查出这批售后单的最新退款申请，返回待平台财务退款的售后 ID，避免逐行查询。
     */
    @Override
    public Set<Integer> findPendingFinanceRefundIds(List<Aftersales> aftersalesList) {
        if (CollUtil.isEmpty(aftersalesList)) {
            return Collections.emptySet();
        }
        List<Integer> completedIds = aftersalesList.stream()
                .filter(item -> item.getStatus() != null && item.getStatus() == COMPLETE.getCode())
                .map(Aftersales::getAftersaleId)
                .toList();
        if (CollUtil.isEmpty(completedIds)) {
            return Collections.emptySet();
        }
        List<RefundApply> refundApplies = refundApplyMapper.selectList(new QueryWrapper<RefundApply>()
                .in("aftersale_id", completedIds));
        Map<Integer, RefundApply> latestMap = new HashMap<>();
        for (RefundApply refundApply : refundApplies) {
            latestMap.merge(refundApply.getAftersaleId(), refundApply,
                    (oldOne, newOne) -> newOne.getRefundId() > oldOne.getRefundId() ? newOne : oldOne);
        }
        return latestMap.values().stream()
                .filter(item -> Objects.equals(item.getRefundStatus(), RefundStatusEnum.REFUND_STATUS_WAIT.getCode())
                        || Objects.equals(item.getRefundStatus(), RefundStatusEnum.REFUND_STATUS_PROCESSING.getCode()))
                .map(RefundApply::getAftersaleId)
                .collect(Collectors.toSet());
    }

    /**
     * 列表批量版：一次查出这批售后单的最新退款申请，返回平台财务已拒绝的售后 ID。
     * 财务拒绝是退款申请终态，不能按售后主单 status=6 显示为「已完成」。
     * 判定口径与详情侧 {@link #getFinanceRejectReason} 保持一致：只看类型是否为仅退款、最新退款申请是否已拒绝，
     * 不限制售后主单状态，否则同一单会在列表和详情出现两种状态。
     */
    public Set<Integer> findFinanceRejectedRefundIds(List<Aftersales> aftersalesList) {
        if (CollUtil.isEmpty(aftersalesList)) {
            return Collections.emptySet();
        }
        List<Integer> payReturnIds = aftersalesList.stream()
                .filter(item -> item.getAftersaleType() != null
                        && item.getAftersaleType() == AftersalesTypeEnum.PAYRETURN.getCode())
                .map(Aftersales::getAftersaleId)
                .toList();
        if (CollUtil.isEmpty(payReturnIds)) {
            return Collections.emptySet();
        }
        List<RefundApply> refundApplies = refundApplyMapper.selectList(new QueryWrapper<RefundApply>()
                .in("aftersale_id", payReturnIds));
        Map<Integer, RefundApply> latestMap = new HashMap<>();
        for (RefundApply refundApply : refundApplies) {
            latestMap.merge(refundApply.getAftersaleId(), refundApply,
                    (oldOne, newOne) -> newOne.getRefundId() > oldOne.getRefundId() ? newOne : oldOne);
        }
        return latestMap.values().stream()
                .filter(item -> Objects.equals(item.getRefundStatus(), RefundStatusEnum.REFUND_STATUS_REJECTED.getCode()))
                .map(RefundApply::getAftersaleId)
                .collect(Collectors.toSet());
    }

    /**
     * 售后列表状态文案：待平台财务退款时按状态 2 出对应端文案，其余沿用列表原有文案。
     */
    private String resolveListStatusName(Aftersales aftersales, boolean pendingFinanceRefund,
                                         boolean financeRejected, boolean adminEnd) {
        if (financeRejected) {
            int status = APPROVED_FOR_PROCESSING.getCode();
            return adminEnd
                    ? AftersalesStatusEnum.getAdminStatusName(
                            aftersales.getAftersaleType(), status, aftersales.getVendorId(), true)
                    : AftersalesStatusEnum.getClientStatusName(
                            aftersales.getAftersaleType(), status, aftersales.getCloseType(), true);
        }
        if (!pendingFinanceRefund) {
            return getStatusName(aftersales.getStatus());
        }
        int status = APPROVED_FOR_PROCESSING.getCode();
        return adminEnd
                ? AftersalesStatusEnum.getAdminStatusName(
                        aftersales.getAftersaleType(), status, aftersales.getVendorId(), false)
                : AftersalesStatusEnum.getClientStatusName(
                        aftersales.getAftersaleType(), status, aftersales.getCloseType(), false);
    }

    /**
     * 计算自动同意退款截止时间（yyyy-MM-dd HH:mm:ss）。
     * 仅在「待审核(1)/待供应商审核(21)」且已开启自动同意退货时返回，否则返回 null。
     */
    private String resolveAutoAgreeEndTime(Aftersales aftersales) {
        if (aftersales == null || aftersales.getStatus() == null
                || (aftersales.getStatus() != IN_REVIEW.getCode()
                && aftersales.getStatus() != WAIT_FOR_SUPPLIER_AUDIT.getCode())
                || !"1".equals(configService.getConfigVal(SettingsEnum.AUTO_RETURN_GOODS))
                || aftersales.getAddTime() == null || aftersales.getAddTime() <= 0) {
            return null;
        }
        try {
            String daysVal = configService.getConfigVal(SettingsEnum.AUTO_RETURN_GOODS_DAYS);
            if (StrUtil.isNotBlank(daysVal)) {
                double days = Double.parseDouble(daysVal.trim());
                if (days > 0) {
                    long endTime = aftersales.getAddTime() + (long) (days * 86400);
                    return TigUtils.handelTime(endTime);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private List<AftersalesLogVO> translateForLogInfo(List<AftersalesLogVO> afterSalesLogVOList) {
        return afterSalesLogVOList.stream().peek(afterSalesLogVO -> {
            if (StrUtil.isNotBlank(afterSalesLogVO.getLogInfo()) && afterSalesLogVO.getLogInfo().contains(":")) {
                String[] splitLogInfo = afterSalesLogVO.getLogInfo().split(":");
                if (splitLogInfo.length > 0) {
                    afterSalesLogVO.setLogInfo(translatePackage.translate(splitLogInfo[0].trim()) + (splitLogInfo.length >= 2 && StrUtil.isNotBlank(splitLogInfo[1]) ? ":" + translatePackage.translate(splitLogInfo[1].trim()) : ":"));
                }
            }
            afterSalesLogVO.setLogInfo(translatePackage.translate(afterSalesLogVO.getLogInfo()));
        }).toList();
    }

    @Override
    public boolean submitFeedbackRecord(AfterSalesFeedbackDTO dto) {

        if (dto != null) {
            Aftersales aftersales = this.getById(dto.getId());
            if (aftersales == null) {
                throw new GlobalException(messageHelper.get("error.aftersales.apply.not.exist"));
            }
            Integer beforeStatus = aftersales.getStatus();
            boolean canSubmitReturnLogistics = dto.getUserId() != null
                    && dto.getUserId() > 0
                    && (aftersales.getStatus() == SEND_BACK.getCode()
                    || aftersales.getStatus() == RETURNED.getCode()
                    || aftersales.getStatus() == WAIT_FOR_SUPPLIER_RECEIPT.getCode());
            if (canSubmitReturnLogistics) {
                //要求寄回
                aftersales.setTrackingNo(dto.getTrackingNo());
                aftersales.setLogisticsName(dto.getLogisticsName());
                aftersales.setDealTime(StringUtils.getCurrentTime());
                if (aftersales.getStatus() == SEND_BACK.getCode()) {
                    aftersales.setStatus(getWaitingReceiptStatus(aftersales));
                }
                this.updateById(aftersales);
                if (Objects.equals(beforeStatus, SEND_BACK.getCode())) {
                    queueAutoReceiptAfterFeedback(aftersales);
                }
            }
            AftersalesLog aftersalesLog = new AftersalesLog();
            aftersalesLog.setAftersaleId(dto.getId());
            aftersalesLog.setLogInfo("会员已回寄，物流名称：" + dto.getLogisticsName()+"，物流单号：" + dto.getTrackingNo());
            User user = userMapper.selectById(dto.getUserId());
            aftersalesLog.setUserName(user != null ? user.getUsername() : "");
            aftersalesLog.setAddTime(StringUtils.getCurrentTime().toString());
            aftersalesLog.setAdminName("");
            if (dto.getReturnPic() != null && !dto.getReturnPic().isEmpty()) {
                aftersalesLog.setReturnPic(JsonUtil.toJson(dto.getReturnPic()));
            } else {
                aftersalesLog.setReturnPic("[]");
            }
            return aftersalesLogService.save(aftersalesLog);

        }
        return false;
    }

    private void queueAutoReceiptAfterFeedback(Aftersales aftersales) {
        String autoConfirmReceiptDays = configService.getConfigVal(SettingsEnum.AUTO_CONFIRM_RECEIPT_DAYS);
        if (StrUtil.isBlank(autoConfirmReceiptDays)) {
            log.info("未配置买家退货多少天后系统自动确认收货，跳过发送延时队列，aftersaleId={}",
                    aftersales.getAftersaleId());
            return;
        }

        BigDecimal delayDays = new BigDecimal(autoConfirmReceiptDays);
        if (delayDays.compareTo(BigDecimal.ONE) < 0) {
            log.info("买家退货自动确认收货天数配置小于1，跳过发送延时队列，aftersaleId={}, autoConfirmReceiptDays={}",
                    aftersales.getAftersaleId(), autoConfirmReceiptDays);
            return;
        }
        long delayTime = delayDays.multiply(BigDecimal.valueOf(24L * 60 * 60 * 1000)).longValue();
        if (delayTime < 0) {
            delayTime = 0L;
        }
        final long finalDelayTime = delayTime;

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DELAY_EXCHANGE,
                RabbitMQConfig.AFTERSALES_AUTO_RECEIPT_ROUTING_KEY,
                aftersales.getAftersaleId(),
                message -> {
                    message.getMessageProperties().setHeader("x-delay", finalDelayTime);
                    return message;
                }
        );
        log.info("买家提交回寄物流后已加入自动确认收货延时队列，aftersaleId={}, delayTime={}",
                aftersales.getAftersaleId(), finalDelayTime);
    }

    @Override
    public boolean cancel(AfterSalesCancelDTO dto) {
        Aftersales aftersales = this.getById(dto.getAftersaleId());
        if (aftersales == null) {
            throw new GlobalException(messageHelper.get("error.record.not.exist"));
        }
        if (aftersales.getStatus() != IN_REVIEW.getCode()) {
            throw new GlobalException(messageHelper.get("error.aftersales.status.cannot.cancel"));
        }
        aftersales.setStatus(CANCEL.getCode());
        boolean res = this.updateById(aftersales);
        if (res) {
            AftersalesLog aftersalesLog = new AftersalesLog();
            aftersalesLog.setLogInfo(CANCEL.getDescription());
            aftersalesLog.setAddTime(StringUtils.getCurrentTime().toString());
            aftersalesLog.setAftersaleId(dto.getAftersaleId());
            aftersalesLog.setReturnPic("");
            User user = userMapper.selectById(dto.getUserId());
            aftersalesLog.setUserName(user != null ? user.getUsername() : "");
            boolean logRes = aftersalesLogService.save(aftersalesLog);

            // 若该订单没有其他“处理中”的售后，则恢复账期订单为待结算
            try {
                CreditOrder creditOrder = creditOrderService.getByOrderId(aftersales.getOrderId());
                if (creditOrder != null
                        && CreditOrderSettlementStatusEnum.REFUNDING.getCode().equals(creditOrder.getSettlementStatus())) {
                    long processingCount = this.lambdaQuery()
                            .eq(Aftersales::getOrderId, aftersales.getOrderId())
                            .in(Aftersales::getStatus, IN_REVIEW.getCode(),
                                    APPROVED_FOR_PROCESSING.getCode(),
                                    SEND_BACK.getCode(),
                                    RETURNED.getCode(),
                                    WAIT_FOR_SUPPLIER_AUDIT.getCode(),
                                    WAIT_FOR_SUPPLIER_RECEIPT.getCode())
                            .count();
                    if (processingCount <= 0) {
                        CreditOrder creditOrderUpdate = new CreditOrder();
                        creditOrderUpdate.setCreditOrderId(creditOrder.getCreditOrderId());
                        creditOrderUpdate.setSettlementStatus(CreditOrderSettlementStatusEnum.UNSETTLED.getCode());
                        creditOrderUpdate.setUpdateTime(StringUtils.getCurrentTime());
                        creditOrderService.updateById(creditOrderUpdate);
                    }
                }
            } catch (Exception e) {
                // 不阻断取消售后流程
            }

            try{
                wechatLiveOrderSyncService.onAftersaleChanged(aftersales, EVENT_AFTERSALE_UPDATED);
            }catch (Exception e){

            }
            return logRes;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(AfterSalesCompleteDTO completeDTO) {
        Integer afterSaleId = completeDTO.getId();
        Aftersales aftersales = this.getById(afterSaleId);
        if (aftersales == null) {
            throw new GlobalException("售后信息不存在");
        }
        if (!aftersales.getStatus().equals(REFUSE.getCode())) {
            throw new GlobalException("该售后状态不能操作");
        }
        //售后单状态改为关闭
        aftersales.setStatus(CANCEL.getCode());
        updateById(aftersales);
        //添加售后记录
        aftersalesLogService.addAftersaleLog(afterSaleId, "售后驳回，关闭售后单");

        try{
            wechatLiveOrderSyncService.onAftersaleChanged(aftersales, EVENT_AFTERSALE_UPDATED);
        }catch (Exception e){

        }
    }

    @Override
    public Long getNewAftersaleCount(String adminType, Integer shopId, Integer vendorId, Long startTime) {
        LambdaQueryWrapper<Aftersales> queryWrapper = new LambdaQueryWrapper<>();
        if (AdminTypeEnum.fromCode(adminType) == AdminTypeEnum.VENDOR) {
            queryWrapper.eq(Aftersales::getVendorId, vendorId);
        } else {
            queryWrapper.eq(Aftersales::getShopId, shopId);
        }
        // 语音播报基于"前后两帧计数差值"判断是否发声，要求计数单调递增。
        // 故此处仅按"申请时间 addTime>=startTime"统计，不再叠加审核中/待审核状态过滤，
        // 避免售后被审核后计数回落导致后续新申请 delta<=0 而漏播。
        queryWrapper.ge(Aftersales::getAddTime, startTime);
        return this.count(queryWrapper);
    }

    //获取流程步骤
    private AfterSalesClientDetailVO.StepStatusVO getStep(Aftersales aftersales) {
        // 初始化所有动作为false
        int current = 0;
        String status = "process";
        List<Map<String, Object>> steps = new ArrayList<>();

        Map<String, Object> step1 = new HashMap<>();
        step1.put("title", messageHelper.get("aftersales.step.submit"));
        step1.put("active", true);
        step1.put("description", TigUtils.handelTime(aftersales.getAddTime()));
        steps.add(step1);

        Map<String, Object> step2 = new HashMap<>();
        step2.put("title", messageHelper.get("aftersales.step.reviewing"));
        step2.put("active", false);
        step2.put("description", aftersales.getAuditTime() != null && aftersales.getAuditTime() > 0 ? TigUtils.handelTime(aftersales.getAuditTime()) : "");
        steps.add(step2);

        Map<String, Object> step3 = new HashMap<>();
        Map<String, Object> step4 = new HashMap<>();
        if (aftersales.getAftersaleType().equals(AftersalesTypeEnum.PAYRETURN.getCode())) {
            step3.put("title", messageHelper.get("aftersales.step.completed"));
            step3.put("active", false);
            step3.put("description", aftersales.getFinalTime() != null && aftersales.getFinalTime() > 0 ? TigUtils.handelTime(aftersales.getFinalTime()) : "");
            steps.add(step3);
        } else {
            step3.put("title", messageHelper.get("aftersales.step.processing"));
            step3.put("active", false);
            step3.put("description", aftersales.getDealTime() != null && aftersales.getDealTime() > 0 ? TigUtils.handelTime(aftersales.getDealTime()) : "");
            steps.add(step3);

            step4.put("title", messageHelper.get("aftersales.step.completed"));
            step4.put("active", false);
            step4.put("description", aftersales.getFinalTime() != null && aftersales.getFinalTime() > 0 ? TigUtils.handelTime(aftersales.getFinalTime()) : "");
            steps.add(step4);
        }

        AftersalesStatusEnum afterSaleStatus = getStatus(aftersales.getStatus());

        switch (afterSaleStatus) {
            case IN_REVIEW -> {
                current = 1;
                step2.put("active", true);
                step2.put("description", messageHelper.get("aftersales.desc.reviewing"));
            }
            case APPROVED_FOR_PROCESSING -> {

                current = 1;
                step2.put("active", true);
                step2.put("description", messageHelper.get("aftersales.desc.approved"));
            }

            case REFUSE -> {
                current = 1;
                step2.put("active", true);
                step2.put("description", messageHelper.get("aftersales.desc.rejected"));
            }
            case SEND_BACK -> {
                current = 2;
                step2.put("active", true);
                step2.put("description", messageHelper.get("aftersales.desc.wait.return"));
                step3.put("active", true);
            }
            case RETURNED -> {
                current = 2;
                step2.put("active", true);
                step2.put("description", messageHelper.get("aftersales.desc.wait.merchant.receipt"));
                step3.put("active", true);
            }
            case WAIT_FOR_SUPPLIER_AUDIT -> {
                current = 2;
                step2.put("active", true);
                step2.put("description", messageHelper.get("aftersales.desc.wait.supplier.review"));
                step3.put("active", true);
            }
            case WAIT_FOR_SUPPLIER_RECEIPT -> {
                current = 2;
                step2.put("active", true);
                step2.put("description", messageHelper.get("aftersales.desc.wait.supplier.receipt"));
                step3.put("active", true);
            }
            case COMPLETE -> {
                current = aftersales.getAftersaleType().equals(AftersalesTypeEnum.PAYRETURN.getCode()) ? 2 : 3;
                status = "finish";
                step2.put("active", true);
                step3.put("active", true);
                step3.put("description", TigUtils.handelTime(aftersales.getFinalTime()));
                if (aftersales.getAftersaleType().equals(AftersalesTypeEnum.PAYRETURN.getCode())) {
                    if (steps.size() > 3) {
                        steps.remove(3);
                    }
                } else {
                    step4.put("active", true);
                }

                if (aftersales.getAftersaleType().equals(AftersalesTypeEnum.PAYRETURN.getCode())) {
                    RefundApply one = refundApplyService.getIfAvailable().lambdaQuery().
                            eq(RefundApply::getAftersaleId, aftersales.getAftersaleId())
                            .last(" limit 1").one();
                    if (one.getRefundStatus() != 2) {
                        //售后审核通过,但是财务退款还未处理完成,进入审核中逻辑处理
                        status = "process";
                        current = 1;
                        step2.put("active", true);
                        step3.put("active", false);
                        step4.put("active", false);
                        step2.put("description", messageHelper.get("aftersales.desc.finance.reviewing"));
                    }
                }

            }
            case CANCEL -> {
                current = 1;
                status = "finish";
                step2.put("title", messageHelper.get("aftersales.status.cancelled"));
                step2.put("active", true);
                step2.put("description", "");
                try{
                    steps.remove(2);
                    if (!aftersales.getAftersaleType().equals(AftersalesTypeEnum.PAYRETURN.getCode())){
                        steps.remove(2);
                    }
                }catch (Exception e){

                }
            }
        }

        AfterSalesClientDetailVO.StepStatusVO stepStatusVO = new AfterSalesClientDetailVO.StepStatusVO();
        stepStatusVO.setCurrent(current);
        stepStatusVO.setStatus(status);
        stepStatusVO.setSteps(steps);
        return stepStatusVO;
    }

    /**
     * 拼团订单团长售后规则校验：
     * - 2人团团长 + 待成团 → 禁止申请退款
     * - ≥3人团团长 + 待成团 → 仅允许"仅退款"
     * - 参团者 / 已成团 / 已失败 → 不拦截
     */
    private void validateGrouponCaptainRefundRule(Order order, Integer aftersaleType) {
        GrouponPromotionRecord record = grouponPromotionRecordService.lambdaQuery()
                .eq(GrouponPromotionRecord::getOrderId, order.getOrderId())
                .one();
        if (record == null) {
            return;
        }
        // 仅对团长 + 待成团状态生效
        if (!JoinTypeEnum.OPEN_GROUP.getCode().equals(record.getJoinType())
                || !JoinStatusEnum.WAIT_GROUP.getCode().equals(record.getJoinStatus())) {
            return;
        }
        Integer minGroupNum = record.getMinGroupNum();
        if (minGroupNum != null && minGroupNum <= 2) {
            throw new GlobalException("2人团团长不允许申请退款，请等待成团或邀请好友参团");
        }
        if (aftersaleType != null && !aftersaleType.equals(AftersalesTypeEnum.PAYRETURN.getCode())) {
            throw new GlobalException("拼团待成团阶段团长仅支持仅退款，团长退款将自动解散整团并退款给其他团员");
        }
    }

    //校验商品可申请数量不足
    private void validateOrderQuantity(List<AftersalesApplyParam.ItemsDTO> items, Integer orderId, boolean isAdd, Integer aftersaleId, Boolean isAdminCancel) {
        for (AftersalesApplyParam.ItemsDTO item : items) {
            //
            if (item.getNumber() == 0) {
                continue;
            }
            //查询是否有订单在售后
            if (isAdd) {
                long count = getApplyAftersalesCount(orderId, item.getOrderItemId());
                // 根据场景选择不同的提示信息：后台取消订单 vs 用户申请售后
                String errorKey = Boolean.TRUE.equals(isAdminCancel)
                        ? "error.aftersales.order.has.applied"
                        : "error.aftersales.product.has.applied";
                Assert.isTrue(count == 0, () -> new GlobalException(messageHelper.get(errorKey)));
            }

            Integer orderQuantity = getOrderItemNumber(item.getOrderItemId(), orderId);
            Integer sumValidNum = getValidNumber(item.getOrderItemId(), orderId, isAdd, aftersaleId);
            Assert.isTrue(orderQuantity >= item.getNumber() + sumValidNum, () -> new GlobalException(messageHelper.get("error.aftersales.quantity.insufficient")));
        }
    }

    // 售后进行中状态
    private List<Integer> getAfterSalseStatusList() {
        List<Integer> status = new ArrayList<>();
        status.add(IN_REVIEW.getCode());
        status.add(APPROVED_FOR_PROCESSING.getCode());
        status.add(SEND_BACK.getCode());
        status.add(RETURNED.getCode());
        Integer isVendor = tigshopProperties.getIsVendor();
        if (isVendor == 1) {
            status.add(WAIT_FOR_SUPPLIER_AUDIT.getCode());
            status.add(WAIT_FOR_SUPPLIER_RECEIPT.getCode());
        }
        return status;
    }

    //获取售后有效状态（审核未通过为终态，不占用可售后数量，故不含 REFUSE）
    private List<Integer> getAfterSalseValidStatusList() {
        List<Integer> status = new ArrayList<>();
        status.add(IN_REVIEW.getCode());
        status.add(APPROVED_FOR_PROCESSING.getCode());
        status.add(SEND_BACK.getCode());
        status.add(RETURNED.getCode());
        status.add(COMPLETE.getCode());
        status.add(WAIT_FOR_SUPPLIER_AUDIT.getCode());
        status.add(WAIT_FOR_SUPPLIER_RECEIPT.getCode());
        return status;
    }

    //获取已申请售后的数量
    private long getApplyAftersalesCount(Integer orderId, Integer itemId) {
        LambdaQueryWrapper<Aftersales> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Aftersales::getOrderId, orderId);
        queryWrapper.in(Aftersales::getStatus, getAfterSalseStatusList());
        List<Aftersales> aftersalesList = this.list(queryWrapper);
        if (!aftersalesList.isEmpty()) {
            //获取主键Id
            List<Integer> ids = aftersalesList.stream().
                    map(Aftersales::getAftersaleId).
                    toList();
            LambdaQueryWrapper<AftersalesItem> aftersalesItemQueryWrapper = new LambdaQueryWrapper<>();
            aftersalesItemQueryWrapper.in(AftersalesItem::getAftersaleId, ids);
            aftersalesItemQueryWrapper.eq(AftersalesItem::getOrderItemId, itemId);
            return aftersalesItemService.count(aftersalesItemQueryWrapper);
        }
        return 0;

    }

    //获取订单购买数量
    private Integer getOrderItemNumber(Integer itemId, Integer orderId) {
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getItemId, itemId);
        queryWrapper.eq(OrderItem::getOrderId, orderId);
        OrderItem orderItem = orderItemMapper.selectOne(queryWrapper);
        if (orderItem != null) {
            return orderItem.getQuantity();
        }
        return 0;
    }

    //获取有效数量
    private Integer getValidNumber(Integer itemId, Integer orderId, boolean isAdd, Integer aftersaleId) {
        List<Aftersales> aftersalesList = this.lambdaQuery()
                .eq(Aftersales::getOrderId, orderId)
                .in(Aftersales::getStatus, getAfterSalseValidStatusList())
                .ne(!isAdd, Aftersales::getAftersaleId, aftersaleId)
                .list();

        Integer sumValidNum = 0;
        if (!aftersalesList.isEmpty()) {
            List<Integer> ids = aftersalesList.stream().
                    map(Aftersales::getAftersaleId).
                    toList();
            QueryWrapper<AftersalesItem> aftersalesItemQueryWrapper = new QueryWrapper<>();
            aftersalesItemQueryWrapper.in("aftersale_id", ids);
            aftersalesItemQueryWrapper.eq("order_item_id", itemId);
            aftersalesItemQueryWrapper.select("sum(number) as number");
            AftersalesItem aftersalesItem = aftersalesItemService.getOne(aftersalesItemQueryWrapper);

            if (aftersalesItem != null) {
                sumValidNum = aftersalesItem.getNumber();
            } else {
                sumValidNum = 0;
            }
        }
        return sumValidNum;
    }


    /**
     * 退款商品金额（含税）：按（商品单价 + 单件税费）* 退款数量计算。
     * 已确认订单若存在附加规格金额，则按（单价 + 附加规格 + 单件税费）* 数量计算。
     */
    private BigDecimal calcRefundProductAmount(Order order, List<AftersalesItemVO> aftersalesItems) {
        if (order == null || CollUtil.isEmpty(aftersalesItems)) {
            return BigDecimal.ZERO;
        }
        BigDecimal refundProductAmount = aftersalesItems.stream()
                .filter(i -> i.getPrice() != null && i.getNumber() != null)
                .map(item -> {
                    BigDecimal unitPrice = item.getPrice();
                    if (order.getOrderStatus() == OrderStatusEnum.CONFIRMED.getCode() && StrUtil.isNotBlank(item.getExtraSkuData())) {
                        List<OrderExtraSkuDataDTO> extras = JSONUtil.toList(item.getExtraSkuData(), OrderExtraSkuDataDTO.class);
                        BigDecimal extraAmount = extras.stream()
                                .map(OrderExtraSkuDataDTO::getAttrPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        unitPrice = unitPrice.add(extraAmount);
                    }
                    return unitPrice.multiply(BigDecimal.valueOf(item.getNumber()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return refundProductAmount.setScale(2, RoundingMode.CEILING);
    }

    /**
     * 整单售后时恢复订单使用的优惠券
     */
    public void restoreOrderCouponUsageIfFullAftersales(Aftersales aftersales, boolean isFullAftersales) {
        if (!isFullAftersales) {
            return;
        }
        UserCoupon update = new UserCoupon();
        update.setOrderId(0);
        update.setUsedTime(0L);
        int updated = userCouponMapper.update(update,
                Wrappers.<UserCoupon>lambdaUpdate()
                        .eq(UserCoupon::getOrderId, aftersales.getOrderId())
                        .eq(UserCoupon::getUserId, aftersales.getUserId()));
        if (updated == 0) {
            Order order = orderMapper.selectById(aftersales.getOrderId());
            if (order != null && order.getParentOrderId() != null && order.getParentOrderId() > 0) {
                long activeCouponSiblings = orderMapper.selectCount(
                        Wrappers.<Order>lambdaQuery()
                                .eq(Order::getParentOrderId, order.getParentOrderId())
                                .ne(Order::getOrderId, order.getOrderId())
                                .gt(Order::getCouponAmount, BigDecimal.ZERO)
                                .ne(Order::getOrderStatus, OrderStatusEnum.CANCELLED.getCode()));
                if (activeCouponSiblings == 0) {
                    userCouponMapper.update(update,
                            Wrappers.<UserCoupon>lambdaUpdate()
                                    .eq(UserCoupon::getOrderId, order.getParentOrderId())
                                    .eq(UserCoupon::getUserId, aftersales.getUserId()));
                }
            }
        }
    }
}
