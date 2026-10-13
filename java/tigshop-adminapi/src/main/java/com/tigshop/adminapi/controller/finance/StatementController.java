package com.tigshop.adminapi.controller.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import com.tigshop.bean.param.finance.statement.StatementDownloadSaveParam;
import com.tigshop.bean.query.finance.StatementDownloadListPageQuery;
import com.tigshop.bean.query.finance.StatementListPageQuery;
import com.tigshop.bean.query.finance.StatementStatisticsQuery;
import com.tigshop.bean.vo.finance.statement.*;
import com.tigshop.service.finance.StatementDownloadService;
import com.tigshop.service.finance.StatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 对账单
 *
 * @author Tigshop项目组
 * @create 2025年08月12日 09:33
 */
@RestController
@Tag(name = "对账单")
@RequestMapping("/adminapi/finance/statement")
public class StatementController {
    @Resource
    private StatementService statementService;

    @Resource
    private StatementDownloadService statementDownloadService;

    @GetMapping("/getStatementList")
    @Operation(summary ="对账单列表", description = "对账单列表")
    public Page<StatementListVO> getStatementList(StatementListPageQuery query) {
        return statementService.getStatementList(query);
    }

    @PostMapping("/saveStatementDownload")
    @Operation(summary ="保存对账单下载信息", description = "保存对账单下载信息")
    public void saveStatementDownload(@RequestBody StatementDownloadSaveParam param) {
        statementDownloadService.saveStatementDownload(param);
    }

    @GetMapping("/exportStatement")
    @ResponseExcel
    @Operation(summary ="导出对账单", description = "导出对账单")
    public List<StatementExportVO> exportStatement(StatementListPageQuery query) {
        return statementService.exportStatement(query);
    }

    @GetMapping("/exportStatementStatistics")
    @ResponseExcel
    @Operation(summary ="导出对账单统计", description = "导出对账单统计")
    public List<StatementStatisticsExportVO> exportStatementStatistics(StatementStatisticsQuery query) {
        return statementService.exportStatementStatistics(query);
    }

    @GetMapping("/getStatementStatisticsList")
    @Operation(summary ="对账单统计", description = "对账单统计")
    public List<StatementStatisticsVO> getStatementStatisticsList(StatementStatisticsQuery query) {
        return statementService.getStatementStatisticsList(query);
    }

    @GetMapping("/getStatementDownloadList")
    @Operation(summary ="对账单下载列表", description = "对账单下载列表")
    Page<StatementDownloadListVO> getStatementDownloadList(StatementDownloadListPageQuery query) {
        return statementDownloadService.getStatementDownloadList(query);
    }

    @GetMapping("/getStatementQueryConfig")
    @Operation(summary ="查询字段", description = "查询字段")
    public StatementQueryContentVO getStatementQueryConfig() {
        return statementService.getStatementQueryContent();
    }
}