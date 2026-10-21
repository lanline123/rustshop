// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.api.controller.common;

import com.tigshop.bean.dto.product.ProductListDTO;
import com.tigshop.bean.dto.product.ProductListResDTO;
import com.tigshop.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 推荐
 * @author Tigshop团队
 * @create 2025年01月24日 15:51
 */
@RestController
@RequestMapping("/api/common/recommend")
@Tag(name = "推荐")
public class RecommendController {
    @Resource
    private ProductService productService;

    @GetMapping("/guessLike")
    @Operation(summary = "猜你喜欢")
    public List<ProductListResDTO> getProductList(ProductListDTO dto){
        return productService.getProductList(dto);
    }

    @GetMapping("/getProductIds")
    @Operation(summary = "获取商品ids")
    public String getProductIds(){
        return productService.getProductIds();
    }
}