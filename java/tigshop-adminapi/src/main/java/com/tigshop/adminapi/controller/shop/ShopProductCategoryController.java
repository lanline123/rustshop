package com.tigshop.adminapi.controller.shop;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.shop.ShopProductCategoryCreateDTO;
import com.tigshop.bean.dto.shop.ShopProductCategoryListDTO;
import com.tigshop.bean.dto.shop.ShopProductCategoryUpdateDTO;
import com.tigshop.bean.model.shop.ShopProductCategory;
import com.tigshop.bean.vo.shop.ShopProductCategoryVO;
import com.tigshop.service.shop.ShopProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.tigshop.common.constant.CheckFieldConstants.SHOP_PRODUCT_CATEGORY_FIELDS;
import static com.tigshop.common.utils.HeaderUtils.getShopId;

/**
 * 店铺商品分类表控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/merchant/shopProductCategory")
@Tag(name = "店铺商品分类信息")
@Validated
public class ShopProductCategoryController {
    @Resource
    private ShopProductCategoryService shopProductCategoryService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<ShopProductCategoryVO> list(ShopProductCategoryListDTO listDTO) {
        Integer shopId = getShopId();
        listDTO.setShopId(shopId);
        return shopProductCategoryService.list(listDTO);
    }

    @GetMapping("/getCategoryName")
    @Operation(summary = "获取列表")
    public String getCategoryName(Integer parentId) {
        ShopProductCategory shopProductCategory = shopProductCategoryService.getById(parentId);
        if (shopProductCategory == null) {
            return "";
        }
        return shopProductCategory.getCategoryName();
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public ShopProductCategoryVO detail(@RequestParam Integer id) {
        return shopProductCategoryService.detail(id);
    }

    @GetMapping("/getAllCategory")
    @Operation(summary = "获取所有分类")
    public List<Tree<Integer>> getAllCategory() {
        Integer shopId = getShopId();
        return shopProductCategoryService.tree(shopId);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('merchantShopProductCategoryModifyManage')")
    public void create(@RequestBody ShopProductCategoryCreateDTO createDTO) {
        Integer shopId = getShopId();
        createDTO.setShopId(shopId);
        shopProductCategoryService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('merchantShopProductCategoryModifyManage')")
    public void update(@RequestBody ShopProductCategoryUpdateDTO updateDTO) {
        Integer shopId = getShopId();
        updateDTO.setShopId(shopId);
        shopProductCategoryService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('merchantShopProductCategoryModifyManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        Integer shopId = getShopId();
        shopProductCategoryService.del(operateDTO.getId(), shopId);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('merchantShopProductCategoryModifyManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        Integer shopId = getShopId();
        shopProductCategoryService.batch(batchDTO, shopId);
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('merchantShopProductCategoryModifyManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        Integer shopId = getShopId();
        shopProductCategoryService.updateField(updateField, "category_id", SHOP_PRODUCT_CATEGORY_FIELDS, shopId);
    }
}
