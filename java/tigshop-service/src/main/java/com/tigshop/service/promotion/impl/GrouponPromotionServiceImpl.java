package com.tigshop.service.promotion.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.cart.AddToCartDTO;
import com.tigshop.bean.dto.cart.CartPromotionParsePriceDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.enums.common.shareTypeEnum;
import com.tigshop.bean.enums.product.DeleteType;
import com.tigshop.bean.enums.product.ProductStatus;
import com.tigshop.bean.enums.promotion.JoinStatusEnum;
import com.tigshop.bean.enums.promotion.JoinTypeEnum;
import com.tigshop.bean.enums.promotion.PromotionRangeEnum;
import com.tigshop.bean.enums.promotion.PromotionType;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.promotion.*;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.param.promotion.groupon.PromotionPromotionCreateParam;
import com.tigshop.bean.param.promotion.groupon.PromotionPromotionUpdateParam;
import com.tigshop.bean.query.promotion.groupon.GrouponItemPageQuery;
import com.tigshop.bean.query.promotion.groupon.GrouponPromotionClientPageQuery;
import com.tigshop.bean.query.promotion.groupon.GrouponPromotionPageQuery;
import com.tigshop.bean.vo.cart.CartVO;
import com.tigshop.bean.vo.promotion.*;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.service.user.MemberAudienceService;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.promotion.GrouponPromotionItemMapper;
import com.tigshop.mapper.promotion.GrouponPromotionMapper;
import com.tigshop.mapper.promotion.GrouponPromotionShopMapper;
import com.tigshop.mapper.promotion.PromotionMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.oauth.WechatOAuthService;
import com.tigshop.service.promotion.GrouponPromotionDetailService;
import com.tigshop.service.promotion.GrouponPromotionRecordService;
import com.tigshop.service.promotion.GrouponPromotionService;
import com.tigshop.service.promotion.ShortUrlService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.wholesale.WholesalePromotionFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.tigshop.bean.enums.setting.SettingsEnum.H5_DOMAIN;
import static com.tigshop.bean.enums.setting.SettingsEnum.PC_DOMAIN;
import static com.tigshop.common.utils.TigUtils.handelTime;

/**
 * @author tigshop
 */
@RequiredArgsConstructor
@Service
public class GrouponPromotionServiceImpl extends BaseServiceImpl<GrouponPromotionMapper, GrouponPromotion> implements GrouponPromotionService {

    private final PromotionMapper promotionMapper;
    private final ShopMapper shopMapper;
    private final GrouponPromotionRecordService grouponPromotionRecordService;
    private final GrouponPromotionItemMapper grouponPromotionItemMapper;
    private final ProductMapper productMapper;
    private final GrouponPromotionDetailService grouponPromotionDetailService;
    private final ShortUrlService shortUrlService;
    private final WechatOAuthService wechatOAuthService;
    private final GrouponPromotionMapper grouponPromotionMapper;
    private final GrouponPromotionShopMapper grouponPromotionShopMapper;
    private final ConfigService configService;
    private final UserMapper userMapper;
    private final MemberAudienceService memberAudienceService;
    private final ProductSkuService productSkuService;

    // 批发商促销列表过滤（CR-20260713-1620）。setter + @Lazy 注入，避免潜在构造器循环。
    private WholesalePromotionFilter wholesalePromotionFilter;

    @Autowired
    public void setWholesalePromotionFilter(@Lazy WholesalePromotionFilter wholesalePromotionFilter) {
        this.wholesalePromotionFilter = wholesalePromotionFilter;
    }

    @Transactional
    @Override
    public void create(PromotionPromotionCreateParam param) {
        GrouponPromotion grouponPromotion = param.buildGrouponPromotion();
        this.save(grouponPromotion);
        this.savePromotion(grouponPromotion, param.getMemberRange(), param.getMemberMarks(),
                memberAudienceService.resolveWriteScope(param.getSendUserTarget(), param.getMemberAudience()));
        // 绑定适用组织
        GrouponPromotionShop grouponPromotionShop = param.buildGrouponPromotionShop(grouponPromotion.getGrouponPromotionId().intValue());
        List<Integer> shopIds = param.getShopIds();
        shopIds.add(0);
        if (shopIds.size() == 1 && shopIds.getFirst() == -1) {
            shopIds.addAll(shopMapper.selectList(Wrappers.lambdaQuery(Shop.class))
                    .stream().map(Shop::getShopId).toList());
            grouponPromotionShop.setShopIds(JSONArray.toJSONString(shopIds));
        }
        grouponPromotionShopMapper.insert(grouponPromotionShop);
    }

    private void savePromotion(GrouponPromotion grouponPromotion, Integer memberRange, List<Integer> memberMarks, String memberScope) {
        Promotion promotion = new Promotion();
        promotion.setPromotionName(grouponPromotion.getName());
        promotion.setStartTime(grouponPromotion.getStartTime());
        promotion.setEndTime(grouponPromotion.getEndTime());
        promotion.setType(PromotionType.TYPE_GROUPON.getCode());
        promotion.setShopId(grouponPromotion.getShopId());
        promotion.setRelationId(grouponPromotion.getGrouponPromotionId().intValue());
        promotion.setRange(PromotionRangeEnum.PROMOTION_RANGE_PRODUCT.getCode());
        promotion.setMemberRange(memberRange);
        promotion.setMemberMarks(memberMarks != null ? memberMarks.toString() : null);
        promotion.setMemberScope(memberScope);

        promotionMapper.insert(promotion);
    }

