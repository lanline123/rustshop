// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.api.controller.search;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.ProductListResDTO;
import com.tigshop.bean.dto.product.ProductSearchClientDTO;
import com.tigshop.bean.vo.search.SearchFilterVO;
import com.tigshop.service.product.ProductSearchService;
import com.tigshop.service.product.ProductEsSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品
 *
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/api/search/search")
@Tag(name = "商品")
public class SearchController {

    @Resource
    private ProductSearchService productSearchService;

    @PostMapping("/getProduct")
    @Operation(summary = "商品列表")
    public Page<ProductListResDTO> list(@RequestBody ProductSearchClientDTO productSearchClientDTO) {
        return productSearchService.list(productSearchClientDTO);
    }

    @PostMapping("/getFilter")
    @Operation(summary = "商品筛选")
    public SearchFilterVO getFilter(@RequestBody ProductSearchClientDTO productSearchClientDTO) {
        return SearchFilterVO.builder()
                .filter(productSearchService.getFilterList(productSearchClientDTO))
                .filterSelected(productSearchService.getFilterSelected(productSearchClientDTO))
                .build();
    }

    @GetMapping("/getAttributeSuggestions")
    @Operation(summary = "获取属性搜索建议")
    public Map<String, Object> getAttributeSuggestions(ProductSearchClientDTO productSearchClientDTO) {
        List<Map<String, Object>> attributes = productSearchService.getAttributeSuggestions(productSearchClientDTO);

        Map<String, Object> data = new HashMap<>();
        data.put("attributes", attributes);

        return data;
    }

}