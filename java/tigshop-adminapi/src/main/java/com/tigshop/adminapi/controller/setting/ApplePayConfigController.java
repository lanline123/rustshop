package com.tigshop.adminapi.controller.setting;

import com.tigshop.bean.param.settings.config.ApplePaySettingsSaveParam;
import com.tigshop.bean.vo.setting.config.ApplePaySettingsVO;
import com.tigshop.service.pay.ApplePayConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Apple Pay 设置控制器（VORT 门控 common.pay.applePay 专属，整文件可删）。
 *
 * <p>路由 path 与原共享 {@code ConfigController} 保持一致，前端无感知。</p>
 *
 * @author Tigshop团队
 * @since 2026/07/16
 */
@RequiredArgsConstructor
@Tag(name = "设置-Apple Pay支付")
@RestController
@RequestMapping("/adminapi/setting/config")
public class ApplePayConfigController {

    private final ApplePayConfigService applePayConfigService;

    @Operation(summary = "获取系统设置-Apple Pay支付设置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/applePaySettings")
    public ApplePaySettingsVO getApplePaySettings() {
        return applePayConfigService.getApplePaySettings();
    }

    @Operation(summary = "保存系统设置-Apple Pay支付设置")
    @PreAuthorize("@pms.hasPermission('uploadFileModifyManage')")
    @PostMapping("/saveApplePay")
    public void saveApplePaySettings(@RequestBody @Validated ApplePaySettingsSaveParam param) {
        applePayConfigService.saveApplePaySettings(param);
    }
}
