package com.tigshop.service.vendor;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.vendor.VendorAccountLog;
import com.tigshop.bean.query.vendor.AccountListPageQuery;
import com.tigshop.bean.vo.vendor.AccountVO;
import com.tigshop.bean.vo.vendor.VendorAccountLogVO;
import com.tigshop.service.common.BaseService;

import java.math.BigDecimal;

/**
* @author Admin
* @description 针对表【vendor_account_log(供应商资金变化)】的数据库操作Service
* @createDate 2025-07-16 14:30:30
*/
public interface VendorAccountLogService extends BaseService<VendorAccountLog> {

    AccountVO index();

    Page<VendorAccountLogVO> logList(AccountListPageQuery pageQuery);

    void addWithDrawLog(Integer vendorId, BigDecimal amount);

    void refundWithDrawLog(Integer vendorId, BigDecimal amount);

    void completeWithDrawLog(Integer vendorId, BigDecimal amount);
}
