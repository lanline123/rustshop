package com.tigshop.service.promotion.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.enums.promotion.GiftGroupsEnum;
import com.tigshop.bean.enums.promotion.ValidityTypeEnum;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.promotion.GiftCardGroups;
import com.tigshop.bean.model.promotion.GiftCardProducts;
import com.tigshop.bean.model.promotion.GiftCards;
import com.tigshop.bean.param.promotion.giftcard.GiftCardGroupsCreateParam;
import com.tigshop.bean.param.promotion.giftcard.GiftCardGroupsUpdateParam;
import com.tigshop.bean.query.promotion.card.GiftCardGroupsPageQuery;
import com.tigshop.bean.vo.product.ProductSkuVO;
import com.tigshop.bean.vo.promotion.card.GiftCardGroupsVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.promotion.GiftCardGroupsMapper;
import com.tigshop.mapper.promotion.GiftCardProductsMapper;
import com.tigshop.mapper.promotion.GiftCardsMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.promotion.GiftCardGroupsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static com.tigshop.common.constant.Constants.NO;
import static com.tigshop.common.constant.Constants.YES;

/**
 * @author Tigshop项目组
 */
@RequiredArgsConstructor
@Service
public class GiftCardGroupsServiceImpl extends BaseServiceImpl<GiftCardGroupsMapper, GiftCardGroups> implements GiftCardGroupsService {

    private final GiftCardProductsMapper giftCardProductsMapper;
    private final ProductMapper productMapper;
    private final GiftCardsMapper giftCardsMapper;

    @Override
    public Page<GiftCardGroupsVO> list(GiftCardGroupsPageQuery query) {
        Page<GiftCardGroups> page = buildSortOrder(query);

        Page<GiftCardGroups> giftCardGroupsPage = this.lambdaQuery()
                .like(StrUtil.isNotBlank(query.getKeyword()), GiftCardGroups::getCardGroupName, query.getKeyword())
                .eq(GiftCardGroups::getShopId, getShopId())
                .page(page);

        List<GiftCardGroupsVO> list = giftCardGroupsPage.getRecords().stream()
                .map(item -> {
                    GiftCardGroupsVO giftCardGroupsVO = BeanUtil.copyProperties(item, GiftCardGroupsVO.class);
                    giftCardGroupsVO.initTypeName(giftCardGroupsVO);
                    // 返回是否可以点击删除按钮
                    Long bingedGiftCardCount = productMapper.selectCount(Wrappers.lambdaQuery(Product.class)
                            .eq(Product::getGiftCardGroupsId, item.getCardGroupsId())
                            .eq(Product::getShopId, getShopId())
                            .eq(Product::getProductStatus, YES)
                            .eq(Product::getIsDelete, NO));
                    giftCardGroupsVO.setCanDelete(bingedGiftCardCount == 0);
                    return giftCardGroupsVO;
                }).toList();

        list.forEach(item -> {
            GiftCardGroupsVO giftCardGroupsDetail = this.detail(item.getCardGroupsId());
            item.setGiftCardProducts(giftCardGroupsDetail.getGiftCardProducts());

        });
        return PageUtil.convertPage(giftCardGroupsPage, list);
    }

    @Override
    public GiftCardGroupsVO detail(Integer cardGroupsId) {
        GiftCardGroups giftCardGroups = this.getById(cardGroupsId);
        GiftCardGroupsVO giftCardGroupsVO = BeanUtil.copyProperties(giftCardGroups, GiftCardGroupsVO.class);
        this.getExpireTime(giftCardGroupsVO,giftCardGroups);
        if (giftCardGroups.getGroupType().equals(GiftGroupsEnum.EXCHANGE.getCode())) {
            // 查出商品信息
            List<GiftCardProducts> giftCardProducts = giftCardProductsMapper.selectList(
                    new LambdaQueryWrapper<>(GiftCardProducts.class)
                            .eq(GiftCardProducts::getCardGroupsId, cardGroupsId)
            );

            List<ProductSkuVO> productSkus = new ArrayList<>();
            for (GiftCardProducts giftCardProduct : giftCardProducts) {
                ProductSkuVO productSku = productMapper.selectProduct(giftCardProduct.getProductId(), giftCardProduct.getSkuId());
                if (productSku == null){
                    continue;
                }
                productSkus.add(productSku);
            }
            giftCardGroupsVO.setGiftCardProducts(productSkus);
        }

        // 返回是否可以点击删除按钮
        Long bingedGiftCardCount = productMapper.selectCount(Wrappers.lambdaQuery(Product.class)
                .eq(Product::getGiftCardGroupsId, cardGroupsId)
                .eq(Product::getShopId, getShopId())
                .eq(Product::getProductStatus, YES)
                .eq(Product::getIsDelete, NO));
        giftCardGroupsVO.setCanDelete(bingedGiftCardCount == 0);

        giftCardGroupsVO.initTypeName(giftCardGroupsVO);
        return giftCardGroupsVO;
    }

