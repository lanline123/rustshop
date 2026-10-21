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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.login.*;
import com.tigshop.bean.dto.product.ProductListResDTO;
import com.tigshop.bean.dto.user.*;
import com.tigshop.bean.enums.order.CommentStatus;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.user.*;
import com.tigshop.bean.feign.wechat.Code2SessionResult;
import com.tigshop.bean.feign.wechat.GetTokenResult;
import com.tigshop.bean.enums.finance.SettlementTypeEnum;
import com.tigshop.bean.model.finance.CreditUser;
import com.tigshop.bean.model.finance.UserBalanceLog;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.promotion.Coupon;
import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.model.salesman.SalesmanCustomer;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.user.*;
import com.tigshop.bean.param.login.BindWechatParam;
import com.tigshop.bean.param.login.CheckEmailParam;
import com.tigshop.bean.param.login.RegisterParam;
import com.tigshop.bean.param.salesman.SalesmanSaveParam;
import com.tigshop.bean.param.user.*;
import com.tigshop.bean.query.user.UserFundDetailPageQuery;
import com.tigshop.bean.vo.config.GalleryPicUploadVO;
import com.tigshop.bean.vo.finance.UserBalanceLogVO;
import com.tigshop.bean.vo.login.LoginWechatEventVO;
import com.tigshop.bean.vo.salesman.SalesmanConfigVO;
import com.tigshop.bean.vo.user.*;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.*;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.feign.WechatApiClient;
import com.tigshop.service.oauth.WechatOAuthService;
import com.tigshop.mapper.finance.UserBalanceLogMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.salesman.SalesmanCustomerMapper;
import com.tigshop.mapper.salesman.SalesmanMapper;
import com.tigshop.mapper.user.UserCouponMapper;
import com.tigshop.mapper.user.UserGrowthPointsLogMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.mapper.user.UserMemberTagMapper;
import com.tigshop.mapper.user.UserPointsLogMapper;
import com.tigshop.security.common.utils.JwtUtil;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.CreditUserService;
import com.tigshop.service.finance.UserBalanceLogService;
import com.tigshop.service.wholesale.WholesalerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.salesman.SalesmanConfigService;
import com.tigshop.service.salesman.SalesmanService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.GalleryPicService;
import com.tigshop.service.shop.AdminUserShopService;
import com.tigshop.service.shop.CollectShopService;
import com.tigshop.service.user.*;
import io.jsonwebtoken.lang.Collections;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.user.UserConstants.*;
import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 软件许可服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
@RequiredArgsConstructor
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    private static final SecureRandom PASSWORD_RESET_RANDOM = new SecureRandom();
    private static final String PASSWORD_RESET_PREFIX = "password-reset:";
    private static final DefaultRedisScript<Long> CONSUME_VERIFICATION = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtil jwtUtil;
    private final TigshopProperties tigshopProperties;
    private final RedisCache redisCache;
    private final RabbitTemplate rabbitTemplate;

    private final UserRankService userRankService;
    private final UserAddressService userAddressService;
    private final ConfigService configService;
    private final UserPointsLogMapper userPointsLogMapper;
    private final UserBalanceLogMapper userBalanceLogMapper;
    private final UserRankConfigService userRankConfigService;
    private final UserRankLogService userRankLogService;
    private final RankGrowthLogService rankGrowthLogService;
    private final UserCouponMapper userCouponMapper;
    private final UserAuthorizeService userAuthorizeService;
    private final SalesmanMapper salesmanMapper;
    private final SalesmanCustomerMapper salesmanCustomerMapper;
    private final AdminUserShopService adminUserShopService;
    private final OrderMapper orderMapper;
    private final ObjectProvider<OrderService> orderService;
    private final ProductService productService;
    private final GalleryPicService galleryPicService;
    private final CollectShopService collectShopService;
    private final UserBalanceLogService userBalanceLogService;
    private final UserGrowthPointsLogMapper userGrowthPointsLogMapper;
    private final UserMemberTagMapper userMemberTagMapper;
    private final WechatApiClient wechatApiClient;
    private final WechatOAuthService wechatOAuthService;
    private final com.tigshop.service.security.WxImgSecCheckHelper wxImgSecCheckHelper;
    private final TranslatePackageImpl translatePackage;
    private final com.tigshop.common.i18n.MessageHelper messageHelper;
    private final WxMpService wxMpService;

    private final com.tigshop.service.setting.wechat.WechatAutoReplyService wechatAutoReplyService;

    private final com.tigshop.service.setting.wechat.WxKefuSender wxKefuSender;
    private final WxMpMessageRouter wxMpMessageRouter;
    private final SalesmanService salesmanService;
    private final SalesmanConfigService salesmanConfigService;
    private final UserBalanceService userBalanceService;
    private final CreditUserService creditUserService;

    // 不用 final + Lombok 构造器注入：与 WholesalerServiceImpl(final UserService) 形成 user↔wholesaler 二段构造器循环。
    // 改用 setter + @Lazy 参数注入：Spring 注入 lazy proxy 解循环。
    // Mockito @InjectMocks 在 Lombok @RequiredArgsConstructor 场景下不会自动 fallback 到 setter 注入，
    // 需测试侧在 @BeforeEach 手动调 setWholesalerService(wholesalerService) 保持 mock 注入。
    private WholesalerService wholesalerService;

    @Autowired
    public void setWholesalerService(@Lazy WholesalerService wholesalerService) {
        this.wholesalerService = wholesalerService;
    }

    @Override
    public Page<UserDTO> list(UserListDTO dto) {
        Page<User> page = buildSortOrder(dto);

        // 按标签筛选：先查出拥有指定标签的用户ID集合
        List<Long> tagFilterUserIds = null;
        if (CollUtil.isNotEmpty(dto.getTagIds())) {
            List<UserMemberTag> matchedRecords = userMemberTagMapper.selectList(
                    new LambdaQueryWrapper<UserMemberTag>()
                            .in(UserMemberTag::getTagId, dto.getTagIds())
                            .select(UserMemberTag::getUserId, UserMemberTag::getTagId));

            if (Integer.valueOf(1).equals(dto.getTagMatchMode())) {
                // AND 模式：用户的标签集合必须包含传入的所有 tagIds（可以有额外标签）
                Set<Long> requiredTagIds = new java.util.HashSet<>(dto.getTagIds());
                tagFilterUserIds = matchedRecords.stream()
                        .collect(Collectors.groupingBy(UserMemberTag::getUserId,
                                Collectors.mapping(UserMemberTag::getTagId, Collectors.toSet())))
                        .entrySet().stream()
                        .filter(e -> e.getValue().containsAll(requiredTagIds))
                        .map(Map.Entry::getKey)
                        .toList();
            } else {
                // OR 模式（默认）：匹配任意一个即可
                tagFilterUserIds = matchedRecords.stream()
                        .map(UserMemberTag::getUserId).distinct().toList();
            }

            if (tagFilterUserIds.isEmpty()) {
                Page<UserDTO> emptyPage = new Page<>();
                BeanUtils.copyProperties(page, emptyPage);
                emptyPage.setRecords(Collections.emptyList());
                return emptyPage;
            }
        }

        final List<Long> finalTagFilterUserIds = tagFilterUserIds;
        Integer userType = dto.getUserType() == null
                ? UserTypeEnum.MEMBER.getCode() : dto.getUserType();
        Page<User> userPage = this.lambdaQuery()
                .and(StrUtil.isNotBlank(dto.getKeyword()),
                        wrapper -> {
                            if (Boolean.TRUE.equals(dto.getIsExact())) {
                                // 精确匹配
                                wrapper.eq(User::getUsername, dto.getKeyword())
                                        .or()
                                        .eq(User::getEmail, dto.getKeyword())
                                        .or()
                                        .eq(User::getMobile, dto.getKeyword());
                            } else {
                                // 模糊匹配
                                wrapper.like(User::getUsername, dto.getKeyword())
                                        .or()
                                        .like(User::getEmail, dto.getKeyword())
                                        .or()
                                        .like(User::getMobile, dto.getKeyword());
                            }
                        }
                )
                .in(finalTagFilterUserIds != null, User::getUserId, finalTagFilterUserIds)
                .eq(User::getUserType, userType)
                .eq(dto.getFromTag() != null && dto.getFromTag() > 0, User::getFromTag, dto.getFromTag())
                .eq(dto.getRankId() != null && dto.getRankId() > 0, User::getRankId, dto.getRankId())
                .gt(dto.getPointsGt() != null && dto.getPointsGt().compareTo(BigDecimal.ZERO) > 0, User::getPoints, dto.getPointsGt())
                .lt(dto.getPointsLt() != null && dto.getPointsLt().compareTo(BigDecimal.ZERO) > 0, User::getPoints, dto.getPointsLt())
                .gt(dto.getBalance() != null, User::getBalance, dto.getBalance())
                .eq(dto.getMark() != null, User::getMark, dto.getMark())
                .page(page);
        List<User> users = userPage.getRecords();
        List<UserDTO> userdtoList = users.stream().map(this::convertToDTO).toList();
        return PageUtil.convertPage(userPage, userdtoList);
    }

    public UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        //如果用户头像是空的就用默认头像
        if (StringUtils.isEmpty(user.getAvatar())) {
            //从config里获取默认头像
            userDTO.setAvatar(getUserAvatar(user.getAvatar()));
        }
        userDTO.setRegTime(TigUtils.handelTime(user.getRegTime()));
        userDTO.setFromTagName(FromTag.getFromTagName(user.getFromTag()));
        UserRank userRank = userRankService.getById(user.getRankId());
        if (userRank != null) {
            userDTO.setRankIco(userRank.getRankIco());
            userDTO.setRankLogo(userRank.getRankLogo());
            userDTO.setRankName(userRank.getRankName());
        }
        userDTO.setPassword("");

        UserAddress userAddress = userAddressService.lambdaQuery()
                .eq(UserAddress::getUserId, user.getUserId())
                .orderByDesc(UserAddress::getIsDefault)
                .last("limit 1")
                .one();
        if (userAddress != null) {
            UserAddressVO detail = userAddressService.detail(userAddress.getAddressId());
            userDTO.setUserAddress(detail);
        }

        List<Order> orderList = orderService.getIfAvailable().lambdaQuery()
                .eq(Order::getUserId, user.getUserId())
                .eq(Order::getPayStatus, 2)
                .list();
        if (CollUtil.isNotEmpty(orderList)) {
            userDTO.setOrderCount(orderList.size());
            String orderAmount = orderList.stream()
                    .map(Order::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .toString();
            userDTO.setOrderAmount(orderAmount);
        }

        return userDTO;
    }

    @Override
    public UserDTO detail(Integer id) {
        if (id == null) {
            throw new GlobalException(messageHelper.get("error.service"));
        }
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getUserId, id));
        return this.convertToDTO(user);
    }

    @Override
    public UserItemVO getUserAndUserRank(Integer id) {
        UserDTO detail = detail(id);
        return new UserItemVO(detail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batch(BatchDTO batchDTO) {
        UserBatchParam param = (UserBatchParam) batchDTO;
        if ("setRank".equals(param.getType())) {
            List<Integer> ids = param.getIds();
            return this.lambdaUpdate()
                    .set(User::getRankId, param.getRankId())
                    .in(User::getUserId, ids)
                    .update();
        }
        if ("del".equals(batchDTO.getType())) {
            return this.removeByIds(batchDTO.getIds());
        }


        if ("incrPoints".equals(batchDTO.getType())) {
            //增加积分
            List<Integer> ids = param.getIds();
            if (Objects.isNull(ids) || ids.isEmpty()) {
                return true;
            }
            if (Objects.isNull(param.getValue())) {
                throw new GlobalException(messageHelper.get("error.param.invalid"));
            }
            for (Integer id : ids) {
                incPoints(param.getValue(), id, "后台增加积分");
            }
            return true;
        }

        if ("incrBalance".equals(batchDTO.getType())) {
            //增加余额
            List<Integer> ids = param.getIds();
            if (Objects.isNull(ids) || ids.isEmpty()) {
                return true;
            }
            if (Objects.isNull(param.getBalance())) {
                throw new GlobalException(messageHelper.get("error.param.invalid"));
            }
            for (Integer id : ids) {
                userBalanceService.incBalance(param.getBalance(), id, "后台增加余额");
            }
            return true;
        }

        return false;
    }

    @Override
    public User create(UserDTO userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setRegTime(StringUtils.getCurrentTime());
        //检查用户名和手机号是否重复
        boolean hasSameUserName = this.checkUserInfo("username", userDTO.getUsername());
        if (hasSameUserName) {
            throw new GlobalException("该用户名已经被使用");
        }

        boolean hasSameMobile = this.checkUserInfo("mobile", userDTO.getMobile());
        if (hasSameMobile) {
            throw new GlobalException("该手机号已被注册");
        }
        // 检查密码是否为空，如果不为空则进行加密
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        boolean save = this.save(user);

        // 后台 / POS 新建会员同样走「新增会员默认成为批发商」钩子（2026-06-11 Bug 修复）：
        // 与 C 端各注册入口口径一致；内部双开关 + 默认等级校验，失败不抛错、不影响建会员主流程
        wholesalerService.autoRegisterIfEnabled(user.getUserId());

        return user;
    }

    public boolean checkUserInfo(String type, String value) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if ("username".equals(type)) {
            queryWrapper.eq(User::getUsername, value);
            return this.count(queryWrapper) > 0L;
        }
        if ("mobile".equals(type)) {
            queryWrapper.eq(User::getMobile, value);
            return this.count(queryWrapper) > 0L;
        }
        return false;
    }

    @Override
    public boolean update(UserParam userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        if (userDTO.getPwdConfirm() != null && !Objects.equals(userDTO.getPassword(), userDTO.getPwdConfirm())) {
            throw new GlobalException("两次输入的密码不一致");

        }
        // 检查密码是否为空，如果不为空则进行加密
        String password = user.getPassword().replace(" ", "");
        if (StrUtil.isNotBlank(password)) {
            user.setPassword(passwordEncoder.encode(password));
        } else {
            user.setPassword(null);
        }
        if (ObjectUtil.isNotEmpty(userDTO.getEmail())) {
            Long emailCount = this.lambdaQuery().eq(User::getEmail, user.getEmail())
                    .ne(User::getUserId, user.getUserId()).count();
            Assert.isTrue(emailCount == 0, () -> new GlobalException("邮箱已注册"));
        }
        if (ObjectUtil.isNotEmpty(userDTO.getMobile())) {
            Long mobileCount = this.lambdaQuery().eq(User::getMobile, user.getMobile())
                    .ne(User::getUserId, user.getUserId()).count();
            Assert.isTrue(mobileCount == 0, () -> new GlobalException("手机号已注册"));
        }
        return this.updateById(user);
    }

    /**
     * 获取所有用户的余额总和
     *
     * @return BigDecimal
     */
    @Override
    public BigDecimal getUserBalanceTotal() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(User::getBalance);
        List<User> userList = this.list(queryWrapper);
        return userList.stream().map(User::getBalance).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    @Override
    public List<User> getUserByUsernameLike(String keyword) {
        if (StrUtil.isEmpty(keyword)) {
            return List.of();
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(User::getUsername, keyword);
        return this.list(queryWrapper);
    }

    @Override
    public List<User> getUserByUserInfoLike(String keyword) {
        if (StrUtil.isEmpty(keyword)) {
            return List.of();
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(User::getUsername, keyword)
                .or().like(User::getEmail, keyword)
                .or().like(User::getMobile, keyword);
        return this.list(queryWrapper);
    }

    @Override
    public void wechatServer(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WxMpConfigStorage wxMpConfigStorage = wxMpService.getWxMpConfigStorage();


        String signature = request.getParameter("signature");
        String nonce = request.getParameter("nonce");
        String timestamp = request.getParameter("timestamp");

        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        if (!wxMpService.checkSignature(timestamp, nonce, signature)) {
            // 消息签名不正确，说明不是公众平台发过来的消息
            response.getWriter().println("非法请求");
            return;
        }

        String echostr = request.getParameter("echostr");
        if (StringUtils.isNotBlank(echostr)) {
            // 说明是一个仅仅用来验证的请求，回显echostr
            response.getWriter().println(echostr);
            return;
        }

        String encryptType = StringUtils.isBlank(request.getParameter("encrypt_type")) ?
                "raw" :
                request.getParameter("encrypt_type");

        WxMpXmlMessage inMessage;

        if ("raw".equals(encryptType)) {
            // 明文传输的消息
            inMessage = WxMpXmlMessage.fromXml(request.getInputStream());
        } else if ("aes".equals(encryptType)) {
            // 是aes加密的消息
            String msgSignature = request.getParameter("msg_signature");
            inMessage = WxMpXmlMessage.fromEncryptedXml(request.getInputStream(), wxMpConfigStorage, timestamp, nonce, msgSignature);
        } else {
            response.getWriter().println("不可识别的加密类型");
            return;
        }
        if (inMessage != null && StrUtil.isNotBlank(inMessage.getEvent())) {
            String openid = inMessage.getFromUser();
            String ticket = inMessage.getTicket();
            String event = inMessage.getEvent();
            // 普通关注无 ticket，勿对 null 调用 isEmpty()，否则 NPE 会中断后续 route，关注欢迎语无法下发
            if (Arrays.asList("subscribe", "SCAN").contains(event)) {
                if (StrUtil.isNotBlank(ticket) && StrUtil.isNotBlank(openid)) {
                    redisTemplate.opsForValue().set(ticket, openid, 30, TimeUnit.MINUTES);
                    // 带 ticket 的关注/扫码即"扫码登录"：登录成功后主动下发自定义"登录成功回复"客服消息
                    sendLoginSuccessReply(openid, inMessage);
                }
            }
        }

        WxMpXmlOutMessage outMessage = wxMpMessageRouter.route(inMessage);
        if (outMessage != null) {
            if ("raw".equals(encryptType)) {
                response.getWriter().write(outMessage.toXml());
            } else if ("aes".equals(encryptType)) {
                response.getWriter().write(outMessage.toEncryptedXml(wxMpConfigStorage));
            }
        }
    }

    /**
     * 扫码登录成功后主动下发自定义"登录成功回复"（客服消息）。
     * <p>未配置或文本内容为空时跳过，避免下发空消息；发送本身容错（{@link com.tigshop.service.setting.wechat.WxKefuSender}
     * 内部已做能力探测与异常兜底），不影响登录主流程。
     *
     * @param openid    扫码用户 openid
     * @param inMessage 原始入站事件（供变量替换上下文）
     */
    private void sendLoginSuccessReply(String openid, WxMpXmlMessage inMessage) {
        com.tigshop.bean.vo.setting.wechat.ReplyPayloadVO reply = wechatAutoReplyService.getLoginSuccess();
        if (reply == null || reply.getMsgType() == null) {
            return;
        }
        // 文本类型内容为空视为未配置，跳过
        if (Integer.valueOf(1).equals(reply.getMsgType()) && StrUtil.isBlank(reply.getContent())) {
            return;
        }
        wxKefuSender.sendAsync(openid, java.util.List.of(reply), null, inMessage);
    }

    @Override
    public String generateUniqueUsername() {
        String username;
        do {
            username = StringUtils.getRandomString(8);
        } while (this.lambdaQuery().eq(User::getUsername, username).count() > 0);
        return username;
    }

    @Override
    public User getUserByUsername(String username) {
        if (StrUtil.isEmpty(username)) {
            return null;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        wrapper.and(w -> {
            w.eq(User::getUsername, username)
                    .or().eq(User::getMobile, username);

        });

        List<User> list = this.list(wrapper);
        if (ObjectUtil.isNotEmpty(list)) {
            return list.getFirst();
        }
        return null;
    }

    @Override
    public User getUserByMobile(String mobile) {
        if (StrUtil.isEmpty(mobile)) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getMobile, mobile));
    }

    @Override
    public Map<String, User> getUsersByMobiles(Collection<String> mobiles) {
        if (CollUtil.isEmpty(mobiles)) {
            return Collections.emptyMap();
        }
        Set<String> distinct = mobiles.stream()
                .filter(StrUtil::isNotEmpty)
                .collect(Collectors.toSet());
        if (distinct.isEmpty()) {
            return Collections.emptyMap();
        }
        return this.list(new LambdaQueryWrapper<User>().in(User::getMobile, distinct)).stream()
                .filter(u -> StrUtil.isNotEmpty(u.getMobile()))
                .collect(Collectors.toMap(User::getMobile, u -> u, (a, b) -> a));
    }

    @Override
    public void checkUserCompanyAuth() {
        String isIdentity = configService.getConfigVal(SettingsEnum.IS_IDENTITY);

        if ("1".equals(isIdentity) && tigshopProperties.getIsB2b() == 1) {
            User user = getById(SecurityUtils.getCurrentUserId());
            if (user != null && user.getIsCompanyAuth() == 0) {
                throw new GlobalException("请先完成实名认证");
            }
        }

    }

    @Override
    public Integer getUserReferrerId(Integer currentUserId) {
        if (currentUserId != null) {
            User user = getById(currentUserId);
            if (user != null) {
                return user.getReferrerUserId();
            }
        }
        return 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void decPoints(Integer usePoint, Integer currentUserId, String desc) {
        String uuid = UUID.randomUUID().toString();
        boolean lock = redisCache.tryLock("decPointsUserId::" + currentUserId, uuid, 30, TimeUnit.SECONDS);
        if (lock) {
            try {

                UserPointsLog userPointsLog = new UserPointsLog();
                userPointsLog.setUserId(currentUserId);
                userPointsLog.setPoints(usePoint);
                userPointsLog.setChangeTime(StringUtils.getCurrentTime());
                userPointsLog.setChangeDesc(desc);
                userPointsLog.setChangeType(ChangTypeEnum.DEC_POINTS.getValue());
                LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(User::getUserId, currentUserId).setDecrBy(User::getPoints, usePoint);

                String configVal = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
                if (this.update(updateWrapper)) {
                    if (userPointsLogMapper.insert(userPointsLog) < 1) {
                        throw new GlobalException(translatePackage.translate(configVal + "日志操作失败"));
                    }
                } else {
                    throw new GlobalException(translatePackage.translate(configVal + "不足"));
                }
            } catch (Exception e) {
                throw new GlobalException(e.getMessage());
            } finally {
                redisCache.unlock("decPointsUserId::" + currentUserId, uuid);
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incPoints(Integer point, Integer currentUserId, String desc) {
        String uuid = UUID.randomUUID().toString();
        boolean lock = redisCache.tryLock("incPointsUserId::" + currentUserId, uuid, 30, TimeUnit.SECONDS);
        if (lock) {
            try {
                UserPointsLog userPointsLog = new UserPointsLog();
                userPointsLog.setUserId(currentUserId);
                userPointsLog.setPoints(point);
                userPointsLog.setChangeTime(StringUtils.getCurrentTime());
                userPointsLog.setChangeDesc(desc);
                userPointsLog.setChangeType(ChangTypeEnum.INC_POINTS.getValue());
                this.lambdaUpdate()
                        .eq(User::getUserId, currentUserId)
                        .setIncrBy(User::getPoints, point)
                        .update();
                userPointsLogMapper.insert(userPointsLog);
            } catch (Exception e) {
                throw new GlobalException(e.getMessage());
            } finally {
                redisCache.unlock("incPointsUserId::" + currentUserId, uuid);
            }
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void decBalance(BigDecimal useBalance, Integer currentUserId, String desc) {
        User user = this.getById(currentUserId);

        String uuid = UUID.randomUUID().toString();
        boolean lock = redisCache.tryLock("decBalanceUserId::" + currentUserId, uuid, 30, TimeUnit.SECONDS);
        if (lock) {
            try {
                UserBalanceLog userBalanceLog = new UserBalanceLog();
                userBalanceLog.setUserId(currentUserId);
                userBalanceLog.setBeforeBalance(user.getBalance());
                userBalanceLog.setBalance(useBalance);
                userBalanceLog.setAfterBalance(user.getBalance().subtract(useBalance));
                userBalanceLog.setBeforeFrozenBalance(user.getFrozenBalance());
                userBalanceLog.setAfterFrozenBalance(user.getFrozenBalance());
                userBalanceLog.setChangeTime(StringUtils.getCurrentTime());
                userBalanceLog.setChangeDesc(desc);
                userBalanceLog.setChangeType(ChangTypeEnum.DEC_POINTS.getValue());
                LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(User::getUserId, currentUserId).ge(User::getBalance, useBalance).setDecrBy(User::getBalance, useBalance);

                String configVal = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
                if (this.update(updateWrapper)) {
                    if (userBalanceLogMapper.insert(userBalanceLog) < 1) {
                        throw new GlobalException(messageHelper.get("error.user.balance.log.failed"));
                    }
                } else {
                    throw new GlobalException(translatePackage.translate(configVal + "不足"));
                }
            } catch (Exception e) {
                throw new GlobalException(e.getMessage());
            } finally {
                redisCache.unlock("decBalanceUserId::" + currentUserId, uuid);
            }
        } else {
            // 抢锁失败必须抛异常触发外层事务回滚，禁止静默返回导致"订单按余额支付成功但余额未扣减"
            throw new GlobalException(translatePackage.translate("系统繁忙，请稍后重试"));
        }
    }

    @Override
    public UserClientDetailVO clientDetail(Integer userId) {
        UserClientDetailVO userClientDetailVO = this.getBaseInfo(userId);
        //查询用户是否授权
        userClientDetailVO.setIsBindWechat(userAuthorizeService.checkUserIsAuthorize(userId, 1));
        Salesman salesman = getSalesmanByUserId(userId);
        UserClientDetailVO.Salesman salesmanVO = new UserClientDetailVO.Salesman();
        if (salesman != null) {
            BeanUtils.copyProperties(salesman, salesmanVO);
            salesmanVO.setAddTime(TigUtils.handelTime(salesman.getAddTime()));
            userClientDetailVO.setSalesman(salesmanVO);
        }
        //查询是否开启签到
        ConfigPO useQiandaoPointConfig = configService.getConfigByCode(SettingsEnum.USE_QIANDAO_POINT.getBizCode());
        userClientDetailVO.setShowSign(Integer.parseInt(useQiandaoPointConfig.getBizVal()));
        userClientDetailVO.setHasShop(adminUserShopService.hasShop(userId));
        String avatar = userClientDetailVO.getAvatar();
        if (StrUtil.isEmpty(avatar)) {
            userClientDetailVO.setAvatar("https://oss.tigshop.com/img/gallery/202403/17111886010Pg60qIrJEzwe3tw06.gif");
        }

        // 填充账期支付信息
        try {
            String useCredit = configService.getConfigVal(SettingsEnum.USE_CREDIT);
            if ("1".equals(useCredit)) {
                CreditUser creditUser = creditUserService.getByUserId(userId);
                if (creditUser != null) {
                    UserClientDetailVO.CreditPaymentInfo creditPaymentInfo = new UserClientDetailVO.CreditPaymentInfo();
                    creditPaymentInfo.setCreditLimit(creditUser.getCreditLimit());
                    creditPaymentInfo.setUsedAmount(creditUser.getUsedAmount());
                    creditPaymentInfo.setAvailableAmount(creditUser.getAvailableAmount());
                    creditPaymentInfo.setSettlementType(creditUser.getSettlementType());
                    creditPaymentInfo.setSettlementTypeText(SettlementTypeEnum.getDescByCode(creditUser.getSettlementType()));
                    userClientDetailVO.setCreditPaymentInfo(creditPaymentInfo);
                }
            }
        } catch (Exception e) {
            // 账期支付信息获取失败不影响用户详情返回
        }

        return userClientDetailVO;
    }

    /**
     * 获取用户基本信息
     *
     * @param userId 用户id
     * @return UserClientDetailVO
     */
    public UserClientDetailVO getBaseInfo(Integer userId) {
        UserClientDetailVO userClientDetailVO = new UserClientDetailVO();
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(USER_NOT_EXIST);
        }
        UserRankConfig userRankConfig = this.getUserRankConfig();
        BeanUtils.copyProperties(user, userClientDetailVO);
        UserRank userRank = userRankService.getById(user.getRankId());
        if (userRank != null) {
            BeanUtils.copyProperties(userRank, userClientDetailVO);
            userClientDetailVO.setRights(JsonUtil.fromJson(userRank.getRights(), JSONArray.class));
        }
        Map<String, Integer> map = this.refreshRank(userId, userRankConfig);
        userClientDetailVO.setTotalBalance(user.getBalance().add(user.getFrozenBalance()));
        userClientDetailVO.setAvatar(this.getUserAvatar(user.getAvatar()));
        userClientDetailVO.setCoupon(userCouponMapper.selectCount(new MPJLambdaWrapper<UserCoupon>()
                .leftJoin(Coupon.class, Coupon::getCouponId, UserCoupon::getCouponId)
                .eq(Coupon::getIsShow, Constants.YES)
                .eq(UserCoupon::getUserId, userId).eq(UserCoupon::getOrderId, 0)
                .ge(UserCoupon::getEndDate, StringUtils.getCurrentTime())
                .le(UserCoupon::getStartDate, StringUtils.getCurrentTime())
        ));
        //获取会员等级有效期
        //从map中获取 rank_expire_time
        userClientDetailVO.setRankExpireTime(map.get("rank_expire_time").toString());
        userClientDetailVO.setGrowth(map.get("growth"));
        UserClientDetailVO.RankConfig rankConfig = new UserClientDetailVO.RankConfig();
        BeanUtils.copyProperties(userRankConfig, rankConfig);
        rankConfig.setData(JsonUtil.fromJson(userRankConfig.getData(), JSONObject.class));
        userClientDetailVO.setRankConfig(rankConfig);
        userClientDetailVO.setDimUsername(getDimUsername(user.getUsername()));
        // D1146：匿名评价的展示名由后端统一生成，避免各端各自复制掩码规则后与入库展示不一致。
        // 评价入库写的是 user.getUsername()（见 CommentEvaluateParam.createComment），故此处脱敏同一字段。
        userClientDetailVO.setAnonymousUsername(AnonymousMaskUtils.maskName(user.getUsername()));
        userClientDetailVO.setGrowthPoints(user.getGrowthPoints());
        userClientDetailVO.setRegisterTime(TigUtils.handelTime(user.getRegTime()));
        return userClientDetailVO;
    }

    /**
     * 获取脱敏用户名
     *
     * @param username 用户名
     * @return String
     */
    public String getDimUsername(String username) {
        if (username == null) {
            return "";
        }
        return username.charAt(0) + "***" + username.substring(username.length() - 1);
    }

    /**
     * 获取会员头像
     *
     * @param avatar String
     * @return String
     */
    @Override
    public String getUserAvatar(String avatar) {
        if (avatar == null || StringUtils.isEmpty(avatar)) {
            //从配置获取默认头像
            avatar = configService.getConfigByCode(SettingsEnum.DEFAULT_AVATAR.getBizCode()).getBizVal();
        }
        return avatar;
    }

    @Transactional
    @Override
    public boolean updateInformation(UpdateInformationDTO updateInformationDTO) {
        Integer userId = getCurrentUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(USER_NOT_EXIST);
        }
        if (StrUtil.isNotBlank(updateInformationDTO.getMobile())) {
            long mobileCount = this.count(new LambdaQueryWrapper<User>()
                    .eq(User::getMobile, updateInformationDTO.getMobile())
                    .ne(User::getUserId, userId));
            Assert.isTrue(mobileCount == 0, () -> new GlobalException("手机号已注册"));
        }
        this.getGrowthPoints(user, 1);
        user.setBirthday(updateInformationDTO.getBirthday());
        user.setNickname(updateInformationDTO.getNickname());
        user.setEmail(updateInformationDTO.getEmail());
        user.setMobile(updateInformationDTO.getMobile());
        user.setWechatImg(updateInformationDTO.getWechatImg());
        boolean updateFlag = this.updateById(user);

        userRankService.addUserGrowthPoints(user.getUserId(), RankGrowthLogTypeEnum.GROWTH_TYPE_INFORMATION);

        return updateFlag;
    }

    @Override
    public MemberCenterVO memberCenter() {
        Integer userId = getCurrentUserId();
        User user = this.getById(userId);
        Assert.notNull(user, () -> new GlobalException(USER_NOT_EXIST));

        MemberCenterVO memberCenterVO = new MemberCenterVO();
        BeanUtils.copyProperties(user, memberCenterVO);
        UserRank userRank = userRankService.getById(user.getRankId());
        if (userRank != null) {
            BeanUtils.copyProperties(userRank, memberCenterVO);
            memberCenterVO.setRights(JsonUtil.fromJson(userRank.getRights(), JSONArray.class));
        }
        memberCenterVO.setDimUsername(getDimUsername(user.getUsername()));
        if (user.getMobileValidated() > 0 && user.getEmailValidated() > 0) {
            memberCenterVO.setSecurityLv(3);
        } else if (user.getMobileValidated() > 0 && user.getEmailValidated() == 0 ||
                user.getMobileValidated() == 0 && user.getEmailValidated() > 0) {
            memberCenterVO.setSecurityLv(2);
        } else {
            memberCenterVO.setSecurityLv(1);
        }
        memberCenterVO.setAwaitPay(orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId).eq(Order::getOrderStatus, OrderStatusEnum.PENDING.getCode())));
        memberCenterVO.setAwaitShipping(orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId).eq(Order::getOrderStatus, OrderStatusEnum.CONFIRMED.getCode())));

        Long awaitReceived = orderMapper.selectCount(
                Wrappers.lambdaQuery(Order.class)
                        .eq(Order::getUserId, userId)
                        .eq(Order::getOrderStatus, OrderStatusEnum.PROCESSING.getCode())
        );
        memberCenterVO.setAwaitReceived(awaitReceived);

        Long awaitComment = orderMapper.selectCount(
                Wrappers.lambdaQuery(Order.class)
                        .eq(Order::getUserId, userId)
                        .eq(Order::getCommentStatus, CommentStatus.PENDING.getCode())
                        .eq(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode())
        );
        memberCenterVO.setAwaitComment(awaitComment);

        memberCenterVO.setAwaitCoupon(userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getUserId, userId)));
        String avatar = memberCenterVO.getAvatar();
        if (StrUtil.isEmpty(avatar)) {
            memberCenterVO.setAvatar("https://oss.tigshop.com/img/gallery/202403/17111886010Pg60qIrJEzwe3tw06.gif");
        } else {
            // 去掉头像 URL 上的处理参数（?x-oss-process= 等），返回原图，兼容历史已存带参数据
            memberCenterVO.setAvatar(galleryPicService.getOriginalImage(avatar));
        }
        return memberCenterVO;
    }

    @Override
    public void sendMobileCodeByModifyPassword(RegisterSmsDTO dto) {
        Integer userId = getCurrentUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(messageHelper.get("error.member.not.exist"));
        }
        // 统一使用当前登录用户手机号，确保验证码存取的 Redis key 一致
        dto.setMobile(user.getMobile());
        configService.sendSms(dto);
    }

    @Override
    public boolean checkModifyPasswordMobileCode(CheckModifyPasswordDTO checkModifyPasswordDTO) {
        Integer userId = getCurrentUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(messageHelper.get("error.member.not.exist"));
        }
        // 校验验证码
