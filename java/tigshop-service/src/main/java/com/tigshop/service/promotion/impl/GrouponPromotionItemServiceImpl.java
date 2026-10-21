package com.tigshop.service.promotion.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.enums.promotion.PromotionType;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductGroup;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.promotion.*;
import com.tigshop.bean.param.promotion.groupon.PromotionPromotionItemCreateParam;
import com.tigshop.bean.param.promotion.groupon.PromotionPromotionItemUpdateParam;
import com.tigshop.bean.query.promotion.groupon.GrouponProductPageQuery;
import com.tigshop.bean.query.promotion.groupon.GrouponPromotionItemPageQuery;
import com.tigshop.bean.vo.promotion.GrouponPostVO;
import com.tigshop.bean.vo.promotion.GrouponProductVO;
import com.tigshop.bean.vo.promotion.GrouponPromotionItemDetailVO;
import com.tigshop.bean.vo.promotion.GrouponPromotionItemPageVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.constant.promotion.TimeDiscountConstants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.promotion.*;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductGroupService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.promotion.GrouponPromotionDetailService;
import com.tigshop.service.promotion.GrouponPromotionItemService;
import com.tigshop.service.promotion.SeckillItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tigshop.bean.enums.product.ProductType.isReservationProduct;
import static com.tigshop.common.constant.promotion.TimeDiscountConstants.PRODUCT_EXIST_TIME_DISCOUNT;

/**
 * @author tigshop
 */
@RequiredArgsConstructor
@Service
public class GrouponPromotionItemServiceImpl extends BaseServiceImpl<GrouponPromotionItemMapper, GrouponPromotionItem> implements GrouponPromotionItemService {

    private final GrouponPromotionDetailService grouponPromotionDetailService;
    private final ProductService productService;
    private final ProductSkuService productSkuService;
    private final ProductGroupService productGroupService;
    private final SeckillItemService seckillItemService;
    private final ProductMapper productMapper;
    private final PromotionMapper promotionMapper;
    private final GrouponPromotionMapper grouponPromotionMapper;
    private final TimeDiscountItemMapper timeDiscountItemMapper;
    private final SeckillItemMapper seckillItemMapper;
    private final TigshopProperties tigshopProperties;
    private final com.tigshop.common.core.TranslatePackage translatePackage;

    @Transactional
    @Override
    public void create(PromotionPromotionItemCreateParam param) {
        Long grouponPromotionId = param.getGrouponPromotionId();
        GrouponPromotion grouponPromotion = grouponPromotionMapper.selectById(grouponPromotionId);

        checkPromotionByProduct(grouponPromotionId, param.getProductId(), grouponPromotion.getStartTime(), grouponPromotion.getEndTime());

        GrouponPromotionItem grouponPromotionItem = param.buildGrouponPromotionItem();
        this.save(grouponPromotionItem);

        List<GrouponPromotionDetail> grouponPromotionDetails = param.buildGrouponPromotionDetails(grouponPromotionItem.getGrouponPromotionItemId());
        validateEnabledSkus(grouponPromotionDetails);
        grouponPromotionDetailService.saveBatch(grouponPromotionDetails);

        updatePromotionData(param);
    }

    /**
     * 更新 promotion 数据
     *
     * @param param 更新参数
     */
    private void updatePromotionData(PromotionPromotionItemCreateParam param) {
        Long grouponPromotionId = param.getGrouponPromotionId();
        GrouponPromotion grouponPromotion = grouponPromotionMapper.selectById(grouponPromotionId);

        checkPromotionByProduct(grouponPromotionId, param.getProductId(), grouponPromotion.getStartTime(), grouponPromotion.getEndTime());

        updPromotion(grouponPromotionId);
    }

