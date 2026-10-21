package com.tigshop.service.promotion.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.cart.CartPromotionParsePriceDTO;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.enums.promotion.PromotionRangeEnum;
import com.tigshop.bean.enums.promotion.PromotionTimeStatusEnum;
import com.tigshop.bean.enums.promotion.PromotionType;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.promotion.*;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.param.promotion.timediscount.TimeDiscountEditParam;
import com.tigshop.bean.param.promotion.timediscount.TimeDiscountItemParam;
import com.tigshop.bean.param.promotion.timediscount.TimeDiscountSaveParam;
import com.tigshop.bean.query.promotion.timediscount.TimeDiscountPageQuery;
import com.tigshop.bean.vo.cart.CartVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.bean.vo.promotion.TimeDiscountDetailVO;
import com.tigshop.bean.vo.promotion.TimeDiscountVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.promotion.GrouponPromotionItemMapper;
import com.tigshop.mapper.promotion.TimeDiscountMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.promotion.PromotionService;
import com.tigshop.service.promotion.SeckillItemService;
import com.tigshop.service.promotion.TimeDiscountItemService;
import com.tigshop.service.promotion.TimeDiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import static com.tigshop.common.constant.promotion.TimeDiscountConstants.PRODUCT_EXIST_SECKILL;
import static com.tigshop.common.constant.promotion.TimeDiscountConstants.PRODUCT_EXIST_TIME_DISCOUNT;

/**
 * 限时折扣服务实现类
 *
 * @author kidd
 * @create 2025/7/1
 */
@RequiredArgsConstructor
@Service
public class TimeDiscountServiceImpl extends BaseServiceImpl<TimeDiscountMapper, TimeDiscount> implements TimeDiscountService {

    private final TimeDiscountItemService timeDiscountItemService;

    private final AdminLogService adminLogService;

    private final PromotionService promotionService;

    private final SeckillItemService seckillItemService;

    private final ProductMapper productMapper;

    private final ProductSkuService productSkuService;
    private final GrouponPromotionItemMapper grouponPromotionItemMapper;
    private final com.tigshop.common.core.TranslatePackage translatePackage;

    private final com.tigshop.service.user.MemberAudienceService memberAudienceService;

    private final UserMapper userMapper;

    @Override
    public Page<TimeDiscountVO> list(TimeDiscountPageQuery pageQuery) {
        Page<TimeDiscount> page = buildSortOrder(pageQuery);
        Integer shopId = HeaderUtils.getShopId();
        Integer state = pageQuery.getActiveState();
        long currentTime = StringUtils.getCurrentTime();

        Page<TimeDiscount> timeDiscountPage = this.lambdaQuery()
                .like(StrUtil.isNotBlank(pageQuery.getKeyword()), TimeDiscount::getPromotionName, pageQuery.getKeyword())
                .eq(shopId != null, TimeDiscount::getShopId, shopId)
                .and(state != null && PromotionTimeStatusEnum.PROMOTION_STATUS_ON.getCode() == state, query -> query.le(TimeDiscount::getStartTime, currentTime).ge(TimeDiscount::getEndTime, currentTime))
                .gt(state != null && PromotionTimeStatusEnum.PROMOTION_STATUS_FORTHCOMING.getCode() == state, TimeDiscount::getStartTime, currentTime)
                .lt(state != null && PromotionTimeStatusEnum.PROMOTION_STATUS_OFF.getCode() == state, TimeDiscount::getEndTime, currentTime)
                .page(page);

        List<TimeDiscount> timeDiscountPageRecords = timeDiscountPage.getRecords();

        if (CollUtil.isEmpty(timeDiscountPageRecords)) {
            return new Page<>();
        }

        List<TimeDiscountVO> timeDiscountVOList = timeDiscountPageRecords.stream()
                .map(timeDiscount -> {
                    TimeDiscountVO timeDiscountVO = new TimeDiscountVO(timeDiscount);
                    timeDiscountVO.setStatusName(PromotionTimeStatusEnum.handleStatusName(timeDiscount.getStartTime(), timeDiscount.getEndTime()));
                    return timeDiscountVO;
                }).toList();
        return PageUtil.convertPage(timeDiscountPage, timeDiscountVOList);
    }

