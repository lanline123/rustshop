// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.finance.impl;

import com.tigshop.bean.enums.finance.SettlementStatus;
import com.tigshop.bean.model.finance.StatementOutput;
import com.tigshop.bean.param.finance.statement.StatementOutputSaveParam;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.finance.StatementOutputMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.StatementOutputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 对账单服务实现类
 *
 * @author Tigshop项目组
 * @create 2025年08月02日 10:17
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatementOutputServiceImpl extends BaseServiceImpl<StatementOutputMapper, StatementOutput> implements StatementOutputService {

    @Override
    public void saveStatementOutput(StatementOutputSaveParam param) {
        StatementOutput statementOutput = new StatementOutput();
        BeanUtils.copyProperties(param, statementOutput);
        statementOutput.setGmtCreate(StringUtils.getCurrentTime());
        this.save(statementOutput);
    }

    @Override
    public void batchSaveStatementOutput(List<StatementOutputSaveParam> params) {
        List<StatementOutput> list = params.stream().map(param -> {
            StatementOutput statementOutput = new StatementOutput();
            BeanUtils.copyProperties(param, statementOutput);
            statementOutput.setGmtCreate(StringUtils.getCurrentTime());
            return statementOutput;
        }).toList();
        this.baseMapper.insert(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatementOutput(Integer recordId, Integer recordType) {
        if (recordId == null || recordId <= 0) {
            return;
        }

        // 查询是否存在该单据
        StatementOutput statementOutput = this.lambdaQuery()
                .eq(StatementOutput::getRecordId, recordId)
                .eq(StatementOutput::getRecordType, recordType)
                .one();

        if (statementOutput == null) {
            return;
        }

        // 代表该单据改为入账状态
        this.lambdaUpdate()
                .eq(StatementOutput::getRecordId, recordId)
                .eq(StatementOutput::getRecordType, recordType)
                .set(StatementOutput::getSettlementStatus, SettlementStatus.SETTLED.getCode())
                .update();
    }
}
