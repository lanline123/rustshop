package com.tigshop.api.controller.product;

import com.tigshop.bean.vo.product.ProductActiveStyleVO;
import com.tigshop.service.product.ProductStyleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product/productStyle")
@Tag(name = "商品样式")
@Validated
public class ProductStyleController {

    @Resource
    private ProductStyleService productStyleService;

    @GetMapping("/activeStyle")
    @Operation(summary = "获取商品当前生效样式")
    public ProductActiveStyleVO getActiveStyle(@RequestParam("productId") Integer productId) {
        return productStyleService.getActiveStyle(productId);
    }

    @GetMapping("/batchActiveStyle")
    @Operation(summary = "批量获取商品生效样式")
    public Map<Integer, ProductActiveStyleVO> batchGetActiveStyles(@RequestParam("productIds") List<Integer> productIds) {
        return productStyleService.batchGetActiveStyles(productIds);
    }

    @GetMapping("/batchActiveStyleBySn")
    @Operation(summary = "批量获取商品生效样式（通过商品编号SN）")
    public Map<String, ProductActiveStyleVO> batchGetActiveStylesBySns(@RequestParam("productSns") List<String> productSns) {
        return productStyleService.batchGetActiveStylesBySns(productSns);
    }
}