    @Override
    public TimeDiscountDetailVO detail(Integer id) {
        TimeDiscount timeDiscount = this.getById(id);
        Assert.notNull(timeDiscount, () -> new GlobalException("未知的限时折扣"));

        List<TimeDiscountItem> timeDiscountItems = timeDiscountItemService.lambdaQuery().eq(TimeDiscountItem::getDiscountId, id).list();

        // 查询商品
        List<Integer> productIds = timeDiscountItems.stream().map(TimeDiscountItem::getProductId).toList();
        List<Product> products = productMapper.selectBatchIds(productIds);

        // 查询商品规格
        List<ProductSku> productSkus = productSkuService.lambdaQuery().in(ProductSku::getProductId, productIds).list();

        TimeDiscountDetailVO detailVO = new TimeDiscountDetailVO(timeDiscount, timeDiscountItems, products, productSkus);

        // 从 promotion 表获取适用会员范围字段
        Promotion promotion = promotionService.lambdaQuery()
                .eq(Promotion::getRelationId, id)
                .eq(Promotion::getType, PromotionType.TYPE_LIMIT_DISCOUNT.getCode())
                .one();
        if (promotion != null && detailVO.getDiscountInfo() != null) {
            detailVO.getDiscountInfo().setMemberRange(promotion.getMemberRange());
            detailVO.getDiscountInfo().setMemberMarks(StringUtils.str2IntList(promotion.getMemberMarks()));
            com.tigshop.bean.dto.user.SendUserTargetDTO sendUserTarget =
                    memberAudienceService.parseScope(promotion.getMemberScope());
            detailVO.getDiscountInfo().setSendUserTarget(memberAudienceService.fillTargetUsers(sendUserTarget));
            detailVO.getDiscountInfo().setMemberAudience(memberAudienceService.toLegacy(sendUserTarget));
        }

        // ProductSkuInfo.skuValue（拼接串）与 skuData（hutool JSONArray）绕过 @JsonTranslate，手动翻译
        if (detailVO.getDiscountInfo() != null && detailVO.getDiscountInfo().getItem() != null) {
            detailVO.getDiscountInfo().getItem().forEach(item -> {
                if (item.getProduct() == null || item.getProduct().getProductSku() == null) {
                    return;
                }
                item.getProduct().getProductSku().forEach(skuInfo -> {
                    skuInfo.setSkuValue(com.tigshop.common.utils.SkuValueTranslateUtils.translateSkuValue(skuInfo.getSkuValue(), translatePackage));
                    cn.hutool.json.JSONArray skuData = skuInfo.getSkuData();
                    if (skuData != null) {
                        for (int i = 0; i < skuData.size(); i++) {
                            cn.hutool.json.JSONObject obj = skuData.getJSONObject(i);
                            if (obj == null) {
                                continue;
                            }
                            for (String key : new String[]{"name", "value"}) {
                                String val = obj.getStr(key);
                                if (StrUtil.isNotBlank(val)) {
                                    obj.set(key, translatePackage.translate(val, com.tigshop.common.enums.DataType.SKU.getCode()));
                                }
                            }
                            String attrPic = obj.getStr("attrPicThumb");
                            if (StrUtil.isNotBlank(attrPic)) {
                                obj.set("attrPicThumb", translatePackage.translate(attrPic, com.tigshop.common.enums.DataType.IMAGE_TITLE.getCode()));
                            }
                        }
                    }
                });
            });
        }

        return detailVO;
    }

