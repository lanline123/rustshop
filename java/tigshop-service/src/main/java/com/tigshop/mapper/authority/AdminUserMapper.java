// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.mapper.authority;

import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 后台用户
 * @author Jayce
*  @create 2024-09-27 10:34:44
*/
public interface AdminUserMapper extends BaseMapper<AdminUser> {

    List<Integer> getBindMainShopIds(@Param("mainAccount") String mainAccount,
                                     @Param("onlyAdmin") Boolean onlyAdmin);
    List<Integer> getBindMainVendorIds(@Param("mainAccount") String mainAccount,
                                       @Param("onlyAdmin") Boolean onlyAdmin);
}

