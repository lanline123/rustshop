// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.adminapi.controller.search;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.ProductListDTO;
import com.tigshop.bean.dto.product.ProductListResDTO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 商家端工作台全局搜索
 *
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/adminapi/search")
@Tag(name = "全局搜索")
@PreAuthorize("@pms.hasPermission('productManage')")
public class SearchController {

    /**
     * 综合搜索商品预览默认条数
     */
    private static final int AGGREGATE_PRODUCT_PREVIEW_SIZE = 5;

    @Resource
    private ProductService productService;

    @GetMapping("/aggregate")
    @Operation(summary = "综合搜索")
    public Page<ProductListResDTO> aggregate(ProductListDTO listDTO) {
        // 综合搜索固定取前 N 条预览，忽略客户端传入的分页
        listDTO.setPage(1);
        listDTO.setSize(AGGREGATE_PRODUCT_PREVIEW_SIZE);
        applySearchScope(listDTO);
        Page<ProductListResDTO> page = productService.list(listDTO);
        return page;
    }

    @GetMapping("/product")
    @Operation(summary = "商品搜索")
    public Page<ProductListResDTO> product(ProductListDTO listDTO) {
        applySearchScope(listDTO);
        return productService.list(listDTO);
    }

    /**
     * 统一收口搜索口径：店铺隔离与状态范围，保证与 product/product/list 一致。
     */
    private void applySearchScope(ProductListDTO listDTO) {
        // 店铺隔离：按请求头身份强制覆盖 shopId（同 product/product/list）
        Integer headerShopId = HeaderUtils.getShopId();
        if (headerShopId != null && headerShopId > 0) {
            listDTO.setShopId(headerShopId);
        }
        // 搜索范围：仅排除回收站（全状态可被搜到），与文档第 5.2 节默认口径一致
        listDTO.setIsDelete(Constants.NO);
    }
}
