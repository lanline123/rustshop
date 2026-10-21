package com.tigshop.adminapi.controller.panel;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.panel.*;
import com.tigshop.bean.query.panel.SalesProductDetailPageQuery;
import com.tigshop.bean.vo.panel.SalesDetailVO;
import com.tigshop.bean.vo.panel.SalesIndicatorsVO;
import com.tigshop.bean.vo.panel.SalesProductOrderItemVO;
import com.tigshop.bean.vo.panel.SalesStatisticsDataVO;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.service.panel.SalesStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 销售统计控制器
 *
 * @author Tigshop团队
 * @create 2024年12月31日 14:44
 */
@RestController
@RequestMapping("/adminapi/panel/salesStatistics")
@Tag(name = "销售统计控制器")
public class SalesStatisticsController {

    @Resource
    private SalesStatisticsService salesStatisticsService;

    @Operation(summary = "销售统计数据")
    @GetMapping("/list")
    @PreAuthorize("@pms.hasPermission('statisticsOrder')")
    public SalesStatisticsDataVO list(SalesDataDTO salesDataDTO, HttpServletResponse response) {
        salesDataDTO.setShopId(HeaderUtils.getShopId());
        return salesStatisticsService.getSalesData(salesDataDTO, response);
    }

    @Operation(summary = "销售明细")
    @GetMapping("/salesDetail")
    @PreAuthorize("@pms.hasPermission('statisticsSale')")
    public SalesDetailVO salesDetail(SalesDetailDTO salesDetailDTO) {
        // 实现销售明细逻辑
        salesDetailDTO.setShopId(HeaderUtils.getShopId());
        return salesStatisticsService.getSalesDetail(salesDetailDTO);
    }

    @Operation(summary = "销售商品明细")
    @GetMapping("/salesProductDetail")
    @PreAuthorize("@pms.hasPermission('statisticsSale')")
    public Page<SalesProductOrderItemVO> salesProductDetail(SalesProductDetailPageQuery pageQuery, HttpServletResponse response) {
        // 实现销售商品明细逻辑
        pageQuery.setShopId(HeaderUtils.getShopId());
        if (pageQuery.getSortField() == null || pageQuery.getSortField().isEmpty()) {
            pageQuery.setSortField("itemId");
            pageQuery.setSortOrder("desc");
        }
        return salesStatisticsService.getSalesProductDetail(pageQuery, response);
    }

    @Operation(summary = "销售指标")
    @GetMapping("/salesIndicators")
    @PreAuthorize("@pms.hasPermission('saleTargets')")
    public SalesIndicatorsVO salesIndicators() {
        return salesStatisticsService.getSalesIndicators();
    }

    @Operation(summary = "销售排行")
    @GetMapping("/salesRanking")
    @PreAuthorize("@pms.hasPermission('consumerRanking','statisticsGoods')")
    public Page<SalesRankingExportDTO> salesRanking(SalesRankingDTO salesRankingDTO, HttpServletResponse response) {
        // 实现销售排行逻辑
        salesRankingDTO.setShopId(HeaderUtils.getShopId());
        return salesStatisticsService.getSalesRanking(salesRankingDTO, response);
    }

}
