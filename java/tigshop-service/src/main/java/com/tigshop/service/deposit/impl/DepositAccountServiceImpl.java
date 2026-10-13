package com.tigshop.service.deposit.impl;

import com.tigshop.bean.enums.deposit.DepositLogType;
import com.tigshop.bean.model.deposit.ShopDeposit;
import com.tigshop.bean.model.deposit.ShopDepositLog;
import com.tigshop.bean.vo.deposit.DepositSettingVO;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.mapper.deposit.ShopDepositLogMapper;
import com.tigshop.mapper.deposit.ShopDepositMapper;
import com.tigshop.service.deposit.DepositAccountService;
import com.tigshop.service.deposit.DepositSettingService;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 保证金账户服务实现（统一 owner 维度）。仿 UserBalanceServiceImpl 的行锁记账模式。
 *
 * @author Tigshop团队
 */
@Service
public class DepositAccountServiceImpl implements DepositAccountService {

    @Resource
    private ShopDepositMapper shopDepositMapper;

    @Resource
    private ShopDepositLogMapper shopDepositLogMapper;

    @Resource
    private DepositRuleMatcher depositRuleMatcher;

    @Resource
    private DepositSettingService depositSettingService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShopDeposit getOrCreate(Integer ownerType, Integer ownerId) {
        ShopDeposit account = shopDepositMapper.selectForUpdateByOwner(ownerType, ownerId);
        if (account != null) {
            return account;
        }
        long now = nowSeconds();
        account = new ShopDeposit();
        account.setOwnerType(ownerType);
        account.setOwnerId(ownerId);
        account.setPaid(BigDecimal.ZERO);
        account.setRequiredManual(null);
        account.setRequiredSnapshot(BigDecimal.ZERO);
        account.setCollectDueTime(0L);
        account.setSupplementDueTime(0L);
        account.setCollectActivated(0);
        account.setPenaltyStatus(0);
        account.setStatus(1);
        account.setAddTime(now);
        account.setUpdateTime(now);
        try {
            shopDepositMapper.insert(account);
        } catch (DuplicateKeyException e) {
            // 并发下另一个事务已创建，忽略后重新加锁读取
        }
        return shopDepositMapper.selectForUpdateByOwner(ownerType, ownerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeDeposit(Integer ownerType, Integer ownerId, BigDecimal amount, DepositLogType type,
                              Integer relatedId, Integer orderId, Integer adminId, String remark) {
        if (amount == null) {
            throw new GlobalException("保证金金额非法", HttpStatus.DEPOSIT_AMOUNT_INVALID);
        }
        ShopDeposit account = getOrCreate(ownerType, ownerId);
        BigDecimal before = account.getPaid();
        BigDecimal changeAmount;
        BigDecimal after;
        if (type == DepositLogType.ADJUST) {
            // 平台调整：amount 为带符号金额（可正可负），直接加到 paid，调整后不可为负
            if (amount.compareTo(BigDecimal.ZERO) == 0) {
                throw new GlobalException("保证金金额非法", HttpStatus.DEPOSIT_AMOUNT_INVALID);
            }
            changeAmount = amount;
            after = before.add(amount);
            if (after.compareTo(BigDecimal.ZERO) < 0) {
                throw new GlobalException("保证金余额不足", HttpStatus.DEPOSIT_BALANCE_NOT_ENOUGH);
            }
        } else if (type == DepositLogType.RECHARGE) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new GlobalException("保证金金额非法", HttpStatus.DEPOSIT_AMOUNT_INVALID);
            }
            changeAmount = amount;
            after = before.add(amount);
        } else {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new GlobalException("保证金金额非法", HttpStatus.DEPOSIT_AMOUNT_INVALID);
            }
            if (before.compareTo(amount) < 0) {
                throw new GlobalException("保证金余额不足", HttpStatus.DEPOSIT_BALANCE_NOT_ENOUGH);
            }
            changeAmount = amount.negate();
            after = before.subtract(amount);
        }
        account.setPaid(after);
        account.setUpdateTime(nowSeconds());
        shopDepositMapper.updateById(account);

