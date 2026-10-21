package com.tigshop.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.model.user.UserAuthorize;
import com.tigshop.mapper.user.UserAuthorizeMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.user.UserAuthorizeService;
import org.springframework.stereotype.Service;
/**
 * 用户第三方授权信息服务实现
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
@Service
public class UserAuthorizeServiceImpl extends BaseServiceImpl<UserAuthorizeMapper, UserAuthorize> implements UserAuthorizeService {
    @Override
    public boolean checkUserIsAuthorize(int userId, int authorizeType) {
        LambdaQueryWrapper<UserAuthorize> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAuthorize::getUserId, userId);
        queryWrapper.eq(UserAuthorize::getAuthorizeType, authorizeType);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper) != null;
    }
}
