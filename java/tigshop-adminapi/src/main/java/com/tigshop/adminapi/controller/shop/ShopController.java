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
package com.tigshop.adminapi.controller.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.shop.*;
import com.tigshop.bean.enums.o2o.ShopTypeEnum;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.param.login.LoginChooseParam;
import com.tigshop.bean.param.vendor.ShopVendorSettingUpdateParam;
import com.tigshop.bean.vo.o2o.StoreListVO;
import com.tigshop.bean.vo.shop.ShopVO;
import com.tigshop.bean.vo.shop.StaffShowVO;
import com.tigshop.bean.vo.vendor.ShopVendorSettingVO;
import com.tigshop.bean.vo.vendor.VendorVO;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.shop.AdminUserShopService;
import com.tigshop.service.shop.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.tigshop.common.constant.CheckFieldConstants.GENERAL_FIELDS;

/**
 * 店铺表控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/merchant/shop")
@Tag(name = "店铺信息")
@Validated
public class ShopController {
    @Resource
    private ShopService shopService;
    @Resource
    private AdminUserService adminUserService;
    @Resource
    private AdminUserShopService userShopService;
    @Resource
    private TigshopProperties tigshopProperties;


    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<ShopVO> list(ShopListDTO listDTO) {
        return shopService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public ShopVO detail(@RequestParam("shopId") Integer id) {
        return shopService.detailWithScore(id);
    }

    @GetMapping("currentDetail")
    @Operation(summary = "获取当前登录用户的店铺信息")
    public ShopVO currentShop() {
        Integer shopId = HeaderUtils.getShopId();
        shopService.lambdaUpdate()
                .eq(Shop::getShopId, shopId)
                .set(Shop::getLastLoginTime, StringUtils.getCurrentTime())
                .update();
        return shopService.detail(shopId, true);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('merchantShopModifyManage','storeSelfPackListUpdateManage','storeListUpdateManage')")
    public void create(@Valid @RequestBody ShopCreateDTO createDTO) {
        shopService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('merchantShopModifyManage','storeSelfPackListUpdateManage','storeListUpdateManage')")
    public void update(@RequestBody ShopUpdateDTO updateDTO) {
        shopService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    public void del(@RequestBody OperateDTO operateDTO) {
        shopService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('merchantShopModifyManage','storeSelfPackListUpdateManage','storeListUpdateManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        shopService.updateField(updateField, GENERAL_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    public void batch(@RequestBody BatchDTO batchDTO) {
        shopService.batch(batchDTO);
    }

    @GetMapping("myShop")
    @Operation(summary = "获取当前登录用户的店铺信息")
    public Map<String, Object> myShop(ShopListDTO dto) {
        int adminId = SecurityUtils.getCurrentAdminId();
        // 查询用户店铺
        List<Integer> shopIds = userShopService.getShopIds(adminId);
        Page<ShopVO> shopPage = new Page<>();
        if (!shopIds.isEmpty()) {
            dto.setIsMyShop(1);
            dto.setShopIds(shopIds);
            // 多门店(O2O)版本无「店铺」类型，只返回门店/自提点；其余版本只返回店铺
            dto.setShopTypes(tigshopProperties.getIsO2o() == 1
                    ? List.of(ShopTypeEnum.STORE.getCode(), ShopTypeEnum.PICKUP.getCode())
                    : List.of(ShopTypeEnum.SHOP.getCode()));
            dto.setSortField("lastLoginTime");
            dto.setSortOrder("desc");
            dto.setSize(100);
            shopPage = shopService.list(dto);
        }
        Page<VendorVO> vendorPage = new Page<>();
        // 收银台登录偏好：defaultPosShopId>0 时前端可自动进该门店收银台；lastLoginSystem 用于记忆上次登录端
        Integer defaultPosShopId = userShopService.getDefaultPosShopId(adminId);
        return Map.of("shop", shopPage, "vendor", vendorPage,
                "userinfo", adminUserService.detail(adminId),
                "defaultPosShopId", defaultPosShopId == null ? 0 : defaultPosShopId,
                "lastLoginSystem", userShopService.getLastLoginSystem(adminId));
    }

    @PostMapping("setPosLoginDefault")
    @Operation(summary = "设置登录后默认进收银台")
    public void setPosLoginDefault(@RequestBody PosLoginSettingDTO dto) {
        userShopService.setPosLoginDefault(dto.getShopId(), dto.getDefaultPosLogin());
    }

    @PostMapping("recordLoginSystem")
    @Operation(summary = "记录上次登录端")
    public void recordLoginSystem(@RequestBody PosLoginSettingDTO dto) {
        userShopService.recordLoginSystem(dto.getShopId(), dto.getLastLoginSystem());
    }

    @Deprecated
    @PostMapping("choose")
    @Operation(summary = "选择管理员、店铺、供应商或门店登录")
    public void choose(@RequestBody LoginChooseParam loginChooseParam) {
        // 根据选择，查询权限保存到 redis ；key 为 admin::auth::[[admin_id]]
        shopService.choose(loginChooseParam);
    }

    @GetMapping("shopList")
    @Operation(summary = "获取店铺标题和名称")
    public List<Shop> shopList(ShopListDTO dto) {
        return shopService.getShopList(dto);
    }

    @GetMapping("staffShow")
    @Operation(summary = "获取员工展示信息")
    public StaffShowVO staffShow() {
        return shopService.staffShow(HeaderUtils.getShopId());
    }

    @PostMapping("updateInfo")
    @Operation(summary = "更新店铺信息")
    @PreAuthorize("@pms.hasPermission('merchantShopModifyManage','storeSelfPackListUpdateManage','storeListUpdateManage','shopSettingsUpdateManage')")
    public Shop updateInfo(@Valid @RequestBody ShopUpdateInfoDTO dto) {
        return shopService.updateInfo(dto);
    }

    @PostMapping("/setting")
    @Operation(summary = "更新配置")
    @PreAuthorize("@pms.hasPermission('merchantShopModifyManage','storeSelfPackListUpdateManage','storeListUpdateManage')")
    public void setting(@RequestBody ShopSettingDTO shopSettingDTO) {
        shopService.setting(shopSettingDTO);
    }

    @GetMapping("/getVendorSetting")
    @Operation(summary = "获取供应商配置")
    @PreAuthorize("@pms.hasPermission('merchantShopModifyManage','storeSelfPackListUpdateManage','storeListUpdateManage','vendorSetting')")
    public ShopVendorSettingVO getVendorSetting() {
        return shopService.getVendorSetting();
    }

    @PostMapping("/updateVendorSetting")
    @Operation(summary = "保存供应商配置")
    @PreAuthorize("@pms.hasPermission('merchantShopModifyManage','storeSelfPackListUpdateManage','storeListUpdateManage')")
    public void updateVendorSetting(@RequestBody ShopVendorSettingUpdateParam param) {
        shopService.updateVendorSetting(param);
    }


    @GetMapping("/getStoreListByProductId")
    @Operation(summary = "获取商品对应的门店列表")
    public List<StoreListVO> getStoreListByProductId(@RequestParam Integer productId) {
        return shopService.getStoreListByProductId(productId);
    }

    @Operation(summary = "删除店铺/门店/自提点")
    @PostMapping("/delShop")
    @PreAuthorize("@pms.hasPermission('merchantShopModifyManage','storeSelfPackListUpdateManage','storeListUpdateManage')")
    public boolean delShop(@RequestBody OperateDTO operateDTO) {
        return shopService.delShop(operateDTO);
    }

}
