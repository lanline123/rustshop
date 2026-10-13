package com.tigshop.adminapi.controller.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.query.product.ProductInventoryLogPageQuery;
import com.tigshop.bean.vo.product.ProductInventoryLogVO;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.service.product.ProductInventoryLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存日志控制器
 *
 * @author kidd
 * @create 2025/6/30
 */
@RequiredArgsConstructor
@Tag(name = "库存日志", description = "库存日志功能")
@PreAuthorize("@pms.hasPermission('productInventoryLogManage')")
@RestController
@RequestMapping("/adminapi/product/productInventoryLog")
public class ProductInventoryLogController {

    private final ProductInventoryLogService productInventoryLogService;

    @Operation(summary = "获取列表")
    @GetMapping("/list")
    public Page<ProductInventoryLogVO> list(ProductInventoryLogPageQuery pageQuery) {
        pageQuery.setShopId(HeaderUtils.getShopId());
        return productInventoryLogService.list(pageQuery);
    }

}
