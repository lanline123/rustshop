package com.tigshop.service.product.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.dto.product.ProductStockAdjustDTO;
import com.tigshop.bean.enums.product.ProductType;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.product.ProductStockAdjustRecord;
import com.tigshop.bean.vo.product.ProductStockAdjustRecordDetailVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.product.ProductStockAdjustRecordMapper;
import com.tigshop.service.ProductStockAdjustRecordService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.product.StockWarningService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class ProductStockAdjustRecordServiceImpl extends BaseServiceImpl<ProductStockAdjustRecordMapper, ProductStockAdjustRecord> implements ProductStockAdjustRecordService {

    private final ProductSkuService productSkuService;

    private final ProductService productService;

    private final StockWarningService stockWarningService;

    private final RedisCache redisCache;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean stockAdjust(ProductStockAdjustDTO productStockAdjustDTO) {

        if (productStockAdjustDTO.getAdjustType() == 0 &&  productStockAdjustDTO.getQuantity() < 0){
            throw new GlobalException("覆盖调整调整数量不能小于0");
        }
        String uuid = UUID.randomUUID().toString();
        boolean lock = redisCache.tryLock("addOrDescStockLock", uuid, 10, TimeUnit.SECONDS);
        Integer changeBeforeQuantity = 0;
        Integer changeAfterQuantity = 0;
        if (lock) {
            try {
                Product product = productService.getById(productStockAdjustDTO.getProductId());
                changeBeforeQuantity = product.getProductStock();
                ProductSku productSku = null;
                if (productStockAdjustDTO.getSkuId() != null) {
                    productSku = productSkuService.getById(productStockAdjustDTO.getSkuId());
                    changeBeforeQuantity = productSku.getSkuStock();
                }

                //如果是增量更新
                if (productStockAdjustDTO.getAdjustType() == 1) {
                    //更新商品库存,如果是无规格商品则直接设置商品库存
                    if (productSku != null) {
                        if (productStockAdjustDTO.getQuantity() < 0) {
                            productSkuService.decStock(productSku.getSkuId(), Math.abs(productStockAdjustDTO.getQuantity()), "调整减少库存");
                        } else {
                            productSkuService.incStock(productSku.getSkuId(), Math.abs(productStockAdjustDTO.getQuantity()), "调整增加库存");
                        }

                    }
                    boolean hassku = productSku != null;
                    if (productStockAdjustDTO.getQuantity() < 0) {
                        if (hassku){
                            int quantity = productSku.getSkuStock() - Math.abs(productStockAdjustDTO.getQuantity());
                            if (quantity < 0){
                                quantity = productSku.getSkuStock();
                                productService.decStock(product.getProductId(), quantity, "调整减少库存", false);
                            }else {
                                productService.decStock(product.getProductId(), Math.abs(productStockAdjustDTO.getQuantity()), "调整减少库存", false);
                            }

                        }else {
                            productService.decStock(product.getProductId(), Math.abs(productStockAdjustDTO.getQuantity()), "调整减少库存");
                        }

                    } else {
                        productService.incStock(product.getProductId(), productStockAdjustDTO.getQuantity(), "调整增加库存", !hassku);
                    }
                } else if (productStockAdjustDTO.getAdjustType() == 0) {
                    //如果是覆盖调整,商品表减掉旧库存，加上新库存
                    boolean hasSku = productSku != null;
                    Integer quantity = productStockAdjustDTO.getQuantity();
                    if (hasSku){
                        quantity = productSku.getSkuStock() - productStockAdjustDTO.getQuantity();
                        if (quantity < 0){
                            productService.incStock(product.getProductId(), Math.abs(quantity), "覆盖调整库存", false);
                        }else {
                            productService.decStock(product.getProductId(), quantity, "覆盖调整库存", false);
                        }
                    }else {
                        //如果没有sku，则直接设置product库存
                        int stockQuantity = product.getProductStock() - productStockAdjustDTO.getQuantity();
                        if (stockQuantity > 0){
                            productService.decStock(product.getProductId(), stockQuantity, "覆盖调整库存");
                        }else {
                            productService.incStock(product.getProductId(), Math.abs(stockQuantity), "覆盖调整库存");
                        }
                    }

                    if (productSku != null) {
                        //如果传入库存大于现有库存则是增加，否是减少
                        int productSkuStockQuantity = productSku.getSkuStock() - productStockAdjustDTO.getQuantity();
                        if (productSkuStockQuantity > 0) {
                            productSkuService.decStock(productSku.getSkuId(), productSkuStockQuantity, "调整库存");
                        } else {
                            productSkuService.incStock(productSku.getSkuId(), Math.abs(productSkuStockQuantity), "调整库存");
                        }
                    }
                }
                Product changeAfterProduct = productService.getById(productStockAdjustDTO.getProductId());
                changeAfterQuantity = changeAfterProduct.getProductStock();
                ProductSku changeAfterProductSku = null;
                if (productStockAdjustDTO.getSkuId() != null) {
                    changeAfterProductSku = productSkuService.getById(productStockAdjustDTO.getSkuId());
                    changeAfterQuantity = changeAfterProductSku.getSkuStock();
                }

                ProductStockAdjustRecord productStockAdjustRecord = ProductStockAdjustRecord.builder()
                        .skuId(productStockAdjustDTO.getSkuId())
                        .adjustType(productStockAdjustDTO.getAdjustType())
                        .quantity(productStockAdjustDTO.getQuantity())
                        .addBy(SecurityUtils.getCurrentUsername())
                        .addTime(System.currentTimeMillis() / 1000)
                        .productId(productStockAdjustDTO.getProductId())
                        .changeBeforeQuantity(changeBeforeQuantity)
                        .changeAfterQuantity(changeAfterQuantity)
                        .shopId(HeaderUtils.getShopId() == null ? 0 : HeaderUtils.getShopId()).build();
                boolean save = this.save(productStockAdjustRecord);
                if (save) {
                    //库存已调整，重新判定库存预警状态
                    stockWarningService.evaluate(productStockAdjustRecord.getShopId(),
                            productStockAdjustDTO.getProductId(), productStockAdjustDTO.getSkuId());
                }
                return save;
            } catch (Exception e) {
                throw new GlobalException(e.getMessage());
            } finally {
                redisCache.unlock("addOrDescStockLock", uuid);
            }
        }else {
            throw new GlobalException("其他线程正在修改此数据，请稍侯再试");
        }
    }

    @Override
    public List<ProductStockAdjustRecordDetailVO> adjustDetail(Integer id,Integer skuId) {

        QueryWrapper<ProductStockAdjustRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", id);
        if (skuId != null){
            queryWrapper.eq("sku_id", skuId);
        }
        queryWrapper.eq("shop_id", HeaderUtils.getShopId() == null ? 0 : HeaderUtils.getShopId());
        List<ProductStockAdjustRecord> productStockAdjustRecords = this.list(queryWrapper);
        ArrayList<ProductStockAdjustRecordDetailVO> productStockAdjustRecordDetailVOS = new ArrayList<>();
        if (CollUtil.isNotEmpty(productStockAdjustRecords)) {
            for (ProductStockAdjustRecord productStockAdjustRecord : productStockAdjustRecords) {
                ProductStockAdjustRecordDetailVO productStockAdjustRecordDetailVO = new ProductStockAdjustRecordDetailVO();
                BeanUtils.copyProperties(productStockAdjustRecord, productStockAdjustRecordDetailVO);
                productStockAdjustRecordDetailVO.setAddTime(TigUtils.handelTime(productStockAdjustRecord.getAddTime()));
                productStockAdjustRecordDetailVOS.add(productStockAdjustRecordDetailVO);
            }
        }
        return productStockAdjustRecordDetailVOS;
    }
}
