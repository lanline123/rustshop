// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.adminapi.controller.authority;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.authority.AdminRoleDTO;
import com.tigshop.bean.dto.authority.AdminRoleListDTO;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.service.authority.AdminRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.ROLE_FIELDS;

/**
 * @author Tigshop团队
 */
@RestController
@Tag(name = "角色")
@RequestMapping("/adminapi/authority/adminRole")
@PreAuthorize("@pms.hasPermission('adminRoleManage','ShopRoleManage','vendorRoleManage')")
public class AdminRoleController {

    @Resource
    private AdminRoleService adminRoleService;

    @GetMapping("/list")
    @Operation(summary = "角色列表")
    public Page<AdminRoleDTO> list(AdminRoleListDTO adminRoleListDTO) {
        return adminRoleService.list(adminRoleListDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "角色详情")
    public AdminRoleDTO detail(@RequestParam Integer id) {
        return adminRoleService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建角色")
    @PreAuthorize("@pms.hasPermission('adminRoleUpdateManage')")
    public void create(@RequestBody AdminRoleDTO adminRoleDTO) {
        adminRoleService.create(adminRoleDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新角色")
    @PreAuthorize("@pms.hasPermission('adminRoleUpdateManage')")
    public void update(@RequestBody AdminRoleDTO adminRoleDTO) {
        adminRoleService.update(adminRoleDTO);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('adminRoleUpdateManage')")
    public void batch(@RequestBody BatchDTO batch) {
        adminRoleService.batch(batch);
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('adminRoleUpdateManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        adminRoleService.updateField(updateField, ROLE_FIELDS);
    }

    @PostMapping("/del")
    @Operation(summary = "删除角色")
    @PreAuthorize("@pms.hasPermission('adminRoleDelManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        adminRoleService.delRole(operateDTO.getId());
    }
}
