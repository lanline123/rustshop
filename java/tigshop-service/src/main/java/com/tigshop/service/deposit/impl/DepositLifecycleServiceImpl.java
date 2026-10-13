package com.tigshop.service.deposit.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.enums.deposit.DepositAdjustSource;
import com.tigshop.bean.enums.deposit.DepositOwnerType;
import com.tigshop.bean.enums.deposit.DepositPenaltyAction;
import com.tigshop.bean.model.deposit.ShopDeposit;
import com.tigshop.bean.model.deposit.ShopDepositAdjustLog;
import com.tigshop.bean.model.deposit.ShopDepositOffshelfLog;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.vo.deposit.DepositSettingVO;
import com.tigshop.mapper.deposit.ShopDepositAdjustLogMapper;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.mapper.deposit.ShopDepositMapper;
import com.tigshop.mapper.deposit.ShopDepositOffshelfLogMapper;
import com.tigshop.service.deposit.DepositAccountService;
import com.tigshop.service.deposit.DepositLifecycleService;
import com.tigshop.service.deposit.DepositSettingService;
import com.tigshop.service.product.ProductService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 保证金生命周期服务实现（W5）。统一 owner 维度（店铺 + 供应商）。
 *
 * <p>处罚/恢复均按 owner 行锁（{@code selectForUpdateByOwner}）+ 单账户独立事务执行，单账户失败不影响其余；
 * 所有动作幂等，部分失败可由次日定时任务重跑自愈。</p>
 *
 * <p>商品下架/上架：店铺走核心 {@link ProductService}（{@code product.product_status}）；供应商走
 * 本模块自有 SQL（{@code vendor_product.product_state}），不引用 vendor 模块 Java 类型，使本模块在
 * vendor feature 关闭时仍可独立编译/运行。</p>
 *
 * @author Tigshop团队
 * @create 2026-06-10
 */
@Slf4j
@Service
public class DepositLifecycleServiceImpl implements DepositLifecycleService {

    private static final long SECONDS_PER_DAY = 86400L;

    @Resource
    private ShopDepositMapper shopDepositMapper;

    @Resource
    private ShopDepositOffshelfLogMapper shopDepositOffshelfLogMapper;

    @Resource
    private ShopDepositAdjustLogMapper shopDepositAdjustLogMapper;

    @Resource
    private DepositAccountService depositAccountService;

    @Resource
    private DepositSettingService depositSettingService;

    @Resource
    private ProductService productService;

    @Resource
    private DepositRuleMatcher depositRuleMatcher;

    private final TransactionTemplate transactionTemplate;

