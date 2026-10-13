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
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.param.salesman.SalesmanGroupSaveParam;
import com.tigshop.bean.param.salesman.SalesmanGroupEditParam;
import com.tigshop.bean.query.salesman.SalesmanGroupPageQuery;
import com.tigshop.bean.vo.salesman.SalesmanGroupVO;
import com.tigshop.service.salesman.SalesmanGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 分销员分组控制器
 *
 * @author kidd
 * @since 2025/06/21
 */
@Tag(name = "分组标签", description = "分销员分组功能")
@RequiredArgsConstructor
@RestController
@RequestMapping("/adminapi/salesman/group")
public class SalesmanGroupController {

    private final SalesmanGroupService salesmanGroupService;

    @Operation(summary = "获取列表")
    @GetMapping("/list")
    public Page<SalesmanGroupVO> list(SalesmanGroupPageQuery pageQuery) {
        return salesmanGroupService.list(pageQuery);
    }

    @Operation(summary = "获取详情")
    @GetMapping("/detail")
    public SalesmanGroupVO detail(@RequestParam("id") Integer id) {
        return salesmanGroupService.detail(id);
    }

    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('salesmanGroupModifyManage')")
    @PostMapping("/create")
    public void create(@RequestBody @Validated SalesmanGroupSaveParam param) {
        salesmanGroupService.create(param);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('salesmanGroupModifyManage')")
    public void update(@RequestBody @Validated SalesmanGroupEditParam param) {
        salesmanGroupService.update(param);
    }

    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('salesmanGroupModifyManage')")
    @PostMapping("/del")
    public void del(@RequestBody OperateDTO operateDTO) {
        salesmanGroupService.del(operateDTO.getId());
    }

}