    private void updPromotion(Long grouponPromotionId) {
        List<GrouponPromotionDetail> grouponPromotionDetails = grouponPromotionDetailService.lambdaQuery()
                .eq(GrouponPromotionDetail::getGrouponPromotionId,grouponPromotionId).list();

        Set<Long> skuIds = grouponPromotionDetails.stream()
                .map(GrouponPromotionDetail::getSkuId)
                .filter(skuId -> skuId != null && skuId != 0)
                .collect(Collectors.toSet());

        Set<Long> productIds = grouponPromotionDetails.stream()
                .map(GrouponPromotionDetail::getProductId)
                .filter(productId -> productId != null && productId != 0)
                .collect(Collectors.toSet());

        // 更新 promotion 数据
        promotionMapper.update(
                Wrappers.lambdaUpdate(Promotion.class)
                        .eq(Promotion::getType, PromotionType.TYPE_GROUPON.getCode())
                        .eq(Promotion::getRelationId, grouponPromotionId)
                        .set(Promotion::getRangeData, productIds.toString())
                        .set(Promotion::getSkuIds, skuIds.toString())
        );
    }

    private void checkPromotionByProduct(Long grouponPromotionId, Long productId, Long startTime, Long endTime) {
        // 秒杀活动校验 - 通过关联查询seckill表获取shop_id进行比较
        Long seckillItemCount = seckillItemService.lambdaQuery()
                .in(SeckillItem::getProductId, productId)
                .le(SeckillItem::getSeckillStartTime, startTime)
                .ge(SeckillItem::getSeckillEndTime, endTime)
                .count();
        Assert.isTrue(seckillItemCount == 0, () -> new GlobalException(TimeDiscountConstants.PRODUCT_EXIST_SECKILL));
        // 限时折扣校验
        if (timeDiscountItemMapper.selectCount(new LambdaQueryWrapper<TimeDiscountItem>()
                .eq(TimeDiscountItem::getProductId, productId)
                .le(TimeDiscountItem::getStartTime, startTime)
                .ge(TimeDiscountItem::getEndTime, endTime)) > 0) {
            throw new GlobalException(PRODUCT_EXIST_TIME_DISCOUNT);
        }

        long conflictCount = this.baseMapper.getInGroupOnProductCount(
                productId, grouponPromotionId, StringUtils.getCurrentTime(), endTime);
        if (conflictCount > 0) {
            throw new GlobalException("该商品在时间段内已参与其他拼团活动，不能重复绑定");
        }
    }

    @Transactional
    @Override
    public void update(PromotionPromotionItemUpdateParam param) {
        GrouponPromotionItem dbGrouponPromotionItem = this.getById(param.getGrouponPromotionItemId());
        Assert.notNull(dbGrouponPromotionItem, () -> new GlobalException("未知的拼团活动商品"));

        GrouponPromotionItem grouponPromotionItem = param.buildGrouponPromotionItem();
        this.updateById(grouponPromotionItem);

        List<GrouponPromotionDetail> grouponPromotionDetails = param.buildGrouponPromotionDetails(grouponPromotionItem.getGrouponPromotionItemId());
        validateEnabledSkus(grouponPromotionDetails);
        grouponPromotionDetailService.updateBatchById(grouponPromotionDetails);

        updatePromotionData(param);
    }

    private void validateEnabledSkus(List<GrouponPromotionDetail> details) {
        details.stream()
                .filter(detail -> detail.getSkuId() != null && detail.getSkuId() > 0)
                .forEach(detail -> productSkuService.checkSkuEnabled(
                        detail.getProductId().intValue(), detail.getSkuId().intValue()));
    }

    @Transactional
    @Override
    public boolean del(Integer id) {
        GrouponPromotionItem dbGrouponPromotionItem = this.getById(id);
        Assert.notNull(dbGrouponPromotionItem, () -> new GlobalException("未知的拼团活动商品"));

        this.removeById(id);

        grouponPromotionDetailService.lambdaUpdate()
                .eq(GrouponPromotionDetail::getGrouponPromotionItemId, id)
                .remove();

        // TODO 是否需要更新 promotion 数据
        updPromotion(dbGrouponPromotionItem.getGrouponPromotionId());

        return true;
    }

