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

package com.tigshop.service.promotion.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.promotion.ProductGiftCreateDTO;
import com.tigshop.bean.dto.promotion.ProductGiftListDTO;
import com.tigshop.bean.dto.promotion.ProductGiftUpdateDTO;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.promotion.GiftCardGroups;
import com.tigshop.bean.model.promotion.ProductGift;
import com.tigshop.bean.vo.promotion.ProductGiftVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.promotion.ProductGiftMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.promotion.GiftCardGroupsService;
import com.tigshop.service.promotion.ProductGiftService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.promotion.ProductGiftConstants.*;

/**
 * 活动赠品服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class ProductGiftServiceImpl extends BaseServiceImpl<ProductGiftMapper, ProductGift> implements ProductGiftService {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private ProductSkuService productSkuService;

    @Resource
    private GiftCardGroupsService giftCardGroupsService;

    @Resource
    private com.tigshop.common.core.TranslatePackage translatePackage;

    /**
     * 翻译赠品 skuInfo：模型未注解，skuValue 为拼接串、skuData 为 JSON 串，需手动翻译。
     */
    private void translateSkuInfo(ProductSku skuInfo) {
        if (skuInfo == null) {
            return;
        }
        skuInfo.setSkuValue(com.tigshop.common.utils.SkuValueTranslateUtils.translateSkuValue(skuInfo.getSkuValue(), translatePackage));
        skuInfo.setSkuData(com.tigshop.common.utils.SkuValueTranslateUtils.translateSkuDataJson(skuInfo.getSkuData(), translatePackage));
    }

    @Override
    public Page<ProductGiftVO> list(ProductGiftListDTO listDTO) {
        // 分页
        Page<ProductGift> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<ProductGift> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(ProductGift::getGiftName, keyword);
        }

        Integer shopId = getShopId();
//        if (shopId != null && shopId > 0) {
            queryWrapper.eq(ProductGift::getShopId, shopId);
