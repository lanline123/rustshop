package com.tigshop.service.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.finance.StatementDownload;
import com.tigshop.bean.param.finance.statement.StatementDownloadSaveParam;
import com.tigshop.bean.query.finance.StatementDownloadListPageQuery;
import com.tigshop.bean.vo.finance.statement.StatementDownloadListVO;
import com.tigshop.service.common.BaseService;

/**
 * 对账单下载接口
 *
 * @author Tigshop项目组
 * @create 2025年08月12日 10:44
 */
public interface StatementDownloadService extends BaseService<StatementDownload> {

    /**
     * 保存对账单下载信息
     * @param param 保存参数
     */
    void saveStatementDownload(StatementDownloadSaveParam param);

    /**
     * 对账单下载列表
     * @param query 查询参数
     * @return 对账单下载列表
     */
    Page<StatementDownloadListVO> getStatementDownloadList(StatementDownloadListPageQuery query);
}
