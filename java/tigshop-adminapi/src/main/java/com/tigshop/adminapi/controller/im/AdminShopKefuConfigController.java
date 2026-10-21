package com.tigshop.adminapi.controller.im;

import com.tigshop.bean.dto.im.KefuAccessConfigSaveDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.vo.im.KefuAccessConfigVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.service.im.ShopKefuConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 客服接入规则控制器
 *
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/adminapi/im/shopKefuConfig")
@Tag(name = "客服接入规则", description = "客服接入规则配置")
@Validated
@PreAuthorize("@pms.hasPermission('kefuAccessSetting')")
public class AdminShopKefuConfigController {

    @Resource
    private ShopKefuConfigService shopKefuConfigService;

    @GetMapping("/accessConfig/detail")
    @Operation(summary = "获取接入规则")
    public KefuAccessConfigVO detail(@RequestParam(value = "shopId", required = false) Integer shopId) {
        return shopKefuConfigService.getAccessConfig(resolveShopId(shopId));
    }

    @PostMapping("/accessConfig/save")
    @Operation(summary = "保存接入规则")
    public void save(@RequestBody KefuAccessConfigSaveDTO dto) {
        dto.setShopId(resolveShopId(dto.getShopId()));
        shopKefuConfigService.saveAccessConfig(dto);
    }

    private Integer resolveShopId(Integer requestedShopId) {
        if (AdminTypeEnum.ADMIN.getCode().equals(HeaderUtils.getAdminType())) {
            return requestedShopId == null ? 0 : requestedShopId;
        }
        Integer currentShopId = HeaderUtils.getShopId();
        if (currentShopId == null || currentShopId <= 0) {
            throw new GlobalException("无法获取当前店铺信息");
        }
        return currentShopId;
    }
}
