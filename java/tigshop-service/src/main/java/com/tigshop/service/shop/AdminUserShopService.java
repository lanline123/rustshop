// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.shop.AdminUserShopCreateDTO;
import com.tigshop.bean.query.shop.AdminUserShopPageQuery;
import com.tigshop.bean.dto.shop.AdminUserShopModifyDTO;
import com.tigshop.bean.dto.shop.AdminUserShopUpdateDTO;
import com.tigshop.bean.model.shop.AdminUserShop;
import com.tigshop.bean.vo.shop.AdminUserShopVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 店铺服务
 *
 * @author Tigshop团队
 * @create 2024年12月30日 16:30
 */
public interface AdminUserShopService extends BaseService<AdminUserShop> {
    /**
     * 创建管理员店铺
     *
     * @param createDTO 创建参数
     */
    void createAdminUserShop(AdminUserShopCreateDTO createDTO);

    /**
     * 更新管理员店铺
     * @param updateDTO 更新参数
     */
    void updateAdminUserShop(AdminUserShopUpdateDTO updateDTO, Integer currentShopId);

    /**
     * 删除
     * @param id id
     * @param adminId adminId
     */
    void delShop(Integer id, Integer adminId, Integer currentShopId);

    /**
     * 修改店铺员工
     * @param modifyDTO 修改参数
     */
    void modifyUser(AdminUserShopModifyDTO modifyDTO, Integer currentShopId);

    /**
     * 判断用户是否存在店铺
     * @param userId 用户id
     * @return boolean
     */
    boolean hasShop(Integer userId);

    /**
     * 获取店铺员工列表
     */
    Page<AdminUserShopVO> list(AdminUserShopPageQuery pageQuery, Integer currentShopId);

    /**
     * 获取详情
     * @param id id
     * @return AdminUserShopVO
     */
    AdminUserShopVO detail(Integer id, Integer currentShopId);

    /**
     * 批量操作，限定当前店铺。
     */
    boolean batch(BatchDTO batchDTO, Integer currentShopId);

    /**
     * 获取详情
     * @param id id
     * @param shopId 商户id
     * @return AdminUserShopVO
     */
    AdminUserShopVO info(Integer id, Integer shopId);

    /**
     * 获取店铺id集合（仅启用中的绑定，is_using=0；已被店铺禁用的绑定不返回）
     * @param adminId 管理员id
     * @return List
     */
    List<Integer> getShopIds(Integer adminId);

    /**
     * 设置当前用户在指定门店“登录后默认进收银台”开关
     * @param shopId 门店id
     * @param defaultPosLogin 1 是 0 否
     */
    void setPosLoginDefault(Integer shopId, Integer defaultPosLogin);

    /**
     * 记录当前用户在指定门店的上次登录端（admin/shop/store/pos）
     * @param shopId 门店id
     * @param lastLoginSystem 登录端标识
     */
    void recordLoginSystem(Integer shopId, String lastLoginSystem);

    /**
     * 获取当前用户“登录后默认进收银台”的门店id；无则返回 null
     * @param adminId 管理员id
     * @return 门店id 或 null
     */
    Integer getDefaultPosShopId(Integer adminId);

    /**
     * 获取当前用户最近一次登录端
     * @param adminId 管理员id
     * @return 登录端标识，无则空字符串
     */
    String getLastLoginSystem(Integer adminId);

}
