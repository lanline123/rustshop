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

package com.tigshop.service.finance.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.bo.pay.WechatPayContext;
import com.tigshop.bean.dto.finance.PaylogCreateDTO;
import com.tigshop.bean.dto.finance.PaylogUpdateDTO;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.order.PayMethodType;
import com.tigshop.bean.enums.order.PaymentStatus;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.query.finance.PaylogListPageQuery;
import com.tigshop.bean.vo.finance.PaylogVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.finance.PaylogMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.PaylogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * 交易日志服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@RequiredArgsConstructor
@Service
public class PaylogServiceImpl extends BaseServiceImpl<PaylogMapper, Paylog> implements PaylogService {

    /**
     * paylog 自有的支付状态口径：0 未支付 / 1 已支付，与 {@code PaymentStatus} 枚举不同一套
     */
    private static final int PAYLOG_STATUS_UNPAID = 0;
    private static final int PAYLOG_STATUS_PAID = 1;
    private static final String PROFIT_SHARING_CHANNEL_WECHAT = "WECHAT";
    private static final Set<String> PROFIT_SHARING_CHANNELS = Set.of("WECHAT", "HUIFU", "ALIPAY");

    private final OrderMapper orderMapper;

    @Override
    public Page<PaylogVO> list(PaylogListPageQuery pageQuery) {
        Page<Paylog> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        String sortField = pageQuery.getSortField();
        String sortColumn = ("addTime".equals(sortField) || "add_time".equals(sortField)
                || "payTime".equals(sortField) || "pay_time".equals(sortField)) ? "add_time" : "paylog_id";
        boolean asc = "asc".equalsIgnoreCase(pageQuery.getSortOrder());
        page.addOrder(asc ? OrderItem.asc(sortColumn) : OrderItem.desc(sortColumn));

        Integer payStatus = pageQuery.getPayStatus();
        String keyword = pageQuery.getKeyword();

        LambdaQueryWrapper<Paylog> wrapper = new LambdaQueryWrapper<Paylog>()
                .like(StrUtil.isNotBlank(keyword), Paylog::getOrderSn, keyword);
        // 支付状态分组：0=未支付(含支付中 0/1)，其它值=已支付及之后状态(已支付/退款中/已退款，>=2)
        if (payStatus != null && payStatus > -1) {
            if (payStatus == 0) {
                wrapper.in(Paylog::getPayStatus, 0, 1);
            } else {
                wrapper.ge(Paylog::getPayStatus, PaymentStatus.PAID.getCode());
            }
        }

        Page<Paylog> orderPage = this.page(page, wrapper);
        List<Paylog> records = orderPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        // 转换为VO
        List<PaylogVO> paylogVOList = records.stream().map(order -> {
            PaylogVO paylogVO = new PaylogVO();
            BeanUtils.copyProperties(order, paylogVO);
            if (StrUtil.isBlank(paylogVO.getPayName())) {
                paylogVO.setPayName(PayMethodType.getTypeName(order.getPayCode()));
            }
            paylogVO.setPayStatus(order.getPayStatus());
            paylogVO.setPayStatusName(PaymentStatus.getStatusName(order.getPayStatus()));

            Long time = order.getAddTime();
            if (time != null && time > 0) {
                paylogVO.setAddTime(DateUtil.format(DateUtil.date(time * 1000), "yyyy-MM-dd HH:mm:ss"));
            }

            paylogVO.setRefundAmount(order.getRefundAmount());
            return paylogVO;
        }).toList();
        return PageUtil.convertPage(orderPage, paylogVOList);
    }

    @Override
    public PaylogVO detail(Integer id) {
        if (id != null) {
            Paylog paylog = this.getById(id);
            PaylogVO paylogVO = new PaylogVO();
            BeanUtils.copyProperties(paylog, paylogVO);
            if (StrUtil.isBlank(paylogVO.getPayName())) {
                paylogVO.setPayName(PayMethodType.getTypeName(paylog.getPayCode()));
            }
            return paylogVO;
        }
        return null;
    }

    @Override
    public boolean create(PaylogCreateDTO createDTO) {
        if (createDTO != null) {
            Paylog paylog = new Paylog();
            BeanUtils.copyProperties(createDTO, paylog);
            return this.save(paylog);
        }
        return false;
    }

    @Override
    public boolean update(PaylogUpdateDTO updateDTO) {
        if (updateDTO != null) {
            Paylog paylog = new Paylog();
            BeanUtils.copyProperties(updateDTO, paylog);
            return this.updateById(paylog);
        }
        return false;
    }

    private String generatePaySn() {
        return StringUtils.getRandomString(16);
    }

    @Override
    public Paylog creatPayLog(String originPaySn, OrderVO detail, String type, Integer orderType) {
        return creatPayLog(originPaySn, detail, type, orderType, detail.getUnpaidAmount(), null);
    }

