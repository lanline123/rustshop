package com.tigshop.service.account;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.param.account.*;
import com.tigshop.bean.vo.account.AdminUserAccountListVO;
import com.tigshop.bean.vo.account.ShopOrVendorListVO;
import com.tigshop.bean.vo.authority.AdminUserAccountVO;
import jakarta.validation.Valid;

public interface AdminAccountService {
    /**
     * 根据店铺或供应商ID查询主账号信息
     *
     * @param id   店铺ID或供应商ID
     * @param type 类型：shop-店铺，vendor-供应商
     * @return AdminUserAccountVO
     */
    AdminUserAccountVO getMainAccountByShopOrVendorId(Integer adminId, Integer id, String type);

    /**
     * 根据主账号ID和类型获取账号列表（分页）
     *
     * @param shopOrVendorPageParam 查询参数
     * @return Page<AccountListVO>
     */
    Page<ShopOrVendorListVO> pageShopOrVendor(ShopOrVendorPageParam shopOrVendorPageParam);

    /**
     * 根据主账号ID和店铺ID/供应商ID绑定账号
     *
     * @param shopOrVendorBindParam 绑定参数
     */
    void bindMainAccount(ShopOrVendorBindParam shopOrVendorBindParam);

    AdminUser getAdmin(ShopOrVendorBindParam createParam);

    AdminUser getAdminUser(User user, ShopOrVendorBindParam createParam);

    void updateMainAccount(UpdateMainAccountParam updateMainAccountParam);

    void updateMainAccountPwd(UpdateMainAccountPwdParam updateMainAccountPwdParam);

    Page<AdminUserAccountListVO> pageAdminUser(@Valid AdminUserPageParam adminUserPageParam);

    /**
     * 软删除管理员账号
     *
     * @param adminId 管理员ID
     */
    void deleteAdminUser(Integer adminId);
}
