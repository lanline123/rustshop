// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.authority;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.authority.AdminUserDTO;
import com.tigshop.bean.query.authority.adminuser.AdminUserPageQuery;
import com.tigshop.bean.dto.authority.AdminUserModifyManageAccountsDTO;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 后台用户
 *
 * @author Jayce
 * @create 2024-09-27 10:34:44
 */
public interface AdminUserService extends BaseService<AdminUser> {

    /**
     * 新增
     *
     * @param adminUser 新增参数
     * @return boolean
     */
    boolean create(AdminUserDTO adminUser);

    /**
     * 列表
     */
    Page<AdminUserDTO> list(AdminUserPageQuery pageQuery);

    /**
     * 更新
     *
     * @param adminUser 更新参数
     * @return boolean
     */
    boolean update(AdminUserDTO adminUser);

    /**
     * 详情
     *
     * @param id id
     * @return ItemVO
     */
    AdminUserDTO detail(Integer id);

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return AdminUser
     */
    AdminUser getAdminUserByUsername(String username, Boolean cache);

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return AdminUser
     */
    AdminUser getAdminUserByUsername(String username, Boolean cache,Boolean isDeleteThrowException);

    /**
     * 删除方法
     *
     * @param id id
     * @return boolean
     */
    boolean adminUserDel(Integer id);

    /**
     * 根据手机号获取用户信息
     */
    AdminUser getAdminUserByUseMobile(String mobile);

    /**
     * 根据用户IdS 列表获取数据
     */
    List<AdminUser> getAdminUserByIds(List<Integer> ids);

    /**
     * 提供新增用户
     */
    AdminUser providerCreate(AdminUserDTO adminUserDTO);

    /**
     * 修改管理员账号
     *
     * @param adminUserModifyManageAccountsDTO 修改参数
     * @return boolean
     */
    boolean modifyManageAccounts(AdminUserModifyManageAccountsDTO adminUserModifyManageAccountsDTO);

    //获取商户id
    Integer getMerchantId(Integer adminId);

    /**
     * 配置
     */
    List<AdminRole> getConfig(String adminType);

    /**
     * 获取验证码
     *
     * @param type
     * @param mobile
     * @param verifyToken
     */
    void getCode(String type, String mobile, String verifyToken);

    /**
     * 获取绑定的店铺id
     *
     * @param mainAccount
     * @return
     */
    List<Integer> getBindMainShopIds(String mainAccount, Boolean onlyAdmin);

    /**
     * 获取绑定的供应商d
     *
     * @param mainAccount
     * @return
     */
    List<Integer> getBindMainVendorIds(String mainAccount, Boolean onlyAdmin);
}
