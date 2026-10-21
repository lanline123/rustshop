// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.finance.RecordRate;
import com.tigshop.bean.model.finance.Statement;
import com.tigshop.bean.param.finance.statement.StatementSaveParam;
import com.tigshop.bean.query.finance.StatementListPageQuery;
import com.tigshop.bean.query.finance.StatementStatisticsQuery;
import com.tigshop.bean.vo.finance.statement.*;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 对账单服务接口
 *
 * @author Tigshop项目组
 * @create 2025年08月02日 10:17
 */
public interface StatementService extends BaseService<Statement> {
    /**
     * 获取对账单列表
     * @param query 查询参数
     * @return Page<Statement> 对账单列表
     */
    Page<StatementListVO> getStatementList(StatementListPageQuery query);

    /**
     * 保存对账单
     * @param param 保存参数
     */
    void saveStatement(StatementSaveParam param);

    /**
     * 获取费率
     * @param recordId 记录ID
     * @param recordType 记录类型
     * @return RecordRate
     */
    RecordRate getRecordRate(int recordId, int recordType);

    /**
     * 导出对账单
     * @param query 查询参数
     */
    List<StatementExportVO> exportStatement(StatementListPageQuery query);

    /**
     * 导出统计的对账单
     * @param query 查询参数
     * @return List<StatementStatisticsVO>
     */
    List<StatementStatisticsExportVO> exportStatementStatistics(StatementStatisticsQuery query);
    /**
     * 对账单统计
     * @param query 查询参数
     * @return List<StatementStatisticsVO> 对账单统计列表
     */
    List<StatementStatisticsVO> getStatementStatisticsList(StatementStatisticsQuery query);

    /**
     * 查询字段
     * @return StatementQueryContentVO 查询字段
     */
    StatementQueryContentVO getStatementQueryContent();
}
