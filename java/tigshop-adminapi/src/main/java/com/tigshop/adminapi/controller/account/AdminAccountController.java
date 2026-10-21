// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.adminapi.controller.account;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.param.account.*;
import com.tigshop.bean.vo.account.AdminUserAccountListVO;
import com.tigshop.bean.vo.account.ShopOrVendorListVO;
import com.tigshop.bean.vo.authority.AdminUserAccountVO;
import com.tigshop.service.account.AdminAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * @author Tigshop团队
 * @create 2025/8/6 11:16
 */
@RestController
@RequestMapping("/adminapi/admin/adminAccount")
@Tag(name = "账号管理", description = "账号管理功能")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    @Operation(summary = "根据店铺或供应商ID查询主账号信息")
    @GetMapping("/getMainAccount")
    public AdminUserAccountVO getMainAccount(@Schema(description = "adminId") @RequestParam(required = false) Integer adminId,
                                             @Schema(description = "店铺ID/供应商ID") @RequestParam(required = false) Integer id,
                                             @Schema(description = "shop/vendor/store") @RequestParam(required = false) String type) {
        return adminAccountService.getMainAccountByShopOrVendorId(adminId, id, type);
    }

    @Operation(summary = "根据主账号ID和类型查询账号列表", description = "分页查询店铺或供应商列表")
    @GetMapping("/pageShopOrVendor")
    public Page<ShopOrVendorListVO> pageShopOrVendor(@Valid ShopOrVendorPageParam shopOrVendorPageParam) {
        return adminAccountService.pageShopOrVendor(shopOrVendorPageParam);
    }

    @Operation(summary = "根据主账号ID和店铺ID/供应商ID绑定账号")
    @PostMapping("/bindMainAccount")
    @PreAuthorize("@pms.hasPermission('adminAccountBindMainAccount')")
    public void bindMainAccount(@RequestBody ShopOrVendorBindParam shopOrVendorBindParam) {
        adminAccountService.bindMainAccount(shopOrVendorBindParam);
    }

    //修改主账号信息
    @Operation(summary = "修改主账号信息")
    @PostMapping("/updateMainAccount")
    @PreAuthorize("@pms.hasPermission('adminAccountUpdateManager')")
    public void updateMainAccount(@RequestBody UpdateMainAccountParam updateMainAccountParam) {
        adminAccountService.updateMainAccount(updateMainAccountParam);
    }

    //重置主账号密码
    @Operation(summary = "修改主账号密码")
    @PostMapping("/updateMainAccountPwd")
    @PreAuthorize("@pms.hasPermission('adminAccountUpdateManager')")
    public void updateMainAccountPwd(@RequestBody UpdateMainAccountPwdParam updateMainAccountPwdParam) {
        adminAccountService.updateMainAccountPwd(updateMainAccountPwdParam);
    }


    @Operation(summary = "账号管理列表", description = "账号管理列表")
    @GetMapping("/pageAdminUser")
    public Page<AdminUserAccountListVO> pageAdminUser(@Valid AdminUserPageParam adminUserPageParam) {
        adminUserPageParam.setIsDeleted(0);
        return adminAccountService.pageAdminUser(adminUserPageParam);
    }

    @Operation(summary = "删除管理员账号", description = "软删除管理员账号")
    @PostMapping("/deleteAdminUser")
    @PreAuthorize("@pms.hasPermission('adminAccountUpdateManager')")
    public void deleteAdminUser(@Schema(description = "管理员ID") @RequestParam Integer adminId) {
        adminAccountService.deleteAdminUser(adminId);
    }
}