//        String code = (String) redisTemplate.opsForValue().get("modify_password" + user.getMobile());
        Object object = redisTemplate.opsForValue().get("modify_password" + user.getMobile());
        String code = object == null ? null : object.toString();
        if (code == null || !code.equals(checkModifyPasswordDTO.getCode())) {
            throw new GlobalException(messageHelper.get("error.user.sms.code.invalid"));
        }
        if (checkModifyPasswordDTO.getPassword() == null) {
            throw new GlobalException(messageHelper.get("error.user.new.password.required"));
        }

        if (StringUtils.isEmpty(user.getMobile())) {
            throw new GlobalException(messageHelper.get("error.user.mobile.required"));
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getMobile, user.getMobile());
        queryWrapper.ne(User::getUserId, userId);
        if (this.count(queryWrapper) > 0) {
            throw new GlobalException(messageHelper.get("error.user.mobile.already.bound"));
        }
        user.setPassword(passwordEncoder.encode(checkModifyPasswordDTO.getPassword()));
        return this.updateById(user);
    }

    @Transactional
    @Override
    public void modifyMobile(ModifyMobileParam param) {
        Integer modifyMobileCode = (Integer) redisTemplate.opsForValue().get("modify_mobile" + param.getMobile());
        Assert.isTrue(modifyMobileCode != null && param.getCode().equals(modifyMobileCode.toString()), () -> new GlobalException("验证码错误或已过期，请重试"));

        User user = this.getById(SecurityUtils.getCurrentUserId());
        Assert.notNull(user, () -> new GlobalException(messageHelper.get("error.member.not.exist")));

        // 校验手机号是否存在
        if (ObjectUtil.notEqual(user.getMobile(), param.getMobile())) {
            Long mobileCount = this.lambdaQuery().eq(User::getMobile, param.getMobile()).count();
            Assert.isTrue(mobileCount == 0, () -> new GlobalException("手机号已经被绑定"));

            // 首次绑定，判断是否增加会员成长值
            if (StrUtil.isBlank(user.getMobile())) {
                userRankService.addUserGrowthPoints(user.getUserId(), RankGrowthLogTypeEnum.GROWTH_TYPE_BIND_PHONE);
            }

            user.setMobileValidated(1);
            user.setMobile(param.getMobile());
            this.updateById(user);
        }

    }

    @Override
    public void modifyEmail(ModifyEmailParam param) {
        User user = this.getById(SecurityUtils.getCurrentUserId());
        Assert.notNull(user, () -> new GlobalException(messageHelper.get("error.member.not.exist")));

        //注册用户
        User emailUser = this.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, param.getEmail()));
        if (emailUser != null) {
            throw new GlobalException("邮箱已经被绑定");
        }

        Integer modifyEmailCode = (Integer) redisTemplate.opsForValue().get("modify_email" + param.getEmail());
        Assert.isTrue(modifyEmailCode != null && param.getCode().equals(modifyEmailCode.toString()), () -> new GlobalException("验证码错误或已过期，请重试"));
        user.setEmailValidated(1);
        user.setEmail(param.getEmail());
        this.updateById(user);
    }

    @Override
    public List<ProductListResDTO> historyProduct() {
        Integer userId = getCurrentUserId();
        User user = this.getById(userId);
        Assert.notNull(user, () -> new GlobalException(messageHelper.get("error.member.not.exist")));

        if (StrUtil.isBlank(user.getHistoryProductIds()) || "[ ]".equals(user.getHistoryProductIds())) {
            return Collections.emptyList();
        }

        List<Integer> historyProductIds = StringUtils.str2IntList(user.getHistoryProductIds());
        return productService.historyProduct(historyProductIds);
    }

    @Override
    public boolean mobileValidate(CheckModifyPasswordDTO checkModifyPasswordDTO) {
        String event = "mobile_validate";
        Integer userId = getCurrentUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(messageHelper.get("error.member.not.exist"));
        }
        // 前端传了手机号则用前端传的，否则取当前登录用户的手机号
        String mobile = StringUtils.isEmpty(checkModifyPasswordDTO.getMobile())
                ? user.getMobile()
                : checkModifyPasswordDTO.getMobile();
        Object code = redisTemplate.opsForValue().get(event + user.getMobile());
        String mobileCode = code == null ? "" : code.toString();

        if (StrUtil.isEmpty(mobileCode)) {
            throw new GlobalException(messageHelper.get("error.user.code.empty"));
        }
        if (!mobileCode.equals(checkModifyPasswordDTO.getCode())) {
            throw new GlobalException(messageHelper.get("error.user.sms.code.invalid"));
        }
        //查看是否已经绑定了手机号
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getMobile, mobile);
        queryWrapper.ne(User::getUserId, userId);
        if (this.count(queryWrapper) > 0) {
            throw new GlobalException(messageHelper.get("error.user.mobile.already.bound"));
        }
        return true;
    }

    @Override
    public boolean emailValidateNew(CheckModifyEmailDTO checkModifyEmailDTO) {
        String event = "email_validate";
        Integer userId = getCurrentUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(messageHelper.get("error.member.not.exist"));
        }
        if (StringUtils.isEmpty(checkModifyEmailDTO.getEmail())) {
            throw new GlobalException(messageHelper.get("error.user.email.required"));
        }
        Object code = redisTemplate.opsForValue().get(event + user.getEmail());
        String emailCode = code == null ? "" : code.toString();

        if (StrUtil.isEmpty(emailCode)) {
            throw new GlobalException(messageHelper.get("error.user.code.empty"));
        }
        if (!emailCode.equals(checkModifyEmailDTO.getCode())) {
            throw new GlobalException(messageHelper.get("error.user.email.code.invalid"));
        }
        //查看是否已经绑定了手机号
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, checkModifyEmailDTO.getEmail());
        queryWrapper.ne(User::getUserId, userId);
        if (this.count(queryWrapper) > 0) {
            throw new GlobalException(messageHelper.get("error.user.email.already.bound"));
        }
        return true;
    }

    @Override
    public boolean emailValidate(CheckEmailModifyDTO checkEmailModifyDTO) {
        Integer userId = getCurrentUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(messageHelper.get("error.member.not.exist"));
        }
        if (StringUtils.isEmpty(checkEmailModifyDTO.getEmail())) {
            throw new GlobalException(messageHelper.get("error.user.email.required"));
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, checkEmailModifyDTO.getEmail());
        queryWrapper.ne(User::getUserId, userId);
        if (this.count(queryWrapper) > 0) {
            throw new GlobalException(messageHelper.get("error.user.email.already.bound"));
        }

        if (checkEmailModifyDTO.getType() != null && checkEmailModifyDTO.getType() > 0) {
            user.setEmail(checkEmailModifyDTO.getEmail());
            user.setEmailValidated(1);
            return this.updateById(user);
        }
        return true;
    }

    @Override
    public boolean delHistoryProduct(DelHistoryProductDTO dto) {
        Integer userId = getCurrentUserId();
        User user = this.getById(userId);

        List<Integer> userHistoryProductIds = StringUtils.str2IntList(user.getHistoryProductIds());

        if (CollUtil.isNotEmpty(dto.getIds())) {
            userHistoryProductIds = userHistoryProductIds.stream().filter(id -> !dto.getIds().contains(id)).toList();
        }

        user.setHistoryProductIds(JsonUtil.toJson(userHistoryProductIds));
        this.updateById(user);
        return true;
    }

    @Override
    public GalleryPicUploadVO uploadImg(MultipartFile file) {
        return galleryPicService.uploadImg(0, file);
    }

    @Override
    public GalleryPicUploadVO uploadImg(MultipartFile file,boolean isHidden) {
        return galleryPicService.uploadImg(0, file,isHidden);
    }

    @Override
    public boolean modifyAvatar(MultipartFile file) {
        Integer userId = getCurrentUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(messageHelper.get("error.member.not.exist"));
        }
        // 小程序端头像上传内容安全校验（鉴黄鉴违），命中违规则拦截，不落库
        wxImgSecCheckHelper.checkAvatar(file);
        GalleryPicUploadVO galleryPicUploadVO = galleryPicService.uploadImg(0, file);
        // 存原图地址（picUrl），不存带处理参数的缩略图（picThumb），避免保存后展示为缩放图与上传原图不一致
        user.setAvatar(galleryPicUploadVO.getPicUrl());
        return this.updateById(user);
    }

    @Override
    public Page<CollectShopVO> myCollectShop(CollectShopListDTO collectShopListDTO) {
        return collectShopService.list(collectShopListDTO);
    }

    /**
     * 获取成长值
     *
     * @param user User
     */
    public void getGrowthPoints(User user, Integer type) {
        UserRankConfig userRankConfig = userRankConfigService.getUserRankConfig("grow_config");
        if (userRankConfig != null) {
            Integer growthPoints = 0;
            UserGrowConfig userGrowConfig = JsonUtil.fromJson(userRankConfig.getData(), UserGrowConfig.class);

            switch (type) {
                case 1 -> {
                    if (userGrowConfig.getEvpi() != null) {
                        //判断字符串未空
                        if (StringUtils.isEmpty(user.getNickname()) && StringUtils.isEmpty(user.getBirthday())) {
                            growthPoints = userGrowConfig.getEvpiGrowth();
                        }
                    }
                }
                case 2 -> {
                    //第一次绑定手机号
                    if (userGrowConfig.getBindPhone() != null) {
                        if (StringUtils.isEmpty(user.getMobile())) {
                            growthPoints = userGrowConfig.getBindPhoneGrowth();
                        }
                    }
                }
            }
            if (growthPoints != 0) {
                RankGrowthLog userGrowthLog = new RankGrowthLog();
                userGrowthLog.setUserId(user.getUserId());
                if (type == 1) {
                    userGrowthLog.setType(RankGrowthLogTypeEnum.GROWTH_TYPE_INFORMATION.getCode());
                }
                if (type == 2) {
                    userGrowthLog.setType(RankGrowthLogTypeEnum.GROWTH_TYPE_BIND_PHONE.getCode());
                }
                userGrowthLog.setGrowthPoints(growthPoints);
                userGrowthLog.setChangeType(ChangTypeEnum.INC_POINTS.getValue());
                userGrowthLog.setCreateTime(StringUtils.getCurrentTime());
                rankGrowthLogService.save(userGrowthLog);
            }
        }

    }

    /**
     * 处理用户会员等级
     *
     * @param userId 用户ID
     */
    public Map<String, Integer> refreshRank(Integer userId, UserRankConfig userRankConfig) {

        Map<String, Integer> map = new HashMap<>();
        Integer growth = 0;
        long rankExpireTime = 0L;
        if (userRankConfig != null) {
            UserRankConfigDTO userRankConfigDTO = JsonUtil.fromJson(userRankConfig.getData(), UserRankConfigDTO.class);
            growth = this.getExpireRangeGrowth(userRankConfigDTO, userId);
            User user = this.getById(userId);
            UserRank userRank = userRankService.getById(user.getRankId());
            if (userRank == null) {
                this.handleUserRankLog(userId, user);
            }
            LambdaQueryWrapper<UserRankLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserRankLog::getUserId, userId);
            queryWrapper.orderByDesc(UserRankLog::getId);
            queryWrapper.last("limit 1");
            UserRankLog userRankLog = userRankLogService.getOne(queryWrapper);
            if (userRankLog != null && userRankConfigDTO.getType() == 2) {
                //有时效
                rankExpireTime = userRankLog.getChangeTime() + userRankConfigDTO.getRankAfterMonth() * 30 * 24 * 60 * 60;
                //等级过期后重新定义等级
                if (StringUtils.getCurrentTime() > rankExpireTime) {
                    this.getGrowthByRule(userId);
                    // 重新定义有效期
                    LambdaQueryWrapper<UserRankLog> queryWrapperLog = new LambdaQueryWrapper<>();
                    queryWrapperLog.eq(UserRankLog::getUserId, userId);
                    queryWrapperLog.orderByDesc(UserRankLog::getId);
                    queryWrapperLog.last("limit 1");
                    UserRankLog userRankLogNew = userRankLogService.getOne(queryWrapperLog);
                    rankExpireTime = userRankLogNew.getChangeTime() + userRankConfigDTO.getRankAfterMonth() * 30 * 24 * 60 * 60;
                }
            }
        }
        map.put("growth", growth);
        map.put("rank_expire_time", (int) rankExpireTime);
        return map;
    }

    public void getGrowthByRule(Integer userId) {
        UserRankConfig userRankConfig = this.getUserRankConfig();
        if (userRankConfig != null) {
            UserRankConfigDTO userRankConfigDTO = JsonUtil.fromJson(userRankConfig.getData(), UserRankConfigDTO.class);
            if (userRankConfigDTO != null && userRankConfigDTO.getType() == 2) {
                Integer growth = this.getExpireRangeGrowth(userRankConfigDTO, userId);
                if (userRankConfig.getRankType() == 1) {
                    this.changeUserRank(userId, growth, userRankConfig.getRankType());
                }
            }
        }
    }

    /**
     * 根据成长值重新计算等级  -- 在保级范围内等级不变
     *
     * @param userId   用户ID
     * @param growth   成长值
     * @param rankType 类型
     */
    public void changeUserRank(Integer userId, Integer growth, Integer rankType) {
        User user = this.getById(userId);
        LambdaQueryWrapper<UserRank> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRank::getRankType, rankType);
        queryWrapper.le(UserRank::getMinGrowthPoints, growth);
        queryWrapper.orderByDesc(UserRank::getMinGrowthPoints);
        queryWrapper.last("limit 1");
        UserRank userRank = userRankService.getOne(queryWrapper);
        if (userRank != null && !Objects.equals(userRank.getRankId(), user.getRankId())) {
            createUserRankLog(userId, user, userRank);
        }

    }

    private void createUserRankLog(Integer userId, User user, UserRank userRank) {
        user.setRankId(userRank.getRankId());
        boolean isUpdate = this.updateById(user);
        if (isUpdate) {
            UserRankLog userRankLog = new UserRankLog();
            userRankLog.setUserId(userId);
            userRankLog.setRankId(userRank.getRankId());
            userRankLog.setRankType(userRank.getRankType());
            userRankLog.setRankName(userRank.getRankName());
            userRankLog.setChangeTime(StringUtils.getCurrentTime());
            userRankLogService.save(userRankLog);
        }
    }

    //创建user_rank_log
    public void handleUserRankLog(Integer userId, User user) {
        LambdaQueryWrapper<UserRank> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRank::getMinGrowthPoints, 0);
        queryWrapper.orderByAsc(UserRank::getRankId);
        queryWrapper.last("limit 1");
        UserRank userRank = userRankService.getOne(queryWrapper);
        if (userRank != null) {
            createUserRankLog(userId, user, userRank);
        }
    }

    /**
     * 获取用户等级配置
     *
     * @return UserRankConfigDTO
     */
    public UserRankConfig getUserRankConfig() {
        return userRankConfigService.getUserRankConfig("rank_config");
    }

    /**
     * 获取成长值
     *
     * @param userRankConfigDTO 会员配置
     * @return Integer
     */
    public Integer getExpireRangeGrowth(UserRankConfigDTO userRankConfigDTO, Integer userId) {
        if (userRankConfigDTO != null && userRankConfigDTO.getType() == 2) {
            LambdaQueryWrapper<UserRankLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserRankLog::getUserId, userId);
            queryWrapper.orderByDesc(UserRankLog::getId);
            queryWrapper.last("limit 1");
            UserRankLog userRankLog = userRankLogService.getOne(queryWrapper);
            QueryWrapper<RankGrowthLog> rankGrowthLogQueryWrapper = new QueryWrapper<>();
            rankGrowthLogQueryWrapper.eq("user_id", userId);
            Long incGrowthQuery = 0L;
            Long decGrowthQuery = 0L;
            if (userRankLog != null) {
                Long rankEndTime = userRankLog.getChangeTime() + userRankConfigDTO.getRankAfterMonth() * 30 * 24 * 60 * 60;
                rankGrowthLogQueryWrapper.le("create_time", rankEndTime);
                rankGrowthLogQueryWrapper.ge("create_time", userRankLog.getChangeTime());
            } else {
                rankGrowthLogQueryWrapper.le("create_time", StringUtils.getCurrentTime());
            }
            rankGrowthLogQueryWrapper.eq("change_type", 1);
            rankGrowthLogQueryWrapper.select("sum(growth_points) as createTime");
            RankGrowthLog rankGrowthLog = rankGrowthLogService.getOne(rankGrowthLogQueryWrapper);
            if (rankGrowthLog != null) {
                incGrowthQuery = rankGrowthLog.getCreateTime();
            }

            rankGrowthLogQueryWrapper.eq("change_type", 2);
            rankGrowthLogQueryWrapper.select("sum(growth_points) as createTime");
            RankGrowthLog rankGrowthLog1 = rankGrowthLogService.getOne(rankGrowthLogQueryWrapper);
            if (rankGrowthLog1 != null) {
                decGrowthQuery = rankGrowthLog1.getCreateTime();
            }
            return Math.max(incGrowthQuery.intValue() - decGrowthQuery.intValue(), 0);
        }
        return 0;
    }

    /**
     * 根据用户ID获取销售员
     *
     * @param userId 用户ID
     * @return Salesman
     */
    public Salesman getSalesmanByUserId(Integer userId) {
        LambdaQueryWrapper<Salesman> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Salesman::getUserId, userId);
        queryWrapper.last("limit 1");
        return salesmanMapper.selectOne(queryWrapper);
    }

    @Transactional
    @Override
    public String register(RegisterParam param) {
        if ("email".equals(param.getRegistType())) {
            return registerByEmail(param);
        } else {
            return registerByMobile(param);
        }
    }

    private String registerByEmail(RegisterParam param) {
        param.emailValidate();
        String email = param.getEmail();

        String redCode = configService.getConfigVal(SettingsEnum.SHOP_REG_CLOSED);
        Assert.isFalse(StrUtil.equals(redCode, "1"), () -> new GlobalException("系统已关闭注册"));


        // 验证验证码 需要加上之前发送验证码时带的 event
        validateVerificationCode("login" + email, param.getEmailCode());

        Long emailCount = this.lambdaQuery().eq(User::getEmail, email).count();
        Assert.isTrue(emailCount == 0, () -> new GlobalException("邮箱已注册"));
        String userNamePrefix = configService.getConfigVal(SettingsEnum.USERNAME_PREFIX);
        User user = param.createEmailUser(generateUniqueUsername(), userNamePrefix);
        user.setPassword(passwordEncoder.encode(param.getPassword()));
        this.save(user);

        wholesalerService.autoRegisterIfEnabled(user.getUserId());

        redisTemplate.delete("login" + email);

        SalesmanSaveParam salesmanSaveParam = new SalesmanSaveParam();
        salesmanSaveParam.setUserId(user.getUserId());
        salesmanSaveParam.setLevel(1);
        if (param.getSalesmanId() != null) {
            Salesman salesman = findSalesmanByIdOrUserId(param.getSalesmanId());
            if (salesman != null) {
                SalesmanCustomer entity = new SalesmanCustomer();
                entity.setSalesmanId(salesman.getSalesmanId());
                entity.setUserId(user.getUserId());
                entity.setAddTime(System.currentTimeMillis() / 1000);
                entity.setChannel(FromTag.getDescriptionByClientType(HeaderUtils.getClientType()));
                salesmanCustomerMapper.insert(entity);
                // 发送拉人消息，用于分销员等级升级
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.DIRECT_EXCHANGE,
                        RabbitMQConfig.SALESMAN_UPDATE_ROUTING_KEY,
                        salesman.getSalesmanId());
                // pid 语义为上级分销员ID（salesman_id），此前误存为 userId，已订正
                salesmanSaveParam.setPid(salesman.getSalesmanId());
            }
        }
        SalesmanConfigVO salesmanConfig = salesmanConfigService.detail("salesmanConfig");
        if (salesmanConfig.getRegisterToSalesman() != null && salesmanConfig.getRegisterToSalesman() == 1) {
            salesmanService.create(salesmanSaveParam);
        }
        return jwtUtil.generateToken(user.getUserId(), false, user.getUsername());
    }

    @Nullable
    private String registerByMobile(RegisterParam param) {
        // 跨境版本跳过手机号格式验证
        param.mobileValidate(tigshopProperties.getIsOverseas() == 1);
        String mobile = param.getMobile();

        String redCode = configService.getConfigVal(SettingsEnum.SHOP_REG_CLOSED);
        Assert.isFalse(StrUtil.equals(redCode, "1"), () -> new GlobalException("系统已关闭注册"));

        // 验证验证码 需要加上之前发送验证码时带的 event
        validateVerificationCode("login" + mobile, param.getMobileCode());

        Long mobileCount = this.lambdaQuery().eq(User::getMobile, mobile).count();
        Assert.isTrue(mobileCount == 0, () -> new GlobalException("手机号已注册"));
        String userNamePrefix = configService.getConfigVal(SettingsEnum.USERNAME_PREFIX);
        User user = param.createMobileUser(generateUniqueUsername(), userNamePrefix);
        user.setPassword(passwordEncoder.encode(param.getPassword()));
        this.save(user);

        wholesalerService.autoRegisterIfEnabled(user.getUserId());

        redisTemplate.delete("login" + mobile);

        SalesmanSaveParam salesmanSaveParam = new SalesmanSaveParam();
        salesmanSaveParam.setUserId(user.getUserId());
        salesmanSaveParam.setLevel(1);
        if (param.getSalesmanId() != null) {
            Salesman salesman = findSalesmanByIdOrUserId(param.getSalesmanId());
            if (salesman != null) {
                SalesmanCustomer entity = new SalesmanCustomer();
                entity.setSalesmanId(salesman.getSalesmanId());
                entity.setUserId(user.getUserId());
                entity.setAddTime(System.currentTimeMillis() / 1000);
                entity.setChannel(FromTag.getDescriptionByClientType(HeaderUtils.getClientType()));
                salesmanCustomerMapper.insert(entity);
                // 发送拉人消息，用于分销员等级升级
                rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE, RabbitMQConfig.SALESMAN_UPDATE_ROUTING_KEY, salesman.getSalesmanId());
                // pid 语义为上级分销员ID（salesman_id），此前误存为 userId，已订正
                salesmanSaveParam.setPid(salesman.getSalesmanId());
            }
        }
        SalesmanConfigVO salesmanConfig = salesmanConfigService.detail("salesmanConfig");
        if (salesmanConfig.getRegisterToSalesman() != null && salesmanConfig.getRegisterToSalesman() == 1) {
            salesmanService.create(salesmanSaveParam);
        }
        return jwtUtil.generateToken(user.getUserId(), false, user.getUsername());
    }

    /**
     * 分销链接历史上可能携带分销员ID，也可能携带分销员用户ID。
     */
    private Salesman findSalesmanByIdOrUserId(Integer salesmanId) {
        Salesman salesman = salesmanMapper.selectById(salesmanId);
        if (salesman == null) {
            salesman = getSalesmanByUserId(salesmanId);
        }
        return salesman;
    }

    @Override
    public void loginByPhoneAndRegister(String mobile) {
        // 自动注册前再次查重，已有会员由调用方继续走短信登录认证。
        if (this.count(new LambdaQueryWrapper<User>().eq(User::getMobile, mobile)) > 0) {
            return;
        }
        String userNamePrefix = configService.getConfigVal(SettingsEnum.USERNAME_PREFIX);
        String randomUserName = generateUniqueUsername();
        String randomPassword = RandomUtil.randomString(10);
        User user = User.builder()
                .username(randomUserName)
                .nickname(StrUtil.isNotBlank(userNamePrefix) ? userNamePrefix + randomUserName : StrUtil.format("user_{}", randomUserName))
                .mobile(mobile)
                .password(passwordEncoder.encode(randomPassword))
                .regTime(StringUtils.getCurrentTime())
                .mobileValidated(Constants.YES)
                .build();
        this.save(user);

        wholesalerService.autoRegisterIfEnabled(user.getUserId());


        // 创建分销员
        SalesmanConfigVO salesmanConfig = salesmanConfigService.detail("salesmanConfig");
        if (salesmanConfig.getRegisterToSalesman() != null && salesmanConfig.getRegisterToSalesman() == 1) {
            SalesmanSaveParam salesmanSaveParam = new SalesmanSaveParam();
            salesmanSaveParam.setUserId(user.getUserId());
            salesmanSaveParam.setLevel(1);
            salesmanService.create(salesmanSaveParam);
        }
    }

    @Override
    public String checkMobile(CheckMobileDTO dto) {
        return issuePasswordReset(dto.getMobile(), dto.getCode(), false);
    }

    @Override
    public String checkEmail(CheckEmailParam param) {
        return issuePasswordReset(param.getEmail(), param.getCode(), true);
    }

    private Object validateVerificationCode(String key, String code) {
        if (code == null || !code.matches("[0-9]{6}")) {
            throw new GlobalException("验证码错误或已过期，请重试");
        }
        Object expected = redisTemplate.opsForValue().get(key);
        if (expected == null || !code.equals(expected.toString())) {
            throw new GlobalException("验证码错误或已过期，请重试");
        }
        return expected;
    }

    private String issuePasswordReset(String identity, String code, boolean email) {
        if (StrUtil.isBlank(identity)) {
            throw new GlobalException("手机号/邮箱不能为空");
        }
        String codeKey = "forgetPassword" + identity;
        Object expected = validateVerificationCode(codeKey, code);
        List<User> users = this.list(new LambdaQueryWrapper<User>()
                .eq(email, User::getEmail, identity)
                .eq(!email, User::getMobile, identity)
                .last("LIMIT 2"));
        if (users.size() != 1 || !Constants.YES.equals(users.getFirst().getStatus())) {
            throw new GlobalException("账号无法自动重置密码，请联系客服");
        }
        // 使用缓存原值参与原子比较，兼容历史验证码的字符串/数字序列化。
        if (!Long.valueOf(1).equals(redisTemplate.execute(CONSUME_VERIFICATION, List.of(codeKey), expected))) {
            throw new GlobalException("验证码错误或已过期，请重试");
        }
        byte[] random = new byte[32];
        PASSWORD_RESET_RANDOM.nextBytes(random);
        String token = HexFormat.of().formatHex(random);
        redisTemplate.opsForValue().set(PASSWORD_RESET_PREFIX + SecureUtil.sha256(token),
                users.getFirst().getUserId(), 10, TimeUnit.MINUTES);
        return token;
    }

    @Override
    public boolean modifyPassword(ForgetPasswordDTO dto) {
        String token = dto.getMobileKey();
        if (token == null || !token.matches("[0-9a-f]{64}")) {
            throw new GlobalException("重置凭据无效或已过期，请重新获取");
        }
        if (StrUtil.isBlank(dto.getPassword())) {
            throw new GlobalException("新密码不能为空");
        }
        String key = PASSWORD_RESET_PREFIX + SecureUtil.sha256(token);
        Object storedUserId = redisTemplate.opsForValue().get(key);
        if (!(storedUserId instanceof Integer userId) || userId <= 0) {
            throw new GlobalException("重置凭据无效或已过期，请重新获取");
        }
        User user = this.getById(userId);
        if (user == null || !Constants.YES.equals(user.getStatus())) {
            throw new GlobalException("账号无法自动重置密码，请联系客服");
        }
        User update = new User();
        update.setUserId(userId);
        update.setPassword(passwordEncoder.encode(dto.getPassword()));
        // 先一次性消费再写库；写库失败也不恢复凭据，用户需重新验证。
        if (!Long.valueOf(1).equals(redisTemplate.execute(CONSUME_VERIFICATION, List.of(key), storedUserId))) {
            throw new GlobalException("重置凭据无效或已过期，请重新获取");
        }
        if (baseMapper.updateById(update) != 1) {
            throw new GlobalException("密码修改失败，请重新获取验证码");
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void fundManagement(UserFundManagementDTO dto) {
        dto.validParam();

        User user = this.getById(dto.getId());
        Assert.notNull(user, () -> new GlobalException("会员不存在"));

        // 先在副本上完成资金校验，拒绝的请求不写流水；原对象保留变动前金额供流水使用。
        User updatedUser = new User();
        BeanUtils.copyProperties(user, updatedUser);
        updateUserFunds(updatedUser, dto);

        // 处理余额相关日志
        handleBalanceLogs(dto, user);

        // 处理积分相关日志
        handlePointsLogs(dto);

        // 积分使用数据库原子增减，避免并发赠分被本次请求加载的旧 user.points 覆盖。
        if (dto.getPoints() != null) {
            applyPointsChange(user.getUserId(), dto.getPoints(), dto.getTypePoints());
        }
        // 即使本次仅调整余额，也不能将加载时的旧积分回写覆盖并发变更。
        updatedUser.setPoints(null);

        // 更新用户信息
        this.updateById(updatedUser);
    }

    private void applyPointsChange(Integer userId, Integer points, Integer pointsType) {
        LambdaUpdateWrapper<User> pointsUpdate = new LambdaUpdateWrapper<User>()
                .eq(User::getUserId, userId);
        if (pointsType == 1) {
            pointsUpdate.setIncrBy(User::getPoints, points);
        } else {
            pointsUpdate.ge(User::getPoints, points)
                    .setDecrBy(User::getPoints, points);
        }
        if (!this.update(pointsUpdate)) {
            String configVal = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
            throw new GlobalException("用户" + configVal + (pointsType == 1 ? "增加失败" : "不足"));
        }
    }

    @Override
    public void sendMobileCodeByMobileValidate(RegisterSmsDTO dto, Integer userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(messageHelper.get("error.member.not.exist"));
        }
        dto.setMobile(user.getMobile());
        //发送短信
        configService.sendSms(dto);
    }

    @Override
    public void sendEmailCodeByEmailValidate(RegisterEmailDTO dto, Integer userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(messageHelper.get("error.member.not.exist"));
        }
        dto.setEmail(user.getEmail());
        //发送短信
        configService.sendEmail(dto);
    }

    @Override
    public void sendMobileCodeByModifyMobile(RegisterSmsDTO dto, Integer userId) {
        if (dto.getMobile() == null || StringUtils.isEmpty(dto.getMobile())) {
            throw new GlobalException(messageHelper.get("error.user.mobile.required"));
        }
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(messageHelper.get("error.member.not.exist"));
        }

        //发送短信
        configService.sendSms(dto);
    }

    @Override
    public void sendEmailCodeByModifyEmail(RegisterEmailDTO dto, Integer userId) {
        if (dto.getEmail() == null || StringUtils.isEmpty(dto.getEmail())) {
            throw new GlobalException(messageHelper.get("error.user.email.required"));
        }
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(messageHelper.get("error.member.not.exist"));
        }

        //发送短信
        configService.sendEmail(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String bindMobile(BindMobileDTO dto) {
        String mobile = dto.getMobile();
        validateVerificationCode("login" + mobile, dto.getMobileCode());

        // 获取openData中的openid
        Map<String, Object> openData = dto.getOpenData();
        // 用于处理前端多套了一层openData
        if (ObjectUtil.isNotEmpty(openData) && openData.containsKey("openData")) {
            openData = (Map<String, Object>) openData.get("openData");
        }

        String openid = null;
        String unionid = null;
        if (ObjectUtil.isNotEmpty(openData) && ObjectUtil.isNotEmpty(openData.get("openid"))) {
            openid = openData.get("openid") != null ? String.valueOf(openData.get("openid")) : null;
            unionid = openData.get("unionid") != null ? String.valueOf(openData.get("unionid")) : null;
            openid = StringUtils.trim(openid);
            unionid = StringUtils.trim(unionid);
        }

        // 查询手机号是否已被注册
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getMobile, mobile));
        boolean isExistingUser = (user != null);

        if (!isExistingUser) {
            // 手机号未被注册，创建新用户
            String redCode = configService.getConfigByCode(SettingsEnum.SHOP_REG_CLOSED.getBizCode()).getBizVal();
            if (StrUtil.equals(redCode, "1")) {
                throw new GlobalException("系统已关闭注册");
            }

            // 从openData中获取微信用户信息（头像、昵称等）
            String randomUserName = generateUniqueUsername();
            String nickname = openData != null && openData.get("nickname") != null
                    ? String.valueOf(openData.get("nickname")) : StrUtil.format("user_{}", randomUserName);
            String avatar = openData != null && openData.get("headimgurl") != null
                    ? String.valueOf(openData.get("headimgurl")) : "";

            user = new User();
            user.setUsername(randomUserName);
            user.setNickname(nickname);
            user.setAvatar(avatar);
            user.setMobile(mobile);
            user.setMobileValidated(1);
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setReferrerUserId(dto.getReferrerUserId());
            user.setRegTime(StringUtils.getCurrentTime());
            this.save(user);

            wholesalerService.autoRegisterIfEnabled(user.getUserId());
        } else {
            // 老用户，更新手机验证状态
            user.setMobileValidated(1);
            this.updateById(user);
        }

        // 处理微信授权绑定
        if (StrUtil.isNotBlank(openid)) {
            // 获取授权类型（公众号或小程序）
            String clientType = HeaderUtils.getClientType();
            Integer authorizeType = AuthorizeTypeEnum.getAuthorizeType(clientType);
            // 微信H5默认为公众号类型
            if ("wechat".equals(clientType)) {
                authorizeType = AuthorizeTypeEnum.WECHAT.getCode();
            }

            // 查询该openid是否已有授权记录
            UserAuthorize existingAuthorize = userAuthorizeService.getOne(
                    new LambdaQueryWrapper<UserAuthorize>()
                            .eq(UserAuthorize::getOpenId, openid)
                            .eq(UserAuthorize::getAuthorizeType, authorizeType)
            );

            if (existingAuthorize != null) {
                // 该openid已被其他用户绑定，需要更新为当前用户
                // 这种情况发生在：老用户换新微信号登录时，使用已注册手机号绑定
                existingAuthorize.setUserId(user.getUserId());
                existingAuthorize.setUnionid(unionid);
                // 移除nickname防止特殊字符
                if (openData != null) {
                    openData.remove("nickname");
                }
                existingAuthorize.setOpenData(JSON.toJSONString(openData));
                userAuthorizeService.updateById(existingAuthorize);
            } else {
                // 查询当前用户是否已有该类型的授权记录（同一类型只保留一个）
                UserAuthorize userExistingAuthorize = userAuthorizeService.getOne(
                        new LambdaQueryWrapper<UserAuthorize>()
                                .eq(UserAuthorize::getUserId, user.getUserId())
                                .eq(UserAuthorize::getAuthorizeType, authorizeType)
                );

                if (userExistingAuthorize != null) {
                    // 用户已有该类型授权，更新openid（老用户换新微信场景）
                    userExistingAuthorize.setOpenId(openid);
                    userExistingAuthorize.setUnionid(unionid);
                    // 移除nickname防止特殊字符
                    if (openData != null) {
                        openData.remove("nickname");
                    }
                    userExistingAuthorize.setOpenData(JSON.toJSONString(openData));
                    userAuthorizeService.updateById(userExistingAuthorize);
                } else {
                    // 创建新的授权记录
                    UserAuthorize newAuthorize = new UserAuthorize();
                    newAuthorize.setUserId(user.getUserId());
                    newAuthorize.setOpenId(openid);
                    // 移除nickname防止特殊字符
                    if (openData != null) {
                        openData.remove("nickname");
                    }
                    newAuthorize.setOpenData(JSON.toJSONString(openData));
                    newAuthorize.setUnionid(unionid);
                    newAuthorize.setAddTime(StringUtils.getCurrentTime());
                    newAuthorize.setAuthorizeType(authorizeType);
                    userAuthorizeService.save(newAuthorize);
                }
            }
        }

        // 将token存入Redis并返回
        String token = jwtUtil.generateToken(user.getUserId(), false, user.getUsername());
        redisCache.setCacheObject(Constants.getLoginTokenFormat(token, user.getUsername()), user.getUsername(), 30, TimeUnit.DAYS);
        return token;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginWechatEventVO wechatEvent(WechatEventDTO dto) {
        Assert.isTrue(StrUtil.isNotBlank(dto.getKey()), () -> new GlobalException("未登录"));
        // 根据 key 从 cache 中拿 openid
        String openid = (String) redisTemplate.opsForValue().get(dto.getKey());
        Assert.isTrue(StrUtil.isNotBlank(openid), () -> new GlobalException("未登录"));

        // 同一 openid 的扫码回调可能被前端重复轮询，使用短期分布式锁串行化
        // “查询授权记录 → 创建用户 → 创建授权记录”整个过程。
        String lockKey = "wechat:register:lock:" + openid;
        String lockValue = UUID.randomUUID().toString();
        if (!redisCache.tryLock(lockKey, lockValue, 30, TimeUnit.SECONDS)) {
            throw new GlobalException("登录处理中，请稍后重试");
        }

        boolean unlockAfterTransaction = registerUnlockAfterTransaction(lockKey, lockValue);
        try {
            // 必须在成功加锁后再次查询，避免并发请求都使用加锁前的旧查询结果。
            UserAuthorize userAuthorize = userAuthorizeService.lambdaQuery()
                    .eq(UserAuthorize::getOpenId, openid).one();
            if (userAuthorize == null) {
                ConfigPO shopRegClosedConfig = configService.getConfigByCode(SettingsEnum.SHOP_REG_CLOSED.getBizCode());
                int shopRegClosed = Integer.parseInt(shopRegClosedConfig.getBizVal());

                ConfigPO wechatOauthConfig = configService.getConfigByCode(SettingsEnum.OPEN_WECHAT_OAUTH.getBizCode());
                int wechatOauth = Integer.parseInt(wechatOauthConfig.getBizVal());

                ConfigPO openWechatRegisterConfig = configService.getConfigByCode(SettingsEnum.OPEN_WECHAT_REGISTER.getBizCode());
                int openWechatRegister = Integer.parseInt(openWechatRegisterConfig.getBizVal());

                // 判断是否开启微信注册
                if (Constants.NO == shopRegClosed && Constants.YES == wechatOauth && Constants.YES == openWechatRegister) {
                    // 注册用户
                    String randomStr = generateUniqueUsername();
                    User user = User.builder()
                            .username(randomStr)
                            .nickname(StrUtil.format("user_{}", randomStr))
                            .password(RandomUtil.randomString(6))
                            .regTime(StringUtils.getCurrentTime())
                            .build();
                    this.save(user);

                    wholesalerService.autoRegisterIfEnabled(user.getUserId());

                    // 绑定第三方账号信息
                    userAuthorize = UserAuthorize.builder()
                            .authorizeType(AuthorizeTypeEnum.WECHAT.getCode())
                            .userId(user.getUserId())
                            .openId(openid)
                            .openData("{}")
                            .addTime(StringUtils.getCurrentTime())
                            .build();
                    userAuthorizeService.save(userAuthorize);
                } else {
                    Map<String, Map<String, String>> openData = Map.of("openData", Map.of("openid", openid));
                    return LoginWechatEventVO.builder()
                            .type(2)
                            .openData(openData)
                            .build();
                }
            }

            User user = this.getById(userAuthorize.getUserId());
            String token = jwtUtil.generateToken(user.getUserId(), false, user.getUsername());

            // 将token存入Redis中，key为 ADMIN_TOKEN + username + ":" + token
            redisCache.setCacheObject(Constants.getLoginTokenFormat(token,user.getUsername()), user.getUsername(), 30, TimeUnit.DAYS);
            return LoginWechatEventVO.builder()
                    .type(1)
                    .token(token)
                    .build();
        } finally {
            if (!unlockAfterTransaction) {
                redisCache.unlock(lockKey, lockValue);
            }
        }
    }

    private boolean registerUnlockAfterTransaction(String lockKey, String lockValue) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return false;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                redisCache.unlock(lockKey, lockValue);
            }
        });
        return true;
    }

    @Override
    public Page<?> userFundDetail(UserFundDetailPageQuery pageQuery) {
        return switch (pageQuery.getFromTag()) {
            case 3 -> getUserGrowthPointsLogVOPage(pageQuery);
            case 4 -> getUserPointsLogVOPage(pageQuery);
            default -> getUserBalanceLogVOPage(pageQuery);
        };
    }

    @Override
    public String getMobile(LoginGetMobileParam param) {
        String accessToken = wechatOAuthService.getMiniAccessToken();
        Assert.notBlank(accessToken, () -> new GlobalException("获取token失败"));

        // 获取小程序用户手机号
        String url = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + accessToken;

        // 构建请求数据
        JSONObject data = new JSONObject();
        data.put("code", param.getCode());

        // 发送POST请求并获取响应
        String response = HttpUtil.post(url, data.toString());
        JSONObject result = JSON.parseObject(response);

        Integer errcode = result.getInteger("errcode");
        Assert.isTrue(errcode != null && errcode == 0, () -> new GlobalException(result.getString("errmsg")));
        JSONObject phoneInfo = result.getJSONObject("phone_info");

        //用户绑定的手机号（国外手机号会有区号）
        String phoneNumber = phoneInfo.getString("phoneNumber");
        //没有区号的手机号
        String purePhoneNumber = phoneInfo.getString("purePhoneNumber");
        //区号
        String countryCode = phoneInfo.getString("countryCode");

        String mobile;
        ConfigPO isOpenMobileAreaCodeConfig = configService.getConfigByCode(SettingsEnum.IS_OPEN_MOBILE_AREA_CODE.getBizCode());
        if(isOpenMobileAreaCodeConfig==null || !Objects.equals(isOpenMobileAreaCodeConfig.getBizVal(),"1")){
            //不开启区号处理
            mobile = purePhoneNumber;
        }else{
            //开启区号
            if (StrUtil.isNotBlank(countryCode) && StrUtil.isNotBlank(purePhoneNumber)) {
                //区号+手机号
                mobile = countryCode + purePhoneNumber;
            } else if (StrUtil.isNotBlank(phoneNumber)) {
                //国外手机的区号
                mobile = phoneNumber;
            } else {
                //兼容(直接用手机号)
                mobile = purePhoneNumber;
            }
        }
        if (StrUtil.isNotBlank(mobile) && StrUtil.startWith(mobile, "+")) {
            mobile = mobile.substring(1);
        }

        // 根据用户手机号注册用户
        User user = this.getUserByMobile(mobile);
        if (user == null && StrUtil.isNotBlank(purePhoneNumber) && !StrUtil.equals(mobile, purePhoneNumber)) {
            user = this.getUserByMobile(purePhoneNumber);
        }
        if (user == null) {
            ConfigPO shopRegClosedConfig = configService.getConfigByCode(SettingsEnum.SHOP_REG_CLOSED.getBizCode());
            Assert.isTrue("0".equals(shopRegClosedConfig.getBizVal()), () -> new GlobalException("商城已停止注册"));

            ConfigPO wechatOauthConfig = configService.getConfigByCode(SettingsEnum.OPEN_WECHAT_OAUTH.getBizCode());
            Assert.isTrue("1".equals(wechatOauthConfig.getBizVal()), () -> new GlobalException("公众号授权登录未开启"));

            ConfigPO openWechatRegisterConfig = configService.getConfigByCode(SettingsEnum.OPEN_WECHAT_REGISTER.getBizCode());
            Assert.isTrue("1".equals(openWechatRegisterConfig.getBizVal()), () -> new GlobalException("微信注册未开启"));

            String randomUserName = generateUniqueUsername();
            String password = RandomUtil.randomNumbers(8);

            user = User.builder()
                    .username(randomUserName)
                    .mobile(mobile)
                    .email("")
                    .password(passwordEncoder.encode(password))
                    .avatar("")
                    .nickname(StrUtil.format("user_{}", randomUserName))
                    .regTime(StringUtils.getCurrentTime())
                    .referrerUserId(0)
                    .mobileValidated(1)
                    .emailValidated(0)
                    .build();
            this.save(user);

            wholesalerService.autoRegisterIfEnabled(user.getUserId());
        }
        // 返回登录token
        String token = jwtUtil.generateToken(user.getUserId(), false, user.getUsername());
        redisCache.setCacheObject(Constants.getLoginTokenFormat(token,user.getUsername()), user.getUsername(), 30, TimeUnit.DAYS);
        return token;
    }

    @Override
    public void updateUserOpenId(LoginUpdateUserOpenIdParam param) {
        // 获取微信配置
        String appId = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_APP_ID.getBizCode()).getBizVal();
        String appSecret = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_SECRET.getBizCode()).getBizVal();
        String result = wechatApiClient.code2Session(appId, appSecret, param.getCode(), "authorization_code");
        Code2SessionResult code2SessionResult = JsonUtil.fromJson(result, Code2SessionResult.class);
        // 错误code -1,40029，45011,40226
        if (code2SessionResult.getErrcode() != null && code2SessionResult.getErrcode() != 0) {
            throw new GlobalException(code2SessionResult.getErrmsg());
        }

        String openid = code2SessionResult.getOpenid();
        if (StrUtil.isNotBlank(openid)) {
            UserAuthorize userAuthorize = userAuthorizeService.lambdaQuery().eq(UserAuthorize::getOpenId, openid).one();
            Integer userId = getCurrentUserId();
            if (userAuthorize != null) {
                userAuthorize.setUserId(userId);
                userAuthorize.setAuthorizeType(AuthorizeTypeEnum.MINI_PROGRAM.getCode());
                userAuthorizeService.updateById(userAuthorize);
            } else {
                userAuthorize = UserAuthorize.builder()
                        .authorizeType(AuthorizeTypeEnum.MINI_PROGRAM.getCode())
                        .userId(userId)
                        .openId(openid)
                        .openData(JsonUtil.toJson(code2SessionResult))
                        .addTime(StringUtils.getCurrentTime())
                        .build();
                userAuthorizeService.save(userAuthorize);
            }
        }

    }

    @Transactional
    @Override
    public WxLoginInfoByCodeVO getWxLoginInfoByCode(String code) throws WxErrorException {
        String openid;
        String unionid;
        String nickname;
        String headImgUrl;

        // 检查是否开启模拟模式
        boolean isMockMode = false;
        if (isMockMode) {
            // ========== 模拟模式：不调用微信API，直接生成模拟数据 ==========
            // 使用code生成稳定的模拟openid（相同code返回相同openid，方便测试）
            openid = "mock_openid_" + SecureUtil.md5(code).substring(0, 16);
            unionid = "mock_unionid_" + SecureUtil.md5(code).substring(0, 16);
            nickname = "模拟用户_" + code.substring(0, Math.min(4, code.length()));
            headImgUrl = "https://thirdwx.qlogo.cn/mmopen/mock_avatar.png";
        } else {
            // ========== 正式模式：调用微信API获取用户信息 ==========
            WxMpDefaultConfigImpl configStorage = new WxMpDefaultConfigImpl();
            configStorage.setAppId(configService.getConfigVal(SettingsEnum.WECHAT_APP_ID));
            configStorage.setSecret(configService.getConfigVal(SettingsEnum.WECHAT_APP_SECRET));
            configStorage.setToken(configService.getConfigVal(SettingsEnum.WECHAT_SERVER_TOKEN));
            configStorage.setAesKey(configService.getConfigVal(SettingsEnum.WECHAT_SERVER_SECRET));
            WxMpService service = new WxMpServiceImpl();
            service.setWxMpConfigStorage(configStorage);

            // 使用 WxJava 获取 access_token
            WxOAuth2AccessToken accessToken = service.getOAuth2Service().getAccessToken(code);
            openid = accessToken.getOpenId();
            unionid = accessToken.getUnionId();

            // 获取用户信息
            WxOAuth2UserInfo userInfo = service.getOAuth2Service().getUserInfo(accessToken, "zh_CN");
            nickname = userInfo.getNickname();
            headImgUrl = userInfo.getHeadImgUrl();
        }

        // 3. 查询是否已有该openid的授权记录（老用户直接登录）
        UserAuthorize userAuthorize = userAuthorizeService.lambdaQuery()
                .eq(UserAuthorize::getOpenId, openid)
                .eq(UserAuthorize::getAuthorizeType, AuthorizeTypeEnum.WECHAT.getCode())
                .one();

        if (userAuthorize != null) {
            User user = this.getById(userAuthorize.getUserId());
            String token = jwtUtil.generateToken(user.getUserId(), false, user.getUsername());
            // 将token存入Redis中，key为 ADMIN_TOKEN + username + ":" + token
            redisCache.setCacheObject(Constants.getLoginTokenFormat(token, user.getUsername()), user.getUsername(), 30, TimeUnit.DAYS);
            return WxLoginInfoByCodeVO.builder()
                    .type(1)
                    .token(token)
                    .build();
        }

        // 4. 新用户，检查配置开关
        ConfigPO shopRegClosedConfig = configService.getConfigByCode(SettingsEnum.SHOP_REG_CLOSED.getBizCode());
        Assert.isTrue("0".equals(shopRegClosedConfig.getBizVal()), () -> new GlobalException("商城已停止注册"));

        ConfigPO wechatOauthConfig = configService.getConfigByCode(SettingsEnum.OPEN_WECHAT_OAUTH.getBizCode());
        Assert.isTrue("1".equals(wechatOauthConfig.getBizVal()), () -> new GlobalException("公众号授权登录未开启"));

        ConfigPO openWechatRegisterConfig = configService.getConfigByCode(SettingsEnum.OPEN_WECHAT_REGISTER.getBizCode());
        Assert.isTrue("1".equals(openWechatRegisterConfig.getBizVal()), () -> new GlobalException("微信注册未开启"));

        // 5. 检查是否需要绑定手机号
        ConfigPO wechatRegisterBindPhoneConfig = configService.getConfigByCode(SettingsEnum.WECHAT_REGISTER_BIND_PHONE.getBizCode());
        boolean needBindPhone = "1".equals(wechatRegisterBindPhoneConfig.getBizVal());

        if (needBindPhone) {
            // 需要绑定手机号，返回type=2和openData，让前端跳转到绑定手机号页面
            Map<String, Object> openData = new HashMap<>();
            openData.put("openid", openid);
            openData.put("unionid", unionid);
            openData.put("nickname", nickname);
            openData.put("headimgurl", headImgUrl);
            openData.put("mockMode", isMockMode); // 标记是否为模拟模式

            return WxLoginInfoByCodeVO.builder()
                    .type(2)
                    .openData(openData)
                    .build();
        }

        // 6. 不需要绑定手机号，直接创建用户并登录
        String randomUserName = generateUniqueUsername();
        String password = RandomUtil.randomNumbers(8);

        User user = User.builder()
                .username(randomUserName)
                .mobile("")
                .email("")
                .password(passwordEncoder.encode(password))
                .avatar(headImgUrl != null ? headImgUrl : "")
                .nickname(nickname != null ? nickname : StrUtil.format("user_{}", randomUserName))
                .regTime(StringUtils.getCurrentTime())
                .referrerUserId(0)
                .mobileValidated(0)
                .emailValidated(0)
                .build();
        this.save(user);

        wholesalerService.autoRegisterIfEnabled(user.getUserId());

        userAuthorize = UserAuthorize.builder()
                .authorizeType(AuthorizeTypeEnum.WECHAT.getCode())
                .userId(user.getUserId())
                .openId(openid)
                .unionid(unionid)
                .openData(isMockMode ? "{\"mockMode\":true}" : "{}")
                .addTime(StringUtils.getCurrentTime())
                .build();
        userAuthorizeService.save(userAuthorize);

        String token = jwtUtil.generateToken(user.getUserId(), false, user.getUsername());
        // 将token存入Redis中
        redisCache.setCacheObject(Constants.getLoginTokenFormat(token, user.getUsername()), user.getUsername(), 30, TimeUnit.DAYS);
        return WxLoginInfoByCodeVO.builder()
                .type(1)
                .token(token)
                .build();
    }

    @Override
    public void clientLogout() {
        String username = SecurityUtils.getCurrentUsername();
        String token = HeaderUtils.getHeaderValue(JwtUtil.HEADER);
        if(Objects.nonNull(token)){
            token = token.replaceAll(Constants.TOKEN_PREFIX, "");
            redisTemplate.delete( Constants.getLoginTokenFormat(token,username));
        }
    }

    @Override
    public void adminLogout() {
        String username = SecurityUtils.getCurrentUsername();
        String token = HeaderUtils.getHeaderValue(JwtUtil.HEADER);
        if(Objects.nonNull(token)){
            token = token.replaceAll(Constants.TOKEN_PREFIX, "");
            redisTemplate.delete(Constants.getLoginTokenFormat(token,username));
        }
    }

    @NotNull
    private Page<UserPointsLogVO> getUserPointsLogVOPage(UserFundDetailPageQuery pageQuery) {
        Page<UserPointsLog> userPointsLogPage = userPointsLogMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery.getSize()),
                Wrappers.lambdaQuery(UserPointsLog.class)
                        .eq(UserPointsLog::getUserId, pageQuery.getUserId())
                        .orderByDesc(UserPointsLog::getChangeTime)
        );

        Page<UserPointsLogVO> resultPage = new Page<>();
        BeanUtil.copyProperties(userPointsLogPage, resultPage);

        List<UserPointsLog> records = userPointsLogPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return resultPage;
        }

        List<UserPointsLogVO> resultRecords = records.stream()
                .map(UserPointsLogVO::new)
                .toList();
        resultPage.setRecords(resultRecords);

        return resultPage;
    }

    @NotNull
    private Page<UserGrowthPointsLogVO> getUserGrowthPointsLogVOPage(UserFundDetailPageQuery pageQuery) {
        Page<UserGrowthPointsLog> userGrowthPointsLogPage = userGrowthPointsLogMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery.getSize()),
                Wrappers.lambdaQuery(UserGrowthPointsLog.class)
                        .eq(UserGrowthPointsLog::getUserId, pageQuery.getUserId())
                        .orderByDesc(UserGrowthPointsLog::getChangeTime)
        );

        Page<UserGrowthPointsLogVO> resultPage = new Page<>();
        BeanUtil.copyProperties(userGrowthPointsLogPage, resultPage);

        List<UserGrowthPointsLog> records = userGrowthPointsLogPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return resultPage;
        }

        List<UserGrowthPointsLogVO> resultRecords = records.stream()
                .map(UserGrowthPointsLogVO::new)
                .toList();
        resultPage.setRecords(resultRecords);

        return resultPage;
    }

    @NotNull
    private Page<UserBalanceLogVO> getUserBalanceLogVOPage(UserFundDetailPageQuery pageQuery) {
        User user = this.getById(pageQuery.getUserId());

        Page<UserBalanceLog> userBalanceLogPage = userBalanceLogService.lambdaQuery()
                .and(qw -> {
                    qw.gt(UserBalanceLog::getBalance, 0)
                            .or()
                            .gt(UserBalanceLog::getFrozenBalance, 0);
                })
                .eq(UserBalanceLog::getUserId, pageQuery.getUserId())
                .orderByDesc(UserBalanceLog::getChangeTime)
                .page(new Page<>(pageQuery.getPage(), pageQuery.getSize()));

        Page<UserBalanceLogVO> resultPage = new Page<>();
        BeanUtil.copyProperties(userBalanceLogPage, resultPage);

        List<UserBalanceLog> records = userBalanceLogPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return resultPage;
        }

        List<UserBalanceLogVO> resultRecords = records.stream()
                .map(item -> {
                    UserBalanceLogVO vo = new UserBalanceLogVO(item, user);
                    vo.setChangeTypeName(messageHelper.get(resolveBalanceChangeTypeKey(item.getChangeType())));
                    return vo;
                })
                .toList();
        resultPage.setRecords(resultRecords);

        return resultPage;
    }

    /**
     * 余额明细类型文案 key，分支与 {@link UserBalanceLogVO} 构造器中的中文映射保持一致。
     */
    private static String resolveBalanceChangeTypeKey(Integer changeType) {
        if (changeType == null) {
            return "balance.log.change.type.unknown";
        }
        return switch (changeType) {
            case 1 -> "balance.log.change.type.increase";
            case 2 -> "balance.log.change.type.decrease";
            case 99 -> "balance.log.change.type.other";
            default -> "balance.log.change.type.unknown";
        };
    }

    private void handleBalanceLogs(UserFundManagementDTO dto, User user) {
        if (dto.getBalance() != null && dto.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            saveUserBalanceLog(dto, user.getBalance(), dto.getBalance(), dto.getTypeBalance(), user.getFrozenBalance(), null, null);
        }

        if (dto.getFrozenBalance() != null && dto.getFrozenBalance().compareTo(BigDecimal.ZERO) != 0) {
            saveUserBalanceLog(dto, user.getBalance(), null, null, user.getFrozenBalance(), dto.getFrozenBalance(), dto.getTypeFrozenBalance());

            // 冻结余额变动时，生成对应的余额日志
            BigDecimal frozenBalance = dto.getFrozenBalance();
            int type = dto.getTypeFrozenBalance();
            saveUserBalanceLog(dto, user.getBalance(), frozenBalance, type == 1 ? 2 : 1, user.getFrozenBalance(), null, null);
        }
    }

    private void handlePointsLogs(UserFundManagementDTO dto) {
        if (dto.getPoints() != null) {
            saveUserPointsLog(dto, dto.getPoints(), dto.getTypePoints());
        }

        if (dto.getGrowthPoints() != null) {
            saveUserGrowthPointsLog(dto, dto.getGrowthPoints(), dto.getTypeGrowthPoints());
        }
    }

    private void saveUserBalanceLog(UserFundManagementDTO dto, BigDecimal beforeBalance, BigDecimal balance, Integer balanceType, BigDecimal beforeFrozenBalance, BigDecimal frozenBalance, Integer frozenBalanceType) {
        BigDecimal afterBalance = beforeBalance;
        if (balanceType != null) {
            afterBalance = balanceType == 1 ? beforeBalance.add(balance) : beforeBalance.subtract(balance);
        }
        BigDecimal afterFrozenBalance = beforeFrozenBalance;
        if (frozenBalanceType != null) {
            afterFrozenBalance = frozenBalanceType == 1 ? beforeFrozenBalance.add(frozenBalance) : beforeFrozenBalance.subtract(frozenBalance);
        }

        UserBalanceLog log = UserBalanceLog.builder()
                .userId(dto.getId())
                .changeDesc(dto.getChangeDesc())
                .beforeBalance(beforeBalance)
                .balance(balance)
                .afterBalance(afterBalance)
                .frozenBalance(frozenBalance)
                .beforeFrozenBalance(beforeFrozenBalance)
                .afterFrozenBalance(afterFrozenBalance)
                .changeType(balanceType != null ? balanceType : frozenBalanceType)
                .changeTime(StringUtils.getCurrentTime())
                .build();
        userBalanceLogService.save(log);
    }

    private void saveUserPointsLog(UserFundManagementDTO dto, Integer points, Integer type) {
        // 剔除后台添加余额的情况
        if (points != 0) {
            UserPointsLog log = UserPointsLog.builder()
                    .userId(dto.getId())
                    .changeDesc(dto.getChangeDesc())
                    .points(points)
                    .changeType(type)
                    .changeTime(StringUtils.getCurrentTime())
                    .build();
            userPointsLogMapper.insert(log);
        }
    }

    private void saveUserGrowthPointsLog(UserFundManagementDTO dto, Integer growthPoints, Integer type) {
        // 剔除后台添加余额的情况
        if (growthPoints != 0) {
            UserGrowthPointsLog log = UserGrowthPointsLog.builder()
                    .userId(dto.getId())
                    .changeDesc(dto.getChangeDesc())
                    .points(growthPoints)
                    .changeType(type)
                    .changeTime(StringUtils.getCurrentTime())
                    .build();
            userGrowthPointsLogMapper.insert(log);

            // 发送升级消息
            if (tigshopProperties.getIsPro() != 1) {
                return;
            }
            rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE, RabbitMQConfig.VIP_UPDATE_ROUTING_KEY, dto.getId());
        }
    }

    private void updateUserFunds(User user, UserFundManagementDTO dto) {
        if (dto.getBalance() != null) {
            if (dto.getTypeBalance() == 1) {
                BigDecimal balance = user.getBalance().add(dto.getBalance());
                user.setBalance(balance);
            } else {
                Assert.isTrue(user.getBalance().compareTo(dto.getBalance()) >= 0, () -> new GlobalException("用户余额不足"));
                BigDecimal balance = user.getBalance().subtract(dto.getBalance());
                user.setBalance(balance);
            }

        }

        if (dto.getGrowthPoints() != null) {
            if (dto.getTypeGrowthPoints() == 1) {
                user.setGrowthPoints(user.getGrowthPoints() + dto.getGrowthPoints());
            } else {
                Assert.isTrue(user.getGrowthPoints() >= dto.getGrowthPoints(), () -> new GlobalException("用户成长积分不足"));
                user.setGrowthPoints(user.getGrowthPoints() - dto.getGrowthPoints());
            }
        }

        if (dto.getFrozenBalance() != null) {
            if (dto.getTypeFrozenBalance() == 1) {
                Assert.isTrue(user.getBalance().compareTo(dto.getFrozenBalance()) >= 0,
                        () -> new GlobalException("可用余额不足，无法冻结"));
                user.setFrozenBalance(user.getFrozenBalance().add(dto.getFrozenBalance()));
                user.setBalance(user.getBalance().subtract(dto.getFrozenBalance()));
            } else {
                Assert.isTrue(user.getFrozenBalance().compareTo(dto.getFrozenBalance()) >= 0,
                        () -> new GlobalException("冻结余额不足，无法解冻"));
                user.setFrozenBalance(user.getFrozenBalance().subtract(dto.getFrozenBalance()));
                user.setBalance(user.getBalance().add(dto.getFrozenBalance()));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeUser(int userId) {
        if (userId > 0) {
            this.update(Wrappers.lambdaUpdate(User.class).set(User::getStatus, 0).eq(User::getUserId, userId));
        }
    }

    @Override
    @SneakyThrows
    public void bindWechat(BindWechatParam param) {
        String code = param.getCode();
        Assert.isTrue(StringUtils.isNotBlank(code), () -> new GlobalException("code不能为空"));

        UserAuthorize userAuthorize = userAuthorizeService.getOne(
                new LambdaQueryWrapper<UserAuthorize>().eq(UserAuthorize::getUserId, getCurrentUserId()));
        //检测用户是否有授权，有则不进行授权
        if (userAuthorize != null) {
            Assert.isTrue(userAuthorize.getOpenId() == null, () -> new GlobalException("该账号已绑定微信公众号"));
        }

        // 获取微信配置
        WxMpDefaultConfigImpl configStorage = new WxMpDefaultConfigImpl();
        configStorage.setAppId(configService.getConfigVal(SettingsEnum.WECHAT_APP_ID));
        configStorage.setSecret(configService.getConfigVal(SettingsEnum.WECHAT_APP_SECRET));
        configStorage.setToken(configService.getConfigVal(SettingsEnum.WECHAT_SERVER_TOKEN));
        configStorage.setAesKey(configService.getConfigVal(SettingsEnum.WECHAT_SERVER_SECRET));
        WxMpService service = new WxMpServiceImpl();
        service.setWxMpConfigStorage(configStorage);
        // 使用 WxJava 获取 access_token
        WxOAuth2AccessToken accessToken = service.getOAuth2Service().getAccessToken(code);
        String openId = accessToken.getOpenId();
        Assert.notNull(openId, () -> new GlobalException("该账号异常"));

        UserAuthorize userAuthorizeByOpenId = userAuthorizeService.getOne(
                new LambdaQueryWrapper<UserAuthorize>().eq(UserAuthorize::getOpenId, openId));

        if (userAuthorizeByOpenId != null) {
            Integer userId = userAuthorizeByOpenId.getUserId();
            Assert.isTrue(userId != null && userId.equals(getCurrentUserId()),
                    () -> new GlobalException("该微信号已绑定其他账号，请解绑后再重试"));
        }

        userAuthorize = UserAuthorize.builder()
                .authorizeType(AuthorizeTypeEnum.WECHAT.getCode())
                .userId(getCurrentUserId())
                .openId(openId)
                .openData(JsonUtil.toJson(accessToken))
                .addTime(StringUtils.getCurrentTime())
                .build();
        userAuthorizeService.save(userAuthorize);
    }

    @Override
    public void unbindWechat() {
        UserAuthorize userAuthorize = userAuthorizeService.getOne(
                new LambdaQueryWrapper<UserAuthorize>().eq(UserAuthorize::getUserId, getCurrentUserId()));
        String openId = userAuthorize.getOpenId();
        Assert.notNull(openId, () -> new GlobalException("该账号未绑定微信公众号"));
        userAuthorizeService.remove(new LambdaQueryWrapper<UserAuthorize>().eq(UserAuthorize::getOpenId, openId));
    }

    /**
     * 下载会员导入模板
     */
    @Override
    public void downloadUserImportTemplate(HttpServletResponse response) {
        java.util.List<UserImportExcelDTO> templateList = java.util.List.of();
        ExcelUtils<UserImportExcelDTO> excelUtils = new ExcelUtils<>(templateList, UserImportExcelDTO.class);
        excelUtils.exportExcelWeb(response);
    }

    /**
     * 批量导入会员
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<UserImportSuccessListVO> batchImportUser(MultipartFile file) {
        Assert.notNull(file, () -> new GlobalException("请上传文件"));
//        Assert.isTrue(Objects.requireNonNull(file.getOriginalFilename()).endsWith(".xlsx"),
//                () -> new GlobalException("请上传.xlsx格式文件"));

        ExcelUtils<UserImportExcelDTO> excelUtils = new ExcelUtils<>(file, UserImportExcelDTO.class);
        List<UserImportExcelDTO> userImportList = excelUtils.importExcel();
        Assert.isTrue(CollUtil.isNotEmpty(userImportList), () -> new GlobalException("表格数据为空"));

        // 校验导入数据
        List<UserImportErrorExcelVO> errorList = checkUserImportData(userImportList);
        if (CollUtil.isNotEmpty(errorList)) {
            throw new GlobalException("导入失败:共导入" + userImportList.size() + "个,失败" + errorList.size() + "个");
        }

        // 批量创建会员
        List<UserImportSuccessListVO> successList = new java.util.ArrayList<>();
        for (UserImportExcelDTO importDTO : userImportList) {
            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(importDTO.getUsername());
            userDTO.setMobile(importDTO.getMobile());
            userDTO.setEmail(importDTO.getEmail());
            userDTO.setNickname(importDTO.getNickname());
            userDTO.setPassword(importDTO.getPassword());
            userDTO.setBirthday(importDTO.getBirthday());
            userDTO.setFromTag(FromTag.ADMIN_ADD.getCode());

            // 处理会员等级ID
            if (StrUtil.isNotEmpty(importDTO.getRankId())) {
                try {
                    userDTO.setRankId(Integer.parseInt(importDTO.getRankId()));
                } catch (NumberFormatException e) {
                    // 如果转换失败，使用默认值
                    userDTO.setRankId(0);
                }
            }

            // 处理余额
            if (StrUtil.isNotEmpty(importDTO.getBalance())) {
                try {
                    userDTO.setBalance(new BigDecimal(importDTO.getBalance()));
                } catch (NumberFormatException e) {
                    userDTO.setBalance(java.math.BigDecimal.ZERO);
                }
            }

            // 处理积分
            if (StrUtil.isNotEmpty(importDTO.getPoints())) {
                try {
                    userDTO.setPoints(Integer.parseInt(importDTO.getPoints()));
                } catch (NumberFormatException e) {
                    userDTO.setPoints(0);
                }
            }

            // 处理成长积分
            if (StrUtil.isNotEmpty(importDTO.getGrowthPoints())) {
                try {
                    userDTO.setGrowthPoints(Integer.parseInt(importDTO.getGrowthPoints()));
                } catch (NumberFormatException e) {
                    userDTO.setGrowthPoints(0);
                }
            }

            // 创建会员
            User createdUser = this.create(userDTO);

            // 构建成功返回对象
            UserImportSuccessListVO successVO = new UserImportSuccessListVO();
            successVO.setUserId(createdUser.getUserId());
            successVO.setUsername(createdUser.getUsername());
            successVO.setMobile(createdUser.getMobile());
            successVO.setEmail(createdUser.getEmail());
            successVO.setNickname(createdUser.getNickname());
            successVO.setAvatar(createdUser.getAvatar());

            // 获取会员等级名称
            if (createdUser.getRankId() != null && createdUser.getRankId() > 0) {
                UserRank userRank = userRankService.getById(createdUser.getRankId());
                if (userRank != null) {
                    successVO.setRankName(userRank.getRankName());
                }
            }

            successList.add(successVO);
        }

        return successList;
    }


    /**
     * 获取会员导入失败的数据
     */
    @Override
    public void getUserImportErrorData(MultipartFile file, HttpServletResponse response) {
        Assert.notNull(file, () -> new GlobalException("请上传文件"));

        ExcelUtils<UserImportExcelDTO> excelUtils = new ExcelUtils<>(file, UserImportExcelDTO.class);
        List<UserImportExcelDTO> userImportList = excelUtils.importExcel();
        Assert.isTrue(CollUtil.isNotEmpty(userImportList), () -> new GlobalException("表格数据为空"));

        // 校验导入数据
        List<UserImportErrorExcelVO> errorList = checkUserImportData(userImportList);

        ExcelUtils<UserImportErrorExcelVO> errorExcelUtils = new ExcelUtils<>(errorList, UserImportErrorExcelVO.class);
        errorExcelUtils.exportExcelWeb(response);
    }

    /**
     * 校验会员导入数据
     */
    private List<UserImportErrorExcelVO> checkUserImportData(List<UserImportExcelDTO> userImportList) {
        List<UserImportErrorExcelVO> errorList = new ArrayList<>();

        for (UserImportExcelDTO importDTO : userImportList) {
            UserImportErrorExcelVO errorVO = new UserImportErrorExcelVO();
            BeanUtils.copyProperties(importDTO, errorVO);

            // 校验用户名
            if (StrUtil.isEmpty(importDTO.getUsername())) {
                errorVO.setErrorMsg("用户名不能为空");
                errorList.add(errorVO);
                continue;
            }

            // 校验用户名是否已存在
            if (this.checkUserInfo("username", importDTO.getUsername())) {
                errorVO.setErrorMsg("用户名已存在");
                errorList.add(errorVO);
                continue;
            }

            // 校验手机号
            if (StrUtil.isEmpty(importDTO.getMobile())) {
                errorVO.setErrorMsg("手机号不能为空");
                errorList.add(errorVO);
                continue;
            }

            // 校验手机号格式
            if (!importDTO.getMobile().matches("^1[3-9]\\d{9}$")) {
                errorVO.setErrorMsg("手机号格式不正确");
                errorList.add(errorVO);
                continue;
            }

            // 校验手机号是否已存在
            if (this.checkUserInfo("mobile", importDTO.getMobile())) {
                errorVO.setErrorMsg("手机号已被注册");
                errorList.add(errorVO);
                continue;
            }

            //密码必须存在
            if (StringUtils.isBlank(importDTO.getPassword())) {
//                errorVO.setErrorMsg("密码未填写");
//                errorList.add(errorVO);
                importDTO.setPassword("");
                continue;
            }

            // 校验邮箱格式（如果填写了邮箱）
            if (StrUtil.isNotEmpty(importDTO.getEmail())) {
                if (!importDTO.getEmail().matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
                    errorVO.setErrorMsg("邮箱格式不正确");
                    errorList.add(errorVO);
                    continue;
                }

                // 校验邮箱是否已存在
                Long emailCount = this.lambdaQuery().eq(User::getEmail, importDTO.getEmail()).count();
                if (emailCount > 0) {
                    errorVO.setErrorMsg("邮箱已被注册");
                    errorList.add(errorVO);
                    continue;
                }
            }

            // 校验会员等级ID（如果填写了）
            if (StrUtil.isNotEmpty(importDTO.getRankId())) {
                try {
                    Integer rankId = Integer.parseInt(importDTO.getRankId());
                    UserRank userRank = userRankService.getById(rankId);
                    if (userRank == null) {
                        errorVO.setErrorMsg("会员等级ID不存在");
                        errorList.add(errorVO);
                        continue;
                    }
                } catch (NumberFormatException e) {
                    errorVO.setErrorMsg("会员等级ID格式不正确");
                    errorList.add(errorVO);
                    continue;
                }
            }

            // 校验余额格式（如果填写了）
            if (StrUtil.isNotEmpty(importDTO.getBalance())) {
                try {
                    new BigDecimal(importDTO.getBalance());
                } catch (NumberFormatException e) {
                    errorVO.setErrorMsg("余额格式不正确");
                    errorList.add(errorVO);
                    continue;
                }
            }

            // 校验积分格式（如果填写了）
            if (StrUtil.isNotEmpty(importDTO.getPoints())) {
                try {
                    Integer.parseInt(importDTO.getPoints());
                } catch (NumberFormatException e) {
                    errorVO.setErrorMsg("积分格式不正确");
                    errorList.add(errorVO);
                    continue;
                }
            }

            // 校验成长积分格式（如果填写了）
            if (StrUtil.isNotEmpty(importDTO.getGrowthPoints())) {
                try {
                    Integer.parseInt(importDTO.getGrowthPoints());
                } catch (NumberFormatException e) {
                    errorVO.setErrorMsg("成长积分格式不正确");
                    errorList.add(errorVO);
                    continue;
                }
            }

            // 校验生日格式（如果填写了）
            if (StrUtil.isNotEmpty(importDTO.getBirthday())) {
                if (!importDTO.getBirthday().matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                    errorVO.setErrorMsg("生日格式不正确，应为:2025-01-01");
                    errorList.add(errorVO);
                    continue;
                }
            }
        }

        return errorList;
    }

    @Override
    public void setAdminNote(SetAdminNoteParam param) {
        // 如果userIds不为空，则批量更新
        if (StrUtil.isNotBlank(param.getUserIds())) {
            // 将字符串转为list
            List<Integer> userIdList = Arrays.stream(param.getUserIds().split(","))
                    .map(String::trim)
                    .filter(StrUtil::isNotBlank)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            // 批量更新
            if (!userIdList.isEmpty()) {
                this.lambdaUpdate()
                        .set(User::getAdminNote, param.getAdminNote())
                        .set(User::getMark, param.getMark())
                        .in(User::getUserId, userIdList)
                        .update();
            }
        } else if (param.getUserId() != null) {
            // 如果只传了userId，则只更新这个userId
            this.lambdaUpdate()
                    .set(User::getAdminNote, param.getAdminNote())
                    .set(User::getMark, param.getMark())
                    .eq(User::getUserId, param.getUserId())
                    .update();
        }
    }

}
