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

//**---------------------------------------------------------------------+
//** 服务层文件 -- 权限
//**---------------------------------------------------------------------+
//** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
//**---------------------------------------------------------------------+
//** 作者：Tigshop团队，yq@tigshop.com
//**---------------------------------------------------------------------+
//** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
//**---------------------------------------------------------------------+

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.authority.AuthorityDTO;
import com.tigshop.bean.dto.authority.AuthorityListDTO;
import com.tigshop.bean.model.authority.Authority;
import com.tigshop.bean.vo.authority.AuthorityVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * @author Jayce
 * @create 2024/10/14 11:13
 */
public interface AuthorityService extends BaseService<Authority> {

    /**
     * 获取所有权限
     */
    List<Tree<Integer>> getAllAuthority(Integer type, String adminType);

    /**
     * 获取权限列表
     */
    List<Authority> getAuthority();

    /**
     * 获取权限列表
     * @param authority 查询参数
     * @return AuthorityResVO
     */
    Page<AuthorityDTO> list(AuthorityListDTO authority);

    /**
     * 获取权限父级名称
     * @param id 权限id
     * @return String
     */
    String getAuthorityParentName(Integer id);

    /**
     * 创建权限
     * @param authority 权限
     * @return boolean
     */
    boolean create(AuthorityDTO authority);

    /**
     * 更新权限
     * @param authority 权限
     * @return boolean
     */
    boolean update(AuthorityDTO authority);

    /**
     * 获取权限详情
     * @param id 权限id
     * @return AuthorityItemVO
     */
    AuthorityDTO detail(Integer id);

    /**
     * 根据权限名称获取权限
     *
     * @param name 权限名称
     * @return list
     */
    List<AuthorityVO> getAuthorityByName(String name);

    /**
     * 批量删除权限（事务）
     * @param ids 权限ID列表
     */
    void batchDel(List<Integer> ids);

}
