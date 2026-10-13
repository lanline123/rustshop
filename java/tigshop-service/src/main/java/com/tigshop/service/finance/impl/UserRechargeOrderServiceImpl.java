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
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.bo.PayPlatformStateBO;
import com.tigshop.bean.dto.finance.*;
import com.tigshop.bean.enums.common.IsShowType;
import com.tigshop.bean.enums.finance.UserRechargeOrderStatus;
import com.tigshop.bean.enums.finance.UserWithDrawApplyStatus;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.user.AuthorizeTypeEnum;
import com.tigshop.bean.model.user.UserAuthorize;
import com.tigshop.bean.feign.wechat.Code2SessionResult;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.model.finance.UserCashLog;
import com.tigshop.bean.model.finance.UserRechargeOrder;
import com.tigshop.bean.model.finance.UserWithdrawApply;
import com.tigshop.bean.model.promotion.RechargeSetting;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.vo.finance.ClientUserRechargeOrderVO;
import com.tigshop.bean.vo.finance.UserRechargeOrderPayVO;
import com.tigshop.bean.vo.finance.UserRechargeOrderUserVO;
import com.tigshop.bean.vo.finance.UserRechargeOrderVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.order.pay.PayCreateVO;
import com.tigshop.common.constant.finance.PayConstants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.*;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.feign.WechatApiClient;
import com.tigshop.mapper.finance.UserRechargeOrderMapper;
import com.tigshop.mapper.finance.UserWithdrawApplyMapper;
import com.tigshop.mapper.promotion.RechargeSettingMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.PaylogService;
import com.tigshop.service.finance.UserCashLogService;
import com.tigshop.service.finance.UserRechargeOrderService;
import com.tigshop.service.pay.NewPayChannelRegistry;
import com.tigshop.service.pay.PayService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.user.UserAuthorizeService;
import com.tigshop.service.user.UserBalanceService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.finance.UserRechargeOrderConstants.*;

