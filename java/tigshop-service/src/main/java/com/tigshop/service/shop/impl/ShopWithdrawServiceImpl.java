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
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.dto.shop.*;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.finance.StatementType;
import com.tigshop.bean.enums.finance.MerchantApplicationStatus;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.shop.ShopType;
import com.tigshop.bean.enums.shop.ShopWithdrawStatus;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.finance.RecordRate;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.shop.ShopWithdraw;
import com.tigshop.bean.param.finance.statement.StatementOutputSaveParam;
import com.tigshop.bean.param.finance.statement.StatementSaveParam;
import com.tigshop.bean.vo.common.StatusListVO;
import com.tigshop.bean.vo.setting.config.WithdrawalSettingsVO;
import com.tigshop.bean.vo.shop.ShopWithdrawSummaryVO;
import com.tigshop.bean.vo.shop.ShopWithdrawVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.*;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.shop.ShopWithdrawMapper;
import com.tigshop.service.authority.AdminRoleService;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.RecordRateService;
import com.tigshop.service.finance.MerchantApplicationService;
import com.tigshop.service.finance.StatementOutputService;
import com.tigshop.service.finance.StatementService;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.shop.AccountService;
import com.tigshop.service.shop.ShopWithdrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tigshop.bean.enums.finance.StatementType.SHOP_WITHDRAWAL;
import static com.tigshop.bean.enums.finance.StatementType.STORE_WITHDRAWAL;

