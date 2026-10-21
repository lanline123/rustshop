package com.tigshop.service.promotion.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.cart.AddToCartDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.enums.common.DateTypeEnum;
import com.tigshop.bean.enums.order.AftersalesTypeEnum;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.promotion.JoinStatusEnum;
import com.tigshop.bean.enums.promotion.JoinTypeEnum;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.promotion.GrouponPromotion;
import com.tigshop.bean.model.promotion.GrouponPromotionDetail;
import com.tigshop.bean.model.promotion.GrouponPromotionItem;
import com.tigshop.bean.model.promotion.GrouponPromotionRecord;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.param.order.AftersalesApplyParam;
import com.tigshop.bean.param.promotion.groupon.PromotionPromotionItemCreateParam;
import com.tigshop.bean.query.promotion.groupon.GrouponRecordOpenJoinMemberQuery;
import com.tigshop.bean.query.promotion.groupon.GrouponRecordOpenJonPageQuery;
import com.tigshop.bean.query.promotion.groupon.GrouponRecordOrderPageQuery;
import com.tigshop.bean.query.promotion.groupon.GrouponRecordPageQuery;
import com.tigshop.bean.vo.promotion.*;
import com.tigshop.bean.vo.promotion.buyershow.BuyerVO;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.*;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.promotion.GrouponPromotionItemMapper;
import com.tigshop.mapper.promotion.GrouponPromotionMapper;
import com.tigshop.mapper.promotion.GrouponPromotionRecordMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.order.AftersalesService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.promotion.GrouponPromotionDetailService;
import com.tigshop.service.promotion.GrouponPromotionRecordService;
import com.tigshop.service.setting.GalleryPicService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * @author kidd
 * @description 针对表【groupon_promotion_record(拼团记录)】的数据库操作Service实现
 * @createDate 2025-08-28 11:18:30
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class GrouponPromotionRecordServiceImpl extends BaseServiceImpl<GrouponPromotionRecordMapper, GrouponPromotionRecord> implements GrouponPromotionRecordService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final ShopMapper shopMapper;
    private final GrouponPromotionMapper grouponPromotionMapper;
    private final GrouponPromotionDetailService grouponPromotionDetailService;
    private final GrouponPromotionItemMapper grouponPromotionItemMapper;
    private final RabbitTemplate rabbitTemplate;
    private final MessageHelper messageHelper;
    private final OrderMapper orderMapper;
    private final ApplicationContext applicationContext;
    private final OrderItemMapper orderItemMapper;
    private final GalleryPicService galleryPicService;
    @Resource
    private ObjectProvider<OrderService> orderService;

    @Override
    public Page<GrouponRecordOpenJonPageVO> openJoinList(GrouponRecordOpenJonPageQuery pageQuery) {
        Page<GrouponPromotionRecord> page = buildSortOrder(pageQuery);
        Page<GrouponPromotionRecord> recordPage = this.lambdaQuery()
                .eq(GrouponPromotionRecord::getGrouponPromotionId, pageQuery.getGrouponPromotionId())
                .eq(GrouponPromotionRecord::getJoinType, JoinTypeEnum.OPEN_GROUP.getCode())
                .eq(pageQuery.getJoinStatus() != null, GrouponPromotionRecord::getJoinStatus, pageQuery.getJoinStatus())
                .like(StrUtil.isNotBlank(pageQuery.getProductName()), GrouponPromotionRecord::getProductName, pageQuery.getProductName())
                .orderByDesc(GrouponPromotionRecord::getOpenGroupTime)
                .page(page);

        List<GrouponPromotionRecord> records = recordPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        List<GrouponRecordOpenJonPageVO> resultRecords = records.stream()
                .map(item -> BeanUtil.copyProperties(item, GrouponRecordOpenJonPageVO.class))
                .toList();

        return PageUtil.convertPage(recordPage, resultRecords);
    }

    @Override
    public Page<GrouponRecordOrderPageVO> orderList(GrouponRecordOrderPageQuery pageQuery) {
        Page<GrouponPromotionRecord> page = buildSortOrder(pageQuery);
        Page<GrouponRecordOrderPageVO> grouponRecordOrderPageVOPage = this.baseMapper.orderList(page, pageQuery);
        grouponRecordOrderPageVOPage.getRecords().forEach(item -> {
            item.setOrderStatusName(OrderStatusEnum.getStatusName(item.getOrderStatus()));
            if (JoinStatusEnum.FAIL.getCode().equals(item.getJoinStatus())) {
                item.setOrderStatusName("拼团失败，待退款");
            }
        });
        return grouponRecordOrderPageVOPage;
    }

    @Override
    public List<GrouponRecordOpenJoinMemberVO> groupMemberList(GrouponRecordOpenJoinMemberQuery pageQuery) {
        List<GrouponPromotionRecord> grouponPromotionRecords = this.lambdaQuery()
                .eq(GrouponPromotionRecord::getOpenGroupRecordId, pageQuery.getGrouponPromotionRecordId())
                .list();

        if (CollUtil.isEmpty(grouponPromotionRecords)) {
            return new ArrayList<>();
        }

        List<Long> userIds = grouponPromotionRecords.stream().map(GrouponPromotionRecord::getUserId).distinct().toList();
        List<User> users = userMapper.selectBatchIds(userIds);

        return grouponPromotionRecords.stream()
                .map(record -> {
                    User user = users.stream()
                            .filter(u -> u.getUserId().equals(record.getUserId().intValue()))
                            .findFirst()
                            .orElse(null);

                    Integer shopId = record.getShopId();
                    String shopTitle;
                    if (shopId == null || shopId == 0) {
                        shopTitle = "商城自营";
                    } else {
                        Shop shop = shopMapper.selectById(shopId);
                        shopTitle = shop.getShopTitle();
                    }

                    return new GrouponRecordOpenJoinMemberVO(record, user, shopTitle);
                })
                .toList();
    }

    @Transactional
    @Override
    public void manualGroup(List<Long> grouponPromotionRecordIds) {
        if (CollUtil.isEmpty(grouponPromotionRecordIds)) {
            return;
        }
        for (Long id : grouponPromotionRecordIds) {
            applicationContext.getBean(GrouponPromotionRecordService.class).manualGroup(id);
        }
    }

    @Transactional
    @Override
    public void manualGroup(Long grouponPromotionRecordId) {
        GrouponPromotionRecord openJoinRecord = this.getById(grouponPromotionRecordId);
        if (openJoinRecord == null || openJoinRecord.getJoinStatus().equals(JoinStatusEnum.FAIL.getCode())) {
            throw new GlobalException("拼团记录不存在或已失败");
        }

        Long groupNum = this.lambdaQuery()
                .eq(GrouponPromotionRecord::getOpenGroupRecordId, grouponPromotionRecordId)
                .count();

        long virtualMemberNum = Math.max(openJoinRecord.getMinGroupNum() - groupNum, 0);

        openJoinRecord.setJoinStatus(JoinStatusEnum.SUCCESS.getCode());
        this.updateById(openJoinRecord);

        this.lambdaUpdate()
                .eq(GrouponPromotionRecord::getOpenGroupRecordId, grouponPromotionRecordId)
                .set(GrouponPromotionRecord::getJoinStatus, JoinStatusEnum.SUCCESS.getCode())
                .update();

        List<GrouponPromotionRecord> surplusJoinRecords = new ArrayList<>();
        List<BuyerVO> buyerInfos = getVirtualUser(virtualMemberNum);
        for (int i = 0; i < virtualMemberNum; i++) {
            // 创建参团记录
            GrouponPromotionRecord joinRecord = BeanUtil.copyProperties(openJoinRecord, GrouponPromotionRecord.class);
            joinRecord.setGrouponPromotionRecordId(null);
            joinRecord.setJoinType(JoinTypeEnum.JOIN_GROUP.getCode());
            joinRecord.setJoinStatus(JoinStatusEnum.SUCCESS.getCode());
            joinRecord.setUserId(0L);
            joinRecord.setOrderId(0L);
            joinRecord.setOrderSn("0");
            joinRecord.setUsername(buyerInfos.get(i).getBuyerName());
            joinRecord.setAvatar(buyerInfos.get(i).getBuyerPhoto());
            joinRecord.setIsVirtual(Constants.YES);
            joinRecord.setPaidAmount(BigDecimal.ZERO);
            joinRecord.setAddTime(StringUtils.getCurrentTime());
            surplusJoinRecords.add(joinRecord);
        }
        this.saveBatch(surplusJoinRecords);

        writeGrouponStatementOutput(openJoinRecord.getGrouponPromotionRecordId(), null);

        //完成拼团才创建派送订单
        orderService.getIfAvailable().createDeliveryOrderIfNeeded(orderService.getIfAvailable().detail(openJoinRecord.getOrderId().intValue()));
        triggerVirtualAutoDeliver(grouponPromotionRecordId);
    }

    @Override
    public Page<GrouponRecordVO> clientList(GrouponRecordPageQuery pageQuery) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Page<GrouponPromotionRecord> page = buildSortOrder(pageQuery);
        if (userId == null || userId <= 0) {
            //未登录的情况下,必传商品id
            Integer productId = pageQuery.getProductId();
            if (productId == null) {
                //非法请求直接变为空
                return PageUtil.convertPage(page, new ArrayList<>());
            }
        } else {
            if (pageQuery.getProductId() == null) {
                pageQuery.setIsMine(true);
            }
        }


        MPJLambdaWrapper<GrouponPromotionRecord> wrapper = new MPJLambdaWrapper<>();
        Integer dateType = pageQuery.getDateType();
        if (dateType != null) {
            long[] timestampRangeByCode = DateTypeEnum.getTimestampRangeByCode(dateType);
            wrapper.leftJoin(Order.class, Order::getOrderId, GrouponPromotionRecord::getOrderId)
                    .ge(Order::getAddTime, timestampRangeByCode[0])
                    .le(Order::getAddTime, timestampRangeByCode[1]);
        }

        wrapper.eq(pageQuery.getIsMine(), GrouponPromotionRecord::getUserId, userId)
                .eq(pageQuery.getJoinType() != null, GrouponPromotionRecord::getJoinType, pageQuery.getJoinType())
                .eq(pageQuery.getJoinStatus() != null, GrouponPromotionRecord::getJoinStatus, pageQuery.getJoinStatus())
                .eq(pageQuery.getProductId() != null, GrouponPromotionRecord::getProductId, pageQuery.getProductId())
                .eq(Boolean.TRUE.equals(pageQuery.getExcludeEnded()), GrouponPromotionRecord::getJoinStatus, JoinStatusEnum.WAIT_GROUP.getCode());

        if (!pageQuery.getIsMine()) {
            wrapper.eq(GrouponPromotionRecord::getJoinType, JoinTypeEnum.OPEN_GROUP.getCode());
            wrapper.leftJoin(GrouponPromotion.class, GrouponPromotion::getGrouponPromotionId, GrouponPromotionRecord::getGrouponPromotionId)
                    .gt(GrouponPromotion::getEndTime, StringUtils.getCurrentTime());
        }

        // 关键词检索 收货人 + 订单号 + 订单id
        String keyword = pageQuery.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            wrapper.leftJoin(OrderItem.class, OrderItem::getOrderId, Order::getOrderId);
            wrapper.and(wrapper1 -> wrapper1
                    .like(Order::getOrderSn, keyword)
                    .or()
                    .like(Order::getConsignee, keyword)
                    .or()
                    .like(OrderItem::getProductName, keyword)
                    .or()
                    .eq(Order::getOrderId, keyword));
        }
        Page<GrouponPromotionRecord> recordPage = this.page(page, wrapper);

        List<GrouponPromotionRecord> records = recordPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return PageUtil.convertPage(recordPage, new ArrayList<>());
        }

        List<Long> productIds = records.stream().map(GrouponPromotionRecord::getProductId).toList();
        List<Product> products = productMapper.selectBatchIds(productIds);

        List<Long> grouponItemIds = records.stream().map(GrouponPromotionRecord::getGrouponPromotionItemId).toList();
        List<GrouponPromotionRecord> grouponRecords = this.lambdaQuery()
                .in(GrouponPromotionRecord::getGrouponPromotionItemId, grouponItemIds)
                .list();

        List<GrouponPromotionDetail> grouponPromotionDetails = grouponPromotionDetailService.lambdaQuery()
                .in(GrouponPromotionDetail::getGrouponPromotionItemId, grouponItemIds)
                .list();

        List<GrouponRecordVO> resultRecords = records.stream()
                .map(item -> {
                    Product product = products.stream()
                            .filter(p -> p.getProductId().equals(item.getProductId().intValue()))
                            .findFirst()
                            .orElse(null);

                    List<GrouponPromotionRecord> currRecords = grouponRecords.stream()
                            .filter(r ->
                                    r.getGrouponPromotionItemId().equals(item.getGrouponPromotionItemId()) &&
                                            r.getOpenGroupRecordId().equals(item.getOpenGroupRecordId())
                            )
                            .toList();

                    List<GrouponPromotionDetail> currDetails = grouponPromotionDetails.stream()
                            .filter(d -> d.getGrouponPromotionItemId().equals(item.getGrouponPromotionItemId()))
                            .toList();

                    User user = userMapper.selectById(item.getUserId());

                    Order order = orderMapper.selectById(item.getOrderId());
                    return new GrouponRecordVO(item, product, currRecords, currDetails, user, order);
                })
                .toList();

        return PageUtil.convertPage(recordPage, resultRecords);
    }

    @Override
    public GrouponRecordDetailVO clientDetails(Long grouponPromotionRecordId) {
        GrouponPromotionRecord record = this.getById(grouponPromotionRecordId);
        Assert.notNull(record, () -> new GlobalException(messageHelper.get("error.groupon.record.not.exist")));

        Shop shop = shopMapper.selectById(record.getShopId());

        GrouponPromotion grouponPromotion = grouponPromotionMapper.selectById(record.getGrouponPromotionId());

        Product product = productMapper.selectById(record.getProductId());

        Long joinNumCount = this.lambdaQuery()
                .eq(GrouponPromotionRecord::getGrouponPromotionItemId, record.getGrouponPromotionItemId())
                .eq(GrouponPromotionRecord::getOpenGroupRecordId, record.getGrouponPromotionRecordId())
                .count();

        List<GrouponPromotionRecord> grouponPromotionRecords = this.lambdaQuery()
                .eq(GrouponPromotionRecord::getOpenGroupRecordId, record.getOpenGroupRecordId())
                .list();

        List<User> users = grouponPromotionRecords.stream().map(item -> {
            String username = item.getUsername();
            String avatar = item.getAvatar();
            if (StrUtil.isBlank(username)) {
                User user = userMapper.selectById(item.getUserId());
                username = user.getUsername();
                avatar = user.getAvatar();
            }
            return User.builder()
                    .userId(item.getUserId().intValue())
                    .username(username)
                    .avatar(avatar)
                    .build();
        }).toList();

        List<GrouponPromotionDetail> grouponPromotionDetails = grouponPromotionDetailService.lambdaQuery()
                .eq(GrouponPromotionDetail::getGrouponPromotionItemId, record.getGrouponPromotionItemId())
                .list();

        return new GrouponRecordDetailVO(record, shop, grouponPromotion, product, joinNumCount, users, grouponPromotionDetails);
    }

    @Override
    public void saveRecord(OrderItem orderItem, AddToCartDTO.GrouponData grouponData) {
        GrouponPromotion grouponPromotion = grouponPromotionMapper.selectById(grouponData.getGrouponPromotionId());
        User user = userMapper.selectById(orderItem.getUserId());
        Long currentTime = StringUtils.getCurrentTime();

        // 创建开团记录
        if (grouponData.getGrouponPromotionRecordId() == null || grouponData.getGrouponPromotionRecordId() == 0) {
            Long joinExpirationTime = currentTime + grouponPromotion.getExpirationTime();

            GrouponPromotionItem grouponPromotionItem = grouponPromotionItemMapper.selectById(grouponData.getGrouponPromotionItemId());
            List<PromotionPromotionItemCreateParam.GrouponNumParam> grouponNumParams = JSONArray.parseArray(grouponPromotionItem.getGroupNum(), PromotionPromotionItemCreateParam.GrouponNumParam.class);
            PromotionPromotionItemCreateParam.GrouponNumParam grouponNum = grouponNumParams.stream()
                    .filter(grouponNumParam -> grouponNumParam.getGroupNumKey().equals(grouponData.getGroupNumKey()))
                    .findFirst()
                    .get();

            GrouponPromotionRecord openRecord = GrouponPromotionRecord.builder()
                    .grouponPromotionId(grouponData.getGrouponPromotionId())
                    .grouponPromotionItemId(grouponData.getGrouponPromotionItemId())
                    .productId((long) orderItem.getProductId())
                    .productName(orderItem.getProductName())
                    .joinType(JoinTypeEnum.OPEN_GROUP.getCode())
                    .joinStatus(JoinStatusEnum.WAIT_GROUP.getCode())
                    .userId((long) orderItem.getUserId())
                    .orderId((long) orderItem.getOrderId())
                    .orderSn(orderItem.getOrderSn())
                    .isVirtual(0)
                    .paidAmount(grouponData.getPrice())
                    .openGroupTime(currentTime)
                    .groupCaptainName(user.getUsername())
                    .joinExpirationTime(joinExpirationTime)
                    .minGroupNum(grouponNum.getGroupNum())
                    .shopId(grouponPromotion.getShopId())
                    .addTime(currentTime)
                    .openGroupRecordId(0L)
                    .build();
            this.save(openRecord);
            openRecord.setOpenGroupRecordId(openRecord.getGrouponPromotionRecordId());
            this.updateById(openRecord);

            // 如果是1人团，开团立即拼团成功
            int joinStatus = grouponNum.getGroupNum() == 1 ? 2 : 1;

            if (joinStatus == 2) {
                openRecord.setJoinStatus(joinStatus);
                this.updateById(openRecord);
            }

            // 发送延时消息
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.DELAY_EXCHANGE,
                    RabbitMQConfig.GROUPON_RECORD_ROUTING_KEY,
                    openRecord.getGrouponPromotionRecordId(),
                    message -> {
                        message.getMessageProperties().setHeader("x-delay", grouponPromotion.getExpirationTime() * 1000);
                        return message;
                    }
            );
        }

        // 参团记录
        if (grouponData.getGrouponPromotionRecordId() != null && grouponData.getGrouponPromotionRecordId() != 0) {
            GrouponPromotionRecord openRecord = this.getById(grouponData.getGrouponPromotionRecordId());
            Integer minGroupNum = openRecord.getMinGroupNum();

            Long joinNum = this.lambdaQuery().eq(GrouponPromotionRecord::getOpenGroupRecordId, openRecord.getGrouponPromotionRecordId()).count();

            Integer joinStatus = joinNum + 1 >= minGroupNum ? JoinStatusEnum.SUCCESS.getCode() : JoinStatusEnum.WAIT_GROUP.getCode();

            if (joinStatus.equals(JoinStatusEnum.SUCCESS.getCode())) {
                openRecord.setJoinStatus(joinStatus);
                this.lambdaUpdate()
                        .eq(GrouponPromotionRecord::getOpenGroupRecordId, openRecord.getGrouponPromotionRecordId())
                        .set(GrouponPromotionRecord::getJoinStatus, joinStatus).update();
                writeGrouponStatementOutput(openRecord.getGrouponPromotionRecordId(), (long) orderItem.getOrderId());
            }

            GrouponPromotionRecord joinRecord = BeanUtil.copyProperties(openRecord, GrouponPromotionRecord.class);
            joinRecord.setGrouponPromotionRecordId(null);
            joinRecord.setJoinType(JoinTypeEnum.JOIN_GROUP.getCode());
            joinRecord.setJoinStatus(joinStatus);
            joinRecord.setUserId((long) orderItem.getUserId());
            joinRecord.setOrderId((long) orderItem.getOrderId());
            joinRecord.setOrderSn(orderItem.getOrderSn());
            joinRecord.setIsVirtual(0);
            joinRecord.setPaidAmount(grouponData.getPrice());
            joinRecord.setOpenGroupTime(currentTime);
            joinRecord.setAddTime(currentTime);
            this.save(joinRecord);
        }

    }

    @Transactional
    @Override
    public void expirationTime(Long grouponRecordId) {
        GrouponPromotionRecord openRecord = this.getById(grouponRecordId);
        GrouponPromotionItem grouponPromotionItem = grouponPromotionItemMapper.selectById(openRecord.getGrouponPromotionItemId());
        Integer minimumJoinNum = grouponPromotionItem.getMinGroupNum();
        Long joinNum = this.lambdaQuery().eq(GrouponPromotionRecord::getOpenGroupRecordId, grouponRecordId).count();
        long minimumJoinShortage = minimumJoinNum - joinNum;
        if (grouponPromotionItem.getIsAutoGroup() == 1 && minimumJoinShortage <= 0) {
            // 达到活动最低真实参团人数后自动成团
            Long currentTime = StringUtils.getCurrentTime();
            List<GrouponPromotionRecord> virtualRecords = new ArrayList<>();
            long virtualMemberNum = Math.max(openRecord.getMinGroupNum() - joinNum, 0);

            // 按当前开团所选成团人数补齐虚拟用户
            List<BuyerVO> buyerInfos = getVirtualUser(virtualMemberNum);
            for (int i = 0; i < virtualMemberNum; i++) {
                GrouponPromotionRecord joinRecord = BeanUtil.copyProperties(openRecord, GrouponPromotionRecord.class);
                joinRecord.setJoinType(JoinTypeEnum.JOIN_GROUP.getCode());
                joinRecord.setJoinStatus(JoinStatusEnum.SUCCESS.getCode());
                joinRecord.setUserId(0L);
                joinRecord.setUsername(buyerInfos.get(i).getBuyerName());
                joinRecord.setAvatar(buyerInfos.get(i).getBuyerPhoto());
                joinRecord.setOrderId(0L);
                joinRecord.setOrderSn("");
                joinRecord.setIsVirtual(1);
                joinRecord.setPaidAmount(BigDecimal.ZERO);
                joinRecord.setAddTime(currentTime);
                joinRecord.setGrouponPromotionRecordId(null);
                virtualRecords.add(joinRecord);
            }
            this.saveBatch(virtualRecords);

            // 更新为拼团成功
            this.lambdaUpdate()
                    .eq(GrouponPromotionRecord::getOpenGroupRecordId, grouponRecordId)
                    .eq(GrouponPromotionRecord::getJoinStatus, JoinStatusEnum.WAIT_GROUP.getCode())
                    .set(GrouponPromotionRecord::getJoinStatus, JoinStatusEnum.SUCCESS.getCode())
                    .update();
            writeGrouponStatementOutput(grouponRecordId, null);
        }
        if (grouponPromotionItem.getIsAutoGroup() == 1 && minimumJoinShortage > 0) {
            boolean update = this.lambdaUpdate()
                    .eq(GrouponPromotionRecord::getOpenGroupRecordId, grouponRecordId)
                    .eq(GrouponPromotionRecord::getJoinStatus, JoinStatusEnum.WAIT_GROUP.getCode())
                    .set(GrouponPromotionRecord::getJoinStatus, JoinStatusEnum.FAIL.getCode())
                    .update();
            if (update) {
                writeGrouponStatementOutput(grouponRecordId, null);
                buildAftersale(grouponRecordId);
            }

        }

        if (grouponPromotionItem.getIsAutoGroup() == 0) {
            boolean isUpdate = this.lambdaUpdate().eq(GrouponPromotionRecord::getOpenGroupRecordId, grouponRecordId)
                    .set(GrouponPromotionRecord::getJoinStatus, JoinStatusEnum.FAIL.getCode())
                    .eq(GrouponPromotionRecord::getJoinStatus, JoinStatusEnum.WAIT_GROUP.getCode())
                    .update();

            if (isUpdate) {
                writeGrouponStatementOutput(grouponRecordId, null);
                buildAftersale(grouponRecordId);
            }
        }


        //完成拼团才创建派送订单
        orderService.getIfAvailable().createDeliveryOrderIfNeeded(orderService.getIfAvailable().detail(openRecord.getOrderId().intValue()));
        triggerVirtualAutoDeliver(grouponRecordId);
    }

    /** 成团后仅对真实订单触发虚拟商品自动发货；实物及混合订单由原发货流程处理。 */
    private void triggerVirtualAutoDeliver(Long openGroupRecordId) {
        List<GrouponPromotionRecord> records = this.lambdaQuery()
                .eq(GrouponPromotionRecord::getOpenGroupRecordId, openGroupRecordId)
                .list();
        for (GrouponPromotionRecord record : records) {
            if (record.getUserId() != null && record.getUserId() != 0
                    && record.getOrderId() != null && record.getOrderId() > 0) {
                Integer orderId = record.getOrderId().intValue();
                TransactionAfterCommitExecutor.execute(() -> {
                    try {
                        orderService.getIfAvailable().autoDeliverVirtualOrder(orderId);
                    } catch (Exception e) {
                        log.warn("拼团成功后虚拟订单自动发货失败，orderId={}", orderId, e);
                    }
                });
            }
        }
    }

    private void buildAftersale(Long grouponRecordId) {
        // 退款
        List<GrouponPromotionRecord> list = this.lambdaQuery().eq(GrouponPromotionRecord::getOpenGroupRecordId, grouponRecordId).list();
        for (GrouponPromotionRecord record : list) {
            if (record.getUserId() == 0) {
                continue;
            }
            // 获取订单商品
            List<AftersalesApplyParam.ItemsDTO> itemList = orderItemMapper.selectList(
                            new LambdaQueryWrapper<>(OrderItem.class).eq(OrderItem::getOrderId, record.getOrderId()))
                    .stream().map(orderItem -> {
                        AftersalesApplyParam.ItemsDTO itemsDTO = new AftersalesApplyParam.ItemsDTO();
                        itemsDTO.setOrderItemId(orderItem.getItemId());
                        itemsDTO.setNumber(orderItem.getQuantity());
                        return itemsDTO;
                    }).toList();
            // 创建售后
            AftersalesApplyParam aftersalesApplyParam = AftersalesApplyParam.builder()
                    .refundAmount(record.getPaidAmount())
                    .aftersaleType(AftersalesTypeEnum.PAYRETURN.getCode())
                    .description("拼团失败，发起退款")
                    .orderId(record.getOrderId().intValue())
                    .userId(record.getUserId().intValue())
                    .aftersaleReason("拼团失败，发起退款")
                    .pics(List.of())
                    .items(itemList)
                    .build();
            applicationContext.getBean(AftersalesService.class).afterSalesApply(aftersalesApplyParam);
        }
    }

    /**
     * 为拼团内的所有真实订单补写 statement_output（排除虚拟用户和可选的当前订单）
     *
     * @param openGroupRecordId 开团记录 ID
     * @param excludeOrderId    不需要补写的订单 ID（当前正在支付的订单由 after-commit 写入），传 null 表示全部写入
     */
    private void writeGrouponStatementOutput(Long openGroupRecordId, Long excludeOrderId) {
        List<GrouponPromotionRecord> records = this.lambdaQuery()
                .eq(GrouponPromotionRecord::getOpenGroupRecordId, openGroupRecordId)
                .ne(GrouponPromotionRecord::getUserId, 0L)
                .list();
        for (GrouponPromotionRecord record : records) {
            if (excludeOrderId != null && record.getOrderId().equals(excludeOrderId)) {
                continue;
            }
            Order order = orderMapper.selectById(record.getOrderId());
            if (order != null) {
                orderService.getIfAvailable().batchSaveStatementOutputParam(order);
            }
        }
    }

    private List<BuyerVO> getVirtualUser(long surplusNum) {
        List<BuyerVO> buyerInfos = new ArrayList<>();
        for (long i = 0; i < surplusNum; i++) {
            try {
                String buyerName = RandomInfoUtil.getRandomNickname();

                String photoUrl = RandomInfoUtil.getRandomPhoto();
                MultipartFile multipartFile = MultipartFileUtil.urlToMultipartFile(photoUrl);
                String buyerPhoto = galleryPicService.uploadImg(multipartFile);
                buyerInfos.add(new BuyerVO(buyerPhoto, buyerName));
            } catch (Exception e) {
                BuyerVO buyerInfo = new BuyerVO();
                buyerInfo.setBuyerName("user" + i);
                buyerInfo.setBuyerPhoto("https://oss.tigshop.com/img/gallery/202403/17111886010Pg60qIrJEzwe3tw06.gif");
                buyerInfos.add(buyerInfo);
                log.error("获取信息失败", e);
            }
        }
        return buyerInfos;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void dissolveGroupByCaptainRefund(Integer captainOrderId) {
        GrouponPromotionRecord captainRecord = this.lambdaQuery()
                .eq(GrouponPromotionRecord::getOrderId, captainOrderId.longValue())
                .one();
        if (captainRecord == null) {
            return;
        }
        // 仅团长 + 待成团状态才触发整团解散
        if (!JoinTypeEnum.OPEN_GROUP.getCode().equals(captainRecord.getJoinType())
                || !JoinStatusEnum.WAIT_GROUP.getCode().equals(captainRecord.getJoinStatus())) {
            return;
        }

        // 2人团不触发整团解散
        Integer minGroupNum = captainRecord.getMinGroupNum();
        if (minGroupNum == null || minGroupNum < 3) {
            return;
        }

        Long openGroupRecordId = captainRecord.getOpenGroupRecordId();

        // 查找其他真实团员（排除团长自身和虚拟用户）
        List<GrouponPromotionRecord> members = this.lambdaQuery()
                .eq(GrouponPromotionRecord::getOpenGroupRecordId, openGroupRecordId)
                .eq(GrouponPromotionRecord::getJoinStatus, JoinStatusEnum.WAIT_GROUP.getCode())
                .ne(GrouponPromotionRecord::getOrderId, captainOrderId.longValue())
                .ne(GrouponPromotionRecord::getUserId, 0L)
                .list();

        // 无其他参团者，不解散
        if (CollUtil.isEmpty(members)) {
            return;
        }

        // 将整团所有待成团记录标记为拼团失败
        this.lambdaUpdate()
                .eq(GrouponPromotionRecord::getOpenGroupRecordId, openGroupRecordId)
                .eq(GrouponPromotionRecord::getJoinStatus, JoinStatusEnum.WAIT_GROUP.getCode())
                .set(GrouponPromotionRecord::getJoinStatus, JoinStatusEnum.FAIL.getCode())
                .update();

        // 为其他真实团员自动创建售后
        for (GrouponPromotionRecord member : members) {
            try {
                List<AftersalesApplyParam.ItemsDTO> itemList = orderItemMapper.selectList(
                                new LambdaQueryWrapper<>(OrderItem.class).eq(OrderItem::getOrderId, member.getOrderId()))
                        .stream().map(orderItem -> {
                            AftersalesApplyParam.ItemsDTO itemsDTO = new AftersalesApplyParam.ItemsDTO();
                            itemsDTO.setOrderItemId(orderItem.getItemId());
                            itemsDTO.setNumber(orderItem.getQuantity());
                            return itemsDTO;
                        }).toList();

                AftersalesApplyParam param = AftersalesApplyParam.builder()
                        .refundAmount(member.getPaidAmount())
                        .aftersaleType(AftersalesTypeEnum.PAYRETURN.getCode())
                        .description("团长退款，整团解散，发起退款")
                        .orderId(member.getOrderId().intValue())
                        .userId(member.getUserId().intValue())
                        .aftersaleReason("团长退款，整团解散，发起退款")
                        .pics(List.of())
                        .items(itemList)
                        .build();
                applicationContext.getBean(AftersalesService.class).afterSalesApply(param);
            } catch (Exception e) {
                log.error("整团解散：为团员创建售后失败, orderId={}, userId={}", member.getOrderId(), member.getUserId(), e);
            }
        }
    }
}

