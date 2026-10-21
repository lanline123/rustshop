// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.api.controller.product;

import cn.hutool.core.lang.tree.Tree;
import com.tigshop.bean.dto.order.CategoryParentTreeDTO;
import com.tigshop.bean.dto.product.ProductRelateDTO;
import com.tigshop.bean.model.product.Brand;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.vo.content.ArticleVO;
import com.tigshop.bean.vo.product.ProductLookAlsoVO;
import com.tigshop.bean.vo.product.RelateRankVO;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.service.product.CategoryService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.shop.ShopProductCategoryService;
import com.tigshop.service.shop.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品分类控制器
 *
 * @author Jayce
 * @create 2024年11月01日 10:13
 */
@RestController
@RequestMapping("/api/category/category")
@Tag(name = "商品分类")
@Validated
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @Resource
    private ProductService productService;
    @Resource
    private TigshopProperties tigshopProperties;
    @Resource
    private ShopService shopService;
    @Resource
    private ShopProductCategoryService shopProductCategoryService;

    @GetMapping("/all")
    @Operation(summary = "获取所有商品分类")
    public List<Tree<Integer>> getAllCategory() {
        return categoryService.getAllCategory();
    }

    @GetMapping("/list")
    @Operation(summary = "获取指定分类下的所有子分类")
    public List<Tree<Integer>> getChildrenCategoryTreeById(@RequestParam("id") Integer categoryId, @RequestParam(value = "shopId", required = false) Integer shopId) {
        List<Tree<Integer>> childrenCategoryTreeById = categoryService.getChildrenCategoryTreeById(categoryId);
        return childrenCategoryTreeById;
    }

    // 看了还看
    @GetMapping("/relateLookAlso")
    @Operation(summary = "获取商品详情页中，看了还看商品")
    public List<ProductLookAlsoVO> getRelateLookAlso(ProductRelateDTO dto) {
        return productService.getLookAlso(dto);
    }

    // 热门分类
    @GetMapping("/hot")
    @Operation(summary = "获取热门分类")
    public List<Category> getHotCategory() {
        return categoryService.getHotCategory();
    }

    // 相关排行
    @GetMapping("/relateRank")
    @Operation(summary = "获取相关排行")
    public RelateRankVO getRelateRank(ProductRelateDTO dto) {
        return productService.getRelateRank(dto);
    }

    // 相关文章
    @GetMapping("/relateArticle")
    @Operation(summary = "获取相关文章")
    public List<ArticleVO> getRelateArticle(ProductRelateDTO dto) {
        return productService.getArticleList(dto);
    }

    // 相关品牌
    @GetMapping("/relateBrand")
    @Operation(summary = "获取相关品牌")
    public List<Brand> getRelateBrand(ProductRelateDTO dto) {
        if (dto.getSize() == null) {
            dto.setSize(10);
        }
        if (dto.getRankNum() == null) {
            dto.setRankNum(5);
        }
        return productService.getRelateBrand(dto);
    }

    @GetMapping("/relateCategory")
    @Operation(summary = "获取相关分类")
    public List<Category> getRelateCategory(ProductRelateDTO dto) {
        return productService.getRelateCategory(dto);
    }

    @GetMapping("/parentTree")
    @Operation(summary = "获取父级分类")
    public List<CategoryParentTreeDTO> parentTree(@RequestParam(value = "id", required = false) Integer categoryId) {
        return productService.parentTree(categoryId);
    }
}