    /**
     * 处理状态
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return String
     */
    public String handleStatusName(Long startTime, Long endTime) {
        long currentTime = StringUtils.getCurrentTime();
        if (currentTime >= startTime && currentTime <= endTime) {
            return PromotionTimeStatusEnum.PROMOTION_STATUS_ON.getDescription();
        } else if (currentTime < startTime) {
            return PromotionTimeStatusEnum.PROMOTION_STATUS_FORTHCOMING.getDescription();
        } else {
            return PromotionTimeStatusEnum.PROMOTION_STATUS_OFF.getDescription();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(TimeDiscountSaveParam param) {
        // 1. 判断商品是否已有活动
        Long startTime = StringUtils.dateToTimestampExample(param.getStartTime());
        Long endTime = StringUtils.dateToTimestampExample(param.getEndTime());
        checkPromotionByProduct(null, param.getProductIds(), startTime, endTime);

        TimeDiscount timeDiscount = param.createTimeDiscount();
        this.save(timeDiscount);

        // 2. 活动商品
        List<TimeDiscountItem> timeDiscountItems = param.createTimeDiscountItems();
        timeDiscountItemService.saveBatchTimeDiscountItem(timeDiscountItems, timeDiscount);

        // 3. 营销写入
        savePromotion(param.getItem(), timeDiscount, param.getMemberRange(), param.getMemberMarks(),
                memberAudienceService.resolveWriteScope(param.getSendUserTarget(), param.getMemberAudience()));

        // 4. 保存日志
        adminLogService.createByLogInfo(StrUtil.format("新增限时折扣活动:{}", timeDiscount.getPromotionName()));
    }

    /**
     * 校验商品是否参与其他活动
     *
     * @param discountId 限时折扣 ID
     * @param productIds 活动商品
     * @param startTime  活动开始时间
     * @param endTime    活动结束时间
     */
    private void checkPromotionByProduct(Integer discountId, List<Integer> productIds, Long startTime, Long endTime) {
        // 限时折扣判断
        long timeDiscountCount = timeDiscountItemService.lambdaQuery()
                .in(TimeDiscountItem::getProductId, productIds)
                .le(TimeDiscountItem::getStartTime, endTime)
                .ge(TimeDiscountItem::getEndTime, startTime)
                .ne(null != discountId, TimeDiscountItem::getDiscountId, discountId)
                .count();
        Assert.isTrue(timeDiscountCount == 0, () -> new GlobalException(PRODUCT_EXIST_TIME_DISCOUNT));

        // 秒杀活动校验
        Long seckillItemCount = seckillItemService.lambdaQuery()
                .in(SeckillItem::getProductId, productIds)
                .le(SeckillItem::getSeckillStartTime, endTime)
                .ge(SeckillItem::getSeckillEndTime, startTime)
                .count();
        Assert.isTrue(seckillItemCount == 0, () -> new GlobalException(PRODUCT_EXIST_SECKILL));

        // 团购活动校验
        if (grouponPromotionItemMapper.countProductConflict(productIds.getFirst().longValue(), null, startTime, endTime) > 0) {
            throw new GlobalException("该商品在时间段内已参与其他拼团活动，不能重复绑定");
        }
    }

    /**
     * 保存营销活动
     *
     * @param timeDiscountItem 活动商品信息
     * @param timeDiscount     活动
     * @param memberRange      适用会员范围
     * @param memberMarks      选择的会员标记列表
     */
    private void savePromotion(List<TimeDiscountItemParam> timeDiscountItem, TimeDiscount timeDiscount, Integer memberRange, List<Integer> memberMarks, String memberScope) {
        Integer relationId = timeDiscount.getDiscountId() == null ? 0 : timeDiscount.getDiscountId();
        String memberMarksStr = memberMarks != null ? memberMarks.toString() : null;
        Long timeDiscountCount = promotionService.lambdaQuery()
                .eq(Promotion::getRelationId, relationId)
                .eq(Promotion::getType, PromotionType.TYPE_LIMIT_DISCOUNT.getCode())
                .count();
        // 根据 relationId 查询是否有营销活动
        if (timeDiscountCount > 0) {
            List<Integer> rangeData = timeDiscountItem.stream().map(TimeDiscountItemParam::getProductId).toList();

            List<Integer> skuIds = timeDiscountItem.stream()
                    .filter(item -> CollUtil.isNotEmpty(item.getSkuIds()))
                    .flatMap(dto -> dto.getSkuIds().stream())
                    .toList();

            // 更新部分字段
            promotionService.lambdaUpdate()
                    .eq(Promotion::getRelationId, relationId)
                    .eq(Promotion::getType, PromotionType.TYPE_LIMIT_DISCOUNT.getCode())
                    .set(Promotion::getStartTime, timeDiscount.getStartTime())
                    .set(Promotion::getEndTime, timeDiscount.getEndTime())
                    .set(Promotion::getPromotionName, timeDiscount.getPromotionName())
                    .set(Promotion::getRangeData, rangeData.toString())
                    .set(Promotion::getSkuIds, skuIds.toString())
                    .set(Promotion::getMemberRange, memberRange)
                    .set(Promotion::getMemberMarks, memberMarksStr)
                    .set(Promotion::getMemberScope, memberScope)
                    .update();

        }
        if (timeDiscountCount == 0) {
            Integer shopId = HeaderUtils.getShopId() == null ? 0 : HeaderUtils.getShopId();

            Promotion promotion = new Promotion();
            promotion.setPromotionName(timeDiscount.getPromotionName());
            promotion.setStartTime(timeDiscount.getStartTime());
            promotion.setEndTime(timeDiscount.getEndTime());
            promotion.setType(PromotionType.TYPE_LIMIT_DISCOUNT.getCode());
            promotion.setShopId(shopId);
            promotion.setRelationId(relationId);
            promotion.setRange(PromotionRangeEnum.PROMOTION_RANGE_PRODUCT.getCode());
            promotion.setMemberRange(memberRange);
            promotion.setMemberMarks(memberMarksStr);
            promotion.setMemberScope(memberScope);

            List<Integer> rangeData = timeDiscountItem.stream().map(TimeDiscountItemParam::getProductId).toList();
            promotion.setRangeData(rangeData.toString());

            List<Integer> skuIds = timeDiscountItem.stream()
                    .filter(item -> CollUtil.isNotEmpty(item.getSkuIds()))
                    .flatMap(dto -> dto.getSkuIds().stream())
                    .toList();
            promotion.setSkuIds(skuIds.toString());

            promotionService.save(promotion);
        }
    }

    @Transactional
    @Override
    public void update(TimeDiscountEditParam param) {
        TimeDiscount timeDiscount = this.getById(param.getDiscountId());
        Assert.notNull(timeDiscount, () -> new GlobalException("未知的限时折扣"));

        // 1. 判断商品是否已有活动
        Long startTime = StringUtils.dateToTimestampExample(param.getStartTime());
        Long endTime = StringUtils.dateToTimestampExample(param.getEndTime());
        checkPromotionByProduct(timeDiscount.getDiscountId(), param.getProductIds(), startTime, endTime);

        // 2. 更新限时折扣
        TimeDiscount editTimeDiscount = param.createTimeDiscount();
        this.updateById(editTimeDiscount);
        timeDiscount = this.getById(param.getDiscountId());

        // 3. 更新活动商品
        timeDiscountItemService.updateBatchTimeDiscountItem(param.createTimeDiscountItems(), timeDiscount);

        // 4. 营销写入
        savePromotion(param.getItem(), timeDiscount, param.getMemberRange(), param.getMemberMarks(),
                memberAudienceService.resolveWriteScope(param.getSendUserTarget(), param.getMemberAudience()));

        // 5. 保存日志
        adminLogService.createByLogInfo(StrUtil.format("更新限时折扣活动:{}", timeDiscount.getPromotionName()));
    }

    @Transactional
    @Override
    public boolean del(Integer id) {
        this.removeById(id);

        // 删除活动商品
        timeDiscountItemService.lambdaUpdate().eq(TimeDiscountItem::getDiscountId, id).remove();

        // 删除对应营销活动
        promotionService.lambdaUpdate()
                .eq(Promotion::getRelationId, id)
                .eq(Promotion::getType, PromotionType.TYPE_LIMIT_DISCOUNT.getCode())
                .remove();

        return true;
    }

    @Transactional
    @Override
    public boolean batch(BatchDTO batchDTO) {
        if ("del".equals(batchDTO.getType())) {

            List<Integer> ids = batchDTO.getIds();
            this.removeByIds(ids);

            // 删除活动商品
            timeDiscountItemService.lambdaUpdate().in(TimeDiscountItem::getDiscountId, ids).remove();

            // 删除对应营销活动
            promotionService.lambdaUpdate()
                    .in(Promotion::getRelationId, ids)
                    .eq(Promotion::getType, PromotionType.TYPE_LIMIT_DISCOUNT.getCode())
                    .remove();
        }
        return true;
    }

    @Override
    public BigDecimal getTimeDiscountPrice(BigDecimal productPrice, Integer skuId, TimeDiscountItem discountItem) {
        BigDecimal price = productPrice;
        List<Integer> skuIds = JsonUtil.jsonToList(discountItem.getSkuIds(), Integer.class);
        if (CollUtil.isEmpty(skuIds) || (CollUtil.isNotEmpty(skuIds) && skuId != null && skuIds.contains(skuId))) {
            if (discountItem.getDiscountType() == 3) {
                price = discountItem.getValue();
            } else if (discountItem.getDiscountType() == 2) {
                price = productPrice.subtract(discountItem.getValue());
            } else if (discountItem.getDiscountType() == 1) {
                price = productPrice.multiply(discountItem.getValue().divide(new BigDecimal(10), 2, RoundingMode.HALF_UP));
            }
        }
        return price;
    }

    @Override
    public CartPromotionParsePriceDTO parsePrice(List<CartVO> cartList, PromotionVO promotionVO) {
        List<TimeDiscountItem> list = timeDiscountItemService.lambdaQuery().eq(TimeDiscountItem::getDiscountId, promotionVO.getRelationId()).list();

        for (CartVO cart : cartList) {

            for (TimeDiscountItem item : list) {
                List<Integer> skuIds = JsonUtil.jsonToList(item.getSkuIds(), Integer.class);
                if (Objects.equals(item.getProductId(), cart.getProductId()) && (skuIds.contains(cart.getSkuId()) || cart.getSkuId() == 0)) {
                    // ProductService.getProductFinalPrice 可能已经按 kaic 的详情口径预套限时价。
                    // 以会员价为统一基准计算目标价：当前价已等于目标价时只保留该价格，不再重复累计优惠。
                    BigDecimal basePrice = cart.getMemberPrice();
                    BigDecimal discountPrice = getTimeDiscountPrice(basePrice, cart.getSkuId(), item);
                    BigDecimal currentPrice = cart.getPrice() == null ? cart.getProductPrice() : cart.getPrice();
                    boolean alreadyApplied = currentPrice != null && currentPrice.compareTo(discountPrice) <= 0;
                    if (!alreadyApplied) {
                        cart.setPrice(discountPrice);
                    }

                    BigDecimal discountAmount = alreadyApplied ? BigDecimal.ZERO
                            : currentPrice.subtract(discountPrice).multiply(new BigDecimal(cart.getQuantity()));
                    discountAmount = discountAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : discountAmount;
                    BigDecimal amount = promotionVO.getAmount() == null ? BigDecimal.ZERO : promotionVO.getAmount();
                    promotionVO.setAmount(amount.add(discountAmount));
                }
            }
        }

        CartPromotionParsePriceDTO cartPromotionParsePriceDTO = new CartPromotionParsePriceDTO();
        cartPromotionParsePriceDTO.setCartList(cartList);
        cartPromotionParsePriceDTO.setPromotion(promotionVO);
        return cartPromotionParsePriceDTO;
    }

    @Override
    public boolean canUserJoinTimeDiscount(Integer productId, Integer skuId, Integer userId) {
        // 未登录不拦截（与列表/详情人群判定一致）
        if (userId == null || userId <= 0) {
            return true;
        }
        long currentTime = StringUtils.getCurrentTime();
        List<TimeDiscountItem> items = timeDiscountItemService.lambdaQuery()
                .eq(TimeDiscountItem::getProductId, productId)
                .le(TimeDiscountItem::getStartTime, currentTime)
                .ge(TimeDiscountItem::getEndTime, currentTime)
                .list();
        for (TimeDiscountItem item : items) {
            // 只对真正作用于该 SKU 的活动做人群判定，适用性口径与 getTimeDiscountPrice 保持一致
            if (!appliesToSku(item, skuId)) {
                continue;
            }
            Promotion promotion = promotionService.lambdaQuery()
                    .eq(Promotion::getType, PromotionType.TYPE_LIMIT_DISCOUNT.getCode())
                    .eq(Promotion::getRelationId, item.getDiscountId())
                    .last("limit 1")
                    .one();
            if (promotion == null) {
                continue;
            }
            String memberScope = promotion.getMemberScope();
            boolean canJoin = StringUtils.isNotBlank(memberScope)
                    ? memberAudienceService.matchesScope(userId.longValue(), memberScope)
                    : matchesLegacyMemberMark(promotion, userId);
            if (!canJoin) {
                return false;
            }
        }
        return true;
    }

    /**
     * skuIds 为空表示活动作用于该商品全部规格，非空则只作用于列出的规格。
     */
    private boolean appliesToSku(TimeDiscountItem item, Integer skuId) {
        List<Integer> skuIds = JSON.parseArray(item.getSkuIds(), Integer.class);
        return CollUtil.isEmpty(skuIds) || (skuId != null && skuIds.contains(skuId));
    }

    /**
     * 存量 memberRange/memberMarks 口径判定（CR-20260730-1745）。
     * 已登录但未设标记的用户同样视为不在名单内，与 PromotionServiceImpl 活动总线、SeckillServiceImpl 保持一致。
     */
    private boolean matchesLegacyMemberMark(Promotion promotion, Integer userId) {
        if (!Objects.equals(1, promotion.getMemberRange())) {
            return true;
        }
        List<Integer> allowedMarks = JSON.parseArray(promotion.getMemberMarks(), Integer.class);
        if (CollUtil.isEmpty(allowedMarks)) {
            return false;
        }
        User user = userMapper.selectById(userId);
        Integer mark = user == null ? null : user.getMark();
        return mark != null && allowedMarks.contains(mark);
    }

}
