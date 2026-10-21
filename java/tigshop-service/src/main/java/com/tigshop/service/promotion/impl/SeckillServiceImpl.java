package com.tigshop.service.promotion.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.cart.CartPromotionParsePriceDTO;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.product.ProductListResDTO;
import com.tigshop.bean.dto.promotion.*;
import com.tigshop.bean.enums.product.ProductStatus;
import com.tigshop.bean.enums.promotion.PromotionRangeEnum;
import com.tigshop.bean.enums.promotion.PromotionType;
import com.tigshop.bean.enums.promotion.SeckillStatusEnum;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.promotion.*;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.vo.cart.CartVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.bean.vo.promotion.SeckillListVO;
import com.tigshop.bean.vo.promotion.SeckillPromotionVO;
import com.tigshop.bean.vo.promotion.SeckillVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.service.user.MemberAudienceService;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.promotion.GrouponPromotionItemMapper;
import com.tigshop.mapper.promotion.SeckillItemMapper;
import com.tigshop.mapper.promotion.SeckillMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.promotion.PromotionService;
import com.tigshop.service.promotion.SeckillItemService;
import com.tigshop.service.promotion.SeckillService;
import com.tigshop.service.promotion.TimeDiscountItemService;
import com.tigshop.service.wholesale.WholesalePromotionFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;

import static com.tigshop.common.constant.ExceptionConstants.SERVICE_ERROR;
import static com.tigshop.common.constant.promotion.SeckillConstants.PRODUCT_EXIST_SECKILL;
import static com.tigshop.common.constant.promotion.SeckillConstants.PRODUCT_STOCK_NOT_ENOUGH;
import static com.tigshop.common.constant.promotion.TimeDiscountConstants.PRODUCT_EXIST_TIME_DISCOUNT;

/**
 * 限时秒杀服务实现类
 *
 * @author kidd
 * @create 2025/7/2
 */
@RequiredArgsConstructor
@Service
public class SeckillServiceImpl extends BaseServiceImpl<SeckillMapper, Seckill> implements SeckillService {

    private final ProductMapper productMapper;

    private final ProductSkuService productSkuService;

    private final SeckillItemService seckillItemService;

    private final TimeDiscountItemService timeDiscountItemService;

    private final PromotionService promotionService;

    private final AdminLogService adminLogService;

    private final SeckillItemMapper seckillItemMapper;
    private final GrouponPromotionItemMapper grouponPromotionItemMapper;
    private final TigshopProperties tigshopProperties;
    private final UserMapper userMapper;
    private final com.tigshop.common.core.TranslatePackage translatePackage;
    private final MemberAudienceService memberAudienceService;

    // 批发商促销列表过滤（CR-20260713-1620）。用 setter + @Lazy 注入，避免与 wholesale→user→product
    // 潜在构造器循环冲突，且不影响 @RequiredArgsConstructor 生成的构造器。
    private WholesalePromotionFilter wholesalePromotionFilter;

    @Autowired
    public void setWholesalePromotionFilter(@Lazy WholesalePromotionFilter wholesalePromotionFilter) {
        this.wholesalePromotionFilter = wholesalePromotionFilter;
    }

    @Override
    public Page<SeckillVO> list(SeckillListDTO listDTO) {
        // 分页
        Page<Seckill> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Seckill> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(Seckill::getSeckillName, keyword);
        }

        Integer shopId = getShopId();
        if (shopId != null && shopId >= 0) {
            queryWrapper.eq(Seckill::getShopId, shopId);
        }

        // 执行查询
        Page<Seckill> seckillPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<Seckill> seckillPageRecords = seckillPage.getRecords();
        if (seckillPageRecords.isEmpty()) {
            return new Page<>();
        }

        // 查询商品id
        List<Integer> productIds = seckillPageRecords.stream()
                .map(Seckill::getProductId)
                .distinct()
                .toList();

