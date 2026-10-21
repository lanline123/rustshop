package com.tigshop.adminapi.controller.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.shop.AdminUserShopCreateDTO;
import com.tigshop.bean.query.shop.AdminUserShopPageQuery;
import com.tigshop.bean.dto.shop.AdminUserShopModifyDTO;
import com.tigshop.bean.dto.shop.AdminUserShopUpdateDTO;
import com.tigshop.bean.vo.shop.AdminUserShopVO;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.shop.AdminUserShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.tigshop.common.utils.HeaderUtils.getShopId;

/**
 * 店铺员工管理控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/merchant/adminUserShop")
@Tag(name = "店铺员工管理", description = "店铺员工管理功能")
@Validated
public class AdminUserShopController {
    @Resource
    AdminUserShopService adminUserShopService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<AdminUserShopVO> list(AdminUserShopPageQuery pageQuery){
        return adminUserShopService.list(pageQuery, getShopId());
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public AdminUserShopVO detail(@RequestParam Integer id){
        return adminUserShopService.detail(id, getShopId());
    }

    @GetMapping("/info")
    @Operation(summary = "获取店铺员工信息")
    public AdminUserShopVO info(@RequestParam Integer id){
        Integer shopId = getShopId();
        return adminUserShopService.info(id, shopId);
    }

    @GetMapping("/checkIsAdmin")
    @Operation(summary = "判断当前用户是否为当前店铺/门店的主账号")
    public Map<String, Boolean> checkIsAdmin() {
        Integer adminId = SecurityUtils.getCurrentAdminId();
        Integer shopId = getShopId();
        try {
            AdminUserShopVO adminUserShop = adminUserShopService.info(adminId, shopId);
            return Map.of("isAdmin", Integer.valueOf(1).equals(adminUserShop.getIsAdmin()));
        } catch (Exception e) {
            return Map.of("isAdmin", false);
        }
    }

    @PostMapping("/create")
    @Operation(summary = "创建店铺员工")
    @PreAuthorize("@pms.hasPermission('adminUserShopModifyManage')")
    public void create(@RequestBody AdminUserShopCreateDTO createDTO){
        Integer shopId = getShopId();
        createDTO.setShopId(shopId);
        createDTO.setIsAdmin(0);
        adminUserShopService.createAdminUserShop(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新店铺员工")
    @PreAuthorize("@pms.hasPermission('adminUserShopModifyManage')")
    public void update(@RequestBody AdminUserShopUpdateDTO updateDTO){
        Integer shopId = getShopId();
        updateDTO.setShopId(shopId);
        adminUserShopService.updateAdminUserShop(updateDTO, shopId);
    }

    @PostMapping("/del")
    @Operation(summary = "删除店铺员工")
    @PreAuthorize("@pms.hasPermission('adminUserShopModifyManage')")
    public void del(@RequestBody OperateDTO operateDTO){
        Integer adminId = SecurityUtils.getCurrentAdminId();
        adminUserShopService.delShop(operateDTO.getId(), adminId, getShopId());
    }

    @PostMapping("/batch")
    @Operation(summary = "批量删除店铺员工")
    @PreAuthorize("@pms.hasPermission('adminUserShopModifyManage')")
    public void batch(@RequestBody BatchDTO batchDTO){
        adminUserShopService.batch(batchDTO, getShopId());
    }

    @PostMapping("/modifyUser")
    @Operation(summary = "修改店铺员工用户")
    @PreAuthorize("@pms.hasPermission('adminUserShopModifyManage')")
    public void modifyUser(@RequestBody AdminUserShopModifyDTO modifyDTO){
        Integer shopId = getShopId();
        modifyDTO.setShopId(shopId);
        Integer adminId = SecurityUtils.getCurrentAdminId();
        modifyDTO.setAdminId(adminId);
        adminUserShopService.modifyUser(modifyDTO, shopId);
    }
}