    public DepositLifecycleServiceImpl(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void onCollectTrigger(Integer ownerType, Integer ownerId, Integer triggerNode) {
        try {
            if (ownerType == null || ownerId == null) {
                return;
            }
            DepositSettingVO setting = depositSettingService.detail();
            if (!isEnabled(setting)) {
                return;
            }
            // 仅在本次触发节点与全局配置的收取节点一致时才处理
            if (setting.getDepositCollectNode() == null || !setting.getDepositCollectNode().equals(triggerNode)) {
                return;
            }
            // 首单收取语义：命中收取节点即"激活收取"。激活前 computeRequired 恒为 0（首单前不产生义务、
            // 不建号、概览不显示待补缴）；激活后才按类目算应缴。节点=上架收在上架挂钩激活，节点=首单收在首单挂钩激活。
            // 仅当按类目现算确有应缴(>0)才激活建号，避免为无命中规则的 owner 建空账户。
            final BigDecimal rawRequired = depositRuleMatcher.maxRequired(ownerType, ownerId);
            final boolean[] firstActivation = {false};
            transactionTemplate.executeWithoutResult(status -> {
                ShopDeposit account = shopDepositMapper.selectForUpdateByOwner(ownerType, ownerId);
                boolean activated = account != null && Integer.valueOf(1).equals(account.getCollectActivated());
                if (activated) {
                    return;
                }
                if (rawRequired.compareTo(BigDecimal.ZERO) <= 0) {
                    // 无应缴：不激活、不建号
                    return;
                }
                if (account == null) {
                    depositAccountService.getOrCreate(ownerType, ownerId);
                    account = shopDepositMapper.selectForUpdateByOwner(ownerType, ownerId);
                    if (account == null) {
                        return;
                    }
                }
                long now = nowSeconds();
                account.setCollectActivated(1);
                account.setUpdateTime(now);
                shopDepositMapper.updateById(account);
                firstActivation[0] = true;
            });
            if (firstActivation[0]) {
                // 首次激活：建立应缴快照 + 写"创建保证金"调整记录（激活后 computeRequired 方按类目算）
                reconcileRequired(ownerType, ownerId, DepositAdjustSource.CATEGORY_CHANGE);
            }
            // 激活后再算欠缴：已足额无需收取
            if (depositAccountService.pending(ownerType, ownerId).compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }
            final int bufferDays = nvl(setting.getDepositCollectBufferDays(), 0);
            transactionTemplate.executeWithoutResult(status -> {
                ShopDeposit account = shopDepositMapper.selectForUpdateByOwner(ownerType, ownerId);
                if (account == null) {
                    return;
                }
                Long collectDue = account.getCollectDueTime();
                if (collectDue != null && collectDue > 0) {
                    // 已启动收取缓冲，幂等不重复
                    return;
                }
                long now = nowSeconds();
                account.setCollectDueTime(now + (long) bufferDays * SECONDS_PER_DAY);
                account.setUpdateTime(now);
                shopDepositMapper.updateById(account);
            });
        } catch (Exception e) {
            // 收取节点挂钩绝不阻断上架/下单主流程：吞掉异常仅告警
            log.warn("保证金收取节点触发处理失败(已忽略,不影响主流程) ownerType={} ownerId={} node={}",
                    ownerType, ownerId, triggerNode, e);
        }
    }

    @Override
    public void onSupplementTrigger(Integer ownerType, Integer ownerId) {
        try {
            if (ownerType == null || ownerId == null) {
                return;
            }
            DepositSettingVO setting = depositSettingService.detail();
            if (!isEnabled(setting)) {
                return;
            }
            // 无待补缴无需启动补缴缓冲
            if (depositAccountService.pending(ownerType, ownerId).compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }
            final int bufferDays = nvl(setting.getDepositSupplementBufferDays(), 0);
            // 确保账户存在（owner 维度），再行锁启动补缴缓冲
            depositAccountService.getOrCreate(ownerType, ownerId);
            transactionTemplate.executeWithoutResult(status -> {
                ShopDeposit account = shopDepositMapper.selectForUpdateByOwner(ownerType, ownerId);
                if (account == null) {
                    return;
                }
                Long supplementDue = account.getSupplementDueTime();
                if (supplementDue != null && supplementDue > 0) {
                    // 已启动补缴缓冲，幂等不重启不重置（先到先处罚，后面的不需要了）
                    return;
                }
                long now = nowSeconds();
                account.setSupplementDueTime(now + (long) bufferDays * SECONDS_PER_DAY);
                account.setUpdateTime(now);
                shopDepositMapper.updateById(account);
            });
        } catch (Exception e) {
            // 补缴触发挂钩绝不阻断上下架/调整/扣除主流程：吞掉异常仅告警
            log.warn("保证金补缴缓冲触发处理失败(已忽略,不影响主流程) ownerType={} ownerId={}",
                    ownerType, ownerId, e);
        }
    }

    @Override
    public void onCategoryChange(Integer ownerType, Integer ownerId) {
        try {
            if (ownerType == null || ownerId == null || ownerId <= 0) {
                return;
            }
            DepositSettingVO setting = depositSettingService.detail();
            if (!isEnabled(setting)) {
                return;
            }
            reconcileRequired(ownerType, ownerId, DepositAdjustSource.CATEGORY_CHANGE);
        } catch (Exception e) {
            // 类目变更记录绝不阻断上下架主流程：吞掉异常仅告警
            log.warn("保证金类目变更应缴记录处理失败(已忽略,不影响主流程) ownerType={} ownerId={}",
                    ownerType, ownerId, e);
        }
    }

    @Override
    public void onDepositRuleChange() {
        try {
            DepositSettingVO setting = depositSettingService.detail();
            if (!isEnabled(setting)) {
                return;
            }
            List<ShopDeposit> accounts = shopDepositMapper.selectList(null);
            if (accounts == null || accounts.isEmpty()) {
                return;
            }
            for (ShopDeposit account : accounts) {
                Integer ownerType = account.getOwnerType();
                Integer ownerId = account.getOwnerId();
                if (ownerType == null || ownerId == null || ownerId <= 0) {
                    continue;
                }
                try {
                    reconcileRequired(ownerType, ownerId, DepositAdjustSource.RULE_CHANGE);
                } catch (Exception e) {
                    log.warn("保证金规则调整对账单账户失败(已忽略) ownerType={} ownerId={}", ownerType, ownerId, e);
                }
            }
        } catch (Exception e) {
            // 规则调整对账绝不阻断规则保存主流程：吞掉异常仅告警
            log.warn("保证金规则调整对账处理失败(已忽略,不影响主流程)", e);
        }
    }

    /**
     * 应缴对账：重算 owner 当前应缴，与账户 {@code required_snapshot} 快照 diff，有变动则写调整记录
     * （指定来源）并刷新快照；应缴上调（after&gt;before 且 before&gt;0）时启动补缴缓冲。
     *
     * <p>手动固定（{@code required_manual} 非空）跳过；无账户且无应缴不建空账户；首次产生应缴（before=0）
     * 只记录、不启动补缴缓冲（初始缺口由收取缓冲承担，避免同一缺口双计时）。</p>
     */
    private void reconcileRequired(Integer ownerType, Integer ownerId, DepositAdjustSource source) {
        // 实时重算当前应缴（requiredManual 非空时返回手动固定值）
        BigDecimal after = depositAccountService.computeRequired(ownerType, ownerId);
        boolean[] supplementNeeded = {false};
        transactionTemplate.executeWithoutResult(status -> {
            ShopDeposit account = shopDepositMapper.selectForUpdateByOwner(ownerType, ownerId);
            if (account == null) {
                if (after.compareTo(BigDecimal.ZERO) <= 0) {
                    // 无账户且无应缴：不创建空账户、不记录
                    return;
                }
                depositAccountService.getOrCreate(ownerType, ownerId);
                account = shopDepositMapper.selectForUpdateByOwner(ownerType, ownerId);
                if (account == null) {
                    return;
                }
            }
            if (account.getRequiredManual() != null) {
                // 手动固定：应缴不随类目/规则变动，跳过记录
                return;
            }
            BigDecimal before = account.getRequiredSnapshot() == null
                    ? BigDecimal.ZERO : account.getRequiredSnapshot();
            if (after.compareTo(before) == 0) {
                // 应缴无变化，幂等不记录
                return;
            }
            if (after.compareTo(before) > 0 && before.compareTo(BigDecimal.ZERO) > 0) {
                supplementNeeded[0] = true;
            }
            long now = nowSeconds();
            ShopDepositAdjustLog adjustLog = new ShopDepositAdjustLog();
            adjustLog.setOwnerType(ownerType);
            adjustLog.setOwnerId(ownerId);
            adjustLog.setBeforeRequired(before);
            adjustLog.setAfterRequired(after);
            adjustLog.setSource(source.getCode());
            adjustLog.setAdminId(0);
            adjustLog.setReason(buildReconcileReason(ownerType, before, after, source));
            adjustLog.setAddTime(now);
            shopDepositAdjustLogMapper.insert(adjustLog);
            account.setRequiredSnapshot(after);
            account.setUpdateTime(now);
            shopDepositMapper.updateById(account);
        });
        // 应缴上调后启动补缴缓冲（事务提交后再触发，onSupplementTrigger 自带行锁 + 幂等）
        if (supplementNeeded[0]) {
            onSupplementTrigger(ownerType, ownerId);
        }
    }

    /** 应缴调整文案：按来源区分（类目变更 / 规则调整）；首次产生应缴=创建，其后按升/降区分上调/下调。 */
    private String buildReconcileReason(Integer ownerType, BigDecimal before, BigDecimal after,
            DepositAdjustSource source) {
        if (source == DepositAdjustSource.RULE_CHANGE) {
            if (before.compareTo(BigDecimal.ZERO) == 0) {
                return "保证金规则调整，应缴额度 ¥" + money(after);
            }
            String verb = after.compareTo(before) > 0 ? "上调至" : "下调至";
            return "保证金规则调整，原额度 ¥" + money(before) + " " + verb + " ¥" + money(after);
        }
        // 类目变更
        if (before.compareTo(BigDecimal.ZERO) == 0) {
            boolean supplier = DepositOwnerType.SUPPLIER.getCode().equals(ownerType);
            return (supplier ? "创建了供应商保证金，额度 ¥" : "创建了店铺保证金，额度 ¥") + money(after);
        }
        String verb = after.compareTo(before) > 0 ? "上调至" : "下调至";
        return "修改了保证金额度，原额度 ¥" + money(before) + " " + verb + " ¥" + money(after);
    }

    private static String money(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    @Override
    public void runDueCheck() {
        DepositSettingVO setting = depositSettingService.detail();
        if (!isEnabled(setting)) {
            log.info("保证金功能未开启，跳过双缓冲到期扫描");
            return;
        }
        int collectAction = nvl(setting.getDepositCollectBufferAction(), DepositPenaltyAction.FORBID_ON_SHELF.getCode());
        int supplementAction = nvl(setting.getDepositSupplementBufferAction(), DepositPenaltyAction.FORBID_ON_SHELF.getCode());

        List<ShopDeposit> accounts = shopDepositMapper.selectList(null);
        if (accounts == null || accounts.isEmpty()) {
            return;
        }
        int penalized = 0;
        int restored = 0;
        for (ShopDeposit account : accounts) {
            Integer ownerType = account.getOwnerType();
            Integer ownerId = account.getOwnerId();
            if (ownerType == null || ownerId == null) {
                continue;
            }
            try {
                int result = handleAccount(ownerType, ownerId, collectAction, supplementAction);
                if (result > 0) {
                    penalized++;
                } else if (result < 0) {
                    restored++;
                }
            } catch (Exception e) {
                log.error("保证金到期扫描单账户处理失败 ownerType={} ownerId={}", ownerType, ownerId, e);
            }
        }
        log.info("保证金双缓冲到期扫描完成：账户数={} 新增/维持处罚={} 恢复={}", accounts.size(), penalized, restored);
    }

    /**
     * 处理单个账户：返回 1=执行处罚，-1=执行恢复，0=无动作。
     */
    private int handleAccount(Integer ownerType, Integer ownerId, int collectAction, int supplementAction) {
        BigDecimal pending = depositAccountService.pending(ownerType, ownerId);
        if (pending.compareTo(BigDecimal.ZERO) <= 0) {
            // 已足额：若处于惩罚态则恢复
            ShopDeposit acc = findAccount(ownerType, ownerId);
            if (acc != null && acc.getPenaltyStatus() != null && acc.getPenaltyStatus() != 0) {
                restore(ownerType, ownerId);
                return -1;
            }
            return 0;
        }
        // 待补缴 > 0：判断双缓冲是否到期
        ShopDeposit acc = findAccount(ownerType, ownerId);
        if (acc == null) {
            return 0;
        }
        long now = nowSeconds();
        // 先到先处罚、后到期不升级：取"最近到期"档位的唯一动作
        int action = resolveEffectiveAction(acc.getCollectDueTime(), collectAction,
                acc.getSupplementDueTime(), supplementAction, now);
        if (action == 0) {
            // 缓冲未启动或未到期
            return 0;
        }
        applyPenalty(ownerType, ownerId, action);
        return 1;
    }

    /**
     * 执行处罚（行锁 + 单账户事务）：penalty_status 存生效动作码（1/2）；命中"全店下架"(2) 时
     * 物理下架在售商品 + 写下架日志。"首次处罚后冻结"：账户已处罚（{@code penalty_status != 0}）
     * 则不改档、不随后到期升级，直到补足后 {@link #restore} 清零。
     *
     * <p>但冻结档位为"全店下架"(2) 时，每次仍会复扫下架——把处罚后又被重新上架的在售商品继续
     * 物理下架（{@link #offShelfAllProducts} 幂等，仅补写尚无未恢复日志的商品），保证处罚期间
     * 不会因商户重新上新品而绕过下架。</p>
     */
    private void applyPenalty(Integer ownerType, Integer ownerId, int action) {
        transactionTemplate.executeWithoutResult(status -> {
            ShopDeposit account = shopDepositMapper.selectForUpdateByOwner(ownerType, ownerId);
            if (account == null) {
                return;
            }
            int current = account.getPenaltyStatus() == null ? 0 : account.getPenaltyStatus();
            if (current != 0) {
                // 首次处罚后冻结：维持原档位不升级；冻结档位为"全店下架"(2) 则复扫下架处罚后新上架的在售商品
                if (current == DepositPenaltyAction.OFF_SHELF_ALL.getCode()) {
                    offShelfAllProducts(ownerType, ownerId);
                }
                return;
            }
            account.setPenaltyStatus(action);
            account.setUpdateTime(nowSeconds());
            shopDepositMapper.updateById(account);
            // 全店下架：物理下架在售商品
            if (action == DepositPenaltyAction.OFF_SHELF_ALL.getCode()) {
                offShelfAllProducts(ownerType, ownerId);
            }
        });
    }

    /**
     * 取"最近到期"档位的处罚动作（先到先处罚，后到期的更重档位不叠加升级）。
     * 在已到期（{@code now >= due > 0}）的收取/补缴缓冲中取 due_time 最小者对应的 action；
     * 两档到期时间相同则取较重（全店下架 2 &gt; 限制上架 1）。返回 0 表示无已到期缓冲。
     */
    private static int resolveEffectiveAction(Long collectDue, int collectAction,
            Long supplementDue, int supplementAction, long now) {
        boolean collectExpired = collectDue != null && collectDue > 0 && now >= collectDue;
        boolean supplementExpired = supplementDue != null && supplementDue > 0 && now >= supplementDue;
        if (!collectExpired && !supplementExpired) {
            return 0;
        }
        if (collectExpired && !supplementExpired) {
            return collectAction;
        }
        if (supplementExpired && !collectExpired) {
            return supplementAction;
        }
        // 两档均到期：取到期更早者；相同则取较重(2>1)
        if (collectDue < supplementDue) {
            return collectAction;
        }
        if (supplementDue < collectDue) {
            return supplementAction;
        }
        return Math.max(collectAction, supplementAction);
    }

    @Override
    public void restore(Integer ownerType, Integer ownerId) {
        transactionTemplate.executeWithoutResult(status -> {
            ShopDeposit account = shopDepositMapper.selectForUpdateByOwner(ownerType, ownerId);
            if (account == null) {
                return;
            }
            // 行锁内二次确认已足额
            if (depositAccountService.pending(ownerType, ownerId).compareTo(BigDecimal.ZERO) > 0) {
                return;
            }
            long now = nowSeconds();
            boolean changed = false;
            if (account.getPenaltyStatus() != null && account.getPenaltyStatus() != 0) {
                account.setPenaltyStatus(0);
                changed = true;
            }
            if (account.getCollectDueTime() != null && account.getCollectDueTime() > 0) {
                account.setCollectDueTime(0L);
                changed = true;
            }
            if (account.getSupplementDueTime() != null && account.getSupplementDueTime() > 0) {
                account.setSupplementDueTime(0L);
                changed = true;
            }
            if (changed) {
                account.setUpdateTime(now);
                shopDepositMapper.updateById(account);
            }
            restoreOffShelfProducts(ownerType, ownerId, now);
        });
    }

    /**
     * 实时解析某 owner 当前生效的惩罚动作码（单一动作，无副作用、不落库），用于准入拦截。
     *
     * <p>判定与 {@link #runDueCheck()} 单账户口径一致但实时现算、不依赖定时任务是否已打标：
     * 功能关闭 / 账户不存在 → 空集；已处罚（{@code penalty_status != 0}）→ 按已落库档位现算（首次处罚后冻结，
     * 不随后到期升级）；否则取"最近到期"缓冲对应配置 action（先到先处罚，后到期不叠加升级）；
     * 无已到期缓冲或当前已补足（{@code pending<=0}）→ 空集。对正常账户廉价短路（仅一次账户查询）。</p>
     */
    private Set<Integer> resolveActiveActions(Integer ownerType, Integer ownerId) {
        if (ownerType == null || ownerId == null || ownerId <= 0) {
            return Set.of();
        }
        DepositSettingVO setting = depositSettingService.detail();
        if (!isEnabled(setting)) {
            return Set.of();
        }
        ShopDeposit account = findAccount(ownerType, ownerId);
        if (account == null) {
            return Set.of();
        }
        int penalty = account.getPenaltyStatus() == null ? 0 : account.getPenaltyStatus();
        int action;
        if (penalty != 0) {
            // 首次处罚后冻结：按已落库档位现算，不随后到期升级
            action = penalty;
        } else {
            int collectAction = nvl(setting.getDepositCollectBufferAction(),
                    DepositPenaltyAction.FORBID_ON_SHELF.getCode());
            int supplementAction = nvl(setting.getDepositSupplementBufferAction(),
                    DepositPenaltyAction.FORBID_ON_SHELF.getCode());
            action = resolveEffectiveAction(account.getCollectDueTime(), collectAction,
                    account.getSupplementDueTime(), supplementAction, nowSeconds());
        }
        if (action == 0) {
            // 未处罚且无到期缓冲：正常账户廉价短路，不触发应缴重算
            return Set.of();
        }
        // 已补足则即时放行（补缴后无需等次日定时任务恢复）
        if (depositAccountService.pending(ownerType, ownerId).compareTo(BigDecimal.ZERO) <= 0) {
            return Set.of();
        }
        return Set.of(action);
    }

    /** 是否拦上架：命中"限制上架"(1) 或"全店下架"(2)。 */
    private static boolean blocksOnShelf(Set<Integer> actions) {
        return actions.contains(DepositPenaltyAction.FORBID_ON_SHELF.getCode())
                || actions.contains(DepositPenaltyAction.OFF_SHELF_ALL.getCode());
    }

    /** 是否拦下单：命中"全店下架"(2)（商品被物理下架，显式拦单兜底购物车/直链/并发）。 */
    private static boolean blocksOrder(Set<Integer> actions) {
        return actions.contains(DepositPenaltyAction.OFF_SHELF_ALL.getCode());
    }

    @Override
    public void assertOnShelfAllowed(Integer ownerType, Integer ownerId) {
        if (blocksOnShelf(resolveActiveActions(ownerType, ownerId))) {
            throw new GlobalException("保证金不足，已限制上架新品，请先补缴保证金",
                    HttpStatus.DEPOSIT_INSUFFICIENT_FORBID_ONSHELF);
        }
    }

    @Override
    public void assertOrderAllowed(Integer ownerType, Integer ownerId) {
        if (blocksOrder(resolveActiveActions(ownerType, ownerId))) {
            throw new GlobalException("该店铺/供应商保证金不足已被限制，暂时无法下单",
                    HttpStatus.DEPOSIT_INSUFFICIENT_FORBID_ORDER);
        }
    }

    /**
     * 物理下架 owner 当前全部在售商品（status=1），并为尚无未恢复下架记录的商品补写下架日志。
     *
     * <p>复扫幂等：处罚后被商户重新上架、但仍有未恢复(status=1)下架日志的商品，会被再次物理下架
     * 且不重复写日志；首次处罚时全部在售商品下架并写日志。</p>
     */
    private void offShelfAllProducts(Integer ownerType, Integer ownerId) {
        List<Integer> onSaleIds = listOnSaleProductIds(ownerType, ownerId).stream()
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (onSaleIds.isEmpty()) {
            return;
        }
        // 下架当前全部在售商品（含处罚后被重新上架、仍有未恢复日志的：复扫兜底）
        if (isShop(ownerType)) {
            productService.lambdaUpdate()
                    .in(Product::getProductId, onSaleIds)
                    .eq(Product::getShopId, ownerId)
                    .set(Product::getProductStatus, 0)
                    .update();
        } else {
            shopDepositMapper.offShelfVendorProducts(onSaleIds);
        }
        // 仅为尚无未恢复(status=1)下架记录的商品补写日志，避免重复日志
        Set<Integer> alreadyOff = listOpenOffShelfLogs(ownerType, ownerId).stream()
                .map(ShopDepositOffshelfLog::getProductId)
                .collect(Collectors.toSet());
        long now = nowSeconds();
        for (Integer productId : onSaleIds) {
            if (alreadyOff.contains(productId)) {
                continue;
            }
            ShopDepositOffshelfLog logRow = new ShopDepositOffshelfLog();
            logRow.setOwnerType(ownerType);
            logRow.setOwnerId(ownerId);
            logRow.setProductId(productId);
            logRow.setStatus(1);
            logRow.setOffTime(now);
            logRow.setAddTime(now);
            shopDepositOffshelfLogMapper.insert(logRow);
        }
    }

    /**
     * 恢复 owner 被惩罚下架（status=1）的商品上架，并把日志置 status=2 + restore_time。
     */
    private void restoreOffShelfProducts(Integer ownerType, Integer ownerId, long now) {
        List<ShopDepositOffshelfLog> logs = listOpenOffShelfLogs(ownerType, ownerId);
        if (logs.isEmpty()) {
            return;
        }
        List<Integer> productIds = logs.stream()
                .map(ShopDepositOffshelfLog::getProductId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (!productIds.isEmpty()) {
            if (isShop(ownerType)) {
                productService.lambdaUpdate()
                        .in(Product::getProductId, productIds)
                        .eq(Product::getShopId, ownerId)
                        .eq(Product::getIsDelete, 0)
                        .set(Product::getProductStatus, 1)
                        .update();
            } else {
                shopDepositMapper.onShelfVendorProducts(productIds);
            }
        }
        for (ShopDepositOffshelfLog logRow : logs) {
            logRow.setStatus(2);
            logRow.setRestoreTime(now);
            shopDepositOffshelfLogMapper.updateById(logRow);
        }
    }

    /**
     * owner 在售商品id：店铺走 product 表，供应商走 vendor_product 表（仅 SQL）。
     */
    private List<Integer> listOnSaleProductIds(Integer ownerType, Integer ownerId) {
        if (isShop(ownerType)) {
            return productService.lambdaQuery()
                    .eq(Product::getShopId, ownerId)
                    .eq(Product::getProductStatus, 1)
                    .eq(Product::getIsDelete, 0)
                    .list()
                    .stream()
                    .map(Product::getProductId)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());
        }
        List<Integer> ids = shopDepositMapper.selectVendorOnSaleProductIds(ownerId);
        return ids == null ? List.of() : ids;
    }

    /**
     * owner 未恢复（status=1）的下架日志。
     */
    private List<ShopDepositOffshelfLog> listOpenOffShelfLogs(Integer ownerType, Integer ownerId) {
        List<ShopDepositOffshelfLog> logs = shopDepositOffshelfLogMapper.selectList(
                Wrappers.<ShopDepositOffshelfLog>lambdaQuery()
                        .eq(ShopDepositOffshelfLog::getOwnerType, ownerType)
                        .eq(ShopDepositOffshelfLog::getOwnerId, ownerId)
                        .eq(ShopDepositOffshelfLog::getStatus, 1));
        return logs == null ? List.of() : logs;
    }

    private ShopDeposit findAccount(Integer ownerType, Integer ownerId) {
        return shopDepositMapper.selectOne(
                Wrappers.<ShopDeposit>lambdaQuery()
                        .eq(ShopDeposit::getOwnerType, ownerType)
                        .eq(ShopDeposit::getOwnerId, ownerId));
    }

    private static boolean isShop(Integer ownerType) {
        return DepositOwnerType.SHOP.getCode().equals(ownerType);
    }

    private static boolean isEnabled(DepositSettingVO setting) {
        return setting != null && setting.getDepositEnable() != null && setting.getDepositEnable() == 1;
    }

    private static int nvl(Integer value, int def) {
        return value == null ? def : value;
    }

    private static long nowSeconds() {
        return System.currentTimeMillis() / 1000L;
    }
}
