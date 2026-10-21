package com.tigshop.adminapi.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.param.user.ShopMemberAddParam;
import com.tigshop.bean.param.user.ShopMemberQueryPage;
import com.tigshop.bean.param.user.ShopMemberSettingParam;
import com.tigshop.bean.param.user.ShopMemberShopQueryPage;
import com.tigshop.bean.vo.user.ShopMemberListVO;
import com.tigshop.bean.vo.user.ShopMemberSettingVO;
import com.tigshop.bean.vo.user.ShopMemberShopListVO;
import com.tigshop.bean.vo.user.UserImportSuccessListVO;
import com.tigshop.common.annotation.Log;
import com.tigshop.service.user.ShopMemberService;
import com.tigshop.service.user.ShopMemberSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 店铺会员：平台总开关 + 平台端店铺列表/会员列表（只读）+ 店铺端会员列表。
 *
 * <p>店铺隔离靠 {@code HeaderUtils.getShopId()}：平台 admin=0，店铺/门店=自身 shopId。
 */
@RestController
@RequestMapping("/adminapi/user/shopMember")
@Tag(name = "店铺会员设置管理", description = "店铺会员设置管理")
@Validated
public class ShopMemberController {

    @Resource
    private ShopMemberSettingService shopMemberSettingService;

    @Resource
    private ShopMemberService shopMemberService;

    // ==================== 平台总开关（仅平台端）====================

    @GetMapping("/setting/get")
    @Operation(summary = "读取店铺会员总开关")
    @PreAuthorize("@pms.hasPermission('shopMemberSettingManage')")
    public ShopMemberSettingVO settingGet() {
        return shopMemberSettingService.get();
    }

    @PostMapping("/setting/save")
    @Operation(summary = "保存店铺会员总开关")
    @PreAuthorize("@pms.hasPermission('shopMemberSettingManage')")
    @Log("保存店铺会员总开关：#{#param.shopMemberEnabled}")
    public void settingSave(@Valid @RequestBody ShopMemberSettingParam param) {
        shopMemberSettingService.save(param);
    }

    // ==================== 平台端店铺列表（只读）====================

    @GetMapping("/shopList")
    @Operation(summary = "平台端店铺列表（含会员数）")
    @PreAuthorize("@pms.hasPermission('shopMemberManage')")
    public Page<ShopMemberShopListVO> shopList(ShopMemberShopQueryPage query) {
        return shopMemberService.shopList(query);
    }

    // ==================== 会员列表 / 详情 ====================
    // 平台端(shopMemberManage)显式传 shopId 只读查看；店铺端(shopMemberCustomerManage)按当前登录店铺查看。

    @GetMapping("/list")
    @Operation(summary = "会员列表")
    @PreAuthorize("@pms.hasPermission('shopMemberManage','shopMemberCustomerManage')")
    public Page<ShopMemberListVO> list(ShopMemberQueryPage query) {
        return shopMemberService.memberList(query);
    }

    @GetMapping("/detail")
    @Operation(summary = "会员详情")
    @PreAuthorize("@pms.hasPermission('shopMemberManage','shopMemberCustomerManage')")
    public ShopMemberListVO detail(@RequestParam(required = false) Integer shopId,
                                   @RequestParam Integer userId) {
        return shopMemberService.memberDetail(shopId, userId);
    }

    // ==================== 店铺端 添加 / 导入 客户（CR-20260708-1530）====================
    // 受平台开关双守卫：总开关冻结（assertEnabledForShop）+ 子开关（allowAdd/allowImport）。

    @PostMapping("/add")
    @Operation(summary = "店铺新增客户")
    @PreAuthorize("@pms.hasPermission('shopMemberCustomerManage')")
    @Log("店铺新增客户：#{#param.mobile}")
    public Integer add(@Valid @RequestBody ShopMemberAddParam param) {
        return shopMemberService.addMember(param);
    }

    @GetMapping("/downloadImportTemplate")
    @Operation(summary = "下载店铺客户导入模板")
    @PreAuthorize("@pms.hasPermission('shopMemberCustomerManage')")
    public void downloadImportTemplate(HttpServletResponse response) {
        shopMemberService.downloadImportTemplate(response);
    }

    @PostMapping("/import")
    @Operation(summary = "店铺导入客户")
    @PreAuthorize("@pms.hasPermission('shopMemberCustomerManage')")
    @Log("店铺导入客户")
    public List<UserImportSuccessListVO> importMembers(@RequestParam("file") MultipartFile file) {
        return shopMemberService.importMembers(file);
    }

    @PostMapping("/importErrorData")
    @Operation(summary = "导出导入失败行")
    @PreAuthorize("@pms.hasPermission('shopMemberCustomerManage')")
    public void importErrorData(@RequestParam("file") MultipartFile file, HttpServletResponse response) {
        shopMemberService.getImportErrorData(file, response);
    }


}
