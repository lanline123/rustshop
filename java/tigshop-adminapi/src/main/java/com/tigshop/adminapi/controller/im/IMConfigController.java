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
package com.tigshop.adminapi.controller.im;

import com.tigshop.bean.dto.im.ConfigCreateDTO;
import com.tigshop.bean.dto.im.ConfigDataDTO;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.service.im.ImConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * im配置控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("im/config/config")
@Tag(name = "im配置", description = "im配置功能")
@Validated
public class IMConfigController {
    @Resource
    private ImConfigService configService;

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public ConfigDataDTO detail(@RequestParam String code) {
        Integer shopId = HeaderUtils.getShopId();
        return configService.detail(code, shopId);
    }

    @PostMapping("/save")
    @Operation(summary = "保存")
    public void save(@Valid @RequestBody ConfigCreateDTO createDTO) {
        Integer shopId = HeaderUtils.getShopId();
        createDTO.setShopId(shopId);
        configService.create(createDTO);
    }

}