    @Override
    public Paylog creatPayLog(String originPaySn, OrderVO detail, String type, Integer orderType, BigDecimal payAmount) {
        return creatPayLog(originPaySn, detail, type, orderType, payAmount, null);
    }

    @Override
    public Paylog creatPayLog(String originPaySn, OrderVO detail, String type, Integer orderType,
                              BigDecimal payAmount, String profitSharingChannel) {
        String targetChannel = normalizeOptionalProfitSharingChannel(profitSharingChannel);
        // 查询该订单的最新一条支付日志（按id降序取第一条）
        Paylog latestPayLog = this.getOne(new LambdaQueryWrapper<Paylog>()
                .eq(Paylog::getOrderType, orderType)
                .eq(Paylog::getOrderId, detail.getOrderId())
                .eq(Paylog::getOrderSn, detail.getOrderSn())
                .eq(Paylog::getPayCode, type)
                .orderByDesc(Paylog::getPaylogId)
                .last("LIMIT 1"));

        if (latestPayLog != null && ObjectUtil.notEqual(latestPayLog.getPayCode(), "wechat")) {
            // 未支付,如果是定金预售
            Integer presaleType = detail.getPresaleType();
            if(Objects.equals(presaleType, 2)){
                //定金预售不进行判断,存在多条支付log
            }else{
                if (latestPayLog.getPayStatus() == 0) {
                    if (sameProfitSharingChannel(latestPayLog.getProfitSharingChannel(), targetChannel)) {
                        return latestPayLog;
                    }
                }
                if (latestPayLog.getPayStatus() == 1) {
                    throw new GlobalException("订单已支付");
                }
            }
        }

        return createPaylog(originPaySn, detail, type, orderType, payAmount, targetChannel);
    }

    private Paylog createPaylog(String originPaySn, OrderVO detail, String type, Integer orderType,
                                BigDecimal payAmount, String profitSharingChannel) {
        Paylog paylog = new Paylog();
        paylog.setPaySn(originPaySn != null ? originPaySn : generatePaySn());
        paylog.setOrderId(detail.getOrderId());
        paylog.setOrderSn(detail.getOrderSn());
        paylog.setOrderAmount(detail.getTotalAmount());
        paylog.setPayAmount(payAmount);
        paylog.setOrderType(orderType);
        paylog.setPayStatus(PAYLOG_STATUS_UNPAID);
        paylog.setAddTime(StringUtils.getCurrentTime());
        paylog.setNotifyData("");
        paylog.setPayCode(type);
        paylog.setProfitSharingChannel(profitSharingChannel);
        try {
            boolean isSaved = this.save(paylog);
            if (isSaved) {
                return paylog;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Override
    public void bindProfitSharingChannel(String paySn, String channel) {
        String targetChannel = requireProfitSharingChannel(channel);
        if (StrUtil.isBlank(paySn)) {
            throw new GlobalException("支付号不能为空");
        }

        Paylog existing = getLatestByPaySn(paySn);
        if (existing == null) {
            throw new GlobalException("支付日志不存在");
        }
        bindProfitSharingChannel(existing, targetChannel);
    }

    private void bindProfitSharingChannel(Paylog existing, String targetChannel) {
        if (StrUtil.isNotBlank(existing.getProfitSharingChannel())) {
            if (targetChannel.equals(existing.getProfitSharingChannel())) {
                return;
            }
            throw new GlobalException("支付日志分账渠道快照已绑定，禁止覆盖");
        }
        if (!Integer.valueOf(PAYLOG_STATUS_UNPAID).equals(existing.getPayStatus())) {
            throw new GlobalException("仅待支付日志允许绑定分账渠道快照");
        }

        int updated = this.baseMapper.update(null, new LambdaUpdateWrapper<Paylog>()
                .set(Paylog::getProfitSharingChannel, targetChannel)
                .eq(Paylog::getPaylogId, existing.getPaylogId())
                .eq(Paylog::getPaySn, existing.getPaySn())
                .eq(Paylog::getPayStatus, PAYLOG_STATUS_UNPAID)
                .and(wrapper -> wrapper.isNull(Paylog::getProfitSharingChannel)
                        .or().eq(Paylog::getProfitSharingChannel, "")));
        if (updated > 0) {
            return;
        }

        Paylog concurrent = getByPaylogIdAndPaySn(existing.getPaylogId(), existing.getPaySn());
        if (concurrent != null && targetChannel.equals(concurrent.getProfitSharingChannel())) {
            return;
        }
        throw new GlobalException("支付日志分账渠道快照绑定冲突");
    }

    @Override
    public Paylog getLastByOrderId(Integer orderId, Integer orderType) {
        // orderType 必须同时限定：Paylog 商品订单(0)/充值单(1)共表，orderId 跨类型会撞号
        return this.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getOrderId, orderId).eq(Paylog::getOrderType, orderType).orderByDesc(Paylog::getPaylogId).last("LIMIT 1"));
    }

    @Override
    public Paylog getPaidProfitSharingPaylog(Integer orderId) {
        if (orderId == null) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<Paylog>()
                .eq(Paylog::getOrderId, orderId)
                .eq(Paylog::getOrderType, 0)
                .eq(Paylog::getPayStatus, PAYLOG_STATUS_PAID)
                .gt(Paylog::getPayAmount, BigDecimal.ZERO)
                .in(Paylog::getProfitSharingChannel, PROFIT_SHARING_CHANNELS)
                .orderByDesc(Paylog::getPaylogId)
                .last("LIMIT 1"));
    }

