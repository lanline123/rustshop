// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.api.controller.app;


import com.tigshop.bean.param.settings.appversion.AppVersionParam;
import com.tigshop.common.core.AjaxResult;
import com.tigshop.service.setting.AppVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tigshop团队
 * @create 2025/6/24 9:17
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/appVersion")
@Tag(name = "版本")
public class AppVersionController {

    private final AppVersionService appVersionService;

    @Operation(summary = "详情")
    @PostMapping("/getAppUpdate")
    public AjaxResult<String> getAppUpdate(@RequestBody AppVersionParam param) {
        AjaxResult<String> success = AjaxResult.success();
        success.setData(appVersionService.getAppUpdate(param));
        return success;
    }
}