//        }
        
        // 执行查询
        Page<ProductGift> productGiftPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<ProductGift> productGiftPageRecords = productGiftPage.getRecords();

        if (CollUtil.isEmpty(productGiftPageRecords)) {
            return PageUtil.convertPage(productGiftPage, Collections.emptyList());
        }

        // 查询商品id
        List<Integer> giftProductIds = productGiftPageRecords.stream()
                .map(ProductGift::getProductId)
                .distinct()
                .toList();

        List<Integer> giftSkuIds = productGiftPageRecords.stream()
                .map(ProductGift::getSkuId)
                .distinct()
                .toList();
        List<Product> productList = productMapper.selectBatchIds(giftProductIds);

        // 将商品信息存入 map 中
        Map<Integer, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        Map<Integer, ProductSku> productSkuMap;
        if(!CollUtil.isEmpty(giftSkuIds)){
            //商品sku列表
            List<ProductSku> productSkuList = productSkuService.lambdaQuery()
                    .in(ProductSku::getSkuId, giftSkuIds).list();
            // 将商品信息存入 map 中
            productSkuMap  = productSkuList.stream()
                    .collect(Collectors.toMap(ProductSku::getSkuId, Function.identity()));
        } else {
            productSkuMap = Collections.emptyMap();
        }

        // 转换为VO
        List<ProductGiftVO> productGiftVOList = productGiftPageRecords.stream()
                .map(productGift -> {
                    ProductGiftVO productGiftVO = new ProductGiftVO();

                    Product product = productMap.get(productGift.getProductId());
                    if (product != null) {
                        productGiftVO.setProductInfo(productMap.get(productGift.getProductId()));
                    }

                    ProductSku productSku = productSkuMap.get(productGift.getSkuId());
                    if (productSku != null) {
                        translateSkuInfo(productSku);
                        productGiftVO.setSkuInfo(productSku);
                    }

                    BeanUtils.copyProperties(productGift, productGiftVO);
                    return productGiftVO;
                }).toList();
        return PageUtil.convertPage(productGiftPage, productGiftVOList);
    }

    @Override
    public ProductGiftVO detail(Integer id) {
        if (id != null) {
            Integer shopId = getShopId();
            LambdaQueryWrapper<ProductGift> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ProductGift::getShopId, shopId);
            queryWrapper.eq(ProductGift::getGiftId, id);
            ProductGift productGift = this.getOne(queryWrapper);
            ProductGiftVO productGiftVO = new ProductGiftVO();
            BeanUtils.copyProperties(productGift, productGiftVO);
            //查询产品信息
            LambdaQueryWrapper<Product> productQueryWrapper = new LambdaQueryWrapper<>();
            productQueryWrapper.eq(Product::getProductId, productGift.getProductId());
            Product product = productMapper.selectOne(productQueryWrapper);
            productGiftVO.setProductInfo(product);
            return productGiftVO;
        }
        return null;
    }

    /**
     * 校验商品是否存在
     * @param id 主键id
     * @param productId 商品id
     * @return 商品数量
     */
    public Long getCount(Integer id,Integer productId) {
        return this.count(new LambdaQueryWrapper<ProductGift>()
                .eq(ProductGift::getProductId,productId)
                .ne(ProductGift::getGiftId,id)
                .select(ProductGift::getProductId));
    }

    /**
     *  商品库存判断
     * @param productId 商品id
     * @param giftStock 赠品库存
     * @param skuId 商品规格id
     */
    public void checkStock(Integer productId, Integer giftStock, Integer skuId) {
        if (null != skuId && skuId > 0) {
            // 规格库存判断
            productSkuService.checkSkuEnabled(productId, skuId);
            ProductSku productSku = productSkuService.getById(skuId);
            if (productSku.getSkuStock() < giftStock) {
                throw new GlobalException(GIFT_STOCK_EXCEED_PRODUCT_SKU);
            }
        } else {
            // 商品库存判断
            Product product = productMapper.selectById(productId);
            if (product.getProductStock() < giftStock) {
                throw new GlobalException(GIFT_STOCK_EXCEED_PRODUCT);
            }
        }
    }

    /**
     * 校验商品是否为礼品卡电子卡，电子卡不允许作为赠品
     */
    private void checkGiftCardECard(Integer productId) {
        Product product = productMapper.selectById(productId);
        if (product != null && product.getGiftCardGroupsId() != null && product.getGiftCardGroupsId() > 0) {
            GiftCardGroups giftCardGroups = giftCardGroupsService.getById(product.getGiftCardGroupsId());
            if (giftCardGroups != null && Integer.valueOf(1).equals(giftCardGroups.getCardType())) {
                throw new GlobalException(GIFT_CARD_E_CARD_NOT_ALLOWED);
            }
        }
    }

    @Override
    public boolean create(ProductGiftCreateDTO createDTO) {
        if (createDTO != null) {
            ProductGift productGift = new ProductGift();
            Integer shopId = getShopId();
            productGift.setShopId(shopId);

            // 商品是否存在
            Long count = getCount(0, createDTO.getProductId());
            if (count > 0) {
                throw new GlobalException(PRODUCT_ID_EXISTS);
            }
            // 礼品卡电子卡校验
            checkGiftCardECard(createDTO.getProductId());
            // 商品库存判断
            checkStock(createDTO.getProductId(), createDTO.getGiftStock(), createDTO.getSkuId());

            BeanUtils.copyProperties(createDTO, productGift);
            return this.save(productGift);
        }
        return false;
    }

    @Override
    public boolean update(ProductGiftUpdateDTO updateDTO) {
        if (updateDTO != null) {
            ProductGift productGift = new ProductGift();

            // 商品是否存在
            Long count = getCount(updateDTO.getGiftId(), updateDTO.getProductId());
            if (count > 0) {
                throw new GlobalException(PRODUCT_ID_EXISTS);
            }
            // 礼品卡电子卡校验
            checkGiftCardECard(updateDTO.getProductId());
            // 商品库存判断
            checkStock(updateDTO.getProductId(), updateDTO.getGiftStock(), updateDTO.getSkuId());

            BeanUtils.copyProperties(updateDTO, productGift);
            return this.updateById(productGift);
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decStock(Integer giftId, Integer quantity) {
        LambdaUpdateWrapper<ProductGift> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProductGift::getGiftId, giftId).setDecrBy(ProductGift::getGiftStock, quantity);
        this.update(updateWrapper);
    }
}
