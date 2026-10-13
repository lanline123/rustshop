package com.tigshop.mapper.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.ProductStockDTO;
import com.tigshop.bean.model.product.ProductStockRecord;
import com.tigshop.bean.vo.product.ProductStockRecordVO;
import com.tigshop.mapper.common.BaseMapper;


public interface ProductStockRecordMapper extends BaseMapper<ProductStockRecord> {

    Page<ProductStockRecordVO> productStockRecordPage(Page<Object> queryPage, ProductStockDTO productStockDTO);
}




