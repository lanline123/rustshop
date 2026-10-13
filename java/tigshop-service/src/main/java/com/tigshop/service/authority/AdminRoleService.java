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
import com.tigshop.bean.dto.authority.AdminRoleDTO;
import com.tigshop.bean.dto.authority.AdminRoleListDTO;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.service.common.BaseService;

/**
 * @author Tigshop团队
 */
public interface AdminRoleService extends BaseService<AdminRole> {

    /**
     * 角色列表
     *
     * @param page 日志分页
     * @return ListResVO
     */
    Page<AdminRoleDTO> list(AdminRoleListDTO page);

    /**
     * 角色详情
     *
     * @param id 角色ID
     * @return ItemVO
     */
    AdminRoleDTO detail(Integer id);

    /**
     * 创建角色
     *
     * @param adminRoleDTO 角色信息
     * @return boolean
     */
    boolean create(AdminRoleDTO adminRoleDTO);

    /**
     * 更新角色
     *
     * @param adminRoleDTO 角色信息
     * @return boolean
     */
    boolean update(AdminRoleDTO adminRoleDTO);

    /**
     * 初始化角色
     *
     * @param merchantId 商家ID
     * @param shopId 店铺ID
     * @return 角色ID
     */
    Integer initAdminRole(Integer merchantId, Integer shopId);

    /**
     * 按店铺类型初始化系统员工角色
     *
     * @param shopId 店铺ID
     */
    void initEmployeeRoles(Integer shopId);

    /**
     * 初始化角色
     *
     * @param vendorId 供应商
     * @return AdminRole
     */
    AdminRole initVendorRole(Integer vendorId);


    /**
     * 删除角色
     * @param id
     */
    void delRole(Integer id);
}