    @Transactional
    @Override
    public void update(PromotionPromotionUpdateParam param) {
        GrouponPromotion dbGrouponPromotion = this.getById(param.getGrouponPromotionId());
        Assert.notNull(dbGrouponPromotion, () -> new GlobalException("未知的拼团活动"));

        getBindedOtherGtoupPromotionProduct(param, dbGrouponPromotion);
        GrouponPromotion grouponPromotion = param.buildGrouponPromotion();
        this.updateById(grouponPromotion);

        this.updatePromotion(grouponPromotion, param.getMemberRange(), param.getMemberMarks(),
                memberAudienceService.resolveWriteScope(param.getSendUserTarget(), param.getMemberAudience()));

        // 绑定适用组织
        GrouponPromotionShop grouponPromotionShop = param.buildGrouponPromotionShop(grouponPromotion.getGrouponPromotionId().intValue());
        Long count = grouponPromotionShopMapper.selectCount(Wrappers.lambdaQuery(GrouponPromotionShop.class)
                .eq(GrouponPromotionShop::getGrouponPromotionId, grouponPromotion.getGrouponPromotionId()));

        if (count < 1) {
            grouponPromotionShopMapper.insert(grouponPromotionShop);
        } else {
            grouponPromotionShopMapper.update(
                    grouponPromotionShop,
                    Wrappers.lambdaUpdate(GrouponPromotionShop.class)
                            .eq(GrouponPromotionShop::getGrouponPromotionId, grouponPromotion.getGrouponPromotionId())
            );
        }
    }

    private void getBindedOtherGtoupPromotionProduct(PromotionPromotionUpdateParam param, GrouponPromotion dbGrouponPromotion) {
        //如果过期的拼团重新打开，则需要判断管理的商品是否参加了其他拼团活动，如果参加了其他活动则不允许打开
        if ((dbGrouponPromotion.getEndTime() < System.currentTimeMillis() / 1000)
                && TigUtils.toTimestampYmdHms(param.getEndTime()) != dbGrouponPromotion.getEndTime()
                && (TigUtils.toTimestampYmdHms(param.getEndTime()) > System.currentTimeMillis() / 1000)) {

            //1.查找管理的商品
            List<GrouponPromotionDetail> gpProducts = grouponPromotionDetailService.list(Wrappers.lambdaQuery(GrouponPromotionDetail.class)
                    .eq(GrouponPromotionDetail::getGrouponPromotionId, dbGrouponPromotion.getGrouponPromotionId()));

            if (CollUtil.isEmpty(gpProducts)){
                return;
            }
            //2.遍历商品并判断有没有被其他活动绑定
            HashSet<Long> gpIds = new HashSet<>();
            ArrayList<GrouponPromotionDetail> grouponPromotionDetails = new ArrayList<>();
            gpProducts.forEach(gpProduct -> {

                List<GrouponPromotionDetail> otherGpProductsDetails = grouponPromotionDetailService.list(Wrappers.lambdaQuery(GrouponPromotionDetail.class)
                        .eq(GrouponPromotionDetail::getProductId, gpProduct.getProductId())
                        .ne(GrouponPromotionDetail::getGrouponPromotionId, gpProduct.getGrouponPromotionId()));

                if (CollUtil.isEmpty(otherGpProductsDetails)){
                    return;
                }
                otherGpProductsDetails.forEach(otherGpProduct -> {
                    gpIds.add(otherGpProduct.getGrouponPromotionId());
                });
                grouponPromotionDetails.addAll(otherGpProductsDetails);
            });
            if (CollUtil.isEmpty(grouponPromotionDetails)){
                return;
            }
            //3.被其他活动绑定的商品 参与其他拼团活动的活动时间查看其是否已过期
            List<Long> otherGpPromotionIds = new ArrayList<>(gpIds);
            //查找出未过期的拼团活动
            List<GrouponPromotion> grouponPromotions = this.list(Wrappers.lambdaQuery(GrouponPromotion.class)
                    .eq(GrouponPromotion::getShopId, dbGrouponPromotion.getShopId())
                    .in(GrouponPromotion::getGrouponPromotionId, otherGpPromotionIds)
                    .le(GrouponPromotion::getStartTime, TigUtils.toTimestampYmdHms(param.getEndTime()))
                    .ge(GrouponPromotion::getEndTime, TigUtils.toTimestampYmdHms(param.getStartTime())));
            if (CollUtil.isNotEmpty(grouponPromotions)) {
                Map<Long, GrouponPromotion> grouponPromotionMap = grouponPromotions.stream()
                        .collect(Collectors.toMap(GrouponPromotion::getGrouponPromotionId, grouponPromotion -> grouponPromotion));
                StringBuilder errorMsg = new StringBuilder();
                for (GrouponPromotionDetail grouponPromotionDetail : grouponPromotionDetails) {
                    if (grouponPromotionMap.containsKey(grouponPromotionDetail.getGrouponPromotionId())) {
                        GrouponPromotion grouponPromotion = grouponPromotionMap.get(grouponPromotionDetail.getGrouponPromotionId());
                        Product product = productMapper.selectById(grouponPromotionDetail.getProductId());
                        if (grouponPromotion.getEndTime() > System.currentTimeMillis() / 1000) {
                            errorMsg.append("商品:").append(product.getProductName()).append(" 正在参与其他拼团活动:").append(grouponPromotion.getName());
                        }
                    }
                }
                if (StrUtil.isNotBlank(errorMsg)){
                    throw new GlobalException(errorMsg.toString());
                }
            }
        }
    }

