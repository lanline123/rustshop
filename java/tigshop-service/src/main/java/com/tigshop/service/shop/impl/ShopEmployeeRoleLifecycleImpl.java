package com.tigshop.service.shop.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.model.im.ImServant;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.im.ImServantMapper;
import com.tigshop.service.shop.ShopEmployeeRoleLifecycle;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 店铺员工客服状态生命周期实现。
 */
@Service
public class ShopEmployeeRoleLifecycleImpl implements ShopEmployeeRoleLifecycle {

    private final ImServantMapper imServantMapper;

    public ShopEmployeeRoleLifecycleImpl(ImServantMapper imServantMapper) {
        this.imServantMapper = imServantMapper;
    }

    @Override
    public void ensureKefu(Integer adminId, Integer shopId) {
        if (adminId == null || shopId == null) {
            return;
        }
        long count = imServantMapper.selectCount(scopedWrapper(adminId, shopId));
        if (count > 0) {
            return;
        }
        ImServant servant = new ImServant();
        servant.setServantId(adminId);
        servant.setShopId(shopId);
        servant.setStatus(3);
        servant.setConversationNum(0L);
        servant.setAddTime(StringUtils.getCurrentTime());
        servant.setLastUpdateTime(StringUtils.getCurrentTime());
        try {
            imServantMapper.insert(servant);
        } catch (DuplicateKeyException ignored) {
            // 可选唯一索引已安装时，并发确保由数据库完成幂等收敛。
        }
    }

    @Override
    public void removeKefu(Integer adminId, Integer shopId) {
        if (adminId == null || shopId == null) {
            return;
        }
        imServantMapper.delete(scopedWrapper(adminId, shopId));
    }

    @Override
    public void disableKefu(Integer adminId, Integer shopId) {
        if (adminId == null || shopId == null) {
            return;
        }
        ImServant servant = new ImServant();
        servant.setStatus(3);
        servant.setLastUpdateTime(StringUtils.getCurrentTime());
        imServantMapper.update(servant, scopedWrapper(adminId, shopId));
    }

    private LambdaQueryWrapper<ImServant> scopedWrapper(Integer adminId, Integer shopId) {
        return new LambdaQueryWrapper<ImServant>()
                .eq(ImServant::getServantId, adminId)
                .eq(ImServant::getShopId, shopId);
    }
}
