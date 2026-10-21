package com.tigshop.service.panel;

import com.tigshop.bean.dto.panel.AccessStatisticsDTO;
import com.tigshop.bean.vo.panel.AxisVO;
import org.springframework.stereotype.Service;

/**
 * 面板服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
@Service
public interface StatisticsAccessService {

    AxisVO getAccessStatistics(AccessStatisticsDTO accessStatisticsDTO);
}
