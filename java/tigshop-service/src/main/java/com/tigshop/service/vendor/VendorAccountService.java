package com.tigshop.service.vendor;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.shop.AccountListDTO;
import com.tigshop.bean.model.vendor.VendorAccount;
import com.tigshop.bean.param.vendor.VendorAccountCreateParam;
import com.tigshop.bean.param.vendor.VendorAccountUpdateParam;
import com.tigshop.bean.param.vendor.VendorAccountVO;
import com.tigshop.bean.vo.shop.AccountVO;
import com.tigshop.bean.vo.vendor.AdminVendorAccountVO;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.service.common.BaseService;

/**
 * @author Admin
 * @description 针对表【vendor_account(供应商账户表)】的数据库操作Service
 * @createDate 2025-07-16 14:55:49
 */
public interface VendorAccountService extends BaseService<VendorAccount> {

     AdminVendorAccountVO index(AccountListDTO listDTO);

     Integer getCardCount(Integer vendorId);

     void create(VendorAccountCreateParam param);

     void update(VendorAccountUpdateParam param);

     Page<VendorAccountVO> listAccount(BasePage page);

    VendorAccountVO detail(Integer id);
}
