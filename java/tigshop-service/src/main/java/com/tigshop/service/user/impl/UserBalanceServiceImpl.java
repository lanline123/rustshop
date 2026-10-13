// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.user.impl;

import com.tigshop.bean.dto.finance.UserBalanceLogCreatDTO;
import com.tigshop.bean.model.user.User;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.finance.UserBalanceLogService;
import com.tigshop.service.user.UserBalanceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * @author Tigshop团队
 */
@Service
public class UserBalanceServiceImpl implements UserBalanceService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserBalanceLogService userBalanceLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean decBalance(BigDecimal amount, Integer userId, String changeDesc) {
        User user = userMapper.selectForUpdateById(userId);
        if (user.getBalance().compareTo(amount) < 0) {
            throw new GlobalException("余额不足");
        }
        user.setBalance(user.getBalance().subtract(amount));
        addBalanceLog(userId, changeDesc, 2, amount, null);
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean incBalance(BigDecimal amount, Integer userId, String changeDesc) {
        User user = userMapper.selectForUpdateById(userId);
        user.setBalance(user.getBalance().add(amount));
        addBalanceLog(userId, changeDesc, 1, amount, null);
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean decFrozenBalance(BigDecimal amount, Integer userId, String changeDesc) {
        User user = userMapper.selectForUpdateById(userId);
        user.setFrozenBalance(user.getFrozenBalance().subtract(amount));
        addBalanceLog(userId, changeDesc, 2, null, amount);
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean incFrozenBalance(BigDecimal amount, Integer userId, String changeDesc) {
        User user = userMapper.selectForUpdateById(userId);
        user.setFrozenBalance(user.getFrozenBalance().add(amount));
        addBalanceLog(userId, changeDesc, 1, null, amount);
        return userMapper.updateById(user) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    protected void addBalanceLog(Integer userId, String changeDesc, int changeType, BigDecimal balanceAmount, BigDecimal frozenBalanceAmount) {
        User user = userMapper.selectById(userId);


        UserBalanceLogCreatDTO userBalanceLogCreat = new UserBalanceLogCreatDTO();
        userBalanceLogCreat.setUserId(userId);
        userBalanceLogCreat.setChangeDesc(changeDesc);
        userBalanceLogCreat.setChangeType(changeType);
        // 余额账户流水（D1107）
        userBalanceLogCreat.setWalletType(0);
        if (balanceAmount != null) {
            userBalanceLogCreat.setBeforeBalance(user.getBalance());
            userBalanceLogCreat.setBalance(balanceAmount);
            BigDecimal afterBalance = changeType == 1 ? user.getBalance().add(balanceAmount) : user.getBalance().subtract(balanceAmount);
            userBalanceLogCreat.setAfterBalance(afterBalance);

            userBalanceLogCreat.setBeforeFrozenBalance(user.getFrozenBalance());
            userBalanceLogCreat.setFrozenBalance(BigDecimal.ZERO);
            userBalanceLogCreat.setAfterFrozenBalance(user.getFrozenBalance());
        }
        if (frozenBalanceAmount != null) {
            userBalanceLogCreat.setBeforeBalance(user.getBalance());
            userBalanceLogCreat.setBalance(BigDecimal.ZERO);
            userBalanceLogCreat.setAfterBalance(user.getBalance());

            userBalanceLogCreat.setFrozenBalance(frozenBalanceAmount);
            userBalanceLogCreat.setBeforeFrozenBalance(user.getFrozenBalance());
            BigDecimal afterFrozenBalance= changeType == 1 ? user.getFrozenBalance().add(frozenBalanceAmount) : user.getFrozenBalance().subtract(frozenBalanceAmount);
            userBalanceLogCreat.setAfterFrozenBalance(afterFrozenBalance);
        } else {
            userBalanceLogCreat.setFrozenBalance(BigDecimal.ZERO);
            userBalanceLogCreat.setBeforeFrozenBalance(user.getFrozenBalance());
            userBalanceLogCreat.setAfterFrozenBalance(user.getFrozenBalance());
        }
        userBalanceLogService.create(userBalanceLogCreat);
    }

    // ===================== 佣金账户（D1107，与余额账户对称） =====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean incCommissionBalance(BigDecimal amount, Integer userId, String changeDesc) {
        User user = userMapper.selectForUpdateById(userId);
        user.setCommissionBalance(nz(user.getCommissionBalance()).add(amount));
        addCommissionLog(userId, changeDesc, 1, amount, null);
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean decCommissionBalance(BigDecimal amount, Integer userId, String changeDesc) {
        User user = userMapper.selectForUpdateById(userId);
        user.setCommissionBalance(nz(user.getCommissionBalance()).subtract(amount));
        addCommissionLog(userId, changeDesc, 2, amount, null);
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean incFrozenCommissionBalance(BigDecimal amount, Integer userId, String changeDesc) {
        User user = userMapper.selectForUpdateById(userId);
        user.setFrozenCommissionBalance(nz(user.getFrozenCommissionBalance()).add(amount));
        addCommissionLog(userId, changeDesc, 1, null, amount);
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean decFrozenCommissionBalance(BigDecimal amount, Integer userId, String changeDesc) {
        User user = userMapper.selectForUpdateById(userId);
        user.setFrozenCommissionBalance(nz(user.getFrozenCommissionBalance()).subtract(amount));
        addCommissionLog(userId, changeDesc, 2, null, amount);
        return userMapper.updateById(user) > 0;
    }

    /**
     * 佣金账户流水（wallet_type=1）。before/after 记录的是佣金账户的余额/冻结值。
     */
    @Transactional(rollbackFor = Exception.class)
    protected void addCommissionLog(Integer userId, String changeDesc, int changeType, BigDecimal balanceAmount, BigDecimal frozenBalanceAmount) {
        User user = userMapper.selectById(userId);
        BigDecimal commissionBalance = nz(user.getCommissionBalance());
        BigDecimal frozenCommissionBalance = nz(user.getFrozenCommissionBalance());

        UserBalanceLogCreatDTO log = new UserBalanceLogCreatDTO();
        log.setUserId(userId);
        log.setChangeDesc(changeDesc);
        log.setChangeType(changeType);
        log.setWalletType(1);
        if (balanceAmount != null) {
            log.setBeforeBalance(commissionBalance);
            log.setBalance(balanceAmount);
            log.setAfterBalance(changeType == 1 ? commissionBalance.add(balanceAmount) : commissionBalance.subtract(balanceAmount));
            log.setBeforeFrozenBalance(frozenCommissionBalance);
            log.setFrozenBalance(BigDecimal.ZERO);
            log.setAfterFrozenBalance(frozenCommissionBalance);
        }
        if (frozenBalanceAmount != null) {
            log.setBeforeBalance(commissionBalance);
            log.setBalance(BigDecimal.ZERO);
            log.setAfterBalance(commissionBalance);
            log.setFrozenBalance(frozenBalanceAmount);
            log.setBeforeFrozenBalance(frozenCommissionBalance);
            log.setAfterFrozenBalance(changeType == 1 ? frozenCommissionBalance.add(frozenBalanceAmount) : frozenCommissionBalance.subtract(frozenBalanceAmount));
        } else if (balanceAmount == null) {
            log.setFrozenBalance(BigDecimal.ZERO);
            log.setBeforeFrozenBalance(frozenCommissionBalance);
            log.setAfterFrozenBalance(frozenCommissionBalance);
        }
        userBalanceLogService.create(log);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

}

