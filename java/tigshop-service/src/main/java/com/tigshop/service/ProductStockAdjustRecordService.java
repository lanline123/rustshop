package com.tigshop.service;

import com.tigshop.bean.dto.product.ProductStockAdjustDTO;
import com.tigshop.bean.model.product.ProductStockAdjustRecord;
import com.tigshop.bean.vo.product.ProductStockAdjustRecordDetailVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

public interface ProductStockAdjustRecordService extends BaseService<ProductStockAdjustRecord> {
    boolean stockAdjust(ProductStockAdjustDTO productStockAdjustDTO);

    List<ProductStockAdjustRecordDetailVO> adjustDetail(Integer id,Integer skuId);

}
