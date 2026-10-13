package com.tigshop.adminapi.controller.authority;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.authority.AdminApiTokenGenerateDTO;
import com.tigshop.bean.dto.authority.AdminUserDTO;
import com.tigshop.bean.dto.authority.AdminUserModifyManageAccountsDTO;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.query.authority.adminuser.AdminUserPageQuery;
import com.tigshop.bean.vo.authority.AdminApiTokenGenerateVO;
import com.tigshop.bean.vo.authority.AdminApiTokenVO;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.authority.AdminApiTokenService;
import com.tigshop.service.authority.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员列表控制器
 *
 * @author kidd
 * @since 2025/7/7
 */
@Tag(name = "管理员列表")
@RequiredArgsConstructor
@RestController
@RequestMapping("/adminapi/authority/adminUser")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final AdminApiTokenService adminApiTokenService;


    @Operation(summary = "查询管理员列表")
    @PreAuthorize("@pms.hasPermission('adminUserManage')")
    @GetMapping("/list")
    public Page<AdminUserDTO> list(AdminUserPageQuery pageQuery) {
        return adminUserService.list(pageQuery);
    }

    @Operation(summary = "当前管理员详情")
    @GetMapping("/mineDetail")
    public AdminUserDTO mineDetail() {
        return adminUserService.detail(SecurityUtils.getCurrentAdminId());
    }

    @Operation(summary = "管理员详情")
    @PreAuthorize("@pms.hasPermissionOrSelf('adminUserManage',#id)")
    @GetMapping("/detail")
    public AdminUserDTO detail(@RequestParam Integer id) {
        AdminUserDTO detail = adminUserService.detail(id);
        detail.setPassword(null);
        return detail;
    }

    @Operation(summary = "创建管理员")
    @PreAuthorize("@pms.hasPermission('adminUserUpdateManage')")
    @PostMapping("/create")
    public void create(@RequestBody @Valid AdminUserDTO adminUser) {
        adminUserService.create(adminUser);
    }

    @Operation(summary = "更新管理员")
    @PreAuthorize("@pms.hasPermission('adminUserUpdateManage')")
    @PostMapping("/update")
    public void update(@RequestBody @Valid AdminUserDTO adminUser) {
        adminUserService.update(adminUser);
    }

    @Operation(summary = "删除管理员")
    @PreAuthorize("@pms.hasPermission('adminUserDelManage')")
    @PostMapping("/del")
    public void del(@RequestBody @Valid OperateDTO operate) {
        adminUserService.adminUserDel(operate.getId());
    }

    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('adminUserBatchManage')")
    @PostMapping("/batch")
    public void batch(@RequestBody @Valid BatchDTO batch) {
        adminUserService.batch(batch);
    }

    @Operation(summary = "修改管理员账号")
    @PostMapping("/modifyManageAccounts")
    public void modifyManageAccounts(@RequestBody AdminUserModifyManageAccountsDTO adminUserModifyManageAccountsDTO) {
        if (adminUserModifyManageAccountsDTO.getModifyType() == null) {
            adminUserModifyManageAccountsDTO.setModifyType(1);
        }
        adminUserService.modifyManageAccounts(adminUserModifyManageAccountsDTO);
    }

    @Operation(summary = "配置")
    @PreAuthorize("@pms.hasPermission('adminUserManage','shopAdminUserManageConfig','EmployeeManagement')")
    @GetMapping("/config")
    public List<AdminRole> getConfig(@RequestParam(name = "adminType", required = false) String adminType) {
        return adminUserService.getConfig(adminType);
    }

    @GetMapping("/getCode")
    @Operation(summary = "获取验证码")
    public void getCode(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String mobile,
            @RequestParam String verifyToken) {
        adminUserService.getCode(type, mobile, verifyToken);
    }

    @Operation(summary = "生成API Token")
    @PostMapping("/apiToken/generate")
    public AdminApiTokenGenerateVO generateApiToken(@RequestBody AdminApiTokenGenerateDTO dto) {
        return adminApiTokenService.generateToken(SecurityUtils.getCurrentAdminId(), dto.getName(), dto.getExpiresInDays());
    }

    @Operation(summary = "查询API Token列表")
    @GetMapping("/apiToken/list")
    public List<AdminApiTokenVO> listApiTokens() {
        return adminApiTokenService.listTokens(SecurityUtils.getCurrentAdminId());
    }

    @Operation(summary = "吊销API Token")
    @PostMapping("/apiToken/revoke")
    public void revokeApiToken(@RequestBody @Valid OperateDTO operate) {
        adminApiTokenService.revokeToken(operate.getId(), SecurityUtils.getCurrentAdminId());
    }
}
