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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.query.user.FeedBackListPageQuery;
import com.tigshop.bean.dto.user.FeedbackCreateDTO;
import com.tigshop.bean.dto.user.FeedbackUpdateDTO;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.user.UserFeedbackStatus;
import com.tigshop.bean.enums.user.UserFeedbackType;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.Feedback;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.vo.user.UserFeedbackDetailVO;
import com.tigshop.bean.vo.user.UserFeedbackVO;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.common.utils.XssUtils;
import com.tigshop.mapper.user.FeedbackMapper;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.shop.ShopService;
import com.tigshop.service.user.FeedbackService;
import com.tigshop.service.user.FeedbackShopResolver;
import com.tigshop.service.user.UserService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.bean.enums.setting.SettingsEnum;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.user.FeedbackConstants.USER_IS_NULL;
import static com.tigshop.common.utils.SecurityUtils.getCurrentAdminId;
import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 会员留言
 *
 * @author baishang
 */
@Service
public class FeedbackServiceImpl extends BaseServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {
    private static final Set<Integer> SHOP_FEEDBACK_TYPES = Set.of(UserFeedbackType.PRODUCT.getCode());

    @Resource
    UserService userService;
    @Resource
    OrderService orderService;
    @Resource
    AdminUserService adminUserService;
    @Resource
    ProductService productService;
    @Resource
    ShopService shopService;
    @Resource
    AdminMsgService adminMsgService;
    @Resource
    FeedbackShopResolver feedbackShopResolver;
    @Resource
    private MessageHelper messageHelper;
    @Resource
    private ConfigService configService;

    @Override
    public Page<UserFeedbackVO> list(FeedBackListPageQuery pageQuery) {
        return list(pageQuery, new TenantScope(false, false, null));
    }

    @Override
    public Page<UserFeedbackVO> listForAdmin(FeedBackListPageQuery pageQuery) {
        return list(pageQuery, currentAdminTenantScope());
    }

    private Page<UserFeedbackVO> list(FeedBackListPageQuery pageQuery, TenantScope tenantScope) {

        // 分页参数
        Page<Feedback> page = buildSortOrder(pageQuery);

        boolean scopedTypeRequested = tenantScope.scoped() && StrUtil.isNotBlank(pageQuery.getType());
        List<?> types = tenantScope.scoped()
                ? resolveShopFeedbackTypes(pageQuery.getType())
                : StrUtil.split(pageQuery.getType(), StrUtil.COMMA);
        Integer queryShopId = tenantScope.scoped() ? tenantScope.shopId() : pageQuery.getShopId();

        Page<Feedback> feedbackPage = this.lambdaQuery()
                .like(StrUtil.isNotBlank(pageQuery.getKeyword()), Feedback::getContent, pageQuery.getKeyword())
                .in(CollUtil.isNotEmpty(types), Feedback::getType, types)
                .apply(scopedTypeRequested && CollUtil.isEmpty(types), "1 = 0")
                .eq(Feedback::getParentId, 0)
                .eq(pageQuery.getIsOrder() != null && pageQuery.getIsOrder() == 1, Feedback::getOrderId, 0)
                .gt(pageQuery.getIsOrder() != null && pageQuery.getIsOrder() > 1, Feedback::getOrderId, 0)
                .eq(pageQuery.getProductId() != null, Feedback::getProductId, pageQuery.getProductId())
                .eq(pageQuery.getStatus() != null, Feedback::getStatus, pageQuery.getStatus())
                .eq(pageQuery.getUserId() != null && pageQuery.getUserId() > 0, Feedback::getUserId, pageQuery.getUserId())
                .eq(queryShopId != null, Feedback::getShopId, queryShopId)
                .and(tenantScope.platformAdmin(), wrapper -> wrapper
                        .eq(Feedback::getShopId, 0)
                        .or()
                        .notIn(Feedback::getType, SHOP_FEEDBACK_TYPES))
                .page(page);

        List<Feedback> feedbackList = feedbackPage.getRecords();
        if (CollUtil.isEmpty(feedbackList)) {
            return PageUtil.convertPage(feedbackPage, List.of());
        }

        Set<Integer> userIds = feedbackList.stream()
                .map(Feedback::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, User> userMap = userIds.isEmpty()
                ? new HashMap<>()
                : userService.list(new LambdaQueryWrapper<User>().in(User::getUserId, userIds))
                .stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));

