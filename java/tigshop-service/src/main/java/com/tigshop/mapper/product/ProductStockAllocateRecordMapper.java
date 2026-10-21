package com.tigshop.mapper.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.ProductStockAllocateExcelExportDTO;
import com.tigshop.bean.dto.product.ProductStockAllocateRecordDTO;
import com.tigshop.bean.model.product.ProductStockAllocateRecord;
import com.tigshop.bean.vo.product.ProductStockAllocateRecordPageVO;
import com.tigshop.mapper.common.BaseMapper;

import java.util.List;

/**
* @author 13978305776
* @description 针对表【product_stock_allocate_recoed(库存调拨表)】的数据库操作Mapper
* @createDate 2025-10-11 14:07:43
* @Entity generator.model.ProductStockAllocateRecoed
*/
public interface ProductStockAllocateRecordMapper extends BaseMapper<ProductStockAllocateRecord> {

    Page<ProductStockAllocateRecordPageVO> getPage(Page<ProductStockAllocateRecordPageVO> allocateRecordPage, ProductStockAllocateRecordDTO productStockAllocateRecordDTO);

    List<ProductStockAllocateRecord> getListByProductIds(ProductStockAllocateExcelExportDTO excelExportDTO);
}




