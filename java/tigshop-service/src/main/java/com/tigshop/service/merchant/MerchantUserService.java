// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.merchant;

import com.tigshop.bean.model.merchant.MerchantUser;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * @author Tigshop团队
 */
public interface MerchantUserService extends BaseService<MerchantUser> {
    /**
     * 根据商户id查询出admin_user_id
     * @param ids 商户id
     * @return List
     */
    List<MerchantUser> getAdminUserIdSByMerchantId(List<Integer> ids);

    /**
     * 更新优惠数据
     * @param merchantUser 商家信息
     */
    void updateMerchantUser(MerchantUser merchantUser ,Integer id);

    /**
     * 通过主键查询
     * @param id 商家id
     * @return MerchantUser
     */
    MerchantUser getDetailByMerchantId(Integer id, boolean needAdmin);

    /**
     * 通过admin_user_id查询
     * @param id 商家id
     * @return MerchantUser
     */
    MerchantUser getDetailByAdminUserId(Integer id);

}