    private void updatePromotion(GrouponPromotion grouponPromotion, Integer memberRange, List<Integer> memberMarks, String memberScope) {
        promotionMapper.update(
                Wrappers.lambdaUpdate(Promotion.class)
                        .eq(Promotion::getRelationId, grouponPromotion.getGrouponPromotionId())
                        .eq(Promotion::getType, PromotionType.TYPE_GROUPON.getCode())
                        .set(Promotion::getStartTime, grouponPromotion.getStartTime())
                        .set(Promotion::getEndTime, grouponPromotion.getEndTime())
                        .set(Promotion::getPromotionName, grouponPromotion.getName())
                        // 必须无条件 set：置空代表「改回全部会员」，条件 set 会让旧人群残留
                        .set(Promotion::getMemberRange, memberRange)
                        .set(Promotion::getMemberMarks, memberMarks != null ? memberMarks.toString() : null)
                        .set(Promotion::getMemberScope, memberScope)
        );
    }

    @Transactional
    @Override
    public void finish(OperateDTO param) {
        GrouponPromotion dbGrouponPromotion = this.getById(param.getId());
        Assert.notNull(dbGrouponPromotion, () -> new GlobalException("未知的拼团活动"));

        dbGrouponPromotion.setEndTime(StringUtils.getCurrentTime());
        this.updateById(dbGrouponPromotion);

        // 结束活动只提前结束时间，不能复用 updatePromotion：后者无条件 set 人群三字段，
        // 传 null 会写 member_range = NULL（NOT NULL 列）并清空已配置人群。
        promotionMapper.update(
                Wrappers.lambdaUpdate(Promotion.class)
                        .eq(Promotion::getRelationId, dbGrouponPromotion.getGrouponPromotionId())
                        .eq(Promotion::getType, PromotionType.TYPE_GROUPON.getCode())
                        .set(Promotion::getEndTime, dbGrouponPromotion.getEndTime())
        );
    }

    @Transactional
    @Override
    public boolean del(Integer id) {
        GrouponPromotion dbGrouponPromotion = this.getById(id);
        Assert.notNull(dbGrouponPromotion, () -> new GlobalException("未知的拼团活动"));
        Assert.isTrue(Instant.now().toEpochMilli() > dbGrouponPromotion.getEndTime(), () -> new GlobalException("活动未结束，不能删除"));

        this.removeById(id);

        // 删除对应营销活动
        promotionMapper.delete(
                Wrappers.lambdaQuery(Promotion.class)
                        .eq(Promotion::getRelationId, id)
                        .eq(Promotion::getType, PromotionType.TYPE_GROUPON.getCode())
        );
        return true;
    }

    @Override
    public GrouponPromotionDetailVO detail(Integer grouponPromotionId) {
        GrouponPromotion dbGrouponPromotion = this.getById(grouponPromotionId);
        Assert.notNull(dbGrouponPromotion, () -> new GlobalException("未知的拼团活动"));
        GrouponPromotionDetailVO grouponPromotionDetailVO = BeanUtil.copyProperties(dbGrouponPromotion, GrouponPromotionDetailVO.class);

        GrouponPromotionShop grouponPromotionShop = grouponPromotionShopMapper.selectOne(Wrappers.lambdaQuery(GrouponPromotionShop.class)
                .eq(GrouponPromotionShop::getGrouponPromotionId, grouponPromotionId));

        if (grouponPromotionShop != null) {
            List<Integer> shopIds = JsonUtil.jsonToList(grouponPromotionShop.getShopIds(), Integer.class);
            List<Shop> shops = shopMapper.selectList(Wrappers.lambdaQuery(Shop.class)
                    .in(Shop::getShopId, shopIds));
            shops.add(Shop.builder().shopId(0).shopTitle("商城自营").build());
            grouponPromotionDetailVO.setShops(shops);
        }

        // 从 promotion 表获取适用会员范围字段
        Promotion promotion = promotionMapper.selectOne(Wrappers.lambdaQuery(Promotion.class)
                .eq(Promotion::getRelationId, grouponPromotionId)
                .eq(Promotion::getType, PromotionType.TYPE_GROUPON.getCode()));
        if (promotion != null) {
            grouponPromotionDetailVO.setMemberRange(promotion.getMemberRange());
            grouponPromotionDetailVO.setMemberMarks(StringUtils.str2IntList(promotion.getMemberMarks()));
            com.tigshop.bean.dto.user.SendUserTargetDTO sendUserTarget =
                    memberAudienceService.parseScope(promotion.getMemberScope());
            grouponPromotionDetailVO.setSendUserTarget(memberAudienceService.fillTargetUsers(sendUserTarget));
            grouponPromotionDetailVO.setMemberAudience(memberAudienceService.toLegacy(sendUserTarget));
        }

        return grouponPromotionDetailVO;
    }

    @Override
    public Page<GrouponPromotionPageVO> list(GrouponPromotionPageQuery pageQuery) {
        Page<GrouponPromotion> page = buildSortOrder(pageQuery);
        Page<GrouponPromotionPageVO> resultPage = this.baseMapper.list(page, pageQuery);

        List<GrouponPromotionPageVO> records = resultPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        List<Long> grouponPromotionIds = records.stream().map(GrouponPromotionPageVO::getGrouponPromotionId).toList();
        List<GrouponPromotionRecord> grouponPromotionRecords = grouponPromotionRecordService.lambdaQuery()
                .in(GrouponPromotionRecord::getGrouponPromotionId, grouponPromotionIds)
                .list();

        List<Integer> shopIds = records.stream().map(GrouponPromotionPageVO::getShopId).distinct().toList();
        List<Shop> shops = shopMapper.selectBatchIds(shopIds);

        records.forEach(record -> {
            List<GrouponPromotionRecord> currGrouponPromotionRecords = grouponPromotionRecords.stream()
                    .filter(item -> item.getGrouponPromotionId().equals(record.getGrouponPromotionId()))
                    .toList();
            Shop shop = shops.stream().filter(item -> item.getShopId().equals(record.getShopId())).findFirst().orElse(null);
            record.assemble(currGrouponPromotionRecords, shop);
        });
        return resultPage;
    }

