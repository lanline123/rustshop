package com.tigshop.adminapi.controller.panel;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.panel.UserConsumptionRankingDTO;
import com.tigshop.bean.dto.panel.UserStatisticsPanelDTO;
import com.tigshop.bean.query.panel.UserTrendsQuery;
import com.tigshop.bean.vo.panel.AxisVO;
import com.tigshop.bean.vo.panel.UserConsumptionRankingVO;
import com.tigshop.bean.vo.panel.UserStatisticsPanelVO;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.service.panel.StatisticsUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/adminapi/panel/statisticsUser")
@Tag(name = "访问统计控制器")
public class StatisticsUserController {

    @Resource
    private StatisticsUserService statisticsUserService;

    /**
     * 新增会员趋势
     *
     * @return AjaxResult
     */
    @GetMapping("/addUserTrends")
    @Operation(summary = "新增会员趋势")
    public AxisVO addUserTrends(@Validated UserTrendsQuery query, HttpServletResponse response) {
        return statisticsUserService.getAddUserTrends(query, response);
    }

    /**
     * 会员消费排行
     *
     * @return AjaxResult
     */
    @GetMapping("/userConsumptionRanking")
    @Operation(summary = "会员消费排行")
    public Page<UserConsumptionRankingVO> userConsumptionRanking(UserConsumptionRankingDTO userConsumptionRankingDTO, HttpServletResponse response) {
        userConsumptionRankingDTO.setShopId(HeaderUtils.getShopId());
        return statisticsUserService.getUserConsumptionRanking(userConsumptionRankingDTO, response);
    }

    /**
     * 用户统计面板
     *
     * @return AjaxResult
     */
    @GetMapping("/userStatisticsPanel")
    @Operation(summary = "用户统计面板")
    public UserStatisticsPanelVO userStatisticsPanel(UserStatisticsPanelDTO userStatisticsPanelDTO, HttpServletResponse response) {
        userStatisticsPanelDTO.setShopId(HeaderUtils.getShopId());
        return statisticsUserService.getUserStatisticsPanel(userStatisticsPanelDTO, response);
    }

}
