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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.promotion.PointsExchangeCreateDTO;
import com.tigshop.bean.dto.promotion.PointsExchangeListDTO;
import com.tigshop.bean.dto.promotion.PointsExchangeUpdateDTO;
import com.tigshop.bean.enums.product.DeleteType;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.promotion.PointsExchange;
import com.tigshop.bean.vo.promotion.PointsExchangeVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.promotion.PointsExchangeMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.promotion.PointsExchangeService;
import com.tigshop.service.setting.GalleryPicService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.promotion.PointsExchangeConstants.POINTS_EXCHANGE_PRODUCT_EXIST;

/**
 * 积分商品服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class PointsExchangeServiceImpl extends BaseServiceImpl<PointsExchangeMapper, PointsExchange> implements PointsExchangeService {

    @Resource
    private ProductService productService;

    @Resource
    private ProductSkuService productSkuService;

    @Resource
    private GalleryPicService galleryPicService;

    @Override
    public Page<PointsExchangeVO> list(PointsExchangeListDTO listDTO) {
        // 分页
        Page<PointsExchange> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        MPJLambdaWrapper<PointsExchange> queryWrapper = new MPJLambdaWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            // 获取商品id
            LambdaQueryWrapper<Product> productQueryWrapper = new LambdaQueryWrapper<>();
            productQueryWrapper.eq(Product::getIsDelete, DeleteType.NOT_DELETE.getCode());
            productQueryWrapper.like(Product::getProductName, keyword);
            //未被删除
            productQueryWrapper.eq(Product::getIsDelete, 0);
            //上架
            productQueryWrapper.eq(Product::getProductStatus, 1);
            List<Product> product = productService.list(productQueryWrapper);

            List<Integer> matchingProductIds = product.stream()
                    .map(Product::getProductId)
                    .toList();
            if (matchingProductIds.isEmpty()) {
                return new Page<>();
            }
            queryWrapper.in(PointsExchange::getProductId, matchingProductIds);
        } else {
            //用于连表,增加商品有效过滤
            queryWrapper.innerJoin(Product.class,
                    Product::getProductId, PointsExchange::getProductId);
            //上架
            queryWrapper.eq(Product::getProductStatus, 1);
            //已被删除
            queryWrapper.eq(Product::getIsDelete, 0);
        }

        // 状态查询
        Integer isEnabled = listDTO.getIsEnabled();
        if (isEnabled != null) {
            queryWrapper.eq(PointsExchange::getIsEnabled, isEnabled);
        }
        if (Objects.equals(isEnabled, Constants.YES)) {
            queryWrapper.leftJoin(ProductSku.class, ProductSku::getSkuId, PointsExchange::getSkuId)
                    .and(wrapper -> wrapper
                            .isNull(PointsExchange::getSkuId)
                            .or().le(PointsExchange::getSkuId, 0)
                            .or().eq(ProductSku::getIsEnabled, Constants.YES));
        }

        Integer isHot = listDTO.getIsHot();
        if (isHot != null) {
            queryWrapper.eq(PointsExchange::getIsHot, isHot);
        }

        // 执行查询
        Page<PointsExchange> pointsExchangePage = this.page(page, queryWrapper);
        // 获取查询结果
        List<PointsExchange> pointsExchangePageRecords = pointsExchangePage.getRecords();

        // 查询商品id
        List<Integer> productIds = pointsExchangePageRecords.stream()
                .map(PointsExchange::getProductId)
                .distinct()
                .toList();
        List<Product> productList = new ArrayList<>();
        if (CollUtil.isNotEmpty(productIds)) {
            productList = productService.listByIds(productIds);
        }
        Map<Integer, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        // 查询商品skuId
        List<Integer> skuIds = pointsExchangePageRecords.stream()
                .map(PointsExchange::getSkuId)
                .distinct()
                .toList();

        Map<Integer, ProductSku> productSkuMap;
        if (!skuIds.isEmpty()) {
            List<ProductSku> productSkus = productSkuService.listByIds(skuIds);
            productSkuMap = productSkus.stream()
                    .collect(Collectors.toMap(ProductSku::getSkuId, Function.identity()));
        } else {
            productSkuMap = null;
        }

        // 转换为VO
        List<PointsExchangeVO> pointsExchangeVOList = pointsExchangePageRecords.stream()
                .map(pointsExchange -> {
                    PointsExchangeVO pointsExchangeVO = new PointsExchangeVO();
                    BeanUtils.copyProperties(pointsExchange, pointsExchangeVO);

                    Product product = productMap.get(pointsExchange.getProductId());
                    if (product != null) {
                        pointsExchangeVO.setProductName(product.getProductName());
                        pointsExchangeVO.setPicThumb(product.getPicThumb());
                        pointsExchangeVO.setPicUrl(product.getPicUrl());
                        pointsExchangeVO.setMarketPrice(product.getMarketPrice());
                        pointsExchangeVO.setProductPrice(product.getProductPrice());
                        pointsExchangeVO.setProductStock(product.getProductStock());
                        pointsExchangeVO.setVirtualSales(product.getVirtualSales());
                        pointsExchangeVO.setProductSn(product.getProductSn());
                    }

                    if (productSkuMap != null) {
                        ProductSku productSku = productSkuMap.get(pointsExchange.getSkuId());
                        if (productSku != null && productSku.getSkuId() > 0) {
                            pointsExchangeVO.setProductPrice(productSku.getSkuPrice());
                        }

                    }
                    BigDecimal productPrice = pointsExchangeVO.getProductPrice();
                    if (productPrice != null) {
                        pointsExchangeVO.setDiscountsPrice(pointsExchangeVO.getProductPrice().subtract(pointsExchangeVO.getPointsDeductedAmount()));
                    } else {
                        pointsExchangeVO.setDiscountsPrice(BigDecimal.ZERO);
                    }

                    return pointsExchangeVO;
                }).toList();
        return PageUtil.convertPage(pointsExchangePage, pointsExchangeVOList);
    }

    @Override
    public PointsExchangeVO detail(Integer id) {
        if (id == null) {
            return null;
        }
        PointsExchange pointsExchange = this.getById(id);
        PointsExchangeVO pointsExchangeVO = new PointsExchangeVO();
        BeanUtils.copyProperties(pointsExchange, pointsExchangeVO);

        // 获取商品名称
        Product product = productService.getById(pointsExchange.getProductId());
        pointsExchangeVO.setProductName(product.getProductName());
        pointsExchangeVO.setPicThumb(product.getPicThumb());
        pointsExchangeVO.setPicUrl(product.getPicUrl());
        pointsExchangeVO.setMarketPrice(product.getMarketPrice());
        pointsExchangeVO.setProductPrice(product.getProductPrice());
        pointsExchangeVO.setProductStock(product.getProductStock());
        pointsExchangeVO.setVirtualSales(product.getVirtualSales());
        pointsExchangeVO.setProductSn(product.getProductSn());

        if (pointsExchange.getSkuId() > 0) {
            ProductSku productSku = productSkuService.getById(pointsExchange.getSkuId());
            if (productSku != null && productSku.getSkuId() > 0) {
                pointsExchangeVO.setProductPrice(productSku.getSkuPrice());
            }
        }
        pointsExchangeVO.setDiscountsPrice(pointsExchangeVO.getProductPrice().subtract(pointsExchangeVO.getPointsDeductedAmount()));

        BeanUtils.copyProperties(pointsExchange, pointsExchangeVO);
        return pointsExchangeVO;
    }

    @Override
    public PointsExchangeVO clientDetail(Integer id) {
        if (id != null) {
            PointsExchange pointsExchange = this.getById(id);
            PointsExchangeVO pointsExchangeVO = new PointsExchangeVO();

            // 获取商品名称
            Product product = productService.getById(pointsExchange.getProductId());
            pointsExchangeVO.setProductName(product.getProductName());

            BeanUtils.copyProperties(pointsExchange, pointsExchangeVO);
            return pointsExchangeVO;
        }
        return null;
    }

    /**
     * 校验商品是否存在
     *
     * @param id        积分商品id
     * @param productId 商品id
     * @return 商品数量
     */
    public Long getCount(Integer id, Integer productId) {
        return this.count(new LambdaQueryWrapper<PointsExchange>()
                .eq(PointsExchange::getProductId, productId)
                .ne(PointsExchange::getId, id)
                .select(PointsExchange::getProductId));
    }

    @Override
    public boolean create(PointsExchangeCreateDTO createDTO) {
        if (createDTO != null) {
            PointsExchange pointsExchange = new PointsExchange();
            if (createDTO.getExchangeIntegral() <= 0) {
                throw new GlobalException("兑换积分不能小于或等于0");
            }
            // 校验商品是否存在
            Long count = getCount(0, createDTO.getProductId());
            if (count > 0) {
                throw new GlobalException(POINTS_EXCHANGE_PRODUCT_EXIST);
            }

            productSkuService.checkSkuEnabled(createDTO.getProductId(), createDTO.getSkuId());
            BeanUtils.copyProperties(createDTO, pointsExchange);
            return this.save(pointsExchange);
        }
        return false;
    }

    @Override
    public boolean update(PointsExchangeUpdateDTO updateDTO) {
        if (updateDTO != null) {
            PointsExchange pointsExchange = new PointsExchange();
            if (updateDTO.getExchangeIntegral() <= 0) {
                throw new GlobalException("兑换积分不能小于或等于0");
            }
            // 校验商品是否存在
            Long count = getCount(updateDTO.getId(), updateDTO.getProductId());
            if (count > 0) {
                throw new GlobalException(POINTS_EXCHANGE_PRODUCT_EXIST);
            }

            productSkuService.checkSkuEnabled(updateDTO.getProductId(), updateDTO.getSkuId());
            BeanUtils.copyProperties(updateDTO, pointsExchange);
            return this.updateById(pointsExchange);
        }
        return false;
    }

    @Override
    public PointsExchange getInfoByProductId(Integer productId, int skuId) {
        return this.getOne(new LambdaQueryWrapper<PointsExchange>().eq(PointsExchange::getProductId, productId).eq(PointsExchange::getSkuId, skuId).last("limit 1"));
    }
}