        Set<Integer> orderIds = feedbackList.stream()
                .map(Feedback::getOrderId)
                .filter(orderId -> orderId != null && orderId > 0)
                .collect(Collectors.toSet());
        Map<Integer, Order> orderMap = orderIds.isEmpty()
                ? new HashMap<>()
                : orderService.list(new LambdaQueryWrapper<Order>().in(Order::getOrderId, orderIds))
                .stream()
                .collect(Collectors.toMap(Order::getOrderId, order -> order));

        Set<Integer> productIds = feedbackList.stream()
                .map(Feedback::getProductId)
                .filter(productId -> productId != null && productId > 0)
                .collect(Collectors.toSet());
        Map<Integer, Product> productMap = productIds.isEmpty()
                ? new HashMap<>()
                : productService.list(new LambdaQueryWrapper<Product>().in(Product::getProductId, productIds))
                .stream()
                .collect(Collectors.toMap(Product::getProductId, product -> product));

        Set<Integer> shopIds = feedbackList.stream()
                .map(Feedback::getShopId)
                .filter(shopId -> shopId != null && shopId > 0)
                .collect(Collectors.toSet());
        Map<Integer, Shop> shopMap = shopIds.isEmpty()
                ? new HashMap<>()
                : shopService.list(new LambdaQueryWrapper<Shop>().in(Shop::getShopId, shopIds))
                .stream()
                .collect(Collectors.toMap(Shop::getShopId, shop -> shop));

        Set<Integer> feedbackIds = feedbackList.stream()
                .map(Feedback::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, Feedback> replyMap = new HashMap<>();
        if (!feedbackIds.isEmpty()) {
            List<Feedback> replyList = this.list(new LambdaQueryWrapper<Feedback>()
                    .in(Feedback::getParentId, feedbackIds)
                    .orderByDesc(Feedback::getId));
            for (Feedback reply : replyList) {
                replyMap.putIfAbsent(reply.getParentId(), reply);
            }
        }

        List<UserFeedbackVO> list = feedbackList.stream()
                .map(feedback -> {
                    UserFeedbackVO userFeedbackVO = new UserFeedbackVO();
                    BeanUtils.copyProperties(feedback, userFeedbackVO);
                    userFeedbackVO.setAddTime(TigUtils.handelTime(feedback.getAddTime()));
                    userFeedbackVO.setTypeName(UserFeedbackType.getTypeName(feedback.getType()));
                    User user = userMap.get(feedback.getUserId());
                    if (user != null) {
                        userFeedbackVO.setNickname(user.getNickname());
                    }
                    Order order = orderMap.get(feedback.getOrderId());
                    if (order != null) {
                        userFeedbackVO.setOrderSn(order.getOrderSn());
                    }

                    Product product = productMap.get(feedback.getProductId());
                    if (product != null) {
                        UserFeedbackVO.Product productVO = new UserFeedbackVO.Product();
                        BeanUtils.copyProperties(product, productVO);
                        userFeedbackVO.setProduct(productVO);
                    }

                    Shop shop = shopMap.get(feedback.getShopId());
                    if (shop != null) {
                        UserFeedbackVO.Shop shopVO = new UserFeedbackVO.Shop();
                        shopVO.setShopId(shop.getShopId());
                        shopVO.setShopTitle(shop.getShopTitle());
                        userFeedbackVO.setShop(shopVO);
                    }

                    Feedback feedbackReply = replyMap.get(feedback.getId());
                    if (feedbackReply != null) {
                        UserFeedbackVO.Reply reply = new UserFeedbackVO.Reply();
                        BeanUtils.copyProperties(feedbackReply, reply);
                        reply.setAddTime(TigUtils.handelTime(feedbackReply.getAddTime()));
                        userFeedbackVO.setReply(reply);
                    }
                    return userFeedbackVO;
                })
                .toList();
        return PageUtil.convertPage(feedbackPage, list);
    }