    @Override
    public GrouponPostVO post(Integer grouponPromotionId) {
        String configVal = configService.getConfigVal(H5_DOMAIN);

        GrouponPromotion dbGrouponPromotion = this.getById(grouponPromotionId);
        Assert.notNull(dbGrouponPromotion, () -> new GlobalException("未知的拼团活动"));
        String shareLink = "/pages/groupon/list?id=";
        return GrouponPostVO.builder()
                .promotionName(dbGrouponPromotion.getName())
                .promotionType(PromotionType.TYPE_GROUPON.getDescription())
                .promotionPicUrl(dbGrouponPromotion.getShareImage())
                .wechatShortLink(configVal + shortUrlService.getWechatShortLink(shareLink, grouponPromotionId))
                .shortLink(configVal + shortUrlService.getWebShortLink(shareLink, grouponPromotionId, shareTypeEnum.GROUPON.getKey()))
                .originalLink(configVal + shareLink.replace("/mobile", ""))
                .miniCode(wechatOAuthService.getMiniCode(shareLink, grouponPromotionId))
                .promotionBrief(dbGrouponPromotion.getGrouponExplain())
                .promotionDateTime(StrUtil.format("{} - {}",
                        handelTime(dbGrouponPromotion.getStartTime(), "yyyy-MM-dd")
                        , handelTime(dbGrouponPromotion.getEndTime(), "yyyy-MM-dd")))
                .build();
    }

    @Override
    public Page<GrouponPromotionClientPageVO> clientList(GrouponPromotionClientPageQuery pageQuery) {
        Page<GrouponPromotion> page = buildSortOrder(pageQuery);
        Page<GrouponPromotionClientPageVO> resultPage = this.baseMapper.clientList(page, pageQuery);

        List<GrouponPromotionClientPageVO> records = resultPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        List<Integer> shopIds = records.stream().map(GrouponPromotionClientPageVO::getShopId).distinct().toList();
        List<Shop> shops = shopMapper.selectBatchIds(shopIds);

        records.forEach(record -> {
            Shop shop = shops.stream().filter(item -> item.getShopId().equals(record.getShopId())).findFirst().orElse(null);
            record.assemble(shop);
        });
        return resultPage;
    }

    @Override
    public GrouponPromotionClientDetailVO clientDetail(Long grouponPromotionId) {
        GrouponPromotion grouponPromotion = this.getById(grouponPromotionId);
        Assert.notNull(grouponPromotion, "拼团不存在");

        GrouponPromotionClientDetailVO result = BeanUtil.copyProperties(grouponPromotion, GrouponPromotionClientDetailVO.class);

        Shop shop = shopMapper.selectById(grouponPromotion.getShopId());
        if (shop != null) {
            result.setShopTitle(shop.getShopTitle());
        }

        // 二期（D1096）：仅新建 member_scope 拼团改为「展示 + 标记」前台置灰；存量 memberRange 拼团保持原 checkUserMark 抛异常行为不变。
        Promotion grouponPromo = promotionMapper.selectOne(
                new LambdaQueryWrapper<Promotion>()
                        .eq(Promotion::getRelationId, grouponPromotionId.intValue())
                        .eq(Promotion::getType, PromotionType.TYPE_GROUPON.getCode())
                        .last("limit 1")
        );
        if (grouponPromo != null && StringUtils.isNotBlank(grouponPromo.getMemberScope())) {
            boolean canJoin = currentUserCanJoinGroupon(grouponPromotionId);
            result.setCanJoin(canJoin);
            if (!canJoin) {
                result.setRestrictTip("您不在该拼团活动的参与范围内");
            }
        } else {
            // 存量旧逻辑：指定标记会员不匹配则抛异常（恢复改造前行为）
            checkUserMark(grouponPromotionId);
            result.setCanJoin(true);
        }
        return result;
    }

    /**
     * 检查当前用户是否允许参与拼团（开团/参团下单时调用，不满足抛业务异常）。
     *
     * @param grouponPromotionId 拼团活动ID
     */
    public void checkUserMark(Long grouponPromotionId) {
        if (!currentUserCanJoinGroupon(grouponPromotionId)) {
            // 存量场景沿用改造前文案
            throw new GlobalException("该拼团活动仅限指定会员参与");
        }
    }

    /**
     * 二期（D1096）：判定当前用户是否可参与拼团。member_scope 优先（标签/等级人群），回退旧 memberRange/memberMarks。
     * 未登录 / 查无用户 / 无人群约束 → true。
     *
     * @param grouponPromotionId 拼团活动ID
     */
    private boolean currentUserCanJoinGroupon(Long grouponPromotionId) {
        return canUserJoinGroupon(grouponPromotionId == null ? null : grouponPromotionId.intValue(), SecurityUtils.getCurrentUserId());
    }

