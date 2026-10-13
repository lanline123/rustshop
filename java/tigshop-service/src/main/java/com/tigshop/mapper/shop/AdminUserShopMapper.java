package com.tigshop.mapper.shop;

import com.tigshop.bean.model.shop.AdminUserShop;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 店铺员工列表映射
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:03
 */
public interface AdminUserShopMapper extends BaseMapper<AdminUserShop> {
    Long selectShopCountByAdminIdAndType(@Param("adminId") Integer adminId,
                                         @Param("shopType") int shopType);
}
