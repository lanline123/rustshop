package com.tigshop.adminapi.controller.panel;

import com.tigshop.bean.dto.panel.AccessStatisticsDTO;
import com.tigshop.bean.vo.panel.AxisVO;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.service.panel.StatisticsAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 访问统计
 *
 * @author Tigshop团队
 * @create 2024年12月31日 14:44
 */
@RestController
@RequestMapping("/adminapi/panel/statisticsAccess")
@Tag(name = "访问统计控制器")
public class StatisticsAccessController {

    @Resource
    private StatisticsAccessService statisticsAccessService;

    /**
     * 访问统计数据
     *
     * @return AjaxResult
     */
    @GetMapping("/accessStatistics")
    @Operation(summary = "访问统计数据")
    @PreAuthorize("@pms.hasPermission('statisticsAccess')")
    public AxisVO accessStatistics(AccessStatisticsDTO accessStatisticsDTO) {
        accessStatisticsDTO.setShopId(HeaderUtils.getShopId());
        return statisticsAccessService.getAccessStatistics(accessStatisticsDTO);
    }


}
