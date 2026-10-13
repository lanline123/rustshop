package com.tigshop.service.vendor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.model.vendor.product.VendorProductSku;
import com.tigshop.bean.param.vendor.product.VendorProductEditParam;
import com.tigshop.bean.param.vendor.product.VendorProductSaveParam;
import com.tigshop.bean.param.vendor.product.VendorProductSkuStockParam;

import java.util.List;

/**
* @author kidd
*/
public interface VendorProductSkuService extends IService<VendorProductSku> {

    /**
     * 保存商品规格
     */
    void saveBatchSku(VendorProductSaveParam param, Long vendorProductId);

    /**
     * 修改商品规格
     */
    void updateBatchSku(VendorProductEditParam param, Long vendorProductId);

    /**
     * 新增商品规格库存
     */
    void incStock(VendorProductSkuStockParam param);

    /**
     * 增加商品规格库存扣减销量
     */
    void incStockAndSalesVolume(VendorProductSkuStockParam param);

    /**
     * 批量新增商品规格库存
     */
    void batchIncStock(List<VendorProductSkuStockParam> params);

    /**
     * 扣减商品规格库存
     */
    void decStock(VendorProductSkuStockParam param);

    /**
     * 扣减商品规格库存增加销量
     */
    void decStockAndSalesVolume(VendorProductSkuStockParam param);

    /**
     * 批量扣减商品规格库存
     */
    void batchDecStock(List<VendorProductSkuStockParam> params);
}