    private List<Integer> resolveShopFeedbackTypes(String requestedTypes) {
        if (StrUtil.isBlank(requestedTypes)) {
            return List.copyOf(SHOP_FEEDBACK_TYPES);
        }
        return StrUtil.split(requestedTypes, StrUtil.COMMA).stream()
                .map(String::trim)
                .filter(type -> SHOP_FEEDBACK_TYPES.stream()
                        .anyMatch(allowedType -> allowedType.toString().equals(type)))
                .map(Integer::valueOf)
                .distinct()
                .toList();
    }

    @Override
    public Page<UserFeedbackVO> listProductConsultations(FeedBackListPageQuery pageQuery) {
        int finalShopId = feedbackShopResolver.resolveProductShopId(pageQuery.getProductId(), pageQuery.getShopId());
        pageQuery.setShopId(finalShopId);
        pageQuery.setType(String.valueOf(UserFeedbackType.PRODUCT.getCode()));
        pageQuery.setExcludeReply(true);
        return list(pageQuery);
    }

    @Override
    public UserFeedbackDetailVO detail(Integer id) {
        Feedback feedback = requireAdminAccessibleFeedback(id);
        UserFeedbackDetailVO feedbackVO = new UserFeedbackDetailVO();
        BeanUtils.copyProperties(feedback, feedbackVO);
        feedbackVO.setAddTime(TigUtils.handelTime(feedback.getAddTime()));
        feedbackVO.setStatusName(UserFeedbackStatus.getStatusName(feedback.getStatus()));
        feedbackVO.setTypeName(UserFeedbackType.getTypeName(feedback.getType()));

        LambdaQueryWrapper<User> queryWrapperUser = new LambdaQueryWrapper<>();
        queryWrapperUser.eq(User::getUserId, feedback.getUserId());
        User user = userService.getOne(queryWrapperUser);
        if (user != null) {
            feedbackVO.setNickname(user.getNickname());
        }

        if (feedback.getOrderId() > 0) {
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getOrderId, feedback.getOrderId());
            Order order = orderService.getOne(queryWrapper);
            if (order != null) {
                UserFeedbackDetailVO.OrderInfo orderInfo = new UserFeedbackDetailVO.OrderInfo();
                BeanUtils.copyProperties(order, orderInfo);
                feedbackVO.setOrderInfo(orderInfo);
                feedbackVO.setOrderSn(order.getOrderSn());
            }
        }