/**
 * 商家账户表服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
@RequiredArgsConstructor
public class ShopWithdrawServiceImpl extends BaseServiceImpl<ShopWithdrawMapper, ShopWithdraw> implements ShopWithdrawService {

    private final AdminRoleService adminRoleService;

    private final AdminUserService adminUserService;

    private final AccountService accountService;
    private final RecordRateService recordRateService;
    private final MerchantApplicationService merchantApplicationService;
    private final StatementOutputService statementOutputService;
    private final StatementService statementService;

    private final RedisCache redisCache;
    private final ConfigService configService;
    private final AdminMsgService adminMsgService;
    private final TigshopProperties tigshopProperties;
    private final ShopMapper shopMapper;
    private final com.tigshop.common.core.TranslatePackage translatePackage;
    private final com.tigshop.service.finance.SubMerchantService subMerchantService;
    private final com.tigshop.service.finance.profitsharing.ProfitSharingClientRegistry profitSharingClientRegistry;
    private final PlatformTransactionManager transactionManager;

    private static final String PAY_CHANNEL_WECHAT = "WECHAT";
    private static final String PAY_CHANNEL_HUIFU = "HUIFU";
    private static final String PAY_CHANNEL_ALIPAY = "ALIPAY";

    /**
     * 三渠道独立体系（CR-20260721-144356）：申请渠道由请求写入的 payChannel 决定，
     * 为空默认 WECHAT（兼容存量单入口 /create）。大写归一。
     */
    private String resolveCreateChannel(ShopWithdrawCreateDTO createDTO) {
        if (createDTO != null && StringUtils.isNotEmpty(createDTO.getPayChannel())) {
            String channel = createDTO.getPayChannel().trim().toUpperCase(java.util.Locale.ROOT);
            if (!Set.of(PAY_CHANNEL_WECHAT, PAY_CHANNEL_HUIFU, PAY_CHANNEL_ALIPAY).contains(channel)) {
                throw new GlobalException("不支持的线上提现渠道");
            }
            return channel;
        }
        return PAY_CHANNEL_WECHAT;
    }

    /**
     * 归一化取现渠道：优先记录自带 pay_channel，缺省 WECHAT（存量兼容）。大写归一。
     */
    private String resolveRecordChannel(ShopWithdraw shopWithdraw) {
        if (shopWithdraw != null && StringUtils.isNotEmpty(shopWithdraw.getPayChannel())) {
            return shopWithdraw.getPayChannel().trim().toUpperCase(java.util.Locale.ROOT);
        }
        return PAY_CHANNEL_WECHAT;
    }

    /**
     * 线上提现配置（三渠道共用一套，CR-20260722-114500）：开关/限额/频次统一读 online* 一套。
     */
    private OnlineWithdrawConfig resolveOnlineConfig(WithdrawalSettingsVO s) {
        return new OnlineWithdrawConfig(
                s.getOnlineWithdrawEnabled(), s.getOnlineMinAmount(), s.getOnlineMaxAmount(),
                s.getOnlineWithdrawalFrequencyUnit(), s.getOnlineWithdrawalFrequencyCount());
    }

    /**
     * 线上收款方式（onlineWithdrawChannels，多选）是否启用指定渠道；
     * 配置缺失/空时兜底 WECHAT（零回归，CR-20260722-114500）。
     */
    private boolean isChannelEnabled(WithdrawalSettingsVO s, String channel) {
        java.util.List<String> channels = s.getOnlineWithdrawChannels();
        if (channels == null || channels.isEmpty()) {
            return PAY_CHANNEL_WECHAT.equals(channel);
        }
        for (String c : channels) {
            if (c != null && c.trim().toUpperCase(java.util.Locale.ROOT).equals(channel)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 线上提现按渠道解析出的开关/限额/频次配置载体。
     */
    private static class OnlineWithdrawConfig {
        final Integer enabled;
        final BigDecimal min;
        final BigDecimal max;
        final Integer freqUnit;
        final Integer freqCount;

        OnlineWithdrawConfig(Integer enabled, BigDecimal min, BigDecimal max, Integer freqUnit, Integer freqCount) {
            this.enabled = enabled;
            this.min = min;
            this.max = max;
            this.freqUnit = freqUnit;
            this.freqCount = freqCount;
        }
    }


    @Override
    public Page<ShopWithdrawVO> list(ShopWithdrawListDTO listDTO) {
        // 分页
        Page<ShopWithdraw> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<ShopWithdraw> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        Integer status = listDTO.getStatus();
        if (status != null) {
            queryWrapper.eq(ShopWithdraw::getStatus, status);
        }

        if (listDTO.getWithdrawType() != null) {
            if (listDTO.getWithdrawType() != 1 && listDTO.getWithdrawType() != 2) {
                throw new GlobalException("提现类型只能为1或2");
            }
            queryWrapper.eq(ShopWithdraw::getWithdrawType, listDTO.getWithdrawType());
        }

        // 渠道过滤（三渠道独立体系）：仅返回该渠道线上提现记录；微信兼容存量 pay_channel 为空的线上记录
        if (StringUtils.isNotEmpty(listDTO.getPayChannel())) {
            String ch = listDTO.getPayChannel().trim().toUpperCase(java.util.Locale.ROOT);
            if (!Set.of(PAY_CHANNEL_WECHAT, PAY_CHANNEL_HUIFU, PAY_CHANNEL_ALIPAY).contains(ch)) {
                throw new GlobalException("提现渠道仅支持 WECHAT、HUIFU、ALIPAY");
            }
            if (Integer.valueOf(1).equals(listDTO.getWithdrawType())) {
                throw new GlobalException("线下提现不支持线上渠道筛选");
            }
            queryWrapper.eq(ShopWithdraw::getWithdrawType, 2);
            if (PAY_CHANNEL_WECHAT.equals(ch)) {
                queryWrapper.and(w -> w.eq(ShopWithdraw::getPayChannel, ch).or().isNull(ShopWithdraw::getPayChannel));
            } else {
                queryWrapper.eq(ShopWithdraw::getPayChannel, ch);
            }
        }
        String addTimeStart = listDTO.getAddTimeStart();
        //将时间格式转换成时间戳
        if (StringUtils.isNotEmpty(addTimeStart)) {
            addTimeStart = addTimeStart + " 00:00:00";
            long addTimeStartTimestamp = DateUtil.parse(addTimeStart).getTime() / 1000;
            queryWrapper.ge(ShopWithdraw::getAddTime, addTimeStartTimestamp);
        }
        String addTimeEnd = listDTO.getAddTimeEnd();
        if (StringUtils.isNotEmpty(addTimeEnd)) {
            addTimeEnd = addTimeEnd + " 23:59:59";
            long addTimeEndTimestamp = DateUtil.parse(addTimeEnd).getTime() / 1000;
            queryWrapper.le(ShopWithdraw::getAddTime, addTimeEndTimestamp);
        }

        // 数据隔离：店铺/门店 admin 强制按当前 shopId 过滤；平台 admin (shopId=0) 才允许按筛选 shopId 查询
        Integer currentShopId = HeaderUtils.getShopId();
        if (currentShopId != null && currentShopId > 0) {
            queryWrapper.eq(ShopWithdraw::getShopId, currentShopId);
        } else if (listDTO.getShopId() != null) {
            queryWrapper.eq(ShopWithdraw::getShopId, listDTO.getShopId());
        }

        // 搜索关键字
        /*String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like("", keyword);
        }*/
        // 执行查询
        Page<ShopWithdraw> shopWithdrawPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<ShopWithdraw> shopWithdrawPageRecords = shopWithdrawPage.getRecords();
        // 转换为VO
        List<ShopWithdrawVO> shopWithdrawVOList = shopWithdrawPageRecords.stream()
                .map(shopWithdraw -> {
                    ShopWithdrawVO shopWithdrawVO = new ShopWithdrawVO();
                    BeanUtils.copyProperties(shopWithdraw, shopWithdrawVO);
                    long timestampInSeconds = shopWithdraw.getAddTime();
                    Date date = new Date(timestampInSeconds * 1000L);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    shopWithdrawVO.setAddTime(sdf.format(date));
                    shopWithdrawVO.setStatusText(ShopWithdrawStatus.getStatusName(shopWithdraw.getStatus()));
                    fillRecordRate(shopWithdrawVO);
                    shopWithdrawVO.setAccountData(JSONUtil.parseObj(shopWithdraw.getAccountData()));
                    return shopWithdrawVO;
                }).toList();

        return PageUtil.convertPage(shopWithdrawPage, shopWithdrawVOList);
    }

    private void fillRecordRate(ShopWithdrawVO shopWithdrawVO) {
        Integer shopWithdrawLogId = shopWithdrawVO.getShopWithdrawLogId();
        RecordRate one = recordRateService.lambdaQuery().eq(RecordRate::getRecordId, shopWithdrawLogId).
                eq(RecordRate::getRecordType, tigshopProperties.getIsO2o() == 1 ? STORE_WITHDRAWAL.getCode() : SHOP_WITHDRAWAL.getCode())
                .eq(RecordRate::getShopId,shopWithdrawVO.getShopId())
                .last("limit 1").one();
        if(Objects.isNull(one)){
            shopWithdrawVO.setHandlingFee(BigDecimal.ZERO);
            shopWithdrawVO.setHandlingRate(BigDecimal.ZERO);
        }else{
            BigDecimal shopWithdrawalRate = tigshopProperties.getIsO2o() == 1
                    ? one.getStorefrontWithdrawalFee() : one.getShopWithdrawalFee();
            if (shopWithdrawalRate == null || shopWithdrawVO.getAmount() == null) {
                shopWithdrawVO.setHandlingFee(BigDecimal.ZERO);
                shopWithdrawVO.setHandlingRate(BigDecimal.ZERO);
            } else {
                BigDecimal handingFeeAmount = shopWithdrawVO.getAmount().multiply(shopWithdrawalRate)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                shopWithdrawVO.setHandlingFee(handingFeeAmount);
                shopWithdrawVO.setHandlingRate(shopWithdrawalRate);
            }
        }
    }

    @Override
    public ShopWithdrawVO detail(Integer id) {
        if (id != null) {
            ShopWithdraw shopWithdraw = this.getById(id);
            ShopWithdrawVO shopWithdrawVO = new ShopWithdrawVO();
            BeanUtils.copyProperties(shopWithdraw, shopWithdrawVO);
            shopWithdrawVO.setAccountData(JSONUtil.parseObj(shopWithdraw.getAccountData()));
            shopWithdrawVO.setAddTime(TigUtils.handelTime(shopWithdraw.getAddTime()));
            shopWithdrawVO.setStatusText(ShopWithdrawStatus.getStatusName(shopWithdraw.getStatus()));
            shopWithdrawVO.setPaymentVoucher(shopWithdraw.getPaymentVoucher());
            fillRecordRate(shopWithdrawVO);
            return shopWithdrawVO;
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(ShopWithdrawCreateDTO createDTO) {
        String uuid = UUID.randomUUID().toString();
        boolean lock = redisCache.tryLock("shopWithdrawCreateShopId::" + HeaderUtils.getShopId(), uuid, 30, TimeUnit.SECONDS);
        if (lock) {
            try {
                if (createDTO != null) {
                    //店铺提现校验
                    String channelSubMchId = this.shopWithdrawCheck(createDTO);
                    ShopWithdraw shopWithdraw = new ShopWithdraw();
                    shopWithdraw.setShopId(getShopId());
                    String snPrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    String snSuffix = RandomUtil.randomNumbers(5);
                    shopWithdraw.setWithdrawSn(snPrefix + snSuffix);
                    shopWithdraw.setAmount(createDTO.getAmount());
                    shopWithdraw.setStatus(ShopWithdrawStatus.PENDING_REVIEW.getCode());
                    shopWithdraw.setAddTime(StringUtils.getCurrentTime());
                    shopWithdraw.setRemark(createDTO.getRemark());
                    shopWithdraw.setMerchantAccountId(createDTO.getMerchantAccountId());
                    int withdrawType = createDTO.getWithdrawType() == null ? 1 : createDTO.getWithdrawType();
                    shopWithdraw.setWithdrawType(withdrawType);
                    shopWithdraw.setOfflineFrozenAmount(withdrawType == 2 ? BigDecimal.ZERO : createDTO.getAmount());
                    shopWithdraw.setChannelSubMchId(channelSubMchId);
                    // 线上提现记录自带渠道，供审核后执行与列表/审核分渠道使用（三渠道独立体系）
                    if (withdrawType == 2) {
                        String channel = resolveCreateChannel(createDTO);
                        shopWithdraw.setPayChannel(channel);
                        shopWithdraw.setAccountData(resolveOnlineAccountData(createDTO, channel));
                    } else {
                        shopWithdraw.setAccountData(JSONUtil.toJsonStr(createDTO.getAccountData()));
                    }
                    boolean save = this.save(shopWithdraw);
                    if (save) {
                        notifyPlatformWithdrawal(shopWithdraw);
                    }
                    if (withdrawType == 1) {
                        accountService.addWithDrawLog(shopWithdraw.getShopId(), shopWithdraw.getAmount());
                    }
                    if (withdrawType == 1) {
                        recordRateService.saveRecordRate(shopWithdraw.getShopWithdrawLogId(),
                                tigshopProperties.getIsO2o() == 1 ? STORE_WITHDRAWAL.getCode() : SHOP_WITHDRAWAL.getCode(), shopWithdraw.getShopId(), 0);
                    }
                    //审核通过的时候才进行添加
//                    StatementSaveParam statementSaveParam = new StatementSaveParam(shopWithdraw);
//                    statementService.saveStatement(statementSaveParam);
                    return save;
                }
            } finally {
                redisCache.unlock("shopWithdrawCreateShopId::" + HeaderUtils.getShopId(), uuid);
            }
        }

        return false;
    }

    private String resolveOnlineAccountData(ShopWithdrawCreateDTO createDTO, String channel) {
        var application = merchantApplicationService.current(channel);
        if (application != null && MerchantApplicationStatus.FINISH.getCode().equals(application.getStatus())
                && channel.equalsIgnoreCase(application.getPayChannel())
                && StringUtils.isNotEmpty(application.getBankAccountNumber())) {
            Map<String, Object> account = new LinkedHashMap<>();
            account.put("accountName", application.getBankAccountName());
            account.put("accountNo", application.getBankAccountNumber());
            account.put("bankName", application.getBankName());
            account.put("bankBranch", application.getBankBranchName());
            account.put("accountType", application.getBankAccountType());
            return JSONUtil.toJsonStr(account);
        }
        // 线上出金不信任前端提交的收款账户；没有匹配的成功进件时交给渠道校验空账户。
        return JSONUtil.createObj().toString();
    }
    /**
     * 店铺提现申请提交后，通知总后台财务人员审核。
     */
    private void notifyPlatformWithdrawal(ShopWithdraw shopWithdraw) {
        Shop shop = shopMapper.selectById(shopWithdraw.getShopId());
        String shopTitle = shop == null || StringUtils.isEmpty(shop.getShopTitle())
                ? String.valueOf(shopWithdraw.getShopId()) : shop.getShopTitle();
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("shopWithdrawLogId", shopWithdraw.getShopWithdrawLogId());
        relatedData.put("route", "withdrawal");
        AdminMsgCreateDTO message = AdminMsgCreateDTO.builder()
                .msgType(AdminMsgTypeEnum.WITHDRAWAL_REQUEST.getCatId())
                .shopId(0)
                .vendorId(0)
                .isBroadcast(1)
                .title("您有新的店铺提现申请,申请店铺：" + shopTitle)
                .content("店铺【" + shopTitle + "】申请提现,申请金额：" + shopWithdraw.getAmount()
                        + configService.getCurrencyName())
                .relatedData(relatedData)
                .build();
        adminMsgService.createMessage(message);
    }



    private String shopWithdrawCheck(ShopWithdrawCreateDTO createDTO) {
        if (createDTO.getAmount() == null || createDTO.getAmount().signum() <= 0
                || createDTO.getAmount().stripTrailingZeros().scale() > 2) {
            throw new GlobalException("提现金额必须大于0且最多两位小数");
        }
        if (createDTO.getWithdrawType() != null && createDTO.getWithdrawType() != 1 && createDTO.getWithdrawType() != 2) {
            throw new GlobalException("提现类型只能为1或2");
        }
        WithdrawalSettingsVO withdrawalSettings = configService.getWithdrawalSettings();
        boolean online = Integer.valueOf(2).equals(createDTO.getWithdrawType());
        String channel = online ? resolveCreateChannel(createDTO) : null;
        String channelSubMchId = null;
        OnlineWithdrawConfig onlineCfg = online ? resolveOnlineConfig(withdrawalSettings) : null;

        if (online) {
            requireOnlineChannel(channel);
            // 线上提现：三渠道共用一套「线上提现设置」校验开关 + 限额（CR-20260722-114500）
            if (onlineCfg.enabled == null || onlineCfg.enabled != 1) {
                throw new GlobalException("平台设置的线上提现功能未启用");
            }
            // 线上收款方式：该渠道未在启用列表则拒绝（缺省兜底 WECHAT，零回归）
            if (!isChannelEnabled(withdrawalSettings, channel)) {
                throw new GlobalException("平台未启用该渠道的线上提现：" + channel);
            }
            BigDecimal onlineMin = onlineCfg.min;
            BigDecimal onlineMax = onlineCfg.max;
            if (onlineMin != null && createDTO.getAmount().compareTo(onlineMin) < 0) {
                throw new GlobalException("平台设置的线上提现最小金额为:" + onlineMin);
            }
            // 上限为 0 视为不限
            if (onlineMax != null && onlineMax.compareTo(BigDecimal.ZERO) > 0
                    && createDTO.getAmount().compareTo(onlineMax) > 0) {
                throw new GlobalException("平台设置的线上提现最大金额为:" + onlineMax);
            }
        } else {
            // 1. 检查提现功能是否启用
            if (withdrawalSettings.getWithdrawalEnabled() == null || withdrawalSettings.getWithdrawalEnabled() != 1) {
                throw new GlobalException("平台设置的提现功能未启用");
            }

            // 2. 检查提现金额是否在允许范围内
            if (createDTO.getAmount().compareTo(withdrawalSettings.getMinAmount()) < 0 ||
                    createDTO.getAmount().compareTo(withdrawalSettings.getMaxAmount()) > 0) {
                throw new GlobalException("平台设置的提现金额限制为:" + withdrawalSettings.getMinAmount() + "-" + withdrawalSettings.getMaxAmount());
            }
        }

        // 2.1 校验店铺可用余额是否充足（防超额提现，最终以原子扣减兜底）
        if (online) {
            lockShopForOnlineWithdraw(getShopId());
            channelSubMchId = requiredSubMerchantId(getShopId().longValue(), channel);
            BigDecimal available = queryChannelBalanceStrict(channelSubMchId, channel);
            // 同一店铺申请锁内预占待审核/未明确受理申请；已受理金额由渠道可用余额反映。
            BigDecimal reserved = pendingOnlineAmount(getShopId(), channel, null);
            if (available.subtract(reserved).compareTo(createDTO.getAmount()) < 0) {
                throw new GlobalException("该渠道可提现余额不足（已扣除待审核及结果未明确的申请）");
            }
        } else {
            Shop shop = shopMapper.selectById(getShopId());
            if (shop == null || shop.getShopMoney() == null
                    || shop.getShopMoney().compareTo(createDTO.getAmount()) < 0) {
                throw new GlobalException("店铺可用余额不足");
            }
        }

        // 3. 检查提现频率限制（线上/线下各按自身配置，线上仅统计线上提现）
        Integer frequencyUnit = online
                ? onlineCfg.freqUnit
                : withdrawalSettings.getWithdrawalFrequencyUnit();
        Integer frequencyCountCfg = online
                ? onlineCfg.freqCount
                : withdrawalSettings.getWithdrawalFrequencyCount();
        if (frequencyUnit != null && frequencyCountCfg != null && frequencyCountCfg > 0) {
            long frequencyCount = frequencyCountCfg;
            long shopId = HeaderUtils.getShopId();
            long startTime;
            String periodDesc;
            switch (frequencyUnit) {
                case 1 -> {
                    startTime = DateUtil.beginOfDay(new Date()).getTime() / 1000;
                    periodDesc = "每日";
                }
                case 2 -> {
                    startTime = DateUtil.beginOfMonth(new Date()).getTime() / 1000;
                    periodDesc = "每月";
                }
                case 3 -> {
                    startTime = DateUtil.beginOfYear(new Date()).getTime() / 1000;
                    periodDesc = "每年";
                }
                default -> throw new GlobalException("提现频率参数错误");
            }
            String tip = "平台设置的" + (online ? "线上" : "") + periodDesc + "提现次数限制为:" + frequencyCount;
            checkWithdrawalFrequency(shopId, startTime, frequencyCount, tip, online);
        }
        return channelSubMchId;
    }

    /**
     * 检查指定时间范围内的提现次数是否超过限制
     *
     * @param shopId     商户ID
     * @param startTime    时间范围起始时间戳（秒）
     * @param maxCount     最大允许次数
     * @param errorMessage 超限提示信息
     */
    private void checkWithdrawalFrequency(Long shopId, long startTime, long maxCount, String errorMessage, boolean onlineOnly) {
        long count = this.count(Wrappers.lambdaQuery(ShopWithdraw.class)
                .eq(ShopWithdraw::getShopId, shopId)
                .ge(ShopWithdraw::getAddTime, startTime)
                .eq(ShopWithdraw::getWithdrawType, onlineOnly ? 2 : 1));
        if (count >= maxCount) {
            throw new GlobalException(errorMessage);
        }
    }

    @Override
    public boolean update(ShopWithdrawUpdateDTO updateDTO) {
        if (updateDTO != null) {
            ShopWithdraw shopWithdraw = new ShopWithdraw();
            BeanUtils.copyProperties(updateDTO, shopWithdraw);
            return this.updateById(shopWithdraw);
        }
        return false;
    }

    @Override
    public StatusListVO<Map<Integer, String>> getStatusList() {
        Map<Integer, String> statusList = new LinkedHashMap<>();
        for (ShopWithdrawStatus status : ShopWithdrawStatus.values()) {
            if(status.getCode() == 1){
                continue;
            }
            statusList.put(status.getCode(), translatePackage.translate(status.getDescription(), com.tigshop.common.enums.DataType.API.getCode()));
        }
        return new StatusListVO<>(statusList);
    }

    @Override
    public void audit(ShopWithdrawAuditDTO dto) {
        AdminUser adminUser = adminUserService.getById(SecurityUtils.getCurrentAdminId());
        AdminRole adminRole = adminRoleService.getById(adminUser.getRoleId());
        if (!adminRole.getAdminType().equals(AdminTypeEnum.ADMIN.getCode())) {
            throw new GlobalException("没有权限");
        }
        ShopWithdraw shopWithdraw = this.getById(dto.getId());

        if (shopWithdraw != null && Integer.valueOf(2).equals(shopWithdraw.getWithdrawType())) {
            auditOnlineWithdraw(shopWithdraw, dto);
            return;
        }

        if (!shopWithdraw.getStatus().equals(ShopWithdrawStatus.PENDING_REVIEW.getCode())) {
            throw new GlobalException("不是待审核状态");
        }


        shopWithdraw.setStatus(dto.getStatus());
        shopWithdraw.setAuditRemark(dto.getAuditRemark());
        this.updateById(shopWithdraw);

        if (shopWithdraw.getStatus().equals(ShopWithdrawStatus.AUDIT_NOT_PASS.getCode())) {
            accountService.refundWithDrawLog(shopWithdraw.getShopId(), shopWithdraw.getAmount());
        }

        // 添加流水
        StatementOutputSaveParam statementOutputSaveParam = new StatementOutputSaveParam(shopWithdraw, false);
        statementOutputService.saveStatementOutput(statementOutputSaveParam);

        if(ShopWithdrawStatus.STATUS_WAIT_PAYMENT.getCode().equals(shopWithdraw.getStatus())){
            // 添加提现对账单
            StatementSaveParam statementSaveParam = new StatementSaveParam(shopWithdraw);
            statementService.saveStatement(statementSaveParam);

            // 微信线上提现：审核通过后直接调用微信发起提现，由定时任务轮询最终状态
            if (Integer.valueOf(2).equals(shopWithdraw.getWithdrawType())) {
                initiateOnlineWithdraw(shopWithdraw);
            }
        }

        // 获取手续费率
        RecordRate recordRate = statementService.getRecordRate(shopWithdraw.getShopWithdrawLogId(), StatementType.SHOP_WITHDRAWAL.getCode());
        // 添加手续费流水
        StatementOutputSaveParam statementOutputSaveParam2 = new StatementOutputSaveParam(shopWithdraw, true);
        BigDecimal newAmount = shopWithdraw.getAmount().multiply(recordRate.getShopWithdrawalFee())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        statementOutputSaveParam2.setExpenditure(newAmount);
        statementOutputService.saveStatementOutput(statementOutputSaveParam2);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void uploadPayVoucher(ShopWithdrawPayVoucherDTO dto) {
        if (dto.getId() == null) {
            throw new GlobalException("#id错误");
        }
        ShopWithdraw detail = this.getById(dto.getId());
        if (detail != null && Integer.valueOf(2).equals(detail.getWithdrawType())) {
            throw new GlobalException("线上提现以渠道结果为准，不支持人工打款凭证完成");
        }
        if (!ShopWithdrawStatus.STATUS_WAIT_PAYMENT.getCode().equals(detail.getStatus())) {
            throw new GlobalException("状态错误");
        }
        if (dto.getPaymentVoucher() == null || dto.getPaymentVoucher().isEmpty()) {
            throw new GlobalException("请上传打款凭证");
        }
        detail.setPaymentVoucher(dto.getPaymentVoucher());
        detail.setStatus(ShopWithdrawStatus.AUDIT_PASS.getCode());
        this.updateById(detail);

        accountService.completeWithDrawLog(detail.getShopId(), detail.getAmount());
    }

    @Override
    public Page<ShopWithdrawSummaryVO> summaryList(ShopWithdrawSummaryListDTO listDTO) {
        // 提现时间范围转时间戳
        Long withdrawTimeStartTimestamp = null;
        Long withdrawTimeEndTimestamp = null;
        if (StringUtils.isNotEmpty(listDTO.getWithdrawTimeStart())) {
            withdrawTimeStartTimestamp = DateUtil.parse(listDTO.getWithdrawTimeStart() + " 00:00:00").getTime() / 1000;
        }
        if (StringUtils.isNotEmpty(listDTO.getWithdrawTimeEnd())) {
            withdrawTimeEndTimestamp = DateUtil.parse(listDTO.getWithdrawTimeEnd() + " 23:59:59").getTime() / 1000;
        }

        // 提现金额聚合的时间范围条件（withdraw 表别名 t1，时间戳为服务端计算的 long，可安全内联）
        StringBuilder withdrawTimeCond = new StringBuilder();
        if (withdrawTimeStartTimestamp != null) {
            withdrawTimeCond.append(" AND t1.add_time >= ").append(withdrawTimeStartTimestamp);
        }
        if (withdrawTimeEndTimestamp != null) {
            withdrawTimeCond.append(" AND t1.add_time <= ").append(withdrawTimeEndTimestamp);
        }

        // shop(t) LEFT JOIN shop_withdraw(t1)，两个金额按状态条件聚合（单子表 JOIN，无扇出，金额准确）
        MPJLambdaWrapper<Shop> wrapper = new MPJLambdaWrapper<Shop>()
                .select(Shop::getShopId, Shop::getShopTitle, Shop::getShopLogo,
                        Shop::getStatus, Shop::getShopMoney, Shop::getAddTime)
                .select("COALESCE(SUM(CASE WHEN t1.status = 3" + withdrawTimeCond
                        + " THEN t1.amount ELSE 0 END), 0) AS withdrawn_amount")
                .select("COALESCE(SUM(CASE WHEN t1.status IN (0, 4)" + withdrawTimeCond
                        + " THEN t1.amount ELSE 0 END), 0) AS pending_withdraw_amount")
                .leftJoin(ShopWithdraw.class, ShopWithdraw::getShopId, Shop::getShopId);

        // shop 过滤条件
        wrapper.and(w -> w.isNull(Shop::getIsDelete).or().eq(Shop::getIsDelete, 0))
                .like(StringUtils.isNotEmpty(listDTO.getShopTitle()), Shop::getShopTitle, listDTO.getShopTitle())
                .eq(listDTO.getStatus() != null, Shop::getStatus, listDTO.getStatus())
                .eq(listDTO.getShopType() != null, Shop::getShopType, listDTO.getShopType())
                .like(StringUtils.isNotEmpty(listDTO.getKeyword()), Shop::getShopTitle, listDTO.getKeyword());
        if (StringUtils.isNotEmpty(listDTO.getAddTimeStart())) {
            wrapper.ge(Shop::getAddTime, DateUtil.parse(listDTO.getAddTimeStart() + " 00:00:00").getTime() / 1000);
        }
        if (StringUtils.isNotEmpty(listDTO.getAddTimeEnd())) {
            wrapper.le(Shop::getAddTime, DateUtil.parse(listDTO.getAddTimeEnd() + " 23:59:59").getTime() / 1000);
        }

        // 提现时间范围：仅保留范围内有提现记录的店铺（与原逻辑一致）
        if (withdrawTimeStartTimestamp != null || withdrawTimeEndTimestamp != null) {
            List<Integer> filteredShopIds = getShopIdsByWithdrawTimeRange(withdrawTimeStartTimestamp, withdrawTimeEndTimestamp);
            if (filteredShopIds.isEmpty()) {
                return new Page<>(listDTO.getPage(), listDTO.getSize());
            }
            wrapper.in(Shop::getShopId, filteredShopIds);
        }

        // 分组 + 排序（计算字段按聚合别名排序，真实列加 t. 前缀避免与 withdraw 表字段歧义）
        wrapper.groupBy(Shop::getShopId)
                .orderBy(true, "asc".equalsIgnoreCase(listDTO.getSortOrder()),
                        resolveShopSortColumn(listDTO.getSortField()));

        Page<ShopWithdrawSummaryVO> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        Page<ShopWithdrawSummaryVO> result = shopMapper.selectJoinPage(page, ShopWithdrawSummaryVO.class, wrapper);

        // 状态文案回填（保持原逻辑）
        result.getRecords().forEach(vo -> {
            try {
                vo.setStatusText(ShopType.getTypeName(vo.getStatus()));
            } catch (Exception e) {
                vo.setStatusText("");
            }
        });
        return result;
    }

    /**
     * 汇总列表排序字段白名单：计算字段→聚合别名，真实列→t.前缀，未知→主键（防注入）
     */
    private String resolveShopSortColumn(String sortField) {
        if (sortField == null) {
            return "t.shop_id";
        }
        switch (sortField) {
            case "shopTitle":
                return "t.shop_title";
            case "shopMoney":
                return "t.shop_money";
            case "status":
                return "t.status";
            case "addTime":
                return "t.add_time";
            case "withdrawnAmount":
                return "withdrawn_amount";
            case "pendingWithdrawAmount":
                return "pending_withdraw_amount";
            case "shopId":
            default:
                return "t.shop_id";
        }
    }

    private List<Integer> getShopIdsByWithdrawTimeRange(Long withdrawTimeStartTimestamp, Long withdrawTimeEndTimestamp) {
        LambdaQueryWrapper<ShopWithdraw> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(ShopWithdraw::getShopId);
        if (withdrawTimeStartTimestamp != null) {
            queryWrapper.ge(ShopWithdraw::getAddTime, withdrawTimeStartTimestamp);
        }
        if (withdrawTimeEndTimestamp != null) {
            queryWrapper.le(ShopWithdraw::getAddTime, withdrawTimeEndTimestamp);
        }
        return this.list(queryWrapper).stream()
                .map(ShopWithdraw::getShopId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getDualBalance() {
        Map<String, Object> result = new HashMap<>();
        Integer shopId = HeaderUtils.getShopId();
        BigDecimal shopMoney = BigDecimal.ZERO;
        if (shopId != null && shopId > 0) {
            Shop shop = shopMapper.selectById(shopId);
            if (shop != null && shop.getShopMoney() != null) {
                shopMoney = shop.getShopMoney();
            }
        }
        result.put("shopMoney", shopMoney);

        // 逐渠道展示；未开启渠道不输出，查询失败与零余额必须明确区分。
        Map<String, BigDecimal> channelBalances = new LinkedHashMap<>();
        Map<String, String> channelStatuses = new LinkedHashMap<>();
        result.put("profitSharingEnabled", configService.isProfitSharingEnabled());
        if (shopId != null && shopId > 0) {
            for (String channel : new String[]{PAY_CHANNEL_WECHAT, PAY_CHANNEL_HUIFU, PAY_CHANNEL_ALIPAY}) {
                if (!isOnlineChannelEnabled(channel)) {
                    continue;
                }
                if (PAY_CHANNEL_ALIPAY.equals(channel)) {
                    channelStatuses.put(channel, "UNAVAILABLE");
                    continue;
                }
                try {
                    var subMerchant = subMerchantService.getActiveByShop(shopId.longValue(), channel);
                    if (subMerchant == null || StringUtils.isEmpty(subMerchant.getSubMchId())) {
                        channelStatuses.put(channel, "UNBOUND");
                        continue;
                    }
                    var balance = profitSharingClientRegistry.get(channel).queryBalance(subMerchant.getSubMchId());
                    if (balance == null || balance.getAvailableAmount() == null) {
                        channelStatuses.put(channel, "ERROR");
                    } else {
                        channelBalances.put(channel, balance.getAvailableAmount());
                        channelStatuses.put(channel, "AVAILABLE");
                    }
                } catch (Exception e) {
                    channelStatuses.put(channel, "ERROR");
                }
            }
        }
        result.put("channelBalances", channelBalances);
        result.put("channelStatuses", channelStatuses);
        // 兼容旧前端字段：wechatBalance/onlineBalance/onlineChannel（微信零回归）
        BigDecimal wechatBalance = channelBalances.get(PAY_CHANNEL_WECHAT);
        result.put("wechatBalance", wechatBalance);
        result.put("onlineBalance", wechatBalance);
        result.put("onlineChannel", wechatBalance != null ? PAY_CHANNEL_WECHAT : null);
        return result;
    }

    private boolean isOnlineChannelEnabled(String channel) {
        if (!configService.isProfitSharingEnabled()
                || !"2".equals(configService.getConfigVal(SettingsEnum.SPLIT_PAYMENT_METHOD))) {
            return false;
        }
        String selected = configService.getConfigVal(SettingsEnum.PROFIT_SHARING_ONLINE_CHANNEL);
        if (StringUtils.isEmpty(selected)) {
            selected = "wechat";
        }
        if (!channel.equalsIgnoreCase(selected)) {
            return false;
        }
        return switch (channel) {
            case PAY_CHANNEL_WECHAT -> "2".equals(configService.getConfigVal(SettingsEnum.WECHAT_MCHID_TYPE));
            case PAY_CHANNEL_HUIFU -> "1".equals(configService.getConfigVal(SettingsEnum.HUIFU_PROFIT_SHARING_ENABLED));
            case PAY_CHANNEL_ALIPAY -> "1".equals(configService.getConfigVal(SettingsEnum.ALIPAY_PROFIT_SHARING_ENABLED));
            default -> false;
        };
    }

    private void requireOnlineChannel(String channel) {
        if (!isOnlineChannelEnabled(channel)) {
            throw new GlobalException("该渠道线上分账未开启");
        }
        if (PAY_CHANNEL_ALIPAY.equals(channel)) {
            throw new GlobalException("支付宝子商户余额查询及提现暂未支持");
        }
    }

    private String requiredSubMerchantId(long shopId, String channel) {
        var subMerchant = subMerchantService.getActiveByShop(shopId, channel);
        if (subMerchant == null || StringUtils.isEmpty(subMerchant.getSubMchId())) {
            throw new GlobalException("未绑定有效的" + channel + "子商户");
        }
        return subMerchant.getSubMchId();
    }

    private BigDecimal queryChannelBalanceStrict(String subMchId, String channel) {
        var balance = profitSharingClientRegistry.get(channel).queryBalance(subMchId);
        if (balance == null || balance.getAvailableAmount() == null) {
            throw new GlobalException("渠道可用余额查询失败，请稍后重试");
        }
        return balance.getAvailableAmount();
    }

    private void lockShopForOnlineWithdraw(Integer shopId) {
        // Redis租约到期后仍由数据库行锁保护申请预占，事务提交前不释放；只锁店铺，不修改线下资金。
        Shop shop = shopMapper.selectOne(Wrappers.<Shop>lambdaQuery().eq(Shop::getShopId, shopId).last("FOR UPDATE"));
        if (shop == null) {
            throw new GlobalException("店铺不存在");
        }
    }

    private BigDecimal pendingOnlineAmount(Integer shopId, String channel, Integer excludingId) {
        LambdaQueryWrapper<ShopWithdraw> query = new LambdaQueryWrapper<ShopWithdraw>()
                .eq(ShopWithdraw::getShopId, shopId).eq(ShopWithdraw::getWithdrawType, 2)
                .ne(excludingId != null, ShopWithdraw::getShopWithdrawLogId, excludingId)
                .and(w -> w.eq(ShopWithdraw::getStatus, ShopWithdrawStatus.PENDING_REVIEW.getCode())
                        .or().eq(ShopWithdraw::getStatus, ShopWithdrawStatus.STATUS_WAIT_PAYMENT.getCode())
                        .or(p -> p.eq(ShopWithdraw::getStatus, ShopWithdrawStatus.AUDIT_PROCESS.getCode())
                                .in(ShopWithdraw::getChannelStatus, "SUBMITTING", "UNKNOWN")));
        if (PAY_CHANNEL_WECHAT.equals(channel)) {
            query.and(w -> w.eq(ShopWithdraw::getPayChannel, channel).or().isNull(ShopWithdraw::getPayChannel));
        } else {
            query.eq(ShopWithdraw::getPayChannel, channel);
        }
        return this.list(query.last("FOR UPDATE")).stream().map(ShopWithdraw::getAmount).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void auditOnlineWithdraw(ShopWithdraw withdraw, ShopWithdrawAuditDTO dto) {
        if (withdraw.getOfflineFrozenAmount() == null || withdraw.getOfflineFrozenAmount().signum() != 0) {
            throw new GlobalException("历史线上提现包含待核对的线下冻结资金，请先核账处理");
        }
        boolean approved = ShopWithdrawStatus.STATUS_WAIT_PAYMENT.getCode().equals(dto.getStatus());
        if (!approved && !ShopWithdrawStatus.AUDIT_NOT_PASS.getCode().equals(dto.getStatus())) {
            throw new GlobalException("无效的提现审核状态");
        }
        String uuid = UUID.randomUUID().toString();
        String lockKey = "shopWithdrawCreateShopId::" + withdraw.getShopId();
        if (!redisCache.tryLock(lockKey, uuid, 30, TimeUnit.SECONDS)) {
            throw new GlobalException("该店铺提现处理中，请稍后重试");
        }
        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(tx -> {
                lockShopForOnlineWithdraw(withdraw.getShopId());
                if (approved) {
                    String channel = resolveRecordChannel(withdraw);
                    requireOnlineChannel(channel);
                    if (StringUtils.isEmpty(withdraw.getChannelSubMchId())) {
                        throw new GlobalException("线上提现缺少申请时子商户快照，请先核账处理");
                    }
                    BigDecimal available = queryChannelBalanceStrict(withdraw.getChannelSubMchId(), channel);
                    if (available.subtract(pendingOnlineAmount(withdraw.getShopId(), channel, withdraw.getShopWithdrawLogId()))
                            .compareTo(withdraw.getAmount()) < 0) {
                        throw new GlobalException("该渠道可提现余额不足");
                    }
                }
                boolean claimed = this.update(Wrappers.<ShopWithdraw>lambdaUpdate()
                        .eq(ShopWithdraw::getShopWithdrawLogId, withdraw.getShopWithdrawLogId())
                        .eq(ShopWithdraw::getStatus, ShopWithdrawStatus.PENDING_REVIEW.getCode())
                        .set(ShopWithdraw::getStatus, approved ? ShopWithdrawStatus.AUDIT_PROCESS.getCode() : dto.getStatus())
                        .set(ShopWithdraw::getAuditRemark, dto.getAuditRemark())
                        .set(ShopWithdraw::getChannelStatus, approved ? "SUBMITTING" : "REJECTED"));
                if (!claimed) {
                    throw new GlobalException("不是待审核状态，请刷新后重试");
                }
            });
            if (approved) {
                initiateOnlineWithdraw(withdraw);
            }
        } finally {
            redisCache.unlock(lockKey, uuid);
        }
    }

    @Override
    public void applyOnlineWithdrawResult(Integer id, com.tigshop.bean.dto.finance.profitsharing.WithdrawResult result) {
        if (result == null || result.getStatus() == null) {
            return;
        }
        String channelStatus = result.getStatus().toUpperCase(Locale.ROOT);
        boolean success = "SUCCESS".equals(channelStatus);
        boolean failed = Set.of("FAIL", "FAILED", "REFUND", "CLOSED").contains(channelStatus);
        String status = success || failed || Set.of("CREATE_SUCCESS", "PROCESSING").contains(channelStatus)
                ? channelStatus : "UNKNOWN";
        new TransactionTemplate(transactionManager).executeWithoutResult(tx -> {
            ShopWithdraw withdraw = this.getById(id);
            if (withdraw == null || !Integer.valueOf(2).equals(withdraw.getWithdrawType())) {
                return;
            }
            var update = Wrappers.<ShopWithdraw>lambdaUpdate()
                    .eq(ShopWithdraw::getShopWithdrawLogId, id)
                    .eq(ShopWithdraw::getWithdrawType, 2)
                    .eq(ShopWithdraw::getStatus, ShopWithdrawStatus.AUDIT_PROCESS.getCode())
                    .set(ShopWithdraw::getChannelStatus, status)
                    .set(StringUtils.isNotEmpty(result.getWithdrawId()), ShopWithdraw::getChannelWithdrawId, result.getWithdrawId());
            if (success || failed) {
                update.set(ShopWithdraw::getStatus, success ? ShopWithdrawStatus.AUDIT_PASS.getCode()
                        : ShopWithdrawStatus.AUDIT_NOT_PASS.getCode());
                if (withdraw.getOfflineFrozenAmount() == null) {
                    update.set(ShopWithdraw::getAuditRemark, "渠道已确认结果，历史线下冻结资金仍需人工核账");
                }
            }
            if (this.update(update) && success) {
                accountService.recordOnlineChange(withdraw.getShopId(), resolveRecordChannel(withdraw),
                        "withdraw:" + id, withdraw.getAmount().negate(), "线上提现成功", StringUtils.getCurrentTime());
                if (withdraw.getOfflineFrozenAmount() != null && withdraw.getOfflineFrozenAmount().signum() == 0) {
                    // 仅新线上单在渠道成功时生成出账统计，历史审核已写统计不可重复生成。
                    statementOutputService.saveStatementOutput(new StatementOutputSaveParam(withdraw, false));
                }
            }
        });
    }

    /**
     * 发起线上提现：从子商户可用余额提现到绑定账户，置为"正在处理"，由定时任务轮询最终状态。
     */
    private void initiateOnlineWithdraw(ShopWithdraw shopWithdraw) {
        // 三渠道独立体系：渠道由记录自带 pay_channel 驱动，不再依赖平台单选
        String channel = resolveRecordChannel(shopWithdraw);
        com.tigshop.bean.dto.finance.profitsharing.WithdrawRequest request =
                new com.tigshop.bean.dto.finance.profitsharing.WithdrawRequest();
        request.setAmount(shopWithdraw.getAmount());
        request.setOutRequestNo("SW" + shopWithdraw.getWithdrawSn());
        request.setRemark("店铺提现");
        // 收款方真实账号（支付宝转账出金需登录号/UID，取自提现账户数据；微信/汇付走子商户余额忽略此项）
        if (StringUtils.isNotEmpty(shopWithdraw.getAccountData())) {
            try {
                cn.hutool.json.JSONObject acc = cn.hutool.json.JSONUtil.parseObj(shopWithdraw.getAccountData());
                request.setPayeeAccount(acc.getStr("accountNo"));
                request.setPayeeName(acc.getStr("accountName"));
            } catch (Exception ignored) {
                // 微信/汇付从子商户账户出金，不依赖该回显数据；支付宝入口暂不可用。
            }
        }
        shopWithdraw.setPayChannel(channel);
        com.tigshop.bean.dto.finance.profitsharing.WithdrawResult withdrawResult = null;
        try {
            if (StringUtils.isEmpty(shopWithdraw.getChannelSubMchId())) {
                throw new GlobalException("缺少渠道子商户快照，提现结果待确认");
            }
            request.setSubMchId(shopWithdraw.getChannelSubMchId());
            withdrawResult = profitSharingClientRegistry.get(channel).withdraw(request);
            if (withdrawResult == null || StringUtils.isEmpty(withdrawResult.getStatus())) {
                throw new GlobalException("渠道未返回明确的提现受理状态");
            }
            applyOnlineWithdrawResult(shopWithdraw.getShopWithdrawLogId(), withdrawResult);
        } catch (Exception e) {
            // 超时/网络错误不代表渠道未出金，保留处理中等待查单，不能退账或重发。
            String knownWithdrawId = withdrawResult == null ? null : withdrawResult.getWithdrawId();
            this.update(Wrappers.<ShopWithdraw>lambdaUpdate()
                    .eq(ShopWithdraw::getShopWithdrawLogId, shopWithdraw.getShopWithdrawLogId())
                    .eq(ShopWithdraw::getStatus, ShopWithdrawStatus.AUDIT_PROCESS.getCode())
                    .set(ShopWithdraw::getChannelStatus, "UNKNOWN")
                    .set(StringUtils.isNotEmpty(knownWithdrawId), ShopWithdraw::getChannelWithdrawId, knownWithdrawId)
                    .set(ShopWithdraw::getAuditRemark, "线上提现结果待确认，请等待渠道查单，勿重复申请"));
        }
    }
}
