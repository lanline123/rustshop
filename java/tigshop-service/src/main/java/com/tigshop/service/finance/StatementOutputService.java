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

import com.tigshop.bean.model.finance.StatementOutput;
import com.tigshop.bean.param.finance.statement.StatementOutputSaveParam;
import com.tigshop.service.common.BaseService;

import java.util.List;


/**
 * 出账单服务接口
 *
 * @author Tigshop项目组
 * @create 2025年08月02日 10:17
 */
public interface StatementOutputService extends BaseService<StatementOutput> {
    /**
     * 保存出账单
     *
     * @param param 保存参数
     */
    void saveStatementOutput(StatementOutputSaveParam param);

    /**
     * 批量保存出账单
     *
     * @param params 批量保存参数
     */
    void batchSaveStatementOutput(List<StatementOutputSaveParam> params);

    /**
     * 入账后出账, 变更状态
     *
     * @param recordId 记录ID
     * @param recordType 记录类型
     */
    void updateStatementOutput(Integer recordId, Integer recordType);
}