    private Paylog getLatestByPaySn(String paySn) {
        return this.getOne(new LambdaQueryWrapper<Paylog>()
                .eq(Paylog::getPaySn, paySn)
                .orderByDesc(Paylog::getPaylogId)
                .last("LIMIT 1"));
    }

    private Paylog getByPaylogIdAndPaySn(Integer paylogId, String paySn) {
        return this.getOne(new LambdaQueryWrapper<Paylog>()
                .eq(Paylog::getPaylogId, paylogId)
                .eq(Paylog::getPaySn, paySn)
                .last("FOR UPDATE"));
    }

    private String normalizeOptionalProfitSharingChannel(String channel) {
        if (StrUtil.isBlank(channel)) {
            return null;
        }
        return requireProfitSharingChannel(channel);
    }

    private String requireProfitSharingChannel(String channel) {
        if (channel == null || !PROFIT_SHARING_CHANNELS.contains(channel)) {
            throw new GlobalException("分账渠道快照只允许 WECHAT/HUIFU/ALIPAY");
        }
        return channel;
    }

    private boolean sameProfitSharingChannel(String existing, String target) {
        String normalizedExisting = StrUtil.isBlank(existing) ? null : existing;
        return Objects.equals(normalizedExisting, target);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSplitPaylogs(WechatPayContext context, OrderVO parent, String payCode) {
        if (context == null || parent == null || context.isDirect() || !context.hasSubOrders()) {
            return;
        }
        if (parent.getOrderId() == null || StrUtil.isBlank(parent.getPaySn())) {
            throw new GlobalException("父订单支付信息无效，请重新发起支付");
        }
        if (StrUtil.isBlank(context.getCombineOutTradeNo())) {
            context.setCombineOutTradeNo(parent.getPaySn());
        }
        for (WechatPayContext.SubOrderCtx sub : context.getSubOrders()) {
            if (sub.getOrderId() == null) {
                throw new GlobalException("子订单支付信息无效，请重新发起支付");
            }
            Order child = requireWechatSplitChild(parent, sub);
            // 用户重复点击支付时复用既有待支付行，保证号段稳定且不产生重复行
            Paylog existing = this.getOne(new LambdaQueryWrapper<Paylog>()
                    .eq(Paylog::getOrderType, 0)
                    .eq(Paylog::getOrderId, sub.getOrderId())
                    .eq(Paylog::getOrderSn, sub.getOrderSn())
                    .eq(Paylog::getPayCode, payCode)
                    .eq(Paylog::getPayStatus, PAYLOG_STATUS_UNPAID)
                    .orderByDesc(Paylog::getPaylogId)
                    .last("LIMIT 1"));
            if (existing != null) {
                if (!Integer.valueOf(PAYLOG_STATUS_UNPAID).equals(existing.getPayStatus())) {
                    throw new GlobalException("子订单支付状态已变化，请重新发起支付");
                }
                bindProfitSharingChannel(existing, PROFIT_SHARING_CHANNEL_WECHAT);
                sub.setPaySn(existing.getPaySn());
                continue;
            }

            Paylog nonReusable = this.getOne(new LambdaQueryWrapper<Paylog>()
                    .eq(Paylog::getOrderType, 0)
                    .eq(Paylog::getOrderId, sub.getOrderId())
                    .eq(Paylog::getOrderSn, sub.getOrderSn())
                    .eq(Paylog::getPayCode, payCode)
                    .orderByDesc(Paylog::getPaylogId)
                    .last("LIMIT 1"));
            if (nonReusable != null) {
                throw new GlobalException("子订单支付状态已变化，请重新发起支付");
            }

            OrderVO childDetail = new OrderVO();
            childDetail.setOrderId(child.getOrderId());
            childDetail.setOrderSn(child.getOrderSn());
            childDetail.setTotalAmount(child.getTotalAmount());
            childDetail.setUnpaidAmount(sub.getAmount());
            Paylog paylog = createPaylog(null, childDetail, payCode, 0,
                    sub.getAmount(), PROFIT_SHARING_CHANNEL_WECHAT);
            if (paylog == null || StrUtil.isBlank(paylog.getPaySn())
                    || !Objects.equals(child.getOrderId(), paylog.getOrderId())
                    || !Objects.equals(child.getOrderSn(), paylog.getOrderSn())
                    || !Integer.valueOf(0).equals(paylog.getOrderType())
                    || !Objects.equals(payCode, paylog.getPayCode())
                    || !Integer.valueOf(PAYLOG_STATUS_UNPAID).equals(paylog.getPayStatus())
                    || !PROFIT_SHARING_CHANNEL_WECHAT.equals(paylog.getProfitSharingChannel())) {
                throw new GlobalException("子订单支付日志创建失败，请重新发起支付");
            }
            sub.setPaySn(paylog.getPaySn());
        }
    }

    private Order requireWechatSplitChild(OrderVO parent, WechatPayContext.SubOrderCtx sub) {
        Order child = orderMapper.selectById(sub.getOrderId());
        if (child == null
                || !Objects.equals(parent.getOrderId(), child.getParentOrderId())
                || !Objects.equals(sub.getOrderSn(), child.getOrderSn())
                || !Objects.equals(child.getOrderStatus(), OrderStatusEnum.PENDING.getCode())
                || !Objects.equals(child.getPayStatus(), PaymentStatus.UNPAID.getCode())
                || !Integer.valueOf(0).equals(child.getIsDel())) {
            throw new GlobalException("子订单归属或状态已变化，请重新发起支付");
        }
        if (sub.getAmount() == null || sub.getAmount().compareTo(BigDecimal.ZERO) <= 0
                || child.getTotalAmount() == null || child.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0
                || child.getUnpaidAmount() == null || child.getUnpaidAmount().compareTo(BigDecimal.ZERO) <= 0
                || sub.getAmount().compareTo(child.getUnpaidAmount()) != 0) {
            throw new GlobalException("子订单金额已变化，请重新发起支付");
        }
        return child;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int fillCombineChildTransactionIds(String parentPaySn, String payCode, Map<String, String> transactionIdByPaySn) {
        Map<String, String> mapping = transactionIdByPaySn == null ? Map.of() : transactionIdByPaySn;
        return fillChildTransactionIds(parentPaySn, payCode, childPaylog -> mapping.get(childPaylog.getPaySn()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int fillPartnerChildTransactionIds(String parentPaySn, String payCode, String transactionId) {
        return fillChildTransactionIds(parentPaySn, payCode, childPaylog -> transactionId);
    }

    /**
     * 以父单为锚遍历有效子单写号：只写真正属于该父单的行，回调里出现的陌生商户单号不会被误写。
     *
     * @param transactionIdResolver 按子单 paylog 给出应写入的交易号，返回空表示本次回调未覆盖该子单
     * @return 仍未拿到交易号的有效子单数
     */
    private int fillChildTransactionIds(String parentPaySn, String payCode, Function<Paylog, String> transactionIdResolver) {
        if (StrUtil.isBlank(parentPaySn)) {
            return 0;
        }
        Paylog parent = this.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, parentPaySn));
        if (parent == null || parent.getOrderId() == null) {
            return 0;
        }
        // 有效子单口径与 WechatPayModeResolver 建行时一致，避免建了行却漏写号
        List<Order> children = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getParentOrderId, parent.getOrderId())
                .ne(Order::getOrderStatus, OrderStatusEnum.CANCELLED.getCode())
                .eq(Order::getIsDel, 0));
        int missing = 0;
        for (Order child : children) {
            Paylog childPaylog = this.getOne(new LambdaQueryWrapper<Paylog>()
                    .eq(Paylog::getOrderId, child.getOrderId())
                    // 合单子单均为商品订单，限定 orderType=0 避免撞充值单号
                    .eq(Paylog::getOrderType, 0)
                    .eq(Paylog::getPayCode, payCode)
                    .orderByDesc(Paylog::getPaylogId)
                    .last("LIMIT 1"));
            if (childPaylog == null) {
                missing++;
                continue;
            }
            if (StrUtil.isNotBlank(childPaylog.getTransactionId())) {
                // 首次回调已写号，重复投递不覆盖
                continue;
            }
            String transactionId = transactionIdResolver.apply(childPaylog);
            if (StrUtil.isBlank(transactionId)) {
                missing++;
                continue;
            }
            Paylog update = new Paylog();
            update.setPaylogId(childPaylog.getPaylogId());
            update.setTransactionId(transactionId);
            update.setPayStatus(PAYLOG_STATUS_PAID);
            this.updateById(update);
        }
        return missing;
    }
}