/**
 * 充值记录服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@RequiredArgsConstructor
@Service
public class UserRechargeOrderServiceImpl extends BaseServiceImpl<UserRechargeOrderMapper, UserRechargeOrder> implements UserRechargeOrderService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserBalanceService userBalanceService;

    @Resource
    private UserWithdrawApplyMapper userWithdrawApplyMapper;

    @Resource
    private RechargeSettingMapper rechargeSettingMapper;

    @Resource
    private PaylogService paylogService;

    @Resource
    @Qualifier("WechatPayService")
    private PayService wechatPayService;

    @Resource
    @Qualifier("AliPayService")
    private PayService aliPayService;

    @Resource
    @Qualifier("CashPayService")
    private PayService clashPayService;

    @Resource
    private NewPayChannelRegistry newPayChannelRegistry;

    @Resource
    private com.tigshop.common.i18n.MessageHelper messageHelper;

    @Resource
    private UserCashLogService userCashLogService;

    private final ConfigService configService;

    private final WechatApiClient wechatApiClient;

    @Resource
    private UserAuthorizeService userAuthorizeService;


    @Override
    public Page<UserRechargeOrderVO> list(UserRechargeOrderListDTO listDTO) {
        // 分页
        Page<UserRechargeOrder> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<UserRechargeOrder> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        Integer status = listDTO.getStatus();
        if (status != null) {
            queryWrapper.eq(UserRechargeOrder::getStatus, status);
        }
        // 搜索关键字：按会员名称（用户名/昵称）匹配
        String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            List<Integer> matchedUserIds = userMapper.selectList(new LambdaQueryWrapper<User>()
                            .select(User::getUserId)
                            .and(w -> w.like(User::getUsername, keyword)
                                    .or()
                                    .like(User::getNickname, keyword)))
                    .stream()
                    .map(User::getUserId)
                    .toList();
            if (CollUtil.isEmpty(matchedUserIds)) {
                return PageUtil.convertPage(new Page<>(listDTO.getPage(), listDTO.getSize()), List.of());
            }
            queryWrapper.in(UserRechargeOrder::getUserId, matchedUserIds);
        }
        // 执行查询
        Page<UserRechargeOrder> userRechargeOrderPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<UserRechargeOrder> userRechargeOrderPageRecords = userRechargeOrderPage.getRecords();

        if (CollUtil.isEmpty(userRechargeOrderPageRecords)) {
            return PageUtil.convertPage(userRechargeOrderPage, List.of());
        }

        // 获取所有 ID
        List<Integer> userIds = userRechargeOrderPageRecords.stream()
                                                            .map(UserRechargeOrder::getUserId)
                                                            .distinct()
                                                            .toList();

        // 调用方法获取会员信息
        List<User> users = userMapper.selectBatchIds(userIds);

        // 将分类信息存入 Map，方便后续查找
        Map<Integer, User> userMap = users.stream()
                                          .collect(Collectors.toMap(User::getUserId, Function.identity()));

        // 转换为VO
        List<UserRechargeOrderVO> userRechargeOrderVOList = userRechargeOrderPageRecords.stream()
                                                                                        .map(userRechargeOrder -> {
                                                                                            UserRechargeOrderVO userRechargeOrderVO = new UserRechargeOrderVO();
                                                                                            BeanUtils.copyProperties(userRechargeOrder, userRechargeOrderVO);

                                                                                            User user = userMap.get(userRechargeOrder.getUserId());
                                                                                            if (user != null) {
                                                                                                UserRechargeOrderUserVO userVO = new UserRechargeOrderUserVO();
                                                                                                BeanUtils.copyProperties(user, userVO);
                                                                                                userRechargeOrderVO.setUser(userVO);
                                                                                            }

                                                                                            DateTime date = DateUtil.date(userRechargeOrder.getAddTime() * 1000);
                                                                                            userRechargeOrderVO.setAddTime(DateUtil.format(date, "yyyy-MM-dd HH:mm:ss"));

                                                                                            userRechargeOrderVO.setStatusType(STATUS_TYPE.get(userRechargeOrder.getStatus())); // 将分类名称设置到对象中

                                                                                            return userRechargeOrderVO;
                                                                                        })
                                                                                        .toList();
        return PageUtil.convertPage(userRechargeOrderPage, userRechargeOrderVOList);
    }

    @Override
    public UserRechargeOrderVO detail(Integer id) {
        if (id != null) {
            UserRechargeOrder userRechargeOrder = this.getById(id);
            UserRechargeOrderVO userRechargeOrderVO = new UserRechargeOrderVO();
            BeanUtils.copyProperties(userRechargeOrder, userRechargeOrderVO);
            return userRechargeOrderVO;
        }
        return new UserRechargeOrderVO();
    }

    @Override
    public boolean create(UserRechargeOrderCreateDTO createDTO) {
        if (createDTO != null) {
            UserRechargeOrder userRechargeOrder = new UserRechargeOrder();
            BeanUtils.copyProperties(createDTO, userRechargeOrder);
            //假如userRechargeOrder.getAmount() <=0
            if (createDTO.getAmount()
                         .compareTo(BigDecimal.ZERO) <= 0) {
                throw new GlobalException(USER_RECHARGE_AMOUNT_ERROR);
            }
            userRechargeOrder.setAddTime(DateUtil.currentSeconds());

            return this.save(userRechargeOrder);
        }
        return false;
    }

    @Override
    public boolean update(UserRechargeOrderUpdateDTO updateDTO) {
        if (updateDTO != null) {
            UserRechargeOrder userRechargeOrder = new UserRechargeOrder();
            BeanUtils.copyProperties(updateDTO, userRechargeOrder);

            //如果修改之前的旧 status状态不为0，抛出错误
            if (updateDTO.getStatus() != 0 && this.getById(updateDTO.getOrderId())
                                                  .getStatus() != 0) {
                throw new GlobalException(USER_RECHARGE_STATUS_ERROR);
            }
            if (updateDTO.getStatus() == 1) {
                BigDecimal amount = userRechargeOrder.getAmount();
                if (userRechargeOrder.getDiscountMoney() != null && userRechargeOrder.getDiscountMoney()
                                                                                     .compareTo(BigDecimal.ZERO) > 0) {
                    // 添加赠送金额
                    amount = amount.add(userRechargeOrder.getDiscountMoney());
                }
                if (!userBalanceService.incBalance(amount, userRechargeOrder.getUserId(), "充值订单审核通过增加余额")) {
                    return false;
                }
            }

            return this.updateById(userRechargeOrder);
        }
        return false;
    }

    @Override
    public BigDecimal getUserRechargeSurplusTotal(String startTime, String endTime) {
        LambdaQueryWrapper<UserRechargeOrder> queryWrapper = new LambdaQueryWrapper<>();

        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
            Date startTimeDate = DateUtil.parse(startTime);
            Date endTimeDate = DateUtil.parse(endTime);
            // 将时间设置为当天的结束时间
            endTimeDate = DateUtil.endOfDay(endTimeDate);

            // 添加时间范围的条件，假设 changeTime 是 Unix 时间戳
            queryWrapper.ge(UserRechargeOrder::getAddTime, startTimeDate.getTime() / 1000)
                        .le(UserRechargeOrder::getAddTime, endTimeDate.getTime() / 1000);
        }

        // 查询所有符合条件的余额变动
        queryWrapper.select(UserRechargeOrder::getAmount)
                    .eq(UserRechargeOrder::getStatus, 1);
        List<UserRechargeOrder> userRechargeOrder = this.list(queryWrapper);

        // 计算余额变动总和
        return userRechargeOrder.stream()
                                .map(UserRechargeOrder::getAmount)
                                .reduce(BigDecimal::add)
                                .orElse(BigDecimal.ZERO);
    }

    @Override
    public Page<ClientUserRechargeOrderVO> clientList(UserRechargeOrderListDTO listDTO) {
        // 用户提现申请
        LambdaQueryWrapper<UserWithdrawApply> queryWrapper = new LambdaQueryWrapper<>();
        // 充值记录
        LambdaQueryWrapper<UserRechargeOrder> queryWrapperOrder = new LambdaQueryWrapper<>();
        if (listDTO.getUserId() != null && listDTO.getUserId() > 0) {
            queryWrapper.eq(UserWithdrawApply::getUserId, listDTO.getUserId());
            queryWrapperOrder.eq(UserRechargeOrder::getUserId, listDTO.getUserId());
        }
        // D1107：充值/申请记录属于「会员余额」钱包，只统计余额提现（wallet_type=0），不含分销佣金提现（bug 67e2d2c6）
        queryWrapper.eq(UserWithdrawApply::getWalletType, 0);
        if (listDTO.getStatus() != null && listDTO.getStatus() != -1) {
            queryWrapper.eq(UserWithdrawApply::getStatus, listDTO.getStatus());
            queryWrapperOrder.eq(UserRechargeOrder::getStatus, listDTO.getStatus());
        }
        // 查询用户提现申请列表
        List<UserWithdrawApply> userWithdrawApplyList = userWithdrawApplyMapper.selectList(queryWrapper);
        int userWithdrawApplySize = userWithdrawApplyList == null ? 0 : userWithdrawApplyList.size();
        // 查询充值记录列表
        List<UserRechargeOrder> userRechargeOrderList = this.list(queryWrapperOrder);
        int userRechargeOrderSize = userRechargeOrderList == null ? 0 : userRechargeOrderList.size();
        long total = userWithdrawApplySize + userRechargeOrderSize;

        List<ClientUserRechargeOrderVO> clientUserRechargeOrderApplyVOList = new ArrayList<>();
        List<ClientUserRechargeOrderVO> clientUserRechargeOrderVOList = new ArrayList<>();

        if (userWithdrawApplyList != null && !userWithdrawApplyList.isEmpty()) {
            clientUserRechargeOrderApplyVOList = userWithdrawApplyList.stream()
                                                                      .map(userWithdrawApply -> {
                                                                          ClientUserRechargeOrderVO clientUserRechargeOrderVO = new ClientUserRechargeOrderVO();
                                                                          BeanUtils.copyProperties(userWithdrawApply, clientUserRechargeOrderVO);
                                                                          clientUserRechargeOrderVO.setStatusType(resolveWithdrawStatusName(userWithdrawApply.getStatus()));
                                                                          clientUserRechargeOrderVO.setAddTime(TigUtils.handelTime(userWithdrawApply.getAddTime()));
                                                                          clientUserRechargeOrderVO.setType(messageHelper.get("recharge.type.withdraw"));
                                                                          clientUserRechargeOrderVO.setAddTimeLong(userWithdrawApply.getAddTime());
                                                                          return clientUserRechargeOrderVO;
                                                                      })
                                                                      .toList();
        }

        if (userRechargeOrderList != null && !userRechargeOrderList.isEmpty()) {
            clientUserRechargeOrderVOList = userRechargeOrderList.stream()
                                                                 .map(userRechargeOrder -> {
                                                                     ClientUserRechargeOrderVO clientUserRechargeOrderVO = new ClientUserRechargeOrderVO();
                                                                     BeanUtils.copyProperties(userRechargeOrder, clientUserRechargeOrderVO);
                                                                     clientUserRechargeOrderVO.setStatusType(resolveRechargeStatusName(userRechargeOrder.getStatus()));
                                                                     clientUserRechargeOrderVO.setAddTime(TigUtils.handelTime(userRechargeOrder.getAddTime()));
                                                                     clientUserRechargeOrderVO.setType(messageHelper.get("recharge.type.recharge"));
                                                                     clientUserRechargeOrderVO.setAddTimeLong(userRechargeOrder.getAddTime());
                                                                     return clientUserRechargeOrderVO;
                                                                 })
                                                                 .toList();
        }
        //将 clientUserRechargeOrderApplyVOList 和 clientUserRechargeOrderVOList 合并成一个 List
        List<ClientUserRechargeOrderVO> clientUserRechargeOrderVoListAll = new ArrayList<>();
        clientUserRechargeOrderVoListAll.addAll(clientUserRechargeOrderApplyVOList);
        clientUserRechargeOrderVoListAll.addAll(clientUserRechargeOrderVOList);

        //将 clientUserRechargeOrderVOListAll 按照 addTimeLong 倒序排序
        clientUserRechargeOrderVoListAll.sort(Comparator.comparing(ClientUserRechargeOrderVO::getAddTimeLong)
                                                        .reversed());

        //根据 listDTO.getPage() 和 listDTO.getSize() 分页
        List<ClientUserRechargeOrderVO> clientUserRechargeOrderVoList = clientUserRechargeOrderVoListAll.stream()
                                                                                                        .skip((long) (listDTO.getPage() - 1) * listDTO.getSize())
                                                                                                        .limit(listDTO.getSize())
                                                                                                        .toList();

        Page<ClientUserRechargeOrderVO> page = new Page<>(listDTO.getPage(), listDTO.getSize(), total);
        page.setRecords(clientUserRechargeOrderVoList);
        return page;
    }

    /**
     * 买家端提现申请状态文案：按请求语言解析，未知状态沿用枚举原有校验（抛数据类型异常）。
     */
    private String resolveWithdrawStatusName(Integer status) {
        // 沿用枚举对非法状态码的校验，再按语言解析文案
        UserWithDrawApplyStatus.getDescription(status);
        return switch (status) {
            case 0 -> messageHelper.get("withdraw.status.wait");
            case 1 -> messageHelper.get("withdraw.status.finished");
            default -> messageHelper.get("withdraw.status.reject");
        };
    }

    /**
     * 买家端充值记录状态文案：按请求语言解析，未知状态沿用枚举原有校验（抛数据类型异常）。
     */
    private String resolveRechargeStatusName(Integer status) {
        // 沿用枚举对非法状态码的校验，再按语言解析文案
        UserRechargeOrderStatus.getDescription(status);
        return switch (status) {
            case 0 -> messageHelper.get("recharge.status.wait");
            case 1 -> messageHelper.get("recharge.status.success");
            default -> messageHelper.get("recharge.status.fail");
        };
    }

    @Override
    public Integer rechargeOperation(ClientUserRechargeOrderUpdateDTO updateDTO) {
        BigDecimal amount;
        BigDecimal discountMoney;

        if (updateDTO.getAmount() != null) {
            LambdaQueryWrapper<RechargeSetting> queryWrapper = new LambdaQueryWrapper<>();
            //'money', '<=', $amount
            queryWrapper.eq(RechargeSetting::getMoney, updateDTO.getAmount());
            queryWrapper.orderByDesc(RechargeSetting::getMoney);
            queryWrapper.last("limit 1");
            RechargeSetting rechargeSetting = rechargeSettingMapper.selectOne(queryWrapper);
            discountMoney = rechargeSetting != null ? rechargeSetting.getDiscountMoney() : BigDecimal.ZERO;
            amount = updateDTO.getAmount();
        } else {
            LambdaQueryWrapper<RechargeSetting> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RechargeSetting::getIsShow, 1);
            queryWrapper.eq(RechargeSetting::getRechargeId, updateDTO.getId());
            RechargeSetting rechargeSetting = rechargeSettingMapper.selectOne(queryWrapper);
            if (rechargeSetting == null) {
                throw new GlobalException(messageHelper.get("error.recharge.amount.invalid"));
            }
            amount = rechargeSetting.getMoney();
            discountMoney = rechargeSetting.getDiscountMoney();
        }

        UserRechargeOrder userRechargeOrder = new UserRechargeOrder();
        userRechargeOrder.setUserId(updateDTO.getUserId());
        userRechargeOrder.setAmount(amount);
        userRechargeOrder.setDiscountMoney(discountMoney);
        userRechargeOrder.setAddTime(StringUtils.getCurrentTime());
        userRechargeOrder.setPostscript("会员充值");
        this.save(userRechargeOrder);

        return userRechargeOrder.getOrderId();
    }

    @Override
    public UserRechargeOrderPayVO pay(ClientUserRechargeOrderPayDTO payDTO) {
        return pay(payDTO, null);
    }

    @Override
    public UserRechargeOrderPayVO pay(ClientUserRechargeOrderPayDTO payDTO, Integer userId) {
        if (payDTO == null || payDTO.getOrderId() == null || (userId != null && userId <= 0)) {
            throw new GlobalException(messageHelper.get("error.param.missing"));
        }
        UserRechargeOrder userRechargeOrder = userId == null
                ? this.getById(payDTO.getOrderId())
                : baseMapper.selectOne(new LambdaQueryWrapper<UserRechargeOrder>()
                        .eq(UserRechargeOrder::getOrderId, payDTO.getOrderId())
                        .eq(UserRechargeOrder::getUserId, userId));
        if (userRechargeOrder == null) {
            throw new GlobalException(messageHelper.get("error.recharge.apply.not.exist"));
        }
        if (userRechargeOrder.getStatus() == 1) {
            throw new GlobalException(messageHelper.get("error.recharge.apply.paid"));
        }
        UserRechargeOrderPayVO userRechargeOrderPayVO = new UserRechargeOrderPayVO();
        BeanUtils.copyProperties(userRechargeOrder, userRechargeOrderPayVO);
        userRechargeOrderPayVO.setStatusType(UserRechargeOrderStatus.getDescription(userRechargeOrder.getStatus()));
        userRechargeOrderPayVO.setAddTime(TigUtils.handelTime(userRechargeOrder.getAddTime()));
        userRechargeOrderPayVO.setPaidTime(TigUtils.handelTime(userRechargeOrder.getPaidTime()));
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserId, userRechargeOrder.getUserId());
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new GlobalException(messageHelper.get("error.user.not.exist"));
        }
        UserRechargeOrderPayVO.UserVO userVO = new UserRechargeOrderPayVO.UserVO();
        BeanUtils.copyProperties(user, userVO);
        userRechargeOrderPayVO.setUser(userVO);
        return userRechargeOrderPayVO;
    }

    /**
     * 检查订单状态
     *
     * @param id 主键
     * @return
     */
    @Override
    public Integer checkStatus(Integer id, Integer userId) {
        if (id == null || (userId != null && userId <= 0)) {
            throw new GlobalException(messageHelper.get("error.param.missing"));
        }
        UserRechargeOrder userRechargeOrder = userId == null
                ? this.getById(id)
                : this.lambdaQuery()
                        .eq(UserRechargeOrder::getOrderId, id)
                        .eq(UserRechargeOrder::getUserId, userId)
                        .one();
        if (userRechargeOrder == null) {
            throw new GlobalException(messageHelper.get("error.recharge.apply.not.exist"));
        }

        Integer status = userRechargeOrder.getStatus();
        if (status > 0) {
            //支付成功或者支付失败
            return status;
        }
        // 充值单：orderType=1
        Paylog paylog = paylogService.getLastByOrderId(id, 1);
        if (paylog == null) {
            return status;
        }
        String payCode = paylog.getPayCode();
        if (!"wechat".equals(payCode)) {
            return status;
        }
        //微信特殊处理 (付款码支付的时候需要前端轮训处理支付结果)
        String posPayType = HeaderUtils.getPosPayType();
        if (PayConstants.CODE_PAY_CODE.equals(posPayType)) {
            //查询状态,如果状态是支付成功的话,调用支付的paySuccess
            PayPlatformStateBO payPlatformStateBO = wechatPayService.payPlatformStatus(paylog.getPaySn(), paylog.getTransactionId(), posPayType);
            if (Objects.isNull(payPlatformStateBO)) {
                return status;
            }
            //可查看微信支付状态转移图 https://pay.weixin.qq.com/doc/v2/merchant/4011937125
            String tradeState = payPlatformStateBO.getTradeState();
            if (PayConstants.WX_PAY_SUCCESS_CODE.equals(tradeState)) {
                wechatPayService.paySuccess(payPlatformStateBO.getOutTradeNo(), payPlatformStateBO.getTradeNo());
                return status;
            }
        }
        return status;
    }

    @Override
    public Integer checkStatus(Integer id) {
        return checkStatus(id, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayCreateVO createPay(UserRechargeOrderPayDTO payDTO, Integer userId) {
        if (payDTO.getType() == null || StringUtils.isEmpty(payDTO.getType())) {
            throw new GlobalException(messageHelper.get("error.pay.method.not.selected"));
        }
        String openid = null;
        if (StringUtils.isNotBlank(payDTO.getCode())) {
            // 获取微信配置
            String appId = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_APP_ID.getBizCode())
                                        .getBizVal();
            String appSecret = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_SECRET.getBizCode())
                                            .getBizVal();
            String result = wechatApiClient.code2Session(appId, appSecret, payDTO.getCode(), "authorization_code");
            Code2SessionResult code2SessionResult = JsonUtil.fromJson(result, Code2SessionResult.class);
            // 错误code -1,40029，45011,40226
            if (code2SessionResult.getErrcode() != null && code2SessionResult.getErrcode() != 0) {
                throw new GlobalException(code2SessionResult.getErrmsg());
            }

            openid = code2SessionResult.getOpenid();
        }

        String clientType = HeaderUtils.getClientType();
        // 微信钱包类支付码（原生微信 + 汇付/拉卡拉/易宝的微信子码）在微信内/小程序均需公众号 openid
        boolean needWechatOpenid = "wechat".equals(payDTO.getType())
                || "huifuWechat".equals(payDTO.getType())
                || "lakalaWechat".equals(payDTO.getType())
                || "yeepayWechat".equals(payDTO.getType());
        // 微信内支付 - 需要获取公众号的openid (authorizeType=1)
        if (ObjectUtil.equals("wechat", clientType) && needWechatOpenid) {
            UserAuthorize userAuthorize = userAuthorizeService.getOne(new LambdaQueryWrapper<UserAuthorize>()
                    .eq(UserAuthorize::getUserId, SecurityUtils.getCurrentUserId())
                    .eq(UserAuthorize::getAuthorizeType, AuthorizeTypeEnum.WECHAT.getCode()));
            if (userAuthorize != null) {
                openid = userAuthorize.getOpenId();
            }
            if (StringUtils.isEmpty(openid)) {
                throw new GlobalException(messageHelper.get("error.recharge.wechat.auth.required"));
            }
        }

        // 小程序支付场景校验openid
        if (ObjectUtil.equals("miniProgram", clientType) && needWechatOpenid && StringUtils.isEmpty(openid)) {
            throw new GlobalException(messageHelper.get("error.recharge.userinfo.failed"));
        }

        UserRechargeOrder userRechargeOrder = this.getById(payDTO.getId());
        if (Objects.isNull(userRechargeOrder)) {
            throw new GlobalException(messageHelper.get("error.recharge.apply.required"));
        }
        if (userRechargeOrder.getStatus() == 1) {
            throw new GlobalException(messageHelper.get("error.order.already.paid"));
        }

        OrderVO orderVO = new OrderVO();
        orderVO.setOrderType(1);
        orderVO.setAuthCode(payDTO.getAuthCode());
        orderVO.setTotalAmount(userRechargeOrder.getAmount());
        orderVO.setUnpaidAmount(userRechargeOrder.getAmount());
        orderVO.setOrderId(userRechargeOrder.getOrderId());
        Paylog paylog = paylogService.creatPayLog(null, orderVO, payDTO.getType(), 1);
        orderVO.setPaySn(paylog.getPaySn());
        // 由于需要签名校验参数所以构建签名的时候这个也必传
        orderVO.setOrderSn(paylog.getPaySn());
        PayCreateVO.PayInfo payResult = new PayCreateVO.PayInfo();

        String payType = null; // 扫码支付,扫脸支付等等
        String adminType = HeaderUtils.getAdminType();
        if ("pos".equals(adminType)) {
            //如果是cash的话,特殊处理(找零金额进行保存到记录表里面,用于后期对账)
            if (PayConstants.CASH_PAY_CODE.equals(payDTO.getType())) {
                //判断金额是否一致
                BigDecimal amount = userRechargeOrder.getAmount();
                if (payDTO.getCashAmount() == null) {
                    throw new GlobalException(messageHelper.get("error.pay.cash.amount.required"));
                }
                BigDecimal cashChangeAmount = payDTO.getCashChangeAmount();
                if (Objects.isNull(cashChangeAmount)) {
                    cashChangeAmount = BigDecimal.ZERO;
                    payDTO.setCashChangeAmount(cashChangeAmount);
                }
                if (payDTO.getCashAmount()
                          .compareTo(amount.add(payDTO.getCashChangeAmount())) != 0) {
                    throw new GlobalException(messageHelper.get("error.pay.cash.change.invalid"));
                }
                //存在的话,就进行更新,不存在就进行插入
                UserCashLog userCashLog = new UserCashLog();
                userCashLog.setOrderId(userRechargeOrder.getOrderId());
                userCashLog.setOrderType(1);
                userCashLog.setCashAmount(payDTO.getCashAmount());
                userCashLog.setCashChangeAmount(payDTO.getCashChangeAmount());
                userCashLogService.lambdaUpdate()
                                  .eq(UserCashLog::getOrderType, 1)
                                  .eq(UserCashLog::getOrderId, userRechargeOrder.getOrderId())
                                  .remove();
                userCashLogService.save(userCashLog);
            } else {
                //可能出现付款码支付和扫脸支付和碰一碰支付
                payType = HeaderUtils.getPosPayType();
            }
        } else {
            payType = clientType;
        }

        if ("wechat".equals(payDTO.getType())) {
            // 充值恒走直连：资金进平台商户用于余额入账，没有分账对象，不做收付通形态判定
            payResult = wechatPayService.pay(orderVO, openid, payType);
        }
        if ("alipay".equals(payDTO.getType())) {
            payResult = aliPayService.pay(orderVO, openid, payType);
        }
        if (PayConstants.CASH_PAY_CODE.equals(payDTO.getType())) {
            payResult = clashPayService.pay(orderVO, openid, payType);
            clashPayService.paySuccess(paylog.getPaySn(), null);
        }
        // 新增渠道（汇付/拉卡拉）走局部工厂分发；ApplePay 充值属二期，排除之。
        // 支付成功后统一由 PayBaseServiceImpl.paySuccess 按 orderType==1 回填充值余额。
        PayService rechargeNewChannel = newPayChannelRegistry.get(payDTO.getType());
        if (rechargeNewChannel != null && !"applepay".equals(payDTO.getType())) {
            payResult = rechargeNewChannel.pay(orderVO, openid, payType);
        }
        PayCreateVO payCreateVO = new PayCreateVO();
        payCreateVO.setPayInfo(payResult);
        payCreateVO.setOrderId(userRechargeOrder.getOrderId());
        // 支付宝支付这个必传
        payCreateVO.setOrderSn(orderVO.getOrderSn());
        payCreateVO.setOrderAmount(userRechargeOrder.getAmount());
        return payCreateVO;
    }
}
