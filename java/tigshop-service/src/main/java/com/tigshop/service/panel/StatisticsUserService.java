package com.tigshop.service.panel;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.panel.UserConsumptionRankingDTO;
import com.tigshop.bean.dto.panel.UserStatisticsPanelDTO;
import com.tigshop.bean.query.panel.UserTrendsQuery;
import com.tigshop.bean.vo.panel.AxisVO;
import com.tigshop.bean.vo.panel.UserConsumptionRankingVO;
import com.tigshop.bean.vo.panel.UserStatisticsPanelVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

/**
 * 面板服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
@Service
public interface StatisticsUserService {

    UserStatisticsPanelVO getUserStatisticsPanel(UserStatisticsPanelDTO userStatisticsPanelDTO, HttpServletResponse response);

    AxisVO getAddUserTrends(UserTrendsQuery query, HttpServletResponse response);

    Page<UserConsumptionRankingVO> getUserConsumptionRanking(UserConsumptionRankingDTO userConsumptionRankingDTO, HttpServletResponse response);
}
