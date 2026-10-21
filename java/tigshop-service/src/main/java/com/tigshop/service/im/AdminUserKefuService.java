package com.tigshop.service.im;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.im.AdminUserKefuCreateDTO;
import com.tigshop.bean.dto.im.AdminUserKefuUpdateDTO;
import com.tigshop.bean.model.shop.AdminUserShop;
import com.tigshop.bean.query.im.AdminUserKefuPageQuery;
import com.tigshop.bean.vo.im.AdminUserKefuVO;
import com.tigshop.bean.vo.im.KefuRoleSimpleVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 客服人员管理：底层表为 admin_user_shop，按 kefu_agent 角色过滤，负责客服子集的 CRUD。
 *
 * @author Tigshop团队
 */
public interface AdminUserKefuService extends BaseService<AdminUserShop> {

    Page<AdminUserKefuVO> listUser(AdminUserKefuPageQuery pageQuery, Integer currentShopId);

    AdminUserKefuVO detail(Integer id, Integer currentShopId);

    AdminUserKefuVO info(Integer id, Integer shopId);

    void createAdminUserKefu(AdminUserKefuCreateDTO createDTO, Integer currentShopId);

    void updateAdminUserKefu(AdminUserKefuUpdateDTO updateDTO, Integer currentShopId);

    void delKefu(Integer id, Integer adminId, Integer currentShopId);

    boolean batch(BatchDTO batchDTO, Integer currentShopId, Integer currentAdminId);

    boolean updateUsing(UpdateFieldDTO updateFieldDTO, Integer currentShopId);

    List<KefuRoleSimpleVO> roles(Integer shopId);
}
