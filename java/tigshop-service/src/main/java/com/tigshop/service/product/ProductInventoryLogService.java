package com.tigshop.service.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.ProductStockDetailDTO;
import com.tigshop.bean.query.product.ProductInventoryLogPageQuery;
import com.tigshop.bean.model.product.ProductInventoryLog;
import com.tigshop.bean.vo.product.ProductInventoryLogVO;
import com.tigshop.bean.vo.product.ProductStockDetailVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 库存日志服务
 *
 * @author kidd
 * @create 2025/6/30
 */
public interface ProductInventoryLogService extends BaseService<ProductInventoryLog> {

    /**
     * 列表
     */
    Page<ProductInventoryLogVO> list(ProductInventoryLogPageQuery pageQuery);

    Page<ProductStockDetailVO> getDetail(ProductStockDetailDTO productStockDetailDTO);
}
