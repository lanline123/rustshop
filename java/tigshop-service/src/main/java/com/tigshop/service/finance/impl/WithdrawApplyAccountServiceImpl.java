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

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.UserWithDrawApplyDTO;
import com.tigshop.bean.dto.finance.UserWithdrawApplyDelDTO;
import com.tigshop.bean.dto.finance.WithdrawApplyAccountUpdateDTO;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.enums.finance.UserWithdrawAccountType;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.user.ChangTypeEnum;
import com.tigshop.helper.SettingsHelper;
import com.tigshop.bean.model.finance.UserBalanceLog;
import com.tigshop.bean.model.finance.UserWithdrawApply;
import com.tigshop.bean.model.finance.WithdrawApplyAccount;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.param.finance.withdrawapply.WithdrawApplyAccountCreateParam;
import com.tigshop.bean.query.finance.userwithdrawapply.UserWithdrawApplyListQuery;
import com.tigshop.bean.vo.finance.WithdrawApplyAccountVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.finance.UserBalanceLogMapper;
import com.tigshop.mapper.finance.UserWithdrawApplyMapper;
import com.tigshop.mapper.finance.WithdrawApplyAccountMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.WithdrawApplyAccountService;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.user.UserBalanceService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会员提现账号服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class WithdrawApplyAccountServiceImpl extends BaseServiceImpl<WithdrawApplyAccountMapper, WithdrawApplyAccount> implements WithdrawApplyAccountService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserWithdrawApplyMapper userWithdrawApplyMapper;
    @Resource
    private UserBalanceLogMapper userBalanceLogMapper;
    @Resource
    private AdminMsgService adminMsgService;
    @Resource
    private MessageHelper messageHelper;
    @Resource
    private com.tigshop.service.setting.ConfigService configService;
    @Resource
    private TranslatePackageImpl translatePackage;
    @Resource
    private SettingsHelper settingsHelper;
    @Resource
    private UserBalanceService userBalanceService;

    @Override
    public Page<WithdrawApplyAccountVO> list(UserWithdrawApplyListQuery query) {
        Page<WithdrawApplyAccount> page = new Page<>(query.getPage(), query.getSize());
        Integer userId = SecurityUtils.getCurrentUserId();
        Page<WithdrawApplyAccount> withdrawApplyAccountPage = this.lambdaQuery()
                .eq(query.getAccountId() != null && query.getAccountId() > 0, WithdrawApplyAccount::getAccountId, query.getAccountId())
                .eq(query.getAccountType() != null && query.getAccountType() > 0, WithdrawApplyAccount::getAccountType, query.getAccountType())
                .eq(userId > 0, WithdrawApplyAccount::getUserId, userId)
                .page(page);

        List<WithdrawApplyAccountVO> list = withdrawApplyAccountPage.getRecords().stream()
                .map(withdrawApplyAccount -> {
                    WithdrawApplyAccountVO withdrawApplyAccountVO = new WithdrawApplyAccountVO();
                    BeanUtils.copyProperties(withdrawApplyAccount, withdrawApplyAccountVO);
                    withdrawApplyAccountVO.setAccountTypeName(UserWithdrawAccountType.getDescription(withdrawApplyAccount.getAccountType()));
                    return withdrawApplyAccountVO;
                })
                .toList();
        return PageUtil.convertPage(withdrawApplyAccountPage, list);
    }

    @Override
    public WithdrawApplyAccountVO detail(Integer id, Integer userId) {
        if (id != null) {
            LambdaQueryWrapper<WithdrawApplyAccount> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(WithdrawApplyAccount::getAccountId, id);
            queryWrapper.eq(WithdrawApplyAccount::getUserId, userId);
            WithdrawApplyAccount withdrawApplyAccount = this.getOne(queryWrapper);
            WithdrawApplyAccountVO withdrawApplyAccountVO = new WithdrawApplyAccountVO();
            BeanUtils.copyProperties(withdrawApplyAccount, withdrawApplyAccountVO);
            return withdrawApplyAccountVO;
        }
        return null;
    }

    @Override
    public void create(WithdrawApplyAccountCreateParam param) {
        Integer userId = SecurityUtils.getCurrentUserId();

        Long withdrawApplyAccountCount = this.lambdaQuery().eq(WithdrawApplyAccount::getUserId, userId).count();
        Assert.isTrue(withdrawApplyAccountCount <= 15, () -> new GlobalException(messageHelper.get("error.withdraw.account.max.count")));

        // 检测是否在相同的卡号
        Long WithdrawApplyAccountCount = this.lambdaQuery()
                .eq(WithdrawApplyAccount::getUserId, userId)
                .eq(WithdrawApplyAccount::getAccountNo, param.getAccountNo())
                .eq(WithdrawApplyAccount::getAccountType, param.getAccountType())
                .count();
        Assert.isTrue(WithdrawApplyAccountCount == 0, () -> new GlobalException(messageHelper.get("error.withdraw.account.exist")));

        WithdrawApplyAccount withdrawApplyAccount = param.createWithdrawApplyAccount(userId);
        this.save(withdrawApplyAccount);
    }

    @Override
    public boolean update(WithdrawApplyAccountUpdateDTO updateDTO, Integer userId) {
        if (updateDTO != null) {
            //检测是否在相同的卡号
            LambdaQueryWrapper<WithdrawApplyAccount> queryWrapperCheck = new LambdaQueryWrapper<>();
            queryWrapperCheck.eq(WithdrawApplyAccount::getUserId, userId)
                    .eq(WithdrawApplyAccount::getAccountId, updateDTO.getAccountId())
                    .eq(WithdrawApplyAccount::getAccountNo, updateDTO.getAccountNo())
                    .eq(WithdrawApplyAccount::getAccountType, updateDTO.getAccountType())
                    .last("limit 1");
            WithdrawApplyAccount withdrawApplyAccount = new WithdrawApplyAccount();
            BeanUtils.copyProperties(updateDTO, withdrawApplyAccount);
            return this.updateById(withdrawApplyAccount);
        }
        return false;
    }

    @Override
    public boolean delAccount(UserWithdrawApplyDelDTO dto, Integer userId) {
        LambdaQueryWrapper<WithdrawApplyAccount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WithdrawApplyAccount::getUserId, userId)
                .eq(WithdrawApplyAccount::getAccountId, dto.getAccountId());
        return this.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean apply(UserWithDrawApplyDTO dto, Integer userId) {
        if (dto == null) {
            throw new GlobalException(messageHelper.get("error.withdraw.account.info.required"));
        }
        // 会员余额提现总开关（D1107）：关闭时不允许会员申请余额提现，不影响分销佣金提现
        Integer memberWithdrawalEnabled = settingsHelper.get(SettingsEnum.MEMBER_WITHDRAWAL_ENABLED);
        if (memberWithdrawalEnabled == null || memberWithdrawalEnabled != 1) {
            throw new GlobalException(translatePackage.translate("会员余额提现功能已关闭"));
        }
        // 校验提现方式是否在后台已开启的收款方式内（D1107，防绕过）
        checkReceiptMethodEnabled(dto.getAccountData() != null ? dto.getAccountData().getAccountType() : null);
        User user = userMapper.selectById(userId);
        BigDecimal balance = user != null ? user.getBalance() : BigDecimal.ZERO;
        if (dto.getAmount().compareTo(balance) > 0) {
            throw new GlobalException(messageHelper.get("error.withdraw.amount.exceed.balance"));
        }
        UserWithdrawApply userWithdrawApply = new UserWithdrawApply();
        userWithdrawApply.setUserId(userId);
        // 余额账户提现（D1107）
        userWithdrawApply.setWalletType(0);
        userWithdrawApply.setAmount(dto.getAmount());
        userWithdrawApply.setAddTime(StringUtils.getCurrentTime());
        userWithdrawApply.setAccountData(JsonUtil.toJson(dto.getAccountData()));
        userWithdrawApplyMapper.insert(userWithdrawApply);
        //提现冻结余额
        BigDecimal beforeFrozenBalance = null;
        if (user != null) {
            beforeFrozenBalance = user.getFrozenBalance();
            user.setFrozenBalance(user.getFrozenBalance().add(dto.getAmount()));
            userMapper.updateById(user);
        }
        UserBalanceLog userBalanceLog = new UserBalanceLog();
        userBalanceLog.setUserId(userId);
        userBalanceLog.setChangeDesc("提现冻结余额");
        if (user != null) {
            userBalanceLog.setBeforeBalance(user.getBalance());
            userBalanceLog.setAfterBalance(user.getBalance());
            userBalanceLog.setBeforeFrozenBalance(beforeFrozenBalance);
            userBalanceLog.setAfterFrozenBalance(user.getFrozenBalance());
        }
        userBalanceLog.setFrozenBalance(dto.getAmount());
        userBalanceLog.setChangeType(ChangTypeEnum.INC_POINTS.getValue());
        userBalanceLog.setChangeTime(StringUtils.getCurrentTime());
        userBalanceLogMapper.insert(userBalanceLog);
        //减余额
        User userDec = userMapper.selectById(userId);
        BigDecimal beforeBalance = null;
        if (userDec != null) {
            beforeBalance = userDec.getBalance();
            userDec.setBalance(userDec.getBalance().subtract(dto.getAmount()));
            userMapper.updateById(userDec);
        }
        UserBalanceLog userBalanceLogDec = new UserBalanceLog();
        userBalanceLogDec.setUserId(userId);
        userBalanceLogDec.setChangeDesc("提现扣除余额");
        if (userDec != null) {
            userBalanceLogDec.setBeforeBalance(beforeBalance);
            userBalanceLogDec.setAfterBalance(beforeBalance.subtract(dto.getAmount()));
            userBalanceLogDec.setBeforeFrozenBalance(userDec.getFrozenBalance());
            userBalanceLogDec.setAfterFrozenBalance(userDec.getFrozenBalance());
        }
        userBalanceLogDec.setBalance(dto.getAmount());
        userBalanceLogDec.setChangeType(ChangTypeEnum.DEC_POINTS.getValue());
        userBalanceLogDec.setChangeTime(StringUtils.getCurrentTime());
        userBalanceLogMapper.insert(userBalanceLogDec);

        // 发送后台消息  -- 提现申请
        User userInfo = userMapper.selectById(userId);
        AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
        adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.WITHDRAWAL_REQUEST.getCatId());
        adminMsgCreateDTO.setTitle("您有新的提现申请,申请用户：" + userInfo.getUsername());
        adminMsgCreateDTO.setContent("用户【" + userInfo.getUsername() + "】申请提现,申请金额：" + dto.getAmount() + configService.getCurrencyName());
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("withdrawApplyId", userWithdrawApply.getId());
        adminMsgCreateDTO.setRelatedData(relatedData);
        adminMsgService.createMessage(adminMsgCreateDTO);
        return true;
    }

    /**
     * 佣金提现申请（D1107）：从佣金账户扣减+冻结，与会员余额提现完全独立。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyCommission(UserWithDrawApplyDTO dto, Integer userId) {
        if (dto == null) {
            throw new GlobalException(translatePackage.translate("请填写提现账号信息"));
        }
        // 分销佣金提现为分销核心能力：不受「会员余额提现开关」控制，也不受「余额提现方式」勾选限制，
        // 仅按可用佣金校验（D1107）。收款方式开关与开启开关只作用于余额提现。
        if (dto.getAccountData() == null || dto.getAccountData().getAccountType() == null) {
            throw new GlobalException(translatePackage.translate("请选择提现方式"));
        }
        User user = userMapper.selectById(userId);
        BigDecimal commissionBalance = user != null && user.getCommissionBalance() != null
                ? user.getCommissionBalance() : BigDecimal.ZERO;
        if (dto.getAmount().compareTo(commissionBalance) > 0) {
            throw new GlobalException(translatePackage.translate("提现金额大于账户的可用佣金"));
        }

        UserWithdrawApply userWithdrawApply = new UserWithdrawApply();
        userWithdrawApply.setUserId(userId);
        // 佣金账户提现
        userWithdrawApply.setWalletType(1);
        userWithdrawApply.setAmount(dto.getAmount());
        userWithdrawApply.setStatus(0);
        userWithdrawApply.setAddTime(StringUtils.getCurrentTime());
        userWithdrawApply.setAccountData(JsonUtil.toJson(dto.getAccountData()));
        userWithdrawApplyMapper.insert(userWithdrawApply);

        // 冻结佣金 + 扣减佣金可用（走 UserBalanceService，wallet_type=1 流水）
        userBalanceService.incFrozenCommissionBalance(dto.getAmount(), userId, "佣金提现冻结");
        userBalanceService.decCommissionBalance(dto.getAmount(), userId, "佣金提现扣除");

        // 发送后台消息 -- 佣金提现申请
        User userInfo = userMapper.selectById(userId);
        AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
        adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.WITHDRAWAL_REQUEST.getCatId());
        adminMsgCreateDTO.setTitle("您有新的佣金提现申请,申请用户：" + userInfo.getUsername());
        adminMsgCreateDTO.setContent("用户【" + userInfo.getUsername() + "】申请佣金提现,申请金额：" + dto.getAmount() + "元");
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("withdrawApplyId", userWithdrawApply.getId());
        adminMsgCreateDTO.setRelatedData(relatedData);
        adminMsgService.createMessage(adminMsgCreateDTO);
        return true;
    }

    /**
     * 校验提现到账方式是否在后台已开启的收款方式内（D1107）。
     * 配置 MEMBER_WITHDRAWAL_RECEIPT_METHOD 为 JSON 数组，如 [1,2,3]（1银行卡 2支付宝 3微信）。
     * 余额提现与佣金提现共用该配置；未配置时不限制。
     */
    private void checkReceiptMethodEnabled(Integer accountType) {
        if (accountType == null) {
            throw new GlobalException(translatePackage.translate("请选择提现方式"));
        }
        Object methods = settingsHelper.get(SettingsEnum.MEMBER_WITHDRAWAL_RECEIPT_METHOD);
        if (methods instanceof com.alibaba.fastjson.JSONArray arr) {
            for (int i = 0; i < arr.size(); i++) {
                if (accountType.equals(arr.getInteger(i))) {
                    return;
                }
            }
            throw new GlobalException(translatePackage.translate("该提现方式已关闭"));
        }
    }
}
