// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.adminapi.controller.setting;

import com.tigshop.bean.param.settings.config.LicensedEditParam;
import com.tigshop.bean.param.settings.config.LicensedSaveParam;
import com.tigshop.bean.vo.setting.config.LicensedSettingsVO;
import com.tigshop.service.setting.LicensedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 许可控制器
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:58
 */
@RestController
@RequestMapping("/adminapi/setting/licensed")
@Tag(name = "许可证")
public class LicensedController {
    @Resource
    private LicensedService licensedService;

    @GetMapping("/index")
    @Operation(summary = "授权信息")
    public LicensedSettingsVO getLicensedIndex() {
        return licensedService.getLicensedIndex();
    }

    @PreAuthorize("@pms.hasPermission('licensedModifyManage')")
    @PostMapping("/update")
    @Operation(summary = "更新授权")
    public void update(@RequestBody @Validated LicensedEditParam param) {
        licensedService.update(param);
    }

    @PostMapping("/saveLicensed")
    @PreAuthorize("@pms.hasPermission('licensed')")
    @Operation(summary = "保存授权")
    public void saveLicensed(@RequestBody @Validated LicensedSaveParam param) {
        licensedService.saveLicensed(param);
    }
}
