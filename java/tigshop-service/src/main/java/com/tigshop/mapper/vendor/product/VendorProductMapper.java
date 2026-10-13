package com.tigshop.mapper.vendor.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.vendor.product.VendorProduct;
import com.tigshop.bean.query.vendor.VendorProductPageQuery;
import com.tigshop.bean.query.vendor.VendorProductPlatformPageQuery;
import com.tigshop.bean.vo.vendor.product.VendorProductPlatformPageVO;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author kidd
 */
public interface VendorProductMapper extends BaseMapper<VendorProduct> {

    /**
     * 供应商商品平台端列表
     */
    Page<VendorProductPlatformPageVO> platformPageList(@Param("page") Page<VendorProductPlatformPageVO> page, @Param("pageQuery") VendorProductPlatformPageQuery pageQuery);

    Page<VendorProduct> pageList(@Param("page") Page<VendorProduct> page, @Param("vendorId") Integer vendorId, @Param("query") VendorProductPageQuery pageQuery);
}




