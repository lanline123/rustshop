package com.tigshop.adminapi.controller.im;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.im.AdminUserKefuCreateDTO;
import com.tigshop.bean.dto.im.AdminUserKefuUpdateDTO;
import com.tigshop.bean.query.im.AdminUserKefuPageQuery;
import com.tigshop.bean.vo.im.AdminUserKefuVO;
import com.tigshop.bean.vo.im.KefuRoleSimpleVO;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.im.AdminUserKefuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.tigshop.common.utils.HeaderUtils.getShopId;

/**
 * 客服人员管理控制器
 *
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/adminapi/im/adminUserKefu")
@Tag(name = "客服人员管理", description = "客服人员管理功能")
@Validated
@PreAuthorize("@pms.hasPermission('kefuManageList')")
public class AdminUserKefuController {

    @Resource
    private AdminUserKefuService adminUserKefuService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<AdminUserKefuVO> list(AdminUserKefuPageQuery pageQuery) {
        return adminUserKefuService.listUser(pageQuery, getShopId());
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public AdminUserKefuVO detail(@RequestParam Integer id) {
        return adminUserKefuService.detail(id, getShopId());
    }

    @GetMapping("/info")
    @Operation(summary = "获取客服人员信息")
    public AdminUserKefuVO info(@RequestParam Integer id) {
        Integer shopId = getShopId();
        return adminUserKefuService.info(id, shopId);
    }

    @GetMapping("/roles")
    @Operation(summary = "获取客服角色列表")
    public List<KefuRoleSimpleVO> roles() {
        Integer shopId = getShopId();
        return adminUserKefuService.roles(shopId);
    }

    @PostMapping("/create")
    @Operation(summary = "创建客服人员")
    @PreAuthorize("@pms.hasPermission('kefuManageModify')")
    public void create(@RequestBody AdminUserKefuCreateDTO createDTO) {
        Integer shopId = getShopId();
        createDTO.setShopId(shopId);
        createDTO.setAuthList(List.of("all"));
        adminUserKefuService.createAdminUserKefu(createDTO, shopId);
    }

    @PostMapping("/update")
    @Operation(summary = "更新客服人员")
    @PreAuthorize("@pms.hasPermission('kefuManageModify')")
    public void update(@RequestBody AdminUserKefuUpdateDTO updateDTO) {
        Integer shopId = getShopId();
        updateDTO.setShopId(shopId);
        updateDTO.setAuthList(List.of("all"));
        adminUserKefuService.updateAdminUserKefu(updateDTO, shopId);
    }

    @PostMapping("/del")
    @Operation(summary = "删除客服人员")
    @PreAuthorize("@pms.hasPermission('kefuManageModify')")
    public void del(@RequestBody OperateDTO operateDTO) {
        Integer adminId = SecurityUtils.getCurrentAdminId();
        adminUserKefuService.delKefu(operateDTO.getId(), adminId, getShopId());
    }

    @PostMapping("/batch")
    @Operation(summary = "批量删除客服人员")
    @PreAuthorize("@pms.hasPermission('kefuManageModify')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        adminUserKefuService.batch(batchDTO, getShopId(), SecurityUtils.getCurrentAdminId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('kefuManageModify')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        adminUserKefuService.updateUsing(updateField, getShopId());
    }
}