        List<Product> productList = productMapper.selectBatchIds(productIds);
        // 将商品信息存入 map 中
        Map<Integer, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        // 转换为VO
        List<SeckillVO> seckillVOList = seckillPageRecords.stream()
                .map(seckill -> {
                    SeckillVO seckillVO = new SeckillVO();
                    convert(seckill, seckillVO);
                    BeanUtils.copyProperties(seckill, seckillVO);
                    Product product = productMap.get(seckill.getProductId());
                    if (product != null) {
                        seckillVO.setProductName(product.getProductName());
                    }
                    return seckillVO;
                }).toList();
        return PageUtil.convertPage(seckillPage, seckillVOList);
    }

    /**
     * 数据格式转换
     */
    public void convert(Seckill seckill, SeckillVO seckillVO) {
        DateTime startDate = DateUtil.date(seckill.getSeckillStartTime() * 1000);
        seckillVO.setSeckillStartTime(DateUtil.format(startDate, "yyyy-MM-dd HH:mm:ss"));

        DateTime endDate = DateUtil.date(seckill.getSeckillEndTime() * 1000);
        seckillVO.setSeckillEndTime(DateUtil.format(endDate, "yyyy-MM-dd HH:mm:ss"));

        long currentTime = StringUtils.getCurrentTime();
        if (currentTime < seckill.getSeckillStartTime()) {
            seckillVO.setStatusName(SeckillStatusEnum.STATUS_NOT_STARTED.getDescription());
        } else if (currentTime > seckill.getSeckillEndTime()) {
            seckillVO.setStatusName(SeckillStatusEnum.STATUS_ENDED.getDescription());
        } else {
            seckillVO.setStatusName(SeckillStatusEnum.STATUS_STARTED.getDescription());
        }
    }

    @Override
    public SeckillVO detail(Integer id) {
        Seckill seckill = this.getById(id);
        Assert.notNull(seckill, () -> new GlobalException("未知的秒杀活动"));

        // 查询产品信息
        Product product = productMapper.selectById(seckill.getProductId());

        List<SeckillItem> seckillItems = seckillItemService.lambdaQuery().eq(SeckillItem::getSeckillId, id).list();
        List<Integer> skuIds = seckillItems.stream().map(SeckillItem::getSkuId).toList();
        List<ProductSku> productSkus = productSkuService.lambdaQuery().in(ProductSku::getSkuId, skuIds).list();
        Map<Integer, ProductSku> productSkuMap = productSkus.stream().collect(Collectors.toMap(ProductSku::getSkuId, Function.identity()));

        SeckillVO seckillVO = new SeckillVO(seckill, product, seckillItems, productSkuMap);

        // 从 promotion 表获取适用会员范围字段
        Promotion promotion = promotionService.getOne(new LambdaQueryWrapper<Promotion>()
                .eq(Promotion::getRelationId, id)
                .eq(Promotion::getType, PromotionType.TYPE_SECKILL.getCode()));
        if (promotion != null) {
            seckillVO.setMemberRange(promotion.getMemberRange());
            seckillVO.setMemberMarks(StringUtils.str2IntList(promotion.getMemberMarks()));
            com.tigshop.bean.dto.user.SendUserTargetDTO sendUserTarget =
                    memberAudienceService.parseScope(promotion.getMemberScope());
            seckillVO.setSendUserTarget(memberAudienceService.fillTargetUsers(sendUserTarget));
            seckillVO.setMemberAudience(memberAudienceService.toLegacy(sendUserTarget));
        }

        // 秒杀项 skuData 为 Fastjson JSONArray，绕过 @JsonTranslate，需手动翻译 name/value/图片
        if (seckillVO.getSeckillItem() != null) {
            seckillVO.getSeckillItem().forEach(item -> {
                com.alibaba.fastjson.JSONArray skuData = item.getSkuData();
                if (skuData != null) {
                    for (int i = 0; i < skuData.size(); i++) {
                        com.alibaba.fastjson.JSONObject obj = skuData.getJSONObject(i);
                        com.tigshop.common.utils.SkuValueTranslateUtils.translateSkuDataObject(obj, translatePackage);
                    }
                }
            });
        }

        return seckillVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(SeckillCreateDTO createDTO) {
        if (createDTO != null) {
            Seckill seckill = new Seckill();
            Integer shopId = getShopId();
            shopId = shopId == null ? 0 : shopId;
            seckill.setShopId(shopId);
            seckill.setSeckillStartTime(StringUtils.dateToTimestampExample(createDTO.getSeckillStartTime()));
            seckill.setSeckillEndTime(StringUtils.dateToTimestampExample(createDTO.getSeckillEndTime()));
            BeanUtils.copyProperties(createDTO, seckill);
            checkSeckillItem(createDTO.getSeckillItem(), seckill);
            boolean isSave = this.save(seckill);
            if (isSave) {
                // 活动商品 + 营销写入 + 判断商品是否已有活动
                saveSeckillItem(createDTO.getSeckillItem(), seckill);
                savePromotion(createDTO.getSeckillItem(), seckill, createDTO.getMemberRange(), createDTO.getMemberMarks(),
                        memberAudienceService.resolveWriteScope(createDTO.getSendUserTarget(), createDTO.getMemberAudience()));
                adminLogService.createByLogInfo(StrUtil.format("新增秒杀活动:{}", seckill.getSeckillName()));
            }
            return isSave;
        }
        return false;
    }

    /**
     * 活动商品批量写入
     *
     * @param seckillItem 活动商品
     * @param seckill     秒杀数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveSeckillItem(List<SeckillItemDTO> seckillItem, Seckill seckill) {
        // 需要保存的实体对象
        List<SeckillItem> seckillItems = seckillItem.stream().map(dto -> {
            SeckillItem item = new SeckillItem();
            Integer skuId = dto.getSkuId();
            if (skuId == null) {
                skuId = 0;
            }
            if (dto.getSecondsSeckill() == 1) {
                if (dto.getRecId() != null) {
                    item.setRecId(dto.getRecId());
                }
                item.setSeckillId(seckill.getSeckillId());
                item.setProductId(seckill.getProductId());
                item.setSkuId(skuId);
                item.setSeckillPrice(dto.getSeckillPrice());
                item.setSeckillStock(dto.getSeckillStock());
                item.setSeckillStartTime(seckill.getSeckillStartTime());
                item.setSeckillEndTime(seckill.getSeckillEndTime());
            }
            return item;
        }).collect(Collectors.toList());

        boolean isSuccess = seckillItemService.saveOrUpdateBatch(seckillItems);
        if (!isSuccess) {
            throw new GlobalException(SERVICE_ERROR);
        }
    }

    /**
     * 判断商品是否已有活动
     *
     * @param seckillItem 活动商品
     * @param seckill     秒杀数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkSeckillItem(List<SeckillItemDTO> seckillItem, Seckill seckill) {
        Integer productId = seckill.getProductId();
        Long endTime = seckill.getSeckillEndTime();
        Long startTime = seckill.getSeckillStartTime();
        Integer shopId = seckill.getShopId();

        // 秒杀活动校验 - 通过关联查询seckill表获取shop_id进行比较
        if (seckillItemMapper.selectCount(new LambdaQueryWrapper<SeckillItem>()
                .eq(SeckillItem::getProductId, productId)
                .le(SeckillItem::getSeckillStartTime, endTime)
                .ge(SeckillItem::getSeckillEndTime, startTime)
                .ne(seckill.getSeckillId() != null, SeckillItem::getSeckillId, seckill.getSeckillId())
                // 关联查询seckill表获取shop_id进行比较
                .apply("EXISTS (SELECT 1 FROM seckill ps WHERE ps.seckill_id = seckill_item.seckill_id AND ps.shop_id = {0})", shopId)) > 0) {
            throw new GlobalException(PRODUCT_EXIST_SECKILL);
        }
        // 限时折扣校验
        if (timeDiscountItemService.count(new LambdaQueryWrapper<TimeDiscountItem>()
                .eq(TimeDiscountItem::getProductId, productId)
                .le(TimeDiscountItem::getStartTime, endTime)
                .ge(TimeDiscountItem::getEndTime, startTime)) > 0) {
            throw new GlobalException(PRODUCT_EXIST_TIME_DISCOUNT);
        }

        // 团购活动校验
        if (grouponPromotionItemMapper.countProductConflict(productId.longValue(), null, startTime, endTime) > 0) {
            throw new GlobalException("该商品在时间段内已参与其他拼团活动，不能重复绑定");
        }

        // 商品库存校验
        for (SeckillItemDTO item : seckillItem) {
            if (item.getSecondsSeckill() == 0) {
                continue;
            }
            int skuId = item.getSkuId() == null ? 0 : item.getSkuId();

            if (skuId == 0) {
                if (productMapper.selectById(productId).getProductStock() < item.getSeckillStock()) {
                    throw new GlobalException(PRODUCT_STOCK_NOT_ENOUGH);
                }
            } else {
                productSkuService.checkSkuEnabled(productId, skuId);
                if (productSkuService.getById(skuId).getSkuStock() < item.getSeckillStock()) {
                    throw new GlobalException(PRODUCT_STOCK_NOT_ENOUGH);
                }
            }
        }
    }

    /**
     * 营销写入
     *
     * @param seckillItem 活动商品
     * @param seckill     秒杀数据
     * @param memberRange 适用会员范围
     * @param memberMarks 选择的会员标记列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void savePromotion(List<SeckillItemDTO> seckillItem, Seckill seckill, Integer memberRange, List<Integer> memberMarks, String memberScope) {
        Integer relationId = seckill.getSeckillId();
        relationId = relationId == null ? 0 : relationId;
        String memberMarksStr = memberMarks != null ? memberMarks.toString() : null;
        // 根据 relationId 查询是否有营销活动
        if (promotionService.count(new LambdaQueryWrapper<Promotion>()
                .eq(Promotion::getRelationId, relationId)
                .eq(Promotion::getType, PromotionType.TYPE_SECKILL.getCode())) > 0) {
            // 更新部分字段
            String rangeDataStr = null;
            if (seckill.getProductId() != null) {
                Integer[] rangeDataArray = {seckill.getProductId()};
                List<Integer> rangeData = Arrays.asList(rangeDataArray);
                rangeDataStr = rangeData.toString();
            }
            promotionService.update(new LambdaUpdateWrapper<Promotion>()
                    .eq(Promotion::getRelationId, relationId)
                    .eq(Promotion::getType, PromotionType.TYPE_SECKILL.getCode())
                    .set(Promotion::getStartTime, seckill.getSeckillStartTime())
                    .set(Promotion::getEndTime, seckill.getSeckillEndTime())
                    .set(Promotion::getPromotionName, seckill.getSeckillName())
                    .set(Promotion::getMemberRange, memberRange)
                    //后期秒杀更改商品的时候
                    .set(StringUtils.isNotBlank(rangeDataStr), Promotion::getRange, PromotionRangeEnum.PROMOTION_RANGE_PRODUCT.getCode())
                    .set(StringUtils.isNotBlank(rangeDataStr), Promotion::getRangeData, rangeDataStr)
                    .set(Promotion::getMemberMarks, memberMarksStr)
                    .set(Promotion::getMemberScope, memberScope));
        } else {
            Promotion promotion = new Promotion();
            Integer shopId = getShopId();
            shopId = shopId == null ? 0 : shopId;
            Integer productId = seckill.getProductId();
            promotion.setPromotionName(seckill.getSeckillName());
            promotion.setStartTime(seckill.getSeckillStartTime());
            promotion.setEndTime(seckill.getSeckillEndTime());
            promotion.setType(PromotionType.TYPE_SECKILL.getCode());
            promotion.setShopId(shopId);
            promotion.setRelationId(seckill.getSeckillId());
            promotion.setRange(PromotionRangeEnum.PROMOTION_RANGE_PRODUCT.getCode());
            promotion.setMemberRange(memberRange);
            promotion.setMemberMarks(memberMarksStr);
            promotion.setMemberScope(memberScope);

            Integer[] rangeDataArray = {productId};
            List<Integer> rangeData = Arrays.asList(rangeDataArray);
            promotion.setRangeData(rangeData.toString());

            List<Integer> skuIds = seckillItem.stream()
                    .filter(item -> item.getSkuId() != null && item.getSecondsSeckill() == 1)
                    .map(SeckillItemDTO::getSkuId)
                    .toList();
            if (!skuIds.isEmpty()) {
                promotion.setSkuIds(skuIds.toString());
            }

            boolean isSuccess = promotionService.save(promotion);
            if (!isSuccess) {
                throw new GlobalException(SERVICE_ERROR);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(SeckillUpdateDTO updateDTO) {
        if (updateDTO != null) {
            Seckill seckill = new Seckill();
            seckill.setSeckillStartTime(StringUtils.dateToTimestampExample(updateDTO.getSeckillStartTime()));
            seckill.setSeckillEndTime(StringUtils.dateToTimestampExample(updateDTO.getSeckillEndTime()));
            BeanUtils.copyProperties(updateDTO, seckill);
            boolean isSave = this.updateById(seckill);
            if (isSave) {
                // 活动商品 + 营销写入 + 判断商品是否已有活动
                checkSeckillItem(updateDTO.getSeckillItem(), seckill);
                saveSeckillItem(updateDTO.getSeckillItem(), seckill);
                savePromotion(updateDTO.getSeckillItem(), seckill, updateDTO.getMemberRange(), updateDTO.getMemberMarks(),
                        memberAudienceService.resolveWriteScope(updateDTO.getSendUserTarget(), updateDTO.getMemberAudience()));
                adminLogService.createByLogInfo(StrUtil.format("更新秒杀活动:{}", seckill.getSeckillName()));
            }
            return isSave;
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void incSales(Integer productId, Integer skuId, Integer quantity) {
        SeckillItem productActivityInfo = getProductActivityInfo(productId, skuId);
        if (productActivityInfo != null) {
            LambdaUpdateWrapper<SeckillItem> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SeckillItem::getRecId, productActivityInfo.getRecId()).setIncrBy(SeckillItem::getSeckillSales, quantity);
            seckillItemMapper.update(updateWrapper);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void decStock(Integer productId, Integer skuId, Integer quantity) {
        SeckillItem productActivityInfo = getProductActivityInfo(productId, skuId);
        if (productActivityInfo != null) {
            LambdaUpdateWrapper<SeckillItem> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SeckillItem::getRecId, productActivityInfo.getRecId())
                    .ge(SeckillItem::getSeckillStock, quantity)
                    .setDecrBy(SeckillItem::getSeckillStock, quantity);
            int rows = seckillItemMapper.update(updateWrapper);
            if (rows == 0) {
                throw new GlobalException("秒杀商品库存不足");
            }
        }
    }

    @Override
    public CartPromotionParsePriceDTO parsePrice(List<CartVO> cartList, PromotionVO promotionVO) {
        CartPromotionParsePriceDTO cartPromotionParsePriceDTO = new CartPromotionParsePriceDTO();

        // seckillId 对整批不变，按 skuId 批量预取秒杀明细，避免逐项 getOne（N9）
        List<Integer> skuIds = cartList.stream().map(CartVO::getSkuId).filter(Objects::nonNull).distinct().toList();
        Map<Integer, SeckillItem> seckillItemMap = skuIds.isEmpty() ? Collections.emptyMap()
                : seckillItemService.lambdaQuery()
                .eq(SeckillItem::getSeckillId, promotionVO.getRelationId())
                .in(SeckillItem::getSkuId, skuIds)
                .list().stream()
                .collect(Collectors.toMap(SeckillItem::getSkuId, it -> it, (a, b) -> a));
        for (CartVO cart : cartList) {
            SeckillItem one = seckillItemMap.get(cart.getSkuId());
            if (one != null) {
                cart.setPrice(one.getSeckillPrice());
            }
        }
        cartPromotionParsePriceDTO.setCartList(cartList);
        cartPromotionParsePriceDTO.setPromotion(promotionVO);
        return cartPromotionParsePriceDTO;
    }

    @Override
    public void decSales(Integer productId, Integer skuId, Integer quantity) {
        SeckillItem productActivityInfo = getProductActivityInfo(productId, skuId);
        if (productActivityInfo != null) {
            LambdaUpdateWrapper<SeckillItem> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SeckillItem::getRecId, productActivityInfo.getRecId()).setDecrBy(SeckillItem::getSeckillSales, quantity);
            seckillItemMapper.update(updateWrapper);
        }
    }

    @Override
    public void incStock(Integer productId, Integer skuId, Integer quantity) {
        SeckillItem productActivityInfo = getProductActivityInfo(productId, skuId);
        if (productActivityInfo != null) {
            LambdaUpdateWrapper<SeckillItem> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SeckillItem::getRecId, productActivityInfo.getRecId()).setIncrBy(SeckillItem::getSeckillStock, quantity);
            seckillItemMapper.update(updateWrapper);
        }
    }

    @Override
    public Page<SeckillListVO> getSeckillProductList(Integer size, Integer page, Integer unStarted) {

        MPJLambdaWrapper<Seckill> queryWrapper = new MPJLambdaWrapper<>();

        // 二期（D1096）：仅「新建 member_scope 秒杀」展示秒杀价 + 标记置灰（scopeRestrictedIds）；
        // 「存量 memberRange 指定标记会员」秒杀保持原有隐藏逻辑（legacyExcludedIds → notIn）。
        Set<Integer> legacyExcludedIds = new HashSet<>();
        Set<Integer> scopeRestrictedIds = new HashSet<>();
        collectSeckillRestrictions(legacyExcludedIds, scopeRestrictedIds);
        if (CollUtil.isNotEmpty(legacyExcludedIds)) {
            queryWrapper.notIn(Seckill::getSeckillId, legacyExcludedIds);
        }

        // 批发商只看批发价：隐藏「对该批发商有批发价」的商品的秒杀（CR-20260713-1620）
        Set<Integer> wholesaleExcludedProductIds = wholesalePromotionFilter.currentBuyerExcludedProductIds();
        if (CollUtil.isNotEmpty(wholesaleExcludedProductIds)) {
            queryWrapper.notIn(Seckill::getProductId, wholesaleExcludedProductIds);
        }

        // 添加秒杀时间条件
        if (unStarted != 0) {
            queryWrapper.gt(Seckill::getSeckillStartTime, StringUtils.getCurrentTime());
        } else {
            queryWrapper.lt(Seckill::getSeckillStartTime, StringUtils.getCurrentTime())
                    .gt(Seckill::getSeckillEndTime, StringUtils.getCurrentTime());
        }

        // 关联 product 表并过滤已下架商品(假设 status=1 为上架状态)
        queryWrapper.leftJoin(Product.class, Product::getProductId, Seckill::getProductId)
                .eq(Product::getProductStatus, ProductStatus.ON_SALE.getCode());

        // 显式按结束时间升序：此前无 ORDER BY，返回顺序只是优化器走 seckill_end_time
        // 索引区间扫描的副产物，执行计划一变首页选中的活动就会突变；后台装修预览
        // (/adminapi/promotion/seckill/listForHomePreview) 也依赖这个顺序与首页保持一致。
        queryWrapper.orderByAsc(Seckill::getSeckillEndTime);

        Page<Seckill> seckillPage = this.baseMapper.selectJoinPage(new Page<>(page, size), Seckill.class, queryWrapper);

        List<Seckill> seckillRecords = seckillPage.getRecords();
        if (seckillRecords.isEmpty()) {
            return new Page<>(page, size);
        }

        List<Integer> seckillIds = seckillRecords.stream().map(Seckill::getSeckillId).toList();
        List<SeckillItem> seckillItems = filterEnabledSkuItems(
                seckillItemService.lambdaQuery().in(SeckillItem::getSeckillId, seckillIds).list());

        List<Integer> productIds = seckillItems.stream().map(SeckillItem::getProductId).distinct().toList();
        List<Product> products = productMapper.selectBatchIds(productIds);
        // 移除删除的商品
        products = products.stream()
                .filter(product -> product.getIsDelete().equals(0))
                .toList();
        Map<Integer, Product> productMap = products.stream().collect(Collectors.toMap(Product::getProductId, product -> product));


        List<SeckillListVO> resultRecords = seckillRecords.stream()
                .map(seckill -> {
                    List<SeckillItem> currSeckillItems = seckillItems.stream()
                            .filter(item -> item.getSeckillId().equals(seckill.getSeckillId()))
                            .toList();

                    if (currSeckillItems.isEmpty()) {
                        return null; // 没有秒杀项，跳过
                    }

                    // 过滤掉已下架商品
                    List<SeckillItem> validItems = currSeckillItems.stream()
                            .filter(item -> productMap.get(item.getProductId()) != null)
                            .toList();

                    // 如果所有秒杀商品都下架了，则跳过
                    if (validItems.isEmpty()) {
                        return null;
                    }

                    SeckillListVO listVO = new SeckillListVO();

                    SeckillItem seckillItem = currSeckillItems.getFirst();
                    BeanUtil.copyProperties(seckillItem, listVO);

                    Product product = productMap.get(seckillItem.getProductId());
                    BeanUtil.copyProperties(product, listVO);
                    listVO.setMarketPrice(product.getProductPrice());
                    listVO.setProductPrice(product.getMarketPrice());
                    listVO.setOrgProductPrice(product.getProductPrice().toString());

                    SeckillListVO.SeckillData seckillData = new SeckillListVO.SeckillData();
                    BeanUtil.copyProperties(seckill, seckillData);
                    // 处理 seckillData 时间
                    seckillData.setSeckillStartTime(TigUtils.handelTime(seckill.getSeckillStartTime()));
                    seckillData.setSeckillEndTime(TigUtils.handelTime(seckill.getSeckillEndTime()));

                    seckillData.setSeckillItem(currSeckillItems);
                    listVO.setSeckkillData(seckillData);
                    listVO.setShopId(seckill.getShopId());
                    boolean canJoin = !scopeRestrictedIds.contains(seckill.getSeckillId());
                    listVO.setCanJoin(canJoin);
                    if (!canJoin) {
                        listVO.setRestrictTip("仅限指定会员参与");
                    }
                    return listVO;
                })
                .filter(Objects::nonNull)
                .toList();

        return PageUtil.convertPage(seckillPage, resultRecords);
    }

    /**
     * 根据用户标记获取需要排除的秒杀活动ID
     * 只有在用户登录时才进行限制，未登录时不限制
     *
     * @return 需要排除的秒杀活动ID集合
     */
    private void collectSeckillRestrictions(Set<Integer> legacyExcluded, Set<Integer> scopeRestricted) {
        // 获取当前用户ID，未登录时不进行限制
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return;
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }

        // 查询所有秒杀促销活动（二期标签人群与旧会员标记并存判定）
        List<Promotion> promotions = promotionService.lambdaQuery()
                .eq(Promotion::getType, PromotionType.TYPE_SECKILL.getCode())
                .list();

        for (Promotion promotion : promotions) {
            String memberScope = promotion.getMemberScope();
            if (StringUtils.isNotBlank(memberScope)) {
                // 二期新建：会员人群不匹配 → 展示但置灰（标记，不隐藏）
                if (!memberAudienceService.matchesScope(userId.longValue(), memberScope)) {
                    scopeRestricted.add(promotion.getRelationId());
                }
            } else if (promotion.getMemberRange() != null && promotion.getMemberRange() == 1) {
                // 存量旧逻辑：按会员标记不匹配 → 隐藏（保持改造前行为）
                String memberMarks = promotion.getMemberMarks();
                List<Integer> allowedMarks = Optional.ofNullable(JSON.parseArray(memberMarks, Integer.class)).orElse(List.of());
                if (!allowedMarks.isEmpty() && !allowedMarks.contains(user.getMark())) {
                    legacyExcluded.add(promotion.getRelationId());
                }
            }
        }
    }

    @Override
    public List<SeckillPromotionVO> listForDecorate(SeckillListDTO listDTO) {
        Long currentTime = StringUtils.getCurrentTime();
        Page<Seckill> seckillPage = this.baseMapper.listForDecorate(new Page<>(listDTO.getPage(), listDTO.getSize()), currentTime);

        List<Seckill> records = seckillPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return Collections.emptyList();
        }

        Map<Integer, Seckill> seckillMap = records.stream().collect(Collectors.toMap(Seckill::getProductId, Function.identity()));

        List<Integer> seckillIds = records.stream().map(Seckill::getSeckillId).toList();
        List<SeckillItem> seckillItems = filterEnabledSkuItems(seckillItemService.lambdaQuery()
                .in(SeckillItem::getSeckillId, seckillIds)
                .list());

        Map<Integer, List<SeckillItem>> seckillItemMap = seckillItems.stream()
                .collect(Collectors.groupingBy(SeckillItem::getProductId));

        List<Integer> productIds = seckillItems.stream().map(SeckillItem::getProductId).distinct().toList();
        List<Product> products = productMapper.selectBatchIds(productIds);

        List<Integer> skuIds = seckillItems.stream().map(SeckillItem::getSkuId).toList();
        List<ProductSku> productSkus = productSkuService.listByIds(skuIds);
        Map<Integer, ProductSku> productSkuMap = productSkus.stream().collect(Collectors.toMap(ProductSku::getSkuId, Function.identity()));

        List<ProductListResDTO> productsRes = products.stream()
                .map(item -> {
                    List<ProductSku> currProductSkus = productSkus.stream()
                            .filter(productSku -> productSku.getProductId().equals(item.getProductId()))
                            .toList();
                    return new ProductListResDTO(item, currProductSkus);
                })
                .toList();
        return productsRes.stream()
                .map(product -> {
                    SeckillPromotionVO seckillProduct = BeanUtil.copyProperties(product, SeckillPromotionVO.class);
                    seckillProduct.setOrgProductPrice(product.getProductPrice().toString());

                    Seckill seckill = seckillMap.get(product.getProductId());

                    seckillProduct.setSeckillLimitNum(seckill.getSeckillLimitNum());

                    List<SeckillItem> currSeckillItems = seckillItemMap.get(product.getProductId());

                    Integer seckillSales = currSeckillItems.stream().map(SeckillItem::getSeckillSales).reduce(0, Integer::sum);
                    seckillProduct.setSeckillSales(seckillSales);

                    Integer seckillStock = currSeckillItems.stream().map(SeckillItem::getSeckillStock).reduce(0, Integer::sum);
                    seckillProduct.setSeckillSales(seckillStock);

                    List<SeckillItem> seckillItemsBySeckillId = seckillItems.stream().filter(item -> Objects.equals(item.getSeckillId(), seckill.getSeckillId())).toList();

                    SeckillVO seckillVO = new SeckillVO(seckill, seckillItemsBySeckillId);
                    seckillProduct.setSeckkillData(seckillVO);

                    ProductSku productSku = productSkuMap.get(product.getProductId());
                    if (productSku != null) {
                        seckillProduct.setSkuId(productSku.getSkuId());
                        seckillProduct.setSkuSn(productSku.getSkuSn());
                        seckillProduct.setMarketPrice(product.getMarketPrice());
                        seckillProduct.setOrgProductPrice(productSku.getSkuPrice().toString());
                    }

                    return seckillProduct;
                })
                .toList();
    }

    /**
     * 删除
     *
     * @param id id
     * @return boolean
     */
    @Override
    public boolean del(Integer id) {
        if (id == null) {
            return false;
        }

        boolean isMove = this.removeById(id);

        if (isMove) {
            // 删除活动商品
            seckillItemService.remove(new LambdaQueryWrapper<SeckillItem>()
                    .eq(SeckillItem::getSeckillId, id));
            // 删除对应营销活动
            promotionService.remove(new LambdaQueryWrapper<Promotion>()
                    .eq(Promotion::getRelationId, id)
                    .eq(Promotion::getType, PromotionType.TYPE_SECKILL.getCode()));

        }
        return isMove;
    }

    /**
     * 批量操作
     *
     * @param batchDTO 批量操作参数
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batch(BatchDTO batchDTO) {
        if ("del".equals(batchDTO.getType())) {
            List<Integer> ids = batchDTO.getIds();
            boolean isMove = this.removeByIds(ids);
            if (isMove) {
                // 删除活动商品
                seckillItemService.remove(new LambdaQueryWrapper<SeckillItem>()
                        .in(SeckillItem::getSeckillId, ids));
                // 删除对应营销活动
                promotionService.remove(new LambdaQueryWrapper<Promotion>()
                        .in(Promotion::getRelationId, ids)
                        .eq(Promotion::getType, PromotionType.TYPE_SECKILL.getCode()));
            }
            return isMove;
        }
        return false;
    }

    @Override
    public List<SeckillItemInfoDTO> getSeckillInfo(Integer productId, Integer shopId) {
        Long currentTime = StringUtils.getCurrentTime();
        Seckill seckill = this.lambdaQuery().eq(Seckill::getProductId, productId)
                .eq(tigshopProperties.getIsO2o() == 1, Seckill::getShopId, shopId)
                .lt(Seckill::getSeckillStartTime, currentTime)
                .gt(Seckill::getSeckillEndTime, currentTime)
                .one();
        if (seckill == null) {
            return Collections.emptyList();
        }

        List<SeckillItem> seckillItems = filterEnabledSkuItems(
                seckillItemService.lambdaQuery().eq(SeckillItem::getSeckillId, seckill.getSeckillId()).list());

        // 二期（D1096）：商详秒杀不隐藏，标记 canJoin/restrictTip，前台展示秒杀价但置灰「抢购」按钮
        boolean canJoin = currentUserCanJoinSeckill(seckill);
        return seckillItems.stream().map(item -> {
            SeckillItemInfoDTO dto = new SeckillItemInfoDTO(seckill, item);
            dto.setCanJoin(canJoin);
            if (!canJoin) {
                dto.setRestrictTip("仅限指定会员参与");
            }
            return dto;
        }).toList();
    }

    private List<SeckillItem> filterEnabledSkuItems(List<SeckillItem> seckillItems) {
        if (CollUtil.isEmpty(seckillItems)) {
            return Collections.emptyList();
        }
        List<Integer> skuIds = seckillItems.stream()
                .map(SeckillItem::getSkuId)
                .filter(Objects::nonNull)
                .filter(skuId -> skuId > 0)
                .distinct()
                .toList();
        if (skuIds.isEmpty()) {
            return seckillItems;
        }
        Set<Integer> enabledSkuIds = productSkuService.lambdaQuery()
                .select(ProductSku::getSkuId)
                .in(ProductSku::getSkuId, skuIds)
                .eq(ProductSku::getIsEnabled, Constants.YES)
                .list()
                .stream()
                .map(ProductSku::getSkuId)
                .collect(Collectors.toSet());
        return seckillItems.stream()
                .filter(item -> item.getSkuId() == null || item.getSkuId() == 0 || enabledSkuIds.contains(item.getSkuId()))
                .toList();
    }

    /**
     * 二期（D1096）：判定当前用户是否可参与该秒杀。member_scope 优先（标签/等级人群），回退旧 memberRange/memberMarks。
     * 未登录 / 管理员 / 无人群约束 → true。
     */
    private boolean currentUserCanJoinSeckill(Seckill seckill) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null || userId <= 0 || SecurityUtils.isAdminUser()) {
            return true;
        }
        Promotion promotion = promotionService.lambdaQuery()
                .eq(Promotion::getType, PromotionType.TYPE_SECKILL.getCode())
                .eq(Promotion::getRelationId, seckill.getSeckillId())
                .last("limit 1")
                .one();
        if (promotion == null) {
            return true;
        }
        String memberScope = promotion.getMemberScope();
        if (StringUtils.isNotBlank(memberScope)) {
            return memberAudienceService.matchesScope(userId.longValue(), memberScope);
        }
        return matchesLegacyMemberMark(promotion, userId);
    }

    /**
     * 存量 memberRange/memberMarks 口径判定（CR-20260730-1745）。
     * 已登录但未设标记的用户同样视为不在名单内，与 PromotionServiceImpl 活动总线的判定口径保持一致。
     */
    private boolean matchesLegacyMemberMark(Promotion promotion, Integer userId) {
        if (!Objects.equals(1, promotion.getMemberRange())) {
            return true;
        }
        List<Integer> allowedMarks = Optional.ofNullable(JSON.parseArray(promotion.getMemberMarks(), Integer.class)).orElse(List.of());
        Integer mark = Optional.ofNullable(userMapper.selectById(userId)).map(User::getMark).orElse(null);
        return mark != null && allowedMarks.contains(mark);
    }

    public SeckillItem getProductActivityInfo(Integer productId, Integer skuId) {
        LambdaQueryWrapper<SeckillItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SeckillItem::getProductId, productId)
                .le(SeckillItem::getSeckillStartTime, StringUtils.getCurrentTime())
                .ge(SeckillItem::getSeckillEndTime, StringUtils.getCurrentTime())
                .eq(SeckillItem::getSkuId, skuId).last("limit 1");

        return seckillItemMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean canUserJoinSeckill(Integer productId, Integer skuId, Integer userId) {
        // 未登录不拦截（与列表/详情人群判定一致）
        if (userId == null || userId <= 0) {
            return true;
        }
        // 找到该商品当前进行中的秒杀活动；无活动则不拦截
        SeckillItem item = getProductActivityInfo(productId, skuId);
        if (item == null) {
            return true;
        }
        Promotion promotion = promotionService.lambdaQuery()
                .eq(Promotion::getType, PromotionType.TYPE_SECKILL.getCode())
                .eq(Promotion::getRelationId, item.getSeckillId())
                .last("limit 1")
                .one();
        if (promotion == null) {
            return true;
        }
        String memberScope = promotion.getMemberScope();
        if (StringUtils.isNotBlank(memberScope)) {
            // 二期新建：按指定会员/等级/标签人群匹配
            return memberAudienceService.matchesScope(userId.longValue(), memberScope);
        }
        // CR-20260730-1745：存量 memberRange 秒杀此前恒放行，可绕过前台置灰按秒杀价成交，现同步拦截
        return matchesLegacyMemberMark(promotion, userId);
    }

}
