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

import com.tigshop.bean.vo.product.KeywordSearchListVO;
import com.tigshop.service.product.ProductSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品
 *
 * @author Tigshop团队
 * @create 2025年02月12日 14:59
 */
@RestController
@RequestMapping("/api/search/searchGuess")
@Tag(name = "商品")
public class SearchGuessController {
    @Resource
    private ProductSearchService productSearchService;

    @GetMapping("/index")
    @Operation(summary = "搜索关键词联想")
    public List<KeywordSearchListVO> getSearchGuessList(@RequestParam(value = "keyword", required = false) String keyword) {
        return productSearchService.searchGuess(keyword);
    }

}