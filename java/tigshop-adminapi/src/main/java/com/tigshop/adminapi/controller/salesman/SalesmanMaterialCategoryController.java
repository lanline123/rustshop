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
package com.tigshop.adminapi.controller.salesman;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.param.salesman.SalesmanMaterialCategoryEditParam;
import com.tigshop.bean.param.salesman.SalesmanMaterialCategorySaveParam;
import com.tigshop.bean.query.salesman.SalesmanMaterialCategoryPageQuery;
import com.tigshop.bean.vo.salesman.SalesmanMaterialCategoryVO;
import com.tigshop.service.salesman.SalesmanMaterialCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.GENERAL_FIELDS;

/**
 * 素材分类控制器
 *
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/adminapi/salesman/category")
@Tag(name = "素材分类", description = "素材分类功能")
@Validated
public class SalesmanMaterialCategoryController {
    @Resource
    private SalesmanMaterialCategoryService salesmanMaterialCategoryService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<SalesmanMaterialCategoryVO> list(SalesmanMaterialCategoryPageQuery query) {
        return salesmanMaterialCategoryService.list(query);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public SalesmanMaterialCategoryVO detail(@RequestParam Integer id) {
        return salesmanMaterialCategoryService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('salesmanCategoryModifyManage')")
    public void create(@RequestBody  @Valid SalesmanMaterialCategorySaveParam param) {
        salesmanMaterialCategoryService.create(param);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('salesmanCategoryModifyManage')")
    public void update(@RequestBody @Valid SalesmanMaterialCategoryEditParam param) {
        salesmanMaterialCategoryService.update(param);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('salesmanCategoryModifyManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        salesmanMaterialCategoryService.delete(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        salesmanMaterialCategoryService.updateField(updateField, GENERAL_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    public void batch(@RequestBody BatchDTO batchDTO) {
        salesmanMaterialCategoryService.batch(batchDTO);
    }
}
