package com.tigshop.adminapi.controller.setting;

import com.tigshop.bean.param.settings.config.YeepaySettingsSaveParam;
import com.tigshop.bean.vo.setting.config.YeepaySettingsVO;
import com.tigshop.service.pay.YeepayConfigService;
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
 * 易宝支付（YOP）设置控制器（VORT 门控 common.pay.yeepayPay 专属，整文件可删）。
 *
 * <p>路由 path 挂在 {@code /adminapi/setting/config}，与其它渠道配置一致，前端无感知。</p>
 *
 * @author Tigshop团队
 * @since 2026/08/06
 */
@RequiredArgsConstructor
@Tag(name = "设置-易宝支付")
@RestController
@RequestMapping("/adminapi/setting/config")
public class YeepayConfigController {

    private final YeepayConfigService yeepayConfigService;

    @Operation(summary = "获取系统设置-易宝支付设置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/yeepaySettings")
    public YeepaySettingsVO getYeepaySettings() {
        return yeepayConfigService.getYeepaySettings();
    }

    @Operation(summary = "保存系统设置-易宝支付设置")
    @PreAuthorize("@pms.hasPermission('uploadFileModifyManage')")
    @PostMapping("/saveYeepay")
    public void saveYeepaySettings(@RequestBody @Validated YeepaySettingsSaveParam param) {
        yeepayConfigService.saveYeepaySettings(param);
    }
}
