package com.tigshop.adminapi.controller.setting;

import com.tigshop.bean.param.settings.config.HuifuSettingsSaveParam;
import com.tigshop.bean.vo.setting.config.HuifuSettingsVO;
import com.tigshop.service.pay.HuifuConfigService;
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
 * 汇付斗拱支付设置控制器（VORT 门控 common.pay.huifuPay 专属，整文件可删）。
 *
 * <p>路由 path 与原共享 {@code ConfigController} 保持一致，前端无感知。</p>
 *
 * @author Tigshop团队
 * @since 2026/07/16
 */
@RequiredArgsConstructor
@Tag(name = "设置-汇付斗拱支付")
@RestController
@RequestMapping("/adminapi/setting/config")
public class HuifuConfigController {

    private final HuifuConfigService huifuConfigService;

    @Operation(summary = "获取系统设置-汇付斗拱支付设置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/huifuSettings")
    public HuifuSettingsVO getHuifuSettings() {
        return huifuConfigService.getHuifuSettings();
    }

    @Operation(summary = "保存系统设置-汇付斗拱支付设置")
    @PreAuthorize("@pms.hasPermission('uploadFileModifyManage')")
    @PostMapping("/saveHuifu")
    public void saveHuifuSettings(@RequestBody @Validated HuifuSettingsSaveParam param) {
        huifuConfigService.saveHuifuSettings(param);
    }
}
