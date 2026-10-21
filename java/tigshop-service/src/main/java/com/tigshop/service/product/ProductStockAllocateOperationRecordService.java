package com.tigshop.service.product;

import com.tigshop.bean.model.product.ProductStockAllocateOperationRecord;
import com.tigshop.bean.vo.product.ProductStockAllocateOperationRecordVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

public interface ProductStockAllocateOperationRecordService extends BaseService<ProductStockAllocateOperationRecord> {
    List<ProductStockAllocateOperationRecordVO> productStockAllocateOperationDetail(Integer allocateId);
}
