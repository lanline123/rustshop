package com.tigshop.mapper.vendor;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.shop.ShopListDTO;
import com.tigshop.bean.model.vendor.VendorShopBind;
import com.tigshop.bean.param.vendor.VendorShopBindPageParam;
import com.tigshop.bean.param.vendor.shopbind.VendorOrderBindPageParam;
import com.tigshop.bean.param.vendor.shopbind.VendorProductBindPageParam;
import com.tigshop.bean.query.merchant.MerchantListPageQuery;
import com.tigshop.bean.vo.merchant.MerchantVO;
import com.tigshop.bean.vo.shop.ShopVO;
import com.tigshop.bean.vo.vendor.shopbind.VendorShopBindVO;
import com.tigshop.bean.vo.vendor.shopbind.VendorShopOrderBindVO;
import com.tigshop.bean.vo.vendor.shopbind.VendorShopProductBindVO;
import com.tigshop.mapper.common.BaseMapper;
import io.lettuce.core.dynamic.annotation.Param;

/**
 * @author Admin
 * @description 针对表【vendor_shop_bind(供应商店铺绑定表)】的数据库操作Mapper
 * @createDate 2025-07-09 10:53:49
 * @Entity generator.model.VendorShopBind
 */
public interface VendorShopBindMapper extends BaseMapper<VendorShopBind> {


    Page<VendorShopBindVO> pageVendorShopBind(Page<Object> page,
                                              @Param("vendorId") Integer vendorId,
                                              @Param("param") VendorShopBindPageParam param);

    Page<VendorShopProductBindVO> pageProductList(Page<Object> page,
                                                  @Param("vendorId") Integer vendorId,
                                                  @Param("param") VendorProductBindPageParam param);

    Page<VendorShopOrderBindVO> pageOrderList(Page<Object> page,
                                              @Param("vendorId") Integer vendorId,
                                              @Param("param") VendorOrderBindPageParam param);

    Page<MerchantVO> merchantList(Page<Object> page,
                                  @Param("vendorId") Integer vendorId,
                                  @Param("param") MerchantListPageQuery param);


    Page<ShopVO> shopList(Page<Object> page,
                          @Param("vendorId") Integer vendorId,
                          @Param("param") ShopListDTO param);
}




