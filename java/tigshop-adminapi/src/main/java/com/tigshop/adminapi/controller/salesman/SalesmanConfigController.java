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
package com.tigshop.adminapi.controller.salesman;

import com.tigshop.bean.param.salesman.SalesmanConfigSaveParam;
import com.tigshop.bean.vo.salesman.SalesmanConfigVO;
import com.tigshop.service.salesman.SalesmanConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 分销模式设置控制器
 *
 * @author kidd
 * @since 2023-07-05
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/adminapi/salesman/config")
@Tag(name = "分销模式设置", description = "分销模式设置功能")
public class SalesmanConfigController {

    private final SalesmanConfigService salesmanConfigService;

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public SalesmanConfigVO detail(@RequestParam(value = "code") String code) {
        SalesmanConfigVO detail = salesmanConfigService.detail(code);
        if (detail.getRegisterToSalesman() == null) {
            detail.setRegisterToSalesman(0);
        }
        return detail;
    }

    @PostMapping("/save")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('salesmanConfigModifyManage')")
    public void save(@RequestParam("code") String code, @RequestBody SalesmanConfigSaveParam param) {
        salesmanConfigService.save(code, param);
    }

}
