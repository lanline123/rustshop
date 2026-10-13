package com.tigshop.service.finance.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.finance.StatementDownload;
import com.tigshop.bean.param.finance.statement.StatementDownloadSaveParam;
import com.tigshop.bean.query.finance.StatementDownloadListPageQuery;
import com.tigshop.bean.vo.finance.statement.StatementDownloadListVO;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.finance.StatementDownloadMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.StatementDownloadService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 对账单下载接口实现
 *
 * @author Tigshop项目组
 * @create 2025年08月12日 10:48
 */
@Service
public class StatementDownloadServiceImpl extends BaseServiceImpl<StatementDownloadMapper, StatementDownload> implements StatementDownloadService {
    @Override
    public void saveStatementDownload(StatementDownloadSaveParam param) {
        StatementDownload.builder()
                .startTime(StringUtils.dateToTimestampExample(param.getStartTime()))
                .endTime(StringUtils.dateToTimestampExample(param.getEndTime()))
                .remark(param.getRemark())
                .shopId(param.getShopId())
                .gmtCreate(StringUtils.getCurrentTime())
                .build();
    }

    @Override
    public Page<StatementDownloadListVO> getStatementDownloadList(StatementDownloadListPageQuery query) {
        Page<StatementDownload> page = this.buildSortOrder(query);
        Page<StatementDownload> statementDownloadPage = this.lambdaQuery()
                .eq(getShopId() != null, StatementDownload::getShopId, getShopId())
                .eq(getVendorId() != null, StatementDownload::getVendorId, getVendorId())
                .eq(query.getStartTime() != null, StatementDownload::getStartTime, query.getStartTime())
                .eq(query.getEndTime() != null, StatementDownload::getEndTime, query.getEndTime())
                .page(page);
        List<StatementDownloadListVO> list = statementDownloadPage.getRecords().stream().map(statementDownload -> {
            StatementDownloadListVO statementDownloadListVO = new StatementDownloadListVO();
            BeanUtils.copyProperties(statementDownload, statementDownloadListVO);
            return statementDownloadListVO;
        }).toList();
        return PageUtil.convertPage(statementDownloadPage, list);
    }
}