        ShopDepositLog log = new ShopDepositLog();
        log.setOwnerType(ownerType);
        log.setOwnerId(ownerId);
        log.setType(type.getCode());
        log.setChangeAmount(changeAmount);
        log.setBeforePaid(before);
        log.setAfterPaid(after);
        log.setRelatedId(relatedId == null ? 0 : relatedId);
        log.setOrderId(orderId == null ? 0 : orderId);
        log.setAdminId(adminId == null ? 0 : adminId);
        log.setRemark(remark);
        log.setAddTime(nowSeconds());
        shopDepositLogMapper.insert(log);
    }

    @Override
    public BigDecimal computeRequired(Integer ownerType, Integer ownerId) {
        // 保证金功能关闭：应缴统一视为 0，令待补缴归零、可提现/可退放开、账户状态转正常，
        // 与生命周期侧（onCollectTrigger/onCategoryChange/runDueCheck/resolveActiveActions）的 isEnabled 短路口径一致
        if (!isDepositEnabled()) {
            return BigDecimal.ZERO;
        }
        ShopDeposit account = findAccount(ownerType, ownerId);
        if (account != null && account.getRequiredManual() != null) {
            // 手动应缴覆盖值优先（永久生效，直到平台改回按类目最高），不受收取激活门限约束
            return account.getRequiredManual();
        }
        // 首单收取语义：账户未激活收取（首单前）不产生应缴——上架不建号、概览不显示待补缴。
        // 无账户 = 未激活；节点=上架收时在上架挂钩即激活，节点=首单收时首单挂钩才激活。
        if (account == null || !Integer.valueOf(1).equals(account.getCollectActivated())) {
            return BigDecimal.ZERO;
        }
        // 否则按 (主体类型 × 持有者类型 × 在售商品类目三级就近 × 当前生效期) 命中规则全店取最高
        return depositRuleMatcher.maxRequired(ownerType, ownerId);
    }

    /**
     * 保证金功能是否开启（deposit_enable=1）。配置缺失/为 0 视为关闭。
     */
    private boolean isDepositEnabled() {
        DepositSettingVO setting = depositSettingService.detail();
        return setting != null && setting.getDepositEnable() != null && setting.getDepositEnable() == 1;
    }

    @Override
    public BigDecimal getPaid(Integer ownerType, Integer ownerId) {
        ShopDeposit account = findAccount(ownerType, ownerId);
        return account == null || account.getPaid() == null ? BigDecimal.ZERO : account.getPaid();
    }

    @Override
    public BigDecimal frozen(Integer ownerType, Integer ownerId) {
        BigDecimal paid = getPaid(ownerType, ownerId);
        BigDecimal required = computeRequired(ownerType, ownerId);
        return paid.min(required);
    }

    @Override
    public BigDecimal available(Integer ownerType, Integer ownerId) {
        return max0(getPaid(ownerType, ownerId).subtract(computeRequired(ownerType, ownerId)));
    }

    @Override
    public BigDecimal pending(Integer ownerType, Integer ownerId) {
        return max0(computeRequired(ownerType, ownerId).subtract(getPaid(ownerType, ownerId)));
    }

    @Override
    public BigDecimal withdrawable(Integer ownerType, Integer ownerId) {
        return available(ownerType, ownerId);
    }

    /**
     * 只读读取 owner 维度账户（不存在返回 null）。
     */
    private ShopDeposit findAccount(Integer ownerType, Integer ownerId) {
        return shopDepositMapper.selectOne(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<ShopDeposit>lambdaQuery()
                        .eq(ShopDeposit::getOwnerType, ownerType)
                        .eq(ShopDeposit::getOwnerId, ownerId));
    }

    private static BigDecimal max0(BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : value;
    }

    private static long nowSeconds() {
        return System.currentTimeMillis() / 1000L;
    }
}
