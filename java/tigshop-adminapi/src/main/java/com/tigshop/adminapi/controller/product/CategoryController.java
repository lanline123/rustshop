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

package com.tigshop.adminapi.controller.product;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.product.CategoryDTO;
import com.tigshop.bean.dto.product.CategoryListDTO;
import com.tigshop.bean.dto.product.MoveCatDTO;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.product.Product;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.product.CategoryService;
import com.tigshop.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.tigshop.common.constant.CheckFieldConstants.CATEGORY_FIELDS;
import static com.tigshop.common.constant.ResultTextConstants.ID_CANNOT_IS_NULL;

/**
 * 商品分类控制器
 *
 * @author Jayce
 * @create 2024年11月01日 10:13
 */
@RestController
@RequestMapping("/adminapi/product/category")
@PreAuthorize("@pms.hasPermission('categoryManage','productManage','vendorProductManage')")
@Tag(name = "商品分类")
@Validated
public class CategoryController {

    @Resource
    private CategoryService categoryService;
    @Autowired
    private ProductService productService;

    @Resource
    private MessageHelper messageHelper;

    @GetMapping("/list")
    @Operation(summary = "商品分类列表")
    public Page<CategoryDTO> list(CategoryListDTO dto) {
        return categoryService.list(dto);
    }

    @GetMapping("/getParentName")
    @Operation(summary = "获取列表")
    public String getParentName(Integer parentId) {
        return categoryService.getParentName(parentId);
    }

    @GetMapping("/detail")
    @Operation(summary = "商品分类详情")
    public CategoryDTO detail(@RequestParam @NotNull(message = ID_CANNOT_IS_NULL) Integer id) {
        return categoryService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建商品分类")
    @PreAuthorize("@pms.hasPermission('productCategoryModifyManage')")
    public void create(@RequestBody CategoryDTO dto) {
        categoryService.create(dto);
    }

    @PostMapping("/update")
    @Operation(summary = "更新商品分类")
    @PreAuthorize("@pms.hasPermission('productCategoryModifyManage')")
    public void update(@RequestBody CategoryDTO dto) {
        categoryService.update(dto);
    }

    @PostMapping("/del")
    @Operation(summary = "删除商品分类")
    @PreAuthorize("@pms.hasPermission('productCategoryModifyManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        //不能有子级
        Category one = categoryService.lambdaQuery().eq(Category::getParentId, operateDTO.getId()).last("limit 1").one();
        if (one != null) {
            throw new GlobalException(messageHelper.get("error.category.has.subcategory"));
        }

        //商品判断
        Long count = productService.lambdaQuery().eq(Product::getCategoryId, operateDTO.getId()).count();
        if (count > 0) {
            throw new GlobalException(messageHelper.get("error.category.in.use.has.products", count));
        }
        categoryService.del(operateDTO.getId());
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作商品分类")
    @PreAuthorize("@pms.hasPermission('productCategoryModifyManage')")
    public void batch(@RequestBody BatchDTO batch) {
        if ("del".equals(batch.getType())) {
            categoryService.batchDel(batch.getIds());
        } else {
            categoryService.batch(batch);
        }
    }

    @PostMapping("/updateField")
    @Operation(summary = "商品分类字段更新")
    @PreAuthorize("@pms.hasPermission('productCategoryModifyManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        categoryService.updateField(updateField, CATEGORY_FIELDS);
    }

    @GetMapping("/getAllCategory")
    @PreAuthorize("@pms.hasPermission('decorate','categoryManage','productManage','vendorProductManage')")
    @Operation(summary = "获取所有商品分类")
    public List<Tree<Integer>> getAllCategory() {
        return categoryService.getAllCategory();
    }

    @PostMapping("/moveCat")
    @Operation(summary = "移动商品分类")
    public void moveCat(@RequestBody MoveCatDTO dto) {
        categoryService.moveCat(dto);
    }
}