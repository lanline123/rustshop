package com.tigshop.mapper.o2o;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.ProductStockDTO;
import com.tigshop.bean.model.o2o.StoreProduct;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tigshop.bean.vo.product.ProductStockPageVO;
import com.tigshop.bean.vo.product.ProductStockResultVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author kidd
 * @description 针对表【store_product(分配店铺商品)】的数据库操作Mapper
 * @createDate 2025-08-25 13:08:31
 * @Entity com.tigshop.bean.model.o2o.StoreProduct
 */
@Mapper
public interface StoreProductMapper extends BaseMapper<StoreProduct>, com.tigshop.mapper.common.BaseMapper<StoreProduct> {

    Page<ProductStockPageVO> getStoreProductStockPage(Page<ProductStockResultVO> page, ProductStockDTO productStockDTO);

    /**
     * 场景1：平台调拨给门店 - 查询平台商品中已分配给目标门店的商品
     * shopId = 0, targetShopId > 0
     */
    Page<ProductStockPageVO> getPlatformAllocateToStorePage(Page<ProductStockPageVO> page, @Param("productStockDTO") ProductStockDTO productStockDTO);

    /**
     * 场景2：门店调拨给平台 - 查询门店已分配的商品
     * shopId > 0, targetShopId = 0
     */
    Page<ProductStockPageVO> getStoreAllocateToPlatformPage(Page<ProductStockPageVO> page, @Param("productStockDTO") ProductStockDTO productStockDTO);

    /**
     * 场景3：门店调拨给门店 - 查询两个门店都有分配的商品（product_id相同）
     * shopId > 0, targetShopId > 0
     */
    Page<ProductStockPageVO> getStoreAllocateToStorePage(Page<ProductStockPageVO> page, @Param("productStockDTO") ProductStockDTO productStockDTO);
}




