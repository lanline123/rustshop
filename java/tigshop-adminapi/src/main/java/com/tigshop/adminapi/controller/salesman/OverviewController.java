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

import com.tigshop.bean.vo.salesman.OverviewCoreSummaryVO;
import com.tigshop.bean.vo.salesman.OverviewCoreTrendVO;
import com.tigshop.service.salesman.OverviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分销概览控制器
 *
 * @author kidd
 * @since 2025/6/23
 */
@Tag(name = "分销概览控制器")
@RequiredArgsConstructor
@RestController
@RequestMapping("/adminapi/salesman/overview")
public class OverviewController {

    private final OverviewService overviewService;

    @Operation(summary = "获取列表")
    @GetMapping("/coreTrend")
    public OverviewCoreTrendVO coreTrend(Integer dateType, String startEndTime) {
        return overviewService.coreTrend(dateType, startEndTime);
    }

    @Operation(summary = "获取详情")
    @GetMapping("/coreSummary")
    public OverviewCoreSummaryVO coreSummary(Integer summaryType) {
        return overviewService.coreSummary(summaryType);
    }

}
