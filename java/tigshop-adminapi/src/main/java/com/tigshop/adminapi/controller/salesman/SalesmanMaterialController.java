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
import com.tigshop.bean.param.salesman.SalesmanMaterialEditParam;
import com.tigshop.bean.param.salesman.SalesmanMaterialSaveParam;
import com.tigshop.bean.query.salesman.SalesmanMaterialPageQuery;
import com.tigshop.bean.vo.salesman.SalesmanMaterialVO;
import com.tigshop.common.constant.CheckFieldConstants;
import com.tigshop.service.salesman.SalesmanMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 分销员分组控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/salesman/material")
@Tag(name = "素材管理", description = "分销员分组功能")
@Validated
public class SalesmanMaterialController {
    @Resource
    private SalesmanMaterialService salesmanMaterialService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<SalesmanMaterialVO> list(SalesmanMaterialPageQuery query) {
        return salesmanMaterialService.list(query);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public SalesmanMaterialVO detail(@RequestParam Integer id) {
        return salesmanMaterialService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('salesmanMaterialModifyManage')")
    public void create(@RequestBody @Valid SalesmanMaterialSaveParam param) {
        salesmanMaterialService.create(param);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('salesmanMaterialModifyManage')")
    public void update(@RequestBody @Valid SalesmanMaterialEditParam param) {
        salesmanMaterialService.update(param);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('salesmanMaterialModifyManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        salesmanMaterialService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('salesmanMaterialModifyManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        salesmanMaterialService.updateField(updateField, CheckFieldConstants.SALESMAN_CONTENT_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('salesmanMaterialModifyManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        salesmanMaterialService.batch(batchDTO);
    }
}
