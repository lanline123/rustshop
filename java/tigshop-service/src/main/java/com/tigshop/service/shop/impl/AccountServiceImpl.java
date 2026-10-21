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

package com.tigshop.service.shop.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.shop.AccountCreateDTO;
import com.tigshop.bean.dto.shop.AccountListDTO;
import com.tigshop.bean.dto.shop.AccountUpdateDTO;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.shop.Account;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.vo.merchant.MerchantVO;
import com.tigshop.bean.vo.shop.AccountVO;
import com.tigshop.bean.vo.shop.ShopAccountLogVO;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.shop.AccountMapper;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.merchant.MerchantAccountService;
import com.tigshop.service.merchant.MerchantService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.shop.AccountService;
import com.tigshop.service.shop.ShopService;
import com.tigshop.service.shop.ShopWithdrawService;
import io.netty.util.internal.StringUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.tigshop.common.utils.SecurityUtils.getCurrentAdminId;

/**
 * 店铺资金变化服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class AccountServiceImpl extends BaseServiceImpl<AccountMapper, Account> implements AccountService {
    @Resource
    ShopService shopService;
    @Resource
    OrderService orderService;
    @Resource
    AdminUserService adminUserService;
    @Resource
    MerchantAccountService merchantAccountService;
    @Resource
    MerchantService merchantService;
    @Resource
    MessageHelper messageHelper;
    @Resource
    @Lazy
    ShopWithdrawService shopWithdrawService;

    @Override
    public AccountVO index(AccountListDTO listDTO) {
        Integer shopId = getShopId();
        Integer adminId = getCurrentAdminId();
        AccountVO accountVO = new AccountVO();
        BigDecimal sumShopMoney = shopService.getSumShopMoney(shopId);
        BigDecimal sumFrozenMoney = shopService.getSumFrozenMoney(shopId);
        BigDecimal sumOrderUnSettlementAmount = orderService.getOrderUnSettlementAmount(shopId);
        //查询该adminId下的merchant_id
        AdminUser adminUser = adminUserService.getById(adminId);
        Integer merchantId = adminUser.getMerchantId();
        Integer count = 0;
        accountVO.setMerchant(null);
        count = merchantAccountService.getCardCount(merchantId, shopId);
        if (merchantId > 0) {
            MerchantVO merchantVo = merchantService.getMerchantVO(merchantId);
            accountVO.setMerchant(merchantVo);
        }

        accountVO.setShopMoney(sumShopMoney);
        BigDecimal availableBalance = sumShopMoney;
        try {
            Object channelBalancesValue = shopWithdrawService.getDualBalance().get("channelBalances");
            if (channelBalancesValue instanceof java.util.Map<?, ?> channelBalances) {
                for (Object value : channelBalances.values()) {
                    if (value instanceof BigDecimal channelBalance) {
                        availableBalance = availableBalance.add(channelBalance);
                    }
                }
            }
        } catch (Exception ignored) {
            // 线上渠道查询失败时保持线下余额，避免概览接口不可用。
        }
        accountVO.setAvailableBalance(availableBalance);
        accountVO.setFrozenMoney(sumFrozenMoney);
        accountVO.setUnSettlementMoney(sumOrderUnSettlementAmount);
        accountVO.setCardCount(count);
        return accountVO;
    }

    @Override
    public AccountVO detail(Integer id) {
        if (id != null) {
            Account account = this.getById(id);
            AccountVO accountVO = new AccountVO();
            BeanUtils.copyProperties(account, accountVO);
            return accountVO;
        }
        return null;
    }

    @Override
    public boolean create(AccountCreateDTO createDTO) {
        if (createDTO != null) {
            Account account = new Account();
            BeanUtils.copyProperties(createDTO, account);
            return this.save(account);
        }
        return false;
    }

    @Override
    public boolean update(AccountUpdateDTO updateDTO) {
        if (updateDTO != null) {
            Account account = new Account();
            BeanUtils.copyProperties(updateDTO, account);
            return this.updateById(account);
        }
        return false;
    }

    @Override
    public Page<ShopAccountLogVO> list(AccountListDTO listDTO) {
        // 分页
        Page<Account> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Account> queryWrapper = new LambdaQueryWrapper<>();
        String balanceType = listDTO.getBalanceType() == null || listDTO.getBalanceType().isBlank()
                ? "all" : listDTO.getBalanceType().trim().toLowerCase(Locale.ROOT);
        if (!List.of("all", "online", "offline").contains(balanceType)) {
            throw new GlobalException("资金类型仅支持 all、online、offline");
        }
        if (!"all".equals(balanceType)) {
            queryWrapper.eq(Account::getBalanceType, "online".equals(balanceType) ? 2 : 1);
        }
        if (StringUtils.isNotEmpty(listDTO.getPayChannel())) {
            String channel = normalizeChannel(listDTO.getPayChannel());
            if ("offline".equals(balanceType)) {
                throw new GlobalException("线下资金不支持线上渠道筛选");
            }
            queryWrapper.eq(Account::getBalanceType, 2).eq(Account::getPayChannel, channel);
        }
        if (listDTO.getShopType() != null) {
            if (!List.of(1, 2).contains(listDTO.getShopType())) {
                throw new GlobalException("店铺类型仅支持 1 店铺、2 门店");
            }
            // 已校验为整数枚举；资金日志仍由当前登录店铺约束作用域。
            queryWrapper.inSql(Account::getShopId,
                    "SELECT shop_id FROM shop WHERE shop_type = " + listDTO.getShopType());
        }
        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());
        if (listDTO.getSortField() == null || listDTO.getSortField().isBlank()) {
            queryWrapper.orderByDesc(Account::getAddTime).orderByDesc(Account::getShopAccountLogId);
        }

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            List<Shop> shop = shopService.getShopByShopTitle(keyword);
            List<Integer> shopIds = shop.stream().map(Shop::getShopId).toList();
            if (shopIds.isEmpty()) {
                return PageUtil.convertPage(page, List.of());
            }
            queryWrapper.in(Account::getShopId, shopIds);
        }

        Integer currentShopId = getShopId();
        if (currentShopId != null && currentShopId > 0) {
            queryWrapper.eq(Account::getShopId, currentShopId);
        } else if (listDTO.getShopId() != null) {
            queryWrapper.eq(Account::getShopId, listDTO.getShopId());
        }
        //时间查询
        String startTime = listDTO.getStartTime();
        String endTime = listDTO.getEndTime();
        if (!StringUtil.isNullOrEmpty(startTime)) {
            startTime = startTime + " 00:00:00";
            long addTimeStartTimestamp = DateUtil.parse(startTime).getTime() / 1000;
            queryWrapper.ge(Account::getAddTime, addTimeStartTimestamp);
        }
        if (!StringUtil.isNullOrEmpty(endTime)) {
            endTime = endTime + " 23:59:59";
            long addTimeEndTimestamp = DateUtil.parse(endTime).getTime() / 1000;
            queryWrapper.le(Account::getAddTime, addTimeEndTimestamp);
        }
        // 执行查询
        Page<Account> accountPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<Account> accountPageRecords = accountPage.getRecords();
        // 转换为VO
        List<ShopAccountLogVO> shopAccountLogVOList = accountPageRecords.stream().map(account -> {
            ShopAccountLogVO shopAccountLogVO = new ShopAccountLogVO();
            BeanUtils.copyProperties(account, shopAccountLogVO);
            long timestampInSeconds = account.getAddTime();
            Date date = new Date(timestampInSeconds * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            shopAccountLogVO.setAddTime(sdf.format(date));
            shopAccountLogVO.setShopTitle(Optional.ofNullable(shopService.getById(account.getShopId())).map(Shop::getShopTitle).orElse(""));
            shopAccountLogVO.setUnSettlementOrder(orderService.getOrderUnSettlementAmount(account.getShopId()));
            BigDecimal changeAmount = resolveChangeAmount(account);
            shopAccountLogVO.setChangeAmount(changeAmount);
            // 保持旧前端 shopMoney 作为变动金额的返回语义，与 changeAmount 一致。
            shopAccountLogVO.setShopMoney(changeAmount);
            shopAccountLogVO.setBalanceType(account.getBalanceType() == null ? 1 : account.getBalanceType());

            if (Integer.valueOf(2).equals(account.getBalanceType())) {
                shopAccountLogVO.setChangeReasons(account.getRemarks());
                shopAccountLogVO.setFrozenMoney(BigDecimal.ZERO);
                shopAccountLogVO.setNewFrozenMoney(BigDecimal.ZERO);
                return shopAccountLogVO;
            }

            Integer type = shopAccountLogVO.getType();
            if (Integer.valueOf(1).equals(type)) {
                //提现失败(店铺金额增加,冻结金额减少)和提现成功(店铺资金不变,冻结金额减少)
                if (changeAmount != null && changeAmount.signum() == 0) {
                    shopAccountLogVO.setChangeReasons(messageHelper.get("account.log.reason.withdraw.success"));
                } else {
                    shopAccountLogVO.setChangeReasons(messageHelper.get("account.log.reason.withdraw.reject"));
                }
            } else if (Integer.valueOf(2).equals(type)) {
                //提现申请
                shopAccountLogVO.setChangeReasons(messageHelper.get("account.log.reason.withdraw.apply"));
            } else if (Integer.valueOf(4).equals(type)) {
                //订单收支
                shopAccountLogVO.setChangeReasons(messageHelper.get("account.log.reason.order.income.expense"));
            }
            return shopAccountLogVO;
        }).toList();
        return PageUtil.convertPage(accountPage, shopAccountLogVOList);
    }

    private BigDecimal resolveChangeAmount(Account account) {
        if (account.getChangeAmount() != null) {
            return account.getChangeAmount();
        }
        BigDecimal before = account.getShopMoney() == null ? BigDecimal.ZERO : account.getShopMoney();
        // 新日志保存前后余额；旧提现结果日志虽未置 isNew，也使用前后余额。
        if (Integer.valueOf(1).equals(account.getIsNew()) || Integer.valueOf(1).equals(account.getType())) {
            return account.getNewShopMoney() == null ? null : account.getNewShopMoney().subtract(before);
        }
        return before;
    }

    private String normalizeChannel(String channel) {
        String normalized = channel == null ? "" : channel.trim().toUpperCase(Locale.ROOT);
        if (!List.of("WECHAT", "HUIFU", "ALIPAY").contains(normalized)) {
            throw new GlobalException("线上资金渠道仅支持 WECHAT、HUIFU、ALIPAY");
        }
        return normalized;
    }

    @Override
    public void recordOnlineChange(Integer shopId, String payChannel, String eventKey,
                                   BigDecimal changeAmount, String changeReasons, Long addTime) {
        if (shopId == null || shopId <= 0 || changeAmount == null
                || eventKey == null || eventKey.isBlank() || eventKey.length() > 128) {
            throw new GlobalException("线上资金日志缺少有效的店铺、金额或事件标识");
        }
        if (changeAmount.signum() == 0) {
            return;
        }
        Account account = new Account();
        account.setShopId(shopId);
        account.setPayChannel(normalizeChannel(payChannel));
        account.setEventKey(eventKey);
        account.setChangeAmount(changeAmount);
        account.setRemarks(changeReasons);
        account.setAddTime(addTime == null ? StringUtils.getCurrentTime() : addTime);
        account.setBalanceType(2);
        // 线上账本记录渠道资金事件，不以日志累计或当前查询值伪造历史可用余额。
        account.setNewShopMoney(null);
        getBaseMapper().insertOnlineEvent(account);
    }

    @Override
    public void refundWithDrawLog(Integer shopId, BigDecimal amount) {

        Shop shop = this.changeShopAccount(shopId, amount, 2);
        BigDecimal oldShopMoney = shop.getShopMoney().subtract(amount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal oldFrozenMoney = shop.getFrozenMoney().add(amount).setScale(2, RoundingMode.HALF_UP);

        Account accountLog = new Account();
        accountLog.setShopMoney(oldShopMoney);
        accountLog.setFrozenMoney(oldFrozenMoney);
        accountLog.setNewShopMoney(shop.getShopMoney());
        accountLog.setNewFrozenMoney(shop.getFrozenMoney());
        accountLog.setShopId(shopId);
        accountLog.setType(1);
        accountLog.setIsNew(1);
        accountLog.setAddTime(StringUtils.getCurrentTime());

        this.save(accountLog);
    }

    @Override
    public void addWithDrawLog(Integer shopId, BigDecimal amount) {
        Shop oldShop = shopService.getById(shopId);
        Shop shop = this.changeShopAccount(shopId, amount, 1);
        Account accountLog = new Account();
        accountLog.setShopMoney(oldShop.getShopMoney());
        accountLog.setFrozenMoney(oldShop.getFrozenMoney());
        accountLog.setNewShopMoney(shop.getShopMoney());
        accountLog.setNewFrozenMoney(shop.getFrozenMoney());
        accountLog.setShopId(shopId);
        accountLog.setAddTime(StringUtils.getCurrentTime());
        // 1.店铺资金增加 2提现
        accountLog.setType(2);
        // 1是新版本
        accountLog.setIsNew(1);
        this.save(accountLog);
    }

    @Override
    public void completeWithDrawLog(Integer shopId, BigDecimal amount) {
        Shop oldShop = shopService.getById(shopId);
        Shop shop = this.changeShopAccount(shopId, amount, 3);
        Account accountLog = new Account();
        accountLog.setAddTime(StringUtils.getCurrentTime());
        accountLog.setShopMoney(oldShop.getShopMoney());
        accountLog.setFrozenMoney(oldShop.getFrozenMoney());
        accountLog.setNewShopMoney(shop.getShopMoney());
        accountLog.setNewFrozenMoney(shop.getFrozenMoney());
        accountLog.setShopId(shopId);
        accountLog.setType(1);
        accountLog.setIsNew(1);
        this.save(accountLog);
    }

    public Shop changeShopAccount(Integer shopId, BigDecimal amount, int type) {

        Shop shop = shopService.getById(shopId);
        LambdaUpdateChainWrapper<Shop> wrapper = shopService.lambdaUpdate().eq(Shop::getShopId, shopId);
        if (shop == null) {
            throw new RuntimeException("店铺不存在");
        }
        switch (type) {
            case 1:
                // 提现冻结：可用余额减少、冻结金额增加。可用余额不足则原子拒绝，防止超额提现/余额变负
                wrapper.ge(Shop::getShopMoney, amount)
                        .setDecrBy(Shop::getShopMoney, amount).setIncrBy(Shop::getFrozenMoney, amount);
                break;
            case 2:
                // 提现拒绝返还：可用余额增加、释放冻结金额（冻结金额释放不加硬守卫，避免脏数据卡死提现流转）
                wrapper.setIncrBy(Shop::getShopMoney, amount).setDecrBy(Shop::getFrozenMoney, amount);
                break;
            case 3:
                // 打款完成：释放冻结金额
                wrapper.setDecrBy(Shop::getFrozenMoney, amount);
                break;
            case 4:
                wrapper.setIncrBy(Shop::getShopMoney, amount);
                break;
            default:
                return shop;
        }
        if (!wrapper.update()) {
            throw new GlobalException("店铺可用余额不足");
        }
        return shopService.getById(shopId);
    }

}
