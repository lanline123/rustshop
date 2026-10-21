package com.tigshop.service.panel;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.panel.*;
import com.tigshop.bean.query.panel.SalesProductDetailPageQuery;
import com.tigshop.bean.vo.panel.*;
import com.tigshop.bean.vo.vendor.PanelVendorIndexVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

/**
 * 面板服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
@Service
public interface SalesStatisticsService {

    /**
     * 面板首页
     */
    PanelIndexVO getPanelIndex();

    /**
     * 按天数获取面板趋势图表数据（访问趋势 / 交易趋势）
     *
     * @param days 统计天数，仅支持 7 / 15 / 30，非法值回退 7
     */
    PanelIndexVO.PanelStatisticalData getPanelStatisticalData(int days);

    /**
     * 按天数获取供应商收益统计图表数据
     *
     * @param days 统计天数，仅支持 7 / 15 / 30，非法值回退 7
     */
    PanelVendorIndexVO.PanelStatisticalData getVendorPanelStatisticalData(int days);

    SalesStatisticsDataVO getSalesData(SalesDataDTO salesDataDTO, HttpServletResponse response);

    SalesDetailVO getSalesDetail(SalesDetailDTO salesDetailDTO);

    /**
     * 销售商品明细
     */
    Page<SalesProductOrderItemVO> getSalesProductDetail(SalesProductDetailPageQuery pageQuery, HttpServletResponse response);

    SalesIndicatorsVO getSalesIndicators();

    Page<SalesRankingExportDTO> getSalesRanking(SalesRankingDTO salesRankingDTO, HttpServletResponse response);

    SalesDetailVO.SalesStatisticsData getSalesStatisticsDetail(String[] startEndTime, Integer shopId);

    PanelVendorIndexVO getPanelVendorIndex();

}
