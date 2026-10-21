package com.tigshop.service.finance;

import com.tigshop.bean.model.finance.RecordRate;
import com.tigshop.service.common.BaseService;

/**
 * 费率记录接口
 *
 * @author Tigshop项目组
 * @create 2025年08月15日 09:45
 */
public interface RecordRateService extends BaseService<RecordRate> {
    /**
     * 保存费率记录
     *
     * @param recordId   记录ID
     * @param recordType 记录类型
     */
    void saveRecordRate(int recordId, int recordType, int shopId, int vendorId);

}
