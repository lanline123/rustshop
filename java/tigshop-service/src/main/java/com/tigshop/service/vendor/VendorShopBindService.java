package com.tigshop.service.vendor;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.dto.shop.ShopListDTO;
import com.tigshop.bean.model.vendor.VendorShopBind;
import com.tigshop.bean.param.vendor.VendorShopBindPageParam;
import com.tigshop.bean.param.vendor.shopbind.VendorOrderBindPageParam;
import com.tigshop.bean.param.vendor.shopbind.VendorProductBindPageParam;
import com.tigshop.bean.query.merchant.MerchantListPageQuery;
import com.tigshop.bean.vo.merchant.MerchantVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.shop.ShopVO;
import com.tigshop.bean.vo.vendor.shopbind.VendorShopBindVO;
import com.tigshop.bean.vo.vendor.shopbind.VendorShopProductBindVO;

/**
* @author Admin
* @description 针对表【vendor_shop_bind(供应商店铺绑定表)】的数据库操作Service
* @createDate 2025-07-09 10:53:50
*/
public interface VendorShopBindService extends IService<VendorShopBind> {
    Page<VendorShopBindVO> list(VendorShopBindPageParam param);

    Page<VendorShopProductBindVO> productList(VendorProductBindPageParam param);

    Page<OrderVO> orderList(VendorOrderBindPageParam param);

    Page<MerchantVO> merchantList(MerchantListPageQuery pageQuery);


    Page<ShopVO> shopList(ShopListDTO listDTO);
}
