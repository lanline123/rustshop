package com.tigshop.api.controller.shop;

import cn.hutool.core.lang.tree.Tree;
import com.tigshop.bean.vo.shop.ShopParentTreeVO;
import com.tigshop.service.shop.ShopProductCategoryService;
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
 * 店铺分类控制器
 * @author Jayce
 * @create 2024/9/30 15:56
 */
@RestController
@RequestMapping("/api/shop/category")
@Tag(name = "店铺分类")
@Validated
public class ShopClientCategoryController {
    @Resource
    private ShopProductCategoryService shopProductCategoryService;
    @GetMapping("/tree")
    @Operation(summary = "获取分列表")
    public List<Tree<Integer>> tree(@RequestParam("shopId") Integer shopId) {
        return shopProductCategoryService.tree(shopId);
    }

    @GetMapping("/parentTree")
    @Operation(summary = "获取父级分类分列表")
    public List<ShopParentTreeVO> parentTree(@RequestParam("id") Integer id) {
        return shopProductCategoryService.parentTree(id);
    }

}