    @Transactional
    @Override
    public boolean batch(BatchDTO batchDTO) {
        if ("del".equals(batchDTO.getType())) {
            this.removeByIds(batchDTO.getIds());

            grouponPromotionDetailService.lambdaUpdate()
                    .in(GrouponPromotionDetail::getGrouponPromotionItemId, batchDTO.getIds())
                    .remove();
        }

        return true;
    }

    @Override
    public GrouponPromotionItemDetailVO detail(Integer grouponPromotionItemId) {
        GrouponPromotionItem dbGrouponPromotionItem = this.getById(grouponPromotionItemId);
        Assert.notNull(dbGrouponPromotionItem, () -> new GlobalException("未知的拼团活动商品"));

        List<GrouponPromotionDetail> dbGrouponPromotionDetails = grouponPromotionDetailService.lambdaQuery()
                .eq(GrouponPromotionDetail::getGrouponPromotionItemId, grouponPromotionItemId)
                .list();

        GrouponPromotionItemDetailVO grouponPromotionItemDetailVO = new GrouponPromotionItemDetailVO(dbGrouponPromotionItem, dbGrouponPromotionDetails);
        Product product = productMapper.selectById(dbGrouponPromotionItem.getProductId());
        grouponPromotionItemDetailVO.setProductPrice(product.getProductPrice());
        grouponPromotionItemDetailVO.setProductStock(product.getProductStock());
        grouponPromotionItemDetailVO.setProductSkus(productSkuService.getProductSkusByPid(dbGrouponPromotionItem.getProductId().intValue()));

        return grouponPromotionItemDetailVO;
    }

    @Override
    public GrouponPostVO post(Long grouponPromotionItemId) {
        return null;
    }

    @Override
    public Page<GrouponPromotionItemPageVO> list(GrouponPromotionItemPageQuery pageQuery) {
        Page<GrouponPromotionItem> page = buildSortOrder(pageQuery);

        if (pageQuery.getProductGroupId() != null) {
            ProductGroup productGroup = productGroupService.getById(pageQuery.getProductGroupId());
            List<Integer> groupProductIds = StringUtils.str2IntList(productGroup.getProductIds());
            pageQuery.setGroupProductIds(CollUtil.isNotEmpty(groupProductIds) ? groupProductIds : List.of(0));
        }

        Page<GrouponPromotionItemPageVO> resultPage = this.baseMapper.page(page, pageQuery);

        List<GrouponPromotionItemPageVO> records = resultPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        List<Long> grouponPromotionItemIds = records.stream().map(GrouponPromotionItemPageVO::getGrouponPromotionItemId).toList();
        List<GrouponPromotionDetail> grouponPromotionDetails = grouponPromotionDetailService.lambdaQuery()
                .in(GrouponPromotionDetail::getGrouponPromotionItemId, grouponPromotionItemIds)
                .list();

        records.forEach(item -> {
            List<GrouponPromotionDetail> currGrouponPromotionDetails = grouponPromotionDetails.stream()
                    .filter(detail -> detail.getGrouponPromotionItemId().equals(item.getGrouponPromotionItemId()))
                    .toList();
            item.assembleData(currGrouponPromotionDetails);
        });

        return resultPage;
    }

