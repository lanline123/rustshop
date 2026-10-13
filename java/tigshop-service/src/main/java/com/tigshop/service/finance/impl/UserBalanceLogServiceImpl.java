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
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.finance.UserBalanceLogCreatDTO;
import com.tigshop.bean.dto.finance.UserBalanceLogListDTO;
import com.tigshop.bean.enums.finance.UserBalanceLogType;
import com.tigshop.bean.model.finance.UserBalanceLog;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.vo.finance.UserBalanceLogVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.finance.UserBalanceLogMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.UserBalanceLogService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 示例服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class UserBalanceLogServiceImpl extends BaseServiceImpl<UserBalanceLogMapper, UserBalanceLog> implements UserBalanceLogService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private MessageHelper messageHelper;

    @Resource
    private TranslatePackage translatePackage;

    @Override
    public Page<UserBalanceLogVO> list(UserBalanceLogListDTO listDTO) {
        // 分页
        Page<UserBalanceLog> page = buildSortOrder(listDTO);
        // 构造查询构造器
        MPJLambdaWrapper<UserBalanceLog> queryWrapper = new MPJLambdaWrapper<>();

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        // D1107：钱包维度筛选（0余额账户/1佣金账户）。优先用 walletType；
        // excludeDistribution 作为历史未回标数据的兜底（按文案排除佣金行）。
        Integer walletType = listDTO.getWalletType();
        boolean excludeDistribution = listDTO.getExcludeDistribution() != null && listDTO.getExcludeDistribution() == 1;
        queryWrapper.like(StringUtils.isNotBlank(keyword), UserBalanceLog::getChangeDesc, keyword)
                .eq(listDTO.getUserId() != null && listDTO.getUserId() > 0, UserBalanceLog::getUserId, listDTO.getUserId())
                .gt(listDTO.getBalance() != null && listDTO.getBalance() > 0, UserBalanceLog::getBalance, 0)
                .eq(walletType != null, UserBalanceLog::getWalletType, walletType)
                .notLike(excludeDistribution, UserBalanceLog::getChangeDesc, "结算佣金")
                .leftJoin(User.class, User::getUserId, UserBalanceLog::getUserId).select(User::getUsername).selectAll(UserBalanceLog.class).like(StrUtil.isNotBlank(listDTO.getUsername()), User::getUsername, listDTO.getUsername());
        // 执行查询
        Page<UserBalanceLog> userBalanceLogPage = this.selectJoinListPage(page, UserBalanceLog.class, queryWrapper);
        // 获取查询结果
        List<UserBalanceLog> userBalanceLogPageRecords = userBalanceLogPage.getRecords();
        if (CollUtil.isEmpty(userBalanceLogPageRecords)) {
            return new Page<>();
        }
        List<UserBalanceLogVO> userBalanceLogVOList = userBalanceLogPageRecords.stream().map(userBalanceLog -> {
            UserBalanceLogVO userBalanceLogVO = new UserBalanceLogVO();
            BeanUtils.copyProperties(userBalanceLog, userBalanceLogVO);

            userBalanceLogVO.setChangeTime(TigUtils.handelTime(userBalanceLog.getChangeTime()));
            userBalanceLogVO.setChangeTypeName(resolveChangeTypeName(userBalanceLog.getChangeType()));

            return userBalanceLogVO;
        }).toList();
        return PageUtil.convertPage(userBalanceLogPage, userBalanceLogVOList);
    }

    private static String resolveBalanceChangeTypeKey(Integer changeType) {
        if (changeType == null) {
            return "balance.log.change.type.unknown";
        }
        return switch (changeType) {
            case 1 -> "balance.log.change.type.increase";
            case 2 -> "balance.log.change.type.decrease";
            case 3, 99 -> "balance.log.change.type.other";
            default -> "balance.log.change.type.unknown";
        };
    }

    private String resolveChangeTypeName(Integer changeType) {
        String key = resolveBalanceChangeTypeKey(changeType);
        String translated = messageHelper.get(key);
        if (!key.equals(translated)) {
            return translated;
        }
        String chineseText = switch (changeType == null ? 0 : changeType) {
            case 1 -> UserBalanceLogType.CHANGE_TYPE_INCREASE.getDescription();
            case 2 -> UserBalanceLogType.CHANGE_TYPE_DECREASE.getDescription();
            case 3, 99 -> UserBalanceLogType.CHANGE_TYPE_FROZEN.getDescription();
            default -> "未知";
        };
        return translatePackage.translate(chineseText);
    }

    /**
     * 获取所有用户的余额变动总和
     *
     * @return BigDecimal
     */
    @Override
    public BigDecimal getUserBalanceLogTotal(String startTime, String endTime) {
        LambdaQueryWrapper<UserBalanceLog> queryWrapper = new LambdaQueryWrapper<>();

        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
            Date startTimeDate = DateUtil.parse(startTime);
            Date endTimeDate = DateUtil.parse(endTime);
            // 将时间设置为当天的结束时间
            endTimeDate = DateUtil.endOfDay(endTimeDate);

            // 添加时间范围的条件，假设 changeTime 是 Unix 时间戳
            queryWrapper.ge(UserBalanceLog::getChangeTime, startTimeDate.getTime() / 1000).le(UserBalanceLog::getChangeTime, endTimeDate.getTime() / 1000);
        }

        // 查询所有符合条件的余额变动（需带上变动类型以区分增减方向）
        queryWrapper.select(UserBalanceLog::getBalance, UserBalanceLog::getChangeType);
        List<UserBalanceLog> userBalanceLogs = this.list(queryWrapper);

        // 计算余额变动净值：change_type=1 增加累加，change_type=2 减少累减
        return userBalanceLogs.stream()
                .map(log -> {
                    BigDecimal balance = log.getBalance() != null ? log.getBalance() : BigDecimal.ZERO;
                    return log.getChangeType() != null && log.getChangeType() == 2 ? balance.negate() : balance;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取所有用户的 冻结余额变动总和
     *
     * @return BigDecimal
     */
    @Override
    public BigDecimal getUserFrozenBalanceLogTotal(String startTime, String endTime) {
        LambdaQueryWrapper<UserBalanceLog> queryWrapper = new LambdaQueryWrapper<>();

        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
            Date startTimeDate = DateUtil.parse(startTime);
            Date endTimeDate = DateUtil.parse(endTime);
            // 将时间设置为当天的结束时间
            endTimeDate = DateUtil.endOfDay(endTimeDate);

            // 添加时间范围的条件，假设 changeTime 是 Unix 时间戳
            queryWrapper.ge(UserBalanceLog::getChangeTime, startTimeDate.getTime() / 1000).le(UserBalanceLog::getChangeTime, endTimeDate.getTime() / 1000);
        }

        // 查询所有符合条件的冻结余额变动（需带上变动类型以区分增减方向）
        queryWrapper.select(UserBalanceLog::getFrozenBalance, UserBalanceLog::getChangeType);
        List<UserBalanceLog> userBalanceLogs = this.list(queryWrapper);

        // 计算冻结余额变动净值：change_type=1 增加累加，change_type=2 减少累减
        return userBalanceLogs.stream()
                .map(log -> {
                    BigDecimal frozenBalance = log.getFrozenBalance() != null ? log.getFrozenBalance() : BigDecimal.ZERO;
                    return log.getChangeType() != null && log.getChangeType() == 2 ? frozenBalance.negate() : frozenBalance;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changesInFunds(UserBalanceLog userBalanceLog) {
        return this.save(userBalanceLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void accountChange(Integer userId, UserBalanceLog data) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new GlobalException("用户不存在");
        }

        // 创建余额日志记录
        UserBalanceLog log = new UserBalanceLog();
        log.setUserId(userId);
        //本次增加或者减少的金额(冻结的金额)
        log.setBalance(data.getBalance() != null ? data.getBalance() : BigDecimal.ZERO);
        log.setFrozenBalance(data.getFrozenBalance() != null ? data.getFrozenBalance() : BigDecimal.ZERO);
        log.setChangeDesc(data.getChangeDesc() != null ? data.getChangeDesc() : "");
        log.setChangeType(data.getChangeType() != null ? data.getChangeType() : 99);


        //前置(根据查询用户拿到数据)
        BigDecimal balance = user.getBalance();
        BigDecimal frozenBalance = user.getFrozenBalance();
        log.setBeforeBalance(balance);
        log.setBeforeFrozenBalance(frozenBalance);

        //后置(增加或者扣减的金额)
        if (log.getChangeType() == 1) {
            user.setBalance(user.getBalance().add(log.getBalance()));
            user.setFrozenBalance(user.getFrozenBalance().add(log.getFrozenBalance()));
        } else {
            user.setBalance(user.getBalance().subtract(log.getBalance()));
            user.setFrozenBalance(user.getFrozenBalance().subtract(log.getFrozenBalance()));
        }
        log.setAfterBalance(user.getBalance());
        log.setAfterFrozenBalance(user.getFrozenBalance());

        //额外参数
        log.setChangeTime(StringUtils.getCurrentTime());

        save(log);
        userMapper.updateById(user);
    }

    @Override
    public boolean create(UserBalanceLogCreatDTO userBalanceLogCreatDTO) {
        UserBalanceLog userBalanceLog = new UserBalanceLog();
        BeanUtils.copyProperties(userBalanceLogCreatDTO, userBalanceLog);
        userBalanceLog.setChangeTime(StringUtils.getCurrentTime());
        return this.save(userBalanceLog);
    }

}