    @Override
    public boolean canUserJoinGrouponByProduct(Integer productId, Integer userId) {
        if (userId == null || userId <= 0 || productId == null) {
            return true;
        }
        Long currentTime = StringUtils.getCurrentTime();
        List<GrouponPromotion> grouponPromotions = this.lambdaQuery()
                .lt(GrouponPromotion::getStartTime, currentTime)
                .gt(GrouponPromotion::getEndTime, currentTime)
                .list();
        if (CollUtil.isEmpty(grouponPromotions)) {
            return true;
        }
        List<Long> activeIds = grouponPromotions.stream().map(GrouponPromotion::getGrouponPromotionId).toList();
        GrouponPromotionItem grouponItem = grouponPromotionItemMapper.selectOne(
                Wrappers.lambdaQuery(GrouponPromotionItem.class)
                        .in(GrouponPromotionItem::getGrouponPromotionId, activeIds)
                        .eq(GrouponPromotionItem::getProductId, productId)
                        .last("limit 1")
        );
        if (grouponItem == null) {
            return true;
        }
        return canUserJoinGroupon(grouponItem.getGrouponPromotionId().intValue(), userId);
    }

    @Override
    public boolean canUserJoinGroupon(Integer grouponPromotionId, Integer userId) {
        if (userId == null || userId <= 0 || grouponPromotionId == null) {
            return true;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return true;
        }
        Promotion promotion = promotionMapper.selectOne(
                new LambdaQueryWrapper<Promotion>()
                        .eq(Promotion::getRelationId, grouponPromotionId)
                        .eq(Promotion::getType, PromotionType.TYPE_GROUPON.getCode())
                        .last("limit 1")
        );
        if (promotion == null) {
            return true;
        }
        String memberScope = promotion.getMemberScope();
        if (StringUtils.isNotBlank(memberScope)) {
            return memberAudienceService.matchesScope(userId.longValue(), memberScope);
        }
        if (Objects.equals(1, promotion.getMemberRange())) {
            List<Integer> allowedMarks = Optional.ofNullable(JSON.parseArray(promotion.getMemberMarks(), Integer.class)).orElse(List.of());
            return allowedMarks.isEmpty() || allowedMarks.contains(user.getMark());
        }
        return true;
    }

    /**
     * 根据用户标识过滤拼团活动
     *
     * @param grouponPromotions 拼团活动列表
     * @return 用户可以参与的拼团活动ID列表
     */
    private List<Long> filterGrouponByUserMark(List<GrouponPromotion> grouponPromotions) {
        if (CollUtil.isEmpty(grouponPromotions)) {
            return List.of();
        }

        Integer userId = SecurityUtils.getCurrentUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userMapper.selectById(userId);
        }

        List<Long> grouponIds = grouponPromotions.stream().map(GrouponPromotion::getGrouponPromotionId).toList();

        // 查询所有拼团活动对应的促销记录
        List<Promotion> promotions = promotionMapper.selectList(
                new LambdaQueryWrapper<Promotion>()
                        .in(Promotion::getRelationId, grouponIds.stream().map(Long::intValue).toList())
                        .eq(Promotion::getType, PromotionType.TYPE_GROUPON.getCode())
        );

        // 未登录/查不到用户：仅隐藏存量 memberRange 指定标记活动（保持原行为）；新建 member_scope 活动保留展示，由 canJoin 标记处理
        if (user == null) {
            Set<Integer> restrictedPromotionIds = promotions.stream()
                    .filter(p -> StringUtils.isBlank(p.getMemberScope()) && Objects.equals(1, p.getMemberRange()))
                    .map(Promotion::getRelationId)
                    .collect(Collectors.toSet());

            return grouponIds.stream()
                    .filter(id -> !restrictedPromotionIds.contains(id.intValue()))
                    .toList();
        }

        // 构建拼团ID与促销记录的映射
        Map<Integer, Promotion> promotionMap = promotions.stream()
                .collect(Collectors.toMap(Promotion::getRelationId, p -> p, (a, b) -> a));