    @Override
    public Page<GrouponProductVO> productList(GrouponProductPageQuery pageQuery) {
        Page<Product> productPage = productService.productList(pageQuery);

        List<Product> records = productPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        List<Integer> allProductIds = records.stream().map(Product::getProductId).toList();
        List<ProductSku> productSkus = productSkuService.lambdaQuery()
                .in(ProductSku::getProductId, allProductIds)
                .eq(ProductSku::getIsEnabled, Constants.YES)
                .list();

        GrouponPromotion grouponPromotion = grouponPromotionMapper.selectById(pageQuery.getGrouponPromotionId());
        List<GrouponPromotionItem> items = this.lambdaQuery().eq(GrouponPromotionItem::getGrouponPromotionId, pageQuery.getGrouponPromotionId()).list();
        List<Long> productIds = items.stream().map(GrouponPromotionItem::getProductId).toList();

        List<GrouponProductVO> result = records.stream()
                .map(p -> {
                    GrouponProductVO item = new GrouponProductVO();
                    item.setProduct(p);
                    item.setIsAdded(productIds.contains((long) p.getProductId()) ? Constants.YES : Constants.NO);

                    long conflictCount = this.baseMapper.getInGroupOnProductCount(
                            p.getProductId().longValue(), grouponPromotion.getGrouponPromotionId(),
                            System.currentTimeMillis()/1000,grouponPromotion.getEndTime());
                    item.setIsOtherGrouponProduct(conflictCount > 0);

                    item.setIsReservationProduct(isReservationProduct(p.getProductType()));

                    List<ProductSkuDTO> currProductSkus = productSkus.stream()
                            .filter(s -> s.getProductId().equals(p.getProductId()))
                            .map(productSkuService::convertToDTO)
                            .toList();
                    // convertToDTO 未走 enrich，skuValue 拼接串需单独翻译（name/value/attrPicThumb 由注解覆盖）
                    currProductSkus.forEach(sku ->
                            sku.setSkuValue(com.tigshop.common.utils.SkuValueTranslateUtils.translateSkuValue(sku.getSkuValue(), translatePackage)));
                    item.setProductSkus(currProductSkus);


                    // 店铺ID处理：如果是门店且商品是平台分配的，使用当前门店ID
                    Integer shopId = p.getShopId();
                    if(tigshopProperties.getIsO2o() == 1){
                        if(!Objects.equals(shopId, HeaderUtils.getShopId())){
                            shopId = HeaderUtils.getShopId();
                        }
                    }

                    //增加是否是秒杀
                    item.setIsSeckillProduct(isSeckillProduct(p.getProductId(), shopId));
                    //增加是否是限时折扣
                    item.setIsTimeDiscountProduct(isTimeDiscountProduct(p.getProductId(), shopId));

                    Integer saleMode = Optional.ofNullable(p.getSaleMode()).orElse(0);
                    if(saleMode>=1 && saleMode<=2){
                        item.setIsPresaleProduct(true);
                    }
                    return item;
                })
                .toList();

        return PageUtil.convertPage(productPage, result);
    }

    /**
     * 判断商品是否正在参与秒杀活动
     *
     * @param productId 商品ID
     * @param shopId    店铺ID
     * @return true-正在参与秒杀, false-未参与秒杀
     */
    private boolean isSeckillProduct(Integer productId, Integer shopId) {
        long currentTime = System.currentTimeMillis() / 1000;
        // 关联Seckill表获取shopId进行匹配
        MPJLambdaWrapper<SeckillItem> wrapper = new MPJLambdaWrapper<SeckillItem>()
                .leftJoin(Seckill.class, Seckill::getSeckillId, SeckillItem::getSeckillId)
                .eq(SeckillItem::getProductId, productId)
                .le(SeckillItem::getSeckillStartTime, currentTime)
                .ge(SeckillItem::getSeckillEndTime, currentTime)
                .eq(Seckill::getShopId, shopId);
        Long count = seckillItemMapper.selectCount(wrapper);
        return count > 0;
    }

    /**
     * 判断商品是否正在参与限时折扣活动
     *
     * @param productId 商品ID
     * @param shopId    店铺ID
     * @return true-正在参与限时折扣, false-未参与限时折扣
     */
    private boolean isTimeDiscountProduct(Integer productId, Integer shopId) {
        long currentTime = System.currentTimeMillis() / 1000;
        // 关联TimeDiscount表获取shopId进行匹配
        MPJLambdaWrapper<TimeDiscountItem> wrapper = new MPJLambdaWrapper<TimeDiscountItem>()
                .leftJoin(TimeDiscount.class, TimeDiscount::getDiscountId, TimeDiscountItem::getDiscountId)
                .eq(TimeDiscountItem::getProductId, productId)
                .le(TimeDiscountItem::getStartTime, currentTime)
                .ge(TimeDiscountItem::getEndTime, currentTime)
                .eq(TimeDiscount::getShopId, shopId);
        Long count = timeDiscountItemMapper.selectCount(wrapper);
        return count > 0;
    }
}
