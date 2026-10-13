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
package com.tigshop.adminapi.controller.decorate;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.decorate.MobileCatNavCreateDTO;
import com.tigshop.bean.dto.decorate.MobileCatNavListDTO;
import com.tigshop.bean.dto.decorate.MobileCatNavUpdateDTO;
import com.tigshop.bean.vo.decorate.MobileCatNavVO;
import com.tigshop.service.decorate.MobileCatNavService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 首页分类栏控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/decorate/mobileCatNav")
@Tag(name = "首页分类栏（H5）")
public class MobileCatNavController {
    @Resource
    private MobileCatNavService mobileCatNavService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<MobileCatNavVO> list(MobileCatNavListDTO listDTO) {
        return mobileCatNavService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public MobileCatNavVO detail(@RequestParam Integer id) {
        return mobileCatNavService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('mobileCatNavModifyManage')")
    public void create(@RequestBody MobileCatNavCreateDTO createDTO) {
        mobileCatNavService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('mobileCatNavModifyManage')")
    public void update(@RequestBody MobileCatNavUpdateDTO updateDTO) {
        mobileCatNavService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('mobileCatNavModifyManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        mobileCatNavService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('mobileCatNavModifyManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        mobileCatNavService.updateField(updateField, new String[]{"is_show", "sort_order"});
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('mobileCatNavModifyManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        mobileCatNavService.batch(batchDTO);
    }
}