        if (feedback.getProductId() > 0) {
            LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Product::getProductId, feedback.getProductId());
            Product product = productService.getOne(queryWrapper);
            if (product != null) {
                UserFeedbackDetailVO.Product productVO = new UserFeedbackDetailVO.Product();
                BeanUtils.copyProperties(product, productVO);
                feedbackVO.setProduct(productVO);
            }
        }

        if (feedback.getShopId() > 0) {
            LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Shop::getShopId, feedback.getShopId());
            Shop shop = shopService.getOne(queryWrapper);
            if (shop != null) {
                UserFeedbackDetailVO.Shop shopVO = new UserFeedbackDetailVO.Shop();
                shopVO.setShopId(shop.getShopId());
                shopVO.setShopTitle(shop.getShopTitle());
                feedbackVO.setShop(shopVO);
            }
        }

        // 查询所有回复列表
        LambdaQueryWrapper<Feedback> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Feedback::getParentId, feedback.getId());
        queryWrapper.orderByDesc(Feedback::getId);
        List<Feedback> feedbackReplyList = this.list(queryWrapper);
        if (CollUtil.isNotEmpty(feedbackReplyList)) {
            List<UserFeedbackDetailVO.Reply> replyList = feedbackReplyList.stream()
                    .map(feedbackReply -> {
                        UserFeedbackDetailVO.Reply reply = new UserFeedbackDetailVO.Reply();
                        BeanUtils.copyProperties(feedbackReply, reply);
                        return reply;
                    })
                    .toList();
            feedbackVO.setReplyList(replyList);
        }

        if (StrUtil.isNotBlank(feedback.getFeedbackPics()) && feedback.getFeedbackPics().length() > 3) {
            feedbackVO.setFeedbackPics(JsonUtil.jsonToList(feedback.getFeedbackPics(), String.class));
        }
        return feedbackVO;
    }

    @Override
    public boolean submit(FeedbackCreateDTO dto) {
        Integer userId = getCurrentUserId();
        if (dto != null) {
            // XSS过滤留言内容
            if (dto.getContent() != null) {
                dto.setContent(XssUtils.clean(dto.getContent()));
            }
            if (dto.getTitle() != null) {
                dto.setTitle(XssUtils.clean(dto.getTitle()));
            }

            // 敏感词检查
            String hackWords = configService.getConfigVal(SettingsEnum.MSG_HACK_WORD);
            if (StrUtil.isNotBlank(hackWords)) {
                String[] hackWordArray = hackWords.split(",");
                String contentToCheck = StrUtil.blankToDefault(dto.getContent(), "") + StrUtil.blankToDefault(dto.getTitle(), "");
                for (String hackWord : hackWordArray) {
                    if (StrUtil.isNotBlank(hackWord) && contentToCheck.contains(hackWord.trim())) {
                        throw new GlobalException(messageHelper.get("error.feedback.sensitive.word"));
                    }
                }
            }

            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUserId, userId);
            User user = userService.getOne(queryWrapper);
            if (user == null) {
                throw new GlobalException(messageHelper.get("error.member.not.found"));
            }
            Feedback feedback = new Feedback();
            BeanUtils.copyProperties(dto, feedback);
            if (dto.getFeedbackPics() != null && dto.getFeedbackPics().length > 0) {
                feedback.setFeedbackPics(JsonUtil.toJson(dto.getFeedbackPics()));
            } else {
                feedback.setFeedbackPics("[]");
            }
            feedback.setUserId(userId);
            feedback.setUsername(user.getUsername());
            feedback.setAddTime(StringUtils.getCurrentTime());
            feedback.setParentId(0);
            feedback.setStatus(UserFeedbackStatus.WAIT_REPLY.getCode());
            sanitizeSubmittedBusinessContext(feedback);
            feedback.setShopId(resolveSubmittedShopId(dto, userId));
            boolean save = save(feedback);

            // 记录管理员消息：按会员留言类型映射到消息中心分类，保留历史 FEEDBACK(53) 作为未知类型兜底。
            int adminMsgType = resolveAdminMsgType(feedback.getType());
            AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
            adminMsgCreateDTO.setMsgType(adminMsgType);
            String typeName = AdminMsgTypeEnum.getByCatId(adminMsgType).map(AdminMsgTypeEnum::getCatName).orElse("意见反馈");
            adminMsgCreateDTO.setTitle("您有一个新的" + typeName);
            adminMsgCreateDTO.setContent("用户" + user.getUsername() + "提交了一个新的" + typeName);
            adminMsgCreateDTO.setShopId(SHOP_FEEDBACK_TYPES.contains(feedback.getType())
                    ? feedback.getShopId()
                    : 0);
            adminMsgCreateDTO.setVendorId(0);
            adminMsgCreateDTO.setAdminId(0);
            Map<String, Object> relatedData = new HashMap<>();
            relatedData.put("id", feedback.getId());
            relatedData.put("feedbackId", feedback.getId());
            if (feedback.getProductId() != null && feedback.getProductId() > 0) {
                relatedData.put("productId", feedback.getProductId());
            }
            if (feedback.getOrderId() != null && feedback.getOrderId() > 0) {
                relatedData.put("orderId", feedback.getOrderId());
                adminMsgCreateDTO.setOrderId(feedback.getOrderId());
            }
            if (feedback.getProductId() != null && feedback.getProductId() > 0) {
                adminMsgCreateDTO.setProductId(feedback.getProductId());
            }
            relatedData.put("route", resolveFeedbackRoute(adminMsgType));
            relatedData.put("type", feedback.getType());
            adminMsgCreateDTO.setRelatedData(relatedData);
            adminMsgService.createMessage(adminMsgCreateDTO);

            return save;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(FeedbackUpdateDTO dto) {
        if (dto != null) {
            // XSS过滤回复内容
            if (dto.getContent() != null) {
                dto.setContent(XssUtils.clean(dto.getContent()));
            }
            if (dto.getTitle() != null) {
                dto.setTitle(XssUtils.clean(dto.getTitle()));
            }

            Integer currentAdminId = getCurrentAdminId();
            LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AdminUser::getAdminId, currentAdminId);
            AdminUser adminUser = adminUserService.getOne(queryWrapper);
            if (adminUser == null) {
                throw new GlobalException("管理员不存在");
            }
            if (Objects.equals(dto.getParentId(), 0)) {
                Feedback parentFeedback = requireAdminAccessibleFeedback(dto.getId());
                Feedback reply = new Feedback();
                reply.setAddTime(StringUtils.getCurrentTime());
                reply.setUserId(currentAdminId);
                reply.setUsername(adminUser.getUsername());
                reply.setEmail(StrUtil.blankToDefault(dto.getEmail(), ""));
                reply.setMobile(StrUtil.blankToDefault(dto.getMobile(), ""));
                reply.setContent(StrUtil.blankToDefault(dto.getContent(), ""));
                reply.setFeedbackPics(toFeedbackPics(dto.getFeedbackPics()));
                reply.setParentId(parentFeedback.getId());
                reply.setTitle("reply");
                reply.setType(parentFeedback.getType());
                reply.setStatus(UserFeedbackStatus.WAIT_REPLY.getCode());
                reply.setProductId(parentFeedback.getProductId());
                reply.setOrderId(parentFeedback.getOrderId());
                reply.setShopId(parentFeedback.getShopId());
                if (!this.save(reply)) {
                    return false;
                }
                parentFeedback.setStatus(UserFeedbackStatus.REPLIED.getCode());
                return this.updateById(parentFeedback);
            } else {
                Feedback parentFeedback = requireAdminAccessibleFeedback(dto.getParentId());
                Feedback reply;
                if (dto.getId() != null && !Objects.equals(dto.getId(), parentFeedback.getId())) {
                    reply = requireAdminAccessibleFeedback(dto.getId());
                    if (!Objects.equals(reply.getParentId(), parentFeedback.getId())) {
                        throw new GlobalException("回复记录不属于该留言");
                    }
                } else {
                    reply = this.getOne(new LambdaQueryWrapper<Feedback>()
                            .eq(Feedback::getParentId, parentFeedback.getId())
                            .orderByDesc(Feedback::getId)
                            .last("LIMIT 1"));
                    if (reply == null) {
                        throw new GlobalException("回复记录不存在");
                    }
                }
                reply.setUserId(currentAdminId);
                reply.setUsername(adminUser.getUsername());
                reply.setEmail(StrUtil.blankToDefault(dto.getEmail(), ""));
                reply.setMobile(StrUtil.blankToDefault(dto.getMobile(), ""));
                reply.setContent(StrUtil.blankToDefault(dto.getContent(), ""));
                reply.setFeedbackPics(toFeedbackPics(dto.getFeedbackPics()));
                reply.setType(parentFeedback.getType());
                reply.setProductId(parentFeedback.getProductId());
                reply.setOrderId(parentFeedback.getOrderId());
                reply.setShopId(parentFeedback.getShopId());
                return this.updateById(reply);
            }
        }
        return false;
    }

    @Override
    public boolean del(Integer id) {
        requireAdminAccessibleFeedback(id);
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batch(BatchDTO batchDTO) {
        if (batchDTO == null || !"del".equals(batchDTO.getType()) || CollUtil.isEmpty(batchDTO.getIds())) {
            return false;
        }
        batchDTO.getIds().forEach(this::requireAdminAccessibleFeedback);
        return this.removeByIds(batchDTO.getIds());
    }

    private int resolveSubmittedShopId(FeedbackCreateDTO dto, Integer userId) {
        if (Objects.equals(dto.getType(), UserFeedbackType.PRODUCT.getCode())) {
            return feedbackShopResolver.resolveProductShopId(dto.getProductId(), dto.getShopId());
        }
        if (Objects.equals(dto.getType(), UserFeedbackType.ORDER_PROBLEM.getCode())
                || Objects.equals(dto.getType(), UserFeedbackType.ORDER_CONSULTATION.getCode())) {
            if (dto.getOrderId() == null || dto.getOrderId() <= 0) {
                throw new GlobalException("订单不存在");
            }
            Order order = orderService.getById(dto.getOrderId());
            if (order == null) {
                throw new GlobalException("订单不存在");
            }
            if (!Objects.equals(order.getUserId(), userId)) {
                throw new GlobalException("非本人订单，无权提交留言");
            }
            return order.getShopId() == null ? 0 : order.getShopId();
        }
        return 0;
    }

    private void sanitizeSubmittedBusinessContext(Feedback feedback) {
        if (Objects.equals(feedback.getType(), UserFeedbackType.PRODUCT.getCode())) {
            feedback.setOrderId(0);
            return;
        }
        if (Objects.equals(feedback.getType(), UserFeedbackType.ORDER_PROBLEM.getCode())
                || Objects.equals(feedback.getType(), UserFeedbackType.ORDER_CONSULTATION.getCode())) {
            feedback.setProductId(0);
            return;
        }
        feedback.setProductId(0);
        feedback.setOrderId(0);
    }

    private Feedback requireAdminAccessibleFeedback(Integer id) {
        if (id == null) {
            throw new GlobalException("留言不存在");
        }
        Feedback feedback = this.getById(id);
        if (feedback == null) {
            throw new GlobalException("留言不存在");
        }

        TenantScope tenantScope = currentAdminTenantScope();
        Feedback tenantFeedback = feedback;
        if (feedback.getParentId() != null && feedback.getParentId() > 0) {
            tenantFeedback = this.getById(feedback.getParentId());
            if (tenantFeedback == null) {
                throw new GlobalException("留言不存在");
            }
        }
        if (tenantScope.platformAdmin()) {
            if (SHOP_FEEDBACK_TYPES.contains(tenantFeedback.getType())
                    && !Objects.equals(tenantFeedback.getShopId(), 0)) {
                throw new GlobalException("无权操作该留言");
            }
            return feedback;
        }
        if (!Objects.equals(tenantFeedback.getShopId(), tenantScope.shopId())
                || !SHOP_FEEDBACK_TYPES.contains(tenantFeedback.getType())) {
            throw new GlobalException("无权操作该留言");
        }
        return feedback;
    }

    private TenantScope currentAdminTenantScope() {
        String adminType = getAdminType();
        if (adminType == null) {
            throw new GlobalException("缺少后台租户信息");
        }
        if (AdminTypeEnum.ADMIN.getCode().equals(adminType)) {
            return new TenantScope(false, true, 0);
        }
        boolean scoped = AdminTypeEnum.SHOP.getCode().equals(adminType)
                || AdminTypeEnum.STORE.getCode().equals(adminType);
        if (!scoped) {
            throw new GlobalException("无权访问会员留言");
        }
        Integer shopId = getShopId();
        if (shopId == null || shopId <= 0) {
            throw new GlobalException("缺少当前店铺信息");
        }
        return new TenantScope(true, false, shopId);
    }

    private String toFeedbackPics(String[] feedbackPics) {
        return feedbackPics != null && feedbackPics.length > 0 ? JsonUtil.toJson(feedbackPics) : "[]";
    }

    private int resolveAdminMsgType(Integer feedbackType) {
        if (Objects.equals(feedbackType, UserFeedbackType.PRODUCT.getCode())) {
            return AdminMsgTypeEnum.PRODUCT_CONSULTATION.getCatId();
        }
        // 订单问题/订单咨询分类已下线；新提交统一落到历史意见反馈，避免生成隐藏的15/16消息。
        if (Objects.equals(feedbackType, UserFeedbackType.ORDER_PROBLEM.getCode())
                || Objects.equals(feedbackType, UserFeedbackType.ORDER_CONSULTATION.getCode())) {
            return AdminMsgTypeEnum.FEEDBACK.getCatId();
        }
        if (Objects.equals(feedbackType, UserFeedbackType.SUGGESTION.getCode())) {
            return AdminMsgTypeEnum.SUGGESTION.getCatId();
        }
        if (Objects.equals(feedbackType, UserFeedbackType.COMPLAINT.getCode())
                || Objects.equals(feedbackType, UserFeedbackType.SHOP_COMPLAINT.getCode())) {
            return AdminMsgTypeEnum.COMPLAINT.getCatId();
        }
        if (Objects.equals(feedbackType, UserFeedbackType.OTHER.getCode())) {
            return AdminMsgTypeEnum.OTHER_FEEDBACK.getCatId();
        }
        return AdminMsgTypeEnum.FEEDBACK.getCatId();
    }

    private String resolveFeedbackRoute(int adminMsgType) {
        return "memberFeedback";
    }

    private record TenantScope(boolean scoped, boolean platformAdmin, Integer shopId) {
    }

}
