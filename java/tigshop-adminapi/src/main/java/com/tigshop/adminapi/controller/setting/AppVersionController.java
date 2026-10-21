package com.tigshop.adminapi.controller.setting;

import com.tigshop.bean.param.settings.appversion.AppVersionEditParam;
import com.tigshop.bean.vo.setting.AppVersionDetailVO;
import com.tigshop.common.core.AjaxResult;
import com.tigshop.service.setting.AppVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * app 版本 控制器
 *
 * @author kidd
 * @since 2025/4/10 17:17
 */
@RequiredArgsConstructor
@Tag(name = "app版本管理", description = "app版本管理")
@RequestMapping("/adminapi/setting/appVersion")
@RestController
@PreAuthorize("@pms.hasPermission('appVersionManage')")
public class AppVersionController {

    private final AppVersionService appVersionService;

    @Operation(summary = "详情")
    @GetMapping("/detail")
    public AppVersionDetailVO getDetail() {
        return appVersionService.getDetail();
    }

    @Operation(summary = "执行更新操作")
    @PostMapping("/update")
    @PreAuthorize("@pms.hasPermission('appVersionUpdateManage')")
    public void update(@RequestBody @Validated AppVersionEditParam param) {
        appVersionService.update(param);
    }

    @Operation(summary = "上传更新文件")
    @PreAuthorize("@pms.hasPermission('appVersionUpdateManage')")
    @PostMapping("/uploadFile")
    public AjaxResult<String> uploadFile(@RequestParam("file") MultipartFile file) {
        AjaxResult<String> success = AjaxResult.success();
        success.setData(appVersionService.upload(file));
        return success;
    }
}
