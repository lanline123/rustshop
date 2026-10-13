// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.merchant.impl;
/**
 * 商家用户表服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.model.merchant.MerchantUser;
import com.tigshop.mapper.merchant.MerchantUserMapper;
import com.tigshop.service.merchant.MerchantUserService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MerchantUserServiceImpl extends BaseServiceImpl<MerchantUserMapper, MerchantUser> implements MerchantUserService {
    @Override
    public List<MerchantUser> getAdminUserIdSByMerchantId(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        ids = ids.stream().filter(id -> id != 0).collect(Collectors.toList());
        LambdaQueryWrapper<MerchantUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(MerchantUser::getAdminUserId, ids)
                .and(wrapper -> wrapper.eq(MerchantUser::getIsAdmin, 1));
        return this.list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMerchantUser(MerchantUser merchantUser, Integer adminUserId) {

        if (merchantUser.getMerchantId() == null || merchantUser.getMerchantId() == 0) {
            return;
        }

        MerchantUser dbMerchantUser = this.lambdaQuery()
                .eq(MerchantUser::getMerchantId, merchantUser.getMerchantId())
                .eq(MerchantUser::getAdminUserId, adminUserId)
                .one();

        if (dbMerchantUser != null) {
            //更新数据
            dbMerchantUser.setUserId(merchantUser.getUserId());
            dbMerchantUser.setIsAdmin(merchantUser.getIsAdmin());
            this.updateById(dbMerchantUser);
        } else {
            dbMerchantUser = MerchantUser.builder()
                    .userId(merchantUser.getUserId() != null ? merchantUser.getUserId() : 0)
                    .merchantId(merchantUser.getMerchantId())
                    .adminUserId(adminUserId)
                    .isAdmin(merchantUser.getIsAdmin() != null ? merchantUser.getIsAdmin() : 0)
                    .build();
            this.save(dbMerchantUser);
        }
    }

    @Override
    public MerchantUser getDetailByMerchantId(Integer id, boolean needAdmin) {
        //根据merchant_id查询出一条数据
        LambdaQueryWrapper<MerchantUser> queryWrapper = new LambdaQueryWrapper<>();
        if(needAdmin) {
            return this.getOne(queryWrapper.eq(MerchantUser::getMerchantId, id)
                    .and(wrapper -> wrapper.eq(MerchantUser::getIsAdmin, 1)).last("limit 1"));
        } else {
            return this.getOne(queryWrapper.eq(MerchantUser::getMerchantId, id).last("limit 1"));
        }
    }

    @Override
    public MerchantUser getDetailByAdminUserId(Integer id) {
        //根据admin_user_id查询出一条数据
        LambdaQueryWrapper<MerchantUser> queryWrapper = new LambdaQueryWrapper<>();
        return this.getOne(queryWrapper.eq(MerchantUser::getAdminUserId, id).last("limit 1"));
    }
}
