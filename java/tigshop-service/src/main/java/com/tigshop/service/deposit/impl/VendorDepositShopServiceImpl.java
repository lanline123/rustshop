package com.tigshop.service.deposit.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.enums.deposit.DepositLogType;
import com.tigshop.bean.enums.deposit.DepositOwnerType;
import com.tigshop.bean.model.deposit.ShopDeposit;
import com.tigshop.bean.model.deposit.ShopDepositLog;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.param.deposit.DepositShopLogQueryParam;
import com.tigshop.bean.vo.deposit.DepositShopFrozenDetailVO;
import com.tigshop.bean.vo.deposit.DepositShopLogVO;
import com.tigshop.bean.vo.deposit.DepositShopOverviewVO;
import com.tigshop.bean.vo.deposit.DepositShopProtocolVO;
import com.tigshop.bean.vo.deposit.DepositShopWarningVO;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.deposit.ShopDepositLogMapper;
import com.tigshop.mapper.deposit.ShopDepositMapper;
import com.tigshop.service.deposit.DepositAccountService;
import com.tigshop.service.deposit.DepositRefundService;
import com.tigshop.service.deposit.VendorDepositShopService;
import com.tigshop.service.deposit.support.DepositShopLogEnricher;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 供应商端保证金聚合服务实现（只读展示）。
 *
 * <p>与店铺端 {@link DepositShopServiceImpl} 平行，仅把持有者维度由 SHOP/shopId 换成
 * SUPPLIER/vendorId。账户三数与应缴/已缴均通过只读调用 {@link DepositAccountService} 的
 * owner 版方法获取；为避免在一次总览加载里重复触发应缴规则匹配（computeRequired 会扫描在售
 * 商品类目），这里调用一次 computeRequired/getPaid，再按已锁定设计的同一组公式派生
 * 冻结/可提现/待补缴：冻结=min(已缴,应缴)、可提现=max(0,已缴-应缴)、待补缴=max(0,应缴-已缴)。</p>
 *
 * @author Tigshop团队
 */
@Service
public class VendorDepositShopServiceImpl implements VendorDepositShopService {

    /** 协议开关配置键。 */
    private static final String CONFIG_PROTOCOL_ENABLE = "deposit_protocol_enable";

    /** 协议内容配置键。 */
    private static final String CONFIG_PROTOCOL_CONTENT = "deposit_protocol_content";

    /** 当前持有者类型：供应商。 */
    private static final Integer OWNER_TYPE = DepositOwnerType.SUPPLIER.getCode();

    @Resource
    private DepositAccountService depositAccountService;

    @Resource
    private DepositRefundService depositRefundService;

    @Resource
    private ShopDepositLogMapper shopDepositLogMapper;

    @Resource
    private ShopDepositMapper shopDepositMapper;

    @Resource
    private ConfigService configService;

    @Resource
    private DepositShopLogEnricher depositShopLogEnricher;

    @Override
    public DepositShopOverviewVO getOverview(Integer vendorId) {
        BigDecimal required = nullToZero(depositAccountService.computeRequired(OWNER_TYPE, vendorId));
        BigDecimal paid = nullToZero(depositAccountService.getPaid(OWNER_TYPE, vendorId));
        BigDecimal frozen = paid.min(required);
        BigDecimal withdrawable = max0(paid.subtract(required));
        BigDecimal pending = max0(required.subtract(paid));
        // 待审核提现金额：owner 维度全部待审核退还/提现申请金额合计（仅展示，不参与可提现扣减）
        BigDecimal pendingWithdraw = nullToZero(depositRefundService.pendingRefundSum(OWNER_TYPE, vendorId));

        ShopDeposit account = findAccount(vendorId);
        Integer status = account != null ? account.getStatus() : 1;
        Integer penaltyStatus = account != null && account.getPenaltyStatus() != null ? account.getPenaltyStatus() : 0;
        Long supplementDueTime = account != null && account.getSupplementDueTime() != null ? account.getSupplementDueTime() : 0L;

        DepositShopProtocolVO protocol = getProtocol();
        boolean hasProtocol = Boolean.TRUE.equals(protocol.getEnable()) && StrUtil.isNotBlank(protocol.getContent());

        DepositShopOverviewVO vo = new DepositShopOverviewVO();
        vo.setRequired(required);
        vo.setPaid(paid);
        vo.setPending(pending);
        vo.setWithdrawable(withdrawable);
        vo.setPendingWithdraw(pendingWithdraw);
        vo.setFrozen(frozen);
        vo.setStatus(status);
        vo.setPenaltyStatus(penaltyStatus);
        vo.setStatusText(resolveStatusText(penaltyStatus, pending));
        vo.setSupplementDueTime(supplementDueTime);
        vo.setSupplementDueTimeText(TigUtils.handelTime(supplementDueTime));
        vo.setHasProtocol(hasProtocol);
        return vo;
    }

