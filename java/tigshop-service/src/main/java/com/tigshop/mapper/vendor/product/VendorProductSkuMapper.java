package com.tigshop.mapper.vendor.product;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.vendor.VendorStockDTO;
import com.tigshop.bean.model.vendor.product.VendorProductSku;
import com.tigshop.bean.vo.vendor.product.VendorProductStockPageVO;
import org.apache.ibatis.annotations.Param;

/**
* @author kidd
*/
public interface VendorProductSkuMapper extends BaseMapper<VendorProductSku> {

    /**
     * 供应商库存列表分页（按规格维度，join vendor_product 取商品名/缩略图）
     */
    Page<VendorProductStockPageVO> getVendorProductStockPage(@Param("page") Page<VendorProductStockPageVO> page, @Param("dto") VendorStockDTO dto);
}