    private void getExpireTime(GiftCardGroupsVO giftCardGroupsVO,GiftCardGroups giftCardGroups) {
        ValidityTypeEnum vte = ValidityTypeEnum.getByCode(giftCardGroups.getValidityType());
         switch (vte) {
            case PERMANENT -> giftCardGroups.setExpireTime(null);
            case NUMBER -> giftCardGroupsVO.setValidityDays(giftCardGroups.getValidityDays());
            case FIXED -> giftCardGroupsVO.setExpireTime(TigUtils.handelTime(giftCardGroups.getExpireTime()));
            default -> throw new GlobalException("未知的卡有效期类型");
        };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Integer cardGroupsId, Integer status) {
        Assert.notNull(cardGroupsId, () -> new GlobalException("卡组不存在"));
        Assert.isTrue(status == 0 || status == 1, () -> new GlobalException("卡组状态不正确"));

        this.lambdaUpdate().eq(GiftCardGroups::getCardGroupsId, cardGroupsId)
                .set(GiftCardGroups::getUpdateTime, StringUtils.getCurrentTime())
                .set(GiftCardGroups::getStatus, status).update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createGiftCardGroups(GiftCardGroupsCreateParam param) {
        GiftCardGroups giftCardGroups = param.buildGiftCardGroups(param);

        this.save(giftCardGroups);

        // 如果是兑换卡，则需要保存商品信息
        if (param.getGroupType().equals(GiftGroupsEnum.EXCHANGE.getCode())) {
            List<GiftCardProducts> giftCardProducts = param.getGiftCardProducts();

            giftCardProducts.forEach(item -> item.setCardGroupsId(giftCardGroups.getCardGroupsId()));

            giftCardProductsMapper.insert(giftCardProducts);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGiftCardGroups(GiftCardGroupsUpdateParam param) {
        GiftCardGroups giftCardGroups = param.buildGiftCardGroups(param);

        GiftCardGroups dbGiftCardGroups = this.getById(param.getCardGroupsId());
        if (dbGiftCardGroups.getGroupType().equals(GiftGroupsEnum.TOP_UP.getCode())) {
            Assert.notNull(param.getFaceAmount(), () -> new GlobalException("请填写面额"));
        }
        this.updateById(giftCardGroups);

        // 如果是兑换卡，则需要保存商品信息
        if (dbGiftCardGroups.getGroupType().equals(GiftGroupsEnum.EXCHANGE.getCode())) {
            List<GiftCardProducts> giftCardProducts = param.getGiftCardProducts();
            Assert.notNull(giftCardProducts, () -> new GlobalException("请最少选择一个商品"));

            // 先删除原有记录
            giftCardProductsMapper.delete(
                    new LambdaQueryWrapper<>(GiftCardProducts.class).eq(GiftCardProducts::getCardGroupsId, param.getCardGroupsId())
            );

            // 为每个商品设置卡组ID
            giftCardProducts.forEach(item -> item.setCardGroupsId(giftCardGroups.getCardGroupsId()));
            // 批量插入新记录（使用saveBatch避免重复键问题）
            if (CollUtil.isNotEmpty(giftCardProducts)) {
                giftCardProductsMapper.insert(giftCardProducts);
            }
        }
    }

    @Override
    public List<GiftCardGroupsVO> getGiftCardGroupsByCardType(Integer cardType, Integer giftCardGroupsId, Integer shopId) {
        if (cardType == null) {
            return List.of();
        }

        // 获取商品关联卡组
        List<Product> productList = productMapper.selectList(new LambdaQueryWrapper<>(Product.class)
                .isNotNull(Product::getGiftCardGroupsId)
                .ne(giftCardGroupsId != null, Product::getGiftCardGroupsId, giftCardGroupsId)
                .select(Product::getGiftCardGroupsId));

        List<Integer> cardGroupsIdList = productList.stream()
                .map(Product::getGiftCardGroupsId)
                .toList();

        List<GiftCardGroups> giftCardGroupsList = lambdaQuery().eq(GiftCardGroups::getCardType, cardType)
                .eq(GiftCardGroups::getStatus, YES)
                .eq(GiftCardGroups::getShopId, shopId)
                .notIn(CollUtil.isNotEmpty(cardGroupsIdList), GiftCardGroups::getCardGroupsId, cardGroupsIdList).list();

        return giftCardGroupsList.stream()
                .map(item -> {
                    GiftCardGroupsVO giftCardGroupsVO = BeanUtil.copyProperties(item, GiftCardGroupsVO.class);
                    giftCardGroupsVO.initTypeName(giftCardGroupsVO);
                    return giftCardGroupsVO;
                }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGiftCardGroups(Integer cardGroupsId) {

        Integer shopId = HeaderUtils.getShopId() == null ? 0 : HeaderUtils.getShopId();
        Long bingedGiftCardCount = productMapper.selectCount(Wrappers.lambdaQuery(Product.class)
                .eq(Product::getGiftCardGroupsId, cardGroupsId)
                .eq(Product::getShopId, shopId)
                .eq(Product::getProductStatus, YES)
                .eq(Product::getIsDelete, NO));

        Assert.isTrue(bingedGiftCardCount == 0, () -> new GlobalException("该礼品卡组已经被商品绑定，不能删除"));
        this.removeById(cardGroupsId);
    }

    @Override
    public void giftCardGroupProductsUpdate(GiftCardGroupsUpdateParam param) {

        param.setValidityType(0);
        GiftCardGroups giftCardGroups = param.buildGiftCardGroups(param);

        GiftCardGroups dbGiftCardGroups = getById(param.getCardGroupsId());
        // 如果是兑换卡，则需要保存商品信息
        if (dbGiftCardGroups.getGroupType().equals(GiftGroupsEnum.EXCHANGE.getCode())) {
            List<GiftCardProducts> giftCardProducts = param.getGiftCardProducts();
            Assert.notNull(giftCardProducts, () -> new GlobalException("请最少选择一个商品"));

            int isDelete = giftCardProductsMapper.delete(
                    new LambdaQueryWrapper<>(GiftCardProducts.class).eq(GiftCardProducts::getCardGroupsId, param.getCardGroupsId())
            );
            if (isDelete > 0) {
                giftCardProducts.forEach(item -> item.setCardGroupsId(giftCardGroups.getCardGroupsId()));
                giftCardProductsMapper.insert(giftCardProducts);
            }
        }
    }
}
