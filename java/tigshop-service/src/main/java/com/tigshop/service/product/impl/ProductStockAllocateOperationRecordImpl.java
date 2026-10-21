package com.tigshop.service.product.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.model.product.ProductStockAllocateOperationRecord;
import com.tigshop.bean.vo.product.ProductStockAllocateOperationRecordVO;
import com.tigshop.bean.vo.product.ProductStockAllocateRecordDetailVO;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.product.ProductStockAllocateOperationRecordMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductStockAllocateOperationRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductStockAllocateOperationRecordImpl extends BaseServiceImpl<ProductStockAllocateOperationRecordMapper, ProductStockAllocateOperationRecord> implements ProductStockAllocateOperationRecordService {
    @Override
    public List<ProductStockAllocateOperationRecordVO> productStockAllocateOperationDetail(Integer allocateId) {
        if (allocateId == null) {
            return new ArrayList<>();
        }

        List<ProductStockAllocateOperationRecord> allocateOperationRecords = this.list(
                Wrappers.lambdaQuery(ProductStockAllocateOperationRecord.class)
                        .eq(ProductStockAllocateOperationRecord::getAllocateId, allocateId)
        );

        if (allocateOperationRecords == null || allocateOperationRecords.isEmpty()) {
            return new ArrayList<>();
        }

        return allocateOperationRecords.stream()
                .map(record -> {
                    ProductStockAllocateOperationRecordVO vo = new ProductStockAllocateOperationRecordVO();
                    BeanUtils.copyProperties(record, vo);
                    vo.setAddTime(TigUtils.handelTime(record.getAddTime()));
                    return vo;
                })
                .toList();
    }

}
