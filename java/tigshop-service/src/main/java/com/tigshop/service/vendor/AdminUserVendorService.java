package com.tigshop.service.vendor;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.vendor.AdminUserVendor;
import com.tigshop.bean.param.vendor.AdminUserVendorCreateParam;
import com.tigshop.bean.param.vendor.AdminUserVendorModifyParam;
import com.tigshop.bean.param.vendor.AdminUserVendorPageParam;
import com.tigshop.bean.param.vendor.AdminUserVendorUpdateParam;
import com.tigshop.bean.vo.vendor.AdminUserVendorVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
* @author Admin
* @description 针对表【admin_user_vendor(供应商员工列表)】的数据库操作Service
* @createDate 2025-07-04 13:23:56
*/
public interface AdminUserVendorService extends BaseService<AdminUserVendor> {

    /**
     * 获取供应商id列表
     * @param adminId
     * @return
     */
    List<Integer> getVendorIds(int adminId);

    /**
     * 获取供应商员工列表
     * @return
     */
    AdminUserVendorVO info(Integer id, Integer vendorId);

    /**
     * 账户信息
     * @param modifyParam
     */
    void modifyUser(AdminUserVendorModifyParam modifyParam);

    /**
     * 获取列表
     * @param pageQuery
     * @return
     */
    Page<AdminUserVendorVO> list(AdminUserVendorPageParam pageQuery);

    /**
     * 详情
     * @param id
     * @return
     */
    AdminUserVendorVO detail(Integer id);

    /**
     * 创建供应商员工
     * @param createParam
     */
    void createAdminUserVendor(AdminUserVendorCreateParam createParam);

    /**
     * 修改供应商员工
     * @param updateParam
     */
    void updateAdminUserVendor(AdminUserVendorUpdateParam updateParam);

    /**
     * 删除供应商员工
     * @param id
     */
    void delVendor(Integer id, Integer adminId);
}