    @Override
    public Page<DepositShopLogVO> logList(Integer vendorId, DepositShopLogQueryParam param) {
        Page<ShopDepositLog> page = new Page<>(param.getPage(), param.getSize());
        LambdaQueryWrapper<ShopDepositLog> wrapper = Wrappers.<ShopDepositLog>lambdaQuery()
                .eq(ShopDepositLog::getOwnerType, OWNER_TYPE)
                .eq(ShopDepositLog::getOwnerId, vendorId);
        if (param.getType() != null) {
            if (DepositLogType.isWithdrawCategory(param.getType())) {
                wrapper.in(ShopDepositLog::getType, DepositLogType.WITHDRAW.getCode(), DepositLogType.REFUND.getCode());
            } else {
                wrapper.eq(ShopDepositLog::getType, param.getType());
            }
        }
        wrapper.ge(param.getStartTime() != null, ShopDepositLog::getAddTime, param.getStartTime());
        wrapper.le(param.getEndTime() != null, ShopDepositLog::getAddTime, param.getEndTime());
        String tradeNoStr = param.getTradeNo() == null ? "" : param.getTradeNo().trim();
        if (!tradeNoStr.isEmpty() && tradeNoStr.matches("\\d+")) {
            Integer tradeNo = Integer.valueOf(tradeNoStr);
            wrapper.and(w -> w.eq(ShopDepositLog::getOrderId, tradeNo)
                    .or().eq(ShopDepositLog::getRelatedId, tradeNo));
        }
        wrapper.orderByDesc(ShopDepositLog::getShopDepositLogId);
        Page<ShopDepositLog> logPage = shopDepositLogMapper.selectPage(page, wrapper);
        List<DepositShopLogVO> records = logPage.getRecords().stream().map(log -> {
            DepositShopLogVO vo = new DepositShopLogVO();
            vo.setShopDepositLogId(log.getShopDepositLogId());
            vo.setType(DepositLogType.normalizeCode(log.getType()));
            vo.setTypeText(DepositLogType.getDescByCode(log.getType()));
            vo.setChangeAmount(log.getChangeAmount());
            vo.setBeforePaid(log.getBeforePaid());
            vo.setAfterPaid(log.getAfterPaid());
            vo.setOrderId(log.getOrderId());
            vo.setRelatedId(log.getRelatedId());
            vo.setRemark(log.getRemark());
            vo.setAddTime(log.getAddTime());
            vo.setAddTimeText(TigUtils.handelTime(log.getAddTime()));
            return vo;
        }).toList();
        depositShopLogEnricher.fillSourceDestAccounts(logPage.getRecords(), records);
        Page<DepositShopLogVO> result = new Page<>(logPage.getCurrent(), logPage.getSize(), logPage.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public Page<DepositShopFrozenDetailVO> frozenDetailList(Integer vendorId, DepositShopLogQueryParam param) {
        Page<ShopDepositLog> page = new Page<>(param.getPage(), param.getSize());
        // 冻结明细：仅展示本供应商平台扣款记录(DEDUCT)，可按业务编号/时间筛选
        LambdaQueryWrapper<ShopDepositLog> wrapper = Wrappers.<ShopDepositLog>lambdaQuery()
                .eq(ShopDepositLog::getOwnerType, OWNER_TYPE)
                .eq(ShopDepositLog::getOwnerId, vendorId)
                .eq(ShopDepositLog::getType, DepositLogType.DEDUCT.getCode());
        wrapper.ge(param.getStartTime() != null, ShopDepositLog::getAddTime, param.getStartTime());
        wrapper.le(param.getEndTime() != null, ShopDepositLog::getAddTime, param.getEndTime());
        // 业务编号：按订单号 / 关联单号精确匹配
        String tradeNoStr = param.getTradeNo() == null ? "" : param.getTradeNo().trim();
        if (!tradeNoStr.isEmpty() && tradeNoStr.matches("\\d+")) {
            Integer tradeNo = Integer.valueOf(tradeNoStr);
            wrapper.and(w -> w.eq(ShopDepositLog::getOrderId, tradeNo)
                    .or().eq(ShopDepositLog::getRelatedId, tradeNo));
        }
        wrapper.orderByDesc(ShopDepositLog::getShopDepositLogId);
        Page<ShopDepositLog> logPage = shopDepositLogMapper.selectPage(page, wrapper);
        List<DepositShopFrozenDetailVO> records = logPage.getRecords().stream().map(log -> {
            DepositShopFrozenDetailVO vo = new DepositShopFrozenDetailVO();
            vo.setShopDepositLogId(log.getShopDepositLogId());
            vo.setType(DepositLogType.normalizeCode(log.getType()));
            vo.setTypeText(DepositLogType.getDescByCode(log.getType()));
            vo.setChangeAmount(log.getChangeAmount());
            vo.setDeductAmount(log.getChangeAmount() == null ? BigDecimal.ZERO : log.getChangeAmount().abs());
            vo.setReason(log.getRemark());
            vo.setOrderId(log.getOrderId());
            vo.setRelatedId(log.getRelatedId());
            vo.setAddTime(log.getAddTime());
            vo.setAddTimeText(TigUtils.handelTime(log.getAddTime()));
            return vo;
        }).toList();
        Page<DepositShopFrozenDetailVO> result = new Page<>(logPage.getCurrent(), logPage.getSize(), logPage.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public DepositShopWarningVO getWarning(Integer vendorId) {
        BigDecimal pending = nullToZero(depositAccountService.pending(OWNER_TYPE, vendorId));
        ShopDeposit account = findAccount(vendorId);
        Long supplementDueTime = account != null && account.getSupplementDueTime() != null ? account.getSupplementDueTime() : 0L;
        Long collectDueTime = account != null && account.getCollectDueTime() != null ? account.getCollectDueTime() : 0L;
        boolean needWarning = pending.compareTo(BigDecimal.ZERO) > 0;
        int bufferDays = resolveBufferDays(collectDueTime, supplementDueTime);
        List<String> messages = buildWarningMessages(needWarning, collectDueTime, supplementDueTime, "供应商");

        DepositShopWarningVO vo = new DepositShopWarningVO();
        vo.setNeedWarning(needWarning);
        vo.setPendingAmount(pending);
        vo.setSupplementDueTime(supplementDueTime);
        vo.setSupplementDueTimeText(TigUtils.handelTime(supplementDueTime));
        vo.setBufferDays(bufferDays);
        vo.setMessages(messages);
        vo.setMessage(String.join("\n", messages));
        return vo;
    }

    /**
     * 缓冲缴存期剩余天数：收取缓冲(collectDueTime)优先，否则补缴缓冲(supplementDueTime)；
     * 时间戳为 0/已过期则返回 0。向上取整到整天。
     */
    private static int resolveBufferDays(Long collectDueTime, Long supplementDueTime) {
        long due = collectDueTime != null && collectDueTime > 0 ? collectDueTime
                : supplementDueTime != null && supplementDueTime > 0 ? supplementDueTime : 0L;
        return remainDays(due, System.currentTimeMillis() / 1000L);
    }

    /**
     * 剩余天数：向上取整到整天；dueTime=0/已过期返回 0。
     */
    private static int remainDays(long dueTime, long now) {
        if (dueTime <= 0) {
            return 0;
        }
        long remain = dueTime - now;
        if (remain <= 0) {
            return 0;
        }
        return (int) Math.ceil(remain / 86400.0);
    }

    /**
     * 构造预警文案列表：待补缴&gt;0 时，按账户当前状态匹配<b>单条</b>场景文案，
     * 优先级：超期未缴足 &gt; 补缴差额 &gt; 收取规则。
     *
     * <ul>
     *   <li>场景3 超期未缴足：任一缓冲已启动且已过期（due&gt;0 且 due&lt;=now）→ 立即补缴提示。</li>
     *   <li>场景2 补缴差额：补缴缓冲未过期（supplementDueTime&gt;now）→ 剩余天数补齐差额。</li>
     *   <li>场景1 收取规则：收取缓冲未过期（collectDueTime&gt;now）→ 剩余天数缓冲缴存期。</li>
     *   <li>兜底：待补缴&gt;0 但两缓冲均未启动 → 立即补缴提示。</li>
     * </ul>
     *
     * @param entity 主体名称（"店铺" / "供应商"），用于区分店铺端与供应商端文案。
     */
    private static List<String> buildWarningMessages(boolean needWarning, Long collectDueTime,
            Long supplementDueTime, String entity) {
        List<String> messages = new ArrayList<>();
        if (!needWarning) {
            return messages;
        }
        long now = System.currentTimeMillis() / 1000L;
        long collectDue = collectDueTime == null ? 0L : collectDueTime;
        long supplementDue = supplementDueTime == null ? 0L : supplementDueTime;
        boolean collectExpired = collectDue > 0 && collectDue <= now;
        boolean supplementExpired = supplementDue > 0 && supplementDue <= now;
        // 按当前状态匹配单条文案，优先级：超期未缴足 > 补缴差额 > 收取规则
        if (collectExpired || supplementExpired) {
            messages.add("超过限缴存周期仍未缴足保证金，当前无法发布全新商品(根据设置)，存量商品可正常成交，请立即补缴");
        } else if (supplementDue > now) {
            messages.add(entity + "新增主营类目，所需保证金额度提升，请在 " + remainDays(supplementDue, now)
                    + " 日内补齐差额，避免权限受限");
        } else if (collectDue > now) {
            messages.add("上架商品 / 产生首单触发保证金收取规则，您尚有 " + remainDays(collectDue, now)
                    + " 天缓冲缴存期，逾期将限制" + entity + "经营");
        } else {
            // 兜底：待补缴>0 但两缓冲均未启动 → 提示立即补缴
            messages.add("超过限缴存周期仍未缴足保证金，当前无法发布全新商品(根据设置)，存量商品可正常成交，请立即补缴");
        }
        return messages;
    }

    @Override
    public DepositShopProtocolVO getProtocol() {
        DepositShopProtocolVO vo = new DepositShopProtocolVO();
        String enableVal = readConfig(CONFIG_PROTOCOL_ENABLE);
        vo.setEnable(parseBool(enableVal));
        vo.setContent(readConfig(CONFIG_PROTOCOL_CONTENT));
        return vo;
    }

    /**
     * 只读读取供应商保证金账户（不存在返回 null，不创建行）。
     */
    private ShopDeposit findAccount(Integer vendorId) {
        return shopDepositMapper.selectOne(
                Wrappers.<ShopDeposit>lambdaQuery()
                        .eq(ShopDeposit::getOwnerType, OWNER_TYPE)
                        .eq(ShopDeposit::getOwnerId, vendorId));
    }

    /**
     * 防御式读取保证金配置键值：以独立配置项 bizCode 存储。
     *
     * <p>【假设/待确认】沿用 ConfigService 现有键值机制，按 CR §4 的扁平键
     * （deposit_protocol_enable / deposit_protocol_content）作为 ConfigPO.bizCode 独立存储。
     * 协议为平台级配置，店铺端与供应商端共用同一组键。getConfigByCode 在配置不存在时抛异常，
     * 这里捕获并返回 null 以保证页面不报错。</p>
     */
    private String readConfig(String bizCode) {
        try {
            ConfigPO configPO = configService.getConfigByCode(bizCode);
            return configPO == null ? null : configPO.getBizVal();
        } catch (Exception e) {
            return null;
        }
    }

    private static String resolveStatusText(Integer penaltyStatus, BigDecimal pending) {
        if (penaltyStatus != null && penaltyStatus > 0) {
            return "已处罚";
        }
        if (pending != null && pending.compareTo(BigDecimal.ZERO) > 0) {
            return "待补缴";
        }
        return "正常";
    }

    private static boolean parseBool(String value) {
        if (StrUtil.isBlank(value)) {
            return false;
        }
        return "1".equals(value.trim()) || "true".equalsIgnoreCase(value.trim());
    }

    private static BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static BigDecimal max0(BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : value;
    }
}