        Integer userMark = user.getMark();
        return grouponIds.stream()
                .filter(grouponId -> {
                    Promotion promotion = promotionMap.get(grouponId.intValue());
                    if (promotion == null) {
                        return true;
                    }
                    // 二期新建 member_scope 活动：一律保留（展示），由 canJoin 标记处理置灰，不在此隐藏
                    if (StringUtils.isNotBlank(promotion.getMemberScope())) {
                        return true;
                    }
                    // 存量旧逻辑：memberRange 为 0 表示全部会员
                    if (!Objects.equals(1, promotion.getMemberRange())) {
                        return true;
                    }
                    // 存量旧逻辑：memberRange 为 1，检查用户标记是否在允许列表中
                    String memberMarks = promotion.getMemberMarks();
                    List<Integer> allowedMarks = Optional.ofNullable(JSON.parseArray(memberMarks, Integer.class)).orElse(List.of());
                    return allowedMarks.isEmpty() || allowedMarks.contains(userMark);
                })
                .toList();
    }

    /**
     * 获取列表可见的拼团活动ID：查有效拼团后，按存量 memberRange 隐藏不可参与者；
     * 新建 member_scope 活动保留（展示），由调用方按 canJoin 标记置灰。
     *
     * @param specifiedGrouponId 指定的拼团活动ID（可为空）
     * @return 列表可见的拼团活动ID列表
     */
    private List<Long> getAllowedGrouponIds(Long specifiedGrouponId) {
        Long currentTime = StringUtils.getCurrentTime();
        List<GrouponPromotion> grouponPromotions;
        if (specifiedGrouponId != null) {
            GrouponPromotion promotion = this.getById(specifiedGrouponId);
            if (promotion == null || promotion.getEndTime() < currentTime) {
                return List.of();
            }
            grouponPromotions = List.of(promotion);
        } else {
            grouponPromotions = this.lambdaQuery()
                    .ge(GrouponPromotion::getEndTime, currentTime)
                    .list();
        }
        if (CollUtil.isEmpty(grouponPromotions)) {
            return List.of();
        }
        return filterGrouponByUserMark(grouponPromotions);
    }

    @Override
    public Page<GrouponItemVO> itemList(GrouponItemPageQuery pageQuery) {
        // 二期（D1096）：存量 memberRange 拼团仍隐藏；新建 member_scope 拼团展示并标记 canJoin/restrictTip（与秒杀一致）
        List<Long> allowedGrouponIds = getAllowedGrouponIds(pageQuery.getGrouponPromotionId());
        if (CollUtil.isEmpty(allowedGrouponIds)) {
            return new Page<>();
        }

        MPJLambdaWrapper<GrouponPromotionItem> wrapper = new MPJLambdaWrapper<>();
        wrapper.leftJoin(Product.class, Product::getProductId, GrouponPromotionItem::getProductId)
                .like(StrUtil.isNotBlank(pageQuery.getKeyword()), Product::getProductName, pageQuery.getKeyword())
                // 仅展示上架且未删除的商品
                .eq(Product::getProductStatus, ProductStatus.ON_SALE.getCode())
                .eq(Product::getIsDelete, DeleteType.NOT_DELETE.getCode());

        if ("sale".equals(pageQuery.getSortField()) && "desc".equals(pageQuery.getSortOrder())) {
            wrapper.orderByDesc(Product::getVirtualSales);
        } else if ("sale".equals(pageQuery.getSortField()) && "asc".equals(pageQuery.getSortOrder())) {
            wrapper.orderByAsc(Product::getVirtualSales);
        }

        if ("price".equals(pageQuery.getSortField()) && "desc".equals(pageQuery.getSortOrder())) {
            wrapper.orderByDesc(GrouponPromotionItem::getMinGroupPrice);
        } else if ("price".equals(pageQuery.getSortField()) && "asc".equals(pageQuery.getSortOrder())) {
            wrapper.orderByAsc(GrouponPromotionItem::getMinGroupPrice);
        }

        wrapper.leftJoin(GrouponPromotion.class, GrouponPromotion::getGrouponPromotionId, GrouponPromotionItem::getGrouponPromotionId)
                .isNotNull(GrouponPromotion::getGrouponPromotionId)
                // 只展示用户可以参与的拼团活动商品
                .in(GrouponPromotionItem::getGrouponPromotionId, allowedGrouponIds)
                .ge(GrouponPromotion::getEndTime, StringUtils.getCurrentTime());

        // 仅保留上架且未删除的商品
        wrapper.eq(Product::getProductStatus, ProductStatus.ON_SALE.getCode())
                .eq(Product::getIsDelete, DeleteType.NOT_DELETE.getCode());

        // 批发商只看批发价：隐藏「对该批发商有批发价」的拼团商品（CR-20260713-1620）
        Set<Integer> wholesaleExcludedProductIds = wholesalePromotionFilter.currentBuyerExcludedProductIds();
        if (CollUtil.isNotEmpty(wholesaleExcludedProductIds)) {
            wrapper.notIn(GrouponPromotionItem::getProductId, wholesaleExcludedProductIds);
        }

        Page<GrouponPromotionItem> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        Page<GrouponPromotionItem> itemPage = grouponPromotionItemMapper.selectPage(page, wrapper);

        List<GrouponPromotionItem> records = itemPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        // 参与活动的商品信息
        List<Long> productIds = records.stream().map(GrouponPromotionItem::getProductId).toList();
        List<Product> products = productMapper.selectBatchIds(productIds);

        // 获取拼团记录
        List<Long> grouponItemIds = records.stream().map(GrouponPromotionItem::getGrouponPromotionItemId).toList();
        List<GrouponPromotionRecord> grouponRecords = grouponPromotionRecordService.lambdaQuery()
                .in(GrouponPromotionRecord::getGrouponPromotionItemId, grouponItemIds)
                .list();

        List<GrouponPromotionDetail> grouponPromotionDetails = grouponPromotionDetailService.lambdaQuery()
                .in(GrouponPromotionDetail::getGrouponPromotionItemId, grouponItemIds)
                .list();

        List<GrouponItemVO> resultRecords = records.stream()
                .map(item -> {
                    Product product = products.stream()
                            .filter(p -> p.getProductId().equals(item.getProductId().intValue()))
                            .findFirst()
                            .orElse(null);

                    List<GrouponPromotionRecord> currRecords = grouponRecords.stream()
                            .filter(r -> r.getGrouponPromotionItemId().equals(item.getGrouponPromotionItemId()))
                            .toList();

                    List<GrouponPromotionDetail> currDetails = grouponPromotionDetails.stream()
                            .filter(d -> d.getGrouponPromotionItemId().equals(item.getGrouponPromotionItemId()))
                            .toList();

                    GrouponPromotion grouponPromotion = this.getById(item.getGrouponPromotionId());
                    GrouponItemVO vo = new GrouponItemVO(grouponPromotion, item, product, currRecords, currDetails);
                    boolean canJoin = currentUserCanJoinGroupon(item.getGrouponPromotionId());
                    vo.setCanJoin(canJoin);
                    if (!canJoin) {
                        vo.setRestrictTip("仅限指定会员参与");
                    }
                    return vo;
                })
                .toList();

        return PageUtil.convertPage(page, resultRecords);
    }

    @Override
    public GrouponItemInfoVO getGrouponInfo(int productId) {
        Long currentTime = StringUtils.getCurrentTime();
        List<GrouponPromotion> grouponPromotions = this.lambdaQuery()
                .lt(GrouponPromotion::getStartTime, currentTime)
                .gt(GrouponPromotion::getEndTime, currentTime)
                .list();
        if (CollUtil.isEmpty(grouponPromotions)) {
            return null;
        }

        // 二期（D1096）：存量 memberRange 拼团仍隐藏；新建 member_scope 拼团保留展示（末尾标记 canJoin/restrictTip）
        List<Long> filteredGrouponIds = filterGrouponByUserMark(grouponPromotions);
        if (CollUtil.isEmpty(filteredGrouponIds)) {
            return null;
        }

        GrouponPromotionItem grouponItem = grouponPromotionItemMapper.selectOne(
                Wrappers.lambdaQuery(GrouponPromotionItem.class)
                        .in(GrouponPromotionItem::getGrouponPromotionId, filteredGrouponIds)
                        .eq(GrouponPromotionItem::getProductId, productId)
                        .last("limit 1")
        );

        if (grouponItem == null) {
            return null;
        }

        List<GrouponPromotionDetail> grouponPromotionDetails = grouponPromotionDetailService.lambdaQuery()
                .eq(GrouponPromotionDetail::getGrouponPromotionItemId, grouponItem.getGrouponPromotionItemId())
                .list();
        Set<Long> enabledSkuIds = productSkuService.lambdaQuery()
                .select(ProductSku::getSkuId)
                .eq(ProductSku::getProductId, productId)
                .eq(ProductSku::getIsEnabled, Constants.YES)
                .list()
                .stream()
                .map(ProductSku::getSkuId)
                .map(Long::valueOf)
                .collect(Collectors.toSet());
        grouponPromotionDetails = grouponPromotionDetails.stream()
                .filter(detail -> detail.getSkuId() == null || detail.getSkuId() == 0 || enabledSkuIds.contains(detail.getSkuId()))
                .toList();
        if (grouponPromotionDetails.isEmpty()) {
            return null;
        }

        GrouponItemInfoVO grouponItemInfoVO = BeanUtil.copyProperties(grouponItem, GrouponItemInfoVO.class);
        grouponPromotions.stream()
                .filter(item -> item.getGrouponPromotionId().equals(grouponItem.getGrouponPromotionId()))
                .findFirst()
                .ifPresent(item -> BeanUtil.copyProperties(item, grouponItemInfoVO));
        grouponItemInfoVO.setGrouponPromotionDetails(grouponPromotionDetails);

        // 获取最小拼团价格
        BigDecimal minGrouponPrice = grouponPromotionDetails.stream()
                .flatMap(detail ->
                        JSONArray.parseArray(detail.getGroupPrice(), GrouponPromotionItemDetailVO.GrouponPromotionDetailVO.GrouponPromotionDetailPriceVO.class).stream()
                )
                .map(GrouponPromotionItemDetailVO.GrouponPromotionDetailVO.GrouponPromotionDetailPriceVO::getPrice)
                .min(BigDecimal::compareTo)
                .get();
        grouponItemInfoVO.setMinGrouponPrice(minGrouponPrice);

        Long grouponPromotionId = grouponItemInfoVO.getGrouponPromotionId();
        GrouponPromotion grouponPromotion = grouponPromotionMapper.selectById(grouponPromotionId);
        grouponItemInfoVO.setStartTime(grouponPromotion.getStartTime().toString());
        grouponItemInfoVO.setEndTime(grouponPromotion.getEndTime().toString());

        List<GrouponItemInfoVO.GroupNumItem> groupNumItems = JsonUtil.jsonToList(grouponItem.getGroupNum(), GrouponItemInfoVO.GroupNumItem.class);
        grouponItemInfoVO.setGroupNums(groupNumItems);

        Long groupSuccessCount = grouponPromotionRecordService.lambdaQuery()
                .eq(GrouponPromotionRecord::getGrouponPromotionId, grouponPromotionId)
                .eq(GrouponPromotionRecord::getJoinType, JoinTypeEnum.OPEN_GROUP.getCode())
                .eq(GrouponPromotionRecord::getJoinStatus, JoinStatusEnum.SUCCESS.getCode())
                .eq(GrouponPromotionRecord::getProductId, productId)
                .count();
        grouponItemInfoVO.setGroupSuccessCount(groupSuccessCount);

        long joinNumCount = grouponPromotionRecordService.count(Wrappers.lambdaQuery(GrouponPromotionRecord.class)
                .eq(GrouponPromotionRecord::getProductId, productId)
                .eq(GrouponPromotionRecord::getJoinStatus, JoinStatusEnum.WAIT_GROUP.getCode()));
        grouponItemInfoVO.setJoinNumCount(joinNumCount);

        Product product = productMapper.selectById(productId);
        if (product != null) {
            grouponItemInfoVO.setProductSn(product.getProductSn());
        }

        // 二期（D1096）：标记当前用户是否可参与，前台展示价格但置灰拼团按钮
        boolean canJoin = currentUserCanJoinGroupon(grouponItemInfoVO.getGrouponPromotionId());
        grouponItemInfoVO.setCanJoin(canJoin);
        if (!canJoin) {
            grouponItemInfoVO.setRestrictTip("仅限指定会员参与");
        }

        return grouponItemInfoVO;
    }

    @Override
    public Map<Integer, BigDecimal> getGrouponMinPriceBatch(Collection<Integer> productIds) {
        Map<Integer, BigDecimal> result = new HashMap<>();
        if (CollUtil.isEmpty(productIds)) {
            return result;
        }
        Long currentTime = StringUtils.getCurrentTime();
        // 与 getGrouponInfo 一致：取当前生效的拼团活动，再按用户标识过滤
        List<GrouponPromotion> grouponPromotions = this.lambdaQuery()
                .lt(GrouponPromotion::getStartTime, currentTime)
                .gt(GrouponPromotion::getEndTime, currentTime)
                .list();
        if (CollUtil.isEmpty(grouponPromotions)) {
            return result;
        }
        List<Long> filteredGrouponIds = filterGrouponByUserMark(grouponPromotions);
        if (CollUtil.isEmpty(filteredGrouponIds)) {
            return result;
        }
        // 一次查出本页商品在生效活动下的 item；每商品取首个，等价原逐商品 selectOne(...).last("limit 1")
        List<GrouponPromotionItem> items = grouponPromotionItemMapper.selectList(
                Wrappers.lambdaQuery(GrouponPromotionItem.class)
                        .in(GrouponPromotionItem::getGrouponPromotionId, filteredGrouponIds)
                        .in(GrouponPromotionItem::getProductId, productIds));
        if (CollUtil.isEmpty(items)) {
            return result;
        }
        Map<Integer, GrouponPromotionItem> itemByProductId = new HashMap<>();
        for (GrouponPromotionItem item : items) {
            if (item.getProductId() != null) {
                itemByProductId.putIfAbsent(item.getProductId().intValue(), item);
            }
        }
        // 一次查出这些 item 的全部明细，按 itemId 分组
        List<Long> itemIds = itemByProductId.values().stream()
                .map(GrouponPromotionItem::getGrouponPromotionItemId)
                .toList();
        Map<Long, List<GrouponPromotionDetail>> detailsByItemId = grouponPromotionDetailService.lambdaQuery()
                .in(GrouponPromotionDetail::getGrouponPromotionItemId, itemIds)
                .list().stream()
                .collect(Collectors.groupingBy(GrouponPromotionDetail::getGrouponPromotionItemId));
        // 逐商品在内存算最低拼团价，口径与 getGrouponInfo 完全一致
        for (Map.Entry<Integer, GrouponPromotionItem> entry : itemByProductId.entrySet()) {
            List<GrouponPromotionDetail> details = detailsByItemId.get(entry.getValue().getGrouponPromotionItemId());
            if (CollUtil.isEmpty(details)) {
                continue;
            }
            details.stream()
                    .flatMap(detail -> JSONArray.parseArray(detail.getGroupPrice(), GrouponPromotionItemDetailVO.GrouponPromotionDetailVO.GrouponPromotionDetailPriceVO.class).stream())
                    .map(GrouponPromotionItemDetailVO.GrouponPromotionDetailVO.GrouponPromotionDetailPriceVO::getPrice)
                    .min(BigDecimal::compareTo)
                    .ifPresent(price -> result.put(entry.getKey(), price));
        }
        return result;
    }

    @Override
    public CartPromotionParsePriceDTO parsePrice(List<CartVO> carts, PromotionVO promotion) {
        List<GrouponPromotionDetail> grouponPromotionDetails = grouponPromotionDetailService.lambdaQuery()
                .eq(GrouponPromotionDetail::getGrouponPromotionId, promotion.getRelationId())
                .list();

        for (CartVO cart : carts) {
            AddToCartDTO.GrouponData grouponData = JSON.parseObject(cart.getExtraData(), AddToCartDTO.GrouponData.class);
            if (grouponData == null) {
                continue;
            }

            grouponPromotionDetails.stream()
                    .filter(detail -> detail.getGrouponDetailId().equals(grouponData.getGrouponDetailId()))
                    .forEach(detail -> {
                        cart.setPrice(grouponData.getPrice());

                        BigDecimal amount = grouponData.getPrice();
                        // BigDecimal discountAmount = amount.add(cart.getMemberPrice().subtract(cart.getPrice()).multiply(new BigDecimal(cart.getQuantity())));
                        BigDecimal discountAmount = cart.getMemberPrice().subtract(cart.getPrice()).multiply(new BigDecimal(cart.getQuantity()));
                        discountAmount = discountAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : discountAmount;
                        promotion.setAmount(discountAmount);
                    });
        }

        CartPromotionParsePriceDTO cartPromotionParsePriceDTO = new CartPromotionParsePriceDTO();
        cartPromotionParsePriceDTO.setCartList(carts);
        cartPromotionParsePriceDTO.setPromotion(promotion);
        return cartPromotionParsePriceDTO;
    }

    @Override
    public Page<GrouponPromotionImageVO> grouponPromotionImages(BasePage pageQuery) {
        Long currentTime = StringUtils.getCurrentTime();

        // 查询进行中的拼团活动
        Page<GrouponPromotion> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        Page<GrouponPromotion> grouponPage = this.lambdaQuery()
                .le(GrouponPromotion::getStartTime, currentTime)
                .ge(GrouponPromotion::getEndTime, currentTime)
                .orderByDesc(GrouponPromotion::getStartTime)
                .page(page);

        List<GrouponPromotion> records = grouponPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        // 根据用户标识过滤拼团活动
        List<Long> allowedGrouponIds = filterGrouponByUserMark(records);
        if (CollUtil.isEmpty(allowedGrouponIds)) {
            return new Page<>();
        }

        // 转换为 VO
        List<GrouponPromotionImageVO> voList = records.stream()
                .filter(item -> allowedGrouponIds.contains(item.getGrouponPromotionId()))
                .map(item -> {
                    GrouponPromotionImageVO vo = new GrouponPromotionImageVO();
                    vo.setGrouponPromotionId(item.getGrouponPromotionId());
                    vo.setName(item.getName());
                    vo.setGroupType(item.getGroupType());
                    vo.setGrouponImage(item.getGrouponImage());
                    vo.setPcGrouponImage(item.getPcGrouponImage());
                    vo.setShareImage(item.getShareImage());
                    vo.setShopId(item.getShopId());
                    vo.setAddTime(item.getAddTime());
                    return vo;
                })
                .toList();

        return PageUtil.convertPage(grouponPage, voList);
    }

}
