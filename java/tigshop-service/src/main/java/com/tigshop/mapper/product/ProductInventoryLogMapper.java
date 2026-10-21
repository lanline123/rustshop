package com.tigshop.mapper.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.query.product.ProductInventoryLogPageQuery;
import com.tigshop.bean.vo.product.ProductInventoryLogVO;
import com.tigshop.mapper.common.BaseMapper;
import com.tigshop.bean.model.product.ProductInventoryLog;
import org.apache.ibatis.annotations.Param;

/**
 * 库存日志映射
 *
 * @author kidd
 * @create 2025/7/3
 */
public interface ProductInventoryLogMapper extends BaseMapper<ProductInventoryLog> {

    /**
     * 获取库存日志列表
     */
    Page<ProductInventoryLogVO> productInventoryLog(@Param("page") Page<ProductInventoryLogVO> page, @Param("pageQuery") ProductInventoryLogPageQuery pageQuery);
}
